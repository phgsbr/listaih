import {
  Injectable,
  NotFoundException,
  ForbiddenException,
  BadRequestException,
  Logger,
} from '@nestjs/common';
import { PrismaService } from '../../prisma/prisma.service';
import { SyncService } from '../sync/sync.service';
import { CreateListDto } from './dto/create-list.dto';
import { UpdateListDto } from './dto/update-list.dto';
import { CreateItemDto } from './dto/create-item.dto';
import { UpdateItemDto } from './dto/update-item.dto';
import { CheckoutDto } from './dto/checkout.dto';
import { UpdatePurchaseDto } from './dto/update-purchase.dto';
import { CreateProductDto } from './dto/create-product.dto';
import { AddBarcodeDto } from './dto/add-barcode.dto';
import { OffService } from './off.service';
import { GS1Parser } from './gs1.parser';
import { GrocySyncService } from '../grocy/grocy-sync.service';
import { SystemService } from '../system/system.service';
import { Prisma, ListType } from '@prisma/client';

@Injectable()
export class ListsService {
  private readonly logger = new Logger(ListsService.name);

  constructor(
    private prisma: PrismaService,
    private syncService: SyncService,
    private offService: OffService,
    private gs1Parser: GS1Parser,
    private grocySync: GrocySyncService,
    private systemService: SystemService,
  ) {}

  private async verifyHouseholdMembership(userId: string, householdId: string) {
    const membership = await this.prisma.householdMember.findUnique({
      where: { householdId_userId: { householdId, userId } },
    });
    if (!membership) {
      throw new ForbiddenException('Voce nao tem acesso a este household');
    }
    return membership;
  }

  private async verifyListAccess(userId: string, listId: string) {
    const list = await this.prisma.shoppingList.findUnique({
      where: { id: listId },
    });
    if (!list) throw new NotFoundException('Lista nao encontrada');
    await this.verifyHouseholdMembership(userId, list.householdId);
    return list;
  }

  async createList(userId: string, householdId: string, dto: CreateListDto) {
    await this.verifyHouseholdMembership(userId, householdId);
    const list = await this.prisma.shoppingList.create({
      data: {
        name: dto.name,
        householdId,
        category: dto.category,
        template: dto.template ?? false,
        listType: dto.listType ?? 'PONTUAL',
        grocyAssociated: dto.grocyAssociated ?? false,
      },
    });

    this.syncService.publish({
      householdId,
      event: 'list_created',
      data: list,
    });

    return list;
  }

  async getLists(userId: string, householdId: string) {
    await this.verifyHouseholdMembership(userId, householdId);
    return this.prisma.shoppingList.findMany({
      where: {
        householdId,
        archivedAt: null,
      },
      include: {
        items: true,
      },
      orderBy: { updatedAt: 'desc' },
    });
  }

  async getList(userId: string, listId: string) {
    const list = await this.verifyListAccess(userId, listId);
    return this.prisma.shoppingList.findUnique({
      where: { id: listId },
      include: {
        items: {
          orderBy: { position: 'asc' },
        },
      },
    });
  }

  async updateList(userId: string, listId: string, dto: UpdateListDto) {
    const existing = await this.verifyListAccess(userId, listId);
    const data: any = {};
    if (dto.name !== undefined) data.name = dto.name;
    if (dto.category !== undefined) data.category = dto.category;
    if (dto.archivedAt !== undefined) {
      data.archivedAt = dto.archivedAt ? new Date(dto.archivedAt) : null;
    }
    if (dto.listType !== undefined) data.listType = dto.listType;
    if (dto.grocyAssociated !== undefined) data.grocyAssociated = dto.grocyAssociated;
    const list = await this.prisma.shoppingList.update({
      where: { id: listId },
      data,
    });

    this.syncService.publish({
      householdId: existing.householdId,
      event: 'list_updated',
      data: list,
    });

    return list;
  }

  async deleteList(userId: string, listId: string) {
    const existing = await this.verifyListAccess(userId, listId);
    const list = await this.prisma.shoppingList.delete({ where: { id: listId } });

    this.syncService.publish({
      householdId: existing.householdId,
      event: 'list_removed',
      data: { listId },
    });

    return list;
  }

  async addItem(userId: string, listId: string, dto: CreateItemDto) {
    const list = await this.verifyListAccess(userId, listId);

    let barcode: string | undefined = dto.barcode;
    let barcodeData: Record<string, any> | undefined;
    let offData: Record<string, any> | undefined;

    // Parse barcodeRaw to extract GS1 structured data
    if (dto.barcodeRaw) {
      barcodeData = this.gs1Parser.parse(dto.barcodeRaw);
      if (barcodeData?.gtin && !barcode) {
        barcode = barcodeData.gtin;
      }
    }

    // If we have a barcode, try Open Food Facts lookup
    if (barcode && !dto.name) {
      const offProduct = await this.offService.lookupByBarcode(barcode);
      if (offProduct) {
        offData = offProduct;
        // Pre-fill name from OFF if not provided
        if (!dto.name) {
          dto.name = offProduct.product_name || offProduct.generic_name;
        }
        if (!dto.category) {
          dto.category = offProduct.categories?.split(',')[0]?.trim();
        }
      }
    }

    const maxPos = await this.prisma.listItem.aggregate({
      where: { listId },
      _max: { position: true },
    });
    const item = await this.prisma.listItem.create({
      data: {
        listId,
        addedById: userId,
        name: dto.name,
        productId: dto.productId,
        quantity: dto.quantity ?? 1,
        unit: dto.unit ?? 'unit',
        estimatedPrice: dto.estimatedPrice,
        category: dto.category,
        notes: dto.notes,
        position: dto.position ?? (maxPos._max.position ?? 0) + 1,
        barcode: barcode,
        barcodeData: barcodeData as any,
        offData: offData as any,
      },
    });

    this.syncService.publish({
      householdId: list.householdId,
      event: 'item_added',
      data: item,
    });

    return item;
  }

  /**
   * Product lookup by barcode with Open Food Facts fallback.
   * If no local Product exists, queries OFF; if OFF finds it, creates the
   * Product automatically and returns it. Returns null when unknown.
   */
  async lookupProductByBarcode(userId: string, barcode: string) {
    const normalized = (barcode ?? '').trim();
    if (!normalized) {
      throw new BadRequestException('Barcode invalido');
    }

    let product = await this.prisma.product.findUnique({
      where: { barcode: normalized },
      include: { barcodes: true },
    });

    // Try alternative barcodes table
    if (!product) {
      const alt = await this.prisma.productBarcode.findUnique({
        where: { barcode: normalized },
        include: { product: { include: { barcodes: true } } },
      });
      if (alt) product = alt.product;
    }

    // OFF fallback: create Product from Open Food Facts data
    if (!product) {
      const offProduct = await this.offService.lookupByBarcode(normalized);
      if (offProduct) {
        const name = offProduct.product_name || offProduct.generic_name;
        if (name) {
          product = await this.prisma.product.create({
            data: {
              name,
              barcode: normalized,
              category: offProduct.categories?.split(',')[0]?.trim() || undefined,
              defaultUnit: 'unit',
            },
            include: { barcodes: true },
          });
          this.logger.log(`Created Product "${name}" from OFF lookup for barcode ${normalized}`);
        }
      } else {
        this.logger.debug(`No product found for barcode ${normalized}`);
      }
    }

    return product ?? null;
  }

  /**
   * Create a new Product manually (barcode optional).
   */
  async createProduct(userId: string, dto: CreateProductDto) {
    const barcode = dto.barcode?.trim();
    if (barcode) {
      const existing = await this.prisma.product.findUnique({
        where: { barcode },
      });
      if (existing) {
        throw new BadRequestException('Produto ja cadastrado com este codigo de barras');
      }
      const existingAlt = await this.prisma.productBarcode.findUnique({
        where: { barcode },
      });
      if (existingAlt) {
        throw new BadRequestException('Codigo de barras ja associado a outro produto');
      }
    }

    const product = await this.prisma.product.create({
      data: {
        name: dto.name,
        barcode: barcode || null,
        category: dto.category,
        defaultUnit: dto.defaultUnit ?? 'unit',
      },
      include: { barcodes: true },
    });

    this.logger.log(`Product created: ${product.name} (${product.id})`);
    return product;
  }

  /**
   * Associate an alternative barcode with an existing Product
   * ("manteiga Pikachu = manteiga Sonic" case).
   */
  async addProductBarcode(userId: string, productId: string, dto: AddBarcodeDto) {
    const product = await this.prisma.product.findUnique({
      where: { id: productId },
    });
    if (!product) {
      throw new NotFoundException('Produto nao encontrado');
    }

    const barcode = dto.barcode.trim();
    if (!barcode) {
      throw new BadRequestException('Barcode invalido');
    }

    const existingMain = await this.prisma.product.findUnique({
      where: { barcode },
    });
    if (existingMain) {
      throw new BadRequestException('Codigo de barras ja cadastrado em outro produto');
    }
    const existingAlt = await this.prisma.productBarcode.findUnique({
      where: { barcode },
    });
    if (existingAlt) {
      throw new BadRequestException('Codigo de barras ja associado a outro produto');
    }

    await this.prisma.productBarcode.create({
      data: { productId, barcode },
    });

    return this.prisma.product.findUnique({
      where: { id: productId },
      include: { barcodes: true },
    });
  }

  async updateItem(userId: string, listId: string, itemId: string, dto: UpdateItemDto) {
    const list = await this.verifyListAccess(userId, listId);
    const item = await this.prisma.listItem.findUnique({
      where: { id: itemId },
    });
    if (!item || item.listId !== listId) {
      throw new NotFoundException('Item nao encontrado');
    }

    const data: any = {};
    if (dto.name !== undefined) data.name = dto.name;
    if (dto.quantity !== undefined) data.quantity = dto.quantity;
    if (dto.unit !== undefined) data.unit = dto.unit;
    if (dto.estimatedPrice !== undefined) data.estimatedPrice = dto.estimatedPrice;
    if (dto.actualPrice !== undefined) data.actualPrice = dto.actualPrice;
    if (dto.category !== undefined) data.category = dto.category;
    if (dto.notes !== undefined) data.notes = dto.notes;
    if (dto.position !== undefined) data.position = dto.position;
    if (dto.barcode !== undefined) data.barcode = dto.barcode;
    if (dto.barcodeRaw !== undefined) data.barcodeRaw = dto.barcodeRaw;
    if (dto.productId !== undefined) data.productId = dto.productId;

    if (dto.checked !== undefined) {
      data.checked = dto.checked;
      data.checkedById = dto.checked ? userId : null;
      data.checkedAt = dto.checked ? new Date() : null;

      if (dto.checked) {
        const est = dto.estimatedPrice ?? item.estimatedPrice ?? 0;
        const qty = dto.quantity ?? item.quantity;
        data.actualPrice = est * qty;
      } else {
        data.actualPrice = null;
      }
    } else if (item.checked && (dto.estimatedPrice !== undefined || dto.quantity !== undefined)) {
      const est = dto.estimatedPrice ?? item.estimatedPrice ?? 0;
      const qty = dto.quantity ?? item.quantity;
      data.actualPrice = est * qty;
    }

    const updatedItem = await this.prisma.listItem.update({
      where: { id: itemId },
      data,
    });

    this.syncService.publish({
      householdId: list.householdId,
      event: 'item_updated',
      data: updatedItem,
    });

    return updatedItem;
  }

  async deleteItem(userId: string, listId: string, itemId: string) {
    const list = await this.verifyListAccess(userId, listId);
    const item = await this.prisma.listItem.findUnique({
      where: { id: itemId },
    });
    if (!item || item.listId !== listId) {
      throw new NotFoundException('Item nao encontrado');
    }
    const deleted = await this.prisma.listItem.delete({ where: { id: itemId } });

    this.syncService.publish({
      householdId: list.householdId,
      event: 'item_removed',
      data: { listId, itemId },
    });

    return deleted;
  }

  async getHistory(userId: string, householdId: string) {
    await this.verifyHouseholdMembership(userId, householdId);
    return this.prisma.shoppingList.findMany({
      where: {
        householdId,
        archivedAt: { not: null },
      },
      include: {
        items: true,
      },
      orderBy: { archivedAt: 'desc' },
    });
  }

  /**
   * Checkout a ShoppingList — creates a Purchase snapshot,
   * handles listType behavior, and optionally syncs to Grocy
   */
  async checkout(userId: string, listId: string, dto: CheckoutDto) {
    const list = await this.verifyListAccess(userId, listId);

    if (list.listType === 'MODELO') {
      throw new BadRequestException('Não é possível fazer checkout de uma lista modelo');
    }

    const checkedItems = await this.prisma.listItem.findMany({
      where: { listId, checked: true },
    });

    if (checkedItems.length === 0) {
      throw new BadRequestException('Nenhum item marcado como comprado');
    }

    let grocySynced = false;
    let grocySyncedAt: Date | undefined;

    const config = await this.systemService.getConfig();
    if (dto.grocySync !== false && list.grocyAssociated && config.grocyEnabled) {
      try {
        await this.grocySync.loadConfig?.();
        const result = await this.grocySync.sendPurchasedItemsToStock(listId);
        grocySynced = result.success;
        grocySyncedAt = new Date();
      } catch (error: any) {
        this.logger.warn(`Grocy sync failed during checkout: ${error.message}`);
      }
    }

    // Build item snapshot for the Purchase
    const itemsSnapshot = checkedItems.map(item => ({
      id: item.id,
      name: item.name,
      quantity: item.quantity,
      unit: item.unit,
      estimatedPrice: item.estimatedPrice,
      actualPrice: item.actualPrice,
      category: item.category,
      notes: item.notes,
      barcode: item.barcode,
      barcodeData: item.barcodeData,
      checked: item.checked,
      checkedAt: item.checkedAt,
    }));

    // Calculate totalAmount from snapshot if not provided
    const calculatedTotal = itemsSnapshot.reduce((sum, item) => {
      const price = item.actualPrice ?? item.estimatedPrice ?? 0;
      return sum + price * item.quantity;
    }, 0);

    const purchase = await this.prisma.purchase.create({
      data: {
        listId,
        householdId: list.householdId,
        userId,
        totalAmount: dto.totalAmount ?? (calculatedTotal > 0 ? calculatedTotal : undefined),
        paymentMethod: dto.paymentMethod,
        notes: dto.notes,
        receiptPhoto: dto.receiptPhoto,
        receiptStatus: dto.receiptPhoto ? 'PENDING' : 'NOT_PROVIDED',
        itemCount: checkedItems.length,
        items: itemsSnapshot,
        grocySynced,
        grocySyncedAt,
      },
    });

    // Handle listType behavior AFTER creating purchase
    if (list.listType === 'RECORRENTE') {
      // Uncheck all items, reset for next cycle
      await this.prisma.listItem.updateMany({
        where: { listId, checked: true },
        data: {
          checked: false,
          checkedById: null,
          checkedAt: null,
          actualPrice: null,
        },
      });
    } else if (list.listType === 'PONTUAL') {
      // Archive the list
      const now = new Date();
      await this.prisma.shoppingList.update({
        where: { id: listId },
        data: {
          archivedAt: now,
          completedAt: now,
        },
      });
    }

    this.syncService.publish({
      householdId: list.householdId,
      event: 'checkout_completed',
      data: { purchaseId: purchase.id, listId, listType: list.listType },
    });

    return purchase;
  }

  /**
   * Get all purchases for a household (history)
   */
  async getPurchases(userId: string, householdId: string) {
    await this.verifyHouseholdMembership(userId, householdId);
    return this.prisma.purchase.findMany({
      where: { householdId },
      include: {
        list: {
          select: { id: true, name: true },
        },
      },
      orderBy: { date: 'desc' },
    });
  }

  /**
   * Get all purchases for a specific list
   */
  async getListPurchases(userId: string, listId: string) {
    const list = await this.verifyListAccess(userId, listId);
    return this.prisma.purchase.findMany({
      where: { listId },
      orderBy: { date: 'desc' },
    });
  }

  /**
   * Get a single purchase by ID
   */
  async getPurchase(userId: string, purchaseId: string) {
    const purchase = await this.prisma.purchase.findUnique({
      where: { id: purchaseId },
      include: {
        list: { select: { id: true, name: true } },
      },
    });
    if (!purchase) {
      throw new NotFoundException('Compra não encontrada');
    }
    // Verify user belongs to the household
    await this.verifyHouseholdMembership(userId, purchase.householdId);
    return purchase;
  }

  /**
   * Update a purchase (post-checkout enrichment)
   * Allows adding/editing paymentMethod, totalAmount, notes, receiptPhoto, receiptParsed, receiptStatus
   */
  async updatePurchase(userId: string, purchaseId: string, dto: UpdatePurchaseDto) {
    const purchase = await this.prisma.purchase.findUnique({
      where: { id: purchaseId },
    });
    if (!purchase) {
      throw new NotFoundException('Compra não encontrada');
    }
    await this.verifyHouseholdMembership(userId, purchase.householdId);

    const data: any = {};
    if (dto.paymentMethod !== undefined) data.paymentMethod = dto.paymentMethod;
    if (dto.totalAmount !== undefined) data.totalAmount = dto.totalAmount;
    if (dto.notes !== undefined) data.notes = dto.notes;
    if (dto.receiptPhoto !== undefined) {
      data.receiptPhoto = dto.receiptPhoto;
      if (!dto.receiptStatus) data.receiptStatus = 'PENDING';
    }
    if (dto.receiptParsed !== undefined) data.receiptParsed = dto.receiptParsed;
    if (dto.receiptStatus !== undefined) data.receiptStatus = dto.receiptStatus;

    const updated = await this.prisma.purchase.update({
      where: { id: purchaseId },
      data,
    });

    this.syncService.publish({
      householdId: purchase.householdId,
      event: 'purchase_updated',
      data: { purchaseId, updatedFields: Object.keys(data) },
    });

    return updated;
  }

  /**
   * Get stock status from Grocy for items in a list
   * Delegates to GrocySyncService for product matching and stock lookup
   */
  async getStockStatus(userId: string, listId: string) {
    const list = await this.verifyListAccess(userId, listId);

    const config = await this.systemService.getConfig();
    if (!config.grocyEnabled) {
      return { enabled: false, message: 'Grocy não configurado' };
    }

    try {
      await this.grocySync.loadConfig?.();
      const results = await this.grocySync.getStockStatus(listId);
      return results;
    } catch (error: any) {
      this.logger.warn(`Stock status query failed: ${error.message}`);
      return {
        enabled: true,
        error: error.message,
      };
    }
  }
}

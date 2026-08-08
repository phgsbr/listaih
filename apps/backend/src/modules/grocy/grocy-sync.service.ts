import { Injectable, Logger } from '@nestjs/common';
import { PrismaService } from '../../prisma/prisma.service';
import { GrocyApiClient } from './grocy-api.client';
import { SystemService } from '../system/system.service';
import { Unit } from '@prisma/client';

@Injectable()
export class GrocySyncService {
  private readonly logger = new Logger(GrocySyncService.name);

  constructor(
    private readonly prisma: PrismaService,
    private readonly grocyApi: GrocyApiClient,
    private readonly systemService: SystemService,
  ) {}

  async sendPurchasedItemsToStock(listId: string): Promise<{
    success: boolean;
    message: string;
    itemsSent: number;
    errors: string[];
  }> {
    const config = await this.systemService.getConfig();
    if (!config.grocyEnabled || !config.grocyUrl || !config.grocyApiKey) {
      return {
        success: false,
        message: 'Integração com Grocy não configurada ou desabilitada',
        itemsSent: 0,
        errors: ['Configure a URL e API Key do Grocy nas Integrações'],
      };
    }

    await this.grocyApi.loadConfig();

    const list = await this.prisma.shoppingList.findUnique({
      where: { id: listId },
      include: {
        items: {
          where: { checked: true },
        },
      },
    });

    if (!list) {
      return {
        success: false,
        message: 'Lista não encontrada',
        itemsSent: 0,
        errors: ['Lista não encontrada'],
      };
    }

    const checkedItems = list.items.filter(item => item.checked);
    if (checkedItems.length === 0) {
      return {
        success: true,
        message: 'Nenhum item marcado como comprado',
        itemsSent: 0,
        errors: [],
      };
    }

    const errors: string[] = [];
    let itemsSent = 0;

    const grocyProducts = await this.grocyApi.getProducts();
    const quantityUnits = await this.grocyApi.getQuantityUnits();

    for (const item of checkedItems) {
      try {
        let grocyProductId: number | undefined;
        let created = false;

        // Try hybrid match (barcode → name → fuzzy)
        const matched = await this.matchOrCreateProduct(item, grocyProducts, quantityUnits);

        if (matched) {
          grocyProductId = matched.productId;
        } else {
          // Create product in Grocy
          const quStock = this.getGrocyQuId(item.unit || 'unit', quantityUnits) || 1;
          const quPurchase = quStock;

          const newProduct = await this.grocyApi.createProduct({
            name: item.name,
            qu_id_stock: quStock,
            qu_id_purchase: quPurchase,
            qu_factor_purchase_to_stock: 1,
            barcode: item.barcode || undefined,
          });
          grocyProductId = newProduct.id;
          created = true;

          // Update local productBarcode map for subsequent items
          if (item.barcode) {
            grocyProducts.push({ id: grocyProductId, name: item.name, barcode: item.barcode } as any);
          }
        }

        // Convert quantity to Grocy stock unit
        const grocyProduct = grocyProducts.find(p => p.id === grocyProductId);
        let amount = item.quantity;
        if (grocyProduct && !created) {
          // Only convert if product already existed (we know its QU)
          amount = this.convertQuantity(item.quantity, item.unit || 'unit', grocyProduct, quantityUnits);
        }

        // Get expiry date from barcodeData if available (GS1 AI 17)
        let bestBeforeDate: string | undefined;
        if (item.barcodeData) {
          try {
            const bd = JSON.parse(item.barcodeData as string);
            if (bd.expiryDate) {
              bestBeforeDate = bd.expiryDate;
            }
          } catch {
            // ignore parse error
          }
        }

        // Add to stock (pantry)
        await this.grocyApi.addStockEntry({
          product_id: grocyProductId,
          amount: amount,
          quantity_unit_id: 1, // Default to stock unit
          best_before_date: bestBeforeDate,
          price: item.actualPrice || item.estimatedPrice || undefined,
          stock_label: undefined,
        });

        itemsSent++;
      } catch (error: any) {
        this.logger.error(`Failed to send item ${item.name} to Grocy: ${error.message}`);
        errors.push(`${item.name}: ${error.message}`);
      }
    }

    return {
      success: errors.length === 0,
      message: errors.length === 0
        ? `${itemsSent} itens enviados para a despensa do Grocy`
        : `${itemsSent} itens enviados, ${errors.length} falharam`,
      itemsSent,
      errors,
    };
  }

  async syncShoppingListToGrocy(listId: string): Promise<{
    success: boolean;
    message: string;
    itemsSynced: number;
    errors: string[];
  }> {
    const config = await this.systemService.getConfig();
    if (!config.grocyEnabled || !config.grocyUrl || !config.grocyApiKey) {
      return {
        success: false,
        message: 'Integração com Grocy não configurada ou desabilitada',
        itemsSynced: 0,
        errors: ['Configure a URL e API Key do Grocy nas Integrações'],
      };
    }

    await this.grocyApi.loadConfig();

    const list = await this.prisma.shoppingList.findUnique({
      where: { id: listId },
      include: { items: true },
    });

    if (!list) {
      return {
        success: false,
        message: 'Lista não encontrada',
        itemsSynced: 0,
        errors: ['Lista não encontrada'],
      };
    }

    const errors: string[] = [];
    let itemsSynced = 0;

    for (const item of list.items) {
      try {
        let grocyProductId: number | undefined;

        // Try to find by name
        const grocyProducts = await this.grocyApi.getProducts();
        const match = grocyProducts.find(p => p.name.toLowerCase() === item.name.toLowerCase());
        if (match) grocyProductId = match.id;

        if (!grocyProductId) {
          const quantityUnits = await this.grocyApi.getQuantityUnits();
          const quStock = quantityUnits.find(qu => qu.name.toLowerCase() === (item.unit || 'unit').toLowerCase())?.id || 1;
          
          const newProduct = await this.grocyApi.createProduct({
            name: item.name,
            qu_id_stock: quStock,
            qu_id_purchase: quStock,
            qu_factor_purchase_to_stock: 1,
          });
          grocyProductId = newProduct.id;
        }

        // Add to Grocy shopping list (list_id = 1 is default)
        await this.grocyApi.addShoppingListItem({
          product_id: grocyProductId,
          amount: item.quantity,
          note: item.notes || '',
          shopping_list_id: 1,
        });

        itemsSynced++;
      } catch (error: any) {
        this.logger.error(`Failed to sync item ${item.name} to Grocy: ${error.message}`);
        errors.push(`${item.name}: ${error.message}`);
      }
    }

    return {
      success: errors.length === 0,
      message: errors.length === 0 
        ? `${itemsSynced} itens sincronizados com lista de compras do Grocy` 
        : `${itemsSynced} itens sincronizados, ${errors.length} falharam`,
      itemsSynced,
      errors,
    };
  }

  async syncGrocyToLocal(): Promise<{
    success: boolean;
    message: string;
    productsImported: number;
    errors: string[];
  }> {
    const config = await this.systemService.getConfig();
    if (!config.grocyEnabled || !config.grocyUrl || !config.grocyApiKey) {
      return {
        success: false,
        message: 'Integração com Grocy não configurada ou desabilitada',
        productsImported: 0,
        errors: ['Configure a URL e API Key do Grocy nas Integrações'],
      };
    }

    await this.grocyApi.loadConfig();

    const errors: string[] = [];
    let productsImported = 0;

    try {
      const grocyProducts = await this.grocyApi.getProducts();
      const quantityUnits = await this.grocyApi.getQuantityUnits();

      for (const gp of grocyProducts) {
        try {
          const existingProduct = await this.prisma.product.findFirst({
            where: { name: gp.name },
          });

          if (!existingProduct) {
            await this.prisma.product.create({
              data: {
                name: gp.name,
                barcode: undefined,
                category: gp.product_group_id ? `group_${gp.product_group_id}` : undefined,
                defaultUnit: gp.qu_id_stock ? this.mapQuantityUnit(gp.qu_id_stock, quantityUnits) : Unit.unit,
              },
            });
            productsImported++;
          }
        } catch (error: any) {
          errors.push(`${gp.name}: ${error.message}`);
        }
      }
    } catch (error: any) {
      errors.push(`Erro ao buscar produtos do Grocy: ${error.message}`);
    }

    return {
      success: errors.length === 0,
      message: errors.length === 0 
        ? `${productsImported} produtos importados do Grocy` 
        : `${productsImported} produtos importados, ${errors.length} erros`,
      productsImported,
      errors,
    };
  }

  private mapQuantityUnit(quId: number, quantityUnits: any[]): Unit {
    const unit = quantityUnits.find(u => u.id === quId);
    if (!unit) return Unit.unit;
    
    const name = unit.name.toLowerCase();
    if (name.includes('kg') || name.includes('quilo')) return Unit.kg;
    if (name.includes('g') && !name.includes('kg')) return Unit.g;
    if (name.includes('l') && !name.includes('ml')) return Unit.L;
    if (name.includes('ml')) return Unit.ml;
    return Unit.unit;
  }

  /**
   * Refresh Grocy config from SystemConfig
   */
  async loadConfig() {
    await this.grocyApi.loadConfig();
  }

  /**
   * Get stock status for all items in a list
   * Matches items to Grocy products (by barcode, then name) and returns stock levels
   */
  async getStockStatus(listId: string): Promise<{
    enabled: boolean;
    items: Array<{
      itemId: string;
      name: string;
      barcode?: string;
      hasStock: boolean;
      stockAmount: number;
      bestBeforeDate?: string;
      inGrocy: boolean;
      productName?: string;
      grocyProductId?: number;
    }>;
  }> {
    const config = await this.systemService.getConfig();
    if (!config.grocyEnabled || !config.grocyUrl || !config.grocyApiKey) {
      return { enabled: false, items: [] };
    }

    await this.grocyApi.loadConfig();

    const list = await this.prisma.shoppingList.findUnique({
      where: { id: listId },
      include: { items: true },
    });

    if (!list) {
      return { enabled: true, items: [] };
    }

    const grocyProducts = await this.grocyApi.getProducts();
    const stockOverview = await this.grocyApi.getStockOverview();

    // Build stock map by product ID
    const stockMap = new Map<number, { amount: number; bestBeforeDate?: string }>();
    for (const entry of stockOverview) {
      const productId = entry.product_id || entry.productId;
      const stockInfo = entry.stock || (Array.isArray(entry.stock) ? entry.stock[0] : null);
      if (productId && stockInfo && stockInfo.amount !== undefined) {
        stockMap.set(productId, {
          amount: stockInfo.amount,
          bestBeforeDate: stockInfo.best_before_date,
        });
      }
    }

    // Also enrich grocyProducts with barcode info for matching
    // Grocy product barcode may be in userfields
    const productBarcodeMap = new Map<string, number>(); // barcode -> grocy product ID
    for (const p of grocyProducts) {
      if (p.barcode) {
        productBarcodeMap.set(p.barcode, p.id);
      }
    }

    // Match list items to Grocy products
    const results = list.items.map(item => {
      let grocyProductId: number | undefined;

      // 1. Match by barcode
      if (item.barcode) {
        grocyProductId = productBarcodeMap.get(item.barcode);
      }

      // 2. Match by name (case-insensitive)
      if (!grocyProductId) {
        const match = grocyProducts.find(p =>
          p.name.toLowerCase() === item.name.toLowerCase()
        );
        if (match) grocyProductId = match.id;
      }

      // 3. Fuzzy match by name (simple startsWith check)
      if (!grocyProductId) {
        const fuzzyMatch = grocyProducts.find(p =>
          p.name.toLowerCase().includes(item.name.toLowerCase()) ||
          item.name.toLowerCase().includes(p.name.toLowerCase())
        );
        if (fuzzyMatch) grocyProductId = fuzzyMatch.id;
      }

      if (grocyProductId) {
        const stock = stockMap.get(grocyProductId);
        const stockAmount = stock?.amount ?? 0;
        return {
          itemId: item.id,
          name: item.name,
          barcode: item.barcode,
          hasStock: stockAmount > 0,
          stockAmount,
          bestBeforeDate: stock?.bestBeforeDate,
          inGrocy: true,
          productName: grocyProducts.find(p => p.id === grocyProductId)?.name,
          grocyProductId,
        };
      }

      return {
        itemId: item.id,
        name: item.name,
        barcode: item.barcode,
        hasStock: false,
        stockAmount: 0,
        inGrocy: false,
      };
    });

    return { enabled: true, items: results };
  }

  /**
   * Match a list item to a Grocy product using hybrid strategy:
   * 1. By barcode (item.barcode → grocyProduct.barcode)
   * 2. By product barcode
   * 3. By exact name (case-insensitive)
   * 4. By fuzzy name (similarity >= 0.90)
   * 5. Create new product if no match
   */
  private async matchOrCreateProduct(
    item: { name: string; barcode?: string | null; quantity: number; unit: string },
    grocyProducts: any[],
    quantityUnits: any[]
  ): Promise<{ productId: number; created: boolean } | null> {
    // 1. Match by item.barcode on local Product
    if (item.barcode) {
      const localProduct = await this.prisma.product.findUnique({
        where: { barcode: item.barcode },
        select: { barcode: true },
      });
      if (localProduct?.barcode) {
        const grocyMatch = grocyProducts.find(p => p.barcode === item.barcode);
        if (grocyMatch) return { productId: grocyMatch.id, created: false };
      }
    }

    // 2. Match by grocy product barcode
    if (item.barcode) {
      const grocyMatch = grocyProducts.find(p => p.barcode === item.barcode);
      if (grocyMatch) return { productId: grocyMatch.id, created: false };
    }

    // 3. Match by exact name
    let match = grocyProducts.find(p =>
      p.name.toLowerCase() === item.name.toLowerCase().trim()
    );
    if (match) return { productId: match.id, created: false };

    // 4. Fuzzy match (simple similarity check)
    for (const p of grocyProducts) {
      const similarity = this.stringSimilarity(item.name.toLowerCase(), p.name.toLowerCase());
      if (similarity >= 0.90) return { productId: p.id, created: false };
    }

    // 5. No match found — return null, let caller decide whether to create
    return null;
  }

  /**
   * Simple string similarity using Levenshtein distance
   */
  private stringSimilarity(s1: string, s2: string): number {
    if (s1 === s2) return 1;
    if (!s1 || !s2) return 0;
    
    const len1 = s1.length;
    const len2 = s2.length;
    const matrix: number[][] = [];
    
    for (let i = 0; i <= len2; i++) matrix[i] = [i];
    for (let j = 0; j <= len1; j++) matrix[0][j] = j;
    
    for (let i = 1; i <= len2; i++) {
      for (let j = 1; j <= len1; j++) {
        const cost = s1[j - 1] === s2[i - 1] ? 0 : 1;
        matrix[i][j] = Math.min(
          matrix[i][j - 1] + 1,     // insertion
          matrix[i - 1][j] + 1,     // deletion
          matrix[i - 1][j - 1] + cost  // substitution
        );
      }
    }
    
    const maxLen = Math.max(len1, len2);
    return maxLen === 0 ? 1 : 1 - matrix[len2][len1] / maxLen;
  }

  /**
   * Convert quantity from item unit to Grocy purchase unit
   * Handles kg↔g, L↔ml conversions
   */
  private convertQuantity(
    quantity: number,
    fromUnit: Unit,
    grocyProduct: any,
    quantityUnits: any[]
  ): number {
    // If the units match, no conversion needed
    const quStockId = grocyProduct.qu_id_stock;
    const quUnit = quantityUnits.find(u => u.id === quStockId);
    if (!quUnit) return quantity;

    const grocyUnitName = quUnit.name.toLowerCase();
    const listUnit = fromUnit.toLowerCase();

    // Convert from list unit to Grocy stock unit
    if (listUnit === 'g' && grocyUnitName.includes('kg')) {
      return quantity / 1000; // g → kg
    }
    if (listUnit === 'kg' && (grocyUnitName.includes('g') && !grocyUnitName.includes('kg'))) {
      return quantity * 1000; // kg → g
    }
    if (listUnit === 'ml' && grocyUnitName.includes('l') && !grocyUnitName.includes('ml')) {
      return quantity / 1000; // ml → L
    }
    if (listUnit === 'L' && grocyUnitName.includes('ml')) {
      return quantity * 1000; // L → ml
    }

    return quantity;
  }

  /**
   * Convert QuantityUnit enum to Grocy QU ID
   */
  private getGrocyQuId(unit: Unit, quantityUnits: any[]): number | undefined {
    const unitName = unit.toLowerCase();
    const match = quantityUnits.find(qu => qu.name.toLowerCase().includes(unitName));
    return match?.id;
  }
}
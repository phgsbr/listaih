import { Injectable, Logger, OnModuleInit } from '@nestjs/common';
import { HttpService } from '@nestjs/axios';
import { firstValueFrom, catchError } from 'rxjs';
import { AxiosError } from 'axios';
import { PrismaService } from '../../prisma/prisma.service';
import { SystemService } from '../system/system.service';

interface GrocyProduct {
  id: number;
  name: string;
  description?: string;
  product_group_id?: number;
  location_id?: number;
  shopping_location_id?: number;
  qu_id_purchase?: number;
  qu_id_stock?: number;
  qu_factor_purchase_to_stock?: number;
  min_stock_amount?: number;
  default_best_before_days?: number;
  default_best_before_days_after_open?: number;
  default_best_before_days_after_freezing?: number;
  picture_file_name?: string;
  enable_tare_weight_handling?: boolean;
  tare_weight?: number;
  not_check_stock_fulfillment_for_recipes?: boolean;
  parent_product_id?: number;
  calories?: number;
  cumulate_min_stock_amount_of_sub_products?: boolean;
  due_type?: number;
  quick_consume_amount?: number;
  row_created_timestamp: string;
  userfields?: any;
  barcode?: string;
}

interface GrocyShoppingListItem {
  id: number;
  shopping_list_id: number;
  product_id: number;
  note?: string;
  amount: number;
  quantity_unit_id?: number;
  done: number;
  row_created_timestamp: string;
}

interface GrocyQuantityUnit {
  id: number;
  name: string;
  name_plural?: string;
}

interface GrocyProductGroup {
  id: number;
  name: string;
  description?: string;
  row_created_timestamp: string;
}

interface GrocyLocation {
  id: number;
  name: string;
  description?: string;
  row_created_timestamp: string;
}

@Injectable()
export class GrocyApiClient implements OnModuleInit {
  private readonly logger = new Logger(GrocyApiClient.name);
  private baseUrl: string = '';
  private apiKey: string = '';
  private enabled: boolean = false;

  constructor(
    private readonly httpService: HttpService,
    private readonly prisma: PrismaService,
    private readonly systemService: SystemService,
  ) {}

  async onModuleInit() {
    await this.loadConfig();
  }

  async loadConfig() {
    const config = await this.systemService.getConfig();
    this.baseUrl = config.grocyUrl || '';
    this.apiKey = config.grocyApiKey || '';
    this.enabled = config.grocyEnabled || false;
  }

  private getHeaders() {
    return {
      'GROCY-API-KEY': this.apiKey,
      'Content-Type': 'application/json',
    };
  }

  private async request<T>(method: 'GET' | 'POST' | 'PUT' | 'DELETE', endpoint: string, data?: any): Promise<T> {
    if (!this.enabled || !this.baseUrl || !this.apiKey) {
      throw new Error('Grocy integration not configured or disabled');
    }

    try {
      const url = `${this.baseUrl}/api/${endpoint}`;
      const response = await firstValueFrom(
        this.httpService.request<T>({
          method,
          url,
          headers: this.getHeaders(),
          data,
          timeout: 10000,
        }).pipe(
          catchError((error: AxiosError) => {
            this.logger.error(`Grocy API error (${method} ${endpoint}): ${error.message}`);
            throw error;
          })
        )
      );
      return response.data;
    } catch (error: any) {
      this.logger.error(`Grocy API error (${method} ${endpoint}): ${error.message}`);
      throw error;
    }
  }

  async testConnection(): Promise<boolean> {
    try {
      await this.request<GrocyProduct[]>('GET', 'objects/products');
      return true;
    } catch {
      return false;
    }
  }

  // Products
  async getProducts(): Promise<GrocyProduct[]> {
    return this.request<GrocyProduct[]>('GET', 'objects/products');
  }

  async getProduct(id: number): Promise<GrocyProduct> {
    return this.request<GrocyProduct>('GET', `objects/products/${id}`);
  }

  async createProduct(product: any): Promise<GrocyProduct> {
    return this.request<GrocyProduct>('POST', 'objects/products', product);
  }

  async updateProduct(id: number, product: any): Promise<GrocyProduct> {
    return this.request<GrocyProduct>('PUT', `objects/products/${id}`, product);
  }

  async deleteProduct(id: number): Promise<void> {
    await this.request<void>('DELETE', `objects/products/${id}`);
  }

  // Quantity Units
  async getQuantityUnits(): Promise<GrocyQuantityUnit[]> {
    return this.request<GrocyQuantityUnit[]>('GET', 'objects/quantity_units');
  }

  async getQuantityUnit(id: number): Promise<GrocyQuantityUnit> {
    return this.request<GrocyQuantityUnit>('GET', `objects/quantity_units/${id}`);
  }

  // Product Groups
  async getProductGroups(): Promise<GrocyProductGroup[]> {
    return this.request<GrocyProductGroup[]>('GET', 'objects/product_groups');
  }

  async getProductGroup(id: number): Promise<GrocyProductGroup> {
    return this.request<GrocyProductGroup>('GET', `objects/product_groups/${id}`);
  }

  // Locations
  async getLocations(): Promise<GrocyLocation[]> {
    return this.request<GrocyLocation[]>('GET', 'objects/locations');
  }

  async getLocation(id: number): Promise<GrocyLocation> {
    return this.request<GrocyLocation>('GET', `objects/locations/${id}`);
  }

  // Shopping List
  async getShoppingListItems(listId?: number): Promise<GrocyShoppingListItem[]> {
    const endpoint = listId ? `objects/shopping_list?query[]=shopping_list_id=${listId}` : 'objects/shopping_list';
    return this.request<GrocyShoppingListItem[]>('GET', endpoint);
  }

  async addShoppingListItem(item: any): Promise<GrocyShoppingListItem> {
    return this.request<GrocyShoppingListItem>('POST', 'objects/shopping_list', item);
  }

  async updateShoppingListItem(id: number, item: any): Promise<GrocyShoppingListItem> {
    return this.request<GrocyShoppingListItem>('PUT', `objects/shopping_list/${id}`, item);
  }

  async removeShoppingListItem(id: number): Promise<void> {
    await this.request<void>('DELETE', `objects/shopping_list/${id}`);
  }

  // Stock
  async addStockEntry(entry: any): Promise<any> {
    return this.request<any>('POST', 'stock/entries', entry);
  }

  async consumeStockEntry(entry: any): Promise<any> {
    return this.request<any>('POST', 'stock/consume', entry);
  }

  async inventoryStock(productId: number): Promise<any> {
    return this.request<any>('GET', `stock/products/${productId}`);
  }

  async getStockOverview(): Promise<any[]> {
    return this.request<any[]>('GET', 'stock/overview');
  }

  // System
  async getSystemInfo(): Promise<any> {
    return this.request<any>('GET', 'system/info');
  }
}

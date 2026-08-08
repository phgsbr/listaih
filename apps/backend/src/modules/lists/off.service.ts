import { Injectable, Logger, OnModuleInit } from '@nestjs/common';
import { HttpService } from '@nestjs/axios';
import { firstValueFrom, catchError } from 'rxjs';
import { AxiosError } from 'axios';

interface OffProduct {
  code: string;
  product_name?: string;
  generic_name?: string;
  brands?: string;
  categories?: string;
  image_url?: string;
  image_front_url?: string;
  nutriments?: Record<string, number>;
  additives_tags?: string[];
  allergens_tags?: string[];
  traces_tags?: string[];
  stores?: string;
  countries?: string;
  labels?: string;
  manufacturing_places?: string;
  purchase_places?: string;
  origins?: string;
  creator?: string;
  created_t?: string;
  last_modified_t?: string;
}

interface OffResponse {
  status: number;
  product?: OffProduct;
  status_verbose?: string;
  code?: string;
  message?: string;
}

@Injectable()
export class OffService implements OnModuleInit {
  private readonly logger = new Logger(OffService.name);
  private readonly baseUrl = 'https://world.openfoodfacts.org/api/v2';

  constructor(private httpService: HttpService) {}

  async onModuleInit() {
    // Nothing to initialize
  }

  /**
   * Fetch product data from Open Food Facts by barcode
   * Returns null if not found or error
   */
  async lookupByBarcode(barcode: string): Promise<OffProduct | null> {
    if (!barcode) return null;

    try {
      const url = `${this.baseUrl}/product/${barcode}.json`;
      const response = await firstValueFrom(
        this.httpService.get<OffResponse>(url, { timeout: 5000 }).pipe(
          catchError((error: AxiosError) => {
            this.logger.warn(`OFF API error for barcode ${barcode}: ${error.message}`);
            throw error;
          })
        )
      );

      const data = response.data;
      if (data.status === 1 && data.product) {
        return data.product;
      } else {
        this.logger.debug(`Product not found in OFF for barcode ${barcode}`);
        return null;
      }
    } catch (error: any) {
      this.logger.error(`Failed to fetch from OFF for barcode ${barcode}: ${error.message}`);
      return null;
    }
  }

  /**
   * Extract useful fields from OFF product for prefilling ListItem
   */
  extractItemData(offProduct: OffProduct): {
    name?: string;
    category?: string;
    estimatedPrice?: number; // Not available in OFF, but keeping for future
    barcode?: string;
  } {
    if (!offProduct) return {};

    // Prefer product_name, fallback to generic_name
    const name = offProduct.product_name || offProduct.generic_name;
    
    // Extract category from the first category if available
    let category: string | undefined;
    if (offProduct.categories) {
      // Take first category, clean it up
      const firstCat = offProduct.categories.split(',')[0]?.trim();
      if (firstCat) {
        category = firstCat;
      }
    }

    return {
      name: name || undefined,
      category: category || undefined,
      estimatedPrice: undefined, // OFF doesn't have price data
      barcode: offProduct.code,
    };
  }
}
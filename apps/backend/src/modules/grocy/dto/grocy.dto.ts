import { IsOptional, IsString, IsNumber, IsBoolean, IsUrl } from 'class-validator';
import { ApiPropertyOptional } from '@nestjs/swagger';

export class GrocyConfigDto {
  @ApiPropertyOptional({ example: 'http://grocy:9283' })
  @IsOptional()
  @IsUrl()
  grocyUrl?: string;

  @ApiPropertyOptional({ example: 'your-grocy-api-key' })
  @IsOptional()
  @IsString()
  grocyApiKey?: string;

  @ApiPropertyOptional({ example: true })
  @IsOptional()
  @IsBoolean()
  grocyEnabled?: boolean;
}

export class GrocyProductDto {
  @ApiPropertyOptional()
  @IsOptional()
  @IsString()
  name?: string;

  @ApiPropertyOptional()
  @IsOptional()
  @IsString()
  barcode?: string;

  @ApiPropertyOptional()
  @IsOptional()
  @IsNumber()
  quantityUnitStockId?: number;

  @ApiPropertyOptional()
  @IsOptional()
  @IsNumber()
  quantityUnitPurchaseId?: number;

  @ApiPropertyOptional()
  @IsOptional()
  @IsNumber()
  quantityUnitStockToPurchaseFactor?: number;

  @ApiPropertyOptional()
  @IsOptional()
  @IsNumber()
  minStockAmount?: number;

  @ApiPropertyOptional()
  @IsOptional()
  @IsNumber()
  defaultBestBeforeDays?: number;

  @ApiPropertyOptional()
  @IsOptional()
  @IsNumber()
  defaultBestBeforeDaysAfterOpen?: number;

  @ApiPropertyOptional()
  @IsOptional()
  @IsNumber()
  defaultBestBeforeDaysAfterFreezing?: number;

  @ApiPropertyOptional()
  @IsOptional()
  @IsString()
  pictureFileName?: string;

  @ApiPropertyOptional()
  @IsOptional()
  @IsNumber()
  productGroupId?: number;

  @ApiPropertyOptional()
  @IsOptional()
  @IsNumber()
  locationId?: number;

  @ApiPropertyOptional()
  @IsOptional()
  @IsNumber()
  shoppingLocationId?: number;

  @ApiPropertyOptional()
  @IsOptional()
  @IsString()
  note?: string;

  @ApiPropertyOptional()
  @IsOptional()
  @IsNumber()
  parentProductId?: number;

  @ApiPropertyOptional()
  @IsOptional()
  @IsNumber()
  calories?: number;

  @ApiPropertyOptional()
  @IsOptional()
  @IsNumber()
  cumulateMinStockAmountOfSubProducts?: boolean;

  @ApiPropertyOptional()
  @IsOptional()
  @IsNumber()
  dueType?: number;

  @ApiPropertyOptional()
  @IsOptional()
  @IsString()
  quickConsumeAmount?: string;

  @ApiPropertyOptional()
  @IsOptional()
  @IsNumber()
  rowCreatedTimestamp?: string;

  @ApiPropertyOptional()
  @IsOptional()
  @IsString()
  userfields?: any;
}

export class GrocyShoppingListItemDto {
  @ApiPropertyOptional()
  @IsOptional()
  @IsString()
  productId?: string;

  @ApiPropertyOptional()
  @IsOptional()
  @IsString()
  note?: string;

  @ApiPropertyOptional()
  @IsOptional()
  @IsNumber()
  amount?: number;

  @ApiPropertyOptional()
  @IsOptional()
  @IsNumber()
  quantityUnitId?: number;

  @ApiPropertyOptional()
  @IsOptional()
  @IsString()
  shoppingListId?: string;

  @ApiPropertyOptional()
  @IsOptional()
  @IsString()
  rowCreatedTimestamp?: string;
}

export class GrocyStockEntryDto {
  @ApiPropertyOptional()
  @IsOptional()
  @IsString()
  productId?: string;

  @ApiPropertyOptional()
  @IsOptional()
  @IsNumber()
  amount?: number;

  @ApiPropertyOptional()
  @IsOptional()
  @IsString()
  bestBeforeDate?: string;

  @ApiPropertyOptional()
  @IsOptional()
  @IsNumber()
  quantityUnitId?: number;

  @ApiPropertyOptional()
  @IsOptional()
  @IsNumber()
  price?: number;

  @ApiPropertyOptional()
  @IsOptional()
  @IsString()
  locationId?: string;

  @ApiPropertyOptional()
  @IsOptional()
  @IsString()
  rowCreatedTimestamp?: string;
}

export class GrocySyncResultDto {
  @ApiPropertyOptional()
  @IsOptional()
  @IsBoolean()
  success?: boolean;

  @ApiPropertyOptional()
  @IsOptional()
  @IsString()
  message?: string;

  @ApiPropertyOptional()
  @IsOptional()
  @IsNumber()
  productsSynced?: number;

  @ApiPropertyOptional()
  @IsOptional()
  @IsNumber()
  itemsSynced?: number;

  @ApiPropertyOptional()
  @IsOptional()
  @IsNumber()
  errors?: number;
}
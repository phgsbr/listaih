import {
  IsString,
  IsNumber,
  IsOptional,
  IsEnum,
  Min,
  IsInt,
} from 'class-validator';
import { Unit } from '@prisma/client';

export class CreateItemDto {
  @IsString()
  name: string;

  @IsOptional()
  @IsString()
  productId?: string;

  @IsOptional()
  @IsNumber()
  @Min(0)
  quantity?: number;

  @IsOptional()
  @IsEnum(Unit)
  unit?: Unit;

  @IsOptional()
  @IsNumber()
  estimatedPrice?: number;

  @IsOptional()
  @IsString()
  category?: string;

  @IsOptional()
  @IsString()
  notes?: string;

  @IsOptional()
  @IsInt()
  position?: number;

  @IsOptional()
  @IsString()
  barcode?: string;

  @IsOptional()
  @IsString()
  barcodeRaw?: string;
}

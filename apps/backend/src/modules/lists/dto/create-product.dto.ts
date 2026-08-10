import { IsString, IsOptional, IsEnum, MinLength, MaxLength } from 'class-validator';
import { Unit } from '@prisma/client';

export class CreateProductDto {
  @IsString()
  @MinLength(2)
  @MaxLength(200)
  name: string;

  @IsOptional()
  @IsString()
  @MinLength(2)
  @MaxLength(50)
  barcode?: string;

  @IsOptional()
  @IsString()
  @MaxLength(100)
  category?: string;

  @IsOptional()
  @IsEnum(Unit)
  defaultUnit?: Unit;
}
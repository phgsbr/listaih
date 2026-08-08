import {
  IsString,
  IsNumber,
  IsOptional,
  IsBoolean,
  IsEnum,
} from 'class-validator';
import { PaymentMethod, ReceiptStatus } from '@prisma/client';

export class UpdatePurchaseDto {
  @IsOptional()
  @IsEnum(PaymentMethod)
  paymentMethod?: PaymentMethod;

  @IsOptional()
  @IsNumber()
  totalAmount?: number;

  @IsOptional()
  @IsString()
  notes?: string;

  @IsOptional()
  @IsString()
  receiptPhoto?: string;

  @IsOptional()
  @IsString()
  receiptParsed?: string;

  @IsOptional()
  @IsEnum(ReceiptStatus)
  receiptStatus?: ReceiptStatus;
}
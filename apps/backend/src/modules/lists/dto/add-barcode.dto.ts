import { IsString, MinLength, MaxLength } from 'class-validator';

export class AddBarcodeDto {
  @IsString()
  @MinLength(2)
  @MaxLength(50)
  barcode: string;
}
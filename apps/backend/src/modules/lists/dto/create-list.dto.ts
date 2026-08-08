import { IsString, MinLength, MaxLength, IsBoolean, IsOptional, IsIn, IsEnum } from 'class-validator';
import { ListType } from '@prisma/client';

export class CreateListDto {
  @IsString()
  @MinLength(2)
  @MaxLength(50)
  name: string;

  @IsOptional()
  @IsBoolean()
  template?: boolean;

  @IsOptional()
  @IsString()
  @IsIn(['Alimentos', 'Farmacia', 'Papelaria', 'Material de Construcao', 'Geral'])
  category?: string;

  @IsOptional()
  @IsEnum(ListType)
  listType?: ListType;

  @IsOptional()
  @IsBoolean()
  grocyAssociated?: boolean;
}

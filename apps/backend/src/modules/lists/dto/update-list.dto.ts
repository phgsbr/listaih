import { IsString, MinLength, MaxLength, IsOptional, IsIn, IsEnum, IsBoolean } from 'class-validator';
import { ListType } from '@prisma/client';

export class UpdateListDto {
  @IsOptional()
  @IsString()
  @MinLength(2)
  @MaxLength(50)
  name?: string;

  @IsOptional()
  @IsString()
  archivedAt?: string | null;

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

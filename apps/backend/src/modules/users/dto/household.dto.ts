import { IsString, MinLength, MaxLength, IsEnum } from 'class-validator';
import { HouseholdRole } from '@prisma/client';

export class CreateHouseholdDto {
  @IsString()
  @MinLength(2)
  @MaxLength(50)
  name: string;
}

export class UpdateMemberRoleDto {
  @IsEnum(HouseholdRole)
  role: HouseholdRole;
}

export class JoinHouseholdDto {
  @IsString()
  inviteCode: string;
}

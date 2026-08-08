import { IsString, MinLength, MaxLength } from 'class-validator';

export class CreateTokenDto {
  @IsString()
  @MinLength(2)
  @MaxLength(50)
  name: string;

  @IsString()
  type: string;
}

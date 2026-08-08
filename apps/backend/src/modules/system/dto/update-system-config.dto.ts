import { IsOptional, IsString, IsBoolean, IsUrl } from 'class-validator';

export class UpdateSystemConfigDto {
  @IsOptional()
  @IsString()
  grocyUrl?: string;

  @IsOptional()
  @IsString()
  grocyApiKey?: string;

  @IsOptional()
  @IsBoolean()
  grocyEnabled?: boolean;

  @IsOptional()
  @IsString()
  haUrl?: string;

  @IsOptional()
  @IsString()
  haWebhookToken?: string;

  @IsOptional()
  @IsBoolean()
  haEnabled?: boolean;

  @IsOptional()
  @IsBoolean()
  apiEnabled?: boolean;

  @IsOptional()
  @IsString()
  apiBaseUrl?: string;

  @IsOptional()
  @IsString()
  apiKey?: string;

  @IsOptional()
  @IsBoolean()
  aiEnabled?: boolean;

  @IsOptional()
  @IsString()
  aiProvider?: string;

  @IsOptional()
  @IsString()
  aiApiKey?: string;

  @IsOptional()
  @IsString()
  aiEndpoint?: string;

  @IsOptional()
  @IsString()
  aiModel?: string;

  @IsOptional()
  @IsString()
  currency?: string;
}

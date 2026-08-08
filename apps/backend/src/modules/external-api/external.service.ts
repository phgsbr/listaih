import { Injectable, Logger } from '@nestjs/common';
import { PrismaService } from '../../prisma/prisma.service';

@Injectable()
export class ExternalService {
  private readonly logger = new Logger(ExternalService.name);

  constructor(private prisma: PrismaService) {}

  async logAccess(endpoint: string, apiKeyName: string, ip: string, success: boolean) {
    this.logger.log(`External API access: ${endpoint} | Key: ${apiKeyName} | IP: ${ip} | ${success ? 'OK' : 'FAILED'}`);
  }

  async validateApiKey(apiKey: string) {
    const config = await this.prisma.systemConfig.findFirst();
    return config?.apiEnabled && config?.apiKey === apiKey;
  }
}
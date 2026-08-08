import { Injectable, Logger } from '@nestjs/common';
import { PrismaService } from '../../prisma/prisma.service';

@Injectable()
export class SystemService {
  private readonly logger = new Logger(SystemService.name);

  constructor(private prisma: PrismaService) {}

  async getConfig() {
    let config = await this.prisma.systemConfig.findFirst();
    if (!config) {
      config = await this.prisma.systemConfig.create({ data: {} });
    }
    return {
      isSetup: config.isSetup,
      installedAt: config.installedAt,
      currency: config.currency,
      grocyUrl: config.grocyUrl,
      grocyApiKey: config.grocyApiKey ? '••••••••' : null,
      grocyEnabled: config.grocyEnabled,
      haUrl: config.haUrl,
      haWebhookToken: config.haWebhookToken ? '••••••••' : null,
      haEnabled: config.haEnabled,
      apiEnabled: config.apiEnabled,
      apiBaseUrl: config.apiBaseUrl,
      apiKey: config.apiKey ? '••••••••' : null,
      aiEnabled: config.aiEnabled,
      aiProvider: config.aiProvider,
      aiApiKey: config.aiApiKey ? '••••••••' : null,
      aiEndpoint: config.aiEndpoint,
      aiModel: config.aiModel,
    };
  }

  async updateConfig(data: {
    grocyUrl?: string;
    grocyApiKey?: string;
    grocyEnabled?: boolean;
    haUrl?: string;
    haWebhookToken?: string;
    haEnabled?: boolean;
    apiEnabled?: boolean;
    apiBaseUrl?: string;
    apiKey?: string;
    aiEnabled?: boolean;
    aiProvider?: string;
    aiApiKey?: string;
    aiEndpoint?: string;
    aiModel?: string;
    currency?: string;
  }) {
    let config = await this.prisma.systemConfig.findFirst();
    if (!config) {
      config = await this.prisma.systemConfig.create({ data: {} });
    }

    const updateData: any = {};
    if (data.grocyUrl !== undefined) updateData.grocyUrl = data.grocyUrl || null;
    if (data.grocyApiKey !== undefined && data.grocyApiKey !== '••••••••') {
      updateData.grocyApiKey = data.grocyApiKey || null;
    }
    if (data.grocyEnabled !== undefined) updateData.grocyEnabled = data.grocyEnabled;
    if (data.haUrl !== undefined) updateData.haUrl = data.haUrl || null;
    if (data.haWebhookToken !== undefined && data.haWebhookToken !== '••••••••') {
      updateData.haWebhookToken = data.haWebhookToken || null;
    }
    if (data.haEnabled !== undefined) updateData.haEnabled = data.haEnabled;
    if (data.apiEnabled !== undefined) updateData.apiEnabled = data.apiEnabled;
    if (data.apiBaseUrl !== undefined) updateData.apiBaseUrl = data.apiBaseUrl || null;
    if (data.apiKey !== undefined && data.apiKey !== '••••••••') {
      updateData.apiKey = data.apiKey || null;
    }
    if (data.currency !== undefined) updateData.currency = data.currency;
    if (data.aiEnabled !== undefined) updateData.aiEnabled = data.aiEnabled;
    if (data.aiProvider !== undefined) updateData.aiProvider = data.aiProvider || null;
    if (data.aiApiKey !== undefined && data.aiApiKey !== '••••••••') {
      updateData.aiApiKey = data.aiApiKey || null;
    }
    if (data.aiEndpoint !== undefined) updateData.aiEndpoint = data.aiEndpoint || null;
    if (data.aiModel !== undefined) updateData.aiModel = data.aiModel || null;

    const updated = await this.prisma.systemConfig.update({
      where: { id: config.id },
      data: updateData,
    });

    this.logger.log('SystemConfig updated');
    return {
      isSetup: updated.isSetup,
      installedAt: updated.installedAt,
      currency: updated.currency,
      grocyUrl: updated.grocyUrl,
      grocyApiKey: updated.grocyApiKey ? '••••••••' : null,
      grocyEnabled: updated.grocyEnabled,
      haUrl: updated.haUrl,
      haWebhookToken: updated.haWebhookToken ? '••••••••' : null,
      haEnabled: updated.haEnabled,
      apiEnabled: updated.apiEnabled,
      apiBaseUrl: updated.apiBaseUrl,
      apiKey: updated.apiKey ? '••••••••' : null,
      aiEnabled: updated.aiEnabled,
      aiProvider: updated.aiProvider,
      aiApiKey: updated.aiApiKey ? '••••••••' : null,
      aiEndpoint: updated.aiEndpoint,
      aiModel: updated.aiModel,
    };
  }
}

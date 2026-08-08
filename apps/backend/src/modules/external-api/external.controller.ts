import { Controller, Get, UseGuards, Request, Param, Query, UseInterceptors } from '@nestjs/common';
import { ApiTags, ApiBearerAuth, ApiOperation, ApiQuery } from '@nestjs/swagger';
import { Throttle, SkipThrottle } from '@nestjs/throttler';
import { ApiKeyGuard } from '../auth/api-key.guard';
import { PrismaService } from '../../prisma/prisma.service';
import { ExternalApiAuditInterceptor } from './external-audit.interceptor';

@ApiTags('External API')
@ApiBearerAuth('api-key')
@Controller('external')
@UseGuards(ApiKeyGuard)
@UseInterceptors(ExternalApiAuditInterceptor)
@Throttle({ default: { limit: 60, ttl: 60000 } })
export class ExternalController {
  constructor(private prisma: PrismaService) {}

  @Get('health')
  @ApiOperation({ summary: 'Health check for external API' })
  @SkipThrottle()
  health() {
    return { status: 'ok', timestamp: new Date().toISOString() };
  }

  @Get('households/:householdId/lists')
  @ApiOperation({ summary: 'Get all lists for a household' })
  @ApiQuery({ name: 'archived', required: false, type: Boolean })
  async getLists(@Param('householdId') householdId: string, @Query('archived') archived?: string) {
    const where: any = { householdId };
    if (archived === 'true') {
      where.archivedAt = { not: null };
    } else {
      where.archivedAt = null;
    }
    return this.prisma.shoppingList.findMany({
      where,
      include: { items: true },
      orderBy: { updatedAt: 'desc' },
    });
  }

  @Get('lists/:listId')
  @ApiOperation({ summary: 'Get a single list with items' })
  async getList(@Param('listId') listId: string) {
    return this.prisma.shoppingList.findUnique({
      where: { id: listId },
      include: { items: true },
    });
  }

  @Get('lists/:listId/items')
  @ApiOperation({ summary: 'Get items for a list' })
  async getItems(@Param('listId') listId: string) {
    return this.prisma.listItem.findMany({
      where: { listId },
      orderBy: { addedAt: 'asc' },
    });
  }

  @Get('config')
  @ApiOperation({ summary: 'Get external API config (base URL only)' })
  @SkipThrottle()
  async getConfig() {
    const config = await this.prisma.systemConfig.findFirst();
    return {
      apiBaseUrl: config?.apiBaseUrl || null,
    };
  }
}
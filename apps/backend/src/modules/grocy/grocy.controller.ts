import { Controller, Post, Get, Param, Body, UseGuards, Req } from '@nestjs/common';
import { ApiTags, ApiOperation, ApiBearerAuth, ApiResponse } from '@nestjs/swagger';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { GrocySyncService } from './grocy-sync.service';

@ApiTags('grocy')
@Controller('api/grocy')
@UseGuards(JwtAuthGuard)
@ApiBearerAuth()
export class GrocyController {
  constructor(private readonly grocySync: GrocySyncService) {}

  @Post('send-to-stock/:listId')
  @ApiOperation({ summary: 'Enviar itens comprados para a despensa do Grocy' })
  @ApiResponse({ status: 200, description: 'Resultado do envio' })
  async sendToStock(@Param('listId') listId: string) {
    return this.grocySync.sendPurchasedItemsToStock(listId);
  }

  @Post('sync-list/:listId')
  @ApiOperation({ summary: 'Sincronizar lista de compras com Grocy' })
  @ApiResponse({ status: 200, description: 'Resultado da sincronização' })
  async syncList(@Param('listId') listId: string) {
    return this.grocySync.syncShoppingListToGrocy(listId);
  }

  @Post('sync-from-grocy')
  @ApiOperation({ summary: 'Importar produtos do Grocy para o Listaih' })
  @ApiResponse({ status: 200, description: 'Resultado da importação' })
  async syncFromGrocy() {
    return this.grocySync.syncGrocyToLocal();
  }

  @Get('test-connection')
  @ApiOperation({ summary: 'Testar conexão com Grocy' })
  @ApiResponse({ status: 200, description: 'Status da conexão' })
  async testConnection() {
    const connected = await this.grocySync['grocyApi'].testConnection();
    return { connected };
  }
}
import { Module } from '@nestjs/common';
import { HttpModule } from '@nestjs/axios';
import { GrocyController } from './grocy.controller';
import { GrocySyncService } from './grocy-sync.service';
import { GrocyApiClient } from './grocy-api.client';
import { SystemModule } from '../system/system.module';
import { PrismaModule } from '../../prisma/prisma.module';

@Module({
  imports: [
    HttpModule,
    SystemModule,
    PrismaModule,
  ],
  controllers: [GrocyController],
  providers: [GrocySyncService, GrocyApiClient],
  exports: [GrocySyncService, GrocyApiClient],
})
export class GrocyModule {}
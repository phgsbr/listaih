import { Module } from '@nestjs/common';
import { ListsController } from './lists.controller';
import { ListsService } from './lists.service';
import { SyncModule } from '../sync/sync.module';
import { GrocyModule } from '../grocy/grocy.module';
import { HttpModule } from '@nestjs/axios';
import { SystemModule } from '../system/system.module';
import { OffService } from './off.service';
import { GS1Parser } from './gs1.parser';

@Module({
  imports: [SyncModule, GrocyModule, SystemModule, HttpModule],
  controllers: [ListsController],
  providers: [ListsService, OffService, GS1Parser],
  exports: [ListsService],
})
export class ListsModule {}

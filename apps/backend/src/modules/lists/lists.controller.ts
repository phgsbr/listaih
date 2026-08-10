import {
  Controller,
  Get,
  Post,
  Put,
  Patch,
  Delete,
  Body,
  Param,
  Query,
  UseGuards,
  Req,
} from '@nestjs/common';
import { Request } from 'express';
import { ListsService } from './lists.service';
import { CreateListDto } from './dto/create-list.dto';
import { UpdateListDto } from './dto/update-list.dto';
import { CreateItemDto } from './dto/create-item.dto';
import { UpdateItemDto } from './dto/update-item.dto';
import { CheckoutDto } from './dto/checkout.dto';
import { UpdatePurchaseDto } from './dto/update-purchase.dto';
import { CreateProductDto } from './dto/create-product.dto';
import { AddBarcodeDto } from './dto/add-barcode.dto';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';

@Controller()
@UseGuards(JwtAuthGuard)
export class ListsController {
  constructor(private listsService: ListsService) {}

  @Get('households/:householdId/lists')
  getLists(
    @Req() req: Request,
    @Param('householdId') householdId: string,
  ) {
    return this.listsService.getLists(req.user['id'], householdId);
  }

  @Post('households/:householdId/lists')
  createList(
    @Req() req: Request,
    @Param('householdId') householdId: string,
    @Body() dto: CreateListDto,
  ) {
    return this.listsService.createList(req.user['id'], householdId, dto);
  }

  @Get('lists/:id')
  getList(@Req() req: Request, @Param('id') id: string) {
    return this.listsService.getList(req.user['id'], id);
  }

  @Put('lists/:id')
  updateList(
    @Req() req: Request,
    @Param('id') id: string,
    @Body() dto: UpdateListDto,
  ) {
    return this.listsService.updateList(req.user['id'], id, dto);
  }

  @Delete('lists/:id')
  deleteList(@Req() req: Request, @Param('id') id: string) {
    return this.listsService.deleteList(req.user['id'], id);
  }

  @Post('lists/:id/items')
  addItem(
    @Req() req: Request,
    @Param('id') id: string,
    @Body() dto: CreateItemDto,
  ) {
    return this.listsService.addItem(req.user['id'], id, dto);
  }

  @Patch('lists/:id/items/:itemId')
  updateItem(
    @Req() req: Request,
    @Param('id') id: string,
    @Param('itemId') itemId: string,
    @Body() dto: UpdateItemDto,
  ) {
    return this.listsService.updateItem(req.user['id'], id, itemId, dto);
  }

  @Delete('lists/:id/items/:itemId')
  deleteItem(
    @Req() req: Request,
    @Param('id') id: string,
    @Param('itemId') itemId: string,
  ) {
    return this.listsService.deleteItem(req.user['id'], id, itemId);
  }

  // Product endpoints (barcode catalog for the app scanner)
  @Get('products/lookup/:barcode')
  lookupProduct(@Req() req: Request, @Param('barcode') barcode: string) {
    return this.listsService.lookupProductByBarcode(req.user['id'], barcode);
  }

  @Post('products')
  createProduct(@Req() req: Request, @Body() dto: CreateProductDto) {
    return this.listsService.createProduct(req.user['id'], dto);
  }

  @Post('products/:productId/barcodes')
  addProductBarcode(
    @Req() req: Request,
    @Param('productId') productId: string,
    @Body() dto: AddBarcodeDto,
  ) {
    return this.listsService.addProductBarcode(req.user['id'], productId, dto);
  }

  @Get('households/:householdId/history')
  getHistory(
    @Req() req: Request,
    @Param('householdId') householdId: string,
  ) {
    return this.listsService.getHistory(req.user['id'], householdId);
  }

  // Checkout endpoint
  @Post('lists/:id/checkout')
  checkout(
    @Req() req: Request,
    @Param('id') id: string,
    @Body() dto: CheckoutDto,
  ) {
    return this.listsService.checkout(req.user['id'], id, dto);
  }

  // Purchase endpoints
  @Get('households/:householdId/purchases')
  getPurchases(
    @Req() req: Request,
    @Param('householdId') householdId: string,
  ) {
    return this.listsService.getPurchases(req.user['id'], householdId);
  }

  @Get('lists/:id/purchases')
  getListPurchases(
    @Req() req: Request,
    @Param('id') id: string,
  ) {
    return this.listsService.getListPurchases(req.user['id'], id);
  }

  @Get('purchases/:id')
  getPurchase(@Req() req: Request, @Param('id') id: string) {
    return this.listsService.getPurchase(req.user['id'], id);
  }

  @Patch('purchases/:id')
  updatePurchase(
    @Req() req: Request,
    @Param('id') id: string,
    @Body() dto: UpdatePurchaseDto,
  ) {
    return this.listsService.updatePurchase(req.user['id'], id, dto);
  }

  // Stock status
  @Get('lists/:id/stock-status')
  getStockStatus(@Req() req: Request, @Param('id') id: string) {
    return this.listsService.getStockStatus(req.user['id'], id);
  }
}

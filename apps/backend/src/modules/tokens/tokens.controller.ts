import {
  Controller,
  Get,
  Post,
  Delete,
  Body,
  Param,
  UseGuards,
  Req,
} from '@nestjs/common';
import { Request } from 'express';
import { TokensService } from './tokens.service';
import { CreateTokenDto } from './dto/create-token.dto';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';

@Controller('tokens')
@UseGuards(JwtAuthGuard)
export class TokensController {
  constructor(private tokensService: TokensService) {}

  @Get()
  list(@Req() req: Request) {
    return this.tokensService.list(req.user['id']);
  }

  @Post()
  create(@Req() req: Request, @Body() dto: CreateTokenDto) {
    return this.tokensService.create(req.user['id'], dto);
  }

  @Delete(':id')
  revoke(@Req() req: Request, @Param('id') id: string) {
    return this.tokensService.revoke(req.user['id'], id);
  }
}

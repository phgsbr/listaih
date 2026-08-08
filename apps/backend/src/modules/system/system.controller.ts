import { Controller, Get, Put, Body, UseGuards } from '@nestjs/common';
import { SystemService } from './system.service';
import { UpdateSystemConfigDto } from './dto/update-system-config.dto';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';

@Controller('system')
@UseGuards(JwtAuthGuard)
export class SystemController {
  constructor(private readonly systemService: SystemService) {}

  @Get('config')
  async getConfig() {
    return this.systemService.getConfig();
  }

  @Put('config')
  async updateConfig(@Body() dto: UpdateSystemConfigDto) {
    return this.systemService.updateConfig(dto);
  }
}

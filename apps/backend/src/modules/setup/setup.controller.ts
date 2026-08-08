import { Controller, Get, Post, Body } from '@nestjs/common';
import { SetupService } from './setup.service';
import { SetupDto } from './dto/setup.dto';

@Controller('setup')
export class SetupController {
  constructor(private readonly setupService: SetupService) {}

  @Get('status')
  async getStatus() {
    return this.setupService.getSetupStatus();
  }

  @Post()
  async runSetup(@Body() dto: SetupDto) {
    return this.setupService.runSetup(dto);
  }
}

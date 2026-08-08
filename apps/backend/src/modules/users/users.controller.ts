import {
  Controller,
  Get,
  Put,
  Body,
  UseGuards,
  Req,
  Post,
  Param,
  Patch,
  Delete,
} from '@nestjs/common';
import { Request } from 'express';
import { UsersService } from './users.service';
import { UpdateProfileDto } from './dto/update-profile.dto';
import {
  UpdateMemberRoleDto,
  JoinHouseholdDto,
} from './dto/household.dto';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';

@Controller('users')
@UseGuards(JwtAuthGuard)
export class UsersController {
  constructor(private usersService: UsersService) {}

  @Get('me')
  getProfile(@Req() req: Request) {
    return this.usersService.getProfile(req.user['id']);
  }

  @Put('me')
  updateProfile(@Req() req: Request, @Body() dto: UpdateProfileDto) {
    return this.usersService.updateProfile(req.user['id'], dto);
  }

  @Get('households')
  getHouseholds(@Req() req: Request) {
    return this.usersService.getHouseholds(req.user['id']);
  }

  @Post('households/join')
  joinHousehold(@Req() req: Request, @Body() dto: JoinHouseholdDto) {
    return this.usersService.joinHousehold(req.user['id'], dto);
  }

  @Patch('households/:householdId/regenerate-code')
  regenerateInviteCode(
    @Req() req: Request,
    @Param('householdId') householdId: string,
  ) {
    return this.usersService.regenerateInviteCode(req.user['id'], householdId);
  }

  @Patch('households/:householdId/members/:memberId')
  updateMemberRole(
    @Req() req: Request,
    @Param('householdId') householdId: string,
    @Param('memberId') memberId: string,
    @Body() dto: UpdateMemberRoleDto,
  ) {
    return this.usersService.updateMemberRole(
      req.user['id'],
      householdId,
      memberId,
      dto,
    );
  }

  @Delete('households/:householdId/members/:memberId')
  removeMember(
    @Req() req: Request,
    @Param('householdId') householdId: string,
    @Param('memberId') memberId: string,
  ) {
    return this.usersService.removeMember(
      req.user['id'],
      householdId,
      memberId,
    );
  }
}

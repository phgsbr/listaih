import { Injectable, NotFoundException, ConflictException } from '@nestjs/common';
import { PrismaService } from '../../prisma/prisma.service';
import { UpdateProfileDto } from './dto/update-profile.dto';
import {
  UpdateMemberRoleDto,
  JoinHouseholdDto,
} from './dto/household.dto';

@Injectable()
export class UsersService {
  constructor(private prisma: PrismaService) {}

  async getProfile(userId: string) {
    const user = await this.prisma.user.findUnique({
      where: { id: userId },
      select: {
        id: true,
        email: true,
        name: true,
        avatar: true,
        provider: true,
        googleId: true,
        appleId: true,
        households: {
          include: {
            household: {
              include: {
                members: {
                  include: { user: { select: { id: true, name: true, avatar: true } } },
                },
              },
            },
          },
        },
      },
    });
    if (!user) throw new NotFoundException('Usuario nao encontrado');
    return user;
  }

  async updateProfile(userId: string, dto: UpdateProfileDto) {
    return this.prisma.user.update({
      where: { id: userId },
      data: dto,
      select: { id: true, email: true, name: true, avatar: true },
    });
  }

  async joinHousehold(userId: string, dto: JoinHouseholdDto) {
    const household = await this.prisma.household.findUnique({
      where: { inviteCode: dto.inviteCode },
    });
    if (!household) throw new NotFoundException('Codigo de convite invalido');

    const existing = await this.prisma.householdMember.findUnique({
      where: {
        householdId_userId: { householdId: household.id, userId },
      },
    });
    if (existing) throw new ConflictException('Voce ja e membro deste household');

    await this.prisma.householdMember.create({
      data: {
        householdId: household.id,
        userId,
        role: 'EDITOR',
      },
    });

    return this.prisma.household.findUnique({
      where: { id: household.id },
      include: {
        members: {
          include: { user: { select: { id: true, name: true, avatar: true } } },
        },
      },
    });
  }

  async regenerateInviteCode(userId: string, householdId: string) {
    const membership = await this.prisma.householdMember.findUnique({
      where: { householdId_userId: { householdId, userId } },
    });
    if (!membership || (membership.role !== 'ADMIN')) {
      throw new NotFoundException('Sem permissao para alterar este household');
    }

    const inviteCode = this.generateInviteCode();
    return this.prisma.household.update({
      where: { id: householdId },
      data: { inviteCode },
    });
  }

  private generateInviteCode(): string {
    const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789';
    let code = '';
    for (let i = 0; i < 6; i++) code += chars[Math.floor(Math.random() * chars.length)];
    return code.slice(0, 3) + '-' + code.slice(3);
  }

  async updateMemberRole(
    userId: string,
    householdId: string,
    memberId: string,
    dto: UpdateMemberRoleDto,
  ) {
    const membership = await this.prisma.householdMember.findUnique({
      where: { householdId_userId: { householdId, userId } },
    });
    if (!membership || membership.role !== 'ADMIN') {
      throw new NotFoundException('Sem permissao para alterar este household');
    }

    return this.prisma.householdMember.update({
      where: { id: memberId },
      data: { role: dto.role },
    });
  }

  async removeMember(
    userId: string,
    householdId: string,
    memberId: string,
  ) {
    const membership = await this.prisma.householdMember.findUnique({
      where: { householdId_userId: { householdId, userId } },
    });
    if (!membership || membership.role !== 'ADMIN') {
      throw new NotFoundException('Sem permissao para alterar este household');
    }

    return this.prisma.householdMember.delete({
      where: { id: memberId },
    });
  }

  async getHouseholds(userId: string) {
    const memberships = await this.prisma.householdMember.findMany({
      where: { userId },
      include: {
        household: {
          include: {
            members: {
              include: { user: { select: { id: true, name: true, avatar: true } } },
            },
          },
        },
      },
    });
    return memberships.map((m) => ({
      ...m.household,
      role: m.role,
    }));
  }
}

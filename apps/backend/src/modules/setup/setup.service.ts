import {
  Injectable,
  BadRequestException,
  ConflictException,
} from '@nestjs/common';
import * as bcrypt from 'bcrypt';
import { randomUUID } from 'crypto';
import { PrismaService } from '../../prisma/prisma.service';
import { SetupDto } from './dto/setup.dto';

@Injectable()
export class SetupService {
  constructor(private prisma: PrismaService) {}

  async getSetupStatus() {
    const config = await this.prisma.systemConfig.findFirst();

    if (!config) {
      return { isSetup: false };
    }

    return {
      isSetup: config.isSetup,
      installedAt: config.installedAt,
    };
  }

  async runSetup(dto: SetupDto) {
    const config = await this.prisma.systemConfig.findFirst();

    if (config?.isSetup) {
      throw new BadRequestException('Sistema ja configurado');
    }

    const existingUser = await this.prisma.user.findUnique({
      where: { email: dto.email },
    });
    if (existingUser) {
      throw new ConflictException('Email ja cadastrado');
    }

    const passwordHash = await bcrypt.hash(dto.password, 10);

    const result = await this.prisma.$transaction(async (tx) => {
      const user = await tx.user.create({
        data: {
          email: dto.email,
          name: dto.name,
          passwordHash,
        },
      });

      const household = await tx.household.create({
        data: {
          name: dto.householdName,
          inviteCode: this.generateInviteCode(),
        },
      });

      await tx.householdMember.create({
        data: {
          householdId: household.id,
          userId: user.id,
          role: 'ADMIN',
        },
      });

      if (config) {
        await tx.systemConfig.update({
          where: { id: config.id },
          data: {
            isSetup: true,
            adminUserId: user.id,
            installedAt: new Date(),
          },
        });
      } else {
        await tx.systemConfig.create({
          data: {
            isSetup: true,
            adminUserId: user.id,
            installedAt: new Date(),
          },
        });
      }

      return { user, household };
    });

    return {
      message: 'Sistema configurado com sucesso',
      admin: { id: result.user.id, email: result.user.email, name: result.user.name },
      household: { id: result.household.id, name: result.household.name },
    };
  }

  private generateInviteCode(): string {
    return randomUUID().substring(0, 6).toUpperCase();
  }
}

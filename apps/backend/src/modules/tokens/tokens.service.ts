import { Injectable, NotFoundException } from '@nestjs/common';
import { randomBytes } from 'crypto';
import * as bcrypt from 'bcrypt';
import { PrismaService } from '../../prisma/prisma.service';
import { CreateTokenDto } from './dto/create-token.dto';

@Injectable()
export class TokensService {
  constructor(private prisma: PrismaService) {}

  async list(userId: string) {
    const tokens = await this.prisma.apiToken.findMany({
      where: { userId, revokedAt: null },
      orderBy: { createdAt: 'desc' },
    });
    return tokens.map((t) => ({
      id: t.id,
      name: t.name,
      prefix: t.prefix,
      type: t.type,
      lastUsedAt: t.lastUsedAt,
      createdAt: t.createdAt,
    }));
  }

  async create(userId: string, dto: CreateTokenDto) {
    const raw = `sk_${dto.type}_${randomBytes(24).toString('hex')}`;
    const prefix = raw.slice(0, 12);
    const tokenHash = await bcrypt.hash(raw, 10);

    const token = await this.prisma.apiToken.create({
      data: {
        name: dto.name,
        type: dto.type,
        token: raw,
        tokenHash,
        prefix,
        userId,
      },
    });

    return {
      id: token.id,
      name: token.name,
      type: token.type,
      prefix: token.prefix,
      createdAt: token.createdAt,
      token: raw,
    };
  }

  async revoke(userId: string, tokenId: string) {
    const token = await this.prisma.apiToken.findFirst({
      where: { id: tokenId, userId },
    });
    if (!token) throw new NotFoundException('Token nao encontrado');

    await this.prisma.apiToken.update({
      where: { id: tokenId },
      data: { revokedAt: new Date() },
    });

    return { success: true };
  }
}

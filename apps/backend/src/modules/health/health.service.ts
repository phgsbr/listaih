import { Injectable } from '@nestjs/common';
import { PrismaService } from '../../prisma/prisma.service';
import { ConfigService } from '@nestjs/config';
import * as net from 'net';

@Injectable()
export class HealthService {
  constructor(
    private prisma: PrismaService,
    private configService: ConfigService,
  ) {}

  async check() {
    const [db, redis] = await Promise.all([
      this.checkDb(),
      this.checkRedis(),
    ]);

    const integrations = await this.checkIntegrations();

    return {
      status: db === 'up' && redis === 'up' ? 'ok' : 'degraded',
      timestamp: new Date().toISOString(),
      services: {
        database: db,
        redis,
      },
      integrations,
    };
  }

  private async checkDb(): Promise<string> {
    try {
      await this.prisma.$queryRaw`SELECT 1`;
      return 'up';
    } catch {
      return 'down';
    }
  }

  private async checkRedis(): Promise<string> {
    const redisUrl = this.configService.get<string>('REDIS_URL', 'redis://localhost:6379');
    const url = new URL(redisUrl);
    const port = parseInt(url.port || '6379', 10);
    const host = url.hostname;

    return new Promise((resolve) => {
      const socket = new net.Socket();
      const timeout = setTimeout(() => {
        socket.destroy();
        resolve('down');
      }, 3000);

      socket.connect(port, host, () => {
        clearTimeout(timeout);
        socket.destroy();
        resolve('up');
      });

      socket.on('error', () => {
        clearTimeout(timeout);
        socket.destroy();
        resolve('down');
      });
    });
  }

  private async checkIntegrations() {
    const config = await this.prisma.systemConfig.findFirst();

    return {
      grocy: {
        enabled: config?.grocyEnabled ?? false,
        url: config?.grocyUrl ?? null,
      },
      homeAssistant: {
        enabled: config?.haEnabled ?? false,
        url: config?.haUrl ?? null,
      },
    };
  }
}

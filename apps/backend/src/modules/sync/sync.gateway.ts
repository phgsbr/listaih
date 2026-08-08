import {
  WebSocketGateway,
  WebSocketServer,
  OnGatewayConnection,
  OnGatewayDisconnect,
} from '@nestjs/websockets';
import { Logger } from '@nestjs/common';
import { Server, Socket } from 'socket.io';
import * as jwt from 'jsonwebtoken';
import { ConfigService } from '@nestjs/config';
import { PrismaService } from '../../prisma/prisma.service';
import { SyncService } from './sync.service';

@WebSocketGateway({
  namespace: '/ws',
  cors: {
    origin: '*',
    credentials: true,
  },
})
export class SyncGateway implements OnGatewayConnection, OnGatewayDisconnect {
  @WebSocketServer()
  server: Server;

  private readonly logger = new Logger(SyncGateway.name);

  constructor(
    private configService: ConfigService,
    private prisma: PrismaService,
    private syncService: SyncService,
  ) {}

  async afterInit() {
    this.syncService.setIo(this.server);
    this.logger.log('Sync gateway initialized');
  }

  async handleConnection(client: Socket) {
    try {
      const token =
        client.handshake.auth?.token ||
        (client.handshake.headers.authorization?.startsWith('Bearer ')
          ? client.handshake.headers.authorization.substring(7)
          : null);

      if (!token) {
        this.logger.warn(`Connection rejected: no token`);
        client.disconnect();
        return;
      }

      const payload = jwt.verify(token, this.configService.get<string>('JWT_SECRET')) as {
        sub: string;
        email: string;
      };

      const user = await this.prisma.user.findUnique({
        where: { id: payload.sub },
        select: { id: true, households: { select: { householdId: true } } },
      });

      if (!user) {
        this.logger.warn(`Connection rejected: user not found`);
        client.disconnect();
        return;
      }

      client.data.userId = user.id;

      for (const membership of user.households) {
        client.join(`household:${membership.householdId}`);
      }

      this.logger.log(`Client connected: ${user.id}`);
    } catch (err) {
      this.logger.warn(`Connection rejected: ${(err as Error).message}`);
      client.disconnect();
    }
  }

  async handleDisconnect(client: Socket) {
    if (client.data.userId) {
      this.logger.log(`Client disconnected: ${client.data.userId}`);
    }
  }
}

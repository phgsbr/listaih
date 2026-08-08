import { Injectable, OnModuleInit, OnModuleDestroy, Logger } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import Redis from 'ioredis';

export interface SyncEvent {
  householdId: string;
  event: string;
  data: any;
}

@Injectable()
export class SyncService implements OnModuleInit, OnModuleDestroy {
  private readonly logger = new Logger(SyncService.name);
  private publisher: Redis | null = null;
  private subscriber: Redis | null = null;
  private readonly channel = 'listaih:sync';

  constructor(private configService: ConfigService) {}

  onModuleInit() {
    const redisUrl = this.configService.get<string>('REDIS_URL', 'redis://localhost:6379');

    try {
      this.publisher = new Redis(redisUrl, {
        maxRetriesPerRequest: null,
        retryStrategy: (times) => Math.min(times * 500, 3000),
      });
      this.subscriber = new Redis(redisUrl, {
        maxRetriesPerRequest: null,
        retryStrategy: (times) => Math.min(times * 500, 3000),
      });

      this.publisher.on('error', (err) => {
        this.logger.warn(`Redis publisher error (continuing without sync): ${err.message}`);
      });
      this.subscriber.on('error', (err) => {
        this.logger.warn(`Redis subscriber error (continuing without sync): ${err.message}`);
      });

      this.subscriber.subscribe(this.channel);
      this.subscriber.on('message', (_channel, message) => {
        try {
          const syncEvent: SyncEvent = JSON.parse(message);
          this.emitToLocalClients(syncEvent);
        } catch (err) {
          this.logger.error(`Failed to parse sync event: ${err}`);
        }
      });

      this.logger.log('Redis pub/sub initialized');
    } catch (err) {
      this.logger.warn(`Redis unavailable, sync disabled: ${String(err)}`);
    }
  }

  onModuleDestroy() {
    this.publisher?.disconnect();
    this.subscriber?.disconnect();
  }

  private emitToLocalClients(syncEvent: SyncEvent) {
    const io = this.getIo();
    if (io) {
      io.to(`household:${syncEvent.householdId}`).emit(syncEvent.event, syncEvent.data);
    }
  }

  private ioInstance: any = null;

  setIo(io: any) {
    this.ioInstance = io;
  }

  private getIo(): any {
    return this.ioInstance;
  }

  publish(syncEvent: SyncEvent) {
    if (this.publisher) {
      this.publisher.publish(this.channel, JSON.stringify(syncEvent)).catch(() => {});
    }
  }
}

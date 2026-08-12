import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { ServeStaticModule } from '@nestjs/serve-static';
import { ThrottlerModule, ThrottlerGuard } from '@nestjs/throttler';
import { APP_GUARD } from '@nestjs/core';
import { join } from 'path';
import { PrismaModule } from './prisma/prisma.module';
import { AuthModule } from './modules/auth/auth.module';
import { UsersModule } from './modules/users/users.module';
import { ListsModule } from './modules/lists/lists.module';
import { SetupModule } from './modules/setup/setup.module';
import { HealthModule } from './modules/health/health.module';
import { SyncModule } from './modules/sync/sync.module';
import { SystemModule } from './modules/system/system.module';
import { TokensModule } from './modules/tokens/tokens.module';
import { ExternalModule } from './modules/external-api/external.module';
import { GrocyModule } from './modules/grocy/grocy.module';

@Module({
  imports: [
    ConfigModule.forRoot({ isGlobal: true }),
    ServeStaticModule.forRoot({
      rootPath: process.env.ADMIN_DIST_PATH || join(__dirname, '..', '..', 'admin', 'dist'),
      serveRoot: '/admin',
      serveStaticOptions: { index: ['index.html'] },
    }),
    ThrottlerModule.forRoot([
      {
        ttl: 60000,
        limit: 100,
      },
    ]),
    PrismaModule,
    SetupModule,
    HealthModule,
    AuthModule,
    UsersModule,
    ListsModule,
    SyncModule,
    SystemModule,
    TokensModule,
    ExternalModule,
    GrocyModule,
  ],
  providers: [
    {
      provide: APP_GUARD,
      useClass: ThrottlerGuard,
    },
  ],
})
export class AppModule {}

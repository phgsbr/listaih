import { NestFactory } from '@nestjs/core';
import { ValidationPipe, Logger } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { json } from 'express';
import { AppModule } from './app.module';

const logger = new Logger('Bootstrap');

process.on('unhandledRejection', (reason) => {
  logger.warn(`Unhandled rejection: ${reason}`);
});

process.on('uncaughtException', (err) => {
  logger.error(`Uncaught exception: ${err.message}`, err.stack);
});

async function bootstrap() {
  const app = await NestFactory.create(AppModule);
  const configService = app.get(ConfigService);

  app.use(json({ limit: '10mb' }));

  // Redirecionar raiz "/" para "/admin/"
  app.use((req, res, next) => {
    if (req.path === '/' || req.path === '') {
      return res.redirect(302, '/admin/');
    }
    next();
  });

  app.setGlobalPrefix('api');
  app.useGlobalPipes(
    new ValidationPipe({
      whitelist: true,
      transform: true,
      forbidNonWhitelisted: true,
    }),
  );

  app.enableCors({
    origin: configService.get<string>('CORS_ORIGIN', '*'),
    credentials: true,
  });

  const port = configService.get<number>('PORT', 3000);
  await app.listen(port);
  console.log(`Listaih backend running on port ${port}`);
  console.log(`WebSocket sync available at ws://localhost:${port}/ws`);
}

bootstrap();

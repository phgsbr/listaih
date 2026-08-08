import { Injectable, UnauthorizedException } from '@nestjs/common';
import { PassportStrategy } from '@nestjs/passport';
import HeaderAPIKeyStrategy from 'passport-headerapikey';
import { PrismaService } from '../../prisma/prisma.service';

export interface ApiKeyPayload {
  type: 'external';
  name: string;
}

@Injectable()
export class ApiKeyStrategy extends PassportStrategy(HeaderAPIKeyStrategy, 'api-key') {
  constructor(private prisma: PrismaService) {
    super(
      { header: 'x-api-key', prefix: '' },
      true,
      async (apiKey: string, done: (error: Error | null, payload?: ApiKeyPayload) => void) => {
        await this.validate(apiKey, done);
      },
    );
  }

  async validate(apiKey: string, done: (error: Error | null, payload?: ApiKeyPayload) => void) {
    const config = await this.prisma.systemConfig.findFirst();
    
    if (!config || !config.apiEnabled || !config.apiKey || config.apiKey !== apiKey) {
      return done(new UnauthorizedException('API Key inválida ou desabilitada'), null);
    }

    const payload: ApiKeyPayload = {
      type: 'external',
      name: 'External API',
    };
    done(null, payload);
  }
}
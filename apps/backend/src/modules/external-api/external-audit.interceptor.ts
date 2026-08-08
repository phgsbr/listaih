import { Injectable, NestInterceptor, ExecutionContext, CallHandler, Logger } from '@nestjs/common';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';

@Injectable()
export class ExternalApiAuditInterceptor implements NestInterceptor {
  private readonly logger = new Logger('ExternalAPI');

  intercept(context: ExecutionContext, next: CallHandler): Observable<any> {
    const request = context.switchToHttp().getRequest();
    const method = request.method;
    const url = request.url;
    const ip = request.ip || request.connection?.remoteAddress;
    const apiKey = request.headers['x-api-key']?.substring(0, 8) + '...';
    const userAgent = request.headers['user-agent'];
    const startTime = Date.now();

    return next.handle().pipe(
      tap({
        next: (data) => {
          const duration = Date.now() - startTime;
          this.logger.log(
            `${method} ${url} | IP: ${ip} | Key: ${apiKey} | UA: ${userAgent} | ${duration}ms | 200 OK`
          );
        },
        error: (error) => {
          const duration = Date.now() - startTime;
          this.logger.error(
            `${method} ${url} | IP: ${ip} | Key: ${apiKey} | UA: ${userAgent} | ${duration}ms | ${error.status || 500} ERROR`
          );
        },
      }),
    );
  }
}
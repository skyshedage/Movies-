import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler } from '@angular/common/http';
import { UserStoreService } from '../helpers/user-store.service';
@Injectable()
export class TokenInterceptor implements HttpInterceptor {
  constructor(private store: UserStoreService) {}
  intercept(req: HttpRequest<any>, next: HttpHandler) {
    const token=this.store.authUser?.jwtToken;
    return next.handle(token && req.url.startsWith('/api/') ? req.clone({setHeaders:{Authorization: `Bearer ${token}`}}) : req);
  }
}

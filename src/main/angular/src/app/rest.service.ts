
import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs/Rx';
import 'rxjs/add/operator/map'


/**
 * This service makes the actual calls (with given parameters and content) to the servlet part of this application.
 * Returns the Observable results and in the restProxy data will be subscribed.
 */
@Injectable()
export class RestService
{
  constructor(private http: HttpClient) { }

  private servletURL = '../RestServlet';

  getRequest(params: HttpParams): Observable<any>
  {
    const headers = new HttpHeaders().set("Content-Type", "application/json");
    return this.http.get(this.servletURL, {headers, params, responseType: 'json' }) // ...using post request
  }

  postRequest(body: Object, params: HttpParams): Observable<any>
  {
    let bodyString = JSON.stringify(body); // Stringify payload
    const headers = new HttpHeaders().set("Content-Type", "application/json");

    return this.http.post(this.servletURL, bodyString, {headers, params, responseType: 'json' }) // ...using post request
  }

}

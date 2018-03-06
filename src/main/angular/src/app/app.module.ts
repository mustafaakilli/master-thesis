import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { AngularMultiSelectModule } from '../../node_modules/angular2-multiselect-dropdown/angular2-multiselect-dropdown';

import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { AppComponent } from './app.component';
import {RestService} from "./rest.service";

import { HttpClientModule } from '@angular/common/http';
import { AnalyseTransformationComponent } from './analyse-transformation/analyse-transformation.component';
import { CreateCommunicationComponent } from './create-communication/create-communication.component';
import { CreateEnvironmentComponent } from './create-environment/create-environment.component';
import {RestProxy} from "./rest.proxy";



@NgModule({
  declarations: [
    AppComponent,
    AnalyseTransformationComponent,
    CreateCommunicationComponent,
    CreateEnvironmentComponent,
  ],
  imports: [
    BrowserModule, AngularMultiSelectModule, FormsModule, ReactiveFormsModule, HttpClientModule
  ],
  providers: [RestService, RestProxy],
  bootstrap: [AppComponent]
})
export class AppModule { }

import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateEnvironmentComponent } from './create-environment.component';

describe('CreateEnvironmentComponent', () => {
  let component: CreateEnvironmentComponent;
  let fixture: ComponentFixture<CreateEnvironmentComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CreateEnvironmentComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateEnvironmentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

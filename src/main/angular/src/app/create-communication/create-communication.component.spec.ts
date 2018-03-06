import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateCommunicationComponent } from './create-communication.component';

describe('CreateCommunicationComponent', () => {
  let component: CreateCommunicationComponent;
  let fixture: ComponentFixture<CreateCommunicationComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CreateCommunicationComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateCommunicationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

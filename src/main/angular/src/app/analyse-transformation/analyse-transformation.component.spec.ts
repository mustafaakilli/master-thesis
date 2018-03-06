import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AnalyseTransformationComponent } from './analyse-transformation.component';

describe('AnalyseTransformationComponent', () => {
  let component: AnalyseTransformationComponent;
  let fixture: ComponentFixture<AnalyseTransformationComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AnalyseTransformationComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AnalyseTransformationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

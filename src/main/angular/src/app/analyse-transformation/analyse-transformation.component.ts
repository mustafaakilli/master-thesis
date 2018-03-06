import { Component, OnInit } from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {RestProxy} from "../rest.proxy";


@Component({
  selector: 'app-analyse-transformation',
  templateUrl: './analyse-transformation.component.html',
  styleUrls: ['./analyse-transformation.component.css']
})
export class AnalyseTransformationComponent implements OnInit
{
  // define some constants

  defaultFileName = "Default";
  folderName = "environments";
  newLineStr = "<br\>";

  // For output part of the UI
  outputWindowText = "";

  // for dropdown lists
  itemListOldEnvironment = [];
  settingsOldEnvironment = {};
  selectedItemsOldEnvironment = [];

  itemListOldCommunication = [];
  settingsOldCommunication = {};
  selectedItemsOldCommunication = [];

  itemListNewEnvironment = [];
  settingsNewEnvironment = {};
  selectedItemsNewEnvironment = [];

  userForm: FormGroup;


  constructor(private fb: FormBuilder, private restProxy: RestProxy)
  {
    this.createForm();
  }

  ngOnInit()
  {
    // Set the Environment component of the proxy. It will need this
    this.restProxy.setAnalysisComp(this);
  }

  createForm()
  {
    // form has following fields
    this.userForm = this.fb.group(
    {
      oldEnvironment: [[], Validators.required],
      oldCommunication: [[], Validators.required],
      newEnvironment: [[], Validators.required],
    });
  }

  submitForm()
  {
    // onclick to submit, send the values for analysis
    let env1 = this.userForm.value.oldEnvironment[0]["itemName"];
    let env2 = this.userForm.value.newEnvironment[0]["itemName"];
    let oldComm = this.userForm.value.oldCommunication[0]["itemName"];

    // for now body is empty. Above parameters are enough
    this.restProxy.getAnalysisResult(env1, env2, oldComm,{});
    this.initAnalyser();
  }

  /**
   * Writes the given data to the output part of the UI
   *
   * @param result
   */
  sendResultToOutputWindow(result: any)
  {
    // clear output, evey-time a new analysis result comes.
    this.outputWindowText = "";
    let self = this;

    for(let key in result)
    {
      if (result.hasOwnProperty(key))
      {
        // print in a nice way
        self.outputWindowText += key.bold() + ": " + result[key] + self.newLineStr + self.newLineStr;
      }
    }

    //this.outputWindowText = JSON.stringify(result, null, '\t') + this.newLineStr;
  }

  /**
   * Initialize all the necessary variables. Dropdown elements, settings of drop down, etc..
   * All the values will be default, i.e., every element is selectable
   */
  initAnalyser()
  {
    // get the settings and namse
    let settings = this.restProxy.getSettings(this.folderName);
    let envNames = this.restProxy.getNames(this.folderName);


    // They are empty, do nothing. Wait for loading.
    if(Object.keys(settings).length <= 0 || envNames.length <= 0)
    {
      return;
    }

    // filter the default name
    let envNamesFiltered = envNames.filter(item => item.itemName != this.defaultFileName);

    // init everything to default
    this.itemListOldEnvironment = envNamesFiltered;
    this.settingsOldEnvironment = settings["oldEnvironment"];
    this.selectedItemsOldEnvironment = [];

    //old communication list will be empty till an old environment is selected
    this.itemListOldCommunication = [];
    this.settingsOldCommunication = settings["oldCommunication"];
    this.selectedItemsOldCommunication = [];

    this.itemListNewEnvironment = envNamesFiltered;
    this.settingsNewEnvironment = settings["newEnvironment"];
    this.selectedItemsNewEnvironment = [];
  }


  onItemSelectOldEnvironment(item: any)
  {
    // When the old environment selected, give options to select old communication
    // Old communications are from the list that is supported by the old communication
    // Till it is selected, old communication will be empty.

    let selectedOldEnv = this.restProxy.getContent(this.folderName, item["itemName"]);
    this.itemListOldCommunication = selectedOldEnv["supportedCommunications"];
    this.selectedItemsOldCommunication = [];
  }

  OnItemDeSelectOldEnvironment(item: any)
  {
    // When old environment is deselected, init everything to default values.
    this.initAnalyser();
  }
}

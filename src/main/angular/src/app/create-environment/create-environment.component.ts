import { Component, OnInit } from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {RestProxy} from "../rest.proxy";


@Component({
  selector: 'app-create-environment',
  templateUrl: './create-environment.component.html',
  styleUrls: ['./create-environment.component.css']
})
export class CreateEnvironmentComponent implements OnInit
{

  // define some constants
  defaultFileName = "Default";
  folderName = "environments";
  newLineStr = "<br\>";

  // For output part of the UI
  outputWindowText = "";

  // for dropdown lists
  itemListBaseEnvironment = [];
  settingsBaseEnvironment = {};
  selectedItemsBaseEnvironment = [];

  itemListSupportedCommunications = [];
  settingsSupportedCommunications = {};
  selectedItemsSupportedCommunications = [];

  environmentName = "";

  userForm: FormGroup;


  constructor(private fb: FormBuilder, private restProxy: RestProxy)
  {
    this.createForm();
  }

  ngOnInit()
  {
    // Set the Environment component of the proxy. It will need this
    this.restProxy.setEnvironmentComp(this);
  }

  createForm()
  {
    // form has following fields
    this.userForm = this.fb.group(
    {
      name: ['', Validators.required],
      supportedCommunications: [[], Validators.required],
    });
  }

  submitForm()
  {
    // onclick to submit, save the content of the form
    this.restProxy.saveContent(this.folderName, this.userForm.value.name, this.userForm.value);
  }

  /**
   * Writes the given data to the output part of the UI
   *
   * @param result
   */
  sendResultToOutputWindow(result: any)
  {
    //this.outputWindowText = "";
    let self = this;

    for(let key in result)
    {
      if (result.hasOwnProperty(key))
      {
        // print in a nice way
        self.outputWindowText += key.bold() + ": " + result[key] + self.newLineStr;
      }
    }
    self.outputWindowText += self.newLineStr;

    //this.outputWindowText += JSON.stringify(result) + this.newLineStr;
  }


  /**
   * Initialize all the necessary variables. Dropdown elements, settings of drop down, etc..
   * All the values will be default, i.e., every element is selectable
   */
  initEnvironment()
  {
    // get names and settings for dropdown elements
    let envNames = this.restProxy.getNames(this.folderName);
    let settings = this.restProxy.getSettings(this.folderName);

    let commNames = this.restProxy.getNames("communications");

    // They are empty, do nothing. Wait for loading.
    if(Object.keys(settings).length <= 0 || envNames.length <= 0 || commNames.length <= 0)
    {
      return;
    }

    // filter the default name
    let commNamesFiltered = commNames.filter(item => item.itemName != this.defaultFileName);
    let envNamesFiltered = envNames.filter(item => item.itemName != this.defaultFileName);


    // Selectable environments for the base type for customization
    this.itemListBaseEnvironment = envNamesFiltered;
    this.settingsBaseEnvironment = settings["baseEnvironment"];
    this.selectedItemsBaseEnvironment = [];

    this.environmentName = "";      // empty initially

    // Selectable supported communications will be all at first.
    this.itemListSupportedCommunications = commNamesFiltered;
    this.settingsSupportedCommunications = settings["supportedCommunications"];
    this.selectedItemsSupportedCommunications = [];
  }


  /**
   * A base type is selected for customization
   * Recalculate all the selectable elements using the base type.
   *
   * @param item
   */
  onItemSelectBaseEnvironment(item: any)
  {
    let selectedName = item["itemName"];
    if(selectedName == this.defaultFileName)
    {
      // if default is selected, then re-init everything to default, nothing to do here.
      this.initEnvironment();
      return;
    }

    // get the base item which is selected on the UI
    let baseItem = this.restProxy.getContent(this.folderName, selectedName);

    // get the communications of the selected environment
    let commNames = this.restProxy.getNames("communications");
    let commNamesFiltered = commNames.filter(item => item.itemName != this.defaultFileName);

    // Give random unique name
    this.environmentName = selectedName + "_ENV_" + Math.random().toString(36).substr(2, 10);

    // Init dropdown elements according to the selected environment
    this.itemListSupportedCommunications = commNamesFiltered;
    this.selectedItemsSupportedCommunications = baseItem["supportedCommunications"];
  }


  OnItemDeSelectBaseEnvironment(item: any)
  {
    // when there is no selected base item, then re-init everything to default.
    this.initEnvironment();
  }

}

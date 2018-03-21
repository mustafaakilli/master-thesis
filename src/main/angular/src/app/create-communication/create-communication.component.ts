import { Component, OnInit } from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {RestProxy} from "../rest.proxy";


@Component({
  selector: 'app-create-communication',
  templateUrl: './create-communication.component.html',
  styleUrls: ['./create-communication.component.css']
})
export class CreateCommunicationComponent implements OnInit
{

  // define some constants
  defaultFileName = "Default";
  defaultBaseType = "NEW";
  folderName = "communications";
  newLineStr = "<br\>";

  // For output part of the UI
  outputWindowText = "";
  inputWindowText = "";

  // for dropdown lists
  itemListBaseCommunication = [];
  settingsBaseCommunication = {};
  selectedItemsBaseCommunication = [];

  communicationName = "";
  baseType = "";

  itemListCommunicationType = [];
  settingsCommunicationType = {};
  selectedItemsCommunicationType = [];

  itemListCommunicationPattern = [];
  settingsCommunicationPattern = {};
  selectedItemsCommunicationPattern = [];

  itemListRequestType = [];
  settingsRequestType = {};
  selectedItemsRequestType = [];

  itemListHeaderSize = [];
  settingsHeaderSize = {};
  selectedItemsHeaderSize = [];

  itemListPayloadType = [];
  settingsPayloadType = {};
  selectedItemsPayloadType = [];

  itemListPayloadSize = [];
  settingsPayloadSize = {};
  selectedItemsPayloadSize = [];

  itemListSecurityType = [];
  settingsSecurityType = {};
  selectedItemsSecurityType = [];

  itemListAuthenticationType = [];
  settingsAuthenticationType = {};
  selectedItemsAuthenticationType = [];

  itemListQosType = [];
  settingsQosType = {};
  selectedItemsQosType = [];

  itemListIotLevel = [];
  settingsIotLevel = {};
  selectedItemsIotLevel = [];

  userForm: FormGroup;


  constructor(private fb: FormBuilder, private restProxy: RestProxy)
  {
    this.createForm();
  }

  ngOnInit()
  {
    // Set the Environment component of the proxy. It will need this
    this.restProxy.setCommunicationComp(this);
  }

  createForm()
  {
    // form has following fields
    this.userForm = this.fb.group(
    {
      name: ['', Validators.required],
      baseType: '',
      communicationType: [[], Validators.required],
      communicationPattern: [[], Validators.required],
      requestType: [[], Validators.required],
      headerSize: [[], Validators.required],
      payloadType:[[], Validators.required],
      payloadSize: [[], Validators.required],
      securityType: [],
      authenticationType: [],
      qosType: [],
      iotLevel: []
    });
  }

  submitForm()
  {
    // show the selected content to the user
    this.sendSelectedValuesToInputWindow(this.userForm.value);

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
   * Print the users inputs
   * @param inputs
   */
  sendSelectedValuesToInputWindow(inputs: any)
  {
      this.inputWindowText = "";
      let self = this;

    for(let key in inputs)
    {
      if (inputs.hasOwnProperty(key))
      {
        // print in a nice way
        self.inputWindowText += key.bold() + ": ";
        // inputs[key] is a dropDownItem, print its itemName value
        if(typeof inputs[key] === 'string' )
        {
          self.inputWindowText += inputs[key];
        }
        else
        {
          for (let dropDownItem of inputs[key])
          {

            self.inputWindowText += dropDownItem["itemName"] + ", ";
          }
        }

        self.inputWindowText += self.newLineStr;
      }
    }
  }


  /**
   * Initialize all the necessary variables. Dropdown elements, settings of drop down, etc..
   * All the values will be default, i.e., every element is selectable
   */
  initCommunication()
  {
    // get names and settings for dropdown elements
    let names = this.restProxy.getNames(this.folderName);
    let settings = this.restProxy.getSettings(this.folderName);

    // default all items, has all the possible fields
    let allItems = this.restProxy.getContent(this.folderName, this.defaultFileName);

    // They are empty, do nothing. Wait for loading.
    if(Object.keys(allItems).length <= 0 || Object.keys(settings).length <= 0 || names.length <= 0)
    {
      return;
    }

    // init dropdown elements, and their settings. At first, everything should be default.
    this.itemListBaseCommunication = names;
    this.settingsBaseCommunication = settings["baseCommunication"];
    this.selectedItemsBaseCommunication = [];

    this.communicationName = "";
    this.baseType = this.defaultBaseType;

    this.itemListCommunicationType = allItems["communicationType"];
    this.settingsCommunicationType = settings["communicationType"];
    this.selectedItemsCommunicationType = [];

    this.itemListCommunicationPattern = allItems["communicationPattern"];
    this.settingsCommunicationPattern = settings["communicationPattern"];
    this.selectedItemsCommunicationPattern = [];

    this.itemListRequestType = allItems["requestType"];
    this.settingsRequestType = settings["requestType"];
    this.selectedItemsRequestType = [];

    this.itemListHeaderSize = allItems["headerSize"];
    this.settingsHeaderSize = settings["headerSize"];
    this.selectedItemsHeaderSize = [];

    this.itemListPayloadType = allItems["payloadType"];
    this.settingsPayloadType = settings["payloadType"];
    this.selectedItemsPayloadType = [];

    this.itemListPayloadSize = allItems["payloadSize"];
    this.settingsPayloadSize = settings["payloadSize"];
    this.selectedItemsPayloadSize = [];

    this.itemListSecurityType = allItems["securityType"];
    this.settingsSecurityType = settings["securityType"];
    this.selectedItemsSecurityType = [];

    this.itemListAuthenticationType = allItems["authenticationType"];
    this.settingsAuthenticationType = settings["authenticationType"];
    this.selectedItemsAuthenticationType = [];

    this.itemListQosType = allItems["qosType"];
    this.settingsQosType = settings["qosType"];
    this.selectedItemsQosType = [];

    this.itemListIotLevel = allItems["iotLevel"];
    this.settingsIotLevel = settings["iotLevel"];
    this.selectedItemsIotLevel = [];
  }


  /**
   * A base type is selected for customization
   * Recalculate all the selectable elements using the base type.
   *
   * @param item
   */
  onItemSelectBaseCommunication(item: any)
  {
    let selectedName = item["itemName"];
    if(selectedName == this.defaultFileName)
    {
      this.initCommunication();
      return;
    }

    let baseItem = this.restProxy.getContent(this.folderName, selectedName);
    let allItems = this.restProxy.getContent(this.folderName, this.defaultFileName);


    // Arrange fields according to the selected base type

    // Give unique random name
    this.communicationName = selectedName + "_COMM_" + Math.random().toString(36).substr(2, 10);
    this.baseType = baseItem["baseType"];


    // Disable the communication type field, if a base type is used to customized.
    // Create copy of config or settings, and then re-assign. Simply changing a field of config does not work.
    let settingsCommunicationTypeCopy = JSON.parse(JSON.stringify(this.settingsCommunicationType));
    settingsCommunicationTypeCopy["disabled"] = true;
    this.settingsCommunicationType = settingsCommunicationTypeCopy;

    // Selectable content will be everything, but selected ones will come from the base item.
    let baseItemForCommType = JSON.parse(JSON.stringify(baseItem));
    this.itemListCommunicationType = baseItem["communicationType"];
    this.selectedItemsCommunicationType = baseItemForCommType["communicationType"];

    let baseItemForCommPattern = JSON.parse(JSON.stringify(baseItem));
    this.itemListCommunicationPattern = baseItem["communicationPattern"];
    this.selectedItemsCommunicationPattern = baseItemForCommPattern["communicationPattern"];

    this.itemListRequestType = baseItem["requestType"];
    this.selectedItemsRequestType = [];

    this.itemListHeaderSize = baseItem["headerSize"];
    this.selectedItemsHeaderSize = [];

    this.itemListPayloadType = baseItem["payloadType"];
    this.selectedItemsPayloadType = [];

    this.itemListPayloadSize = baseItem["payloadSize"];
    this.selectedItemsPayloadSize = [];

    this.itemListSecurityType = baseItem["securityType"];
    this.selectedItemsSecurityType = [];

    this.itemListAuthenticationType = baseItem["authenticationType"];
    this.selectedItemsAuthenticationType = [];

    this.itemListQosType = baseItem["qosType"];
    this.selectedItemsQosType = [];

    this.itemListIotLevel = baseItem["iotLevel"];
    this.selectedItemsIotLevel = [];
  }


  OnItemDeSelectBaseCommunication(item: any)
  {
    // when there is no selected base item, then re-init everything to default.
    this.initCommunication();
  }
}

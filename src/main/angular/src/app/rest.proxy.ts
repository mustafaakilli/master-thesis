
import {Injectable} from "@angular/core";
import {RestService} from "./rest.service";
import {Environment} from "./environment";
import {Communication} from "./communication";
import {HttpParams} from "@angular/common/http";
import {DropDownListItem} from "./dropDownListItem";
import {CreateCommunicationComponent} from "./create-communication/create-communication.component";
import {CreateEnvironmentComponent} from "./create-environment/create-environment.component";
import {AnalyseTransformationComponent} from "./analyse-transformation/analyse-transformation.component";

/**
 * The service is the bridge between rest and the application.
 * It sends get requests to load all the content.
 * It sends post requests to save the content and get the analysis.
 *
 * It loads everything once in the beginning and updates all the components.
 * Then whenever the components need data, it returns from its variables and does not make HTTP requests again.
 *
 * After a new file created, it loads that file additionally to keep its variables updated.
 * Then it updates all the components again.
 *
 */

@Injectable()
export class RestProxy
{
  environments :Environment[];
  communications :Communication[];

  commNames: DropDownListItem[];
  envNames: DropDownListItem[];

  loadSingleOperationState : string;
  loadMultiOperationState : string;

  saveFileOperationState : string;
  analysisOperationState : string;

  settingsCommunications: object;
  settingsEnvironments: object;

  communicationComp : CreateCommunicationComponent;
  environmentComp : CreateEnvironmentComponent;
  analysisComp : AnalyseTransformationComponent;

  // These should be same as the folder and file names in server local folder
  communicationFolder = "communications";
  environmentFolder = "environments";
  settingsFilename = "settingsDropdownElements";

  // These should be same as the variables in RestServlet.java
  jsonErrorAttributeString = "ERROR";
  jsonSuccessAttributeString = "SUCCESS";

  errorState = "error";
  successState = "success";
  notInitializedState = "not initialized!";

  errorNotificationForUI = {"FATAL ERROR" : "Something went wrong. Please check Console and Network tabs."};


  constructor(private restService: RestService)
  {
    this.init();
  }

  init()
  {
    this.loadSingleOperationState = this.notInitializedState;
    this.loadMultiOperationState = this.notInitializedState;
    this.saveFileOperationState = this.notInitializedState;
    this.saveFileOperationState = this.notInitializedState;


    this.environments = [];
    this.communications = [];

    this.settingsCommunications = {};
    this.settingsEnvironments = {};

    this.envNames = [];
    this.commNames = [];

    // At first load all settings and all communications and environments
    this.loadContentSingle(this.communicationFolder, this.settingsFilename);
    this.loadContentSingle(this.environmentFolder, this.settingsFilename);

    this.loadContentAll();
  }

  // Get the component variables, so that call the its functions (init,s etc.)
  setCommunicationComp(comp: CreateCommunicationComponent)
  {
    this.communicationComp = comp;
  }

  setEnvironmentComp(comp: CreateEnvironmentComponent)
  {
    this.environmentComp = comp;
  }

  setAnalysisComp(comp: AnalyseTransformationComponent)
  {
    this.analysisComp = comp;
  }

  /** When there is a new data, update components so that they re-init their variables.
   * It is called once in the beginning and whenever a new file is created.
   */
  private updateAllComponents()
  {
    // If all the content are loaded correctly, then update. Otherwise no need to update them.
    if(this.loadMultiOperationState == this.successState)
    {
      this.communicationComp.initCommunication();
      this.environmentComp.initEnvironment();
      this.analysisComp.initAnalyser();
      console.log("Variables of all components are updated!");
    }
  }

  /**
   * Returns the content of the given filename as Json object.
   * The file is in the local server directory. It is loaded to here at the beginning of the app.
   * Now return the desired content from the local variables.
   *
   * JSON.parse(JSON.stringify(entry)  this lines makes deep copy.
   * Since the return value may bound to selectedItems, this causes update of this element too.
   * Thus a deep copy is needed.
   * For names array and settings, deep copy is not needed, as there is no chance that are modified.
   *
   * @param {string} folderName
   * @param {string} fileName
   * @returns {any}
   */
  getContent(folderName: string, fileName:string) : any
  {
    if(folderName == this.environmentFolder)
    {
      for (let entry of this.environments)
      {
        if (entry["name"] == fileName)
        {
          // make a copy and return copy
          return JSON.parse(JSON.stringify(entry));
        }
      }
      return {};
    }

    else if(folderName == this.communicationFolder)
    {
      for (let entry of this.communications)
      {
        if (entry["name"] == fileName)
        {
          // make a copy and return copy
          return JSON.parse(JSON.stringify(entry));
        }
      }
      return {};
    }
  }

  /**
   * Returns the names in the form of Json object that is needed by the Dropdown element.
   * [{id, itemName}, {}, {}]
   *
   * The the names can be environments names or communication names.
   * They are used to display BaseTypes for customizing.
   *
   * No need for deep copy. There will be no update on this variable.
   *
   * @param {string} folderName
   * @returns {any[]}
   */
  getNames(folderName: string) : any[]
  {
    if(folderName == this.environmentFolder) { return this.envNames;}
    else if(folderName == this.communicationFolder) { return this.commNames;}
  }


  /**
   * Returns the settings for Dropdown elements
   * The settings are saved in the local server. Contents are loaded to here at the beginning
   *
   * No need for deep copy. There will be no update on this variable.
   *
   * @param {string} folderName
   * @returns {any}
   */
  getSettings(folderName: string) : any
  {
    if(folderName == this.environmentFolder) { return this.settingsEnvironments;}
    if(folderName == this.communicationFolder) { return this.settingsCommunications;}
  }

  /**
   * Gets the results of the analysis. The results will be calculated in the backend.
   * Makes a post request with given content and parameters.
   * When there is a result, it calls the sendResultToOutputWindow function of Analysis component.
   * It then updates the output part on the interface.
   *
   * @param {string} env1
   * @param {string} env2
   * @param {string} oldComm
   * @param {object} content
   */
  getAnalysisResult(env1: string, env2: string, oldComm: string, content: object)
  {
    // set the param
    const params = new HttpParams({
      fromObject: {
        env1: env1,
        env2: env2,
        oldComm: oldComm,
        postRequestType: "analyse",
      }
    });

    // call the service
    this.restService.postRequest(content, params).subscribe(
      // the first argument is a function which runs on success
      data =>
      {
        console.log(data);
        this.analysisComp.sendResultToOutputWindow(data);
      },
      // the second argument is a function which runs on error
      err =>
      {
        // The request didn't even reach to the Servlet. There is a fatal error.
        this.analysisComp.sendResultToOutputWindow(this.errorNotificationForUI);
        this.analysisOperationState = this.errorState;
        console.error(err);
      },
      // the third argument is a function which runs on completion
      () =>
      {
        this.analysisOperationState = this.successState;
        console.log('Done analysis.');
      });
  }

  /**
   * Saves the content into a file in the local server
   * Makes the post requests to save the content.
   *
   * When there is a response, send it to the output of the component which is displayed on the interface
   * After saving, loads the content of the file into local variables which will be used in components.
   *
   * Loading is via again REST. If the request is successfully done, then load this content too.
   *
   * @param {string} folderName
   * @param {string} fileName
   * @param {object} content
   */
  saveContent(folderName: string, fileName:string, content:object)
  {
    // set params
    const params = new HttpParams({
      fromObject: {
        fileName: fileName,
        folderName: folderName,
        postRequestType: 'create'
      }
    });

    // call the service
    this.restService.postRequest(content, params).subscribe(
      // the first argument is a function which runs on success
      data =>
      {
        console.log(data);

        if(folderName == this.environmentFolder)
        {
          this.environmentComp.sendResultToOutputWindow(data);
        }
        else if(folderName == this.communicationFolder)
        {
          this.communicationComp.sendResultToOutputWindow(data);
        }

        if(data.hasOwnProperty(this.jsonSuccessAttributeString))
        {
          // If it has "SUCCESS" attribute, then file is valid and saved, so load this file here.
          this.loadContentSingle(folderName, fileName);
        }
      },
      // the second argument is a function which runs on error
      err =>
      {
        // The request didn't even reach to the Servlet. There is a fatal error.
        if(folderName == this.environmentFolder)
        {
          this.environmentComp.sendResultToOutputWindow(this.errorNotificationForUI);
        }
        else if(folderName == this.communicationFolder)
        {
          this.communicationComp.sendResultToOutputWindow(this.errorNotificationForUI);
        }
        this.saveFileOperationState = this.errorState;
        console.error(err);
      },
      // the third argument is a function which runs on completion
      () =>
      {
        this.saveFileOperationState = this.successState;
        console.log('Done saving.');
      });
  }

  /**
   * Makes the GET requests to load the content of the given file..
   *
   * A new created type is requested
   * Settings files are requested
   *
   * If the file is not a settings file, then it updates the components and names arrays.
   *
   * @param {string} folderName
   * @param {string} fileName
   */
  private loadContentSingle(folderName: string, fileName: string)
  {
    // set the params
    const params = new HttpParams({
      fromObject: {
        fileName: fileName,
        folderName: folderName,
        operation: 'single'
      }
    });

    // make the request
    this.restService.getRequest(params).subscribe(
      // the first argument is a function which runs on success
      data =>
      {
        console.log(data);

        if(folderName == this.environmentFolder)
        {
          if(data.hasOwnProperty(this.jsonErrorAttributeString))
          {
            // If it has "ERROR" attribute, then there is something wrong
            // The request is reached but it has errors or exceptions
            this.environmentComp.sendResultToOutputWindow(data);
            this.loadSingleOperationState = this.errorState;
          }
          else
          {
            // Otherwise, JSON content successfully generated and returned.
            // If it is settings file, set the local settings variable
            if(fileName == this.settingsFilename)
            {
              this.settingsEnvironments = data;
            }
            else
            {
              // if it is a standard json content, (Communication or Environment)
              // Then update names array
              this.environments.push(data);
              this.envNames.push({ id: this.envNames.length + 1, itemName: data.name});
            }

            this.updateAllComponents();
          }
        }

        // Do the similar thing for the other folder if it is called
        else if(folderName == this.communicationFolder)
        {
          if(data.hasOwnProperty(this.jsonErrorAttributeString))
          {
            // something went wrong.
            this.communicationComp.sendResultToOutputWindow(data);
            this.loadSingleOperationState = this.errorState;
          }
          else
          {
            if(fileName == this.settingsFilename)
            {
              this.settingsCommunications = data;
            }
            else
            {
              this.communications.push(data);
              this.commNames.push({ id: this.commNames.length + 1, itemName: data.name});
            }

            this.updateAllComponents();
          }
        }

      },
      // the second argument is a function which runs on error
      err =>
      {
        // The request didn't even reach to servlet. It is fatal error.
        if(folderName == this.environmentFolder)
        {
          this.environmentComp.sendResultToOutputWindow(this.errorNotificationForUI);
        }
        else if(folderName == this.communicationFolder)
        {
          this.communicationComp.sendResultToOutputWindow(this.errorNotificationForUI);
        }
        this.loadSingleOperationState = this.errorState;
        console.error(err);
      },
      // the third argument is a function which runs on completion
      () =>
      {
        this.loadSingleOperationState = this.successState;
        console.log('Done loading ' + fileName);
      }
    );
  }


  /**
   * Makes get request to load all the contents
   * Does this for both communications and environments.
   *
   * Updates all the components and names arrays.
   * UpdateComponents will be called twice since it is async. the order is not deterministic.
   * To be safe, after each completion, the updateComponents is called.
   *
   */
  private loadContentAll()
  {
    // set the params
    const paramsEnv = new HttpParams({
      fromObject: {
        folderName: 'environments',
        operation: 'multi'
      }
    });

    // call the service
    this.restService.getRequest(paramsEnv).subscribe(
      // the first argument is a function which runs on success
      data =>
      {
        console.log(data);

        if(data.hasOwnProperty(this.jsonErrorAttributeString))
        {
          // If it has "ERROR" attribute, then there is something wrong
          // The request is reached but it has errors or exceptions
          this.environmentComp.sendResultToOutputWindow(data);
          this.loadMultiOperationState = this.errorState;
        }
        else
        {
          // Otherwise, JSON content successfully generated and returned.
          this.environments = data;
          this.setEnvNames();

          this.loadMultiOperationState = this.successState;
          this.updateAllComponents();
        }
      },
      // the second argument is a function which runs on error
      err =>
      {
        // The request didn't even reach to servlet. It is fatal error.
        this.environmentComp.sendResultToOutputWindow(this.errorNotificationForUI);
        this.loadMultiOperationState = this.errorState;
        console.error(err);
      },
      // the third argument is a function which runs on completion
      () =>
      {
        console.log('Done loading environments.');
      }
    );


    // do the same for the other folder

    // set the param
    const paramsComm = new HttpParams({
      fromObject: {
        folderName: 'communications',
        operation: 'multi'
      }
    });

    // call the service
    this.restService.getRequest(paramsComm).subscribe(
      // the first argument is a function which runs on success
      data =>
      {
        console.log(data);

        if(data.hasOwnProperty(this.jsonErrorAttributeString))
        {
          // something went wrong.
          this.communicationComp.sendResultToOutputWindow(data);
          this.loadMultiOperationState = this.errorState;
        }
        else
        {
          this.communications = data;
          this.setCommNames();

          this.loadMultiOperationState = this.successState;
          this.updateAllComponents();
        }
      },
      // the second argument is a function which runs on error
      err =>
      {
        this.communicationComp.sendResultToOutputWindow(this.errorNotificationForUI);
        this.loadMultiOperationState = this.errorState;
        console.error(err);
      },
      // the third argument is a function which runs on completion
      () =>
      {
        console.log('Done loading communications.');
      }
    );
  }


  /**
   * Sets the names in the form of Json object that is used by Dropdown elements.
   * Gets the names of each communication and make a new json array which is needed by Dropdown
   * [{id, itemName}, {id, itemName}, {}, ..] where id is 1,2,3,.. and itemName is the communication name
   */
  setCommNames()
  {
    let index = 1;
    let self = this;
    self.commNames = [];

    this.communications.forEach(function (value)
    {
      self.commNames.push({id:index, itemName: value.name});
      index += 1;
    });
  }

  /**
   * Same as above.
   * Can be merged into one function but makes a bit dirty.
   */
  setEnvNames()
  {
    let index = 1;
    let self = this;
    self.envNames = [];

    this.environments.forEach(function (value)
    {
      self.envNames.push({id:index, itemName: value.name});
      index += 1;
    });
  }
}

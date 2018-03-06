
# Analysis of Transformation Capabilities Between Communication Types
This project is developed with Intellij IDEA, Angular 5 and Java 8 

## License 
This project is a part of Master Thesis from [IAAS](http://www.iaas.uni-stuttgart.de/) department of [Uni-Stuttgart](https://www.uni-stuttgart.de/). All the rights are reserved. 
The concepts and implementations created in this thesis are compatible with the Apache v2.0 license. 
The source codes are published under the Apache v2.0 license as well as under the Eclipse public license, [Eclipse Contributor Agreement](http://www.eclipse.org/legal/ECA.php)

Copyright (c) 2018 University of Stuttgart

## Information for Users
If you only need to use the application (without extending), then do the followings.
* <b>Install</b>
    * Install [JRE 1.8](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html)
    * Install a Web application container software. (Recommended: [Tomcat 9](https://tomcat.apache.org/download-90.cgi))
    * Download only the war file (transformation-analyzer.war) from the [GitHub](https://github.com/mustafaakilli/master-thesis)
    * Copy the war file to `{tomcat-installed-folder}\webapps\`
    * Double click to the file in `{tomcat-installed-folder}\bin\startup`
    * Type `http://localhost:8080/transformation-analyzer/` in a browser such as Chrome.
* <b>Usage</b>
    * It has three tabs. Analyse, Environments, Communications
    * In the analyse tab:
        * Select old environment, then old communication list will be updated.
        * Selectable content will be from the supported communication list of the old communication.
        * Then select old communication and new environment fields too.
        * Click submit to see the result of the analysis.
    * In the environments tab:
        * A brand new environment can be created.
        * Or an already created environment can be edited or customized.
    * In the create communications tab:
        * A brand new communication can be created.
        * Or an already created communication can be edited or customized.
    * All the files will be saved in the local server path. `{tomcat-installed-folder}\webapps\transformation-analyzer\json_data\`
        * Please carefully read `readme.txt` before doing something there.       
        * You can edit the files.
        * Create files from the UI to avoid copy paste errors.
        * If you deploy a new version, save all the JSON files that are created from UI.
    * For the rest of the information, you can get it interactively from the UI.
    
## Information for Developers
If you only need extend or contribute, then do the followings.
* <b>Setting up the environment</b>
    * Install [JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
    * Install [Intellij IDEA](https://www.jetbrains.com/idea/)
        * Use ultimate version. (Free for students)
        * For community version, install Tomcat manually for Intellij IDEA
    * Install [Node.js and npm](https://www.npmjs.com/get-npm?utm_source=house&utm_medium=homepage&utm_campaign=free%20orgs&utm_term=Install%20npm)
    * Install Angular CLI, open terminal and run `npm install -g @angular/cli`
    * Download all the project files. Go to the `{main-project-folder}\src\main\angular\`
    * Run following command to install all the required modules. `npm install`
        * This step is not necessary, when you build the project, it will run this command for you.
        * However it is recommended to run it here, since it takes too much time to download all modules.
        * Otherwise building for the first time will take too much time.
    * Open the project using Intellij IDEA
    * Create a new Run Configuration in Intellij IDEA
        * On the main menu, Run -> Edit Configurations
        * Click green '+' button, select local. 
        * Select `Tomcat 9` for Application Server
        * In the following part, put this value `http://localhost:8080/transformation-analyzer` to the open in browser section
        * HTTP Port 8080 and JRE should be 1.8
        * Click fix button, on the bottom-right to add an artifact
            * Select `transformationanalyzer:war exploded`
                * Deployment tab will be opened. Write `/transformation-analyzer` to the Application context
            * Optionally, add a war too, if you want to have war output too.
                * Click green + button at the bottom of the window
                * Select build artifacts, and select transformationanalyzer:war
                * Now you will have two output, when you build. The exploded version and war version.
        * Give a name and save. 
    * On the main menu, View -> Tool Windows -> Maven Project 
    * Now Maven menu will be added to the right
    * Click `Install` for building and generating targets. 
        * It will run `npm install` and `npm build`, then it will compile java codes
        * `Compile` from the Maven menu is not enough.
    * After that, target folder will be generated `{main-project-folder}\target`
    * After seeing "build is successful", you can run.
    * No need to deploy, just run and it will open a browser with url `http://localhost:8080/transformation-analyzer`
        * When you click to run, Intellij IDEA will deploy to its local server for you.
    
* <b>Project Layout and important files</b>
    * The source codes are in the folder `{main-project-folder}\src\main`
    * `{main-project-folder}\src\main\angular` is for Angular part (front-end)
        * This has three components. Analyse-Transformation, Create-Environment, Create-Communication
        * For each tab on the UI, there is one component.
        * `rest.service.ts` makes actual requests to the java part (servlet) to get or save content.
        * `rest.proxy.ts` is a bridge between `rest.service` and `components`.
        * `rest.proxy` will load everything once when the application is started. 
        * Components will use `rest.proxy` for all the information and there will be no need for further `GET` operation when components need information. 
            * All the communications and environments are brought to local memory from files.
            * Then all the components will be initialized.
            * All the fields and settings of the DropDown lists will be initialized in the components.
            * See [Angular2 MultiSelect Dropdown](http://cuppalabs.github.io/components/multiselectDropdown/) for details.
            * There you can find example settings and demos. 
    * `{main-project-folder}\src\main\java` is for Java part (back-end)
        * This has two main java files. RestServlet and JsonReaderWriter
        * RestServlet handles all the `GET` and `POST` requests from the front-end.
            * Sends the contents of the files when there is `GET` request.
            * Saves the contents of the `POST` requests into the files.
            * Makes the analysis and returns the results to the front-end.
        * JsonReaderWriter handles all the conversion between Java Objects, JSON Strings and JSON files.
            * Each these three types can be converted to each other. 
            * String <=> File conversions will be used when there is save or load content.
                * String content will be retrieved from front-end and written into file.
                * File content will be converted to String and returned to front-end. 
            * Object <=> File conversions will be used when the analysis is needed.
                * The names of the files will come from front-end and the file content will be converted to Java Class Objects.
                * It is way easier to work with Java Class objects than Java JSON objects. That's why it is created.    
    * `{main-project-folder}\src\main\webapp` contains followings:
      * `json_data` has the default JSON files
      * `angular_dist` has the output of the build and compilation of angular source codes 
      * `WEB-INF` has `web.xml` where you can configure your WebApp.
        * Welcoming file is set as `angular_dist\index.html`
            * That means when you type `http://localhost:8080/transformation-analyzer/` in a browser, the content of the `angular_dist\index.html` file will be loaded.
            * So basically `http://localhost:8080/transformation-analyzer/` and `http://localhost:8080/transformation-analyzer/angular_dist/index.html` are the same.
            * `angular_dist\index.html` has all the references for the compiled scripts. It has `base href="/transformation-analyzer/angular_dist/"`. 
                * This is set from `pom.xml`. 
            * Note that you will see `base href="/"`in the source code folder, `{main-project-folder}\src\main\angular\src\index.html`
                * You don't have to edit anything there. Correct information will be written when compiling.
        * `http://localhost:8080/transformation-analyzer/index.html(index.jsp)` has no content.
      
 

## Further improvements

* In case files are not loaded, retry 5-10 times and notify user.
* Detailed exception and error handling. 
* More flexible folder names and deducting them automatically (now hardcoded at the beginning of the codes).
* Delete unwanted files from interface (now manuel).
* Clear OutputWindow by using a button on the UI (now refreshing page will clear).
* Use CSS instead of Inline HTML.
* Add comments to HTML and CSS files.
* Installing latest versions of NPM and Angular using POM.xml (now it is assumed developer has already installed them).
* Copy generated files into local file system from local server (now manuel)
    - Option 1: Use post-deploy scripts (PowerShell, Bash, etc.) or ..
    - Option 2: Download button on the interface

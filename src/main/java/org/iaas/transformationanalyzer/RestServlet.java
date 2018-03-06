package org.iaas.transformationanalyzer;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The main servlet of the application.
 * It gets the requests (GET or POST)
 *
 * The main duties of this servlet
 * - Get Json content and save as a file.
 * - Get Json file and send as Json content
 * - Do analysis of transformation capabilities.
 */

@WebServlet(name="RestServlet", urlPatterns = {"/RestServlet"})
public class RestServlet extends HttpServlet
{
    String environmentsFolder;
    String communicationsFolder;
    String settingsFileName;
    String jsonDetailsAttributeString;
    String jsonDataUrl;

    JSONObject jsonRequestNotValid;
    JSONObject jsonFileLoadError;
    JSONObject jsonFileCreateError;
    JSONObject jsonFileAnalysisError;

    String jsonErrorAttributeString;
    String jsonSuccessAttributeString;

    String fileSeperator;


    /**
     * Defines some constants.
     * @throws ServletException
     */
    public void init() throws ServletException
    {
        jsonErrorAttributeString = "ERROR";
        jsonSuccessAttributeString = "SUCCESS";
        jsonDetailsAttributeString = "Details";

        environmentsFolder = "environments";
        communicationsFolder = "communications";
        settingsFileName = "settingsDropdownElements";
        jsonDataUrl = getServletContext().getRealPath("/json_data");

        jsonRequestNotValid = new JSONObject().put(jsonErrorAttributeString, "Request is not valid, check parameters.");
        jsonFileLoadError = new JSONObject().put(jsonErrorAttributeString, "Could not load files.");
        jsonFileCreateError = new JSONObject().put(jsonErrorAttributeString, "Could not create the file.");
        jsonFileAnalysisError = new JSONObject().put(jsonErrorAttributeString, "Could not make the analysis.");

        fileSeperator = "\\";
    }


    /**
     * Handles the POST requests.
     * Saves the content into files.
     * Does the analysis.
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        // get body and parameters
        String postBody = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        String postRequestType = request.getParameter("postRequestType");

        String result = jsonRequestNotValid.toString();

        if(postRequestType != null)
        {
            // if it is file creation type of request..
            if(postRequestType.equals("create"))
            {
                String folderName = request.getParameter("folderName");
                String fileName = request.getParameter("fileName");

                if(fileName != null && folderName != null && postBody != null)
                {
                    result = doCreation(folderName, fileName, postBody);
                }
            }

            // if it is analysis type of request..
            else if(postRequestType.equals("analyse"))
            {
                String env1 = request.getParameter("env1");
                String env2 = request.getParameter("env2");
                String oldComm = request.getParameter("oldComm");

                if(env1 != null && env2 != null && oldComm != null)
                {
                    result = doAnalysis(env1, env2, oldComm);
                }
            }
        }

        // set content type and body and make the response.
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(result);
    }

    /**
     * Handles the GET requests.
     * Returns all the JSON files to the Angular part of the application.
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        String result = jsonRequestNotValid.toString();

        // get parameters
        String operation = request.getParameter("operation");
        String folderName = request.getParameter("folderName");

        if(operation != null && folderName != null)
        {
            // if a single file requested..
            if(operation.equals("single"))
            {
                String fileName = request.getParameter("fileName");
                if(fileName != null)
                {
                    result = doGetJsonContentSingleFile(folderName, fileName);
                }
            }

            // if all the files are requested..
            else if(operation.equals("multi"))
            {
                result = doGetJsonContentMultiFile(folderName);
            }
        }

        // set content type and body and make the response.
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(result);
    }

    /**
     * Get the content as Json string from the given folder with give fileName (without json)
     *
     * @param folderName
     * @param fileName
     * @return Sting in Json format.
     */
    private String doGetJsonContentSingleFile(String folderName, String fileName)
    {
        JsonReaderWriter<Communication> communicationJsonReaderWriter = new JsonReaderWriter<Communication>(Communication.class);
        JsonReaderWriter<Environment> environmentJsonReaderWriter = new JsonReaderWriter<Environment>(Environment.class);

        String errorDetails = "";
        String jsonResult = "";

        String fullFilePath = jsonDataUrl + fileSeperator + folderName + fileSeperator + fileName + ".json";
        try
        {
            // convert file into string
            if (folderName.equals(this.communicationsFolder))
            {
                jsonResult = communicationJsonReaderWriter.ConvertJsonFileToJsonSting(fullFilePath);
            }
            else if (folderName.equals(this.environmentsFolder))
            {
                jsonResult = environmentJsonReaderWriter.ConvertJsonFileToJsonSting(fullFilePath);
            }

            return  jsonResult;
        }
        catch (Exception e)
        {
            errorDetails = e.getMessage();
        }

        return jsonFileLoadError.put("Details", errorDetails).toString();
    }

    /**
     * Gets all the content in the given folderName
     *
     * @param folderName
     * @return Sting in Json array format, each item is a file content
     */
    private String doGetJsonContentMultiFile(String folderName)
    {
        JsonReaderWriter<Communication> communicationJsonReaderWriter = new JsonReaderWriter<Communication>(Communication.class);
        JsonReaderWriter<Environment> environmentJsonReaderWriter = new JsonReaderWriter<Environment>(Environment.class);

        String errorDetails = "";

        String jsonResult = "";
        String fullFolderPath = jsonDataUrl + fileSeperator + folderName;
        try
        {
            // get the files and filter to read only *.json and except for settings.json (it has different logic)
            File folder = new File(fullFolderPath);
            if (folder != null)
            {
                File[] listOfFiles = folder.listFiles();
                List<String> tempResult = new ArrayList<String>();

                for (int i = 0; i < listOfFiles.length; i++)
                {
                    if (listOfFiles[i].isFile())
                    {
                        String fileName = listOfFiles[i].getName();
                        if (fileName.contains("json") && !fileName.contains(settingsFileName))
                        {
                            // Convert each file into json string and add to the list
                            if (folderName.equals(this.communicationsFolder))
                            {
                                tempResult.add(communicationJsonReaderWriter.ConvertJsonFileToJsonSting(fullFolderPath + fileSeperator + fileName));
                            }
                            else if (folderName.equals(this.environmentsFolder))
                            {
                                tempResult.add(environmentJsonReaderWriter.ConvertJsonFileToJsonSting(fullFolderPath + fileSeperator + fileName));
                            }
                        }
                    }
                }

                // Make a json array. [json1, json2, ...] where json{i} is a file content.
                jsonResult = "[" + String.join(",", tempResult) + "]";
                return jsonResult;
            }
        }
        catch (Exception e)
        {
            errorDetails = e.getMessage();
        }

        return jsonFileLoadError.put("Details", errorDetails).toString();
    }


    /**
     * Gets the content and writes into a Json file
     *
     * @param folderName
     * @param fileName
     * @param content
     * @return result string of the operation. (Success or Error)
     */
    private String doCreation(String folderName, String fileName, String content)
    {
        String fullFilePath = jsonDataUrl + fileSeperator + folderName + fileSeperator + fileName + ".json";
        JsonReaderWriter<Communication> io = new JsonReaderWriter<Communication>(Communication.class);

        String errorDetails = "";

        try
        {
            io.ConvertJsonStingToJsonFile(fullFilePath, content);

            String result = "File is created in local server!";
            JSONObject jsonObj = new JSONObject().put(jsonSuccessAttributeString, result);

            // Full path can be given, but the escape characters make unreadable.
            String details = "FileName: " + fileName;
            jsonObj.put("Details", details);

            // Convert result stings to Json strings.
            return  jsonObj.toString();
        }
        catch (Exception e)
        {
            errorDetails = e.getMessage();

        }
        return jsonFileCreateError.put("Details", errorDetails).toString();
    }


    /**
     * Gets the name of the environments and the old communication between them.
     *
     * Finds all the supported communications of the both environments and converts those info from file to java object.
     * Working with java objects is way easier than JSON objects. That's why it converts everyting to java object.
     *
     * All the environments and the supported communications of the both will be converted to java object.
     * Old communication is also converted to the java object too.
     *
     * Then the logic starts and evaluates according to the content of the supported communications and old communication.
     *
     * @param env1
     * @param env2
     * @param oldComm
     * @return result of the analysis in Sting (Json format)
     */
    private String doAnalysis(String env1, String env2, String oldComm)
    {
        JSONArray jsonArrayResult = new JSONArray();
        try
        {
            JsonReaderWriter<Communication> communicationJsonReaderWriter = new JsonReaderWriter<Communication>(Communication.class);
            JsonReaderWriter<Environment> environmentJsonReaderWriter = new JsonReaderWriter<Environment>(Environment.class);

            // Get the old communication in object form.
            String oldCommunicationStringFormat = doGetJsonContentSingleFile(communicationsFolder, oldComm);
            Communication oldCommunicationObj = communicationJsonReaderWriter.ConvertJsonStringToJavaObject(oldCommunicationStringFormat);

            // Get the old env in object form.
            String oldEnvironmentStringFormat = doGetJsonContentSingleFile(environmentsFolder, env1);
            Environment oldEnvironmentObj = environmentJsonReaderWriter.ConvertJsonStringToJavaObject(oldEnvironmentStringFormat);

            // Get the new env in object form.
            String newEnvironmentStringFormat = doGetJsonContentSingleFile(environmentsFolder, env2);
            Environment newEnvironmentObj = environmentJsonReaderWriter.ConvertJsonStringToJavaObject(newEnvironmentStringFormat);


            // get all the supported comm types of old and new env in java object form.
            List<Communication> oldEnvironmentSupportedCommObjs = new ArrayList<Communication>();
            List<Communication> newEnvironmentSupportedCommObjs = new ArrayList<Communication>();

            for (DropDownListItem commItem: oldEnvironmentObj.getSupportedCommunications())
            {
                String commName = commItem.getItemName();
                String tempCommStringFormat = doGetJsonContentSingleFile(communicationsFolder, commName);
                Communication tempCommObj = communicationJsonReaderWriter.ConvertJsonStringToJavaObject(tempCommStringFormat);
                oldEnvironmentSupportedCommObjs.add(tempCommObj);
            }

            for (DropDownListItem commItem: newEnvironmentObj.getSupportedCommunications())
            {
                String commName = commItem.getItemName();
                String tempCommStringFormat = doGetJsonContentSingleFile(communicationsFolder, commName);
                Communication tempCommObj = communicationJsonReaderWriter.ConvertJsonStringToJavaObject(tempCommStringFormat);
                newEnvironmentSupportedCommObjs.add(tempCommObj);
            }

            // Now everything is ready, get the results.
            Analyzer analyzer = new Analyzer(oldEnvironmentObj, newEnvironmentObj, oldCommunicationObj, oldEnvironmentSupportedCommObjs, newEnvironmentSupportedCommObjs);
            jsonArrayResult = analyzer.GetAnalysisResult();
        }
        catch (Exception e)
        {
            jsonArrayResult = new JSONArray();
            JSONObject jsonObjLine1 = jsonFileAnalysisError;
            JSONObject jsonObjLine2 = new JSONObject().put("Details", e.getMessage());

            jsonArrayResult.put(jsonObjLine1);
            jsonArrayResult.put(jsonObjLine2);
        }

        String jsonString = jsonArrayResult.toString();
        return jsonString;
    }


}

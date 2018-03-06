package org.iaas.transformationanalyzer;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

/**
 * This is a template class.
 * Usage  JsonReaderWriter<JAVA_CLASS> varName = new JsonReaderWriter<JAVA_CLASS>(JAVA_CLASS.class);
 * See Servlet file for examples.
 *
 * Type of class should be sent as parameter too. Since it is template, compile time does not know about the type.
 * That's why we need to pass the Class type (CLASSNAME.class). This is the cleanest solution.
 *
 * It makes conversion between JAVA_OBJECT, JSON File, and JSON String.
 * All the possible conversions between each of them can be made.
 *
 * The Java Class should have all the fields of the JSON type. All the setters and getters should be implemented.
 *
 * JAVA_OBJECT <=> JSON File
 * JAVA_OBJECT <=> JSON String
 * JSON String <=> JSON File
 *
 *
 */

public class JsonReaderWriter<T>
{
    private Class<T> classTypeExplicit;

    // Classname must be sent as parameter too.
    public JsonReaderWriter(Class<T> classNameParameter)
    {
        this.classTypeExplicit = classNameParameter;
    }


    //region JsonFile-JavaObj Conversion

    /**
     *  Converts the Json file into Java Object.
     *  Simply pass the filepath and it will return the java object.
     */
    public T ConvertJsonFileToJavaObj(String fileName) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        try
        {
            // Convert JSON from file to Object
            T obj = mapper.readValue(new File(fileName), this.classTypeExplicit);
            System.out.println(obj);

            //Pretty print
            String jsonPrettyStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
            //System.out.println(jsonPrettyStr);

            return obj;
        }

        catch (JsonGenerationException e) { e.printStackTrace(); throw new Exception(); }
        catch (JsonMappingException e) { e.printStackTrace(); throw new Exception(); }
        catch (IOException e) { e.printStackTrace(); throw new Exception(); }

        //return null;
    }

    /**
     *  Writes Java objects into file as Json in pretty format.
     *  Simply pass the filepath and the object that is needed to save to the file.
     */
    public void ConvertJavaObjectJsonFile(String fileName, T obj) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        try
        {
            // Convert object to JSON string and save into a file directly
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(fileName), obj);

            //Pretty print
            String jsonPrettyStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
            //System.out.println(jsonPrettyStr);
        }

        catch (JsonGenerationException e) { e.printStackTrace(); throw new Exception(); }
        catch (JsonMappingException e) { e.printStackTrace(); throw new Exception(); }
        catch (IOException e) { e.printStackTrace(); throw new Exception(); }
    }

    //endregion



    //region JsonFile-JsonString Conversion

    /**
     *  Converts Json file into string.
     *  Simply pass the filepath and it will return the file content as string.
     */
    public String ConvertJsonFileToJsonSting(String fileName) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        try
        {
            // Convert JSON string from file to json string
            JsonNode root = mapper.readTree(new File(fileName));
            String jsonStr = mapper.writeValueAsString(root);

            //Pretty print
            String jsonPrettyStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
            //System.out.println(jsonPrettyStr);

            return jsonStr;
        }

        catch (JsonGenerationException e) { e.printStackTrace(); throw new Exception(); }
        catch (JsonMappingException e) { e.printStackTrace(); throw new Exception(); }
        catch (IOException e) { e.printStackTrace(); throw new Exception(); }

        //return null;
    }

    /**
     *  Writes strings into file as Json in pretty format.
     *  Simply pass the filepath and the string that is needed to save to the file.
     */
    public void ConvertJsonStingToJsonFile(String fileName, String jsonStr) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        try
        {
            // Write json string to file
            JsonNode root = mapper.readTree(jsonStr);
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(fileName), root);

            //Pretty print
            String jsonPrettyStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
            //System.out.println(jsonPrettyStr);
        }

        catch (JsonGenerationException e) { e.printStackTrace(); throw new Exception(); }
        catch (JsonMappingException e) { e.printStackTrace(); throw new Exception(); }
        catch (IOException e) { e.printStackTrace(); throw new Exception(); }
    }

    //endregion



    //region JsonString-JavaObj Conversion

    /**
     *  Converts Json content in string to the corresponding java object.
     *  Simply pass the string and it will return the java object.
     */
    public T ConvertJsonStringToJavaObject(String jsonStr) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        try
        {
            // Convert JSON string to Object
            T obj = mapper.readValue(jsonStr, this.classTypeExplicit);

            String jsonPrettyStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
            //System.out.println("Pretty JSON:  " + jsonPrettyStr);

            return obj;
        }

        catch (JsonGenerationException e) { e.printStackTrace(); throw new Exception(); }
        catch (JsonMappingException e) { e.printStackTrace(); throw new Exception(); }
        catch (IOException e) { e.printStackTrace(); throw new Exception(); }

        //return null;
    }

    /**
     *  Converts Java object to Json string.
     *  Simply pass the object and it will return string (json format) of the object
     */
    public String ConvertJavaObjectToJsonString(T obj) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        try
        {
            // Convert object to JSON string
            String jsonStr = mapper.writeValueAsString(obj);

            // Convert object to JSON string and pretty print
            String jsonPrettyStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
            //System.out.println("Pretty JSON:  " + jsonPrettyStr);

            return jsonStr;
        }

        catch (JsonGenerationException e) { e.printStackTrace(); throw new Exception(); }
        catch (JsonMappingException e) { e.printStackTrace(); throw new Exception(); }
        catch (IOException e) { e.printStackTrace(); throw new Exception(); }

        //return null;
    }

    //endregion
}

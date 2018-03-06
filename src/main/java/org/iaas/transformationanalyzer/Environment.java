package org.iaas.transformationanalyzer;

import java.util.ArrayList;
import java.util.List;

/**
 * The class definition for json objects of communication types.
 * It is used to get the JSON content (from file or from string) as a Java object.
 * The Json file or string and the class definition should match.
 * That means, all the fields in JSON file should be defined in the class and vice-versa.
 * All the fields should have setters and getters. The names should be the Java-recommended way.
 * Example : fieldName. Starts with small case and Uppercase for the next words.
 * The getter and setter should be getFieldName, setFieldName. get or set then continue with name of the field name.
 * The first letter should be capital.
 *
 */
public class Environment
{
    private String name;
    private List<DropDownListItem> supportedCommunications;

    public Environment()
    {
        this.supportedCommunications = new ArrayList<DropDownListItem>();
    }

    public Environment(String name, List<DropDownListItem> supportedCommunicationTypes)
    {
        this.name = name;
        this.supportedCommunications = supportedCommunicationTypes;
    }

    public String getName()
    {
        return this.name;
    }

    public void setName(String name)
    {
        this.name = name;
    }


    public List<DropDownListItem> getSupportedCommunications()
    {
        return this.supportedCommunications;
    }

    public void setSupportedCommunications(List<DropDownListItem> supportedCommunications)
    {
        this.supportedCommunications = supportedCommunications;
    }
}

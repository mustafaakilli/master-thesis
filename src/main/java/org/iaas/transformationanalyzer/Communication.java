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
public class Communication
{
    private String name;
    private String baseType;
    private List<DropDownListItem> communicationType;
    private List<DropDownListItem> communicationPattern;
    private List<DropDownListItem> requestType;
    private List<DropDownListItem> headerSize;
    private List<DropDownListItem> payloadType;
    private List<DropDownListItem> payloadSize;
    private List<DropDownListItem> securityType;
    private List<DropDownListItem> authenticationType;
    private List<DropDownListItem> qosType;
    private List<DropDownListItem> iotLevel;


    public Communication()
    {
        this.communicationType = new ArrayList<DropDownListItem>();
        this.communicationPattern = new ArrayList<DropDownListItem>();
        this.requestType = new ArrayList<DropDownListItem>();
        this.headerSize = new ArrayList<DropDownListItem>();
        this.payloadType = new ArrayList<DropDownListItem>();
        this.payloadSize = new ArrayList<DropDownListItem>();
        this.securityType = new ArrayList<DropDownListItem>();
        this.authenticationType = new ArrayList<DropDownListItem>();
        this.qosType = new ArrayList<DropDownListItem>();
        this.iotLevel = new ArrayList<DropDownListItem>();
    }

    public Communication(String name, String baseType, List<DropDownListItem> communicationType, List<DropDownListItem> communicationPattern,
                         List<DropDownListItem> requestType, List<DropDownListItem> headerSize, List<DropDownListItem> payloadType,
                         List<DropDownListItem> payloadSize, List<DropDownListItem> securityType, List<DropDownListItem> authenticationType,
                         List<DropDownListItem> qosType, List<DropDownListItem> iotLevel)
    {
        this.name = name;
        this.baseType = baseType;
        this.communicationType = communicationType;
        this.communicationPattern = communicationPattern;
        this.requestType = requestType;
        this.headerSize = headerSize;
        this.payloadType = payloadType;
        this.payloadSize = payloadSize;
        this.securityType = securityType;
        this.authenticationType = authenticationType;
        this.qosType = qosType;
        this.iotLevel = iotLevel;
    }


    public String getName()
    {
        return this.name;
    }

    public void setName(String name)
    {
        this.name = name;
    }


    public String getBaseType()
    {
        return this.baseType;
    }

    public void setBaseType(String baseType)
    {
        this.baseType = baseType;
    }


    public List<DropDownListItem> getCommunicationType()
    {
        return this.communicationType;
    }

    public void setCommunicationType(List<DropDownListItem> communicationType)
    {
        this.communicationType = communicationType;
    }


    public List<DropDownListItem> getCommunicationPattern()
    {
        return this.communicationPattern;
    }

    public void setCommunicationPattern(List<DropDownListItem> communicationPattern)
    {
        this.communicationPattern = communicationPattern;
    }


    public List<DropDownListItem> getRequestType()
    {
        return this.requestType;
    }

    public void setRequestType(List<DropDownListItem> requestType)
    {
        this.requestType = requestType;
    }


    public List<DropDownListItem> getHeaderSize()
    {
        return this.headerSize;
    }

    public void setHeaderSize(List<DropDownListItem> headerSize)
    {
        this.headerSize = headerSize;
    }


    public List<DropDownListItem> getPayloadType()
    {
        return this.payloadType;
    }

    public void setPayloadType(List<DropDownListItem> payloadType)
    {
        this.payloadType = payloadType;
    }


    public List<DropDownListItem> getPayloadSize()
    {
        return this.payloadSize;
    }

    public void setPayloadSize(List<DropDownListItem> payloadSize)
    {
        this.payloadSize = payloadSize;
    }


    public List<DropDownListItem> getSecurityType()
    {
        return this.securityType;
    }

    public void setSecurityType(List<DropDownListItem> securityType)
    {
        this.securityType = securityType;
    }


    public List<DropDownListItem> getAuthenticationType()
    {
        return this.authenticationType;
    }

    public void setAuthenticationType(List<DropDownListItem> authenticationType)
    {
        this.authenticationType = authenticationType;
    }


    public List<DropDownListItem> getQosType()
    {
        return this.qosType;
    }

    public void setQosType(List<DropDownListItem> qosType)
    {
        this.qosType = qosType;
    }


    public List<DropDownListItem> getIotLevel()
    {
        return this.iotLevel;
    }

    public void setIotLevel(List<DropDownListItem> iotLevel)
    {
        this.iotLevel = iotLevel;
    }

}

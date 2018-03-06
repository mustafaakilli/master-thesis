package org.iaas.transformationanalyzer;

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
public class DropDownListItem
{
    private int id;
    private String itemName;

    public DropDownListItem()
    {
    }

    public DropDownListItem(int id, String itemName)
    {
        this.id = id;
        this.itemName = itemName;
    }

    public int getId()
    {
        return this.id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getItemName()
    {
        return this.itemName;
    }

    public void setItemName(String itemName)
    {
        this.itemName = itemName;
    }
}

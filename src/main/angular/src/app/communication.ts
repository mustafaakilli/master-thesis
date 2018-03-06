import {DropDownListItem} from "./dropdownListItem";

/**
 * Object form of the Json files
 */
export class Communication {
  constructor(
    public name: string,
    public baseType: string,
    public communicationType: DropDownListItem[],
    public communicationPattern: DropDownListItem[],
    public requestType: DropDownListItem[],
    public headerSize: DropDownListItem[],
    public payloadType: DropDownListItem[],
    public payloadSize: DropDownListItem[],
    public securityType: DropDownListItem[],
    public authenticationType: DropDownListItem[],
    public qosType: DropDownListItem[],
    public iotLevel: DropDownListItem[],
  ){}
}

import {DropDownListItem} from "./dropdownListItem";

/**
 * Object form of the Json files
 */
export class Environment {
  constructor(
    public name: string,
    public supportedCommunications: DropDownListItem[]
  ){}
}

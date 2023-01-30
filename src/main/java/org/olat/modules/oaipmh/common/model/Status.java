/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 *
 * Content got modified for OpenOlat Context
 */

package org.olat.modules.oaipmh.common.model;

/*import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "statusType")
@XmlEnum*/
public enum Status {

	//@XmlEnumValue("deleted")
	DELETED("deleted");
	private final String value;

	Status(String v) {
		value = v;
	}

	public static Status fromValue(String v) {
		for (Status c : Status.values()) {
			if (c.value.equals(v)) {
				return c;
			}
		}
		throw new IllegalArgumentException(v);
	}

	public String value() {
		return value;
	}

}

/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 *
 * Content got modified for OpenOlat Context
 */

package org.olat.modules.oaipmh.common.oaidc;

import java.util.HashMap;
import java.util.Map;

public class MetadataItem {
	public static final String XMLLANG = "xml:lang";
	String value;
	Map<String, String> properties = new HashMap<>();

	public void addProperty(String name, String value) {
		//TODO - TDD a better implementation of this
		properties.put(name, value);

	}

	public Object getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;

	}

	public String getProperty(String property) {
		return properties.get(property);
	}

}

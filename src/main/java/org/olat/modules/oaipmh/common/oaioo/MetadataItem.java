/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 *
 * Content got modified for OpenOlat Context
 */

package org.olat.modules.oaipmh.common.oaioo;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Sumit Kapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class MetadataItem {
    String value;
    Map<String, String> properties = new HashMap<>();

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

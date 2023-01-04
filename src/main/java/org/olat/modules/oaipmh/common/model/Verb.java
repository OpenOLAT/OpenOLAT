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

import org.olat.modules.oaipmh.common.xml.XmlWritable;

public interface Verb extends XmlWritable {
    Type getType();

    public static enum Type {
        Identify("Identify"),
        ListMetadataFormats("ListMetadataFormats"),
        ListSets("ListSets"),
        GetRecord("GetRecord"),
        ListIdentifiers("ListIdentifiers"),
        ListRecords("ListRecords");

        private final String value;

        Type(String value) {
            this.value = value;
        }

        public static Type fromValue(String value) {
            for (Type c : Type.values()) {
                if (c.value.equalsIgnoreCase(value)) {
                    return c;
                }
            }
            throw new IllegalArgumentException(value);
        }

        public String displayName() {
            return value;
        }

    }

}

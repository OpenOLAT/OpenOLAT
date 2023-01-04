/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.olat.modules.oaipmh.common.model;

public enum DeletedRecord {

    NO("no"),
    PERSISTENT("persistent"),
    TRANSIENT("transient");

    private final String value;

    DeletedRecord(String value) {
        this.value = value;
    }

    public static DeletedRecord fromValue(String value) {
        for (DeletedRecord c : DeletedRecord.values()) {
            if (c.value.equals(value)) {
                return c;
            }
        }
        throw new IllegalArgumentException(value);
    }

    public String value() {
        return value;
    }


    @Override
    public String toString() {
        return value();
    }
}

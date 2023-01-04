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

import org.olat.modules.oaipmh.common.exceptions.XmlWriteException;
import org.olat.modules.oaipmh.common.oaidc.OAIDCMetadata;
import org.olat.modules.oaipmh.common.xml.EchoElement;
import org.olat.modules.oaipmh.common.xml.XmlWritable;
import org.olat.modules.oaipmh.common.xml.XmlWriter;


public class Description implements XmlWritable {
    protected String value;

    private OAIDCMetadata OAIDCMetadata;

    public Description() {
    }

    public Description(OAIDCMetadata OAIDCMetadata) {
        this.OAIDCMetadata = OAIDCMetadata;
    }

    public Description(String compiledMetadata) {
        value = compiledMetadata;
    }

    public static Description description(OAIDCMetadata metadata) {
        return new Description(metadata);
    }

    public Description withMetadata(OAIDCMetadata OAIDCMetadata) {
        this.OAIDCMetadata = OAIDCMetadata;
        return this;
    }

    public Description withMetadata(String metadata) {
        this.value = metadata;
        return this;
    }

    @Override
    public void write(XmlWriter writer) throws XmlWriteException {
        if (OAIDCMetadata != null) {
            this.OAIDCMetadata.write(writer);
        } else if (this.value != null) {
            EchoElement echo = new EchoElement(value);
            echo.write(writer);
        }
    }

}

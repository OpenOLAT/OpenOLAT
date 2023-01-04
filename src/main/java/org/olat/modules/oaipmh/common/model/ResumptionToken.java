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
import org.olat.modules.oaipmh.common.xml.XmlWritable;
import org.olat.modules.oaipmh.common.xml.XmlWriter;

import javax.xml.stream.XMLStreamException;
import java.util.Date;

import static com.google.common.base.Predicates.isNull;
import static org.olat.modules.oaipmh.common.model.Granularity.Second;

public class ResumptionToken implements XmlWritable {

    private final Value value;
    private Date expirationDate;
    private Long completeListSize;
    private Long cursor;

    public ResumptionToken(Value value) {
        this.value = value;
    }

    public ResumptionToken() {
        this.value = new Value();
    }

    public Value getValue() {
        return value;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public ResumptionToken withExpirationDate(Date value) {
        this.expirationDate = value;
        return this;
    }

    public Long getCompleteListSize() {
        return completeListSize;
    }

    public ResumptionToken withCompleteListSize(long value) {
        this.completeListSize = value;
        return this;
    }

    public Long getCursor() {
        return cursor;
    }

    public ResumptionToken withCursor(long value) {
        this.cursor = value;
        return this;
    }

    @Override
    public void write(XmlWriter writer) throws XmlWriteException {
        try {
            if (this.expirationDate != null)
                writer.writeAttribute("expirationDate", this.expirationDate, Second);
            if (this.completeListSize != null)
                writer.writeAttribute("completeListSize", "" + this.completeListSize);
            if (this.cursor != null)
                writer.writeAttribute("cursor", "" + this.cursor);
            if (this.value != null)
                writer.write(this.value);
        } catch (XMLStreamException e) {
            throw new XmlWriteException(e);
        }
    }

    public static class Value {
        private Long offset;
        private String set;
        private Date from;
        private Date until;
        private String metadataPrefix;

        public boolean isEmpty() {
            return isNull().apply(offset) &&
                    isNull().apply(set) &&
                    isNull().apply(from) &&
                    isNull().apply(until) &&
                    isNull().apply(metadataPrefix);
        }

        public Value withOffset(long integer) {
            this.offset = integer;
            return this;
        }

        public Value withSetSpec(String setSpec) {
            this.set = setSpec;
            return this;
        }

        public Value withFrom(Date from) {
            this.from = from;
            return this;
        }

        public Value withUntil(Date until) {
            this.until = until;
            return this;
        }

        public Value withMetadataPrefix(String metadataPrefix) {
            this.metadataPrefix = metadataPrefix;
            return this;
        }

        public Value next(long sum) {
            return new Value().withSetSpec(set)
                    .withFrom(from)
                    .withUntil(until)
                    .withMetadataPrefix(metadataPrefix)
                    .withOffset((offset == null ? 0 : offset) + sum);
        }

        public Long getOffset() {
            return offset;
        }

        public String getSetSpec() {
            return set;
        }

        public Date getFrom() {
            return from;
        }

        public Date getUntil() {
            return until;
        }

        public String getMetadataPrefix() {
            return metadataPrefix;
        }


        public boolean hasOffset() {
            return offset != null;
        }

        public boolean hasSetSpec() {
            return set != null;
        }

        public boolean hasFrom() {
            return from != null;
        }

        public boolean hasUntil() {
            return until != null;
        }

        public boolean hasMetadataPrefix() {
            return metadataPrefix != null;
        }


    }
}

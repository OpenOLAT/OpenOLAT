/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 *
 * Content got modified for OpenOlat Context
 */

package org.olat.modules.oaipmh.dataprovider.handlers;

import org.olat.modules.oaipmh.common.exceptions.XmlWriteException;
import org.olat.modules.oaipmh.common.model.DeletedRecord;
import org.olat.modules.oaipmh.common.model.Description;
import org.olat.modules.oaipmh.common.model.Identify;
import org.olat.modules.oaipmh.common.xml.XmlWritable;
import org.olat.modules.oaipmh.common.xml.XmlWriter;
import org.olat.modules.oaipmh.dataprovider.exceptions.HandlerException;
import org.olat.modules.oaipmh.dataprovider.exceptions.InternalOAIException;
import org.olat.modules.oaipmh.dataprovider.exceptions.OAIException;
import org.olat.modules.oaipmh.dataprovider.model.Context;
import org.olat.modules.oaipmh.dataprovider.parameters.OAICompiledRequest;
import org.olat.modules.oaipmh.dataprovider.repository.Repository;
import org.olat.modules.oaipmh.dataprovider.repository.RepositoryConfiguration;

import javax.xml.stream.XMLStreamException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;


public class IdentifyHandler extends VerbHandler<Identify> {

    private static final String PROTOCOL_VERSION = "2.0";

    public IdentifyHandler(Context context, Repository repository) {
        super(context, repository);

        // Static validation
        RepositoryConfiguration configuration = getRepository().getConfiguration();
        if (configuration == null)
            throw new InternalOAIException("No repository configuration provided");
        if (configuration.getMaxListSets() <= 0)
            throw new InternalOAIException("The repository configuration must return maxListSets greater then 0");
        if (configuration.getMaxListIdentifiers() <= 0)
            throw new InternalOAIException("The repository configuration must return maxListIdentifiers greater then 0");
        if (configuration.getMaxListRecords() <= 0)
            throw new InternalOAIException("The repository configuration must return maxListRecords greater then 0");
        try {
            if (configuration.getBaseUrl() == null)
                throw new InternalOAIException("The repository configuration must return a valid base url (absolute)");
            new URL(configuration.getBaseUrl());
        } catch (MalformedURLException e) {
            throw new InternalOAIException("The repository configuration must return a valid base url (absolute)", e);
        }
        if (configuration.getDeleteMethod() == null)
            throw new InternalOAIException("The repository configuration must return a valid delete method");
        if (configuration.getRepositoryName() == null)
            throw new InternalOAIException("The repository configuration must return a valid repository name");

    }

    @Override
    public Identify handle(OAICompiledRequest params) throws OAIException, HandlerException {
        Identify identify = new Identify();
        RepositoryConfiguration configuration = getRepository().getConfiguration();
        identify.withBaseURL(configuration.getBaseUrl());
        identify.withRepositoryName(configuration.getRepositoryName());
        identify.withEarliestDatestamp(configuration.getEarliestDate());
        identify.withDeletedRecord(DeletedRecord.valueOf(configuration.getDeleteMethod().name()));

        identify.withGranularity(configuration.getGranularity());
        identify.withProtocolVersion(PROTOCOL_VERSION);
        if (configuration.hasCompressions())
            for (String com : configuration.getCompressions())
                identify.getCompressions().add(com);


        List<String> descriptions = configuration.getDescription();
        if (descriptions == null) {
            try {
                identify.withDescription(new Description(XmlWriter.toString(
                        new DefaultOODescription().withValue("OpenOlat: Das LMS für Wissensvermittlung," +
                        " eTesting & Verwaltung,das sich Ihren Ansprüchen anpasst!"))));
            } catch (XmlWriteException | XMLStreamException e) {
                //
            }
        } else {
            for (String description : descriptions) {
                identify.getDescriptions().add(new Description().withMetadata(description));
            }
        }

        return identify;
    }

    public class DefaultOODescription implements XmlWritable {
        protected String value;

        public String getValue() {
            return value;
        }

        public DefaultOODescription withValue(String value) {
            this.value = value;
            return this;
        }

        @Override
        public void write(XmlWriter writer) throws XmlWriteException {
            try {
                writer.writeStartElement("OpenOlat");
                writer.writeCharacters(getValue());
                writer.writeEndElement();
            } catch (XMLStreamException e) {
                throw new XmlWriteException(e);
            }
        }
    }
}

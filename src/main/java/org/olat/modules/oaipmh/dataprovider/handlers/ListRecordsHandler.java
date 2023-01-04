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
import org.olat.modules.oaipmh.common.model.About;
import org.olat.modules.oaipmh.common.model.Header;
import org.olat.modules.oaipmh.common.model.ListRecords;
import org.olat.modules.oaipmh.common.model.Metadata;
import org.olat.modules.oaipmh.common.model.ResumptionToken;
import org.olat.modules.oaipmh.common.xml.XSLPipeline;
import org.olat.modules.oaipmh.common.xml.XmlWriter;
import org.olat.modules.oaipmh.dataprovider.exceptions.BadArgumentException;
import org.olat.modules.oaipmh.dataprovider.exceptions.CannotDisseminateFormatException;
import org.olat.modules.oaipmh.dataprovider.exceptions.DoesNotSupportSetsException;
import org.olat.modules.oaipmh.dataprovider.exceptions.HandlerException;
import org.olat.modules.oaipmh.dataprovider.exceptions.NoMatchesException;
import org.olat.modules.oaipmh.dataprovider.exceptions.NoMetadataFormatsException;
import org.olat.modules.oaipmh.dataprovider.exceptions.OAIException;
import org.olat.modules.oaipmh.dataprovider.handlers.helpers.ItemHelper;
import org.olat.modules.oaipmh.dataprovider.handlers.helpers.ItemRepositoryHelper;
import org.olat.modules.oaipmh.dataprovider.handlers.helpers.PreconditionHelper;
import org.olat.modules.oaipmh.dataprovider.handlers.helpers.ResumptionTokenHelper;
import org.olat.modules.oaipmh.dataprovider.handlers.helpers.SetRepositoryHelper;
import org.olat.modules.oaipmh.dataprovider.handlers.results.ListItemsResults;
import org.olat.modules.oaipmh.dataprovider.model.Context;
import org.olat.modules.oaipmh.dataprovider.model.Item;
import org.olat.modules.oaipmh.dataprovider.model.MetadataFormat;
import org.olat.modules.oaipmh.dataprovider.model.Set;
import org.olat.modules.oaipmh.dataprovider.parameters.OAICompiledRequest;
import org.olat.modules.oaipmh.dataprovider.repository.Repository;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;


public class ListRecordsHandler extends VerbHandler<ListRecords> {
    private final ItemRepositoryHelper itemRepositoryHelper;
    private final SetRepositoryHelper setRepositoryHelper;

    public ListRecordsHandler(Context context, Repository repository) {
        super(context, repository);
        this.itemRepositoryHelper = new ItemRepositoryHelper(getRepository().getItemRepository());
        this.setRepositoryHelper = new SetRepositoryHelper(getRepository().getSetRepository());
    }

    @Override
    public ListRecords handle(OAICompiledRequest parameters) throws OAIException, HandlerException {
        ListRecords res = new ListRecords();
        int length = getRepository().getConfiguration().getMaxListRecords();

        if (parameters.hasSet() && !getRepository().getSetRepository().supportSets())
            throw new DoesNotSupportSetsException();

        PreconditionHelper.checkMetadataFormat(getContext(), parameters.getMetadataPrefix());

        int offset = getOffset(parameters);
        ListItemsResults result;
        if (!parameters.hasSet()) {
            if (parameters.hasFrom() && !parameters.hasUntil())
                result = itemRepositoryHelper.getItems(getContext(), offset,
                        length, parameters.getMetadataPrefix(),
                        parameters.getFrom());
            else if (!parameters.hasFrom() && parameters.hasUntil())
                result = itemRepositoryHelper.getItemsUntil(getContext(), offset,
                        length, parameters.getMetadataPrefix(),
                        parameters.getUntil());
            else if (parameters.hasFrom() && parameters.hasUntil())
                result = itemRepositoryHelper.getItems(getContext(), offset,
                        length, parameters.getMetadataPrefix(),
                        parameters.getFrom(), parameters.getUntil());
            else
                result = itemRepositoryHelper.getItems(getContext(), offset,
                        length, parameters.getMetadataPrefix());
        } else {
            if (!setRepositoryHelper.exists(getContext(), parameters.getSet()))
                throw new NoMatchesException();
            if (parameters.hasFrom() && !parameters.hasUntil())
                result = itemRepositoryHelper.getItems(getContext(), offset,
                        length, parameters.getMetadataPrefix(),
                        parameters.getSet(), parameters.getFrom());
            else if (!parameters.hasFrom() && parameters.hasUntil())
                result = itemRepositoryHelper.getItemsUntil(getContext(), offset,
                        length, parameters.getMetadataPrefix(),
                        parameters.getSet(), parameters.getUntil());
            else if (parameters.hasFrom() && parameters.hasUntil())
                result = itemRepositoryHelper.getItems(getContext(), offset,
                        length, parameters.getMetadataPrefix(),
                        parameters.getSet(), parameters.getFrom(),
                        parameters.getUntil());
            else
                result = itemRepositoryHelper.getItems(getContext(), offset,
                        length, parameters.getMetadataPrefix(),
                        parameters.getSet(), getRepository().getSetRepository());
        }

        List<Item> results = result.getResults();
        if (results.isEmpty()) throw new NoMatchesException();

        for (Item i : results)
            res.withRecord(this.createRecord(parameters, i));


        ResumptionToken.Value currentResumptionToken = new ResumptionToken.Value();
        if (parameters.hasResumptionToken()) {
            currentResumptionToken = parameters.getResumptionToken();
        } else if (result.hasMore()) {
            currentResumptionToken = parameters.extractResumptionToken();
        }

        ResumptionTokenHelper resumptionTokenHelper = new ResumptionTokenHelper(currentResumptionToken,
                getRepository().getConfiguration().getMaxListRecords());
        res.withResumptionToken(resumptionTokenHelper.resolve(result.hasMore()));

        return res;
    }


    private int getOffset(OAICompiledRequest parameters) {
        if (!parameters.hasResumptionToken())
            return 0;
        if (parameters.getResumptionToken().getOffset() == null)
            return 0;
        return parameters.getResumptionToken().getOffset().intValue();
    }

    private org.olat.modules.oaipmh.common.model.Record createRecord(OAICompiledRequest parameters, Item item)
            throws BadArgumentException, OAIException, NoMetadataFormatsException, CannotDisseminateFormatException {
        MetadataFormat format = getContext().formatForPrefix(parameters.getMetadataPrefix());
        Header header = new Header();
        org.olat.modules.oaipmh.common.model.Record record = new org.olat.modules.oaipmh.common.model.Record().withHeader(header);
        header.withIdentifier(item.getIdentifier());

        ItemHelper itemHelperWrap = new ItemHelper(item, parameters.getMetadataPrefix());

        header.withDatestamp(item.getDatestamp());
        for (Set set : itemHelperWrap.getSets(getContext(), getRepository().getFilterResolver()))
            header.withSetSpec(set.getSpec());
        if (item.isDeleted())
            header.withStatus(Header.Status.DELETED);

        if (!item.isDeleted()) {
            Metadata metadata = null;
            try {
                if (getContext().hasTransformer()) {
                    metadata = new Metadata(toPipeline(item, parameters.getMetadataPrefix())
                            .apply(getContext().getTransformer())
                            .apply(format.getTransformer())
                            .process());
                } else {
                    metadata = new Metadata(toPipeline(item, parameters.getMetadataPrefix())
                            .apply(format.getTransformer())
                            .process());
                }
            } catch (XMLStreamException | TransformerException | IOException | XmlWriteException e) {
                throw new OAIException(e);
            }

            record.withMetadata(metadata);

            if (item.getAbout() != null) {
                for (About about : item.getAbout()) {
                    record.withAbout(about);
                }
            }
        }
        return record;
    }


    private XSLPipeline toPipeline(Item item, String metadataPrefix) throws XmlWriteException, XMLStreamException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        XmlWriter writer = new XmlWriter(output);
        Metadata metadata;
        if (metadataPrefix.equals("oai_dc")) {
            metadata = item.getMetadata(metadataPrefix);
        } else if (metadataPrefix.equals("oai_oo")) {
            metadata = item.getMetadata(metadataPrefix);
        } else {
            return new XSLPipeline(new ByteArrayInputStream(output.toByteArray()), true);
        }

        metadata.write(writer);
        writer.close();
        return new XSLPipeline(new ByteArrayInputStream(output.toByteArray()), true);
    }
}

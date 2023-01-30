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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;

import org.olat.modules.oaipmh.common.exceptions.XmlWriteException;
import org.olat.modules.oaipmh.common.model.About;
import org.olat.modules.oaipmh.common.model.GetRecord;
import org.olat.modules.oaipmh.common.model.Header;
import org.olat.modules.oaipmh.common.model.Metadata;
import org.olat.modules.oaipmh.common.model.Record;
import org.olat.modules.oaipmh.common.xml.XSLPipeline;
import org.olat.modules.oaipmh.common.xml.XmlWriter;
import org.olat.modules.oaipmh.dataprovider.exceptions.CannotDisseminateFormatException;
import org.olat.modules.oaipmh.dataprovider.exceptions.HandlerException;
import org.olat.modules.oaipmh.dataprovider.exceptions.IdDoesNotExistException;
import org.olat.modules.oaipmh.dataprovider.exceptions.OAIException;
import org.olat.modules.oaipmh.dataprovider.model.Context;
import org.olat.modules.oaipmh.dataprovider.model.Item;
import org.olat.modules.oaipmh.dataprovider.model.MetadataFormat;
import org.olat.modules.oaipmh.dataprovider.model.Set;
import org.olat.modules.oaipmh.dataprovider.parameters.OAICompiledRequest;
import org.olat.modules.oaipmh.dataprovider.repository.Repository;


public class GetRecordHandler extends VerbHandler<GetRecord> {
	public GetRecordHandler(Context context, Repository repository) {
		super(context, repository);
	}

	@Override
	public GetRecord handle(OAICompiledRequest parameters) throws OAIException, HandlerException {
		Header header = new Header();
		Record record = new Record().withHeader(header);
		GetRecord result = new GetRecord(record);

		MetadataFormat format = getContext().formatForPrefix(parameters.getMetadataPrefix());
		if (format == null) {
			throw new CannotDisseminateFormatException("Format " + parameters.getMetadataPrefix() + " not applicable to this item");
		}

		Item item = getRepository().getItemRepository().getItem(parameters.getIdentifier());

		if (getContext().hasCondition() &&
				!getContext().getCondition().getFilter(getRepository().getFilterResolver()).isItemShown(item))
			throw new IdDoesNotExistException("This context does not include this item");

		if (format.hasCondition() &&
				!format.getCondition().getFilter(getRepository().getFilterResolver()).isItemShown(item))
			throw new CannotDisseminateFormatException("Format " + parameters.getMetadataPrefix() + " not applicable to this item");


		header.withIdentifier(item.getIdentifier());
		header.withDatestamp(item.getDatestamp());

		for (Set set : getContext().getSets())
			if (set.getCondition().getFilter(getRepository().getFilterResolver()).isItemShown(item))
				header.withSetSpec(set.getSpec());

		for (Set set : item.getSets())
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
				for (About about : item.getAbout())
					record.withAbout(about);
			}
		}
		return result;
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

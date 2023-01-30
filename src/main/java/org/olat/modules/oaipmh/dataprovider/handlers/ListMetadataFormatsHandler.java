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

import java.util.List;

import org.olat.modules.oaipmh.common.model.ListMetadataFormats;
import org.olat.modules.oaipmh.dataprovider.exceptions.HandlerException;
import org.olat.modules.oaipmh.dataprovider.exceptions.InternalOAIException;
import org.olat.modules.oaipmh.dataprovider.exceptions.NoMetadataFormatsException;
import org.olat.modules.oaipmh.dataprovider.exceptions.OAIException;
import org.olat.modules.oaipmh.dataprovider.handlers.helpers.ItemRepositoryHelper;
import org.olat.modules.oaipmh.dataprovider.model.Context;
import org.olat.modules.oaipmh.dataprovider.model.Item;
import org.olat.modules.oaipmh.dataprovider.model.MetadataFormat;
import org.olat.modules.oaipmh.dataprovider.parameters.OAICompiledRequest;
import org.olat.modules.oaipmh.dataprovider.repository.Repository;


public class ListMetadataFormatsHandler extends VerbHandler<ListMetadataFormats> {
	private ItemRepositoryHelper itemRepositoryHelper;

	public ListMetadataFormatsHandler(Context context, Repository repository) {
		super(context, repository);
		itemRepositoryHelper = new ItemRepositoryHelper(repository.getItemRepository());

		// Static validation
		if (getContext().getMetadataFormats() == null ||
				getContext().getMetadataFormats().isEmpty())
			throw new InternalOAIException("The context must expose at least one metadata format");
	}


	@Override
	public ListMetadataFormats handle(OAICompiledRequest params) throws OAIException, HandlerException {
		ListMetadataFormats result = new ListMetadataFormats();

		if (params.hasIdentifier()) {
			Item item = itemRepositoryHelper.getItem(String.valueOf(params.getIdentifier()));
			List<MetadataFormat> metadataFormats = getContext().formatFor(getRepository().getFilterResolver(), item);
			if (metadataFormats.isEmpty())
				throw new NoMetadataFormatsException();
			for (MetadataFormat metadataFormat : metadataFormats) {
				org.olat.modules.oaipmh.common.model.MetadataFormat format = new org.olat.modules.oaipmh.common.model.MetadataFormat()
						.withMetadataPrefix(metadataFormat.getPrefix())
						.withMetadataNamespace(metadataFormat.getNamespace())
						.withSchema(metadataFormat.getSchemaLocation());
				result.withMetadataFormat(format);
			}
		} else {
			for (MetadataFormat metadataFormat : getContext().getMetadataFormats()) {
				org.olat.modules.oaipmh.common.model.MetadataFormat format = new org.olat.modules.oaipmh.common.model.MetadataFormat()
						.withMetadataPrefix(metadataFormat.getPrefix())
						.withMetadataNamespace(metadataFormat.getNamespace())
						.withSchema(metadataFormat.getSchemaLocation());
				result.withMetadataFormat(format);
			}
		}

		return result;
	}

}

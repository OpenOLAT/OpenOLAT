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

import org.olat.modules.oaipmh.common.model.Header;
import org.olat.modules.oaipmh.common.model.ListIdentifiers;
import org.olat.modules.oaipmh.common.model.ResumptionToken;
import org.olat.modules.oaipmh.dataprovider.exceptions.BadArgumentException;
import org.olat.modules.oaipmh.dataprovider.exceptions.DoesNotSupportSetsException;
import org.olat.modules.oaipmh.dataprovider.exceptions.HandlerException;
import org.olat.modules.oaipmh.dataprovider.exceptions.InternalOAIException;
import org.olat.modules.oaipmh.dataprovider.exceptions.NoMatchesException;
import org.olat.modules.oaipmh.dataprovider.exceptions.NoMetadataFormatsException;
import org.olat.modules.oaipmh.dataprovider.exceptions.OAIException;
import org.olat.modules.oaipmh.dataprovider.handlers.helpers.ItemRepositoryHelper;
import org.olat.modules.oaipmh.dataprovider.handlers.helpers.PreconditionHelper;
import org.olat.modules.oaipmh.dataprovider.handlers.helpers.ResumptionTokenHelper;
import org.olat.modules.oaipmh.dataprovider.handlers.results.ListItemIdentifiersResult;
import org.olat.modules.oaipmh.dataprovider.model.Context;
import org.olat.modules.oaipmh.dataprovider.model.ItemIdentifier;
import org.olat.modules.oaipmh.dataprovider.model.MetadataFormat;
import org.olat.modules.oaipmh.dataprovider.model.Set;
import org.olat.modules.oaipmh.dataprovider.parameters.OAICompiledRequest;
import org.olat.modules.oaipmh.dataprovider.repository.Repository;


public class ListIdentifiersHandler extends VerbHandler<ListIdentifiers> {
	private final ItemRepositoryHelper itemRepositoryHelper;

	public ListIdentifiersHandler(Context context, Repository repository) {
		super(context, repository);
		this.itemRepositoryHelper = new ItemRepositoryHelper(repository.getItemRepository());
	}


	@Override
	public ListIdentifiers handle(OAICompiledRequest parameters) throws OAIException, HandlerException {
		ListIdentifiers result = new ListIdentifiers();

		if (parameters.hasSet() && !getRepository().getSetRepository().supportSets())
			throw new DoesNotSupportSetsException();

		PreconditionHelper.checkMetadataFormat(getContext(), parameters.getMetadataPrefix());

		int length = getRepository().getConfiguration().getMaxListIdentifiers();
		int offset = getOffset(parameters);
		ListItemIdentifiersResult listItemIdentifiersResult;
		if (!parameters.hasSet()) {
			if (parameters.hasFrom() && !parameters.hasUntil())
				listItemIdentifiersResult = itemRepositoryHelper.getItemIdentifiers(getContext(), offset, length,
						parameters.getMetadataPrefix(), parameters.getFrom());
			else if (!parameters.hasFrom() && parameters.hasUntil())
				listItemIdentifiersResult = itemRepositoryHelper.getItemIdentifiersUntil(getContext(), offset, length,
						parameters.getMetadataPrefix(), parameters.getUntil());
			else if (parameters.hasFrom() && parameters.hasUntil())
				listItemIdentifiersResult = itemRepositoryHelper.getItemIdentifiers(getContext(), offset, length,
						parameters.getMetadataPrefix(), parameters.getFrom(),
						parameters.getUntil());
			else
				listItemIdentifiersResult = itemRepositoryHelper.getItemIdentifiers(getContext(), offset, length,
						parameters.getMetadataPrefix());
		} else {
			if (!getRepository().getSetRepository().exists(parameters.getSet()) && !getContext().hasSet(parameters.getSet()))
				throw new NoMatchesException();

			if (parameters.hasFrom() && !parameters.hasUntil())
				listItemIdentifiersResult = itemRepositoryHelper.getItemIdentifiers(getContext(), offset, length,
						parameters.getMetadataPrefix(), parameters.getSet(),
						parameters.getFrom());
			else if (!parameters.hasFrom() && parameters.hasUntil())
				listItemIdentifiersResult = itemRepositoryHelper.getItemIdentifiersUntil(getContext(), offset, length,
						parameters.getMetadataPrefix(), parameters.getSet(),
						parameters.getUntil());
			else if (parameters.hasFrom() && parameters.hasUntil())
				listItemIdentifiersResult = itemRepositoryHelper.getItemIdentifiers(getContext(), offset, length,
						parameters.getMetadataPrefix(), parameters.getSet(),
						parameters.getFrom(), parameters.getUntil());
			else
				listItemIdentifiersResult = itemRepositoryHelper.getItemIdentifiers(getContext(), offset, length,
						parameters.getMetadataPrefix(), parameters.getSet());
		}

		List<ItemIdentifier> itemIdentifiers = listItemIdentifiersResult.getResults();
		if (itemIdentifiers.isEmpty()) throw new NoMatchesException();

		for (ItemIdentifier itemIdentifier : itemIdentifiers)
			result.getHeaders().add(createHeader(parameters, itemIdentifier));

		ResumptionToken.Value currentResumptionToken = new ResumptionToken.Value();
		if (parameters.hasResumptionToken()) {
			currentResumptionToken = parameters.getResumptionToken();
		} else if (listItemIdentifiersResult.hasMore()) {
			currentResumptionToken = parameters.extractResumptionToken();
		}

		ResumptionTokenHelper resumptionTokenHelper = new ResumptionTokenHelper(currentResumptionToken,
				getRepository().getConfiguration().getMaxListIdentifiers());
		result.withResumptionToken(resumptionTokenHelper.resolve(listItemIdentifiersResult.hasMore()));

		return result;
	}

	private int getOffset(OAICompiledRequest parameters) {
		if (!parameters.hasResumptionToken())
			return 0;
		if (parameters.getResumptionToken().getOffset() == null)
			return 0;
		return parameters.getResumptionToken().getOffset().intValue();
	}


	private Header createHeader(OAICompiledRequest parameters,
								ItemIdentifier itemIdentifier) throws BadArgumentException,
			OAIException,
			NoMetadataFormatsException {
		MetadataFormat format = getContext().formatForPrefix(parameters
				.getMetadataPrefix());
		if (!itemIdentifier.isDeleted() && !canDisseminate(itemIdentifier, format))
			throw new InternalOAIException("The item repository is currently providing items which cannot be disseminated with format " + format.getPrefix());

		Header header = new Header();
		header.withDatestamp(itemIdentifier.getDatestamp());
		header.withIdentifier(itemIdentifier.getIdentifier());
		if (itemIdentifier.isDeleted())
			header.withStatus(Header.Status.DELETED);

		for (Set set : getContext().getSets())
			if (set.getCondition().getFilter(getRepository().getFilterResolver()).isItemShown(itemIdentifier))
				header.withSetSpec(set.getSpec());

		for (Set set : itemIdentifier.getSets())
			header.withSetSpec(set.getSpec());

		return header;
	}

	private boolean canDisseminate(ItemIdentifier itemIdentifier, MetadataFormat format) {
		return !format.hasCondition() ||
				format.getCondition().getFilter(getRepository().getFilterResolver()).isItemShown(itemIdentifier);
	}
}

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

import org.olat.modules.oaipmh.common.model.ListSets;
import org.olat.modules.oaipmh.common.model.ResumptionToken;
import org.olat.modules.oaipmh.dataprovider.exceptions.DoesNotSupportSetsException;
import org.olat.modules.oaipmh.dataprovider.exceptions.HandlerException;
import org.olat.modules.oaipmh.dataprovider.exceptions.NoMatchesException;
import org.olat.modules.oaipmh.dataprovider.exceptions.OAIException;
import org.olat.modules.oaipmh.dataprovider.handlers.helpers.ResumptionTokenHelper;
import org.olat.modules.oaipmh.dataprovider.handlers.helpers.SetRepositoryHelper;
import org.olat.modules.oaipmh.dataprovider.handlers.results.ListSetsResult;
import org.olat.modules.oaipmh.dataprovider.model.Context;
import org.olat.modules.oaipmh.dataprovider.model.Set;
import org.olat.modules.oaipmh.dataprovider.parameters.OAICompiledRequest;
import org.olat.modules.oaipmh.dataprovider.repository.Repository;

import java.util.List;


public class ListSetsHandler extends VerbHandler<ListSets> {
    private final SetRepositoryHelper setRepositoryHelper;

    public ListSetsHandler(Context context, Repository repository) {
        super(context, repository);
        this.setRepositoryHelper = new SetRepositoryHelper(getRepository().getSetRepository());
    }


    @Override
    public ListSets handle(OAICompiledRequest parameters) throws OAIException, HandlerException {
        ListSets result = new ListSets();
        if (!getRepository().getSetRepository().supportSets())
            throw new DoesNotSupportSetsException();

        int length = getRepository().getConfiguration().getMaxListSets();
        ListSetsResult listSetsResult = setRepositoryHelper.getSets(getContext(), getOffset(parameters), length);
        List<Set> sets = listSetsResult.getResults();

        if (sets.isEmpty() && parameters.getResumptionToken().isEmpty())
            throw new NoMatchesException();

        if (sets.size() > length)
            sets = sets.subList(0, length);

        for (Set set : sets) {
            result.getSets().add(set.toOAIPMH());
        }

        ResumptionToken.Value currentResumptionToken = new ResumptionToken.Value();
        if (parameters.hasResumptionToken()) {
            currentResumptionToken = parameters.getResumptionToken();
        } else if (listSetsResult.hasMore()) {
            currentResumptionToken = parameters.extractResumptionToken();
        }

        ResumptionTokenHelper resumptionTokenHelper = new ResumptionTokenHelper(currentResumptionToken,
                getRepository().getConfiguration().getMaxListSets());
        result.withResumptionToken(resumptionTokenHelper.resolve(listSetsResult.hasMore()));

        return result;
    }

    private int getOffset(OAICompiledRequest parameters) {
        if (!parameters.hasResumptionToken())
            return 0;
        if (parameters.getResumptionToken().getOffset() == null)
            return 0;
        return parameters.getResumptionToken().getOffset().intValue();
    }

}

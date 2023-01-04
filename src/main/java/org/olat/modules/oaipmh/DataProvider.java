/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 *
 * Content slightly modified for OO Context
 */
package org.olat.modules.oaipmh;

import com.lyncode.builder.Builder;
import org.olat.modules.oaipmh.common.exceptions.InvalidResumptionTokenException;
import org.olat.modules.oaipmh.common.model.OAIPMH;
import org.olat.modules.oaipmh.common.model.Request;
import org.olat.modules.oaipmh.common.services.api.DateProvider;
import org.olat.modules.oaipmh.common.services.impl.UTCDateProvider;
import org.olat.modules.oaipmh.dataprovider.exceptions.BadArgumentException;
import org.olat.modules.oaipmh.dataprovider.exceptions.BadResumptionToken;
import org.olat.modules.oaipmh.dataprovider.exceptions.DuplicateDefinitionException;
import org.olat.modules.oaipmh.dataprovider.exceptions.HandlerException;
import org.olat.modules.oaipmh.dataprovider.exceptions.IllegalVerbException;
import org.olat.modules.oaipmh.dataprovider.exceptions.OAIException;
import org.olat.modules.oaipmh.dataprovider.exceptions.UnknownParameterException;
import org.olat.modules.oaipmh.dataprovider.handlers.GetRecordHandler;
import org.olat.modules.oaipmh.dataprovider.handlers.IdentifyHandler;
import org.olat.modules.oaipmh.dataprovider.handlers.ListIdentifiersHandler;
import org.olat.modules.oaipmh.dataprovider.handlers.ListMetadataFormatsHandler;
import org.olat.modules.oaipmh.dataprovider.handlers.ListRecordsHandler;
import org.olat.modules.oaipmh.dataprovider.handlers.ListSetsHandler;
import org.olat.modules.oaipmh.dataprovider.model.Context;
import org.olat.modules.oaipmh.dataprovider.parameters.OAICompiledRequest;
import org.olat.modules.oaipmh.dataprovider.parameters.OAIRequest;
import org.olat.modules.oaipmh.dataprovider.repository.Repository;

import static org.olat.modules.oaipmh.dataprovider.parameters.OAIRequest.Parameter.From;
import static org.olat.modules.oaipmh.dataprovider.parameters.OAIRequest.Parameter.Identifier;
import static org.olat.modules.oaipmh.dataprovider.parameters.OAIRequest.Parameter.MetadataPrefix;
import static org.olat.modules.oaipmh.dataprovider.parameters.OAIRequest.Parameter.ResumptionToken;
import static org.olat.modules.oaipmh.dataprovider.parameters.OAIRequest.Parameter.Set;
import static org.olat.modules.oaipmh.dataprovider.parameters.OAIRequest.Parameter.Until;
import static org.olat.modules.oaipmh.dataprovider.parameters.OAIRequest.Parameter.Verb;

public class DataProvider {

    private final IdentifyHandler identifyHandler;
    private final GetRecordHandler getRecordHandler;
    private final ListSetsHandler listSetsHandler;
    private final ListRecordsHandler listRecordsHandler;
    private final ListIdentifiersHandler listIdentifiersHandler;
    private final ListMetadataFormatsHandler listMetadataFormatsHandler;
    private final Repository repository;
    private final DateProvider dateProvider;

    public static DataProvider dataProvider (Context context, Repository repository) {
        return new DataProvider(context, repository);
    }

    public DataProvider(Context context, Repository repository) {
        this.repository = repository;
        this.dateProvider = new UTCDateProvider();

        this.identifyHandler = new IdentifyHandler(context, repository);
        this.listSetsHandler = new ListSetsHandler(context, repository);
        this.listMetadataFormatsHandler = new ListMetadataFormatsHandler(context, repository);
        this.listRecordsHandler = new ListRecordsHandler(context, repository);
        this.listIdentifiersHandler = new ListIdentifiersHandler(context, repository);
        this.getRecordHandler = new GetRecordHandler(context, repository);
    }

    public OAIPMH handle(Builder<OAIRequest> builder) throws OAIException {
        return handle(builder.build());
    }

    public OAIPMH handle(OAIRequest requestParameters) throws OAIException {
        Request request = new Request(repository.getConfiguration().getBaseUrl())
                .withVerbType(requestParameters.get(Verb))
                .withResumptionToken(requestParameters.get(ResumptionToken))
                .withIdentifier(requestParameters.get(Identifier))
                .withMetadataPrefix(requestParameters.get(MetadataPrefix))
                .withSet(requestParameters.get(Set))
                .withFrom(requestParameters.get(From))
                .withUntil(requestParameters.get(Until));

        OAIPMH response = new OAIPMH()
                .withRequest(request)
                .withResponseDate(dateProvider.now());
        try {
            OAICompiledRequest parameters = compileParameters(requestParameters);

            switch (request.getVerbType()) {
                case Identify:
                    response.withVerb(identifyHandler.handle(parameters));
                    break;
                case ListSets:
                    response.withVerb(listSetsHandler.handle(parameters));
                    break;
                case ListMetadataFormats:
                    response.withVerb(listMetadataFormatsHandler.handle(parameters));
                    break;
                case GetRecord:
                    response.withVerb(getRecordHandler.handle(parameters));
                    break;
                case ListIdentifiers:
                    response.withVerb(listIdentifiersHandler.handle(parameters));
                    break;
                case ListRecords:
                    response.withVerb(listRecordsHandler.handle(parameters));
                    break;
            }
        } catch (HandlerException e) {
            //
        }

        return response;
    }

    private OAICompiledRequest compileParameters(OAIRequest requestParameters) throws IllegalVerbException, UnknownParameterException, BadArgumentException, DuplicateDefinitionException, BadResumptionToken {
        try {
            return requestParameters.compile();
        } catch (InvalidResumptionTokenException e) {
            throw new BadResumptionToken("The resumption token is invalid");
        }
    }

}

/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 *
 * Content got modified for OpenOlat Context
 */

package org.olat.modules.oaipmh.dataprovider.builder;

import com.lyncode.builder.Builder;
import org.olat.modules.oaipmh.common.exceptions.InvalidResumptionTokenException;
import org.olat.modules.oaipmh.common.model.Verb;
import org.olat.modules.oaipmh.common.services.impl.UTCDateProvider;
import org.olat.modules.oaipmh.dataprovider.exceptions.BadArgumentException;
import org.olat.modules.oaipmh.dataprovider.exceptions.DuplicateDefinitionException;
import org.olat.modules.oaipmh.dataprovider.exceptions.IllegalVerbException;
import org.olat.modules.oaipmh.dataprovider.exceptions.UnknownParameterException;
import org.olat.modules.oaipmh.dataprovider.parameters.OAICompiledRequest;
import org.olat.modules.oaipmh.dataprovider.parameters.OAIRequest;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public class OAIRequestParametersBuilder implements Builder<OAIRequest> {
    private final UTCDateProvider utcDateProvider = new UTCDateProvider();
    private final Map<String, List<String>> params = new HashMap<>();

    public OAIRequestParametersBuilder with(String name, String... values) {
        if (values == null || (values.length > 0 && values[0] == null))
            return without(name);
        if (!params.containsKey(name))
            params.put(name, new ArrayList<>());

        params.get(name).addAll(asList(values));
        return this;
    }



    @Override
    public OAIRequest build() {
        return new OAIRequest(params);
    }

    public OAIRequestParametersBuilder withVerb(String verb) {
        return with("verb", verb);
    }

    public OAIRequestParametersBuilder withVerb(Verb.Type verb) {
        return with("verb", verb.displayName());
    }

    public OAIRequestParametersBuilder withMetadataPrefix(String mdp) {
        return with("metadataPrefix", mdp);
    }

    public OAIRequestParametersBuilder withFrom(Date date) {
        if (date != null)
            return with("from", utcDateProvider.format(date));
        else
            return without("from");
    }

    private OAIRequestParametersBuilder without(String field) {
        params.remove(field);
        return this;
    }

    public OAIRequestParametersBuilder withUntil(Date date) {
        if (date != null)
            return with("until", utcDateProvider.format(date));
        else
            return without("until");
    }

    public OAIRequestParametersBuilder withIdentifier(String identifier) {
        return with("identifier", identifier);
    }

    public OAIRequestParametersBuilder withResumptionToken(String resumptionToken) {
        return with("resumptionToken", resumptionToken);
    }

    public OAICompiledRequest compile() throws BadArgumentException, InvalidResumptionTokenException, UnknownParameterException, IllegalVerbException, DuplicateDefinitionException {
        return this.build().compile();
    }

    public OAIRequestParametersBuilder withSet(String set) {
        return with("set", set);
    }
}

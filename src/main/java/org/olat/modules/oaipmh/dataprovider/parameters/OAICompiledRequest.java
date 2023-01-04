/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 *
 * Content got modified for OpenOlat Context
 */

package org.olat.modules.oaipmh.dataprovider.parameters;

import com.lyncode.builder.Builder;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.olat.modules.oaipmh.common.exceptions.InvalidResumptionTokenException;
import org.olat.modules.oaipmh.common.model.ResumptionToken;
import org.olat.modules.oaipmh.common.services.api.DateProvider;
import org.olat.modules.oaipmh.common.services.api.ResumptionTokenFormat;
import org.olat.modules.oaipmh.common.services.impl.SimpleResumptionTokenFormat;
import org.olat.modules.oaipmh.common.services.impl.UTCDateProvider;
import org.olat.modules.oaipmh.dataprovider.exceptions.BadArgumentException;
import org.olat.modules.oaipmh.dataprovider.exceptions.DuplicateDefinitionException;
import org.olat.modules.oaipmh.dataprovider.exceptions.IllegalVerbException;
import org.olat.modules.oaipmh.dataprovider.exceptions.UnknownParameterException;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.olat.modules.oaipmh.common.model.Verb.Type;
import static org.olat.modules.oaipmh.dataprovider.parameters.OAIRequest.Parameter.From;
import static org.olat.modules.oaipmh.dataprovider.parameters.OAIRequest.Parameter.Identifier;
import static org.olat.modules.oaipmh.dataprovider.parameters.OAIRequest.Parameter.MetadataPrefix;
import static org.olat.modules.oaipmh.dataprovider.parameters.OAIRequest.Parameter.ResumptionToken;
import static org.olat.modules.oaipmh.dataprovider.parameters.OAIRequest.Parameter.Set;
import static org.olat.modules.oaipmh.dataprovider.parameters.OAIRequest.Parameter.Until;

/**
 * @author Development @ Lyncode
 * @version 3.1.0
 */
public class OAICompiledRequest {
    public static OAICompiledRequest compile (OAIRequest request) throws BadArgumentException, InvalidResumptionTokenException, UnknownParameterException, IllegalVerbException, DuplicateDefinitionException {
        return new OAICompiledRequest(request);
    }
    public static OAICompiledRequest compile (OAIRequest request, ResumptionTokenFormat formatter) throws BadArgumentException, InvalidResumptionTokenException, UnknownParameterException, IllegalVerbException, DuplicateDefinitionException {
        return new OAICompiledRequest(request, formatter);
    }
    public static OAICompiledRequest compile (Builder<OAIRequest> request) throws BadArgumentException, InvalidResumptionTokenException, UnknownParameterException, IllegalVerbException, DuplicateDefinitionException {
        return new OAICompiledRequest(request.build());
    }
    public static OAICompiledRequest compile (Builder<OAIRequest> request, ResumptionTokenFormat formatter) throws BadArgumentException, InvalidResumptionTokenException, UnknownParameterException, IllegalVerbException, DuplicateDefinitionException {
        return new OAICompiledRequest(request.build(), formatter);
    }

    private static DateProvider dateProvider = new UTCDateProvider();

    private Type verbType;
    private ResumptionToken.Value resumptionToken = null;
    private String identifier;
    private String metadataPrefix;
    private String set;
    private Date until;
    private Date from;

    private OAICompiledRequest(OAIRequest request)
            throws IllegalVerbException, BadArgumentException,
            UnknownParameterException, DuplicateDefinitionException, InvalidResumptionTokenException {
        this(request, new SimpleResumptionTokenFormat());
    }

    private OAICompiledRequest(OAIRequest request, ResumptionTokenFormat resumptionTokenFormat)
            throws IllegalVerbException, BadArgumentException,
            UnknownParameterException, DuplicateDefinitionException, InvalidResumptionTokenException {

        Collection<String> parameterNames = request.getParameterNames();
        if (isTrueThat(parameterNames, not(hasItem(equalTo("verb")))))
            throw new IllegalVerbException("No verb provided");

        for (String parameterName : parameterNames)
            if (isTrueThat(parameterName, not(in("verb", "from", "until", "metadataPrefix", "identifier", "set", "resumptionToken"))))
                throw new UnknownParameterException("Unknown parameter '" + parameterName + "'");

        String until = request.getString(Until);
        String from = request.getString(From);
        if (isTrueThat(until, is(not(nullValue())))
                && isTrueThat(from, is(not(nullValue())))
                && from.length() != until.length())
            throw new BadArgumentException("Distinct granularities provided for until and from parameters");


        this.verbType = request.getVerb();
        this.from = request.getDate(From);
        this.until = request.getDate(Until);
        this.metadataPrefix = request.getString(MetadataPrefix);
        this.set = request.getString(Set);

        if (request.getString(Identifier) != null) {
            this.identifier = request.getString(Identifier);
        }

        if (request.has(ResumptionToken))
            this.resumptionToken = resumptionTokenFormat.parse(request.getString(ResumptionToken));
        else
            this.resumptionToken = new ResumptionToken.Value();


        this.validate();
        this.loadResumptionToken(this.resumptionToken);
    }

    private Matcher<String> in(final String... possibilities) {
        return new TypeSafeMatcher<String>() {
            @Override
            protected boolean matchesSafely(String item) {
                for (String possibility : possibilities)
                    if (possibility.equals(item))
                        return true;

                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("in");
            }
        };
    }

    private <T> boolean isTrueThat(T value, Matcher<T> matcher) {
        return matcher.matches(value);
    }

    public boolean hasResumptionToken() {
        return this.resumptionToken != null && !this.resumptionToken.isEmpty();
    }

    public String getIdentifier() {
        return identifier;
    }

    public boolean hasIdentifier() {
        return (this.identifier != null);
    }

    public String getMetadataPrefix() {
        return metadataPrefix;
    }

    public boolean hasMetadataPrefix() {
        return (this.metadataPrefix != null);
    }

    public String getSet() {
        return set;
    }

    public boolean hasSet() {
        return (this.set != null);
    }

    public boolean hasFrom() {
        return (this.from != null);
    }

    public boolean hasUntil() {
        return (this.until != null);
    }

    private Date getDate(String date, String param) throws BadArgumentException {
        if (date == null) return null;
        try {
            return dateProvider.parse(date);
        } catch (ParseException e) {
            throw new BadArgumentException("The " + param
                    + " parameter given is not valid");
        }
    }

    public Date getFrom() {
        return from;
    }

    public ResumptionToken.Value getResumptionToken() {
        return resumptionToken;
    }

    public Date getUntil() {
        return until;
    }

    public Type getVerbType() {
        return verbType;
    }

    private void validate() throws IllegalVerbException, BadArgumentException {
        if (this.hasResumptionToken()) {
            if (this.hasFrom() || this.hasSet() || this.hasUntil()
                    || this.hasMetadataPrefix())
                throw new BadArgumentException(
                        "ResumptionToken cannot be sent together with from, until, metadataPrefix or set parameters");
        }

        switch (this.getVerbType()) {
            case Identify:
                if (this.hasIdentifier() || this.hasResumptionToken()
                        || this.hasSet() || this.hasMetadataPrefix()
                        || this.hasFrom() || this.hasUntil())
                    throw new BadArgumentException(
                            "Identify verb does not accept any extra parameter");
                break;
            case ListMetadataFormats:
                if (this.hasResumptionToken() || this.hasSet()
                        || this.hasMetadataPrefix() || this.hasFrom()
                        || this.hasUntil())
                    throw new BadArgumentException(
                            "ListMetadataFormats verb only accepts one optional parameter - identifier");
                break;
            case ListSets:
                if (this.hasIdentifier() || this.hasSet()
                        || this.hasMetadataPrefix() || this.hasFrom()
                        || this.hasUntil())
                    throw new BadArgumentException(
                            "ListSets verb only accepts one optional parameter - resumptionTokenResolver");
                break;
            case GetRecord:
                if (!this.hasIdentifier() || !this.hasMetadataPrefix()
                        || this.hasSet() || this.hasFrom() || this.hasUntil())
                    throw new BadArgumentException(
                            "GetRecord verb requires the use of the parameters - identifier and metadataPrefix");
                if (this.hasResumptionToken())
                    throw new BadArgumentException(
                            "GetRecord verb does not accept the resumptionTokenResolver parameter. It requires the use of the parameters - identifier and metadataPrefix");
                break;
            case ListIdentifiers:
                if (!this.hasResumptionToken() && !this.hasMetadataPrefix())
                    throw new BadArgumentException(
                            "ListIdentifiers verb must receive the metadataPrefix parameter");
                if (this.hasIdentifier())
                    throw new BadArgumentException(
                            "ListIdentifiers verb does not accept the identifier parameter");
                if (this.hasFrom() && this.hasUntil())
                    this.validateDates();
                break;
            case ListRecords:
                if (!this.hasResumptionToken() && !this.hasMetadataPrefix())
                    throw new BadArgumentException(
                            "ListRecords verb must receive the metadataPrefix parameter");
                if (this.hasIdentifier())
                    throw new BadArgumentException(
                            "ListRecords verb does not accept the identifier parameter");
                if (this.hasFrom() && this.hasUntil())
                    this.validateDates();
                break;
        }
    }

    private void validateDates() throws BadArgumentException {
        Calendar from = Calendar.getInstance();
        Calendar until = Calendar.getInstance();

        from.setTime(this.from);
        until.setTime(this.until);

        if (from.after(until)) throw new BadArgumentException("The 'from' date must be less then the 'until' one");
    }

    private void loadResumptionToken(ResumptionToken.Value resumptionToken) {
        if (resumptionToken.hasFrom())
            this.from = resumptionToken.getFrom();
        if (resumptionToken.hasMetadataPrefix())
            this.metadataPrefix = resumptionToken.getMetadataPrefix();
        if (resumptionToken.hasSetSpec())
            this.set = resumptionToken.getSetSpec();
        if (resumptionToken.hasUntil())
            this.until = resumptionToken.getUntil();
    }

    public ResumptionToken.Value extractResumptionToken() {
        return new ResumptionToken.Value().withOffset(0)
                .withMetadataPrefix(this.metadataPrefix)
                .withSetSpec(this.set)
                .withFrom(this.from)
                .withUntil(this.until);
    }
}

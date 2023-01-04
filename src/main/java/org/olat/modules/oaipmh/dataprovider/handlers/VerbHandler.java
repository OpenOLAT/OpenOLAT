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

import com.lyncode.builder.Builder;
import org.olat.modules.oaipmh.common.exceptions.InvalidResumptionTokenException;
import org.olat.modules.oaipmh.common.xml.XmlWritable;
import org.olat.modules.oaipmh.dataprovider.builder.OAIRequestParametersBuilder;
import org.olat.modules.oaipmh.dataprovider.exceptions.HandlerException;
import org.olat.modules.oaipmh.dataprovider.exceptions.OAIException;
import org.olat.modules.oaipmh.dataprovider.model.Context;
import org.olat.modules.oaipmh.dataprovider.parameters.OAICompiledRequest;
import org.olat.modules.oaipmh.dataprovider.parameters.OAIRequest;
import org.olat.modules.oaipmh.dataprovider.repository.Repository;


public abstract class VerbHandler<T extends XmlWritable> {
    private Context context;
    private Repository repository;

    public VerbHandler (Context context, Repository repository) {
        this.context = context;
        this.repository = repository;
    }

    public Context getContext() {
        return context;
    }

    public Repository getRepository() {
        return repository;
    }

    public T handle (OAIRequest parameters) throws HandlerException, InvalidResumptionTokenException, OAIException {
        return handle(parameters.compile());
    }

    public T handle (OAIRequestParametersBuilder parameters) throws OAIException, HandlerException, InvalidResumptionTokenException {
        return handle(parameters.build());
    }

    public T handle(Builder<OAICompiledRequest> parameters) throws OAIException, HandlerException {
        return handle(parameters.build());
    }

    public abstract T handle(OAICompiledRequest params) throws OAIException, HandlerException;
}

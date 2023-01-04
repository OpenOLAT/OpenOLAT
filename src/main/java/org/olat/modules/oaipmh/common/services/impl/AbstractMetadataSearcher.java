/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 *
 * Content got modified for OpenOlat Context
 */

package org.olat.modules.oaipmh.common.services.impl;


import org.olat.modules.oaipmh.common.oaidc.Element;
import org.olat.modules.oaipmh.common.oaidc.OAIDCMetadata;
import org.olat.modules.oaipmh.common.services.api.MetadataSearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractMetadataSearcher<T> implements MetadataSearch<T> {

    protected static final String DEFAULT_FIELD = "value";
    protected Map<String, List<T>> index = new HashMap<String, List<T>>();


    public AbstractMetadataSearcher(OAIDCMetadata metadata) {
        for (Element element : metadata.getElements()) {
            consume(new ArrayList<String>(), element);
        }
    }

    @Override
    public T findOne(String xoaiPath) {
        List<T> elements = index.get(xoaiPath);
        if (elements != null && !elements.isEmpty())
            return elements.get(0);
        return null;
    }

    ;

    @Override
    public List<T> findAll(String xoaiPath) {
        return index.get(xoaiPath);
    }

    ;

    @Override
    public Map<String, List<T>> index() {
        return index;
    }

    protected void init(OAIDCMetadata metadata) {
        for (Element element : metadata.getElements()) {
            consume(new ArrayList<String>(), element);
        }

    }

    protected abstract void consume(List<String> newNames, Element element);

}

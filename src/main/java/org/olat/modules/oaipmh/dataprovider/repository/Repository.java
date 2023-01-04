/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 *
 * Content got modified for OpenOlat Context
 */

package org.olat.modules.oaipmh.dataprovider.repository;

import org.olat.modules.oaipmh.common.services.api.ResumptionTokenFormat;
import org.olat.modules.oaipmh.dataprovider.filter.FilterResolver;

public class Repository {
    private FilterResolver filterResolver;
    private RepositoryConfiguration configuration;
    private ItemRepository itemRepository;
    private SetRepository setRepository;
    private ResumptionTokenFormat resumptionTokenFormatter;

    public static Repository repository() {
        return new Repository();
    }

    public RepositoryConfiguration getConfiguration() {
        return configuration;
    }

    public Repository withConfiguration(RepositoryConfiguration configuration) {
        this.configuration = configuration;
        return this;
    }

    public ItemRepository getItemRepository() {
        return itemRepository;
    }

    public Repository withItemRepository(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
        return this;
    }

    public SetRepository getSetRepository() {
        return setRepository;
    }

    public Repository withSetRepository(SetRepository setRepository) {
        this.setRepository = setRepository;
        return this;
    }

    public ResumptionTokenFormat getResumptionTokenFormatter() {
        return resumptionTokenFormatter;
    }

    public Repository withResumptionTokenFormatter(ResumptionTokenFormat resumptionTokenFormatter) {
        this.resumptionTokenFormatter = resumptionTokenFormatter;
        return this;
    }

    public FilterResolver getFilterResolver() {
        return filterResolver;
    }
}

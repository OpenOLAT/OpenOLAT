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

import org.olat.modules.oaipmh.dataprovider.exceptions.IdDoesNotExistException;
import org.olat.modules.oaipmh.dataprovider.exceptions.OAIException;
import org.olat.modules.oaipmh.dataprovider.filter.ScopedFilter;
import org.olat.modules.oaipmh.dataprovider.handlers.results.ListItemIdentifiersResult;
import org.olat.modules.oaipmh.dataprovider.handlers.results.ListItemsResults;
import org.olat.modules.oaipmh.dataprovider.model.Context;
import org.olat.modules.oaipmh.dataprovider.model.Item;
import org.olat.modules.oaipmh.dataprovider.model.MetadataItems;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.lang.Math.min;
import static java.util.Arrays.asList;

public class MetadataItemRepository implements ItemRepository {
    private List<MetadataItems> list = new ArrayList<>();

    public MetadataItemRepository withNoItems() {
        return this;
    }

    public MetadataItemRepository withItem(MetadataItems item) {
        list.add(item);
        return this;
    }

    public MetadataItemRepository withItems(MetadataItems... item) {
        list.addAll(asList(item));
        return this;
    }

    public MetadataItemRepository withRepositoryItems(MetadataSetRepository setRepository) {
        list.clear();
        list.addAll(MetadataItems.repositoryDCItems(setRepository));
        return this;
    }

    public MetadataItemRepository withOORepositoryItems(MetadataSetRepository setRepository) {
        list.clear();
        list.addAll(MetadataItems.repositoryOOItems(setRepository));
        return this;
    }

    @Override
    public Item getItem(String identifier) throws IdDoesNotExistException, OAIException {
        for (MetadataItems item : this.list) {
            if (item.getIdentifier().equals(identifier))
                return item;
        }
        throw new IdDoesNotExistException();
    }

    @Override
    public ListItemIdentifiersResult getItemIdentifiers(List<ScopedFilter> filters, int offset, int length) throws OAIException {
        return new ListItemIdentifiersResult(offset + length < list.size(), new ArrayList<>(list.subList(offset, min(offset + length, list.size()))));
    }

    @Override
    public ListItemIdentifiersResult getItemIdentifiers(List<ScopedFilter> filters, int offset, int length, Date from) throws OAIException {
        return new ListItemIdentifiersResult(offset + length < list.size(), new ArrayList<>(list.subList(offset, min(offset + length, list.size()))), null, from, null);
    }

    @Override
    public ListItemIdentifiersResult getItemIdentifiersUntil(List<ScopedFilter> filters, int offset, int length, Date until) throws OAIException {
        return new ListItemIdentifiersResult(offset + length < list.size(), new ArrayList<>(list.subList(offset, min(offset + length, list.size()))), null, null, until);
    }

    @Override
    public ListItemIdentifiersResult getItemIdentifiers(List<ScopedFilter> filters, int offset, int length, Date from, Date until) throws OAIException {
        return new ListItemIdentifiersResult(offset + length < list.size(), new ArrayList<>(list.subList(offset, min(offset + length, list.size()))), null, from, until);
    }

    @Override
    public ListItemIdentifiersResult getItemIdentifiers(List<ScopedFilter> filters, int offset, int length, String setSpec) throws OAIException {
        return new ListItemIdentifiersResult(offset + length < list.size(), new ArrayList<>(list.subList(offset, min(offset + length, list.size()))), setSpec, null, null);
    }

    @Override
    public ListItemIdentifiersResult getItemIdentifiers(List<ScopedFilter> filters, int offset, int length, String setSpec, Date from) throws OAIException {
        return new ListItemIdentifiersResult(offset + length < list.size(), new ArrayList<>(list.subList(offset, min(offset + length, list.size()))), setSpec, from, null);
    }

    @Override
    public ListItemIdentifiersResult getItemIdentifiersUntil(List<ScopedFilter> filters, int offset, int length, String setSpec, Date until) throws OAIException {
        return new ListItemIdentifiersResult(offset + length < list.size(), new ArrayList<>(list.subList(offset, min(offset + length, list.size()))), setSpec, null, until);
    }

    @Override
    public ListItemIdentifiersResult getItemIdentifiers(List<ScopedFilter> filters, int offset, int length, String setSpec, Date from, Date until) throws OAIException {
        return new ListItemIdentifiersResult(offset + length < list.size(), new ArrayList<>(list.subList(offset, min(offset + length, list.size()))), setSpec, from, until);
    }

    @Override
    public ListItemsResults getItems(List<ScopedFilter> filters, int offset, int length) throws OAIException {
        return new ListItemsResults(offset + length < list.size(), new ArrayList<>(list.subList(offset, min(offset + length, list.size()))));
    }

    @Override
    public ListItemsResults getItems(List<ScopedFilter> filters, int offset, int length, Date from) throws OAIException {
        return new ListItemsResults(offset + length < list.size(), new ArrayList<>(list.subList(offset, min(offset + length, list.size()))), null, from, null);
    }

    @Override
    public ListItemsResults getItemsUntil(List<ScopedFilter> filters, int offset, int length, Date until) throws OAIException {
        return new ListItemsResults(offset + length < list.size(), new ArrayList<>(list.subList(offset, min(offset + length, list.size()))), null, null, until);
    }

    @Override
    public ListItemsResults getItems(List<ScopedFilter> filters, int offset, int length, Date from, Date until) throws OAIException {
        return new ListItemsResults(offset + length < list.size(), new ArrayList<>(list.subList(offset, min(offset + length, list.size()))), null, from, until);
    }

    @Override
    public ListItemsResults getItems(Context context, List<ScopedFilter> filters, int offset, int length, String setSpec, SetRepository setRepository) throws OAIException {
        return new ListItemsResults(offset + length < list.size(), new ArrayList<>(list.subList(offset, min(offset + length, list.size()))), setSpec, null, null);
    }

    @Override
    public ListItemsResults getItems(List<ScopedFilter> filters, int offset, int length, String setSpec, Date from) throws OAIException {
        return new ListItemsResults(offset + length < list.size(), new ArrayList<>(list.subList(offset, min(offset + length, list.size()))), setSpec, from, null);
    }

    @Override
    public ListItemsResults getItemsUntil(List<ScopedFilter> filters, int offset, int length, String setSpec, Date until) throws OAIException {
        return new ListItemsResults(offset + length < list.size(), new ArrayList<>(list.subList(offset, min(offset + length, list.size()))), setSpec, null, until);
    }

    @Override
    public ListItemsResults getItems(List<ScopedFilter> filters, int offset, int length, String setSpec, Date from, Date until) throws OAIException {
        return new ListItemsResults(offset + length < list.size(), new ArrayList<>(list.subList(offset, min(offset + length, list.size()))), setSpec, from, until);
    }
}

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
import org.olat.modules.oaipmh.common.oaidc.Field;
import org.olat.modules.oaipmh.common.oaidc.OAIDCMetadata;
import org.olat.modules.oaipmh.common.services.api.MetadataSearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.join;

public class MetadataSearchImpl extends AbstractMetadataSearcher<String> implements MetadataSearch<String> {

    public MetadataSearchImpl(OAIDCMetadata metadata) {
        super(metadata);
    }

    protected void consume(List<String> newNames, Element element) {
        List<String> names = new ArrayList<String>(newNames);
        names.add(element.getName());

        if (!element.getFields().isEmpty()) {
            for (Field field : element.getFields()) {
                if (field.getName() != null && !field.getName().equals(DEFAULT_FIELD)) {
                    add(join(names, ".") + ":" + field.getName(), field.getValue());
                } else {
                    add(join(names, "."), field.getValue());
                }
            }
        }

        if (!element.getElements().isEmpty()) {
            for (Element subElement : element.getElements()) {
                consume(names, subElement);
            }
        }
    }

    private void add(String name, String value) {
        if (!index.containsKey(name))
            index.put(name, new ArrayList<String>());

        index.get(name).add(value);
    }

    @Override
    public String findOne(String xoaiPath) {
        return super.findOne(xoaiPath);
    }

    @Override
    public List<String> findAll(String xoaiPath) {
        return super.findAll(xoaiPath);
    }

    @Override
    public Map<String, List<String>> index() {
        return index;
    }
}

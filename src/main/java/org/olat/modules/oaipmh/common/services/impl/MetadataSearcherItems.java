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
import org.olat.modules.oaipmh.common.oaidc.MetadataItem;
import org.olat.modules.oaipmh.common.oaidc.OAIDCMetadata;
import org.olat.modules.oaipmh.common.services.api.MetadataSearch;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.join;

/**
 * An implementation whose searches return {@link MetadataItem} elements.
 * Useful for when more information for each OAI metadata item is available (e.g., xml attributes like xml:lang).
 *
 * @author mmalmeida
 */
public class MetadataSearcherItems extends AbstractMetadataSearcher<MetadataItem> implements MetadataSearch<MetadataItem> {

    public MetadataSearcherItems(OAIDCMetadata metadata) {
        super(metadata);
    }

    @Override
    protected void consume(List<String> newNames, Element element) {
        List<String> names = new ArrayList<String>(newNames);
        names.add(element.getName());

        if (!element.getFields().isEmpty()) {
            add(join(names, "."), element.getFields());
        }

        if (!element.getElements().isEmpty()) {
            for (Element subElement : element.getElements()) {
                consume(names, subElement);
            }
        }
    }

    private void add(String name, List<Field> fields) {
        if (!index.containsKey(name))
            index.put(name, new ArrayList<MetadataItem>());

        MetadataItem newElement = new MetadataItem();
        for (Field field : fields) {
            if (field.getName() != null && !field.getName().equals(DEFAULT_FIELD)) {
                newElement.addProperty(field.getName(), field.getValue());
            } else {
                newElement.setValue(field.getValue());
            }
        }
        index.get(name).add(newElement);

    }

    @Override
    public MetadataItem findOne(String xoaiPath) {
        return super.findOne(xoaiPath);
    }

    @Override
    public List<MetadataItem> findAll(String xoaiPath) {
        return super.findAll(xoaiPath);
    }


}

/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 *
 * Content got modified for OpenOlat Context
 */

package org.olat.modules.oaipmh.dataprovider.handlers.helpers;

import org.olat.modules.oaipmh.common.exceptions.XmlWriteException;
import org.olat.modules.oaipmh.common.xml.XmlWriter;
import org.olat.modules.oaipmh.dataprovider.filter.FilterResolver;
import org.olat.modules.oaipmh.dataprovider.model.Context;
import org.olat.modules.oaipmh.dataprovider.model.Item;
import org.olat.modules.oaipmh.dataprovider.model.Set;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ItemHelper extends ItemIdentifyHelper {
    private Item item;
    private String metadataPrefix;

    public ItemHelper(Item item, String metadataPrefix) {
        super(item);
        this.item = item;
        this.metadataPrefix = metadataPrefix;
    }

    public Item getItem() {
        return item;
    }

    public InputStream toStream() throws XMLStreamException, XmlWriteException {
        if (item.getMetadata(metadataPrefix) != null) {
            return new ByteArrayInputStream(item.getMetadata(metadataPrefix).toString().getBytes());
        } else {
            ByteArrayOutputStream mdOUT = new ByteArrayOutputStream();
            XmlWriter writer = new XmlWriter(mdOUT);
            item.getMetadata(metadataPrefix).write(writer);
            writer.flush();
            writer.close();
            return new ByteArrayInputStream(mdOUT.toByteArray());
        }
    }

    public List<Set> getSets(Context context, FilterResolver resolver) {
        List<Set> result = new ArrayList<>();
        for (Set set : context.getSets())
            if (set.getCondition().getFilter(resolver).isItemShown(item))
                result.add(set);

        result.addAll(item.getSets());

        return result;
    }
}

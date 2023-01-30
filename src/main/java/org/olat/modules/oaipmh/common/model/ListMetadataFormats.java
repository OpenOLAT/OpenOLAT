/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 *
 * Content got modified for OpenOlat Context
 */

package org.olat.modules.oaipmh.common.model;


import java.util.ArrayList;
import java.util.List;

import org.olat.modules.oaipmh.common.exceptions.XmlWriteException;
import org.olat.modules.oaipmh.common.xml.XmlWriter;

public class ListMetadataFormats implements Verb {
	protected List<MetadataFormat> metadataFormats = new ArrayList<MetadataFormat>();

	public List<MetadataFormat> getMetadataFormats() {
		return this.metadataFormats;
	}

	public ListMetadataFormats withMetadataFormat(MetadataFormat mdf) {
		metadataFormats.add(mdf);
		return this;
	}

	@Override
	public void write(XmlWriter writer) throws XmlWriteException {
		if (!this.metadataFormats.isEmpty())
			for (MetadataFormat format : this.metadataFormats)
				writer.writeElement("metadataFormat", format);
	}

	@Override
	public Type getType() {
		return Type.ListMetadataFormats;
	}
}

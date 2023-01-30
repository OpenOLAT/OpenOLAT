/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 *
 * Content slightly modified for OO Context
 */

package org.olat.modules.oaipmh.dataprovider.model;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;

import org.olat.core.util.xml.XMLFactories;
import org.olat.modules.oaipmh.dataprovider.model.conditions.Condition;

public class MetadataFormat {
	private Condition condition;
	private String prefix;
	private Transformer transformer;
	private String namespace;
	private String schemaLocation;

	public static Transformer identity() {
		try {
			return XMLFactories.newTransformerFactory().newTransformer();
		} catch (TransformerConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	public static MetadataFormat metadataFormat(String prefix) {
		return new MetadataFormat().withPrefix(prefix);
	}

	public String getPrefix() {
		return prefix;
	}

	public MetadataFormat withPrefix(String prefix) {
		this.prefix = prefix;
		return this;
	}

	public Transformer getTransformer() {
		return transformer;
	}

	public MetadataFormat withTransformer(Transformer transformer) {
		this.transformer = transformer;
		return this;
	}

	public String getNamespace() {
		return namespace;
	}

	public MetadataFormat withNamespace(String namespace) {
		this.namespace = namespace;
		return this;
	}

	public String getSchemaLocation() {
		return schemaLocation;
	}

	public MetadataFormat withSchemaLocation(String schemaLocation) {
		this.schemaLocation = schemaLocation;
		return this;
	}

	public MetadataFormat withCondition(Condition filter) {
		this.condition = filter;
		return this;
	}

	public Condition getCondition() {
		return condition;
	}

	public boolean hasCondition() {
		return condition != null;
	}
}

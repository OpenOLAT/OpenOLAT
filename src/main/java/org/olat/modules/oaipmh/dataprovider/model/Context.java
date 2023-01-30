/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 *
 * Content got modified for OpenOlat Context
 */

package org.olat.modules.oaipmh.dataprovider.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Transformer;

import org.olat.modules.oaipmh.dataprovider.exceptions.InternalOAIException;
import org.olat.modules.oaipmh.dataprovider.filter.FilterResolver;
import org.olat.modules.oaipmh.dataprovider.model.conditions.Condition;

public class Context {
	private final List<MetadataFormat> metadataFormats = new ArrayList<>();
	private final List<Set> sets = new ArrayList<>();
	private Transformer metadataTransformer;
	private Condition condition;

	public static Context context() {
		return new Context();
	}

	public List<Set> getSets() {
		return sets;
	}

	public Context withSet(Set set) {
		if (!set.hasCondition())
			throw new InternalOAIException("Context sets must have a condition");
		this.sets.add(set);
		return this;
	}

	public Transformer getTransformer() {
		return metadataTransformer;
	}

	public Context withTransformer(Transformer metadataTransformer) {
		this.metadataTransformer = metadataTransformer;
		return this;
	}

	public List<MetadataFormat> getMetadataFormats() {
		return metadataFormats;
	}

	public Context withMetadataFormat(MetadataFormat metadataFormat) {
		int remove = -1;
		for (int i = 0; i < metadataFormats.size(); i++)
			if (metadataFormats.get(i).getPrefix().equals(metadataFormat.getPrefix()))
				remove = i;
		if (remove >= 0)
			this.metadataFormats.remove(remove);
		this.metadataFormats.add(metadataFormat);
		return this;
	}

	public Condition getCondition() {
		return condition;
	}

	public Context withCondition(Condition condition) {
		this.condition = condition;
		return this;
	}

	public MetadataFormat formatForPrefix(String metadataPrefix) {
		for (MetadataFormat format : this.metadataFormats)
			if (format.getPrefix().equals(metadataPrefix))
				return format;

		return null;
	}

	public boolean hasTransformer() {
		return metadataTransformer != null;
	}

	public boolean hasCondition() {
		return this.condition != null;
	}

	public boolean isStaticSet(String setSpec) {
		for (Set set : this.sets)
			if (set.getSpec().equals(setSpec))
				return true;

		return false;
	}

	public Set getSet(String setSpec) {
		for (Set set : this.sets)
			if (set.getSpec().equals(setSpec))
				return set;

		return null;
	}

	public boolean hasSet(String set) {
		return isStaticSet(set);
	}

	public Context withMetadataFormat(String prefix, Transformer transformer) {
		withMetadataFormat(new MetadataFormat().withNamespace(prefix).withPrefix(prefix).withSchemaLocation(prefix).withTransformer(transformer));
		return this;
	}

	public Context withMetadataFormat(String prefix, Transformer transformer, Condition condition) {
		withMetadataFormat(
				new MetadataFormat()
						.withNamespace(prefix)
						.withPrefix(prefix)
						.withSchemaLocation(prefix)
						.withTransformer(transformer)
						.withCondition(condition)
		);
		return this;
	}

	public Context withoutMetadataFormats() {
		metadataFormats.clear();
		return this;
	}

	public List<MetadataFormat> formatFor(FilterResolver resolver, ItemIdentifier item) {
		List<MetadataFormat> result = new ArrayList<>();
		for (MetadataFormat format : this.metadataFormats)
			if (!format.hasCondition() || format.getCondition().getFilter(resolver).isItemShown(item))
				result.add(format);
		return result;
	}
}

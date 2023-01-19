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

import com.lyncode.builder.ListBuilder;
import org.olat.modules.oaipmh.common.model.About;
import org.olat.modules.oaipmh.common.model.Metadata;
import org.olat.modules.oaipmh.common.oaidc.Element;
import org.olat.modules.oaipmh.common.oaidc.OAIDCMetadata;
import org.olat.modules.oaipmh.common.oaioo.OAIOOMetadata;
import org.olat.modules.oaipmh.common.oaioo.OOElement;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MetadataItems implements Item {

	private Map<String, Object> values = new HashMap<>();

	public static MetadataItems item() {
		return new MetadataItems();
	}

	public MetadataItems with(String name, Object value) {
		values.put(name, value);
		return this;
	}

	public MetadataItems withSet(String name) {
		((List<String>) values.get("sets")).add(name);
		return this;
	}

	public MetadataItems getMetadataBySetSpec(String setSpec) {
		getSets().stream().filter(s -> s.getSpec().equals(setSpec)).collect(Collectors.toList());
		return this;
	}

	@Override
	public List<About> getAbout() {
		return new ArrayList<>();
	}

	@Override
	public Metadata getMetadata(String metadataPrefix) {
		if (metadataPrefix.equals("oai_dc")) {
			return new Metadata(toDCMetadata());
		} else if (metadataPrefix.equals("oai_oo")) {
			return new Metadata(toOOMetadata());
		}
		return null;
	}

	private OAIDCMetadata toDCMetadata() {
		OAIDCMetadata builder = new OAIDCMetadata();
		for (String key : values.keySet()) {
			Element elementBuilder = new Element(key);
			Object value = values.get(key);
			if (value instanceof String)
				elementBuilder.withField(key, value.toString());
			else if (value instanceof Date)
				elementBuilder.withField(key, value.toString());
			else if (value instanceof List) {
				List<String> obj = (List<String>) value;
				int i = 1;
				for (String e : obj)
					elementBuilder.withField(key + (i++), e);
			}
			builder.withElement(elementBuilder);
		}
		return builder;
	}

	private OAIOOMetadata toOOMetadata() {
		OAIOOMetadata builder = new OAIOOMetadata();
		for (String key : values.keySet()) {
			OOElement elementBuilder = new OOElement(key);
			Object value = values.get(key);
			if (value instanceof String)
				elementBuilder.withValue(value.toString());
			else if (value instanceof Date)
				elementBuilder.withValue(value.toString());
			else if (value instanceof List) {
				List<String> obj = (List<String>) value;
				for (String e : obj)
					elementBuilder.withValue(e);
			}
			builder.withElement(elementBuilder);
		}
		return builder;
	}

	@Override
	public String getIdentifier() {
		return values.get("identifier").toString();
	}

	@Override
	public Date getDatestamp() {
		if (values.get("date") != null) {
			return (Date) values.get("date");
		} else if (values.get("creationdate") != null) {
			return (Date) values.get("creationdate");
		} else {
			return new Date();
		}
	}

	@Override
	public List<Set> getSets() {
		List<String> list = ((List<String>) values.get("sets"));
		return new ListBuilder<String>().add(list.toArray(new String[list.size()])).build(new ListBuilder.Transformer<String, Set>() {
			@Override
			public Set transform(String elem) {
				return new Set(elem);
			}
		});
	}

	@Override
	public boolean isDeleted() {
		return (Boolean) values.get("deleted");
	}


	public MetadataItems withIdentifier(String identifier) {
		this.with("identifier", identifier);
		return this;
	}
}

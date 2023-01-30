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

import java.util.ArrayList;
import java.util.List;

import org.olat.modules.oaipmh.common.oaidc.OAIDCMetadata;
import org.olat.modules.oaipmh.dataprovider.model.conditions.Condition;

/**
 * @author Development @ Lyncode
 * @version 3.1.0
 */
public class Set {
	private final String spec;
	private String name;
	private List<OAIDCMetadata> descriptions = new ArrayList<>();
	private Condition condition;
	public Set(String spec) {
		this.spec = spec;
	}

	public static Set set(String spec) {
		return new Set(spec);
	}

	public String getName() {
		return name;
	}

	public Set withName(String name) {
		this.name = name;
		return this;
	}

	public List<OAIDCMetadata> getDescriptions() {
		return descriptions;
	}

	public Set withDescription(OAIDCMetadata description) {
		descriptions.add(description);
		return this;
	}

	public boolean hasDescription() {
		return (!this.descriptions.isEmpty());
	}

	public Condition getCondition() {
		return condition;
	}

	public boolean hasCondition() {
		return condition != null;
	}

	public Set withCondition(Condition condition) {
		this.condition = condition;
		return this;
	}

	public String getSpec() {
		return spec;
	}

	public org.olat.modules.oaipmh.common.model.Set toOAIPMH() {
		org.olat.modules.oaipmh.common.model.Set set = new org.olat.modules.oaipmh.common.model.Set();
		set.withName(getName());
		set.withSpec(getSpec());
		for (OAIDCMetadata description : descriptions)
			set.withDescription(description);
		return set;
	}
}

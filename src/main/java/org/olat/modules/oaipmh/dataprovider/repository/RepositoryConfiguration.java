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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.helpers.Settings;
import org.olat.modules.oaipmh.common.model.DeletedRecord;
import org.olat.modules.oaipmh.common.model.Granularity;

public class RepositoryConfiguration {
	private String repositoryName;
	private String baseUrl;
	private Date earliestDate;
	private int maxListIdentifiers;
	private int maxListSets;
	private int maxListRecords;
	private Granularity granularity;
	private DeletedRecord deleteMethod;
	private List<String> descriptions;
	private List<String> compressions;

	public String getRepositoryName() {
		return repositoryName;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public Date getEarliestDate() {
		return earliestDate;
	}

	public int getMaxListIdentifiers() {
		return this.maxListIdentifiers;
	}

	public int getMaxListSets() {
		return this.maxListSets;
	}

	public int getMaxListRecords() {
		return this.maxListRecords;
	}

	public Granularity getGranularity() {
		return granularity;
	}

	public DeletedRecord getDeleteMethod() {
		return deleteMethod;
	}

	public List<String> getDescription() {
		return descriptions;
	}

	public List<String> getCompressions() {
		return compressions;
	}

	public RepositoryConfiguration withMaxListSets(int maxListSets) {
		this.maxListSets = maxListSets;
		return this;
	}

	public RepositoryConfiguration withGranularity(Granularity granularity) {
		this.granularity = granularity;
		return this;
	}

	public RepositoryConfiguration withRepositoryName(String repositoryName) {
		this.repositoryName = repositoryName;
		return this;
	}

	public RepositoryConfiguration and() {
		return this;
	}

	public RepositoryConfiguration withDeleteMethod(DeletedRecord deleteMethod) {
		this.deleteMethod = deleteMethod;
		return this;
	}

	public RepositoryConfiguration withDescription(String description) {
		if (descriptions == null)
			descriptions = new ArrayList<>();
		descriptions.add(description);
		return this;
	}

	public RepositoryConfiguration withBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
		return this;
	}

	public RepositoryConfiguration withCompression(String compression) {
		if (compressions == null)
			compressions = new ArrayList<String>();
		compressions.add(compression);
		return this;
	}

	public RepositoryConfiguration withMaxListRecords(int maxListRecords) {
		this.maxListRecords = maxListRecords;
		return this;
	}

	public RepositoryConfiguration withEarliestDate(Date earliestDate) {
		this.earliestDate = earliestDate;
		return this;
	}

	public RepositoryConfiguration withDefaults() {
		this.repositoryName = "Repository";
		this.earliestDate = new Date();
		this.baseUrl = Settings.getServerContextPathURI() + "/oaipmh";
		this.maxListIdentifiers = 100;
		this.maxListRecords = 100;
		this.maxListSets = 10;
		this.granularity = Granularity.Day;
		this.deleteMethod = DeletedRecord.NO;

		return this;
	}

	public boolean hasCompressions() {
		return compressions != null && !compressions.isEmpty();
	}

	public RepositoryConfiguration withMaxListIdentifiers(int maxListIdentifiers) {
		this.maxListIdentifiers = maxListIdentifiers;
		return this;
	}
}

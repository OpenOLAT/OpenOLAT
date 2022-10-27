/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.repository.bulk.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.repository.RepositoryEntry;
import org.olat.repository.bulk.SettingsBulkEditable;

/**
 * 
 * Initial date: 18 Oct 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SettingsContext {
	
	public static final String DEFAULT_KEY = "settingsContext";

	private final List<RepositoryEntry> repositoryEntries;
	private final Set<SettingsBulkEditable> editables = new HashSet<>();
	private String authors;
	private Long educationalTypeKey;
	private String mainLanguage;
	private String expenditureOfWork;
	private String licenseTypeKey;
	private String freetext;
	private String licensor;
	private Set<Long> taxonomyLevelAddKeys;
	private Set<Long> taxonomyLevelRemoveKeys;
	private Set<Long> organisationAddKeys;
	private Set<Long> organisationRemoveKeys;
	private boolean authorRightReference = true;
	private boolean authorRightCopy = true;
	private boolean authorRightDownload = true;

	public SettingsContext(List<RepositoryEntry> repositoryEntries) {
		this.repositoryEntries = repositoryEntries;
	}

	public List<RepositoryEntry> getRepositoryEntries() {
		return repositoryEntries;
	}
	
	public void select(SettingsBulkEditable editable, boolean select) {
		if (select) {
			editables.add(editable);
		} else {
			editables.remove(editable);
		}
	}
	
	public boolean isSelected(SettingsBulkEditable editable) {
		return editables.contains(editable);
	}
	
	public String getAuthors() {
		return authors;
	}

	public void setAuthors(String authors) {
		this.authors = authors;
	}

	public Long getEducationalTypeKey() {
		return educationalTypeKey;
	}

	public void setEducationalTypeKey(Long educationalTypeKey) {
		this.educationalTypeKey = educationalTypeKey;
	}

	public String getMainLanguage() {
		return mainLanguage;
	}

	public void setMainLanguage(String mainLanguage) {
		this.mainLanguage = mainLanguage;
	}

	public String getExpenditureOfWork() {
		return expenditureOfWork;
	}

	public void setExpenditureOfWork(String expenditureOfWork) {
		this.expenditureOfWork = expenditureOfWork;
	}

	public String getLicenseTypeKey() {
		return licenseTypeKey;
	}

	public void setLicenseTypeKey(String licenseTypeKey) {
		this.licenseTypeKey = licenseTypeKey;
	}

	public String getFreetext() {
		return freetext;
	}

	public void setFreetext(String freetext) {
		this.freetext = freetext;
	}

	public String getLicensor() {
		return licensor;
	}

	public void setLicensor(String licensor) {
		this.licensor = licensor;
	}

	public Set<Long> getTaxonomyLevelAddKeys() {
		return taxonomyLevelAddKeys;
	}

	public void setTaxonomyLevelAddKeys(Set<Long> taxonomyLevelAddKeys) {
		this.taxonomyLevelAddKeys = taxonomyLevelAddKeys;
	}

	public Set<Long> getTaxonomyLevelRemoveKeys() {
		return taxonomyLevelRemoveKeys;
	}

	public void setTaxonomyLevelRemoveKeys(Set<Long> taxonomyLevelRemoveKeys) {
		this.taxonomyLevelRemoveKeys = taxonomyLevelRemoveKeys;
	}

	public Set<Long> getOrganisationAddKeys() {
		return organisationAddKeys;
	}

	public void setOrganisationAddKeys(Set<Long> organisationAddKeys) {
		this.organisationAddKeys = organisationAddKeys;
	}

	public Set<Long> getOrganisationRemoveKeys() {
		return organisationRemoveKeys;
	}

	public void setOrganisationRemoveKeys(Set<Long> organisationRemoveKeys) {
		this.organisationRemoveKeys = organisationRemoveKeys;
	}

	public boolean isAuthorRightReference() {
		return authorRightReference;
	}

	public void setAuthorRightReference(boolean authorRightReference) {
		this.authorRightReference = authorRightReference;
	}

	public boolean isAuthorRightCopy() {
		return authorRightCopy;
	}

	public void setAuthorRightCopy(boolean authorRightCopy) {
		this.authorRightCopy = authorRightCopy;
	}

	public boolean isAuthorRightDownload() {
		return authorRightDownload;
	}

	public void setAuthorRightDownload(boolean authorRightDownload) {
		this.authorRightDownload = authorRightDownload;
	}
	
}

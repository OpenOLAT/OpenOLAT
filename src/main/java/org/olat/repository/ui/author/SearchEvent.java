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
package org.olat.repository.ui.author;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.control.Event;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.context.StateEntry;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.repository.model.SearchAuthorRepositoryEntryViewParams.ResourceUsage;

/**
 * 
 * Initial date: 02.05.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SearchEvent extends Event implements StateEntry {

	private static final long serialVersionUID = -1222660688926846838L;
	
	private String id;
	private String displayname;
	private String author;
	private String description;
	private Set<String> types;
	private Set<String> technicalTypes;
	private Set<Long> educationalTypeKeys;
	private Boolean closed;
	private boolean ownedResourcesOnly;
	private ResourceUsage resourceUsage;
	private Set<Long> licenseTypeKeys;
	private List<OrganisationRef> entryOrganisations;
	private List<TaxonomyLevelRef> taxonomyLevels;
	
	public SearchEvent() {
		super("re-search");
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDisplayname() {
		return displayname;
	}

	public void setDisplayname(String displayname) {
		this.displayname = displayname;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Set<String> getTypes() {
		return types;
	}

	public void setTypes(Set<String> types) {
		this.types = types;
	}

	public Set<String> getTechnicalTypes() {
		return technicalTypes;
	}

	public void setTechnicalTypes(Set<String> technicalTypes) {
		this.technicalTypes = technicalTypes;
	}

	public Set<Long> getEducationalTypeKeys() {
		return educationalTypeKeys;
	}

	public void setEducationalTypeKeys(Set<Long> educationalTypeKeys) {
		this.educationalTypeKeys = educationalTypeKeys;
	}

	public boolean isOwnedResourcesOnly() {
		return ownedResourcesOnly;
	}

	public void setOwnedResourcesOnly(boolean ownedResourcesOnly) {
		this.ownedResourcesOnly = ownedResourcesOnly;
	}

	public ResourceUsage getResourceUsage() {
		return resourceUsage;
	}

	public void setResourceUsage(ResourceUsage resourceUsage) {
		this.resourceUsage = resourceUsage;
	}

	public Boolean getClosed() {
		return closed;
	}

	public void setClosed(Boolean closed) {
		this.closed = closed;
	}

	public Set<Long> getLicenseTypeKeys() {
		return licenseTypeKeys;
	}

	public void setLicenseTypeKeys(Set<Long> licenseTypeKeys) {
		this.licenseTypeKeys = licenseTypeKeys;
	}

	public List<OrganisationRef> getEntryOrganisations() {
		return entryOrganisations;
	}

	public void setEntryOrganisations(List<OrganisationRef> entryOrganisations) {
		this.entryOrganisations = entryOrganisations;
	}

	public List<TaxonomyLevelRef> getTaxonomyLevels() {
		return taxonomyLevels;
	}

	public void setTaxonomyLevels(List<TaxonomyLevelRef> taxonomyLevels) {
		this.taxonomyLevels = taxonomyLevels;
	}

	@Override
	public SearchEvent clone() {
		SearchEvent clone = new SearchEvent();
		clone.id = id;
		clone.displayname = displayname;
		clone.author = author;
		clone.description = description;
		clone.types = (types == null ? null : new HashSet<>(types));
		clone.ownedResourcesOnly = ownedResourcesOnly;
		clone.resourceUsage = resourceUsage;
		clone.closed = closed;
		clone.licenseTypeKeys = (licenseTypeKeys == null ? null : new HashSet<>(licenseTypeKeys));
		clone.entryOrganisations = (entryOrganisations == null ? null : new ArrayList<>(entryOrganisations));
		clone.taxonomyLevels = taxonomyLevels != null? new ArrayList<>(taxonomyLevels): null;
		return clone;
	}
}

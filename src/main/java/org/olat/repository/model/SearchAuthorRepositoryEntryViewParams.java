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
 * 12.10.2011 by frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.repository.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.repository.RepositoryEntryStatusEnum;

/**
 * 
 * Initial date: 28.04.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SearchAuthorRepositoryEntryViewParams {
	private final IdentityRef identity;
	private final Roles roles;
	
	private Boolean marked;
	private boolean ownedResourcesOnly;
	private OERRelease oerRelease = OERRelease.all;
	private ResourceUsage resourceUsage = ResourceUsage.all;
	private RepositoryEntryStatusEnum[] status;
	
	private boolean canCopy = false;
	private boolean canDownload = false;
	private boolean canReference = false;
	
	private String idAndRefs;
	private String idRefsAndTitle;
	private String author;
	private String displayname;
	private String reference;
	private String description;
	
	private boolean exactSearch;	// Use exact search instead of fuzzy search
	
	private OrderBy orderBy;
	private boolean orderByAsc;
	private List<String> resourceTypes;
	private Collection<String> technicalTypes;
	private Collection<Long> educationalTypeKeys;
	private Collection<Long> licenseTypeKeys;
	private List<OrganisationRef> entryOrganisations;
	private List<TaxonomyLevelRef> taxonomyLevels;
	private List<Long> excludeEntryKeys;
	
	public SearchAuthorRepositoryEntryViewParams(IdentityRef identity, Roles roles) {
		this.identity = identity;
		this.roles = roles;
	}
	
	public boolean isExactSearch() {
		return exactSearch;
	}
	
	public void setExactSearch(boolean exactSearch) {
		this.exactSearch = exactSearch;
	}

	public String getIdAndRefs() {
		return idAndRefs;
	}

	public void setIdAndRefs(String idAndRefs) {
		this.idAndRefs = idAndRefs;
	}

	public String getIdRefsAndTitle() {
		return idRefsAndTitle;
	}

	public void setIdRefsAndTitle(String idRefsAndTitle) {
		this.idRefsAndTitle = idRefsAndTitle;
	}

	public boolean isOwnedResourcesOnly() {
		return ownedResourcesOnly;
	}

	public void setOwnedResourcesOnly(boolean ownedResourcesOnly) {
		this.ownedResourcesOnly = ownedResourcesOnly;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getDisplayname() {
		return displayname;
	}

	public void setDisplayname(String displayname) {
		this.displayname = displayname;
	}
	
	public String getReference() {
		return reference;
	}
	
	public void setReference(String reference) {
		this.reference = reference;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isCanCopy() {
		return canCopy;
	}

	public void setCanCopy(boolean canCopy) {
		this.canCopy = canCopy;
	}

	public boolean isCanDownload() {
		return canDownload;
	}

	public void setCanDownload(boolean canDownload) {
		this.canDownload = canDownload;
	}

	public boolean isCanReference() {
		return canReference;
	}

	public void setCanReference(boolean canReference) {
		this.canReference = canReference;
	}

	public OrderBy getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(OrderBy orderBy) {
		this.orderBy = orderBy;
	}
	
	public boolean isOrderByAsc() {
		return orderByAsc;
	}

	public void setOrderByAsc(boolean orderByAsc) {
		this.orderByAsc = orderByAsc;
	}

	public boolean isResourceTypesDefined() {
		return resourceTypes != null && !resourceTypes.isEmpty();
	}

	public List<String> getResourceTypes() {
		return resourceTypes;
	}
	
	public boolean includeResourceType(String resourceType) {
		return resourceTypes == null || resourceTypes.contains(resourceType);
	}
	
	public void setResourceTypes(List<String> resourceTypes) {
		this.resourceTypes = resourceTypes;
	}
	
	public void addResourceTypes(String... types) {
		if(resourceTypes == null) {
			resourceTypes = new ArrayList<>();
		}
		if(types != null) {
			for(String resourceType:types) {
				resourceTypes.add(resourceType);
			}
		}
	}
	
	public boolean isTechnicalTypeDefined() {
		return technicalTypes != null && !technicalTypes.isEmpty();
	}
	
	public Collection<String> getTechnicalTypes() {
		return technicalTypes;
	}

	public void setTechnicalTypes(Collection<String> technicalTypes) {
		this.technicalTypes = technicalTypes;
	}

	public boolean isEducationalTypeDefined() {
		return educationalTypeKeys != null && !educationalTypeKeys.isEmpty();
	}
	
	public Collection<Long> getEducationalTypeKeys() {
		return educationalTypeKeys;
	}

	public void setEducationalTypeKeys(Collection<Long> educationalTypeKeys) {
		this.educationalTypeKeys = educationalTypeKeys;
	}

	public IdentityRef getIdentity() {
		return identity;
	}
	
	public Roles getRoles() {
		return roles;
	}

	public Boolean getMarked() {
		return marked;
	}

	public void setMarked(Boolean marked) {
		this.marked = marked;
	}

	public OERRelease getOerRelease() {
		return oerRelease;
	}

	public void setOerRelease(OERRelease oerRelease) {
		this.oerRelease = oerRelease;
	}
	
	public ResourceUsage getResourceUsage() {
		return resourceUsage;
	}

	public void setResourceUsage(ResourceUsage resourceUsage) {
		this.resourceUsage = resourceUsage;
	}
	
	public boolean isLicenseTypeDefined() {
		return licenseTypeKeys != null && !licenseTypeKeys.isEmpty();
	}
 
	public Collection<Long> getLicenseTypeKeys() {
		return licenseTypeKeys;
	}

	public void setLicenseTypeKeys(Collection<Long> licenseTypeKeys) {
		this.licenseTypeKeys = licenseTypeKeys;
	}
	
	public boolean isEntryOrganisationsDefined() {
		return entryOrganisations != null && !entryOrganisations.isEmpty();
	}

	public List<OrganisationRef> getEntryOrganisation() {
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
	
	public List<Long> getExcludeEntryKeys() {
		return excludeEntryKeys;
	}

	public void setExcludeEntryKeys(List<Long> excludeEntryKeys) {
		this.excludeEntryKeys = excludeEntryKeys;
	}

	public boolean hasStatus() {
		return status != null && status.length > 0;
	}
	
	public boolean hasStatus(RepositoryEntryStatusEnum search) {
		if(status != null && status.length > 0) {
			for(RepositoryEntryStatusEnum s:status) {
				if(s == search) {
					return true;
				}
			}
		}
		return false;
	}
	
	public RepositoryEntryStatusEnum[] getStatus() {
		return status;
	}

	public void setStatus(RepositoryEntryStatusEnum[] statusArr) {
		if(statusArr == null || statusArr.length == 0) {
			status = null;
		} else {
			status = statusArr;
		}
	}

	public enum OrderBy {
		key,
		favorit,	
		type,
		technicalType,
		displayname,
		authors,
		author,
		license,
		location,
		access,
		ac,
		creationDate,
		lastUsage,
		externalId,
		externalRef,
		lifecycleLabel,
		lifecycleSoftkey,
		lifecycleStart,
		lifecycleEnd,
		references,
		deletionDate,
		deletedBy,
		lectureEnabled,
		guests,
		oer
	}
	
	public enum ResourceUsage {
		all,
		used,
		notUsed
	}

	public enum OERRelease {
		all,
		released,
		notReleased
	}
}

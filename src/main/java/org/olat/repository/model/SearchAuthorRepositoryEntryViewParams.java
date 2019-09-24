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
import java.util.List;
import java.util.Set;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;

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
	private Boolean closed;
	private boolean deleted = false;
	private boolean ownedResourcesOnly;
	private ResourceUsage resourceUsage = ResourceUsage.all;
	
	private String idAndRefs;
	private String idRefsAndTitle;
	private String author;
	private String displayname;
	private String description;
	
	private OrderBy orderBy;
	private boolean orderByAsc;
	private List<String> resourceTypes;
	private Set<Long> licenseTypeKeys;
	private List<OrganisationRef> entryOrganisations;
	
	public SearchAuthorRepositoryEntryViewParams(IdentityRef identity, Roles roles) {
		this.identity = identity;
		this.roles = roles;
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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
		if(this.resourceTypes == null) {
			this.resourceTypes = new ArrayList<>();
		}
		if(types != null) {
			for(String resourceType:types) {
				this.resourceTypes.add(resourceType);
			}
		}
	}
	
	public IdentityRef getIdentity() {
		return identity;
	}
	
	public Roles getRoles() {
		return roles;
	}
	
	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public Boolean getMarked() {
		return marked;
	}

	public void setMarked(Boolean marked) {
		this.marked = marked;
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
	
	public boolean isLicenseTypeDefined() {
		return licenseTypeKeys != null && !licenseTypeKeys.isEmpty();
	}
 
	public Set<Long> getLicenseTypeKeys() {
		return licenseTypeKeys;
	}

	public void setLicenseTypeKeys(Set<Long> licenseTypeKeys) {
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

	public enum OrderBy {
		key,
		favorit,
		type,
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
		guests
	}
	
	public enum ResourceUsage {
		all,
		used,
		notUsed
	}
}

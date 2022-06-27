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
package org.olat.modules.catalog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.core.id.Identity;
import org.olat.core.id.OrganisationRef;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.resource.accesscontrol.model.AccessMethod;

/**
 * 
 * Initial date: 24 May 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CatalogRepositoryEntrySearchParams {
	
	public enum OrderBy {
		key,
		type,
		displayName,
		externalId,
		externalRef,
		lifecycleLabel,
		lifecycleSoftkey,
		lifecycleStart,
		lifecycleEnd,
		location,
		publishedDate,
		random;
		
		public static final OrderBy secureValueOf(String val) {
			for(OrderBy order:values()) {
				if(order.name().equals(val)) {
					return order;
				}
			}
			return null;
		}
	}

	// Base selection
	private Identity member;
	private boolean isGuestOnly;
	private List<? extends OrganisationRef> offerOrganisations;
	private boolean offerValidAtNow = true;
	private Date offerValidAt;
	
	// Search
	private String searchString;
	
	// Filter
	private Collection<Long> repositoryEntryKeys;
	private String author;
	private Collection<RepositoryEntryStatusEnum> status;
	private Collection<Long> educationalTypeKeys;
	private Map<String, List<TaxonomyLevel>> identToTaxonomyLevels = new HashMap<>(3);
	private boolean taxonomyLevelChildren = true;
	private Boolean openAccess;
	private Boolean showAccessMethods;
	private Collection<AccessMethod> accessMethods;
	
	// Order
	private OrderBy orderBy;
	private boolean orderByAsc = true;

	public Identity getMember() {
		return member;
	}

	public void setMember(Identity member) {
		this.member = member;
	}

	public boolean isGuestOnly() {
		return isGuestOnly;
	}

	public void setGuestOnly(boolean isGuestOnly) {
		this.isGuestOnly = isGuestOnly;
	}

	public List<? extends OrganisationRef> getOfferOrganisations() {
		return offerOrganisations;
	}

	public void setOfferOrganisations(List<? extends OrganisationRef> offerOrganisations) {
		this.offerOrganisations = offerOrganisations;
	}

	public boolean isOfferValidAtNow() {
		return offerValidAtNow;
	}

	public void setOfferValidAtNow(boolean offerValidAtNow) {
		this.offerValidAtNow = offerValidAtNow;
	}

	public Date getOfferValidAt() {
		return offerValidAtNow? new Date(): offerValidAt;
	}

	public void setOfferValidAt(Date offerValidAt) {
		this.offerValidAt = offerValidAt;
	}

	public String getSearchString() {
		return searchString;
	}
	
	public void setSearchString(String searchString) {
		this.searchString = searchString;
	}

	public Collection<Long> getRepositoryEntryKeys() {
		return repositoryEntryKeys;
	}

	public void setRepositoryEntryKeys(Collection<Long> repositoryEntryKeys) {
		this.repositoryEntryKeys = repositoryEntryKeys;
	}

	public void setRepositoryEntries(List<? extends RepositoryEntryRef> repositoryEntries) {
		this.repositoryEntryKeys = repositoryEntries.stream().map(RepositoryEntryRef::getKey).collect(Collectors.toList());
	}
	
	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public Collection<RepositoryEntryStatusEnum> getStatus() {
		return status;
	}

	public void setStatus(Collection<RepositoryEntryStatusEnum> status) {
		this.status = status;
	}

	public Collection<Long> getEducationalTypeKeys() {
		return educationalTypeKeys;
	}

	public void setEducationalTypeKeys(Collection<Long> educationalTypeKeys) {
		this.educationalTypeKeys = educationalTypeKeys;
	}
	
	public Map<String, List<TaxonomyLevel>> getIdentToTaxonomyLevels() {
		return identToTaxonomyLevels;
	}

	public boolean isTaxonomyLevelChildren() {
		return taxonomyLevelChildren;
	}

	public void setTaxonomyLevelChildren(boolean taxonomyLevelChildren) {
		this.taxonomyLevelChildren = taxonomyLevelChildren;
	}

	public Boolean getOpenAccess() {
		return openAccess;
	}

	public void setOpenAccess(Boolean openAccess) {
		this.openAccess = openAccess;
	}

	public Boolean getShowAccessMethods() {
		return showAccessMethods;
	}

	public void setShowAccessMethods(Boolean showAccessMethods) {
		this.showAccessMethods = showAccessMethods;
	}

	public Collection<AccessMethod> getAccessMethods() {
		return accessMethods;
	}

	public void setAccessMethods(Collection<AccessMethod> accessMethods) {
		this.accessMethods = accessMethods;
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

	public CatalogRepositoryEntrySearchParams copy() {
		CatalogRepositoryEntrySearchParams copy = new CatalogRepositoryEntrySearchParams();
		
		// Base selection
		copy.member = this.member;
		copy.isGuestOnly = this.isGuestOnly;
		if (this.offerOrganisations != null) {
			copy.offerOrganisations = new ArrayList<>(offerOrganisations);
		}
		copy.offerValidAtNow = this.offerValidAtNow;
		if (this.offerValidAt != null) {
			copy.offerValidAt = new Date(this.offerValidAt.getTime());
		}
		
		// Search
		copy.searchString = this.searchString;
		
		// Filter
		if (repositoryEntryKeys != null) {
			copy.repositoryEntryKeys = new ArrayList<>(this.repositoryEntryKeys);
		}
		copy.author = this.author;
		if (this.educationalTypeKeys != null) {
			copy.educationalTypeKeys = new ArrayList<>(this.educationalTypeKeys);
		}
		copy.taxonomyLevelChildren = this.taxonomyLevelChildren;
		copy.identToTaxonomyLevels= new HashMap<>(3);
		for (Map.Entry<String, List<TaxonomyLevel>> entry : this.identToTaxonomyLevels.entrySet()) {
			copy.identToTaxonomyLevels.put(entry.getKey(), new ArrayList<>(entry.getValue()));
		}
		copy.openAccess = this.openAccess;
		copy.showAccessMethods = this.showAccessMethods;
		if (this.accessMethods != null) {
			copy.accessMethods = new ArrayList<>(accessMethods);
		}
		
		// Order
		copy.orderBy = this.orderBy;
		copy.orderByAsc = this.orderByAsc;
		
		return copy;
	}

}

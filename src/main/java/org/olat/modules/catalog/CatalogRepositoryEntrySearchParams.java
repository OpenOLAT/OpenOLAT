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
	
	public static final String KEY_LAUNCHER = "launcher";
	
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
		popularCourses,
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
	private List<CatalogSearchTerm> searchTerms;
	
	// Filter
	private Collection<Long> repositoryEntryKeys;
	private String author;
	private Collection<RepositoryEntryStatusEnum> status;
	private Map<String, Collection<String>> identToResourceTypes = new HashMap<>(2);
	private Map<String, Collection<Long>> identToEducationalTypeKeys = new HashMap<>(2);
	private Map<String, List<TaxonomyLevel>> identToTaxonomyLevels = new HashMap<>(2);
	private boolean taxonomyLevelChildren = true;
	private List<String> mainLanguages;
	private List<String> expendituresOfWork;
	private List<String> locations;
	private Collection<Long> licenseTypeKeys;
	private Collection<Long> lifecyclesPublicKeys;
	private Date lifecyclesPrivateFrom;
	private Date lifecyclesPrivateTo;
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
	
	public List<CatalogSearchTerm> getSearchTerms() {
		return searchTerms;
	}

	public void setSearchTerms(List<CatalogSearchTerm> searchTerms) {
		this.searchTerms = searchTerms;
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
	
	public Map<String, Collection<String>> getIdentToResourceTypes() {
		return identToResourceTypes;
	}

	public Map<String, Collection<Long>> getIdentToEducationalTypeKeys() {
		return identToEducationalTypeKeys;
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

	public List<String> getMainLanguages() {
		return mainLanguages;
	}

	public void setMainLanguages(List<String> mainLanguages) {
		this.mainLanguages = mainLanguages;
	}

	public List<String> getExpendituresOfWork() {
		return expendituresOfWork;
	}

	public void setExpendituresOfWork(List<String> expendituresOfWork) {
		this.expendituresOfWork = expendituresOfWork;
	}

	public List<String> getLocations() {
		return locations;
	}

	public void setLocations(List<String> locations) {
		this.locations = locations;
	}

	public Collection<Long> getLicenseTypeKeys() {
		return licenseTypeKeys;
	}

	public void setLicenseTypeKeys(Collection<Long> licenseTypeKeys) {
		this.licenseTypeKeys = licenseTypeKeys;
	}

	public Collection<Long> getLifecyclesPublicKeys() {
		return lifecyclesPublicKeys;
	}

	public void setLifecyclesPublicKeys(Collection<Long> lifecyclesPublicKeys) {
		this.lifecyclesPublicKeys = lifecyclesPublicKeys;
	}

	public Date getLifecyclesPrivateFrom() {
		return lifecyclesPrivateFrom;
	}

	public void setLifecyclesPrivateFrom(Date lifecyclesPrivateFrom) {
		this.lifecyclesPrivateFrom = lifecyclesPrivateFrom;
	}

	public Date getLifecyclesPrivateTo() {
		return lifecyclesPrivateTo;
	}

	public void setLifecyclesPrivateTo(Date lifecyclesPrivateTo) {
		this.lifecyclesPrivateTo = lifecyclesPrivateTo;
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
		if (this.searchTerms != null) {
			copy.searchTerms = new ArrayList<>(this.searchTerms);
		}
		
		// Filter
		if (repositoryEntryKeys != null) {
			copy.repositoryEntryKeys = new ArrayList<>(this.repositoryEntryKeys);
		}
		copy.author = this.author;
		copy.identToResourceTypes = new HashMap<>(this.identToResourceTypes.size());
		for (Map.Entry<String, Collection<String>> entry : this.identToResourceTypes.entrySet()) {
			copy.identToResourceTypes.put(entry.getKey(), new ArrayList<>(entry.getValue()));
		}
		copy.identToEducationalTypeKeys = new HashMap<>(this.identToEducationalTypeKeys.size());
		for (Map.Entry<String, Collection<Long>> entry : this.identToEducationalTypeKeys.entrySet()) {
			copy.identToEducationalTypeKeys.put(entry.getKey(), new ArrayList<>(entry.getValue()));
		}
		copy.identToTaxonomyLevels = new HashMap<>(this.identToTaxonomyLevels.size());
		for (Map.Entry<String, List<TaxonomyLevel>> entry : this.identToTaxonomyLevels.entrySet()) {
			copy.identToTaxonomyLevels.put(entry.getKey(), new ArrayList<>(entry.getValue()));
		}
		copy.taxonomyLevelChildren = this.taxonomyLevelChildren;
		if (this.mainLanguages != null) {
			copy.mainLanguages  = new ArrayList<>(this.mainLanguages);
		}
		if (this.expendituresOfWork != null) {
			copy.expendituresOfWork = new ArrayList<>(this.expendituresOfWork);
		}
		if (this.locations != null) {
			copy.locations = new ArrayList<>(this.locations);
		}
		if (this.licenseTypeKeys != null) {
			copy.licenseTypeKeys = new ArrayList<>(this.licenseTypeKeys);
		}
		if (this.lifecyclesPublicKeys != null) {
			copy.lifecyclesPublicKeys = new ArrayList<>(this.lifecyclesPublicKeys);
		}
		if (this.lifecyclesPrivateFrom != null) {
			copy.lifecyclesPrivateFrom = new Date(lifecyclesPrivateFrom.getTime());
		}
		if (this.lifecyclesPrivateTo != null) {
			copy.lifecyclesPrivateTo =  new Date(lifecyclesPrivateTo.getTime());
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

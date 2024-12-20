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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.id.Identity;
import org.olat.core.id.OrganisationRef;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 24 May 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CatalogEntrySearchParams {
	
	// Base selection
	private boolean webPublish;
	private Identity member;
	private boolean isGuestOnly;
	private List<? extends OrganisationRef> offerOrganisations;
	private boolean offerValidAtNow = true;
	private Date offerValidAt;
	
	// Filter
	private Collection<Long> repositoryEntryKeys;
	private Collection<String>launcherResourceTypes;
	private Collection<Long> launcherEducationalTypeKeys;
	private List<TaxonomyLevel> launcherTaxonomyLevels;

	public boolean isWebPublish() {
		return webPublish;
	}

	public void setWebPublish(boolean webPublish) {
		this.webPublish = webPublish;
	}

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

	public Collection<Long> getRepositoryEntryKeys() {
		return repositoryEntryKeys;
	}

	public void setRepositoryEntryKeys(Collection<Long> repositoryEntryKeys) {
		this.repositoryEntryKeys = repositoryEntryKeys;
	}

	public void setRepositoryEntries(List<? extends RepositoryEntryRef> repositoryEntries) {
		this.repositoryEntryKeys = repositoryEntries.stream().map(RepositoryEntryRef::getKey).collect(Collectors.toList());
	}
	
	public Collection<String> getLauncherResourceTypes() {
		return launcherResourceTypes;
	}

	public void setLauncherResourceTypes(Collection<String> launcherResourceTypes) {
		this.launcherResourceTypes = launcherResourceTypes;
	}

	public Collection<Long> getLauncherEducationalTypeKeys() {
		return launcherEducationalTypeKeys;
	}

	public void setLauncherEducationalTypeKeys(Collection<Long> launcherEducationalTypeKeys) {
		this.launcherEducationalTypeKeys = launcherEducationalTypeKeys;
	}

	public List<TaxonomyLevel> getLauncherTaxonomyLevels() {
		return launcherTaxonomyLevels;
	}

	public void setLauncherTaxonomyLevels(List<TaxonomyLevel> launcherTaxonomyLevels) {
		this.launcherTaxonomyLevels = launcherTaxonomyLevels;
	}

	public CatalogEntrySearchParams copy() {
		CatalogEntrySearchParams copy = new CatalogEntrySearchParams();
		
		// Base selection
		copy.webPublish = this.webPublish;
		copy.member = this.member;
		copy.isGuestOnly = this.isGuestOnly;
		if (this.offerOrganisations != null) {
			copy.offerOrganisations = new ArrayList<>(offerOrganisations);
		}
		copy.offerValidAtNow = this.offerValidAtNow;
		if (this.offerValidAt != null) {
			copy.offerValidAt = new Date(this.offerValidAt.getTime());
		}
		
		// Filter
		if (repositoryEntryKeys != null) {
			copy.repositoryEntryKeys = new ArrayList<>(this.repositoryEntryKeys);
		}
		if (this.launcherResourceTypes != null) {
			copy.launcherResourceTypes = Set.copyOf(this.launcherResourceTypes);
		}
		if (this.launcherEducationalTypeKeys != null) {
			copy.launcherEducationalTypeKeys = Set.copyOf(this.launcherEducationalTypeKeys);
		}
		if (this.launcherTaxonomyLevels != null) {
			copy.launcherTaxonomyLevels = List.copyOf(this.launcherTaxonomyLevels);
		}
		
		return copy;
	}

}

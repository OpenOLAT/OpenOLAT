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

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.commons.services.license.ResourceLicense;
import org.olat.core.id.Organisation;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 25 Oct 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryInfo implements RepositoryEntryRef {
	
	private final Long key;
	private ResourceLicense license;
	private Set<Long> taxonomyLevelKeys;
	private Set<TaxonomyLevel> taxonomyLevels;
	private Set<Long> organisationKeys;
	private Set<Organisation> organisations;
	
	public RepositoryEntryInfo(Long key) {
		this.key = key;
	}
	
	@Override
	public Long getKey() {
		return key;
	}

	public ResourceLicense getLicense() {
		return license;
	}
	
	public void setLicense(ResourceLicense license) {
		this.license = license;
	}
	
	public Set<Long> getTaxonomyLevelKeys() {
		return taxonomyLevelKeys != null? taxonomyLevelKeys: Collections.emptySet();
	}
	
	public Set<TaxonomyLevel> getTaxonomyLevels() {
		return taxonomyLevels != null? taxonomyLevels: Collections.emptySet();
	}

	public void setTaxonomyLevels(Set<TaxonomyLevel> taxonomyLevels) {
		this.taxonomyLevels = taxonomyLevels;
		this.taxonomyLevelKeys = taxonomyLevels.stream().map(TaxonomyLevel::getKey).collect(Collectors.toSet());
	}

	public Set<Long> getOrganisationKeys() {
		return organisationKeys != null? organisationKeys: Collections.emptySet();
	}

	public Set<Organisation> getOrganisations() {
		return organisations;
	}

	public void setOrganisations(Set<Organisation> organisations) {
		this.organisations = organisations;
		this.organisationKeys = organisations.stream().map(Organisation::getKey).collect(Collectors.toSet());
	}
	
}

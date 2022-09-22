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
package org.olat.modules.catalog.model;

import java.util.List;
import java.util.Set;

import org.olat.modules.catalog.CatalogRepositoryEntry;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryEducationalType;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.model.OLATResourceAccess;

/**
 * 
 * Initial date: 25 May 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CatalogRepositoryEntryImpl implements CatalogRepositoryEntry {
	
	private final Long key;
	private final String externalId;
	private final String externalRef;
	private final String displayname;
	private final String description;
	private final String teaser;
	private final String authors;
	private final String mainLanguage;
	private final String location;
	private final RepositoryEntryEducationalType educationalType;
	private final String expenditureOfWork;
	private final RepositoryEntryLifecycle lifecycle;
	private final RepositoryEntryStatusEnum status;
	private final boolean publicVisible;
	
	private final OLATResource olatResource;
	
	private Set<TaxonomyLevel> taxonomyLevels;
	private boolean member;
	private boolean openAccess;
	private List<OLATResourceAccess> resourceAccess;

	public CatalogRepositoryEntryImpl(RepositoryEntry re) {
		key = re.getKey();
		externalId = re.getExternalId();
		externalRef = re.getExternalRef();
		displayname = re.getDisplayname();
		description = re.getDescription();
		teaser = re.getTeaser();
		authors = re.getAuthors();
		mainLanguage = re.getMainLanguage();
		location = re.getLocation();
		educationalType = re.getEducationalType();
		expenditureOfWork = re.getExpenditureOfWork();
		lifecycle = re.getLifecycle();
		status = re.getEntryStatus();
		publicVisible = re.isPublicVisible();
		
		olatResource = re.getOlatResource();
	}

	@Override
	public Long getKey() {
		return key;
	}

	@Override
	public String getExternalId() {
		return externalId;
	}

	@Override
	public String getExternalRef() {
		return externalRef;
	}

	@Override
	public String getDisplayname() {
		return displayname;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String getTeaser() {
		return teaser;
	}

	@Override
	public String getAuthors() {
		return authors;
	}

	@Override
	public String getMainLanguage() {
		return mainLanguage;
	}

	@Override
	public String getLocation() {
		return location;
	}

	@Override
	public RepositoryEntryEducationalType getEducationalType() {
		return educationalType;
	}

	@Override
	public String getExpenditureOfWork() {
		return expenditureOfWork;
	}

	@Override
	public RepositoryEntryLifecycle getLifecycle() {
		return lifecycle;
	}

	@Override
	public RepositoryEntryStatusEnum getStatus() {
		return status;
	}

	@Override
	public boolean isPublicVisible() {
		return publicVisible;
	}

	@Override
	public OLATResource getOlatResource() {
		return olatResource;
	}

	@Override
	public Set<TaxonomyLevel> getTaxonomyLevels() {
		return taxonomyLevels;
	}

	public void setTaxonomyLevels(Set<TaxonomyLevel> taxonomyLevels) {
		this.taxonomyLevels = taxonomyLevels;
	}

	@Override
	public boolean isMember() {
		return member;
	}

	public void setMember(boolean member) {
		this.member = member;
	}
	
	@Override
	public boolean isOpenAccess() {
		return openAccess;
	}

	public void setOpenAccess(boolean openAccess) {
		this.openAccess = openAccess;
	}

	@Override
	public List<OLATResourceAccess> getResourceAccess() {
		return resourceAccess;
	}

	public void setResourceAccess(List<OLATResourceAccess> resourceAccess) {
		this.resourceAccess = resourceAccess;
	}
	
}

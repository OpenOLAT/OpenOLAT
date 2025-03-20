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

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.olat.core.commons.services.license.License;
import org.olat.modules.catalog.CatalogEntry;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryEducationalType;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.model.OLATResourceAccess;

/**
 * 
 * Initial date: 25 May 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CatalogEntryImpl implements CatalogEntry {
	
	private final Long repositotyEntryKey;
	private final Long curriculumElementKey;
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
	
	private String lifecycleLabel;
	private String lifecycleSoftKey;
	private Date lifecycleStart;
	private Date lifecycleEnd;
	private final RepositoryEntryStatusEnum status;
	private final Date publishedDate;
	private final boolean publicVisible;
	
	private final Long curriculumKey;
	private final String curriculumElementTypeName;
	
	private final OLATResource olatResource;
	
	private Set<TaxonomyLevel> taxonomyLevels;
	private boolean member;
	private boolean reservationAvailable;
	private boolean openAccess;
	private boolean guestAccess;
	private List<OLATResourceAccess> resourceAccess;
	private final Long maxParticipants;
	private Long numParticipants;
	private License license;
	private final boolean singleCourseImplementation;
	private RepositoryEntry singleCourse;

	public CatalogEntryImpl(RepositoryEntry re) {
		repositotyEntryKey = re.getKey();
		curriculumElementKey = null;
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
		
		if(re.getLifecycle() != null) {
			lifecycleStart = re.getLifecycle().getValidFrom();
			lifecycleEnd = re.getLifecycle().getValidTo();
			if(!re.getLifecycle().isPrivateCycle()) {
				lifecycleLabel = re.getLifecycle().getLabel();
				lifecycleSoftKey = re.getLifecycle().getSoftKey();
			}
		}
		status = re.getEntryStatus();
		publishedDate = re.getStatusPublishedDate();
		publicVisible = re.isPublicVisible();
		maxParticipants = null;
		
		curriculumKey = null;
		curriculumElementTypeName = null;
		singleCourseImplementation = false;
		
		olatResource = re.getOlatResource();
	}
	
	public CatalogEntryImpl(CurriculumElement element) {
		repositotyEntryKey = null;
		curriculumElementKey = element.getKey();
		externalId = element.getExternalId();
		externalRef = element.getIdentifier();
		displayname = element.getDisplayName();
		description = element.getDescription();
		teaser = element.getTeaser();
		authors = element.getAuthors();
		mainLanguage = element.getMainLanguage();
		location = element.getLocation();
		educationalType = element.getEducationalType();
		expenditureOfWork = element.getExpenditureOfWork();
		lifecycleStart = element.getBeginDate();
		lifecycleEnd = element.getEndDate();
		status = null;
		publishedDate = null;
		publicVisible = true;
		maxParticipants = element.getMaxParticipants();
		
		curriculumKey = element.getCurriculum().getKey();
		curriculumElementTypeName = element.getType().getDisplayName();
		singleCourseImplementation = element.isSingleCourseImplementation();
		
		olatResource = element.getResource();
	}

	@Override
	public Long getRepositoryEntryKey() {
		return repositotyEntryKey;
	}

	@Override
	public Long getCurriculumElementKey() {
		return curriculumElementKey;
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
	public String getLifecycleLabel() {
		return lifecycleLabel;
	}

	@Override
	public String getLifecycleSoftKey() {
		return lifecycleSoftKey;
	}

	@Override
	public Date getLifecycleStart() {
		return lifecycleStart;
	}

	@Override
	public Date getLifecycleEnd() {
		return lifecycleEnd;
	}

	@Override
	public RepositoryEntryStatusEnum getStatus() {
		return status;
	}

	@Override
	public Date getPublishedDate() {
		return publishedDate;
	}

	@Override
	public boolean isPublicVisible() {
		return publicVisible;
	}

	@Override
	public Long getCurriculumKey() {
		return curriculumKey;
	}

	@Override
	public String getCurriculumElementTypeName() {
		return curriculumElementTypeName;
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
	public boolean isReservationAvailable() {
		return reservationAvailable;
	}

	public void setReservationAvailable(boolean reservationAvailable) {
		this.reservationAvailable = reservationAvailable;
	}

	@Override
	public boolean isOpenAccess() {
		return openAccess;
	}

	public void setOpenAccess(boolean openAccess) {
		this.openAccess = openAccess;
	}

	@Override
	public boolean isGuestAccess() {
		return guestAccess;
	}

	public void setGuestAccess(boolean guestAccess) {
		this.guestAccess = guestAccess;
	}

	@Override
	public List<OLATResourceAccess> getResourceAccess() {
		return resourceAccess;
	}

	public void setResourceAccess(List<OLATResourceAccess> resourceAccess) {
		this.resourceAccess = resourceAccess;
	}

	@Override
	public Long getMaxParticipants() {
		return maxParticipants;
	}

	@Override
	public Long getNumParticipants() {
		return numParticipants;
	}

	public void setNumParticipants(Long numParticipants) {
		this.numParticipants = numParticipants;
	}

	@Override
	public License getLicense() {
		return license;
	}

	public void setLicense(License license) {
		this.license = license;
	}

	@Override
	public boolean isSingleCourseImplementation() {
		return singleCourseImplementation;
	}

	@Override
	public RepositoryEntry getSingleCourse() {
		return singleCourse;
	}

	public void setSingleCourse(RepositoryEntry singleCourse) {
		this.singleCourse = singleCourse;
	}
	
}

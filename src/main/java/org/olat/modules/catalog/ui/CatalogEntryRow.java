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
package org.olat.modules.catalog.ui;

import java.util.Date;
import java.util.Set;

import org.olat.core.commons.services.license.License;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.catalog.CatalogEntry;
import org.olat.modules.catalog.CatalogEntrySecurityCallback;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.repository.RepositoryEntryEducationalType;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.ui.RepositoyUIFactory;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ParticipantsAvailability.ParticipantsAvailabilityNum;

/**
 * 
 * Initial date: 29.01.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CatalogEntryRow {
	
	private final Long repositotyEntryKey;
	private final Long curriculumElementKey;
	private final String externalId;
	private final String externalRef;
	private final String title;
	private final String authors;
	private final String mainLanguage;
	private final String location;
	private final String teaser;
	private String translatedTechnicalType;
	private final RepositoryEntryEducationalType educationalType;
	private final String expenditureOfWork;
	private final RepositoryEntryStatusEnum repositoryEntryStatus;
	private final CurriculumElementStatus curriculumElementStatus;
	private final boolean publicVisible;
	private final String lifecycleLabel;
	private final String lifecycleSoftKey;
	private final Date lifecycleStart;
	private final Date lifecycleEnd;
	
	private final Long curriculumKey;
	private final Long curriculumElementTypeKey;
	private final String curriculumElementTypeName;
	
	private final OLATResource olatResource;
	private final Set<TaxonomyLevel> taxonomyLevels;
	private Set<String> taxonomyLevelDisplayNames;
	private String taxonomyLevelTags;
	private final boolean member;
	private final boolean participant;
	private final boolean reservationAvailable;
	private final boolean openAccess;
	private final boolean guestAccess;
	private Set<String> accessMethodTypes;
	private String accessInfo;
	private String accessInfoIconCssClass;
	private String accessWarning;
	private String accessError;
	private final Long maxParticipants;
	private final Long numParticipants;
	private final Integer sortPriority;
	private ParticipantsAvailabilityNum participantsAvailabilityNum;
	private CatalogEntrySecurityCallback secCallback;
	private final License license;
	private final boolean singleCourseImplementation;
	private final Long singleCourseEntryKey;
	private final RepositoryEntryStatusEnum singleCourseEntryStatus;
	private String infoUrl;
	private String startUrl;
	
	private boolean certificate;
	private String creditPointAmount;
	
	private final Double averageRating;
	
	private String thumbnailRelPath;
	private FormItem startLink;
	private FormItem ratingFormItem;
	
	public CatalogEntryRow(CatalogEntry catalogEntry) {
		repositotyEntryKey = catalogEntry.getRepositoryEntryKey();
		curriculumElementKey = catalogEntry.getCurriculumElementKey();
		externalId = catalogEntry.getExternalId();
		externalRef = catalogEntry.getExternalRef();
		title = catalogEntry.getDisplayname();
		teaser = catalogEntry.getTeaser();
		authors = catalogEntry.getAuthors();
		mainLanguage = catalogEntry.getMainLanguage();
		location = catalogEntry.getLocation();
		educationalType = catalogEntry.getEducationalType();
		expenditureOfWork = catalogEntry.getExpenditureOfWork();
		repositoryEntryStatus = catalogEntry.getRepositoryEntryStatus();
		curriculumElementStatus = catalogEntry.getCurriculumElementStatus();
		publicVisible = catalogEntry.isPublicVisible();
		lifecycleLabel = catalogEntry.getLifecycleLabel();
		lifecycleSoftKey = catalogEntry.getLifecycleSoftKey();
		lifecycleStart = DateUtils.getStartOfDay(catalogEntry.getLifecycleStart());
		lifecycleEnd = DateUtils.getStartOfDay(catalogEntry.getLifecycleEnd());
		olatResource = catalogEntry.getOlatResource();
		taxonomyLevels = catalogEntry.getTaxonomyLevels();
		member = catalogEntry.isMember();
		participant = catalogEntry.isParticipant();
		reservationAvailable = catalogEntry.isReservationAvailable();
		openAccess = catalogEntry.isOpenAccess();
		guestAccess = catalogEntry.isGuestAccess();
		maxParticipants = catalogEntry.getMaxParticipants();
		numParticipants = catalogEntry.getNumParticipants();
		sortPriority = catalogEntry.getSortPriority();
		license = catalogEntry.getLicense();
		singleCourseImplementation = catalogEntry.isSingleCourseImplementation();
		singleCourseEntryKey = catalogEntry.getSingleCourseEntryKey();
		singleCourseEntryStatus = catalogEntry.getSingleCourseEntryStatus();
		
		averageRating = catalogEntry.getAverageRating();
		
		curriculumKey = catalogEntry.getCurriculumKey();
		curriculumElementTypeKey = catalogEntry.getCurriculumElementTypeKey();
		curriculumElementTypeName = catalogEntry.getCurriculumElementTypeName();
		
		certificate = catalogEntry.isHasCertificate();
		creditPointAmount = catalogEntry.getCreditPointAmount();
	}

	public Long getRepositoryEntryKey() {
		return repositotyEntryKey;
	}
	
	public OLATResourceable getRepositoryEntryResourceable() {
		return repositotyEntryKey != null
				? OresHelper.createOLATResourceableInstance("RepositoryEntry", getRepositoryEntryKey())
				: null;
	}

	public Long getCurriculumElementKey() {
		return curriculumElementKey;
	}

	public boolean isClosed() {
		return repositoryEntryStatus != null && repositoryEntryStatus.decommissioned();
	}

	public RepositoryEntryStatusEnum getRepositoryEntryStatus() {
		return repositoryEntryStatus;
	}
	
	public boolean isPublicVisible() {
		return publicVisible;
	}

	public String getExternalId() {
		return externalId;
	}

	public String getExternalRef() {
		return externalRef;
	}

	public String getTitle() {
		return title;
	}

	public String getLifecycleSoftKey() {
		return lifecycleSoftKey;
	}

	public String getLifecycleLabel() {
		return lifecycleLabel;
	}

	public Date getLifecycleStart() {
		return lifecycleStart;
	}

	public Date getLifecycleEnd() {
		return lifecycleEnd;
	}

	public Long getCurriculumKey() {
		return curriculumKey;
	}

	public Long getCurriculumElementTypeKey() {
		return curriculumElementTypeKey;
	}

	public String getCurriculumElementTypeName() {
		return curriculumElementTypeName;
	}

	public OLATResource getOlatResource() {
		return olatResource;
	}

	public boolean isMember() {
		return member;
	}
	
	public boolean isParticipant() {
		return participant;
	}
	
	public boolean isReservationAvailable() {
		return reservationAvailable;
	}

	public boolean isOpenAccess() {
		return openAccess;
	}
	
	public boolean isGuestAccess() {
		return guestAccess;
	}

	public Long getMaxParticipants() {
		return maxParticipants;
	}

	public Long getNumParticipants() {
		return numParticipants;
	}

	public ParticipantsAvailabilityNum getParticipantsAvailabilityNum() {
		return participantsAvailabilityNum;
	}

	public void setParticipantsAvailabilityNum(ParticipantsAvailabilityNum participantsAvailabilityNum) {
		this.participantsAvailabilityNum = participantsAvailabilityNum;
	}

	public Integer getSortPriority() {
		return sortPriority;
	}
	
	public CatalogEntrySecurityCallback getSecCallback() {
		return secCallback;
	}

	public void setSecCallback(CatalogEntrySecurityCallback secCallback) {
		this.secCallback = secCallback;
	}

	public Set<String> getAccessMethodTypes() {
		return accessMethodTypes;
	}

	public void setAccessMethodTypes(Set<String> accessMethodTypes) {
		this.accessMethodTypes = accessMethodTypes;
	}
	
	public String getAccessInfo() {
		return accessInfo;
	}

	public void setAccessInfo(String accessInfo) {
		this.accessInfo = accessInfo;
	}

	public String getAccessInfoIconCssClass() {
		return accessInfoIconCssClass;
	}

	public void setAccessInfoIconCssClass(String accessInfoIconCssClass) {
		this.accessInfoIconCssClass = accessInfoIconCssClass;
	}

	public String getAccessWarning() {
		return accessWarning;
	}

	public void setAccessWarning(String accessWarning) {
		this.accessWarning = accessWarning;
	}

	public String getAccessError() {
		return accessError;
	}

	public void setAccessError(String accessError) {
		this.accessError = accessError;
	}

	public String getAuthors() {
		return authors;
	}
	
	public String getMainLanguage() {
		return mainLanguage;
	}

	public String getTeaser() {
		return teaser;
	}
	
	public String getLocation() {
		return location;
	}

	public String getTranslatedTechnicalType() {
		return translatedTechnicalType;
	}

	public void setTranslatedTechnicalType(String translatedTechnicalType) {
		this.translatedTechnicalType = translatedTechnicalType;
	}

	public RepositoryEntryEducationalType getEducationalType() {
		return educationalType;
	}

	public String getEducationalTypei18nKey() {
		return RepositoyUIFactory.getI18nKey(educationalType);
	}

	public String getExpenditureOfWork() {
		return expenditureOfWork;
	}

	public String getThumbnailRelPath() {
		return thumbnailRelPath;
	}
	
	public Set<TaxonomyLevel> getTaxonomyLevels() {
		return taxonomyLevels;
	}

	public void setTaxonomyLevelTags(String taxonomyLevelTags) {
		this.taxonomyLevelTags = taxonomyLevelTags;
	}

	public Set<String> getTaxonomyLevelDisplayNames() {
		return taxonomyLevelDisplayNames;
	}

	public void setTaxonomyLevelDisplayNames(Set<String> taxonomyLevelDisplayNames) {
		this.taxonomyLevelDisplayNames = taxonomyLevelDisplayNames;
	}

	public String getTaxonomyLevelTags() {
		return taxonomyLevelTags;
	}

	public License getLicense() {
		return license;
	}

	public boolean isCertificate() {
		return certificate;
	}

	public String getCreditPointAmount() {
		return creditPointAmount;
	}

	public boolean isThumbnailAvailable() {
		return StringHelper.containsNonWhitespace(thumbnailRelPath);
	}
	
	public void setThumbnailRelPath(String thumbnailRelPath) {
		this.thumbnailRelPath = thumbnailRelPath;
	}
	
	public String getInfoUrl() {
		return infoUrl;
	}

	public void setInfoUrl(String infoUrl) {
		this.infoUrl = infoUrl;
	}

	public String getStartUrl() {
		return startUrl;
	}

	public void setStartUrl(String startUrl) {
		this.startUrl = startUrl;
	}
	
	public String getStartLinkName() {
		return startLink == null ? null :startLink.getComponent().getComponentName();
	}
	
	public FormItem getStartLink() {
		return startLink;
	}

	public void setStartLink(FormItem startLink) {
		this.startLink = startLink;
	}
	
	public String getRatingFormItemName() {
		return ratingFormItem == null ? null : ratingFormItem.getComponent().getComponentName();
	}

	public FormItem getRatingFormItem() {
		return ratingFormItem;
	}

	public void setRatingFormItem(FormItem ratingFormItem) {
		this.ratingFormItem = ratingFormItem;
	}

	public boolean isSingleCourseImplementation() {
		return singleCourseImplementation;
	}

	public Long getSingleCourseEntryKey() {
		return singleCourseEntryKey;
	}

	public RepositoryEntryStatusEnum getSingleCourseEntryStatus() {
		return singleCourseEntryStatus;
	}

	public boolean isUnpublishedImplementation() {
		if (isSingleCourseImplementation()) {
			return singleCourseEntryStatus == null 
					|| !RepositoryEntryStatusEnum.isInArray(singleCourseEntryStatus, RepositoryEntryStatusEnum.publishedAndClosed());
		} if (curriculumElementStatus != null) {
			return !CurriculumElementStatus.isInArray(curriculumElementStatus, CurriculumElementStatus.visibleUser());
		}
		
		return false;
	}

	public Double getAverageRating() {
		return averageRating;
	}
}
/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.repository.ui.list;

import java.util.Date;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryEducationalType;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.ui.RepositoyUIFactory;
import org.olat.resource.OLATResource;

/**
 * 
 * Initial date: 13 mars 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class InPreparationRow {
	
	private final Long repositoryEntryKey;
	private final Long curriculumElementKey;
	
	private final Date creationDate;
	private final Date lastModified;

	private final String displayname;
	private final String externalId;
	private final String externalRef;
	private final String authors;
	private final String teaser;
	private final RepositoryEntryStatusEnum entryStatus;
	private String translatedTechnicalType;
	
	private String lifecycleLabel;
	private String lifecycleSoftKey;
	private Date lifecycleStart;
	private Date lifecycleEnd;
	
	private final String location;
	private String thumbnailRelPath;
	private final OLATResource olatResource;
	private final RepositoryEntryEducationalType educationalType;
	
	private boolean marked;

	private FormLink markLink;
	private FormLink selectLink;
	private FormLink detailsLink;
	private FormLink detailsSmallLink;
	
	private Long key;
	
	public InPreparationRow(Long key, RepositoryEntry re, boolean marked) {
		repositoryEntryKey = re.getKey();
		curriculumElementKey = null;
		this.key = key;
		this.marked = marked;
		creationDate = re.getCreationDate();
		lastModified = re.getLastModified();
		externalId = re.getExternalId();
		externalRef = re.getExternalRef();
		displayname = re.getDisplayname();
		authors = re.getAuthors();
		teaser = re.getTeaser();
		entryStatus = re.getEntryStatus();
		if(re.getLifecycle() != null) {
			if(!re.getLifecycle().isPrivateCycle()) {
				lifecycleLabel = re.getLifecycle().getLabel();
				lifecycleSoftKey = re.getLifecycle().getSoftKey();
			}
			lifecycleStart = re.getLifecycle().getValidFrom();
			lifecycleEnd = re.getLifecycle().getValidTo();
		}
		location = re.getLocation();
		educationalType = re.getEducationalType();
		olatResource = re.getOlatResource();
	}
	
	public InPreparationRow(Long key, CurriculumElement element, RepositoryEntry entry, boolean marked) {
		repositoryEntryKey = entry == null ? null : entry.getKey();
		curriculumElementKey = element.getKey();
		this.key = key;
		this.marked = marked;
		creationDate = element.getCreationDate();
		lastModified = element.getLastModified();
		externalId = element.getExternalId();
		externalRef = element.getIdentifier();
		displayname = element.getDisplayName();
		authors = element.getAuthors();
		teaser = element.getTeaser();
		entryStatus = null;
		
		lifecycleStart = element.getBeginDate();
		lifecycleEnd = element.getEndDate();
		
		location = element.getLocation();
		educationalType = element.getEducationalType();
		olatResource = element.getResource();
	}
	
	public Long getKey() {
		return key;
	}

	public Long getRepositoryEntryKey() {
		return repositoryEntryKey;
	}

	public Long getCurriculumElementKey() {
		return curriculumElementKey;
	}
	
	public Date getCreationDate() {
		return creationDate;
	}
	
	public Date getLastModified() {
		return lastModified;
	}
	
	public boolean isMarked() {
		return marked;
	}
	
	public void setMarked(boolean marked) {
		this.marked = marked;
	}
	
	public RepositoryEntryStatusEnum getRepositoryEntryStatus() {
		return entryStatus == null ? RepositoryEntryStatusEnum.preparation : entryStatus;
	}

	public String getDisplayName() {
		return displayname;
	}

	public String getExternalId() {
		return externalId;
	}

	public String getExternalRef() {
		return externalRef;
	}
	
	public String getAuthors() {
		return authors;
	}
	
	public String getTeaser() {
		return teaser;
	}

	public String getLifecycleLabel() {
		return lifecycleLabel;
	}

	public void setLifecycleLabel(String lifecycleLabel) {
		this.lifecycleLabel = lifecycleLabel;
	}

	public String getLifecycleSoftKey() {
		return lifecycleSoftKey;
	}

	public void setLifecycleSoftKey(String lifecycleSoftKey) {
		this.lifecycleSoftKey = lifecycleSoftKey;
	}

	public Date getLifecycleStart() {
		return lifecycleStart;
	}

	public void setLifecycleStart(Date lifecycleStart) {
		this.lifecycleStart = lifecycleStart;
	}

	public Date getLifecycleEnd() {
		return lifecycleEnd;
	}

	public void setLifecycleEnd(Date lifecycleEnd) {
		this.lifecycleEnd = lifecycleEnd;
	}

	public String getLocation() {
		return location;
	}

	public RepositoryEntryEducationalType getEducationalType() {
		return educationalType;
	}
	
	public String getEducationalTypei18nKey() {
		return RepositoyUIFactory.getI18nKey(educationalType);
	}
	
	public String getTranslatedTechnicalType() {
		return translatedTechnicalType;
	}

	public void setTranslatedTechnicalType(String translatedTechnicalType) {
		this.translatedTechnicalType = translatedTechnicalType;
	}
	
	public OLATResource getOlatResource() {
		return olatResource;
	}
	
	public boolean isThumbnailAvailable() {
		return StringHelper.containsNonWhitespace(thumbnailRelPath);
	}

	public String getThumbnailRelPath() {
		return thumbnailRelPath;
	}

	public void setThumbnailRelPath(String thumbnailRelPath) {
		this.thumbnailRelPath = thumbnailRelPath;
	}
	
	public FormLink getMarkLink() {
		return markLink;
	}
	
	public String getMarkLinkName() {
		return markLink == null ? null : markLink.getComponent().getComponentName();
	}
	
	public void setMarkLink(FormLink markLink) {
		this.markLink = markLink;
	}

	public String getDetailsLinkName() {
		return detailsLink == null ? null : detailsLink.getComponent().getComponentName();
	}

	public FormLink getDetailsLink() {
		return detailsLink;
	}

	public void setDetailsLink(FormLink detailsLink) {
		this.detailsLink = detailsLink;
	}

	public FormLink getDetailsSmallLink() {
		return detailsSmallLink;
	}

	public void setDetailsSmallLink(FormLink detailsSmallLink) {
		this.detailsSmallLink = detailsSmallLink;
	}
	
	public String getSelectLinkName() {
		return selectLink == null ? null : selectLink.getComponent().getComponentName();
	}
	
	public FormLink getSelectLink() {
		return selectLink;
	}
	
	public void setSelectLink(FormLink selectLink) {
		this.selectLink = selectLink;
	}
}

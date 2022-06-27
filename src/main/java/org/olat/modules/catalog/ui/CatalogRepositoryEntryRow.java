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
import java.util.List;
import java.util.Set;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.catalog.CatalogRepositoryEntry;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.repository.RepositoryEntryEducationalType;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.repository.ui.PriceMethod;
import org.olat.repository.ui.RepositoyUIFactory;

/**
 * 
 * Initial date: 29.01.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CatalogRepositoryEntryRow implements RepositoryEntryRef {
	
	private final Long key;
	private final String externalId;
	private final String externalRef;
	private final String name;
	private final String authors;
	private final String location;
	private final String teaser;
	private final RepositoryEntryEducationalType educationalType;
	private final String expenditureOfWork;
	private final RepositoryEntryStatusEnum status;
	private final boolean publicVisible;
	
	private String lifecycleLabel;
	private String lifecycleSoftKey;
	private Date lifecycleStart;
	private Date lifecycleEnd;
	
	private final OLATResourceable olatResource;
	private final Set<TaxonomyLevel> taxonomyLevels;
	private final boolean member;
	private final boolean openAccess;
	private List<PriceMethod> accessTypes;
	private String thumbnailRelPath;
	private FormLink selectLink;
	private FormLink startLink;
	private FormLink startSmallLink;
	private FormLink detailsLink;
	private FormLink detailsSmallLink;
	
	public CatalogRepositoryEntryRow(CatalogRepositoryEntry catalogRepositoryEntry) {
		key = catalogRepositoryEntry.getKey();
		externalId = catalogRepositoryEntry.getExternalId();
		externalRef = catalogRepositoryEntry.getExternalRef();
		name = catalogRepositoryEntry.getDisplayname();
		teaser = catalogRepositoryEntry.getTeaser();
		authors = catalogRepositoryEntry.getAuthors();
		location = catalogRepositoryEntry.getLocation();
		educationalType = catalogRepositoryEntry.getEducationalType();
		expenditureOfWork = catalogRepositoryEntry.getExpenditureOfWork();
		status = catalogRepositoryEntry.getStatus();
		publicVisible = catalogRepositoryEntry.isPublicVisible();
		olatResource = OresHelper.clone(catalogRepositoryEntry.getOlatResource());
		taxonomyLevels = catalogRepositoryEntry.getTaxonomyLevels();
		member = catalogRepositoryEntry.isMember();
		openAccess = catalogRepositoryEntry.isOpenAccess();
		
		RepositoryEntryLifecycle reLifecycle = catalogRepositoryEntry.getLifecycle();
		if(reLifecycle != null) {
			setLifecycleStart(reLifecycle.getValidFrom());
			setLifecycleEnd(reLifecycle.getValidTo());
			if(!reLifecycle.isPrivateCycle()) {
				setLifecycleLabel(reLifecycle.getLabel());
				setLifecycleSoftKey(reLifecycle.getSoftKey());
			}
		}
	}
	
	@Override
	public Long getKey() {
		return key;
	}

	public boolean isClosed() {
		return status.decommissioned();
	}

	public RepositoryEntryStatusEnum getStatus() {
		return status;
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

	public String getDisplayName() {
		return name;
	}

	public String getLifecycleSoftKey() {
		return lifecycleSoftKey;
	}

	public void setLifecycleSoftKey(String lifecycleSoftKey) {
		this.lifecycleSoftKey = lifecycleSoftKey;
	}

	public String getLifecycleLabel() {
		return lifecycleLabel;
	}

	public void setLifecycleLabel(String lifecycleLabel) {
		this.lifecycleLabel = lifecycleLabel;
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
	
	public boolean isActive() {
		boolean isCurrent = true; 
		if (lifecycleEnd != null || lifecycleStart != null) {
			Date now = new Date();
			if (lifecycleStart != null && lifecycleStart.after(now)) {
				isCurrent = false;
			} else if (lifecycleEnd != null && lifecycleEnd.before(now)) {
				isCurrent = false;
			}
		}
		return isCurrent;
	}

	public OLATResourceable getOlatResource() {
		return olatResource;
	}

	public boolean isMember() {
		return member;
	}
	
	public boolean isOpenAccess() {
		return openAccess;
	}

	public List<PriceMethod> getAccessTypes() {
		return accessTypes;
	}

	public void setAccessTypes(List<PriceMethod> accessTypes) {
		this.accessTypes = accessTypes;
	}
	
	public String getAuthors() {
		return authors;
	}
	
	public String getTeaser() {
		return teaser;
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

	public String getExpenditureOfWork() {
		return expenditureOfWork;
	}

	public String getThumbnailRelPath() {
		return thumbnailRelPath;
	}
	
	public Set<TaxonomyLevel> getTaxonomyLevels() {
		return taxonomyLevels;
	}
	
	public boolean isThumbnailAvailable() {
		return StringHelper.containsNonWhitespace(thumbnailRelPath);
	}
	
	public void setThumbnailRelPath(String thumbnailRelPath) {
		this.thumbnailRelPath = thumbnailRelPath;
	}
	
	public String getSelectLinkName() {
		return selectLink == null ? null :selectLink.getComponent().getComponentName();
	}
	
	public FormLink getSelectLink() {
		return selectLink;
	}

	public void setSelectLink(FormLink selectLink) {
		this.selectLink = selectLink;
	}
	
	public String getStartLinkName() {
		return startLink == null ? null :startLink.getComponent().getComponentName();
	}
	
	public FormLink getStartLink() {
		return startLink;
	}

	public void setStartLink(FormLink startLink) {
		this.startLink = startLink;
	}
	
	public FormLink getStartSmallLink() {
		return startSmallLink;
	}

	public void setStartSmallLink(FormLink startSmallLink) {
		this.startSmallLink = startSmallLink;
	}

	public String getDetailsLinkName() {
		return detailsLink.getComponent().getComponentName();
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

	@Override
	public int hashCode() {
		return key == null ? 161745452 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof CatalogRepositoryEntryRow) {
			CatalogRepositoryEntryRow row = (CatalogRepositoryEntryRow)obj;
			return key != null && key.equals(row.getKey());
		}
		return false;
	}
}
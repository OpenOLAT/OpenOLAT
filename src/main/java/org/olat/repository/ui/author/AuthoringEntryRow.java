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
package org.olat.repository.ui.author;

import java.util.Date;
import java.util.List;

import org.olat.core.commons.services.license.License;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.repository.RepositoryEntryAuthorView;
import org.olat.repository.RepositoryEntryEducationalType;
import org.olat.repository.RepositoryEntryLight;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.repository.ui.PriceMethod;

/**
 * 
 * Initial date: 28.04.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AuthoringEntryRow implements RepositoryEntryLight {
	private boolean marked;
	private boolean selected;
	
	private final Long key;
	private final String technicalType;
	private final String name;
	private final String author;
	private final String authors;
	private final String location;
	private final String shortenedDescription;
	
	private RepositoryEntryStatusEnum status;
	private final boolean publicVisible;

	private final Date lastUsage;
	private final Date creationDate;
	
	private final String externalId;
	private final String externalRef;
	private final boolean managed;
	private final RepositoryEntryManagedFlag[] managedFlags;
	
	private String lifecycleLabel;
	private String lifecycleSoftKey;
	private Date lifecycleStart;
	private Date lifecycleEnd;
	
	private final int numOfReferences;
	private final int numOfCurriculumElements;
	
	private final boolean lectureEnabled;
	private final boolean rollCallEnabled;
	
	private final String deletedByFullName;
	private final Date deletionDate;
	
	private boolean openAccess;
	private boolean guestAccess;
	private List<PriceMethod> accessTypes;
	private List<TaxonomyLevel> taxonomyLevels;
	private final RepositoryEntryEducationalType educationalType;

	private OLATResourceable olatResource;
	
	private License license;
	
	private final String url;
	
	private FormLink markLink;
	private FormLink infosLink;
	private FormLink toolsLink;
	private FormLink referencesLink;
	private FormLink detailsLink;
	
	public AuthoringEntryRow(RepositoryEntryAuthorView view, String fullnameAuthor) {
		key = view.getKey();
		technicalType = view.getTechnicalType();
		name = view.getDisplayname();
		author = fullnameAuthor;
		authors = view.getAuthors();
		location = view.getLocation();
		shortenedDescription = StringHelper.truncateText(view.getDescription());
		educationalType = view.getEducationalType();
		
		String path = "[RepositoryEntry:" + key + "]";
		url = BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathString(path);

		lastUsage = view.getLastUsage();
		creationDate = view.getCreationDate();
		
		externalId = view.getExternalId();
		externalRef = view.getExternalRef();
		managed = view.getManagedFlags() != null && view.getManagedFlags().length > 0;
		managedFlags = view.getManagedFlags();
		
		status = view.getEntryStatus();
		publicVisible = view.isPublicVisible();
		
		olatResource = OresHelper.clone(view.getOlatResource());
		
		RepositoryEntryLifecycle lifecycle = view.getLifecycle();
		if(lifecycle != null) {
			lifecycleStart = lifecycle.getValidFrom();
			lifecycleEnd = lifecycle.getValidTo();
			if(!lifecycle.isPrivateCycle()) {
				lifecycleLabel = lifecycle.getLabel();
				lifecycleSoftKey = lifecycle.getSoftKey();
			}
		}
		
		numOfReferences = view.getNumOfReferences();
		numOfCurriculumElements = view.getNumOfCurriculumElements();
		lectureEnabled = view.isLectureEnabled();
		rollCallEnabled = view.isRollCallEnabled();
		
		deletedByFullName = view.getDeletedByFullName();
		deletionDate = view.getDeletionDate();
	}
	
	public String getCssClass() {
		return "o_CourseModule_icon";
	}
	
	@Override
	public Long getKey() {
		return key;
	}
	
	public Date getCreationDate() {
		return creationDate;
	}
	
	public String getUrl() {
		return url;
	}
	
	@Override
	public RepositoryEntryStatusEnum getEntryStatus() {
		return status;
	}

	@Override
	public boolean isPublicVisible() {
		return publicVisible;
	}

	public Date getLastUsage() {
		return lastUsage;
	}

	public String getTechnicalType() {
		return technicalType;
	}

	@Override
	public String getDisplayname() {
		return name;
	}

	@Override
	public String getDescription() {
		return getShortenedDescription();
	}
	
	public String getShortenedDescription() {
		return shortenedDescription;
	}

	public String getExternalId() {
		return externalId;
	}

	public String getExternalRef() {
		return externalRef;
	}
	
	public boolean isManaged() {
		return managed;
	}
	
	public RepositoryEntryManagedFlag[] getManagedFlags() {
		return managedFlags;
	}

	public String getLifecycleLabel() {
		return lifecycleLabel;
	}

	public String getLifecycleSoftKey() {
		return lifecycleSoftKey;
	}

	public Date getLifecycleStart() {
		return lifecycleStart;
	}

	public Date getLifecycleEnd() {
		return lifecycleEnd;
	}
	
	public int getNumOfReferences() {
		return numOfReferences;
	}
	
	public int getNumOfCurriculumElements() {
		return numOfCurriculumElements;
	}

	public boolean isLectureEnabled() {
		return lectureEnabled;
	}

	public boolean isRollCallEnabled() {
		return rollCallEnabled;
	}

	public String getDeletedByFullName() {
		return deletedByFullName;
	}

	public Date getDeletionDate() {
		return deletionDate;
	}

	public boolean isOpenAccess() {
		return openAccess;
	}

	public void setOpenAccess(boolean openAccess) {
		this.openAccess = openAccess;
	}

	public boolean isGuestAccess() {
		return guestAccess;
	}

	public void setGuestAccess(boolean guestAccess) {
		this.guestAccess = guestAccess;
	}

	public List<PriceMethod> getAccessTypes() {
		return accessTypes;
	}

	public void setAccessTypes(List<PriceMethod> accessTypes) {
		this.accessTypes = accessTypes;
	}
	
	public List<TaxonomyLevel> getTaxonomyLevels() {
		return taxonomyLevels;
	}

	public void setTaxonomyLevels(List<TaxonomyLevel> taxonomyLevels) {
		this.taxonomyLevels = taxonomyLevels;
	}
	
	public RepositoryEntryEducationalType getEducationalType() {
		return educationalType;
	}

	public OLATResourceable getRepositoryEntryResourceable() {
		return OresHelper.createOLATResourceableInstance("RepositoryEntry", getKey());
	}
	
	@Override
	public String getResourceType() {
		return olatResource.getResourceableTypeName();
	}

	/**
	 * This is a clone of the repositoryEntry.getOLATResource();
	 * @return
	 */
	public OLATResourceable getOLATResourceable() {
		return olatResource;
	}

	public String getAuthor() {
		return author;
	}

	public String getAuthors() {
		return authors;
	}

	public String getLocation() {
		return location;
	}
	
	public boolean isMarked() {
		return marked;
	}
	
	public void setMarked(boolean marked) {
		this.marked = marked;
	}
	
	public boolean isSelected() {
		return selected;
	}
	
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public License getLicense() {
		return license;
	}

	public void setLicense(License license) {
		this.license = license;
	}

	public FormLink getMarkLink() {
		return markLink;
	}

	public void setMarkLink(FormLink markLink) {
		this.markLink = markLink;
	}

	public FormLink getToolsLink() {
		return toolsLink;
	}

	public void setToolsLink(FormLink toolsLink) {
		this.toolsLink = toolsLink;
	}
	
	public FormLink getInfosLink() {
		return infosLink;
	}

	public void setInfosLink(FormLink infosLink) {
		this.infosLink = infosLink;
	}

	public FormLink getReferencesLink() {
		return referencesLink;
	}

	public void setReferencesLink(FormLink referencesLink) {
		this.referencesLink = referencesLink;
	}

	public FormLink getDetailsLink() {
		return detailsLink;
	}

	public void setDetailsLink(FormLink detailsLink) {
		this.detailsLink = detailsLink;
	}

	@Override
	public int hashCode() {
		return key == null ? -79224867 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof AuthoringEntryRow) {
			AuthoringEntryRow row = (AuthoringEntryRow)obj;
			return key != null && key.equals(row.getKey());
		}
		return super.equals(obj);
	}
}
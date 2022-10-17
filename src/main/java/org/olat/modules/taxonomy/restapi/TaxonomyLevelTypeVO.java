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
package org.olat.modules.taxonomy.restapi;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.olat.modules.taxonomy.TaxonomyLevelType;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 
 * Initial date: 5 Oct 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "taxonomyLevelTypeVO")
public class TaxonomyLevelTypeVO {
	
	private Long key;
	private String identifier;
	private String displayName;
	private String description;
	private String externalId;

	@Schema(required = true, description = "Action to be performed on managedFlags", allowableValues = { 
			"all",
			 "identifier(all)",
			 "displayName(all)",
			 "description(all)",
			 "cssClass(all)",
			 "externalId(all)",
			 "visibility(all)",
			 "subTypes(all)",
			 "librarySettings(all)",
			 "copy(all)",
			 "delete(all)"})
	private String managedFlags;
	
	private String cssClass;
	private Boolean visible;

	private Boolean documentsLibraryEnabled;
	private Boolean documentsLibraryManagerCompetenceEnabled;
	private Boolean documentsLibraryTeachCompetenceReadEnabled;
	private Integer documentsLibraryTeachCompetenceReadParentLevels;
	private Boolean documentsLibraryTeachCompetenceWriteEnabled;
	private Boolean documentsLibraryHaveCompetenceReadEnabled;
	private Boolean documentsLibraryTargetCompetenceReadEnabled;
	
	public TaxonomyLevelTypeVO() {
		//
	}
	
	public TaxonomyLevelTypeVO(TaxonomyLevelType taxonomyLevelType) {
		key = taxonomyLevelType.getKey();
		identifier = taxonomyLevelType.getIdentifier();
		displayName = taxonomyLevelType.getDisplayName();
		description = taxonomyLevelType.getDescription();
		externalId = taxonomyLevelType.getExternalId();
		managedFlags = taxonomyLevelType.getManagedFlagsString();
		
		cssClass = taxonomyLevelType.getCssClass();
		visible = taxonomyLevelType.isVisible();
		
		documentsLibraryManagerCompetenceEnabled = taxonomyLevelType.isDocumentsLibraryManageCompetenceEnabled();
		documentsLibraryTeachCompetenceReadEnabled = taxonomyLevelType.isDocumentsLibraryTeachCompetenceReadEnabled();
		documentsLibraryTeachCompetenceReadParentLevels = taxonomyLevelType.getDocumentsLibraryTeachCompetenceReadParentLevels();
		documentsLibraryTeachCompetenceWriteEnabled = taxonomyLevelType.isDocumentsLibraryTeachCompetenceWriteEnabled();
		documentsLibraryHaveCompetenceReadEnabled = taxonomyLevelType.isDocumentsLibraryHaveCompetenceReadEnabled();
		documentsLibraryTargetCompetenceReadEnabled = taxonomyLevelType.isDocumentsLibraryTargetCompetenceReadEnabled();
	}

	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public String getManagedFlags() {
		return managedFlags;
	}

	public void setManagedFlags(String managedFlags) {
		this.managedFlags = managedFlags;
	}

	public String getCssClass() {
		return cssClass;
	}

	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
	}

	public Boolean getVisible() {
		return visible;
	}

	public void setVisible(Boolean visible) {
		this.visible = visible;
	}

	public Boolean getDocumentsLibraryEnabled() {
		return documentsLibraryEnabled;
	}

	public void setDocumentsLibraryEnabled(Boolean documentsLibraryEnabled) {
		this.documentsLibraryEnabled = documentsLibraryEnabled;
	}

	public Boolean getDocumentsLibraryManagerCompetenceEnabled() {
		return documentsLibraryManagerCompetenceEnabled;
	}

	public void setDocumentsLibraryManagerCompetenceEnabled(Boolean documentsLibraryManagerCompetenceEnabled) {
		this.documentsLibraryManagerCompetenceEnabled = documentsLibraryManagerCompetenceEnabled;
	}

	public Boolean getDocumentsLibraryTeachCompetenceReadEnabled() {
		return documentsLibraryTeachCompetenceReadEnabled;
	}

	public void setDocumentsLibraryTeachCompetenceReadEnabled(Boolean documentsLibraryTeachCompetenceReadEnabled) {
		this.documentsLibraryTeachCompetenceReadEnabled = documentsLibraryTeachCompetenceReadEnabled;
	}

	public Integer getDocumentsLibraryTeachCompetenceReadParentLevels() {
		return documentsLibraryTeachCompetenceReadParentLevels;
	}

	public void setDocumentsLibraryTeachCompetenceReadParentLevels(
			Integer documentsLibraryTeachCompetenceReadParentLevels) {
		this.documentsLibraryTeachCompetenceReadParentLevels = documentsLibraryTeachCompetenceReadParentLevels;
	}

	public Boolean getDocumentsLibraryTeachCompetenceWriteEnabled() {
		return documentsLibraryTeachCompetenceWriteEnabled;
	}

	public void setDocumentsLibraryTeachCompetenceWriteEnabled(Boolean documentsLibraryTeachCompetenceWriteEnabled) {
		this.documentsLibraryTeachCompetenceWriteEnabled = documentsLibraryTeachCompetenceWriteEnabled;
	}

	public Boolean getDocumentsLibraryHaveCompetenceReadEnabled() {
		return documentsLibraryHaveCompetenceReadEnabled;
	}

	public void setDocumentsLibraryHaveCompetenceReadEnabled(Boolean documentsLibraryHaveCompetenceReadEnabled) {
		this.documentsLibraryHaveCompetenceReadEnabled = documentsLibraryHaveCompetenceReadEnabled;
	}

	public Boolean getDocumentsLibraryTargetCompetenceReadEnabled() {
		return documentsLibraryTargetCompetenceReadEnabled;
	}

	public void setDocumentsLibraryTargetCompetenceReadEnabled(Boolean documentsLibraryTargetCompetenceReadEnabled) {
		this.documentsLibraryTargetCompetenceReadEnabled = documentsLibraryTargetCompetenceReadEnabled;
	}
}
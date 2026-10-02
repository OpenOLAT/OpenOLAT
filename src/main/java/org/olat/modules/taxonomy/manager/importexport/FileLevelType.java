/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.taxonomy.manager.importexport;

import java.util.ArrayList;
import java.util.List;

import org.olat.modules.taxonomy.TaxonomyLevelType;

/**
 * One row of the level types sheet.
 *
 * Initial date: 30 Sept 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 */
public class FileLevelType extends FileRow {

	private String identifier;
	private String displayName;
	private String description;
	private String externalId;
	private String cssClass;
	private boolean visible = true;
	private boolean allowedAsSubject = true;
	private boolean allowedAsCompetence = true;
	private List<String> subTypes = new ArrayList<>();
	private String managedFlags;
	private boolean libraryEnabled;
	private boolean libraryManage;
	private boolean libraryTeachRead;
	private int libraryTeachReadParentLevels;
	private boolean libraryTeachWrite;
	private boolean libraryHaveRead;
	private boolean libraryTargetRead;

	/** true if the type is only referenced by a level and not defined in the types sheet */
	private boolean implicit;

	private TaxonomyLevelType existingType;

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

	public String getCssClass() {
		return cssClass;
	}

	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public boolean isAllowedAsSubject() {
		return allowedAsSubject;
	}

	public void setAllowedAsSubject(boolean allowedAsSubject) {
		this.allowedAsSubject = allowedAsSubject;
	}

	public boolean isAllowedAsCompetence() {
		return allowedAsCompetence;
	}

	public void setAllowedAsCompetence(boolean allowedAsCompetence) {
		this.allowedAsCompetence = allowedAsCompetence;
	}

	public List<String> getSubTypes() {
		return subTypes;
	}

	public void setSubTypes(List<String> subTypes) {
		this.subTypes = subTypes == null ? new ArrayList<>() : subTypes;
	}

	public String getManagedFlags() {
		return managedFlags;
	}

	public void setManagedFlags(String managedFlags) {
		this.managedFlags = managedFlags;
	}

	public boolean isLibraryEnabled() {
		return libraryEnabled;
	}

	public void setLibraryEnabled(boolean libraryEnabled) {
		this.libraryEnabled = libraryEnabled;
	}

	public boolean isLibraryManage() {
		return libraryManage;
	}

	public void setLibraryManage(boolean libraryManage) {
		this.libraryManage = libraryManage;
	}

	public boolean isLibraryTeachRead() {
		return libraryTeachRead;
	}

	public void setLibraryTeachRead(boolean libraryTeachRead) {
		this.libraryTeachRead = libraryTeachRead;
	}

	public int getLibraryTeachReadParentLevels() {
		return libraryTeachReadParentLevels;
	}

	public void setLibraryTeachReadParentLevels(int libraryTeachReadParentLevels) {
		this.libraryTeachReadParentLevels = libraryTeachReadParentLevels;
	}

	public boolean isLibraryTeachWrite() {
		return libraryTeachWrite;
	}

	public void setLibraryTeachWrite(boolean libraryTeachWrite) {
		this.libraryTeachWrite = libraryTeachWrite;
	}

	public boolean isLibraryHaveRead() {
		return libraryHaveRead;
	}

	public void setLibraryHaveRead(boolean libraryHaveRead) {
		this.libraryHaveRead = libraryHaveRead;
	}

	public boolean isLibraryTargetRead() {
		return libraryTargetRead;
	}

	public void setLibraryTargetRead(boolean libraryTargetRead) {
		this.libraryTargetRead = libraryTargetRead;
	}

	public boolean isImplicit() {
		return implicit;
	}

	public void setImplicit(boolean implicit) {
		this.implicit = implicit;
	}

	public TaxonomyLevelType getExistingType() {
		return existingType;
	}

	public void setExistingType(TaxonomyLevelType existingType) {
		this.existingType = existingType;
	}
}

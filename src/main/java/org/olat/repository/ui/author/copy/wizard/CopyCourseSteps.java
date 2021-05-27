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
package org.olat.repository.ui.author.copy.wizard;

/**
 * Initial date: 22.02.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class CopyCourseSteps {
	
	// Date steps
	private boolean moveDates;
	
	// General steps
	private boolean editTitleOrReference;
	private boolean editExecution;
	private boolean editMetadata;
	private boolean editGroups;
	private boolean editCoaches;
	private boolean editOwners;
	private boolean editCatalog;
	private boolean editDisclaimer;
	private boolean editResourceFolder;
	
	// Node steps
	private boolean editBlogSettings;
	private boolean editFolderSettings;
	private boolean editWikiSettings;
	private boolean editDates;
	
	// Reminder steps
	private boolean editReminders;	
	
	// Assessment mode steps
	private boolean editAssessmentModes;
	
	// Lecutre block steps
	private boolean editLecutreBlocks;
	
	public boolean isMoveDates() {
		return moveDates;
	}
	public void setMoveDates(boolean moveDates) {
		this.moveDates = moveDates;
	}
	public boolean isEditTitleOrReference() {
		return editTitleOrReference;
	}
	public void setEditTitleOrReference(boolean editTitleOrReference) {
		this.editTitleOrReference = editTitleOrReference;
	}
	public boolean isEditExecution() {
		return editExecution;
	}
	public void setEditExecution(boolean editExecution) {
		this.editExecution = editExecution;
	}
	public boolean isEditMetadata() {
		return editMetadata;
	}
	public void setEditMetadata(boolean editMetadata) {
		this.editMetadata = editMetadata;
	}
	public boolean isEditGroups() {
		return editGroups;
	}
	public void setEditGroups(boolean editGroups) {
		this.editGroups = editGroups;
	}
	public boolean isEditCoaches() {
		return editCoaches;
	}
	public void setEditCoaches(boolean editCoaches) {
		this.editCoaches = editCoaches;
	}
	public boolean isEditOwners() {
		return editOwners;
	}
	public void setEditOwners(boolean editOwners) {
		this.editOwners = editOwners;
	}
	public boolean isEditCatalog() {
		return editCatalog;
	}
	public void setEditCatalog(boolean editCatalog) {
		this.editCatalog = editCatalog;
	}
	public boolean isEditDisclaimer() {
		return editDisclaimer;
	}
	public void setEditDisclaimer(boolean editDisclaimer) {
		this.editDisclaimer = editDisclaimer;
	}
	public boolean isEditResourceFolder() {
		return editResourceFolder;
	}
	public void setEditResourceFolder(boolean editResourceFolder) {
		this.editResourceFolder = editResourceFolder;
	}
	
	public boolean isEditBlogSettings() {
		return editBlogSettings;
	}
	
	public void setEditBlogSettings(boolean editBlogSteps) {
		this.editBlogSettings = editBlogSteps;
	}
	
	public boolean isEditFolderSettings() {
		return editFolderSettings;
	}
	
	public void setEditFolderSettings(boolean editFolderSteps) {
		this.editFolderSettings = editFolderSteps;
	}
	
	public boolean isEditWikiSettings() {
		return editWikiSettings;
	}
	
	public void setEditWikiSettings(boolean editWikiSteps) {
		this.editWikiSettings = editWikiSteps;
	}
	
	public boolean isEditDates() {
		return editDates;
	}
	
	public void setEditDates(boolean editDates) {
		this.editDates = editDates;
	}
	
	public boolean isEditReminders() {
		return editReminders;
	}
	
	public void setEditReminders(boolean editReminders) {
		this.editReminders = editReminders;
	}
	
	public boolean isEditAssessmentModes() {
		return editAssessmentModes;
	}
	
	public void setEditAssessmentModes(boolean editAssessmentModes) {
		this.editAssessmentModes = editAssessmentModes;
	}
	
	public boolean isEditLecutreBlocks() {
		return editLecutreBlocks;
	}
	
	public void setEditLecutreBlocks(boolean editLecutreBlocks) {
		this.editLecutreBlocks = editLecutreBlocks;
	}	
	
	public boolean showNodesOverview() {
		return editBlogSettings || editWikiSettings || editFolderSettings;
	}
}

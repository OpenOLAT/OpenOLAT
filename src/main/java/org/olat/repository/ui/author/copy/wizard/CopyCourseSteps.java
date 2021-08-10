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

import org.olat.repository.ui.author.copy.wizard.CopyCourseContext.CopyType;

/**
 * Initial date: 22.02.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class CopyCourseSteps {
	
	public static final String CONTEXT_KEY = CopyCourseSteps.class.getSimpleName();
	
	// Advanced mode 
	private boolean advancedMode;
	
	// General steps
	private boolean editGroups;
	private boolean editCoaches;
	private boolean editOwners;
	private boolean editCatalog;
	private boolean editDisclaimer;
	
	// Node steps
	private boolean showNodesOverview;
	
	// Reminder steps
	private boolean editReminders;	
	
	// Assessment mode steps
	private boolean editAssessmentModes;
	
	// Lecutre block steps
	private boolean editLectureBlocks;
	
	// Load steps from config 
	public void loadFromWizardConfig(CopyCourseWizardModule wizardModule) {
		setAdvancedMode(isCustomConfig(wizardModule.getWizardMode()));
		
		setEditGroups(isCustomConfig(wizardModule.getGroupsCopyType()));
		setEditCoaches(isCustomConfig(wizardModule.getCoachesCopyType()));
		setEditOwners(isCustomConfig(wizardModule.getOwnersCopyType()));
		setEditCatalog(isCustomConfig(wizardModule.getCatalogCopyType()));
		setEditDisclaimer(isCustomConfig(wizardModule.getDisclaimerCopyType()));
		
		setEditReminders(isCustomConfig(wizardModule.getReminderCopyType()));
		setEditAssessmentModes(isCustomConfig(wizardModule.getAssessmentCopyType()));
		setEditLectureBlocks(isCustomConfig(wizardModule.getLectureBlockCopyType()));
	}
	
	private boolean isCustomConfig(CopyType copyType) {
		return copyType.equals(CopyType.custom);
	}
	
	public boolean isAdvancedMode() {
		return advancedMode;
	}
	
	public void setAdvancedMode(boolean advancedMode) {
		this.advancedMode = advancedMode;
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
	
	public void setShowNodesOverview(boolean showNodesOverview) {
		this.showNodesOverview = showNodesOverview;
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
	
	public boolean isEditLectureBlocks() {
		return editLectureBlocks;
	}
	
	public void setEditLectureBlocks(boolean editLecutreBlocks) {
		this.editLectureBlocks = editLecutreBlocks;
	}	
	
	public boolean showNodesOverview() {
		return showNodesOverview;
	}
}

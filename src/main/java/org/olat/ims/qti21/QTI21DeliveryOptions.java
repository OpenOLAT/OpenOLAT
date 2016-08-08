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
package org.olat.ims.qti21;

import org.olat.core.util.StringHelper;
import org.olat.ims.qti.process.AssessmentInstance;

/**
 * 
 * External options
 * 
 * Initial date: 05.01.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21DeliveryOptions {

	private boolean enableCancel;
	private boolean enableSuspend;
	private boolean displayScoreProgress;
	private boolean displayQuestionProgress;
	
	private boolean showMenu;
	private boolean showTitles;
	private boolean personalNotes;
	
	private boolean blockAfterSuccess;
	private int maxAttempts;
	
	private Integer templateProcessingLimit;
	
	private ShowResultsOnFinish showResultsOnFinish;

	public boolean isEnableCancel() {
		return enableCancel;
	}

	public void setEnableCancel(boolean enableCancel) {
		this.enableCancel = enableCancel;
	}

	public boolean isEnableSuspend() {
		return enableSuspend;
	}

	public void setEnableSuspend(boolean enableSuspend) {
		this.enableSuspend = enableSuspend;
	}

	public boolean isDisplayScoreProgress() {
		return displayScoreProgress;
	}

	public void setDisplayScoreProgress(boolean displayScoreProgress) {
		this.displayScoreProgress = displayScoreProgress;
	}

	public boolean isDisplayQuestionProgress() {
		return displayQuestionProgress;
	}

	public void setDisplayQuestionProgress(boolean displayQuestionProgress) {
		this.displayQuestionProgress = displayQuestionProgress;
	}
	
	public boolean isShowTitles() {
		return showTitles;
	}

	public void setShowTitles(boolean showTitles) {
		this.showTitles = showTitles;
	}

	public boolean isShowMenu() {
		return showMenu;
	}

	public void setShowMenu(boolean showMenu) {
		this.showMenu = showMenu;
	}

	public boolean isPersonalNotes() {
		return personalNotes;
	}

	public void setPersonalNotes(boolean personalNotes) {
		this.personalNotes = personalNotes;
	}
	
	public boolean isBlockAfterSuccess() {
		return blockAfterSuccess;
	}

	public void setBlockAfterSuccess(boolean blockAfterSuccess) {
		this.blockAfterSuccess = blockAfterSuccess;
	}

	public int getMaxAttempts() {
		return maxAttempts;
	}

	public void setMaxAttempts(int maxAttempts) {
		this.maxAttempts = maxAttempts;
	}

	public ShowResultsOnFinish getShowResultsOnFinish() {
		return showResultsOnFinish;
	}

	public void setShowResultsOnFinish(ShowResultsOnFinish showResultsOnFinish) {
		this.showResultsOnFinish = showResultsOnFinish;
	}
	
	public Integer getTemplateProcessingLimit() {
		return templateProcessingLimit;
	}
	
	public void setTemplateProcessingLimit(Integer templateProcessingLimit) {
		this.templateProcessingLimit = templateProcessingLimit;
	}

	public static final QTI21DeliveryOptions defaultSettings() {
		QTI21DeliveryOptions defaultSettings = new QTI21DeliveryOptions();
		defaultSettings.enableCancel = false;
		defaultSettings.enableSuspend = false;
		defaultSettings.displayScoreProgress = false;
		defaultSettings.displayQuestionProgress = false;
		defaultSettings.showMenu = true;
		defaultSettings.showTitles = true;
		defaultSettings.personalNotes = false;
		defaultSettings.blockAfterSuccess = false;
		defaultSettings.maxAttempts = 0;
		defaultSettings.showResultsOnFinish = ShowResultsOnFinish.none;
		return defaultSettings;
	}

	@Override
	public QTI21DeliveryOptions clone() {
		QTI21DeliveryOptions clone = new QTI21DeliveryOptions();
		clone.enableCancel = enableCancel;
		clone.enableSuspend = enableSuspend;
		clone.displayScoreProgress = displayScoreProgress;
		clone.displayQuestionProgress = displayQuestionProgress;
		clone.showMenu = showMenu;
		clone.showTitles = showTitles;
		clone.personalNotes = personalNotes;
		clone.blockAfterSuccess = blockAfterSuccess;
		clone.maxAttempts = maxAttempts;
		clone.showResultsOnFinish = showResultsOnFinish;
		return clone;
	}
	
	public enum ShowResultsOnFinish {
		none,
		compact,//without solution
		sections,//summary without solution
		details;//with solution
		
		public String getIQEquivalent() {
			switch(this) {
				case none: return AssessmentInstance.QMD_ENTRY_SUMMARY_NONE;
				case compact: return AssessmentInstance.QMD_ENTRY_SUMMARY_COMPACT;
				case sections: return AssessmentInstance.QMD_ENTRY_SUMMARY_SECTION;
				case details: return AssessmentInstance.QMD_ENTRY_SUMMARY_DETAILED;
				default: return null;
			}
		}
		
		public static final ShowResultsOnFinish fromIQEquivalent(String value, ShowResultsOnFinish defaultValue) {
			if(StringHelper.containsNonWhitespace(value)) {
				switch(value) {
					case AssessmentInstance.QMD_ENTRY_SUMMARY_NONE: return none;
					case AssessmentInstance.QMD_ENTRY_SUMMARY_COMPACT: return compact;
					case AssessmentInstance.QMD_ENTRY_SUMMARY_SECTION: return sections;
					case AssessmentInstance.QMD_ENTRY_SUMMARY_DETAILED: return details;
					default: return defaultValue;
				}
			}
			return defaultValue;
		}
	}
}
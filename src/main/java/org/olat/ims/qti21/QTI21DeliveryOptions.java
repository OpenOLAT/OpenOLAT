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
	private boolean displayMaxScoreItem;
	
	private boolean showMenu;
	private boolean showTitles;
	private boolean personalNotes;
	private boolean hideLms;
	
	private boolean hideFeedbacks;
	
	private boolean blockAfterSuccess;
	private int maxAttempts;
	
	private boolean allowAnonym;
	
	private boolean digitalSignature;
	private boolean digitalSignatureMail;
	
	private Integer templateProcessingLimit;
	
	private Boolean showAssessmentResultsOnFinish;
	
	private boolean enableAssessmentItemBack;
	private boolean enableAssessmentItemResetHard;
	private boolean enableAssessmentItemResetSoft;
	private boolean enableAssessmentItemSkip;
	
	private TestType testType;
	private ShowResultsOnFinish showResultsOnFinish;
	private PassedType passedType;
	private QTI21AssessmentResultsOptions assessmentResultsOptions;

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
	
	public boolean isDisplayMaxScoreItem() {
		return displayMaxScoreItem;
	}

	public void setDisplayMaxScoreItem(boolean displayMaxScoreItem) {
		this.displayMaxScoreItem = displayMaxScoreItem;
	}

	public boolean isHideFeedbacks() {
		return hideFeedbacks;
	}

	public void setHideFeedbacks(boolean hideFeedbacks) {
		this.hideFeedbacks = hideFeedbacks;
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

	public boolean isHideLms() {
		return hideLms;
	}

	public void setHideLms(boolean hideLms) {
		this.hideLms = hideLms;
	}

	public boolean isPersonalNotes() {
		return personalNotes;
	}

	public void setPersonalNotes(boolean personalNotes) {
		this.personalNotes = personalNotes;
	}
	
	public boolean isAllowAnonym() {
		return allowAnonym;
	}

	public void setAllowAnonym(boolean allowAnonym) {
		this.allowAnonym = allowAnonym;
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

	public boolean isDigitalSignature() {
		return digitalSignature;
	}

	public void setDigitalSignature(boolean digitalSignature) {
		this.digitalSignature = digitalSignature;
	}

	public boolean isDigitalSignatureMail() {
		return digitalSignatureMail;
	}

	public void setDigitalSignatureMail(boolean digitalSignatureMail) {
		this.digitalSignatureMail = digitalSignatureMail;
	}

	/**
	 * The field is deprecated and only use for backwards compatibility
	 * with QTI 1.2
	 * @return
	 */
	public ShowResultsOnFinish getShowResultsOnFinish() {
		return showResultsOnFinish;
	}

	public void setShowResultsOnFinish(ShowResultsOnFinish showResultsOnFinish) {
		this.showResultsOnFinish = showResultsOnFinish;
	}
	
	/**
	 * Legacy options do not have a PassedType. Use this method to get the PassedTpe
	 * even for null values.
	 * 
	 * @param cutValue
	 * @return
	 */
	public PassedType getPassedType(Double cutValue) {
		return passedType != null
				? passedType
				: cutValue != null? PassedType.cutValue: PassedType.manually;
	}
	
	public void setPassedType(PassedType passedType) {
		this.passedType = passedType;
	}

	public QTI21AssessmentResultsOptions getAssessmentResultsOptions() {
		if(assessmentResultsOptions == null) {
			assessmentResultsOptions = QTI21AssessmentResultsOptions.convert(showResultsOnFinish);
		}
		return assessmentResultsOptions;
	}

	public void setAssessmentResultsOptions(QTI21AssessmentResultsOptions assessmentResultsOptions) {
		this.assessmentResultsOptions = assessmentResultsOptions;
	}
	
	public boolean isShowAssessmentResultsOnFinish() {
		return showAssessmentResultsOnFinish == null ? !getAssessmentResultsOptions().none() : showAssessmentResultsOnFinish.booleanValue();
	}
	
	public void setShowAssessmentResultsOnFinish(boolean onFinish) {
		showAssessmentResultsOnFinish = onFinish;
	}

	public Integer getTemplateProcessingLimit() {
		return templateProcessingLimit;
	}
	
	public void setTemplateProcessingLimit(Integer templateProcessingLimit) {
		this.templateProcessingLimit = templateProcessingLimit;
	}

	public TestType getTestType() {
		return testType;
	}

	public void setTestType(TestType testType) {
		this.testType = testType;
	}
	
	/**
	 * This feature is only available for assessment item display, not test
	 * 
	 * @return if back button will shown after an answer
	 */
	public boolean isEnableAssessmentItemBack() {
		return enableAssessmentItemBack;
	}

	/**
	 * This feature is only available for assessment item display, not test
	 * 
	 * @param enableAssessmentItemBack
	 */
	public void setEnableAssessmentItemBack(boolean enableAssessmentItemBack) {
		this.enableAssessmentItemBack = enableAssessmentItemBack;
	}

	/**
	 * This feature is only available for assessment item display, not test
	 * 
	 * @return if a retry button will shown after an answer
	 */
	public boolean isEnableAssessmentItemResetSoft() {
		return enableAssessmentItemResetSoft;
	}

	public void setEnableAssessmentItemResetSoft(boolean enable) {
		enableAssessmentItemResetSoft = enable;
	}
	
	/**
	 * This feature is only available for assessment item display, not test
	 * 
	 * @return if a retry button will shown after an answer (hard
	 * 		mean random and shuffled answers will be recalculated)
	 */
	public boolean isEnableAssessmentItemResetHard() {
		return enableAssessmentItemResetHard;
	}

	public void setEnableAssessmentItemResetHard(boolean enableAssessmentItemResetHard) {
		this.enableAssessmentItemResetHard = enableAssessmentItemResetHard;
	}

	/**
	 * This feature is only available for assessment item display, not test
	 * 
	 * @return if skip button will shown after an answer
	 */
	public boolean isEnableAssessmentItemSkip() {
		return enableAssessmentItemSkip;
	}

	public void setEnableAssessmentItemSkip(boolean enable) {
		this.enableAssessmentItemSkip = enable;
	}

	public static final QTI21DeliveryOptions defaultSettings() {
		QTI21DeliveryOptions defaultSettings = new QTI21DeliveryOptions();
		defaultSettings.enableCancel = false;
		defaultSettings.enableSuspend = false;
		defaultSettings.displayScoreProgress = false;
		defaultSettings.displayQuestionProgress = false;
		defaultSettings.displayMaxScoreItem = true;
		defaultSettings.hideFeedbacks = false;
		defaultSettings.hideLms = true;
		defaultSettings.showMenu = true;
		defaultSettings.showTitles = true;
		defaultSettings.personalNotes = false;
		defaultSettings.allowAnonym = false;
		defaultSettings.blockAfterSuccess = false;
		defaultSettings.maxAttempts = 0;
		defaultSettings.digitalSignature = false;
		defaultSettings.digitalSignatureMail = false;
		defaultSettings.enableAssessmentItemBack = false;
		defaultSettings.enableAssessmentItemResetSoft = false;
		defaultSettings.enableAssessmentItemResetHard = false;
		defaultSettings.enableAssessmentItemSkip = false;
		defaultSettings.assessmentResultsOptions = QTI21AssessmentResultsOptions.noOptions();
		defaultSettings.showAssessmentResultsOnFinish = Boolean.FALSE;
		return defaultSettings;
	}
	
	public static final QTI21DeliveryOptions formativeSettings() {
		QTI21DeliveryOptions defaultSettings = new QTI21DeliveryOptions();
		defaultSettings.enableCancel = true;
		defaultSettings.enableSuspend = true;
		defaultSettings.displayScoreProgress = true;
		defaultSettings.displayQuestionProgress = true;
		defaultSettings.displayMaxScoreItem = true;
		defaultSettings.hideFeedbacks = false;
		defaultSettings.hideLms = true;
		defaultSettings.showMenu = true;
		defaultSettings.showTitles = true;
		defaultSettings.personalNotes = false;
		defaultSettings.allowAnonym = false;
		defaultSettings.blockAfterSuccess = false;
		defaultSettings.maxAttempts = 0;
		defaultSettings.digitalSignature = false;
		defaultSettings.digitalSignatureMail = false;
		defaultSettings.testType = TestType.formative;
		defaultSettings.passedType = null; // not part of the profile
		defaultSettings.enableAssessmentItemBack = false;
		defaultSettings.enableAssessmentItemResetSoft = false;
		defaultSettings.enableAssessmentItemResetHard = false;
		defaultSettings.enableAssessmentItemSkip = false;
		defaultSettings.assessmentResultsOptions = QTI21AssessmentResultsOptions.allOptions();
		defaultSettings.showAssessmentResultsOnFinish = Boolean.TRUE;
		return defaultSettings;
	}
	
	public static final QTI21DeliveryOptions summativeSettings() {
		QTI21DeliveryOptions defaultSettings = new QTI21DeliveryOptions();
		defaultSettings.enableCancel = false;
		defaultSettings.enableSuspend = false;
		defaultSettings.displayScoreProgress = false;
		defaultSettings.displayQuestionProgress = true;
		defaultSettings.displayMaxScoreItem = true;
		defaultSettings.hideFeedbacks = true;
		defaultSettings.hideLms = true;
		defaultSettings.showMenu = true;
		defaultSettings.showTitles = true;
		defaultSettings.personalNotes = false;
		defaultSettings.allowAnonym = false;
		defaultSettings.blockAfterSuccess = false;
		defaultSettings.maxAttempts = 1;
		defaultSettings.digitalSignature = false;
		defaultSettings.digitalSignatureMail = false;
		defaultSettings.testType = TestType.summative;
		defaultSettings.passedType = null; // not part of the profile
		defaultSettings.enableAssessmentItemBack = false;
		defaultSettings.enableAssessmentItemResetSoft = false;
		defaultSettings.enableAssessmentItemResetHard = false;
		defaultSettings.enableAssessmentItemSkip = false;
		defaultSettings.assessmentResultsOptions = QTI21AssessmentResultsOptions.noOptions();
		defaultSettings.showAssessmentResultsOnFinish = Boolean.FALSE;
		return defaultSettings;
	}

	@Override
	public QTI21DeliveryOptions clone() {
		QTI21DeliveryOptions clone = new QTI21DeliveryOptions();
		clone.enableCancel = enableCancel;
		clone.enableSuspend = enableSuspend;
		clone.displayScoreProgress = displayScoreProgress;
		clone.displayQuestionProgress = displayQuestionProgress;
		clone.displayMaxScoreItem = displayMaxScoreItem;
		clone.hideFeedbacks = hideFeedbacks;
		clone.hideLms = hideLms;
		clone.showMenu = showMenu;
		clone.showTitles = showTitles;
		clone.personalNotes = personalNotes;
		clone.allowAnonym = allowAnonym;
		clone.blockAfterSuccess = blockAfterSuccess;
		clone.maxAttempts = maxAttempts;
		clone.digitalSignature = digitalSignature;
		clone.digitalSignatureMail = digitalSignatureMail;
		clone.assessmentResultsOptions = getAssessmentResultsOptions().clone();
		clone.showAssessmentResultsOnFinish = showAssessmentResultsOnFinish;
		clone.testType = testType;
		clone.passedType = passedType;
		clone.enableAssessmentItemBack = enableAssessmentItemBack;
		clone.enableAssessmentItemResetSoft = enableAssessmentItemResetSoft;
		clone.enableAssessmentItemResetHard = enableAssessmentItemResetHard;
		clone.enableAssessmentItemSkip = enableAssessmentItemSkip;
		return clone;
	}
	
	public enum TestType {
		summative,
		formative
	}
	
	public enum ShowResultsOnFinish {
		none,
		compact,//without solution
		sections,//summary without solution
		details;//with solution
		
		public String getIQEquivalent() {
			switch(this) {
				case none: return QTI21Constants.QMD_ENTRY_SUMMARY_NONE;
				case compact: return QTI21Constants.QMD_ENTRY_SUMMARY_COMPACT;
				case sections: return QTI21Constants.QMD_ENTRY_SUMMARY_SECTION;
				case details: return QTI21Constants.QMD_ENTRY_SUMMARY_DETAILED;
				default: return null;
			}
		}
		
		public static final ShowResultsOnFinish fromIQEquivalent(String value, ShowResultsOnFinish defaultValue) {
			if(StringHelper.containsNonWhitespace(value)) {
				switch(value) {
					case QTI21Constants.QMD_ENTRY_SUMMARY_NONE: return none;
					case QTI21Constants.QMD_ENTRY_SUMMARY_COMPACT: return compact;
					case QTI21Constants.QMD_ENTRY_SUMMARY_SECTION: return sections;
					case QTI21Constants.QMD_ENTRY_SUMMARY_DETAILED: return details;
					default: return defaultValue;
				}
			}
			return defaultValue;
		}
	}
	
	public enum PassedType {	
		none,
		cutValue,
		manually;
	
	}
}
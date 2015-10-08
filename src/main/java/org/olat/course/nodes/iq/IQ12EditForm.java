/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.course.nodes.iq;

import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.ims.qti.process.AssessmentInstance;
import org.olat.modules.ModuleConfiguration;

/**
 * Test configuration form. 
 * Used for configuring Test, Self-test, and Questionnaire(aka Survey).
 * <p>  
 * Initial Date:  Mar 3, 2004
 *
 * @author Mike Stock
 */
public class IQ12EditForm extends FormBasicController {
	
	private static final String[] correctionModeKeys = new String[] { "auto", "manual" };
	
	private SelectionElement enableMenu;
	private SelectionElement displayMenu;
	private SelectionElement displayScoreProgress;
	private SelectionElement displayQuestionProgress;
	private SelectionElement displayQuestionTitle;
	private SelectionElement autoEnumerateChoices;
	private SelectionElement provideMemoField;
	private SingleSelection sequence;
	private SingleSelection correctionModeEl;
	private SelectionElement enableCancel;
	private SelectionElement enableSuspend;
	private SingleSelection summary;
	private SelectionElement limitAttempts;
	private SelectionElement blockAfterSuccess;
	private IntegerElement attempts;
	private SingleSelection menuRenderOptions;
	private SelectionElement scoreInfo;
	private SelectionElement showResultsDateDependentButton;
	private DateChooser startDateElement;
	private DateChooser endDateElement;
	private SelectionElement showResultsAfterFinishTest;
	private SelectionElement showResultsOnHomePage;
	private SelectionElement fullWindowEl;
	
	private ModuleConfiguration modConfig;
	
	private String[] menuRenderOptKeys, menuRenderOptValues;
	private String[] sequenceKeys, sequenceValues;
	private String[] correctionModeValues;
	private String configKeyType;
	
	private boolean isAssessment, isSelfTest, isSurvey;
	private final boolean hasEssay;
	
	/**
	 * Constructor for the qti configuration form
	 * @param ureq
	 * @param wControl
	 * @param modConfig
	 */
	IQ12EditForm(UserRequest ureq, WindowControl wControl, ModuleConfiguration modConfig, boolean hasEssay) {
		super (ureq, wControl);
		this.modConfig = modConfig;
		this.hasEssay = hasEssay;
		
		configKeyType = (String)modConfig.get(IQEditController.CONFIG_KEY_TYPE);
		
		isAssessment = configKeyType.equals(AssessmentInstance.QMD_ENTRY_TYPE_ASSESS);
		isSelfTest   = configKeyType.equals(AssessmentInstance.QMD_ENTRY_TYPE_SELF);
		isSurvey     = configKeyType.equals(AssessmentInstance.QMD_ENTRY_TYPE_SURVEY);
		
		menuRenderOptKeys = new String[] {
				Boolean.FALSE.toString(),
				Boolean.TRUE.toString()
		};
		menuRenderOptValues = new String[] {
				translate("qti.form.menurender.allquestions"),
				translate("qti.form.menurender.sectionsonly")
		};
		sequenceKeys = new String[] {
				AssessmentInstance.QMD_ENTRY_SEQUENCE_ITEM,
				AssessmentInstance.QMD_ENTRY_SEQUENCE_SECTION
		};
		sequenceValues = new String[] {
				translate("qti.form.sequence.item"),
				translate("qti.form.sequence.section")
		};
		correctionModeValues = new String[]{
				translate("correction.auto"),
				translate("correction.manual")
		};
		
		initForm(ureq);
	}
	
	protected boolean validateFormLogic (UserRequest ureq) {
		startDateElement.clearError();
		endDateElement.clearError();
		
		if (startDateElement.isVisible()) {
			if (startDateElement.isEmpty()) {
				startDateElement.setErrorKey("qti.form.date.start.error.mandatory", null);
				return false;
			} else {
				if (startDateElement.getDate() == null) {
					startDateElement.setErrorKey("qti.form.date.error.format", null);
					return false;
				}
			}

			if (!endDateElement.isEmpty()) {
				if (endDateElement.getDate() == null) {
					endDateElement.setErrorKey("qti.form.date.error.format", null);
					return false;
				}

				if (endDateElement.getDate().before(startDateElement.getDate())) {
					endDateElement.setErrorKey("qti.form.date.error.endbeforebegin", null);
					return false;
				}
			}
		}
		
		return true;
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		modConfig.set(IQEditController.CONFIG_KEY_DISPLAYMENU, new Boolean(isDisplayMenu()));
		modConfig.set(IQEditController.CONFIG_FULLWINDOW, new Boolean(isFullWindow()));
		if(correctionModeEl.isOneSelected()) {
			modConfig.setStringValue(IQEditController.CONFIG_CORRECTION_MODE, correctionModeEl.getSelectedKey());
		}
		
		if (isDisplayMenu()) {
			modConfig.set(IQEditController.CONFIG_KEY_RENDERMENUOPTION, isMenuRenderSectionsOnly());
			modConfig.set(IQEditController.CONFIG_KEY_ENABLEMENU, new Boolean(isEnableMenu()));
		} else {
			// set default values when menu is not displayed
			modConfig.set(IQEditController.CONFIG_KEY_RENDERMENUOPTION, Boolean.FALSE);
			modConfig.set(IQEditController.CONFIG_KEY_ENABLEMENU, Boolean.FALSE); 
		}
		
		modConfig.set(IQEditController.CONFIG_KEY_QUESTIONPROGRESS, new Boolean(isDisplayQuestionProgress()));
		modConfig.set(IQEditController.CONFIG_KEY_SEQUENCE, getSequence());
		modConfig.set(IQEditController.CONFIG_KEY_ENABLECANCEL, new Boolean(isEnableCancel()));
		modConfig.set(IQEditController.CONFIG_KEY_ENABLESUSPEND, new Boolean(isEnableSuspend()));
		modConfig.set(IQEditController.CONFIG_KEY_QUESTIONTITLE, new Boolean(isDisplayQuestionTitle()));
		modConfig.set(IQEditController.CONFIG_KEY_AUTOENUM_CHOICES, new Boolean(isAutoEnumChoices()));
		modConfig.set(IQEditController.CONFIG_KEY_MEMO, new Boolean(isProvideMemoField()));
		// Only tests and selftests have summaries and score progress
		if (!isSurvey) {
			modConfig.set(IQEditController.CONFIG_KEY_SUMMARY, getSummary());
			modConfig.set(IQEditController.CONFIG_KEY_SCOREPROGRESS, new Boolean(isDisplayScoreProgress()));
			modConfig.set(IQEditController.CONFIG_KEY_ENABLESCOREINFO, new Boolean(isEnableScoreInfo()));
			modConfig.set(IQEditController.CONFIG_KEY_DATE_DEPENDENT_RESULTS, new Boolean(isShowResultsDateDependent()));
			modConfig.set(IQEditController.CONFIG_KEY_RESULTS_START_DATE, getShowResultsStartDate());
			modConfig.set(IQEditController.CONFIG_KEY_RESULTS_END_DATE, getShowResultsEndDate());
			modConfig.set(IQEditController.CONFIG_KEY_RESULT_ON_FINISH, isShowResultsAfterFinishTest());
			modConfig.set(IQEditController.CONFIG_KEY_RESULT_ON_HOME_PAGE, isShowResultsOnHomePage());
		}
		// Only tests have a limitation on number of attempts
		if (isAssessment) {
			modConfig.set(IQEditController.CONFIG_KEY_ATTEMPTS, getAttempts());
			modConfig.set(IQEditController.CONFIG_KEY_BLOCK_AFTER_SUCCESS, new Boolean(isBlockAfterSuccess()));
		}
		
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		limitAttempts = uifactory.addCheckboxesHorizontal("limitAttempts", "qti.form.limit.attempts", formLayout, new String[]{"xx"}, new String[]{null});
		
		Integer confAttempts = (Integer) modConfig.get(IQEditController.CONFIG_KEY_ATTEMPTS);
		if (confAttempts == null) confAttempts = new Integer(0);
		attempts = uifactory.addIntegerElement("qti.form.attempts", confAttempts, formLayout);	
		attempts.setDisplaySize(2);
		attempts.setMinValueCheck(1, null);
		attempts.setMaxValueCheck(20, null);
		
		//add it
		blockAfterSuccess = uifactory.addCheckboxesHorizontal("blockAfterSuccess", "qti.form.block.afterSuccess", formLayout, new String[]{"xx"}, new String[]{null});
		Boolean block = (Boolean) modConfig.get(IQEditController.CONFIG_KEY_BLOCK_AFTER_SUCCESS);
		blockAfterSuccess.select("xx", block == null ? false : block.booleanValue() );

		// Only assessments have a limitation on number of attempts
		if (isAssessment) {
			uifactory.addSpacerElement("s1", formLayout, true);
			
			limitAttempts.select("xx", confAttempts>0);
			limitAttempts.addActionListener(FormEvent.ONCLICK);
		} else {
			limitAttempts.select("xx", false);
			limitAttempts.setVisible(false);
			attempts.setVisible(false);
			blockAfterSuccess.select("xx", false);
			blockAfterSuccess.setVisible(false);
		}
		
		correctionModeEl = uifactory.addRadiosVertical("correction.mode", "correction.mode", formLayout, correctionModeKeys, correctionModeValues);
		String mode = modConfig.getStringValue(IQEditController.CONFIG_CORRECTION_MODE);
		boolean selected = false;
		for(String correctionModeKey:correctionModeKeys) {
			if(correctionModeKey.equals(mode)) {
				correctionModeEl.select(correctionModeKey, true);
				selected = true;
			}
		}
		if(!selected) {
			if(hasEssay) {
				correctionModeEl.select(correctionModeKeys[1], true);
			} else {
				correctionModeEl.select(correctionModeKeys[0], true);
			}
		}
		
		Boolean fullWindow = (Boolean) modConfig.get(IQEditController.CONFIG_FULLWINDOW);
		fullWindowEl = uifactory.addCheckboxesHorizontal("fullwindow", "qti.form.fullwindow", formLayout, new String[]{"fullwindow"}, new String[]{null});
		fullWindowEl.select("fullwindow", fullWindow == null ? true : fullWindow.booleanValue());
		
		Boolean CdisplayMenu = (Boolean)modConfig.get(IQEditController.CONFIG_KEY_DISPLAYMENU);
		displayMenu = uifactory.addCheckboxesHorizontal("qti_displayMenu", "qti.form.menudisplay", formLayout, new String[]{"xx"}, new String[]{null});
		displayMenu.select("xx", CdisplayMenu == null ? true : CdisplayMenu );
		displayMenu.addActionListener(FormEvent.ONCLICK);
		
		Boolean CenableMenu = (Boolean)modConfig.get(IQEditController.CONFIG_KEY_ENABLEMENU);
		enableMenu = uifactory.addCheckboxesHorizontal("qti_enableMenu", "qti.form.menuenable", formLayout, new String[]{"xx"}, new String[]{null});
		enableMenu.select("xx", CenableMenu == null ? true : CenableMenu);
		
		menuRenderOptions = uifactory.addRadiosVertical("qti_form_menurenderoption", "qti.form.menurender", formLayout, menuRenderOptKeys, menuRenderOptValues);
		menuRenderOptions.setVisible(displayMenu.isSelected(0));
		Boolean renderSectionsOnly;
		if (modConfig.get(IQEditController.CONFIG_KEY_RENDERMENUOPTION) == null) {
			// migration
			modConfig.set(IQEditController.CONFIG_KEY_RENDERMENUOPTION, Boolean.FALSE);
			renderSectionsOnly = Boolean.FALSE;
		} else {
			renderSectionsOnly = (Boolean)modConfig.get(IQEditController.CONFIG_KEY_RENDERMENUOPTION);
		}
		menuRenderOptions.select(renderSectionsOnly.toString(), true);
		menuRenderOptions.addActionListener(FormEvent.ONCLICK);
		
		// sequence type
		sequence = uifactory.addRadiosVertical("qti_form_sequence", "qti.form.sequence", formLayout, sequenceKeys, sequenceValues);
		String confSequence = (String)modConfig.get(IQEditController.CONFIG_KEY_SEQUENCE);
		if (confSequence == null) confSequence = AssessmentInstance.QMD_ENTRY_SEQUENCE_ITEM;
		sequence.select(confSequence, true);
		sequence.addActionListener(FormEvent.ONCLICK);
		// when menu rendering is set to section only, show all question on the section otherwise not accessible
		if (renderSectionsOnly) confSequence = AssessmentInstance.QMD_ENTRY_SEQUENCE_SECTION;
		sequence.setEnabled(!renderSectionsOnly);

		
		Boolean bDisplayQuestionTitle = (Boolean)modConfig.get(IQEditController.CONFIG_KEY_QUESTIONTITLE);
		boolean confDisplayQuestionTitle = (bDisplayQuestionTitle != null) ? bDisplayQuestionTitle.booleanValue() : true;
		displayQuestionTitle = uifactory.addCheckboxesHorizontal("qti_displayQuestionTitle", "qti.form.questiontitle", formLayout, new String[]{"xx"}, new String[]{null});
		displayQuestionTitle.select("xx", confDisplayQuestionTitle);

		//display  automatic enumetation of choice options
		Boolean bAutoEnum = (Boolean)modConfig.get(IQEditController.CONFIG_KEY_AUTOENUM_CHOICES);
		boolean confAutoEnum = (bAutoEnum != null) ? bAutoEnum.booleanValue() : false;
		autoEnumerateChoices = uifactory.addCheckboxesHorizontal("qti_AutoEnumChoices", "qti.form.auto.enumerate.choices", formLayout, new String[]{"xx"}, new String[]{null});
		autoEnumerateChoices.select("xx", confAutoEnum);
		
		//provide  memo field
		Boolean bMemo = (Boolean)modConfig.get(IQEditController.CONFIG_KEY_MEMO);
		boolean confMemo = (bMemo != null) ? bMemo.booleanValue() : false;
		provideMemoField = uifactory.addCheckboxesHorizontal("qti_provideMemoField", "qti.form.auto.memofield", formLayout, new String[]{"xx"}, new String[]{null});
		provideMemoField.select("xx", confMemo);
		
		
		// question progress
		Boolean bEnableQuestionProgress = (Boolean)modConfig.get(IQEditController.CONFIG_KEY_QUESTIONPROGRESS);
		boolean confEnableQuestionProgress = (bEnableQuestionProgress != null) ? bEnableQuestionProgress.booleanValue() : true;
		displayQuestionProgress	= uifactory.addCheckboxesHorizontal("qti_enableQuestionProgress", "qti.form.questionprogress", formLayout, new String[]{"xx"}, new String[]{null});
		displayQuestionProgress.select("xx", confEnableQuestionProgress);
		displayQuestionProgress.setVisible(!isSurvey);
		
		// score progress
		Boolean bEnableScoreProgress = (Boolean)modConfig.get(IQEditController.CONFIG_KEY_SCOREPROGRESS);
		boolean confEnableScoreProgress = (bEnableScoreProgress != null) ? bEnableScoreProgress.booleanValue() : true;
		displayScoreProgress = uifactory.addCheckboxesHorizontal("resultTitle", "qti.form.scoreprogress", formLayout, new String[]{"xx"}, new String[]{null});
		
		if (isAssessment || isSelfTest) {
			displayScoreProgress.select("xx", confEnableScoreProgress);			
		} else {
			displayScoreProgress.select("xx", false);
			displayScoreProgress.setEnabled(false);
			displayScoreProgress.setVisible(false);
		}
		
		
		// enable cancel
		Boolean bEnableCancel = (Boolean)modConfig.get(IQEditController.CONFIG_KEY_ENABLECANCEL);
		boolean confEnableCancel = true;
		if (bEnableCancel != null) {
			// if defined use config value
			confEnableCancel = bEnableCancel.booleanValue();
		} else {
			// undefined... migrate according to old behaviour
			if (configKeyType != null && configKeyType.equals(AssessmentInstance.QMD_ENTRY_TYPE_ASSESS))
				confEnableCancel = false;
		}
		enableCancel = uifactory.addCheckboxesHorizontal("qti_enableCancel", "qti.form.enablecancel", formLayout, new String[]{"xx"}, new String[]{null});
		enableCancel.select("xx", confEnableCancel);
		
		if (isSelfTest) {
			enableCancel.select("xx", true);
			enableCancel.setVisible(false);
			enableCancel.setEnabled(false);
		}
		
		// enable suspend
		Boolean bEnableSuspend = (Boolean)modConfig.get(IQEditController.CONFIG_KEY_ENABLESUSPEND);
		boolean confEnableSuspend = (bEnableSuspend != null) ? bEnableSuspend.booleanValue() : false;
		enableSuspend = uifactory.addCheckboxesHorizontal("qti_enableSuspend", "qti.form.enablesuspend", formLayout, new String[]{"xx"}, new String[]{null});
		enableSuspend.select("xx", confEnableSuspend);
	
		uifactory.addSpacerElement("s2", formLayout, true);

		//Show score infos on start page
		Boolean bEnableScoreInfos = (Boolean)modConfig.get(IQEditController.CONFIG_KEY_ENABLESCOREINFO);
	  boolean enableScoreInfos = (bEnableScoreInfos != null) ? bEnableScoreInfos.booleanValue() : true;
		scoreInfo = uifactory.addCheckboxesHorizontal("qti_scoreInfo", "qti.form.scoreinfo", formLayout, new String[]{"xx"}, new String[]{null});
		scoreInfo.select("xx", enableScoreInfos);
		if (isAssessment || isSelfTest) {
			scoreInfo.select("xx", enableScoreInfos);
			scoreInfo.addActionListener(FormEvent.ONCLICK);
		} else {
			// isSurvey
			scoreInfo.setVisible(false);
		}
		
		
		//migration: check if old tests have no summary 
	  String configuredSummary = (String) modConfig.get(IQEditController.CONFIG_KEY_SUMMARY);
	  boolean noSummary = configuredSummary!=null && configuredSummary.equals(AssessmentInstance.QMD_ENTRY_SUMMARY_NONE) ? true : false;
		
		
		Boolean showResultOnHomePage = (Boolean) modConfig.get(IQEditController.CONFIG_KEY_RESULT_ON_HOME_PAGE);
		boolean confEnableShowResultOnHomePage = (showResultOnHomePage != null) ? showResultOnHomePage.booleanValue() : false;
		confEnableShowResultOnHomePage = !noSummary && confEnableShowResultOnHomePage;
		showResultsOnHomePage = uifactory.addCheckboxesHorizontal("qti_enableResultsOnHomePage", "qti.form.results.onhomepage", formLayout, new String[]{"xx"}, new String[]{null});
		showResultsOnHomePage.select("xx", confEnableShowResultOnHomePage);
		showResultsOnHomePage.addActionListener(FormEvent.ONCLICK);
		showResultsOnHomePage.setVisible(!isSurvey);
		
		Boolean showResultsActive = (Boolean) modConfig.get(IQEditController.CONFIG_KEY_DATE_DEPENDENT_RESULTS);
		boolean showResultsDateDependent = false; // default false
		if (showResultsActive != null) {
			showResultsDateDependent = showResultsActive.booleanValue();
		}

		showResultsDateDependentButton = uifactory.addCheckboxesHorizontal("qti_showresult", "qti.form.show.results", formLayout, new String[]{"xx"}, new String[]{null});
		if (isAssessment || isSelfTest) {
			showResultsDateDependentButton.select("xx", showResultsDateDependent);
			showResultsDateDependentButton.addActionListener(FormEvent.ONCLICK);
		} else {
			showResultsDateDependentButton.setEnabled(false);
		}
	
		Date startDate = (Date) modConfig.get(IQEditController.CONFIG_KEY_RESULTS_START_DATE);
		startDateElement = uifactory.addDateChooser("qti_form_start_date", "qti.form.date.start", null, formLayout);
		startDateElement.setDateChooserTimeEnabled(true);
		startDateElement.setDate(startDate);
		startDateElement.setMandatory(true);
		
		Date endDate = (Date) modConfig.get(IQEditController.CONFIG_KEY_RESULTS_END_DATE);
		endDateElement = uifactory.addDateChooser("qti_form_end_date", "qti.form.date.end", null, formLayout);
		endDateElement.setDateChooserTimeEnabled(true);
		endDateElement.setDate(endDate);

		Boolean showResultOnFinish = (Boolean) modConfig.get(IQEditController.CONFIG_KEY_RESULT_ON_FINISH);
		boolean confEnableShowResultOnFinish = (showResultOnFinish != null) ? showResultOnFinish.booleanValue() : true;
		confEnableShowResultOnFinish = !noSummary && confEnableShowResultOnFinish;
		showResultsAfterFinishTest = uifactory.addCheckboxesHorizontal("qti_enableResultsOnFinish", "qti.form.results.onfinish", formLayout, new String[]{"xx"}, new String[]{null});
		showResultsAfterFinishTest.select("xx", confEnableShowResultOnFinish);
		showResultsAfterFinishTest.addActionListener(FormEvent.ONCLICK);
		showResultsAfterFinishTest.setVisible(!isSurvey);
			
		String[] summaryKeys = new String[] { 
				AssessmentInstance.QMD_ENTRY_SUMMARY_COMPACT, 
				AssessmentInstance.QMD_ENTRY_SUMMARY_SECTION,
				AssessmentInstance.QMD_ENTRY_SUMMARY_DETAILED
		};
		
		String[] summaryValues = new String[] {
				translate("qti.form.summary.compact"),
				translate("qti.form.summary.section"),
				translate("qti.form.summary.detailed")
		};

		summary = uifactory.addRadiosVertical("qti_form_summary", "qti.form.summary", formLayout, summaryKeys, summaryValues);
		String confSummary = (String) modConfig.get(IQEditController.CONFIG_KEY_SUMMARY);
		uifactory.addSpacerElement("spcSummary", formLayout, true);
		if (confSummary == null || noSummary) {
			confSummary = AssessmentInstance.QMD_ENTRY_SUMMARY_COMPACT;
		}
		if (isAssessment || isSelfTest) {
			summary.select(confSummary, true);
		} else {
			summary.setEnabled(false);
		}

		uifactory.addSpacerElement("submitSpacer", formLayout, true);
		uifactory.addFormSubmitButton("submit", formLayout);
		
		update();
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		update();
	}

	private void update() {
		
		enableMenu.setVisible(displayMenu.isSelected(0));
		
		menuRenderOptions.setVisible(displayMenu.isSelected(0));
		if (!limitAttempts.isSelected(0)) {
			attempts.setIntValue(0);
		}
		attempts.setVisible(limitAttempts.isVisible()&&limitAttempts.isSelected(0));
		attempts.setMandatory(attempts.isVisible());
		attempts.clearError();
		
		summary.setVisible(showResultsAfterFinishTest.isSelected(0) || showResultsOnHomePage.isSelected(0) );
		
		showResultsDateDependentButton.setVisible(showResultsOnHomePage.isSelected(0));
		
		if (!startDateElement.isVisible()) {
			startDateElement.setValue("");
		}
		startDateElement.clearError();
		startDateElement.setVisible(
				showResultsDateDependentButton.isVisible() &&
				showResultsDateDependentButton.isSelected(0)
		);
		
		endDateElement.clearError();
		if (!endDateElement.isVisible()) endDateElement.setValue("");
		endDateElement.setVisible(startDateElement.isVisible());

		if (isDisplayMenu() && isEnableMenu()) {
			// when items not visible in menu, question sequence must be set to section to make items accessible
			if (isMenuRenderSectionsOnly()) {
				sequence.select(AssessmentInstance.QMD_ENTRY_SEQUENCE_SECTION, true);
				sequence.setEnabled(false);
			} else {
				sequence.setEnabled(true);
			}
		}
		
		flc.setDirty(true);
	}
	
	private boolean isDisplayMenu() {
		return displayMenu.isSelected(0);
	}
	
	/**
	 * @return true: menu is enabled
	 */
	private boolean isEnableMenu() {
		return enableMenu.isSelected(0);
	}

	/**
	 * @return true: menu should be displayed
	 */
	private boolean isFullWindow() {
		return fullWindowEl.isSelected(0);
	}
	
	/**
	 * @return true: score progress is enabled
	 */
	private boolean isDisplayScoreProgress() {
		return displayScoreProgress.isSelected(0);
	}
	
	/**
	 * @return true: score progress is enabled
	 */
	private boolean isDisplayQuestionProgress() { return displayQuestionProgress.isSelected(0); }
	/**
	 * @return true: question title is enabled
	 */
	private boolean isDisplayQuestionTitle() { return displayQuestionTitle.isSelected(0); }
	/**
	 * @return true: automatic enumeration of choice options enabled 
	 */
	private boolean isAutoEnumChoices() { return autoEnumerateChoices.isSelected(0); }
	/**
	 * @return true: provide memo field
	 */
	private boolean isProvideMemoField() { return provideMemoField.isSelected(0); }
	/**
	 * @return sequence configuration: section or item
	 */
	private String getSequence() { return sequence.getSelectedKey(); }
	/**
	 * @return true: cancel is enabled
	 */
	private boolean isEnableCancel() { return enableCancel.isSelected(0); }
	/**
	 * @return true: suspend is enabled
	 */
	private boolean isEnableSuspend() { return enableSuspend.isSelected(0); }
	/**
	 * @return summary type: compact or detailed
	 */
	private String getSummary() { return summary.getSelectedKey();}
	/**
	 * @return number of max attempts
	 */
	private Integer getAttempts() { 
		Integer a =  attempts.getIntValue();
		return a == 0 ? null : attempts.getIntValue();
	}
	
	private boolean isBlockAfterSuccess() {
		return blockAfterSuccess.isSelected(0);
	}
	
	/**
	 * 
	 * @return true if only section title should be rendered
	 */
	private Boolean isMenuRenderSectionsOnly() {	return Boolean.valueOf(menuRenderOptions.getSelectedKey());}
	/**
	 * @return true: score-info on start-page is enabled
	 */
	private boolean isEnableScoreInfo() { return scoreInfo.isSelected(0); }	
	
	/**
	 * 
	 * @return true is the results are shown date dependent
	 */
	private boolean isShowResultsDateDependent() { return showResultsDateDependentButton.isSelected(0); }
	
	/**
	 * 
	 * @return Returns the start date for the result visibility.
	 */
	private Date getShowResultsStartDate() { return startDateElement.getDate(); }
	
	/**
	 * 
	 * @return Returns the end date for the result visibility.
	 */
	private Date getShowResultsEndDate() { return endDateElement.getDate(); }
	
	/**
	 * 
	 * @return Returns true if the results are shown after test finished.
	 */
	private boolean isShowResultsAfterFinishTest() { return showResultsAfterFinishTest.isSelected(0); }
	
	/**
	 * 
	 * @return Returns true if the results are shown on the test home page.
	 */
	private boolean isShowResultsOnHomePage() { return showResultsOnHomePage.isSelected(0); }
	
	
	@Override
	protected void doDispose() {
		//
	}
	
}


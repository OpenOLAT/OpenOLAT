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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
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
 * 
 * Initial date: 23 févr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IQ12LayoutEditForm extends FormBasicController {

	private SelectionElement enableMenu;
	private SelectionElement displayMenu;
	private SelectionElement displayScoreProgress;
	private SelectionElement displayQuestionProgress;
	private SelectionElement displayQuestionTitle;
	private SelectionElement autoEnumerateChoices;
	private SelectionElement provideMemoField;
	private SingleSelection sequence;
	private SelectionElement enableCancel;
	private SelectionElement enableSuspend;
	private SelectionElement limitAttempts;
	private SelectionElement blockAfterSuccess;
	private IntegerElement attempts;
	private SingleSelection menuRenderOptions;
	private SelectionElement fullWindowEl;
	
	private ModuleConfiguration modConfig;
	
	private String[] menuRenderOptKeys, menuRenderOptValues;
	private String[] sequenceKeys, sequenceValues;
	private String configKeyType;
	
	private boolean isAssessment, isSelfTest, isSurvey;
	
	/**
	 * Constructor for the qti configuration form
	 * @param ureq
	 * @param wControl
	 * @param modConfig
	 */
	IQ12LayoutEditForm(UserRequest ureq, WindowControl wControl, ModuleConfiguration modConfig) {
		super (ureq, wControl);
		this.modConfig = modConfig;
		
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
		
		initForm(ureq);
	}
	
	@Override
	protected boolean validateFormLogic (UserRequest ureq) {
		return true;
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		modConfig.set(IQEditController.CONFIG_KEY_DISPLAYMENU, Boolean.valueOf(isDisplayMenu()));
		modConfig.set(IQEditController.CONFIG_FULLWINDOW, Boolean.valueOf(isFullWindow()));
		
		if (isDisplayMenu()) {
			modConfig.set(IQEditController.CONFIG_KEY_RENDERMENUOPTION, isMenuRenderSectionsOnly());
			modConfig.set(IQEditController.CONFIG_KEY_ENABLEMENU, Boolean.valueOf(isEnableMenu()));
		} else {
			// set default values when menu is not displayed
			modConfig.set(IQEditController.CONFIG_KEY_RENDERMENUOPTION, Boolean.FALSE);
			modConfig.set(IQEditController.CONFIG_KEY_ENABLEMENU, Boolean.FALSE); 
		}
		
		modConfig.set(IQEditController.CONFIG_KEY_QUESTIONPROGRESS, Boolean.valueOf(isDisplayQuestionProgress()));
		modConfig.set(IQEditController.CONFIG_KEY_SEQUENCE, getSequence());
		modConfig.set(IQEditController.CONFIG_KEY_ENABLECANCEL, Boolean.valueOf(isEnableCancel()));
		modConfig.set(IQEditController.CONFIG_KEY_ENABLESUSPEND, Boolean.valueOf(isEnableSuspend()));
		modConfig.set(IQEditController.CONFIG_KEY_QUESTIONTITLE, Boolean.valueOf(isDisplayQuestionTitle()));
		modConfig.set(IQEditController.CONFIG_KEY_AUTOENUM_CHOICES, Boolean.valueOf(isAutoEnumChoices()));
		modConfig.set(IQEditController.CONFIG_KEY_MEMO, Boolean.valueOf(isProvideMemoField()));
		// Only tests and selftests have summaries and score progress
		if (!isSurvey) {
			modConfig.set(IQEditController.CONFIG_KEY_SCOREPROGRESS, Boolean.valueOf(isDisplayScoreProgress()));
		}
		// Only tests have a limitation on number of attempts
		if (isAssessment) {
			modConfig.set(IQEditController.CONFIG_KEY_ATTEMPTS, getAttempts());
			modConfig.set(IQEditController.CONFIG_KEY_BLOCK_AFTER_SUCCESS, Boolean.valueOf(isBlockAfterSuccess()));
		}
		
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		limitAttempts = uifactory.addCheckboxesHorizontal("limitAttempts", "qti.form.limit.attempts", formLayout, new String[]{"xx"}, new String[]{null});
		
		Integer confAttempts = (Integer) modConfig.get(IQEditController.CONFIG_KEY_ATTEMPTS);
		if (confAttempts == null) confAttempts = Integer.valueOf(0);
		attempts = uifactory.addIntegerElement("qti.form.attempts", confAttempts, formLayout);	
		attempts.setDisplaySize(2);
		attempts.setMinValueCheck(1, null);
		attempts.setMaxValueCheck(20, null);
		
		//add it
		blockAfterSuccess = uifactory.addCheckboxesHorizontal("blockAfterSuccess", "qti.form.block.afterSuccess", formLayout, new String[]{"xx"}, new String[]{null});
		Boolean block = modConfig.getBooleanEntry(IQEditController.CONFIG_KEY_BLOCK_AFTER_SUCCESS);
		blockAfterSuccess.select("xx", block == null ? false : block.booleanValue() );

		// Only assessments have a limitation on number of attempts
		if (isAssessment) {
			limitAttempts.select("xx", confAttempts>0);
			limitAttempts.addActionListener(FormEvent.ONCLICK);
		} else {
			limitAttempts.select("xx", false);
			limitAttempts.setVisible(false);
			attempts.setVisible(false);
			blockAfterSuccess.select("xx", false);
			blockAfterSuccess.setVisible(false);
		}
		
		Boolean fullWindow = modConfig.getBooleanEntry(IQEditController.CONFIG_FULLWINDOW);
		fullWindowEl = uifactory.addCheckboxesHorizontal("fullwindow", "qti.form.fullwindow", formLayout, new String[]{"fullwindow"}, new String[]{null});
		fullWindowEl.select("fullwindow", fullWindow == null ? true : fullWindow.booleanValue());
		
		Boolean CdisplayMenu = modConfig.getBooleanEntry(IQEditController.CONFIG_KEY_DISPLAYMENU);
		displayMenu = uifactory.addCheckboxesHorizontal("qti_displayMenu", "qti.form.menudisplay", formLayout, new String[]{"xx"}, new String[]{null});
		displayMenu.select("xx", CdisplayMenu == null ? true : CdisplayMenu );
		displayMenu.addActionListener(FormEvent.ONCLICK);
		
		Boolean CenableMenu = modConfig.getBooleanEntry(IQEditController.CONFIG_KEY_ENABLEMENU);
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
			renderSectionsOnly = modConfig.getBooleanEntry(IQEditController.CONFIG_KEY_RENDERMENUOPTION);
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

		
		Boolean bDisplayQuestionTitle = modConfig.getBooleanEntry(IQEditController.CONFIG_KEY_QUESTIONTITLE);
		boolean confDisplayQuestionTitle = (bDisplayQuestionTitle != null) ? bDisplayQuestionTitle.booleanValue() : true;
		displayQuestionTitle = uifactory.addCheckboxesHorizontal("qti_displayQuestionTitle", "qti.form.questiontitle", formLayout, new String[]{"xx"}, new String[]{null});
		displayQuestionTitle.select("xx", confDisplayQuestionTitle);

		//display  automatic enumetation of choice options
		Boolean bAutoEnum = modConfig.getBooleanEntry(IQEditController.CONFIG_KEY_AUTOENUM_CHOICES);
		boolean confAutoEnum = (bAutoEnum != null) ? bAutoEnum.booleanValue() : false;
		autoEnumerateChoices = uifactory.addCheckboxesHorizontal("qti_AutoEnumChoices", "qti.form.auto.enumerate.choices", formLayout, new String[]{"xx"}, new String[]{null});
		autoEnumerateChoices.select("xx", confAutoEnum);
		
		//provide  memo field
		Boolean bMemo = modConfig.getBooleanEntry(IQEditController.CONFIG_KEY_MEMO);
		boolean confMemo = (bMemo != null) ? bMemo.booleanValue() : false;
		provideMemoField = uifactory.addCheckboxesHorizontal("qti_provideMemoField", "qti.form.auto.memofield", formLayout, new String[]{"xx"}, new String[]{null});
		provideMemoField.select("xx", confMemo);
		
		
		// question progress
		Boolean bEnableQuestionProgress = modConfig.getBooleanEntry(IQEditController.CONFIG_KEY_QUESTIONPROGRESS);
		boolean confEnableQuestionProgress = (bEnableQuestionProgress != null) ? bEnableQuestionProgress.booleanValue() : true;
		displayQuestionProgress	= uifactory.addCheckboxesHorizontal("qti_enableQuestionProgress", "qti.form.questionprogress", formLayout, new String[]{"xx"}, new String[]{null});
		displayQuestionProgress.select("xx", confEnableQuestionProgress);
		displayQuestionProgress.setVisible(!isSurvey);
		
		// score progress
		Boolean bEnableScoreProgress = modConfig.getBooleanEntry(IQEditController.CONFIG_KEY_SCOREPROGRESS);
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
		Boolean bEnableCancel = modConfig.getBooleanEntry(IQEditController.CONFIG_KEY_ENABLECANCEL);
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
		Boolean bEnableSuspend = modConfig.getBooleanEntry(IQEditController.CONFIG_KEY_ENABLESUSPEND);
		boolean confEnableSuspend = (bEnableSuspend != null) ? bEnableSuspend.booleanValue() : false;
		enableSuspend = uifactory.addCheckboxesHorizontal("qti_enableSuspend", "qti.form.enablesuspend", formLayout, new String[]{"xx"}, new String[]{null});
		enableSuspend.select("xx", confEnableSuspend);

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
	
	@Override
	protected void doDispose() {
		//
	}
	
}


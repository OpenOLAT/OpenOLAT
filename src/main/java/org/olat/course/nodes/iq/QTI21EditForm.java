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
package org.olat.course.nodes.iq;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.ims.qti.process.AssessmentInstance;
import org.olat.ims.qti21.QTI21DeliveryOptions;
import org.olat.ims.qti21.QTI21DeliveryOptions.ShowResultsOnFinish;
import org.olat.modules.ModuleConfiguration;

/**
 * 
 * Initial date: 26.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21EditForm extends FormBasicController {
	
	private static final String[] onKeys = new String[]{ "on" };
	private static final String[] onValues = new String[]{ "" };

	private SelectionElement fullWindowEl;
	private SingleSelection correctionModeEl;
	private MultipleSelectionElement showTitlesEl, showMenuEl;
	private MultipleSelectionElement personalNotesEl;
	private MultipleSelectionElement enableCancelEl, enableSuspendEl;
	private MultipleSelectionElement limitAttemptsEl, blockAfterSuccessEl;
	private MultipleSelectionElement displayQuestionProgressEl, displayScoreProgressEl;
	private MultipleSelectionElement showResultsOnFinishEl;
	private MultipleSelectionElement allowAnonymEl;
	private SingleSelection typeShowResultsOnFinishEl;
	
	private TextElement maxAttemptsEl;
	
	private final boolean needManulCorrection;
	private final ModuleConfiguration modConfig;
	private final QTI21DeliveryOptions deliveryOptions;
	
	private static final String[] correctionModeKeys = new String[]{ "auto", "manual" };
	
	public QTI21EditForm(UserRequest ureq, WindowControl wControl, ModuleConfiguration modConfig,
			QTI21DeliveryOptions deliveryOptions, boolean needManulCorrection) {
		super(ureq, wControl);
		
		this.modConfig = modConfig;
		this.deliveryOptions = (deliveryOptions == null ? new QTI21DeliveryOptions() : deliveryOptions);
		this.needManulCorrection = needManulCorrection;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String [] correctionModeValues = new String[]{
			translate("correction.auto"),
			translate("correction.manual")
		};
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
			if(needManulCorrection) {
				correctionModeEl.select(correctionModeKeys[1], true);
			} else {
				correctionModeEl.select(correctionModeKeys[0], true);
			}
		}
		
		limitAttemptsEl = uifactory.addCheckboxesHorizontal("limitAttempts", "qti.form.limit.attempts", formLayout, onKeys, onValues);
		limitAttemptsEl.addActionListener(FormEvent.ONCLICK);
		String maxAttemptsValue = "";
		int maxAttempts = modConfig.getIntegerSafe(IQEditController.CONFIG_KEY_ATTEMPTS, deliveryOptions.getMaxAttempts());
		if(maxAttempts > 0) {
			limitAttemptsEl.select(onKeys[0], true);
		}
		maxAttemptsEl = uifactory.addTextElement("maxAttempts", "qti.form.attempts", 8, maxAttemptsValue, formLayout);	
		maxAttemptsEl.setDisplaySize(2);
		maxAttemptsEl.setVisible(maxAttempts > 0);
		
		boolean blockAfterSuccess = modConfig.getBooleanSafe(IQEditController.CONFIG_KEY_BLOCK_AFTER_SUCCESS, deliveryOptions.isBlockAfterSuccess());
		blockAfterSuccessEl = uifactory.addCheckboxesHorizontal("blockAfterSuccess", "qti.form.block.afterSuccess", formLayout, onKeys, onValues);
		if(blockAfterSuccess) {
			blockAfterSuccessEl.select(onKeys[0], true);
		}
		
		boolean allowAnonym = modConfig.getBooleanSafe(IQEditController.CONFIG_ALLOW_ANONYM, deliveryOptions.isAllowAnonym());
		allowAnonymEl = uifactory.addCheckboxesHorizontal("allowAnonym", "qti.form.allow.anonym", formLayout, onKeys, onValues);
		if(allowAnonym) {
			allowAnonymEl.select(onKeys[0], true);
		}
		
		boolean fullWindow = modConfig.getBooleanSafe(IQEditController.CONFIG_FULLWINDOW);
		fullWindowEl = uifactory.addCheckboxesHorizontal("fullwindow", "qti.form.fullwindow", formLayout, new String[]{"x"}, new String[]{""});
		fullWindowEl.select("x", fullWindow);

		boolean showTitles = modConfig.getBooleanSafe(IQEditController.CONFIG_KEY_QUESTIONTITLE, deliveryOptions.isShowTitles());
		showTitlesEl = uifactory.addCheckboxesHorizontal("showTitles", "qti.form.questiontitle", formLayout, onKeys, onValues);
		if(showTitles) {
			showTitlesEl.select(onKeys[0], true);
		}
		
		boolean showMenu = modConfig.getBooleanSafe(IQEditController.CONFIG_KEY_ENABLEMENU, deliveryOptions.isShowMenu());
		showMenuEl = uifactory.addCheckboxesHorizontal("showmenu", "qti.form.menuenable", formLayout, onKeys, onValues);
		if(showMenu) {
			showMenuEl.select(onKeys[0], true);
		}
		
		boolean personalNotes = modConfig.getBooleanSafe(IQEditController.CONFIG_KEY_MEMO, deliveryOptions.isPersonalNotes());
		personalNotesEl = uifactory.addCheckboxesHorizontal("personalNotes", "qti.form.auto.memofield", formLayout, onKeys, onValues);
		if(personalNotes) {
			personalNotesEl.select(onKeys[0], true);
		}

		boolean questionProgress = modConfig.getBooleanSafe(IQEditController.CONFIG_KEY_QUESTIONPROGRESS, deliveryOptions.isDisplayQuestionProgress());
		displayQuestionProgressEl = uifactory.addCheckboxesHorizontal("questionProgress", "qti.form.questionprogress", formLayout, onKeys, onValues);
		if(questionProgress) {
			displayQuestionProgressEl.select(onKeys[0], true);
		}
		
		boolean questionScore = modConfig.getBooleanSafe(IQEditController.CONFIG_KEY_SCOREPROGRESS, deliveryOptions.isDisplayScoreProgress());
		displayScoreProgressEl = uifactory.addCheckboxesHorizontal("scoreProgress", "qti.form.scoreprogress", formLayout, onKeys, onValues);
		if(questionScore) {
			displayScoreProgressEl.select(onKeys[0], true);
		}

		boolean enableSuspend = modConfig.getBooleanSafe(IQEditController.CONFIG_KEY_ENABLESUSPEND, deliveryOptions.isEnableSuspend());
		enableSuspendEl = uifactory.addCheckboxesHorizontal("suspend", "qti.form.enablesuspend", formLayout, onKeys, onValues);
		if(enableSuspend) {
			enableSuspendEl.select(onKeys[0], true);
		}

		boolean enableCancel = modConfig.getBooleanSafe(IQEditController.CONFIG_KEY_ENABLECANCEL, deliveryOptions.isEnableCancel());
		enableCancelEl = uifactory.addCheckboxesHorizontal("cancel", "qti.form.enablecancel", formLayout, onKeys, onValues);
		if(enableCancel) {
			enableCancelEl.select(onKeys[0], true);
		}
		
		showResultsOnFinishEl = uifactory.addCheckboxesHorizontal("resultOnFinish", "qti.form.results.onfinish", formLayout, onKeys, onValues);
		showResultsOnFinishEl.addActionListener(FormEvent.ONCHANGE);
		
		ShowResultsOnFinish showSummary = deliveryOptions.getShowResultsOnFinish();
		String defaultConfSummary = showSummary == null ? AssessmentInstance.QMD_ENTRY_SUMMARY_COMPACT : showSummary.getIQEquivalent();
		String confSummary = modConfig.getStringValue(IQEditController.CONFIG_KEY_SUMMARY, defaultConfSummary);
		if(!AssessmentInstance.QMD_ENTRY_SUMMARY_NONE.equals(confSummary)) {
			showResultsOnFinishEl.select(onKeys[0], true);
		}

		String[] typeShowResultsOnFinishKeys = new String[] {
				AssessmentInstance.QMD_ENTRY_SUMMARY_COMPACT, AssessmentInstance.QMD_ENTRY_SUMMARY_SECTION, AssessmentInstance.QMD_ENTRY_SUMMARY_DETAILED
		};
		String[] typeShowResultsOnFinishValues = new String[] {
			translate("qti.form.summary.compact"), translate("qti.form.summary.section"), translate("qti.form.summary.detailed")
		};
		typeShowResultsOnFinishEl = uifactory.addRadiosVertical("typeResultOnFinish", "qti.form.summary", formLayout, typeShowResultsOnFinishKeys, typeShowResultsOnFinishValues);
		typeShowResultsOnFinishEl.setVisible(showResultsOnFinishEl.isAtLeastSelected(1));
		if(StringHelper.containsNonWhitespace(confSummary)) {
			for(String typeShowResultsOnFinishKey:typeShowResultsOnFinishKeys) {
				typeShowResultsOnFinishEl.select(typeShowResultsOnFinishKey, true);
			}
		} 
		if(typeShowResultsOnFinishEl.isOneSelected()) {
			typeShowResultsOnFinishEl.select(AssessmentInstance.QMD_ENTRY_SUMMARY_COMPACT, true);
		}
		
		uifactory.addFormSubmitButton("submit", formLayout);
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		if(limitAttemptsEl.isAtLeastSelected(1)) {
			maxAttemptsEl.clearError();
			if(StringHelper.containsNonWhitespace(maxAttemptsEl.getValue())) {
				try {
					Integer.parseInt(maxAttemptsEl.getValue());
				} catch(NumberFormatException e) {
					maxAttemptsEl.setErrorKey("form.error.nointeger", null);
					allOk &= false;
				}
			} else {
				maxAttemptsEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			}
		}

		return allOk & super.validateFormLogic(ureq);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(limitAttemptsEl == source) {
			maxAttemptsEl.setVisible(limitAttemptsEl.isAtLeastSelected(1));
		} else if(showResultsOnFinishEl == source) {
			typeShowResultsOnFinishEl.setVisible(showResultsOnFinishEl.isAtLeastSelected(1));
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		modConfig.setBooleanEntry(IQEditController.CONFIG_FULLWINDOW, fullWindowEl.isSelected(0));
		if(correctionModeEl.isOneSelected()) {
			modConfig.setStringValue(IQEditController.CONFIG_CORRECTION_MODE, correctionModeEl.getSelectedKey());
		}
		if(limitAttemptsEl.isSelected(0)) {
			int maxAttempts = Integer.parseInt(maxAttemptsEl.getValue());
			modConfig.setIntValue(IQEditController.CONFIG_KEY_ATTEMPTS, maxAttempts);
		} else {
			modConfig.setIntValue(IQEditController.CONFIG_KEY_ATTEMPTS, 0);
		}
		modConfig.setBooleanEntry(IQEditController.CONFIG_KEY_BLOCK_AFTER_SUCCESS, blockAfterSuccessEl.isSelected(0));
		modConfig.setBooleanEntry(IQEditController.CONFIG_KEY_ENABLEMENU, showMenuEl.isSelected(0));
		modConfig.setBooleanEntry(IQEditController.CONFIG_KEY_QUESTIONTITLE, showTitlesEl.isSelected(0));
		modConfig.setBooleanEntry(IQEditController.CONFIG_KEY_MEMO, personalNotesEl.isSelected(0));
		modConfig.setBooleanEntry(IQEditController.CONFIG_KEY_ENABLECANCEL, enableCancelEl.isSelected(0));
		modConfig.setBooleanEntry(IQEditController.CONFIG_KEY_ENABLESUSPEND, enableSuspendEl.isSelected(0));
		modConfig.setBooleanEntry(IQEditController.CONFIG_KEY_QUESTIONPROGRESS, displayQuestionProgressEl.isSelected(0));
		modConfig.setBooleanEntry(IQEditController.CONFIG_KEY_SCOREPROGRESS, displayScoreProgressEl.isSelected(0));
		modConfig.setBooleanEntry(IQEditController.CONFIG_ALLOW_ANONYM, allowAnonymEl.isSelected(0));
		
		if(showResultsOnFinishEl.isAtLeastSelected(1)) {
			if(typeShowResultsOnFinishEl.isOneSelected()) {
				modConfig.set(IQEditController.CONFIG_KEY_SUMMARY, typeShowResultsOnFinishEl.getSelectedKey());
			} else {
				modConfig.set(IQEditController.CONFIG_KEY_SUMMARY, AssessmentInstance.QMD_ENTRY_SUMMARY_NONE);
			}
		} else {
			modConfig.set(IQEditController.CONFIG_KEY_SUMMARY, AssessmentInstance.QMD_ENTRY_SUMMARY_NONE);
		}
		
		fireEvent(ureq, Event.DONE_EVENT);
	}
}

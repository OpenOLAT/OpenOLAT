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
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.ims.qti21.QTI21DeliveryOptions;
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
	private MultipleSelectionElement showTitlesEl;
	private MultipleSelectionElement personalNotesEl;
	private MultipleSelectionElement enableCancelEl, enableSuspendEl;
	private MultipleSelectionElement displayQuestionProgressEl, displayScoreProgressEl;
	

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
		
		boolean fullWindow = modConfig.getBooleanSafe(IQEditController.CONFIG_FULLWINDOW);
		fullWindowEl = uifactory.addCheckboxesHorizontal("fullwindow", "qti.form.fullwindow", formLayout, new String[]{"x"}, new String[]{""});
		fullWindowEl.select("x", fullWindow);

		boolean showTitles = mergeBoolean(modConfig.getBooleanEntry(IQEditController.CONFIG_KEY_QUESTIONTITLE), deliveryOptions.isDisplayQuestionProgress());
		showTitlesEl = uifactory.addCheckboxesHorizontal("showTitles", "qti.form.questiontitle", formLayout, onKeys, onValues);
		if(showTitles) {
			showTitlesEl.select(onKeys[0], true);
		}

		boolean personalNotes = mergeBoolean(modConfig.getBooleanEntry(IQEditController.CONFIG_KEY_MEMO), deliveryOptions.isDisplayQuestionProgress());
		personalNotesEl = uifactory.addCheckboxesHorizontal("personalNotes", "qti.form.auto.memofield", formLayout, onKeys, onValues);
		if(personalNotes) {
			personalNotesEl.select(onKeys[0], true);
		}

		boolean questionProgress = mergeBoolean(modConfig.getBooleanEntry(IQEditController.CONFIG_KEY_QUESTIONPROGRESS), deliveryOptions.isDisplayQuestionProgress());
		displayQuestionProgressEl = uifactory.addCheckboxesHorizontal("questionProgress", "qti.form.questionprogress", formLayout, onKeys, onValues);
		if(questionProgress) {
			displayQuestionProgressEl.select(onKeys[0], true);
		}
		
		boolean questionScore = mergeBoolean(modConfig.getBooleanEntry(IQEditController.CONFIG_KEY_SCOREPROGRESS), deliveryOptions.isDisplayScoreProgress());
		displayScoreProgressEl = uifactory.addCheckboxesHorizontal("scoreProgress", "qti.form.scoreprogress", formLayout, onKeys, onValues);
		if(questionScore) {
			displayScoreProgressEl.select(onKeys[0], true);
		}

		boolean enableSuspend = mergeBoolean(modConfig.getBooleanEntry(IQEditController.CONFIG_KEY_ENABLESUSPEND), deliveryOptions.isEnableSuspend());
		enableSuspendEl = uifactory.addCheckboxesHorizontal("suspend", "qti.form.enablesuspend", formLayout, onKeys, onValues);
		if(enableSuspend) {
			enableSuspendEl.select(onKeys[0], true);
		}

		boolean enableCancel = mergeBoolean(modConfig.getBooleanEntry(IQEditController.CONFIG_KEY_ENABLECANCEL), deliveryOptions.isEnableCancel());
		enableCancelEl = uifactory.addCheckboxesHorizontal("cancel", "qti.form.enablecancel", formLayout, onKeys, onValues);
		if(enableCancel) {
			enableCancelEl.select(onKeys[0], true);
		}
		
		uifactory.addFormSubmitButton("submit", formLayout);
	}
	
	private boolean mergeBoolean(Boolean config, boolean options) {
		if(config != null) return config.booleanValue();
		return options;
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		modConfig.setBooleanEntry(IQEditController.CONFIG_FULLWINDOW, fullWindowEl.isSelected(0));
		if(correctionModeEl.isOneSelected()) {
			modConfig.setStringValue(IQEditController.CONFIG_CORRECTION_MODE, correctionModeEl.getSelectedKey());
		}
		modConfig.setBooleanEntry(IQEditController.CONFIG_KEY_QUESTIONTITLE, showTitlesEl.isSelected(0));
		modConfig.setBooleanEntry(IQEditController.CONFIG_KEY_MEMO, personalNotesEl.isSelected(0));
		modConfig.setBooleanEntry(IQEditController.CONFIG_KEY_ENABLECANCEL, enableCancelEl.isSelected(0));
		modConfig.setBooleanEntry(IQEditController.CONFIG_KEY_ENABLESUSPEND, enableSuspendEl.isSelected(0));
		modConfig.setBooleanEntry(IQEditController.CONFIG_KEY_QUESTIONPROGRESS, displayQuestionProgressEl.isSelected(0));
		modConfig.setBooleanEntry(IQEditController.CONFIG_KEY_SCOREPROGRESS, displayScoreProgressEl.isSelected(0));
		fireEvent(ureq, Event.DONE_EVENT);
	}
}

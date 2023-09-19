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
package org.olat.course.nodes.gta.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.avrecorder.AVVideoQuality;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.MailHelper;
import org.olat.course.nodes.GTACourseNode;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.audiovideorecording.AVModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTASubmissionEditController extends FormBasicController {
	
	private static final String[] enableKeys = new String[] { "on" };
	
	private RichTextElement textEl;
	private TextElement minNumberOfDocsEl;
	private TextElement maxNumberOfDocsEl;
	private MultipleSelectionElement externalEditorEl;
	private MultipleSelectionElement embeddedEditorEl;
	private MultipleSelectionElement submissionTemplateEl;
	private MultipleSelectionElement allowVideoRecordingsEl;
	private SelectionValues videoQualityKV;
	private TextElement maxVideoDurationEl;
	private SingleSelection videoQualityEl;
	private MultipleSelectionElement allowAudioRecordingsEl;
	private TextElement maxAudioDurationEl;
	private MultipleSelectionElement  emailConfirmationEl;
	
	private final ModuleConfiguration config;

	@Autowired
	private AVModule avModule;
	
	public GTASubmissionEditController(UserRequest ureq, WindowControl wControl, ModuleConfiguration config) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		
		this.config = config;
		
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//configuration
		FormLayoutContainer configCont = FormLayoutContainer.createDefaultFormLayout("config", getTranslator());
		configCont.setRootForm(mainForm);
		configCont.setFormTitle(translate("editor.title"));
		configCont.setFormContextHelp("manual_user/task/Three_Steps_to_Your_Task/#configuration");
		formLayout.add(configCont);
		
		String[] enableValues = new String[]{ translate("enabled") };
		externalEditorEl = uifactory.addCheckboxesHorizontal("external.editor", "external.editor", configCont, enableKeys, enableValues);
		boolean external = config.getBooleanSafe(GTACourseNode.GTASK_EXTERNAL_EDITOR);
		externalEditorEl.select(enableKeys[0], external);
	
		embeddedEditorEl = uifactory.addCheckboxesHorizontal("embedded.editor", "embedded.editor", configCont, enableKeys, enableValues);
		boolean embbeded = config.getBooleanSafe(GTACourseNode.GTASK_EMBBEDED_EDITOR);
		embeddedEditorEl.select(enableKeys[0], embbeded);
		
		submissionTemplateEl = uifactory.addCheckboxesHorizontal("submission.template", "submission.template", configCont, enableKeys, enableValues);
		boolean submissionTemplate = config.getBooleanSafe(GTACourseNode.GTASK_SUBMISSION_TEMPLATE);
		submissionTemplateEl.select(enableKeys[0], submissionTemplate);

		boolean videoRecordingEnabled = avModule.isVideoRecordingEnabled();
		allowVideoRecordingsEl = uifactory.addCheckboxesHorizontal("av.allow.video.recordings", "av.allow.video.recordings", configCont, enableKeys, enableValues);
		boolean allowVideoRecordings = config.getBooleanSafe(GTACourseNode.GTASK_ALLOW_VIDEO_RECORDINGS);
		allowVideoRecordingsEl.select(enableKeys[0], allowVideoRecordings);
		allowVideoRecordingsEl.addActionListener(FormEvent.ONCHANGE);
		allowVideoRecordingsEl.setVisible(videoRecordingEnabled);

		String maxVideoDuration = config.getStringValue(GTACourseNode.GTASK_MAX_VIDEO_DURATION, "600");
		maxVideoDurationEl = uifactory.addTextElement("av.max.video.duration", "av.max.duration", 5, maxVideoDuration, configCont);
		maxVideoDurationEl.setRegexMatchCheck("\\d+", "av.max.duration.error");
		maxVideoDurationEl.setVisible(videoRecordingEnabled && allowVideoRecordings);

		AVVideoQuality videoQuality = AVVideoQuality.valueOf(config.getStringValue(GTACourseNode.GTASK_VIDEO_QUALITY, AVVideoQuality.medium.name()));
		videoQualityKV = AVVideoQuality.getSelectionValues(getLocale());
		videoQualityEl = uifactory.addDropdownSingleselect("av.video.quality", configCont, videoQualityKV.keys(),
				videoQualityKV.values());
		videoQualityEl.select(videoQuality.name(), true);
		videoQualityEl.setVisible(videoRecordingEnabled && allowVideoRecordings);

		boolean audioRecordingsEnabled = avModule.isAudioRecordingEnabled();
		allowAudioRecordingsEl = uifactory.addCheckboxesHorizontal("av.allow.audio.recordings", "av.allow.audio.recordings", configCont, enableKeys, enableValues);
		boolean allowAudioRecordings = config.getBooleanSafe(GTACourseNode.GTASK_ALLOW_AUDIO_RECORDINGS);
		allowAudioRecordingsEl.select(enableKeys[0], allowAudioRecordings);
		allowAudioRecordingsEl.addActionListener(FormEvent.ONCHANGE);
		allowAudioRecordingsEl.setVisible(audioRecordingsEnabled);

		String maxAudioDuration = config.getStringValue(GTACourseNode.GTASK_MAX_AUDIO_DURATION, "600");
		maxAudioDurationEl = uifactory.addTextElement("av.max.audio.duration", "av.max.duration", 5, maxAudioDuration, configCont);
		maxAudioDurationEl.setRegexMatchCheck("\\d+", "av.max.duration.error");
		maxAudioDurationEl.setVisible(audioRecordingsEnabled && allowAudioRecordings);

		int minDocs = config.getIntegerSafe(GTACourseNode.GTASK_MIN_SUBMITTED_DOCS, -1);
		String minVal = "";
		if(minDocs > 0) {
			minVal = Integer.toString(minDocs);
		}
		minNumberOfDocsEl = uifactory.addTextElement("min.documents", "min.documents", 5, minVal, configCont);
		
		int maxDocs = config.getIntegerSafe(GTACourseNode.GTASK_MAX_SUBMITTED_DOCS, -1);
		String maxVal = "";
		if(maxDocs > 0) {
			maxVal = Integer.toString(maxDocs);
		}
		maxNumberOfDocsEl = uifactory.addTextElement("max.documents", "max.documents", 5, maxVal, configCont);

		//confirmation
		FormLayoutContainer confirmationCont = uifactory.addDefaultFormLayout("confirmation", null, formLayout);
		confirmationCont.setFormTitle(translate("confirmation.title"));
		confirmationCont.setFormInfo(translate("confirmation.hint"));
		
		emailConfirmationEl = uifactory.addCheckboxesHorizontal("confirmation", "submission.email.confirmation", confirmationCont, enableKeys, enableValues);
		emailConfirmationEl.addActionListener(FormEvent.ONCHANGE);
		boolean confirm = config.getBooleanSafe(GTACourseNode.GTASK_SUBMISSION_MAIL_CONFIRMATION);
		emailConfirmationEl.select(enableKeys[0], confirm);
		
		String text = config.getStringValue(GTACourseNode.GTASK_SUBMISSION_TEXT);
		if(!StringHelper.containsNonWhitespace(text)) {
			text = translate("submission.confirmation");
		}
		textEl = uifactory.addRichTextElementForStringDataMinimalistic("text", "submission.text", text, 10, -1, confirmationCont, getWindowControl());
		textEl.setMandatory(true);
		MailHelper.setVariableNamesAsHelp(textEl, GTAMailTemplate.variableNames(), getLocale());

		//save
		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, confirmationCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == allowVideoRecordingsEl || source == emailConfirmationEl || source == allowAudioRecordingsEl) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void updateUI() {
		boolean allowVideoRecordings = avModule.isVideoRecordingEnabled() && allowVideoRecordingsEl.isAtLeastSelected(1);
		maxVideoDurationEl.setVisible(allowVideoRecordings);
		videoQualityEl.setVisible(allowVideoRecordings);
		
		textEl.setVisible(emailConfirmationEl.isAtLeastSelected(1));

		boolean allowAudioRecordings = avModule.isAudioRecordingEnabled() && allowAudioRecordingsEl.isAtLeastSelected(1);
		maxAudioDurationEl.setVisible(allowAudioRecordings);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		embeddedEditorEl.clearError();
		externalEditorEl.clearError();
		if(!externalEditorEl.isAtLeastSelected(1) && !embeddedEditorEl.isAtLeastSelected(1)) {
			externalEditorEl.setErrorKey("error.editor.atLeastOne");
			embeddedEditorEl.setErrorKey("error.editor.atLeastOne");
			allOk &= false;
		}

		allOk &= validateNumberOfDocuments(minNumberOfDocsEl);
		allOk &= validateNumberOfDocuments(maxNumberOfDocsEl);
		
		if(allOk && StringHelper.isLong(minNumberOfDocsEl.getValue()) && StringHelper.isLong(maxNumberOfDocsEl.getValue())
				&& Long.parseLong(minNumberOfDocsEl.getValue()) > Long.parseLong(maxNumberOfDocsEl.getValue())) {
			maxNumberOfDocsEl.setErrorKey("error.max.smaller.than.min.documents");
			allOk &= false;	
		}
		return allOk;
	}
	
	private boolean validateNumberOfDocuments(TextElement numberEl) {
		boolean allOk = true;
		
		numberEl.clearError();
		String maxVal = numberEl.getValue();
		if(StringHelper.containsNonWhitespace(maxVal)) {
			try {
				int val = Integer.parseInt(maxVal);
				if(val <= 0 || val > 12) {
					numberEl.setErrorKey("error.number.format");
					allOk &= false;
				}
			} catch (NumberFormatException e) {
				//can happen
				allOk &= false;
				numberEl.setErrorKey("error.number.format");
			}
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean externalEditor = externalEditorEl.isAtLeastSelected(1);
		config.setBooleanEntry(GTACourseNode.GTASK_EXTERNAL_EDITOR, externalEditor);
		boolean embeddedEditor = embeddedEditorEl.isAtLeastSelected(1);
		config.setBooleanEntry(GTACourseNode.GTASK_EMBBEDED_EDITOR, embeddedEditor);
		
		boolean submissionTemplate = submissionTemplateEl.isAtLeastSelected(1);
		config.setBooleanEntry(GTACourseNode.GTASK_SUBMISSION_TEMPLATE, submissionTemplate);

		if (avModule.isVideoRecordingEnabled()) {
			boolean allowVideoRecordings = allowVideoRecordingsEl.isAtLeastSelected(1);
			config.setBooleanEntry(GTACourseNode.GTASK_ALLOW_VIDEO_RECORDINGS, allowVideoRecordings);
			if (allowVideoRecordings) {
				config.setStringValue(GTACourseNode.GTASK_MAX_VIDEO_DURATION, maxVideoDurationEl.getValue());
				config.setStringValue(GTACourseNode.GTASK_VIDEO_QUALITY, videoQualityEl.getSelectedKey());
			}
		}

		if (avModule.isAudioRecordingEnabled()) {
			boolean allowAudioRecordings = allowAudioRecordingsEl.isAtLeastSelected(1);
			config.setBooleanEntry(GTACourseNode.GTASK_ALLOW_AUDIO_RECORDINGS, allowAudioRecordings);
			if (allowAudioRecordings) {
				config.setStringValue(GTACourseNode.GTASK_MAX_AUDIO_DURATION, maxAudioDurationEl.getValue());
			}
		}

		setNumberOfdocuments(minNumberOfDocsEl, GTACourseNode.GTASK_MIN_SUBMITTED_DOCS);
		setNumberOfdocuments(maxNumberOfDocsEl, GTACourseNode.GTASK_MAX_SUBMITTED_DOCS);

		String text = textEl.getValue();
		boolean emailConfirmation = emailConfirmationEl.isAtLeastSelected(1);
		config.setBooleanEntry(GTACourseNode.GTASK_SUBMISSION_MAIL_CONFIRMATION, emailConfirmation);
		if(emailConfirmation && StringHelper.containsNonWhitespace(text)) {
			config.setStringValue(GTACourseNode.GTASK_SUBMISSION_TEXT, text);
		} else {
			config.remove(GTACourseNode.GTASK_SUBMISSION_TEXT);
		}
		
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private void setNumberOfdocuments(TextElement numberEl, String configKey) {
		String maxVal = numberEl.getValue();
		if(StringHelper.isLong(maxVal)) {
			try {
				int val = Integer.parseInt(maxVal);
				config.setIntValue(configKey, val);
			} catch (NumberFormatException e) {
				//can happen
			}
		} else {
			config.remove(configKey);
		}
	}
}

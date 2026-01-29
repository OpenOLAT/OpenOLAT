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
package org.olat.modules.audiovideorecording.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.audiovideorecording.AVModule;
import org.olat.modules.video.model.VideoTranscodingMode;
import org.olat.modules.video.ui.VideoAdminController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2022-10-24<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class RecordingAdminConfigController extends FormBasicController {
	private MultipleSelectionElement enableVideoRecordingEl;
	private MultipleSelectionElement enableAudioRecordingEl;
	
	private SingleSelection videoConversionModeEl;
	private TextElement videoConversionServiceUrlEl;
	
	private SingleSelection audioConversionModeEl;
	private TextElement audioConversionServiceUrlEl;

	@Autowired
	private AVModule avModule;

	public RecordingAdminConfigController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(VideoAdminController.class, getLocale(), getTranslator()));
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("tab.admin.recording.configuration");
		setFormContextHelp("manual_admin/administration/Modules_Audio_Video_Recording/");

		String[] enableKeys = new String[]{ "on" };
		String[] enableValues = new String[]{ translate("on") };

		enableVideoRecordingEl = uifactory.addCheckboxesHorizontal("admin.recording.enable.video", formLayout,
				enableKeys, enableValues);
		enableVideoRecordingEl.select("on", avModule.isVideoRecordingEnabled());
		enableVideoRecordingEl.addActionListener(FormEvent.ONCHANGE);

		enableAudioRecordingEl = uifactory.addCheckboxesHorizontal("admin.recording.enable.audio", formLayout,
				enableKeys, enableValues);
		enableAudioRecordingEl.select("on", avModule.isAudioRecordingEnabled());
		enableAudioRecordingEl.addActionListener(FormEvent.ONCHANGE);
		
		if (VideoTranscodingMode.remote.equals(avModule.getVideoConversionMode()) && VideoTranscodingMode.remote.equals(avModule.getAudioConversionMode())) {
			setFormInfo("admin.recording.external.conversion.active");

			uifactory.addStaticTextElement("video.conversion.remote.mode", "video.conversion.mode", translate("transcoding.mode.remote"), formLayout);
			uifactory.addStaticTextElement("audio.conversion.remote.mode", "audio.conversion.mode", translate("transcoding.mode.remote"), formLayout);
		} else {
			SelectionValues modeValues = modeValues();
			initVideoFormPart(formLayout, enableKeys, enableValues, modeValues);
			initAudioFormPart(formLayout, enableKeys, enableValues, modeValues);
		}
	}

	private void initVideoFormPart(FormItemContainer formLayout, String[] enableKeys, String[] enableValues, SelectionValues modeValues) {
		videoConversionModeEl = uifactory.addDropdownSingleselect("video.conversion.mode", formLayout,
				modeValues.keys(), modeValues.values(), null);
		videoConversionModeEl.addActionListener(FormEvent.ONCHANGE);

		videoConversionServiceUrlEl = uifactory.addTextElement("admin.recording.video.conversion.service.url",
				255, avModule.getVideoConversionServiceUrl(), formLayout);
		videoConversionServiceUrlEl.addActionListener(FormEvent.ONCHANGE);

		updateLocalVideoConversion();
	}

	private void initAudioFormPart(FormItemContainer formLayout, String[] enableKeys, String[] enableValues, SelectionValues modeValues) {
		audioConversionModeEl = uifactory.addDropdownSingleselect("audio.conversion.mode", formLayout,
				modeValues.keys(), modeValues.values(), null);
		audioConversionModeEl.addActionListener(FormEvent.ONCHANGE);

		audioConversionServiceUrlEl = uifactory.addTextElement("admin.recording.audio.conversion.service.url",
				255, avModule.getAudioConversionServiceUrl(), formLayout);
		audioConversionServiceUrlEl.addActionListener(FormEvent.ONCHANGE);

		updateLocalAudioConversion();
	}

	SelectionValues modeValues() {
		SelectionValues modeValues = new SelectionValues();
		
		modeValues.add(SelectionValues.entry(VideoTranscodingMode.disabled.name(), translate(VideoTranscodingMode.disabled.getI18nKey())));
		modeValues.add(SelectionValues.entry(VideoTranscodingMode.local.name(), translate(VideoTranscodingMode.local.getI18nKey())));
		modeValues.add(SelectionValues.entry(VideoTranscodingMode.service.name(), translate(VideoTranscodingMode.service.getI18nKey())));
		
		return modeValues;
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == enableVideoRecordingEl) {
			avModule.setVideoRecordingEnabled(enableVideoRecordingEl.isSelected(0));
		} else if (source == enableAudioRecordingEl) {
			avModule.setAudioRecordingEnabled(enableAudioRecordingEl.isSelected(0));
		} else if (source == videoConversionServiceUrlEl) {
			avModule.setVideoConversionServiceUrl(videoConversionServiceUrlEl.getValue());
			updateLocalVideoConversion();
		} else if (source == videoConversionModeEl) {
			avModule.setVideoConversionMode(VideoTranscodingMode.valueOf(videoConversionModeEl.getSelectedKey()));
			updateLocalVideoConversion();
		} else if (source == audioConversionServiceUrlEl) {
			avModule.setAudioConversionServiceUrl(audioConversionServiceUrlEl.getValue());
			updateLocalAudioConversion();
		} else if (source == audioConversionModeEl) {
			avModule.setAudioConversionMode(VideoTranscodingMode.valueOf(audioConversionModeEl.getSelectedKey()));
			updateLocalAudioConversion();
		}
	}

	private void updateLocalVideoConversion() {
		VideoTranscodingMode mode = avModule.getVideoConversionMode();
		videoConversionModeEl.select(mode.name(), true);

		videoConversionModeEl.clearWarning();
		if (VideoTranscodingMode.local.equals(mode) && !avModule.isLocalVideoConversionPossible()) {
			videoConversionModeEl.setWarningKey("admin.recording.enable.local.video.conversion.warning");
		}
		
		videoConversionServiceUrlEl.setVisible(VideoTranscodingMode.service.equals(mode));
		if (videoConversionServiceUrlEl.isVisible()) {
			videoConversionServiceUrlEl.clearError();
			if (!StringHelper.containsNonWhitespace(videoConversionServiceUrlEl.getValue())) {
				videoConversionServiceUrlEl.setErrorKey("form.legende.mandatory");
			}
		}
	}
	
	private void updateLocalAudioConversion() {
		VideoTranscodingMode mode = avModule.getAudioConversionMode();
		audioConversionModeEl.select(mode.name(), true);

		audioConversionModeEl.clearWarning();
		if (VideoTranscodingMode.local.equals(mode) && !avModule.isLocalAudioConversionPossible()) {
			audioConversionModeEl.setWarningKey("admin.recording.enable.local.audio.conversion.warning");
		}

		audioConversionServiceUrlEl.setVisible(VideoTranscodingMode.service.equals(mode));
		if (audioConversionServiceUrlEl.isVisible()) {
			audioConversionServiceUrlEl.clearError();
			if (!StringHelper.containsNonWhitespace(audioConversionServiceUrlEl.getValue())) {
				audioConversionServiceUrlEl.setErrorKey("form.legende.mandatory");
			}
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
	}
}

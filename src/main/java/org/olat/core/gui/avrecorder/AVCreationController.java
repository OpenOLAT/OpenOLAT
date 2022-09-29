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
package org.olat.core.gui.avrecorder;

import org.apache.logging.log4j.Logger;
import org.olat.core.dispatcher.impl.StaticMediaDispatcher;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.AVRecording;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.AVRecordingImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.winmgr.JSCommand;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Initial date: 2022-08-10<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class AVCreationController extends FormBasicController {

	private static final Logger log = Tracing.createLoggerFor(AVCreationController.class);

	private final AVConfiguration config;

	private AVRecording avRecording;
	private SingleSelection qualityDropdown;
	private FormSubmit confirmButton;

	public AVCreationController(UserRequest ureq, WindowControl wControl,
								AVConfiguration config) {
		super(ureq, wControl, "avRecorder");

		this.config = config;

		initForm(ureq);
	}

	public File getRecordedFile() {
		if (avRecording == null) {
			return null;
		}
		return avRecording.getRecordedFile();
	}

	public String getRecordedFileName() {
		if (avRecording == null) {
			return null;
		}
		return avRecording.getRecordedFileName();
	}

	public VFSLeaf moveUploadFileTo(VFSContainer destinationContainer) {
		return avRecording.moveUploadFileTo(destinationContainer);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

		initContext();

		List<String> jsPath = new ArrayList<>();
		List<String> cssPath = new ArrayList<>();
		jsPath.add("js/recordrtc/custom/av-common.js");
		if (config.getMode() == AVConfiguration.Mode.audio) {
			jsPath.add("js/recordrtc/custom/audio-recorder.js");
			flc.contextPut("isAudio", true);
		} else {
			jsPath.add("js/recordrtc/custom/video-recorder.js");
			flc.contextPut("isAudio", false);
		}
		if (Settings.isDebuging()) {
			jsPath.add("js/recordrtc/image-capture/imagecapture.js");
			jsPath.add("js/recordrtc/RecordRTC.js");
			cssPath.add(StaticMediaDispatcher.getStaticURI("movie/mediaelementjs/mediaelementplayer.css"));
		} else {
			jsPath.add("js/recordrtc/image-capture/imagecapture.min.js");
			jsPath.add("js/recordrtc/RecordRTC.min.js");
			cssPath.add(StaticMediaDispatcher.getStaticURI("movie/mediaelementjs/mediaelementplayer.min.css"));
		}
		JSAndCSSComponent avRecorderJs = new JSAndCSSComponent("avRecorder",
				jsPath.toArray(String[]::new),
				cssPath.toArray(String[]::new));

		flc.put("avRecorderJs", avRecorderJs);

		String[] dropdownKeys = new String[] {
				AVQualtiy.low.name(),
				AVQualtiy.medium.name(),
				AVQualtiy.high.name()
		};

		String[] dropdownOptions = new String[] {
				translate(AVQualtiy.low.getTextKey()),
				translate(AVQualtiy.medium.getTextKey()),
				translate(AVQualtiy.high.getTextKey())
		};

		flc.contextPut("firstName", getIdentity().getUser().getFirstName().replaceAll("[\\W_]", "-"));
		flc.contextPut("lastName", getIdentity().getUser().getLastName().replaceAll("[\\W_]", "-"));

		String[] qualities = Arrays.stream(AVQualtiy.values()).map(AVQualtiy::toJson).toArray(String[]::new);
		flc.contextPut("qualities", qualities);

		flc.contextPut("recordingLengthLimit", config.getRecordingLengthLimit());
		flc.contextPut("generatePosterImage", config.isGeneratePosterImage());

		qualityDropdown = uifactory.addDropdownSingleselect("video.audio.quality", formLayout,
				dropdownKeys, dropdownOptions, null);
		qualityDropdown.select(config.getQuality().name(), true);

		avRecording = new AVRecordingImpl(getIdentity(), "avRecording", "posterImage");
		formLayout.add(avRecording);

		confirmButton = new FormSubmit("confirmButton", "confirmButton");
		confirmButton.setElementCssClass("o_av_confirm_button");
		formLayout.add(confirmButton);
	}

	private void initContext() {
		flc.contextPut("videoBitsPerSecond", config.getVideoBitsPerSecond());
		flc.contextPut("audioBitsPerSecond", config.getAudioBitsPerSecond());
		flc.contextPut("idealHeight", config.getIdealHeight());
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// Avoid resetting the JS VideoRecorder instance.
		flc.setDirty(false);

		confirmButton.setEnabled(false);

		fireEvent(ureq, new AVCreationEvent());
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		super.formInnerEvent(ureq, source, event);

		if (qualityDropdown == source) {
			flc.setDirty(false);
		}
	}

	@Override
	protected void doDispose() {
		if (config.getMode() == AVConfiguration.Mode.video) {
			JSCommand cmd = new JSCommand("videoRecorder.dispose();");
			getWindowControl().getWindowBackOffice().sendCommandTo(cmd);
		}
		if (config.getMode() == AVConfiguration.Mode.audio) {
			JSCommand cmd = new JSCommand("audioRecorder.dispose();");
			getWindowControl().getWindowBackOffice().sendCommandTo(cmd);
		}

		super.doDispose();
	}
}
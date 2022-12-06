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
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.audiovideorecording.AVModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2022-10-24<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class RecordingAdminConfigController extends FormBasicController {
	private MultipleSelectionElement enableVideoRecordingEl;
	// TODO OO-6508
	// private MultipleSelectionElement enableAudioRecordingEl;
	private MultipleSelectionElement enableLocalTranscodingEl;

	@Autowired
	private AVModule avModule;

	public RecordingAdminConfigController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("tab.admin.recording.configuration");

		String[] enableKeys = new String[]{ "on" };
		String[] enableValues = new String[]{ translate("on") };

		enableVideoRecordingEl = uifactory.addCheckboxesHorizontal("admin.recording.enable.video", formLayout,
				enableKeys, enableValues);
		enableVideoRecordingEl.select("on", avModule.isVideoRecordingEnabled());
		enableVideoRecordingEl.addActionListener(FormEvent.ONCHANGE);

		// TODO OO-6508
		// enableAudioRecordingEl = uifactory.addCheckboxesHorizontal("admin.recording.enable.audio", formLayout,
		//				enableKeys, enableValues);
		// enableAudioRecordingEl.select("on", avModule.isAudioRecordingEnabled());
		// enableAudioRecordingEl.addActionListener(FormEvent.ONCHANGE);

		enableLocalTranscodingEl = uifactory.addCheckboxesHorizontal("admin.recording.enable.local.transcoding",
				formLayout, enableKeys, enableValues);
		enableLocalTranscodingEl.select("on", avModule.isLocalTranscodingEnabled());
		enableLocalTranscodingEl.addActionListener(FormEvent.ONCHANGE);
		enableLocalTranscodingEl.setHelpTextKey("admin.recording.enable.local.transcoding.info", null);
		if (!avModule.isLocalTranscodingPossible()) {
			enableLocalTranscodingEl.setWarningKey("admin.recording.enable.local.transcoding.warning");
			if (!avModule.isLocalTranscodingEnabled()) {
				enableLocalTranscodingEl.setEnabled(false);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == enableVideoRecordingEl) {
			avModule.setVideoRecordingEnabled(enableVideoRecordingEl.isSelected(0));
		// TODO OO-6508
  		// } else if (source == enableAudioRecordingEl) {
		//		avModule.setAudioRecordingEnabled(enableAudioRecordingEl.isSelected(0));
		} else if (source == enableLocalTranscodingEl) {
			avModule.setLocalTranscodingEnabled(enableLocalTranscodingEl.isSelected(0));
			if (!avModule.isLocalTranscodingPossible() && !avModule.isLocalTranscodingEnabled()) {
				enableLocalTranscodingEl.setEnabled(false);
			}
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
	}
}

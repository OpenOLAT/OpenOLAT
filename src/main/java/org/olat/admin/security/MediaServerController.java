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
package org.olat.admin.security;

import java.util.Arrays;

import org.olat.basesecurity.MediaServerMode;
import org.olat.basesecurity.MediaServerModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2024-09-27<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class MediaServerController extends FormBasicController {

	private SingleSelection modeEl;
	private MultipleSelectionElement mediaServersEl;

	@Autowired
	private MediaServerModule mediaServerModule;

	public MediaServerController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_BAREBONE);

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer topCont = FormLayoutContainer.createDefaultFormLayout("topCont", getTranslator());
		formLayout.add(topCont);
		topCont.setFormTitle(translate("media.server.title"));

		FormLayoutContainer middleCont = FormLayoutContainer.createDefaultFormLayout("middleCont", getTranslator());
		formLayout.add(middleCont);

		SelectionValues modeKV = new SelectionValues();
		Arrays.stream(MediaServerMode.values())
				.map(MediaServerMode::name)
				.map(key -> new SelectionValues.SelectionValue(key, translate("media.server.mode." + key)))
				.forEach(modeKV::add);

		modeEl = uifactory.addRadiosVertical("media.server.mode", middleCont, modeKV.keys(), modeKV.values());
		modeEl.addActionListener(FormEvent.ONCHANGE);
		MediaServerMode mode = mediaServerModule.getMediaServerMode();
		if (mode != null) {
			modeEl.select(mode.name(), true);
		} else {
			modeEl.select(MediaServerMode.allowAll.name(), true);
		}

		SelectionValues mediaServersKV = new SelectionValues();
		mediaServersKV.add(SelectionValues.entry(MediaServerModule.YOUTUBE_KEY, MediaServerModule.YOUTUBE_NAME));
		mediaServersKV.add(SelectionValues.entry(MediaServerModule.VIMEO_KEY, MediaServerModule.VIMEO_NAME));
		mediaServersEl = uifactory.addCheckboxesVertical("media.servers", middleCont, mediaServersKV.keys(),
				mediaServersKV.values(), 1);

		FormLayoutContainer buttonsCont = FormLayoutContainer.createDefaultFormLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);

		updateUI();
	}

	private void updateUI() {
		boolean mediaServersVisible = modeEl.isOneSelected() && MediaServerMode.configure.name().equals(modeEl.getSelectedKey());
		mediaServersEl.setVisible(mediaServersVisible);
		if (mediaServersEl.isVisible()) {
			mediaServersEl.select(MediaServerModule.YOUTUBE_KEY, mediaServerModule.isMediaServerEnabled(MediaServerModule.YOUTUBE_KEY));
			mediaServersEl.select(MediaServerModule.VIMEO_KEY, mediaServerModule.isMediaServerEnabled(MediaServerModule.VIMEO_KEY));
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (modeEl == source) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (modeEl.isOneSelected()) {
			MediaServerMode mode = MediaServerMode.valueOf(modeEl.getSelectedKey());
			mediaServerModule.setMediaServerMode(mode);
		}
		if (MediaServerMode.configure.equals(mediaServerModule.getMediaServerMode())) {
			mediaServerModule.setMediaServerEnabled(MediaServerModule.YOUTUBE_KEY, mediaServersEl.isKeySelected(MediaServerModule.YOUTUBE_KEY));
			mediaServerModule.setMediaServerEnabled(MediaServerModule.VIMEO_KEY, mediaServersEl.isKeySelected(MediaServerModule.VIMEO_KEY));
		}
	}
}

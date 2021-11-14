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
package org.olat.modules.video.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoMeta;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 28.06.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class VideoDownloadSettingsController extends FormBasicController {

	private MultipleSelectionElement allowDownloadEl;
	
	private VideoMeta videoMeta; 
	
	@Autowired
	private VideoManager videoManager;
	
	public VideoDownloadSettingsController(UserRequest ureq, WindowControl wControl, VideoMeta videoMeta) {
		super(ureq, wControl);
		
		this.videoMeta = videoMeta;
		
		initForm(ureq);
		loadData();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("tab.video.downloadConfig");
		
		SelectionValues options = new SelectionValues(SelectionValues.entry(DownloadOptions.allowed.name(), translate("download.options.allowed")));
		allowDownloadEl = uifactory.addCheckboxesVertical("download.options", formLayout, options.keys(), options.values(), 1);
		
		FormLayoutContainer buttonContainer = FormLayoutContainer.createButtonLayout("buttonContainer", getTranslator());
		formLayout.add("buttonContainer", buttonContainer);
		uifactory.addFormSubmitButton("submit", buttonContainer);		
	}
	
	private void loadData() {
		allowDownloadEl.select(DownloadOptions.allowed.name(), videoMeta.isDownloadEnabled());
	}

	@Override
	protected void formOK(UserRequest ureq) {
		videoMeta.setDownloadEnabled(allowDownloadEl.isKeySelected(DownloadOptions.allowed.name()));
		videoManager.updateVideoMetadata(videoMeta);
	}
	
	private enum DownloadOptions {
		allowed
	}
}

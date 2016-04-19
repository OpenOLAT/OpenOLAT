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

import java.text.DateFormat;

import org.apache.commons.io.FileUtils;
import org.olat.core.commons.services.image.Size;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.video.manager.VideoManager;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;


public class VideoMetaDataEditFormController extends FormBasicController {

	protected FormUIFactory uifactory = FormUIFactory.getInstance();
	@Autowired
	private VideoManager videoManager;
	private OLATResource videoResource;
	private StaticTextElement heightField, widthField;
	private RichTextElement descriptionField;


	public VideoMetaDataEditFormController(UserRequest ureq, WindowControl wControl, OLATResource re) {
		super(ureq, wControl);
		videoResource = re;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener,
			UserRequest ureq) {
		setFormTitle("tab.video.metaDataConfig");

		Size videoSize;
		videoSize = videoManager.getVideoSize(videoResource);
		if(videoSize == null){
			videoSize = new Size(0, 0, false);
		}

		widthField = uifactory.addStaticTextElement("video.config.width", String.valueOf(videoSize.getWidth()), formLayout);
		heightField = uifactory.addStaticTextElement("video.config.height", String.valueOf(videoSize.getHeight()), formLayout);
		uifactory.addStaticTextElement("video.config.creationDate",DateFormat.getDateInstance().format(videoResource.getCreationDate()), formLayout);
		uifactory.addStaticTextElement("video.config.fileSize", FileUtils.byteCountToDisplaySize(videoManager.getVideoFile(videoResource).length()), formLayout);
		descriptionField = uifactory.addRichTextElementForStringDataMinimalistic("description", "video.config.description", videoManager.getDescription(videoResource), -1, -1, formLayout, getWindowControl());
		uifactory.addFormSubmitButton("submit", formLayout);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		videoManager.setVideoSize(videoResource, new Size(Integer.parseInt(widthField.getValue()), Integer.parseInt(heightField.getValue()), true));
		videoManager.setDescription(videoResource, descriptionField.getValue());
	}

	@Override
	protected void doDispose() {

	}

}

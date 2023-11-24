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
package org.olat.modules.cemedia.ui.medias;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaHandler;
import org.olat.modules.cemedia.MediaLog;
import org.olat.modules.cemedia.MediaService;
import org.olat.modules.cemedia.MediaVersionMetadata;
import org.olat.modules.cemedia.manager.MediaDAO;
import org.olat.modules.cemedia.ui.MediaCenterController;
import org.olat.modules.video.VideoFormat;
import org.olat.modules.video.ui.VideoAdminController;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-11-21<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class NewVideoViaUrlController extends FormBasicController {

	private TextElement urlEl;

	private Media media;
	private MediaHandler handler;

	@Autowired
	private DB dbInstance;
	@Autowired
	private MediaService mediaService;
	@Autowired
	private MediaDAO mediaDAO;

	public NewVideoViaUrlController(UserRequest ureq, WindowControl wControl, Media media, MediaHandler handler) {
		super(ureq, wControl, Util.createPackageTranslator(MediaCenterController.class, ureq.getLocale(),
				Util.createPackageTranslator(VideoAdminController.class, ureq.getLocale())));
		this.media = media;
		this.handler = handler;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormInfo("add.video.via.url.info");

		urlEl = uifactory.addTextElement("artefact.url", 512, "", formLayout);
		urlEl.setMandatory(true);

		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		uifactory.addFormSubmitButton("upload.version." + handler.getType(), buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		urlEl.clearError();
		if( !StringHelper.containsNonWhitespace(urlEl.getValue())) {
			urlEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		} else if (VideoFormat.valueOfUrl(urlEl.getValue()) == null) {
			urlEl.setErrorKey("error.format.not.supported");
			allOk &= false;
		}

		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String url = urlEl.getValue();
		media = mediaService.getMediaByKey(media.getKey());
		MediaVersionMetadata mediaVersionMetadata = mediaDAO.createVersionMetadata();
		mediaVersionMetadata.setUrl(url);
		mediaDAO.update(mediaVersionMetadata);
		media = mediaService.addVersion(media, getIdentity(), mediaVersionMetadata, MediaLog.Action.UPLOAD);
		dbInstance.commit();
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}

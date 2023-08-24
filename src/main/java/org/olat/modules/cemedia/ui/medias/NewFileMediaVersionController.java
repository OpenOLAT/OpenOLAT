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

import java.io.File;
import java.util.Set;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaHandler;
import org.olat.modules.cemedia.MediaLog;
import org.olat.modules.cemedia.MediaService;
import org.olat.modules.cemedia.ui.MediaCenterController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 16 juin 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class NewFileMediaVersionController extends FormBasicController {

	private FileElement fileEl;
	
	private Media media;
	private final boolean withPreview;
	private final MediaHandler handler;
	private final Set<String> mimeTypes;
	private final long maxFileSizeKB;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private MediaService mediaService;
	
	public NewFileMediaVersionController(UserRequest ureq, WindowControl wControl, Media media,
			MediaHandler handler, Set<String> mimeTypes, long maxFileSizeKB, boolean withPreview) {
		super(ureq, wControl, Util.createPackageTranslator(MediaCenterController.class, ureq.getLocale()));
		this.media = media;
		this.handler = handler;
		this.mimeTypes = mimeTypes;
		this.withPreview = withPreview;
		this.maxFileSizeKB = maxFileSizeKB;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		fileEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "artefact.file", "artefact.file", formLayout);
		fileEl.setMandatory(true);
		if(mimeTypes != null && !mimeTypes.isEmpty()) {
			fileEl.limitToMimeType(mimeTypes, "error.mimetype", new String[]{ mimeTypes.toString() });
		}
		fileEl.addActionListener(FormEvent.ONCHANGE);
		fileEl.setMaxUploadSizeKB(maxFileSizeKB, null, null);
		if(withPreview) {
			fileEl.setPreview(ureq.getUserSession(), true);
		}
		
		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		uifactory.addFormSubmitButton("upload.version." + handler.getType(), buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		fileEl.clearError();
		if(fileEl.getInitialFile() == null && (fileEl.getUploadFile() == null || fileEl.getUploadSize() < 1)) {
			fileEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		} else {
			allOk &= validateFormItem(ureq, fileEl);
		}

		return allOk;
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(fileEl == source) {
			fileEl.clearWarning();
			if(mediaService.isInMediaCenter(getIdentity(), fileEl.getUploadFile())) {
				fileEl.setWarningKey("warning.checksum.file");
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		File file = fileEl.getUploadFile();
		String filename = fileEl.getUploadFileName();
		media = mediaService.getMediaByKey(media.getKey());
		mediaService.addVersion(media, file, filename, getIdentity(), MediaLog.Action.UPLOAD);
		dbInstance.commit();
		fireEvent(ureq, Event.DONE_EVENT);
	}
}

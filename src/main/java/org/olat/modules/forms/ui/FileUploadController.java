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
package org.olat.modules.forms.ui;

import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.CodeHelper;
import org.olat.modules.forms.model.xml.FileUpload;

/**
 * 
 * Initial date: 02.02.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class FileUploadController extends FormBasicController {

	private FileElement fileEl;
	
	private final FileUpload fileUpload;
	
	public FileUploadController(UserRequest ureq, WindowControl wControl, FileUpload fileUpload) {
		super(ureq, wControl, "file_upload");
		this.fileUpload = fileUpload;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		long postfix = CodeHelper.getRAMUniqueID();
		fileEl = uifactory.addFileElement(getWindowControl(), "file_upload_" + postfix, "", formLayout);
		fileEl.setButtonsEnabled(false);
		fileEl.setDeleteEnabled(true);
		fileEl.setMaxUploadSizeKB(fileUpload.getMaxUploadSizeKB(), "file.upload.error.limit.exeeded", null);
		Set<String> mimeTypes = MimeTypeSetFactory.getMimeTypes(fileUpload.getMimeTypeSetKey());
		fileEl.limitToMimeType(mimeTypes, "file.upload.error.mime.type.wrong", null);
		fileEl.setHelpTextKey("ul.select.fhelp", null);

		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.getFormItemComponent().contextPut("postfix", Long.toString(postfix));
		}
	}
	
	public void update() {
		fileEl.setMaxUploadSizeKB(fileUpload.getMaxUploadSizeKB(), null, null);
		Set<String> mimeTypes = MimeTypeSetFactory.getMimeTypes(fileUpload.getMimeTypeSetKey());
		fileEl.limitToMimeType(mimeTypes, null, null);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}

}

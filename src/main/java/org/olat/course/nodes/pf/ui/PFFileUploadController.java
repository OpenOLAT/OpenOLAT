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
package org.olat.course.nodes.pf.ui;

import java.io.File;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
/**
*
* @author Fabian Kiefer, fabian.kiefer@frentix.com, http://www.frentix.com
*
*/
public class PFFileUploadController extends FormBasicController {
	
	private FileElement uploadFileEl;
	
	private File uploadFile;
	private String uploadFileName;
	private final boolean uploadToAll;

	public PFFileUploadController(UserRequest ureq, WindowControl wControl, boolean uploadToall) {
		super(ureq, wControl);
		this.uploadToAll = uploadToall;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		uploadFileEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "upload", "textfield.upload", formLayout);
		uploadFileEl.addActionListener(FormEvent.ONCHANGE);
		
		FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonGroupLayout);
		uifactory.addFormCancelButton("cancel", buttonGroupLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("submit", "upload.link", buttonGroupLayout);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (uploadFileEl.isUploadSuccess()) {
			uploadFile = uploadFileEl.getUploadFile();
			uploadFileName = uploadFileEl.getUploadFileName();
			fireEvent(ureq, Event.DONE_EVENT);	
		}
	}
	
	protected File getUpLoadFile () {
		return uploadFile;
	}
	
	protected String getUploadFileName () {
		return uploadFileName;
	}
	
	protected boolean isUploadToAll () {
		return uploadToAll;
	}
}

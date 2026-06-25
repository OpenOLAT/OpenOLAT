/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.assessment.ui.mode;

import java.io.File;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.FileUtils;
import org.olat.core.util.xml.PList;

/**
 * 
 * Initial date: 24 juin 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class SafeExamBrowserConfigUploadController extends FormBasicController {

	private FileElement uploadEl;
	
	public SafeExamBrowserConfigUploadController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		initForm(ureq);
	}
	
	protected String loadPList() {
		File file = uploadEl.getUploadFile();
		return FileUtils.load(file, "UTF-8");
	}
	
	protected String getFilename() {
		return uploadEl.getUploadFileName();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		uploadEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "seb.template.file", "seb.template.file", formLayout);
		uploadEl.setMultiFileUpload(false);
		
		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		uifactory.addFormSubmitButton("upload", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		uploadEl.clearError();
		if(uploadEl.getUploadFile() == null) {
			uploadEl.setErrorKey("form.mandatory.hover");
			allOk &= false;
		} else if(uploadEl.getUploadFile() != null && !validateFile(uploadEl.getUploadFile())) {
			uploadEl.setErrorKey("error.safe.exam.config.format");
			allOk &= false;
		}
		
		return allOk;
	}
	
	private boolean validateFile(File file) {
		boolean allOk = false;
		
		try {
			String xml = FileUtils.load(file, "UTF-8");
			PList plist = PList.valueOf(xml);
			return plist.getRootDict() != null;
		} catch (Exception e) {
			getLogger().warn("Cannot read a configuration file");
		}

		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}

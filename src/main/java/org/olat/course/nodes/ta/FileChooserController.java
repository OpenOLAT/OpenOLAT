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
package org.olat.course.nodes.ta;

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
 * @author srosse, 
 *
 */
public class FileChooserController extends FormBasicController {
	
	private FileElement fileEl;
	private long maxUploadSizeKb;
	
	public FileChooserController(UserRequest ureq, WindowControl wControl, long maxUploadSizeKb) {
		super(ureq, wControl);
		this.maxUploadSizeKb = maxUploadSizeKb;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_course_gta_upload_task_form");
		
		fileEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "file", "dropbox.upload", formLayout);
		fileEl.setMaxUploadSizeKB(maxUploadSizeKb, "error.limit.exceeded", new String[]{ Long.toString(maxUploadSizeKb) });
		fileEl.setMandatory(true);
		fileEl.addActionListener(FormEvent.ONCHANGE);
		
		FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonCont.setRootForm(mainForm);
		formLayout.add(buttonCont);
		uifactory.addFormSubmitButton("save", buttonCont);
		uifactory.addFormCancelButton("cancel", buttonCont, ureq, getWindowControl());
	}

	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		fileEl.clearError();
		if(fileEl.getInitialFile() == null && fileEl.getUploadFile() == null) {
			fileEl.setErrorKey("form.mandatory.hover", null);
			allOk &= false;
		}
		
		return allOk & super.validateFormLogic(ureq);
	}
	
	public String getUploadFileName() {
		return fileEl.getUploadFileName();
	}
	
	public File getUploadFile() {
		return fileEl.getUploadFile();
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

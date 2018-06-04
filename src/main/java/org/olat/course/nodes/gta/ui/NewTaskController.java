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
package org.olat.course.nodes.gta.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.nodes.gta.model.TaskDefinition;

/**
 * 
 * Initial date: 02.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class NewTaskController extends FormBasicController {
	
	private TextElement filenameEl, titleEl, descriptionEl;
	private final VFSContainer documentContainer;
	
	public NewTaskController(UserRequest ureq, WindowControl wControl, VFSContainer documentContainer) {
		super(ureq, wControl);
		this.documentContainer = documentContainer;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_course_gta_new_task_form");

		titleEl = uifactory.addTextElement("title", "task.title", 128, "", formLayout);
		titleEl.setElementCssClass("o_sel_course_gta_upload_task_title");
		titleEl.setMandatory(true);
		
		descriptionEl = uifactory.addTextAreaElement("descr", "task.description", 2048, 10, -1, true, false, "", formLayout);

		filenameEl = uifactory.addTextElement("fileName", "file.name", -1, "", formLayout);
		filenameEl.setElementCssClass("o_sel_course_gta_doc_filename");
		filenameEl.setExampleKey("file.name.example", null);
		filenameEl.setDisplaySize(20);
		filenameEl.setMandatory(true);
		
		FormLayoutContainer formButtons = FormLayoutContainer.createButtonLayout("formButton", getTranslator());
		formLayout.add(formButtons);
		uifactory.addFormSubmitButton("submit", "create", formButtons);
		uifactory.addFormCancelButton("cancel", formButtons, ureq, getWindowControl());
		
		String jsPage = velocity_root + "/new_task_js.html";
		FormLayoutContainer jsCont = FormLayoutContainer.createCustomFormLayout("js", getTranslator(), jsPage);
		jsCont.contextPut("titleId", titleEl.getFormDispatchId());
		jsCont.contextPut("filenameId", filenameEl.getFormDispatchId());
		formLayout.add(jsCont);
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	public String getFilename() {
		String value = filenameEl.getValue();
		String lowerCased = value.toLowerCase();
		if(!lowerCased.endsWith(".xhtm")
				&& !lowerCased.endsWith(".html")
				&& !lowerCased.endsWith(".htm")) {
			value += ".html";
		}
		return value;
	}
	
	public TaskDefinition getTaskDefinition() {
		TaskDefinition taskDef = new TaskDefinition();
		taskDef.setTitle(titleEl.getValue());
		taskDef.setFilename(getFilename());
		taskDef.setDescription(descriptionEl.getValue());
		return taskDef; 
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		filenameEl.clearError();
		String val = filenameEl.getValue();
		if(!StringHelper.containsNonWhitespace(val)) {
			filenameEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else {
			String filename = getFilename();
			if(documentContainer.resolve(filename) != null) {
				filenameEl.setErrorKey("error.file.exists", new String[]{filename});
				allOk &= false;
			} else if (!FileUtils.validateFilename(filename)) {
				filenameEl.setErrorKey("error.file.invalid", null);
				allOk &= false;
			}
		}
		
		titleEl.clearError();
		if(!StringHelper.containsNonWhitespace(titleEl.getValue())) {
			titleEl.setErrorKey("form.mandatory.hover", null);
			allOk &= false;
		}

		return allOk & super.validateFormLogic(ureq);
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

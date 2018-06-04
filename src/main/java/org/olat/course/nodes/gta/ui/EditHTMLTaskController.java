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

import org.olat.core.commons.editor.htmleditor.HTMLEditorController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.nodes.gta.model.TaskDefinition;

/**
 * 
 * Initial date: 01.09.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditHTMLTaskController extends FormBasicController {
	
	private TextElement titleEl, descriptionEl;
	private HTMLEditorController contentEditor;
	
	private final TaskDefinition task;
	private final VFSContainer taskContainer;
	
	private final String filenameToReplace;
	
	public EditHTMLTaskController(UserRequest ureq, WindowControl wControl,
			TaskDefinition task, VFSContainer taskContainer) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.task = task;
		this.filenameToReplace = task != null ? task.getFilename() : null;
		this.taskContainer = taskContainer;
		initForm(ureq);
	}
	
	public TaskDefinition getTask() {
		return task;
	}
	
	public String getFilenameToReplace() {
		return filenameToReplace;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_course_gta_edit_task_form");
		
		String title = task.getTitle() == null ? "" : task.getTitle();
		titleEl = uifactory.addTextElement("title", "task.title", 128, title, formLayout);
		titleEl.setElementCssClass("o_sel_course_gta_upload_task_title");
		titleEl.setMandatory(true);
		
		String description = task.getDescription() == null ? "" : task.getDescription();
		descriptionEl = uifactory.addTextAreaElement("descr", "task.description", 2048, 10, -1, true, false, description, formLayout);

		contentEditor = new HTMLEditorController(ureq, getWindowControl(), taskContainer, task.getFilename(), null, "media", true, false, false, mainForm);
		contentEditor.getRichTextConfiguration().disableMedia();
		contentEditor.getRichTextConfiguration().setAllowCustomMediaFactory(false);
		listenTo(contentEditor);
		FormItem editorItem = contentEditor.getInitialFormItem();
		editorItem.setLabel("task.file", null);
		formLayout.add(editorItem);

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
		
		titleEl.clearError();
		if(!StringHelper.containsNonWhitespace(titleEl.getValue())) {
			titleEl.setErrorKey("form.mandatory.hover", null);
			allOk &= false;
		}
		
		return allOk & super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(contentEditor.doSaveData()) {
			task.setTitle(titleEl.getValue());
			task.setDescription(descriptionEl.getValue());
			fireEvent(ureq, Event.DONE_EVENT);
		}
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
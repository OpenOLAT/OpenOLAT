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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.gta.model.TaskDefinition;

/**
 *
 * Initial date: 24.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditTaskController extends FormBasicController {

	private TextElement titleEl, descriptionEl;
	private FileElement fileEl;

	private final boolean replaceFile;
	private final TaskDefinition task;
	private final File taskContainer;

	private final String filenameToReplace;
	private final List<TaskDefinition> currentDefinitions;

	public EditTaskController(UserRequest ureq, WindowControl wControl, File taskContainer,
			List<TaskDefinition> currentDefinitions) {
		this(ureq, wControl, new TaskDefinition(), taskContainer, currentDefinitions, false);
	}

	public EditTaskController(UserRequest ureq, WindowControl wControl, TaskDefinition task, File taskContainer,
			List<TaskDefinition> currentDefinitions) {
		this(ureq, wControl, task, taskContainer, currentDefinitions, true);
	}

	public EditTaskController(UserRequest ureq, WindowControl wControl,
			TaskDefinition task, File taskContainer,
			List<TaskDefinition> currentDefinitions, boolean replaceFile) {
		super(ureq, wControl);
		this.replaceFile = replaceFile;
		this.task = task;
		this.filenameToReplace = task != null ? task.getFilename() : null;
		this.taskContainer = taskContainer;
		this.currentDefinitions = currentDefinitions;
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
		formLayout.setElementCssClass("o_sel_course_gta_upload_task_form");

		String title = task.getTitle() == null ? "" : task.getTitle();
		titleEl = uifactory.addTextElement("title", "task.title", 128, title, formLayout);
		titleEl.setElementCssClass("o_sel_course_gta_upload_task_title");
		titleEl.setMandatory(true);

		String description = task.getDescription() == null ? "" : task.getDescription();
		descriptionEl = uifactory.addTextAreaElement("descr", "task.description", 2048, 10, -1, true, false, description, formLayout);

		fileEl = uifactory.addFileElement(getWindowControl(), "file", "task.file", formLayout);
		fileEl.setMandatory(true);
		fileEl.addActionListener(FormEvent.ONCHANGE);
		if(StringHelper.containsNonWhitespace(task.getFilename())) {
			File currentFile = new File(taskContainer, task.getFilename());
			if(currentFile.exists()) {
				fileEl.setInitialFile(currentFile);
			}
		}

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

		fileEl.clearError();
		if(fileEl.getInitialFile() == null && fileEl.getUploadFile() == null) {
			fileEl.setErrorKey("form.mandatory.hover", null);
			allOk &= false;
		} else if (fileEl.getUploadFile() != null && !FileUtils.validateFilename(fileEl.getUploadFileName())) {
			fileEl.setErrorKey("error.file.invalid", null);
			allOk = false;
		} else if(!replaceFile && fileEl.getUploadFile() != null) {
			String filename = fileEl.getUploadFileName();
			File target = new File(taskContainer, filename);
			if(target.exists()) {
				fileEl.setErrorKey("error.file.exists", new String[]{ filename });
				allOk &= false;
			}
		} else if(replaceFile && fileEl.getUploadFile() != null) {
			String filename = fileEl.getUploadFileName();
			if(currentDefinitions != null) {
				for(TaskDefinition definition:currentDefinitions) {
					if(filename.equals(definition.getFilename()) && !task.getTitle().equals(definition.getTitle())) {
						fileEl.setErrorKey("error.file.exists", new String[]{ filename });
						allOk &= false;
					}
				}
			}
		}

		return allOk & super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		task.setTitle(titleEl.getValue());
		task.setDescription(descriptionEl.getValue());

		if(fileEl.getUploadFile() != null) {
			if(replaceFile && StringHelper.containsNonWhitespace(task.getFilename())) {
				int usage = 0;
				if(currentDefinitions != null) {
					for(TaskDefinition definition:currentDefinitions) {
						if(task.getFilename().equals(definition.getFilename())) {
							usage++;
						}
					}
				}

				if(usage == 1) {
					File currentFile = new File(taskContainer, task.getFilename());
					if(currentFile.exists()) {
						FileUtils.deleteFile(currentFile);
					}
				}
			}

			String filename = fileEl.getUploadFileName();
			task.setFilename(filename);
			try {
				Path upload = fileEl.getUploadFile().toPath();
				File target = new File(taskContainer, filename);
				Files.move(upload, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch(Exception ex) {
				logError("", ex);
			}
		}

		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
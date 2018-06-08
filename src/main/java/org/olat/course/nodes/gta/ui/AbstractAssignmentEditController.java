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
import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.editor.htmleditor.HTMLEditorController;
import org.olat.core.commons.editor.htmleditor.WysiwygFactory;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DownloadLink;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.model.TaskDefinition;
import org.olat.course.nodes.gta.ui.TaskDefinitionTableModel.TDCols;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
abstract class AbstractAssignmentEditController extends FormBasicController {

	private FormLink addTaskLink, createTaskLink;
	private FlexiTableElement taskDefTableEl;
	private TaskDefinitionTableModel taskModel;
	private WarningFlexiCellRenderer fileExistsRenderer;
	
	private CloseableModalController cmc;
	private NewTaskController newTaskCtrl;
	private DialogBoxController confirmDeleteCtrl;
	private HTMLEditorController newTaskEditorCtrl;
	private EditHTMLTaskController editTaskEditorCtrl;
	private EditTaskController addTaskCtrl, editTaskCtrl;
	
	private final File tasksFolder;
	protected final boolean readOnly;
	private final VFSContainer tasksContainer;
	protected final GTACourseNode gtaNode;
	protected final CourseEnvironment courseEnv;
	protected final ModuleConfiguration config;
	
	private int linkCounter = 0;
	
	@Autowired
	protected GTAManager gtaManager;
	@Autowired
	protected NotificationsManager notificationsManager;
	
	public AbstractAssignmentEditController(UserRequest ureq, WindowControl wControl,
			GTACourseNode gtaNode, ModuleConfiguration config, CourseEnvironment courseEnv, boolean readOnly) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.config = config;
		this.gtaNode = gtaNode;
		this.readOnly = readOnly;
		this.courseEnv = courseEnv;
		tasksFolder = gtaManager.getTasksDirectory(courseEnv, gtaNode);
		tasksContainer = gtaManager.getTasksContainer(courseEnv, gtaNode);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//tasks
		String tasksPage = velocity_root + "/edit_task_list.html";
		FormLayoutContainer tasksCont = FormLayoutContainer.createCustomFormLayout("tasks", getTranslator(), tasksPage);
		tasksCont.setRootForm(mainForm);
		formLayout.add(tasksCont);
		
		addTaskLink = uifactory.addFormLink("add.task", tasksCont, Link.BUTTON);
		addTaskLink.setElementCssClass("o_sel_course_gta_add_task");
		addTaskLink.setIconLeftCSS("o_icon o_icon_upload");
		addTaskLink.setVisible(!readOnly);
		createTaskLink = uifactory.addFormLink("create.task", tasksCont, Link.BUTTON);
		createTaskLink.setElementCssClass("o_sel_course_gta_create_task");
		createTaskLink.setIconLeftCSS("o_icon o_icon_edit");
		createTaskLink.setVisible(!readOnly);
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TDCols.title.i18nKey(), TDCols.title.ordinal()));
		fileExistsRenderer = new WarningFlexiCellRenderer();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TDCols.file.i18nKey(), TDCols.file.ordinal(), fileExistsRenderer));
		if(!readOnly) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.edit", TDCols.edit.ordinal(), "edit",
					new BooleanCellRenderer(
							new StaticFlexiCellRenderer(translate("edit"), "edit"),
							new StaticFlexiCellRenderer(translate("replace"), "edit"))));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.edit", translate("delete"), "delete"));
		}
		
		taskModel = new TaskDefinitionTableModel(columnsModel);
		taskDefTableEl = uifactory.addTableElement(getWindowControl(), "taskTable", taskModel, getTranslator(), tasksCont);
		taskDefTableEl.setExportEnabled(true);
		updateModel();
	}
	
	protected void updateModel() {
		fileExistsRenderer.setFilenames(tasksFolder.list());
		List<TaskDefinition> taskDefinitions = gtaManager.getTaskDefinitions(courseEnv, gtaNode);
		List<TaskDefinitionRow> rows = new ArrayList<>(taskDefinitions.size());
		for(TaskDefinition def:taskDefinitions) {
			DownloadLink downloadLink = null;
			VFSItem item = tasksContainer.resolve(def.getFilename());
			if(item instanceof VFSLeaf) {
				downloadLink = uifactory
					.addDownloadLink("file_" + (++linkCounter), def.getFilename(), null, (VFSLeaf)item, taskDefTableEl);
			}
			rows.add(new TaskDefinitionRow(def, downloadLink));
		}
		taskModel.setObjects(rows);
		taskDefTableEl.reset();
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(addTaskCtrl == source) {
			if(event == Event.DONE_EVENT) {
				TaskDefinition newTask = addTaskCtrl.getTask();
				gtaManager.addTaskDefinition(newTask, courseEnv, gtaNode);
				fireEvent(ureq, Event.DONE_EVENT);
				updateModel();
				gtaManager.markNews(courseEnv, gtaNode);
			}
			cmc.deactivate();
			cleanUp();
			//fireEvent(ureq, Event.DONE_EVENT);
		} else if(editTaskCtrl == source) {
			if(event == Event.DONE_EVENT) {
				doFinishReplacementOfTask(editTaskCtrl.getFilenameToReplace(), editTaskCtrl.getTask());
				updateModel();
				//fireEvent(ureq, Event.DONE_EVENT);
				gtaManager.markNews(courseEnv, gtaNode);
			}
			cmc.deactivate();
			cleanUp();
		} else if(newTaskCtrl == source) {
			TaskDefinition newTask = newTaskCtrl.getTaskDefinition();
			cmc.deactivate();
			cleanUp();
			
			if(event == Event.DONE_EVENT) {
				gtaManager.addTaskDefinition(newTask, courseEnv, gtaNode);
				doCreateTaskEditor(ureq, newTask);
				updateModel();
			} 
		} else if(newTaskEditorCtrl == source) {
			if(event == Event.DONE_EVENT) {
				updateModel();
				//fireEvent(ureq, Event.DONE_EVENT);
				gtaManager.markNews(courseEnv, gtaNode);
			}
			cmc.deactivate();
			cleanUp();
		} else if(editTaskEditorCtrl == source) {
			if(event == Event.DONE_EVENT) {
				gtaManager.updateTaskDefinition(null, editTaskEditorCtrl.getTask(), courseEnv, gtaNode);
				updateModel();
				//fireEvent(ureq, Event.DONE_EVENT);
				gtaManager.markNews(courseEnv, gtaNode);
			}
			cmc.deactivate();
			cleanUp();
		} else if(confirmDeleteCtrl == source) {
			if(DialogBoxUIFactory.isOkEvent(event) || DialogBoxUIFactory.isYesEvent(event)) {
				TaskDefinition row = (TaskDefinition)confirmDeleteCtrl.getUserObject();
				doDelete(ureq, row);
			}
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmDeleteCtrl);
		removeAsListenerAndDispose(editTaskCtrl);
		removeAsListenerAndDispose(addTaskCtrl);
		removeAsListenerAndDispose(cmc);
		confirmDeleteCtrl = null;
		editTaskCtrl = null;
		addTaskCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addTaskLink == source) {
			doAddTask(ureq);
		} else if(createTaskLink == source) {
			doCreateTask(ureq);
		} else if(taskDefTableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				TaskDefinitionRow row = taskModel.getObject(se.getIndex());
				if("edit".equals(se.getCommand())) {
					doEdit(ureq, row.getTaskDefinition());
				} else if("delete".equals(se.getCommand())) {
					if(gtaManager.isTaskInProcess(courseEnv.getCourseGroupManager().getCourseEntry(), gtaNode, row.getTaskDefinition().getFilename())) {
						doConfirmDelete(ureq, row.getTaskDefinition());
					} else {
						doDelete(ureq, row.getTaskDefinition());
					}
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doAddTask(UserRequest ureq) {
		List<TaskDefinition> currentDefinitions = gtaManager.getTaskDefinitions(courseEnv, gtaNode);
		addTaskCtrl = new EditTaskController(ureq, getWindowControl(), tasksFolder, currentDefinitions);
		listenTo(addTaskCtrl);

		String title = translate("add.task");
		cmc = new CloseableModalController(getWindowControl(), null, addTaskCtrl.getInitialComponent(), true, title, false);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doEdit(UserRequest ureq, TaskDefinition taskDef) {
		if(taskDef.getFilename().endsWith(".html")) {
			doEditTaskEditor(ureq, taskDef);
		} else {
			doReplaceTask(ureq, taskDef);
		}	
	}
	
	private void doReplaceTask(UserRequest ureq, TaskDefinition taskDef) {
		List<TaskDefinition> currentDefinitions = gtaManager.getTaskDefinitions(courseEnv, gtaNode);
		editTaskCtrl = new EditTaskController(ureq, getWindowControl(), taskDef, tasksFolder, currentDefinitions);
		listenTo(editTaskCtrl);

		String title = translate("edit.task");
		cmc = new CloseableModalController(getWindowControl(), null, editTaskCtrl.getInitialComponent(), true, title, false);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doFinishReplacementOfTask(String replacedFilename, TaskDefinition taskDef) {
		TaskList list = gtaManager.getTaskList(courseEnv.getCourseGroupManager().getCourseEntry(), gtaNode);
		if(list != null) {
			gtaManager.updateTaskName(list, replacedFilename, taskDef.getFilename());
		}
		gtaManager.updateTaskDefinition(replacedFilename, taskDef, courseEnv, gtaNode);
	}
	
	private void doCreateTask(UserRequest ureq) {
		newTaskCtrl = new NewTaskController(ureq, getWindowControl(), tasksContainer);
		listenTo(newTaskCtrl);

		String title = translate("create.task");
		cmc = new CloseableModalController(getWindowControl(), "close", newTaskCtrl.getInitialComponent(), true, title, false);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doCreateTaskEditor(UserRequest ureq, TaskDefinition taskDef) {
		String documentName = taskDef.getFilename();
		VFSItem item = tasksContainer.resolve(documentName);
		if(item == null) {
			tasksContainer.createChildLeaf(documentName);
		} else {
			documentName = VFSManager.rename(tasksContainer, documentName);
			tasksContainer.createChildLeaf(documentName);
		}

		newTaskEditorCtrl = WysiwygFactory.createWysiwygController(ureq, getWindowControl(),
				tasksContainer, documentName, "media", true, true);
		newTaskEditorCtrl.getRichTextConfiguration().disableMedia();
		newTaskEditorCtrl.getRichTextConfiguration().setAllowCustomMediaFactory(false);
		newTaskEditorCtrl.setNewFile(true);
		newTaskEditorCtrl.setUserObject(taskDef);
		listenTo(newTaskEditorCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", newTaskEditorCtrl.getInitialComponent());
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doEditTaskEditor(UserRequest ureq, TaskDefinition taskDef) {
		VFSItem htmlDocument = tasksContainer.resolve(taskDef.getFilename());
		if(htmlDocument == null || !(htmlDocument instanceof VFSLeaf)) {
			showError("error.missing.file");
		} else {
			editTaskEditorCtrl = new EditHTMLTaskController(ureq, getWindowControl(), taskDef, tasksContainer);
			listenTo(editTaskEditorCtrl);
			
			cmc = new CloseableModalController(getWindowControl(), "close", editTaskEditorCtrl.getInitialComponent());
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	private void doConfirmDelete(UserRequest ureq, TaskDefinition row) {
		String title = translate("warning.tasks.in.process.delete.title");
		String text = translate("warning.tasks.in.process.delete.text");
		confirmDeleteCtrl = activateOkCancelDialog(ureq, title, text, confirmDeleteCtrl);
		confirmDeleteCtrl.setUserObject(row);
	}
	
	private void doDelete(UserRequest ureq, TaskDefinition taskDef) {
		gtaManager.removeTaskDefinition(taskDef, courseEnv, gtaNode);
		updateModel();
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	protected static class WarningFlexiCellRenderer implements FlexiCellRenderer {
		
		private String[] tasks;

		public void setFilenames(String[] tasks) {
			this.tasks = tasks;
		}

		@Override
		public void render(Renderer renderer, StringOutput target, Object cellValue, int row,
				FlexiTableComponent source, URLBuilder ubu, Translator translator) {
			
			if(cellValue instanceof String) {
				String filename = (String)cellValue;
				boolean found = false;
				
				if(tasks != null && tasks.length > 0) {
					for(String task:tasks) {
						if(task.equals(filename)) {
							found = true;
							break;
						}
					}
				}
				
				if(!found) {
					target.append("<i class='o_icon o_icon_warn'> </i> ");
				}
				StringHelper.escapeHtml(target, filename);
			}
		}
	}
}
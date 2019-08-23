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

import static org.olat.core.commons.services.doceditor.DocEditor.Mode.EDIT;
import static org.olat.course.nodes.gta.ui.GTAUIFactory.getOpenMode;
import static org.olat.course.nodes.gta.ui.GTAUIFactory.htmlOffice;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorSecurityCallback;
import org.olat.core.commons.services.doceditor.DocEditorSecurityCallbackBuilder;
import org.olat.core.commons.services.doceditor.ui.DocEditorFullscreenController;
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
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.model.TaskDefinition;
import org.olat.course.nodes.gta.ui.TaskDefinitionTableModel.TDCols;
import org.olat.course.nodes.gta.ui.component.ModeCellRenderer;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
abstract class AbstractAssignmentEditController extends FormBasicController implements Activateable2 {

	private FormLink addMultipleTasksLink, addTaskLink, createTaskLink;
	private FlexiTableElement taskDefTableEl;
	private TaskDefinitionTableModel taskModel;
	private WarningFlexiCellRenderer fileExistsRenderer;
	
	private CloseableModalController cmc;
	private NewTaskController newTaskCtrl;
	private DialogBoxController confirmDeleteCtrl;
	private EditTaskController addTaskCtrl, editTaskCtrl;
	private BulkUploadTasksController addMultipleTasksCtrl;
	private DocEditorFullscreenController docEditorCtrl;
	
	private final File tasksFolder;
	protected final boolean readOnly;
	private final VFSContainer tasksContainer;
	protected final GTACourseNode gtaNode;
	protected final CourseEnvironment courseEnv;
	protected final ModuleConfiguration config;
	private final Long courseRepoKey;
	
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
		courseRepoKey = courseEnv.getCourseGroupManager().getCourseEntry().getKey();
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//tasks
		String tasksPage = velocity_root + "/edit_task_list.html";
		FormLayoutContainer tasksCont = FormLayoutContainer.createCustomFormLayout("tasks", getTranslator(), tasksPage);
		tasksCont.setRootForm(mainForm);
		formLayout.add(tasksCont);

		addMultipleTasksLink = uifactory.addFormLink("add.multipleTasks", tasksCont, Link.BUTTON);
		addMultipleTasksLink.setElementCssClass("o_sel_course_gta_add_multipleTasks");
		addMultipleTasksLink.setIconLeftCSS("o_icon o_icon_upload");
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
		
		String openI18n = readOnly? "table.header.view": "table.header.edit";
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(openI18n, TDCols.mode.ordinal(), "open", new ModeCellRenderer("open")));
		if(!readOnly) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.metadata", translate("table.header.metadata"), "metadata"));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.delete", translate("table.header.delete"), "delete"));
		}
		
		taskModel = new TaskDefinitionTableModel(columnsModel);
		taskDefTableEl = uifactory.addTableElement(getWindowControl(), "taskTable", taskModel, getTranslator(), tasksCont);
		taskDefTableEl.setExportEnabled(true);
		updateModel(ureq);
	}
	
	protected void updateModel(UserRequest ureq) {
		fileExistsRenderer.setFilenames(tasksFolder.list());
		List<TaskDefinition> taskDefinitions = gtaManager.getTaskDefinitions(courseEnv, gtaNode);
		List<TaskDefinitionRow> rows = new ArrayList<>(taskDefinitions.size());
		for(TaskDefinition def:taskDefinitions) {
			DownloadLink downloadLink = null;
			Mode mode = null;
			VFSItem item = tasksContainer.resolve(def.getFilename());
			if(item instanceof VFSLeaf) {
				VFSLeaf vfsLeaf = (VFSLeaf)item;
				downloadLink = uifactory
					.addDownloadLink("file_" + (++linkCounter), def.getFilename(), null, vfsLeaf, taskDefTableEl);
				mode = getOpenMode(getIdentity(), ureq.getUserSession().getRoles(), vfsLeaf, readOnly);
			}
			
			TaskDefinitionRow row = new TaskDefinitionRow(def, downloadLink, mode);
			rows.add(row);
		}
		taskModel.setObjects(rows);
		taskDefTableEl.reset();
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if((entries == null || entries.isEmpty()) && docEditorCtrl != null) {
			cleanUp();
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(addTaskCtrl == source) {
			if(event == Event.DONE_EVENT) {
				TaskDefinition newTask = addTaskCtrl.getTask();
				gtaManager.addTaskDefinition(newTask, courseEnv, gtaNode);
				fireEvent(ureq, Event.DONE_EVENT);
				updateModel(ureq);
				gtaManager.markNews(courseEnv, gtaNode);
			}
			cmc.deactivate();
			cleanUp();
			//fireEvent(ureq, Event.DONE_EVENT);
		} else if (addMultipleTasksCtrl == source) {
			if (event == Event.DONE_EVENT) {
				List<TaskDefinition> newTaskList = addMultipleTasksCtrl.getTaskList();
				for(TaskDefinition newTask:newTaskList){
					gtaManager.addTaskDefinition(newTask, courseEnv, gtaNode);
				}
				fireEvent(ureq, Event.DONE_EVENT);
				updateModel();
				notificationsManager.markPublisherNews(subscriptionContext, null, false);
			}
			cmc.deactivate();
			cleanUp();
		} else if(editTaskCtrl == source) {
			if(event == Event.DONE_EVENT) {
				doFinishReplacementOfTask(editTaskCtrl.getFilenameToReplace(), editTaskCtrl.getTask());
				updateModel(ureq);
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
				doOpen(ureq, newTask, EDIT);
				updateModel(ureq);
			} 
		} else if (source == docEditorCtrl) {
			if(event == Event.DONE_EVENT) {
				gtaManager.markNews(courseEnv, gtaNode);
				updateModel(ureq);
				cleanUp();
				addToHistory(ureq, this);
			}
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
		removeAsListenerAndDispose(docEditorCtrl);
		removeAsListenerAndDispose(editTaskCtrl);
		removeAsListenerAndDispose(addTaskCtrl);
		removeAsListenerAndDispose(addMultipleTasksCtrl);
		removeAsListenerAndDispose(cmc);
		confirmDeleteCtrl = null;
		docEditorCtrl = null;
		editTaskCtrl = null;
		addTaskCtrl = null;
		addMultipleTasksCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addMultipleTasksLink == source){
			doAddMultipleTasks(ureq);
		} else if(addTaskLink == source) {
			doAddTask(ureq);
		} else if(createTaskLink == source) {
			doCreateTask(ureq);
		} else if(taskDefTableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				TaskDefinitionRow row = taskModel.getObject(se.getIndex());
				if("open".equals(se.getCommand())) {
					doOpen(ureq, row.getTaskDefinition(), row.getMode());
				} else if("metadata".equals(se.getCommand())) {
					doEditMetadata(ureq, row.getTaskDefinition());
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

	private void doAddMultipleTasks(UserRequest ureq){
		addMultipleTasksCtrl = new BulkUploadTasksController(ureq, getWindowControl(), tasksFolder);
		listenTo(addMultipleTasksCtrl);

		String title = translate("add.multipleTasks");
		cmc = new CloseableModalController(getWindowControl(), null, addMultipleTasksCtrl.getInitialComponent(), true, title, false);
		listenTo(cmc);
		cmc.activate();
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

	private void doEditMetadata(UserRequest ureq, TaskDefinition taskDef) {
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
		newTaskCtrl = new NewTaskController(ureq, getWindowControl(), tasksContainer,
				htmlOffice(getIdentity(), ureq.getUserSession().getRoles(), getLocale()));
		listenTo(newTaskCtrl);

		String title = translate("create.task");
		cmc = new CloseableModalController(getWindowControl(), "close", newTaskCtrl.getInitialComponent(), true, title, false);
		listenTo(cmc);
		cmc.activate();
	}

	private void doOpen(UserRequest ureq, TaskDefinition taskDef, Mode mode) {
		VFSItem vfsItem = tasksContainer.resolve(taskDef.getFilename());
		if(!(vfsItem instanceof VFSLeaf)) {
			showError("error.missing.file");
		} else {
			DocEditorSecurityCallback secCallback = DocEditorSecurityCallbackBuilder.builder()
					.withMode(mode)
					.build();
			DocEditorConfigs configs = GTAUIFactory.getEditorConfig(tasksContainer, taskDef.getFilename(), courseRepoKey);
			WindowControl swb = addToHistory(ureq, OresHelper.createOLATResourceableType("DocEditor"), null);
			docEditorCtrl = new DocEditorFullscreenController(ureq, swb, (VFSLeaf)vfsItem, secCallback, configs);
			listenTo(docEditorCtrl);
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
		updateModel(ureq);
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

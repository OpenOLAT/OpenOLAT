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
import java.util.Optional;

import org.olat.core.commons.services.doceditor.DocEditor;
import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.commons.services.doceditor.ui.DocEditorController;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.vfs.VFSTranscodingService;
import org.olat.core.commons.services.vfs.manager.VFSTranscodingDoneEvent;
import org.olat.core.commons.services.video.ui.VideoAudioPlayerController;
import org.olat.core.commons.services.video.viewer.VideoAudioPlayer;
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
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.winmgr.CommandFactory;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Roles;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.io.SystemFilenameFilter;
import org.olat.core.util.vfs.VFSConstants;
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
import org.olat.modules.audiovideorecording.AVModule;
import org.springframework.beans.factory.annotation.Autowired;

import static org.olat.course.nodes.gta.ui.GTAUIFactory.htmlOffice;

/**
 * 
 * Initial date: 24.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
abstract class AbstractAssignmentEditController extends FormBasicController implements Activateable2, GenericEventListener {

	private FormLink addTaskLink;
	private FormLink createTaskLink;

	private FormLink createVideoAssignmentLink;
	private FormLink createAudioAssignmentLink;
	private FlexiTableElement taskDefTableEl;
	private TaskDefinitionTableModel taskModel;
	private WarningFlexiCellRenderer fileExistsRenderer;
	
	private CloseableModalController cmc;
	private NewTaskController newTaskCtrl;
	private AVTaskController avTaskCtrl;
	private EditTaskController addTaskCtrl;
	private EditTaskController editTaskCtrl;
	private DialogBoxController confirmDeleteCtrl;
	private CloseableCalloutWindowController ccwc;
	private AVConvertingMenuController avConvertingMenuCtrl;
	private VideoAudioPlayerController videoAudioPlayerController;
	
	private final File tasksFolder;
	protected final boolean readOnly;
	private final VFSContainer tasksContainer;
	protected final GTACourseNode gtaNode;
	protected final CourseEnvironment courseEnv;
	protected final ModuleConfiguration config;
	private final Long courseRepoKey;
	private Roles roles;

	private int linkCounter = 0;
	
	@Autowired
	protected GTAManager gtaManager;
	@Autowired
	protected NotificationsManager notificationsManager;
	@Autowired
	private DocEditorService docEditorService;
	@Autowired
	private AVModule avModule;
	
	public AbstractAssignmentEditController(UserRequest ureq, WindowControl wControl,
			GTACourseNode gtaNode, ModuleConfiguration config, CourseEnvironment courseEnv, boolean readOnly) {
		super(ureq, wControl, LAYOUT_BAREBONE, Util.createPackageTranslator(DocEditorController.class, ureq.getLocale()));
		this.config = config;
		this.gtaNode = gtaNode;
		this.readOnly = readOnly;
		this.courseEnv = courseEnv;
		tasksFolder = gtaManager.getTasksDirectory(courseEnv, gtaNode);
		tasksContainer = gtaManager.getTasksContainer(courseEnv, gtaNode);
		courseRepoKey = courseEnv.getCourseGroupManager().getCourseEntry().getKey();
		setTranslator(getTranslator());
		initForm(ureq);

		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, null, VFSTranscodingService.ores);
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
		createVideoAssignmentLink = uifactory.addFormLink("av.create.video.assignment", tasksCont, Link.BUTTON);
		createVideoAssignmentLink.setElementCssClass("o_sel_course_gta_create_video_assignment");
		createVideoAssignmentLink.setIconLeftCSS("o_icon o_icon_video_record");
		createVideoAssignmentLink.setVisible(!readOnly && avModule.isVideoRecordingEnabled());
		createAudioAssignmentLink = uifactory.addFormLink("av.create.audio.assignment", tasksCont, Link.BUTTON);
		createAudioAssignmentLink.setElementCssClass("o_sel_course_gta_create_audio_assignment");
		createAudioAssignmentLink.setIconLeftCSS("o_icon o_icon_audio_record");
		createAudioAssignmentLink.setVisible(!readOnly && avModule.isAudioRecordingEnabled());

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TDCols.title.i18nKey(), TDCols.title.ordinal()));
		fileExistsRenderer = new WarningFlexiCellRenderer();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TDCols.file.i18nKey(), TDCols.file.ordinal(), fileExistsRenderer));
		
		String openI18n = "table.header.view";
		DefaultFlexiColumnModel openColumn = new DefaultFlexiColumnModel(openI18n, TDCols.mode.ordinal(), "open", new ModeCellRenderer("open", docEditorService));
		openColumn.setExportable(false);
		columnsModel.addFlexiColumnModel(openColumn);
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
		if (ureq != null) {
			roles = ureq.getUserSession().getRoles();
		}
		fileExistsRenderer.setFilenames(tasksFolder.list());
		List<TaskDefinition> taskDefinitions = getTaskDefinitions();
		List<TaskDefinitionRow> rows = new ArrayList<>(taskDefinitions.size());
		for(TaskDefinition def:taskDefinitions) {
			DownloadLink downloadLink = null;
			Mode mode = Mode.VIEW;
			VFSItem item = tasksContainer.resolve(def.getFilename());
			boolean inTranscoding = item != null && item.canMeta() == VFSConstants.YES && item.getMetaInfo() != null &&
					item.getMetaInfo().isInTranscoding();
			def.setInTranscoding(inTranscoding);
			if(item instanceof VFSLeaf) {
				VFSLeaf vfsLeaf = (VFSLeaf)item;
				downloadLink = def.isInTranscoding() ? null : uifactory
						.addDownloadLink("file_" + (++linkCounter), def.getFilename(), null, vfsLeaf, taskDefTableEl);
				mode = roles != null ? GTAUIFactory.getOpenMode(getIdentity(), roles, vfsLeaf, readOnly) : null;
			}
			TaskDefinitionRow row = new TaskDefinitionRow(def, downloadLink, mode);
			rows.add(row);
		}
		
		taskModel.setObjects(rows);
		taskDefTableEl.reset();
	}
	
	private List<TaskDefinition> getTaskDefinitions() {
		List<TaskDefinition> taskDefinitions = gtaManager.getTaskDefinitions(courseEnv, gtaNode);
		// load ghost files which are not in task definitions
		String[] taskFiles = tasksFolder.list(SystemFilenameFilter.FILES_ONLY);
		if(taskFiles != null) {
			for(String taskFile:taskFiles) {
				boolean found = false;
				for(TaskDefinition taskDefinition:taskDefinitions) {
					if(taskFile.equalsIgnoreCase(taskDefinition.getFilename())) {
						found = true;
					}
				}
				if(!found) {
					TaskDefinition ghostTask = TaskDefinition.fromFile(taskFile);
					gtaManager.addTaskDefinition(ghostTask, courseEnv, gtaNode);
					taskDefinitions.add(ghostTask);
				}
			}
		}
				
		return taskDefinitions;
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if((entries == null || entries.isEmpty())) {
			cleanUp();
		}
	}

	@Override
	public void event(Event event) {
		if (event instanceof VFSTranscodingDoneEvent) {
			VFSTranscodingDoneEvent doneEvent = (VFSTranscodingDoneEvent) event;
			if (taskModel.getObjects().stream().anyMatch(
					t -> doneEvent.getFileName().equals(t.getTaskDefinition().getFilename())
			)) {
				updateModel(null);
			}
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
				fireEvent(ureq, Event.DONE_EVENT);
				updateModel(ureq);
			} 
		} else if(confirmDeleteCtrl == source) {
			if(DialogBoxUIFactory.isOkEvent(event) || DialogBoxUIFactory.isYesEvent(event)) {
				TaskDefinition row = (TaskDefinition)confirmDeleteCtrl.getUserObject();
				doDelete(ureq, row);
			}
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		} else if (avTaskCtrl == source) {
			if (event == Event.DONE_EVENT) {
				gtaManager.addTaskDefinition(avTaskCtrl.getTask(), courseEnv, gtaNode);
				fireEvent(ureq, Event.DONE_EVENT);
				updateModel(ureq);
				gtaManager.markNews(courseEnv, gtaNode);
			}
			cmc.deactivate();
			cleanUp();
		} else if (ccwc == source) {
			cleanUp();
		} else if (avConvertingMenuCtrl == source) {
			if (event == AVConvertingMenuController.PLAY_MASTER_EVENT) {
				TaskDefinition taskDefinition = (TaskDefinition) avConvertingMenuCtrl.getUserObject();
				ccwc.deactivate();
				cleanUp();
				doPlayMaster(ureq, taskDefinition);
			}
		} else if (videoAudioPlayerController == source) {
			cmc.deactivate();
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmDeleteCtrl);
		removeAsListenerAndDispose(editTaskCtrl);
		removeAsListenerAndDispose(addTaskCtrl);
		removeAsListenerAndDispose(avTaskCtrl);
		removeAsListenerAndDispose(avConvertingMenuCtrl);
		removeAsListenerAndDispose(videoAudioPlayerController);
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(ccwc);
		confirmDeleteCtrl = null;
		editTaskCtrl = null;
		addTaskCtrl = null;
		avTaskCtrl = null;
		avConvertingMenuCtrl = null;
		videoAudioPlayerController = null;
		cmc = null;
		ccwc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addTaskLink == source) {
			doAddTask(ureq);
		} else if(createTaskLink == source) {
			doCreateTask(ureq);
		} else if(createVideoAssignmentLink == source) {
			doCreateVideoAsssignment(ureq);
		} else if(createAudioAssignmentLink == source) {
			doCreateAudioAssignment(ureq);
		} else if(taskDefTableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				TaskDefinitionRow row = taskModel.getObject(se.getIndex());
				if("metadata".equals(se.getCommand()) && !row.getTaskDefinition().isInTranscoding()) {
					doEditMetadata(ureq, row.getTaskDefinition());
				} else if("delete".equals(se.getCommand()) && !row.getTaskDefinition().isInTranscoding()) {
					if(gtaManager.isTaskInProcess(courseEnv.getCourseGroupManager().getCourseEntry(), gtaNode, row.getTaskDefinition().getFilename())) {
						doConfirmDelete(ureq, row.getTaskDefinition());
					} else {
						doDelete(ureq, row.getTaskDefinition());
					}
				} else if("open".equals(se.getCommand())) {
					doOpen(ureq, se.getIndex(),	row.getTaskDefinition(), row.getMode());
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
		cmc = new CloseableModalController(getWindowControl(), translate("close"), newTaskCtrl.getInitialComponent(), true, title, false);
		listenTo(cmc);
		cmc.activate();
	}

	private void doCreateVideoAsssignment(UserRequest ureq) {
		List<TaskDefinition> existingDefinitions = gtaManager.getTaskDefinitions(courseEnv, gtaNode);
		avTaskCtrl = new AVTaskController(ureq, getWindowControl(), tasksContainer, existingDefinitions, false);
		listenTo(avTaskCtrl);

		String title = translate("av.create.video.assignment");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), avTaskCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doCreateAudioAssignment(UserRequest ureq) {
		List<TaskDefinition> existingDefinitions = gtaManager.getTaskDefinitions(courseEnv, gtaNode);
		avTaskCtrl = new AVTaskController(ureq, getWindowControl(), tasksContainer, existingDefinitions, true);
		listenTo(avTaskCtrl);

		String title = translate("av.create.audio.assignment");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), avTaskCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doOpen(UserRequest ureq, int rowIndex, TaskDefinition taskDef, Mode mode) {
		if (taskDef.isInTranscoding()) {
			avConvertingMenuCtrl = new AVConvertingMenuController(ureq, getWindowControl(), taskDef);
			listenTo(avConvertingMenuCtrl);
			String targetDomId = ModeCellRenderer.CONVERTING_LINK_PREFIX + rowIndex;
			ccwc = new CloseableCalloutWindowController(ureq, getWindowControl(),
					avConvertingMenuCtrl.getInitialComponent(), targetDomId, "", true, "");
			listenTo(ccwc);
			ccwc.activate();
			return;
		}
		String suffix = FileUtils.getFileSuffix(taskDef.getFilename());
		Optional<DocEditor> videoAudioPlayer = docEditorService.getEditor(VideoAudioPlayer.TYPE);
		if (videoAudioPlayer.isPresent() && (videoAudioPlayer.get().isSupportingFormat(suffix, mode, false))) {
			doOpenMediaInModalController(ureq, taskDef, mode);
		} else {
			doOpenMediaInNewWindow(ureq, taskDef, mode);
		}
	}

	private void doPlayMaster(UserRequest ureq, TaskDefinition taskDef) {
		doOpenMediaInModalController(ureq, taskDef, Mode.VIEW);
	}

	private void doOpenMediaInModalController(UserRequest ureq, TaskDefinition taskDef, Mode mode) {
		VFSItem vfsItem = tasksContainer.resolve(taskDef.getFilename());
		if (!(vfsItem instanceof VFSLeaf)) {
			showError("error.missing.file");
		} else {
			DocEditorConfigs configs = GTAUIFactory.getEditorConfig(tasksContainer, (VFSLeaf)vfsItem,
					taskDef.getFilename(), mode, courseRepoKey);
			videoAudioPlayerController = new VideoAudioPlayerController(ureq, getWindowControl(), configs, null);
			String title = translate("av.play");
			cmc = new CloseableModalController(getWindowControl(), translate("close"),
					videoAudioPlayerController.getInitialComponent(), true, title, true);
			listenTo(cmc);
			cmc.activate();
		}
	}

	private void doOpenMediaInNewWindow(UserRequest ureq, TaskDefinition taskDef, Mode mode) {
		VFSItem vfsItem = tasksContainer.resolve(taskDef.getFilename());
		if (!(vfsItem instanceof VFSLeaf)) {
			showError("error.missing.file");
		} else {
			DocEditorConfigs configs = GTAUIFactory.getEditorConfig(tasksContainer, (VFSLeaf)vfsItem,
					taskDef.getFilename(), mode, courseRepoKey);
			String url = docEditorService.prepareDocumentUrl(ureq.getUserSession(), configs);
			getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowRedirectTo(url));
		}
	}

	private void doConfirmDelete(UserRequest ureq, TaskDefinition row) {
		String title = translate("warning.tasks.in.process.delete.title");
		String text = translate("warning.tasks.in.process.delete.text");
		confirmDeleteCtrl = activateOkCancelDialog(ureq, title, text, confirmDeleteCtrl);
		confirmDeleteCtrl.setUserObject(row);
		confirmDeleteCtrl.setCssClass("o_warning");
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

	@Override
	protected void doDispose() {
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, VFSTranscodingService.ores);
		super.doDispose();
	}
}
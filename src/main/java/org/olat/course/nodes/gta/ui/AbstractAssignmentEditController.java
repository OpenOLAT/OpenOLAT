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
package org.olat.course.nodes.gta.ui;

import static org.olat.course.nodes.gta.ui.GTAUIFactory.officeHtml;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorDisplayInfo;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.commons.services.doceditor.ui.DocEditorController;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSTranscodingService;
import org.olat.core.commons.services.vfs.manager.VFSTranscodingDoneEvent;
import org.olat.core.commons.services.video.ui.VideoAudioPlayerController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
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
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.id.Roles;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.io.SystemFilenameFilter;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.model.TaskDefinition;
import org.olat.course.nodes.gta.ui.TaskDefinitionTableModel.TDCols;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.audiovideorecording.AVModule;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
abstract class AbstractAssignmentEditController extends FormBasicController implements Activateable2, GenericEventListener {


	private FormLink addTaskLink;
	private FormLink createTaskLink;
	private FormLink addMultipleTasks;
	private FormLink createVideoAssignment;
	private FormLink createAudioAssignment;
	private DropdownItem addTaskDropdown;
	private DropdownItem createTaskDropdown;

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
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private StepsMainRunController addMultipleTasksWizardCtrl;
	private Controller docEditorCtrl;
	private ToolsController toolsCtrl;

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
	@Autowired
	private UserManager userManager;
	
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

		addTaskDropdown = uifactory.addDropdownMenu("add.task.dropdown", null, null, tasksCont, getTranslator());
		addTaskDropdown.setOrientation(DropdownOrientation.right);
		addTaskDropdown.setElementCssClass("o_sel_add_more");
		addTaskDropdown.setEmbbeded(true);
		addTaskDropdown.setButton(true);

		addMultipleTasks = uifactory.addFormLink("add.multiple.tasks", tasksCont, Link.LINK);
		addMultipleTasks.setIconLeftCSS("o_icon o_icon-fw o_gta_icon");
		addMultipleTasks.setVisible(!readOnly);
		addTaskDropdown.addElement(addMultipleTasks);

		createTaskLink = uifactory.addFormLink("create.task", tasksCont, Link.BUTTON);
		createTaskLink.setElementCssClass("o_sel_course_gta_create_task");
		createTaskLink.setIconLeftCSS("o_icon o_icon-fw o_icon_add");
		createTaskLink.setVisible(!readOnly);

		if (!readOnly) {
			createTaskDropdown = uifactory.addDropdownMenu("create.task.dropdown", null, null, tasksCont, getTranslator());
			createTaskDropdown.setOrientation(DropdownOrientation.right);
			createTaskDropdown.setElementCssClass("o_sel_add_more");
			createTaskDropdown.setEmbbeded(true);
			createTaskDropdown.setButton(true);
			createTaskDropdown.setVisible(false);
			if (avModule.isVideoRecordingEnabled()) {
				createVideoAssignment = uifactory.addFormLink("av.create.video.assignment", tasksCont, Link.LINK);
				createVideoAssignment.setElementCssClass("o_sel_course_gta_create_video_assignment");
				createVideoAssignment.setIconLeftCSS("o_icon o_icon-fw o_icon_video_record");
				createTaskDropdown.addElement(createVideoAssignment);
			}
			if (avModule.isAudioRecordingEnabled()) {
				createAudioAssignment = uifactory.addFormLink("av.create.audio.assignment", tasksCont, Link.LINK);
				createAudioAssignment.setElementCssClass("o_sel_course_gta_create_audio_assignment");
				createAudioAssignment.setIconLeftCSS("o_icon o_icon-fw o_icon_audio_record");
				createTaskDropdown.addElement(createAudioAssignment);
			}
			if (createVideoAssignment != null || createAudioAssignment != null) {
				createTaskDropdown.setVisible(true);
			}
		}

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TDCols.title.i18nKey(), TDCols.title.ordinal()));
		DefaultFlexiColumnModel descFlexiColumnModel = new DefaultFlexiColumnModel(TDCols.desc.i18nKey(), TDCols.desc.ordinal());
		descFlexiColumnModel.setDefaultVisible(false);
		columnsModel.addFlexiColumnModel(descFlexiColumnModel);
		fileExistsRenderer = new WarningFlexiCellRenderer();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TDCols.file.i18nKey(), TDCols.file.ordinal(), fileExistsRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TDCols.author.i18nKey(), TDCols.author.ordinal()));

		if(!readOnly) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TDCols.edit.i18nKey(), TDCols.edit.ordinal(), "editEntry",
					new StaticFlexiCellRenderer("", "editEntry", "o_icon o_icon-lg o_icon_edit", null, translate("edit"))));
			DefaultFlexiColumnModel toolsFlexiColumnModel = new DefaultFlexiColumnModel(TDCols.toolsLink.i18nKey(), TDCols.toolsLink.ordinal());
			toolsFlexiColumnModel.setAlwaysVisible(true);
			columnsModel.addFlexiColumnModel(toolsFlexiColumnModel);
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
			FormLink openLink = null;
			FormLink documentLink = null;
			FormLink toolsLink = null;
			String author = null;
			
			VFSItem item = tasksContainer.resolve(def.getFilename());
			if(item instanceof VFSLeaf vfsLeaf) {
				VFSMetadata metaInfo = item.getMetaInfo();
				if (metaInfo.getFileInitializedBy() != null) {
					author = userManager.getUserDisplayName(metaInfo.getFileInitializedBy());
				}

				String iconFilename = "";
				if (metaInfo.isInTranscoding()) {
					openLink = uifactory.addFormLink("transcoding_" + (++linkCounter), "transcoding", "av.converting", null, null, Link.LINK);
					openLink.setUserObject(def);
					documentLink = uifactory.addFormLink("transcoding_" + (++linkCounter), "transcoding", "av.converting", null, null, Link.LINK);
					documentLink.setUserObject(def);
				} else {
					downloadLink = uifactory.addDownloadLink("file_" + (++linkCounter), item.getName(), null, vfsLeaf, taskDefTableEl);
					downloadLink.setUserObject(vfsLeaf);

					DocEditorDisplayInfo editorInfo = docEditorService.getEditorInfo(getIdentity(), roles, vfsLeaf,
							metaInfo, true, DocEditorService.modesEditView(!readOnly));
					iconFilename = "<i class=\"o_icon o_icon-fw " + CSSHelper.createFiletypeIconCssClassFor(def.getFilename()) + "\"></i> " + def.getFilename();
					if (editorInfo.isEditorAvailable()) {
						openLink = uifactory.addFormLink("open_" + (++linkCounter), "open", "", null, null, Link.LINK | Link.NONTRANSLATED);
						documentLink = uifactory.addFormLink("open_" + (++linkCounter), "open", iconFilename, null, null, Link.LINK | Link.NONTRANSLATED);
						openLink.setIconLeftCSS("o_icon o_icon-fw " + editorInfo.getModeIcon());
						if (editorInfo.isNewWindow()) {
							openLink.setNewWindow(true, true, false);
							documentLink.setNewWindow(true, true, false);
						}
						openLink.setUserObject(def);
						documentLink.setUserObject(def);
					} else {
						openLink = uifactory.addFormLink("download_" + (++linkCounter), "download", "", null, null, Link.NONTRANSLATED);
						documentLink = uifactory.addFormLink("download_" + (++linkCounter), "download", iconFilename, null, null, Link.NONTRANSLATED);
						openLink.setUserObject(item);
						documentLink.setUserObject(item);
					}
				}
				toolsLink = uifactory.addFormLink("tools_" + (++linkCounter), "tools", translate("table.header.action"), null, null, Link.NONTRANSLATED);
			}
			TaskDefinitionRow row = new TaskDefinitionRow(def, author, downloadLink, openLink, documentLink, toolsLink);
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
					if (taskFile.equalsIgnoreCase(taskDefinition.getFilename())) {
						found = true;
						break;
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
		if (event instanceof VFSTranscodingDoneEvent doneEvent) {
			if (taskModel.getObjects().stream().anyMatch(
					t -> doneEvent.getFileName().equals(t.taskDefinition().getFilename())
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
		} else if(docEditorCtrl == source) {
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
		} else if (source == addMultipleTasksWizardCtrl) {
			if (event == Event.CANCELLED_EVENT) {
				getWindowControl().pop();
			} else if (event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				updateModel(ureq);
			}
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
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(docEditorCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		confirmDeleteCtrl = null;
		editTaskCtrl = null;
		addTaskCtrl = null;
		avTaskCtrl = null;
		avConvertingMenuCtrl = null;
		videoAudioPlayerController = null;
		cmc = null;
		ccwc = null;
		toolsCalloutCtrl = null;
		docEditorCtrl = null;
		toolsCtrl = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addTaskLink == source) {
			doAddTask(ureq);
		} else if (createTaskLink == source) {
			doCreateTask(ureq);
		} else if (addMultipleTasks == source) {
			doOpenAddMultipleTasksWizard(ureq);
		} else if (createVideoAssignment == source) {
			doCreateVideoAsssignment(ureq);
		} else if (createAudioAssignment == source) {
			doCreateAudioAssignment(ureq);
		} else if(taskDefTableEl == source) {
			if(event instanceof SelectionEvent se) {
				TaskDefinitionRow row = taskModel.getObject(se.getIndex());
				if("editEntry".equals(se.getCommand()) && !row.taskDefinition().isInTranscoding()) {
					doEditMetadata(ureq, row.taskDefinition());
				}
			}
		} else if (source instanceof FormLink link) {
			if (link.getUserObject() instanceof TaskDefinition taskDef) {
				if ("open".equalsIgnoreCase(link.getCmd())) {
					doOpenMedia(ureq, taskDef);
				} else if ("transcoding".equalsIgnoreCase(link.getCmd())) {
					doOpenTranscoding(ureq, link, taskDef);
				}
			} else if (link.getUserObject() instanceof VFSItem item) {
				if ("download".equalsIgnoreCase(link.getCmd())) {
					VFSMediaResource vdr = new VFSMediaResource((VFSLeaf) item);
					vdr.setDownloadable(true);
					ureq.getDispatchResult().setResultingMediaResource(vdr);
				}
			}
			if ("tools".equalsIgnoreCase(link.getCmd())) {
				doOpenTools(ureq, link);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doOpenTools(UserRequest ureq, FormLink link) {
		toolsCtrl = new ToolsController(ureq, getWindowControl(), (TaskDefinitionRow) link.getUserObject());
		listenTo(toolsCtrl);

		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}

	private void doAddTask(UserRequest ureq) {
		List<TaskDefinition> currentDefinitions = gtaManager.getTaskDefinitions(courseEnv, gtaNode);
		addTaskCtrl = new EditTaskController(ureq, getWindowControl(), tasksFolder, currentDefinitions);
		listenTo(addTaskCtrl);

		String title = translate("add.task");
		cmc = new CloseableModalController(getWindowControl(), null, addTaskCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doEditMetadata(UserRequest ureq, TaskDefinition taskDef) {
		List<TaskDefinition> currentDefinitions = gtaManager.getTaskDefinitions(courseEnv, gtaNode);
		editTaskCtrl = new EditTaskController(ureq, getWindowControl(), taskDef, tasksFolder, currentDefinitions);
		listenTo(editTaskCtrl);

		String title = translate("edit.task");
		cmc = new CloseableModalController(getWindowControl(), null, editTaskCtrl.getInitialComponent(), true, title, true);
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

	private void doOpenAddMultipleTasksWizard(UserRequest ureq) {
		Step startAddingMultipleTasks = new AddMultipleTasksStep(ureq, courseEnv, gtaNode);
		addMultipleTasksWizardCtrl = new StepsMainRunController(ureq, getWindowControl(), startAddingMultipleTasks, new FinishedCallback(),
				new CancelCallback(), translate("add.tasks"), null);
		listenTo(addMultipleTasksWizardCtrl);
		getWindowControl().pushAsModalDialog(addMultipleTasksWizardCtrl.getInitialComponent());
	}
	
	private void doCreateTask(UserRequest ureq) {
		newTaskCtrl = new NewTaskController(ureq, getWindowControl(), tasksContainer,
				officeHtml(getIdentity(), ureq.getUserSession().getRoles(), getLocale()));
		listenTo(newTaskCtrl);

		String title = translate("create.task");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), newTaskCtrl.getInitialComponent(), true, title, true);
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
	
	private void doOpenTranscoding(UserRequest ureq, FormLink link, TaskDefinition taskDef) {
		if (guardModalController(avConvertingMenuCtrl)) return;
		
		avConvertingMenuCtrl = new AVConvertingMenuController(ureq, getWindowControl(), taskDef);
		listenTo(avConvertingMenuCtrl);
		ccwc = new CloseableCalloutWindowController(ureq, getWindowControl(),
				avConvertingMenuCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(ccwc);
		ccwc.activate();
	}
	
	private void doPlayMaster(UserRequest ureq, TaskDefinition taskDef) {
		doOpenMedia(ureq, taskDef);
	}

	private void doOpenMedia(UserRequest ureq, TaskDefinition taskDef) {
		VFSItem vfsItem = tasksContainer.resolve(taskDef.getFilename());
		if(vfsItem instanceof VFSLeaf vfsLeaf) {
			gtaManager.markNews(courseEnv, gtaNode);
			DocEditorConfigs configs = GTAUIFactory.getEditorConfig(tasksContainer, vfsLeaf, taskDef.getFilename(), Mode.EDIT, courseRepoKey);
			docEditorCtrl = docEditorService.openDocument(ureq, getWindowControl(), configs, DocEditorService.modesEditView(!readOnly)).getController();
			listenTo(docEditorCtrl);
		} else {
			showError("error.missing.file");
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
			
			if(cellValue instanceof String filename) {
				boolean found = false;
				
				if(tasks != null) {
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

	protected class FinishedCallback implements StepRunnerCallback {
		@Override
		public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
			List<TaskDefinition> taskDefinitionList = (List<TaskDefinition>) runContext.get("taskList");
			for (TaskDefinition newTask : taskDefinitionList) {
				tasksContainer.resolve(newTask.getFilename()).getMetaInfo().setFileInitializedBy(getIdentity());
				gtaManager.addTaskDefinition(newTask, courseEnv, gtaNode);
			}

			gtaManager.markNews(courseEnv, gtaNode);
			updateModel(ureq);
			return StepsMainRunController.DONE_MODIFIED;
		}
	}

	protected static class CancelCallback implements StepRunnerCallback {
		@Override
		public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
			return Step.NOSTEP;
		}
	}

	private class ToolsController extends BasicController {

		private final VelocityContainer mainVC;
		private final Link deleteLink;
		private final Link editLink;
		private Link openLink = null;
		private final Link downloadLink;
		private final TaskDefinitionRow taskDefinitionRow;

		public ToolsController(UserRequest ureq, WindowControl wControl, TaskDefinitionRow taskDefinitionRow) {
			super(ureq, wControl);
			this.taskDefinitionRow = taskDefinitionRow;
			setTranslator(getTranslator());

			mainVC = createVelocityContainer("submit_docs_tools");

			List<String> links = new ArrayList<>(2);

			editLink = addLink("edit", "o_icon_edit", links);
			links.add("-");

			String iconLeftCSS = taskDefinitionRow.openLink() != null ? taskDefinitionRow.openLink().getComponent().getIconLeftCSS() : "";
			String i18nKey = "";
			if (StringHelper.containsNonWhitespace(iconLeftCSS)) {
				if (iconLeftCSS.contains("preview")) {
					i18nKey = "open.file";
				} else if (iconLeftCSS.contains("edit")) {
					i18nKey = "edit.file";
					iconLeftCSS = "o_icon-file-pen";
				} else if (iconLeftCSS.contains("_play")) {
					i18nKey = "play.file";
				}
			}

			if (StringHelper.containsNonWhitespace(i18nKey)) {
				openLink = addLink(i18nKey, iconLeftCSS, links);
				if (i18nKey.equalsIgnoreCase("edit.file")) {
					openLink.setNewWindow(true, true);
				}
			}

			if (readOnly) {
				editLink.setVisible(false);
				if (openLink != null) {
					openLink.setVisible(false);
				}
			}
			downloadLink = addLink("download.file", "o_icon_download", links);
			links.add("-");
			deleteLink = addLink("delete", "o_icon_delete_item", links);

			mainVC.contextPut("links", links);

			putInitialPanel(mainVC);
		}

		private Link addLink(String name, String iconCss, List<String> links) {
			int presentation = Link.LINK;
			if (taskDefinitionRow.openLink() != null && taskDefinitionRow.openLink().getI18nKey().equals(name)) {
				presentation = Link.NONTRANSLATED;
			}
			Link link = LinkFactory.createLink(name, name, getTranslator(), mainVC, this, presentation);
			mainVC.put(name, link);
			links.add(name);
			link.setIconLeftCSS("o_icon o_icon-fw " + iconCss);
			return link;
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			if (source == deleteLink) {
				close();
				if(gtaManager.isTaskInProcess(courseEnv.getCourseGroupManager().getCourseEntry(), gtaNode, taskDefinitionRow.taskDefinition().getFilename())) {
					doConfirmDelete(ureq, taskDefinitionRow.taskDefinition());
				} else {
					doDelete(ureq, taskDefinitionRow.taskDefinition());
				}
			} else if (source == editLink) {
				close();
				doEditMetadata(ureq, taskDefinitionRow.taskDefinition());
			} else if (source == openLink) {
				close();
				if (taskDefinitionRow.openLink().getCmd().equalsIgnoreCase("open")) {
					doOpenMedia(ureq, taskDefinitionRow.taskDefinition());
				} else if (taskDefinitionRow.openLink().getCmd().equalsIgnoreCase("transcoding")) {
					doOpenTranscoding(ureq, taskDefinitionRow.openLink(), taskDefinitionRow.taskDefinition());
				}
			} else if (source == downloadLink) {
				VFSMediaResource vdr = new VFSMediaResource((VFSLeaf) taskDefinitionRow.downloadLink().getUserObject());
				vdr.setDownloadable(true);
				ureq.getDispatchResult().setResultingMediaResource(vdr);
			}
		}

		private void close() {
			toolsCalloutCtrl.deactivate();
			cleanUp();
		}
	}
}
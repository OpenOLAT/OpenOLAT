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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.modules.singlepage.SinglePageController;
import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorDisplayInfo;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.winmgr.CommandFactory;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.id.Identity;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.core.util.vfs.filters.VFSLeafButSystemFilter;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.AssignmentResponse;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.GTAType;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.TaskProcess;
import org.olat.course.nodes.gta.model.TaskDefinition;
import org.olat.course.nodes.gta.ui.component.DescriptionWithTooltipCellRenderer;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.modules.assessment.Role;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class GTAAvailableTaskController extends FormBasicController {

	private FlexiTableElement tableEl;
	private AvailableTaskTableModel taskModel;

	private CloseableModalController cmc;
	private SinglePageController previewCtrl;
	private CloseableCalloutWindowController descriptionCalloutCtrl;
	private Controller docEditorCtrl;
	
	/**
	 * True if it's a group task, false if it's an individual task.
	 */
	private final boolean businessGroupTask;
	private final GTACourseNode gtaNode;
	private final List<TaskDefinition> taskDefs;
	private final CourseEnvironment courseEnv;
	private final TaskList taskList;
	private final Identity assessedIdentity;
	private final BusinessGroup assessedGroup;
	private final boolean submissionTemplate;
	
	
	@Autowired
	private GTAManager gtaManager;
	@Autowired
	private MailManager mailManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private DocEditorService docEditorService;
	
	public GTAAvailableTaskController(UserRequest ureq, WindowControl wControl,
			List<TaskDefinition> taskDefs, TaskList taskList,
			BusinessGroup assessedGroup, Identity assessedIdentity,
			CourseEnvironment courseEnv, GTACourseNode gtaNode) {
		super(ureq, wControl, "available_tasks");
		this.gtaNode = gtaNode;
		this.taskDefs = taskDefs;
		this.taskList = taskList;
		this.assessedGroup = assessedGroup;
		this.assessedIdentity = assessedIdentity;
		this.courseEnv = courseEnv;
		businessGroupTask = GTAType.group.name().equals(gtaNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_TYPE));
		submissionTemplate = gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_SUBMISSION_TEMPLATE) 
				&& gtaManager.nextStep(TaskProcess.assignment, gtaNode) == TaskProcess.submit;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ATDCols.title.i18nKey(), ATDCols.title.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ATDCols.description.i18nKey(), ATDCols.description.ordinal(),
				new DescriptionWithTooltipCellRenderer()));
		
		boolean preview = gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_PREVIEW);
		if(preview) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ATDCols.task.i18nKey(), ATDCols.task.ordinal()));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ATDCols.select.i18nKey(), ATDCols.select.ordinal(), "select",
				new BooleanCellRenderer(
						new StaticFlexiCellRenderer(translate("select.edit"), "select", true, true, "btn btn-primary", "o_icon o_icon_submit", null), 
						new StaticFlexiCellRenderer(translate("select"), "select", "btn btn-primary", "o_icon o_icon_submit"))));
		
		taskModel = new AvailableTaskTableModel(columnsModel);
		
		tableEl = uifactory.addTableElement(getWindowControl(), "table", taskModel, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(false);
		formLayout.add("table", tableEl);
		
		loadModel(ureq);
	}
	
	private void loadModel(UserRequest ureq) {
		List<AvailableTask> availableTasks = new ArrayList<>(taskDefs.size());
		List<String> usedSlotes;
		if(GTACourseNode.GTASK_SAMPLING_UNIQUE.equals(gtaNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_SAMPLING))) {
			usedSlotes = gtaManager.getAssignedTasks(taskList);
		} else {
			usedSlotes = Collections.emptyList();
		}

		for(TaskDefinition taskDef:taskDefs) {
			String filename = taskDef.getFilename();
			if(usedSlotes.contains(filename)) {
				continue;
			}
			
			boolean editableSubmission = submissionTemplate
					&& gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_EMBBEDED_EDITOR);
			VFSContainer tasksContainer = gtaManager.getTasksContainer(courseEnv, gtaNode);
			VFSItem vfsItem = tasksContainer.resolve(filename);
			DocEditorDisplayInfo editorInfo = docEditorService.getEditorInfo(getIdentity(),
					ureq.getUserSession().getRoles(), vfsItem, vfsItem.getMetaInfo(), true, DocEditorService.MODES_EDIT);
			if (editableSubmission) {
	 			if (vfsItem instanceof VFSLeaf vfsLeaf) {
					VFSMetadata vfsMetadata = vfsLeaf.getMetaInfo();
	 				if (vfsMetadata != null) {
						editableSubmission = editorInfo.isEditorAvailable();
	 				} else {
						editableSubmission = false;
	 				}
	 			} else {
					editableSubmission = false;
	 			}
			}
			
			FormLink descriptionLink = null;
			if(StringHelper.containsNonWhitespace(taskDef.getDescription())) {
				descriptionLink = uifactory.addFormLink("preview-" + CodeHelper.getRAMUniqueID(), "description", "task.description", null, flc, Link.LINK);
				descriptionLink.setIconLeftCSS("o_icon o_icon_description");
			}
			
			FormLink download = null;
			boolean preview = gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_PREVIEW);
			if(preview) {
				String iconFilename = "<i class=\"o_icon o_icon-fw " + CSSHelper.createFiletypeIconCssClassFor(filename) + "\"></i> " + filename;
				if(taskDef.getFilename().endsWith(".html")) {
					download = uifactory.addFormLink("prev-html-" + CodeHelper.getRAMUniqueID(), "preview-html", iconFilename, null, flc, Link.LINK | Link.NONTRANSLATED);
				} else if (editableSubmission) {
					download = uifactory.addFormLink("prev-" + CodeHelper.getRAMUniqueID(), "open", iconFilename, null, flc, Link.NONTRANSLATED);
				} else {
					download = uifactory.addFormLink("prev-" + CodeHelper.getRAMUniqueID(), "download", iconFilename, null, flc, Link.NONTRANSLATED);
				}

				if (editorInfo != null && editorInfo.isNewWindow() && !filename.endsWith(".html")) {
					download.setNewWindow(true, true, false);
				}
				if (editableSubmission || taskDef.getFilename().endsWith(".html")) {
					download.setUserObject(filename);
				} else {
					VFSItem item = tasksContainer.resolve(filename);
					download.setUserObject(item);
				}
			}
			
			AvailableTask wrapper = new AvailableTask(taskDef, Boolean.valueOf(editableSubmission), descriptionLink, download);
			availableTasks.add(wrapper);
			if(descriptionLink != null) {
				descriptionLink.setUserObject(wrapper);
			}
		}
		taskModel.setObjects(availableTasks);
		tableEl.reset();
		
		if(availableTasks.isEmpty()) {
			flc.contextPut("noMoreTasks", Boolean.TRUE);
		} else {
			flc.contextPut("noMoreTasks", Boolean.FALSE);
		}
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent se) {
				AvailableTask row = taskModel.getObject(se.getIndex());
				if("select".equals(se.getCommand())) {
					doSelect(ureq, row);
				}
			}
		} else if(source instanceof FormLink link) {
			if("description".equals(link.getCmd())) {
				doDescription(ureq, (AvailableTask)link.getUserObject());
			} else if("preview-html".equals(link.getCmd())) {
				String filename = (String)link.getUserObject();
				doPreview(ureq, filename);
			} else if ("open".equals(link.getCmd())) {
				String filename = (String)link.getUserObject();
				doOpenTask(ureq, filename);
			} else if ("download".equalsIgnoreCase(link.getCmd())) {
				VFSMediaResource vdr = new VFSMediaResource((VFSLeaf) link.getUserObject());
				vdr.setDownloadable(true);
				ureq.getDispatchResult().setResultingMediaResource(vdr);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(cmc == source || docEditorCtrl == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(previewCtrl);
		removeAsListenerAndDispose(docEditorCtrl);
		cmc = null;
		previewCtrl = null;
		docEditorCtrl = null;
	}
	
	private void doPreview(UserRequest ureq, String filename) {
		if(filename != null && filename.endsWith(".html")) {
			VFSContainer tasksContainer = gtaManager.getTasksContainer(courseEnv, gtaNode);
			previewCtrl = new SinglePageController(ureq, getWindowControl(), tasksContainer, filename, false);
			listenTo(previewCtrl);

			cmc = new CloseableModalController(getWindowControl(), translate("close"), previewCtrl.getInitialComponent(), true, filename);
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	private void doSelect(UserRequest ureq, AvailableTask row) {
		String taskName = row.taskDef().getFilename();
		File tasksFolder = gtaManager.getTasksDirectory(courseEnv, gtaNode);
		File task = new File(tasksFolder, taskName);
		
		AssignmentResponse response;
		if(businessGroupTask) {
			response = gtaManager.selectTask(assessedGroup, taskList, courseEnv, gtaNode, task, getIdentity());
		} else {
			response = gtaManager.selectTask(assessedIdentity, taskList, courseEnv, gtaNode, task);
		}
		
		if(response == null || response.getStatus() == AssignmentResponse.Status.error) {
			if (row.editableSubmission().booleanValue()) {
				getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowCancelRedirectTo());
			}
			showError("task.assignment.error");
		} else if(response.getStatus() == AssignmentResponse.Status.alreadyAssigned) {
			if (row.editableSubmission().booleanValue()) {
				getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowCancelRedirectTo());
			}
			showWarning("task.alreadyChosen");
		} else if(response.getStatus() == AssignmentResponse.Status.ok) {
			if (row.editableSubmission().booleanValue()) {
				VFSLeaf submissionLeaf = getSubmissionLeaf(ureq);
				if (submissionLeaf != null) {
					doOpenSubmission(ureq, submissionLeaf);
				} else {
					getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowCancelRedirectTo());
					showInfo("task.successfully.assigned");
				}
			} else {
					showInfo("task.successfully.assigned");
			}
			fireEvent(ureq, Event.DONE_EVENT);
			gtaManager.log("Assignment", "task assigned", response.getTask(), getIdentity(),
					assessedIdentity, assessedGroup, courseEnv, gtaNode, Role.user);
			doSendConfirmationEmail(response.getTask());
		}
	}
	
	private VFSLeaf getSubmissionLeaf(UserRequest ureq) {
		VFSContainer submitContainer;
		if(businessGroupTask) {
			submitContainer = gtaManager.getSubmitContainer(courseEnv, gtaNode, assessedGroup);
		} else {
			submitContainer = gtaManager.getSubmitContainer(courseEnv, gtaNode, getIdentity());
		}
		
		List<VFSItem> items = submitContainer.getItems(new VFSLeafButSystemFilter());
		if (!items.isEmpty()) {
			VFSLeaf vfsLeaf = (VFSLeaf)items.get(0);
			DocEditorDisplayInfo editorInfo = docEditorService.getEditorInfo(getIdentity(),
					ureq.getUserSession().getRoles(), vfsLeaf, vfsLeaf.getMetaInfo(), true, DocEditorService.MODES_EDIT);
			if(editorInfo.isEditorAvailable()) {
				return vfsLeaf;
			}
		}
		return null;
	}

	private void doOpenTask(UserRequest ureq, String filename) {
		VFSContainer tasksContainer = gtaManager.getTasksContainer(courseEnv, gtaNode);
		VFSLeaf vfsLeaf = (VFSLeaf) tasksContainer.resolve(filename);
		DocEditorConfigs configs = GTAUIFactory.getEditorConfig(tasksContainer, vfsLeaf, vfsLeaf.getName(), Mode.VIEW, null);
		docEditorCtrl = docEditorService.openDocument(ureq, getWindowControl(), configs, DocEditorService.MODES_VIEW).getController();
		listenTo(docEditorCtrl);
	}

	private void doOpenSubmission(UserRequest ureq, VFSLeaf submissionLeaf) {
		DocEditorConfigs configs = GTAUIFactory.getEditorConfig(submissionLeaf.getParentContainer(), submissionLeaf,
				submissionLeaf.getName(), Mode.EDIT, null);
		docEditorCtrl = docEditorService.openDocument(ureq, getWindowControl(), configs, DocEditorService.MODES_EDIT_VIEW).getController();
		listenTo(docEditorCtrl);
	}

	private void doSendConfirmationEmail(Task assignedTask) {
		MailContext context = new MailContextImpl(getWindowControl().getBusinessControl().getAsString());
		if(GTAType.group.name().equals(gtaNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_TYPE))) {
			String bundleId = UUID.randomUUID().toString();
			List<Identity> participants = businessGroupService.getMembers(assessedGroup, GroupRoles.participant.name());
			for(Identity participant:participants) {
				sendConfirmationEmail(participant, assignedTask, bundleId, context);
			}
		} else {
			sendConfirmationEmail(assessedIdentity, assignedTask, null, context);
		}
	}
	
	private void sendConfirmationEmail(Identity recipient, Task assignedTask, String bundleId, MailContext context) {
		MailBundle bundle = new MailBundle();
		bundle.setContext(context);
		bundle.setMetaId(bundleId);
		ContactList contacts = new ContactList("participants");
		contacts.add(recipient);
		bundle.setContactList(contacts);
		
		String[] args = new String[] {
			recipient.getUser().getFirstName(),	//0 first name
			recipient.getUser().getLastName(),	//1 last name
			courseEnv.getCourseTitle(),				//2 course name
			assignedTask.getTaskName()				//3 task
		};
		String subject = translate("mail.confirm.assignment.subject", args);
		String body = translate("mail.confirm.assignment.body", args);
		bundle.setContent(subject, body);

		mailManager.sendMessage(bundle);
	}
	
	private void doDescription(UserRequest ureq, AvailableTask row) {
		removeAsListenerAndDispose(descriptionCalloutCtrl);
		
		VelocityContainer descriptionVC = createVelocityContainer("description_callout");
		descriptionVC.contextPut("description", row.taskDef().getDescription());
		descriptionCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				descriptionVC, row.descriptionLink().getFormDispatchId(), "", true, "");
		listenTo(descriptionCalloutCtrl);
		descriptionCalloutCtrl.activate();
	}
	
	public enum ATDCols {
		title("task.title"),
		description("task.description"),
		task("table.header.group.taskTitle"),
		select("select");

		private final String i18nKey;
	
		private ATDCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		public String i18nKey() {
			return i18nKey;
		}
	}

	private record AvailableTask(TaskDefinition taskDef, Boolean editableSubmission, FormLink descriptionLink, FormLink openLink) { }
	
	private static class AvailableTaskTableModel extends DefaultFlexiTableDataModel<AvailableTask> {
		
		public AvailableTaskTableModel(FlexiTableColumnModel columnModel) {
			super(columnModel);
		}

		@Override
		public Object getValueAt(int row, int col) {
			AvailableTask task = getObject(row);
			return switch (ATDCols.values()[col]) {
				case title -> task.taskDef().getTitle();
				case description -> task.taskDef().getDescription();
				case task -> task.openLink();
				case select -> task.editableSubmission();
				default -> "ERROR";
			};
		}
	}
}

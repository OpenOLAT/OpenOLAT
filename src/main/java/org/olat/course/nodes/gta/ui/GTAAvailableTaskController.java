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

import static org.olat.core.util.FileUtils.getFileSuffix;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.modules.singlepage.SinglePageController;
import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorService;
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
import org.olat.core.util.vfs.filters.VFSLeafFilter;
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
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTAAvailableTaskController extends FormBasicController {

	private FlexiTableElement tableEl;
	private AvailableTaskTableModel taskModel;

	private CloseableModalController cmc;
	private SinglePageController previewCtrl;
	private CloseableCalloutWindowController descriptionCalloutCtrl;
	
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
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ATDCols.preview.i18nKey(), ATDCols.preview.ordinal()));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ATDCols.select.i18nKey(), ATDCols.select.ordinal(), "select",
				new BooleanCellRenderer(
						new StaticFlexiCellRenderer(translate("select"), "select", true, true, "btn btn-primary", "o_icon o_icon_submit", null), 
						new StaticFlexiCellRenderer(translate("select"), "select", "btn btn-primary", "o_icon o_icon_submit"))));
		
		taskModel = new AvailableTaskTableModel(columnsModel);
		
		tableEl = uifactory.addTableElement(getWindowControl(), "table", taskModel, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(false);
		formLayout.add("table", tableEl);
		
		loadModel(ureq);
	}
	
	private void loadModel(UserRequest ureq) {
		File taskFolder = gtaManager.getTasksDirectory(courseEnv, gtaNode);

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
					&& docEditorService.hasEditor(getIdentity(), ureq.getUserSession().getRoles(), getFileSuffix(filename), Mode.EDIT, true, false);
			
			FormLink descriptionLink = null;
			if(StringHelper.containsNonWhitespace(taskDef.getDescription())) {
				descriptionLink = uifactory.addFormLink("preview-" + CodeHelper.getRAMUniqueID(), "description", "task.description", null, flc, Link.LINK);
				descriptionLink.setIconLeftCSS("o_icon o_icon_description");
			}
			
			FormItem download = null;
			boolean preview = gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_PREVIEW);
			if(preview) {
				if(taskDef.getFilename().endsWith(".html")) {
					download = uifactory.addFormLink("prev-html-" + CodeHelper.getRAMUniqueID(), "preview-html", filename, null, flc, Link.LINK | Link.NONTRANSLATED);
					download.setUserObject(filename);
				} else {
					File taskFile = new File(taskFolder, filename);
					download = uifactory.addDownloadLink("prev-" + CodeHelper.getRAMUniqueID(), filename, null, taskFile, tableEl);
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
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				AvailableTask row = taskModel.getObject(se.getIndex());
				if("select".equals(se.getCommand())) {
					doSelect(ureq, row);
				}
			}
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("description".equals(link.getCmd())) {
				doDescription(ureq, (AvailableTask)link.getUserObject());
			} else if("preview-html".equals(link.getCmd())) {
				String filename = (String)link.getUserObject();
				doPreview(ureq, filename);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(previewCtrl);
		cmc = null;
		previewCtrl = null;
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
		String taskName = row.getTaskDef().getFilename();
		File tasksFolder = gtaManager.getTasksDirectory(courseEnv, gtaNode);
		File task = new File(tasksFolder, taskName);
		
		AssignmentResponse response;
		if(businessGroupTask) {
			response = gtaManager.selectTask(assessedGroup, taskList, courseEnv, gtaNode, task, getIdentity());
		} else {
			response = gtaManager.selectTask(assessedIdentity, taskList, courseEnv, gtaNode, task);
		}
		
		if(response == null || response.getStatus() == AssignmentResponse.Status.error) {
			if (row.getEditableSubmission().booleanValue()) {
				getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowCancelRedirectTo());
			}
			showError("task.assignment.error");
		} else if(response.getStatus() == AssignmentResponse.Status.alreadyAssigned) {
			if (row.getEditableSubmission().booleanValue()) {
				getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowCancelRedirectTo());
			}
			showWarning("task.alreadyChosen");
		} else if(response.getStatus() == AssignmentResponse.Status.ok) {
			if (row.getEditableSubmission().booleanValue()) {
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
		
		List<VFSItem> items = submitContainer.getItems(new VFSLeafFilter());
		if (!items.isEmpty()) {
			VFSLeaf vfsLeaf = (VFSLeaf)items.get(0);
			if(docEditorService.hasEditor(getIdentity(), ureq.getUserSession().getRoles(), vfsLeaf, Mode.EDIT, true)) {
				return vfsLeaf;
			}
		}
		return null;
	}

	private void doOpenSubmission(UserRequest ureq, VFSLeaf submissionLeaf) {
		DocEditorConfigs configs = GTAUIFactory.getEditorConfig(submissionLeaf.getParentContainer(), submissionLeaf,
				submissionLeaf.getName(), Mode.EDIT, null);
		String url = docEditorService.prepareDocumentUrl(ureq.getUserSession(), configs);
		getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowRedirectTo(url));
	}

	private void doSendConfirmationEmail(Task assignedTask) {
		MailContext context = new MailContextImpl(getWindowControl().getBusinessControl().getAsString());
		
		MailBundle bundle = new MailBundle();
		bundle.setContext(context);
		ContactList contacts = new ContactList("participants");
		if(GTAType.group.name().equals(gtaNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_TYPE))) {
			List<Identity> participants = businessGroupService.getMembers(assessedGroup, GroupRoles.participant.name());
			contacts.addAllIdentites(participants);
			bundle.setMetaId(UUID.randomUUID().toString());
		} else {
			contacts.add(assessedIdentity);
		}
		bundle.setContactList(contacts);
		
		String[] args = new String[] {
			getIdentity().getUser().getFirstName(),	//0 first name
			getIdentity().getUser().getLastName(),	//1 last name
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
		descriptionVC.contextPut("description", row.getTaskDef().getDescription());
		descriptionCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				descriptionVC, row.getDescriptionLink().getFormDispatchId(), "", true, "");
		listenTo(descriptionCalloutCtrl);
		descriptionCalloutCtrl.activate();
	}
	
	public enum ATDCols {
		title("task.title"),
		description("task.description"),
		preview("preview"),
		select("select");

		private final String i18nKey;
	
		private ATDCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		public String i18nKey() {
			return i18nKey;
		}
	}
	
	private static class AvailableTask {

		private final TaskDefinition taskDef;
		private final Boolean editableSubmission;
		private final FormLink descriptionLink;
		private final FormItem downloadLink;
		
		public AvailableTask(TaskDefinition taskDef, Boolean editableSubmission, FormLink descriptionLink,  FormItem downloadLink) {
			this.taskDef = taskDef;
			this.editableSubmission = editableSubmission;
			this.downloadLink = downloadLink;
			this.descriptionLink = descriptionLink;
		}

		public TaskDefinition getTaskDef() {
			return taskDef;
		}
		
		public Boolean getEditableSubmission() {
			return editableSubmission;
		}

		public FormLink getDescriptionLink() {
			return descriptionLink;
		}

		public FormItem getDownloadLink() {
			return downloadLink;
		}
	}
	
	private static class AvailableTaskTableModel extends DefaultFlexiTableDataModel<AvailableTask> {
		
		public AvailableTaskTableModel(FlexiTableColumnModel columnModel) {
			super(columnModel);
		}

		@Override
		public DefaultFlexiTableDataModel<AvailableTask> createCopyWithEmptyList() {
			return new AvailableTaskTableModel(getTableColumnModel());
		}

		@Override
		public Object getValueAt(int row, int col) {
			AvailableTask task = getObject(row);
			switch(ATDCols.values()[col]) {
				case title: return task.getTaskDef().getTitle();
				case description: return task.getTaskDef().getDescription();
				case preview: return task.getDownloadLink();
				case select: return task.getEditableSubmission();
				default: return "ERROR";
			}
		}
	}
}

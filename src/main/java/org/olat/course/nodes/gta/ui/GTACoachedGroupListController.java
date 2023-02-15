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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DownloadLink;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskLight;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.TaskProcess;
import org.olat.course.nodes.gta.model.DueDate;
import org.olat.course.nodes.gta.model.TaskDefinition;
import org.olat.course.nodes.gta.ui.CoachGroupsTableModel.CGCols;
import org.olat.course.nodes.gta.ui.component.SubmissionDateCellRenderer;
import org.olat.course.nodes.gta.ui.component.TaskStatusCellRenderer;
import org.olat.course.nodes.gta.ui.events.SelectBusinessGroupEvent;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.co.ContactFormController;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTACoachedGroupListController extends GTACoachedListController {
	
	private FormLink bulkExtendButton;
	private FormLink bulkDownloadButton;
	private FormLink bulkEmailButton;
	private FlexiTableElement tableEl;
	private CoachGroupsTableModel tableModel;
	private final BreadcrumbPanel stackPanel;
	
	private CloseableModalController cmc;
	private GTACoachController coachingCtrl;
	private EditDueDatesController editDueDatesCtrl;
	private EditMultipleDueDatesController editMultipleDueDatesCtrl;
	private ContactFormController contactCtrl;
	
	private int count = 0;
	private final List<BusinessGroup> coachedGroups;
	private final UserCourseEnvironment coachCourseEnv;
	
	@Autowired
	private GTAManager gtaManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	
	public GTACoachedGroupListController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			UserCourseEnvironment coachCourseEnv, GTACourseNode gtaNode, List<BusinessGroup> coachedGroups) {
		super(ureq, wControl, coachCourseEnv.getCourseEnvironment(), gtaNode);
		this.coachedGroups = coachedGroups;
		this.coachCourseEnv = coachCourseEnv;
		this.stackPanel = stackPanel;
		initForm(ureq);
		initMultiSelectionTools(flc);
		updateModel();
	}
	
	public BusinessGroup getBusinessGroup(Long key) {
		for(BusinessGroup group:coachedGroups) {
			if(group.getKey().equals(key)) {
				return group;
			}
		}
		return null;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		super.initForm(formLayout, listener, ureq);
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CGCols.name, "select"));
		
		if(gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CGCols.taskTitle));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CGCols.taskName));
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CGCols.taskStatus,
				new TaskStatusCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CGCols.submissionDate,
				new SubmissionDateCellRenderer(gtaManager, getTranslator())));
		if(gtaManager.isDueDateEnabled(gtaNode)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.duedates", translate("duedates"), "duedates"));
		}
		tableModel = new CoachGroupsTableModel(columnsModel);

		tableEl = uifactory.addTableElement(getWindowControl(), "entries", tableModel, 10, false, getTranslator(), formLayout);
		tableEl.setElementCssClass("o_sel_course_gta_coached_groups");
		tableEl.setShowAllRowsEnabled(true);
		tableEl.setAndLoadPersistedPreferences(ureq, "gta-coached-groups-v2");
		tableEl.setMultiSelect(!coachCourseEnv.isCourseReadOnly());
		tableEl.setSelectAllEnable(true);
	}
	
	protected void initMultiSelectionTools(FormLayoutContainer formLayout) {
		if(gtaManager.isDueDateEnabled(gtaNode) && !gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_RELATIVE_DATES)) {
			bulkExtendButton = uifactory.addFormLink("extend.list", "duedates", "duedates", formLayout, Link.BUTTON);
			bulkExtendButton.setIconLeftCSS("o_icon o_icon-fw o_icon_extra_time");
			tableEl.addBatchButton(bulkExtendButton);
		}
		
		if (isDownloadAvailable()) {
			bulkDownloadButton = uifactory.addFormLink("batch.download", "bulk.download.title", null, formLayout, Link.BUTTON);
			bulkDownloadButton.setIconLeftCSS("o_icon o_icon-fw o_icon_export");
			tableEl.addBatchButton(bulkDownloadButton);
		}
		
		bulkEmailButton = uifactory.addFormLink("bulk.email", formLayout, Link.BUTTON);
		bulkEmailButton.setElementCssClass("o_sel_assessment_bulk_email");
		bulkEmailButton.setIconLeftCSS("o_icon o_icon-fw o_icon_mail");
		bulkEmailButton.setVisible(!coachCourseEnv.isCourseReadOnly());
		tableEl.addBatchButton(bulkEmailButton);
	}
	
	private boolean isDownloadAvailable() {
		ModuleConfiguration config = gtaNode.getModuleConfiguration();
		return config.getBooleanSafe(GTACourseNode.GTASK_SUBMIT)
				|| config.getBooleanSafe(GTACourseNode.GTASK_REVIEW_AND_CORRECTION)
				|| config.getBooleanSafe(GTACourseNode.GTASK_REVISION_PERIOD)
				|| config.getBooleanSafe(GTACourseNode.GTASK_GRADING);
	}

	public List<BusinessGroup> getCoachedGroups() {
		return coachedGroups;
	}
	
	protected void updateModel() {
		List<TaskDefinition> taskDefinitions = gtaManager.getTaskDefinitions(courseEnv, gtaNode);
		Map<String,TaskDefinition> fileNameToDefinitions = taskDefinitions.stream()
				.filter(def -> Objects.nonNull(def.getFilename()))
				.collect(Collectors.toMap(TaskDefinition::getFilename, Function.identity(), (u, v) -> u));
		File tasksFolder = gtaManager.getTasksDirectory(courseEnv, gtaNode);
		
		RepositoryEntry entry = courseEnv.getCourseGroupManager().getCourseEntry();
		List<TaskLight> tasks = gtaManager.getTasksLight(entry, gtaNode);
		Map<Long,TaskLight> groupToTasks = new HashMap<>();
		for(TaskLight task:tasks) {
			if(task.getBusinessGroupKey() != null) {
				groupToTasks.put(task.getBusinessGroupKey(), task);
			}
		}

		List<CoachedGroupRow> rows = new ArrayList<>(coachedGroups.size());
		for(BusinessGroup group:coachedGroups) {
			TaskLight task = groupToTasks.get(group.getKey());
			
			Date syntheticSubmissionDate = null;
			boolean hasSubmittedDocument = false;
			if(task != null && task.getTaskStatus() != null && task.getTaskStatus() != TaskProcess.assignment && task.getTaskStatus() != TaskProcess.submit) {
				syntheticSubmissionDate = getSyntheticSubmissionDate(task);
				if(syntheticSubmissionDate != null) {
					hasSubmittedDocument = hasSubmittedDocument(task);
				}
			}
			
			DueDate submissionDueDate = null;
			DueDate lateSubmissionDueDate = null;
			if(task != null && syntheticSubmissionDate != null) {
				DueDate dueDate = gtaManager.getSubmissionDueDate(task, null, group, gtaNode, entry, true);
				if(dueDate != null && dueDate.getDueDate() != null) {
					submissionDueDate = dueDate;
					DueDate lateDueDate = gtaManager.getLateSubmissionDueDate(task, null, group, gtaNode, entry, true);
					if(lateDueDate != null && lateDueDate.getDueDate() != null) {
						lateSubmissionDueDate = lateDueDate;
					}
				}
			}
			
			String taskName = task == null ? null : task.getTaskName();
			TaskDefinition taskDefinition = null;
			if(StringHelper.containsNonWhitespace(taskName)) {
				taskDefinition = fileNameToDefinitions.get(taskName);
			}
			
			CoachedGroupRow row = new CoachedGroupRow(group, task, taskDefinition, submissionDueDate, lateSubmissionDueDate, syntheticSubmissionDate, hasSubmittedDocument);
			if(taskDefinition != null) {
				File file = new File(tasksFolder, taskDefinition.getFilename());
				DownloadLink downloadLink = uifactory.addDownloadLink("task_" + (count++), taskDefinition.getFilename(), null, file, tableEl);
				row.setDownloadTaskFileLink(downloadLink);
			}
			rows.add(row);
		}
		
		tableModel.setObjects(rows);
		tableEl.reset();
	}
	
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(editDueDatesCtrl == source || editMultipleDueDatesCtrl == source) {
			if(event == Event.DONE_EVENT) {
				updateModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == contactCtrl) {
			if(cmc != null) {
				cmc.deactivate();
			}
			cleanUp();
		} else if(source == cmc) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(editMultipleDueDatesCtrl);
		removeAsListenerAndDispose(editDueDatesCtrl);
		removeAsListenerAndDispose(contactCtrl);
		removeAsListenerAndDispose(cmc);
		editMultipleDueDatesCtrl = null;
		editDueDatesCtrl = null;
		contactCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				CoachedGroupRow row = tableModel.getObject(se.getIndex());
				if("details".equals(cmd) || "select".equals(cmd)) {
					doSelect(ureq, row.getBusinessGroup());
				} else if("duedates".equals(cmd)) {
					doEditDueDate(ureq, row);
				}
			}
		} else if(bulkExtendButton == source) {
			List<CoachedGroupRow> rows = getSelectedRows(row -> true);
			doEditMultipleDueDates(ureq, rows);
		} else if(bulkDownloadButton == source) {
			doBulkDownload(ureq);
		} else if(bulkEmailButton == source) {
			doBulkEmail(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doSelect(UserRequest ureq, BusinessGroup businessGroup) {
		if(stackPanel == null) {
			fireEvent(ureq, new SelectBusinessGroupEvent(businessGroup));	
		} else {
			removeAsListenerAndDispose(coachingCtrl);
			
			coachingCtrl = new GTACoachController(ureq, getWindowControl(), courseEnv, gtaNode, coachCourseEnv, businessGroup, true, true, true, false);
			listenTo(coachingCtrl);
			stackPanel.pushController(businessGroup.getName(), coachingCtrl);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private List<CoachedGroupRow> getSelectedRows(Predicate<CoachedGroupRow> filter) {
		Set<Integer> selectedItems = tableEl.getMultiSelectedIndex();
		List<CoachedGroupRow> rows = new ArrayList<>(selectedItems.size());
		if(!selectedItems.isEmpty()) {
			for(Integer i:selectedItems) {
				int index = i.intValue();
				if(index >= 0 && index < tableModel.getRowCount()) {
					CoachedGroupRow row = tableModel.getObject(index);
					if(row != null && filter.test(row)) {
						rows.add(row);
					}
				}
			}
		}
		return rows;
	}
	
	private void doEditDueDate(UserRequest ureq, CoachedGroupRow row) {
		if(guardModalController(editDueDatesCtrl)) return;
		
		Task task;
		BusinessGroup assessedGroup = row.getBusinessGroup();
		RepositoryEntry entry = coachCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		if(row.getTask() == null) {
			TaskProcess firstStep = gtaManager.firstStep(gtaNode);
			TaskList taskList = gtaManager.getTaskList(entry, gtaNode);
			task = gtaManager.createAndPersistTask(null, taskList, firstStep, assessedGroup, null, gtaNode);
		} else {
			task = gtaManager.getTask(row.getTask());
		}

		editDueDatesCtrl = new EditDueDatesController(ureq, getWindowControl(), task, null, assessedGroup, gtaNode, entry, courseEnv);
		listenTo(editDueDatesCtrl);
		
		String title = translate("duedates.user", StringHelper.escapeHtml(assessedGroup.getName()));
		cmc = new CloseableModalController(getWindowControl(), "close", editDueDatesCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doEditMultipleDueDates(UserRequest ureq, List<CoachedGroupRow> rows) {
		if(guardModalController(editMultipleDueDatesCtrl)) return;
		
		if(rows.isEmpty()) {
			showWarning("error.atleast.task");
		} else {
			List<Task> tasks = new ArrayList<>(rows.size());
			RepositoryEntry entry = coachCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			for (CoachedGroupRow row : rows) {
				Task task;
				BusinessGroup assessedGroup = row.getBusinessGroup();
				if(row.getTask() == null) {
					TaskProcess firstStep = gtaManager.firstStep(gtaNode);
					TaskList taskList = gtaManager.getTaskList(entry, gtaNode);
					task = gtaManager.createAndPersistTask(null, taskList, firstStep, assessedGroup, null, gtaNode);
				} else {
					task = gtaManager.getTask(row.getTask());
				}
				tasks.add(task);
			}
	
			editMultipleDueDatesCtrl = new EditMultipleDueDatesController(ureq, getWindowControl(), tasks, gtaNode, entry, courseEnv);
			listenTo(editMultipleDueDatesCtrl);
			
			String title = translate("duedates.multiple.user");
			cmc = new CloseableModalController(getWindowControl(), "close", editMultipleDueDatesCtrl.getInitialComponent(), true, title, true);
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	private void doBulkDownload(UserRequest ureq) {
		List<BusinessGroup> groups = getSelectedRows(row -> true).stream().map(CoachedGroupRow::getBusinessGroup).toList();
		if(groups.isEmpty()) {
			showWarning("error.atleast.task");
		} else {
			OLATResource ores = courseEnv.getCourseGroupManager().getCourseResource();
			GroupBulkDownloadResource resource = new GroupBulkDownloadResource(gtaNode, ores, groups, getLocale());
			ureq.getDispatchResult().setResultingMediaResource(resource);
		}
	}
	
	private void doBulkEmail(UserRequest ureq) {
		List<BusinessGroup> groups = getSelectedRows(row -> true).stream().map(CoachedGroupRow::getBusinessGroup).toList();
		List<Identity> identities = businessGroupService.getMembers(groups, GroupRoles.participant.name());
		
		if (identities.isEmpty()) {
			showWarning("error.msg.send.no.rcps");
		} else {
			ContactMessage contactMessage = new ContactMessage(getIdentity());
			String name = courseEnv.getCourseGroupManager().getCourseEntry().getDisplayname();
			ContactList contactList = new ContactList(name);
			contactList.addAllIdentites(identities);
			contactMessage.addEmailTo(contactList);

			removeAsListenerAndDispose(contactCtrl);
			contactCtrl = new ContactFormController(ureq, getWindowControl(), true, false, false, contactMessage);
			listenTo(contactCtrl);

			cmc = new CloseableModalController(getWindowControl(), translate("close"),
					contactCtrl.getInitialComponent(), true, translate("bulk.email"));
			cmc.activate();
			listenTo(cmc);
		}
	}
	
}

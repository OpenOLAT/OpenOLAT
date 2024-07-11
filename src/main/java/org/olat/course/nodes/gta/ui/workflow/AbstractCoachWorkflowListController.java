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
package org.olat.course.nodes.gta.ui.workflow;

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

import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.model.IdentityRefImpl;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StickyActionColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableOneClickSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.panel.InfoPanelItem;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.TaskProcess;
import org.olat.course.nodes.gta.model.TaskDefinition;
import org.olat.course.nodes.gta.ui.EditDueDatesController;
import org.olat.course.nodes.gta.ui.EditMultipleDueDatesController;
import org.olat.course.nodes.gta.ui.GTACoachedGroupGradingController;
import org.olat.course.nodes.gta.ui.component.TaskStepStatusCellRenderer;
import org.olat.course.nodes.gta.ui.workflow.CoachedParticipantTableModel.CoachCols;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.co.ContactFormController;
import org.olat.repository.RepositoryEntry;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 24 juin 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
abstract class AbstractCoachWorkflowListController extends AbstractWorkflowListController {

	public static final String ALL_TAB_ID = "All";
	public static final String OPEN_TAB_ID = "Open";
	public static final String DONE_TAB_ID = "Done";
	public static final String PASSED_TAB_ID = "Passed";
	public static final String FAILED_TAB_ID = "Failed";
	public static final String WAITING_TAB_ID = "Waiting";
	public static final String EXPIRED_TAB_ID = "Expired";
	public static final String REVIEWED_TAB_ID = "Reviewed";
	public static final String AVAILABLE_TAB_ID = "Available";
	public static final String TO_RELEASE_TAB_ID = "ToRelease";
	public static final String NOT_AVAILABLE_TAB_ID = "NotAvailable";
	public static final String NEED_REVISIONS_TAB_ID = "NeedRevisions";
	public static final String REVISIONS_REVIEWED_TAB_ID = "Reviewed";
	public static final String ASSSIGNED_TO_ME_TAB_ID = "AssignedToMe";
	public static final String REVISION_AVAILABLE_TAB_ID = "RevisionsAvailable";
	
	public static final String TOOLS_CMD = "tools-task-list";
	
	protected FlexiFiltersTab allTab;
	protected FlexiFiltersTab asssignedToMeTab;

	protected static final String FILTER_PASSED = "passed";
	protected static final String FILTER_STATUS = "workflow-status";
	protected static final String FILTER_ASSIGNED_TO_ME = "assigned-to-me";
	protected static final String FILTER_NEED_REVISIONS = "need-revisions";
	protected static final String FILTER_REVIEWED = "reviewed";
	protected static final String FILTER_TO_RELEASE = "to-release";
	protected static final String FILTER_REVISIONS_REVIEWED = "revisions-reviewed";

	private FormLink bulkEmailButton;
	protected FormLink bulkExtendButton;
	protected FlexiTableElement tableEl;
	protected CoachedParticipantTableModel tableModel;
	
	private Controller toolsCtrl;
	protected CloseableModalController cmc;
	private ContactFormController contactCtrl;
	private EditDueDatesController editDueDatesCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private EditMultipleDueDatesController editMultipleDueDatesCtrl;

	AbstractCoachWorkflowListController(UserRequest ureq, WindowControl wControl, String pageName,
			UserCourseEnvironment coachCourseEnv, List<Identity> identities, GTACourseNode gtaNode) {
		super(ureq, wControl, pageName, coachCourseEnv,  identities, gtaNode);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		InfoPanelItem panel = uifactory.addInfoPanel("configuration", null, formLayout);
		panel.setTitle(translate("workflow.infos.configuration"));
		initConfigurationInfos(panel);
		
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		initUserColumnsModel(columnsModel);
		initColumnsModel(columnsModel);
		initAdministrationColumnsModel(columnsModel);
		
		tableModel = new CoachedParticipantTableModel(columnsModel, getIdentity(), getLocale());
		
		tableEl = uifactory.addTableElement(getWindowControl(), "identities", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(true);
		tableEl.setExportEnabled(true);
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);
		tableEl.setSearchEnabled(true);
		
		initFilters();
		initFiltersPresets(ureq);
		initBulkTools(formLayout);
	}
	
	protected abstract void initConfigurationInfos(InfoPanelItem panel);

	protected void initUserColumnsModel(FlexiTableColumnModel columnsModel) {
		int i=0;
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			int colIndex = GTACoachedGroupGradingController.USER_PROPS_OFFSET + i++;
			if (userPropertyHandler == null) continue;
			
			String propName = userPropertyHandler.getName();
			boolean visible = userManager.isMandatoryUserProperty(USER_PROPS_ID , userPropertyHandler);

			FlexiColumnModel col;
			if(UserConstants.FIRSTNAME.equals(propName)
					|| UserConstants.LASTNAME.equals(propName)) {
				col = new DefaultFlexiColumnModel(userPropertyHandler.i18nColumnDescriptorLabelKey(),
						colIndex, userPropertyHandler.getName(), true, propName,
						new StaticFlexiCellRenderer(userPropertyHandler.getName(), new TextFlexiCellRenderer()));
			} else {
				col = new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex, true, propName);
			}
			columnsModel.addFlexiColumnModel(col);
		}
	}
	
	protected abstract void initColumnsModel(FlexiTableColumnModel columnsModel);
	
	protected void initAdministrationColumnsModel(FlexiTableColumnModel columnsModel) {
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CoachCols.taskStepStatus,
				new TaskStepStatusCellRenderer(getTranslator())));
		
		StickyActionColumnModel toolsCol = new StickyActionColumnModel(CoachCols.tools);
		toolsCol.setExportable(false);
		toolsCol.setIconHeader("o_icon o_icon_actions o_icon-fw o_icon-lg");
		columnsModel.addFlexiColumnModel(toolsCol);
	}
	
	protected abstract void initFilters(List<FlexiTableExtendedFilter> filters);
	
	protected final void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>(2);
		
		initFilters(filters);

		if(assessmentConfig.hasCoachAssignment()) {
			SelectionValues statusValues = new SelectionValues();
			statusValues.add(SelectionValues.entry(FILTER_ASSIGNED_TO_ME, translate("filter.assigned.to.me")));
			filters.add(new FlexiTableOneClickSelectionFilter(translate("filter.assigned.to.me"),
					FILTER_ASSIGNED_TO_ME, statusValues, true));
		}

		tableEl.setFilters(true, filters, false, false);
	}
	
	protected abstract void initFiltersPresets(UserRequest ureq, List<FlexiFiltersTab> tabs);
	
	protected final void initFiltersPresets(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>();
		
		allTab = FlexiFiltersTabFactory.tabWithFilters(ALL_TAB_ID, translate("filter.all"),
				TabSelectionBehavior.clear, List.of());
		allTab.setFiltersExpanded(true);
		tabs.add(allTab);
		
		initFiltersPresets(ureq, tabs);
		
		if(assessmentConfig.hasCoachAssignment()) {
			asssignedToMeTab = FlexiFiltersTabFactory.tabWithImplicitFilters(ASSSIGNED_TO_ME_TAB_ID, translate("filter.assigned.to.me"),
					TabSelectionBehavior.clear, List.of(FlexiTableFilterValue.valueOf(FILTER_ASSIGNED_TO_ME, List.of(FILTER_ASSIGNED_TO_ME))));
			tabs.add(asssignedToMeTab);
		}
		
		tableEl.setFilterTabs(true, tabs);
		tableEl.setSelectedFilterTab(ureq, allTab);
		tableEl.setSearchEnabled(true);
	}

	protected final int getDefaultNumbersOfDocuments(String configKey, String fallbackConfigKey) {
		int docs = gtaNode.getModuleConfiguration().getIntegerSafe(configKey, -1);
		if(docs == -1) {
			// !this only works because there is not another configuration in the controller
			docs = gtaNode.getModuleConfiguration().getIntegerSafe(fallbackConfigKey, -1);
		}
		return docs;
	}
	
	protected void initBulkTools(FormItemContainer formLayout) {
		bulkEmailButton = uifactory.addFormLink("bulk.email", formLayout, Link.BUTTON);
		bulkEmailButton.setElementCssClass("o_sel_assessment_bulk_email");
		bulkEmailButton.setIconLeftCSS("o_icon o_icon-fw o_icon_mail");
		bulkEmailButton.setVisible(!coachCourseEnv.isCourseReadOnly());
		tableEl.addBatchButton(bulkEmailButton);
	}
	
	protected final void initBulkExtendTool(FormItemContainer formLayout) {
		ModuleConfiguration config = gtaNode.getModuleConfiguration();
		if(gtaManager.isDueDateEnabled(gtaNode) && !config.getBooleanSafe(GTACourseNode.GTASK_RELATIVE_DATES)) {
			bulkExtendButton = uifactory.addFormLink("extend.list", "duedates", "duedates", formLayout, Link.BUTTON);
			bulkExtendButton.setIconLeftCSS("o_icon o_icon-fw o_icon_extra_time");
			tableEl.addBatchButton(bulkExtendButton);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(editDueDatesCtrl == source || editMultipleDueDatesCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(toolsCtrl == source) {
			if(event == Event.DONE_EVENT) {
				toolsCalloutCtrl.deactivate();
				cleanUp();
			}
		} else if(contactCtrl == source) {
			cmc.deactivate();
			cleanUp();
		} else if(toolsCalloutCtrl == source || cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	protected void cleanUp() {
		removeAsListenerAndDispose(editMultipleDueDatesCtrl);
		removeAsListenerAndDispose(editDueDatesCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(contactCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(cmc);
		editMultipleDueDatesCtrl = null;
		editDueDatesCtrl = null;
		toolsCalloutCtrl = null;
		contactCtrl = null;
		toolsCtrl = null;
		cmc = null;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof FlexiTableSearchEvent || event instanceof FlexiTableFilterTabEvent) {
				filterModel();
			}
		} else if(bulkEmailButton == source) {
			doEmail(ureq);
		} else if(bulkExtendButton == source) {
			List<CoachedParticipantRow> rows = getSelectedRows(row -> true);
			doEditMultipleDueDates(ureq, rows);
		} else if(source instanceof FormLink link) {
			String cmd = link.getCmd();
			if(TOOLS_CMD.equals(cmd) && link.getUserObject() instanceof CoachedParticipantRow row) {
				doOpenTools(ureq, row, link);
			}
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	protected void filterModel() {
		tableModel.filter(tableEl.getQuickSearchString(), tableEl.getFilters());
		tableEl.reset(true, true, true);
	}
	
	protected void loadModel() {
		now = new Date();
		
		RepositoryEntry entry = courseEnv.getCourseGroupManager().getCourseEntry();
		TaskList taskList = gtaManager.createIfNotExists(entry, gtaNode);
		List<Task> tasks = gtaManager.getTasks(taskList, gtaNode);
		Map<Long,Task> identityToTasks = new HashMap<>(tasks.size());
		for(Task task:tasks) {
			if(task.getIdentity() != null) {
				identityToTasks.put(task.getIdentity().getKey(), task);
			}
		}
		
		List<AssessmentEntry> assessments = assessmentService.loadAssessmentEntriesBySubIdentWithCoach(entry, gtaNode.getIdent());
		Map<Long, AssessmentEntry> identityToAssessments = new HashMap<>(assessments.size());
		for(AssessmentEntry assessment:assessments) {
			if(assessment.getIdentity() != null) {
				identityToAssessments.put(assessment.getIdentity().getKey(), assessment);
			}
		}
		
		List<TaskDefinition> taskDefinitions = gtaManager.getTaskDefinitions(courseEnv, gtaNode);
		Map<String,TaskDefinition> fileNameToDefinitions = taskDefinitions.stream()
				.filter(def -> Objects.nonNull(def.getFilename()))
				.collect(Collectors.toMap(TaskDefinition::getFilename, Function.identity(), (u, v) -> u));
		
		List<CoachedParticipantRow> rows = new ArrayList<>(assessedIdentities.size());
		for(Identity assessedIdentity:assessedIdentities) {
			AssessmentEntry assessmentEntry = identityToAssessments.get(assessedIdentity.getKey());
			Task task = identityToTasks.get(assessedIdentity.getKey());
			String taskName = task == null ? null : task.getTaskName();
			TaskDefinition taskDefinition = null;
			if(StringHelper.containsNonWhitespace(taskName)) {
				taskDefinition = fileNameToDefinitions.get(taskName);
			}
			
			CoachedParticipantRow identityRow = new CoachedParticipantRow(assessedIdentity, task, taskDefinition,
					assessmentEntry, userPropertyHandlers, getLocale());
			forgeRow(identityRow, entry);
			rows.add(identityRow);
		}
		
		tableModel.setObjects(rows);
		tableEl.reset();
	}
	
	protected FormLink forgeToolsLink(CoachedParticipantRow identityRow) {
		FormLink toolsLink = uifactory.addFormLink("tools_" + (++count), TOOLS_CMD, "", null, null, Link.NONTRANSLATED);
		toolsLink.setIconLeftCSS("o_icon o_icon_actions o_icon-fws o_icon-lg");
		toolsLink.setUserObject(identityRow);
		return toolsLink;
	}
	
	protected abstract CoachedParticipantRow forgeRow(CoachedParticipantRow identityRow, RepositoryEntry entry);
	
	protected abstract Controller createToolsController(UserRequest ureq, CoachedParticipantRow row);

	protected void doOpenTools(UserRequest ureq, CoachedParticipantRow row, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		toolsCtrl = createToolsController(ureq, row);
		listenTo(toolsCtrl);

		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}
	
	protected void doEditDueDate(UserRequest ureq, CoachedParticipantRow row) {
		if(guardModalController(editDueDatesCtrl)) return;
		
		Task task;
		Identity assessedIdentity = securityManager.loadIdentityByKey(row.getIdentityKey());
		RepositoryEntry entry = courseEnv.getCourseGroupManager().getCourseEntry();
		if(row.getTask() == null) {
			TaskProcess firstStep = gtaManager.firstStep(gtaNode);
			TaskList taskList = gtaManager.getTaskList(entry, gtaNode);
			task = gtaManager.createAndPersistTask(null, taskList, firstStep, null, assessedIdentity, gtaNode);
		} else {
			task = gtaManager.getTask(row.getTask());
		}

		editDueDatesCtrl = new EditDueDatesController(ureq, getWindowControl(), task, assessedIdentity, null, gtaNode, entry, courseEnv);
		listenTo(editDueDatesCtrl);
		
		String fullname = userManager.getUserDisplayName(assessedIdentity);
		String title = translate("duedates.user", fullname);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), editDueDatesCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doEditMultipleDueDates(UserRequest ureq, List<CoachedParticipantRow> rows) {
		if(guardModalController(editMultipleDueDatesCtrl)) return;
		
		if(rows.isEmpty()) {
			showWarning("error.atleast.task");
		} else {
			List<Task> tasks = new ArrayList<>(rows.size());
			RepositoryEntry entry = coachCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			for (CoachedParticipantRow row : rows) {
				if(row.getTask() == null) {
					TaskProcess firstStep = gtaManager.firstStep(gtaNode);
					TaskList taskList = gtaManager.getTaskList(entry, gtaNode);
					tasks.add(gtaManager.createAndPersistTask(null, taskList, firstStep, null, securityManager.loadIdentityByKey(row.getIdentityKey()), gtaNode));
				} else {
					tasks.add(gtaManager.getTask(row.getTask()));
				}
			}
	
			editMultipleDueDatesCtrl = new EditMultipleDueDatesController(ureq, getWindowControl(), tasks, gtaNode, entry, courseEnv);
			listenTo(editMultipleDueDatesCtrl);
			
			String title = translate("duedates.multiple.user");
			cmc = new CloseableModalController(getWindowControl(), translate("close"), editMultipleDueDatesCtrl.getInitialComponent(), true, title, true);
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	private void doEmail(UserRequest ureq) {
		List<Identity> identities = getSelectedIdentities(row -> true);
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
	
	protected List<Identity> getSelectedIdentities(Predicate<CoachedParticipantRow> filter) {
		List<CoachedParticipantRow> selectedRows = getSelectedRows(filter);
		List<IdentityRef> refs = new ArrayList<>(selectedRows.size());
		for(CoachedParticipantRow row:selectedRows) {
			refs.add(new IdentityRefImpl(row.getIdentityKey()));
		}
		return securityManager.loadIdentityByRefs(refs);
	}
	
	protected List<CoachedParticipantRow> getSelectedRows(Predicate<CoachedParticipantRow> filter) {
		Set<Integer> selectedItems = tableEl.getMultiSelectedIndex();
		List<CoachedParticipantRow> rows = new ArrayList<>(selectedItems.size());
		if(!selectedItems.isEmpty()) {
			for(Integer i:selectedItems) {
				int index = i.intValue();
				if(index >= 0 && index < tableModel.getRowCount()) {
					CoachedParticipantRow row = tableModel.getObject(index);
					if(row != null && filter.test(row)) {
						rows.add(row);
					}
				}
			}
		}
		return rows;
	}
}

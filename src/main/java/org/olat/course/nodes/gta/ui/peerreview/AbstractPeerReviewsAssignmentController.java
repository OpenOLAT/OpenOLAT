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
package org.olat.course.nodes.gta.ui.peerreview;

import java.util.ArrayList;
import java.util.List;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.Util;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.GTAPeerReviewManager;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.TaskProcess;
import org.olat.course.nodes.gta.TaskReviewAssignment;
import org.olat.course.nodes.gta.ui.GTACoachController;
import org.olat.course.nodes.gta.ui.GTACoachedGroupGradingController;
import org.olat.course.nodes.gta.ui.component.TaskStepStatusCellRenderer;
import org.olat.course.nodes.gta.ui.peerreview.GTAPeerReviewAssignmentTableModel.AssignmentsCols;
import org.olat.modules.assessment.Role;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26 août 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractPeerReviewsAssignmentController extends FormBasicController {

	protected static final String ALL_TAB_ID = "All";
	protected static final String ASSIGNED_TAB_ID = "Assigned";
	protected static final String NOT_ASSIGNED_TAB_ID = "ANotAssignedll";
	protected static final String ASSIGNED = "Assigned";
	protected static final String NOT_ASSIGNED = "NotAssigned";
	protected static final String FILTER_ASSIGNMENT_STATUS = "assignment-status";
	protected static final String FILTER_TASK_NAME = "task-name";
	
	protected static final String ASSIGN = "assign";
	
	protected FlexiTableElement tableEl;
	protected GTAPeerReviewAssignmentTableModel tableModel;
	protected final SelectionValues assignmentPK;
	protected TaskStepStatusCellRenderer statusRenderer;
	
	protected int counter = 0;
	protected final TaskList taskList;
	protected final GTACourseNode gtaNode;
	protected final List<UserPropertyHandler> userPropertyHandlers;
	
	@Autowired
	protected GTAManager gtaManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	protected BaseSecurityModule securityModule;
	@Autowired
	protected GTAPeerReviewManager peerReviewManager;
	
	public AbstractPeerReviewsAssignmentController(UserRequest ureq, WindowControl wControl,
			TaskList taskList, RepositoryEntry courseEntry, GTACourseNode gtaNode) {
		super(ureq, wControl, "asssign_reviewers", Util.createPackageTranslator(GTACoachController.class, ureq.getLocale()));
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		this.taskList = taskList;
		this.gtaNode = gtaNode;
		
		Roles roles = ureq.getUserSession().getRoles();
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(GTACoachedGroupGradingController.USER_PROPS_ID, isAdministrativeUser);
		
		statusRenderer = new TaskStepStatusCellRenderer(courseEntry, gtaNode, gtaManager, getTranslator());
		
		assignmentPK = new SelectionValues();
		assignmentPK.add(SelectionValues.entry(ASSIGN, ""));
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		int i=0;
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			int colIndex = GTACoachedGroupGradingController.USER_PROPS_OFFSET + i++;
			if (userPropertyHandler == null) continue;
			
			String propName = userPropertyHandler.getName();
			boolean visible = userManager.isMandatoryUserProperty(GTACoachedGroupGradingController.USER_PROPS_ID , userPropertyHandler);

			FlexiColumnModel col = new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex, true, propName);
			columnsModel.addFlexiColumnModel(col);
		}

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AssignmentsCols.taskTitle));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AssignmentsCols.submissionStatus,
				statusRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AssignmentsCols.numberReviews));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AssignmentsCols.assignment));
		
		tableModel = new GTAPeerReviewAssignmentTableModel(columnsModel, getLocale());
		
		tableEl = uifactory.addTableElement(getWindowControl(), "assignments", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(true);
		tableEl.setSearchEnabled(true);
		
		uifactory.addFormSubmitButton("apply.assignments", formLayout);
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}
	
	protected final void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>(2);
		
		SelectionValues assignmentStatusKV = new SelectionValues();
		assignmentStatusKV.add(SelectionValues.entry(ASSIGNED, translate("filter.assignment.status.assigned")));
		assignmentStatusKV.add(SelectionValues.entry(NOT_ASSIGNED, translate("filter.assignment.status.not.assigned")));

		FlexiTableMultiSelectionFilter coachFilter = new FlexiTableMultiSelectionFilter(translate("filter.assignment.status"),
				FILTER_ASSIGNMENT_STATUS, assignmentStatusKV, true);
		filters.add(coachFilter);
		
		SelectionValues taskNamesKV = new SelectionValues();
		List<String> taskNames = tableModel.getTaskNames();
		for(String taskName:taskNames) {
			taskNamesKV.add(SelectionValues.entry(taskName, taskName));
		}
		FlexiTableMultiSelectionFilter taskFilter = new FlexiTableMultiSelectionFilter(translate("filter.taskname"),
				FILTER_TASK_NAME, taskNamesKV, true);
		filters.add(taskFilter);

		tableEl.setFilters(true, filters, false, false);
	}
	
	protected final void initFiltersPresets(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>();
		
		FlexiFiltersTab allTab = FlexiFiltersTabFactory.tabWithImplicitFilters(ALL_TAB_ID, translate("filter.assignment.status.all"),
				TabSelectionBehavior.clear, List.of());
		tabs.add(allTab);

		FlexiFiltersTab assignedTab = FlexiFiltersTabFactory.tabWithImplicitFilters(ASSIGNED_TAB_ID, translate("filter.assignment.status.assigned"),
				TabSelectionBehavior.clear, List.of(FlexiTableFilterValue.valueOf(FILTER_ASSIGNMENT_STATUS, ASSIGNED)));
		tabs.add(assignedTab);

		FlexiFiltersTab notAssignedTab = FlexiFiltersTabFactory.tabWithImplicitFilters(NOT_ASSIGNED_TAB_ID, translate("filter.assignment.status.not.assigned"),
				TabSelectionBehavior.clear, List.of(FlexiTableFilterValue.valueOf(FILTER_ASSIGNMENT_STATUS, NOT_ASSIGNED)));
		tabs.add(notAssignedTab);

		tableEl.setFilterTabs(true, tabs);
		tableEl.setSelectedFilterTab(ureq, allTab);
	}
	
	protected abstract void loadModel();
	
	protected final PeerReviewAssignmentRow decorateRow(Identity identity, TaskReviewAssignment assignment,
			PeerReviewAssignmentRow assignmentRow, List<TaskReviewAssignment> allAssignments) {

		MultipleSelectionElement assignmentEl = uifactory.addCheckboxesHorizontal("assignment-" + (counter++), null, flc,
				assignmentPK.keys(), assignmentPK.values());
		assignmentEl.setAjaxOnly(true);
		assignmentEl.setUserObject(assignmentRow);
		if(assignment != null && assignment.isAssigned()) {
			assignmentEl.select(ASSIGN, true);
		}
		assignmentRow.setAssignmentEl(assignmentEl);
		
		int numOfAwardedReviews = 0;
		int numOfReceivedReviews = 0;
		for(TaskReviewAssignment reviewAssignment:allAssignments) {
			if(reviewAssignment.getAssignee().equals(identity)) {
				numOfAwardedReviews++;
			}
			if(reviewAssignment.getTask().getIdentity().equals(identity)) {
				numOfReceivedReviews++;
			}
		}
		assignmentRow.setNumOfReviewers(numOfReceivedReviews);
		assignmentRow.setNumOfTasksToReviews(numOfAwardedReviews);
		
		return assignmentRow;
	}
	
	protected final TaskReviewAssignment getAssignmentFor(Task task, Identity assignee, List<TaskReviewAssignment> assignments) {
		for(TaskReviewAssignment assignment:assignments) {
			if(assignment.getTask().equals(task) && assignment.getAssignee().equals(assignee)) {
				return assignment;
			}
		}
		return null;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof FlexiTableSearchEvent
					||event instanceof FlexiTableFilterTabEvent) {
				tableModel.filter(tableEl.getQuickSearchString(), tableEl.getFilters());
				tableEl.reset(true, true, true);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	protected final void reopenTaskIfNeeded(Task task) {
		if(task.getTaskStatus().ordinal() > TaskProcess.peerreview.ordinal()) {
			gtaManager.updateTask(task, TaskProcess.peerreview, gtaNode, false, getIdentity(), Role.coach);
		}
	}
}

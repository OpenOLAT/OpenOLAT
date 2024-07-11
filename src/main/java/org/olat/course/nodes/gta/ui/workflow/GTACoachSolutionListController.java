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

import java.io.File;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.InfoPanelItem;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.duedate.DueDateConfig;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskHelper;
import org.olat.course.nodes.gta.TaskProcess;
import org.olat.course.nodes.gta.model.DueDate;
import org.olat.course.nodes.gta.ui.GTACoachController;
import org.olat.course.nodes.gta.ui.workflow.CoachedParticipantTableModel.CoachCols;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 28 juin 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTACoachSolutionListController extends AbstractCoachWorkflowListController {
	
	private final boolean hasSolutions;
	
	public GTACoachSolutionListController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment coachCourseEnv, List<Identity> assessableIdentities, GTACourseNode gtaNode) {
		super(ureq, wControl, "solution_list", coachCourseEnv, assessableIdentities, gtaNode);
		
		File documentsDir = gtaManager.getSolutionsDirectory(courseEnv, gtaNode);
		hasSolutions = TaskHelper.hasDocuments(documentsDir);
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initConfigurationInfos(InfoPanelItem panel) {
		StringBuilder infos = new StringBuilder();
		
		DueDateConfig dueDateConfig = gtaNode.getDueDateConfig(GTACourseNode.GTASK_SAMPLE_SOLUTION_VISIBLE_AFTER);
		if(dueDateConfig != DueDateConfig.noDueDateConfig()) {
			String dueDateVal = dueDateConfigToString(dueDateConfig);
			if(StringHelper.containsNonWhitespace(dueDateVal)) {
				String deadlineInfos = translate("workflow.deadline.solution", dueDateVal);
				infos.append("<p><i class='o_icon o_icon-fw o_icon_timelimit'> </i> ").append(deadlineInfos).append("</p>");
			}
		}
		
		boolean optional = gtaNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_OBLIGATION).equals(AssessmentObligation.optional.name());
		boolean solutionVisibleRelToAll = gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_SAMPLE_SOLUTION_VISIBLE_ALL, false);
		String visibleVal = "";
		if(solutionVisibleRelToAll) {
			if(optional) {
				visibleVal = translate("sample.solution.visible.all.optional");
			} else {
				visibleVal = translate("sample.solution.visible.all");
				
			}
		} else {
			visibleVal = translate("sample.solution.visible.upload");
		}
		visibleVal = translate("workflow.infos.solutions", visibleVal);
		infos.append("<p><i class='o_icon o_icon-fw o_icon_eye'> </i> ").append(visibleVal).append("</p>");

		panel.setInformations(infos.toString());
	}

	@Override
	protected void initColumnsModel(FlexiTableColumnModel columnsModel) {
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CoachCols.solutionOverrideDueDate));
	}

	@Override
	protected void initFilters(List<FlexiTableExtendedFilter> filters) {
		SelectionValues assignmentStatusPK = new SelectionValues();
		assignmentStatusPK.add(SelectionValues.entry(CoachedParticipantStatus.solutionAvailable.name(), translate(CoachedParticipantStatus.solutionAvailable.i18nKey())));
		assignmentStatusPK.add(SelectionValues.entry(CoachedParticipantStatus.notAvailable.name(), translate(CoachedParticipantStatus.notAvailable.i18nKey())));
		FlexiTableMultiSelectionFilter assignmentStatusFilter = new FlexiTableMultiSelectionFilter(translate("filter.assignment.status"),
				FILTER_STATUS, assignmentStatusPK, true);
		filters.add(assignmentStatusFilter);
	}

	@Override
	protected void initFiltersPresets(UserRequest ureq, List<FlexiFiltersTab> tabs) {
		FlexiFiltersTab availableTab = FlexiFiltersTabFactory.tabWithImplicitFilters(AVAILABLE_TAB_ID, translate(CoachedParticipantStatus.solutionAvailable.i18nKey()),
				TabSelectionBehavior.clear, List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS, List.of(CoachedParticipantStatus.solutionAvailable.name()))));
		tabs.add(availableTab);
		
		FlexiFiltersTab notAvailableTab = FlexiFiltersTabFactory.tabWithImplicitFilters(NOT_AVAILABLE_TAB_ID, translate(CoachedParticipantStatus.notAvailable.i18nKey()),
				TabSelectionBehavior.clear, List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS, List.of(CoachedParticipantStatus.notAvailable.name()))));
		tabs.add(notAvailableTab);
	}
	
	@Override
	protected void initBulkTools(FormItemContainer formLayout) {
		initBulkExtendTool(formLayout);
		super.initBulkTools(formLayout);
	}

	@Override
	protected CoachedParticipantRow forgeRow(CoachedParticipantRow identityRow, RepositoryEntry entry) {
		identityRow.setToolsLink(forgeToolsLink(identityRow));
		
		DueDate solutionDueDate = getSolutionDueDate(identityRow.getTask(), identityRow.getAssessedIdentity());
		identityRow.setSolutionDueDate(solutionDueDate);
		
		status(identityRow);
		return identityRow;
	}
	
	private void status(CoachedParticipantRow identityRow) {
		Task assignedTask = identityRow.getTask();
		Identity assessedIdentity = identityRow.getAssessedIdentity();
		ModuleConfiguration config = gtaNode.getModuleConfiguration();
		
		DueDate availableDate = identityRow.getSolutionDueDate();
		if(config.getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT)
				|| config.getBooleanSafe(GTACourseNode.GTASK_SUBMIT)
				|| config.getBooleanSafe(GTACourseNode.GTASK_REVIEW_AND_CORRECTION)
				|| config.getBooleanSafe(GTACourseNode.GTASK_PEER_REVIEW)
				|| config.getBooleanSafe(GTACourseNode.GTASK_REVISION_PERIOD)) {
			
			if(assignedTask == null || assignedTask.getTaskStatus() == TaskProcess.assignment || assignedTask.getTaskStatus() == TaskProcess.submit
					|| assignedTask.getTaskStatus() == TaskProcess.review || assignedTask.getTaskStatus() == TaskProcess.correction
					|| assignedTask.getTaskStatus() == TaskProcess.revision || assignedTask.getTaskStatus() == TaskProcess.peerreview) {

				identityRow.setStatus(CoachedParticipantStatus.notAvailable);
			} else if(assignedTask.getTaskStatus() == TaskProcess.solution) {
				if(isSolutionVisible(identityRow) && showSolutions(availableDate, assessedIdentity)) {

					identityRow.setStatus(CoachedParticipantStatus.solutionAvailable);
				} else {
					identityRow.setStatus(CoachedParticipantStatus.notAvailable);
				}
			} else {
				identityRow.setStatus(CoachedParticipantStatus.solutionAvailable);
			}	
		} else if (assignedTask == null || assignedTask.getTaskStatus() == TaskProcess.solution) {
			if(isSolutionVisible(identityRow) && showSolutions(availableDate, assessedIdentity)) {
				identityRow.setStatus(CoachedParticipantStatus.solutionAvailable);
			} else {
				identityRow.setStatus(CoachedParticipantStatus.notAvailable);
			}
		} else {
			identityRow.setStatus(CoachedParticipantStatus.solutionAvailable);
		}
	}
	
	protected final boolean isSolutionVisible(CoachedParticipantRow identityRow) {
		DueDate availableDate = identityRow.getSolutionDueDate();
		boolean visible = availableDate == null || 
				(availableDate.getDueDate() != null && availableDate.getDueDate().compareTo(now) <= 0);

		return visible && hasSolutions;
	}
	
	protected DueDate getSolutionDueDate(Task assignedTask, Identity assessedIdentity) {
		RepositoryEntry entry = courseEnv.getCourseGroupManager().getCourseEntry();
		return gtaManager.getSolutionDueDate(assignedTask, assessedIdentity, null, gtaNode, entry, true);
	}
	
	protected final boolean showSolutions(DueDate availableDate, Identity assessedIdentity) {
		boolean show = false;
		boolean optional = false;
		if(gtaNode.isNodeOptional(courseEnv, getIdentity())) {
			ICourse course = CourseFactory.loadCourse(courseEnv.getCourseResourceableId());
			UserCourseEnvironment userCourseEnv = AssessmentHelper.createAndInitUserCourseEnvironment(assessedIdentity, course);
			optional = gtaNode.isOptional(courseEnv, userCourseEnv);
		}
		if(gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_SUBMIT)) {
			if(availableDate == null && !optional) {
				show = true;
			} else if(availableDate == null && optional
					&& (gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_SAMPLE_SOLUTION_VISIBLE_ALL, false) || hasSolutions)) {
				show = true;
			} else if(availableDate != null && (optional || !availableDate.isRelative())
					&& (gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_SAMPLE_SOLUTION_VISIBLE_ALL, false) || hasSolutions)) {
				show = true;
			}
		} else if(optional) {
			show = gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_SAMPLE_SOLUTION_VISIBLE_ALL, false);
		} else {
			show = true;
		}
		return show;
	}

	@Override
	protected Controller createToolsController(UserRequest ureq, CoachedParticipantRow row) {
		return new SolutionToolsController(ureq, getWindowControl(), row);
	}
	
	private class SolutionToolsController extends BasicController {

		private Link dueDatesLink;
		
		private CoachedParticipantRow row;
		
		public SolutionToolsController(UserRequest ureq, WindowControl wControl, CoachedParticipantRow row) {
			super(ureq, wControl, Util.createPackageTranslator(GTACoachController.class, ureq.getLocale()));
			this.row = row;
			
			VelocityContainer mainVC = createVelocityContainer("tools");
			
			dueDatesLink = LinkFactory.createLink("duedates", "duedates", getTranslator(), mainVC, this, Link.LINK);
			dueDatesLink.setIconLeftCSS("o_icon o_icon-fw o_icon_extra_time");

			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.DONE_EVENT);
			if(dueDatesLink == source) {
				doEditDueDate(ureq, row);
			}
		}
	}
}

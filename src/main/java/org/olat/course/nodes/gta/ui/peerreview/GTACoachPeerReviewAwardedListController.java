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
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StickyActionColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TreeNodeFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAPeerReviewManager;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.TaskReviewAssignment;
import org.olat.course.nodes.gta.TaskReviewAssignmentStatus;
import org.olat.course.nodes.gta.model.SessionParticipationListStatistics;
import org.olat.course.nodes.gta.model.SessionParticipationStatistics;
import org.olat.course.nodes.gta.model.SessionStatistics;
import org.olat.course.nodes.gta.ui.GTACoachController;
import org.olat.course.nodes.gta.ui.component.TaskReviewAssignmentStatusCellRenderer;
import org.olat.course.nodes.gta.ui.peerreview.GTACoachPeerReviewTreeTableModel.CoachReviewCols;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10 juin 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTACoachPeerReviewAwardedListController extends AbstractCoachPeerReviewListController {
	
	private final TaskList taskList;
	private final Identity reviewer;
	
	private ToolsController toolsCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private GTAPeerReviewManager peerReviewManager;
	
	public GTACoachPeerReviewAwardedListController(UserRequest ureq, WindowControl wControl,
			TaskList taskList, Identity reviewer, CourseEnvironment courseEnv,
			RepositoryEntry courseEntry, GTACourseNode gtaNode) {
		super(ureq, wControl, courseEnv, courseEntry, gtaNode);
		this.reviewer = reviewer;
		this.taskList = taskList;
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();

		FlexiCellRenderer nodeRenderer = new TreeNodeFlexiCellRenderer(new FullNameNodeRenderer());
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CoachReviewCols.identityFullName, nodeRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CoachReviewCols.numOfReviews));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CoachReviewCols.progress));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CoachReviewCols.plot));
		if(isSumConfigured()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CoachReviewCols.sum));
		}
		if(isAverageConfigured()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CoachReviewCols.average));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CoachReviewCols.sessionStatus,
				new TaskReviewAssignmentStatusCellRenderer(getLocale(), true)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.review.view", translate("review.view"), "view"));
		
		StickyActionColumnModel toolsCol = new StickyActionColumnModel(CoachReviewCols.tools);
		toolsCol.setIconHeader("o_icon o_icon-fw o_icon_actions");
		toolsCol.setExportable(false);
		columnsModel.addFlexiColumnModel(toolsCol);
		
		tableModel = new GTACoachPeerReviewTreeTableModel(columnsModel, getLocale());
		
		tableEl = uifactory.addTableElement(getWindowControl(), "reviews", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(true);
		tableEl.setSearchEnabled(true);
		initFilters();
		initFiltersPresets(ureq);
	}
	
	@Override
	protected void loadModel() {
		String reviewerFullName = userManager.getUserDisplayName(reviewer);
		CoachPeerReviewRow surveyExecutorIdentityRow = new CoachPeerReviewRow(reviewerFullName);
		List<CoachPeerReviewRow> rows = new ArrayList<>();
		List<CoachPeerReviewRow> sessionRows = new ArrayList<>();
		rows.add(surveyExecutorIdentityRow);
		surveyExecutorIdentityRow.setChildrenRows(sessionRows);
		
		List<TaskReviewAssignment> assignments = peerReviewManager.getAssignmentsOfReviewer(taskList, reviewer);
		List<TaskReviewAssignmentStatus> statusForStats = List.of(TaskReviewAssignmentStatus.done);
		SessionParticipationListStatistics statistics = peerReviewManager.loadStatistics(taskList, assignments, reviewer, gtaNode, statusForStats);
		Map<EvaluationFormParticipation, SessionParticipationStatistics> statisticsMap = statistics.toParticipationsMap();
		
		for(TaskReviewAssignment assignment : assignments) {
			SessionParticipationStatistics sessionStatistics = null;
			if(assignment.getParticipation() != null) {
				sessionStatistics = statisticsMap.get(assignment.getParticipation());
			}
			CoachPeerReviewRow sessionRow = forgeSessionRow(assignment, sessionStatistics);
			sessionRow.setParent(surveyExecutorIdentityRow);
			rows.add(sessionRow);
			sessionRows.add(sessionRow);
		}
		
		// Fill statistics
		forgeSurveyExecutorIdentityRow(surveyExecutorIdentityRow, statistics.aggregatedStatistics());

		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private void forgeSurveyExecutorIdentityRow(CoachPeerReviewRow surveyExecutorIdentityRow, SessionStatistics aggregatedStatistics) {
		decorateWithAggregatedStatistics(surveyExecutorIdentityRow, aggregatedStatistics);
		decorateWithTools(surveyExecutorIdentityRow);
	}
	
	private CoachPeerReviewRow forgeSessionRow(TaskReviewAssignment assignment, SessionParticipationStatistics sessionStatistics) {
		Identity assessedIdentity = assignment.getTask().getIdentity();
		String assessedIdentityFullName = userManager.getUserDisplayName(assessedIdentity);
		CoachPeerReviewRow sessionRow = new CoachPeerReviewRow(assignment, assessedIdentityFullName, false);

		decorateWithStatistics(sessionRow, sessionStatistics);
		decorateWithTools(sessionRow);
		return sessionRow;
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(toolsCtrl == source) {
			toolsCalloutCtrl.deactivate();
			cleanUp();
		} else if(toolsCalloutCtrl == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void cleanUp() {
		super.cleanUp();
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		toolsCalloutCtrl = null;
		toolsCtrl = null;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent se) {
				CoachPeerReviewRow row = tableModel.getObject(se.getIndex());
				if(row.getParent() == null) {
					doCompareEvaluationFormSessions(ureq, row);
				} else {
					doViewEvaluationFormSession(ureq, row);
				}
			}
		} else if(source instanceof FormLink link && link.getUserObject() instanceof CoachPeerReviewRow row
				&& "tools".equals(link.getCmd())) {
			doOpenTools(ureq, row, link);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private void doOpenTools(UserRequest ureq, CoachPeerReviewRow row, FormLink link) {
		if(toolsCtrl != null) return;
		
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
		listenTo(toolsCtrl);

		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}
	

	private class ToolsController extends BasicController {
		private Link showReviewLink;
		private Link showReviewsLink;
		private Link invalidateReviewLink;
		private Link reopenReviewLink;
		private VelocityContainer mainVC;
		
		private CoachPeerReviewRow row;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, CoachPeerReviewRow row) {
			super(ureq, wControl, Util.createPackageTranslator(GTACoachController.class, ureq.getLocale()));
			this.row = row;
			
			mainVC = createVelocityContainer("tools");

			if(row.getParent() == null) {
				showReviewsLink = LinkFactory.createLink("show.reviews", "show.reviews", getTranslator(), mainVC, this, Link.LINK);
				showReviewsLink.setIconLeftCSS("o_icon o_icon-fw o_icon_eye");
			} else {
				showReviewLink = LinkFactory.createLink("show.review", "show.review", getTranslator(), mainVC, this, Link.LINK);
				showReviewLink.setIconLeftCSS("o_icon o_icon-fw o_icon_eye");
				
				invalidateReviewLink = LinkFactory.createLink("invalidate.review", "invalidate.review", getTranslator(), mainVC, this, Link.LINK);
				invalidateReviewLink.setIconLeftCSS("o_icon o_icon-fw o_icon_invalidate");
				
				reopenReviewLink = LinkFactory.createLink("reopen.review", "reopen.review", getTranslator(), mainVC, this, Link.LINK);
				reopenReviewLink.setIconLeftCSS("o_icon o_icon-fw o_icon_reopen");
			}
			
			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			if(showReviewsLink == source) {
				fireEvent(ureq, Event.DONE_EVENT);
				doCompareEvaluationFormSessions(ureq, row);
			} else if(showReviewLink == source) {
				fireEvent(ureq, Event.DONE_EVENT);
				doViewEvaluationFormSession(ureq, row);
			} else if(invalidateReviewLink == source) {
				fireEvent(ureq, Event.DONE_EVENT);
				doInvalidateReview(ureq, row);
			} else if(reopenReviewLink == source) {
				fireEvent(ureq, Event.DONE_EVENT);
				doReopenReview(ureq, row);
			}
		}
	}
}

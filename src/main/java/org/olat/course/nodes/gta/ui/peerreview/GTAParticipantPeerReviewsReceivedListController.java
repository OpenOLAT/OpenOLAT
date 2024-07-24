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
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.boxplot.BoxPlot;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.ComponentWrapperElement;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.widget.ComponentWidget;
import org.olat.core.gui.components.widget.FigureWidget;
import org.olat.core.gui.components.widget.WidgetFactory;
import org.olat.core.gui.components.widget.WidgetGroup;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAPeerReviewManager;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskReviewAssignment;
import org.olat.course.nodes.gta.TaskReviewAssignmentStatus;
import org.olat.course.nodes.gta.model.SessionParticipationListStatistics;
import org.olat.course.nodes.gta.model.SessionParticipationStatistics;
import org.olat.course.nodes.gta.model.SessionStatistics;
import org.olat.course.nodes.gta.ui.peerreview.GTAParticipantPeerReviewTableModel.ReviewCols;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.portfolio.ui.MultiEvaluationFormController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Received reviews by the participant
 * 
 * Initial date: 6 juin 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTAParticipantPeerReviewsReceivedListController extends AbstractParticipantPeerReviewsAwardedListController {
	
	private static final String CMD_VIEW = "view";
	
	private BoxPlot assessmentsPlot;
	private WidgetGroup widgetGroup;
	private FormItem widgetGroupItem;
	private FigureWidget reviewersWidget;
	private ComponentWidget assessmentsWidget;
	private ComponentWidget viewAllRubricsWidget;
	private FormLink viewAllRubricsButton;
	private FlexiTableElement tableEl;
	private GTAParticipantPeerReviewTableModel tableModel;
	
	private int counter = 0;
	private Task task;
	private final boolean withYesNoRating;
	private final boolean withStarsRating;
	private final EvaluationFormSurvey survey;
	private final CourseEnvironment courseEnv;
	
	private CloseableModalController cmc;
	private MultiEvaluationFormController multiEvaluationFormCtrl;
	private GTAEvaluationFormExecutionController evaluationFormExecCtrl;
	
	@Autowired
	private GTAPeerReviewManager peerReviewManager;
	
	public GTAParticipantPeerReviewsReceivedListController(UserRequest ureq, WindowControl wControl, Task task,
			CourseEnvironment courseEnv, GTACourseNode gtaNode) {
		super(ureq, wControl, "peer_review_received_list", gtaNode);
		
		this.task = task;
		this.courseEnv = courseEnv;
		survey = peerReviewManager.loadSurvey(task);
		
		boolean qualityFeedback = gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_PEER_REVIEW_QUALITY_FEEDBACK, false);
		String qualityFeedbackType = gtaNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_PEER_REVIEW_QUALITY_FEEDBACK_TYPE,
					GTACourseNode.GTASK_VALUE_PEER_REVIEW_QUALITY_FEEDBACK_YES_NO);
		withYesNoRating = qualityFeedback && GTACourseNode.GTASK_VALUE_PEER_REVIEW_QUALITY_FEEDBACK_YES_NO.equals(qualityFeedbackType);
		withStarsRating = qualityFeedback && GTACourseNode.GTASK_VALUE_PEER_REVIEW_QUALITY_FEEDBACK_STARS.equals(qualityFeedbackType);

		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initWidgetsForm(formLayout);
		initTable(formLayout);
	}
	
	private void initWidgetsForm(FormItemContainer formLayout) {

		assessmentsPlot = new BoxPlot("plot-assessments", 0, 0f, 0f, 0f, 0f, 0f, 0f, null);
		assessmentsWidget = WidgetFactory.createComponentWidget("assessments", null, translate("review.all.assessment"), "o_icon_success_status");
		assessmentsWidget.setContent(assessmentsPlot);
		
		reviewersWidget = WidgetFactory.createFigureWidget("reviewers", null, translate("reviewer"), "o_icon_group");
		reviewersWidget.setValueCssClass("o_sel_reviewers");
		setNumberOfReviewers(0);
		
		viewAllRubricsButton = uifactory.addFormLink("view.all.assessment", formLayout, Link.BUTTON);
		viewAllRubricsButton.setElementCssClass("btn-primary");
		viewAllRubricsWidget = WidgetFactory.createComponentWidget("view.all.assessments", null, translate("all.assessment"), "o_icon_view_all_rubrics");
		viewAllRubricsWidget.setContent(viewAllRubricsButton.getComponent());
		
		widgetGroup = WidgetFactory.createWidgetGroup("results", null);
		widgetGroup.add(assessmentsWidget);
		widgetGroup.add(reviewersWidget);
		widgetGroup.add(viewAllRubricsWidget);
		
		widgetGroupItem = new ComponentWrapperElement(widgetGroup);
		formLayout.add("widgets", widgetGroupItem);
	}
	
	private void initTable(FormItemContainer formLayout) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReviewCols.reviewerIdentity));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReviewCols.plot));
		if(withYesNoRating) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReviewCols.ratingYesNo));
		} else if(withStarsRating) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReviewCols.ratingStars));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReviewCols.viewReview));
		
		tableModel = new GTAParticipantPeerReviewTableModel(columnsModel, getLocale());
		
		tableEl = uifactory.addTableElement(getWindowControl(), "received.reviews", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(false);
	}
	
	private void loadModel() {
		List<TaskReviewAssignment> assignments = peerReviewManager.getAssignmentsForTask(task);
		setNumberOfReviewers(assignments.size());

		List<TaskReviewAssignmentStatus> statusForStats = List.of(TaskReviewAssignmentStatus.done);
		SessionParticipationListStatistics statisticsList = peerReviewManager.loadStatistics(task, assignments, gtaNode, statusForStats);
		Map<EvaluationFormParticipation, SessionParticipationStatistics> statisticsMap = statisticsList.toParticipationsMap();
			
		loadTableModel(assignments, statisticsMap);
		loadBoxPlotModel(statisticsList.aggregatedStatistics());
	}

	private void loadTableModel(List<TaskReviewAssignment> assignments,
			Map<EvaluationFormParticipation, SessionParticipationStatistics> statisticsMap) {
		int count = 0;
		List<ParticipantPeerReviewAssignmentRow> rows = new ArrayList<>(assignments.size());
		for(TaskReviewAssignment assignment:assignments) {
			SessionParticipationStatistics participation = null;
			if(assignment.getParticipation() != null) {
				participation = statisticsMap.get(assignment.getParticipation());
			}
			rows.add(forgeRow(assignment, assignment.getTask(), participation, ++count));
		}
		
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	private void loadBoxPlotModel(SessionStatistics sessionStatistics) {
		double firstQuartile = 0.0d;
		double thirdQuartile = 0.0d;
		double median = 0.0d;
		double min = 0.0d;
		double max = 0.0d;
		double average = 0.0d;
		int maxSteps = 5;
		
		if(sessionStatistics != null) {
			min = sessionStatistics.min();
			max = sessionStatistics.max();
			average = sessionStatistics.average();
			maxSteps = sessionStatistics.maxSteps();
			if(sessionStatistics.numOfQuestions() > 10) {
				firstQuartile = sessionStatistics.firstQuartile();
				median = sessionStatistics.median();
				thirdQuartile = sessionStatistics.thirdQuartile();
			}
		}

		assessmentsPlot = new BoxPlot("plot-assessments", maxSteps,
				(float)min, (float)max, (float)average,
				(float)firstQuartile, (float)thirdQuartile, (float)median, null);
		assessmentsWidget.setContent(assessmentsPlot);
	}
	
	@Override
	protected ParticipantPeerReviewAssignmentRow forgeRow(TaskReviewAssignment assignment, Task task, SessionParticipationStatistics statistics, int pos) {
		ParticipantPeerReviewAssignmentRow row = super.forgeRow(assignment, task, statistics, pos);
		
		String id = Integer.toString(counter++);
		// View evaluation link
		String viewLinkName = "view-".concat(id);
		FormLink viewLink = uifactory.addFormLink(viewLinkName, CMD_VIEW, "review.view", null, flc, Link.LINK);
		viewLink.setUserObject(row);
		row.setViewSessionLink(viewLink);
		
		FormItem ratingItem = null;
		float rating = assignment.getRating() == null ? 0.0f : assignment.getRating().floatValue();
		if(withYesNoRating) {
			ratingItem = uifactory.addRatingItemYesNo("rating-".concat(id), null, rating, 5, false, null);
		} else if(withStarsRating) {
			ratingItem = uifactory.addRatingItem("rating-".concat(id), null, rating, 5, false, null);
		}
		row.setRatingItem(ratingItem);

		return row;
	}
	
	private void setNumberOfReviewers(int val) {
		reviewersWidget.setValue(String.valueOf(val));
		String reviewerI18nKey = val > 1 ? "reviewers" : "reviewer";
		reviewersWidget.setDesc(translate(reviewerI18nKey));
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(evaluationFormExecCtrl == source) {
			loadModel();
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(evaluationFormExecCtrl);
		removeAsListenerAndDispose(cmc);
		evaluationFormExecCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(viewAllRubricsButton == source) {
			doViewAllRubrics(ureq);
		} else if(source instanceof FormLink link && CMD_VIEW.equals(link.getCmd())
				&& link.getUserObject() instanceof ParticipantPeerReviewAssignmentRow sessionRow) {
			doViewReview(ureq, sessionRow);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doViewReview(UserRequest ureq, ParticipantPeerReviewAssignmentRow sessionRow) {
		removeAsListenerAndDispose(evaluationFormExecCtrl);
		
		String mode = gtaNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_PEER_REVIEW_FORM_OF_REVIEW,
				GTACourseNode.GTASK_PEER_REVIEW_FORM_OF_REVIEW_DEFAULT);
		boolean anonym = GTACourseNode.GTASK_PEER_REVIEW_DOUBLE_BLINDED_REVIEW.equals(mode)
				|| GTACourseNode.GTASK_PEER_REVIEW_SINGLE_BLINDED_REVIEW.equals(mode);
		String reviewerFullName = sessionRow.getReviewerName();

		boolean qualityFeedback = gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_PEER_REVIEW_QUALITY_FEEDBACK, false);
		boolean canRate = qualityFeedback && (sessionRow.getStatus() == TaskReviewAssignmentStatus.done);
		
		GTAEvaluationFormExecutionOptions options = GTAEvaluationFormExecutionOptions.valueOf(false, true, anonym, reviewerFullName, false, false, canRate);
		
		TaskReviewAssignment assignment = sessionRow.getAssignment();
		evaluationFormExecCtrl = new GTAEvaluationFormExecutionController(ureq, getWindowControl(),
				assignment, courseEnv, gtaNode, options, false, false);
		listenTo(evaluationFormExecCtrl);
		
		String title = translate("review.assessment.title", reviewerFullName);
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				evaluationFormExecCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doViewAllRubrics(UserRequest ureq) {
		boolean anonym = true;
		
		List<Identity> otherReviewers = tableModel.getObjects().stream().map(ParticipantPeerReviewAssignmentRow::getReviewer)
			.collect(Collectors.toList());
		
		multiEvaluationFormCtrl = new MultiEvaluationFormController(ureq, getWindowControl(), getIdentity(),
				otherReviewers, survey, false, true, true, anonym);
		
		String title = translate("review.assessment.all.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				multiEvaluationFormCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
}

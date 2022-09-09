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
package org.olat.ims.qti21.ui.statistics;

import static org.olat.ims.qti21.ui.statistics.StatisticFormatter.duration;
import static org.olat.ims.qti21.ui.statistics.StatisticFormatter.format;
import static org.olat.ims.qti21.ui.statistics.StatisticFormatter.getModeString;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.chart.BarSeries;
import org.olat.core.gui.components.chart.BarSeries.Stringuified;
import org.olat.core.gui.components.chart.StatisticsComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.link.LinkPopupSettings;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.Identity;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.nodes.QTICourseNode;
import org.olat.ims.qti21.QTI21StatisticsManager;
import org.olat.ims.qti21.model.statistics.AssessmentItemStatistic;
import org.olat.ims.qti21.model.statistics.StatisticAssessment;
import org.olat.modules.assessment.ui.UserFilterController;
import org.olat.modules.assessment.ui.event.UserFilterEvent;
import org.olat.modules.grade.GradeModule;
import org.olat.modules.grade.GradeScale;
import org.olat.modules.grade.GradeService;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21AssessmentTestStatisticsController extends BasicController implements Activateable2, TooledController {

	private final VelocityContainer mainVC;
	private final TooledStackedPanel stackPanel;
	private final Link printLink;
	private final Link downloadRawLink;
	
	private UserFilterController filterCtrl;
	
	private QTICourseNode courseNode;
	private final QTI21StatisticResourceResult resourceResult;
	private final boolean withDiagramm;
	
	@Autowired
	private QTI21StatisticsManager qtiStatisticsManager;
	@Autowired
	private CourseAssessmentService courseAssessmentService;

	public QTI21AssessmentTestStatisticsController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			QTI21StatisticResourceResult resourceResult, boolean withFilter, boolean printMode, boolean withDiagramm) {
		super(ureq, wControl);
		this.stackPanel = stackPanel;
		this.resourceResult = resourceResult;
		this.withDiagramm = withDiagramm;
		courseNode = resourceResult.getTestCourseNode();

		mainVC = createVelocityContainer("statistics_assessment_test");
		mainVC.put("loadd3js", new StatisticsComponent("d3loader"));
		mainVC.contextPut("printMode", Boolean.valueOf(printMode));
		if(resourceResult.getCourseEntry() != null) {
			mainVC.contextPut("courseId", resourceResult.getCourseEntry().getKey());
		}
		mainVC.contextPut("testId", resourceResult.getTestEntry().getKey());
		if(stackPanel != null) {
			printLink = LinkFactory.createToolLink("print" + CodeHelper.getRAMUniqueID(), translate("print"), this);
			printLink.setIconLeftCSS("o_icon o_icon_print o_icon-lg");
			printLink.setPopup(new LinkPopupSettings(680, 500, "qti-stats"));

			downloadRawLink = LinkFactory.createToolLink("download" + CodeHelper.getRAMUniqueID(), translate("download.raw.data"), this);
		} else {
			printLink = null;
			downloadRawLink = LinkFactory.createLink("download.raw.data", mainVC, this);
			downloadRawLink.setCustomEnabledLinkCSS("o_content_download");
			mainVC.put("download", downloadRawLink);
		}
		downloadRawLink.setIconLeftCSS("o_icon o_icon_download o_icon-lg");
		
		if(withFilter && (resourceResult.canViewAnonymousUsers() || resourceResult.canViewNonParticipantUsers())) {
			filterCtrl = new UserFilterController(ureq, getWindowControl(), 
					true, resourceResult.canViewNonParticipantUsers(), false, resourceResult.canViewAnonymousUsers(),
					true, resourceResult.isViewNonParticipantUsers(), false, resourceResult.isViewAnonymousUsers());
			listenTo(filterCtrl);
			mainVC.put("filter", filterCtrl.getInitialComponent());
		}
		
		putInitialPanel(mainVC);
		updateData();
	}
	
	@Override
	protected void doDispose() {
		if(stackPanel != null) {
			stackPanel.removeTool(downloadRawLink);
			stackPanel.removeTool(printLink);
		}
        super.doDispose();
	}
	
	@Override
	public void initTools() {
		if(stackPanel != null) {
			stackPanel.addTool(printLink, Align.right);
			stackPanel.addTool(downloadRawLink, Align.right);
		}
	}
	
	public void updateData(boolean participants, boolean nonParticipans, boolean anonymous) {
		resourceResult.setViewPaticipantUsers(participants);
		if (resourceResult.canViewNonParticipantUsers()) {
			resourceResult.setViewNonPaticipantUsers(nonParticipans);
		}
		if (resourceResult.canViewAnonymousUsers()) {
			resourceResult.setViewAnonymousUsers(anonymous);
		}
		updateData();
	}
	
	public void updateData(List<Identity> limitToIdentities) {
		resourceResult.setLimitToIdentities(limitToIdentities);
		updateData();
	}

	private void updateData() {
		StatisticAssessment stats = resourceResult.getQTIStatisticAssessment();
		if (withDiagramm) {
			initScoreHistogram(stats);
			initScoreStatisticPerItem(stats.getNumOfParticipants());
			initDurationHistogram(stats);
		}
		initCourseNodeInformation(stats);
	}
	
	private Float getMaxScoreSetting(QTICourseNode testNode) {
		return testNode instanceof IQTESTCourseNode
					? courseAssessmentService.getAssessmentConfig(resourceResult.getCourseEntry(), courseNode).getMaxScore()
					: null;
	}
	
	private Float getCutValueSetting(QTICourseNode testNode) {
		Float cutValue = null;
		if (testNode instanceof IQTESTCourseNode) {
			if (resourceResult.getCourseEntry() != null && courseNode != null 
					&& courseNode.getModuleConfiguration().getBooleanSafe(MSCourseNode.CONFIG_KEY_GRADE_ENABLED)
					&& CoreSpringFactory.getImpl(GradeModule.class).isEnabled()) {
				GradeService gradeService = CoreSpringFactory.getImpl(GradeService.class);
				GradeScale gradeScale = gradeService.getGradeScale(resourceResult.getCourseEntry(), courseNode.getIdent());
				BigDecimal minPassedScore = gradeService.getMinPassedScore(gradeScale);
				if (minPassedScore != null) {
					cutValue = Float.valueOf(minPassedScore.floatValue());
				}
			} else {
				cutValue = courseAssessmentService.getAssessmentConfig(resourceResult.getCourseEntry(), courseNode).getCutValue();
			}
		}
		return cutValue;
	}

	private void initCourseNodeInformation(StatisticAssessment stats) {
		mainVC.contextPut("numOfParticipants", stats.getNumOfParticipants());
	
		mainVC.contextPut("type", "qtiworks");
		mainVC.contextPut("numOfPassed", stats.getNumOfPassed());
		mainVC.contextPut("numOfFailed", stats.getNumOfFailed());

		mainVC.contextPut("average", format(stats.getAverage()));
		mainVC.contextPut("range", format(stats.getRange()));
		mainVC.contextPut("standardDeviation", format(stats.getStandardDeviation()));
		mainVC.contextPut("mode", getModeString(stats.getMode()));
		mainVC.contextPut("median", format(stats.getMedian()));
		
		String duration = duration(stats.getAverageDuration());
		mainVC.contextPut("averageDuration", duration);
		
		Float maxScore = getMaxScoreSetting(courseNode);
		mainVC.contextPut("maxScore", maxScore == null ? "-" : format(maxScore));
		Float cutValue = getCutValueSetting(courseNode);
		mainVC.contextPut("cutScore", cutValue == null ? "-" : format(cutValue));
	}

	private void initScoreHistogram(StatisticAssessment stats) {
		int numOfParticipants = stats.getNumOfParticipants();
		VelocityContainer scoreHistogramVC = createVelocityContainer("histogram_score");
		scoreHistogramVC.setVisible(numOfParticipants > 0);
		scoreHistogramVC.contextPut("datas", BarSeries.datasToString(stats.getScores()));
		mainVC.put("scoreHistogram", scoreHistogramVC);
	}
	
	private void initDurationHistogram(StatisticAssessment stats) {
		boolean visible = BarSeries.hasNotNullDatas(stats.getDurations()) && stats.getNumOfParticipants() > 0;
		VelocityContainer durationHistogramVC = createVelocityContainer("histogram_duration");
		durationHistogramVC.setVisible(visible);
		if(visible) {
			durationHistogramVC.contextPut("datas", BarSeries.datasToString(stats.getDurations()));
		}
		mainVC.put("durationHistogram", durationHistogramVC);
	}
	
	/**
	 * The 2 graphs with the score per questions and right answers per questions.
	 * 
	 * @param numOfParticipants The number of participants
	 */
	private void initScoreStatisticPerItem(double numOfParticipants) {
		BarSeries d1 = new BarSeries();
		BarSeries d2 = new BarSeries();
		
		List<AssessmentItemStatistic> statisticItems = qtiStatisticsManager
				.getStatisticPerItem(resourceResult.getResolvedAssessmentTest(), resourceResult.getSearchParams(),
				null, null, numOfParticipants);
		
		int i = 0;
		List<ItemInfos> itemInfos = new ArrayList<>(statisticItems.size());
		for (AssessmentItemStatistic statisticItem: statisticItems) {
			AssessmentItem item = statisticItem.getAssessmentItem();
			if(item != null) {
				String label = Integer.toString(++i);
				String text = item.getTitle(); 
				d1.add(statisticItem.getAverageScore(), label);
				d2.add(statisticItem.getNumOfCorrectAnswers(), label);
				itemInfos.add(new ItemInfos(label, text));
			}
		}
		
		mainVC.contextPut("itemInfoList", itemInfos);

		VelocityContainer averageScorePeritemVC = createVelocityContainer("hbar_average_score_per_item");
		Stringuified data1 = BarSeries.getDatasAndColors(Collections.singletonList(d1), "bar_default");
		averageScorePeritemVC.contextPut("datas", data1);
		mainVC.put("averageScorePerItemChart", averageScorePeritemVC);
		
		VelocityContainer percentRightAnswersPerItemVC = createVelocityContainer("hbar_right_answer_per_item");
		Stringuified data2 = BarSeries.getDatasAndColors(Collections.singletonList(d2), "bar_green");
		percentRightAnswersPerItemVC.contextPut("datas", data2);
		percentRightAnswersPerItemVC.contextPut("numOfParticipants", Long.toString(Math.round(numOfParticipants)));
		mainVC.put("percentRightAnswersPerItemChart", percentRightAnswersPerItemVC);
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(filterCtrl == source) {
			if(event instanceof UserFilterEvent) {
				UserFilterEvent ufe = (UserFilterEvent)event;
				updateData(ufe.isWithMembers(), ufe.isWithNonParticipantUsers(), ufe.isWithAnonymousUser());
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(printLink == source) {
			printPages(ureq);
		} else if(downloadRawLink == source) {
			doDownloadRawData(ureq);
		}
	}
	
	private void printPages(UserRequest ureq) {
		ControllerCreator printControllerCreator = (lureq, lwControl) -> new QTI21PrintController(lureq, lwControl, resourceResult);
		ControllerCreator layoutCtrlr = BaseFullWebappPopupLayoutFactory.createPrintPopupLayout(printControllerCreator);
		openInNewBrowserWindow(ureq, layoutCtrlr, true);
	}
	
	private void doDownloadRawData(UserRequest ureq) {
		String label;
		if(courseNode == null) {
			label = StringHelper.transformDisplayNameToFileSystemName(resourceResult.getTestEntry().getDisplayname());
		} else {
			label = courseNode.getType() + "_"
					+ StringHelper.transformDisplayNameToFileSystemName(courseNode.getShortName());
		}
		label += "_" + Formatter.formatDatetimeFilesystemSave(new Date()) + ".xlsx";
		MediaResource resource = new QTI21StatisticsResource(resourceResult, label, getLocale());
		ureq.getDispatchResult().setResultingMediaResource(resource);
	}
}
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.chart.BarSeries;
import org.olat.core.gui.components.chart.BarSeries.Stringuified;
import org.olat.core.gui.components.chart.StatisticsComponent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.ims.qti21.QTI21StatisticsManager;
import org.olat.ims.qti21.model.statistics.AssessmentItemStatistic;
import org.olat.ims.qti21.model.statistics.StatisticsPart;
import org.olat.ims.qti21.model.xml.QtiMaxScoreEstimator;
import org.olat.modules.assessment.ui.UserFilterController;
import org.olat.modules.assessment.ui.event.UserFilterEvent;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.test.AbstractPart;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentSection;
import uk.ac.ed.ph.jqtiplus.node.test.SectionPart;
import uk.ac.ed.ph.jqtiplus.node.test.TestPart;

/**
 * 
 * Initial date: 8 févr. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21AbstractPartStatisticsController extends BasicController implements Activateable2 {

	private final VelocityContainer mainVC;
	
	private UserFilterController filterCtrl;
	
	private final QTI21StatisticResourceResult resourceResult;
	private final boolean withDiagramm;
	
	private TestPart testPart;
	private AssessmentSection section;
	private List<AssessmentSection> sectionsHierarchy;
	
	@Autowired
	private QTI21StatisticsManager qtiStatisticsManager;

	public QTI21AbstractPartStatisticsController(UserRequest ureq, WindowControl wControl,
			AbstractPart abstractPart, QTI21StatisticResourceResult resourceResult,
			boolean withFilter, boolean printMode, boolean withDiagramm) {
		super(ureq, wControl);
		this.resourceResult = resourceResult;
		this.withDiagramm = withDiagramm;

		mainVC = createVelocityContainer("statistics_assessment_part");
		mainVC.put("loadd3js", new StatisticsComponent("d3loader"));
		mainVC.contextPut("printMode", Boolean.valueOf(printMode));
		if(abstractPart instanceof AssessmentSection) {
			section = (AssessmentSection)abstractPart;
			sectionsHierarchy = new ArrayList<>();
			subSections(section, sectionsHierarchy);
			mainVC.contextPut("partTitle", section.getTitle());
			mainVC.contextPut("itemCss", "o_mi_qtisection");
		} else if(abstractPart instanceof TestPart) {
			testPart = (TestPart)abstractPart;
			mainVC.contextPut("partTitle", translate("test.part"));
			mainVC.contextPut("itemCss", "o_qtiassessment_icon");
		}
		
		if(withFilter && (resourceResult.canViewAnonymousUsers() || resourceResult.canViewNonParticipantUsers())) {
			filterCtrl = new UserFilterController(ureq, getWindowControl(),
					false, resourceResult.canViewNonParticipantUsers(), false, resourceResult.canViewAnonymousUsers(),
					true, resourceResult.isViewNonParticipantUsers(), false, resourceResult.isViewAnonymousUsers());
			listenTo(filterCtrl);
			mainVC.put("filter", filterCtrl.getInitialComponent());
		}
		
		putInitialPanel(mainVC);
		updateData();
	}
	
	private void subSections(AssessmentSection assessmentSection, List<AssessmentSection> subSections) {
		subSections.add(assessmentSection);
		
		List<SectionPart> sectionParts = assessmentSection.getSectionParts();
		for(SectionPart sectionPart:sectionParts) {
			if(sectionPart instanceof AssessmentSection) {
				subSections((AssessmentSection)sectionPart, subSections);
			}
		}
	}

	private void updateData() {
		StatisticsPart stats = qtiStatisticsManager.getAssessmentPartStatistics(getControllerCount(), resourceResult.getSearchParams(), testPart, sectionsHierarchy);
		if (withDiagramm) {
			initScoreHistogram(stats);
			initScoreStatisticPerItem(stats.getNumOfParticipants());
			initDurationHistogram(stats);
		}
		initAbstractPartInformation(stats);
	}
	
	private Double getMaxScoreSetting() {
		if(testPart != null) {
			return QtiMaxScoreEstimator.estimateMaxScore(testPart, resourceResult.getResolvedAssessmentTest());
		} else if(section != null) {
			return QtiMaxScoreEstimator.estimateMaxScore(section, resourceResult.getResolvedAssessmentTest());
		}
		return null;
	}

	private void initAbstractPartInformation(StatisticsPart stats) {
		mainVC.contextPut("numOfParticipants", stats.getNumOfParticipants());

		mainVC.contextPut("average", format(stats.getAverage()));
		mainVC.contextPut("range", format(stats.getRange()));
		mainVC.contextPut("standardDeviation", format(stats.getStandardDeviation()));
		mainVC.contextPut("mode", getModeString(stats.getMode()));
		mainVC.contextPut("median", format(stats.getMedian()));
		
		String duration = duration(stats.getAverageDuration());
		mainVC.contextPut("averageDuration", duration);
		
		Double maxScore = getMaxScoreSetting();
		mainVC.contextPut("maxScore", maxScore == null ? "-" : format(maxScore));
	}

	private void initScoreHistogram(StatisticsPart stats) {
		int numOfParticipants = stats.getNumOfParticipants();
		VelocityContainer scoreHistogramVC = createVelocityContainer("histogram_score");
		scoreHistogramVC.setVisible(numOfParticipants > 0);
		scoreHistogramVC.contextPut("datas", BarSeries.datasToString(stats.getScores()));
		mainVC.put("scoreHistogram", scoreHistogramVC);
	}
	
	private void initDurationHistogram(StatisticsPart stats) {
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
						testPart, sectionsHierarchy, numOfParticipants);
		
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
				resourceResult.setViewAnonymousUsers(ufe.isWithAnonymousUser());
				resourceResult.setViewPaticipantUsers(ufe.isWithMembers());
				resourceResult.setViewNonPaticipantUsers(ufe.isWithNonParticipantUsers());
				updateData();
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		///
	}
}
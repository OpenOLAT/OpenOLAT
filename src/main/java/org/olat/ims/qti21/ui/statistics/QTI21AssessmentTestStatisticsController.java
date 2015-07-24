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

import static org.olat.ims.qti.statistics.ui.StatisticFormatter.duration;
import static org.olat.ims.qti.statistics.ui.StatisticFormatter.format;
import static org.olat.ims.qti.statistics.ui.StatisticFormatter.getModeString;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.chart.BarSeries;
import org.olat.core.gui.components.chart.StatisticsComponent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.ims.qti.statistics.QTIStatisticResourceResult;
import org.olat.ims.qti.statistics.QTIType;
import org.olat.ims.qti.statistics.model.StatisticAssessment;
import org.olat.ims.qti21.QTI21StatisticsManager;
import org.olat.ims.qti21.model.QTI21StatisticSearchParams;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21AssessmentTestStatisticsController extends BasicController implements Activateable2 {
	
	private final VelocityContainer mainVC;
	
	@Autowired
	private QTI21StatisticsManager statisticsManager;

	public QTI21AssessmentTestStatisticsController(UserRequest ureq, WindowControl wControl,
			QTIStatisticResourceResult resourceResult, boolean printMode) {
		super(ureq, wControl);

		mainVC = createVelocityContainer("statistics_assessment_test");
		mainVC.put("loadd3js", new StatisticsComponent("d3loader"));
		mainVC.contextPut("printMode", new Boolean(printMode));
		putInitialPanel(mainVC);

		StatisticAssessment stats = resourceResult.getQTIStatisticAssessment();
		initScoreHistogram(stats);
		initDurationHistogram(stats);
		initCourseNodeInformation(stats);
	}
	
	public QTI21AssessmentTestStatisticsController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry entry, boolean printMode) {
		super(ureq, wControl);

		mainVC = createVelocityContainer("statistics_assessment_test");
		mainVC.put("loadd3js", new StatisticsComponent("d3loader"));
		mainVC.contextPut("printMode", new Boolean(printMode));
		putInitialPanel(mainVC);
		
		QTI21StatisticSearchParams searchParams = new QTI21StatisticSearchParams(entry);
		StatisticAssessment stats = statisticsManager.getAssessmentStatistics(searchParams);
		initScoreHistogram(stats);
		//initDurationHistogram(stats);
		initCourseNodeInformation(stats);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	private void initCourseNodeInformation(StatisticAssessment stats) {
		mainVC.contextPut("numOfParticipants", stats.getNumOfParticipants());
	
		mainVC.contextPut("type", QTIType.qtiworks);
		mainVC.contextPut("numOfPassed", stats.getNumOfPassed());
		mainVC.contextPut("numOfFailed", stats.getNumOfFailed());

		mainVC.contextPut("average", format(stats.getAverage()));
		mainVC.contextPut("range", format(stats.getRange()));
		mainVC.contextPut("standardDeviation", format(stats.getStandardDeviation()));
		mainVC.contextPut("mode", getModeString(stats.getMode()));
		mainVC.contextPut("median", format(stats.getMedian()));
		
		String duration = duration(stats.getAverageDuration());
		mainVC.contextPut("averageDuration", duration);
	}

	private void initScoreHistogram(StatisticAssessment stats) {
		VelocityContainer scoreHistogramVC = createVelocityContainer("histogram_score");
		scoreHistogramVC.contextPut("datas", BarSeries.datasToString(stats.getScores()));
		mainVC.put("scoreHistogram", scoreHistogramVC);
	}
	
	private void initDurationHistogram(StatisticAssessment stats) {
		if(!BarSeries.hasNotNullDatas(stats.getDurations())) return;
		
		VelocityContainer durationHistogramVC = createVelocityContainer("histogram_duration");
		durationHistogramVC.contextPut("datas", BarSeries.datasToString(stats.getDurations()));
		mainVC.put("durationHistogram", durationHistogramVC);
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
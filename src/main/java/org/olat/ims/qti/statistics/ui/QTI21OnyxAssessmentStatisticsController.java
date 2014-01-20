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
package org.olat.ims.qti.statistics.ui;

import static org.olat.ims.qti.statistics.ui.StatisticFormatter.duration;
import static org.olat.ims.qti.statistics.ui.StatisticFormatter.format;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.chart.HistogramComponent;
import org.olat.core.gui.control.WindowControl;
import org.olat.ims.qti.statistics.QTIStatisticResourceResult;
import org.olat.ims.qti.statistics.model.StatisticAssessment;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21OnyxAssessmentStatisticsController extends AbstractAssessmentStatisticsController {
	


	public QTI21OnyxAssessmentStatisticsController(UserRequest ureq, WindowControl wControl,
			QTIStatisticResourceResult resourceResult, boolean printMode) {
		super(ureq, wControl, resourceResult, printMode, "statistics_onyx");

		StatisticAssessment stats = resourceResult.getQTIStatisticAssessment();
		initScoreHistogram(stats);
		initCourseNodeInformation(stats);
	}
	
	private void initCourseNodeInformation(StatisticAssessment stats) {
		mainVC.contextPut("numOfParticipants", stats.getNumOfParticipants());
	
		mainVC.contextPut("type", resourceResult.getType());
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
	
	private String getModeString(List<Double> modes) {
		StringBuilder sb = new StringBuilder();
		for(Double mode:modes) {
			if(sb.length() > 0) sb.append(" ,");
			sb.append(format(mode));
		}
		return sb.toString();
	}

	private void initScoreHistogram(StatisticAssessment stats) {
		HistogramComponent scoreHistogram = new HistogramComponent("scoreHistogram");
		scoreHistogram.setDoubleValues(stats.getScores());
		scoreHistogram.setYLegend(translate("chart.percent.participants"));
		mainVC.put("scoreHistogram", scoreHistogram);
	}
}
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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.chart.BarChartComponent;
import org.olat.core.gui.components.chart.BarSeries;
import org.olat.core.gui.components.chart.HistogramComponent;
import org.olat.core.gui.components.chart.HorizontalBarChartComponent;
import org.olat.core.gui.components.chart.Scale;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.nodes.QTICourseNode;
import org.olat.course.nodes.iq.IQEditController;
import org.olat.ims.qti.editor.beecom.objects.Item;
import org.olat.ims.qti.editor.beecom.objects.QTIDocument;
import org.olat.ims.qti.editor.beecom.objects.Section;
import org.olat.ims.qti.statistics.QTIStatisticResourceResult;
import org.olat.ims.qti.statistics.model.StatisticAssessment;
import org.olat.ims.qti.statistics.model.StatisticItem;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI12AssessmentStatisticsController extends AbstractAssessmentStatisticsController {
	
	private final Float maxScore;
	private final Float cutValue;
	private final String mediaBaseURL;

	public QTI12AssessmentStatisticsController(UserRequest ureq, WindowControl wControl,
			QTIStatisticResourceResult resourceResult, boolean printMode) {
		super(ureq, wControl, resourceResult, printMode, "statistics_assessment");
		
		mediaBaseURL = resourceResult.getResolver().getStaticsBaseURI() + "/";

		//cut value
		QTICourseNode testNode = resourceResult.getTestCourseNode();
		Object cutScoreObj = testNode.getModuleConfiguration().get(IQEditController.CONFIG_KEY_CUTVALUE);
		if (cutScoreObj instanceof Float) {
			cutValue = (Float)cutScoreObj;
		} else {
			cutValue = null;
		}

		StatisticAssessment stats = resourceResult.getQTIStatisticAssessment();

		List<Item> items = new ArrayList<>();
		QTIDocument qtiDocument = resourceResult.getQTIDocument();
		for(Section section:qtiDocument.getAssessment().getSections()) {
			for(Item item:section.getItems()) {
				items.add(item);
			}
		}

		Object maxScoreObj = testNode.getModuleConfiguration().get(IQEditController.CONFIG_KEY_MAXSCORE);
		if (maxScoreObj instanceof Float) {
			maxScore = (Float)maxScoreObj;
		} else {
			// try to calculate max
			float max = 0;
			for (Item item: items) {
				if(item.getQuestion() != null) {
					max += item.getQuestion().getMaxValue();
				}
			}
			maxScore = max > 0 ? max : null;
		}

		initCourseNodeInformation(stats);
		initScoreHistogram(stats);
		initScoreStatisticPerItem(items, stats.getNumOfParticipants());
	}
	
	private void initCourseNodeInformation(StatisticAssessment stats) {
		mainVC.contextPut("numOfParticipants", stats.getNumOfParticipants());
	
		mainVC.contextPut("type", resourceResult.getType());
		mainVC.contextPut("numOfPassed", stats.getNumOfPassed());
		mainVC.contextPut("numOfFailed", stats.getNumOfFailed());

		if (cutValue != null) {
			mainVC.contextPut("cutScore", format(cutValue));
		} else {
			mainVC.contextPut("cutScore", "-");
		}

		mainVC.contextPut("maxScore", format(maxScore));
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
	
	private void initScoreStatisticPerItem(List<Item> items, double numOfParticipants) {
		BarSeries d1 = new BarSeries();
		BarSeries d2 = new BarSeries();
		List<StatisticItem> statisticItems = qtiStatisticsManager
				.getStatisticPerItem(items, resourceResult.getSearchParams(), numOfParticipants);
		
		int i = 0;
		List<ItemInfos> itemInfos = new ArrayList<>(items.size());
		for (StatisticItem statisticItem: statisticItems) {
			Item item = statisticItem.getItem();
			
			String label = StatisticFormatter.getLabel(i++);
			String text = item.getQuestion().getQuestion().renderAsHtml(mediaBaseURL);

			d1.add(statisticItem.getAverageScore(), label);
			double numOfRightAnswers = statisticItem.getNumOfCorrectAnswers();
			double res = numOfRightAnswers / numOfParticipants;
			d2.add(res, label);
			
			itemInfos.add(new ItemInfos(label, text));
		}
		
		mainVC.contextPut("itemInfoList", itemInfos);

		HorizontalBarChartComponent averageScorePerItemChart = new HorizontalBarChartComponent("questionPoint");
		averageScorePerItemChart.addSeries(d1);
		averageScorePerItemChart.setXLegend(translate("chart.answer.averageScoreQuestions.y"));
		mainVC.put("averageScorePerItemChart", averageScorePerItemChart);
		
		BarChartComponent percentRightAnswersPerItemChart = new BarChartComponent("correctQuestion");
		percentRightAnswersPerItemChart.addSeries(d2);
		percentRightAnswersPerItemChart.setDefaultBarClass("bar_green");
		percentRightAnswersPerItemChart.setYScale(Scale.percent);
		percentRightAnswersPerItemChart.setYLegend(translate("chart.percent.participants"));
		mainVC.put("percentRightAnswersPerItemChart", percentRightAnswersPerItemChart);
	}

	private void initScoreHistogram(StatisticAssessment stats) {
		HistogramComponent scoreHistogram = new HistogramComponent("scoreHistogram");
		scoreHistogram.setDoubleValues(stats.getScores());
		scoreHistogram.setYLegend(translate("chart.percent.participants"));
		if(maxScore != null) {
			scoreHistogram.setMaxValue(maxScore.doubleValue());
		}
		if(cutValue != null) {
			scoreHistogram.setCutValue("bar_red", cutValue.doubleValue(), "bar_green");
		}
		mainVC.put("scoreHistogram", scoreHistogram);
	}
}
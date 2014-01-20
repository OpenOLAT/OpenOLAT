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

import static org.olat.ims.qti.statistics.ui.StatisticFormatter.formatTwo;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.chart.BarChartComponent;
import org.olat.core.gui.components.chart.BarSeries;
import org.olat.core.gui.components.chart.Scale;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.ims.qti.editor.beecom.objects.Item;
import org.olat.ims.qti.editor.beecom.objects.Response;
import org.olat.ims.qti.statistics.QTIStatisticResourceResult;
import org.olat.ims.qti.statistics.QTIType;
import org.olat.ims.qti.statistics.model.StatisticsItem;
import org.olat.ims.qti.statistics.model.StatisticChoiceOption;
import org.olat.ims.qti.statistics.model.StatisticKPrimOption;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI12SurveyItemStatisticsController extends AbstractItemStatisticsController {
	
	
	
	public QTI12SurveyItemStatisticsController(UserRequest ureq, WindowControl wControl,
			Item item, QTIStatisticResourceResult resourceResult, boolean printMode) {
		super(ureq, wControl, item, resourceResult, printMode);
	}
	
	@Override
	protected void initSingleChoice() {
		List<StatisticChoiceOption> statisticResponses = qtiStatisticsManager
				.getNumOfAnswersPerSingleChoiceAnswerOption(item, searchParams);

		int i = 0;
		BarSeries series = new BarSeries();
		List<ResponseInfos> responseInfos = new ArrayList<>();
		for (StatisticChoiceOption statisticResponse:statisticResponses) {
			Response response = statisticResponse.getResponse();
			double ans_count = statisticResponse.getCount();
			double ans_count_percent = ans_count / numOfParticipants;

			String label = StatisticFormatter.getLabel(i++);
			series.add(ans_count_percent, label);

			String text = response.getContent().renderAsHtml(mediaBaseURL);
			responseInfos.add(new ResponseInfos(label, text, 0.0f, true, QTIType.survey.equals(type)));
		}

		BarChartComponent chart = new BarChartComponent("questionChart");
		chart.setYScale(Scale.percent);
		chart.setYLegend(translate("chart.percent.participants"));
		chart.addSeries(series);
		mainVC.put("questionChart", chart);
		mainVC.contextPut("responseInfos", responseInfos);
	}
	
	@Override
	protected void initMultipleChoice(StatisticsItem itemStats) {
		List<StatisticChoiceOption> statisticResponses = qtiStatisticsManager
				.getNumOfRightAnsweredMultipleChoice(item, searchParams);

		BarSeries series = new BarSeries();

		int i = 0;
		List<ResponseInfos> responseInfos = new ArrayList<>();
		for(StatisticChoiceOption statisticResponse:statisticResponses) {
			Response response = statisticResponse.getResponse();

			float points = response.getPoints();
			double answersPerAnswerOption = statisticResponse.getCount();
			double percentage = answersPerAnswerOption / numOfParticipants;
			
			String label = StatisticFormatter.getLabel(i++);
			series.add(percentage, label);

			String text = response.getContent().renderAsHtml(mediaBaseURL);
			responseInfos.add(new ResponseInfos(label, text, points, true, QTIType.survey.equals(type)));
		}

		BarChartComponent chart = new BarChartComponent("questionChart");
		chart.setYScale(Scale.percent);
		chart.setYLegend(translate("chart.percent.participants"));
		chart.addSeries(series);
		mainVC.put("questionChart", chart);
		mainVC.contextPut("responseInfos", responseInfos);
	}
	
	@Override
	protected void initKPrim() {
		List<StatisticKPrimOption> statisticResponses = qtiStatisticsManager
				.getNumbersInKPrim(item, searchParams);

		int i = 0;
		BarSeries d1 = new BarSeries("bar_default");
		BarSeries d2 = new BarSeries("bar_default_darker");
		
		List<ResponseInfos> responseInfos = new ArrayList<>();
		for (StatisticKPrimOption statisticResponse:statisticResponses) {
			Response response = statisticResponse.getResponse();
			double left = ((double)statisticResponse.getNumOfCorrect() / numOfParticipants);
			double wrong = ((double)statisticResponse.getNumOfIncorrect() / numOfParticipants);
	
			String label = StatisticFormatter.getLabel(i++);
			d1.add(left, label);
			d2.add(wrong, label);

			String text = response.getContent().renderAsHtml(mediaBaseURL);
			responseInfos.add(new ResponseInfos(label, text, 0.0f, true, QTIType.survey.equals(type)));
		}
		
		BarChartComponent chart = new BarChartComponent("questionChart");
		chart.setYScale(Scale.percent);
		chart.setYLegend(translate("chart.percent.participants"));
		chart.addSeries(d1, d2);
		mainVC.put("questionChart", chart);
		mainVC.contextPut("responseInfos", responseInfos);
	}

	@Override
	protected StatisticsItem initChoice() {
		StatisticsItem itemStats = qtiStatisticsManager
				.getItemStatistics(item.getIdent(), 1.0, searchParams);

		mainVC.contextPut("averageDuration", formatTwo(itemStats.getAverageDuration()));
		return itemStats;
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
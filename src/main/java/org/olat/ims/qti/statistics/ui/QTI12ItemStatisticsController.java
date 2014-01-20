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
import org.olat.ims.qti.statistics.model.StatisticAnswerOption;
import org.olat.ims.qti.statistics.model.StatisticChoiceOption;
import org.olat.ims.qti.statistics.model.StatisticKPrimOption;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI12ItemStatisticsController extends AbstractItemStatisticsController {

	public QTI12ItemStatisticsController(UserRequest ureq, WindowControl wControl,
			Item item, QTIStatisticResourceResult resourceResult, boolean printMode) {
		super(ureq, wControl, item, resourceResult, printMode);
	}
	
	@Override
	protected StatisticsItem initChoice() {
		double maxScore = item.getQuestion().getMaxValue();
		StatisticsItem itemStats = qtiStatisticsManager
				.getItemStatistics(item.getIdent(), maxScore, searchParams);

		long rightAnswers = itemStats.getNumOfCorrectAnswers();
		long wrongAnswers = itemStats.getNumOfIncorrectAnswers();
		long notAnswered = numOfParticipants - rightAnswers - wrongAnswers;
	
		mainVC.contextPut("question", item.getQuestion().getQuestion().renderAsHtml(mediaBaseURL));
		mainVC.contextPut("questionType", item.getQuestion().getType());
		mainVC.contextPut("title", item.getTitle());
		mainVC.contextPut("maxScore", maxScore);
		mainVC.contextPut("rightAnswers", rightAnswers);
		mainVC.contextPut("wrongAnswers", wrongAnswers);
		mainVC.contextPut("notAnswered", notAnswered);
		mainVC.contextPut("itemDifficulty", formatTwo(itemStats.getDifficulty()));
		mainVC.contextPut("averageScore", formatTwo(itemStats.getAverageScore()));
		mainVC.contextPut("averageDuration", formatTwo(itemStats.getAverageDuration()));
		return itemStats;
	}
	
	@Override
	protected void initSingleChoice() {
		List<StatisticChoiceOption> statisticResponses = qtiStatisticsManager
				.getNumOfAnswersPerSingleChoiceAnswerOption(item, searchParams);

		int i = 0;
		BarSeries d1 = new BarSeries();
		List<ResponseInfos> responseInfos = new ArrayList<>();
		for (StatisticChoiceOption statisticResponse:statisticResponses) {
			Response response = statisticResponse.getResponse();
			double ans_count = statisticResponse.getCount();
			double ans_count_percent = ans_count / numOfParticipants;
			float points = response.getPoints();
			String cssColor = response.isCorrect() ? "bar_green" : "bar_red";

			String label = StatisticFormatter.getLabel(i++);;
			d1.add(ans_count_percent, label, cssColor);

			String text = response.getContent().renderAsHtml(mediaBaseURL);
			responseInfos.add(new ResponseInfos(label, text, points, true, QTIType.survey.equals(type)));
		}

		BarChartComponent chart = new BarChartComponent("questionChart");
		chart.addSeries(d1);
		chart.setYLegend(translate("chart.percent.participants"));
		chart.setYScale(Scale.percent);
		mainVC.put("questionChart", chart);
		mainVC.contextPut("responseInfos", responseInfos);
	}

	@Override
	protected void initMultipleChoice(StatisticsItem itemStats) {
		List<StatisticChoiceOption> statisticResponses = qtiStatisticsManager
				.getNumOfRightAnsweredMultipleChoice(item, searchParams);

		BarSeries d1 = new BarSeries("bar_green");
		BarSeries d2 = new BarSeries("bar_red");
		BarSeries d3 = new BarSeries("bar_grey");
		
		double wrongFactor = itemStats.getNumOfResults() / (double)numOfParticipants;
		
		int i = 0;
		List<ResponseInfos> responseInfos = new ArrayList<>();
		for(StatisticChoiceOption statisticResponse:statisticResponses) {
			Response response = statisticResponse.getResponse();

			float points = response.getPoints();
			double answersPerAnswerOption = statisticResponse.getCount();
			double percentageRight;
			if (points > 0) {
				percentageRight = answersPerAnswerOption / numOfParticipants;
			} else {
				percentageRight = 1.0d - answersPerAnswerOption / numOfParticipants;
			}
			
			String label = StatisticFormatter.getLabel(i++);
			
			double rightA = percentageRight;
			d1.add(rightA, label);
			double wrongA = wrongFactor - percentageRight;
			d2.add(wrongA, label);
			
			d3.add(1.0d - wrongFactor, label);
			
			String text = response.getContent().renderAsHtml(mediaBaseURL);
			responseInfos.add(new ResponseInfos(label, text, points, true, QTIType.survey.equals(type)));
		}

		BarChartComponent chart = new BarChartComponent("questionChart");
		chart.setYScale(Scale.percent);
		chart.setYLegend(translate("chart.percent.participants"));
		chart.addSeries(d1, d2);
		if(wrongFactor < 1.0) {
			chart.addSeries(d3);
		}

		mainVC.put("questionChart", chart);
		mainVC.contextPut("responseInfos", responseInfos);
	}

	@Override
	protected void initKPrim() {
		List<StatisticKPrimOption> statisticResponses = qtiStatisticsManager
				.getNumbersInKPrim(item, searchParams);

		int i = 0;
		BarSeries d1 = new BarSeries("bar_green");
		BarSeries d2 = new BarSeries("bar_red");
		BarSeries d3 = new BarSeries("bar_grey");
		
		List<ResponseInfos> responseInfos = new ArrayList<>();
		for (StatisticKPrimOption statisticResponse:statisticResponses) {
			Response response = statisticResponse.getResponse();
			float points = response.getPoints();
			double right = ((double)statisticResponse.getNumOfCorrect() / numOfParticipants);
			double wrong = ((double)statisticResponse.getNumOfIncorrect() / numOfParticipants);
			double notanswered = 1.0 - right - wrong;

			String label = StatisticFormatter.getLabel(i++);
			d1.add(right, label);
			d2.add(wrong, label);
			d3.add(notanswered, label);

			String text = response.getContent().renderAsHtml(mediaBaseURL);
			responseInfos.add(new ResponseInfos(label, text, points, true, QTIType.survey.equals(type)));
		}
		
		BarChartComponent chart = new BarChartComponent("questionChart");
		chart.setYScale(Scale.percent);
		chart.setYLegend(translate("chart.percent.participants"));
		chart.addSeries(d1, d2, d3);
		mainVC.put("questionChart", chart);
		mainVC.contextPut("responseInfos", responseInfos);
	}
	
	@Override
	protected void initFIB() {
		List<StatisticAnswerOption> processedAnswers = qtiStatisticsManager
				.getStatisticAnswerOptionsOfItem(item.getIdent(), searchParams);

		BarSeries d1 = new BarSeries();
		for (StatisticAnswerOption entry : processedAnswers) {
			String answerString = getAllBlanksFromAnswer(entry.getAnswer());
			d1.add(entry.getCount(), answerString);
		}

		BarChartComponent chart = new BarChartComponent("questionChart");
		chart.addSeries(d1);
		mainVC.put("questionChart", chart);
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
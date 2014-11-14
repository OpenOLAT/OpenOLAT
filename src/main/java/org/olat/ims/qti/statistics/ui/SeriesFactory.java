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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.components.chart.BarSeries;
import org.olat.core.gui.translator.Translator;
import org.olat.ims.qti.editor.beecom.objects.Item;
import org.olat.ims.qti.editor.beecom.objects.Question;
import org.olat.ims.qti.editor.beecom.objects.Response;
import org.olat.ims.qti.statistics.QTIStatisticResourceResult;
import org.olat.ims.qti.statistics.QTIStatisticsManager;
import org.olat.ims.qti.statistics.QTIType;
import org.olat.ims.qti.statistics.model.StatisticChoiceOption;
import org.olat.ims.qti.statistics.model.StatisticFIBOption;
import org.olat.ims.qti.statistics.model.StatisticKPrimOption;
import org.olat.ims.qti.statistics.model.StatisticsItem;

/**
 * 
 * Initial date: 10.03.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SeriesFactory {
	
	private static final String BAR_CORRECT_WRONG_NOT = "horizontalBarMultipleChoice";
	private static final String BAR_ANSWERED = "horizontalBarMultipleChoiceSurvey";
	private static final String BAR_CORRECT = "horizontalBarSingleChoice";
	
	private final Translator translator;
	private final QTIStatisticsManager qtiStatisticsManager;
	private final QTIStatisticResourceResult resourceResult;
	
	public SeriesFactory(QTIStatisticResourceResult resourceResult, Translator translator) {
		this.translator = translator;
		this.resourceResult = resourceResult;
		qtiStatisticsManager = CoreSpringFactory.getImpl(QTIStatisticsManager.class);
	}
	
	private String translate(String key) {
		return translator.translate(key);
	}
	
	public static String getCssClass(Item item) {
		int questionType = item.getQuestion().getType();
		switch (questionType) {
			case Question.TYPE_SC: return "o_mi_qtisc";
			case Question.TYPE_MC: return "o_mi_qtimc";
			case Question.TYPE_KPRIM: return "o_mi_qtikprim";
			case Question.TYPE_FIB: return "o_mi_qtifib";
			case Question.TYPE_ESSAY: return "o_mi_qtiessay";
			default: return null;
		}
	}
	
	public Series getSeries(Item item, StatisticsItem itemStats) {
		int questionType = item.getQuestion().getType();
		switch(questionType) {
			case Question.TYPE_SC: return getSingleChoice(item);
			case Question.TYPE_MC: return getMultipleChoice(item, itemStats);
			case Question.TYPE_KPRIM: return getKPrim(item);
			case Question.TYPE_FIB: return getFIB(item);
			default: return null;
		}
	}

	public Series getSingleChoice(Item item) {
		List<StatisticChoiceOption> statisticResponses = qtiStatisticsManager
				.getNumOfAnswersPerSingleChoiceAnswerOption(item, resourceResult.getSearchParams());
		
		String mediaBaseURL = resourceResult.getMediaBaseURL();
		boolean survey = QTIType.survey.equals(resourceResult.getType());
		int numOfParticipants = resourceResult.getQTIStatisticAssessment().getNumOfParticipants();

		int i = 0;
		long numOfResults = 0;
		BarSeries d1 = new BarSeries();
		List<ResponseInfos> responseInfos = new ArrayList<>();
		for (StatisticChoiceOption statisticResponse:statisticResponses) {
			Response response = statisticResponse.getResponse();
			double ans_count = statisticResponse.getCount();
			numOfResults += statisticResponse.getCount();

			Float points;
			String cssColor;
			if(survey) {
				points = null;
				cssColor = "bar_default";
			} else {
				points = response.getPoints();
				cssColor = response.isCorrect() ? "bar_green" : "bar_red";
			}

			String label = Integer.toString(++i);
			d1.add(ans_count, label, cssColor);

			String text = response.getContent().renderAsHtml(mediaBaseURL);
			responseInfos.add(new ResponseInfos(label, text, points, response.isCorrect(), survey, false));
		}
		
		if(numOfResults != numOfParticipants) {
			long notAnswered = numOfParticipants - numOfResults;
			if(notAnswered > 0) {
				String label = Integer.toString(++i);
				String text = translate("user.not.answer");
				responseInfos.add(new ResponseInfos(label, text, null, false, survey, false));
				d1.add(notAnswered, label, "bar_grey");
			}
		}

		List<BarSeries> serieList = Collections.singletonList(d1);
		Series series = new Series(serieList, responseInfos, numOfParticipants, false);
		series.setChartType(BAR_CORRECT);
		series.setItemCss(getCssClass(item));
		return series;
	}
	
	public Series getMultipleChoice(Item item, StatisticsItem itemStats) {
		List<StatisticChoiceOption> statisticResponses = qtiStatisticsManager
				.getNumOfRightAnsweredMultipleChoice(item, resourceResult.getSearchParams());

		BarSeries d1 = new BarSeries("bar_green", "green", translate("answer.correct"));
		BarSeries d2 = new BarSeries("bar_red", "red", translate("answer.false"));
		BarSeries d3 = new BarSeries("bar_grey", "grey", translate("answer.noanswer"));
		
		String mediaBaseURL = resourceResult.getMediaBaseURL();
		boolean survey = QTIType.survey.equals(resourceResult.getType());
		int numOfParticipants = resourceResult.getQTIStatisticAssessment().getNumOfParticipants();
		int notAnswered = numOfParticipants - (itemStats == null ? 0 : itemStats.getNumOfResults());
		
		int i = 0;
		List<ResponseInfos> responseInfos = new ArrayList<>();
		for(StatisticChoiceOption statisticResponse:statisticResponses) {
			Response response = statisticResponse.getResponse();
			String text = response.getContent().renderAsHtml(mediaBaseURL);

			float points = response.getPoints();
			double answersPerAnswerOption = statisticResponse.getCount();

			double rightA;
			double wrongA;
			
			if (survey) {
				rightA = answersPerAnswerOption;
				wrongA = 0d;
			} else if (points > 0.00001f) {
				rightA = answersPerAnswerOption;
				wrongA = numOfParticipants - notAnswered - answersPerAnswerOption;
			} else {
				//minus negative points are not answered right?
				rightA = numOfParticipants - notAnswered - answersPerAnswerOption ;
				wrongA = answersPerAnswerOption;
			}
			
			String label = Integer.toString(++i);
			d1.add(rightA, label);
			d2.add(wrongA, label);
			d3.add(notAnswered, label);
			
			Float pointsObj = survey ? null : points;
			responseInfos.add(new ResponseInfos(label, text, pointsObj, (points > 0f), survey, false));
		}

		List<BarSeries> serieList = new ArrayList<>(3);
		serieList.add(d1);
		if(!survey) {
			serieList.add(d2);
			serieList.add(d3);
		}
		
		Series series = new Series(serieList, responseInfos, numOfParticipants, !survey);
		series.setChartType(survey ? BAR_ANSWERED : BAR_CORRECT_WRONG_NOT);
		series.setItemCss(getCssClass(item));
		return series;
	}
	
	public Series getKPrim(Item item) {
		List<StatisticKPrimOption> statisticResponses = qtiStatisticsManager
				.getNumbersInKPrim(item, resourceResult.getSearchParams());

		String mediaBaseURL = resourceResult.getMediaBaseURL();
		boolean survey = QTIType.survey.equals(resourceResult.getType());
		int numOfParticipants = resourceResult.getQTIStatisticAssessment().getNumOfParticipants();
		
		int i = 0;
		BarSeries d1 = new BarSeries("bar_green", "green", translate("answer.correct"));
		BarSeries d2 = new BarSeries("bar_red", "red", translate("answer.false"));
		BarSeries d3 = new BarSeries("bar_grey", "grey", translate("answer.noanswer"));

		List<ResponseInfos> responseInfos = new ArrayList<>();
		for (StatisticKPrimOption statisticResponse:statisticResponses) {
			Response response = statisticResponse.getResponse();
			
			boolean correct = response.isCorrect();
			double right = statisticResponse.getNumOfCorrect();
			double wrong = statisticResponse.getNumOfIncorrect();
			double notanswered = numOfParticipants - right - wrong;

			String label = Integer.toString(++i);
			d1.add(right, label);
			d2.add(wrong, label);
			d3.add(notanswered, label);

			String text = response.getContent().renderAsHtml(mediaBaseURL);
			responseInfos.add(new ResponseInfos(label, text, null, correct, survey, true));
		}
		
		List<BarSeries> serieList = new ArrayList<>(3);
		serieList.add(d1);
		serieList.add(d2);
		serieList.add(d3);
		Series series = new Series(serieList, responseInfos, numOfParticipants, !survey);
		series.setChartType(survey ? BAR_ANSWERED : BAR_CORRECT_WRONG_NOT);
		series.setItemCss(getCssClass(item));
		return series;
	}
	
	public Series getFIB(Item item) {
		List<StatisticFIBOption> processedAnswers = qtiStatisticsManager
				.getStatisticAnswerOptionsFIB(item, resourceResult.getSearchParams());

		boolean survey = QTIType.survey.equals(resourceResult.getType());
		boolean singleCorrectScore = item.getQuestion().getSingleCorrectScore() > 0.0f;
		int numOfParticipants = resourceResult.getQTIStatisticAssessment().getNumOfParticipants();

		int i = 0;
		String cssColor = survey ? "bar_default" : "bar_green";
		String color = survey ? null : "green";
		BarSeries d1 = new BarSeries(cssColor, color, null);
		List<ResponseInfos> responseInfos = new ArrayList<>();
		for (StatisticFIBOption entry : processedAnswers) {

			String label = Integer.toString(++i);
			String answerString = entry.getCorrectBlank();
			d1.add(entry.getNumOfCorrect(), label, cssColor);
			
			StringBuilder text = new StringBuilder();
			text.append(answerString);
			if(entry.getAlternatives().size() > 1) {
				text.append(" [");
				for(int j=1; j<entry.getAlternatives().size(); j++) {
					if(j > 1) text.append(", ");
					text.append(entry.getAlternatives().get(j));
				}
				text.append("]");
			}
			
			Float score = singleCorrectScore ? null : entry.getPoints();
			responseInfos.add(new ResponseInfos(label, text.toString(), entry.getWrongAnswers(), score, true, survey, false));
		}
		
		List<BarSeries> serieList = Collections.singletonList(d1);
		Series series = new Series(serieList, responseInfos, numOfParticipants, false);
		series.setChartType(BAR_ANSWERED);
		series.setItemCss(getCssClass(item));
		return series;
	}
}
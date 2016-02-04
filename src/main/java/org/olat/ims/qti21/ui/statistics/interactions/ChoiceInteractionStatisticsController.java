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
package org.olat.ims.qti21.ui.statistics.interactions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.chart.BarSeries;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.filter.FilterFactory;
import org.olat.ims.qti.statistics.QTIType;
import org.olat.ims.qti.statistics.model.StatisticsItem;
import org.olat.ims.qti.statistics.ui.ResponseInfos;
import org.olat.ims.qti.statistics.ui.Series;
import org.olat.ims.qti21.QTI21StatisticsManager;
import org.olat.ims.qti21.model.statistics.SimpleChoiceStatistics;
import org.olat.ims.qti21.model.xml.AssessmentHtmlBuilder;
import org.olat.ims.qti21.ui.statistics.QTI21AssessmentItemStatisticsController;
import org.olat.ims.qti21.ui.statistics.QTI21StatisticResourceResult;
import org.olat.ims.qti21.ui.statistics.SeriesFactory;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.CorrectResponse;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.ChoiceInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleChoice;
import uk.ac.ed.ph.jqtiplus.node.item.response.declaration.ResponseDeclaration;
import uk.ac.ed.ph.jqtiplus.node.shared.FieldValue;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.value.Cardinality;
import uk.ac.ed.ph.jqtiplus.value.IdentifierValue;
import uk.ac.ed.ph.jqtiplus.value.MultipleValue;
import uk.ac.ed.ph.jqtiplus.value.SingleValue;
import uk.ac.ed.ph.jqtiplus.value.Value;

/**
 * 
 * Initial date: 04.02.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ChoiceInteractionStatisticsController extends BasicController {
	
	private final ChoiceInteraction interaction;
	private final AssessmentItemRef itemRef;
	private final AssessmentItem assessmentItem;
	private final QTI21StatisticResourceResult resourceResult;
	
	@Autowired
	private QTI21StatisticsManager qtiStatisticsManager;
	
	public ChoiceInteractionStatisticsController(UserRequest ureq, WindowControl wControl,
			AssessmentItemRef itemRef, AssessmentItem assessmentItem, ChoiceInteraction interaction,
			StatisticsItem itemStats, QTI21StatisticResourceResult resourceResult) {
		super(ureq, wControl, Util.createPackageTranslator(QTI21AssessmentItemStatisticsController.class, ureq.getLocale()));
		this.interaction = interaction;
		this.itemRef = itemRef;
		this.assessmentItem = assessmentItem;
		this.resourceResult = resourceResult;
		
		VelocityContainer mainVC = createVelocityContainer("statistics_interaction");
		Series series;
		if(isMultipleChoice()) {
			series = getMultipleChoice(itemStats);
		} else {
			series = getSingleChoice(itemStats);
		}
		VelocityContainer vc = createVelocityContainer("hbar_item");
		vc.contextPut("series", series);
		mainVC.put("questionChart", vc);
		mainVC.contextPut("series", series);
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		
	}
	
	private boolean isMultipleChoice() {
		ResponseDeclaration responseDeclaration = assessmentItem.getResponseDeclaration(interaction.getResponseIdentifier());
		if(responseDeclaration != null && responseDeclaration.getCorrectResponse() != null) {
			CorrectResponse correctResponse = responseDeclaration.getCorrectResponse();
			if(correctResponse.getCardinality().isOneOf(Cardinality.MULTIPLE)) {
				return true;
			}
		}
		return false;
	}
	
	private List<Identifier> getCorrectResponses() {
		List<Identifier> correctAnswers = new ArrayList<>();
		
		ResponseDeclaration responseDeclaration = assessmentItem.getResponseDeclaration(interaction.getResponseIdentifier());
		if(responseDeclaration != null && responseDeclaration.getCorrectResponse() != null) {
			CorrectResponse correctResponse = responseDeclaration.getCorrectResponse();
			if(correctResponse.getCardinality().isOneOf(Cardinality.SINGLE)) {
				List<FieldValue> values = correctResponse.getFieldValues();
				Value value = FieldValue.computeValue(Cardinality.SINGLE, values);
				if(value instanceof IdentifierValue) {
					IdentifierValue identifierValue = (IdentifierValue)value;
					Identifier correctAnswer = identifierValue.identifierValue();
					correctAnswers.add(correctAnswer);
				}
				
			} else if(correctResponse.getCardinality().isOneOf(Cardinality.MULTIPLE)) {
				Value value = FieldValue.computeValue(Cardinality.MULTIPLE, correctResponse.getFieldValues());
				if(value instanceof MultipleValue) {
					MultipleValue multiValue = (MultipleValue)value;
					for(SingleValue sValue:multiValue.getAll()) {
						if(sValue instanceof IdentifierValue) {
							IdentifierValue identifierValue = (IdentifierValue)sValue;
							Identifier correctAnswer = identifierValue.identifierValue();
							correctAnswers.add(correctAnswer);
						}
					}
				}
			}
		}
		
		return correctAnswers;
	}
	
	private Series getSingleChoice(StatisticsItem itemStats) {
		List<SimpleChoiceStatistics> statisticResponses = qtiStatisticsManager
				.getChoiceInteractionStatistics(itemRef.getIdentifier().toString(), assessmentItem, interaction, resourceResult.getSearchParams());
	
		boolean survey = QTIType.survey.equals(resourceResult.getType());
		int numOfParticipants = resourceResult.getQTIStatisticAssessment().getNumOfParticipants();
		List<Identifier> correctAnswers = getCorrectResponses();
		
		int i = 0;
		long numOfResults = 0;
		BarSeries d1 = new BarSeries();
		List<ResponseInfos> responseInfos = new ArrayList<>();
		for (SimpleChoiceStatistics statisticResponse:statisticResponses) {
			SimpleChoice choice = statisticResponse.getChoice();
			String text = getAnswerText(choice);
			double ans_count = statisticResponse.getCount();
			numOfResults += statisticResponse.getCount();
			boolean correct = correctAnswers.contains(choice.getIdentifier());

			Float points;
			String cssColor;
			if(survey) {
				points = null;
				cssColor = "bar_default";
			} else {
				points = correct ? 1.0f : 0.0f; //response.getPoints();
				cssColor = correct ? "bar_green" : "bar_red";
			}

			String label = Integer.toString(++i);
			d1.add(ans_count, label, cssColor);

			responseInfos.add(new ResponseInfos(label, text, points, correct, survey, false));
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
		series.setChartType(SeriesFactory.BAR_CORRECT);
		series.setItemCss("o_qti_scitem");
		return series;
	}
	
	private Series getMultipleChoice(StatisticsItem itemStats) {
		List<SimpleChoiceStatistics> statisticResponses = qtiStatisticsManager
				.getChoiceInteractionStatistics(itemRef.getIdentifier().toString(), assessmentItem, interaction, resourceResult.getSearchParams());

		BarSeries d1 = new BarSeries("bar_green", "green", translate("answer.correct"));
		BarSeries d2 = new BarSeries("bar_red", "red", translate("answer.false"));
		BarSeries d3 = new BarSeries("bar_grey", "grey", translate("answer.noanswer"));
		
		boolean survey = QTIType.survey.equals(resourceResult.getType());
		int numOfParticipants = resourceResult.getQTIStatisticAssessment().getNumOfParticipants();
		int notAnswered = numOfParticipants - (itemStats == null ? 0 : itemStats.getNumOfResults());
		List<Identifier> correctAnswers = getCorrectResponses();
		
		int i = 0;
		List<ResponseInfos> responseInfos = new ArrayList<>();
		for(SimpleChoiceStatistics statisticResponse:statisticResponses) {
			SimpleChoice choice = statisticResponse.getChoice();
			String text = getAnswerText(choice);
			boolean correct = correctAnswers.contains(choice.getIdentifier());
			double answersPerAnswerOption = statisticResponse.getCount();

			double rightA;
			double wrongA;
			
			if (survey) {
				rightA = answersPerAnswerOption;
				wrongA = 0d;
			} else if (correct) {
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
			
			Float pointsObj = survey ? null : (correct ? 1.0f : 0.0f);
			responseInfos.add(new ResponseInfos(label, text, pointsObj, correct, survey, false));
		}

		List<BarSeries> serieList = new ArrayList<>(3);
		serieList.add(d1);
		if(!survey) {
			serieList.add(d2);
			serieList.add(d3);
		}
		
		Series series = new Series(serieList, responseInfos, numOfParticipants, !survey);
		series.setChartType(survey ? SeriesFactory.BAR_ANSWERED : SeriesFactory.BAR_CORRECT_WRONG_NOT);
		series.setItemCss("o_qti_scitem");
		return series;
	}
	
	private String getAnswerText(SimpleChoice choice) {
		String text = choice.getLabel();
		if(!StringHelper.containsNonWhitespace(text)) {
			text = new AssessmentHtmlBuilder().flowStaticString(choice.getFlowStatics());
			text = FilterFactory.getHtmlTagsFilter().filter(text);
		}
		return text;
	}


}

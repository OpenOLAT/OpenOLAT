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
import org.olat.core.util.Util;
import org.olat.ims.qti21.QTI21StatisticsManager;
import org.olat.ims.qti21.manager.CorrectResponsesUtil;
import org.olat.ims.qti21.model.statistics.ChoiceStatistics;
import org.olat.ims.qti21.model.statistics.StatisticsItem;
import org.olat.ims.qti21.ui.statistics.QTI21AssessmentItemStatisticsController;
import org.olat.ims.qti21.ui.statistics.QTI21StatisticResourceResult;
import org.olat.ims.qti21.ui.statistics.SeriesFactory;
import org.olat.ims.qti21.ui.statistics.interactions.ResponseInfos.ExplanationType;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.CorrectResponse;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.Choice;
import uk.ac.ed.ph.jqtiplus.node.item.response.declaration.ResponseDeclaration;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.value.Cardinality;

/**
 * 
 * Initial date: 04.02.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class ChoiceInteractionStatisticsController extends BasicController {
	
	protected final VelocityContainer mainVC;
	
	protected int count = 0;
	protected final String mapperUri;
	protected final Interaction interaction;
	protected final AssessmentItemRef itemRef;
	protected final AssessmentItem assessmentItem;
	protected final QTI21StatisticResourceResult resourceResult;
	
	@Autowired
	protected QTI21StatisticsManager qtiStatisticsManager;
	
	public ChoiceInteractionStatisticsController(UserRequest ureq, WindowControl wControl,
			AssessmentItemRef itemRef, AssessmentItem assessmentItem, Interaction interaction,
			StatisticsItem itemStats, QTI21StatisticResourceResult resourceResult, String mapperUri) {
		super(ureq, wControl, Util.createPackageTranslator(QTI21AssessmentItemStatisticsController.class, ureq.getLocale()));
		this.interaction = interaction;
		this.itemRef = itemRef;
		this.assessmentItem = assessmentItem;
		this.resourceResult = resourceResult;
		this.mapperUri = mapperUri;
		
		mainVC = createVelocityContainer("statistics_interaction");
		Series series;
		if(isMultipleChoice()) {
			series = getMultipleChoice(itemStats);
		} else {
			series = getSingleChoice();
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
	
	private Series getSingleChoice() {
		List<ChoiceStatistics> statisticResponses = getChoiceInteractionStatistics();
	
		int numOfParticipants = resourceResult.getQTIStatisticAssessment().getNumOfParticipants();
		List<Identifier> correctAnswers = CorrectResponsesUtil.getCorrectIdentifierResponses(assessmentItem, interaction);
		
		int i = 0;
		long numOfResults = 0;
		BarSeries d1 = new BarSeries();
		List<ResponseInfos> responseInfos = new ArrayList<>();
		for (ChoiceStatistics statisticResponse:statisticResponses) {
			Choice choice = statisticResponse.getChoice();
			Component text = getAnswerText(choice);
			double ansCount = statisticResponse.getCount();
			numOfResults += statisticResponse.getCount();
			boolean correct = correctAnswers.contains(choice.getIdentifier());

			Float points;
			Double mappedValue = CorrectResponsesUtil.getMappedValue(assessmentItem, interaction, choice);
			if(mappedValue != null) {
				points = mappedValue.floatValue();
			} else {
				points = correct ? 1.0f : 0.0f; //response.getPoints();
			}
			String cssColor = correct ? "bar_green" : "bar_red";
		

			String label = Integer.toString(++i);
			d1.add(ansCount, label, cssColor);

			responseInfos.add(new ResponseInfos(label, text, points, correct, false));
		}
		
		if(numOfResults != numOfParticipants) {
			long notAnswered = numOfParticipants - numOfResults;
			if(notAnswered > 0) {
				String label = Integer.toString(++i);
				String text = translate("user.not.answer");
				responseInfos.add(new ResponseInfos(label, text, null, null, null, false, false, ExplanationType.standard));
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
		List<ChoiceStatistics> statisticResponses = getChoiceInteractionStatistics();

		BarSeries d1 = new BarSeries("bar_green", "green", translate("answer.correct"));
		BarSeries d2 = new BarSeries("bar_red", "red", translate("answer.false"));
		BarSeries d3 = new BarSeries("bar_grey", "grey", translate("answer.noanswer"));
		
		int numOfParticipants = resourceResult.getQTIStatisticAssessment().getNumOfParticipants();
		int notAnswered = numOfParticipants - (itemStats == null ? 0 : itemStats.getNumOfResults());
		List<Identifier> correctAnswers = CorrectResponsesUtil.getCorrectIdentifierResponses(assessmentItem, interaction);
		
		int i = 0;
		List<ResponseInfos> responseInfos = new ArrayList<>();
		for(ChoiceStatistics statisticResponse:statisticResponses) {
			Choice choice = statisticResponse.getChoice();
			Component text = getAnswerText(choice);
			boolean correct = correctAnswers.contains(choice.getIdentifier());
			double answersPerAnswerOption = statisticResponse.getCount();

			double rightA;
			double wrongA;
			
			if (correct) {
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
			
			Float pointsObj;
			Double mappedValue = CorrectResponsesUtil.getMappedValue(assessmentItem, interaction, choice);
			if(mappedValue != null) {
				pointsObj = mappedValue.floatValue();
			} else {
				pointsObj = correct ? 1.0f : 0.0f;
			}
			responseInfos.add(new ResponseInfos(label, text, pointsObj, correct, false));
		}

		List<BarSeries> serieList = new ArrayList<>(3);
		serieList.add(d1);
		serieList.add(d2);
		serieList.add(d3);
		
		Series series = new Series(serieList, responseInfos, numOfParticipants, true);
		series.setChartType(SeriesFactory.BAR_CORRECT_WRONG_NOT);
		series.setItemCss("o_qti_scitem");
		return series;
	}
	
	protected abstract List<ChoiceStatistics> getChoiceInteractionStatistics();
	
	protected abstract Component getAnswerText(Choice choice);


}

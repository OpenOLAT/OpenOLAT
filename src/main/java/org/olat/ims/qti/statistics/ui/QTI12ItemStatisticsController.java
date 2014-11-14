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
import static org.olat.ims.qti.statistics.ui.StatisticFormatter.formatTwo;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.chart.StatisticsComponent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.ims.qti.editor.beecom.objects.FIBResponse;
import org.olat.ims.qti.editor.beecom.objects.Item;
import org.olat.ims.qti.editor.beecom.objects.Question;
import org.olat.ims.qti.editor.beecom.objects.Response;
import org.olat.ims.qti.editor.beecom.objects.Section;
import org.olat.ims.qti.statistics.QTIStatisticResourceResult;
import org.olat.ims.qti.statistics.QTIStatisticSearchParams;
import org.olat.ims.qti.statistics.QTIStatisticsManager;
import org.olat.ims.qti.statistics.QTIType;
import org.olat.ims.qti.statistics.model.StatisticsItem;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI12ItemStatisticsController extends BasicController {

	private final QTIStatisticSearchParams searchParams;
	private final QTIStatisticResourceResult resourceResult;
	private final QTIStatisticsManager qtiStatisticsManager;
	private final VelocityContainer mainVC;
	
	private final int numOfParticipants;
	private final String mediaBaseURL;
	
	private final Item item;
	
	private final SeriesFactory seriesfactory;
	
	public QTI12ItemStatisticsController(UserRequest ureq, WindowControl wControl,
			Section section, Item item, QTIStatisticResourceResult resourceResult, boolean printMode) {
		super(ureq, wControl);
		this.item = item;
		seriesfactory = new SeriesFactory(resourceResult, getTranslator());
		qtiStatisticsManager = CoreSpringFactory.getImpl(QTIStatisticsManager.class);
		
		this.resourceResult = resourceResult;
		searchParams = resourceResult.getSearchParams();
		mediaBaseURL = resourceResult.getResolver().getStaticsBaseURI() + "/";
		numOfParticipants = resourceResult.getQTIStatisticAssessment().getNumOfParticipants();

		int questionType = item.getQuestion().getType();
		if(Question.TYPE_ESSAY == questionType) {
			mainVC = createVelocityContainer("statistics_item_essai");
			initEssay();
		} else {
			mainVC = createVelocityContainer("statistics_item");
			StatisticsItem itemStats = initItemStatistics();
			initItem(itemStats);
		}
		mainVC.put("d3loader", new StatisticsComponent("d3loader"));
		mainVC.contextPut("title", item.getTitle());
		if(section != null) {
			mainVC.contextPut("sectionTitle", section.getTitle());
		}
		mainVC.contextPut("questionType", item.getQuestion().getType());
		mainVC.contextPut("question", getQuestion());
		mainVC.contextPut("numOfParticipants", resourceResult.getQTIStatisticAssessment().getNumOfParticipants());
		mainVC.contextPut("printMode", new Boolean(printMode));
		putInitialPanel(mainVC);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	protected StatisticsItem initItemStatistics() {
		boolean survey = QTIType.survey.equals(resourceResult.getType());
		double maxScore;
		if(survey) {
			maxScore = 1d;
		} else if(item.getQuestion().isSingleCorrect()) {
			maxScore = item.getQuestion().getSingleCorrectScore();
		} else {
			maxScore = item.getQuestion().getMaxValue();
		}
		StatisticsItem itemStats = qtiStatisticsManager
				.getItemStatistics(item.getIdent(), maxScore, searchParams);

		if(survey) {
			long notAnswered = numOfParticipants - itemStats.getNumOfResults();
			mainVC.contextPut("notAnswered", notAnswered);
		} else {
			long rightAnswers = itemStats.getNumOfCorrectAnswers();
			long wrongAnswers = itemStats.getNumOfIncorrectAnswers();
			long notAnswered = numOfParticipants - rightAnswers - wrongAnswers;

			mainVC.contextPut("maxScore", maxScore);
			mainVC.contextPut("rightAnswers", rightAnswers);
			mainVC.contextPut("wrongAnswers", wrongAnswers);
			if(item.getQuestion().getType() != Question.TYPE_FIB) {
				mainVC.contextPut("notAnswered", notAnswered);
			}
			mainVC.contextPut("itemDifficulty", formatTwo(itemStats.getDifficulty()));
			mainVC.contextPut("averageScore", formatTwo(itemStats.getAverageScore()));
		}
		mainVC.contextPut("averageDuration", duration(itemStats.getAverageDuration()));
		return itemStats;
	}
	
	private String getQuestion() {
		String question;
		if(item.getQuestion().getType() == Question.TYPE_FIB) {
			StringBuilder questionSb = new StringBuilder();
			List<Response> elements = item.getQuestion().getResponses();
			if(elements != null) {
				for(Response element:elements) {
					if(element instanceof FIBResponse) {
						FIBResponse response = (FIBResponse)element;
						if(FIBResponse.TYPE_CONTENT.equals(response.getType())) {
							String content = response.getContent().renderAsHtml(mediaBaseURL);
							questionSb.append(content);
						} else if(FIBResponse.TYPE_BLANK.equals(response.getType())) {
							questionSb.append(" ___________________ ");
						}
					}
				}
			}
			question = questionSb.toString();
		} else {
			question = item.getQuestion().getQuestion().renderAsHtml(mediaBaseURL);
		}
		return question;
	}
	
	protected void initItem(StatisticsItem itemStats) {
		Series series = seriesfactory.getSeries(item, itemStats);

		VelocityContainer vc = createVelocityContainer("hbar_item");
		vc.contextPut("series", series);
		mainVC.put("questionChart", vc);
		mainVC.contextPut("series", series);
	}
	
	protected void initEssay() {
		boolean survey = QTIType.survey.equals(resourceResult.getType());
		
		double maxScore;
		if(survey) {
			maxScore = 1d;
		} else if(item.getQuestion().isSingleCorrect()) {
			maxScore = item.getQuestion().getSingleCorrectScore();
		} else {
			maxScore = item.getQuestion().getMaxValue();
		}
		StatisticsItem itemStats = qtiStatisticsManager
				.getItemStatistics(item.getIdent(), maxScore, searchParams);

		mainVC.contextPut("solution", item.getQuestion().getSolutionText());
		int numOfResults = itemStats.getNumOfResults();
		if(!survey) {
			mainVC.contextPut("maxScore", maxScore);
		}
		mainVC.contextPut("averageDuration", duration(itemStats.getAverageDuration()));

		List<String> answers = qtiStatisticsManager.getAnswers(item.getIdent(), searchParams);

		List<String> cleanedAnswers = new ArrayList<>();
		for (String string : answers) {
			String strippedAnswer = stripAnswerText(string);
			if(strippedAnswer == null || strippedAnswer.length() == 0) {
				numOfResults--;
			} else {
				cleanedAnswers.add(strippedAnswer);
			}
		}
		
		mainVC.contextPut("numOfResults", Math.max(0, numOfResults));
		mainVC.contextPut("studentAnswers", cleanedAnswers);
	}
	
	private String stripAnswerText(String answerTextFromDB){
		String result ="";
		int start = answerTextFromDB.indexOf("[");
		result = answerTextFromDB.substring(start+2);
		result = result.substring(0, result.length()-2);
		result = result.replaceAll("\\\\r\\\\n", "<br />");
		return result;
	}
}
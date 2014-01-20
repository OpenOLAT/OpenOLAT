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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.chart.BarChartComponent;
import org.olat.core.gui.components.chart.BarSeries;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.ims.qti.editor.beecom.objects.Item;
import org.olat.ims.qti.editor.beecom.objects.Question;
import org.olat.ims.qti.statistics.QTIStatisticResourceResult;
import org.olat.ims.qti.statistics.QTIStatisticSearchParams;
import org.olat.ims.qti.statistics.QTIStatisticsManager;
import org.olat.ims.qti.statistics.QTIType;
import org.olat.ims.qti.statistics.model.StatisticsItem;
import org.olat.ims.qti.statistics.model.StatisticAnswerOption;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
abstract class AbstractItemStatisticsController extends BasicController {
	
	
	protected final VelocityContainer mainVC;
	
	protected final Item item;
	protected final QTIType type;
	protected final QTIStatisticSearchParams searchParams;
	protected final QTIStatisticsManager qtiStatisticsManager;
	
	protected final int numOfParticipants;
	protected final String mediaBaseURL;
	
	public AbstractItemStatisticsController(UserRequest ureq, WindowControl wControl,
			Item item, QTIStatisticResourceResult resourceResult, boolean printMode) {
		super(ureq, wControl);
		this.item = item;
		numOfParticipants = resourceResult.getQTIStatisticAssessment().getNumOfParticipants();
		searchParams = resourceResult.getSearchParams();
		qtiStatisticsManager = CoreSpringFactory.getImpl(QTIStatisticsManager.class);
		
		mediaBaseURL = resourceResult.getResolver().getStaticsBaseURI() + "/";
		type = resourceResult.getType();

		int questionType = item.getQuestion().getType();
		switch(questionType) {
			case Question.TYPE_SC:
				mainVC = createVelocityContainer("statistics_item_" + type.name());
				initSingleChoice();
				initChoice();
				break;
			case Question.TYPE_MC:
				mainVC = createVelocityContainer("statistics_item_" + type.name());
				StatisticsItem itemstats = initChoice();
				initMultipleChoice(itemstats);
				break;
			case Question.TYPE_KPRIM:
				mainVC = createVelocityContainer("statistics_item_" + type.name());
				initKPrim();
				initChoice();
				break;
			case Question.TYPE_FIB:
				mainVC = createVelocityContainer("statistics_item_" + type.name());
				initFIB();
				initChoice();
				break;
			case Question.TYPE_ESSAY:
				mainVC = createVelocityContainer("statistics_essai");
				initEssay();
				break;
			default:
				mainVC = createVelocityContainer("statistics_item_" + type.name());
				break;
		}
		
		mainVC.contextPut("question", item.getQuestion().getQuestion().renderAsHtml(mediaBaseURL));
		mainVC.contextPut("questionType", questionType);
		mainVC.contextPut("title", item.getTitle());
		mainVC.contextPut("printMode", new Boolean(printMode));
		putInitialPanel(mainVC);
	}
	
	protected abstract void initSingleChoice();
	
	protected abstract void initMultipleChoice(StatisticsItem itemstats);
	
	protected abstract void initKPrim();
	
	protected void initFIB() {
		List<StatisticAnswerOption> processedAnswers = qtiStatisticsManager
				.getStatisticAnswerOptionsOfItem(item.getIdent(), searchParams);

		BarSeries d1 = new BarSeries();
		for (StatisticAnswerOption entry : processedAnswers) {
			String answerString = getAllBlanksFromAnswer(entry.getAnswer());
			d1.add(entry.getCount(), answerString);
		}

		BarChartComponent durationChart = new BarChartComponent("questionChart");
		durationChart.addSeries(d1);
		mainVC.put("questionChart", durationChart);
	}
	
	protected static String getAllBlanksFromAnswer(String answerString) {
		List<String> blanks = new ArrayList<String>();
		Pattern p = Pattern.compile("\\[\\[([^\\[\\[,\\]]*)\\]\\]");
		Matcher m = p.matcher(answerString);
		while (m.find()) {
			blanks.add(m.group().replace("]]", "").replace("[[", ""));
		}
		return StringUtils.join(blanks, ", ");
	}
	

	
	protected abstract StatisticsItem initChoice();
	
	protected void initEssay() {
		mainVC.contextPut("question", item.getQuestion().getQuestion().renderAsHtml(mediaBaseURL));
		mainVC.contextPut("title", item.getTitle());
		
		List<String> answers = qtiStatisticsManager.getAnswers(item.getIdent(), searchParams);

		List<String> cleanedAnswers = new ArrayList<String>();
		for (String string : answers) {
			cleanedAnswers.add(stripAnswerText(string));
		}
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
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	public static class ResponseInfos {
		
		private final String label;
		private final String text;
		private final float points;
		private final boolean correct;
		private final boolean survey;
		
		public ResponseInfos(String label, String text, float points, boolean correct, boolean survey) {
			this.label = label;
			this.text = text;
			this.points = points;
			this.survey = survey;
			this.correct = correct;
		}
	
		public String getLabel() {
			return label;
		}
	
		public String getText() {
			return text;
		}
	
		public float getPoints() {
			return points;
		}
	
		public boolean isSurvey() {
			return survey;
		}

		public boolean isCorrect() {
			return correct;
		}
	}
}
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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.chart.BarSeries;
import org.olat.core.gui.components.chart.HorizontalBarChartComponent;
import org.olat.core.gui.control.WindowControl;
import org.olat.ims.qti.editor.beecom.objects.Item;
import org.olat.ims.qti.editor.beecom.objects.QTIDocument;
import org.olat.ims.qti.editor.beecom.objects.Section;
import org.olat.ims.qti.statistics.QTIStatisticResourceResult;
import org.olat.ims.qti.statistics.model.StatisticAssessment;
import org.olat.ims.qti.statistics.model.StatisticSurveyItem;
import org.olat.ims.qti.statistics.model.StatisticSurveyItemResponse;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI12SurveyStatisticsController extends AbstractAssessmentStatisticsController {

	public QTI12SurveyStatisticsController(UserRequest ureq, WindowControl wControl,
			QTIStatisticResourceResult resourceResult, boolean printMode) {
		super(ureq, wControl, resourceResult, printMode, "statistics_survey");

		StatisticAssessment stats = resourceResult.getQTIStatisticAssessment();
		List<Item> items = new ArrayList<>();
		QTIDocument qtiDocument = resourceResult.getQTIDocument();
		for(Section section:qtiDocument.getAssessment().getSections()) {
			for(Item item:section.getItems()) {
				items.add(item);
			}
		}
		initCourseNodeInformation(stats);
		initItemsOverview(items);
	}
	
	private void initCourseNodeInformation(StatisticAssessment stats) {
		mainVC.contextPut("type", resourceResult.getType());
		mainVC.contextPut("numOfParticipants", stats.getNumOfParticipants());
		String duration = duration(stats.getAverageDuration());
		mainVC.contextPut("averageDuration", duration);
	}
	
	private void initItemsOverview(List<Item> items) {
		List<StatisticSurveyItem> surveyItems = qtiStatisticsManager
				.getStatisticAnswerOptions(resourceResult.getSearchParams(), items);
		
		BarSeries series = new BarSeries("bar_default");
		for(StatisticSurveyItem surveyItem:surveyItems) {
			Item item = surveyItem.getItem();
			String atext = item.getTitle();
			for(StatisticSurveyItemResponse response:surveyItem.getResponses()) {
				long  value = response.getNumOfResponses();
				String category;
				if(response.getResponse() != null && response.getResponse().getContent() != null) {
					String text = response.getResponse().getContent().renderAsText();
					category = text;
				} else {
					category = response.getAnswer();
				}
				series.add(value, atext + ":" + category);
			}
		}

		HorizontalBarChartComponent overviewSurvey = new HorizontalBarChartComponent("overviewSurvey");
		overviewSurvey.addSeries(series);
		mainVC.put("overviewSurveyBarChart", overviewSurvey);
	}
}
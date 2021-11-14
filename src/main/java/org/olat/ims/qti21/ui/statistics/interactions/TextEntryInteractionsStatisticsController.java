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
import org.olat.ims.qti21.model.statistics.AbstractTextEntryInteractionStatistics;
import org.olat.ims.qti21.model.statistics.TextEntryInteractionStatistics;
import org.olat.ims.qti21.ui.statistics.QTI21AssessmentItemStatisticsController;
import org.olat.ims.qti21.ui.statistics.QTI21StatisticResourceResult;
import org.olat.ims.qti21.ui.statistics.SeriesFactory;
import org.olat.ims.qti21.ui.statistics.interactions.ResponseInfos.ExplanationType;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.TextEntryInteraction;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;

/**
 * 
 * Initial date: 03.03.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TextEntryInteractionsStatisticsController extends BasicController {
	
	private final AssessmentItemRef itemRef;
	private final AssessmentItem assessmentItem;
	private final List<TextEntryInteraction> interactions;
	private final QTI21StatisticResourceResult resourceResult;
	
	@Autowired
	private QTI21StatisticsManager qtiStatisticsManager;
	
	public TextEntryInteractionsStatisticsController(UserRequest ureq, WindowControl wControl,
			AssessmentItemRef itemRef, AssessmentItem assessmentItem, List<TextEntryInteraction> interactions,
			QTI21StatisticResourceResult resourceResult) {
		super(ureq, wControl, Util.createPackageTranslator(QTI21AssessmentItemStatisticsController.class, ureq.getLocale()));
		this.interactions = interactions;
		this.itemRef = itemRef;
		this.assessmentItem = assessmentItem;
		this.resourceResult = resourceResult;

		VelocityContainer mainVC = createVelocityContainer("fib_interaction");
		Series series = getFIB();
		VelocityContainer vc = createVelocityContainer("hbar_item");
		vc.contextPut("series", series);
		mainVC.put("questionChart", vc);
		mainVC.contextPut("series", series);
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	private Series getFIB() {
		List<AbstractTextEntryInteractionStatistics> processedAnswers = qtiStatisticsManager
				.getTextEntryInteractionsStatistic(itemRef.getIdentifier().toString(), assessmentItem, interactions, resourceResult.getSearchParams());
		int numOfParticipants = resourceResult.getQTIStatisticAssessment().getNumOfParticipants();

		int i = 0;
		String cssColor = "bar_green";
		String color = "green";
		BarSeries d1 = new BarSeries(cssColor, color, null);
		List<ResponseInfos> responseInfos = new ArrayList<>();
		for (AbstractTextEntryInteractionStatistics entry : processedAnswers) {
			String label = Integer.toString(++i);
			String answerString = entry.getCorrectResponse();
			d1.add(entry.getNumOfCorrect(), label, cssColor);
			
			StringBuilder text = new StringBuilder();
			text.append(answerString);
			if(entry instanceof TextEntryInteractionStatistics) {
				TextEntryInteractionStatistics textEntry = (TextEntryInteractionStatistics)entry;
				if(textEntry.getAlternatives().size() > 1) {
					text.append(" [");
					for(int j=1; j<textEntry.getAlternatives().size(); j++) {
						if(j > 1) text.append(", ");
						text.append(textEntry.getAlternatives().get(j));
					}
					text.append("]");
				}
			}
			
			Float score = entry.getPoints() == null ? null : entry.getPoints().floatValue();
			responseInfos.add(new ResponseInfos(label, text.toString(), null, entry.getWrongAnswers(), score, true, false, ExplanationType.standard));
		}
		
		List<BarSeries> serieList = Collections.singletonList(d1);
		Series series = new Series(serieList, responseInfos, numOfParticipants, false);
		series.setChartType(SeriesFactory.BAR_ANSWERED);
		series.setItemCss("o_mi_qtifib");
		return series;
	}
}

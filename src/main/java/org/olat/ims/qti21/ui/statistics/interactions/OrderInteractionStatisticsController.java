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
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.chart.BarSeries;
import org.olat.core.gui.components.text.TextFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.ims.qti21.QTI21StatisticsManager;
import org.olat.ims.qti21.model.statistics.OrderStatistics;
import org.olat.ims.qti21.ui.components.FlowComponent;
import org.olat.ims.qti21.ui.statistics.QTI21AssessmentItemStatisticsController;
import org.olat.ims.qti21.ui.statistics.QTI21StatisticResourceResult;
import org.olat.ims.qti21.ui.statistics.SeriesFactory;
import org.olat.ims.qti21.ui.statistics.interactions.ResponseInfos.ExplanationType;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.OrderInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleChoice;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;

/**
 * 
 * Initial date: 16 avr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OrderInteractionStatisticsController extends BasicController {
	
	private final VelocityContainer mainVC;
	
	private int count = 0;
	private final String mapperUri;
	private final OrderInteraction interaction;
	private final AssessmentItemRef itemRef;
	private final AssessmentItem assessmentItem;
	private final QTI21StatisticResourceResult resourceResult;
	
	@Autowired
	private QTI21StatisticsManager qtiStatisticsManager;
	
	public OrderInteractionStatisticsController(UserRequest ureq, WindowControl wControl,
			AssessmentItemRef itemRef, AssessmentItem assessmentItem, OrderInteraction interaction,
			QTI21StatisticResourceResult resourceResult, String mapperUri) {
		super(ureq, wControl, Util.createPackageTranslator(QTI21AssessmentItemStatisticsController.class, ureq.getLocale()));
		this.interaction = interaction;
		this.itemRef = itemRef;
		this.assessmentItem = assessmentItem;
		this.resourceResult = resourceResult;
		this.mapperUri = mapperUri;
		
		mainVC = createVelocityContainer("statistics_interaction");
		Series series = getOrderedChoice();
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
	
	private Series getOrderedChoice() {
		List<OrderStatistics> statisticResponses = qtiStatisticsManager
				.getOrderInteractionStatistics(itemRef.getIdentifier().toString(), assessmentItem, interaction, resourceResult.getSearchParams());

		BarSeries d1 = new BarSeries("bar_green", "green", translate("answer.correct"));
		BarSeries d2 = new BarSeries("bar_red", "red", translate("answer.false"));
		BarSeries d3 = new BarSeries("bar_grey", "grey", translate("answer.noanswer"));
		
		int numOfParticipants = resourceResult.getQTIStatisticAssessment().getNumOfParticipants();
	
		int i = 0;
		List<ResponseInfos> responseInfos = new ArrayList<>();
		for(OrderStatistics statisticResponse:statisticResponses) {
			SimpleChoice choice = statisticResponse.getChoice();
			Component text = getAnswerText(choice);

			double correct = statisticResponse.getNumOfCorrect();
			double incorrect = statisticResponse.getNumOfIncorrect();
			double notAnswered = numOfParticipants - correct - incorrect;

			String label = Integer.toString(++i);
			d1.add(correct, label);
			d2.add(incorrect, label);
			d3.add(notAnswered, label);

			responseInfos.add(new ResponseInfos(label, text, null, true, false, ExplanationType.ordered));
		}

		List<BarSeries> serieList = new ArrayList<>(3);
		serieList.add(d1);
		serieList.add(d2);
		serieList.add(d3);
		
		Series series = new Series(serieList, responseInfos, numOfParticipants, true);
		series.setChartType(SeriesFactory.BAR_CORRECT_WRONG_NOT);
		series.setItemCss("o_qti_orderitem");
		return series;
	}
	
	private Component getAnswerText(SimpleChoice choice) {
		String cmpId = "order_" + (count++);
		String text = choice.getLabel();
		
		Component textCmp;
		if(StringHelper.containsNonWhitespace(text)) {
			textCmp = TextFactory.createTextComponentFromString(cmpId, text, null, true, null);
		} else {
			FlowComponent cmp = new FlowComponent(cmpId, resourceResult.getAssessmentItemFile(itemRef));
			cmp.setMapperUri(mapperUri);
			cmp.setFlowStatics(choice.getFlowStatics());
			textCmp = cmp;
		}
		mainVC.put(cmpId, textCmp);
		return textCmp;
	}
}

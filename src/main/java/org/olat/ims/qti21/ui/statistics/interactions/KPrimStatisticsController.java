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
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Util;
import org.olat.ims.qti21.QTI21StatisticsManager;
import org.olat.ims.qti21.model.statistics.KPrimStatistics;
import org.olat.ims.qti21.ui.components.FlowComponent;
import org.olat.ims.qti21.ui.statistics.QTI21AssessmentItemStatisticsController;
import org.olat.ims.qti21.ui.statistics.QTI21StatisticResourceResult;
import org.olat.ims.qti21.ui.statistics.SeriesFactory;
import org.olat.ims.qti21.ui.statistics.interactions.ResponseInfos.ExplanationType;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.MatchInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleAssociableChoice;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleMatchSet;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.types.Identifier;

/**
 * 
 * Initial date: 03.03.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class KPrimStatisticsController extends BasicController {
	
	private final VelocityContainer mainVC;
	
	private int count = 0;
	private final String mapperUri;
	private final MatchInteraction interaction;
	private final AssessmentItemRef itemRef;
	private final AssessmentItem assessmentItem;
	private final QTI21StatisticResourceResult resourceResult;
	
	@Autowired
	private QTI21StatisticsManager qtiStatisticsManager;
	
	public KPrimStatisticsController(UserRequest ureq, WindowControl wControl,
			AssessmentItemRef itemRef, AssessmentItem assessmentItem, MatchInteraction interaction,
			QTI21StatisticResourceResult resourceResult, String mapperUri) {
		super(ureq, wControl, Util.createPackageTranslator(QTI21AssessmentItemStatisticsController.class, ureq.getLocale()));
		this.interaction = interaction;
		this.itemRef = itemRef;
		this.assessmentItem = assessmentItem;
		this.resourceResult = resourceResult;
		this.mapperUri = mapperUri;
		
		mainVC = createVelocityContainer("kprim_interaction");
		Series series = getKPrim();
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
	
	public Series getKPrim() {
		List<KPrimStatistics> statisticResponses = qtiStatisticsManager
				.getKPrimStatistics(itemRef.getIdentifier().toString(), assessmentItem, interaction, resourceResult.getSearchParams());

		int numOfParticipants = resourceResult.getQTIStatisticAssessment().getNumOfParticipants();
		List<SimpleMatchSet> matchSets = interaction.getSimpleMatchSets();
		SimpleMatchSet fourMatchSet = matchSets.get(0);

		int i = 0;
		BarSeries d1 = new BarSeries("bar_green", "green", translate("answer.correct"));
		BarSeries d2 = new BarSeries("bar_red", "red", translate("answer.false"));
		BarSeries d3 = new BarSeries("bar_grey", "grey", translate("answer.noanswer"));

		List<ResponseInfos> responseInfos = new ArrayList<>();
		for (KPrimStatistics statisticResponse:statisticResponses) {
			Identifier choiceIdentifier = statisticResponse.getChoiceIdentifier();
			
			boolean correctRight = statisticResponse.isCorrectRight();
			double right = statisticResponse.getNumOfCorrect();
			double wrong = statisticResponse.getNumOfIncorrect();
			double notanswered = numOfParticipants - right - wrong;

			String label = Integer.toString(++i);
			d1.add(right, label);
			d2.add(wrong, label);
			d3.add(notanswered, label);
			
			FlowComponent text = null;
			for(SimpleAssociableChoice choice:fourMatchSet.getSimpleAssociableChoices()) {
				if(choice.getIdentifier().equals(choiceIdentifier)) {
					String textName = "kprims_" + (count++);
					text = new FlowComponent(textName, resourceResult.getAssessmentItemFile(itemRef));
					text.setFlowStatics(choice.getFlowStatics());
					text.setMapperUri(mapperUri);
					mainVC.put(textName, text);
				}
			}
			responseInfos.add(new ResponseInfos(label, text, null, correctRight, false, ExplanationType.kprim));
		}
		
		List<BarSeries> serieList = new ArrayList<>(3);
		serieList.add(d1);
		serieList.add(d2);
		serieList.add(d3);
		Series series = new Series(serieList, responseInfos, numOfParticipants, true);
		series.setChartType(SeriesFactory.BAR_CORRECT_WRONG_NOT);
		series.setItemCss("o_mi_qtikprim");
		return series;
	}
}

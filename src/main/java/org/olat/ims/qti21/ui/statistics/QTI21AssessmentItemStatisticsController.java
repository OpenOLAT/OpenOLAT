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
package org.olat.ims.qti21.ui.statistics;

import static org.olat.ims.qti.statistics.ui.StatisticFormatter.duration;
import static org.olat.ims.qti.statistics.ui.StatisticFormatter.formatTwo;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.chart.StatisticsComponent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;
import org.olat.ims.qti.statistics.QTIType;
import org.olat.ims.qti.statistics.model.StatisticsItem;
import org.olat.ims.qti21.QTI21StatisticsManager;
import org.olat.ims.qti21.model.QTI21StatisticSearchParams;
import org.olat.ims.qti21.ui.statistics.interactions.ChoiceInteractionStatisticsController;
import org.olat.ims.qti21.ui.statistics.interactions.UnsupportedInteractionController;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.ChoiceInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;

/**
 * 
 * Initial date: 03.02.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21AssessmentItemStatisticsController extends BasicController {
	
	private final VelocityContainer mainVC;
	
	private final int numOfParticipants;
	private final AssessmentItem item;
	private final AssessmentItemRef itemRef;
	private final QTI21StatisticSearchParams searchParams;
	private final QTI21StatisticResourceResult resourceResult;
	
	@Autowired
	private QTI21StatisticsManager qtiStatisticsManager;
	
	public QTI21AssessmentItemStatisticsController(UserRequest ureq, WindowControl wControl,
			AssessmentItemRef itemRef, AssessmentItem item, String sectionTitle, QTI21StatisticResourceResult resourceResult, boolean printMode) {
		super(ureq, wControl);
		
		this.item = item;
		this.itemRef = itemRef;
		this.resourceResult = resourceResult;
		searchParams = resourceResult.getSearchParams();
		numOfParticipants = resourceResult.getQTIStatisticAssessment().getNumOfParticipants();

		mainVC = createVelocityContainer("statistics_item");
		mainVC.put("d3loader", new StatisticsComponent("d3loader"));
		mainVC.contextPut("title", item.getTitle());
		if(StringHelper.containsNonWhitespace(sectionTitle)) {
			mainVC.contextPut("sectionTitle", sectionTitle);
		}
		mainVC.contextPut("numOfParticipants", resourceResult.getQTIStatisticAssessment().getNumOfParticipants());
		mainVC.contextPut("printMode", new Boolean(printMode));
		
		StatisticsItem itemStats = initItemStatistics();
		
		List<Interaction> interactions = item.getItemBody().findInteractions();
		List<String> interactionIds = new ArrayList<>(interactions.size());
		int counter = 0;
		for(Interaction interaction:interactions) {
			Component cmp = interactionControllerFactory(ureq, interaction, itemStats);
			String componentId = "interaction" + counter++;
			mainVC.put(componentId, cmp);
			interactionIds.add(componentId);
		}
		mainVC.contextPut("interactionIds", interactionIds);
		putInitialPanel(mainVC);
	}
	
	private Component interactionControllerFactory(UserRequest ureq, Interaction interaction, StatisticsItem itemStats) {
		Controller interactionCtrl = null;
		
		if(interaction instanceof ChoiceInteraction) {
			interactionCtrl = new ChoiceInteractionStatisticsController(ureq, getWindowControl(),
					itemRef, item, (ChoiceInteraction)interaction, itemStats, resourceResult);
		}

		if(interactionCtrl == null) {
			interactionCtrl = new UnsupportedInteractionController(ureq, getWindowControl(),
					interaction);
		}
		listenTo(interactionCtrl);
		return interactionCtrl.getInitialComponent();
	}
	
	protected StatisticsItem initItemStatistics() {
		boolean survey = QTIType.survey.equals(resourceResult.getType());
		double maxScore = 0.0d;

		StatisticsItem itemStats = qtiStatisticsManager
				.getAssessmentItemStatistics(itemRef.getIdentifier().toString(), maxScore, searchParams);

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
			mainVC.contextPut("notAnswered", notAnswered);
			mainVC.contextPut("itemDifficulty", formatTwo(itemStats.getDifficulty()));
			mainVC.contextPut("averageScore", formatTwo(itemStats.getAverageScore()));
		}
		mainVC.contextPut("averageDuration", duration(itemStats.getAverageDuration()));
		return itemStats;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}
	
	

}

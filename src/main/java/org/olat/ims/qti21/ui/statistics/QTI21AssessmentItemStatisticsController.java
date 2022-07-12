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

import static org.olat.ims.qti21.ui.statistics.StatisticFormatter.duration;
import static org.olat.ims.qti21.ui.statistics.StatisticFormatter.formatTwo;

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
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.QTI21StatisticsManager;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.QTI21StatisticSearchParams;
import org.olat.ims.qti21.model.statistics.StatisticsItem;
import org.olat.ims.qti21.model.xml.QtiNodesExtractor;
import org.olat.ims.qti21.ui.statistics.interactions.HotspotInteractionStatisticsController;
import org.olat.ims.qti21.ui.statistics.interactions.HottextInteractionStatisticsController;
import org.olat.ims.qti21.ui.statistics.interactions.InlineChoiceInteractionsStatisticsController;
import org.olat.ims.qti21.ui.statistics.interactions.KPrimStatisticsController;
import org.olat.ims.qti21.ui.statistics.interactions.MatchStatisticsController;
import org.olat.ims.qti21.ui.statistics.interactions.OrderInteractionStatisticsController;
import org.olat.ims.qti21.ui.statistics.interactions.SimpleChoiceInteractionStatisticsController;
import org.olat.ims.qti21.ui.statistics.interactions.TextEntryInteractionsStatisticsController;
import org.olat.ims.qti21.ui.statistics.interactions.UnsupportedInteractionController;
import org.olat.modules.assessment.ui.UserFilterController;
import org.olat.modules.assessment.ui.event.UserFilterEvent;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.ChoiceInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.EndAttemptInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.HotspotInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.HottextInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.InlineChoiceInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.MatchInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.OrderInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.TextEntryInteraction;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;

/**
 * 
 * Initial date: 03.02.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21AssessmentItemStatisticsController extends BasicController {
	
	private final VelocityContainer mainVC;
	
	private UserFilterController filterCtrl;

	private final String itemCss;
	private final String mapperUri;
	private final AssessmentItem item;
	private final AssessmentItemRef itemRef;
	private final QTI21StatisticSearchParams searchParams;
	private final QTI21StatisticResourceResult resourceResult;
	
	private final QTI21ItemBodyController itemBodyCtrl;
	
	@Autowired
	private QTI21StatisticsManager qtiStatisticsManager;
	
	public QTI21AssessmentItemStatisticsController(UserRequest ureq, WindowControl wControl,
			AssessmentItemRef itemRef, ResolvedAssessmentItem resolvedAssessmentItem,
			String sectionTitle, QTI21StatisticResourceResult resourceResult,
			boolean withFilter, boolean printMode) {
		super(ureq, wControl);
		
		item = resolvedAssessmentItem.getItemLookup().getRootNodeHolder().getRootNode();
		this.itemRef = itemRef;
		this.resourceResult = resourceResult;
		searchParams = resourceResult.getSearchParams();

		mainVC = createVelocityContainer("statistics_item");
		mainVC.put("d3loader", new StatisticsComponent("d3loader"));
		mainVC.contextPut("title", item.getTitle());
		if(StringHelper.containsNonWhitespace(sectionTitle)) {
			mainVC.contextPut("sectionTitle", sectionTitle);
		}
		mainVC.contextPut("printMode", Boolean.valueOf(printMode));
		
		QTI21QuestionType type = QTI21QuestionType.getTypeRelax(item);
		if(type != null) {
			itemCss = type.getCssClass();
		} else {
			itemCss = "o_mi_qtiunkown";
		}
		mainVC.contextPut("itemCss", itemCss);
		
		if(withFilter && (resourceResult.canViewAnonymousUsers() || resourceResult.canViewNonParticipantUsers())) {
			filterCtrl = new UserFilterController(ureq, getWindowControl(),
					resourceResult.canViewNonParticipantUsers(), resourceResult.canViewAnonymousUsers(),
					resourceResult.isViewNonParticipantUsers(), resourceResult.isViewAnonymousUsers());
			listenTo(filterCtrl);
			mainVC.put("filter", filterCtrl.getInitialComponent());
		}
		
		itemBodyCtrl = new QTI21ItemBodyController(ureq, getWindowControl(), itemRef, resolvedAssessmentItem, resourceResult);
		listenTo(itemBodyCtrl);
		mainVC.put("question", itemBodyCtrl.getInitialComponent());
		mainVC.contextPut("questionComponentId", itemBodyCtrl.getInteractionsComponentId());
		mapperUri = itemBodyCtrl.getMapperUri();
		
		putInitialPanel(mainVC);
		updateData(ureq);
	}
	
	protected String getItemCss() {
		return itemCss;
	}
	
	private void updateData(UserRequest ureq) {
		StatisticsItem itemStats = initItemStatistics();
		List<String> interactionIds = initInteractionControllers(ureq, itemStats);
		mainVC.contextPut("interactionIds", interactionIds);
	}
	
	private List<String> initInteractionControllers(UserRequest ureq, StatisticsItem itemStats) {
		List<Interaction> interactions = item.getItemBody().findInteractions();
		List<String> interactionIds = new ArrayList<>(interactions.size());
		int counter = 0;
		List<TextEntryInteraction> textEntryInteractions = new ArrayList<>();
		List<InlineChoiceInteraction> inlineChoiceInteractions = new ArrayList<>();
		for(Interaction interaction:interactions) {
			if(interaction instanceof EndAttemptInteraction) {
				continue;
			}
			
			if(interaction instanceof TextEntryInteraction) {
				textEntryInteractions.add((TextEntryInteraction)interaction);
			} else if(interaction instanceof InlineChoiceInteraction) {
				inlineChoiceInteractions.add((InlineChoiceInteraction)interaction);
			} else {
				Component cmp = interactionControllerFactory(ureq, interaction, itemStats);
				String componentId = "interaction" + counter++;
				mainVC.put(componentId, cmp);
				interactionIds.add(componentId);
			}
		}
		
		if(!textEntryInteractions.isEmpty()) {
			Controller interactionCtrl = new TextEntryInteractionsStatisticsController(ureq, getWindowControl(), itemRef, item, textEntryInteractions, resourceResult);
			listenTo(interactionCtrl);
			String componentId = "interaction" + counter++;
			mainVC.put(componentId, interactionCtrl.getInitialComponent());
			interactionIds.add(componentId);
		} else if(!inlineChoiceInteractions.isEmpty()) {
			Controller interactionCtrl = new InlineChoiceInteractionsStatisticsController(ureq, getWindowControl(), itemRef, item, inlineChoiceInteractions, resourceResult);
			listenTo(interactionCtrl);
			String componentId = "interaction" + counter++;
			mainVC.put(componentId, interactionCtrl.getInitialComponent());
			interactionIds.add(componentId);
		}
		
		return interactionIds;
	}
	
	private Component interactionControllerFactory(UserRequest ureq, Interaction interaction, StatisticsItem itemStats) {
		Controller interactionCtrl = null;
		
		if(interaction instanceof ChoiceInteraction) {
			interactionCtrl = new SimpleChoiceInteractionStatisticsController(ureq, getWindowControl(),
					itemRef, item, (ChoiceInteraction)interaction, itemStats, resourceResult, mapperUri);
		} else if(interaction instanceof OrderInteraction) {
			interactionCtrl = new OrderInteractionStatisticsController(ureq, getWindowControl(),
					itemRef, item, (OrderInteraction)interaction, resourceResult, mapperUri);
		} else if(interaction instanceof MatchInteraction) {
			String responseIdentifier = interaction.getResponseIdentifier().toString();
			if(responseIdentifier.startsWith("KPRIM_") 
					|| QTI21QuestionType.hasClass(interaction, QTI21Constants.CSS_MATCH_KPRIM)) {
				interactionCtrl = new KPrimStatisticsController(ureq, getWindowControl(),
						itemRef, item, (MatchInteraction)interaction, resourceResult, mapperUri);
			} else {
				interactionCtrl = new MatchStatisticsController(ureq, getWindowControl(),
						itemRef, item, (MatchInteraction)interaction, resourceResult, mapperUri);
			}
		} else if(interaction instanceof HotspotInteraction) {
			interactionCtrl = new HotspotInteractionStatisticsController(ureq, getWindowControl(),
					itemRef, item, (HotspotInteraction)interaction, itemStats, resourceResult);
		} else if(interaction instanceof HottextInteraction) {
			interactionCtrl = new HottextInteractionStatisticsController(ureq, getWindowControl(),
					itemRef, item, (HottextInteraction)interaction, itemStats, resourceResult, mapperUri);
		}

		if(interactionCtrl == null) {
			interactionCtrl = new UnsupportedInteractionController(ureq, getWindowControl(),
					interaction);
		}
		listenTo(interactionCtrl);
		return interactionCtrl.getInitialComponent();
	}
	
	protected StatisticsItem initItemStatistics() {
		double maxScore = 0.0d;
		Double maxScoreSettings = QtiNodesExtractor.extractMaxScore(item);
		if(maxScoreSettings != null) {
			maxScore = maxScoreSettings.doubleValue();
		}

		StatisticsItem itemStats = qtiStatisticsManager
				.getAssessmentItemStatistics(itemRef.getIdentifier().toString(), maxScore, searchParams);
		int numOfParticipants = resourceResult.getQTIStatisticAssessment().getNumOfParticipants();
		
		long rightAnswers = itemStats.getNumOfCorrectAnswers();
		long wrongAnswers = itemStats.getNumOfIncorrectAnswers();
		long notAnswered = numOfParticipants - rightAnswers - wrongAnswers;

		mainVC.contextPut("maxScore", maxScore);
		mainVC.contextPut("rightAnswers", rightAnswers);
		mainVC.contextPut("wrongAnswers", wrongAnswers);
		mainVC.contextPut("notAnswered", notAnswered);
		mainVC.contextPut("itemDifficulty", formatTwo(itemStats.getDifficulty()));
		mainVC.contextPut("averageScore", formatTwo(itemStats.getAverageScore()));
		mainVC.contextPut("numOfParticipants", numOfParticipants);
		mainVC.contextPut("averageDuration", duration(itemStats.getAverageDuration()));
		return itemStats;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(filterCtrl == source) {
			if(event instanceof UserFilterEvent) {
				UserFilterEvent ufe = (UserFilterEvent)event;
				resourceResult.setViewAnonymousUsers(ufe.isWithAnonymousUser());
				resourceResult.setViewNonPaticipantUsers(ufe.isWithNonParticipantUsers());
				updateData(ureq);
			}
		}
		super.event(ureq, source, event);
	}
}

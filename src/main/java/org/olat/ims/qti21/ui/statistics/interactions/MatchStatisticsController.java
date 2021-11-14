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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Util;
import org.olat.ims.qti21.QTI21StatisticsManager;
import org.olat.ims.qti21.model.statistics.MatchStatistics;
import org.olat.ims.qti21.model.xml.QtiNodesExtractor;
import org.olat.ims.qti21.ui.components.FlowComponent;
import org.olat.ims.qti21.ui.statistics.QTI21AssessmentItemStatisticsController;
import org.olat.ims.qti21.ui.statistics.QTI21StatisticResourceResult;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.MatchInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleAssociableChoice;
import uk.ac.ed.ph.jqtiplus.node.item.response.declaration.ResponseDeclaration;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.types.Identifier;

/**
 * 
 * Initial date: 22 nov. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MatchStatisticsController extends BasicController {
	
	private int count = 0;
	private final VelocityContainer mainVC;
	
	private final String mapperUri;
	private final MatchInteraction interaction;
	private final AssessmentItemRef itemRef;
	private final AssessmentItem assessmentItem;
	private final QTI21StatisticResourceResult resourceResult;
	
	private final List<ChoiceWrapper> sourceWrappers = new ArrayList<>();
	private final List<ChoiceWrapper> targetWrappers = new ArrayList<>();
	private final Map<Identifier,List<Identifier>> correctAnswers = new HashMap<>();
	
	@Autowired
	private QTI21StatisticsManager qtiStatisticsManager;
	
	public MatchStatisticsController(UserRequest ureq, WindowControl wControl,
			AssessmentItemRef itemRef, AssessmentItem assessmentItem, MatchInteraction interaction,
			QTI21StatisticResourceResult resourceResult, String mapperUri) {
		super(ureq, wControl, Util.createPackageTranslator(QTI21AssessmentItemStatisticsController.class, ureq.getLocale()));
		this.interaction = interaction;
		this.itemRef = itemRef;
		this.assessmentItem = assessmentItem;
		this.resourceResult = resourceResult;
		this.mapperUri = mapperUri;
		
		mainVC = createVelocityContainer("match_interaction");
		
		List<SimpleAssociableChoice> sourceChoices = interaction
				.getSimpleMatchSets().get(0).getSimpleAssociableChoices();
		for(SimpleAssociableChoice choice:sourceChoices) {
			sourceWrappers.add(new ChoiceWrapper(choice));
		}
		List<SimpleAssociableChoice> targetChoices = interaction
				.getSimpleMatchSets().get(1).getSimpleAssociableChoices();
		for(SimpleAssociableChoice choice:targetChoices) {
			targetWrappers.add(new ChoiceWrapper(choice));
		}
		mainVC.contextPut("sourceChoices", sourceWrappers);
		mainVC.contextPut("targetChoices", targetWrappers);
		if(assessmentItem.getResponseDeclaration(interaction.getResponseIdentifier()) != null) {
			ResponseDeclaration responseDeclaration = assessmentItem.getResponseDeclaration(interaction.getResponseIdentifier());
			QtiNodesExtractor.extractIdentifiersFromCorrectResponse(responseDeclaration.getCorrectResponse(), correctAnswers);
		}
		
		renderMatch();
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	private void renderMatch() {
		List<MatchStatistics> statisticResponses = qtiStatisticsManager
				.getMatchStatistics(itemRef.getIdentifier().toString(), assessmentItem, interaction, resourceResult.getSearchParams());

		long max = 0;
		for(MatchStatistics statistics:statisticResponses) {
			max = Math.max(max, statistics.getNumOfCorrect() + statistics.getNumOfIncorrect());
		}
		
		for(MatchStatistics statistics:statisticResponses) {
			Identifier sourceIdentifier = statistics.getSourceIdentifier();
			Identifier targetIdentifier = statistics.getDestinationIdentifier();
			String key = sourceIdentifier.toString() + "-" + targetIdentifier.toString();
			StatisticsWrapper sWrapper = new StatisticsWrapper(sourceIdentifier, targetIdentifier, statistics, max);
			mainVC.contextPut(key, sWrapper);
		}

		//fill the blanks
		for(ChoiceWrapper sourceChoice:sourceWrappers) {
			for(ChoiceWrapper targetChoice:targetWrappers) {
				Identifier sourceIdentifier = sourceChoice.getChoiceIdentifier();
				Identifier targetIdentifier = targetChoice.getChoiceIdentifier();
				String key = sourceIdentifier.toString() + "-" + targetIdentifier.toString();
				if(mainVC.contextGet(key) == null) {
					mainVC.contextPut(key, new StatisticsWrapper(sourceIdentifier, targetIdentifier, null, max));
				}
			}
		}
	}
	
	public class StatisticsWrapper {
		private final long max;
		private final Identifier sourceIdentifier;
		private final Identifier targetIdentifier;
		private final MatchStatistics statistics;
		
		public StatisticsWrapper(Identifier sourceIdentifier, Identifier targetIdentifier,
				MatchStatistics statistics, long max) {
			this.sourceIdentifier = sourceIdentifier;
			this.targetIdentifier = targetIdentifier;
			this.statistics = statistics;
			this.max = max;
		}

		public Identifier getSourceIdentifier() {
			return sourceIdentifier;
		}

		public Identifier getTargetIdentifier() {
			return targetIdentifier;
		}
		
		public long getNumOfCorrect() {
			return statistics == null ? 0 : statistics.getNumOfCorrect();
		}
		
		public long getNumOfIncorrect() {
			return statistics == null ? 0 : statistics.getNumOfIncorrect();
		}
		
		public String getCssClass() {
			String cssClass;
			if(max == 0 || statistics == null) {
				cssClass = "";
			} else if(statistics.getNumOfCorrect() > 0) {
				cssClass = "bar_green";
			} else if(statistics.getNumOfIncorrect() > 0) {
				cssClass = "bar_red";
			} else {
				cssClass = "";
			}
			return cssClass;
		}
		
		public String getValue() {
			String val;
			if(max == 0 || statistics == null) {
				val = "";
			} else if(statistics.getNumOfCorrect() > 0) {
				val = Long.toString(statistics.getNumOfCorrect());
			} else if(statistics.getNumOfIncorrect() > 0) {
				val = Long.toString(statistics.getNumOfIncorrect());
			} else {
				val = "";
			}
			return val;
		}
		
		public long getRelative(int ref) {
			if(max == 0 || statistics == null) {
				return 0;
			}
			
			double val = 0;
			if(statistics.getNumOfCorrect() > 0) {
				val = statistics.getNumOfCorrect();
			} else if(statistics.getNumOfIncorrect() > 0) {
				val = statistics.getNumOfIncorrect();
			}
			
			double point = (double)ref / (double)max;
			return Math.round(point * val);
		}
		
		public long getRelativeBorder(int ref) {
			long border = getRelative(ref);
			return border <= 0 ? 0 : border / 2;
		}
		
		public long getRelativeMargin(int ref) {
			long val = getRelative(ref);
			return val == 0 ? ref : (ref - val) / 2;
		}
	}
	
	public class ChoiceWrapper {
		private final Identifier choiceIdentifier;
		private final SimpleAssociableChoice choice;
		private final FlowComponent summary;
		
		public ChoiceWrapper(SimpleAssociableChoice choice) {
			this.choice = choice;
			this.choiceIdentifier = choice.getIdentifier();
			
			String summaryName = "sum_" + (count++);
			summary = new FlowComponent(summaryName, resourceResult.getAssessmentItemFile(itemRef));
			summary.setMapperUri(mapperUri);
			summary.setFlowStatics(choice.getFlowStatics());
			mainVC.put(summaryName, summary);
		}

		public Identifier getChoiceIdentifier() {
			return choiceIdentifier;
		}

		public SimpleAssociableChoice getChoice() {
			return choice;
		}

		public FlowComponent getSummary() {
			return summary;
		}
	}
}

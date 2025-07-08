/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.topicbroker;

import java.util.Collection;

import org.olat.modules.topicbroker.manager.DefaultEnrollmentStrategy;
import org.olat.modules.topicbroker.manager.MaxEnrollmentsCriterion;
import org.olat.modules.topicbroker.manager.MaxPrioritiesCriterion;
import org.olat.modules.topicbroker.manager.MaxTopicsCriterion;
import org.olat.modules.topicbroker.model.TBEnrollmentStrategyConfigImpl;
import org.olat.modules.topicbroker.model.TBEnrollmentStrategyContextImpl;
import org.olat.modules.topicbroker.ui.TBUIFactory;

/**
 * 
 * Initial date: Jun 25, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBEnrollmentStrategyFactory {
	
	public static TBEnrollmentStrategyConfig getDefaultConfig() {
		return createMaxEnrollmentsConfig();
	}
	
	public static TBEnrollmentStrategyConfig createConfig(TBEnrollmentStrategyType type) {
		if (type == null) {
			return getDefaultConfig();
		}
		
		return switch (type) {
		case maxEnrollments -> createMaxEnrollmentsConfig();
		case maxPriorities -> createMaxPrioritiesConfig();
		case maxTopics -> createMaxTopicsConfig();
		case custom -> createCustomConfig();
		};
	}
	
	public static TBEnrollmentStrategyConfig createMaxEnrollmentsConfig() {
		TBEnrollmentStrategyConfigImpl config = new TBEnrollmentStrategyConfigImpl();
		config.setType(TBEnrollmentStrategyType.maxEnrollments);
		config.setMaxEnrollmentsWeight(Integer.valueOf(3));
		return config;
	}
	
	public static TBEnrollmentStrategyConfig createMaxPrioritiesConfig() {
		TBEnrollmentStrategyConfigImpl config = new TBEnrollmentStrategyConfigImpl();
		config.setType(TBEnrollmentStrategyType.maxPriorities);
		config.setMaxPrioritiesWeight(Integer.valueOf(3));
		return config;
	}
	
	public static TBEnrollmentStrategyConfig createMaxTopicsConfig() {
		TBEnrollmentStrategyConfigImpl config = new TBEnrollmentStrategyConfigImpl();
		config.setType(TBEnrollmentStrategyType.maxTopics);
		config.setMaxTopicsWeight(Integer.valueOf(3));
		return config;
	}
	
	public static TBEnrollmentStrategyConfig createCustomConfig() {
		TBEnrollmentStrategyConfigImpl config = new TBEnrollmentStrategyConfigImpl();
		config.setType(TBEnrollmentStrategyType.custom);
		config.setMaxEnrollmentsWeight(Integer.valueOf(3));
		config.setMaxTopicsWeight(Integer.valueOf(3));
		config.setMaxPrioritiesWeight(Integer.valueOf(3));
		return config;
	}
	
	public static TBEnrollmentStrategyContext createContext(TBBroker broker, Collection<TBTopic> topics, Collection<TBSelection> selections) {
		int maxSelections = broker.getMaxSelections();

		int numTopicsTotal = (int)topics.stream()
				.distinct()
				.count();
		
		int numRequiredEnrollmentsTotal = selections.stream()
				.map(TBSelection::getParticipant)
				.distinct()
				.mapToInt(participant -> TBUIFactory.getRequiredEnrollments(broker, participant))
				.sum();
		
		return new TBEnrollmentStrategyContextImpl(maxSelections, numTopicsTotal, numRequiredEnrollmentsTotal);
	}
	
	public static TBEnrollmentStrategy createStrategy(TBEnrollmentStrategyConfig config, TBEnrollmentStrategyContext context) {
		TBEnrollmentStrategy evaluator = new DefaultEnrollmentStrategy(config);
		
		if (config.getMaxEnrollmentsWeight() != null && config.getMaxEnrollmentsWeight() > 0) {
			int numRequiredEnrollmentsTotal = context.getNumRequiredEnrollmentsTotal();
			TBEnrollmentStrategyCriterion criterion = new MaxEnrollmentsCriterion(numRequiredEnrollmentsTotal);
			evaluator.addCriterion(criterion, config.getMaxEnrollmentsWeight());
		}
		
		if (config.getMaxTopicsWeight() != null && config.getMaxTopicsWeight() > 0) {
			int numTopicsTotal = context.getNumTopicsTotal();
			TBEnrollmentStrategyCriterion criterion = createMaxTopicsCriterion(numTopicsTotal);
			evaluator.addCriterion(criterion, config.getMaxTopicsWeight());
		}
		
		if (config.getMaxPrioritiesWeight() != null && config.getMaxPrioritiesWeight() > 0) {
			int maxSelections = context.getMaxSelections();
			TBEnrollmentStrategyCriterion criterion = createMaxPrioritiesCriterion(maxSelections);
			evaluator.addCriterion(criterion, config.getMaxPrioritiesWeight());
		}
		
		return evaluator;
	}
	
	public static MaxTopicsCriterion createMaxTopicsCriterion(int numTopicsTotal) {
		return new MaxTopicsCriterion(numTopicsTotal);
	}
	
	public static MaxEnrollmentsCriterion createMaxEnrollmentsCriterion(int numRequiredEnrollmentsTotal) {
		return new MaxEnrollmentsCriterion(numRequiredEnrollmentsTotal);
	}
	
	public static MaxPrioritiesCriterion createMaxPrioritiesCriterion(int maxSelections) {
		return new MaxPrioritiesCriterion(maxSelections);
	}

}

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
package org.olat.modules.topicbroker.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.olat.modules.topicbroker.TBEnrollmentStrategy;
import org.olat.modules.topicbroker.TBEnrollmentStrategyConfig;
import org.olat.modules.topicbroker.TBEnrollmentStrategyCriterion;
import org.olat.modules.topicbroker.TBSelection;

/**
 * 
 * Initial date: Jun 25, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class DefaultEnrollmentStrategy implements TBEnrollmentStrategy {
	
	private final TBEnrollmentStrategyConfig config;
	private final Map<String, TBEnrollmentStrategyCriterion> criterionTypeToCriterion = new HashMap<>(3);
	private final Map<String, Integer> criterionTypeToWeight = new HashMap<>(3);
	
	public DefaultEnrollmentStrategy(TBEnrollmentStrategyConfig config) {
		this.config = config;
	}

	@Override
	public void addCriterion(TBEnrollmentStrategyCriterion criterion, int weight) {
		criterionTypeToCriterion.put(criterion.getType(), criterion);
		criterionTypeToWeight.put(criterion.getType(), Integer.valueOf(weight));
	}

	@Override
	public double getValue(List<TBSelection> selections) {
		if (criterionTypeToCriterion.isEmpty()) {
			return 0;
		}
		
		double valuesSum = 0;
		int weightsSum = 0;
		for (Entry<String, TBEnrollmentStrategyCriterion> typeToCriterion : criterionTypeToCriterion.entrySet()) {
			int weight = criterionTypeToWeight.getOrDefault(typeToCriterion.getKey(), 0);
			if (weight > 0) {
				weightsSum += weight;
				valuesSum += weight * typeToCriterion.getValue().getValue(selections);
			}
		}
		
		return weightsSum > 0? valuesSum / weightsSum: 0;
	}

	@Override
	public TBEnrollmentStrategyConfig getConfig() {
		return config;
	}

}

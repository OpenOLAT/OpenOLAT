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
package org.olat.modules.quality.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 
 * Initial date: 12.09.2018<br>
 * 
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class GroupedStatistics<V extends GroupedStatisticKeys> {

	private final Collection<V> allStatistics = new ArrayList<>();
	private final Map<String, Map<MultiKey, V>> statisticsByMultiKey = new HashMap<>();
	private final Map<String, Map<TemporalKey, V>> statisticsByTemporalKey = new HashMap<>();

	public GroupedStatistics() {
		//
	}

	public GroupedStatistics(Collection<V> collection) {
		for (V statistic : collection) {
			putStatistic(statistic);
		}
	}

	public void putStatistic(V statistic) {
		putAll(statistic);
		putMultiKey(statistic);
		putTemporalKey(statistic);
	}

	private void putAll(V statistic) {
		allStatistics.add(statistic);
	}

	private void putMultiKey(V statistic) {
		String identifier = statistic.getIdentifier();
		MultiKey multiKey = statistic.getMultiKey();
		if (!MultiKey.none().equals(multiKey)) {
			Map<MultiKey, V> grouped = statisticsByMultiKey.get(identifier);
			if (grouped == null) {
				grouped = new HashMap<>();
				statisticsByMultiKey.put(identifier, grouped);
			}
			grouped.put(multiKey, statistic);
			
		}
	}

	private void putTemporalKey(V statistic) {
		String identifier = statistic.getIdentifier();
		TemporalKey temporalKey = statistic.getTemporalKey();
		if (!TemporalKey.none().equals(temporalKey)) {
			Map<TemporalKey, V> grouped = statisticsByTemporalKey.get(identifier);
			if (grouped == null) {
				grouped = new HashMap<>();
				statisticsByTemporalKey.put(identifier, grouped);
			}
			grouped.put(temporalKey, statistic);
		}
	}

	public V getStatistic(String identifier, MultiKey multiKey) {
		Map<MultiKey, V> grouped = statisticsByMultiKey.get(identifier);
		if (grouped != null) {
			return grouped.get(multiKey);
		}
		return null;
	}
	
	public V getStatistic(String identifier, TemporalKey temporalKey) {
		Map<TemporalKey, V> grouped = statisticsByTemporalKey.get(identifier);
		if (grouped != null) {
			return grouped.get(temporalKey);
		}
		return null;
	}

	public Map<MultiKey, V> getStatisticsByMultiKey(String identifier) {
		return statisticsByMultiKey.get(identifier);
	}
	
	public Map<TemporalKey, V> getStatisticsByTemporalKey(String identifier) {
		return statisticsByTemporalKey.get(identifier);
	}

	public Collection<V> getStatistics() {
		return allStatistics;
	}
	
	public Set<MultiKey> getMultiKeys() {
		Set<MultiKey> keys = new HashSet<>();
		for (V statistic : getStatistics()) {
			keys.add(statistic.getMultiKey());
		}
		return keys;
	}
	
	public Set<String> getIdentifiers() {
		Set<String> identifiers = new HashSet<>();
		for (V statistic : getStatistics()) {
			identifiers.add(statistic.getIdentifier());
		}
		return identifiers;
	}
}

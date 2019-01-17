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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * Initial date: 15 Jan 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class MultiTrendSeries<V> {
	
	private final TemporalGroupBy temporalGroupBy;
	private final List<TemporalKey> temporalKeys;
	private final Map<V, TrendSeries> multiTrendSeries = new HashMap<>();

	public MultiTrendSeries() {
		this.temporalGroupBy = null;
		this.temporalKeys = Collections.emptyList();
	}
	
	public MultiTrendSeries(TemporalGroupBy temporalGroupBy, TemporalKey min, TemporalKey max) {
		this.temporalGroupBy = temporalGroupBy;
		this.temporalKeys = new ArrayList<>();
		if (!TemporalKey.none().equals(min) && !TemporalKey.none().equals(max)) {
			generateTemporalKeys(temporalKeys, min, max);
		}
	}

	private void generateTemporalKeys(List<TemporalKey> seriesTemporalKeys, TemporalKey current, TemporalKey max) {
		seriesTemporalKeys.add(current);
		if (!current.equals(max)) {
			TemporalKey next = temporalGroupBy.getNextKey(current);
			generateTemporalKeys(seriesTemporalKeys, next, max);
		}
	}

	public List<TemporalKey> getTemporalKeys() {
		return temporalKeys;
	}
	
	public int indexOf(TemporalKey temporalKey) {
		return temporalKeys.indexOf(temporalKey);
	}

	public TrendSeries getSeries(V identifier) {
		return getOrCreateTrendSeries(identifier);
	}
	
	public void put(V identifier, TrendSeries trendSeries) {
		multiTrendSeries.put(identifier, trendSeries);
	}

	public void put(V identifier, TemporalKey temporalKey, Trend trend) {
		TrendSeries series = getOrCreateTrendSeries(identifier);
		series.set(indexOf(temporalKey), trend);
	}

	private TrendSeries getOrCreateTrendSeries(V identifier) {
		TrendSeries series = multiTrendSeries.get(identifier);
		if (series == null) {
			series = new TrendSeries(temporalKeys.size());
			multiTrendSeries.put(identifier, series);
		}
		return series;
	}
	

}

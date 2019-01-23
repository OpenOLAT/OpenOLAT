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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;
import org.olat.modules.quality.analysis.model.TrendImpl;

/**
 * 
 * Initial date: 16 Jan 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class MultiTrendSeriesTest {
	
	@Test
	public void shouldGenerateTemporalKeys() {
		TemporalKey tk2000 = TemporalKey.of(2000);
		TemporalKey tk2001 = TemporalKey.of(2001);
		TemporalKey tk2002 = TemporalKey.of(2002);
		TemporalKey tk2003 = TemporalKey.of(2003);

		MultiTrendSeries<String> multiTrendSeries = new MultiTrendSeries<>(TemporalGroupBy.DATA_COLLECTION_DEADLINE_YEAR, tk2000, tk2003);
		
		assertThat(multiTrendSeries.getTemporalKeys()).containsExactly(tk2000, tk2001, tk2002, tk2003);
	}
	@Test
	public void shouldGenerateTemporalKeysIfMinEqualsMax() {
		TemporalKey tk2000 = TemporalKey.of(2000);

		MultiTrendSeries<String> multiTrendSeries = new MultiTrendSeries<>(TemporalGroupBy.DATA_COLLECTION_DEADLINE_YEAR, tk2000, tk2000);
		
		assertThat(multiTrendSeries.getTemporalKeys()).containsExactly(tk2000);
	}

	@Test
	public void shouldInitSeriesWithNulls() {
		String identifier = "1";
		TemporalKey tk2000 = TemporalKey.of(2000);
		TemporalKey tk2003 = TemporalKey.of(2003);

		MultiTrendSeries<String> multiTrendSeries = new MultiTrendSeries<>(TemporalGroupBy.DATA_COLLECTION_DEADLINE_YEAR, tk2000, tk2003);
		multiTrendSeries.put(identifier, tk2000, null);
		
		List<Trend> trendList = multiTrendSeries.getSeries(identifier).toList();
		assertThat(trendList).hasSize(4).containsExactly(null, null, null, null);
	}

	@Test
	public void shouldPutToIndex() {
		String identifier = "1";
		TemporalKey tk2000 = TemporalKey.of(2000);
		TemporalKey tk2003 = TemporalKey.of(2003);
		Trend trend = new TrendImpl(null, null, null, null);

		MultiTrendSeries<String> multiTrendSeries = new MultiTrendSeries<>(TemporalGroupBy.DATA_COLLECTION_DEADLINE_YEAR, tk2000, tk2003);
		multiTrendSeries.put(identifier, tk2003, trend);
		
		List<Trend> trendList = multiTrendSeries.getSeries(identifier).toList();
		assertThat(trendList).containsExactly(null, null, null, trend);
	}
	
	@Test
	public void shouldNeverGetNullSeries() {
		String identifier = "1";
		TemporalKey tk2000 = TemporalKey.of(2000);
		TemporalKey tk2003 = TemporalKey.of(2003);

		MultiTrendSeries<String> multiTrendSeries = new MultiTrendSeries<>(TemporalGroupBy.DATA_COLLECTION_DEADLINE_YEAR, tk2000, tk2003);
		
		List<Trend> trendList = multiTrendSeries.getSeries(identifier).toList();
		assertThat(trendList).containsExactly(null, null, null, null);
	}

}

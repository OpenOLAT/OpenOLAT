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
package org.olat.modules.quality.analysis.model;

import org.olat.modules.quality.analysis.MultiKey;
import org.olat.modules.quality.analysis.RawGroupedStatistic;
import org.olat.modules.quality.analysis.TemporalKey;

/**
 * 
 * Initial date: 16 Jan 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class RawGroupedStatisticImpl implements RawGroupedStatistic {
	
	private final String identifier;
	private final MultiKey multiKey;
	private final TemporalKey temporalKey;
	private final Long count;
	private final Double rawAvg;
	
	public RawGroupedStatisticImpl(String identifier, String groupedKey1, String groupedKey2, String groupedKey3,
			String temporalKey, Long count, Double rawAvg) {
		this(identifier, MultiKey.of(groupedKey1, groupedKey2, groupedKey3), TemporalKey.parse(temporalKey), count, rawAvg);
	}

	public RawGroupedStatisticImpl(String identifier, MultiKey multiKey, TemporalKey temporalKey, Long count, Double rawAvg) {
		this.identifier = identifier;
		this.multiKey = multiKey;
		this.temporalKey = temporalKey;
		this.count = count;
		this.rawAvg = rawAvg;
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public MultiKey getMultiKey() {
		return multiKey;
	}

	@Override
	public TemporalKey getTemporalKey() {
		return temporalKey;
	}

	@Override
	public Long getCount() {
		return count;
	}

	@Override
	public Double getRawAvg() {
		return rawAvg;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RawGroupedStatisticImpl [identitfier=");
		builder.append(identifier);
		builder.append(", multiKey=");
		builder.append(multiKey);
		builder.append(", temporalKey=");
		builder.append(temporalKey);
		builder.append(", count=");
		builder.append(count);
		builder.append(", rawAvg=");
		builder.append(rawAvg);
		builder.append("]");
		return builder.toString();
	}
}

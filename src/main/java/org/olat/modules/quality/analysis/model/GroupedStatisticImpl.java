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

import org.olat.modules.forms.RubricRating;
import org.olat.modules.quality.analysis.GroupedStatistic;
import org.olat.modules.quality.analysis.MultiKey;
import org.olat.modules.quality.analysis.TemporalKey;

/**
 * 
 * Initial date: 16 Jan 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class GroupedStatisticImpl implements GroupedStatistic {

	private final String identitfier;
	private final MultiKey multiKey;
	private final TemporalKey temporalKey;
	private final Long count;
	private final Double rawAvg;
	private final Double avg;
	private final RubricRating rating;
	
	public GroupedStatisticImpl(String identitfier, MultiKey multiKey, TemporalKey temporalKey, Long count,
			Double rawAvg, Double avg, RubricRating rating) {
		this.identitfier = identitfier;
		this.multiKey = multiKey;
		this.temporalKey = temporalKey;
		this.count = count;
		this.rawAvg = rawAvg;
		this.avg = avg;
		this.rating = rating;
	}

	@Override
	public String getIdentifier() {
		return identitfier;
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
	public Double getAvg() {
		return avg;
	}

	@Override
	public RubricRating getRating() {
		return rating;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GroupedStatisticImpl [identitfier=");
		builder.append(identitfier);
		builder.append(", multiKey=");
		builder.append(multiKey);
		builder.append(", temporalKey=");
		builder.append(temporalKey);
		builder.append(", count=");
		builder.append(count);
		builder.append(", rawAvg=");
		builder.append(rawAvg);
		builder.append(", avg=");
		builder.append(avg);
		builder.append(", rating=");
		builder.append(rating);
		builder.append("]");
		return builder.toString();
	}
}

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

/**
 * 
 * Initial date: 11.09.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class GroupedStatistic {
	
	private final String identitfier;
	private final MultiKey multiKey;
	private final Long count;
	private final Double avg;
	
	public GroupedStatistic(String identitfier, Long groupedKey1, Long groupedKey2, Long groupedKey3, Long count, Double avg) {
		this(identitfier, MultiKey.of(groupedKey1, groupedKey2, groupedKey3), count, avg);
	}

	public GroupedStatistic(String identitfier, MultiKey multiKey, Long count, Double avg) {
		this.identitfier = identitfier;
		this.multiKey = multiKey;
		this.count = count;
		this.avg = avg;
	}

	public String getIdentifier() {
		return identitfier;
	}

	public MultiKey getMultiKey() {
		return multiKey;
	}

	public Long getCount() {
		return count;
	}

	public Double getAvg() {
		return avg;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GroupedStatistic [identitfier=");
		builder.append(identitfier);
		builder.append(", multiKey=");
		builder.append(multiKey);
		builder.append(", count=");
		builder.append(count);
		builder.append(", avg=");
		builder.append(avg);
		builder.append("]");
		return builder.toString();
	}

}

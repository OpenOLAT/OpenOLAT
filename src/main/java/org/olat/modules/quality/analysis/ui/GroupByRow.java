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
package org.olat.modules.quality.analysis.ui;

import java.util.List;

import org.olat.modules.quality.analysis.GroupedStatistic;
import org.olat.modules.quality.analysis.MultiKey;

/**
 * 
 * Initial date: 11.09.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class GroupByRow {
	
	private final MultiKey multiKey;
	private final List<String> groupNames;
	private final List<? extends GroupedStatistic> statistics;
	
	public GroupByRow(MultiKey multiKey, List<String> groupNames, List<? extends GroupedStatistic> statistics) {
		this.multiKey = multiKey;
		this.groupNames = groupNames;
		this.statistics = statistics;
	}
	
	public MultiKey getMultiKey() {
		return multiKey;
	}

	public int getGroupNamesSize() {
		return groupNames.size();
	}

	public String getGroupName(int index) {
		return groupNames.get(index);
	}
	
	public List<String> getGroupNames() {
		return groupNames;
	}

	public int getStatisticsSize() {
		return statistics.size();
	}

	public GroupedStatistic getStatistic(int index) {
		return statistics.get(index);
	}

}

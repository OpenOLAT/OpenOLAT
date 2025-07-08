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

import java.util.List;

import org.olat.modules.topicbroker.TBEnrollmentStrategyCriterion;
import org.olat.modules.topicbroker.TBSelection;

/**
 * 
 * Initial date: Jun 25, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class MaxTopicsCriterion implements TBEnrollmentStrategyCriterion {
	
	private final int numTopicsTotal;
	
	public MaxTopicsCriterion(int numTopicsTotal) {
		this.numTopicsTotal = numTopicsTotal;
	}

	@Override
	public String getType() {
		return "strategy.criterion.max.topics";
	}

	@Override
	public double getValue(List<TBSelection> selections) {
		long enrolledTopics = selections.stream()
			.filter(TBSelection::isEnrolled)
			.map(selection -> selection.getTopic().getKey())
			.distinct()
			.count();
		return numTopicsTotal > 0? (double)enrolledTopics / numTopicsTotal : 0;
	}

}

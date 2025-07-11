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
package org.olat.modules.topicbroker.model;

import org.olat.modules.topicbroker.TBEnrollmentFunction;
import org.olat.modules.topicbroker.TBEnrollmentStrategyConfig;
import org.olat.modules.topicbroker.TBEnrollmentStrategyType;

/**
 * 
 * Initial date: Jul 2, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBEnrollmentStrategyConfigImpl implements TBEnrollmentStrategyConfig {
	
	private TBEnrollmentStrategyType type;
	// max enrollments
	private Integer maxEnrollmentsWeight;
	// max topics
	private Integer maxTopicsWeight;
	// max priorities
	private Integer maxPrioritiesWeight;
	private TBEnrollmentFunction maxPrioritiesFunction;
	private Integer maxPriorityBreakPoint;
	private TBEnrollmentFunction maxPrioritiesFunctionAfter;

	@Override
	public TBEnrollmentStrategyType getType() {
		return type;
	}

	public void setType(TBEnrollmentStrategyType type) {
		this.type = type;
	}

	@Override
	public Integer getMaxEnrollmentsWeight() {
		return maxEnrollmentsWeight;
	}

	@Override
	public void setMaxEnrollmentsWeight(Integer maxEnrollmentsWeight) {
		this.maxEnrollmentsWeight = maxEnrollmentsWeight;
	}

	@Override
	public Integer getMaxTopicsWeight() {
		return maxTopicsWeight;
	}

	@Override
	public void setMaxTopicsWeight(Integer maxTopicsWeight) {
		this.maxTopicsWeight = maxTopicsWeight;
	}

	@Override
	public Integer getMaxPrioritiesWeight() {
		return maxPrioritiesWeight;
	}

	@Override
	public void setMaxPrioritiesWeight(Integer maxPrioritiesWeight) {
		this.maxPrioritiesWeight = maxPrioritiesWeight;
	}

	@Override
	public TBEnrollmentFunction getMaxPrioritiesFunction() {
		return maxPrioritiesFunction;
	}

	@Override
	public void setMaxPrioritiesFunction(TBEnrollmentFunction maxPrioritiesFunction) {
		this.maxPrioritiesFunction = maxPrioritiesFunction;
	}

	@Override
	public Integer getMaxPriorityBreakPoint() {
		return maxPriorityBreakPoint;
	}

	@Override
	public void setMaxPriorityBreakPoint(Integer maxPriorityBreakPoint) {
		this.maxPriorityBreakPoint = maxPriorityBreakPoint;
	}

	@Override
	public TBEnrollmentFunction getMaxPrioritiesFunctionAfter() {
		return maxPrioritiesFunctionAfter;
	}

	@Override
	public void setMaxPrioritiesFunctionAfter(TBEnrollmentFunction maxPrioritiesFunctionAfter) {
		this.maxPrioritiesFunctionAfter = maxPrioritiesFunctionAfter;
	}
	
}

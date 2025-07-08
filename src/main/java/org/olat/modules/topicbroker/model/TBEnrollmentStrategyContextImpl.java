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

import org.olat.modules.topicbroker.TBEnrollmentStrategyContext;

/**
 * 
 * Initial date: Jul 7, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBEnrollmentStrategyContextImpl implements TBEnrollmentStrategyContext {
	
	private final int maxSelections;
	private final int numTopicsTotal;
	private final int numRequiredEnrollmentsTotal;
	
	public TBEnrollmentStrategyContextImpl(int maxSelections, int numTopicsTotal, int numRequiredEnrollmentsTotal) {
		this.maxSelections = maxSelections;
		this.numTopicsTotal = numTopicsTotal;
		this.numRequiredEnrollmentsTotal = numRequiredEnrollmentsTotal;
	}

	@Override
	public int getMaxSelections() {
		return maxSelections;
	}

	@Override
	public int getNumTopicsTotal() {
		return numTopicsTotal;
	}

	@Override
	public int getNumRequiredEnrollmentsTotal() {
		return numRequiredEnrollmentsTotal;
	}
	
}

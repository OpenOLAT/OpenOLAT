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
package org.olat.modules.forms.model;

import java.util.Arrays;

import org.olat.modules.forms.StepCounts;

/**
 * 
 * Initial date: 2 Jul 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class StepCountsBuilder {
	
	private final Long[] stepCounts;
	private Long countNoResponses;
	private Long countComments;

	public static StepCountsBuilder builder(int numberOfSteps) {
		return new StepCountsBuilder(numberOfSteps);
	}
	
	private StepCountsBuilder(int numberOfSteps) {
		this.stepCounts = new Long[numberOfSteps + 1];
		
	}
	
	public StepCountsBuilder withCount(int step, Long count) {
		stepCounts[step] = count;
		return this;
	}
	
	public StepCountsBuilder withCountNoResponses(Long countNoResponses) {
		this.countNoResponses = countNoResponses;
		return this;
	}
	
	public StepCountsBuilder withCountComments(Long countComments) {
		this.countComments = countComments;
		return this;
	}
	
	public StepCounts build() {
		return new StepCountsImpl(this);
	}
	
	private static class StepCountsImpl implements StepCounts {
		
		private final Long[] stepCounts;
		private Long countNoResponses;
		private Long countComments;
		
		public StepCountsImpl(StepCountsBuilder builder) {
			this.stepCounts = Arrays.copyOf(builder.stepCounts, builder.stepCounts.length);
			this.countNoResponses = builder.countNoResponses;
			this.countComments = builder.countComments;
		}
		
		@Override
		public long getStepCount(int step) {
			if (step > stepCounts.length) {
				return 0;
			}
			Long stepCount = stepCounts[step];
			return stepCount != null? stepCount.longValue(): 0;
		}
		
		@Override
		public int getNumberOfSteps() {
			return stepCounts.length -1;
		}

		@Override
		public Long getNumberOfNoResponses() {
			return countNoResponses;
		}
		
		@Override
		public Long getNumberOfComments() {
			return countComments;
		}

		@Override
		public int getMin() {
			for(int i=1; i<stepCounts.length; i++) {
				if(stepCounts[i] != null && stepCounts[i].longValue() > 0) {
					return i;
				}
			}
			return 0;
		}

		@Override
		public int getMax() {
			for(int i=stepCounts.length; i-->1; ) {
				if(stepCounts[i] != null && stepCounts[i].longValue() > 0) {
					return i;
				}
			}
			return 0;
		}
	}

}

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
package org.olat.modules.forms.model.jpa;

import java.util.List;

import org.olat.modules.forms.RubricRating;
import org.olat.modules.forms.SliderStatistic;

/**
 * 
 * Initial date: 22.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SliderStatisticImpl implements SliderStatistic {

	private final Long numberOfNoResponses;
	private final Long numberOfResponses;
	private final Double sum;
	private final Double median;
	private final Double avg;
	private final Double variance;
	private final Double stdDev;
	private final List<Long> stepCounts;
	private final RubricRating rating;
	
	public SliderStatisticImpl(Long numberOfNoResponses, Long numberOfResponses, Double sum, Double median, Double avg,
			Double variance, Double stdDev, List<Long> stepCounts, RubricRating rating) {
		super();
		this.numberOfNoResponses = numberOfNoResponses;
		this.numberOfResponses = numberOfResponses;
		this.sum = sum;
		this.median = median;
		this.avg = avg;
		this.variance = variance;
		this.stdDev = stdDev;
		this.stepCounts = stepCounts;
		this.rating = rating;
	}

	@Override
	public Long getNumberOfNoResponses() {
		return numberOfNoResponses;
	}

	@Override
	public Long getNumberOfResponses() {
		return numberOfResponses;
	}

	@Override
	public Double getSum() {
		return sum;
	}

	@Override
	public Double getMedian() {
		return median;
	}

	@Override
	public Double getAvg() {
		return avg;
	}

	@Override
	public Double getVariance() {
		return variance;
	}

	@Override
	public Double getStdDev() {
		return stdDev;
	}

	@Override
	public List<Long> getStepCounts() {
		return stepCounts;
	}

	@Override
	public RubricRating getRating() {
		return rating;
	}
	
}

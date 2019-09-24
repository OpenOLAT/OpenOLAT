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
import org.olat.modules.quality.analysis.HeatMapStatistic;

/**
 * 
 * Initial date: 8 Jul 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class HeatMapStatisticImpl implements HeatMapStatistic {
	
	private final Long count;
	private final Double avg;
	private final RubricRating rating;
	
	public HeatMapStatisticImpl(Long count, Double avg, RubricRating rating) {
		this.count = count;
		this.avg = avg;
		this.rating = rating;
	}

	@Override
	public Long getCount() {
		return count;
	}

	@Override
	public Double getAvg() {
		return avg;
	}

	@Override
	public RubricRating getRating() {
		return rating;
	}
	

}

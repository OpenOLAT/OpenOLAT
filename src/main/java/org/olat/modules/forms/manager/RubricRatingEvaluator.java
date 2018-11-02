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
package org.olat.modules.forms.manager;

import org.olat.modules.forms.RubricRating;
import org.olat.modules.forms.model.xml.Rubric;

/**
 * 
 * Initial date: 31.10.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class RubricRatingEvaluator {
	
	private final Rubric rubric;
	private final Double value;
	
	public static RubricRating rate(Rubric rubric, Double value) {
		return new RubricRatingEvaluator(rubric, value).getRubricRating();
	}

	private RubricRatingEvaluator(Rubric rubric, Double value) {
		this.rubric = rubric;
		this.value = value;
	}

	private RubricRating getRubricRating() {
		if (value == null) return RubricRating.NOT_RATED;
		
		Range sufficientRange = new Range(rubric.getLowerBoundSufficient(), rubric.getUpperBoundSufficient());
		Range neutralRange = new Range(rubric.getLowerBoundNeutral(), rubric.getUpperBoundNeutral());
		Range insufficientRange = new Range(rubric.getLowerBoundInsufficient(), rubric.getUpperBoundInsufficient());
		
		RubricRating rating = RubricRating.NOT_RATED;
		if (sufficientRange.getLower() <= value && value <= sufficientRange.getUpper()) {
			rating = RubricRating.SUFFICIENT;
		} else if (neutralRange.getLower() <= value && value <= neutralRange.getUpper()) {
			rating = RubricRating.NEUTRAL;
		} else if (insufficientRange.getLower() <= value && value <= insufficientRange.getUpper()) {
			rating = RubricRating.INSUFFICIENT;
		}
		return rating;
	}
	
	private static final class Range {
		private final Double lower;
		private final Double upper;
		
		Range(Double value1, Double value2) {
			if (value1 != null && value2 != null) {
				if (value1 < value2) {
					lower = value1;
					upper = value2;
				} else {
					lower = value2;
					upper = value1;
				}
			} else {
				lower = -99999d;
				upper= -99999d;
			}
		}

		public Double getLower() {
			return lower;
		}

		public Double getUpper() {
			return upper;
		}
		
	}
}

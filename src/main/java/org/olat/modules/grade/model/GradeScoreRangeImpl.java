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
package org.olat.modules.grade.model;

import java.math.BigDecimal;

import org.olat.modules.grade.GradeScoreRange;

/**
 * 
 * Initial date: 24 Feb 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class GradeScoreRangeImpl implements GradeScoreRange {
	
	private final int bestToLowest;
	private final String grade;
	private final String performanceClassIdent;
	private final BigDecimal score;
	private final BigDecimal upperBound;
	private final boolean upperBoundInclusive;
	private final BigDecimal lowerBound;
	private final boolean lowerBoundInclusive;
	private final boolean passed;
	
	public GradeScoreRangeImpl(int bestToLowest, String grade, String performanceClassIdent, BigDecimal score,
			BigDecimal upperBound, boolean upperBoundInclusive, BigDecimal lowerBound, boolean lowerBoundInclusive,
			boolean passed) {
		this.bestToLowest = bestToLowest;
		this.grade = grade;
		this.performanceClassIdent = performanceClassIdent;
		this.score = score;
		this.lowerBound = lowerBound;
		this.lowerBoundInclusive = lowerBoundInclusive;
		this.upperBound = upperBound;
		this.upperBoundInclusive = upperBoundInclusive;
		this.passed = passed;
	}

	@Override
	public int getBestToLowest() {
		return bestToLowest;
	}

	@Override
	public String getGrade() {
		return grade;
	}

	@Override
	public String getPerformanceClassIdent() {
		return performanceClassIdent;
	}

	@Override
	public BigDecimal getScore() {
		return score;
	}

	@Override
	public BigDecimal getUpperBound() {
		return upperBound;
	}

	@Override
	public boolean isUpperBoundInclusive() {
		return upperBoundInclusive;
	}

	@Override
	public BigDecimal getLowerBound() {
		return lowerBound;
	}

	@Override
	public boolean isLowerBoundInclusive() {
		return lowerBoundInclusive;
	}

	@Override
	public boolean isPassed() {
		return passed;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + bestToLowest;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GradeScoreRangeImpl other = (GradeScoreRangeImpl) obj;
		if (bestToLowest != other.bestToLowest)
			return false;
		return true;
	}

	@Override
	public int compareTo(GradeScoreRange o) {
		return bestToLowest - o.getBestToLowest();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GradeScoreRangeImpl [bestToLowest=");
		builder.append(bestToLowest);
		builder.append(", grade=");
		builder.append(grade);
		builder.append(", performanceClassIdent=");
		builder.append(performanceClassIdent);
		builder.append(", upperBound=");
		builder.append(upperBound);
		builder.append(", upperBoundInclusive=");
		builder.append(upperBoundInclusive);
		builder.append(", lowerBound=");
		builder.append(lowerBound);
		builder.append(", lowerBoundInclusive=");
		builder.append(lowerBoundInclusive);
		builder.append(", passed=");
		builder.append(passed);
		builder.append("]");
		return builder.toString();
	}

}

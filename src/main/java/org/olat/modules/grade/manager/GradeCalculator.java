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
package org.olat.modules.grade.manager;

import static org.olat.modules.grade.ui.GradeUIFactory.THREE_DIGITS;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.olat.core.gui.translator.Translator;
import org.olat.modules.grade.Breakpoint;
import org.olat.modules.grade.GradeScoreRange;
import org.olat.modules.grade.NumericResolution;
import org.olat.modules.grade.PerformanceClass;
import org.olat.modules.grade.Rounding;
import org.olat.modules.grade.model.GradeScoreRangeImpl;
import org.olat.modules.grade.ui.GradeUIFactory;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 24 Feb 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class GradeCalculator {

	public NavigableSet<GradeScoreRange> createNumericalRanges(int lowestGrade, int bestGrade, NumericResolution resolution,
			Rounding rounding, BigDecimal cutValue, BigDecimal minScore, BigDecimal maxScore) {
		int numRanges = Math.abs(bestGrade - lowestGrade) * getResolutionRanges(resolution) + 1;
		if (numRanges < 2) return Collections.emptyNavigableSet();
		
		boolean lowestHigherBest = lowestGrade > bestGrade;
		BigDecimal widthDivisor = new BigDecimal(numRanges - 1);
		BigDecimal gradeRangeWidth = new BigDecimal(bestGrade - lowestGrade).divide(widthDivisor, 5, RoundingMode.HALF_DOWN);
		
		BigDecimal scoreAbsRange = maxScore.subtract(minScore).abs();
		BigDecimal scorRangeWidth = scoreAbsRange.divide(widthDivisor, 5, RoundingMode.HALF_DOWN);
		
		TreeSet<GradeScoreRange> ranges = new TreeSet<>();
		BigDecimal grade = new BigDecimal(lowestGrade);
		BigDecimal lowerBound = minScore;
		BigDecimal upperBound = null;
		for (int i = 1; i <= numRanges; i++) {
			boolean upperBoundInclusive = Rounding.up == rounding;
			boolean lowerBoundInclusive = !upperBoundInclusive;
			if (i == 1) {
				lowerBoundInclusive = true;
				if (Rounding.nearest == rounding) {
					upperBound = lowerBound.add(scorRangeWidth.divide(new BigDecimal(2), 5, RoundingMode.HALF_DOWN));
				} else if (Rounding.down == rounding) {
					upperBound = lowerBound.add(scorRangeWidth);
				} else { // Rounding.up
					upperBound = lowerBound;
				}
			} else if (i == numRanges) {
				upperBoundInclusive = true;
				upperBound = maxScore;
			} else {
				upperBound = lowerBound.add(scorRangeWidth);
			}
			upperBound = upperBound.stripTrailingZeros();
			
			boolean passed = false;
			if (cutValue != null) {
				if (grade.compareTo(cutValue) == 0 
						|| (lowestHigherBest && grade.compareTo(cutValue) < 0) 
						|| (!lowestHigherBest && grade.compareTo(cutValue) > 0)) {
					passed = true;
				}
			}
			GradeScoreRange gradeScoreRange = new GradeScoreRangeImpl(numRanges - i, THREE_DIGITS.format(grade), null,
					upperBound, upperBoundInclusive, lowerBound, lowerBoundInclusive, passed);
			ranges.add(gradeScoreRange);
			
			lowerBound = upperBound;
			grade = grade.add(gradeRangeWidth);
		}
		return ranges;
	}
	
	private int getResolutionRanges(NumericResolution resolution) {
		switch (resolution) {
			case whole: return 1;
			case half: return 2;
			case quarter: return 4;
			case tenth: return 10;
			default: return 1;
		}
	}

	public NavigableSet<GradeScoreRange> getTextGradeScoreRanges(List<PerformanceClass> performanceClasses,
			List<Breakpoint> breakpoints, BigDecimal minScore, BigDecimal maxScore, Translator translator) {
		Collections.sort(performanceClasses);
		Map<Integer, BigDecimal> positionToLowerBound = breakpoints.stream()
				.collect(Collectors.toMap(Breakpoint::getBestToLowest, Breakpoint::getValue));
		
		TreeSet<GradeScoreRange> ranges  = new TreeSet<>();
		
		BigDecimal upperBound = maxScore;
		BigDecimal lowerBound;
		boolean upperBoundInclusive;
		boolean lowerBoundInclusive;
		for (int i = 0; i < performanceClasses.size(); i++) {
			PerformanceClass performanceClass = performanceClasses.get(i);
			
			String grade = GradeUIFactory.translatePerformanceClass(translator, performanceClass);
			lowerBound = positionToLowerBound.getOrDefault(Integer.valueOf(performanceClass.getBestToLowest()), minScore);
			if (i == 0) {
				upperBoundInclusive = true;
				lowerBoundInclusive = true;
			} else {
				upperBoundInclusive = false;
				lowerBoundInclusive = true;
			}
			
			GradeScoreRange range = new GradeScoreRangeImpl(performanceClass.getBestToLowest(), grade,
					performanceClass.getIdentifier(), upperBound, upperBoundInclusive, lowerBound,
					lowerBoundInclusive, performanceClass.isPassed());
			ranges.add(range);
			
			upperBound = lowerBound;
		}
		
		return ranges;
	}

	public GradeScoreRange getGrade(NavigableSet<GradeScoreRange> gradeScoreRanges, Float score) {
		BigDecimal scoreValue = new BigDecimal(score.floatValue());
		Iterator<GradeScoreRange> rangeIterator = gradeScoreRanges.iterator();
		while(rangeIterator.hasNext()) {
			GradeScoreRange range = rangeIterator.next();
			if (range.isLowerBoundInclusive() && scoreValue.compareTo(range.getLowerBound()) >= 0) {
				return range;
			} else if (scoreValue.compareTo(range.getLowerBound()) > 0) {
				return range;
			}
		}
		return gradeScoreRanges.last();
	}

}

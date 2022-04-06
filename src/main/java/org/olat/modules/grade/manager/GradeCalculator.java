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
	
	private static final BigDecimal TWO = new BigDecimal("2");
	
	public NavigableSet<GradeScoreRange> createNumericalRanges(BigDecimal lowestGrade, BigDecimal bestGrade, NumericResolution resolution,
			Rounding rounding, BigDecimal cutValue, BigDecimal minScore, BigDecimal maxScore, List<Breakpoint> breakpoints) {
		if (breakpoints == null || breakpoints.isEmpty()) {
			return createNumericalRanges(lowestGrade, bestGrade, resolution, rounding, cutValue, minScore, maxScore);
		}
		
		breakpoints.sort((b1, b2) -> b2.getScore().compareTo(b1.getScore()));
		
		NavigableSet<GradeScoreRange> ranges = null;
		int bestToLowestOffset = 0;
		BigDecimal pBestGrade = bestGrade;
		BigDecimal pMaxScore = maxScore;
		for (int i = 0; i < breakpoints.size(); i++) {
			Breakpoint breakpoint = breakpoints.get(i);
			BigDecimal pLowestGrade =  new BigDecimal(breakpoint.getGrade());
			BigDecimal pMinScore = breakpoint.getScore();
			
			NavigableSet<GradeScoreRange> nextRanges = createNumericalRanges(pLowestGrade, pBestGrade, resolution, rounding, cutValue, pMinScore, pMaxScore, bestToLowestOffset);
			if (ranges == null) {
				ranges = nextRanges;
			} else {
				addNextRanges(ranges, nextRanges);
			}
			
			pMaxScore = pMinScore;
			pBestGrade = pLowestGrade;
			bestToLowestOffset = ranges.size()-1;
		}
		
		// From last breakpoint to the best grade
		NavigableSet<GradeScoreRange> nextRanges = createNumericalRanges(lowestGrade, pBestGrade, resolution, rounding, cutValue, minScore, pMaxScore, bestToLowestOffset);
		addNextRanges(ranges, nextRanges);
		
		return ranges;
	}

	private void addNextRanges(NavigableSet<GradeScoreRange> ranges, NavigableSet<GradeScoreRange> nextRanges) {
		GradeScoreRange upperHalfRange = ranges.last();
		GradeScoreRange lowerHalfRange = nextRanges.first();
		GradeScoreRange mergedRange = new GradeScoreRangeImpl(upperHalfRange.getBestToLowest(),
				upperHalfRange.getGrade(), upperHalfRange.getPerformanceClassIdent(), upperHalfRange.getLowerBound(),
				upperHalfRange.getUpperBound(), upperHalfRange.isUpperBoundInclusive(), lowerHalfRange.getLowerBound(),
				lowerHalfRange.isLowerBoundInclusive(), upperHalfRange.isPassed());
		ranges.addAll(nextRanges);
		// Add is optional, so first remove the half range one.
		ranges.remove(mergedRange);
		ranges.add(mergedRange);
	}
	
	public NavigableSet<GradeScoreRange> createNumericalRanges(BigDecimal lowestGrade, BigDecimal bestGrade,
			NumericResolution resolution, Rounding rounding, BigDecimal cutValue, BigDecimal minScore,
			BigDecimal maxScore) {
		return createNumericalRanges(lowestGrade, bestGrade, resolution, rounding, cutValue, minScore, maxScore, 0);
	}

	private NavigableSet<GradeScoreRange> createNumericalRanges(BigDecimal lowestGrade, BigDecimal bestGrade,
			NumericResolution resolution, Rounding rounding, BigDecimal cutValue, BigDecimal minScore,
			BigDecimal maxScore, int bestToLowestOffset) {
		TreeSet<GradeScoreRange> ranges = new TreeSet<>();
		
		BigDecimal gradeRangeWidth = getGradeRangeWidth(resolution);
		int numRanges = geNumOfRanges(lowestGrade.subtract(bestGrade).abs(), gradeRangeWidth);
		if (numRanges < 2) {
			return ranges;
		}
		
		boolean lowestHigherBest = lowestGrade.compareTo(bestGrade) > 0;
		BigDecimal widthDivisor = new BigDecimal(numRanges - 1);
		gradeRangeWidth = lowestHigherBest? gradeRangeWidth.multiply(new BigDecimal(-1)): gradeRangeWidth;
		BigDecimal scoreAbsRange = maxScore.subtract(minScore).abs();
		BigDecimal scoreRangeWidth = scoreAbsRange.divide(widthDivisor, 5, RoundingMode.HALF_DOWN);
		
		BigDecimal grade = bestGrade;
		BigDecimal upperBound = maxScore;
		BigDecimal lowerBound = null;
		for (int i = 1; i <= numRanges; i++) {
			boolean upperBoundInclusive = Rounding.up == rounding;
			boolean lowerBoundInclusive = !upperBoundInclusive;
			if (i == 1) {
				upperBoundInclusive = true;
				if (Rounding.nearest == rounding) {
					lowerBound = upperBound.subtract(scoreRangeWidth.divide(new BigDecimal(2), 5, RoundingMode.HALF_DOWN));
				} else if (Rounding.up == rounding) {
					lowerBound = upperBound.subtract(scoreRangeWidth);
				} else { // Rounding.down
					lowerBound = upperBound;
				}
			} else if (i == numRanges) {
				lowerBoundInclusive = true;
				lowerBound = minScore;
			} else {
				lowerBound = upperBound.subtract(scoreRangeWidth);
			}
			lowerBound = lowerBound.stripTrailingZeros();
			
			boolean passed = false;
			if (cutValue != null) {
				if (grade.compareTo(cutValue) == 0 
						|| (lowestHigherBest && grade.compareTo(cutValue) < 0) 
						|| (!lowestHigherBest && grade.compareTo(cutValue) > 0)) {
					passed = true;
				}
			}
			
			BigDecimal score = upperBound.add(lowerBound).divide(TWO);
			
			GradeScoreRange gradeScoreRange = new GradeScoreRangeImpl(i + bestToLowestOffset, THREE_DIGITS.format(grade), null,
					score, upperBound, upperBoundInclusive, lowerBound, lowerBoundInclusive, passed);
			ranges.add(gradeScoreRange);
			
			upperBound = lowerBound;
			grade = grade.subtract(gradeRangeWidth);
		}
		
		return ranges;
	}
	
	private BigDecimal getGradeRangeWidth(NumericResolution resolution) {
		switch (resolution) {
		case whole: return new BigDecimal("1");
		case half: return new BigDecimal("0.5");
		case quarter: return new BigDecimal("0.25");
		case tenth: return new BigDecimal("0.1");
		default: return new BigDecimal("1");
	}
	}

	private int geNumOfRanges(BigDecimal totalRange, BigDecimal rangeWidth) {
		int numRanges = 0;
		BigDecimal bound = new BigDecimal(0);
		while (bound.compareTo(totalRange) <= 0) {
			numRanges++;
			bound = bound.add(rangeWidth);
		}
		
		return numRanges;
	}

	public NavigableSet<GradeScoreRange> getTextGradeScoreRanges(List<PerformanceClass> performanceClasses,
			List<Breakpoint> breakpoints, BigDecimal minScore, BigDecimal maxScore, Translator translator) {
		Collections.sort(performanceClasses);
		Map<Integer, BigDecimal> positionToLowerBound = breakpoints.stream()
				.collect(Collectors.toMap(Breakpoint::getBestToLowest, Breakpoint::getScore));
		
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
			
			BigDecimal score = upperBound.add(lowerBound).divide(TWO);
			
			GradeScoreRange range = new GradeScoreRangeImpl(performanceClass.getBestToLowest(), grade,
					performanceClass.getIdentifier(), score, upperBound, upperBoundInclusive,
					lowerBound, lowerBoundInclusive, performanceClass.isPassed());
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

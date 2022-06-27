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

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.junit.Test;
import org.olat.modules.grade.Breakpoint;
import org.olat.modules.grade.GradeScoreRange;
import org.olat.modules.grade.NumericResolution;
import org.olat.modules.grade.PerformanceClass;
import org.olat.modules.grade.Rounding;
import org.olat.modules.grade.model.BreakpointImpl;
import org.olat.modules.grade.model.BreakpointWrapper;
import org.olat.modules.grade.model.GradeScoreRangeImpl;
import org.olat.modules.grade.model.PerformanceClassImpl;
import org.olat.test.KeyTranslator;

/**
 * 
 * Initial date: 24 Feb 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com/
 *
 */
public class GradeCalculatorTest {
	
	private GradeCalculator sut = new GradeCalculator();
	
	@Test
	public void shouldCreateNumericRanges_simpleExample() {
		List<GradeScoreRange> ranges = createNumericalRanges(1, 6, NumericResolution.whole, Rounding.nearest, null, 1, 6)
				.stream().collect(Collectors.toList());
		
		assertThat(ranges).hasSize(6);
		
		GradeScoreRange range = ranges.get(0);
		assertThat(range.getGrade()).isEqualTo("6");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("6"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("5.5"));
		range = ranges.get(1);
		assertThat(range.getGrade()).isEqualTo("5");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("5.5"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("4.5"));
		range = ranges.get(2);
		assertThat(range.getGrade()).isEqualTo("4");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("4.5"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("3.5"));
		range = ranges.get(3);
		assertThat(range.getGrade()).isEqualTo("3");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("3.5"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("2.5"));
		range = ranges.get(4);
		assertThat(range.getGrade()).isEqualTo("2");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("2.5"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("1.5"));
		range = ranges.get(5);
		assertThat(range.getGrade()).isEqualTo("1");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("1.5"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("1"));
	}
	
	@Test
	public void shouldCreateNumericRanges_lowestHigherThanBest() {
		List<GradeScoreRange> ranges = createNumericalRanges(6, 1, NumericResolution.whole, Rounding.nearest, null, 1, 6)
				.stream().collect(Collectors.toList());

		assertThat(ranges).hasSize(6);
		
		GradeScoreRange range = ranges.get(0);
		assertThat(range.getGrade()).isEqualTo("1");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("6"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("5.5"));
		range = ranges.get(1);
		assertThat(range.getGrade()).isEqualTo("2");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("5.5"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("4.5"));
		range = ranges.get(2);
		assertThat(range.getGrade()).isEqualTo("3");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("4.5"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("3.5"));
		range = ranges.get(3);
		assertThat(range.getGrade()).isEqualTo("4");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("3.5"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("2.5"));
		range = ranges.get(4);
		assertThat(range.getGrade()).isEqualTo("5");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("2.5"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("1.5"));
		range = ranges.get(5);
		assertThat(range.getGrade()).isEqualTo("6");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("1.5"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("1"));
	}
	
	@Test
	public void shouldCreateNumericRanges_resolutionHalf() {
		List<GradeScoreRange> ranges = createNumericalRanges(1, 3, NumericResolution.half, Rounding.nearest, null, 1, 3)
				.stream().collect(Collectors.toList());
		
		assertThat(ranges).hasSize(5);
		
		GradeScoreRange range = ranges.get(0);
		assertThat(range.getGrade()).isEqualTo("3");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("3"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("2.75"));
		range = ranges.get(1);
		assertThat(range.getGrade()).isEqualTo("2.5");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("2.75"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("2.25"));
		range = ranges.get(2);
		assertThat(range.getGrade()).isEqualTo("2");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("2.25"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("1.75"));
		range = ranges.get(3);
		assertThat(range.getGrade()).isEqualTo("1.5");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("1.75"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("1.25"));
		range = ranges.get(4);
		assertThat(range.getGrade()).isEqualTo("1");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("1.25"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("1"));
	}
	
	@Test
	public void shouldCreateNumericRanges_resolutionQuarter() {
		List<GradeScoreRange> ranges = createNumericalRanges(2, 3, NumericResolution.quarter, Rounding.nearest, null, 1, 3)
				.stream().collect(Collectors.toList());
		
		assertThat(ranges).hasSize(5);
		
		GradeScoreRange range = ranges.get(0);
		assertThat(range.getGrade()).isEqualTo("3");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("3"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("2.75"));
		range = ranges.get(1);
		assertThat(range.getGrade()).isEqualTo("2.75");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("2.75"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("2.25"));
		range = ranges.get(2);
		assertThat(range.getGrade()).isEqualTo("2.5");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("2.25"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("1.75"));
		range = ranges.get(3);
		assertThat(range.getGrade()).isEqualTo("2.25");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("1.75"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("1.25"));
		range = ranges.get(4);
		assertThat(range.getGrade()).isEqualTo("2");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("1.25"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("1"));
	}
	
	@Test
	public void shouldCreateNumericRanges_resolutionTeanth() {
		List<GradeScoreRange> ranges = createNumericalRanges(9, 10, NumericResolution.tenth, Rounding.nearest, null, 0, 10)
				.stream().collect(Collectors.toList());
		
		assertThat(ranges).hasSize(11);
		
		GradeScoreRange range = ranges.get(0);
		assertThat(range.getGrade()).isEqualTo("10");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("10"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("9.5"));
		range = ranges.get(1);
		assertThat(range.getGrade()).isEqualTo("9.9");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("9.5"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("8.5"));
		range = ranges.get(2);
		assertThat(range.getGrade()).isEqualTo("9.8");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("8.5"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("7.5"));
		range = ranges.get(3);
		assertThat(range.getGrade()).isEqualTo("9.7");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("7.5"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("6.5"));
		range = ranges.get(4);
		assertThat(range.getGrade()).isEqualTo("9.6");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("6.5"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("5.5"));
		range = ranges.get(5);
		assertThat(range.getGrade()).isEqualTo("9.5");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("5.5"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("4.5"));
		range = ranges.get(6);
		assertThat(range.getGrade()).isEqualTo("9.4");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("4.5"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("3.5"));
		range = ranges.get(7);
		assertThat(range.getGrade()).isEqualTo("9.3");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("3.5"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("2.5"));
		range = ranges.get(8);
		assertThat(range.getGrade()).isEqualTo("9.2");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("2.5"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("1.5"));
		range = ranges.get(9);
		assertThat(range.getGrade()).isEqualTo("9.1");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("1.5"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("0.5"));
		range = ranges.get(10);
		assertThat(range.getGrade()).isEqualTo("9");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("0.5"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("0"));
	}
	
	@Test
	public void shouldCreateNumericRanges_roundingUp() {
		List<GradeScoreRange> ranges = createNumericalRanges(2, 3, NumericResolution.quarter, Rounding.up, null, 1, 3)
				.stream().collect(Collectors.toList());
		
		assertThat(ranges).hasSize(5);
		
		GradeScoreRange range = ranges.get(0);
		assertThat(range.getGrade()).isEqualTo("3");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("3"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("2.5"));
		range = ranges.get(1);
		assertThat(range.getGrade()).isEqualTo("2.75");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("2.5"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("2"));
		range = ranges.get(2);
		assertThat(range.getGrade()).isEqualTo("2.5");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("2"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("1.5"));
		range = ranges.get(3);
		assertThat(range.getGrade()).isEqualTo("2.25");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("1.5"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("1"));
		range = ranges.get(4);
		assertThat(range.getGrade()).isEqualTo("2");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("1"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("1"));
	}
	
	@Test
	public void shouldCreateNumericRanges_roundingDown() {
		List<GradeScoreRange> ranges = createNumericalRanges(2, 3, NumericResolution.quarter, Rounding.down, null, 1, 3)
				.stream().collect(Collectors.toList());
		
		assertThat(ranges).hasSize(5);
		
		GradeScoreRange range = ranges.get(0);
		assertThat(range.getGrade()).isEqualTo("3");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("3"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("3"));
		range = ranges.get(1);
		assertThat(range.getGrade()).isEqualTo("2.75");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("3"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("2.5"));
		range = ranges.get(2);
		assertThat(range.getGrade()).isEqualTo("2.5");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("2.5"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("2"));
		range = ranges.get(3);
		assertThat(range.getGrade()).isEqualTo("2.25");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("2"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("1.5"));
		range = ranges.get(4);
		assertThat(range.getGrade()).isEqualTo("2");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("1.5"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("1"));
	}
	
	@Test
	public void shouldCreateNumericRanges_boundsInclusive() {
		List<GradeScoreRange> ranges = createNumericalRanges(1, 3, NumericResolution.whole, Rounding.nearest, null, 1, 3)
				.stream().collect(Collectors.toList());
		
		GradeScoreRange range = ranges.get(0);
		assertThat(range.getGrade()).isEqualTo("3");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("3"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("2.5"));
		assertThat(range.isUpperBoundInclusive()).isTrue();
		assertThat(range.isLowerBoundInclusive()).isTrue();
		range = ranges.get(1);
		assertThat(range.getGrade()).isEqualTo("2");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("2.5"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("1.5"));
		assertThat(range.isUpperBoundInclusive()).isFalse();
		assertThat(range.isLowerBoundInclusive()).isTrue();
		range = ranges.get(2);
		assertThat(range.getGrade()).isEqualTo("1");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("1.5"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("1"));
		assertThat(range.isUpperBoundInclusive()).isFalse();
		assertThat(range.isLowerBoundInclusive()).isTrue();
		
		ranges = createNumericalRanges(1, 3, NumericResolution.whole, Rounding.up, null, 1, 3)
				.stream().collect(Collectors.toList());
		range = ranges.get(0);
		assertThat(range.getGrade()).isEqualTo("3");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("3"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("2"));
		assertThat(range.isUpperBoundInclusive()).isTrue();
		assertThat(range.isLowerBoundInclusive()).isFalse();
		range = ranges.get(1);
		assertThat(range.getGrade()).isEqualTo("2");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("2"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("1"));
		assertThat(range.isUpperBoundInclusive()).isTrue();
		assertThat(range.isLowerBoundInclusive()).isFalse();
		range = ranges.get(2);
		assertThat(range.getGrade()).isEqualTo("1");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("1"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("1"));
		assertThat(range.isUpperBoundInclusive()).isTrue();
		assertThat(range.isLowerBoundInclusive()).isTrue();
		
		ranges = createNumericalRanges(1, 3, NumericResolution.whole, Rounding.down, null, 1, 3)
				.stream().collect(Collectors.toList());
		range = ranges.get(0);
		assertThat(range.getGrade()).isEqualTo("3");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("3"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("3"));
		assertThat(range.isUpperBoundInclusive()).isTrue();
		assertThat(range.isLowerBoundInclusive()).isTrue();
		range = ranges.get(1);
		assertThat(range.getGrade()).isEqualTo("2");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("3"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("2"));
		assertThat(range.isUpperBoundInclusive()).isFalse();
		assertThat(range.isLowerBoundInclusive()).isTrue();
		range = ranges.get(2);
		assertThat(range.getGrade()).isEqualTo("1");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("2"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("1"));
		assertThat(range.isUpperBoundInclusive()).isFalse();
		assertThat(range.isLowerBoundInclusive()).isTrue();
	}
	
	@Test
	public void shouldCreateNumericRanges_passed() {
		List<GradeScoreRange> ranges = createNumericalRanges(1, 4, NumericResolution.whole, Rounding.nearest, 2.5, 11, 14)
				.stream().collect(Collectors.toList());
		
		GradeScoreRange range = ranges.get(0);
		assertThat(range.getGrade()).isEqualTo("4");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("14"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("13.5"));
		assertThat(range.isUpperBoundInclusive()).isTrue();
		assertThat(range.isLowerBoundInclusive()).isTrue();
		assertThat(range.getPassed()).isTrue();
		range = ranges.get(1);
		assertThat(range.getGrade()).isEqualTo("3");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("13.5"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("12.5"));
		assertThat(range.isUpperBoundInclusive()).isFalse();
		assertThat(range.isLowerBoundInclusive()).isTrue();
		assertThat(range.getPassed()).isTrue();
		range = ranges.get(2);
		assertThat(range.getGrade()).isEqualTo("2");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("12.5"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("11.5"));
		assertThat(range.isUpperBoundInclusive()).isFalse();
		assertThat(range.isLowerBoundInclusive()).isTrue();
		assertThat(range.getPassed()).isFalse();
		range = ranges.get(3);
		assertThat(range.getGrade()).isEqualTo("1");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("11.5"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("11"));
		assertThat(range.isUpperBoundInclusive()).isFalse();
		assertThat(range.isLowerBoundInclusive()).isTrue();
		assertThat(range.getPassed()).isFalse();
	}
	
	@Test
	public void shouldCreateNumericRanges_passedLowestHigherBest() {
		List<GradeScoreRange> ranges = createNumericalRanges(5, 1, NumericResolution.half, Rounding.nearest, 3.0, 11, 13)
				.stream().collect(Collectors.toList());
		
		GradeScoreRange range = ranges.get(0);
		assertThat(range.getGrade()).isEqualTo("1");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("13"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("12.875"));
		assertThat(range.isUpperBoundInclusive()).isTrue();
		assertThat(range.isLowerBoundInclusive()).isTrue();
		assertThat(range.getPassed()).isTrue();
		range = ranges.get(1);
		assertThat(range.getGrade()).isEqualTo("1.5");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("12.875"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("12.625"));
		assertThat(range.isUpperBoundInclusive()).isFalse();
		assertThat(range.isLowerBoundInclusive()).isTrue();
		assertThat(range.getPassed()).isTrue();
		range = ranges.get(2);
		assertThat(range.getGrade()).isEqualTo("2");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("12.625"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("12.375"));
		assertThat(range.isUpperBoundInclusive()).isFalse();
		assertThat(range.isLowerBoundInclusive()).isTrue();
		assertThat(range.getPassed()).isTrue();
		range = ranges.get(3);
		assertThat(range.getGrade()).isEqualTo("2.5");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("12.375"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("12.125"));
		assertThat(range.isUpperBoundInclusive()).isFalse();
		assertThat(range.isLowerBoundInclusive()).isTrue();
		assertThat(range.getPassed()).isTrue();
		range = ranges.get(4);
		assertThat(range.getGrade()).isEqualTo("3");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("12.125"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("11.875"));
		assertThat(range.isUpperBoundInclusive()).isFalse();
		assertThat(range.isLowerBoundInclusive()).isTrue();
		assertThat(range.getPassed()).isTrue();
		range = ranges.get(5);
		assertThat(range.getGrade()).isEqualTo("3.5");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("11.875"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("11.625"));
		assertThat(range.isUpperBoundInclusive()).isFalse();
		assertThat(range.isLowerBoundInclusive()).isTrue();
		assertThat(range.getPassed()).isFalse();
		range = ranges.get(6);
		assertThat(range.getGrade()).isEqualTo("4");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("11.625"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("11.375"));
		assertThat(range.isUpperBoundInclusive()).isFalse();
		assertThat(range.isLowerBoundInclusive()).isTrue();
		assertThat(range.getPassed()).isFalse();
		range = ranges.get(7);
		assertThat(range.getGrade()).isEqualTo("4.5");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("11.375"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("11.125"));
		assertThat(range.isUpperBoundInclusive()).isFalse();
		assertThat(range.isLowerBoundInclusive()).isTrue();
		assertThat(range.getPassed()).isFalse();
		range = ranges.get(8);
		assertThat(range.getGrade()).isEqualTo("5");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("11.125"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("11"));
		assertThat(range.isUpperBoundInclusive()).isFalse();
		assertThat(range.isLowerBoundInclusive()).isTrue();
		assertThat(range.getPassed()).isFalse();
	}
	
	private NavigableSet<GradeScoreRange> createNumericalRanges(int lowestGrade, int bestGrade,
			NumericResolution resolution, Rounding rounding, Double cutValue, int minScore,
			int maxScore) {
		return sut.createNumericalRanges(null, new BigDecimal(lowestGrade), new BigDecimal(bestGrade), resolution,
				rounding, cutValue != null ? BigDecimal.valueOf(cutValue) : null, new BigDecimal(minScore),
				new BigDecimal(maxScore));
	}
	
	@Test
	public void shouldCreateNumericRanges_breakpointsStillLinear() {
		List<Breakpoint> breakpoints = new ArrayList<>(1);
		BreakpointWrapper breakpoint = new BreakpointWrapper();
		breakpoint.setGrade("4");
		breakpoint.setScore(new BigDecimal(4));
		breakpoints.add(breakpoint);
		List<GradeScoreRange> ranges = createNumericalRanges(1, 6, NumericResolution.whole, Rounding.nearest, null, 1, 6, breakpoints)
				.stream().collect(Collectors.toList());
		
		assertThat(ranges).hasSize(6);
		
		GradeScoreRange range = ranges.get(0);
		assertThat(range.getGrade()).isEqualTo("6");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("6"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("5.5"));
		range = ranges.get(1);
		assertThat(range.getGrade()).isEqualTo("5");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("5.5"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("4.5"));
		range = ranges.get(2);
		assertThat(range.getGrade()).isEqualTo("4");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("4.5"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("3.5"));
		range = ranges.get(3);
		assertThat(range.getGrade()).isEqualTo("3");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("3.5"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("2.5"));
		range = ranges.get(4);
		assertThat(range.getGrade()).isEqualTo("2");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("2.5"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("1.5"));
		range = ranges.get(5);
		assertThat(range.getGrade()).isEqualTo("1");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("1.5"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("1"));
	}
	
	@Test
	public void shouldCreateNumericRanges_breakpointsLowestHigherBest() {
		List<Breakpoint> breakpoints = new ArrayList<>(1);
		BreakpointWrapper breakpoint = new BreakpointWrapper();
		breakpoint.setGrade("4");
		breakpoint.setScore(new BigDecimal(20));
		breakpoints.add(breakpoint);
		List<GradeScoreRange> ranges = createNumericalRanges(6, 1, NumericResolution.whole, Rounding.nearest, null, 0, 80, breakpoints)
				.stream().collect(Collectors.toList());
		
		assertThat(ranges).hasSize(6);
		
		GradeScoreRange range = ranges.get(0);
		assertThat(range.getGrade()).isEqualTo("1");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("80"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("70"));
		range = ranges.get(1);
		assertThat(range.getGrade()).isEqualTo("2");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("70"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("50"));
		range = ranges.get(2);
		assertThat(range.getGrade()).isEqualTo("3");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("50"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("30"));
		range = ranges.get(3);
		assertThat(range.getGrade()).isEqualTo("4");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("30"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("15"));
		range = ranges.get(4);
		assertThat(range.getGrade()).isEqualTo("5");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("15"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("5"));
		range = ranges.get(5);
		assertThat(range.getGrade()).isEqualTo("6");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("5"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("0"));
	}
	
	@Test
	public void shouldCreateNumericRanges_breakpointsTwo() {
		List<Breakpoint> breakpoints = new ArrayList<>(1);
		BreakpointWrapper breakpoint = new BreakpointWrapper();
		breakpoint.setGrade("4");
		breakpoint.setScore(new BigDecimal(100));
		breakpoints.add(breakpoint);
		breakpoint = new BreakpointWrapper();
		breakpoint.setGrade("8");
		breakpoint.setScore(new BigDecimal(20));
		breakpoints.add(breakpoint);
		List<GradeScoreRange> ranges = createNumericalRanges(10, 1, NumericResolution.whole, Rounding.nearest, null, 0, 190, breakpoints)
				.stream().collect(Collectors.toList());
		
		assertThat(ranges).hasSize(10);
		
		GradeScoreRange range = ranges.get(0);
		assertThat(range.getGrade()).isEqualTo("1");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("190"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("175"));
		range = ranges.get(1);
		assertThat(range.getGrade()).isEqualTo("2");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("175"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("145"));
		range = ranges.get(2);
		assertThat(range.getGrade()).isEqualTo("3");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("145"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("115"));
		range = ranges.get(3);
		assertThat(range.getGrade()).isEqualTo("4");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("115"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("90"));
		range = ranges.get(4);
		assertThat(range.getGrade()).isEqualTo("5");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("90"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("70"));
		range = ranges.get(5);
		assertThat(range.getGrade()).isEqualTo("6");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("70"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("50"));
		range = ranges.get(6);
		assertThat(range.getGrade()).isEqualTo("7");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("50"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("30"));
		range = ranges.get(7);
		assertThat(range.getGrade()).isEqualTo("8");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("30"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("15"));
		range = ranges.get(8);
		assertThat(range.getGrade()).isEqualTo("9");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("15"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("5"));
		range = ranges.get(9);
		assertThat(range.getGrade()).isEqualTo("10");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("5"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("0"));
	}
	
	@Test
	public void shouldCreateNumericRanges_breakpointEqualsMax() {
		List<Breakpoint> breakpoints = new ArrayList<>(1);
		BreakpointWrapper breakpoint = new BreakpointWrapper();
		breakpoint.setGrade("6");
		breakpoint.setScore(new BigDecimal(80));
		breakpoints.add(breakpoint);
		List<GradeScoreRange> ranges = createNumericalRanges(1, 6, NumericResolution.half, Rounding.nearest, null, 0, 100, breakpoints)
				.stream().collect(Collectors.toList());
		
		assertThat(ranges).hasSize(11);
		
		GradeScoreRange range = ranges.get(0);
		assertThat(range.getGrade()).isEqualTo("6");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("100"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("76"));
		range = ranges.get(1);
		assertThat(range.getGrade()).isEqualTo("5.5");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("76"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("68"));
		range = ranges.get(2);
		assertThat(range.getGrade()).isEqualTo("5");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("68"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("60"));
		range = ranges.get(3);
		assertThat(range.getGrade()).isEqualTo("4.5");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("60"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("52"));
		range = ranges.get(4);
		assertThat(range.getGrade()).isEqualTo("4");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("52"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("44"));
		range = ranges.get(5);
		assertThat(range.getGrade()).isEqualTo("3.5");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("44"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("36"));
		range = ranges.get(6);
		assertThat(range.getGrade()).isEqualTo("3");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("36"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("28"));
		range = ranges.get(7);
		assertThat(range.getGrade()).isEqualTo("2.5");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("28"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("20"));
		range = ranges.get(8);
		assertThat(range.getGrade()).isEqualTo("2");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("20"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("12"));
		range = ranges.get(9);
		assertThat(range.getGrade()).isEqualTo("1.5");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("12"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("4"));
		range = ranges.get(10);
		assertThat(range.getGrade()).isEqualTo("1");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("4"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("0"));
	}
	
	@Test
	public void shouldCreateNumericRanges_breakpoints() {
		List<Breakpoint> breakpoints = new ArrayList<>(1);
		BreakpointWrapper breakpoint = new BreakpointWrapper();
		breakpoint.setGrade("3");
		breakpoint.setScore(new BigDecimal(20));
		breakpoints.add(breakpoint);
		List<GradeScoreRange> ranges = createNumericalRanges(1, 6, NumericResolution.whole, Rounding.nearest, null, 0, 80, breakpoints)
				.stream().collect(Collectors.toList());
		
		assertThat(ranges).hasSize(6);
		
		GradeScoreRange range = ranges.get(0);
		assertThat(range.getGrade()).isEqualTo("6");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("80"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("70"));
		range = ranges.get(1);
		assertThat(range.getGrade()).isEqualTo("5");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("70"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("50"));
		range = ranges.get(2);
		assertThat(range.getGrade()).isEqualTo("4");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("50"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("30"));
		range = ranges.get(3);
		assertThat(range.getGrade()).isEqualTo("3");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("30"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("15"));
		range = ranges.get(4);
		assertThat(range.getGrade()).isEqualTo("2");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("15"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("5"));
		range = ranges.get(5);
		assertThat(range.getGrade()).isEqualTo("1");
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("5"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("0"));
	}
	
	@Test
	public void shouldCreateNumericRanges_noPassed() {
		List<GradeScoreRange> ranges = createNumericalRanges(1, 3, NumericResolution.whole, Rounding.nearest, null, 1, 3)
				.stream().collect(Collectors.toList());
		
		GradeScoreRange range = ranges.get(0);
		assertThat(range.getPassed()).isNull();
		range = ranges.get(1);
		assertThat(range.getPassed()).isNull();
		range = ranges.get(1);
		assertThat(range.getPassed()).isNull();
	}
	
	private NavigableSet<GradeScoreRange> createNumericalRanges(int lowestGrade, int bestGrade,
			NumericResolution resolution, Rounding rounding, Double cutValue, int minScore,
			int maxScore, List<Breakpoint> breakpoints) {
		return sut.createNumericalRanges(null, new BigDecimal(lowestGrade), new BigDecimal(bestGrade), resolution,
				rounding, cutValue != null ? BigDecimal.valueOf(cutValue) : null, new BigDecimal(minScore),
				new BigDecimal(maxScore), breakpoints);
	}
	
	@Test
	public void shouldGetTextGradeScoreRanges() {
		List<PerformanceClass> performanceClasses = new ArrayList<>(3);
		PerformanceClassImpl performanceClass1 = new PerformanceClassImpl();
		performanceClass1.setBestToLowest(1);
		performanceClass1.setIdentifier(random());
		performanceClass1.setPassed(true);
		performanceClasses.add(performanceClass1);
		PerformanceClassImpl performanceClass2 = new PerformanceClassImpl();
		performanceClass2.setBestToLowest(2);
		performanceClass2.setIdentifier(random());
		performanceClass2.setPassed(true);
		performanceClasses.add(performanceClass2);
		PerformanceClassImpl performanceClass3 = new PerformanceClassImpl();
		performanceClass3.setBestToLowest(3);
		performanceClass3.setIdentifier(random());
		performanceClass3.setPassed(false);
		performanceClasses.add(performanceClass3);
		List<Breakpoint> breakpoints = new ArrayList<>(3);
		BreakpointImpl breakpoint1 = new BreakpointImpl();
		breakpoint1.setBestToLowest(Integer.valueOf(1));
		breakpoint1.setScore(new BigDecimal(8));
		breakpoints.add(breakpoint1);
		BreakpointImpl breakpoint2 = new BreakpointImpl();
		breakpoint2.setBestToLowest(Integer.valueOf(2));
		breakpoint2.setScore(new BigDecimal(5));
		breakpoints.add(breakpoint2);
		BreakpointImpl breakpoint3 = new BreakpointImpl();
		breakpoint3.setBestToLowest(Integer.valueOf(3));
		breakpoint3.setScore(new BigDecimal(1));
		breakpoints.add(breakpoint3);
		BigDecimal minScore = new BigDecimal(1);
		BigDecimal maxScore = new BigDecimal(10);
		
		List<GradeScoreRange> ranges = sut.getTextGradeScoreRanges(null, true, performanceClasses, breakpoints,
				minScore, maxScore, new KeyTranslator(Locale.ENGLISH)).stream().collect(Collectors.toList());
		
		GradeScoreRange range = ranges.get(0);
		assertThat(range.getGrade()).isEqualTo("1");
		assertThat(range.getPerformanceClassIdent()).isEqualTo(performanceClass1.getIdentifier());
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("10"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("8"));
		assertThat(range.isUpperBoundInclusive()).isTrue();
		assertThat(range.isLowerBoundInclusive()).isTrue();
		assertThat(range.getPassed()).isTrue();
		range = ranges.get(1);
		assertThat(range.getGrade()).isEqualTo("2");
		assertThat(range.getPerformanceClassIdent()).isEqualTo(performanceClass2.getIdentifier());
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("8"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("5"));
		assertThat(range.isUpperBoundInclusive()).isFalse();
		assertThat(range.isLowerBoundInclusive()).isTrue();
		assertThat(range.getPassed()).isTrue();
		range = ranges.get(2);
		assertThat(range.getGrade()).isEqualTo("3");
		assertThat(range.getPerformanceClassIdent()).isEqualTo(performanceClass3.getIdentifier());
		assertThat(range.getUpperBound()).isEqualByComparingTo(new BigDecimal("5"));
		assertThat(range.getLowerBound()).isEqualByComparingTo(new BigDecimal("1"));
		assertThat(range.isUpperBoundInclusive()).isFalse();
		assertThat(range.isLowerBoundInclusive()).isTrue();
		assertThat(range.getPassed()).isFalse();
	}
	
	@Test
	public void shouldGetTextGradeScoreRanges_noPassed() {
		List<PerformanceClass> performanceClasses = new ArrayList<>(3);
		PerformanceClassImpl performanceClass1 = new PerformanceClassImpl();
		performanceClass1.setBestToLowest(1);
		performanceClass1.setIdentifier(random());
		performanceClass1.setPassed(false);
		performanceClasses.add(performanceClass1);
		PerformanceClassImpl performanceClass2 = new PerformanceClassImpl();
		performanceClass2.setBestToLowest(2);
		performanceClass2.setIdentifier(random());
		performanceClass2.setPassed(true);
		performanceClasses.add(performanceClass2);
		BreakpointImpl breakpoint = new BreakpointImpl();
		breakpoint.setBestToLowest(Integer.valueOf(1));
		breakpoint.setScore(new BigDecimal(8));
		BigDecimal minScore = new BigDecimal(1);
		BigDecimal maxScore = new BigDecimal(10);
		
		List<GradeScoreRange> ranges = sut.getTextGradeScoreRanges(null, false, performanceClasses,
				Collections.singletonList(breakpoint), minScore, maxScore, new KeyTranslator(Locale.ENGLISH)).stream()
				.collect(Collectors.toList());
		
		GradeScoreRange range = ranges.get(0);
		assertThat(range.getPassed()).isNull();
		range = ranges.get(1);
		assertThat(range.getPassed()).isNull();
	}
	
	@Test
	public void shouldGetGrade_lowerInclusive() {
		TreeSet<GradeScoreRange> ranges = new TreeSet<>();
		GradeScoreRangeImpl range1 = new GradeScoreRangeImpl(1, "1", null, "11", null, BigDecimal.valueOf(5), false, BigDecimal.valueOf(4), true, false);
		ranges.add(range1);
		GradeScoreRangeImpl range2 = new GradeScoreRangeImpl(2, "2", null, "22", null, BigDecimal.valueOf(4), false, BigDecimal.valueOf(3), true, false);
		ranges.add(range2);
		GradeScoreRangeImpl range3 = new GradeScoreRangeImpl(3, "3", null, "33", null, BigDecimal.valueOf(3), false, BigDecimal.valueOf(2), true, false);
		ranges.add(range3);
		
		GradeScoreRange grade = sut.getGrade(ranges, Float.valueOf(4.0f));
		
		assertThat(grade).isEqualTo(range1);
	}
	
	@Test
	public void shouldGetGrade_lowerNotInclusive() {
		TreeSet<GradeScoreRange> ranges = new TreeSet<>();
		GradeScoreRangeImpl range1 = new GradeScoreRangeImpl(1, "1", null, "11", null, BigDecimal.valueOf(5), false, BigDecimal.valueOf(4), false, false);
		ranges.add(range1);
		GradeScoreRangeImpl range2 = new GradeScoreRangeImpl(2, "2", null, "22", null, BigDecimal.valueOf(4), false, BigDecimal.valueOf(3), false, false);
		ranges.add(range2);
		GradeScoreRangeImpl range3 = new GradeScoreRangeImpl(3, "3", null, "33", null, BigDecimal.valueOf(3), false, BigDecimal.valueOf(2), false, false);
		ranges.add(range3);
		
		GradeScoreRange grade = sut.getGrade(ranges, Float.valueOf(4.0f));
		
		assertThat(grade).isEqualTo(range2);
	}
	
	@Test
	public void shouldGetGrade_scoreHigherThenUpperstBound() {
		TreeSet<GradeScoreRange> ranges = new TreeSet<>();
		GradeScoreRangeImpl range1 = new GradeScoreRangeImpl(1, "1", null, "11", null, BigDecimal.valueOf(5), false, BigDecimal.valueOf(4), true, false);
		ranges.add(range1);
		GradeScoreRangeImpl range2 = new GradeScoreRangeImpl(2, "2", null, "22", null, BigDecimal.valueOf(4), false, BigDecimal.valueOf(3), true, false);
		ranges.add(range2);
		GradeScoreRangeImpl range3 = new GradeScoreRangeImpl(3, "3", null, "33", null, BigDecimal.valueOf(3), false, BigDecimal.valueOf(2), true, false);
		ranges.add(range3);
		
		GradeScoreRange grade = sut.getGrade(ranges, Float.valueOf(10.0f));
		
		assertThat(grade).isEqualTo(range1);
	}
	
	@Test
	public void shouldGetGrade_scoreLowerThenLowerstBound() {
		TreeSet<GradeScoreRange> ranges = new TreeSet<>();
		GradeScoreRangeImpl range1 = new GradeScoreRangeImpl(1, "1", null, "11", null, BigDecimal.valueOf(5), false, BigDecimal.valueOf(4), true, false);
		ranges.add(range1);
		GradeScoreRangeImpl range2 = new GradeScoreRangeImpl(2, "2", null, "22", null, BigDecimal.valueOf(4), false, BigDecimal.valueOf(3), true, false);
		ranges.add(range2);
		GradeScoreRangeImpl range3 = new GradeScoreRangeImpl(3, "3", null, "33", null, BigDecimal.valueOf(3), false, BigDecimal.valueOf(2), true, false);
		ranges.add(range3);
		
		GradeScoreRange grade = sut.getGrade(ranges, Float.valueOf(1.0f));
		
		assertThat(grade).isEqualTo(range3);
	}

}

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
package org.olat.modules.topicbroker.manager;

import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import org.olat.modules.topicbroker.TBEnrollmentFunction;
import org.olat.modules.topicbroker.TBEnrollmentStrategyCriterion;
import org.olat.modules.topicbroker.TBSelection;

/**
 * 
 * Initial date: Jun 25, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class MaxPrioritiesCriterion implements TBEnrollmentStrategyCriterion {
	
	private final int maxSelections;
	private Function<Integer, Double> criterionFunction;
	private Integer criterionBreakPoint;
	private Function<Integer, Double> criterionFunctionAfter = null;
	private final HashMap<Integer, Double> sortOrderToValue;

	public MaxPrioritiesCriterion(int maxSelections, TBEnrollmentFunction function, Integer breakPoint, TBEnrollmentFunction functionAfter) {
		this.maxSelections = maxSelections;
		criterionBreakPoint = null;
		if (breakPoint != null && breakPoint < maxSelections) {
			criterionBreakPoint = breakPoint;
		}
		
		double lowerSortOrderValue = maxSelections;
		
		int upperSortOrder = criterionBreakPoint != null? criterionBreakPoint.intValue(): maxSelections;
		criterionFunction = createCriterionFunction(function, 1, maxSelections, lowerSortOrderValue);
		if (criterionBreakPoint != null) {
			lowerSortOrderValue = criterionFunction.apply(upperSortOrder);
			criterionFunctionAfter = createCriterionFunction(functionAfter, criterionBreakPoint.intValue(), maxSelections, lowerSortOrderValue);
		}
		
		// Calculate the possible values in advance. Maybe we save a few milliseconds.
		sortOrderToValue = new HashMap<>(maxSelections);
		for (int i = 1; i <= maxSelections; i++) {
			Double value = getValue(i);
			sortOrderToValue.put(i, value);
		}
	}
	
	public int getMaxSelections() {
		return maxSelections;
	}

	public Function<Integer, Double> getCriterionFunction() {
		return criterionFunction;
	}

	public Integer getCriterionBreakPoint() {
		return criterionBreakPoint;
	}

	public Function<Integer, Double> getCriterionFunctionAfter() {
		return criterionFunctionAfter;
	}

	public Double getValue(Integer sortOrder) {
		return criterionBreakPoint != null && sortOrder > criterionBreakPoint.intValue()
				? criterionFunctionAfter.apply(Integer.valueOf(sortOrder))
				: criterionFunction.apply(Integer.valueOf(sortOrder));
	}

	@Override
	public String getType() {
		return "strategy.criterion.max.priorities";
	}

	@Override
	public double getValue(List<TBSelection> selections) {
		double sumHighestPriorities = 0;
		double sumPriorities = 0;
		
		for (TBSelection selection : selections) {
			if (selection.isEnrolled()) {
				sumHighestPriorities += maxSelections;
				sumPriorities += sortOrderToValue.get(selection.getSortOrder());
			}
		}
		
		return sumHighestPriorities > 0? sumPriorities / sumHighestPriorities : 0;
	}
	
	private Function<Integer, Double> createCriterionFunction(TBEnrollmentFunction function, int lowerSortOrder, int upperSortOrder, double lowerSortOrderValue) {
		return switch (function) {
		case constant -> new ConstantFunction(lowerSortOrderValue);
		case linear -> new LinearFunction(lowerSortOrder, upperSortOrder, lowerSortOrderValue);
		case logarithmic -> new LogarythmicFunction(lowerSortOrder, lowerSortOrderValue, maxSelections, 0);
		default -> throw new IllegalArgumentException("Unexpected value: " + function);
		};
	}
	
	public final static class ConstantFunction implements Function<Integer, Double> {
		
		private final Double constant;
		
		public ConstantFunction(double lowerSortOrderValue) {
			constant = lowerSortOrderValue;
		}
		
		public Double getConstant() {
			return constant;
		}

		@Override
		public Double apply(Integer t) {
			return constant;
		}
		
	}
	
	public final static class LinearFunction implements Function<Integer, Double> {
		
		private final double lowerSortOrder;
		private final double m;
		private final double b;
		
		public LinearFunction(int lowerSortOrder, int upperSortOrder, double lowerSortOrderValue) {
			this.lowerSortOrder = lowerSortOrder;
			m = (-lowerSortOrderValue / (upperSortOrder - lowerSortOrder));
			b = lowerSortOrderValue;
		}
		
		public double getLowerSortOrder() {
			return lowerSortOrder;
		}

		public double getM() {
			return m;
		}

		public double getB() {
			return b;
		}

		@Override
		public Double apply(Integer sortOrder) {
			double t = sortOrder.intValue() - lowerSortOrder;
			
			return m * t + b;
		}
		
	}
	
	public final static class LogarythmicFunction implements Function<Integer, Double> {

		private final double a;
		private final double b;

		//Should we use log10 or ln (Math.log())?
		public LogarythmicFunction(double x1, double y1, double x2, double y2) {
			double lnX1 = Math.log10(x1);
			double lnX2 = Math.log10(x2);
			
			a = (y2 - y1) / (lnX2 - lnX1);
			b = y1 - a * lnX1;
		}

		public double getA() {
			return a;
		}

		public double getB() {
			return b;
		}

		@Override
		public Double apply(Integer sortOrder) {
			return a * Math.log10(sortOrder) + b;
		}
		
	}

}

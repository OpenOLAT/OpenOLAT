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
package org.olat.modules.grade.ui.component;

import static org.olat.modules.grade.ui.GradeUIFactory.THREE_DIGITS;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.stream.Collectors;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.grade.Breakpoint;
import org.olat.modules.grade.GradeScoreRange;
import org.olat.modules.grade.GradeSystem;
import org.olat.modules.grade.GradeSystemType;

/**
 * 
 * Initial date: 24 Feb 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class GradeScaleChartRenderer extends DefaultComponentRenderer {

	private static final BigDecimal TWO = new BigDecimal("2");

	@Override
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		GradeScaleChart gradeScaleChart = (GradeScaleChart)source;
		String cmpId = gradeScaleChart.getDispatchID();
		
		GradeSystem gradeSystem = gradeScaleChart.getGradeSystem();
		List<Breakpoint> breakpoints = gradeScaleChart.getBreakpoints();
		NavigableSet<GradeScoreRange> ranges = gradeScaleChart.getGradeScoreRanges();
		if (ranges == null) return;
		
		boolean numeric = GradeSystemType.numeric == gradeSystem.getType();
		String data = numeric ? getNumericData(ranges, breakpoints): getTextData(ranges);
		int xMin = numeric ? gradeSystem.getBestGrade().intValue(): ranges.first().getBestToLowest();
		int xMax = numeric ? gradeSystem.getLowestGrade().intValue(): ranges.last().getBestToLowest();
		String yMax = THREE_DIGITS.format(ranges.first().getUpperBound());
		String yMin = THREE_DIGITS.format(ranges.last().getLowerBound());
		PassedInfo passedInfo = numeric ? getNumericPassedInfo(gradeSystem, ranges): getTextPassedInfo(ranges);
		
		sb.append("<div id='d").append(cmpId).append("d3holder' class='d3chart' style='width:360px;height:360px'></div>\n")
		  .append("<script>\n")
		  .append("/* <![CDATA[ */ ")
		  .append("jQuery(function () { \n")
		  .append("var placeholderheight = jQuery('#d").append(cmpId).append("d3holder').height();\n")
		  .append("var placeholderwidth = jQuery('#d").append(cmpId).append("d3holder').width();\n");
		sb.append("var data = [").append(data).append("];\n");
		sb.append("var margin = {top: 5, right: 5, bottom: 40, left: 40},\n")
		  .append("    width = placeholderwidth - margin.left - margin.right,\n")
		  .append("    height = placeholderheight - margin.top - margin.bottom;\n")
		  .append("var xScale = d3.scaleLinear()\n")
		  .append("    .domain([").append(xMin).append(", ").append(xMax).append("])\n")
		  .append("    .range([0, width]);\n")
		  .append("var yScale = d3.scaleLinear()\n")
		  .append("    .domain([").append(yMin).append(", ").append(yMax).append("])\n")
		  .append("    .range([height, 0]);\n");
		
		sb.append("var xAxisTicks = xScale.ticks().filter(tick => Number.isInteger(tick));");
		sb.append("var xAxis = d3.axisBottom(xScale).tickValues(xAxisTicks).tickFormat(d3.format('d'));");
		sb.append("var yAxis = d3.axisLeft(yScale);");

		sb.append("var svg = d3.select('#d").append(cmpId).append("d3holder').append('svg')\n")
		  .append("    .attr('width', width + margin.left + margin.right)\n")
		  .append("    .attr('height', height + margin.top + margin.bottom)\n")
		  .append("    .append('g')\n")
		  .append("    .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');\n");
		
		if(passedInfo.isPassed()) {
			sb.append("svg.append('rect')\n");
			sb.append("    .attr('class', 'o_gr_scale_chart_passed')\n");
			sb.append("    .attr('width', xScale(").append(THREE_DIGITS.format(passedInfo.getGrade())).append("))\n");
			sb.append("    .attr('height', yScale(").append(THREE_DIGITS.format(passedInfo.getScoreCutValue())).append("))\n");
			sb.append(";\n");
		}
		
		sb.append("svg.append('g')\n")
		  .append("   .attr('class', 'x axis')\n")
		  .append("   .attr('transform', 'translate(0,' + height + ')')\n")
		  .append("   .call(xAxis);\n");
		
		sb.append("svg.append('g')\n")
		  .append("    .attr('class', 'y axis')\n")
		  .append("    .call(yAxis)\n");
		sb.append(";\n");
		
		sb.append("var line = d3.line()\n");
		sb.append("    .x(function(d) { return xScale(d[0]); }) \n");
		sb.append("    .y(function(d) { return yScale(d[1]); }) \n");
		sb.append("svg.append('path')\n");
		sb.append("    .datum(data) \n");
		sb.append("    .attr('class', 'line') \n");
		sb.append("    .attr('d', line)\n");
		sb.append("    .attr('class', 'o_gr_scale_chart_line')\n");
		sb.append(";\n");
		
		sb.append("});\n")
		  .append("/* ]]> */")
		  .append("</script>\n");
	}
	
	private String getNumericData(NavigableSet<GradeScoreRange> ranges, List<Breakpoint> breakpoints) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("[").append(ranges.first().getGrade()).append(",").append(THREE_DIGITS.format(ranges.first().getUpperBound())).append("],");
		
		breakpoints.sort((b1, b2) -> b2.getScore().compareTo(b1.getScore()));
		for (Breakpoint breakpoint: breakpoints) {
			sb.append("[").append(breakpoint.getGrade()).append(",").append(THREE_DIGITS.format(breakpoint.getScore())).append("],");
		}
		
		sb.append("[").append(ranges.last().getGrade()).append(",").append(THREE_DIGITS.format(ranges.last().getLowerBound())).append("]");
		
		return sb.toString();
	}
	
	private String getTextData(NavigableSet<GradeScoreRange> ranges) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("[").append(ranges.first().getBestToLowest()).append(",").append(THREE_DIGITS.format(ranges.first().getUpperBound())).append("],");
		Iterator<GradeScoreRange> rangeIterator = ranges.iterator();
		while(rangeIterator.hasNext()) {
			GradeScoreRange range = rangeIterator.next();
			if (rangeIterator.hasNext()) {
				sb.append("[").append(range.getBestToLowest() + 0.5).append(",").append(THREE_DIGITS.format(range.getLowerBound())).append("],");
			} else {
				sb.append("[").append(range.getBestToLowest()).append(",").append(THREE_DIGITS.format(range.getLowerBound())).append("]");
			}
		}
		return sb.toString();
	}

	private PassedInfo getNumericPassedInfo(GradeSystem gradeSystem, NavigableSet<GradeScoreRange> ranges) {
		if (gradeSystem.getCutValue() != null) {
			List<GradeScoreRange> reversedRanges = ranges.stream()
					.sorted(Comparator.reverseOrder())
					.collect(Collectors.toList());
			GradeScoreRange previousRange = null;
			for (int i = 0; i < reversedRanges.size(); i++) {
				GradeScoreRange range = reversedRanges.get(i);
				if (range.isPassed()) {
					BigDecimal boundGrade = previousRange == null
							? new BigDecimal(range.getGrade())
							: new BigDecimal(range.getGrade()).add(new BigDecimal(previousRange.getGrade())).divide(TWO);
					return new PassedInfo(true, boundGrade, range.getLowerBound());
				}
				previousRange = range;
			}
		}
		return new PassedInfo(false, null, null);
	}

	private PassedInfo getTextPassedInfo(NavigableSet<GradeScoreRange> ranges) {
		List<GradeScoreRange> reversedRanges = ranges.stream()
				.sorted(Comparator.reverseOrder())
				.collect(Collectors.toList());
		GradeScoreRange previousRange = null;
		for (int i = 0; i < reversedRanges.size(); i++) {
			GradeScoreRange range = reversedRanges.get(i);
			if (range.isPassed()) {
				BigDecimal boundGrade = previousRange == null
						? new BigDecimal(range.getBestToLowest())
						: new BigDecimal(range.getBestToLowest() + 0.5);
				return new PassedInfo(true, boundGrade, range.getLowerBound());
			}
			previousRange = range;
		}
		return new PassedInfo(false, null, null);
	}
	
	private static final class PassedInfo {
		
		private final boolean passed;
		private final BigDecimal grade;
		private final BigDecimal scoreCutValue;
		
		public PassedInfo(boolean passed, BigDecimal grade, BigDecimal scoreCutValue) {
			this.passed = passed;
			this.grade = grade;
			this.scoreCutValue = scoreCutValue;
		}

		public boolean isPassed() {
			return passed;
		}
		
		public BigDecimal getGrade() {
			return grade;
		}

		public BigDecimal getScoreCutValue() {
			return scoreCutValue;
		}
		
	}
	
}

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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.function.Predicate;
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
import org.olat.modules.grade.ui.GradeUIFactory;

/**
 * 
 * Initial date: 24 Feb 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class GradeScaleChartRenderer extends DefaultComponentRenderer {

	private static final BigDecimal TWELFE = new BigDecimal("12");
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
		PassedInfo passedInfo = numeric ? getNumericPassedInfo(gradeSystem, ranges): getTextPassedInfo(ranges);
		List<Line> lines = numeric ? getNumericData(ranges, breakpoints, passedInfo): getTextData(ranges);
		List<String> yLines = numeric? getNumericYLines(ranges): getTextYLines(ranges);
		String xMax = THREE_DIGITS.format(ranges.first().getUpperBound());
		String xMin = THREE_DIGITS.format(ranges.last().getLowerBound());
		String yMin = numeric ? gradeSystem.getLowestGrade().toString(): String.valueOf(ranges.last().getBestToLowest() + 0.5);
		String yMax = numeric ? gradeSystem.getBestGrade().toString(): String.valueOf(ranges.first().getBestToLowest() - 0.5);
		
		sb.append("<div id='d").append(cmpId).append("d3holder' class='d3chart' style='width:400px;height:200px'></div>\n")
		  .append("<script>\n")
		  .append("/* <![CDATA[ */ ")
		  .append("jQuery(function () { \n")
		  .append("var placeholderheight = jQuery('#d").append(cmpId).append("d3holder').height();\n")
		  .append("var placeholderwidth = jQuery('#d").append(cmpId).append("d3holder').width();\n");
		sb.append("var margin = {top: 5, right: 10, bottom: 40, left: 40},\n")
		  .append("    width = placeholderwidth - margin.left - margin.right,\n")
		  .append("    height = placeholderheight - margin.top - margin.bottom;\n");
		sb.append("var xScale = d3.scaleLinear()\n")
		  .append("    .domain([").append(xMin).append(", ").append(xMax).append("])\n")
		  .append("    .range([0, width]);\n");
		sb.append("var yScale = d3.scaleLinear()\n")
		  .append("    .domain([").append(yMin).append(", ").append(yMax).append("])\n")
		  .append("    .range([height, 0]);\n");

		sb.append("var xAxis = d3.axisBottom(xScale);");
		
		sb.append("var yAxisTicks = yScale.ticks().filter(tick => Number.isInteger(tick));");
		sb.append("var yAxis = d3.axisLeft(yScale).tickValues(yAxisTicks).tickFormat(d3.format('d'));");

		sb.append("var svg = d3.select('#d").append(cmpId).append("d3holder').append('svg')\n")
		  .append("    .attr('width', width + margin.left + margin.right)\n")
		  .append("    .attr('height', height + margin.top + margin.bottom)\n")
		  .append("    .append('g')\n")
		  .append("    .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');\n");
		
		sb.append("svg.append('g')\n")
		  .append("   .attr('class', 'x axis')\n")
		  .append("   .attr('transform', 'translate(0,' + height + ')')\n")
		  .append("   .call(xAxis)");
		sb.append("  .append('text')")
		  .append("    .attr('y', (margin.bottom / 2))")
		  .append("    .attr('x', (width / 2))")
		  .append("    .attr('dy', '1em')")
		  .append("    .attr('fill', '#000')")
		  .append("    .style('text-anchor', 'middle')")
		  .append("    .text('").append(translator.translate("table.header.score")).append("')");
		sb.append(";\n");
		
		sb.append("svg.append('g')\n")
		  .append("    .attr('class', 'y axis')\n")
		  .append("    .call(yAxis)\n");
		sb.append("  .append('text')")
		  .append("    .attr('transform', 'rotate(-90)')")
		  .append("    .attr('y', 0 - margin.left)")
		  .append("    .attr('x', 0 - (height / 2))")
		  .append("    .attr('dy', '1em')")
		  .append("    .attr('fill', '#000')")
		  .append("    .style('text-anchor', 'middle')")
		  .append("    .text('").append(GradeUIFactory.translateGradeSystemLabel(translator, gradeSystem)).append("')");
		sb.append(";\n");

		sb.append("var line = d3.line()\n");
		sb.append("    .x(function(d) { return xScale(d[0]); }) \n");
		sb.append("    .y(function(d) { return yScale(d[1]); }) \n");
		sb.append(";\n");
		
		if (!yLines.isEmpty()) {
			StringBuilder lineSb = new StringBuilder();
			for (String yLine: yLines) {
				lineSb.append("[[").append(xMin).append(",").append(yLine).append("],");
				lineSb.append("[").append(xMax).append(",").append(yLine).append("]],");
			}
			String yLinesArray = lineSb.toString();
			yLinesArray = yLinesArray.substring(0, yLinesArray.length() - 1);
			sb.append("var yLines = [").append(yLinesArray).append("];");
			sb.append("yLines.forEach(function (item) {");
			sb.append("svg.append('path')\n");
			sb.append("  .datum(item)\n");
			sb.append("  .attr('class', 'line')\n");
			sb.append("  .attr('d', line)\n");
			sb.append("  .attr('d', line)\n");
			sb.append("  .attr('class', 'o_gr_scale_chart_line o_gr_help_line o_gr_dash')");
			sb.append(";\n");
			sb.append("})");
			sb.append(";\n");
		}
		
		if (numeric) {
			for (Breakpoint breakpoint: breakpoints) {
				String yValue = numeric ? breakpoint.getGrade() : THREE_DIGITS.format(breakpoint.getBestToLowest().intValue() + 0.5);
				sb.append("svg.append('path')\n");
				sb.append("  .datum([").append(getLineData(yValue, breakpoint.getScore(), xMin, yMin)).append("])\n");
				sb.append("  .attr('class', 'line')\n");
				sb.append("  .attr('d', line)\n");
				sb.append("  .attr('class', 'o_gr_scale_chart_line o_gr_help_line')");
				sb.append(";\n");
			}
		}
		
		if (passedInfo.isPassed()) {
			if (passedInfo.getCutValue() != null) {
				String yValue = THREE_DIGITS.format(passedInfo.getCutValueGrade());
				sb.append("svg.append('path')\n");
				sb.append("  .datum([").append(getLineData(yValue, passedInfo.getCutValue(), xMin, yMin)).append("])\n");
				sb.append("  .attr('class', 'line')\n");
				sb.append("  .attr('d', line)\n");
				sb.append("  .attr('class', 'o_gr_scale_chart_line o_gr_cut_value')");
				sb.append(";\n");
			}
			if (passedInfo.getCutBound() != null) {
				String yValue = THREE_DIGITS.format(passedInfo.getCutBoundGrade());
				sb.append("svg.append('path')\n");
				sb.append("  .datum([").append(getLineData(yValue, passedInfo.getCutBound(), xMin, yMin)).append("])\n");
				sb.append("  .attr('class', 'line')\n");
				sb.append("  .attr('d', line)\n");
				sb.append("  .attr('d', line)\n");
				sb.append("  .attr('class', 'o_gr_scale_chart_line o_gr_help_line o_gr_dash')");
				sb.append(";\n");
			}
		}
		
		for (Line line: lines) {
			sb.append("svg.append('path')\n");
			sb.append("  .datum([").append(line.getData()).append("])\n");
			sb.append("  .attr('class', 'line')\n");
			sb.append("  .attr('d', line)\n");
			sb.append("  .attr('class', '").append(line.getCssClass()).append("')");
			sb.append(";\n");
		}
		
		sb.append("});\n")
		  .append("/* ]]> */")
		  .append("</script>\n");
	}

	private List<Line> getNumericData(NavigableSet<GradeScoreRange> ranges, List<Breakpoint> breakpoints, PassedInfo passedInfo) {
		List<Line> lines = new ArrayList<>(2);
		StringBuilder sb = new StringBuilder();

		String lineCss = passedInfo.isPassed()? "o_gr_failed": "";
		sb.append("[").append(THREE_DIGITS.format(ranges.last().getLowerBound())).append(",").append(ranges.last().getGrade()).append("],");
		
		breakpoints.sort((b1, b2) -> b1.getScore().compareTo(b2.getScore()));
		for (Breakpoint breakpoint: breakpoints) {
			if ("o_gr_failed".equals(lineCss) && passedInfo.isPassed() && passedInfo.getCutBound().compareTo(breakpoint.getScore()) < 0) {
				sb.append("[").append(THREE_DIGITS.format(passedInfo.getCutBound())).append(",").append(passedInfo.getCutBoundGrade()).append("]");
				lines.add(new Line("o_gr_scale_chart_line " + lineCss, sb.toString()));
				sb = new StringBuilder();
				lineCss = "o_gr_passed";
				sb.append("[").append(THREE_DIGITS.format(passedInfo.getCutBound())).append(",").append(passedInfo.getCutBoundGrade()).append("],");
			}
			sb.append("[").append(THREE_DIGITS.format(breakpoint.getScore())).append(",").append(breakpoint.getGrade()).append("],");
		}
		
		if ("o_gr_failed".equals(lineCss) && passedInfo.isPassed() && passedInfo.getCutBound().compareTo(ranges.first().getUpperBound()) < 0) {
			sb.append("[").append(THREE_DIGITS.format(passedInfo.getCutBound())).append(",").append(passedInfo.getCutBoundGrade()).append("]");
			lines.add(new Line("o_gr_scale_chart_line " + lineCss, sb.toString()));
			sb = new StringBuilder();
			lineCss = "o_gr_passed";
			sb.append("[").append(THREE_DIGITS.format(passedInfo.getCutBound())).append(",").append(passedInfo.getCutBoundGrade()).append("],");
		}
		sb.append("[").append(THREE_DIGITS.format(ranges.first().getUpperBound())).append(",").append(ranges.first().getGrade()).append("]");
		lines.add(new Line("o_gr_scale_chart_line " + lineCss, sb.toString()));
		
		return lines;
	}
	
	private List<Line> getTextData(NavigableSet<GradeScoreRange> ranges) {
		List<Line> lines = new ArrayList<>(2);
		
		StringBuilder sb = new StringBuilder();
		String lineCss = "";
		if (ranges.last().getPassed() != null) {
			lineCss = ranges.last().getPassed()? "o_gr_passed": "o_gr_failed"; 
		}
		
		Iterator<GradeScoreRange> rangeIterator = ranges.descendingIterator();
		while(rangeIterator.hasNext()) {
			GradeScoreRange range = rangeIterator.next();
			sb.append("[").append(THREE_DIGITS.format(range.getLowerBound())).append(",").append(range.getBestToLowest() + 0.5).append("]");
				
			String rangeLineCss = "";
			if (range.getPassed() != null) {
				rangeLineCss = range.getPassed()? "o_gr_passed": "o_gr_failed"; 
			}
			if (!rangeLineCss.equals(lineCss)) {
				lines.add(new Line("o_gr_scale_chart_line " + lineCss, sb.toString()));
				lineCss = rangeLineCss;
				sb = new StringBuilder();
				sb.append("[").append(THREE_DIGITS.format(range.getLowerBound())).append(",").append(range.getBestToLowest() + 0.5).append("]");
			}
			if (sb.length() > 0) {
				sb.append(",");
			}
			
		}
		sb.append("[").append(THREE_DIGITS.format(ranges.first().getUpperBound())).append(",").append(ranges.first().getBestToLowest() - 0.5).append("]");
		lines.add(new Line("o_gr_scale_chart_line " + lineCss, sb.toString()));
		
		return lines;
	}
	
	private String getLineData(String grade, BigDecimal value, String xMin, String yMin) {
		StringBuilder sb = new StringBuilder();

		sb.append("[").append(xMin).append(",").append(grade).append("],");
		sb.append("[").append(THREE_DIGITS.format(value)).append(",").append(grade).append("],");
		sb.append("[").append(THREE_DIGITS.format(value)).append(",").append(yMin).append("]");
		
		return sb.toString();
	}
	
	private List<String> getNumericYLines(NavigableSet<GradeScoreRange> ranges) {
		List<String> yLines = new ArrayList<>();
		boolean diffLowerTwelfe = new BigDecimal(ranges.first().getGrade()).subtract(new BigDecimal(ranges.last().getGrade())).compareTo(TWELFE) < 0;
		Predicate<String> filterNumericYLine = diffLowerTwelfe 
				? grade -> grade.indexOf(".") < 1									// Only whole grades.
				:  grade -> grade.indexOf(".") < 1 && grade.endsWith("0");			// Only tenth gardes.
		for (GradeScoreRange range : ranges) {
			// Skip the lowest grade. It overlaps the x domain line.
			if (filterNumericYLine.test(range.getGrade()) && range != ranges.last()) {
				yLines.add(range.getGrade());
			}
		}
		return yLines;
	}

	private List<String> getTextYLines(NavigableSet<GradeScoreRange> ranges) {
		List<String> yLines = new ArrayList<>();
		for (GradeScoreRange range : ranges) {
			yLines.add(THREE_DIGITS.format(range.getBestToLowest() - 0.5));
		}
		return yLines;
	}


	private PassedInfo getNumericPassedInfo(GradeSystem gradeSystem, NavigableSet<GradeScoreRange> ranges) {
		if (gradeSystem.hasPassed() && gradeSystem.getCutValue() != null) {
			GradeScoreRange previousRange = null;
			Iterator<GradeScoreRange> rangeIterator = ranges.descendingIterator();
			while(rangeIterator.hasNext()) {
				GradeScoreRange range = rangeIterator.next();
				if (range.getPassed() != null && range.getPassed().booleanValue()) {
					BigDecimal boundGrade = previousRange == null
							? new BigDecimal(range.getGrade())
							: new BigDecimal(range.getGrade()).add(new BigDecimal(previousRange.getGrade())).divide(TWO);
					return new PassedInfo(true, new BigDecimal(range.getGrade()), range.getScore(), boundGrade, range.getLowerBound());
				}
				previousRange = range;
			}
		}
		return new PassedInfo(false, null, null, null, null);
	}

	private PassedInfo getTextPassedInfo(NavigableSet<GradeScoreRange> ranges) {
		List<GradeScoreRange> reversedRanges = ranges.stream()
				.sorted(Comparator.reverseOrder())
				.collect(Collectors.toList());
		GradeScoreRange previousRange = null;
		for (int i = 0; i < reversedRanges.size(); i++) {
			GradeScoreRange range = reversedRanges.get(i);
			if (range.getPassed() != null && range.getPassed().booleanValue()) {
				BigDecimal grade = previousRange == null
						? new BigDecimal(range.getBestToLowest())
						: new BigDecimal(range.getBestToLowest() + 0.5);
				return new PassedInfo(true, grade, range.getLowerBound(), null, null);
			}
			previousRange = range;
		}
		return new PassedInfo(false, null, null, null, null);
	}
	
	private static final class PassedInfo {
		
		private final boolean passed;
		private final BigDecimal cutValueGrade;
		private final BigDecimal cutValue;
		private final BigDecimal cutBoundGrade;
		private final BigDecimal cutBound;
		
		public PassedInfo(boolean passed, BigDecimal cutValueGrade, BigDecimal cutValue, BigDecimal cutBoundGrade, BigDecimal cutBound) {
			this.passed = passed;
			this.cutValueGrade = cutValueGrade;
			this.cutValue = cutValue;
			this.cutBoundGrade = cutBoundGrade;
			this.cutBound = cutBound;
		}
		
		public boolean isPassed() {
			return passed;
		}
		
		public BigDecimal getCutValueGrade() {
			return cutValueGrade;
		}
		
		public BigDecimal getCutValue() {
			return cutValue;
		}
		
		public BigDecimal getCutBoundGrade() {
			return cutBoundGrade;
		}

		public BigDecimal getCutBound() {
			return cutBound;
		}
		
	}
	
	private static final class Line {
		
		private final String cssClass;
		private final String data;
		
		public Line(String cssClass, String data) {
			this.cssClass = cssClass;
			this.data = data;
		}

		public String getCssClass() {
			return cssClass;
		}

		public String getData() {
			return data;
		}
		
	}
	
}

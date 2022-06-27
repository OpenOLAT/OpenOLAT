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
package org.olat.modules.assessment.ui.component;

import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.assessment.ui.component.GradeChart.GradeCount;
import org.olat.modules.grade.GradeSystem;
import org.olat.modules.grade.GradeSystemType;
import org.olat.modules.grade.NumericResolution;
import org.olat.modules.grade.ui.GradeUIFactory;

/**
 * 
 * Initial date: 18 Mar: 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class GradeChartRenderer extends DefaultComponentRenderer {

	@Override
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		
		GradeChart gradeChart = (GradeChart)source;
		String cmpId = gradeChart.getDispatchID();
		
		GradeSystem gradeSystem = gradeChart.getGradeSystem();
		List<GradeCount> gradeCounts = gradeChart.getGradeCounts();
		String data = getData(gradeCounts);
		long yMax = getYMax(gradeCounts);
		
		sb.append("<div id='d").append(cmpId).append("d3holder' class='d3chart' style='width:400px;height:200px'></div>\n")
		  .append("<script>\n")
		  .append("/* <![CDATA[ */ ")
		  .append("jQuery(function () {\n")
		  .append("var placeholderheight = jQuery('#d").append(cmpId).append("d3holder').height();\n")
		  .append("var placeholderwidth = jQuery('#d").append(cmpId).append("d3holder').width();\n")
		  .append("var data = [").append(data).append("];\n");

		sb.append("var margin = {top: 5, right: 50, bottom: 40, left: 40},\n")
		  .append("    width = placeholderwidth - margin.left - margin.right,\n")
		  .append("    height = placeholderheight - margin.top - margin.bottom;\n")
		  .append("var xScale = d3.scaleBand()\n")
		  .append("    .domain(data.map(function(d) { return d[0]; }))\n")
		  .append("    .range([0, width]).paddingInner(0.1);\n")
		  .append("var yScale = d3.scaleLinear()\n")
		  .append("    .domain([0, ").append(yMax).append("])\n")
		  .append("    .range([height, 0]);\n");
		
		if (GradeSystemType.numeric == gradeSystem.getType()) {
			if (NumericResolution.tenth == gradeSystem.getResolution()) {
				sb.append("var xAxis = d3.axisBottom(xScale).tickValues(xScale.domain().filter(d => Number.isInteger(Number(d)) ));\n");
			} else if (Math.abs(gradeSystem.getBestGrade() - gradeSystem.getLowestGrade()) > 12) {
				sb.append("var xAxis = d3.axisBottom(xScale).tickValues(xScale.domain().filter(d => Number.isInteger(Number(d)) && d % 10 === 0));\n");
			} else {
				sb.append("var yLegend = xScale.domain().filter(d => Number.isInteger(Number(d)) );");
				sb.append("var xAxis = d3.axisBottom(xScale).tickFormat(d => yLegend.includes(d)? d: '');\n");
			}
		} else {
			// first and last
			sb.append("var xLegend = xScale.domain().filter(function(d,i){ return i < 1 || i > (xScale.domain().length-2) });");
			sb.append("var xAxis = d3.axisBottom(xScale).tickFormat(d => xLegend.includes(d)? d: '');\n");
		}
		
		sb.append("var yTicks = [0,").append(yMax).append("];\n")
		  .append("var yAxis = d3.axisLeft(yScale).tickFormat( function(d) { return d } ).tickValues(yTicks);\n");

		sb.append("\n")
		  .append("var svg = d3.select('#d").append(cmpId).append("d3holder').append('svg')\n")
		  .append("    .attr('width', width + margin.left + margin.right)\n")
		  .append("    .attr('height', height + margin.top + margin.bottom)\n")
		  .append("    .append('g')\n")
		  .append("    .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');\n");

		sb.append("svg.append('g')\n")
		  .append("   .attr('class', 'x axis')\n")
		  .append("   .attr('transform', 'translate(0,' + height + ')')\n")
		  .append("   .call(xAxis)\n");
		sb.append("  .append('text')\n")
		  .append("    .attr('y', (margin.bottom / 2))\n")
		  .append("    .attr('x', (width / 2))\n")
		  .append("    .attr('dy', '1em')\n")
		  .append("    .attr('fill', '#000')\n")
		  .append("    .style('text-anchor', 'middle')\n")
		  .append("    .text('").append(GradeUIFactory.translateGradeSystemLabel(translator, gradeSystem)).append("')\n");
		sb.append(";\n");
		
		sb.append("svg.append('g')\n")
		  .append("    .attr('class', 'y axis')\n")
		  .append("    .call(yAxis)\n");
		sb.append("  .append('text')")
		  .append("    .attr('transform', 'rotate(-90)')")
		  .append("    .attr('y', 0 - margin.left / 2)")
		  .append("    .attr('x', 0 - (height / 2))")
		  .append("    .attr('dy', '1em')")
		  .append("    .attr('fill', '#000')")
		  .append("    .style('text-anchor', 'middle')")
		  .append("    .text('").append(translator.translate("participants")).append("')");
		sb.append(";\n");
		
		sb.append("svg.selectAll()\n");
		sb.append("    .data(data)\n");
		sb.append("    .enter()\n");
		sb.append("    .append('rect')\n");
		sb.append("    .attr('class', 'bar bar_default')\n");
		sb.append("    .attr('x', (d) => xScale(d[0]))\n");
		sb.append("    .attr('y', (d) => yScale(d[1]))\n");
		sb.append("    .attr('height', (d) => height - yScale(d[1]))\n");
		sb.append("    .attr('width', xScale.bandwidth());");

		sb.append("});\n")
		  .append("/* ]]> */")
		  .append("</script>\n");
	}
	
	private String getData(List<GradeCount> gradeCounts) {
		return gradeCounts.stream()
				.map(gc -> "['" + gc.getGrade() + "'," + gc.getCount() + "]")
				.collect(Collectors.joining(","));
	}

	private long getYMax(List<GradeCount> gradeCounts) {
		return gradeCounts.stream().map(GradeCount::getCount).max(Long::compareTo).orElse(Long.valueOf(1)).longValue();
	}

}

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
package org.olat.core.gui.components.chart;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Renderer a list of lons or doubles as an histogramm with d3js
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class HistogramComponentRenderer extends DefaultComponentRenderer {

	@Override
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
			Translator translator, RenderResult renderResult, String[] args) {

		HistogramComponent chartCmp = (HistogramComponent)source;
		renderD3js(renderer, sb, chartCmp);
	}
	
	private StringBuilder getValues(HistogramComponent chartCmp) {
		StringBuilder sb = new StringBuilder();
		if(chartCmp.getDoubleValues() != null) {
			for(double value:chartCmp.getDoubleValues()) {
				if(sb.length() > 0) sb.append(",");
				sb.append(value);
			}	
		} else if(chartCmp.getLongValues() != null) {
			for(long value:chartCmp.getLongValues()) {
				if(sb.length() > 0) sb.append(",");
				sb.append((double)value);
			}
		}
		return sb;
	}
	
	private double getMaxValue(HistogramComponent chartCmp) {
		double maxValue = 0.0;
		if(chartCmp.getMaxValue() > 0) {
			maxValue =  chartCmp.getMaxValue();
		} else if(chartCmp.getDoubleValues() != null) {
			for(double value:chartCmp.getDoubleValues()) {
				maxValue = Math.max(maxValue, value);
			}	
		} else if(chartCmp.getLongValues() != null) {
			for(long value:chartCmp.getLongValues()) {
				maxValue = Math.max(maxValue, value);
			}
		}
		
		double ceiledRoundedMaxValue = Math.ceil(maxValue);
		return ceiledRoundedMaxValue;
	}
	
	private StringOutput getFillFunction(HistogramComponent chartCmp) {
		StringOutput sb = new StringOutput();
		if(chartCmp.getCutValue() > 0.0001) {
			String cutValue = Double.toString(chartCmp.getCutValue());
			sb.append("function(d, i) { if(data[i].x < ").append(cutValue).append(") ")
			  .append("  return 'bar ").append(chartCmp.getLowBarClass()).append("';")
			  .append(" return 'bar ").append(chartCmp.getHighBarClass()).append("';}");
		} else {
			sb.append("'bar ").append(chartCmp.getDefaultBarClass()).append("'");
		}
		return sb;
	}
	
	private void renderD3js(Renderer renderer, StringOutput sb, HistogramComponent chartCmp) {
		
		String cmpId = chartCmp.getDispatchID();
		StringBuilder values = getValues(chartCmp);
		double maxValue = getMaxValue(chartCmp);
		String yLegend = chartCmp.getYLegend();
		
		if(maxValue <= 0.1) {
			sb.append("No data");
			return;//no values
		}

		sb.append("<div id='d").append(cmpId).append("d3holder' class='d3chart' style='width:600px;height:300px'></div>\n")
		  .append("<script type='text/javascript'>\n")
		  .append("/* <![CDATA[ */ ")
		  .append("jQuery(function () {\n")
		  .append("var placeholderheight = jQuery('#d").append(cmpId).append("d3holder').height();\n")
		  .append("var placeholderwidth = jQuery('#d").append(cmpId).append("d3holder').width();\n");

		
		sb.append("var values =[").append(values).append("];\n")
		// Formatters for counts and times (converting numbers to Dates).
		  .append("var formatCount = d3.format(',.f'),\n")
		  .append("  formatTime = d3.time.format('%H:%M'),\n")
		  .append("  formatMinutes = function(d) { return formatTime(new Date(2012, 0, 1, 0, d)); };\n")

		  .append("var margin = {top: 10, right: 40, bottom: 30, left: 70},\n")
		  .append("  width = placeholderwidth - margin.left - margin.right,\n")
		  .append("  height = placeholderheight - margin.top - margin.bottom;\n")

		  .append("var x = d3.scale.linear()\n")
		  .append("  .domain([0, ").append(Double.toString(maxValue)).append("])\n")
		  .append("  .range([0, width]);\n")

		  //generate a histogram using twenty uniformly-spaced bins.
		  .append("var data = d3.layout.histogram()\n")
		  .append("  .bins(x.ticks(20))\n")
		  .append("  (values);\n")

		  .append("var sum = d3.sum(data, function(d) { return d.y; });\n")

		  .append("var y = d3.scale.linear()\n")
		  .append("  .domain([0, d3.max(data, function(d) { return d.y; })])\n")
		  .append("  .range([height, 0]);\n")

		  .append("var y2 = d3.scale.linear()\n")
		  .append("  .domain([0, d3.max(data, function(d) { return d.y / sum; })])\n")
		  .append("  .range([height, 0]);\n")

		  .append("var xAxis = d3.svg.axis()\n")
		  .append("  .scale(x)\n")
		  .append("  .orient('bottom')\n");
		if(chartCmp.getXScale() == Scale.hour) {
			sb.append("  .tickFormat(formatMinutes);\n");
		} else {
			sb.append("  .tickFormat(d3.format('.01f'));\n");
		}
		sb.append("var yAxis = d3.svg.axis()\n")
		  .append("  .scale(y)\n")
		  .append("  .orient('right')\n")
		  .append("  .ticks(10);\n")
		    
		  .append("var y2Axis = d3.svg.axis()\n")
		  .append("  .scale(y2)\n")
		  .append("  .orient('left')\n")
		  .append("  .ticks(10, '%');\n")

		  .append("var svg = d3.select('#d").append(cmpId).append("d3holder').append('svg')\n")
		  .append("  .attr('width', width + margin.left + margin.right)\n")
		  .append("  .attr('height', height + margin.top + margin.bottom)\n")
		  .append(" .append('g')\n")
		  .append("  .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');\n")

		  .append("var bar = svg.selectAll('.bar')\n")
		  .append("  .data(data)\n")
		  .append(" .enter().append('g')\n")
		  .append("  .attr('class', ").append(getFillFunction(chartCmp)).append(")\n")

		  .append("  .attr('transform', function(d) { return 'translate(' + x(d.x) + ',' + y(d.y) + ')'; })\n")
		  .append("  .append('rect')\n")
		  .append("  .attr('x', 1)\n")
		  .append("  .attr('width', x(data[0].dx) - 1)\n")
		  .append("  .attr('height', function(d) { return height - y(d.y); });\n")

		  //x axis
		  .append("svg.append('g')\n")
		  .append("  .attr('class', 'x axis')\n")
		  .append("  .attr('transform', 'translate(0,' + height + ')')\n")
		  .append("  .call(xAxis);\n")

		  //right y axis
		  .append("svg.append('g')\n")
		  .append("  .attr('class', 'y axis')\n")
		  .append("  .attr('transform', 'translate(' + width + ',0)')\n")
		  .append(" .call(yAxis)\n")

		  //left y axis with legend
		  .append("svg.append('g')\n")
		  .append("  .attr('class', 'y axis')\n")
		  .append("  .call(y2Axis)\n")
		  .append(" .append('text')\n")
		  .append("  .attr('transform', 'rotate(-90)')\n")
		  .append("  .attr('y', 0 - margin.left)\n")
		  .append("  .attr('x', 0 - (height / 2))\n")
		  .append("  .attr('dy', '1em')\n")
		  .append("  .style('text-anchor', 'middle')\n")
		  .append("  .text('").append(yLegend).append("');\n");

		sb.append("});\n")
		  .append("/* ]]> */")
		  .append("</script>\n");
	}
}

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

import java.util.List;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.chart.BarSeries.Stringuified;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class HorizontalBarChartComponentRenderer extends DefaultComponentRenderer {

	@Override
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
			Translator translator, RenderResult renderResult, String[] args) {
		
		HorizontalBarChartComponent chartCmp = (HorizontalBarChartComponent)source;
		renderD3js(renderer, sb, chartCmp);
	}

	private void renderD3js(Renderer renderer, StringOutput sb, HorizontalBarChartComponent chartCmp) {
		String cmpId = chartCmp.getDispatchID();
		List<BarSeries> seriesList = chartCmp.getSeries();
		Stringuified infos = BarSeries.getDatasAndColors(seriesList, chartCmp.getDefaultBarClass());
		int maxNumOfPoints = getNumOfPoints(chartCmp);
		int height = 50 + (25 * maxNumOfPoints);
		sb.append("<div id='d").append(cmpId).append("d3holder' class='d3chart' style='width:600px;height:").append(height).append("px'></div>\n")
		  .append("<script type='text/javascript'>\n")
		  .append("/* <![CDATA[ */ ")
		  .append("jQuery(function () {\n")
		  .append("var placeholderheight = jQuery('#d").append(cmpId).append("d3holder').height();\n")
		  .append("var placeholderwidth = jQuery('#d").append(cmpId).append("d3holder').width();\n");
		
		sb.append("var data = [").append(infos.getData()).append("];\n");
		
		sb.append("var margin = {top: 20, right: 20, bottom: 30, left: 300},\n")
		  .append("   width = placeholderwidth - margin.left - margin.right,\n")
		  .append("   height = placeholderheight - margin.top - margin.bottom;\n")

		  .append("var x = d3.scale.linear()\n")
		  .append("   .range([0, width], .1);\n")

		  .append("var y = d3.scale.ordinal()\n")
		  .append("   .rangeRoundBands([height, 0]);\n")

		  .append("var xAxis = d3.svg.axis()\n")
		  .append("   .scale(x)\n")
		  .append("   .orient('bottom')\n")
		  .append("   .ticks(5);\n")

		  .append("var yAxis = d3.svg.axis()\n")
		  .append("   .scale(y)\n")
		  .append("   .orient('left');\n")

		  .append("var svg = d3.select('#d").append(cmpId).append("d3holder').append('svg')\n")
		  .append("   .attr('width', width + margin.left + margin.right)\n")
		  .append("   .attr('height', height + margin.top + margin.bottom)\n")
		  .append(" .append('g')\n")
		  .append("   .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');\n")

		  .append("x.domain([0, d3.max(data, function(d) { return d[1]; })]);\n")
		  .append("y.domain(data.map(function(d) { return d[0]; }));\n")

		  .append("svg.append('g')\n")
		  .append("     .attr('class', 'x axis')\n")
		  .append("     .attr('transform', 'translate(0,' + height + ')')\n")
		  .append("     .call(xAxis);\n")

		  .append("svg.append('g')\n")
		  .append("     .attr('class', 'y axis')\n")
		  .append("     .call(yAxis);\n");
		appendSeries( sb, infos.getColors(), chartCmp);
/*
		  .append("svg.selectAll('.bar')\n")
		  .append("     .data(data)\n")
		  .append("   .enter().append('rect')\n")
		  .append("     .attr('class', 'bar bar_default')\n")
		  .append("     .attr('x', 0)\n")
		  .append("     .attr('width', function(d) { return width - x(d[1]); })\n")
		  .append("     .attr('y', function(d) { return y(d[0]); })\n")
		  .append("     .attr('height', y.rangeBand() - 2);\n");
		*/

		sb.append("});\n")
		  .append("/* ]]> */")
		  .append("</script>\n");
	}
	
	private int getNumOfPoints(HorizontalBarChartComponent chartCmp) {
		int maxNumOfPoints = 0;
		List<BarSeries> seriesList = chartCmp.getSeries();
		for(BarSeries series:seriesList) {
			int numOfPoints = series.getPoints().size();
			maxNumOfPoints = Math.max(maxNumOfPoints, numOfPoints);
		}
		return maxNumOfPoints;
	}
	
	private void appendSeries(StringOutput sb, StringBuilder colors, HorizontalBarChartComponent chartCmp) {
		if(colors.length() > 0) {
			sb.append("var colors = [").append(colors).append("];");
		}
		
		List<BarSeries> seriesList = chartCmp.getSeries();
		for(int i=0; i<seriesList.size(); i++) {
			String color = seriesList.get(i).getCssClass();
			if(color == null) {
				color = chartCmp.getDefaultBarClass();
			}
			
			String correction = getCorrection(i);
		
			sb.append("svg.selectAll('.bar").append(i).append("')\n")
			  .append("    .data(data)\n")
			  .append("  .enter().append('rect')\n");
			
			if(colors.length() == 0) {
				sb.append("    .attr('class', 'bar bar").append(i).append(" ").append(color).append("')\n");
			} else {
				sb.append("    .attr('class', function(d, i){ if(colors.length > i) { return colors[i]; } return 'bar bar").append(i).append(" ").append(color).append("'; })\n");
			}

			sb.append("    .attr('fill', '").append(color).append("')\n")
			  .append("    .attr('x', ").append(correction).append(")\n")
			  .append("    .attr('y', function(d) { return y(d[0]); })\n")
			  .append("    .attr('width', function(d) { return x(d[").append((i+1)).append("]); })\n")
			  //.append("    .attr('height', function(d) { return height - y(d[").append((i+1)).append("]); });\n");
			  .append("     .attr('height', y.rangeBand() - 2);\n");
		}
	}
	
	private String getCorrection(int i) {
		if(i == 0) return "0";
		if(i == 1) return "function(d) { return x(d[1]); }";
		if(i == 2) return "function(d) { return x(d[1] + d[2]); }";
		return "";
	}
}

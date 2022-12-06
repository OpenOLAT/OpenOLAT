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
import org.olat.core.util.StringHelper;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BarChartComponentRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
			Translator translator, RenderResult renderResult, String[] args) {
		
		BarChartComponent chartCmp = (BarChartComponent)source;
		List<BarSeries> seriesList = chartCmp.getSeries();
		
		String yLegend = chartCmp.getYLegend();
		String xLegend = chartCmp.getXLegend();

		Stringuified infos = BarSeries.getDatasAndColors(seriesList, chartCmp.getDefaultBarClass());
		int xScaleModule = seriesList.isEmpty()? 1: seriesList.get(0).getPoints().size() / 8;

		String sum = getSum(seriesList);
		String cmpId = chartCmp.getDispatchID();
		
		sb.append("<div id='d").append(cmpId).append("d3holder' class='d3chart' style='width:600px;height:300px'></div>\n")
		  .append("<script>\n")
		  .append("/* <![CDATA[ */ ")
		  .append("jQuery(function () {\n")
		  .append("var placeholderheight = jQuery('#d").append(cmpId).append("d3holder').height();\n")
		  .append("var placeholderwidth = jQuery('#d").append(cmpId).append("d3holder').width();\n")
		  .append("var data = [").append(infos.getData()).append("];\n");

		sb.append("var margin = {top: 20, right: 20, bottom: 40, left: 50},\n")
		  .append("    width = placeholderwidth - margin.left - margin.right,\n")
		  .append("    height = placeholderheight - margin.top - margin.bottom;\n")
		  .append("\n")
		  .append("var x = d3.scaleBand()\n")
		  .append("    .domain(data.map(function(d) { return d[0]; }))\n")
		  .append("    .rangeRound([0, width]).padding(.1);\n")
		  .append("\n")
		  .append("var y = d3.scaleLinear()\n")
		  .append("    .domain([0, d3.max(data, function(d) { return ").append(sum).append("; })])\n")
		  .append("    .range([height, 0]);\n")
		  .append("\n")
		  .append("var xAxis = d3.axisBottom(x).tickValues(x.domain().filter(function(d,i){ return !(i%").append(xScaleModule).append(")}));\n")
		  .append("\n")
		  .append("var yAxis = d3.axisLeft(y);\n");

		sb.append("\n")
		  .append("var svg = d3.select('#d").append(cmpId).append("d3holder').append('svg')\n")
		  .append("    .attr('width', width + margin.left + margin.right)\n")
		  .append("    .attr('height', height + margin.top + margin.bottom)\n")
		  .append("    .append('g')\n")
		  .append("    .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');\n");

		//append x axis and legend
		sb.append("svg.append('g')\n")
		  .append("   .attr('class', 'x axis')\n")
		  .append("   .attr('transform', 'translate(0,' + height + ')')\n")
		  .append("   .call(xAxis)\n");
		if(StringHelper.containsNonWhitespace(xLegend)) {
			sb.append("  .append('text')\n")
			  .append("    .attr('y', (margin.bottom / 2))\n")
			  .append("    .attr('x', (width / 2))\n")
			  .append("    .attr('dy', '1em')\n")
			  .append("    .attr('fill', '#000')\n")
			  .append("    .style('text-anchor', 'middle')\n")
			  .append("    .text('").append(xLegend).append("')\n");
		}
		sb.append(";\n");
		
		//append y axis and legend
		sb.append("svg.append('g')\n")
		  .append("    .attr('class', 'y axis')\n")
		  .append("    .call(yAxis)\n");
		if(StringHelper.containsNonWhitespace(yLegend)) {
			sb.append("  .append('text')\n")
			  .append("    .attr('transform', 'rotate(-90)')\n")
			  .append("    .attr('y', 0 - margin.left)\n")
			  .append("    .attr('x', 0 - (height / 2))\n")
			  .append("    .attr('dy', '1em')\n")
			  .append("    .attr('fill', '#000')\n")
			  .append("    .style('text-anchor', 'middle')\n")
			  .append("    .text('").append(yLegend).append("')\n")
			  .append("\n");
		}
		sb.append(";\n");

		appendSeries(sb, infos.getColors(), chartCmp);
		
		sb.append("});\n")
		  .append("/* ]]> */")
		  .append("</script>\n");
	}
	
	private void appendSeries(StringOutput sb, StringBuilder colors, BarChartComponent chartCmp) {
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
			  .append("    .attr('x', function(d) { return x(d[0]); })\n")
			  .append("    .attr('y', function(d) { return y(d[").append((i+1)).append("]) ").append(correction).append(" ; })\n")
			  .append("    .attr('width', x.bandwidth())\n")
			  .append("    .attr('height', function(d) { return height - y(d[").append((i+1)).append("]); });\n");
		}
	}
	
	private String getSum(List<BarSeries> seriesList) {
		StringBuilder sum = new StringBuilder();
		for(int i=0; i<seriesList.size(); i++) {
			if(sum.length() > 0) sum.append(" + ");
			sum.append(" d[").append((i+1)).append("]");
		}
		return sum.toString();
	}
	
	private String getCorrection(int i) {
		if(i == 0) return "";
		if(i == 1) return "- (height - y(d[1]))";
		if(i == 2) return "- (height - y(d[1] + d[2]))";
		return "";
	}
}

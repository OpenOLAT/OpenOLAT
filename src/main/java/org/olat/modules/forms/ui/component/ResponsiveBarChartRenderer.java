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
package org.olat.modules.forms.ui.component;

import java.util.List;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.chart.BarSeries;
import org.olat.core.gui.components.chart.BarSeries.Stringuified;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 28.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ResponsiveBarChartRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
			Translator translator, RenderResult renderResult, String[] args) {
		
		ResponsiveBarChartComponent chartCmp = (ResponsiveBarChartComponent)source;
		List<BarSeries> seriesList = chartCmp.getSeries();
		
		String yLegend = chartCmp.getYLegend();
		String xLegend = chartCmp.getXLegend();

		Stringuified infos = BarSeries.getDatasAndColors(seriesList, chartCmp.getDefaultBarClass());

		String yMax = getYMax(chartCmp);
		String yMin = getYMin(chartCmp);
		String cmpId = chartCmp.getDispatchID();
		
		sb.append("<div id='d").append(cmpId).append("d3holder' class='d3chart'></div>")
		  .append("<script>")
		  .append("/* <![CDATA[ */ ")
		  .append("jQuery(function () {")
		  .append("var placeholderheight = 300;")
		  .append("var placeholderwidth = 600;")
		  .append("var data = [").append(infos.getData()).append("];");

		sb.append("var margin = {top: 20, right: 20, bottom: 40, left: 50},")
		  .append("    width = placeholderwidth - margin.left - margin.right,")
		  .append("    height = placeholderheight - margin.top - margin.bottom;")
		  .append("")
		  .append("var x = d3.scaleBand()")
		  .append("    .domain(data.map(function(d) { return d[0]; }))")
		  .append("    .rangeRound([0, width]).padding(.1);")
		  .append("")
		  .append("var xAxis = d3.axisBottom(x);")
		  .append("var yAxisScale = d3.scaleLinear()")
		  .append("    .domain([").append(yMin).append(",").append(yMax).append("])")
		  .append("    .range([height, 0]);")
		  .append("var yAxis = d3.axisLeft(yAxisScale);");


		sb.append("var svg = d3.select('#d").append(cmpId).append("d3holder')");
		sb.append("    .append('div')");
		sb.append("    .classed('svg-container', true)");
		sb.append("    .append('svg')");
		sb.append("    .attr('preserveAspectRatio', 'xMinYMin meet')");
		sb.append("    .attr('viewBox', '0 0 600 300')");
		sb.append("    .classed('svg-content-responsive', true)");
		sb.append("    .append('g')");
		sb.append("    .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');");
		
		appendSeries(sb, infos.getColors(), chartCmp);
		
		//append x axis and legend
		sb.append("svg.append('g')")
		  .append("   .attr('class', 'x axis')")
		  .append("   .attr('transform', 'translate(0,' + yAxisScale(0) + ')')")
		  .append("   .call(xAxis)");
		if(StringHelper.containsNonWhitespace(xLegend)) {
			sb.append("  .append('text')")
			  .append("    .attr('y', (margin.bottom / 2))")
			  .append("    .attr('x', (width / 2))")
			  .append("    .attr('dy', '1em')")
			  .append("    .attr('fill', '#000')")
			  .append("    .style('text-anchor', 'middle')")
			  .append("    .text('").append(xLegend).append("')");
		}
		sb.append(";");
		
		//append y axis and legend
		sb.append("svg.append('g')")
		  .append("    .attr('class', 'y axis')")
		  .append("    .call(yAxis)");
		if(StringHelper.containsNonWhitespace(yLegend)) {
			sb.append("  .append('text')")
			  .append("    .attr('transform', 'rotate(-90)')")
			  .append("    .attr('y', 0 - margin.left)")
			  .append("    .attr('x', 0 - (height / 2))")
			  .append("    .attr('dy', '1em')")
			  .append("    .attr('fill', '#000')")
			  .append("    .style('text-anchor', 'middle')")
			  .append("    .text('").append(yLegend).append("')")
			  .append("");
		}
		sb.append(";");
		
		sb.append("});")
		  .append("/* ]]> */")
		  .append("</script>");
	}
	
	private void appendSeries(StringOutput sb, StringBuilder colors, ResponsiveBarChartComponent chartCmp) {
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
		
			sb.append("svg.selectAll('.bar").append(i).append("')")
			  .append("    .data(data)")
			  .append("  .enter().append('rect')");
			
			if(colors.length() == 0) {
				sb.append("    .attr('class', 'bar bar").append(i).append(" ").append(color).append("')");
			} else {
				sb.append("    .attr('class', function(d, i){ if(colors.length > i) { return colors[i]; } return 'bar bar").append(i).append(" ").append(color).append("'; })");
			}

			sb.append("    .attr('fill', '").append(color).append("')")
			  .append("    .attr('x', function(d) { return x(d[0]); })")
			  .append("    .attr('y', function(d) { return yAxisScale(Math.max(0, d[").append((i+1)).append("])) ").append(correction).append(" ; })")
			  .append("    .attr('width', x.bandwidth())")
			  .append("    .attr('height', function(d) { return Math.abs( yAxisScale(d[").append((i+1)).append("]) - yAxisScale(0)); });");
		}
	}
	
	private String getYMax(ResponsiveBarChartComponent chartCmp) {
		Double yMax = chartCmp.getYMax();
		if (yMax != null) {
			return Double.toString(yMax.doubleValue());
		}
		return "1";
	}
	
	private String getYMin(ResponsiveBarChartComponent chartCmp) {
		Double yMin = chartCmp.getYMin();
		if (yMin != null) {
			return Double.toString(yMin.doubleValue());
		}
		return "0";
	}
	
	private String getCorrection(int i) {
		if(i == 0) return "";
		if(i == 1) return "- (height - y(d[1]))";
		if(i == 2) return "- (height - y(d[1] + d[2]))";
		return "";
	}
}

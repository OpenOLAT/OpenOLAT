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
import org.olat.core.gui.components.chart.RadarSeries.RadarPoint;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.FilterFactory;

/**
 * 
 * Initial date: 20 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RadarChartComponentRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {
		RadarChartComponent soc = (RadarChartComponent)source;
		String cmpId = soc.getDispatchID();
		List<RadarSeries> series = soc.getSeries();
		
		sb.append("<div id='d").append(cmpId).append("d3holder' class='radarChart' style='max-width:800px; max-height:650px; margin:0 auto;'></div>\n");
		sb.append("<script>\n")
		  .append("/* <![CDATA[ */ \n")
		  .append("jQuery(function() {\n")
		  .append(" var render = function() {\n")
		  .append("  var placeholderWidth = jQuery('#d").append(cmpId).append("d3holder').width();\n")
		  .append("  var margin = {top: 75, right: 150, bottom: 75, left: 150};\n")
		  .append("  var radarWidth = (placeholderWidth - margin.right - margin.left);\n")
		  .append("  var radarHeight = radarWidth * (6.0 / 8.0);\n");
		
		renderColorRange(sb, series);
		sb.append("  jQuery('#d").append(cmpId).append("d3holder').ooRadarChart({\n")
		  .append("    w: radarWidth,\n")
		  .append("    h: radarHeight,\n")
		  .append("    margin: margin,\n")
		  .append("    labelFactor: 1.25,\n")
		  .append("    maxValue: ").append(soc.getMaxValue()).append(",\n")
		  .append("    levels: ").append(soc.getLevels()).append(",\n")
		  .append("    roundStrokes: true,\n")
		  .append("    wrapWidth: 100,\n")
		  .append("    format: '").append(soc.getFormat().format()).append("',")
		  .append("    color: color,\n");
		if(soc.getAxis() != null && !soc.getAxis().isEmpty()) {
			sb.append("    axis: ");
			renderAxis(sb, soc.getAxis());
			sb.append(",\n");
		}
		if(soc.isShowLegend()) {
			sb.append("    legendOptions: ");
			renderLegends(sb, series);
			sb.append(",\n");
		}
		sb.append("    values: ");
		renderValues(sb, series);
		sb.append("  });\n")
		  .append(" }\n")//end render function
		  .append(" render();\n")
		  .append(" jQuery(window).on('resize', render);\n")
		  .append("});\n")
		  .append("/* ]]> */\n")
		  .append("</script>");
	}
	
	private void renderColorRange(StringOutput sb, List<RadarSeries> series) {
		int hasColor = 0;
		for(RadarSeries serie:series) {
			if(StringHelper.containsNonWhitespace(serie.getColor())) {
				hasColor++;
			}
		}
		
		int numOfSeries = series.size();
		if(hasColor == numOfSeries) {
			sb.append("  var color = d3.scaleOrdinal().range([");
			for(int i=0;i<numOfSeries; i++) {
				if(i > 0) {
					sb.append(",");
				}
				sb.append("\"").append(series.get(i).getColor()).append("\"");
			}
			sb.append("]);\n");
		} else if(numOfSeries < 4) {
			sb.append("  var color = d3.scaleOrdinal().range([\"#EDC951\",\"#CC333F\",\"#00A0B0\"]);\n");
		} else {
			sb.append("  var color = d3.scaleOrdinal(d3.schemeCategory10);\n");
		}
	}
	
	private void renderAxis(StringOutput sb, List<String> axis) {
		int numOfSeries = axis.size();
		
		sb.append("[");
		for(int i=0; i<numOfSeries; i++) {
			String name = axis.get(i);
			name = FilterFactory.getHtmlTagAndDescapingFilter().filter(name);
			name = StringHelper.escapeJavaScript(name);
			sb.append("\"").append(name).append("\"");
			if(i < (numOfSeries - 1)) {
				sb.append(",");
			}
		}
		sb.append("]");
	}

	private void renderLegends(StringOutput sb, List<RadarSeries> series) {
		int numOfSeries = series.size();
		
		sb.append("[");
		for(int i=0; i<numOfSeries; i++) {
			String name = series.get(i).getName();
			name = FilterFactory.getHtmlTagAndDescapingFilter().filter(name);
			name = StringHelper.escapeJavaScript(name);
			sb.append("\"").append(name).append("\"");
			if(i < (numOfSeries - 1)) {
				sb.append(",");
			}
		}
		sb.append("]");
	}
	
	private void renderValues(StringOutput sb, List<RadarSeries> series) {
		int numOfSeries = series.size();
		
		sb.append("[");
		for(int i=0; i<numOfSeries; i++) {
			sb.append("[");
			List<RadarPoint> points = series.get(i).getPoints();
			
			int numOfPoints = points.size();
			for(int j=0; j<numOfPoints; j++) {
				RadarPoint point = points.get(j);
				String axis = point.getAxis();
				axis = FilterFactory.getHtmlTagAndDescapingFilter().filter(axis);
				axis = StringHelper.escapeJavaScript(axis);
				sb.append("{axis:\"").append(axis).append("\",value:").append(point.getValue()).append("}");
				if(j < (numOfPoints - 1)) {
					sb.append(",");
				}
			}

			sb.append("]");
			if(i < (numOfSeries - 1)) {
				sb.append(",");
			}
		}
		sb.append("]");
	}
}

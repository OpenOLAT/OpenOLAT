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
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 17 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PieChartComponentRenderer extends DefaultComponentRenderer {
	
	@Override
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {
		PieChartComponent poc = (PieChartComponent)source;
		String cmpId = poc.getDispatchID();
		List<PiePoint> serie = poc.getSerie();
		int layer = poc.getLayer();
		
		sb.append("<div id='d").append(cmpId).append("d3holder' class='radarChart' style='max-width:650px; max-height:650px; margin:0 auto;'></div>\n");
		sb.append("<script>\n")
		  .append("\"use strict\";")
		  .append("jQuery(function() {\n")
		  .append(" var render = function() {\n")
		  .append("  var placeholderWidth = jQuery('#d").append(cmpId).append("d3holder').width();\n")
		  .append("  var placeholderHeight = jQuery('#d").append(cmpId).append("d3holder').height();\n")
		  .append("  var placeholderSize = placeholderHeight > 20 ? Math.min(placeholderWidth, placeholderHeight) : placeholderWidth;\n")
		  .append("  var margin = 20;\n")
		  .append("  var dWidth = (placeholderSize - margin);\n")
		  .append("  var dHeight = dWidth;\n");

		sb.append("  jQuery('#d").append(cmpId).append("d3holder').ooPieChart({\n")
		  .append("    w: dWidth,\n")
		  .append("    h: dHeight,\n")
		  .append("    margin: margin,\n")
		  .append("    layer: ").append(layer).append(",\n");
		sb.append("    colors: ");
		renderColors(sb, serie);
		sb.append(",\n");
		sb.append("    values: ");
		renderValues(sb, serie);
		sb.append("\n")
		  .append("  });\n")
		  .append(" }\n")//end render function
		  .append(" render();\n")
		  .append("});\n")
		  .append("</script>");
	}
	
	private void renderColors(StringOutput sb, List<PiePoint> serie) {
		int numOfPoints = serie.size();
		
		sb.append("[");
		for(int i=0; i<numOfPoints; i++) {
			if(i > 0) {
				sb.append(",");
			}
			PiePoint point = serie.get(i);
			sb.append("'").append(point.getColor()).append("'");
		}
		sb.append("]");
	}
	
	private void renderValues(StringOutput sb, List<PiePoint> serie) {
		int numOfPoints = serie.size();
		
		sb.append("{");
		for(int i=0; i<numOfPoints; i++) {
			if(i > 0) {
				sb.append(",");
			}
			PiePoint point = serie.get(i);
			String category = Character.toString((char)(i + 97));
			sb.append(category).append(": ").append(point.getValue());
		}
		sb.append("}");
	}
}

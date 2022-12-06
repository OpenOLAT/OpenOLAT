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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 19 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SliderOverviewRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {
		SliderOverviewComponent soc = (SliderOverviewComponent)source;
		
		String id = soc.getFormDispatchId();
		
		sb.append("<div id='o_over").append(id).append("' class='o_slider_overview' data-oo-slider-min='").append(soc.getMinValue()).append("' data-oo-slider-max='").append(soc.getMaxValue()).append("'>");
		sb.append("<div class='o_slider_overview_line'></div>");
		
		double min = soc.getMinValue();
		double max = soc.getMaxValue();
		double size = max - min;// 100%
		List<SliderPoint> values = soc.getValues();
		int maxCount = getMaxCounts(values);
		int bubbleStep = maxCount > 1 ? (10 / (maxCount - 1)) : 0;

		Map<Double,Integer> counts = new HashMap<>();
		if(values != null && values.size() > 0) {
			for(SliderPoint val:values) {
				double value = val.getValue() - min;
				int count;
				if(counts.containsKey(value)) {
					count = counts.get(value).intValue() + 1;
					counts.put(value, count);
				} else {
					count = 0;
					counts.put(value, count);
				}

				int bubble = 10 + (bubbleStep * count);
				sb.append("<div class='o_slider_overview_point'")
				  .append(" style='width:").append(bubble).append("px; height:").append(bubble).append("px;")
				  .append(" z-index: ").append((50 - count)).append(";");
				if(StringHelper.containsNonWhitespace(val.getColor())) {
					sb.append(" background-color:").append(val.getColor()).append("; ");
				}
				sb.append(" border-radius:").append(bubble).append("px;'")
				  .append(" data-oo-slider-point-nr='").append(count).append("'")
				  .append(" data-oo-slider-point='").append(value).append("'> </div>");
			}
		}
		sb.append("</div>");
		
		sb.append("<script>\n")
		  .append("/* <![CDATA[ */ \n")
		  .append("jQuery(function() {\n")
		  .append(" var render = function() {\n")
		  .append("  var width = jQuery('#o_over").append(id).append("').width();\n")
		  .append("  var height = jQuery('#o_over").append(id).append("').height();\n")
		  .append("  var widthStep = width / ").append(size).append(";\n")
		  .append("  jQuery('#o_over").append(id).append(" .o_slider_overview_point').each(function(index, el) {\n")
		  .append("    var val = jQuery(el).data('oo-slider-point');\n")
		  .append("    var pointWidth = jQuery(el).width() / 2;\n")
		  .append("    var left = (val * widthStep) - pointWidth;\n")
		  .append("    var top = (height - jQuery(el).height()) / 2;\n")
		  .append("    jQuery(el).css({ 'left': left + 'px', 'top': top + 'px' });\n")
		  .append("  });\n")
		  .append(" };\n")
		  .append(" render();\n")
		  .append(" jQuery(window).on('resize', render);\n")
		  .append("});\n")
			  .append("/* ]]> */\n")
			  .append("</script>");
	}
	
	private int getMaxCounts(List<SliderPoint> values) {
		int maxCount = 0;
		Map<Double,Integer> valueMap = new HashMap<>();
		for(SliderPoint value:values) {
			int newCount;
			double val = value.getValue();
			if(valueMap.containsKey(val)) {
				newCount = valueMap.get(val).intValue() + 1;
			} else {
				newCount = 1;
			}
			valueMap.put(val, newCount);
			maxCount = Math.max(maxCount, newCount);
		}
		return maxCount;
	}
}

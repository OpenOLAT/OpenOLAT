/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
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
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 30 avr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SpeedometerComponentRenderer extends DefaultComponentRenderer {
	
	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {
		SpeedometerComponent soc = (SpeedometerComponent)source;
		String cmpId = soc.getDispatchID();

		sb.append("<div id='o_c").append(cmpId).append("' class='o_speedometer");
		if(StringHelper.containsNonWhitespace(soc.getValueCssClass())) {
			sb.append(" ").append(soc.getValueCssClass());
		} else {
			sb.append(" o_speedometer_infos");
		}
		if(StringHelper.containsNonWhitespace(soc.getElementCssClass())) {
			sb.append(" ").append(soc.getElementCssClass());
		}
		sb.append("'>");
		
		double val = soc.getValue();
		sb.append("<span class='o_speedometer_value'>").append(Math.round(val)).append("</span>");

		double maxValue = soc.getMaxValue();
		if(maxValue > 0.0d && val > 0.0d) {
			double rotation = (val / maxValue) * 180;
			double transposed = 180 - rotation;
			sb.append("<span class='o_speedometer_indicator' style='--var-speed:-").append(Math.round(transposed)).append("deg;'></span>");
		}
		sb.append("</div>");
	}
}

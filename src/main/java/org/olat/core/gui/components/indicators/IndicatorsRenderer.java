/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.core.gui.components.indicators;

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
 * Initial date: Oct 28, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class IndicatorsRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
			Translator translator, RenderResult renderResult, String[] args) {
		IndicatorsComponent cmp = (IndicatorsComponent) source;
		
		String elementId = cmp.getFormDispatchId();
		
		sb.append("<div class='o_indicators_container'>");
		sb.append("<div ");
		sb.append(" id='").append(elementId).append("'");
		sb.append(" class='o_indicators ");
		sb.append(cmp.getElementCssClass(), StringHelper.containsNonWhitespace(cmp.getElementCssClass()));
		sb.append("'");
		sb.append(">");
		
		Component keyIndicator = cmp.getKeyIndicator();
		if (keyIndicator != null && keyIndicator.isVisible()) {
			sb.append("<div class='o_indicators_key'>");
			renderer.render(keyIndicator, sb, null);
			keyIndicator.setDirty(false);
			sb.append("</div>");
		}
		
		if (cmp.getFocusIndicators() != null && !cmp.getFocusIndicators().isEmpty()) {
			if (keyIndicator != null && keyIndicator.isVisible()) {
				sb.append("<div class=\"o_indicators_separator\"></div>");
			}
			
			sb.append("<div class='o_indicators_focus'>");
			sb.append("<ul class='list-unstyled'>");
			for (Component indicator : cmp.getFocusIndicators()) {
				if (indicator.isVisible()) {
					sb.append("<li>");
					renderer.render(indicator, sb, null);
					indicator.setDirty(false);
					sb.append("</li>");
				}
			}
			sb.append("</ul>");
			sb.append("</div>");
		}
		
		sb.append("</div>");
		sb.append("</div>");
	}

}

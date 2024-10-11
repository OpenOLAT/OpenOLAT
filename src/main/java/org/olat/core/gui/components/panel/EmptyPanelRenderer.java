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
package org.olat.core.gui.components.panel;

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
 * Initial date: 24 sept. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EmptyPanelRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
			Translator translator, RenderResult renderResult, String[] args) {
		
		EmptyPanel panel = (EmptyPanel)source;
		
		sb.append("<div");
		if (!panel.isDomReplacementWrapperRequired() && !panel.isDomLayoutWrapper()) {
			sb.append(" id='o_c").append(panel.getDispatchID()).append("'");
		}
		sb.append(" class='o_empty_panel");
		if(StringHelper.containsNonWhitespace(panel.getElementCssClass())) {
			sb.append(" ").append(panel.getElementCssClass());
		}
		sb.append("'>");
		
		sb.append("<div class='o_empty_panel_icon'>")
		  .append("<i class='").append(panel.getIconCssClass()).append("' aria-hidden='true'> </i></div>");

		sb.append("<div class='o_empty_panel_content'><div>")
		  .append("<strong>").append(panel.getTitle()).append("</strong>");
		if(StringHelper.containsNonWhitespace(panel.getInformations())) {
			sb.append("<p>").append(panel.getInformations()).append("</p>");
		}
		sb.append("</div></div>");

		sb.append("</div>");
	}
}

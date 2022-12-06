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
 * Initial date: 28 Oct 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class IconPanelRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
			Translator translator, RenderResult renderResult, String[] args) {
		
		IconPanel panel = (IconPanel) source;
		
		sb.append("<div");
		if (!panel.isDomReplacementWrapperRequired()) {
			sb.append(" id='o_c").append(panel.getDispatchID()).append("'");
		}
		sb.append(" class='o_icon_panel");
		if(StringHelper.containsNonWhitespace(panel.getElementCssClass())) {
			sb.append(" ").append(panel.getElementCssClass());
		}
		sb.append("'");
		sb.append(">");
		
		// Icon column
		sb.append("<div class='o_icon_panel_icon_col'>");
		sb.append("<h4>");
		sb.append("<i class='");
		if (StringHelper.containsNonWhitespace(panel.getIconCssClass())) {
			sb.append(panel.getIconCssClass());
		} else {
			// Some space
			sb.append("o_icon o_icon-fw");
		}
		sb.append("'> </i>");
		sb.append("</h4>");
		sb.append("</div>");
		
		// Content column
		sb.append("<div class='o_icon_panel_content_col'>");
		// Header
		if (StringHelper.containsNonWhitespace(panel.getTitle()) || StringHelper.containsNonWhitespace(panel.getTagline())) {
			sb.append("<header>");
			if (StringHelper.containsNonWhitespace(panel.getTagline())) {
				sb.append("<p class='o_icon_panel_tagline'>");
				sb.append(panel.getTagline());
				sb.append("</p>");
			}
			if (StringHelper.containsNonWhitespace(panel.getTitle())) {
				sb.append("<h4>");
				sb.append(panel.getTitle());
				sb.append("</h4>");
			}
			sb.append("</header>");
		}
		
		// Content
		if (panel.getContent() != null && panel.getContent().isVisible()) {
			renderer.render(sb, panel.getContent(), args);
		}
		// Links
		if (!panel.getLinks().isEmpty()) {
			sb.append("<div class='o_icon_panel_links pull-right'>");
			for (Component link : panel.getLinks()) {
				if (link.isVisible()) {
					renderer.render(sb, link, args);
				}
			}
			sb.append("</div>");
		}
		
		sb.append("</div>");
		sb.append("</div>");
		
		panel.setDirty(false);
	}

}

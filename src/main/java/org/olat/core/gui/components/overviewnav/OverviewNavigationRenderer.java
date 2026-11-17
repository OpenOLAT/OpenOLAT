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
package org.olat.core.gui.components.overviewnav;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 *
 * Initial date: 16 Nov 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class OverviewNavigationRenderer extends DefaultComponentRenderer {
	
	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
			Translator translator, RenderResult renderResult, String[] args) {
		OverviewNavigation cmp = (OverviewNavigation)source;
		if (cmp.getGroups().stream().noneMatch(OverviewNavigationGroup::isVisible)) {
			return;
		}
		String id = "o_onav_" + cmp.getDispatchID();
		String panelId = cmp.getPanelId();
		
		sb.append("<div id=\"").append(id).append("\" class=\"o_overview_nav ")
		  .append(cmp.isExpanded() ? "o_expanded" : "o_collapsed").append("\">");
		
		sb.append("<button type=\"button\" class=\"btn btn-default o_overview_nav_menu\" aria-expanded=\"false\" aria-controls=\"")
		  .append(panelId).append("\">")
		  .append("<i class=\"o_icon o_icon_list\" aria-hidden=\"true\"></i> ")
		  .appendHtmlEscaped(cmp.getNavTranslator().translate("overview.nav.menu"))
		  .append(" <i class=\"o_icon o_icon_caret\" aria-hidden=\"true\"></i></button>");
		
		sb.append("<nav id=\"").append(panelId).append("\" class=\"o_overview_nav_panel\"");
		if (StringHelper.containsNonWhitespace(cmp.getAriaLabel())) {
			sb.append(" aria-label=\"").appendHtmlAttributeEscaped(cmp.getAriaLabel()).append("\"");
		}
		sb.append(">");
		int groupIndex = 0;
		for (OverviewNavigationGroup group : cmp.getGroups()) {
			if (group.isVisible()) {
				renderGroup(renderer, sb, cmp, group, id + "_g" + groupIndex++);
			}
		}
		sb.append("</nav>");
		
		sb.append("<div class=\"o_overview_nav_toggle\">");
		renderer.render(cmp.getToggleLink(), sb, null);
		sb.append("</div>");
		
		sb.append("<script>\"use strict\";o_overviewNav('").append(id).append("');</script>");
		sb.append("</div>");
	}
	
	private void renderGroup(Renderer renderer, StringOutput sb, OverviewNavigation cmp, OverviewNavigationGroup group, String headingId) {
		long visibleItems = group.getItems().stream().filter(item -> item.getLink().isVisible()).count();
		sb.append("<section class=\"o_overview_nav_group\" style=\"--o-onav-cols:").append((visibleItems + 1) / 2)
		  .append("\" aria-labelledby=\"").append(headingId).append("\">");
		sb.append("<h3 id=\"").append(headingId).append("\" class=\"o_overview_nav_heading\">")
		  .appendHtmlEscaped(group.getHeading()).append("</h3>");
		sb.append("<ul class=\"o_overview_nav_items\">");
		for (OverviewNavigationItem item : group.getItems()) {
			Link link = item.getLink();
			if (link.isVisible()) {
				sb.append("<li class=\"o_overview_nav_item\">");
				renderer.render(link, sb, null);
				if (cmp.isExpanded() && StringHelper.containsNonWhitespace(item.getDescription())) {
					sb.append("<span id=\"").append(cmp.getDescriptionId(item)).append("\" class=\"o_overview_nav_desc\">")
					  .appendHtmlEscaped(item.getDescription()).append("</span>");
				}
				sb.append("</li>");
			}
		}
		sb.append("</ul>");
		sb.append("</section>");
	}
	
}

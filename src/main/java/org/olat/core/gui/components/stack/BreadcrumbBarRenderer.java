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
package org.olat.core.gui.components.stack;

import java.util.List;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel.BreadcrumbBar;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;

/**
 * 
 * Initial date: 7 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BreadcrumbBarRenderer extends DefaultComponentRenderer {
	
	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		
		BreadcrumbBar bar = (BreadcrumbBar)source;
		BreadcrumbedStackedPanel panel = bar.getPanel();
		List<Link> breadCrumbs = panel.getBreadCrumbs();

		if (breadCrumbs.size() > panel.getInvisibleCrumb()) {
			String id = "o_c" + source.getDispatchID();
			sb.append("<div id='").append(id).append("' class='o_breadcrumb_bar o_breadcrumb'>")
			  .append("<ol class='breadcrumb'>");
	
			Link rootLink = panel.getRootLink();
			Link backLink = panel.getBackLink();
			int numOfCrumbs = breadCrumbs.size();
			if((rootLink.isVisible() || backLink.isVisible()) && numOfCrumbs > panel.getInvisibleCrumb()) {
				if (backLink.isVisible()) {
					sb.append("<li class='o_breadcrumb_back'>");
					backLink.getHTMLRendererSingleton().render(renderer, sb, backLink, ubu, translator, renderResult, args);
					sb.append("</li>");
				}
				if (rootLink.isVisible()) {
					sb.append("<li class='o_breadcrumb_root'>");
					rootLink.getHTMLRendererSingleton().render(renderer, sb, rootLink, ubu, translator, renderResult, args);
					sb.append("</li>");
				}
				
				// Button to open the menu
				sb.append("<li class='o_breadcrumb_more'");
				sb.append(" id='").append(id).append("_more'");
				sb.append(">");
				sb.append("<a href='#' class='dropdown-toggle'");
				sb.append(" data-toggle='dropdown'");
				sb.append(" role='button'");
				sb.append(" id='").append(id).append("_dd'");
				sb.append(">");
				String moreLabel = translator.translate("action.more");
				sb.append("<i class='o_icon o_icon_breadcrumb_more' aria-idden='true' title=\"").append(moreLabel).append("\">&nbsp;</i>");
				sb.append("<span class='sr-only'>").append(moreLabel).append("</span>");
				sb.append("</a>");
				// Menu
				sb.append("<ul class='o_breadcrumb_menu dropdown-menu' aria-describedby='").append(id).append("_dd'"); 
				sb.append(" id='").append(id).append("_dm'");
				sb.append(" role='menu'>");
				for (Link menuCrumb : breadCrumbs) {
					sb.append("<li class='o_breadcrumb_menu_item' role='menuitem'>");
					renderer.render(menuCrumb, sb, args);
					sb.append("</li>");
				}
				sb.append("</ul>");
				sb.append("</li>");
				
				for (int i = 0; i < breadCrumbs.size(); i++) {
					Link crumb = breadCrumbs.get(i);
					sb.append("<li class='o_breadcrumb_crumb o_display_none");
					if (i == 0) {
						sb.append(" o_first_crumb");
					} else if (i == breadCrumbs.size()-1) {
						crumb.setEnabled(false);
						sb.append(" o_last_crumb");
					}
					sb.append("' role='menuitem'>");
					
					String displayText = crumb.getCustomDisplayText();
					crumb.setTitle(displayText);
					crumb.setCustomDisplayText(Formatter.truncate(displayText, 40));
					renderer.render(crumb, sb, args);
					crumb.setTitle(null);
					crumb.setCustomDisplayText(displayText);
					
					sb.append("</li>");
					crumb.setEnabled(true);
				}
			}
			
			Link closeLink = panel.getCloseLink();
			if (closeLink.isVisible()) {
				sb.append("<li class='o_breadcrumb_close' role='menuitem'>");
				closeLink.getHTMLRendererSingleton().render(renderer, sb, closeLink, ubu, translator, renderResult, args);
				sb.append("</li>");				
			}
			sb.append("</ol>");
			
			sb.append("<script>\n")
			  .append("\"use strict\";\n")
			  .append("jQuery(function() {\n")
			  .append(" jQuery('#").append(id).append("').oobreadcrumb();\n")
			  .append("});\n")
			  .append("</script>");
		} else {
			sb.append("<div id='o_c").append(source.getDispatchID()).append("'>");
		}
		
		breadCrumbs.forEach(crumb -> crumb.setDirty(false));
		if(panel.getRootLink() != null) {
			panel.getRootLink().setDirty(false);
		}
		if(panel.getBackLink() != null) {
			panel.getBackLink().setDirty(false);
		}
		if(panel.getCloseLink() != null) {
			panel.getCloseLink().setDirty(false);
		}
		
		sb.append("</div>");
	}
}

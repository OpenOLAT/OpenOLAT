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
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel.Tool;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 25.03.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TooledStackedPanelRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		TooledStackedPanel panel = (TooledStackedPanel) source;
		List<Link> breadCrumbs = panel.getBreadCrumbs();
		List<Tool> tools = panel.getTools();
		
		boolean hasBreadcrumb = panel.isBreadcrumbEnabled() && breadCrumbs.size() > panel.getInvisibleCrumb();
		boolean hasToolbar = panel.isToolbarAutoEnabled() || panel.isToolbarEnabled();
		
		// panel div
		String mainCssClass = panel.getCssClass();
		sb.append("<div id='o_c").append(source.getDispatchID()).append("' class='")
			.append("o_with_toolbar ", hasToolbar)
			.append("o_with_breadcrumb ", hasBreadcrumb)
			.append(mainCssClass, mainCssClass != null)
			.append("'>");
		
		if((panel.isBreadcrumbEnabled() && breadCrumbs.size() > panel.getInvisibleCrumb()) || (!tools.isEmpty() && panel.isToolbarEnabled())) {
			sb.append("<div id='o_main_toolbar' class='o_toolbar");
			if((panel.isToolbarAutoEnabled() || panel.isToolbarEnabled()) && panel.hasVisibleTool(Align.segment)) {
				sb.append(" o_toolbar_with_segments");
			}
			sb.append("'>");

			if(hasBreadcrumb) {	
				renderer.render(panel.getBreadcrumbBar(), sb, args);
			}
			
			if (hasToolbar) {
				renderer.render(panel.getToolBar(), sb, args);
			}
			sb.append("</div>"); // o_toolbar
		}
		
		Component navigationCmp = panel.getNavigationComponent();
		boolean navigationCtrlActive = panel.getNavigationBindController() == null
				|| panel.getNavigationBindController() == panel.getLastController();
		if(navigationCmp != null && navigationCmp.isVisible() && navigationCtrlActive) {
			sb.append("<div class='o_toolbar_navigation'>");
			URLBuilder cubu = ubu.createCopyFor(navigationCmp);
			navigationCmp.getHTMLRendererSingleton().render(renderer, sb, navigationCmp, cubu, translator, renderResult, args);
			navigationCmp.setDirty(false);
			sb.append("</div>");
		}
		if(StringHelper.containsNonWhitespace(panel.getMessage())) {
			sb.append("<div class='o_toolbar_message ");
			if(StringHelper.containsNonWhitespace(panel.getMessageCssClass())) {
				sb.append(panel.getMessageCssClass());
			}
			sb.append("'>").append(panel.getMessage()).append("</div>");
		}
		
		// always render it, as an empty container if necessary
		Component messageCmp = panel.getMessagePanel();
		renderer.render(messageCmp, sb, args);
		
		Component toRender = panel.getContent();
		if(toRender != null) {
			renderer.render(sb, toRender, args);
		}

		sb.append("</div>"); // end of panel div
	}
}
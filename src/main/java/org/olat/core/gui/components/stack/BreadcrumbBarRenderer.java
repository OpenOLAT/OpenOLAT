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
			sb.append("<div id='o_c").append(source.getDispatchID()).append("' class='o_breadcrumb'>")
			  .append("<ol class='breadcrumb'>");
	
			Link backLink = panel.getBackLink();
			int numOfCrumbs = breadCrumbs.size();
			if(backLink.isVisible() && numOfCrumbs > panel.getInvisibleCrumb()) {
				sb.append("<li class='o_breadcrumb_back'>");
				backLink.getHTMLRendererSingleton().render(renderer, sb, backLink, ubu, translator, renderResult, args);
				sb.append("</li>");
				
				for(Link crumb:breadCrumbs) {
					sb.append("<li>");
					renderer.render(crumb, sb, args);
					sb.append("</li>");
				}
			}
			backLink.setDirty(false);
			
			Link closeLink = panel.getCloseLink();
			if (closeLink.isVisible()) {
				sb.append("<li class='o_breadcrumb_close'>");
				closeLink.getHTMLRendererSingleton().render(renderer, sb, closeLink, ubu, translator, renderResult, args);
				sb.append("</li>");				
			}
			closeLink.setDirty(false);
			sb.append("</ol>");
		} else {
			sb.append("<div id='o_c").append(source.getDispatchID()).append("'>");
		}
		sb.append("</div>");
	}
}

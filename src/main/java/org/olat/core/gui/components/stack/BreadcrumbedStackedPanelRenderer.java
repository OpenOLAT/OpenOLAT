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
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 25.03.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BreadcrumbedStackedPanelRenderer extends DefaultComponentRenderer {

	@Override
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		BreadcrumbedStackedPanel panel = (BreadcrumbedStackedPanel) source;
		List<Link> breadCrumbs = panel.getBreadCrumbs();
		if(breadCrumbs.size() > 1) {
			String mainCssClass = panel.getCssClass();
			sb.append("<div id='o_main_toolbar' class='b_clearfix ").append(mainCssClass, mainCssClass != null).append("'>")
			  .append("<div class='b_breadcumb_path'>")
			  .append("<ul>");
			
			Link backLink = panel.getBackLink();
			if(backLink.isVisible()) {
				sb.append("<li class='b_breadcumb_back'>");
				backLink.getHTMLRendererSingleton().render(renderer, sb, backLink, ubu, translator, renderResult, args);
				sb.append("</li>");
			}
			
			int numOfCrumbs = breadCrumbs.size();
			for(int i=0; i<numOfCrumbs; i++) {
				sb.append("<li class='").append("b_first", i==0).append("b_last", i==(numOfCrumbs-1)).append("'><span>");
				renderer.render(breadCrumbs.get(i), sb, args);
				sb.append("</span></li>");
			}
			sb.append("</ul>");
			
			Link closeLink = panel.getCloseLink();
			if(closeLink.isVisible()) {
				sb.append("<div class='b_breadcumb_close'>");
				closeLink.getHTMLRendererSingleton().render(renderer, sb, closeLink, ubu, translator, renderResult, args);
				sb.append("</div>");
			}
			sb.append("</div></div>");
		}
		
		Component toRender = panel.getContent();
		if(toRender != null) {
			renderer.render(sb, toRender, args);
		}
	}
}
/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 

package org.olat.core.gui.components.tabbedpane;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.NameValuePair;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.RenderingState;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * @author Felix Jost
 */
public class TabbedPaneRenderer implements ComponentRenderer {


	@Override
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {
		TabbedPane tb = (TabbedPane)source;
		int cnt = tb.getTabCount();
		if (cnt == 0) return; // nothing to render
		
		int selPane = tb.getSelectedPane();
		boolean iframePostEnabled = renderer.getGlobalSettings().getAjaxFlags().isIframePostEnabled();
		 		
		sb.append("<div id=\"o_c").append(tb.getDispatchID()).append("\" class='o_tabbed_pane'>");
		sb.append("<ul class='nav nav-tabs");
		String css = tb.getElementCssClass();
		if (StringHelper.containsNonWhitespace(css)) {
			sb.append(" ").append(css);
		}
		sb.append("' role='navigation'>");
		for (int i = 0; i < cnt; i++) {
			if(tb.isHideDisabledTab() && !tb.isEnabled(i)) {
				continue;
			}
			
			String tabName = tb.getDisplayNameAt(i);
			// Render active tab as non clickable, passive tabs with link
			sb.append("<li");
			if (i != selPane && cnt > 1) {
				if (tb.isEnabled(i)) {
					sb.append("><a ");
					ubu.buildHrefAndOnclick(sb, null, iframePostEnabled, tb.isDirtyCheck(), true, new NameValuePair(TabbedPane.PARAM_PANE_ID, String.valueOf(i)));
					sb.append(">").append(tabName).append("</a></li>");				
				} else {
					// disabled panels can not be clicked, but for layout reason needs still a a href
					sb.append(" class='disabled'><a href='javascript:;' title='")
					  .append(StringHelper.escapeHtml(tb.getCompTrans().translate("disabled"))).append("'>")
					  .append(tabName).append("</a></li>");
				}
			} else {
				sb.append(" class='active'><a href='javascript:;'>").append(tabName).append("</a></li>");
			}
		}
		sb.append("</ul>");

		// now let the selected component render itself
		Component paneToRender = tb.getTabAt(selPane);
		sb.append("<div class='o_tabbed_pane_content'>");
		if (paneToRender != null) {
			renderer.render(sb, paneToRender, null);
		}
		sb.append("</div></div>");
	}
	
	@Override
	public void renderHeaderIncludes(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderingState rstate) {
		TabbedPane tp = (TabbedPane)source;
		int cnt = tp.getTabCount();
		if (cnt > 0 && tp.getSelectedPane() < cnt) {
			Component paneToRender = tp.getTabAt(tp.getSelectedPane());
			// delegate header rendering to the selected pane
			if(paneToRender != null) {
				renderer.renderHeaderIncludes(sb, paneToRender, rstate);
			}
		}
	}

	@Override
	public void renderBodyOnLoadJSFunctionCall(Renderer renderer, StringOutput sb, Component source, RenderingState rstate) {
		TabbedPane tp = (TabbedPane)source;
		int cnt = tp.getTabCount();
		if (cnt > 0 && tp.getSelectedPane() < cnt) {
			Component paneToRender = tp.getTabAt(tp.getSelectedPane());
			//	delegate js rendering to the selected pane
			if(paneToRender != null) {
				renderer.renderBodyOnLoadJSFunctionCall(sb, paneToRender, rstate);
			}
		}
	}
}

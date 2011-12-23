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

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.control.winmgr.AJAXFlags;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.RenderingState;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 *  Description:<br>
 * @author Felix Jost
 */
public class TabbedPaneRenderer implements ComponentRenderer {
/**
 * 
 *
 */
	public TabbedPaneRenderer() {
    //
	}

	/**
   * 
	 * @see org.olat.core.gui.render.ui.ComponentRenderer#render(org.olat.core.gui.render.Renderer, org.olat.core.gui.render.StringOutput, org.olat.core.gui.components.Component, org.olat.core.gui.render.URLBuilder, org.olat.core.gui.translator.Translator, org.olat.core.gui.render.RenderResult, java.lang.String[])
	 */
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {
		TabbedPane tb = (TabbedPane)source;
		int cnt = tb.getTabCount();
		if (cnt == 0) return; // nothing to render
		
		int selPane = tb.getSelectedPane();
		
		boolean iframePostEnabled = renderer.getGlobalSettings().getAjaxFlags().isIframePostEnabled();
		 		
		sb.append("<div class=\"b_tabbedpane_wrapper\"");
		if (args != null) {
			// expecting one args, 1st: width of container, e.g "150" or "70%"
			sb.append("  style=\"width: ").append(args[0]).append("\"");
		}
		sb.append("><div class=\"b_tabbedpane_tabs\"><ul>");
		for (int i = 0; i < cnt; i++) {
			String tabName = tb.getDisplayNameAt(i);
			// Render active tab as non clickable, passive tabs with link
			sb.append("<li class=\"");
			// first / last tab
			if (i == 0 ) sb.append(" b_first");
			if (i == (cnt-1) ) sb.append(" b_last");
			// tab counter
			sb.append(" b_item_").append(i + 1);
			
			if (i != selPane && cnt > 1) {
				if (tb.isEnabled(i)) {
					sb.append("\"><div><a href=\"");
					ubu.buildURI(sb, new String[]{ TabbedPane.PARAM_PANE_ID }, new String[]{ String.valueOf(i) }, iframePostEnabled? AJAXFlags.MODE_TOBGIFRAME : AJAXFlags.MODE_NORMAL);
					sb.append("\" onclick=\"return o2cl()\"");
					if (iframePostEnabled) {
						ubu.appendTarget(sb);
					}
					sb.append(">").append(tabName).append("</a></div></li>");
										
				} else {
					// disabled panels can not be clicked, but for layout reason needs still a a href
					sb.append(" b_disabled\"><div><a href=\"#\" title=\"").append(StringEscapeUtils.escapeHtml(translator.translate("disabled"))).append("\">").append(tabName).append("</a></div></li>");
				}
			}
			else {
				sb.append(" b_active\"><div><strong>").append(tabName).append("</strong></div></li>");
			}
		}
		sb.append("</ul></div>");
		
		sb.append("<div class=\"b_tabbedpane_content b_clearfix\"");
		if (args != null && args.length == 2) {
			String height = args[1]; 
			sb.append(" style=\"height:\"").append(height).append("\"");
		}
		sb.append("><div class=\"b_tabbedpane_content_inner b_floatscrollbox\">");

		
		// now let the selected component render itself
		Component paneToRender = tb.getTabAt(selPane);
		if (paneToRender == null) throw new RuntimeException("a tabbed pane must not be null, but a component");
		renderer.render(sb, paneToRender, null);
		sb.append("</div></div></div>");
	}
	/**
   * 
	 * @see org.olat.core.gui.render.ui.ComponentRenderer#renderHeaderIncludes(org.olat.core.gui.render.Renderer, org.olat.core.gui.render.StringOutput, org.olat.core.gui.components.Component, org.olat.core.gui.render.URLBuilder, org.olat.core.gui.translator.Translator)
	 */
	public void renderHeaderIncludes(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderingState rstate) {
		TabbedPane tp = (TabbedPane)source;
		int cnt = tp.getTabCount();
		if (cnt > 0) {
			Component toRender = tp.getTabAt(tp.getSelectedPane());
			// delegate header rendering to the selected pane
			renderer.renderHeaderIncludes(sb, toRender, rstate);
		}
	}

	/**
   * 
	 * @see org.olat.core.gui.render.ui.ComponentRenderer#renderBodyOnLoadJSFunctionCall(org.olat.core.gui.render.Renderer, org.olat.core.gui.render.StringOutput, org.olat.core.gui.components.Component)
	 */
	public void renderBodyOnLoadJSFunctionCall(Renderer renderer, StringOutput sb, Component source, RenderingState rstate) {
		TabbedPane tp = (TabbedPane)source;
		int cnt = tp.getTabCount();
		if (cnt > 0) {
			Component toRender = tp.getTabAt(tp.getSelectedPane());
			//	delegate js rendering to the selected pane
			renderer.renderBodyOnLoadJSFunctionCall(sb, toRender, rstate);
		}
	}
	

}

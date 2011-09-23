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
* Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.core.gui.components.tabbedpane;

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
public class TabbedPaneScreenreaderRenderer implements ComponentRenderer {
/**
 * 
 *
 */
	public TabbedPaneScreenreaderRenderer() {
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
		// (cnt > 0) render tabs 
		int selPane = tb.getSelectedPane();
		boolean iframePostEnabled = renderer.getGlobalSettings().getAjaxFlags().isIframePostEnabled();
		
		// add overview info for screenreader
		Translator comptrans = tb.getCompTrans();
		sb.append(comptrans.translate("sr.intro", new String[] {String.valueOf(cnt)}));
		for (int i = 0; i < cnt; i++) {
			String tabName = tb.getDisplayNameAt(i);
			if (!tb.isEnabled(i)) {
				// if a pane is not enabled
				sb.append(comptrans.translate("sr.tab.notenabled", new String[] {tabName}));
			} else if (i == selPane) {
				// mark active pane
				sb.append(comptrans.translate("sr.tab.active", new String[] {tabName}));
			} else {
				// normal, enabled, but not active pane
				sb.append("<a href=\"");
				ubu.buildURI(sb, new String[]{ TabbedPane.PARAM_PANE_ID }, new String[]{ String.valueOf(i) }, iframePostEnabled? AJAXFlags.MODE_TOBGIFRAME : AJAXFlags.MODE_NORMAL);
				sb.append("\" onclick=\"return o2cl()\"");
				if (iframePostEnabled) {
					ubu.appendTarget(sb);
				}
				sb.append(">");
				sb.append(comptrans.translate("sr.tab.normal", new String[] {tabName}));				
				sb.append("</a>");
			}
		}
		sb.append(comptrans.translate("sr.introcontent"));
		// now let the selected component render itself
		Component paneToRender = tb.getTabAt(selPane);
		if (paneToRender == null) throw new RuntimeException("a tabbed pane must not be null, but a component");
		renderer.render(sb, paneToRender, null);
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

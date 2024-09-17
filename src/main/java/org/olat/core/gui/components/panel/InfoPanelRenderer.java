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
import org.olat.core.gui.components.form.flexible.impl.NameValuePair;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 21 déc. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InfoPanelRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
			Translator translator, RenderResult renderResult, String[] args) {
		InfoPanel panel = (InfoPanel)source;
		String title = panel.getTitle();
		String informations = panel.getInformations();
		String cid = "o_c" + panel.getDispatchID();
		boolean collapsed = panel.isCollapsed();
		
		sb.append("<div id='").append(cid).append("' class='o_info_with_icon");
		if(StringHelper.containsNonWhitespace(panel.getElementCssClass())) {
			sb.append(" ").append(panel.getElementCssClass());
		}
		sb.append("'>");
		
		if(StringHelper.containsNonWhitespace(title)) {		
			sb.append("<a id='").append(cid).append("_button' class='o_collapse_title").append(collapsed ? " collapsed" : "").append("' role='button' data-toggle='collapse' data-target='#").append(cid).append("_infos' aria-expanded='").append(collapsed ? "false" : "true").append("' aria-controls='").append(cid).append("_infos'>")
			  .append("<h4>").appendHtmlEscaped(title).append("</h4> <i class='o_icon o_icon_lg o_icon_details_expand'> </i>")
			  .append("</a>");
			//TODO a11y title/sr-only 
		}
		
		sb.append("<div id='").append(cid).append("_infos' class='collapse ").append("in", !collapsed).append("' aria-expanded='").append(collapsed ? "false" : "true").append("'>")
		  .appendScanned(informations)
		  .append("</div>");
		
		sb.append("<script>\n")
		  .append("\"use strict\";\n")
		  .append("jQuery('#").append(cid).append("_infos').on('shown.bs.collapse', function () {\n")
		  .append(" jQuery('#").append(cid).append("_button>i').removeClass('o_icon_details_collaps').addClass('o_icon_details_expand');\n");
		ubu.getXHRNoResponseEvent(sb, null, new NameValuePair(VelocityContainer.COMMAND_ID, "expanded"));
		sb.append("\n")
		  .append("});\n")
		  .append("jQuery('#").append(cid).append("_infos').on('hidden.bs.collapse', function () {\n")
		  .append(" jQuery('#").append(cid).append("_button>i').removeClass('o_icon_details_expand').addClass('o_icon_details_collaps');\n");
		ubu.getXHRNoResponseEvent(sb, null, new NameValuePair(VelocityContainer.COMMAND_ID, "collapsed"));
		sb.append("});\n")
		  .append("</script>")
		  .append("</div>");
	}
}

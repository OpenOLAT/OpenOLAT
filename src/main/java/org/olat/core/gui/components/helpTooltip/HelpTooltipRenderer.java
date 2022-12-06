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
package org.olat.core.gui.components.helpTooltip;

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
 * Initial date: 05.11.2015<br>
 * @author gnaegi, gnaegi@frentix.com, http://www.frentix.com
 *
 */
public class HelpTooltipRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source,
			URLBuilder ubu, Translator translator, RenderResult renderResult,
			String[] args) {

		HelpTooltip helpTooltip = (HelpTooltip)source;
		String helpUrl = helpTooltip.getHelpUrl();
		String helpText = helpTooltip.getHelpText();
		
		if (helpUrl == null && helpText == null) {
			// e.g. help is disabled, don't do anything
			return;
		}
		
		// We provide our own dom relacementd ID 
		String id = helpTooltip.getDispatchID();
		sb.append(" <span id='").append(id).append("'>");

		// Same style as in form help text rendering (See SimpleLableText)
		String helpIconId = "o_fh" + id;
		
		// Wrap tooltip with link to external url if available
		if (helpUrl != null) {
			sb.append("<a href=\"").append(helpUrl).append("\" target='_blank'>"); 
		}
		
		// Tooltip is bound to this icon
		sb.append("<i class='o_chelp_tooltip o_icon o_icon-fw o_icon_help' id='").append(helpIconId).append("'></i>");
		if (helpUrl != null) {
			sb.append("</a>");
		}			
		// Attach bootstrap tooltip handler to help icon
		sb.append("<script>jQuery(function () {jQuery('#").append(helpIconId).append("').tooltip({placement:\"top\",container: \"body\",html:true,title:\"");
		if (helpText != null) {
			sb.append(StringHelper.escapeJavaScript(helpText));
		}
		if (helpUrl != null) {
			if (helpText != null) {
				// append Apacer between custom and generic link text
				sb.append("<br />");
			}
			sb.append(translator.translate("help.tooltip.link", new String[]{"<i class='o_icon o_icon-fw o_icon_help'></i>"}));					
		}
		sb.append("\"});})</script>");		

		// Done, close dome replacmeent wrapper
		sb.append("</span>");
		
	}
}

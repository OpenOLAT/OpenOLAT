/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.user.ui.organisation.structure;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.NameValuePair;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * Initial date: Mai 07, 2025
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class OrgStructureRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source,
								URLBuilder ubu, Translator translator,
								RenderResult renderResult, String[] args) {
		OrgStructureComponent comp = (OrgStructureComponent)source;
		
		String elementId = comp.getDispatchID();
		
		sb.append("<button");
		sb.append(" id=\"").append(elementId).append("\"");
		sb.append(" type=\"button\"");
		sb.append(" class=\"btn btn-default o_organisation_tree_button o_can_have_focus o_button_printed ");
		sb.append("\""); // class
		sb.append(" onfocus=\"o_info.lastFormFocusEl='").append(elementId).append("';\" ");
		if(comp.isEnabled()) {
			sb.append("onmousedown=\"o_info.preventOnchange=true;\" onmouseup=\"o_info.preventOnchange=false;\" onclick=\"");
				ubu.buildXHREvent(sb, "", false, true, new NameValuePair(VelocityContainer.COMMAND_ID, "show-tree"));
				sb.append("\" ");
		} else {
			sb.append("disabled=\"true\"");
		}
		sb.append(">"); // button
		sb.append("<i class=\"o_icon o_icon-fw o_icon_levels\"></i> ");
		sb.append("<span class=\"sr-only\">").append(comp.getCompTranslator().translate("show.organisation.tree")).append("</span>");
		sb.append("</button>");
	}
}

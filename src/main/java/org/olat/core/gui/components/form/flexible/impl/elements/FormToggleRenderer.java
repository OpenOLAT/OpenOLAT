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
package org.olat.core.gui.components.form.flexible.impl.elements;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
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
 * Initial date: 9 juin 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FormToggleRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
			Translator translator, RenderResult renderResult, String[] args) {
		FormToggleComponent cmp = (FormToggleComponent)source;
		String elementId = cmp.getFormDispatchId();
		
		sb.append("<button type=\"button\" role=\"switch\" ");
		if (elementId != null) {
			sb.append("id=\"").append(elementId).append("\" ");
		}
		sb.append("class=\"o_button_toggle o_can_have_focus");
		if(StringHelper.containsNonWhitespace(cmp.getElementCssClass())) {
			sb.append(" ").append(cmp.getElementCssClass());
		}
		sb.append("\" ");
		if(StringHelper.containsNonWhitespace(cmp.getAriaLabelledBy())) {
			sb.append("aria-labelledby=\"").append(cmp.getAriaLabelledBy()).append("\" ");
		}
		if(StringHelper.containsNonWhitespace(cmp.getAriaLabel())) {
			sb.append("aria-label=\"").append(cmp.getAriaLabel()).append("\" ");
		}
		sb.append("onmousedown=\"o_info.preventOnchange=true;\" onmouseup=\"o_info.preventOnchange=false;\" onclick=\"");
		if(cmp.getFormItem() != null) {
			sb.append(FormJSHelper.getJSFnCallFor(cmp.getFormItem().getRootForm(), elementId, 1)).append(";").append("\" ");
		} else {
			ubu.buildXHREvent(sb, "", false, true, new NameValuePair(VelocityContainer.COMMAND_ID, "toggle"));
			sb.append("\" ");
		}
		sb.append("onfocus=\"o_info.lastFormFocusEl='").append(elementId).append("';\" ")
		  .append("aria-checked=\"").append("true", "false", cmp.isOn()).append("\" ")
		  .append(">");

		if(!cmp.isOn()) {
			sb.append("<i class=\"o_icon o_icon_toggle\"></i> ");
		}
		String onText = cmp.getToggleOnText();
		sb.append("<span class=\"o_on\">").append(onText, "&nbsp;&nbsp;", StringHelper.containsNonWhitespace(onText)).append("</span>");
		String offText = cmp.getToggleOffText();
		sb.append("<span class=\"o_off\">").append(offText, "&nbsp;&nbsp;", StringHelper.containsNonWhitespace(onText)).append("</span>");
		if(cmp.isOn()) {
			sb.append(" <i class=\"o_icon o_icon_toggle\"></i>");
		}
		sb.append("</button>");
	}
}

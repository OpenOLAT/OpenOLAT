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

import java.util.Arrays;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.form.flexible.elements.FormToggle.Presentation;
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
		
		sb.append("<button type=\"button\" ");
		sb.append("role=\"").append(getRole(cmp)).append("\" ");
		if (elementId != null) {
			sb.append("id=\"").append(elementId).append("\" ");
		}
		sb.append("class=\"");
		sb.append("o_can_have_focus ");
		sb.append("o_toggle_block ", args != null && Arrays.asList(args).contains("vertical"));
		sb.append(getCssClass(cmp));
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
		if(cmp.isEnabled()) {
			sb.append("onmousedown=\"o_info.preventOnchange=true;\" onmouseup=\"o_info.preventOnchange=false;\" onclick=\"");
			if(cmp.getFormItem() != null) {
				sb.append(FormJSHelper.getJSFnCallFor(cmp.getFormItem().getRootForm(), elementId, 1)).append(";").append("\" ");
			} else {
				ubu.buildXHREvent(sb, "", false, true, new NameValuePair(VelocityContainer.COMMAND_ID, "toggle"));
				sb.append("\" ");
			}
		} else {
			sb.append("disabled=\"true\" aria-disabled=\"true\" ");
		}
		sb.append("onfocus=\"o_info.lastFormFocusEl='").append(elementId).append("';\" ")
		  .append("aria-checked=\"").append("true", "false", cmp.isOn()).append("\" ")
		  .append(">");

		if (Presentation.SWITCH == cmp.getPresentation()) {
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
		} else if (Presentation.CHECK == cmp.getPresentation()) {
			if (cmp.isOn()) {
				sb.append("<i class=\"o_icon o_icon_check\"></i>");
			} else {
				sb.append("<i class=\"o_icon o_icon_toggle_check_off\"></i>");
			}
		} else if (Presentation.BUTTON_LARGE == cmp.getPresentation()
				|| Presentation.BUTTON == cmp.getPresentation()
				|| Presentation.BUTTON_SMALL == cmp.getPresentation()
				|| Presentation.BUTTON_XSMALL == cmp.getPresentation()) {
			// Currently the style is optimized for the course node confirmation.
			// Maybe the default colors have to be changed for a wider range of use.
			if (cmp.isOn()) {
				sb.append("<i class=\"o_icon o_icon_toggle_button_on\"></i>");
				if (StringHelper.containsNonWhitespace(cmp.getToggleOnText())) {
					sb.append("<span class=\"o_on\">").append(cmp.getToggleOnText()).append("</span>");
				}
			} else {
				sb.append("<i class=\"o_icon o_icon_toggle_button_off\"></i>");
				if (StringHelper.containsNonWhitespace(cmp.getToggleOffText())) {
					sb.append("<span class=\"o_off\">").append(cmp.getToggleOffText()).append("</span>");
				}
			}
		}
		sb.append("</button>");
	}
	
	private String getRole(FormToggleComponent cmp) {
		if (Presentation.SWITCH == cmp.getPresentation()) {
			return "switch";
		}
		return "checkbox";
	}

	private String getCssClass(FormToggleComponent cmp) {
		if (Presentation.CHECK == cmp.getPresentation()) {
			if (cmp.isOn()) {
				return "o_toggle_check btn btn-primary o_button_printed";
			}
			return "o_toggle_check btn btn-default o_button_printed";
		} else if (Presentation.BUTTON_LARGE == cmp.getPresentation()) {
			return "o_toggle_button btn btn-lg";
		} else if (Presentation.BUTTON == cmp.getPresentation()) {
			return "o_toggle_button btn";
		} else if (Presentation.BUTTON_SMALL == cmp.getPresentation()) {
			return "o_toggle_button btn btn-sm";
		} else if (Presentation.BUTTON_XSMALL == cmp.getPresentation()) {
			return "o_toggle_button btn btn-xs";
		} 
		return "o_button_toggle";
	}
}

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

import java.util.List;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.form.flexible.elements.IconSelectorElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * Initial date: 2024-02-26<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class IconSelectorRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
								Translator translator, RenderResult renderResult, String[] args) {
		IconSelectorComponent iconSelectorComponent = (IconSelectorComponent) source;
		IconSelectorElementImpl iconSelectorEl = iconSelectorComponent.getFormItem();
		List<IconSelectorElement.Icon> icons = iconSelectorEl.getIcons();
		IconSelectorElement.Icon selectedIcon = iconSelectorEl.getIcon();
		boolean iconSelected = selectedIcon != null;
		String iconCssClass = iconSelected ? selectedIcon.iconCssClass() : "";
		String iconName = iconSelected ? selectedIcon.translatedName() : "";
		String iconId = iconSelected ? selectedIcon.id() : "";

		String inputId = iconSelectorEl.getFormDispatchId();
		String dropdownId = inputId + "_D";
		String buttonId = inputId + "_B";

		sb.append("<div class='o_icon_selector_wrapper'>");

		sb.append("<div id='").append(dropdownId).append("'").append(" class='button-group dropdown").append("'>");

		sb.append("<button style='padding-left: ").append(iconSelected ? "32" : "12")
				.append("px;' class='btn btn-default dropdown-toggle o_icon_selector_button o_can_have_focus o_button_printed' type='button' ")
				.append("id='").append(buttonId).append("' data-toggle='dropdown' ")
				.append("aria-haspopup='true' aria-expanded='true'")
				.append("onfocus=\"o_info.lastFormFocusEl='").append(buttonId).append("';\" ")
				.append(!iconSelectorEl.isEnabled() ? " disabled" : "").append(">")
				.append("<i class='o_icon o_icon_selector_icon ")
				.append(iconCssClass, iconSelected)
				.append("'></i>")
				.append("<span>")
				.append(iconName, iconSelected)
				.append("</span>")
				.append("<i class='o_icon o_icon-fw o_icon_caret o_icon_selector_caret'></i>")
				.append("<input type='hidden' id='").append(inputId).append("' name='").append(inputId)
				.append("' value='").append(iconId, iconSelected).append("'>")
				.append("</button>");

		sb.append("<ul class='dropdown-menu o_icon_selector_dropdown")
				.append(" o_drop_up", iconSelectorEl.isDropUp())
				.append("' aria-labelledby='").append(buttonId).append("'>");

		for (IconSelectorElement.Icon icon : icons) {
			sb.append("<li data-icon='").append(icon.id()).append("'");
			if (selectedIcon != null && icon.id().equals(selectedIcon.id())) {
				sb.append(" class='o_selected'");
			}
			sb.append(">");
			sb.append("<a tabindex='0' role='button' aria-pressed='false' class='dropdown-item o_icon_selector_link' ");

			String updateFunctionCall = "o_is_set_icon('" + icon.id() + "', '" +
					icon.translatedName() + "', '" +
					buttonId + "', '" +
					inputId + "', '" +
					dropdownId + "', '" +
					iconSelectorEl.getRootForm().getDispatchFieldId() + "', '" +
					icon.iconCssClass() + "'); ";
			String submitFunctionCall = getRawJSFor(iconSelectorEl.getRootForm(), inputId, iconSelectorEl.getAction()) + "; ";
			submitFunctionCall = "setTimeout(function() { " + submitFunctionCall + " }, 0);";
			String functionCall = updateFunctionCall + submitFunctionCall;
			sb.append("onclick=\"").append(functionCall).append("\" ");
			sb.append("onKeyDown=\"if (event.keyCode === 32) { ").append(functionCall)
					.append("jQuery('#").append(dropdownId).append("').trigger('click.bs.dropdown'); }\" ");
			sb.append("onKeyPress=\"if (event.keyCode === 13) ").append(functionCall).append("\"");

			sb.append(">");

			sb.append("<i class='o_icon o_icon_selector_icon ")
					.append(icon.iconCssClass())
					.append("'>").append("</i>");
			sb.append("<span>").append(icon.translatedName()).append("</span>");

			sb.append("</a>");
			sb.append("</li>");
		}

		sb.append("</ul>");
		sb.append("</div>");
		sb.append("</div>");

		sb.append("<script>");
		sb.append("function o_is_set_icon(iconId, text, buttonId, inputId, dropdownId, formDispatchFieldId, cssClass) {\n");
		sb.append("  const hiddenInput = jQuery('#' + inputId);\n");
		sb.append("  let oldIconId = hiddenInput.val();\n");
		sb.append("  let oldCssClass = hiddenInput.attr('cssClass'); \n");
		sb.append("  hiddenInput.val(iconId);\n");
		sb.append("  hiddenInput.attr('cssClass', cssClass); \n");
		sb.append("  jQuery('#' + buttonId).css('padding-left', '32px');\n");
		sb.append("  jQuery('#' + buttonId + ' i').removeClass(oldCssClass).addClass(cssClass);\n");
		sb.append("  jQuery('#' + buttonId + ' span').text(text);\n");
		sb.append("  jQuery('#' + dropdownId + ' li[data-icon=\"' + oldIconId + '\"]').removeClass('o_selected');\n");
		sb.append("  jQuery('#' + dropdownId + ' li[data-icon=\"' + iconId + '\"]').addClass('o_selected');\n");
		sb.append("  setFlexiFormDirty(formDispatchFieldId);\n");
		sb.append("}\n");
		sb.append("</script>");
	}

	private String getRawJSFor(Form form, String formItemDispatchId, int action) {
		StringBuilder eventHandlers = FormJSHelper.getRawJSFor(form, formItemDispatchId, action, false, null, null);
		String onKeyword = "onchange=";
		int onPos = eventHandlers.indexOf(onKeyword);
		if (onPos != -1) {
			return eventHandlers.substring(onPos + onKeyword.length() + 1, eventHandlers.length() - 1);
		}
		return "";
	}
}

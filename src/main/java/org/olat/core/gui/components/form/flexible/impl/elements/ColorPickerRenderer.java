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
import org.olat.core.gui.components.form.flexible.elements.ColorPickerElement;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.form.flexible.impl.NameValuePair;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;

/**
 * Initial date: 2023-03-23<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class ColorPickerRenderer extends DefaultComponentRenderer {
	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
								Translator translator, RenderResult renderResult, String[] args) {

		ColorPickerComponent colorPickerComponent = (ColorPickerComponent) source;
		ColorPickerElementImpl colorChooserElement = colorPickerComponent.getFormItem();
		List<ColorPickerElement.Color> colors = colorChooserElement.getColors();
		ColorPickerElement.Color selectedColor = colorChooserElement.getColor();

		String dropdownId = "o_" + CodeHelper.getRAMUniqueID();
		String buttonId = "o_" + CodeHelper.getRAMUniqueID();
		String inputId = "o_cp" + colorPickerComponent.getDispatchID();

		sb.append("<div class='o_color_picker_wrapper'>");

		sb.append("<div id='").append(dropdownId).append("'");
		sb.append(" class='button-group dropdown");
		sb.append("'>");

		sb.append("<button style='padding-left: ").append(selectedColor != null ? "32" : "12")
				.append("px;' class='btn btn-default dropdown-toggle o_color_picker_button' type='button' ")
				.append("id='").append(buttonId).append("' data-toggle='dropdown' ")
				.append("aria-haspopup='true' aria-expanded='true'>");
		if (selectedColor != null) {
			sb.append("<i class='o_color_picker_colored_area o_icon o_icon_lg o_icon_fa6_a ")
					.append("o_color_background o_color_contrast_border o_color_text_on_background o_color_")
					.append(selectedColor.getId()).append("'></i>");
			sb.append("<span>").append(selectedColor.getText()).append("</span>");
		} else {
			sb.append("<i class='o_icon o_icon_lg ")
					.append("o_color_background o_color_contrast_border o_color_text_on_background'></i>");
			if (StringHelper.containsNonWhitespace(colorChooserElement.getNonSelectedText())) {
				sb.append("<span>").append(colorChooserElement.getNonSelectedText()).append("</span>");
			} else {
				sb.append("<span></span>");
			}
		}
		sb.append("<i class='o_icon o_icon-fw o_icon_caret o_color_picker_icon'></i>");

		// In standard form submission mode, this element needs to act as a regular input element, which we achieve
		// with a hidden <input> element.
		if (!colorChooserElement.isAjaxOnlyMode()) {
			sb.append("<input type='hidden' id='").append(inputId).append("' name='").append(inputId)
					.append("' value='").append(selectedColor != null ? selectedColor.getId() : "").append("'>");
		}

		sb.append("</button>");

		sb.append("<ul class='dropdown-menu o_color_picker_dropdown' aria-labelledby='").append(buttonId).append("'>");

		for (ColorPickerElement.Color color : colors) {
			sb.append("<li data-color='").append(color.getId()).append("'");
			if (selectedColor != null && color.getId().equals(selectedColor.getId())) {
				sb.append(" class='o_selected'");
			}
			sb.append(">");
			sb.append("<a class='dropdown-item o_color_picker_link' ");

			if (colorChooserElement.isAjaxOnlyMode()) {
				// In ajax-only mode, selecting sends an event to the server:

				sb.append("onclick=\"");
				sb.append(FormJSHelper.getXHRFnCallFor(colorChooserElement.getRootForm(),
						colorPickerComponent.getFormDispatchId(), 1, false, false,
						false, new NameValuePair("colorId", color.getId())));
				sb.append(";\"");
			} else {
				// In form submission mode, selecting updates the UI and the hidden input element:

				// o_cp_set_color(color.id, color.text, buttonId, inputId, dropdownId, formDispatchFieldId);
				sb.append("onclick=\"o_cp_set_color('").append(color.getId()).append("', '");
				sb.append(color.getText()).append("', '");
				sb.append(buttonId).append("', '");
				sb.append(inputId).append("', '");
				sb.append(dropdownId).append("', '");
				sb.append(colorChooserElement.getRootForm().getDispatchFieldId()).append("');\"");
			}
			sb.append(">");

			sb.append("<i class='o_color_picker_colored_area o_icon o_icon_lg o_icon_fa6_a ")
					.append("o_color_background o_color_contrast_border o_color_text_on_background o_color_")
					.append(color.getId()).append("'>").append("</i>");
			sb.append("<span>").append(color.getText()).append("</span>");

			sb.append("</a>");
			sb.append("</li>");
		}

		sb.append("</ul>"); // dropdown-menu
		sb.append("</div>"); // dropdown
		sb.append("</div>"); // o_color_picker_wrapper

		if (!colorChooserElement.isAjaxOnlyMode()) {
			sb.append("<script>");
			sb.append("function o_cp_set_color(colorId, text, buttonId, inputId, dropdownId, formDispatchFieldId) {\n");
			sb.append("  const hiddenInput = jQuery('#' + inputId);\n");
			sb.append("  const oldColorId = hiddenInput.val();\n");
			sb.append("  hiddenInput.val(colorId);\n");
			sb.append("  jQuery('#' + buttonId).css('padding-left', '32px');\n");
			sb.append("  jQuery('#' + buttonId + ' i.o_color_background').removeClass('o_color_' + oldColorId).addClass('o_color_picker_colored_area o_icon_fa6_a o_color_' + colorId);\n");
			sb.append("  jQuery('#' + buttonId + ' span').text(text);\n");
			sb.append("  jQuery('#' + dropdownId + ' li[data-color=\"' + oldColorId + '\"]').removeClass('o_selected');\n");
			sb.append("  jQuery('#' + dropdownId + ' li[data-color=\"' + colorId + '\"]').addClass('o_selected');\n");
			sb.append("  setFlexiFormDirty(formDispatchFieldId);\n");
			sb.append("}\n");
			sb.append("</script>");
		}
	}
}

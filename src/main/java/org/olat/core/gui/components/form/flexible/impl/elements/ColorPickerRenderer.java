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

import org.olat.core.commons.services.color.ColorValidator;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.form.flexible.elements.ColorPickerElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.form.flexible.impl.NameValuePair;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * The button of the colour picker in the form: the selected colour, the
 * hidden field of the value and the hidden field of the pick. A click (or
 * Arrow Down, Arrow Up) sends a request to the element, which opens the
 * colours in a callout, see {@link ColorPickerPopupRenderer}.
 * 
 * Initial date: 2023-03-23<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class ColorPickerRenderer extends DefaultComponentRenderer {

	static final String UNSELECT_KEY = "__color_unselect__";

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
								Translator translator, RenderResult renderResult, String[] args) {

		ColorPickerComponent colorPickerComponent = (ColorPickerComponent) source;
		ColorPickerElementImpl colorPickerEl = colorPickerComponent.getFormItem();
		ColorPickerElement.Color selectedColor = colorPickerEl.getColor();

		String inputId = colorPickerEl.getFormDispatchId();
		String buttonId = inputId + ColorPickerElementImpl.BUTTON_SUFFIX;
		boolean enabled = colorPickerEl.isEnabled();

		sb.append("<div class='o_color_picker_wrapper'>");
		// "true" after the user picked a colour, see ColorPickerElement.isColorPicked()
		String pickedFieldName = inputId + ColorPickerElementImpl.PICKED_SUFFIX;
		sb.append("<input type='hidden' id='").append(pickedFieldName).append("' name='").append(pickedFieldName)
				.append("' value='").append(colorPickerEl.isColorPicked() ? "true" : "").append("'>");

		boolean open = enabled && colorPickerEl.isCalloutOpen();
		sb.append("<button style='padding-left: ").append(selectedColor != null ? "32" : "12")
				.append("px;' class='btn btn-default o_color_picker_button o_button_printed' type='button' ")
				.append("id='").append(buttonId).append("' ")
				.append("aria-haspopup='dialog' aria-expanded='").append(open).append("' ");
		if (open) {
			sb.append("aria-controls='").append(inputId).append(ColorPickerElementImpl.POPUP_SUFFIX).append("' ");
		}
		if (colorPickerEl.hasLabel()) {
			sb.append("aria-labelledby='o_cl").append(colorPickerComponent.getDispatchID()).append(" ").append(buttonId).append("' ");
		} else if (StringHelper.containsNonWhitespace(colorPickerEl.getButtonAriaLabel())) {
			// no label: "Color" and the current colour
			sb.append("aria-label='").append(attribute(colorPickerEl.getButtonAriaLabel())).append("' ");
		}
		if (enabled) {
			// with the values of the form: the callout redraws the form below it
			String openCall = getXHRCall(colorPickerEl.getRootForm(), inputId, ColorPickerElementImpl.CMD_OPEN);
			sb.append("onmousedown=\"o_info.preventOnchange=true;\" onmouseup=\"o_info.preventOnchange=false;\" ")
					.append("onclick=\"").append(openCall).append("; return false;\" ")
					.append("onkeydown=\"if (event.key === 'ArrowDown' || event.key === 'ArrowUp') { event.preventDefault(); ")
					.append(openCall).append("; }\"");
		} else {
			sb.append("disabled");
		}
		sb.append(">");
		if (selectedColor != null) {
			sb.append("<i class='o_color_picker_colored_area o_icon o_icon_a ")
					.append("o_color_background o_color_contrast_border o_color_text_on_background ").append(cssClass(selectedColor.cssClass()))
					.append("' aria-hidden='true'></i>");
			sb.append("<span>").append(text(selectedColor.translatedName())).append("</span>");
		} else {
			sb.append("<i class='o_icon ")
					.append("o_color_background o_color_contrast_border o_color_text_on_background' aria-hidden='true'></i>");
			if (StringHelper.containsNonWhitespace(colorPickerEl.getNonSelectedText())) {
				sb.append("<span>").append(text(colorPickerEl.getNonSelectedText())).append("</span>");
			} else {
				sb.append("<span></span>");
			}
		}
		sb.append("<i class='o_icon o_icon-fw o_icon_caret o_color_picker_icon' aria-hidden='true'></i>");

		sb.append("<input type='hidden' id='").append(inputId).append("' name='").append(inputId)
				.append("' value='")
				.append(selectedColor != null ? attribute(selectedColor.id()) : "")
				.append("'>");

		sb.append("</button>");
		sb.append("</div>"); // o_color_picker_wrapper

		if (colorPickerEl.getResetButtonId() != null) {
			sb.append("<script>\n");
			sb.append("\"use strict\";\n");
			sb.append("jQuery('#").append(colorPickerEl.getResetButtonId()).append("').on('click', function() { jQuery('#").append(buttonId).append("').focus(); });\n");
			sb.append("</script>");
		}
	}

	/**
	 * @return The CSS classes if they are valid class names, else no class:
	 * 		the colour renders without colour
	 */
	static String cssClass(String cssClass) {
		if (cssClass == null || cssClass.isBlank()) {
			return "";
		}
		String valid = ColorValidator.validCssClasses(cssClass, "colour picker");
		return valid == null ? "" : valid;
	}

	/**
	 * @return The text escaped for the content of an element
	 */
	static String text(String text) {
		return text == null ? "" : StringHelper.escapeHtml(text);
	}

	/**
	 * @return The value escaped for an attribute in single or double quotes
	 */
	static String attribute(String value) {
		return value == null ? "" : StringHelper.escapeHtml(value).replace("'", "&#39;");
	}

	/**
	 * @param form The form of the picker
	 * @param inputId The dispatch id of the picker
	 * @param cmd The command, see ColorPickerElementImpl
	 * @return The JavaScript call of a request to the picker which submits
	 * 		the values of the form (also of a multipart form, without its files)
	 */
	static String getXHRCall(Form form, String inputId, String cmd) {
		return FormJSHelper.getXHRFnCallFor(form, inputId, 0, false, false, true, true,
				new NameValuePair(ColorPickerElementImpl.CMD_PARAM, cmd));
	}
}

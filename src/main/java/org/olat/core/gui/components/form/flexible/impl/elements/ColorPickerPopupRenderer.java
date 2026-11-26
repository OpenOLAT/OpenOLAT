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
package org.olat.core.gui.components.form.flexible.impl.elements;

import java.util.List;
import java.util.Objects;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.form.flexible.elements.ColorPickerElement;
import org.olat.core.gui.components.form.flexible.elements.ColorPickerElement.Color;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * The colours of the colour picker in the callout: the list (one entry per
 * line, the colour square and the name) or the grid
 * ({@link ColorPickerGridRenderer}).
 * <p>
 * The callout is outside of the form. A pick writes the value into the
 * hidden fields of the button in the form, marks the form as changed and
 * sends a request with the values of the form to the element. The element
 * closes the callout and fires ONCHANGE if it listens to it, the focus
 * returns to the button. A pick of the selected colour only closes the
 * callout.
 * <p>
 * Keys of the list: Arrow Down and Arrow Up move to the next and the previous
 * entry, Home and End to the first and the last entry, Enter and Space pick
 * the entry. The selected entry, else the first, has the focus when the
 * callout opens. The keys of the grid: see {@link ColorPickerGridRenderer}.
 * Escape and a click outside close the callout (the callout itself).
 *
 * Initial date: 26 Nov 2026<br>
 * @author gnaegi, https://www.frentix.com
 *
 */
public class ColorPickerPopupRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
			Translator translator, RenderResult renderResult, String[] args) {
		ColorPickerPopupComponent popupCmp = (ColorPickerPopupComponent) source;
		render(sb, popupCmp.getColorPickerElement());
	}

	void render(StringOutput sb, ColorPickerElementImpl colorPickerEl) {
		String inputId = colorPickerEl.getFormDispatchId();
		String popupId = inputId + ColorPickerElementImpl.POPUP_SUFFIX;
		String buttonId = inputId + ColorPickerElementImpl.BUTTON_SUFFIX;
		boolean grid = colorPickerEl.getPresentation() == ColorPickerElement.Presentation.GRID;

		sb.append("<div id='").append(popupId).append("' class='o_color_picker_popup ")
				.append(grid ? "o_color_picker_popup_grid" : "o_color_picker_popup_list").append("'>");
		if (grid) {
			ColorPickerComponent component = (ColorPickerComponent) colorPickerEl.getComponent();
			new ColorPickerGridRenderer(component, colorPickerEl, popupId, buttonId).render(sb);
		} else {
			renderList(sb, colorPickerEl, popupId);
		}
		sb.append("</div>");

		renderScript(sb, colorPickerEl, inputId, popupId, grid);
	}

	private void renderList(StringOutput sb, ColorPickerElementImpl colorPickerEl, String popupId) {
		List<Color> colors = colorPickerEl.getColors();
		Color selectedColor = colorPickerEl.getColor();
		String selectedId = selectedColor == null ? null : selectedColor.id();
		// the selected entry has the focus when the callout opens, else the first
		String focusId = selectedId;
		if (focusId == null || colors.stream().noneMatch(color -> color.id().equals(selectedId))) {
			focusId = colors.isEmpty() ? null : colors.get(0).id();
		}

		sb.append("<ul class='o_color_picker_list'>");
		String openGroup = null;
		int numOfGroups = 0;
		for (Color color : colors) {
			String group = StringHelper.containsNonWhitespace(color.group()) ? color.group() : null;
			if (!Objects.equals(group, openGroup)) {
				if (openGroup != null) {
					renderGroupEnd(sb);
				}
				if (group != null) {
					numOfGroups++;
					renderGroupStart(sb, popupId + "_g" + numOfGroups, group);
				}
				openGroup = group;
			}
			boolean selected = color.id().equals(selectedId);
			renderItem(sb, color.id(), color.translatedName(), color.cssClass(), selected, color.id().equals(focusId));
		}
		if (openGroup != null) {
			renderGroupEnd(sb);
		}

		if (colorPickerEl.isAllowUnselect() && selectedColor != null && StringHelper.containsNonWhitespace(colorPickerEl.getNonSelectedText())) {
			renderItem(sb, ColorPickerRenderer.UNSELECT_KEY, colorPickerEl.getNonSelectedText(), "o_color_unselect", false, false);
		}
		sb.append("</ul>");
	}

	/**
	 * A group is a nested list in an item of the list: the gap, the heading
	 * and a group with the heading as name. The arrow keys reach the entries
	 * of the nested list too.
	 */
	private void renderGroupStart(StringOutput sb, String headingId, String group) {
		sb.append("<li class='o_color_picker_group'>");
		sb.append("<div role='group' aria-labelledby='").append(headingId).append("'>");
		sb.append("<div id='").append(headingId).append("' class='dropdown-header o_color_picker_group_header'>")
				.append(ColorPickerRenderer.text(group)).append("</div>");
		sb.append("<ul class='o_color_picker_group_list'>");
	}

	private void renderGroupEnd(StringOutput sb) {
		sb.append("</ul></div></li>");
	}

	private void renderItem(StringOutput sb, String colorId, String colorName, String colorCss, boolean selected, boolean focus) {
		sb.append("<li");
		if (selected) {
			sb.append(" class='o_selected'");
		}
		sb.append(">");
		sb.append("<a tabindex='0' role='button' aria-pressed='").append(selected).append("' class='o_color_picker_link'")
				.append(" autofocus", focus)
				.append(" data-color='").append(ColorPickerRenderer.attribute(colorId)).append("'>");
		sb.append("<i class='o_color_picker_colored_area o_icon o_icon_a ")
				.append("o_color_background o_color_contrast_border o_color_text_on_background ").append(ColorPickerRenderer.cssClass(colorCss))
				.append("' aria-hidden='true'></i>");
		sb.append("<span>").append(ColorPickerRenderer.text(colorName)).append("</span>");
		sb.append("</a>");
		sb.append("</li>");
	}

	/**
	 * The script of the callout: the pick, the keys and the focus. It reads
	 * the colour from the attribute of the entry, no value of an entry is
	 * written into the script. A pick of the selected colour closes the
	 * callout without a change: the form is not marked as changed and the
	 * element fires no event.
	 */
	private void renderScript(StringOutput sb, ColorPickerElementImpl colorPickerEl, String inputId, String popupId, boolean grid) {
		Form form = colorPickerEl.getRootForm();

		sb.append("<script>\n");
		sb.append("\"use strict\";\n");
		sb.append("(function() {\n");
		sb.append("  const popup = jQuery('#").append(popupId).append("');\n");
		sb.append("  let picked = false;\n");
		sb.append("  const pick = function(item, event) {\n");
		sb.append("    if (picked) { return; }\n");
		sb.append("    picked = true;\n");
		sb.append("    const colorId = item.attr('data-color');\n");
		sb.append("    const input = jQuery('#").append(inputId).append("');\n");
		sb.append("    const same = colorId === (input.val() || '").append(ColorPickerRenderer.UNSELECT_KEY).append("');\n");
		// the pick of the selected colour confirms a preselected colour, see ColorPickerElement.isColorPicked()
		sb.append("    jQuery('#").append(inputId).append(ColorPickerElementImpl.PICKED_SUFFIX).append("').val('true');\n");
		sb.append("    let sent;\n");
		sb.append("    if (same) {\n");
		sb.append("      sent = ").append(ColorPickerRenderer.getXHRCall(form, inputId, ColorPickerElementImpl.CMD_CLOSE)).append(";\n");
		sb.append("    } else {\n");
		sb.append("      input.val(colorId);\n");
		sb.append("      setFlexiFormDirty('").append(form.getDispatchFieldId()).append("');\n");
		sb.append("      sent = ").append(ColorPickerRenderer.getXHRCall(form, inputId, ColorPickerElementImpl.CMD_PICK)).append(";\n");
		sb.append("    }\n");
		// a busy window drops the request: the user can pick again
		sb.append("    if (sent === false) { picked = false; }\n");
		sb.append("  };\n");
		sb.append("  popup.on('click', '[data-color]', function(event) {\n");
		sb.append("    event.preventDefault();\n");
		sb.append("    pick(jQuery(this), event);\n");
		sb.append("  });\n");
		if (grid) {
			ColorPickerGridRenderer.renderKeys(sb);
		} else {
			renderListKeys(sb);
		}
		// the dialog of the callout opens after this script: the focus goes to
		// the entry with autofocus when the callout is visible, up to 2 seconds
		// (a timeout, not an animation frame, which a hidden window does not run)
		sb.append("  const show = function(attempt) {\n");
		sb.append("    if (!popup.closest('dialog[open]').length || popup[0].getClientRects().length === 0) {\n");
		sb.append("      if (attempt < 80) { setTimeout(function() { show(attempt + 1); }, 25); }\n");
		sb.append("      return;\n");
		sb.append("    }\n");
		if (grid) {
			ColorPickerGridRenderer.renderScrollToTabStop(sb);
		}
		sb.append("    const start = popup.find('[autofocus]')[0];\n");
		sb.append("    if (start && document.activeElement !== start) {\n");
		sb.append("      start.focus({ preventScroll: true });\n");
		sb.append("    }\n");
		sb.append("  };\n");
		sb.append("  show(0);\n");
		sb.append("})();\n");
		sb.append("</script>");
	}

	private void renderListKeys(StringOutput sb) {
		sb.append("  popup.on('keydown', '.o_color_picker_link', function(event) {\n");
		sb.append("    const links = popup.find('.o_color_picker_link');\n");
		sb.append("    const index = links.index(this);\n");
		sb.append("    let target = null;\n");
		sb.append("    switch (event.key) {\n");
		sb.append("      case 'ArrowDown': target = links.eq(Math.min(index + 1, links.length - 1)); break;\n");
		sb.append("      case 'ArrowUp': target = links.eq(Math.max(index - 1, 0)); break;\n");
		sb.append("      case 'Home': target = links.first(); break;\n");
		sb.append("      case 'End': target = links.last(); break;\n");
		sb.append("      case 'Enter':\n");
		sb.append("      case ' ':\n");
		sb.append("        event.preventDefault();\n");
		sb.append("        pick(jQuery(this), event);\n");
		sb.append("        return;\n");
		sb.append("      default: return;\n");
		sb.append("    }\n");
		sb.append("    event.preventDefault();\n");
		sb.append("    target.trigger('focus');\n");
		sb.append("  });\n");
	}
}

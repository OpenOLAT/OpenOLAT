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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.olat.core.commons.services.color.Swatch;
import org.olat.core.gui.components.form.flexible.elements.ColorPickerElement.Color;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.util.StringHelper;

/**
 * The grid presentation of the colour picker
 * ({@link org.olat.core.gui.components.form.flexible.elements.ColorPickerElement.Presentation#GRID})
 * in the callout: a grid of colour cells and the button "no colour" below
 * the grid.
 * <p>
 * The grid follows the WAI-ARIA grid pattern (as the grid of a date picker
 * dialog): role grid, one role row per variant, one role gridcell per colour.
 * One cell is in the tab order (roving tabindex): the selected cell, else
 * the first cell. It has the focus when the callout opens. The arrow keys
 * move between the cells, Home and End to the first and last cell of the
 * row (with Ctrl of the grid), Page Up and Page Down to the first and last
 * cell of the column. Enter and Space pick the cell. Escape closes the
 * callout. The selected cell has aria-selected true. A cell has no visible
 * name: its accessible name is the name of the entry.
 *
 * Initial date: 25 Nov 2026<br>
 * @author gnaegi, https://www.frentix.com
 *
 */
class ColorPickerGridRenderer {

	private final ColorPickerComponent component;
	private final ColorPickerElementImpl colorPickerEl;
	private final String popupId;
	private final String buttonId;

	ColorPickerGridRenderer(ColorPickerComponent component, ColorPickerElementImpl colorPickerEl,
			String popupId, String buttonId) {
		this.component = component;
		this.colorPickerEl = colorPickerEl;
		this.popupId = popupId;
		this.buttonId = buttonId;
	}

	/**
	 * Renders the grid and the button "no colour", without the script.
	 */
	void render(StringOutput sb) {
		Color selectedColor = colorPickerEl.getColor();
		List<Column> columns = toColumns(colorPickerEl.getColors());
		List<String> rows = toRows(columns);
		String selectedId = selectedColor == null ? null : selectedColor.id();
		String tabStopId = getTabStopId(columns, rows, selectedId);

		sb.append("<div class='o_color_picker_grid_scroll'>");

		// The headings of the groups, e.g. "Theme colors" above the theme colours,
		// one span per column. The cells of a group name the heading as description.
		String[] headingIds = new String[columns.size()];
		boolean hasGroups = columns.stream().anyMatch(column -> column.group() != null);
		if (hasGroups) {
			sb.append("<div class='o_color_picker_grid_groups' aria-hidden='true'>");
			int numOfGroups = 0;
			String headingId = null;
			for (int i = 0; i < columns.size(); i++) {
				Column column = columns.get(i);
				sb.append("<span class='o_color_picker_grid_group").append(" o_color_picker_grid_gap", isGapBefore(columns, i)).append("'");
				if (isGroupStart(columns, i)) {
					numOfGroups++;
					headingId = popupId + "_g" + numOfGroups;
					sb.append(" id='").append(headingId).append("'>").append(ColorPickerRenderer.text(column.group()));
				} else {
					sb.append(">");
				}
				sb.append("</span>");
				headingIds[i] = column.group() == null ? null : headingId;
			}
			sb.append("</div>");
		}

		sb.append("<div class='o_color_picker_grid' role='grid' ");
		if (colorPickerEl.hasLabel()) {
			sb.append("aria-labelledby='o_cl").append(component.getDispatchID()).append("'");
		} else if (StringHelper.containsNonWhitespace(colorPickerEl.getPopupAriaLabel())) {
			// no label: "Color", not the current colour of the button
			sb.append("aria-label='").append(ColorPickerRenderer.attribute(colorPickerEl.getPopupAriaLabel())).append("'");
		} else {
			sb.append("aria-labelledby='").append(buttonId).append("'");
		}
		sb.append(">");
		for (String row : rows) {
			sb.append("<div class='o_color_picker_grid_row' role='row'>");
			for (int i = 0; i < columns.size(); i++) {
				Column column = columns.get(i);
				renderCell(sb, column.variants().get(row), isGapBefore(columns, i), headingIds[i],
						selectedId, tabStopId);
			}
			sb.append("</div>");
		}
		sb.append("</div>"); // grid
		sb.append("</div>"); // scroll

		if (colorPickerEl.isAllowUnselect() && StringHelper.containsNonWhitespace(colorPickerEl.getNonSelectedText())) {
			boolean pressed = selectedColor == null;
			sb.append("<button type='button' class='btn btn-default btn-block o_color_picker_grid_unselect")
					.append(" o_selected", pressed)
					.append("' aria-pressed='").append(pressed)
					.append("' data-color='").append(ColorPickerRenderer.UNSELECT_KEY).append("'>")
					.append(ColorPickerRenderer.text(colorPickerEl.getNonSelectedText()))
					.append("</button>");
		}
	}

	private void renderCell(StringOutput sb, Color color, boolean gap, String headingId, String selectedId, String tabStopId) {
		if (color == null) {
			// no entry for this colour and variant: an empty cell keeps the columns
			sb.append("<div class='o_color_picker_cell o_color_picker_cell_empty")
					.append(" o_color_picker_grid_gap", gap)
					.append("' role='gridcell'></div>");
			return;
		}

		boolean selected = color.id().equals(selectedId);
		boolean tabStop = color.id().equals(tabStopId);
		String cssClass = ColorPickerRenderer.cssClass(color.cssClass());
		sb.append("<div class='o_color_picker_cell");
		if (!cssClass.isEmpty()) {
			sb.append(" ").append(cssClass);
		}
		sb.append(" o_color_picker_grid_gap", gap)
				.append(" o_selected", selected)
				.append("' role='gridcell' tabindex='").append(tabStop ? "0" : "-1").append("'")
				.append(" autofocus", tabStop)
				.append(" aria-selected='").append(selected)
				.append("' aria-label='").append(ColorPickerRenderer.attribute(color.translatedName())).append("'");
		if (headingId != null) {
			sb.append(" aria-describedby='").append(headingId).append("'");
		}
		sb.append(" data-color='").append(ColorPickerRenderer.attribute(color.id()))
				.append("'><span aria-hidden='true'>a</span></div>");
	}

	/**
	 * The keys of the grid. The code runs in the script of the callout
	 * ({@link ColorPickerPopupRenderer}) and uses its variables popup and pick.
	 */
	static void renderKeys(StringOutput sb) {
		sb.append("  const grid = popup.find('.o_color_picker_grid');\n");
		sb.append("  const cells = function() { return grid.find('.o_color_picker_cell[data-color]'); };\n");
		sb.append("  const moveTo = function(cell) {\n");
		sb.append("    if (cell && cell.length) {\n");
		sb.append("      cells().attr('tabindex', '-1');\n");
		sb.append("      cell.attr('tabindex', '0').trigger('focus');\n");
		sb.append("    }\n");
		sb.append("  };\n");
		sb.append("  grid.on('keydown', '.o_color_picker_cell[data-color]', function(event) {\n");
		sb.append("    const cell = jQuery(this);\n");
		sb.append("    const rows = grid.children('[role=row]');\n");
		sb.append("    const row = rows.index(cell.parent());\n");
		sb.append("    const col = cell.index();\n");
		sb.append("    const at = function(r, c) { return rows.eq(r).children().eq(c).filter('[data-color]'); };\n");
		// the next cell in a direction, empty cells are skipped
		sb.append("    const step = function(dr, dc) {\n");
		sb.append("      for (let r = row + dr, c = col + dc; r >= 0 && r < rows.length && c >= 0 && c < rows.eq(r).children().length; r += dr, c += dc) {\n");
		sb.append("        const next = at(r, c);\n");
		sb.append("        if (next.length) { return next; }\n");
		sb.append("      }\n");
		sb.append("      return null;\n");
		sb.append("    };\n");
		sb.append("    const column = function() { return rows.map(function(r) { return at(r, col).get(); }); };\n");
		sb.append("    let target = null;\n");
		sb.append("    switch (event.key) {\n");
		sb.append("      case 'ArrowRight': target = step(0, 1); break;\n");
		sb.append("      case 'ArrowLeft': target = step(0, -1); break;\n");
		sb.append("      case 'ArrowDown': target = step(1, 0); break;\n");
		sb.append("      case 'ArrowUp': target = step(-1, 0); break;\n");
		sb.append("      case 'Home': target = event.ctrlKey ? cells().first() : cell.parent().children('[data-color]').first(); break;\n");
		sb.append("      case 'End': target = event.ctrlKey ? cells().last() : cell.parent().children('[data-color]').last(); break;\n");
		sb.append("      case 'PageUp': target = column().first(); break;\n");
		sb.append("      case 'PageDown': target = column().last(); break;\n");
		sb.append("      case 'Enter':\n");
		sb.append("      case ' ':\n");
		sb.append("        event.preventDefault();\n");
		sb.append("        pick(cell, event);\n");
		sb.append("        return;\n");
		sb.append("      default: return;\n");
		sb.append("    }\n");
		sb.append("    event.preventDefault();\n");
		sb.append("    moveTo(target);\n");
		sb.append("  });\n");
	}

	/**
	 * A grid that scrolls (narrow screen) shows the selected cell. The code
	 * runs in the script of the callout when the callout is visible.
	 */
	static void renderScrollToTabStop(StringOutput sb) {
		sb.append("    const scroller = popup.find('.o_color_picker_grid_scroll')[0];\n");
		sb.append("    const cell = popup.find('.o_color_picker_cell[autofocus]')[0];\n");
		sb.append("    if (scroller && cell && scroller.scrollWidth > scroller.clientWidth) {\n");
		sb.append("      scroller.scrollLeft = cell.offsetLeft - scroller.offsetLeft - (scroller.clientWidth - cell.offsetWidth) / 2;\n");
		sb.append("    }\n");
	}

	/**
	 * @return The tab stop of the grid: the selected cell, else the first
	 * 		cell of the first row
	 */
	private static String getTabStopId(List<Column> columns, List<String> rows, String selectedId) {
		String firstId = null;
		for (String row : rows) {
			for (Column column : columns) {
				Color color = column.variants().get(row);
				if (color != null) {
					if (color.id().equals(selectedId)) {
						return selectedId;
					}
					if (firstId == null) {
						firstId = color.id();
					}
				}
			}
		}
		return firstId;
	}

	private static boolean isGroupStart(List<Column> columns, int index) {
		Column column = columns.get(index);
		return column.group() != null && (index == 0 || !Objects.equals(column.group(), columns.get(index - 1).group()));
	}

	/**
	 * @return true if the column starts another group than the column before
	 */
	private static boolean isGapBefore(List<Column> columns, int index) {
		return index > 0 && !Objects.equals(columns.get(index).group(), columns.get(index - 1).group());
	}

	/**
	 * @return The columns in the order of the first entry of each colour.
	 * 		The column of a swatch key is its hue, e.g. "blue" for "blue-soft";
	 * 		an entry that is no swatch key has its own column and the variant
	 * 		solid. A second entry for the same colour and variant is dropped.
	 */
	static List<Column> toColumns(List<Color> colors) {
		Map<String,Column> columns = new LinkedHashMap<>();
		if (colors == null) {
			return new ArrayList<>();
		}
		for (Color color : colors) {
			if (color == null || color.id() == null) {
				continue;
			}
			Swatch swatch = Swatch.fromKey(color.id());
			String columnKey = swatch == null ? color.id() : swatch.hue();
			String variant = swatch == null ? Swatch.SOLID : swatch.variant();
			String group = StringHelper.containsNonWhitespace(color.group()) ? color.group() : null;
			Column column = columns.computeIfAbsent(columnKey, key -> new Column(key, group, new LinkedHashMap<>()));
			column.variants().putIfAbsent(variant, color);
		}
		return new ArrayList<>(columns.values());
	}

	/**
	 * @return The variants of the columns, light to dark ({@link Swatch#VARIANTS})
	 */
	static List<String> toRows(List<Column> columns) {
		List<String> rows = new ArrayList<>();
		for (String variant : Swatch.VARIANTS) {
			if (columns.stream().anyMatch(column -> column.variants().containsKey(variant))) {
				rows.add(variant);
			}
		}
		return rows;
	}

	/**
	 * A column of the grid: the entries of one colour per variant.
	 *
	 * @param key The hue or theme colour, or the id of an entry that is no swatch key
	 * @param group The group of the first entry of the colour, null for none
	 * @param variants The entries per variant
	 */
	record Column(String key, String group, Map<String,Color> variants) {
		//
	}
}

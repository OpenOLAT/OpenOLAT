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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.gui.components.form.flexible.impl.elements;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;
import org.olat.core.gui.components.form.flexible.elements.ColorPickerElement.Color;
import org.olat.core.gui.components.form.flexible.elements.ColorPickerElement.Presentation;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.render.StringOutput;

/**
 * Checks the button of the colour picker in the form ({@link ColorPickerRenderer})
 * and the colours in the callout ({@link ColorPickerPopupRenderer}): the list
 * without and with groups, the grid ({@link ColorPickerGridRenderer}) with
 * rows, cells, names, selection, the button "no colour", the focus when the
 * callout opens, the request of a pick and the escaping.
 *
 * Initial date: 24 Nov 2026<br>
 * @author gnaegi, https://www.frentix.com
 *
 */
public class ColorPickerRendererTest {

	private static final Pattern LIST = Pattern.compile("<ul class='o_color_picker_list'>(.*)</ul></div><script>",
			Pattern.DOTALL);
	private static final Pattern ITEM = Pattern.compile("<li(?: class='o_selected')?><a [^>]*data-color='([^']*)'>.*?</a></li>");
	// the grid ends with three closing tags: the last cell, the last row and the grid
	private static final Pattern GRID = Pattern.compile("<div class='o_color_picker_grid' role='grid'[^>]*>(.*?</div></div>)</div>",
			Pattern.DOTALL);
	private static final Pattern ROW = Pattern.compile("<div class='o_color_picker_grid_row' role='row'>(.*?)</div>(?=<div class='o_color_picker_grid_row'|$)",
			Pattern.DOTALL);
	private static final Pattern CELL = Pattern.compile("<div class='o_color_picker_cell[^']*'[^>]*?(?: data-color='([^']*)'[^>]*)?>(?:<span aria-hidden='true'>a</span>)?</div>");

	@Test
	public void button() {
		ColorPickerElementImpl pickerEl = mockPicker(List.of(new Color("blue", "Blue", "o_swatch_blue")));
		when(pickerEl.getColor()).thenReturn(new Color("blue", "Blue", "o_swatch_blue"));
		String html = renderButton(pickerEl);

		// the button opens a dialog (the callout) with a request to the element
		assertThat(html).contains("<button style='padding-left: 32px;' class='btn btn-default o_color_picker_button o_button_printed' type='button'"
				+ " id='o_fi1_B' aria-haspopup='dialog' aria-expanded='false' ")
				.contains("onclick=\"o_ffXHREvent(")
				// with the values of the form: the callout redraws the form below it
				.contains(",false,false,true,true,'cp_cmd','open'); return false;\"")
				.contains("onkeydown=\"if (event.key === 'ArrowDown' || event.key === 'ArrowUp')")
				.doesNotContain("data-toggle", "dropdown", "aria-controls", "disabled");
		// the value is in the form
		assertThat(html).contains("<input type='hidden' id='o_fi1' name='o_fi1' value='blue'>")
				.contains("<span>Blue</span>");
		// no colours in the form
		assertThat(html).doesNotContain("data-color", "o_color_picker_list", "role='grid'", "<script>");
	}

	@Test
	public void buttonOpen() {
		ColorPickerElementImpl pickerEl = mockPicker(List.of(new Color("blue", "Blue", "o_swatch_blue")));
		when(pickerEl.isCalloutOpen()).thenReturn(true);

		assertThat(renderButton(pickerEl)).contains("aria-expanded='true' aria-controls='o_fi1_P' ");
	}

	@Test
	public void buttonDisabled() {
		ColorPickerElementImpl pickerEl = mockPicker(List.of(new Color("blue", "Blue", "o_swatch_blue")));
		when(pickerEl.isEnabled()).thenReturn(false);
		when(pickerEl.isCalloutOpen()).thenReturn(true);
		String html = renderButton(pickerEl);

		assertThat(html).contains("aria-expanded='false' disabled>").doesNotContain("onclick", "onkeydown", "o_ffXHREvent");
	}

	@Test
	public void buttonWithoutLabel() {
		ColorPickerElementImpl pickerEl = mockPicker(List.of(new Color("blue", "Blue", "o_swatch_blue")));
		when(pickerEl.getButtonAriaLabel()).thenReturn("Color: Blue & <b>");

		assertThat(renderButton(pickerEl)).contains("aria-label='Color: Blue &amp; &lt;b&gt;' ")
				.doesNotContain("aria-labelledby");
	}

	@Test
	public void buttonWithLabelAndResetButton() {
		ColorPickerElementImpl pickerEl = mockPicker(List.of(new Color("blue", "Blue", "o_swatch_blue")));
		when(pickerEl.hasLabel()).thenReturn(true);
		when(pickerEl.getResetButtonId()).thenReturn("o_reset");
		when(pickerEl.getNonSelectedText()).thenReturn("<b>None</b>");
		String html = renderButton(pickerEl);

		assertThat(html).contains("aria-labelledby='o_cl1 o_fi1_B' ")
				.contains("<span>&lt;b&gt;None&lt;/b&gt;</span>")
				.contains("jQuery('#o_reset').on('click', function() { jQuery('#o_fi1_B').focus(); });");
	}

	@Test
	public void pickedField() {
		ColorPickerElementImpl pickerEl = mockPicker(List.of(new Color("red", "Red", "o_color_red")));
		assertThat(renderButton(pickerEl)).contains("<input type='hidden' id='o_fi1_picked' name='o_fi1_picked' value=''>");
		// a pick in the browser sets the field
		assertThat(renderPopup(pickerEl)).contains("jQuery('#o_fi1_picked').val('true');");

		when(pickerEl.isColorPicked()).thenReturn(true);
		assertThat(renderButton(pickerEl)).contains("<input type='hidden' id='o_fi1_picked' name='o_fi1_picked' value='true'>");
	}

	@Test
	public void withoutGroups() {
		String list = renderList(List.of(
				new Color("red", "Red", "o_color_red"),
				new Color("blue", "Blue", "o_color_blue")), false);

		assertThat(list).doesNotContain("o_color_picker_group", "role='group'", "dropdown-header");
		// the items follow each other directly in the list
		assertThat(ITEM.matcher(list).replaceAll("[$1]")).isEqualTo("[red][blue]");
	}

	@Test
	public void withGroup() {
		String list = renderList(List.of(
				new Color("red", "Red", "o_color_red"),
				new Color("blue", "Blue", "o_color_blue"),
				new Color("theme1", "Theme 1", "o_swatch_theme1", "Theme colors"),
				new Color("theme2", "Theme 2", "o_swatch_theme2", "Theme colors")), false);

		assertThat(ITEM.matcher(list).replaceAll("[$1]")).isEqualTo("[red][blue]"
				+ "<li class='o_color_picker_group'><div role='group' aria-labelledby='o_fi1_P_g1'>"
				+ "<div id='o_fi1_P_g1' class='dropdown-header o_color_picker_group_header'>Theme colors</div>"
				+ "<ul class='o_color_picker_group_list'>[theme1][theme2]</ul></div></li>");
		assertThat(list).contains("o_color_picker_colored_area o_icon o_icon_a o_color_background o_color_contrast_border o_color_text_on_background o_swatch_theme2");
	}

	@Test
	public void unselectAfterGroup() {
		String list = renderList(List.of(
				new Color("red", "Red", "o_color_red"),
				new Color("theme1", "Theme", "o_swatch_theme1", "Theme colors")), true);

		assertThat(ITEM.matcher(list).replaceAll("[$1]")).endsWith("[theme1]</ul></div></li>[__color_unselect__]");
	}

	@Test
	public void groupHeadingIsEscaped() {
		String list = renderList(List.of(
				new Color("theme1", "Theme", "o_swatch_theme1", "<b>Theme</b>")), false);

		assertThat(list).contains(">&lt;b&gt;Theme&lt;/b&gt;</div>");
	}

	@Test
	public void listFocusAndSelection() {
		List<Color> colors = List.of(
				new Color("red", "Red", "o_color_red"),
				new Color("blue", "Blue", "o_color_blue"));
		ColorPickerElementImpl pickerEl = mockPicker(colors);
		// nothing selected: the first entry has the focus when the callout opens
		assertThat(renderPopup(pickerEl))
				.contains("<li><a tabindex='0' role='button' aria-pressed='false' class='o_color_picker_link' autofocus data-color='red'>")
				.containsOnlyOnce(" autofocus ").doesNotContain("o_selected");

		// the selected entry has the focus
		when(pickerEl.getColor()).thenReturn(colors.get(1));
		assertThat(renderPopup(pickerEl))
				.contains("<li class='o_selected'><a tabindex='0' role='button' aria-pressed='true' class='o_color_picker_link' autofocus data-color='blue'>")
				.containsOnlyOnce(" autofocus ");
	}

	@Test
	public void listInCallout() {
		ColorPickerElementImpl pickerEl = mockPicker(List.of(new Color("red", "Red", "o_color_red")));
		String html = renderPopup(pickerEl);

		assertThat(html).startsWith("<div id='o_fi1_P' class='o_color_picker_popup o_color_picker_popup_list'><ul class='o_color_picker_list'>")
				.doesNotContain("dropdown-menu", "role='grid'", "<button", "o_fi1_B'")
				// the list keys and the focus when the callout is visible
				.contains("popup.on('keydown', '.o_color_picker_link'", "case 'ArrowDown':", "case 'Home':", "case ' ':",
						"popup.find('[autofocus]')[0]");
	}

	@Test
	public void pick() {
		ColorPickerElementImpl pickerEl = mockPicker(List.of(new Color("red", "Red", "o_color_red")));
		String html = renderPopup(pickerEl);
		String script = html.substring(html.indexOf("<script>"));

		// the pick and the close submit the values of the form, the hidden field has the colour
		assertThat(script).contains("input.val(colorId);")
				.contains("sent = o_ffXHREvent(").contains(",'o_fi1',").contains(",false,false,true,true,'cp_cmd','pick');")
				.contains("jQuery('#o_fi1_picked').val('true');")
				.doesNotContain("o_ffEvent(", "colorId',colorId", "false,false,false,true");
		// a pick of the selected colour closes the callout: no dirty form, no change
		assertThat(script).contains("const same = colorId === (input.val() || '__color_unselect__');")
				.contains("sent = o_ffXHREvent(").contains(",false,false,true,true,'cp_cmd','close');");
		int same = script.indexOf("if (same) {");
		int dirty = script.indexOf("setFlexiFormDirty(");
		int otherwise = script.indexOf("} else {", same);
		assertThat(same).isPositive();
		assertThat(dirty).isGreaterThan(otherwise);
	}

	@Test
	public void pickWithOnChange() {
		ColorPickerElementImpl pickerEl = mockPicker(List.of(new Color("red", "Red", "o_color_red")));
		String withoutOnChange = renderPopup(pickerEl);
		when(pickerEl.getAction()).thenReturn(FormEvent.ONCHANGE);

		// the same request: the element fires ONCHANGE on the server
		assertThat(renderPopup(pickerEl)).isEqualTo(withoutOnChange);
	}

	@Test
	public void focusWaitsForTheCallout() {
		ColorPickerElementImpl pickerEl = mockPicker(List.of(new Color("red", "Red", "o_color_red")));

		// up to 80 x 25ms = 2s for the dialog to open
		assertThat(renderPopup(pickerEl)).contains("if (attempt < 80) { setTimeout(function() { show(attempt + 1); }, 25); }");
	}

	@Test
	public void hostileValuesAreEscaped() {
		String hostile = "\"><script>alert(1)</script>";
		String quote = "x'); alert(1); ('";
		String entity = "&#39;); alert(1); //";
		String list = renderList(List.of(
				new Color(quote, hostile, "blue onmouseover=alert(1)"),
				new Color(entity, entity, "o_cal_blue\" x=\""),
				new Color("red", "Rot & Grün", "o_color_red")), false);

		assertThat(list).doesNotContain("<script>", "onmouseover", "x=\"", "x'", "alert(1); ('");
		// the id is in an attribute, not in a script
		assertThat(list).contains("data-color='x&#39;); alert(1); (&#39;'")
				.contains("data-color='&amp;#39;); alert(1); //'");
		// the text is escaped for the element
		assertThat(list).contains("<span>&quot;&gt;&lt;script&gt;alert(1)&lt;/script&gt;</span>");
		assertThat(list).contains("<span>Rot &amp; Gr&uuml;n</span>");
		// an invalid class renders no class
		assertThat(list).contains("o_color_text_on_background ' aria-hidden");
		assertThat(list).contains("o_color_text_on_background o_color_red' aria-hidden");

		// the script reads the colour from the entry, no value of an entry is in the script
		ColorPickerElementImpl pickerEl = mockPicker(List.of(new Color(quote, hostile, "o_color_red")));
		String html = renderPopup(pickerEl);
		assertThat(html.substring(html.indexOf("<script>"))).doesNotContain("alert", "script>alert");
	}

	@Test
	public void listPresentationIsTheDefault() {
		ColorPickerElementImpl pickerEl = mockPicker(List.of(new Color("red", "Red", "o_color_red")));
		String byDefault = renderPopup(pickerEl);
		when(pickerEl.getPresentation()).thenReturn(Presentation.LIST);
		assertThat(renderPopup(pickerEl)).isEqualTo(byDefault).doesNotContain("role='grid'");
	}

	@Test
	public void gridRowsAndColumns() {
		List<Color> colors = List.of(
				new Color("red-soft", "Red soft", "o_swatch_red o_swatch_soft"),
				new Color("red", "Red", "o_swatch_red"),
				new Color("blue-soft", "Blue soft", "o_swatch_blue o_swatch_soft"),
				new Color("blue", "Blue", "o_swatch_blue"),
				new Color("theme1-soft", "Theme soft", "o_swatch_theme1 o_swatch_soft", "Theme colors"),
				new Color("theme1", "Theme", "o_swatch_theme1", "Theme colors"));
		ColorPickerElementImpl pickerEl = mockGridPicker(colors);
		when(pickerEl.getColor()).thenReturn(colors.get(3));
		String html = renderPopup(pickerEl);

		// no list
		assertThat(html).startsWith("<div id='o_fi1_P' class='o_color_picker_popup o_color_picker_popup_grid'>")
				.doesNotContain("o_color_picker_list", "<li ");
		// one row per variant, light to dark, one cell per colour
		assertThat(cellsPerRow(html)).containsExactly("[red-soft][blue-soft][theme1-soft]", "[red][blue][theme1]");
		// the cell: swatch classes, the name as accessible name, the letter
		assertThat(html).contains("<div class='o_color_picker_cell o_swatch_red o_swatch_soft' role='gridcell' tabindex='-1'"
				+ " aria-selected='false' aria-label='Red soft' data-color='red-soft'>"
				+ "<span aria-hidden='true'>a</span></div>");
		// the selected cell is the tab stop and has the focus when the callout opens
		assertThat(html).contains("<div class='o_color_picker_cell o_swatch_blue o_selected' role='gridcell' tabindex='0' autofocus"
				+ " aria-selected='true' aria-label='Blue'");
		assertThat(html).containsOnlyOnce("tabindex='0'").containsOnlyOnce("aria-selected='true'").containsOnlyOnce(" autofocus ");
		// the theme colours after a gap, with the heading as description
		assertThat(html).contains("<div class='o_color_picker_grid_groups' aria-hidden='true'>"
				+ "<span class='o_color_picker_grid_group'></span><span class='o_color_picker_grid_group'></span>"
				+ "<span class='o_color_picker_grid_group o_color_picker_grid_gap' id='o_fi1_P_g1'>Theme colors</span></div>");
		assertThat(html).contains("<div class='o_color_picker_cell o_swatch_theme1 o_color_picker_grid_gap' role='gridcell' tabindex='-1'"
				+ " aria-selected='false' aria-label='Theme' aria-describedby='o_fi1_P_g1' data-color='theme1'");
		// the grid is named by the button without a label
		assertThat(html).contains("role='grid' aria-labelledby='o_fi1_B'");
		// the grid keys
		assertThat(html).contains("case 'PageDown':", "case 'Enter':", "pick(cell, event);")
				.doesNotContain("dropdown(", "bs.dropdown");
		// the same button for both presentations
		when(pickerEl.getPresentation()).thenReturn(Presentation.LIST);
		String listButton = renderButton(pickerEl);
		when(pickerEl.getPresentation()).thenReturn(Presentation.GRID);
		assertThat(renderButton(pickerEl)).isEqualTo(listButton);
	}

	@Test
	public void gridWithoutLabel() {
		ColorPickerElementImpl pickerEl = mockGridPicker(List.of(new Color("red", "Red", "o_swatch_red")));
		when(pickerEl.getPopupAriaLabel()).thenReturn("Color");

		// the name of the grid is "Color", not the current colour of the button
		assertThat(renderPopup(pickerEl)).contains("role='grid' aria-label='Color'>").doesNotContain("aria-labelledby='o_fi1_B'");
	}

	@Test
	public void gridWithLabel() {
		ColorPickerElementImpl pickerEl = mockGridPicker(List.of(new Color("red", "Red", "o_swatch_red")));
		when(pickerEl.hasLabel()).thenReturn(true);

		assertThat(renderPopup(pickerEl)).contains("role='grid' aria-labelledby='o_cl1'");
	}

	@Test
	public void gridWithoutGroups() {
		ColorPickerElementImpl pickerEl = mockGridPicker(List.of(
				new Color("red", "Red", "o_swatch_red"),
				new Color("blue", "Blue", "o_swatch_blue")));
		String html = renderPopup(pickerEl);

		assertThat(html).doesNotContain("o_color_picker_grid_groups", "o_color_picker_grid_gap", "aria-describedby");
		assertThat(cellsPerRow(html)).containsExactly("[red][blue]");
		// nothing selected: the first cell is the tab stop
		assertThat(html).contains("aria-label='Red' data-color='red'").containsOnlyOnce("tabindex='0'")
				.contains("o_swatch_red' role='gridcell' tabindex='0' autofocus aria-selected='false'");
	}

	@Test
	public void gridMissingVariantAndOtherKeys() {
		ColorPickerElementImpl pickerEl = mockGridPicker(List.of(
				new Color("red-soft", "Red soft", "o_swatch_red o_swatch_soft"),
				new Color("red", "Red", "o_swatch_red"),
				new Color("blue", "Blue", "o_swatch_blue"),
				new Color("gold", "Gold", "o_color_gold"),
				new Color("red", "Red again", "o_swatch_red")));
		String html = renderPopup(pickerEl);

		// blue has no soft variant: an empty cell keeps the column; gold is no swatch key: its own column, solid row
		assertThat(cellsPerRow(html)).containsExactly("[red-soft][][]", "[red][blue][gold]");
		assertThat(html).contains("<div class='o_color_picker_cell o_color_picker_cell_empty' role='gridcell'></div>")
				.doesNotContain("Red again");
	}

	@Test
	public void gridUnselectButton() {
		Color red = new Color("red", "Red", "o_swatch_red");
		ColorPickerElementImpl pickerEl = mockGridPicker(List.of(red));
		assertThat(renderPopup(pickerEl)).doesNotContain("<button type='button' class='btn btn-default btn-block o_color_picker_grid_unselect");

		when(pickerEl.isAllowUnselect()).thenReturn(true);
		when(pickerEl.getNonSelectedText()).thenReturn("No color");
		// no colour selected: the button is pressed; a click (also Enter and Space) picks it
		String html = renderPopup(pickerEl);
		assertThat(html).contains("<button type='button' class='btn btn-default btn-block o_color_picker_grid_unselect o_selected'"
				+ " aria-pressed='true' data-color='__color_unselect__'>No color</button></div><script>")
				.contains("popup.on('click', '[data-color]'");

		when(pickerEl.getColor()).thenReturn(red);
		assertThat(renderPopup(pickerEl)).contains("<button type='button' class='btn btn-default btn-block o_color_picker_grid_unselect'"
				+ " aria-pressed='false' data-color='__color_unselect__'>No color</button>");
	}

	@Test
	public void gridHostileValuesAreEscaped() {
		String hostile = "\"><script>alert(1)</script>";
		String quote = "x' onmouseover='alert(1)";
		ColorPickerElementImpl pickerEl = mockGridPicker(List.of(
				new Color(quote, hostile, "blue onmouseover=alert(1)", "<b>Group</b>"),
				new Color("red", "Rot & Grün", "o_swatch_red o_swatch_soft\" x=\"")));
		when(pickerEl.isAllowUnselect()).thenReturn(true);
		when(pickerEl.getNonSelectedText()).thenReturn("<i>None</i>");
		String html = renderPopup(pickerEl);

		assertThat(html).doesNotContain("<script>alert", "onmouseover='", "blue onmouseover", "x=\"", "x' ", "<b>", "<i>None");
		assertThat(html).contains("aria-label='&quot;&gt;&lt;script&gt;alert(1)&lt;/script&gt;'")
				.contains("data-color='x&#39; onmouseover=&#39;alert(1)'")
				.contains("aria-label='Rot &amp; Gr&uuml;n'")
				.contains(">&lt;b&gt;Group&lt;/b&gt;</span>")
				.contains(">&lt;i&gt;None&lt;/i&gt;</button>");
		// an invalid class renders no class
		assertThat(html).contains("<div class='o_color_picker_cell' role='gridcell'");
		// the script reads the values from the cells, no value of an entry is in the script
		String script = html.substring(html.indexOf("<script>"));
		assertThat(script).doesNotContain("alert", "Rot", "Group", "None");
	}

	@Test
	public void gridColumns() {
		List<ColorPickerGridRenderer.Column> columns = ColorPickerGridRenderer.toColumns(List.of(
				new Color("blue-dark", "Blue dark", "o_swatch_blue o_swatch_dark"),
				new Color("blue-light", "Blue light", "o_swatch_blue o_swatch_light"),
				new Color("theme2-tint", "Theme 2 tint", "o_swatch_theme2 o_swatch_tint", "Theme colors")));

		assertThat(columns).extracting(ColorPickerGridRenderer.Column::key).containsExactly("blue", "theme2");
		assertThat(columns.get(1).group()).isEqualTo("Theme colors");
		// the rows follow the variants light to dark, not the order of the entries
		assertThat(ColorPickerGridRenderer.toRows(columns)).containsExactly("light", "tint", "dark");
		assertThat(ColorPickerGridRenderer.toColumns(null)).isEmpty();
	}

	@Test
	public void helpers() {
		assertThat(ColorPickerRenderer.cssClass(null)).isEmpty();
		assertThat(ColorPickerRenderer.cssClass("o_swatch_blue o_swatch_light")).isEqualTo("o_swatch_blue o_swatch_light");
		assertThat(ColorPickerRenderer.cssClass("o_swatch_blue' onclick='x")).isEmpty();
		assertThat(ColorPickerRenderer.attribute("a'b\"c<d>&")).isEqualTo("a&#39;b&quot;c&lt;d&gt;&amp;");
		assertThat(ColorPickerRenderer.text(null)).isEmpty();
	}

	private String renderList(List<Color> colors, boolean unselect) {
		ColorPickerElementImpl pickerEl = mockPicker(colors);
		if (unselect) {
			when(pickerEl.getColor()).thenReturn(colors.get(0));
			when(pickerEl.isAllowUnselect()).thenReturn(true);
			when(pickerEl.getNonSelectedText()).thenReturn("No color");
		}

		Matcher matcher = LIST.matcher(renderPopup(pickerEl));
		assertThat(matcher.find()).isTrue();
		return matcher.group(1);
	}

	private ColorPickerElementImpl mockGridPicker(List<Color> colors) {
		ColorPickerElementImpl pickerEl = mockPicker(colors);
		when(pickerEl.getPresentation()).thenReturn(Presentation.GRID);
		return pickerEl;
	}

	/**
	 * @return Per row of the grid the ids of the cells, e.g. "[red][]" for a
	 * 		cell of red and an empty cell
	 */
	private List<String> cellsPerRow(String html) {
		Matcher grid = GRID.matcher(html);
		assertThat(grid.find()).isTrue();
		Matcher rows = ROW.matcher(grid.group(1));
		List<String> cells = new ArrayList<>();
		while (rows.find()) {
			cells.add(CELL.matcher(rows.group(1)).replaceAll(match -> "[" + (match.group(1) == null ? "" : match.group(1)) + "]"));
		}
		return cells;
	}

	private ColorPickerElementImpl mockPicker(List<Color> colors) {
		ColorPickerElementImpl pickerEl = mock(ColorPickerElementImpl.class);
		ColorPickerComponent component = mock(ColorPickerComponent.class);
		when(pickerEl.getComponent()).thenReturn(component);
		when(component.getFormItem()).thenReturn(pickerEl);
		when(component.getDispatchID()).thenReturn("1");
		when(pickerEl.getColors()).thenReturn(colors);
		when(pickerEl.getFormDispatchId()).thenReturn("o_fi1");
		when(pickerEl.getRootForm()).thenReturn(mock(Form.class));
		when(pickerEl.isEnabled()).thenReturn(true);
		return pickerEl;
	}

	private String renderButton(ColorPickerElementImpl pickerEl) {
		StringOutput sb = new StringOutput();
		new ColorPickerRenderer().renderComponent(null, sb, pickerEl.getComponent(), null, null, null, null);
		return sb.toString();
	}

	private String renderPopup(ColorPickerElementImpl pickerEl) {
		StringOutput sb = new StringOutput();
		new ColorPickerPopupRenderer().render(sb, pickerEl);
		return sb.toString();
	}
}

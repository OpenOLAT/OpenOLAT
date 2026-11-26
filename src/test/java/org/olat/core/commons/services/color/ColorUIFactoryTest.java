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
package org.olat.core.commons.services.color;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.olat.core.gui.components.form.flexible.elements.ColorPickerElement;
import org.olat.core.gui.components.form.flexible.elements.ColorPickerElement.Color;
import org.olat.core.gui.translator.Translator;

/**
 * Checks {@link ColorUIFactory#appendThemeColors(List, List, Translator)}:
 * the label rule of the theme colours (one: "Theme", more: "Theme 1" ...
 * "Theme n"), the swatch class and the group.
 *
 * Initial date: 24 Nov 2026<br>
 * @author gnaegi, https://www.frentix.com
 *
 */
public class ColorUIFactoryTest {

	private static final List<Color> HUES = List.of(
			new Color("red", "Red", "o_color_red"),
			new Color("blue", "Blue", "o_color_blue"));

	private Translator translator;

	@Before
	public void setUp() {
		translator = mock(Translator.class);
		when(translator.translate("color.theme")).thenReturn("Theme");
		when(translator.translate("color.theme.group")).thenReturn("Theme colors");
		when(translator.translate(eq("color.theme.n"), any(String[].class)))
				.thenAnswer(invocation -> "Theme " + invocation.getArgument(1));
	}

	@Test
	public void noThemeColor() {
		List<Color> colors = ColorUIFactory.appendThemeColors(HUES, List.of(), translator);

		assertThat(colors).containsExactlyElementsOf(HUES);
		assertThat(colors).allMatch(color -> color.group() == null);
	}

	@Test
	public void noThemeColorNull() {
		List<Color> colors = ColorUIFactory.appendThemeColors(HUES, null, translator);

		assertThat(colors).containsExactlyElementsOf(HUES);
	}

	@Test
	public void oneThemeColor() {
		List<Color> colors = ColorUIFactory.appendThemeColors(HUES, List.of("theme1"), translator);

		assertThat(colors).hasSize(3);
		assertThat(colors.subList(0, 2)).containsExactlyElementsOf(HUES);
		assertThat(colors.get(2)).isEqualTo(new Color("theme1", "Theme", "o_swatch_theme1", "Theme colors"));
	}

	@Test
	public void manyThemeColors() {
		List<Color> colors = ColorUIFactory.appendThemeColors(HUES, List.of("theme1", "theme2", "theme3"), translator);

		assertThat(colors).hasSize(5);
		assertThat(colors.subList(0, 2)).containsExactlyElementsOf(HUES);
		assertThat(colors.subList(2, 5)).containsExactly(
				new Color("theme1", "Theme 1", "o_swatch_theme1", "Theme colors"),
				new Color("theme2", "Theme 2", "o_swatch_theme2", "Theme colors"),
				new Color("theme3", "Theme 3", "o_swatch_theme3", "Theme colors"));
	}

	@Test
	public void listIsNotChanged() {
		List<Color> hues = new ArrayList<>(HUES);
		ColorUIFactory.appendThemeColors(hues, List.of("theme1", "theme2"), translator);

		assertThat(hues).containsExactlyElementsOf(HUES);
	}

	@Test
	public void createColorsDropsJunk() {
		List<String> names = new ArrayList<>(List.of("red", "light.blue", "blue"));
		names.addAll(ColorValidatorTest.JUNK);
		names.add(null);
		List<Color> colors = ColorUIFactory.createColors(names, translator, ColorService.CSS_CLASS_PREFIX);

		// "light.blue" is a key, but "o_color_light.blue" is no class name
		assertThat(colors).extracting(Color::id).containsExactly("red", "blue");
		assertThat(colors).extracting(Color::cssClass).containsExactly("o_color_red", "o_color_blue");
		assertThat(ColorUIFactory.createColors(null, translator, ColorService.CSS_CLASS_PREFIX)).isEmpty();
	}

	@Test
	public void themeColorJunkDropped() {
		List<Color> colors = ColorUIFactory.appendThemeColors(HUES,
				java.util.Arrays.asList("theme1", "\"><script>alert(1)</script>", null, "theme99999", "red"), translator);

		// one valid theme colour: the label of a single theme colour
		assertThat(colors).hasSize(3);
		assertThat(colors.get(2)).isEqualTo(new Color("theme1", "Theme", "o_swatch_theme1", "Theme colors"));
	}

	@Test
	public void createColorsOfColorSetM() {
		when(translator.translate("color.red")).thenReturn("Red");
		when(translator.translate("color.gray")).thenReturn("Grey");
		List<Color> colors = ColorUIFactory.createColors(ColorSet.M, List.of("theme1", "theme2"), translator);

		// the 14 hues with the swatch class, then all theme colours in their group
		assertThat(colors).hasSize(16);
		assertThat(colors).extracting(Color::id).containsExactlyElementsOf(List.of("red", "orange", "yellow", "lime", "green",
				"teal", "cyan", "blue", "violet", "magenta", "pink", "brown", "slate", "gray", "theme1", "theme2"));
		assertThat(colors.get(0)).isEqualTo(new Color("red", "Red", "o_swatch_red"));
		assertThat(colors.get(13)).isEqualTo(new Color("gray", "Grey", "o_swatch_gray"));
		assertThat(colors.get(15)).isEqualTo(new Color("theme2", "Theme 2", "o_swatch_theme2", "Theme colors"));
		// the ids are the keys of the set
		assertThat(colors).extracting(Color::id).containsExactlyElementsOf(ColorSet.M.getKeysWithThemeColors(List.of("theme1", "theme2")));
	}

	@Test
	public void createColorsOfColorSetS() {
		List<Color> colors = ColorUIFactory.createColors(ColorSet.S, List.of("theme1", "theme2"), translator);

		// 7 hues and the first theme colour, with the label of a single theme colour
		assertThat(colors).extracting(Color::id).containsExactly("red", "orange", "yellow", "green", "blue", "pink", "slate", "theme1");
		assertThat(colors.get(7)).isEqualTo(new Color("theme1", "Theme", "o_swatch_theme1", "Theme colors"));
		// without theme colours: the hues only
		assertThat(ColorUIFactory.createColors(ColorSet.M, List.of(), translator)).hasSize(14)
				.allMatch(color -> color.group() == null);
	}

	@Test
	public void createBadgeColors() {
		when(translator.translate("color.gold")).thenReturn("Gold");
		List<Color> colors = ColorUIFactory.createBadgeColors(List.of("theme1"), translator);

		// gold, silver, bronze with the classes of their aliases, then the set M with the theme colour
		assertThat(colors).hasSize(3 + 14 + 1);
		assertThat(colors.get(0)).isEqualTo(new Color("gold", "Gold", "o_color_gold"));
		assertThat(colors.subList(0, 3)).extracting(Color::cssClass).containsExactly("o_color_gold", "o_color_silver", "o_color_bronze");
		assertThat(colors.subList(3, 17)).extracting(Color::cssClass).allMatch(cssClass -> cssClass.startsWith("o_swatch_"));
		assertThat(colors.get(17)).isEqualTo(new Color("theme1", "Theme", "o_swatch_theme1", "Theme colors"));
		// the ids are the badge colours of the colour service
		assertThat(colors).extracting(Color::id).startsWith("gold", "silver", "bronze", "red").endsWith("gray", "theme1");
	}

	@Test
	public void tableCssClass() {
		assertThat(ColorUIFactory.toTableCssClass("red")).isEqualTo("b_red");
		assertThat(ColorUIFactory.toTableCssClass("slate")).isEqualTo("b_slate");
		assertThat(ColorUIFactory.toTableCssClass("theme2")).isEqualTo("b_theme2");
		// b_gray is the old style that renders slate
		assertThat(ColorUIFactory.toTableCssClass("gray")).isEqualTo("b_gray_hue");
		// every key of the set M has a table style, each another one
		List<String> keys = ColorSet.M.getKeysWithThemeColors(List.of("theme1", "theme2"));
		assertThat(keys).extracting(ColorUIFactory::toTableCssClass).doesNotContainNull().doesNotHaveDuplicates()
				.doesNotContain("b_gray");
		// no solid key: no table style
		assertThat(ColorUIFactory.toTableCssClass("blue-light")).isNull();
		assertThat(ColorUIFactory.toTableCssClass("lightblue")).isNull();
		assertThat(ColorUIFactory.toTableCssClass(null)).isNull();
		assertThat(ColorUIFactory.toTableCssClass("red' onclick='x")).isNull();
	}

	@Test
	public void tableColorKey() {
		for (String key : ColorSet.M.getKeysWithThemeColors(List.of("theme1", "theme2"))) {
			assertThat(ColorUIFactory.toTableColorKey(ColorUIFactory.toTableCssClass(key))).isEqualTo(key);
		}
		// the old styles of ceditor.table.styles
		assertThat(ColorUIFactory.toTableColorKey("b_gray")).isEqualTo("slate");
		assertThat(ColorUIFactory.toTableColorKey("b_blue")).isEqualTo("blue");
		assertThat(ColorUIFactory.toTableColorKey("b_green")).isEqualTo("green");
		assertThat(ColorUIFactory.toTableColorKey("b_yellow")).isEqualTo("yellow");
		assertThat(ColorUIFactory.toTableColorKey(" b_red ")).isEqualTo("red");
		// other styles: no colour
		assertThat(ColorUIFactory.toTableColorKey("b_default")).isNull();
		assertThat(ColorUIFactory.toTableColorKey("b_middle")).isNull();
		assertThat(ColorUIFactory.toTableColorKey("o_oo_my_style")).isNull();
		assertThat(ColorUIFactory.toTableColorKey("b_blue-light")).isNull();
		assertThat(ColorUIFactory.toTableColorKey("b_")).isNull();
		assertThat(ColorUIFactory.toTableColorKey(null)).isNull();
	}

	@Test
	public void valueToSaveKeepsOldValueWithoutPick() {
		ColorPickerElement colorEl = colorPicker("blue", false);
		// the chooser preselects "blue" for the old calendar colour " navy", the value stays byte-identical
		assertThat(ColorUIFactory.getValueToSave(" navy", colorEl)).isEqualTo(" navy");
		assertThat(ColorUIFactory.getValueToSave("o_cal_navy", colorEl)).isEqualTo("o_cal_navy");
		assertThat(ColorUIFactory.getValueToSave("", colorEl)).isEmpty();
		// a new item or an item without colour gets the preselected key
		assertThat(ColorUIFactory.getValueToSave(null, colorEl)).isEqualTo("blue");
	}

	@Test
	public void valueToSaveWritesPickedKey() {
		// also the preselected key, if the user picked it
		assertThat(ColorUIFactory.getValueToSave("navy", colorPicker("blue", true))).isEqualTo("blue");
		assertThat(ColorUIFactory.getValueToSave("navy", colorPicker("theme2", true))).isEqualTo("theme2");
		// the user picked "no colour"
		assertThat(ColorUIFactory.getValueToSave("navy", colorPicker(null, true))).isNull();
		// no preselection, no pick
		assertThat(ColorUIFactory.getValueToSave("unknown", colorPicker(null, false))).isEqualTo("unknown");
		assertThat(ColorUIFactory.getValueToSave(null, colorPicker(null, false))).isNull();
	}

	@Test
	public void valueToSaveChanged() {
		assertThat(ColorUIFactory.getValueToSave("b_gray", false, "b_slate")).isEqualTo("b_gray");
		assertThat(ColorUIFactory.getValueToSave("b_gray", true, "b_slate")).isEqualTo("b_slate");
		// reset to no colour
		assertThat(ColorUIFactory.getValueToSave("navy", true, null)).isNull();
		assertThat(ColorUIFactory.getValueToSave(null, false, "b_blue")).isEqualTo("b_blue");
	}

	private static ColorPickerElement colorPicker(String selectedKey, boolean picked) {
		ColorPickerElement colorEl = mock(ColorPickerElement.class);
		when(colorEl.getColor()).thenReturn(selectedKey == null ? null : new Color(selectedKey, selectedKey, "o_swatch_" + selectedKey));
		when(colorEl.isColorPicked()).thenReturn(picked);
		return colorEl;
	}
}

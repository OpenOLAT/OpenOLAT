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
package org.olat.core.commons.services.color;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.services.color.EditorColorMap.Entry;
import org.olat.core.gui.components.form.flexible.elements.ColorPickerElement.Color;
import org.olat.core.gui.translator.Translator;

/**
 * Checks the colour map of the HTML editor {@link EditorColorMap}: the order
 * neutral steps, hues, theme colours; the fallback without palette; the
 * validation of the colours and the escaping of the labels.
 *
 * Initial date: 25 Nov 2026<br>
 * @author gnaegi, https://www.frentix.com
 */
public class EditorColorMapTest {

	private static final Path OPENOLAT_THEME_CSS = Path.of("src/main/webapp/static/themes/openolat/theme.css");

	/** The form of the options: only hex colours and escaped labels */
	private static final Pattern OPTIONS = Pattern.compile(
			"color_map: \\[('#[0-9A-F]{6}','[A-Za-z0-9 \\\\]*')(,'#[0-9A-F]{6}','[A-Za-z0-9 \\\\]*')*\\],\ncolor_cols: 6,\n");

	private Translator translator;

	@Before
	public void setUp() {
		translator = mock(Translator.class);
		when(translator.translate(anyString())).thenAnswer(invocation -> "L " + invocation.getArgument(0));
		when(translator.translate(eq("color.theme"))).thenReturn("Theme");
		when(translator.translate(eq("color.theme.n"), any(String[].class)))
				.thenAnswer(invocation -> "Theme " + invocation.getArgument(1));
	}

	@Test
	public void tableClassListEntries() {
		List<Color> colors = ColorUIFactory.createColors(ColorSet.M, List.of("theme1", "theme2"), translator);
		String entries = EditorColorMap.toTableClassListEntries(colors);

		// one entry per key of the set M, the label as JavaScript string
		assertThat(entries).startsWith("  {title: 'L color\\u002Ered', value: 'b_red'},\n");
		assertThat(entries).contains("value: 'b_gray_hue'").doesNotContain("value: 'b_gray'");
		assertThat(entries).contains("  {title: 'Theme 2', value: 'b_theme2'},\n");
		assertThat(entries.split("\n")).hasSize(16);
		// junk: no entry, a label is escaped
		List<Color> junk = new ArrayList<>();
		junk.add(new Color("blue-light", "Blue light", "o_swatch_blue o_swatch_light"));
		junk.add(new Color("lightblue", "Light blue", "o_color_lightblue"));
		junk.add(new Color("red' onclick='x", "Red", "o_swatch_red"));
		junk.add(null);
		junk.add(new Color("blue", "Blue'</script><script>alert(1)</script>", "o_swatch_blue"));
		String junkEntries = EditorColorMap.toTableClassListEntries(junk);
		assertThat(junkEntries).startsWith("  {title: 'Blue\\u0027\\u003C\\u002Fscript").endsWith("', value: 'b_blue'},\n")
				.doesNotContain("<").doesNotContain("onclick");
		assertThat(EditorColorMap.toTableClassListEntries(null)).isEmpty();
		assertThat(EditorColorMap.toTableClassListEntries(List.of())).isEmpty();
	}

	@Test
	public void orderWithThemeColors() {
		Palette palette = Palette.fromCss(css(true, true, 2));

		List<Entry> entries = EditorColorMap.create(palette, translator);

		assertThat(entries).hasSize(6 + 14 + 2);
		List<String> hexes = new ArrayList<>(neutralHexes());
		hexes.addAll(hueHexes());
		hexes.add(hex(200, 600));
		hexes.add(hex(201, 500));
		assertThat(entries).extracting(Entry::hex).containsExactlyElementsOf(hexes);

		List<String> labels = new ArrayList<>(List.of("L color.neutral.0", "L color.neutral.200", "L color.neutral.450",
				"L color.neutral.600", "L color.neutral.750", "L color.neutral.1000"));
		ColorService.HUES.forEach(hue -> labels.add("L color." + hue));
		labels.add("Theme 1");
		labels.add("Theme 2");
		assertThat(entries).extracting(Entry::label).containsExactlyElementsOf(labels);
	}

	@Test
	public void oneThemeColor() {
		List<Entry> entries = EditorColorMap.create(Palette.fromCss(css(true, true, 1)), translator);

		assertThat(entries).hasSize(6 + 14 + 1);
		assertThat(entries.get(20)).isEqualTo(new Entry(hex(200, 600), "Theme"));
	}

	@Test
	public void orderWithoutThemeColors() {
		Palette palette = Palette.fromCss(css(true, true, 0));

		List<Entry> entries = EditorColorMap.create(palette, translator);

		List<String> hexes = new ArrayList<>(neutralHexes());
		hexes.addAll(hueHexes());
		assertThat(entries).extracting(Entry::hex).containsExactlyElementsOf(hexes);
	}

	@Test
	public void lightTheme() {
		Palette palette = PaletteTest.lightPalette();

		List<Entry> entries = EditorColorMap.create(palette, translator);

		assertThat(entries).hasSize(6 + 14 + palette.getThemeColors().size());
		assertThat(entries.get(0).hex()).isEqualTo("#FFFFFF");
		// the text colour of the light theme
		assertThat(entries.get(4).hex()).isEqualTo("#344054");
		assertThat(entries.get(5).hex()).isEqualTo("#000000");
		assertThat(entries.get(6).hex()).isEqualTo(palette.bg(new Swatch("red", Swatch.SOLID)));
		assertThat(entries.get(20).hex()).isEqualTo(palette.bg(new Swatch("theme1", Swatch.SOLID)));
		assertThat(EditorColorMap.toTinyMceOptions(entries)).matches(OPTIONS);
	}

	/**
	 * The map is the set M: the same colours as before the redesign of the
	 * sets, the solid fill of the 14 hues and of all theme colours.
	 */
	@Test
	public void openolatThemeIsSetM() throws IOException {
		Palette palette = Palette.fromCss(new String(Files.readAllBytes(OPENOLAT_THEME_CSS), StandardCharsets.UTF_8));
		assertThat(palette.getThemeColors()).containsExactly("theme1", "theme2", "theme3", "theme4");

		List<Entry> entries = EditorColorMap.create(palette, translator);

		// the former map, built without the sets
		List<String> hexes = new ArrayList<>();
		for (Integer step : EditorColorMap.NEUTRAL_STEPS) {
			hexes.add(palette.neutral(step.intValue()));
		}
		for (String hue : ColorService.HUES) {
			hexes.add(palette.bg(new Swatch(hue, Swatch.SOLID)));
		}
		for (String themeColor : palette.getThemeColors()) {
			hexes.add(palette.bg(new Swatch(themeColor, Swatch.SOLID)));
		}
		assertThat(entries).extracting(Entry::hex).containsExactlyElementsOf(hexes);
		assertThat(entries).hasSize(6 + 14 + 4);
		// the colours of the set M
		List<String> setM = ColorSet.M.getKeysWithThemeColors(palette.getThemeColors());
		assertThat(setM).hasSize(14 + 4);
		for (int i = 0; i < setM.size(); i++) {
			assertThat(entries.get(6 + i).hex()).as(setM.get(i)).isEqualTo(palette.bg(Swatch.fromKey(setM.get(i))));
		}
		assertThat(entries.get(20).label()).isEqualTo("Theme 1");
		assertThat(EditorColorMap.toTinyMceOptions(entries)).matches(OPTIONS);
	}

	@Test
	public void neutralSteps() {
		Palette palette = Palette.fromCss(css(true, true, 1));

		assertThat(palette.getNeutralSteps()).containsExactly(0, 200, 450, 600, 750, 1000);
		assertThat(palette.neutral(750)).isEqualTo(hex(300, 750));
		assertThat(palette.neutral(9999)).isNull();
		assertThat(palette.contains(Palette.NEUTRAL)).isFalse();
		assertThat(palette.withoutThemeColors().neutral(750)).isEqualTo(hex(300, 750));
	}

	@Test
	public void noPalette() {
		assertThat(EditorColorMap.create(null, translator)).isEmpty();
		assertThat(EditorColorMap.create(Palette.fromCss(css(true, true, 0)), null)).isEmpty();
		assertThat(EditorColorMap.toTinyMceOptions(null)).isEmpty();
		assertThat(EditorColorMap.toTinyMceOptions(List.of())).isEmpty();
	}

	/**
	 * A theme.css without the neutral scale (an older compile) gives the hues
	 * only; a palette with one hue gives this hue only.
	 */
	@Test
	public void brokenPalette() {
		List<Entry> withoutNeutral = EditorColorMap.create(Palette.fromCss(css(false, true, 0)), translator);
		assertThat(withoutNeutral).extracting(Entry::hex).containsExactlyElementsOf(hueHexes());

		List<Entry> redOnly = EditorColorMap.create(Palette.fromCss(css(false, false, 0)), translator);
		assertThat(redOnly).extracting(Entry::label).containsExactly("L color.red");
	}

	@Test
	public void translatorFails() {
		Translator failing = mock(Translator.class);
		when(failing.translate(anyString())).thenThrow(new IllegalStateException("no bundle"));
		when(failing.translate(anyString(), any(String[].class))).thenThrow(new IllegalStateException("no bundle"));

		List<Entry> entries = EditorColorMap.create(Palette.fromCss(css(true, true, 1)), failing);

		// the colour is the label
		assertThat(entries).hasSize(21);
		assertThat(entries).allMatch(entry -> entry.hex().equals(entry.label()));
	}

	@Test
	public void hostileLabelEscaped() {
		String hostile = "x'\"</script><script>alert(1)</script>\\  \n`${a}`";
		List<Entry> entries = List.of(new Entry("#FF0000", hostile), new Entry("#00FF00", "Grün"));

		String options = EditorColorMap.toTinyMceOptions(entries);

		assertThat(options).matches(OPTIONS);
		assertThat(options).doesNotContain("<", ">", "\"", "`", " ", " ", "${");
		assertThat(options).startsWith("color_map: ['#FF0000','x\\u0027\\u0022\\u003C\\u002Fscript\\u003E");
		assertThat(options).contains("'#00FF00','Gr\\u00FCn'");
	}

	@Test
	public void invalidHexDropped() {
		List<Entry> entries = Arrays.asList(
				new Entry("#ff0000", "lower case"),
				new Entry("red", "name"),
				new Entry("#FF0000');alert(1);//", "injection"),
				new Entry("#FF00", "short"),
				new Entry(null, "null"),
				null,
				new Entry("#00FF00", "Green"));

		assertThat(EditorColorMap.toTinyMceOptions(entries)).isEqualTo("color_map: ['#00FF00','Green'],\ncolor_cols: 6,\n");
		assertThat(EditorColorMap.toTinyMceOptions(entries.subList(0, 4))).isEmpty();
		assertThat(EditorColorMap.isValidHex("#00FF00")).isTrue();
		assertThat(EditorColorMap.isValidHex("#00ff00")).isFalse();
		assertThat(EditorColorMap.isValidHex(null)).isFalse();
	}

	@Test
	public void nullLabelIsHex() {
		assertThat(EditorColorMap.toTinyMceOptions(List.of(new Entry("#00FF00", null))))
				.isEqualTo("color_map: ['#00FF00','\\u002300FF00'],\ncolor_cols: 6,\n");
	}

	/**
	 * @param neutral true for the neutral steps of the map
	 * @param allHues true for the 14 hues, else red only
	 * @param themeColors The number of theme colours; theme1 has its fill at
	 * 		step 600, the others have no fill property (step 500)
	 */
	private static String css(boolean neutral, boolean allHues, int themeColors) {
		StringBuilder css = new StringBuilder(":root{");
		if (neutral) {
			for (Integer step : EditorColorMap.NEUTRAL_STEPS) {
				css.append("--o-neutral-").append(step).append(": ").append(hex(300, step.intValue())).append(";");
			}
		}
		List<String> hues = allHues ? ColorService.HUES : List.of("red");
		for (int i = 0; i < hues.size(); i++) {
			for (Integer step : Palette.STEPS) {
				css.append("--o-").append(hues.get(i)).append("-").append(step).append(": ")
					.append(hex(i, step.intValue())).append(";");
			}
		}
		for (int n = 1; n <= themeColors; n++) {
			for (Integer step : Palette.STEPS) {
				css.append("--o-theme").append(n).append("-").append(step).append(": ")
					.append(hex(199 + n, step.intValue())).append(";");
			}
		}
		if (themeColors > 0) {
			css.append("--o-theme1: ").append(hex(200, 600)).append(";");
		}
		return css.append("}").toString();
	}

	private static List<String> neutralHexes() {
		return EditorColorMap.NEUTRAL_STEPS.stream().map(step -> hex(300, step.intValue())).toList();
	}

	private static List<String> hueHexes() {
		List<String> hexes = new ArrayList<>();
		for (int i = 0; i < ColorService.HUES.size(); i++) {
			hexes.add(hex(i, Palette.bgStep(new Swatch(ColorService.HUES.get(i), Swatch.SOLID))));
		}
		return hexes;
	}

	/**
	 * @return A distinct colour per colour index and step, e.g. "#0C1E00"
	 */
	private static String hex(int index, int step) {
		return String.format("#%02X%02X%02X", Integer.valueOf(index % 256), Integer.valueOf(step / 10 % 256), Integer.valueOf(step % 10));
	}
}

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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.themes.Theme;
import org.olat.core.helpers.GUISettings;
import org.olat.core.util.event.FrameworkStartupEventChannel;

/**
 * Checks the chooser sets {@link ColorSet}: the keys and their order, the
 * parsing of the keys ({@link Swatch#fromKey(String)}), their resolution in
 * the family {@link ColorFamily#SWATCH}, the CSS classes and the soft roles
 * of the {@link Palette} against the compiled themes.
 *
 * Initial date: 24 Nov 2026<br>
 * @author gnaegi, https://www.frentix.com
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class ColorSetTest {

	private static final Path OPENOLAT_THEME_CSS = Path.of("src/main/webapp/static/themes/openolat/theme.css");

	private static final List<String> HUES = List.of("red", "orange", "yellow", "lime", "green", "teal", "cyan",
			"blue", "violet", "magenta", "pink", "brown", "slate", "gray");

	/** The hues of the set S */
	private static final List<String> S_HUES = List.of("red", "orange", "yellow", "green", "blue", "pink", "slate");

	@Mock
	private GUISettings guiSettings;
	@InjectMocks
	private ColorServiceImpl colorService;

	private final Palette lightPalette = PaletteTest.lightPalette();

	private void useTheme(Palette palette) {
		Theme theme = mock(Theme.class);
		when(theme.getPalette()).thenReturn(palette);
		when(guiSettings.getGuiTheme()).thenReturn(theme);
		when(guiSettings.getGuiThemeIdentifyer()).thenReturn("test");
	}

	private static Palette openolatPalette() throws IOException {
		return Palette.fromCss(new String(Files.readAllBytes(OPENOLAT_THEME_CSS), StandardCharsets.UTF_8));
	}

	@Test
	public void hues() {
		assertThat(ColorService.HUES).containsExactlyElementsOf(HUES);
		// every palette has the hues, in the same order
		assertThat(List.copyOf(lightPalette.getHues())).containsExactlyElementsOf(HUES);
		assertThat(ColorService.DEFAULT_COLOR_KEY).isEqualTo("blue");
		assertThat(ColorService.HUES).contains(ColorService.DEFAULT_COLOR_KEY);
	}

	@Test
	public void variantsPerSet() {
		assertThat(ColorSet.S.getVariants()).containsExactly("solid");
		assertThat(ColorSet.M.getVariants()).containsExactly("solid");
		assertThat(ColorSet.L.getVariants()).containsExactly("soft", "light", "tint", "solid", "dark");
		assertThat(ColorSet.L.getKeys(null)).isEmpty();
		assertThat(ColorSet.L.getKeys(List.of())).isEmpty();
	}

	@Test
	public void huesPerSet() {
		assertThat(ColorSet.S.getHues()).containsExactlyElementsOf(S_HUES);
		assertThat(ColorSet.M.getHues()).containsExactlyElementsOf(HUES);
		assertThat(ColorSet.L.getHues()).containsExactlyElementsOf(HUES);
		for (ColorSet colorSet : ColorSet.values()) {
			// a subset of the hues, in the order of the hues
			List<String> ordered = HUES.stream().filter(colorSet.getHues()::contains).toList();
			assertThat(colorSet.getHues()).as(colorSet.name()).containsExactlyElementsOf(ordered);
		}
		assertThat(ColorSet.S.getHues()).contains(ColorService.DEFAULT_COLOR_KEY);
	}

	@Test
	public void themeColorsPerSet() {
		List<String> four = List.of("theme1", "theme2", "theme3", "theme4");
		assertThat(ColorSet.S.getThemeColors(four)).containsExactly("theme1");
		assertThat(ColorSet.M.getThemeColors(four)).containsExactlyElementsOf(four);
		assertThat(ColorSet.L.getThemeColors(four)).containsExactlyElementsOf(four);
		assertThat(ColorSet.S.getThemeColors(List.of("theme1"))).containsExactly("theme1");
		for (ColorSet colorSet : ColorSet.values()) {
			assertThat(colorSet.getThemeColors(List.of())).isEmpty();
			assertThat(colorSet.getThemeColors(null)).isEmpty();
		}
		// S has the first theme colour only
		assertThat(ColorSet.S.getThemeColors(List.of("theme2"))).isEmpty();
	}

	@Test
	public void setsWithoutThemeColors() {
		useTheme(lightPalette.withoutThemeColors());

		List<String> s = colorService.getColors(ColorSet.S);
		assertThat(s).containsExactly("red", "orange", "yellow", "green", "blue", "pink", "slate");

		List<String> m = colorService.getColors(ColorSet.M);
		assertThat(m).containsExactlyElementsOf(HUES);

		List<String> l = colorService.getColors(ColorSet.L);
		assertThat(l).hasSize(70);
		assertThat(l.subList(0, 5)).containsExactly("red-soft", "red-light", "red-tint", "red", "red-dark");
		assertThat(l.subList(65, 70)).containsExactly("gray-soft", "gray-light", "gray-tint", "gray", "gray-dark");
		assertThat(l).doesNotHaveDuplicates();
	}

	@Test
	public void setsWithThemeColors() throws IOException {
		useTheme(openolatPalette());

		// S: the first theme colour only
		assertThat(colorService.getColors(ColorSet.S)).containsExactlyElementsOf(concat(S_HUES, List.of("theme1")));

		List<String> m = colorService.getColors(ColorSet.M);
		assertThat(m).containsExactlyElementsOf(concat(HUES, List.of("theme1", "theme2", "theme3", "theme4")));
		assertThat(m.subList(0, 14)).containsExactlyElementsOf(colorService.getHueColors(ColorSet.M));

		List<String> l = colorService.getColors(ColorSet.L);
		assertThat(l).hasSize(70 + 20);
		assertThat(l.subList(70, 75)).containsExactly("theme1-soft", "theme1-light", "theme1-tint", "theme1", "theme1-dark");
		assertThat(l.subList(85, 90)).containsExactly("theme4-soft", "theme4-light", "theme4-tint", "theme4", "theme4-dark");
	}

	@Test
	public void setsWithOneThemeColor() {
		useTheme(lightPalette);

		assertThat(colorService.getColors(ColorSet.S)).containsExactlyElementsOf(concat(S_HUES, List.of("theme1")));
		assertThat(colorService.getColors(ColorSet.M)).containsExactlyElementsOf(concat(HUES, List.of("theme1")));
		List<String> l = colorService.getColors(ColorSet.L);
		assertThat(l).hasSize(70 + 5);
		assertThat(l.subList(70, 75)).containsExactly("theme1-soft", "theme1-light", "theme1-tint", "theme1", "theme1-dark");
	}

	@Test
	public void setsWithoutTheme() {
		// no GUI settings, e.g. in a unit test: the hues only
		ColorService plain = new ColorServiceImpl();
		assertThat(plain.getColors(ColorSet.S)).containsExactlyElementsOf(S_HUES);
		assertThat(plain.getColors(ColorSet.M)).containsExactlyElementsOf(HUES);
		assertThat(plain.getColors(ColorSet.L)).hasSize(70);
	}

	@Test
	public void hueColorsWithoutThemeColors() throws IOException {
		useTheme(openolatPalette());

		assertThat(colorService.getHueColors(ColorSet.S)).containsExactlyElementsOf(S_HUES);
		assertThat(colorService.getHueColors(ColorSet.M)).containsExactlyElementsOf(HUES);
		assertThat(colorService.getHueColors(ColorSet.L)).hasSize(70).noneMatch(key -> key.startsWith("theme"));
	}

	@Test
	public void parseKeys() {
		assertThat(Swatch.fromKey("blue")).isEqualTo(new Swatch("blue", Swatch.SOLID));
		assertThat(Swatch.fromKey("blue-soft")).isEqualTo(new Swatch("blue", Swatch.SOFT));
		assertThat(Swatch.fromKey("blue-light")).isEqualTo(new Swatch("blue", Swatch.LIGHT));
		assertThat(Swatch.fromKey("blue-tint")).isEqualTo(new Swatch("blue", Swatch.TINT));
		assertThat(Swatch.fromKey("blue-dark")).isEqualTo(new Swatch("blue", Swatch.DARK));
		assertThat(Swatch.fromKey("theme1")).isEqualTo(new Swatch("theme1", Swatch.SOLID));
		assertThat(Swatch.fromKey("theme12-tint")).isEqualTo(new Swatch("theme12", Swatch.TINT));
		// the form only: theme7 is a theme colour key without a theme
		assertThat(Swatch.fromKey("theme7-dark")).isEqualTo(new Swatch("theme7", Swatch.DARK));
	}

	@Test
	public void keyRoundTrip() throws IOException {
		Palette openolat = openolatPalette();
		for (ColorSet colorSet : ColorSet.values()) {
			for (String key : colorSet.getKeysWithThemeColors(openolat.getThemeColors())) {
				Swatch swatch = Swatch.fromKey(key);
				assertThat(swatch).as(key).isNotNull();
				assertThat(swatch.key()).isEqualTo(key);
			}
		}
	}

	@Test
	public void unknownKeys() {
		for (String key : new String[] { null, "", "-", "blue-", "-light", "blue-solid", "blue-pale", "blue-light-dark",
				"Blue", "BLUE-LIGHT", "lightblue", "grey", "theme", "theme-light", "theme1x", "o_swatch_blue", "blue light" }) {
			assertThat(Swatch.fromKey(key)).as(key).isNull();
		}
	}

	@Test
	public void resolveVariants() {
		useTheme(lightPalette);

		for (String hue : HUES) {
			assertThat(colorService.resolve(hue, ColorFamily.SWATCH)).isEqualTo(new Swatch(hue, Swatch.SOLID));
			for (String variant : List.of(Swatch.SOFT, Swatch.LIGHT, Swatch.TINT, Swatch.DARK)) {
				Swatch expected = new Swatch(hue, variant);
				assertThat(colorService.resolve(hue + "-" + variant, ColorFamily.SWATCH)).isEqualTo(expected);
				// the swatch class defines the family
				assertThat(colorService.resolve("o_swatch_" + hue + "-" + variant, null)).isEqualTo(expected);
				assertThat(colorService.resolve("o_swatch_" + hue + "-" + variant, ColorFamily.CALENDAR)).isEqualTo(expected);
			}
			assertThat(colorService.resolve(" " + hue + "-light ", ColorFamily.SWATCH)).isEqualTo(new Swatch(hue, Swatch.LIGHT));
		}
	}

	@Test
	public void resolveUnknownKeys() {
		useTheme(lightPalette);

		assertThat(colorService.resolve(null, ColorFamily.SWATCH)).isNull();
		assertThat(colorService.resolve(" ", ColorFamily.SWATCH)).isNull();
		assertThat(colorService.resolve("blue-solid", ColorFamily.SWATCH)).isNull();
		assertThat(colorService.resolve("blue-pale", ColorFamily.SWATCH)).isNull();
		// legacy keys are no swatch keys
		assertThat(colorService.resolve("lightblue", ColorFamily.SWATCH)).isNull();
		assertThat(colorService.resolve("grey", ColorFamily.SWATCH)).isNull();
		assertThat(colorService.resolve("o_swatch_lightblue", ColorFamily.COLOR_SERVICE)).isNull();
		// a swatch key needs the family SWATCH or the prefix o_swatch_
		assertThat(colorService.resolve("blue-light", null)).isNull();
		assertThat(colorService.resolve("blue-light", ColorFamily.CALENDAR)).isNull();
		assertThat(colorService.resolve("teal", ColorFamily.CALENDAR)).isNull();
		// a legacy prefix keeps its family
		assertThat(colorService.resolve("o_cal_teal", ColorFamily.SWATCH)).isNull();
		assertThat(colorService.resolve("o_cal_navy", ColorFamily.SWATCH)).isEqualTo(new Swatch("blue", Swatch.DARK));
		assertThat(colorService.resolve("o_cal_olive", ColorFamily.SWATCH)).isEqualTo(new Swatch("lime", Swatch.SOLID));
		// the calendar classes of the swatch keys are no aliases any more (upgrade to 21.2)
		assertThat(colorService.resolve("o_cal_blue", ColorFamily.SWATCH)).isNull();
		assertThat(colorService.resolve("o_cal_yellow", ColorFamily.SWATCH)).isNull();
	}

	@Test
	public void legacyFamiliesUnchanged() {
		useTheme(lightPalette);

		// colliding bare keys keep the meaning of the legacy family
		assertThat(colorService.resolve("lightgray", ColorFamily.COLOR_SERVICE)).isEqualTo(new Swatch("gray", Swatch.DARK));
		assertThat(colorService.resolve("orange", ColorFamily.COLOR_SERVICE)).isEqualTo(new Swatch("orange", Swatch.SOLID));
		// the calendar colours that are swatch keys have no alias: the swatch renders them
		assertThat(colorService.resolve("yellow", ColorFamily.CALENDAR)).isNull();
		assertThat(colorService.resolveStored("yellow", ColorFamily.CALENDAR)).isEqualTo(new Swatch("yellow", Swatch.SOLID));
		assertThat(colorService.resolve("blue", ColorFamily.COLOR_SERVICE)).isNull();
		// the same keys in the family SWATCH have the new meaning
		assertThat(colorService.resolve("yellow", ColorFamily.SWATCH)).isEqualTo(new Swatch("yellow", Swatch.SOLID));
		assertThat(colorService.resolve("blue", ColorFamily.SWATCH)).isEqualTo(new Swatch("blue", Swatch.SOLID));
		assertThat(colorService.resolve("orange", ColorFamily.SWATCH)).isEqualTo(new Swatch("orange", Swatch.SOLID));
	}

	@Test
	public void resolveThemeKeys() throws IOException {
		useTheme(openolatPalette());

		assertThat(colorService.resolve("theme3", ColorFamily.SWATCH)).isEqualTo(new Swatch("theme3", Swatch.SOLID));
		assertThat(colorService.resolve("theme3-dark", ColorFamily.SWATCH)).isEqualTo(new Swatch("theme3", Swatch.DARK));
		assertThat(colorService.resolve("o_swatch_theme2-soft", null)).isEqualTo(new Swatch("theme2", Swatch.SOFT));
		// theme colours resolve in every family, as before
		assertThat(colorService.resolve("theme4-light", null)).isEqualTo(new Swatch("theme4", Swatch.LIGHT));
		assertThat(colorService.resolve("theme2", ColorFamily.CALENDAR)).isEqualTo(new Swatch("theme2", Swatch.SOLID));
		// a theme colour the theme does not define: theme1 with the same variant
		assertThat(colorService.resolve("theme9-tint", ColorFamily.SWATCH)).isEqualTo(new Swatch("theme1", Swatch.TINT));
		assertThat(colorService.resolve("theme9", ColorFamily.SWATCH)).isEqualTo(new Swatch("theme1", Swatch.SOLID));
		assertThat(colorService.resolve("theme1-solid", ColorFamily.SWATCH)).isNull();
	}

	@Test
	public void resolveThemeKeysWithoutThemeColors() {
		useTheme(lightPalette.withoutThemeColors());

		assertThat(colorService.resolve("theme1-light", ColorFamily.SWATCH)).isNull();
		assertThat(colorService.toSwatchCssClass("theme1")).isNull();
	}

	@Test
	public void cssClasses() {
		useTheme(lightPalette);

		assertThat(colorService.toSwatchCssClass("blue")).isEqualTo("o_swatch_blue");
		assertThat(colorService.toSwatchCssClass("blue-soft")).isEqualTo("o_swatch_blue o_swatch_soft");
		assertThat(colorService.toSwatchCssClass("blue-light")).isEqualTo("o_swatch_blue o_swatch_light");
		assertThat(colorService.toSwatchCssClass("blue-tint")).isEqualTo("o_swatch_blue o_swatch_tint");
		assertThat(colorService.toSwatchCssClass("blue-dark")).isEqualTo("o_swatch_blue o_swatch_dark");
		assertThat(colorService.toSwatchCssClass("theme1-light")).isEqualTo("o_swatch_theme1 o_swatch_light");
		// light has one theme colour: theme3 renders as theme1
		assertThat(colorService.toSwatchCssClass("theme3-dark")).isEqualTo("o_swatch_theme1 o_swatch_dark");
		assertThat(colorService.toSwatchCssClass("lightblue")).isNull();
		assertThat(colorService.toSwatchCssClass(null)).isNull();
	}

	@Test
	public void nearestKey() {
		// M: the hue, solid
		assertThat(ColorSet.M.nearestKey(new Swatch("blue", Swatch.DARK))).isEqualTo("blue");
		assertThat(ColorSet.M.nearestKey(new Swatch("yellow", Swatch.TINT))).isEqualTo("yellow");
		assertThat(ColorSet.M.nearestKey(new Swatch("teal", Swatch.SOFT))).isEqualTo("teal");
		assertThat(ColorSet.M.nearestKey(new Swatch("gray", Swatch.SOLID))).isEqualTo("gray");
		assertThat(ColorSet.M.nearestKey(new Swatch("theme2", Swatch.LIGHT))).isEqualTo("theme2");
		// S: only its hues and theme1
		assertThat(ColorSet.S.nearestKey(new Swatch("red", Swatch.DARK))).isEqualTo("red");
		assertThat(ColorSet.S.nearestKey(new Swatch("theme1", Swatch.DARK))).isEqualTo("theme1");
		assertThat(ColorSet.S.nearestKey(new Swatch("cyan", Swatch.SOLID))).isNull();
		assertThat(ColorSet.S.nearestKey(new Swatch("theme2", Swatch.SOLID))).isNull();
		// L: the swatch itself
		assertThat(ColorSet.L.nearestKey(new Swatch("blue", Swatch.DARK))).isEqualTo("blue-dark");
		assertThat(ColorSet.L.nearestKey(new Swatch("blue", Swatch.SOLID))).isEqualTo("blue");
		assertThat(ColorSet.L.nearestKey(new Swatch("theme3", Swatch.SOFT))).isEqualTo("theme3-soft");
		// unknown variant: solid
		assertThat(ColorSet.L.nearestKey(new Swatch("blue", "junk"))).isEqualTo("blue");
		// no swatch, no hue of the palette
		assertThat(ColorSet.M.nearestKey(null)).isNull();
		assertThat(ColorSet.M.nearestKey(new Swatch(null, Swatch.SOLID))).isNull();
		assertThat(ColorSet.M.nearestKey(new Swatch("lightblue", Swatch.SOLID))).isNull();
		// every key of a set is its own nearest key
		for (ColorSet colorSet : ColorSet.values()) {
			for (String key : colorSet.getKeysWithThemeColors(List.of("theme1", "theme2"))) {
				assertThat(colorSet.nearestKey(Swatch.fromKey(key))).as(colorSet + " " + key).isEqualTo(key);
			}
		}
	}

	@Test
	public void nearestKeyOfStoredValues() throws IOException {
		useTheme(openolatPalette());

		// a key of the set M stays
		assertThat(colorService.getNearestKey(ColorSet.M, "blue", ColorFamily.CALENDAR)).isEqualTo("blue");
		assertThat(colorService.getNearestKey(ColorSet.M, "theme3", ColorFamily.CALENDAR)).isEqualTo("theme3");
		// old values: the hue of their alias
		assertThat(colorService.getNearestKey(ColorSet.M, "navy", ColorFamily.CALENDAR)).isEqualTo("blue");
		assertThat(colorService.getNearestKey(ColorSet.M, "rebeccapurple", ColorFamily.CALENDAR)).isEqualTo("violet");
		assertThat(colorService.getNearestKey(ColorSet.M, "o_cal_navy", ColorFamily.CALENDAR)).isEqualTo("blue");
		assertThat(colorService.getNearestKey(ColorSet.M, "o_cal_grey", null)).isEqualTo("gray");
		assertThat(colorService.getNearestKey(ColorSet.M, "o_color_lightblue", ColorFamily.COLOR_SERVICE)).isEqualTo("cyan");
		assertThat(colorService.getNearestKey(ColorSet.M, "o_color_seagreen", null)).isEqualTo("green");
		// a variant of the set L: its hue
		assertThat(colorService.getNearestKey(ColorSet.M, "teal-dark", ColorFamily.CALENDAR)).isEqualTo("teal");
		// a theme colour the theme does not define: theme1
		assertThat(colorService.getNearestKey(ColorSet.M, "theme9-light", ColorFamily.CALENDAR)).isEqualTo("theme1");
		// unknown and junk
		assertThat(colorService.getNearestKey(ColorSet.M, "cobaltblue", ColorFamily.CALENDAR)).isNull();
		assertThat(colorService.getNearestKey(ColorSet.M, "o_cal_purple", ColorFamily.CALENDAR)).isNull();
		assertThat(colorService.getNearestKey(ColorSet.M, null, ColorFamily.CALENDAR)).isNull();
		assertThat(colorService.getNearestKey(null, "blue", ColorFamily.CALENDAR)).isNull();
		for (String junk : ColorValidatorTest.JUNK) {
			assertThat(colorService.getNearestKey(ColorSet.M, junk, ColorFamily.CALENDAR)).as(junk).isNull();
		}
	}

	@Test
	public void storedValueCssClasses() throws IOException {
		useTheme(openolatPalette());

		// keys of the chooser sets: the swatch classes
		assertThat(colorService.toStoredValueCssClass("blue")).isEqualTo("o_swatch_blue");
		assertThat(colorService.toStoredValueCssClass(" teal ")).isEqualTo("o_swatch_teal");
		assertThat(colorService.toStoredValueCssClass("theme2")).isEqualTo("o_swatch_theme2");
		assertThat(colorService.toStoredValueCssClass("blue-light")).isEqualTo("o_swatch_blue o_swatch_light");
		assertThat(colorService.toStoredValueCssClass("theme9")).isEqualTo("o_swatch_theme1");
		// other classes stay, e.g. the grey initials or an old class that the alias classes of the theme render
		assertThat(colorService.toStoredValueCssClass("o_user_initials_grey")).isEqualTo("o_user_initials_grey");
		assertThat(colorService.toStoredValueCssClass("o_color_lightblue")).isEqualTo("o_color_lightblue");
		assertThat(colorService.toStoredValueCssClass("o_swatch_blue")).isEqualTo("o_swatch_blue");
		// no class
		assertThat(colorService.toStoredValueCssClass(null)).isNull();
		assertThat(colorService.toStoredValueCssClass(" ")).isNull();
		assertThat(colorService.toStoredValueCssClass("o_color_blue\" onmouseover=\"x")).isNull();
		for (String junk : ColorValidatorTest.JUNK) {
			String cssClass = colorService.toStoredValueCssClass(junk);
			assertThat(cssClass == null || ColorValidator.isValidCssClasses(cssClass)).as(junk).isTrue();
		}
	}

	@Test
	public void allSetKeysHaveASwatch() throws IOException {
		assertThat(ColorServiceImpl.missingColorSetKeys(lightPalette)).isEmpty();
		assertThat(ColorServiceImpl.missingColorSetKeys(lightPalette.withoutThemeColors())).isEmpty();
		assertThat(ColorServiceImpl.missingColorSetKeys(openolatPalette())).isEmpty();
	}

	@Test
	public void missingHueReported() {
		Palette withoutTeal = withoutTeal();

		// teal is no hue of S, the solid of M, the variants of L
		assertThat(ColorServiceImpl.missingColorSetKeys(withoutTeal))
				.containsExactly("teal", "teal-soft", "teal-light", "teal-tint", "teal-dark");
		// the startup check reports each key once
		useTheme(withoutTeal);
		assertThat(colorService.checkColorSets())
				.containsExactly("teal", "teal-soft", "teal-light", "teal-tint", "teal-dark");
	}

	@Test
	public void startupEventChecksPalette() {
		useTheme(withoutTeal());

		colorService.event(FrameworkStartupEventChannel.getEvent());

		// the palette is read and the theme named in the warnings
		verify(guiSettings).getGuiTheme();
		verify(guiSettings).getGuiThemeIdentifyer();
	}

	@Test
	public void otherEventChecksNothing() {
		colorService.event(new Event("other"));

		verifyNoInteractions(guiSettings);
	}

	@Test
	public void startupCheckSurvivesExceptionOfGuiSettings() {
		when(guiSettings.getGuiTheme()).thenThrow(new IllegalStateException("broken theme"));

		// nothing escapes the start
		colorService.afterPropertiesSet();
		colorService.event(FrameworkStartupEventChannel.getEvent());
		assertThat(colorService.checkColorSets()).isEmpty();
		// no palette, the sets build from the hues
		assertThat(colorService.getPalette()).isNull();
		assertThat(colorService.getThemeColors()).isEmpty();
		assertThat(colorService.getColors(ColorSet.S)).containsExactlyElementsOf(S_HUES);
		assertThat(colorService.getColors(ColorSet.L)).hasSize(70);
		assertThat(colorService.toSwatchCssClass("blue-light")).isEqualTo("o_swatch_blue o_swatch_light");
		assertThat(colorService.toSwatchCssClass("theme1")).isNull();
		assertThat(colorService.getBackgroundColor(new Swatch("blue", Swatch.SOLID), "#123456")).isEqualTo("#123456");
	}

	@Test
	public void registrationNeverThrows() {
		useTheme(withoutTeal());

		// without the coordinator of a running framework the registration fails:
		// the palette is checked once on its first use
		colorService.afterPropertiesSet();
		colorService.afterSingletonsInstantiated();
		assertThat(colorService.getPalette()).isNotNull();
		assertThat(colorService.getPalette()).isNotNull();
	}

	@Test
	public void startupCheckSurvivesLinkageError() {
		when(guiSettings.getGuiTheme()).thenThrow(new NoClassDefFoundError("broken class"));

		colorService.event(FrameworkStartupEventChannel.getEvent());
		assertThat(colorService.getPalette()).isNull();
		assertThat(colorService.getColors(ColorSet.S)).containsExactlyElementsOf(S_HUES);
	}

	@Test
	public void startupCheckSurvivesExceptionOfThemeIdentifier() {
		useTheme(withoutTeal());
		when(guiSettings.getGuiThemeIdentifyer()).thenThrow(new IllegalStateException("no identifier"));

		colorService.event(FrameworkStartupEventChannel.getEvent());
	}

	@Test
	public void startupWithoutPalette() {
		Theme theme = mock(Theme.class);
		when(theme.getPalette()).thenReturn(null);
		when(guiSettings.getGuiTheme()).thenReturn(theme);

		colorService.afterPropertiesSet();
		colorService.event(FrameworkStartupEventChannel.getEvent());
		assertThat(colorService.getColors(ColorSet.M)).containsExactlyElementsOf(HUES);
		assertThat(colorService.getColorsForBadges()).startsWith("gold", "silver", "bronze").hasSize(3 + HUES.size());
	}

	@Test
	public void storedValueWithThemeColors() {
		useTheme(lightPalette);
		List<String> themeColors = colorService.getThemeColors();
		assertThat(themeColors).isNotEmpty();
		String last = themeColors.get(themeColors.size() - 1);
		assertThat(colorService.toStoredValueCssClass(last, "o_video_marker_")).isEqualTo("o_swatch_" + last);
		// a theme colour that the theme does not define renders as theme1
		assertThat(colorService.toStoredValueCssClass("theme99", "o_color_")).isEqualTo("o_swatch_theme1");
		assertThat(colorService.toStoredValueCssClass("lightblue", "o_alert_box_color_")).isEqualTo("o_alert_box_color_lightblue");
	}

	@Test
	public void badgeColorsFromPalette() {
		useTheme(lightPalette);
		// the colours baked into the badge SVG (OpenBadgesManagerImpl.getColorAsRgb())
		assertThat(colorService.getColorsForBadges()).containsSubsequence("gold", "silver", "bronze", "red", "gray", "theme1");
		assertThat(colorService.getBackgroundColor(colorService.resolveStored("blue", ColorFamily.COLOR_SERVICE), Palette.WHITE))
				.isEqualTo(lightPalette.bg(new Swatch("blue", Swatch.SOLID)));
		assertThat(colorService.getBackgroundColor(colorService.resolveStored("gold", ColorFamily.COLOR_SERVICE), Palette.WHITE))
				.isEqualTo(lightPalette.bg(new Swatch("yellow", Swatch.SOLID)));
		assertThat(colorService.getBackgroundColor(colorService.resolveStored("theme1", ColorFamily.COLOR_SERVICE), Palette.WHITE))
				.isEqualTo(lightPalette.bg(new Swatch("theme1", Swatch.SOLID)));
		// an old badge colour keeps its colour
		assertThat(colorService.getBackgroundColor(colorService.resolveStored("lightblue", ColorFamily.COLOR_SERVICE), Palette.WHITE))
				.isEqualTo(lightPalette.bg(new Swatch("cyan", Swatch.SOLID)));
		// junk: white
		assertThat(colorService.getBackgroundColor(colorService.resolveStored("\"><svg onload=alert(1)>", ColorFamily.COLOR_SERVICE), Palette.WHITE))
				.isEqualTo(Palette.WHITE);
	}

	@Test
	public void startupWithoutTheme() {
		when(guiSettings.getGuiTheme()).thenReturn(null);

		colorService.afterPropertiesSet();
		colorService.event(FrameworkStartupEventChannel.getEvent());
		assertThat(colorService.getPalette()).isNull();
		assertThat(colorService.getColors(ColorSet.S)).containsExactlyElementsOf(S_HUES);
	}

	@Test
	public void startupWithBrokenThemeCss() {
		useTheme(Palette.fromCss("--o-red-99999999999999999999: #FF0000; \u0000 {{{ --o-theme1-50: #zzzzzz"));

		colorService.afterPropertiesSet();
		colorService.event(FrameworkStartupEventChannel.getEvent());
		assertThat(colorService.getColors(ColorSet.S)).containsExactlyElementsOf(S_HUES);
	}

	@Test
	public void checkColorSetsWithoutTheme() {
		// no GUI settings, e.g. in a unit test: nothing to check
		assertThat(new ColorServiceImpl().checkColorSets()).isEmpty();
	}

	/**
	 * @return The palette of a theme CSS without the hue teal
	 */
	private Palette withoutTeal() {
		StringBuilder css = new StringBuilder(":root{");
		for (String hue : HUES) {
			if (!"teal".equals(hue)) {
				for (Integer step : Palette.STEPS) {
					css.append("--o-").append(hue).append('-').append(step).append(": ")
						.append(lightPalette.hex(hue, step.intValue())).append(';');
				}
			}
		}
		css.append('}');
		return Palette.fromCss(css.toString());
	}

	@Test
	public void startupCheck() {
		// checks the form of the keys, needs no theme
		new ColorServiceImpl().checkColorKeys();
	}

	/**
	 * The soft roles of the Java palette are the tokens --o-sw-soft-bg,
	 * --o-sw-soft-fg and --o-sw-soft-border of .o_swatch_{hue} in the compiled CSS.
	 */
	@Test
	public void softRolesAsCompiled() throws IOException {
		String lightCss = new String(Files.readAllBytes(PaletteTest.LIGHT_THEME_CSS), StandardCharsets.UTF_8);
		for (String hue : concat(HUES, lightPalette.getThemeColors())) {
			assertSoftRoles(lightPalette, lightCss, hue);
		}
		String openolatCss = new String(Files.readAllBytes(OPENOLAT_THEME_CSS), StandardCharsets.UTF_8);
		Palette openolat = Palette.fromCss(openolatCss);
		for (String hue : concat(HUES, openolat.getThemeColors())) {
			assertSoftRoles(openolat, openolatCss, hue);
		}
	}

	private void assertSoftRoles(Palette palette, String css, String hue) {
		// .o_swatch_{hue} as a standalone simple selector only: not part of a compound
		// selector such as .o_color_background.o_swatch_{hue}
		Matcher rule = Pattern.compile("(?:^|(?<=[}\\s,]))\\.o_swatch_" + hue + "(?=\\s*[,{])[^{]*\\{([^}]*)\\}").matcher(css);
		String declarations = null;
		while (rule.find()) {
			if (rule.group(1).contains("--o-sw-soft-bg")) {
				declarations = rule.group(1);
				break;
			}
		}
		assertThat(declarations).as("rule of .o_swatch_" + hue + " with --o-sw-soft-bg").isNotNull();
		assertThat(token(declarations, "soft-bg")).as(hue + " soft-bg").isEqualTo(palette.softBg(hue));
		assertThat(token(declarations, "soft-fg")).as(hue + " soft-fg").isEqualTo(palette.softFg(hue));
		assertThat(token(declarations, "soft-border")).as(hue + " soft-border").isEqualTo(palette.softBorder(hue));
		// the soft variant uses the soft roles
		Swatch soft = new Swatch(hue, Swatch.SOFT);
		assertThat(palette.bg(soft)).isEqualTo(palette.softBg(hue));
		assertThat(palette.fg(soft)).isEqualTo(palette.softFg(hue));
		assertThat(ColorContrast.contrast(palette.fg(soft), palette.bg(soft))).as(hue).isGreaterThanOrEqualTo(Palette.MIN_CONTRAST);
	}

	private static String token(String declarations, String role) {
		Matcher matcher = Pattern.compile("--o-sw-" + role + ":\\s*(#[0-9A-Fa-f]{6})").matcher(declarations);
		assertThat(matcher.find()).as(role).isTrue();
		return matcher.group(1).toUpperCase();
	}

	private static List<String> concat(List<String> first, List<String> second) {
		List<String> all = new ArrayList<>(first);
		all.addAll(second);
		return all;
	}
}

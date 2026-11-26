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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

/**
 * Checks {@link ColorService#resolve(String, ColorFamily)} and
 * {@link ColorService#resolveStored(String, ColorFamily)} against the alias
 * table of the swatch aliases (<code>$o-swatch-alias-families</code> in
 * <code>_palette.scss</code>) and the compiled CSS of the legacy classes.
 * The palette is read from the compiled <code>light/theme.css</code>.
 *
 * Initial date: 29 Oct 2026<br>
 * @author gnaegi, https://www.frentix.com
 */
public class ColorServiceResolveTest {

	/**
	 * family, key, hue, variant: the alias table. The hue follows the name of
	 * the old key, the variant is the nearest (OKLab) of the five variants of
	 * the hue to the colour of the key before the palette (option A, OO-9993).
	 */
	private static final String[][] ALIASES = {
		{ "COLOR_SERVICE", "lightgray", "gray", "dark" },
		{ "COLOR_SERVICE", "yellow", "yellow", "solid" },
		{ "COLOR_SERVICE", "orange", "orange", "solid" },
		{ "COLOR_SERVICE", "brown", "brown", "solid" },
		{ "COLOR_SERVICE", "red", "red", "solid" },
		{ "COLOR_SERVICE", "orchid", "pink", "solid" },
		{ "COLOR_SERVICE", "purple", "violet", "dark" },
		{ "COLOR_SERVICE", "lightblue", "cyan", "solid" },
		{ "COLOR_SERVICE", "cobaltblue", "blue", "dark" },
		{ "COLOR_SERVICE", "darkblue", "blue", "dark" },
		{ "COLOR_SERVICE", "lightgreen", "lime", "solid" },
		{ "COLOR_SERVICE", "seagreen", "green", "dark" },
		{ "COLOR_SERVICE", "gold", "yellow", "solid" },
		{ "COLOR_SERVICE", "silver", "gray", "tint" },
		{ "COLOR_SERVICE", "bronze", "orange", "tint" },
		{ "CALENDAR", "rebeccapurple", "violet", "dark" },
		{ "CALENDAR", "fuchsia", "magenta", "solid" },
		{ "CALENDAR", "olive", "lime", "solid" },
		{ "CALENDAR", "navy", "blue", "dark" },
		{ "CALENDAR", "maroon", "red", "dark" },
		{ "CALENDAR", "grey", "gray", "solid" }
	};
	
	/** The badge colours: aliases of the family COLOR_SERVICE and the prefix o_color_ only */
	private static final List<String> BADGE_KEYS = List.of("gold", "silver", "bronze");
	
	/**
	 * The prefixes of the legacy classes and the alias table of their keys,
	 * see $o-swatch-alias-families: COLOR_SERVICE with, COLOR_LIST without
	 * the badge colours
	 */
	private static final String[][] PREFIXES = {
		{ "o_color_", "COLOR_SERVICE" },
		{ "o_video_marker_", "COLOR_LIST" },
		{ "o_alert_box_color_", "COLOR_LIST" },
		{ "o_cal_", "CALENDAR" }
	};
	
	/**
	 * The classes that no longer render: project avatars, user initials and
	 * the predefined course colour categories (upgrade to 21.2), the calendar
	 * classes of the swatch keys, the badge colours with the prefixes of the
	 * alert box and the video markers.
	 */
	private static final String[] REMOVED_CLASSES = {
		"o_proj_avatar_dark_blue", "o_proj_avatar_light_blue", "o_user_initials_red", "o_user_initials_light_green",
		"o_colcat_dark_blue", "o_colcat_light_blue", "o_colcat_purple", "o_colcat_red",
		"o_colcat_orange", "o_colcat_yellow", "o_colcat_light_green", "o_colcat_dark_green",
		"o_cal_yellow", "o_cal_orange", "o_cal_red", "o_cal_blue", "o_cal_lime", "o_cal_green",
		"o_video_marker_gold", "o_video_marker_silver", "o_alert_box_color_bronze"
	};
	
	private static final Path OPENOLAT_THEME_CSS = Path.of("src/main/webapp/static/themes/openolat/theme.css");
	private static final Pattern RULE = Pattern.compile("([^{}]+)\\{([^{}]*)\\}");
	private static final Pattern SWATCH_TOKEN = Pattern.compile("--o-sw-([a-z-]+):\\s*(#[0-9A-Fa-f]{6})");

	private final ColorService colorService = new ColorServiceImpl();
	private final Palette palette = PaletteTest.lightPalette();

	@Test
	public void aliasTable() {
		for (String[] alias : ALIASES) {
			ColorFamily family = ColorFamily.valueOf(alias[0]);
			Swatch expected = new Swatch(alias[2], alias[3]);
			Assert.assertEquals(alias[0] + " " + alias[1], expected, colorService.resolve(alias[1], family));
			// every swatch exists in the palette
			Assert.assertNotNull(palette.bg(expected));
		}
		Assert.assertEquals(15, ColorServiceImpl.COLOR_SERVICE_ALIASES.size());
		Assert.assertEquals(12, ColorServiceImpl.COLOR_LIST_ALIASES.size());
		Assert.assertEquals(6, ColorServiceImpl.CALENDAR_ALIASES.size());
	}

	@Test
	public void prefixedClasses() {
		for (String[] alias : ALIASES) {
			Swatch expected = new Swatch(alias[2], alias[3]);
			String key = alias[1];
			switch (ColorFamily.valueOf(alias[0])) {
				case COLOR_SERVICE -> {
					assertResolve(expected, "o_color_" + key);
					// the badge colours were never stored with these prefixes
					Swatch expectedColorList = BADGE_KEYS.contains(key) ? null : expected;
					assertResolve(expectedColorList, "o_video_marker_" + key);
					assertResolve(expectedColorList, "o_alert_box_color_" + key);
				}
				case CALENDAR -> assertResolve(expected, "o_cal_" + key);
				default -> Assert.fail();
			}
		}
	}

	private void assertResolve(Swatch expected, String cssClass) {
		// the prefix wins over the family, the family may be null
		Assert.assertEquals(cssClass, expected, colorService.resolve(cssClass, null));
		for (ColorFamily family : ColorFamily.values()) {
			Assert.assertEquals(cssClass, expected, colorService.resolve(cssClass, family));
		}
	}

	@Test
	public void ambiguousKeys() {
		Assert.assertEquals(new Swatch("orange", Swatch.SOLID), colorService.resolve("orange", ColorFamily.COLOR_SERVICE));
		// the calendar colour "orange" is a swatch key: no alias
		Assert.assertNull(colorService.resolve("orange", ColorFamily.CALENDAR));
		Assert.assertEquals(new Swatch("orange", Swatch.SOLID), colorService.resolveStored("orange", ColorFamily.CALENDAR));
		Assert.assertNull(colorService.resolve("orange", null));
	}

	/**
	 * The classes of the removed alias families do not resolve: they are no
	 * stored values any more and the theme has no rule for them.
	 */
	@Test
	public void removedClasses() {
		for (String cssClass : REMOVED_CLASSES) {
			for (ColorFamily family : familiesAndNull()) {
				Assert.assertNull(cssClass, colorService.resolve(cssClass, family));
				Assert.assertNull(cssClass, colorService.resolveStored(cssClass, family));
			}
			Assert.assertFalse(cssClass, colorService.isCalendarCssClass(cssClass));
		}
	}

	/**
	 * A stored value that is a key of the chooser sets renders as that
	 * swatch, also if it is a key of the legacy family: the new key wins.
	 */
	@Test
	public void resolveStoredSwatchFirst() {
		Swatch yellow = new Swatch("yellow", Swatch.SOLID);
		Swatch orange = new Swatch("orange", Swatch.SOLID);
		Swatch red = new Swatch("red", Swatch.SOLID);
		// calendar: yellow, orange, red, blue, green, lime
		Assert.assertEquals(yellow, colorService.resolveStored("yellow", ColorFamily.CALENDAR));
		Assert.assertEquals(orange, colorService.resolveStored("orange", ColorFamily.CALENDAR));
		Assert.assertEquals(red, colorService.resolveStored("red", ColorFamily.CALENDAR));
		Assert.assertEquals(new Swatch("blue", Swatch.SOLID), colorService.resolveStored("blue", ColorFamily.CALENDAR));
		Assert.assertEquals(new Swatch("green", Swatch.SOLID), colorService.resolveStored("green", ColorFamily.CALENDAR));
		Assert.assertEquals(new Swatch("lime", Swatch.SOLID), colorService.resolveStored("lime", ColorFamily.CALENDAR));
		// colour list: yellow, orange, brown, red
		Assert.assertEquals(yellow, colorService.resolveStored("yellow", ColorFamily.COLOR_SERVICE));
		Assert.assertEquals(orange, colorService.resolveStored("orange", ColorFamily.COLOR_SERVICE));
		Assert.assertEquals(new Swatch("brown", Swatch.SOLID), colorService.resolveStored("brown", ColorFamily.COLOR_SERVICE));
		Assert.assertEquals(red, colorService.resolveStored("red", ColorFamily.COLOR_SERVICE));
		// resolve() keeps the legacy meaning
		Assert.assertEquals(new Swatch("orange", Swatch.SOLID), colorService.resolve("orange", ColorFamily.COLOR_SERVICE));
		// a legacy class is no swatch key: its alias
		Assert.assertEquals(new Swatch("blue", Swatch.DARK), colorService.resolveStored("o_cal_navy", ColorFamily.CALENDAR));
		Assert.assertEquals(new Swatch("orange", Swatch.SOLID), colorService.resolveStored("o_color_orange", null));
		Assert.assertEquals(new Swatch("blue", Swatch.DARK), colorService.resolveStored("o_color_darkblue", ColorFamily.SWATCH));
		// the swatch class
		Assert.assertEquals(new Swatch("blue", Swatch.LIGHT), colorService.resolveStored("o_swatch_blue-light", ColorFamily.CALENDAR));
	}

	@Test
	public void resolveStoredAliases() {
		for (String[] alias : ALIASES) {
			ColorFamily family = ColorFamily.valueOf(alias[0]);
			String key = alias[1];
			Swatch expected = Swatch.fromKey(key) != null ? Swatch.fromKey(key) : new Swatch(alias[2], alias[3]);
			Assert.assertEquals(alias[0] + " " + key, expected, colorService.resolveStored(key, family));
			Assert.assertEquals(alias[0] + " " + key, expected, colorService.resolveStored(" " + key + " ", family));
		}
		// keys that are no swatch key are aliases
		Assert.assertEquals(new Swatch("blue", Swatch.DARK), colorService.resolveStored("navy", ColorFamily.CALENDAR));
		Assert.assertEquals(new Swatch("gray", Swatch.SOLID), colorService.resolveStored("grey", ColorFamily.CALENDAR));
		Assert.assertEquals(new Swatch("blue", Swatch.DARK), colorService.resolveStored("cobaltblue", ColorFamily.COLOR_SERVICE));
		Assert.assertEquals(new Swatch("yellow", Swatch.SOLID), colorService.resolveStored("gold", ColorFamily.COLOR_SERVICE));
		// new keys in a legacy family
		Assert.assertEquals(new Swatch("blue", Swatch.LIGHT), colorService.resolveStored("blue-light", ColorFamily.CALENDAR));
		Assert.assertEquals(new Swatch("teal", Swatch.SOLID), colorService.resolveStored("teal", ColorFamily.COLOR_SERVICE));
		Assert.assertEquals(new Swatch("slate", Swatch.SOFT), colorService.resolveStored("slate-soft", ColorFamily.CALENDAR));
		// without family: only swatch keys and prefixed classes
		Assert.assertEquals(new Swatch("blue", Swatch.SOLID), colorService.resolveStored("blue", null));
		Assert.assertEquals(new Swatch("blue", Swatch.DARK), colorService.resolveStored("o_cal_navy", null));
		Assert.assertNull(colorService.resolveStored("navy", null));
		Assert.assertNull(colorService.resolveStored("navy", ColorFamily.SWATCH));
		// a key of another legacy family
		Assert.assertNull(colorService.resolveStored("navy", ColorFamily.COLOR_SERVICE));
		Assert.assertNull(colorService.resolveStored("cobaltblue", ColorFamily.CALENDAR));
	}

	@Test
	public void resolveStoredUnknownAndHostile() {
		for (ColorFamily family : familiesAndNull()) {
			for (String value : new String[] { null, "", " ", "nocolor", "inherited", "o_colcat_nocolor", "o_colcat_inherited",
					"o_colcat_course", "light.blue", "light_blue", "dark.green",
					"blue-solid", "blue-pale", "Blue", "BLUE", "neutral", "theme", "o_swatch_lightblue", "o_cal_teal",
					"o_color_background_10", "b_green", "blue light" }) {
				Assert.assertNull(family + " " + value, colorService.resolveStored(value, family));
			}
			for (String junk : ColorValidatorTest.JUNK) {
				Assert.assertNull(junk, colorService.resolveStored(junk, family));
				Assert.assertNull(junk, colorService.resolveStored("o_cal_" + junk, family));
				Assert.assertNull(junk, colorService.resolveStored("o_swatch_" + junk, family));
			}
		}
		// no theme: a theme colour has no swatch
		Assert.assertNull(colorService.resolveStored("theme1", ColorFamily.CALENDAR));
	}

	private static ColorFamily[] familiesAndNull() {
		ColorFamily[] families = ColorFamily.values();
		ColorFamily[] all = new ColorFamily[families.length + 1];
		System.arraycopy(families, 0, all, 0, families.length);
		return all;
	}

	@Test
	public void calendarEventCssClass() {
		// keys of the chooser sets: the swatch
		Assert.assertEquals("o_cal_swatch o_swatch_blue", colorService.toCalendarEventCssClass("blue"));
		Assert.assertEquals("o_cal_swatch o_swatch_yellow", colorService.toCalendarEventCssClass("yellow"));
		Assert.assertEquals("o_cal_swatch o_swatch_orange", colorService.toCalendarEventCssClass(" orange "));
		Assert.assertEquals("o_cal_swatch o_swatch_blue o_swatch_light", colorService.toCalendarEventCssClass("blue-light"));
		Assert.assertEquals("o_cal_swatch o_swatch_teal o_swatch_soft", colorService.toCalendarEventCssClass("teal-soft"));
		// calendar colours that are no swatch key: the calendar class
		for (String key : List.of("grey", "maroon", "navy", "fuchsia", "rebeccapurple", "olive")) {
			Assert.assertEquals("o_cal_" + key, colorService.toCalendarEventCssClass(key));
		}
		// every calendar colour has a class
		for (String key : ColorService.CALENDAR_COLORS) {
			Assert.assertTrue(key, ColorValidator.isValidCssClasses(colorService.toCalendarEventCssClass(key)));
		}
		// no class: other keys, theme colours without theme, junk
		for (String value : new String[] { null, "", " ", "cobaltblue", "lightblue", "o_cal_blue", "o_swatch_blue",
				"Blue", "blue-solid", "theme1", "swatch", "o_cal_swatch" }) {
			Assert.assertNull(value, colorService.toCalendarEventCssClass(value));
		}
		for (String junk : ColorValidatorTest.JUNK) {
			Assert.assertNull(junk, colorService.toCalendarEventCssClass(junk));
		}
	}

	/**
	 * Every alias of Java has the colours of its legacy class in the compiled
	 * CSS of both themes: the swatch tokens of the class are the tokens of the
	 * swatch class of the hue and the variant, and the fill and the text are
	 * the colours of the {@link Palette}. Every legacy class of the CSS is an
	 * alias of Java.
	 */
	@Test
	public void aliasesAsCompiled() throws IOException {
		assertAliasesAsCompiled(new String(Files.readAllBytes(PaletteTest.LIGHT_THEME_CSS), StandardCharsets.UTF_8));
		assertAliasesAsCompiled(new String(Files.readAllBytes(OPENOLAT_THEME_CSS), StandardCharsets.UTF_8));
	}

	private void assertAliasesAsCompiled(String css) {
		Palette themePalette = Palette.fromCss(css);
		Assert.assertNotNull(themePalette);
		for (String[] prefix : PREFIXES) {
			Map<String,Swatch> aliases = switch (prefix[1]) {
				case "COLOR_SERVICE" -> ColorServiceImpl.COLOR_SERVICE_ALIASES;
				case "COLOR_LIST" -> ColorServiceImpl.COLOR_LIST_ALIASES;
				case "CALENDAR" -> ColorServiceImpl.CALENDAR_ALIASES;
				default -> throw new AssertionError(prefix[1]);
			};
			for (Map.Entry<String,Swatch> alias : aliases.entrySet()) {
				String cssClass = prefix[0] + alias.getKey();
				Swatch swatch = alias.getValue();
				Assert.assertEquals(cssClass, swatch, colorService.resolve(cssClass, null));
				
				Map<String,String> tokens = swatchTokens(css, "." + cssClass);
				String swatchSelector = "." + ColorService.SWATCH_CSS_CLASS_PREFIX + swatch.hue();
				Map<String,String> expected = swatchTokens(css, swatchSelector);
				if (!Swatch.SOLID.equals(swatch.variant())) {
					expected.putAll(swatchTokens(css, swatchSelector + "." + ColorService.SWATCH_CSS_CLASS_PREFIX + swatch.variant()));
				}
				Assert.assertFalse(cssClass, tokens.isEmpty());
				Assert.assertEquals(cssClass, expected, tokens);
				Assert.assertEquals(cssClass + " bg", themePalette.bg(swatch), tokens.get("bg"));
				Assert.assertEquals(cssClass + " fg", themePalette.fg(swatch), tokens.get("fg"));
			}
			// no legacy class in the CSS without an alias in Java
			Set<String> compiled = new TreeSet<>();
			Pattern legacyClass = Pattern.compile("\\." + prefix[0] + "([a-z0-9_]+)");
			Matcher rule = RULE.matcher(css);
			while (rule.find()) {
				// a rule that sets the swatch tokens, not one that reads them
				if (rule.group(2).contains("--o-sw-bg:")) {
					for (String selector : rule.group(1).split(",")) {
						Matcher matcher = legacyClass.matcher(selector.trim());
						if (matcher.matches()) {
							compiled.add(matcher.group(1));
						}
					}
				}
			}
			Assert.assertEquals(prefix[0], new TreeSet<>(aliases.keySet()), compiled);
		}
		// the classes of the removed families have no rule
		for (String cssClass : REMOVED_CLASSES) {
			Assert.assertFalse(cssClass, Pattern.compile("\\." + cssClass + "(?![a-z0-9_-])").matcher(css).find());
		}
	}

	/**
	 * @return The swatch tokens of all rules with the selector, the later rule wins
	 */
	private static Map<String,String> swatchTokens(String css, String selector) {
		Map<String,String> tokens = new LinkedHashMap<>();
		Matcher rule = RULE.matcher(css);
		while (rule.find()) {
			boolean match = false;
			for (String part : rule.group(1).split(",")) {
				if (part.trim().equals(selector)) {
					match = true;
					break;
				}
			}
			if (match) {
				Matcher token = SWATCH_TOKEN.matcher(rule.group(2));
				while (token.find()) {
					tokens.put(token.group(1), token.group(2).toUpperCase());
				}
			}
		}
		return tokens;
	}

	@Test
	public void unknownValues() {
		Assert.assertNull(colorService.resolve(null, ColorFamily.COLOR_SERVICE));
		Assert.assertNull(colorService.resolve("", ColorFamily.COLOR_SERVICE));
		Assert.assertNull(colorService.resolve("blue", ColorFamily.COLOR_SERVICE));
		Assert.assertNull(colorService.resolve("lightblue", ColorFamily.CALENDAR));
		Assert.assertNull(colorService.resolve("o_color_blue", ColorFamily.CALENDAR));
		Assert.assertNull(colorService.resolve("o_colcat_inherited", null));
		Assert.assertNull(colorService.resolve("o_colcat_nocolor", ColorFamily.SWATCH));
		Assert.assertNull(colorService.resolve("o_color_background_10", null));
		Assert.assertNull(colorService.resolve("b_green", ColorFamily.COLOR_SERVICE));
	}

	@Test
	public void legacyColorsResolve() {
		for (String color : ColorService.LEGACY_COLORS) {
			Assert.assertNotNull("color.list: " + color, colorService.resolve(color, ColorFamily.COLOR_SERVICE));
		}
		for (String color : ColorService.BADGE_COLORS) {
			Assert.assertNotNull("badge: " + color, colorService.resolve(color, ColorFamily.COLOR_SERVICE));
		}
		// every old key has an alias, the aliases have no other key
		Set<String> keys = new HashSet<>(ColorService.LEGACY_COLORS);
		keys.addAll(ColorService.BADGE_COLORS);
		Assert.assertEquals(ColorServiceImpl.COLOR_SERVICE_ALIASES.keySet(), keys);
	}

	@Test
	public void colorPropertiesRemoved() throws IOException {
		// the colour sets are in code: olat.properties has no colour configuration
		Properties properties = new Properties();
		try(InputStream in = ColorServiceResolveTest.class.getResourceAsStream("/serviceconfig/olat.properties")) {
			Assert.assertNotNull(in);
			properties.load(in);
		}
		Assert.assertNull(properties.getProperty("color.list"));
		Assert.assertNull(properties.getProperty("color.default"));
		Assert.assertNull(properties.getProperty("ceditor.table.styles"));
		Assert.assertNull(properties.getProperty("video.marker.styles"));
	}

	@Test
	public void ignoredPropertiesAreLogged() {
		// an olat.local.properties that still sets the properties: one line, no failure
		Assert.assertTrue(ColorServiceImpl.logIgnoredProperties("lightgray,yellow", null));
		Assert.assertTrue(ColorServiceImpl.logIgnoredProperties(null, "lightblue"));
		Assert.assertTrue(ColorServiceImpl.logIgnoredProperties("\"><script>alert(1)</script>", "x".repeat(100_000)));
		Assert.assertFalse(ColorServiceImpl.logIgnoredProperties(null, null));
		Assert.assertFalse(ColorServiceImpl.logIgnoredProperties("", " "));
	}

	@Test
	public void storedValueWithLegacyPrefix() {
		// a key of the chooser sets renders as its swatch, also if it is an old key
		Assert.assertEquals("o_swatch_blue", colorService.toStoredValueCssClass("blue", "o_color_"));
		Assert.assertEquals("o_swatch_red", colorService.toStoredValueCssClass(" red ", "o_alert_box_color_"));
		Assert.assertEquals("o_swatch_gray", colorService.toStoredValueCssClass("gray", "o_video_marker_"));
		Assert.assertEquals("o_swatch_blue o_swatch_light", colorService.toStoredValueCssClass("blue-light", "o_color_"));
		// an old bare key gets the prefix, an old class stays
		Assert.assertEquals("o_color_lightblue", colorService.toStoredValueCssClass("lightblue", "o_color_"));
		Assert.assertEquals("o_alert_box_color_lightgray", colorService.toStoredValueCssClass("lightgray", "o_alert_box_color_"));
		Assert.assertEquals("o_video_marker_lightblue", colorService.toStoredValueCssClass("lightblue", "o_video_marker_"));
		Assert.assertEquals("o_video_marker_lightblue", colorService.toStoredValueCssClass("o_video_marker_lightblue", "o_video_marker_"));
		Assert.assertEquals("o_video_marker_seagreen", colorService.toStoredValueCssClass("o_video_marker_seagreen", "o_video_marker_"));
		// the alias renders the old value, it resolves to the same swatch
		Assert.assertEquals(colorService.resolveStored("lightblue", ColorFamily.COLOR_SERVICE),
				colorService.resolve(colorService.toStoredValueCssClass("lightblue", "o_video_marker_"), null));
		// junk: no class
		Assert.assertNull(colorService.toStoredValueCssClass(null, "o_color_"));
		Assert.assertNull(colorService.toStoredValueCssClass(" ", "o_color_"));
		Assert.assertNull(colorService.toStoredValueCssClass("lightblue onmouseover=alert(1)", "o_color_"));
		Assert.assertNull(colorService.toStoredValueCssClass("\"><script>alert(1)</script>", "o_video_marker_"));
		Assert.assertNull(colorService.toStoredValueCssClass("blue'", "o_alert_box_color_"));
		Assert.assertNull(colorService.toStoredValueCssClass("#FF0000", "o_video_marker_"));
		Assert.assertNull(colorService.toStoredValueCssClass("x".repeat(100_000), "o_color_"));
		// no theme colours without a palette: no class for a theme colour
		Assert.assertNull(colorService.toStoredValueCssClass("theme1", "o_color_"));
	}

	@Test
	public void badgeColors() {
		List<String> badgeColors = colorService.getColorsForBadges();
		Assert.assertEquals(ColorService.BADGE_COLORS, badgeColors.subList(0, 3));
		Assert.assertEquals(colorService.getColors(ColorSet.M), badgeColors.subList(3, badgeColors.size()));
		// a new list on every call
		badgeColors.clear();
		Assert.assertEquals(3 + ColorService.HUES.size(), colorService.getColorsForBadges().size());
		// every badge colour has a swatch: the badge colours through their alias, the keys of M as themselves
		for (String color : colorService.getColorsForBadges()) {
			Assert.assertNotNull(color, colorService.resolveStored(color, ColorFamily.COLOR_SERVICE));
		}
		Assert.assertEquals(new Swatch("yellow", Swatch.SOLID), colorService.resolveStored("gold", ColorFamily.COLOR_SERVICE));
		Assert.assertEquals(new Swatch("blue", Swatch.SOLID), colorService.resolveStored("blue", ColorFamily.COLOR_SERVICE));
		// an old badge colour
		Assert.assertEquals(new Swatch("cyan", Swatch.SOLID), colorService.resolveStored("lightblue", ColorFamily.COLOR_SERVICE));
	}

	@Test
	public void cssClassRoundTrip() {
		for (String key : ColorServiceImpl.COLOR_SERVICE_ALIASES.keySet()) {
			String cssClass = colorService.toCssClass(key);
			Assert.assertEquals("o_color_" + key, cssClass);
			Assert.assertEquals(key, colorService.toColorKey(cssClass));
		}
		Assert.assertNull(colorService.toCssClass(null));
		Assert.assertNull(colorService.toColorKey(null));
		Assert.assertEquals("lightblue", colorService.toColorKey("lightblue"));
	}

	@Test
	public void calendarColors() {
		Assert.assertEquals(List.of("grey", "yellow", "orange", "red", "maroon", "blue", "navy", "fuchsia",
				"rebeccapurple", "lime", "green", "olive"), colorService.getCalendarColors());
		// the swatch aliases: the keys that are no swatch key, in the same order
		List<String> aliasKeys = colorService.getCalendarColors().stream()
				.filter(key -> Swatch.fromKey(key) == null)
				.toList();
		Assert.assertEquals(List.of("grey", "maroon", "navy", "fuchsia", "rebeccapurple", "olive"), aliasKeys);
		Assert.assertEquals(aliasKeys, List.copyOf(ColorServiceImpl.CALENDAR_ALIASES.keySet()));
		// every calendar colour renders: the swatch key as its swatch, the other keys through the alias
		for (String key : colorService.getCalendarColors()) {
			Swatch swatch = colorService.resolveStored(key, ColorFamily.CALENDAR);
			Assert.assertNotNull(key, swatch);
			Assert.assertNotNull(key, palette.bg(swatch));
		}
	}

	@Test
	public void isCalendarCssClass() {
		for (String key : ColorServiceImpl.CALENDAR_ALIASES.keySet()) {
			Assert.assertTrue(key, colorService.isCalendarCssClass("o_cal_" + key));
		}
		// the calendar colours that are swatch keys have no class
		for (String key : List.of("yellow", "orange", "red", "blue", "lime", "green")) {
			Assert.assertFalse(key, colorService.isCalendarCssClass("o_cal_" + key));
		}
		Assert.assertTrue(colorService.isCalendarCssClass("o_cal_rebeccapurple"));
		// exact match: the class is rendered as it is
		Assert.assertFalse(colorService.isCalendarCssClass("O_CAL_Navy"));
		Assert.assertFalse(colorService.isCalendarCssClass("o_cal_Navy"));
		Assert.assertFalse(colorService.isCalendarCssClass("o_cal_fuchſia"));
		Assert.assertTrue(colorService.isCalendarCssClass("o_cal_fuchsia"));
		Assert.assertFalse(colorService.isCalendarCssClass("blue"));
		Assert.assertFalse(colorService.isCalendarCssClass("o_cal_teal"));
		Assert.assertFalse(colorService.isCalendarCssClass("o_color_blue"));
		Assert.assertFalse(colorService.isCalendarCssClass("o_cal_navy "));
		Assert.assertFalse(colorService.isCalendarCssClass(null));
		Assert.assertFalse(colorService.isCalendarCssClass(""));
	}

	@Test
	public void junkNeverResolves() {
		for (String junk : ColorValidatorTest.JUNK) {
			for (ColorFamily family : ColorFamily.values()) {
				Assert.assertNull(junk, colorService.resolve(junk, family));
				Assert.assertNull(junk, colorService.resolve("o_color_" + junk, family));
			}
			Assert.assertNull(junk, colorService.resolve(junk, null));
			Assert.assertNull(junk, colorService.toSwatchCssClass(junk));
			// no class, or one valid class name (e.g. "o_cal_1blue")
			assertNullOrClass(junk, colorService.toCssClass(junk));
			Assert.assertFalse(junk, colorService.isCalendarCssClass(junk));
			Assert.assertFalse(junk, colorService.isCalendarCssClass("o_cal_" + junk));
		}
		// a theme colour with too many digits is no theme colour
		Assert.assertNull(colorService.resolve("theme" + "9".repeat(5000), ColorFamily.SWATCH));
		Assert.assertNull(colorService.toSwatchCssClass("theme12345"));
	}

	private static void assertNullOrClass(String junk, String cssClass) {
		Assert.assertTrue(junk + " -> " + cssClass, cssClass == null || ColorValidator.isValidCssClass(cssClass));
	}

	@Test
	public void junkGivesNoClass() {
		// the classes keep working for valid keys, values with white space are trimmed
		Assert.assertEquals("o_color_lightblue", colorService.toCssClass(" lightblue "));
		Assert.assertNull(colorService.toCssClass(""));
		Assert.assertNull(colorService.toCssClass(" "));
		Assert.assertNull(colorService.toCssClass("light.blue"));
		Assert.assertNull(colorService.toCssClass("lightblue\" x=\""));
		Assert.assertNull(colorService.getBackgroundColor(null, null));
		Assert.assertEquals("#000000", colorService.getBackgroundColor(new Swatch("blue", "junk"), "#000000"));
	}
}

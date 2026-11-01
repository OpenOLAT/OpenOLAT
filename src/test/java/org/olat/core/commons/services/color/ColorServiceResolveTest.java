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
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

/**
 * Checks {@link ColorService#resolve(String, ColorFamily)} against the alias
 * table of OO-colors (spec section 3.3, mapping_v2.md). The palette is read
 * from the compiled <code>light/theme.css</code>.
 *
 * Initial date: 29 Oct 2026<br>
 * @author gnaegi, https://www.frentix.com
 */
public class ColorServiceResolveTest {

	/** family, key, hue, variant: the alias table of the spec */
	private static final String[][] ALIASES = {
		{ "COLOR_SERVICE", "lightgray", "gray", "dark" },
		{ "COLOR_SERVICE", "yellow", "yellow", "solid" },
		{ "COLOR_SERVICE", "orange", "orange", "dark" },
		{ "COLOR_SERVICE", "brown", "brown", "solid" },
		{ "COLOR_SERVICE", "red", "red", "solid" },
		{ "COLOR_SERVICE", "orchid", "pink", "solid" },
		{ "COLOR_SERVICE", "purple", "violet", "dark" },
		{ "COLOR_SERVICE", "lightblue", "blue", "solid" },
		{ "COLOR_SERVICE", "cobaltblue", "indigo", "solid" },
		{ "COLOR_SERVICE", "darkblue", "indigo", "dark" },
		{ "COLOR_SERVICE", "lightgreen", "lime", "dark" },
		{ "COLOR_SERVICE", "seagreen", "green", "dark" },
		{ "COLOR_SERVICE", "gold", "yellow", "solid" },
		{ "COLOR_SERVICE", "silver", "gray", "tint" },
		{ "COLOR_SERVICE", "bronze", "orange", "solid" },
		{ "CATEGORY", "dark_blue", "indigo", "dark" },
		{ "CATEGORY", "light_blue", "blue", "solid" },
		{ "CATEGORY", "purple", "magenta", "dark" },
		{ "CATEGORY", "red", "red", "solid" },
		{ "CATEGORY", "orange", "orange", "solid" },
		{ "CATEGORY", "yellow", "yellow", "solid" },
		{ "CATEGORY", "light_green", "lime", "solid" },
		{ "CATEGORY", "dark_green", "green", "dark" },
		{ "CALENDAR", "orange", "orange", "tint" },
		{ "CALENDAR", "green", "green", "solid" },
		{ "CALENDAR", "blue", "blue", "dark" },
		{ "CALENDAR", "yellow", "yellow", "tint" },
		{ "CALENDAR", "red", "red", "tint" },
		{ "CALENDAR", "rebeccapurple", "violet", "dark" },
		{ "CALENDAR", "fuchsia", "magenta", "solid" },
		{ "CALENDAR", "olive", "lime", "dark" },
		{ "CALENDAR", "navy", "indigo", "dark" },
		{ "CALENDAR", "maroon", "red", "dark" },
		{ "CALENDAR", "lime", "lime", "solid" },
		{ "CALENDAR", "grey", "gray", "dark" }
	};

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
		Assert.assertEquals(8, ColorServiceImpl.CATEGORY_ALIASES.size());
		Assert.assertEquals(12, ColorServiceImpl.CALENDAR_ALIASES.size());
	}

	@Test
	public void prefixedClasses() {
		for (String[] alias : ALIASES) {
			Swatch expected = new Swatch(alias[2], alias[3]);
			String key = alias[1];
			switch (ColorFamily.valueOf(alias[0])) {
				case COLOR_SERVICE -> {
					assertResolve(expected, "o_color_" + key);
					assertResolve(expected, "o_video_marker_" + key);
					assertResolve(expected, "o_alert_box_color_" + key);
				}
				case CALENDAR -> assertResolve(expected, "o_cal_" + key);
				case CATEGORY -> {
					assertResolve(expected, "o_colcat_" + key);
					assertResolve(expected, "o_proj_avatar_" + key);
					assertResolve(expected, "o_user_initials_" + key);
					Assert.assertEquals(expected, colorService.resolve(key.replace('_', '.'), ColorFamily.CATEGORY));
					Assert.assertEquals(expected, colorService.resolve(key, ColorFamily.AVATAR));
				}
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
		Assert.assertEquals(new Swatch("orange", Swatch.DARK), colorService.resolve("orange", ColorFamily.COLOR_SERVICE));
		Assert.assertEquals(new Swatch("orange", Swatch.TINT), colorService.resolve("orange", ColorFamily.CALENDAR));
		Assert.assertEquals(new Swatch("orange", Swatch.SOLID), colorService.resolve("orange", ColorFamily.CATEGORY));
		Assert.assertNull(colorService.resolve("orange", null));
	}

	@Test
	public void unknownValues() {
		Assert.assertNull(colorService.resolve(null, ColorFamily.COLOR_SERVICE));
		Assert.assertNull(colorService.resolve("", ColorFamily.COLOR_SERVICE));
		Assert.assertNull(colorService.resolve("blue", ColorFamily.COLOR_SERVICE));
		Assert.assertNull(colorService.resolve("lightblue", ColorFamily.CALENDAR));
		Assert.assertNull(colorService.resolve("o_color_blue", ColorFamily.CALENDAR));
		Assert.assertNull(colorService.resolve("o_colcat_inherited", null));
		Assert.assertNull(colorService.resolve("o_colcat_nocolor", ColorFamily.CATEGORY));
		Assert.assertNull(colorService.resolve("o_color_background_10", null));
		Assert.assertNull(colorService.resolve("b_green", ColorFamily.COLOR_SERVICE));
	}

	@Test
	public void defaultColorListResolves() throws IOException {
		Properties properties = new Properties();
		try(InputStream in = ColorServiceResolveTest.class.getResourceAsStream("/serviceconfig/olat.properties")) {
			Assert.assertNotNull(in);
			properties.load(in);
		}
		String colorList = properties.getProperty("color.list");
		Assert.assertNotNull(colorList);
		for (String color : colorList.split(",")) {
			Assert.assertNotNull("color.list: " + color, colorService.resolve(color.trim(), ColorFamily.COLOR_SERVICE));
		}
		Assert.assertNotNull(colorService.resolve(properties.getProperty("color.default"), ColorFamily.COLOR_SERVICE));
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
}

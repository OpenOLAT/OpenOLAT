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
package org.olat.commons.calendar.manager;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.services.color.ColorService;
import org.olat.core.commons.services.color.ColorServiceImpl;
import org.olat.core.commons.services.color.ColorSet;
import org.olat.core.commons.services.color.CssColorNames;
import org.olat.core.commons.services.color.Palette;
import org.olat.core.commons.services.color.PaletteTest;
import org.olat.core.commons.services.color.Swatch;

/**
 * Tests the mapping of the colour key of a calendar event to the
 * iCalendar property COLOR. The palette is read from the compiled
 * <code>light/theme.css</code>.
 *
 * Initial date: 24 Nov 2026<br>
 * @author gnaegi, https://www.frentix.com
 */
public class ICalEventColorsTest {

	private final Palette palette = PaletteTest.lightPalette();
	private final ColorService colorService = new ColorServiceImpl() {
		@Override
		public Palette getPalette() {
			return palette;
		}
	};

	@Test
	public void calendarColorsUnchanged() {
		for (String key : ColorService.CALENDAR_COLORS) {
			Assert.assertEquals(key, ICalEventColors.toCssColorName(key, colorService));
			Assert.assertEquals(key, ICalEventColors.toCssColorName(key, null));
		}
	}

	/**
	 * Every key of the colour set M has a CSS colour name and survives the
	 * round trip of COLOR and X-OLAT-COLOR.
	 */
	@Test
	public void colorSetMKeys() {
		for (String key : colorService.getColors(ColorSet.M)) {
			String cssColorName = ICalEventColors.toCssColorName(key, colorService);
			Assert.assertNotNull(key, cssColorName);
			Assert.assertTrue(key, CssColorNames.isColorName(cssColorName));
			// X-OLAT-COLOR is written only for a key which is no CSS colour name
			String xOlatColor = key.equals(cssColorName) ? null : key;
			Assert.assertEquals(key, ICalEventColors.fromICal(xOlatColor, cssColorName));
		}
		Assert.assertEquals("teal", ICalEventColors.toCssColorName("teal", colorService));
		Assert.assertNotEquals("slate", ICalEventColors.toCssColorName("slate", colorService));
	}

	@Test
	public void cssColorNamesUnchanged() {
		Assert.assertEquals("teal", ICalEventColors.toCssColorName("teal", colorService));
		Assert.assertEquals("darkslateblue", ICalEventColors.toCssColorName("darkslateblue", colorService));
	}

	@Test
	public void colorServiceKeys() {
		// a bare key of the colour service is no calendar colour: the event has no colour
		Assert.assertNull(ICalEventColors.toCssColorName("cobaltblue", colorService));
		Assert.assertNull(ICalEventColors.toCssColorName("bronze", colorService));
		Assert.assertNull(colorService.toCalendarEventCssClass("cobaltblue"));
		// keys which are CSS colour names are used unchanged
		Assert.assertEquals("lightblue", ICalEventColors.toCssColorName("lightblue", colorService));
		Assert.assertEquals("seagreen", ICalEventColors.toCssColorName("seagreen", colorService));
		// a class with the prefix of the colour service keeps its family
		assertNearest(new Swatch("violet", Swatch.DARK), "o_color_purple");
		assertNearest(new Swatch("blue", Swatch.DARK), "o_color_cobaltblue");
	}

	@Test
	public void paletteHues() {
		assertNearest(new Swatch("slate", Swatch.SOLID), "slate");
		// the keys of the chooser sets with a variant
		assertNearest(new Swatch("blue", Swatch.LIGHT), "blue-light");
		assertNearest(new Swatch("teal", Swatch.DARK), "teal-dark");
		assertNearest(new Swatch("yellow", Swatch.SOFT), "o_swatch_yellow-soft");
		// a hue which is a CSS colour name is used unchanged
		Assert.assertEquals("violet", ICalEventColors.toCssColorName("violet", colorService));
	}

	@Test
	public void themeColors() {
		String theme1 = palette.bg(new Swatch("theme1", Swatch.SOLID));
		Assert.assertEquals(CssColorNames.nearestColorName(theme1), ICalEventColors.toCssColorName("theme1", colorService));
		Assert.assertEquals(CssColorNames.nearestColorName(theme1), ICalEventColors.toCssColorName("o_swatch_theme1", colorService));
		// a theme colour the theme does not define is theme1
		Assert.assertEquals(CssColorNames.nearestColorName(theme1), ICalEventColors.toCssColorName("theme9", colorService));
	}

	@Test
	public void unknownKeys() {
		Assert.assertNull(ICalEventColors.toCssColorName(null, colorService));
		Assert.assertNull(ICalEventColors.toCssColorName("", colorService));
		Assert.assertNull(ICalEventColors.toCssColorName("  ", colorService));
		Assert.assertNull(ICalEventColors.toCssColorName("nocolor", colorService));
		Assert.assertNull(ICalEventColors.toCssColorName("neutral", colorService));
		// without colour service only the CSS colour names
		Assert.assertNull(ICalEventColors.toCssColorName("cobaltblue", null));
	}

	@Test
	public void withoutPalette() {
		ColorService noPalette = new ColorServiceImpl() {
			@Override
			public Palette getPalette() {
				return null;
			}
		};
		Assert.assertEquals("blue", ICalEventColors.toCssColorName("blue", noPalette));
		Assert.assertNull(ICalEventColors.toCssColorName("cobaltblue", noPalette));
		Assert.assertNull(ICalEventColors.toCssColorName("theme1", noPalette));
	}

	@Test
	public void hostileKeys() {
		for (String junk : HOSTILE) {
			Assert.assertNull(junk, ICalEventColors.validColorKey(junk));
			Assert.assertNull(junk, ICalEventColors.toCssColorName(junk, colorService));
			Assert.assertNull(junk, ICalEventColors.toCssColorName(junk, null));
		}
		Assert.assertEquals("blue", ICalEventColors.validColorKey(" blue "));
		Assert.assertEquals("blue", ICalEventColors.toCssColorName(" blue ", colorService));
	}

	@Test
	public void fromICal() {
		// X-OLAT-COLOR wins, COLOR is the fallback in lower case
		Assert.assertEquals("cobaltblue", ICalEventColors.fromICal("cobaltblue", "blue"));
		Assert.assertEquals("blue", ICalEventColors.fromICal(null, "blue"));
		Assert.assertEquals("darkslateblue", ICalEventColors.fromICal(" ", " DarkSlateBlue "));
		Assert.assertNull(ICalEventColors.fromICal(null, null));
		Assert.assertNull(ICalEventColors.fromICal("", ""));
		for (String junk : HOSTILE) {
			// a hostile X-OLAT-COLOR falls back to COLOR
			Assert.assertEquals(junk, "navy", ICalEventColors.fromICal(junk, "navy"));
			Assert.assertNull(junk, ICalEventColors.fromICal(junk, junk));
			Assert.assertNull(junk, ICalEventColors.fromICal(null, junk));
		}
	}

	private static final String[] HOSTILE = {
			"\"><script>alert(1)</script>",
			"blue onmouseover=alert(1)",
			"blue;background:url(x)",
			"o_cal_blue\" x=\"",
			"blue\r\nX-OLAT-COLOR:red",
			"x".repeat(10_000),
			"bl\u00FCe",
			"\u7EA2" };

	private void assertNearest(Swatch expectedSwatch, String key) {
		String expected = CssColorNames.nearestColorName(palette.bg(expectedSwatch));
		Assert.assertNotNull(key, expected);
		String cssColorName = ICalEventColors.toCssColorName(key, colorService);
		Assert.assertEquals(key, expected, cssColorName);
		Assert.assertTrue(key, CssColorNames.isColorName(cssColorName));
	}
}

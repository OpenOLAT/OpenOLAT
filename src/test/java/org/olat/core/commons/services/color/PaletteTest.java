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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * Checks the palette read from the custom properties in the compiled
 * <code>light/theme.css</code> and the role rules of the {@link Palette}.
 * The contrast check is the drift guard of the palette: every swatch must
 * carry its text colour with at least 4.5:1.
 *
 * Initial date: 29 Oct 2026<br>
 * @author gnaegi, https://www.frentix.com
 */
public class PaletteTest {

	/** The compiled light theme, relative to the project directory */
	public static final Path LIGHT_THEME_CSS = Path.of("src/main/webapp/static/themes/light/theme.css");

	private static final List<String> VARIANTS = List.of(Swatch.SOLID, Swatch.TINT, Swatch.LIGHT, Swatch.DARK);

	private final Palette palette = lightPalette();

	/**
	 * @return The palette of the compiled light theme of the project
	 */
	public static Palette lightPalette() {
		try {
			Palette palette = Palette.fromCss(new String(Files.readAllBytes(LIGHT_THEME_CSS), StandardCharsets.UTF_8));
			Assert.assertNotNull("No palette in " + LIGHT_THEME_CSS.toAbsolutePath(), palette);
			return palette;
		} catch (IOException e) {
			throw new AssertionError("Cannot read " + LIGHT_THEME_CSS.toAbsolutePath(), e);
		}
	}

	@Test
	public void loadPalette() {
		Assert.assertEquals(14, palette.getHues().size());

		int count = 0;
		for (String hue : palette.getHues()) {
			Assert.assertEquals("Steps of " + hue, List.of(50, 100, 200, 300, 400, 500, 600, 700, 800, 900),
					List.copyOf(palette.getSteps(hue)));
			for (Integer step : palette.getSteps(hue)) {
				String hex = palette.hex(hue, step.intValue());
				Assert.assertTrue(hue + " " + step + ": " + hex, hex.matches("^#[0-9A-F]{6}$"));
				count++;
			}
		}
		Assert.assertEquals(140, count);
	}

	@Test
	public void brightHuesExist() {
		Assert.assertTrue(palette.getHues().containsAll(Palette.BRIGHT_HUES));
	}

	@Test(expected = IllegalArgumentException.class)
	public void unknownHue() {
		palette.hex("lightblue", 500);
	}

	@Test(expected = IllegalArgumentException.class)
	public void unknownStep() {
		palette.hex("blue", 550);
	}

	@Test(expected = IllegalArgumentException.class)
	public void unknownVariant() {
		palette.bg(new Swatch("blue", "soft"));
	}

	@Test
	public void bgSteps() {
		Assert.assertEquals(400, Palette.bgStep(new Swatch("yellow", Swatch.SOLID)));
		Assert.assertEquals(500, Palette.bgStep(new Swatch("blue", Swatch.SOLID)));
		Assert.assertEquals(300, Palette.bgStep(new Swatch("yellow", Swatch.TINT)));
		Assert.assertEquals(300, Palette.bgStep(new Swatch("blue", Swatch.TINT)));
		Assert.assertEquals(200, Palette.bgStep(new Swatch("yellow", Swatch.LIGHT)));
		Assert.assertEquals(200, Palette.bgStep(new Swatch("blue", Swatch.LIGHT)));
		Assert.assertEquals(500, Palette.bgStep(new Swatch("yellow", Swatch.DARK)));
		Assert.assertEquals(700, Palette.bgStep(new Swatch("blue", Swatch.DARK)));
	}

	@Test
	public void foregroundContrast() {
		for (String hue : palette.getHues()) {
			for (String variant : VARIANTS) {
				Swatch swatch = new Swatch(hue, variant);
				String bg = palette.bg(swatch);
				String fg = palette.fg(swatch);
				double contrast = ColorContrast.contrast(fg, bg);
				Assert.assertTrue(swatch + " " + fg + " on " + bg + ": " + contrast, contrast >= Palette.MIN_CONTRAST);
			}
		}
	}

	@Test
	public void textContrastOnWhite() {
		for (String hue : palette.getHues()) {
			String text = palette.text(hue);
			double contrast = ColorContrast.contrast(text, Palette.WHITE);
			Assert.assertTrue(hue + " " + text + ": " + contrast, contrast >= Palette.MIN_CONTRAST);
		}
	}

	@Test
	public void knownValues() {
		// Values of the report oo-colors-p1-scss, computed by the theme build
		Assert.assertEquals("#E4B132", palette.bg(new Swatch("yellow", Swatch.SOLID)));
		Assert.assertEquals("#321E00", palette.fg(new Swatch("yellow", Swatch.SOLID)));
		Assert.assertEquals("#0470D8", palette.bg(new Swatch("blue", Swatch.SOLID)));
		Assert.assertEquals(Palette.WHITE, palette.fg(new Swatch("blue", Swatch.SOLID)));
		Assert.assertEquals("#002249", palette.fg(new Swatch("blue", Swatch.TINT)));
		Assert.assertEquals("#00468B", palette.bg(new Swatch("blue", Swatch.DARK)));
	}

	@Test
	public void cssWithoutPalette() {
		Assert.assertNull(Palette.fromCss(null));
		Assert.assertNull(Palette.fromCss(""));
		Assert.assertNull(Palette.fromCss(":root{--o-primary: #4166E8;--o-red: #D2343C}body{color:#333}"));
	}

	@Test
	public void incompleteHueDropped() {
		StringBuilder css = new StringBuilder(":root{");
		for (Integer step : Palette.STEPS) {
			css.append("--o-red-").append(step).append(": #d2343c;");
			if (step.intValue() != 900) {
				// blue has 9 steps, 900 is missing
				css.append("--o-blue-").append(step).append(":#4166E8;");
			}
		}
		css.append("}");

		Palette incomplete = Palette.fromCss(css.toString());
		Assert.assertNotNull(incomplete);
		Assert.assertEquals(List.of("red"), List.copyOf(incomplete.getHues()));
		Assert.assertEquals("#D2343C", incomplete.hex("red", 500));
		Assert.assertThrows(IllegalArgumentException.class, () -> incomplete.hex("blue", 500));
	}

	/**
	 * The neutral scale --o-neutral-{step} has all steps of a hue, but it is
	 * no hue: the choosers do not offer the greys of the theme.
	 */
	@Test
	public void neutralScaleIsNoHue() {
		Assert.assertFalse(palette.contains(Palette.NEUTRAL));
		Assert.assertFalse(palette.getHues().contains(Palette.NEUTRAL));

		StringBuilder css = new StringBuilder(":root{--o-neutral-0: #FFFFFF;--o-neutral-25: #F9FAFB;");
		for (Integer step : Palette.STEPS) {
			css.append("--o-red-").append(step).append(": #D2343C;");
			css.append("--o-neutral-").append(step).append(": #657286;");
		}
		css.append("--o-neutral-1000: #000000}");
		Palette withNeutral = Palette.fromCss(css.toString());
		Assert.assertNotNull(withNeutral);
		Assert.assertEquals(List.of("red"), List.copyOf(withNeutral.getHues()));
		Assert.assertNull(Palette.fromCss(":root{--o-neutral-50: #F6F7FA;--o-neutral-100: #EAECF0}"));
	}

	@Test
	public void cssWithOnlyIncompleteHues() {
		Assert.assertNull(Palette.fromCss(":root{--o-red-50: #FEF5F5;--o-red-100: #FEE7E6}"));
	}

	@Test
	public void lightThemeColor() {
		Assert.assertEquals(List.of("theme1"), palette.getThemeColors());
		Assert.assertFalse(palette.getHues().contains("theme1"));
		Assert.assertTrue(palette.contains("theme1"));
		// $brand-primary of light, white text with 4.5:1, exact at step 500
		Assert.assertEquals("#2E6BBF", palette.bg(new Swatch("theme1", Swatch.SOLID)));
		for (String variant : VARIANTS) {
			Swatch swatch = new Swatch("theme1", variant);
			Assert.assertTrue("theme1 " + variant, ColorContrast.contrast(palette.fg(swatch), palette.bg(swatch)) >= Palette.MIN_CONTRAST);
		}
		Assert.assertTrue(ColorContrast.contrast(Palette.WHITE, palette.text("theme1")) >= Palette.MIN_CONTRAST);
		Assert.assertTrue(palette.withoutThemeColors().getThemeColors().isEmpty());
	}

	@Test
	public void openolatThemeColors() throws IOException {
		Path openolatCss = Path.of("src/main/webapp/static/themes/openolat/theme.css");
		Palette openolat = Palette.fromCss(new String(Files.readAllBytes(openolatCss), StandardCharsets.UTF_8));
		Assert.assertNotNull(openolat);
		// Sea, Peach, Berry, Sprout of _openolat_config.scss, each exact at its step
		Assert.assertEquals(List.of("theme1", "theme2", "theme3", "theme4"), openolat.getThemeColors());
		Assert.assertEquals("#126365", openolat.bg(new Swatch("theme1", Swatch.SOLID)));
		Assert.assertEquals("#F2AF40", openolat.bg(new Swatch("theme2", Swatch.SOLID)));
		Assert.assertEquals("#8F1F40", openolat.bg(new Swatch("theme3", Swatch.SOLID)));
		Assert.assertEquals("#B2CF42", openolat.bg(new Swatch("theme4", Swatch.SOLID)));
		Assert.assertTrue(openolat.isBrightColor("theme2"));
		// Sprout is exact at 300: tint and light move one step lighter
		Assert.assertEquals(300, openolat.getBgStep(new Swatch("theme4", Swatch.SOLID)));
		Assert.assertEquals(200, openolat.getBgStep(new Swatch("theme4", Swatch.TINT)));
		Assert.assertEquals(100, openolat.getBgStep(new Swatch("theme4", Swatch.LIGHT)));
		Assert.assertFalse(openolat.isBrightColor("theme3"));
		for (String themeColor : openolat.getThemeColors()) {
			for (String variant : VARIANTS) {
				Swatch swatch = new Swatch(themeColor, variant);
				Assert.assertTrue(themeColor + " " + variant,
						ColorContrast.contrast(openolat.fg(swatch), openolat.bg(swatch)) >= Palette.MIN_CONTRAST);
			}
		}
	}

	@Test
	public void themeColorsCountedUntilGap() {
		StringBuilder css = new StringBuilder(":root{");
		for (Integer step : Palette.STEPS) {
			css.append("--o-red-").append(step).append(": #D2343C;");
			css.append("--o-theme1-").append(step).append(": #10").append(step.intValue() == 600 ? "6365" : "0000").append(";");
			css.append("--o-theme3-").append(step).append(": #200000;");
		}
		css.append("--o-theme1: #106365;--o-theme3: #200000}");

		Palette themed = Palette.fromCss(css.toString());
		Assert.assertNotNull(themed);
		// theme2 is missing, theme3 is not a theme colour
		Assert.assertEquals(List.of("theme1"), themed.getThemeColors());
		Assert.assertFalse(themed.contains("theme3"));
		// the fill is step 600: tint 300, light 200, dark two steps darker
		Assert.assertEquals(600, themed.getBgStep(new Swatch("theme1", Swatch.SOLID)));
		Assert.assertEquals(300, themed.getBgStep(new Swatch("theme1", Swatch.TINT)));
		Assert.assertEquals(200, themed.getBgStep(new Swatch("theme1", Swatch.LIGHT)));
		Assert.assertEquals(800, themed.getBgStep(new Swatch("theme1", Swatch.DARK)));
		Assert.assertFalse(themed.isBrightColor("theme1"));
		Assert.assertEquals("#106365", themed.text("theme1"));
	}

	@Test
	public void themeWithoutThemeColors() {
		StringBuilder css = new StringBuilder(":root{");
		for (Integer step : Palette.STEPS) {
			css.append("--o-red-").append(step).append(": #D2343C;");
		}
		css.append("}");
		Palette plain = Palette.fromCss(css.toString());
		Assert.assertNotNull(plain);
		Assert.assertTrue(plain.getThemeColors().isEmpty());
		Assert.assertFalse(Palette.isThemeColor("red"));
		Assert.assertTrue(Palette.isThemeColor("theme12"));
	}

	@Test
	public void contrast() {
		Assert.assertEquals(21.0d, ColorContrast.contrast("#000000", "#FFFFFF"), 0.001d);
		Assert.assertEquals(1.0d, ColorContrast.contrast("#4166E8", "#4166E8"), 0.001d);
		Assert.assertEquals(4.89d, ColorContrast.contrast("#4166E8", "#FFFFFF"), 0.01d);
	}
}

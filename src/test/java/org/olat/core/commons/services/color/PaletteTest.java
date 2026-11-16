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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
			Assert.assertEquals("Steps of " + hue, List.of(50, 100, 200, 300, 400, 450, 500, 600, 700, 800, 900),
					List.copyOf(palette.getSteps(hue)));
			for (Integer step : palette.getSteps(hue)) {
				String hex = palette.hex(hue, step.intValue());
				Assert.assertTrue(hue + " " + step + ": " + hex, hex.matches("^#[0-9A-F]{6}$"));
				count++;
			}
		}
		Assert.assertEquals(154, count);
	}

	/**
	 * Every hue and every theme colour has the fill step 450: the midpoint of
	 * step 400 and step 500 in OKLab, 1 of 255 per channel at most from the
	 * value that the generator computes (rounding of the compile).
	 */
	@Test
	public void fillStepIsOklabMidpoint() throws IOException {
		Path openolatCss = Path.of("src/main/webapp/static/themes/openolat/theme.css");
		Palette openolat = Palette.fromCss(new String(Files.readAllBytes(openolatCss), StandardCharsets.UTF_8));
		for (Palette p : List.of(palette, openolat)) {
			List<String> hues = new ArrayList<>(p.getHues());
			hues.addAll(p.getThemeColors());
			Assert.assertEquals(14 + p.getThemeColors().size(), hues.size());
			for (String hue : hues) {
				Assert.assertTrue(hue, p.getSteps(hue).contains(Integer.valueOf(450)));
				int[] expected = oklabMidpoint(p.hex(hue, 400), p.hex(hue, 500));
				int[] actual = rgb(p.hex(hue, 450));
				for (int i = 0; i < 3; i++) {
					Assert.assertTrue(hue + "-450 " + p.hex(hue, 450) + " channel " + i + " expected " + expected[i],
							Math.abs(expected[i] - actual[i]) <= 1);
				}
				// lighter than 500, darker than 400
				double luminance = ColorContrast.relativeLuminance(p.hex(hue, 450));
				Assert.assertTrue(hue, luminance < ColorContrast.relativeLuminance(p.hex(hue, 400)));
				Assert.assertTrue(hue, luminance > ColorContrast.relativeLuminance(p.hex(hue, 500)));
			}
		}
		// the values of _palette.scss
		Assert.assertEquals("#F14442", palette.hex("red", 450));
		Assert.assertEquals("#CA9600", palette.hex("yellow", 450));
		Assert.assertEquals("#5196EC", palette.hex("blue", 450));
		Assert.assertEquals("#949494", palette.hex("gray", 450));
	}

	/**
	 * The fill step 450 carries no text at 4.5:1: no swatch role, no variant
	 * and no colour chooser reads it. The compiled theme has no class for it
	 * and no swatch property --o-sw-* reads a step 450.
	 */
	@Test
	public void fillStepIsNoChooserColor() {
		Assert.assertEquals(List.of(450), Palette.FILL_STEPS);
		Assert.assertFalse(Palette.STEPS.contains(Integer.valueOf(450)));
		Assert.assertTrue(Palette.isFillStep(450));
		Assert.assertFalse(Palette.isFillStep(400));
		List<String> hues = new ArrayList<>(palette.getHues());
		hues.addAll(palette.getThemeColors());
		for (String hue : hues) {
			String fill = palette.hex(hue, 450);
			Assert.assertNotEquals(hue, fill, palette.text(hue));
			for (String variant : VARIANTS) {
				Swatch swatch = new Swatch(hue, variant);
				Assert.assertNotEquals(swatch.toString(), 450, palette.getBgStep(swatch));
				Assert.assertNotEquals(swatch.toString(), fill, palette.bg(swatch));
				Assert.assertNotEquals(swatch.toString(), fill, palette.fg(swatch));
			}
		}
		for (Path css : List.of(LIGHT_THEME_CSS, Path.of("src/main/webapp/static/themes/openolat/theme.css"))) {
			String content;
			try {
				content = new String(Files.readAllBytes(css), StandardCharsets.UTF_8);
			} catch (IOException e) {
				throw new AssertionError("Cannot read " + css.toAbsolutePath(), e);
			}
			Matcher swatchProperty = Pattern.compile("--o-sw-[a-z-]+\\s*:\\s*var\\(--o-[a-z]+\\d*-450\\)").matcher(content);
			Assert.assertFalse(css + ": a swatch property reads step 450", swatchProperty.find());
			Matcher selector = Pattern.compile("\\.o_[a-z_]*450[^{]*\\{").matcher(content);
			Assert.assertFalse(css + ": a class for step 450", selector.find());
		}
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
		// yellow is tuned by hand (palette_v4_tuning.json)
		Assert.assertEquals("#F9C900", palette.bg(new Swatch("yellow", Swatch.SOLID)));
		Assert.assertEquals("#301E06", palette.fg(new Swatch("yellow", Swatch.SOLID)));
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

	/**
	 * A CSS without the fill steps still loads (a palette of an older
	 * compile): the fill step is missing, the text steps are complete.
	 */
	@Test
	public void cssWithoutFillSteps() {
		StringBuilder css = new StringBuilder(":root{");
		for (Integer step : Palette.STEPS) {
			css.append("--o-red-").append(step).append(": #D2343C;");
		}
		css.append("}");
		Palette plain = Palette.fromCss(css.toString());
		Assert.assertNotNull(plain);
		Assert.assertEquals(Palette.STEPS, List.copyOf(plain.getSteps("red")));
		Assert.assertThrows(IllegalArgumentException.class, () -> plain.hex("red", 450));
	}

	@Test
	public void contrast() {
		Assert.assertEquals(21.0d, ColorContrast.contrast("#000000", "#FFFFFF"), 0.001d);
		Assert.assertEquals(1.0d, ColorContrast.contrast("#4166E8", "#4166E8"), 0.001d);
		Assert.assertEquals(4.89d, ColorContrast.contrast("#4166E8", "#FFFFFF"), 0.01d);
	}

	private static int[] rgb(String hex) {
		return new int[] { Integer.parseInt(hex.substring(1, 3), 16), Integer.parseInt(hex.substring(3, 5), 16),
				Integer.parseInt(hex.substring(5, 7), 16) };
	}

	private static double linear(int channel) {
		double c = channel / 255.0d;
		return c <= 0.04045d ? c / 12.92d : Math.pow((c + 0.055d) / 1.055d, 2.4d);
	}

	private static double[] oklab(String hex) {
		int[] c = rgb(hex);
		double r = linear(c[0]);
		double g = linear(c[1]);
		double b = linear(c[2]);
		double l = Math.cbrt(0.4122214708d * r + 0.5363325363d * g + 0.0514459929d * b);
		double m = Math.cbrt(0.2119034982d * r + 0.6806995451d * g + 0.1073969566d * b);
		double s = Math.cbrt(0.0883024619d * r + 0.2817188376d * g + 0.6299787005d * b);
		return new double[] { 0.2104542553d * l + 0.7936177850d * m - 0.0040720468d * s,
				1.9779984951d * l - 2.4285922050d * m + 0.4505937099d * s,
				0.0259040371d * l + 0.7827717662d * m - 0.8086757660d * s };
	}

	/**
	 * @return The midpoint of two colours in OKLab as 8 bit sRGB channels (clipped)
	 */
	private static int[] oklabMidpoint(String hex1, String hex2) {
		double[] a = oklab(hex1);
		double[] b = oklab(hex2);
		double lightness = (a[0] + b[0]) / 2.0d;
		double green = (a[1] + b[1]) / 2.0d;
		double blue = (a[2] + b[2]) / 2.0d;
		double l = Math.pow(lightness + 0.3963377774d * green + 0.2158037573d * blue, 3);
		double m = Math.pow(lightness - 0.1055613458d * green - 0.0638541728d * blue, 3);
		double s = Math.pow(lightness - 0.0894841775d * green - 1.2914855480d * blue, 3);
		double[] linearRgb = { 4.0767416621d * l - 3.3077115913d * m + 0.2309699292d * s,
				-1.2684380046d * l + 2.6097574011d * m - 0.3413193965d * s,
				-0.0041960863d * l - 0.7034186147d * m + 1.7076147010d * s };
		int[] result = new int[3];
		for (int i = 0; i < 3; i++) {
			double x = Math.max(0.0d, Math.min(1.0d, linearRgb[i]));
			double encoded = x <= 0.0031308d ? 12.92d * x : 1.055d * Math.pow(x, 1.0d / 2.4d) - 0.055d;
			result[i] = (int)Math.round(255.0d * encoded);
		}
		return result;
	}
}

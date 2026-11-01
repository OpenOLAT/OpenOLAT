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

	private static final List<String> VARIANTS = List.of(Swatch.SOLID, Swatch.TINT, Swatch.DARK);

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
		Assert.assertEquals(15, palette.getHues().size());

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
		Assert.assertEquals(150, count);
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
		Assert.assertEquals("#4166E8", palette.bg(new Swatch("blue", Swatch.SOLID)));
		Assert.assertEquals(Palette.WHITE, palette.fg(new Swatch("blue", Swatch.SOLID)));
		Assert.assertEquals("#0C1D54", palette.fg(new Swatch("blue", Swatch.TINT)));
		Assert.assertEquals("#203CA2", palette.bg(new Swatch("blue", Swatch.DARK)));
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

	@Test
	public void cssWithOnlyIncompleteHues() {
		Assert.assertNull(Palette.fromCss(":root{--o-red-50: #FEF5F5;--o-red-100: #FEE7E6}"));
	}

	@Test
	public void contrast() {
		Assert.assertEquals(21.0d, ColorContrast.contrast("#000000", "#FFFFFF"), 0.001d);
		Assert.assertEquals(1.0d, ColorContrast.contrast("#4166E8", "#4166E8"), 0.001d);
		Assert.assertEquals(4.89d, ColorContrast.contrast("#4166E8", "#FFFFFF"), 0.01d);
	}
}

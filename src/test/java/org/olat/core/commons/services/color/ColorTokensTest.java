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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

/**
 * Checks the colour tokens of modules/_tokens.scss in the compiled
 * <code>light/theme.css</code> (OO-9994, OO-10043): every semantic token
 * reads its base colour, every state token reads a palette step, every
 * token resolves to a value, every var(--o-*) of the compiled theme has a
 * definition, the token pairs keep their contrast, the button rules read
 * the tokens and their shade tokens, and no module calls o-a11y-color().
 *
 * Initial date: 8 Nov 2026<br>
 * @author gnaegi, https://www.frentix.com
 */
public class ColorTokensTest {

	private static final List<String> SEMANTIC = List.of("text", "text-muted", "heading", "bg", "bg-subtle", "border",
			"border-strong", "primary", "primary-fg", "primary-hover", "primary-text", "link", "link-hover", "focus");
	private static final List<String> STATES = List.of("neutral", "success", "warning", "critical", "danger",
			"finished", "inactive", "highlight");
	private static final List<String> ROLES = List.of("text", "bg", "fg", "border", "soft-bg", "soft-fg", "soft-border");

	private static final Pattern CUSTOM_PROPERTY = Pattern.compile("--o-([a-z0-9-]+)\\s*:\\s*([^;}]+)");
	private static final Pattern VAR_REFERENCE = Pattern.compile("var\\(--o-([a-z0-9-]+)\\)");
	private static final Pattern ANY_VAR_REFERENCE = Pattern.compile("var\\(--o-([a-z0-9-]+)");
	private static final Pattern PALETTE_REFERENCE = Pattern.compile("var\\(--o-(white|[a-z]+\\d*-\\d+)\\)");
	private static final Pattern HEX = Pattern.compile("#[0-9A-F]{6}");
	private static final Pattern COLOR = Pattern.compile("#[0-9A-Fa-f]{3,8}|rgba?\\([^)]*\\)|transparent");

	private static final List<String> SHADE_BUTTONS = List.of("btn-primary", "btn-default", "btn-dirty",
			"coursesite-start", "coursesite-book");
	// The text colours that a module adjusted with o-a11y-color() for the background of one rule
	private static final List<String> ADJUSTED_TEXT_TOKENS = List.of("login-help-link", "table-link",
			"table-striped-link", "toolbar-breadcrumb-link", "badge-row-expanded-link",
			"badge-row-expanded-link-hover", "badge-status-info", "badge-status-success", "badge-status-warning",
			"badge-status-danger", "course-style-link", "course-style-peekview-link", "forum-message-header-author",
			"forum-message-header-modified", "forum-message-footer-attachment");
	// The compiled themes that emit the tokens (not email.css)
	private static final List<String> TOKEN_FILES = List.of("light/theme.css", "light/content.css", "light/oo-docs.css",
			"openolat/theme.css", "openolat/content.css");

	private final Map<String, String> tokens = loadTokens();

	@Test
	public void semanticTokensReadBaseColors() {
		for (String name : SEMANTIC) {
			Assert.assertEquals(name, "var(--o-base-" + name + ")", tokens.get(name));
			String base = tokens.get("base-" + name);
			Assert.assertNotNull("base-" + name, base);
			Assert.assertTrue("base-" + name + ": " + base, HEX.matcher(base).matches());
		}
	}

	@Test
	public void stateTokensReadPaletteSteps() {
		for (String state : STATES) {
			for (String role : ROLES) {
				String name = "state-" + state + "-" + role;
				String value = tokens.get(name);
				Assert.assertNotNull(name, value);
				Assert.assertTrue(name + ": " + value, PALETTE_REFERENCE.matcher(value).matches());
				Assert.assertTrue(name + ": " + value, HEX.matcher(resolve(name)).matches());
			}
		}
	}

	@Test
	public void stateContrast() {
		for (String state : STATES) {
			String prefix = "state-" + state + "-";
			assertContrast(prefix + "fg", prefix + "bg", 4.5);
			assertContrast(prefix + "soft-fg", prefix + "soft-bg", 4.5);
			assertContrast(prefix + "text", "white", 4.5);
			assertContrast(prefix + "text", prefix + "soft-bg", 4.5);
			assertContrast(prefix + "border", "white", 3.0);
		}
	}

	@Test
	public void semanticContrast() {
		assertContrast("text", "bg", 4.5);
		assertContrast("text-muted", "bg", 4.5);
		assertContrast("heading", "bg", 4.5);
		assertContrast("link", "bg", 4.5);
		assertContrast("link-hover", "bg", 4.5);
		assertContrast("primary-text", "bg", 4.5);
		assertContrast("primary-fg", "primary", 4.5);
	}

	/**
	 * The pairs of the element tokens (base, framework and component layers)
	 * of the light theme. Not checked, below 4.5:1 in the light theme today:
	 * navbar-text and navbar-tools on navbar-bg (4.48:1); btn-dirty-fg and
	 * coursesite-book-fg on their -bg-hover (3.95:1) and -bg-active (2.94:1).
	 */
	@Test
	public void elementContrast() {
		assertContrast("btn-primary-fg", "btn-primary-bg", 4.5);
		assertContrast("btn-default-fg", "btn-default-bg", 4.5);
		assertContrast("btn-dirty-fg", "btn-dirty-bg", 4.5);
		for (String button : List.of("btn-primary", "btn-default", "coursesite-start")) {
			assertContrast(button + "-fg", button + "-bg-hover", 4.5);
			assertContrast(button + "-fg", button + "-bg-active", 4.5);
		}
		assertContrast("navbar-link", "navbar-bg", 4.5);
		assertContrast("footer-text", "footer-inner-bg", 4.5);
		assertContrast("footer-link", "footer-inner-bg", 4.5);
		assertContrast("dialog-header", "bg", 4.5);
		assertContrast("content-header", "bg", 4.5);
		assertContrast("content-link", "bg", 4.5);
		assertContrast("coursesite-start-fg", "coursesite-start-bg", 4.5);
		assertContrast("coursesite-book-fg", "coursesite-book-bg", 4.5);
		assertContrast("coursesite-start-xs-fg", "coursesite-start-xs-bg", 4.5);
		assertContrast("coursesite-title", "bg", 4.5);
		assertContrast("autocomplete-suggestion-fg", "autocomplete-suggestion-bg", 4.5);
		assertContrast("chelp-fg", "chelp-bg", 4.5);
		assertContrast("datecomp-day-fg", "datecomp-day-bg", 4.5);
		assertContrast("swiper-button-fg", "swiper-button-bg", 4.5);
		assertContrast("tag-selection-fg", "tag-selection-bg", 4.5);
		assertContrast("tag-selection-selected-fg", "tag-selection-selected-bg", 4.5);
		assertContrast("wizard-step-main-active-fg", "wizard-step-main-active-bg", 4.5);
		assertContrast("reminder-marker-fg", "reminder-marker-bg", 4.5);
		assertContrast("topicbroker-title-abbr-fg", "topicbroker-title-abbr-bg", 4.5);
		assertContrast("toolbar-breadcrumb-link", "toolbar-breadcrumb-bg", 4.5);
		assertContrast("forum-message-header-author", "forum-message-header-bg", 4.5);
		assertContrast("forum-message-header-modified", "forum-message-header-bg", 4.5);
		assertContrast("forum-message-footer-attachment", "forum-message-footer-bg", 4.5);
	}

	/**
	 * No module calls o-a11y-color(): only _tokens.scss does, for the default
	 * of a token, and _config.scss, for the default of a Sass variable. The
	 * 15 text colours that a module adjusted with the function are tokens
	 * (ADJUSTED_TEXT_TOKENS). At least one rule of the compiled theme reads
	 * each of them. The test does not cover the Sass variables with an
	 * adjusted default that a rule reads without a token.
	 */
	@Test
	public void rulesReadAdjustedTextTokens() throws IOException {
		try (Stream<Path> modules = Files.list(ThemeColorRatchetTest.LIGHT_MODULES)) {
			for (Path module : modules.filter(m -> m.getFileName().toString().endsWith(".scss")).toList()) {
				String name = module.getFileName().toString();
				if (!"_tokens.scss".equals(name)) {
					Assert.assertFalse(name + " calls o-a11y-color(): add a token with the call as default",
							ThemeColorRatchetTest.withoutComments(read(module)).contains("o-a11y-color("));
				}
			}
		}
		String rules = CUSTOM_PROPERTY.matcher(read(PaletteTest.LIGHT_THEME_CSS)).replaceAll("");
		for (String token : ADJUSTED_TEXT_TOKENS) {
			Assert.assertTrue("--o-" + token + " has no definition", tokens.containsKey(token));
			Assert.assertTrue("No rule reads var(--o-" + token + ")", rules.contains("var(--o-" + token + ")"));
		}
	}

	/**
	 * The shade tokens of the buttons (hover, focus and active state of the
	 * mixin button-variant): a hex colour computed at compile time, the same
	 * colour as the Sass function darken() of the token it comes from. The
	 * background is 10% and 17% darker, the border 12% and 25%.
	 */
	@Test
	public void buttonShadeTokens() {
		for (String button : SHADE_BUTTONS) {
			assertShade(button + "-bg-hover", button + "-bg", 10);
			assertShade(button + "-bg-active", button + "-bg", 17);
			assertShade(button + "-border-hover", button + "-border", 12);
			assertShade(button + "-border-focus", button + "-border", 25);
		}
	}

	/**
	 * The button rules of the compiled theme read the tokens: the primary
	 * and the default button read their three colour tokens, and at least
	 * one rule reads each shade token. A rule that reads the Sass variable
	 * again compiles to a colour and fails here.
	 */
	@Test
	public void buttonRulesReadTokens() {
		String css = read(PaletteTest.LIGHT_THEME_CSS);
		for (String button : List.of("btn-primary", "btn-default")) {
			Pattern rule = Pattern.compile("(?:^|[},])\\s*\\." + button + "\\s*\\{([^}]*)\\}");
			boolean found = false;
			Matcher m = rule.matcher(css);
			while (m.find() && !found) {
				String declarations = m.group(1).replaceAll("\\s", "");
				found = declarations.contains("color:var(--o-" + button + "-fg)")
						&& declarations.contains("background-color:var(--o-" + button + "-bg)")
						&& declarations.contains("border-color:var(--o-" + button + "-border)");
			}
			Assert.assertTrue("No rule ." + button + " that reads --o-" + button + "-fg, -bg and -border", found);
		}
		for (String button : SHADE_BUTTONS) {
			for (String shade : List.of("-bg-hover", "-bg-active", "-border-hover", "-border-focus")) {
				String reference = "var(--o-" + button + shade + ")";
				Assert.assertTrue("No rule reads " + reference, css.contains(reference));
			}
		}
	}

	/**
	 * Every token of the :root block of _tokens.scss (the block with
	 * --o-white) resolves to a colour. A keyword like "none" in a token
	 * behaves differently from the same keyword in the rule.
	 */
	@Test
	public void tokensResolveToColors() {
		String css = read(PaletteTest.LIGHT_THEME_CSS);
		int white = css.indexOf("--o-white:");
		Assert.assertTrue("No --o-white in " + PaletteTest.LIGHT_THEME_CSS, white >= 0);
		String block = css.substring(css.lastIndexOf('{', white), css.indexOf('}', white));
		Map<String, String> colorTokens = parseTokens(block);
		Assert.assertTrue("Too few tokens: " + colorTokens.size(), colorTokens.size() > 100);
		for (String name : colorTokens.keySet()) {
			String value = resolve(name);
			Assert.assertTrue("--o-" + name + ": " + value, COLOR.matcher(value).matches());
		}
	}

	/**
	 * Every var(--o-*) in the compiled theme.css, content.css and oo-docs.css
	 * of the themes light and openolat has a definition in the same file.
	 * The compile does not check the names.
	 */
	@Test
	public void referencesHaveDefinitions() {
		Path themes = PaletteTest.LIGHT_THEME_CSS.getParent().getParent();
		for (String file : TOKEN_FILES) {
			String css = read(themes.resolve(file));
			Map<String, String> defined = parseTokens(css);
			Matcher m = ANY_VAR_REFERENCE.matcher(css);
			while (m.find()) {
				Assert.assertTrue(file + ": var(--o-" + m.group(1) + ") has no definition", defined.containsKey(m.group(1)));
			}
		}
	}

	private void assertContrast(String fg, String bg, double ratio) {
		String fgHex = resolve(fg);
		String bgHex = resolve(bg);
		double contrast = ColorContrast.contrast(fgHex, bgHex);
		Assert.assertTrue(fg + " " + fgHex + " on " + bg + " " + bgHex + ": " + contrast, contrast >= ratio);
	}

	private void assertShade(String shade, String source, int percent) {
		String shadeValue = tokens.get(shade);
		Assert.assertNotNull(shade, shadeValue);
		Assert.assertTrue(shade + ": " + shadeValue, HEX.matcher(shadeValue).matches());
		String sourceHex = resolve(source);
		Assert.assertTrue(source + ": " + sourceHex, HEX.matcher(sourceHex).matches());
		int[] expected = darken(sourceHex, percent);
		// The token value is a rounded colour: 2 of 255 per channel
		for (int i = 0; i < 3; i++) {
			int channel = Integer.parseInt(shadeValue.substring(1 + 2 * i, 3 + 2 * i), 16);
			Assert.assertTrue(shade + " " + shadeValue + " is not " + source + " " + sourceHex + " darkened by "
					+ percent + "%: rgb(" + expected[0] + ", " + expected[1] + ", " + expected[2] + ")",
					Math.abs(channel - expected[i]) <= 2);
		}
	}

	/**
	 * The Sass function darken(): the HSL lightness minus the amount.
	 *
	 * @param hex The colour as #RRGGBB
	 * @param percent The amount in percent of the lightness scale
	 * @return The red, green and blue channel, 0 to 255
	 */
	private static int[] darken(String hex, int percent) {
		double r = Integer.parseInt(hex.substring(1, 3), 16) / 255d;
		double g = Integer.parseInt(hex.substring(3, 5), 16) / 255d;
		double b = Integer.parseInt(hex.substring(5, 7), 16) / 255d;
		double max = Math.max(r, Math.max(g, b));
		double min = Math.min(r, Math.min(g, b));
		double delta = max - min;
		double lightness = (max + min) / 2d;
		double hue = 0d;
		double saturation = 0d;
		if (delta > 0d) {
			saturation = delta / (1d - Math.abs(2d * lightness - 1d));
			if (max == r) {
				hue = ((g - b) / delta + 6d) % 6d;
			} else if (max == g) {
				hue = (b - r) / delta + 2d;
			} else {
				hue = (r - g) / delta + 4d;
			}
		}
		lightness = Math.max(0d, lightness - percent / 100d);
		double chroma = (1d - Math.abs(2d * lightness - 1d)) * saturation;
		double x = chroma * (1d - Math.abs(hue % 2d - 1d));
		double m = lightness - chroma / 2d;
		double[] rgb = switch ((int) hue) {
			case 0 -> new double[] { chroma, x, 0d };
			case 1 -> new double[] { x, chroma, 0d };
			case 2 -> new double[] { 0d, chroma, x };
			case 3 -> new double[] { 0d, x, chroma };
			case 4 -> new double[] { x, 0d, chroma };
			default -> new double[] { chroma, 0d, x };
		};
		return new int[] { (int) Math.round((rgb[0] + m) * 255d), (int) Math.round((rgb[1] + m) * 255d),
				(int) Math.round((rgb[2] + m) * 255d) };
	}

	/**
	 * @param name The token name without "--o-"
	 * @return The hex value after following the var() references
	 */
	private String resolve(String name) {
		String value = tokens.get(name);
		for (int i = 0; value != null && i < 10; i++) {
			Matcher m = VAR_REFERENCE.matcher(value);
			if (!m.matches()) {
				return value;
			}
			value = tokens.get(m.group(1));
		}
		throw new AssertionError("Cannot resolve --o-" + name);
	}

	private static Map<String, String> loadTokens() {
		return parseTokens(read(PaletteTest.LIGHT_THEME_CSS));
	}

	private static Map<String, String> parseTokens(String css) {
		Map<String, String> tokens = new HashMap<>();
		Matcher m = CUSTOM_PROPERTY.matcher(css);
		while (m.find()) {
			tokens.putIfAbsent(m.group(1), m.group(2).trim());
		}
		return tokens;
	}

	private static String read(Path file) {
		try {
			return new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new AssertionError("Cannot read " + file.toAbsolutePath(), e);
		}
	}
}

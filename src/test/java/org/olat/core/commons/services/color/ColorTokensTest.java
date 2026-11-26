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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
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
 * the tokens and their shade tokens, the link rules read the link tokens,
 * the shade tokens of the module colour maths are darker or lighter by the
 * percentage of the module and a rule reads each of them, the translucent
 * colours mix a token with transparent, the muted texts read the text
 * tokens, the neutral scale has its default values, no module calls
 * o-a11y-color(), the module rules read the state tokens instead of the
 * Bootstrap state colours ($brand-success, $state-danger-text, ...), and the
 * Bootstrap state rules, the message boxes and the evaluation form read the
 * state tokens and their shade tokens, the hover, pressed and off states keep
 * the contrast of the normal state, and the colours without a role read the
 * tokens backdrop, shadow-color or an own element token,
 * the hover and pressed states read a text token of the state and stay
 * visible, and the selectus progress bars read the tokens of the rating
 * buttons, the selectus abstention bar, marks and
 * explanation texts read the tokens of the rating buttons, the course style
 * border reads a token, a black state fill gets lighter and the navbar text
 * passes, every control has the same focus
 * ring in the adjusted primary colour, the selectus marks
 * and letters keep their contrast on the table rows and read the tokens of
 * the rating buttons, and the literal colours of the
 * review read semantic, state, palette or own element tokens, except the
 * kept literals and the rules without a reader. The framework,
 * assessment and content modules read element tokens fed by their Sass
 * variables, a translucent default included; the shorthand borders are width,
 * style and a colour token.
 *
 * Initial date: 8 Nov 2026<br>
 * @author gnaegi, https://www.frentix.com
 */
public class ColorTokensTest {

	private static final List<String> SEMANTIC = List.of("text", "text-muted", "text-disabled", "heading", "bg",
			"bg-subtle", "bg-hover", "bg-selected", "bg-raised", "border", "border-subtle", "border-strong", "primary",
			"primary-fg", "primary-hover", "primary-text", "link", "link-hover", "focus", "shadow-color", "backdrop",
			"scrim");
	// The neutral scale of _palette_neutral.scss: step and default colour (hue 260, chroma 0.038)
	private static final Map<Integer, String> NEUTRAL = neutralDefaults();
	private static final List<String> STATES = List.of("neutral", "success", "warning", "critical", "danger",
			"finished", "inactive", "highlight");
	private static final List<String> ROLES = List.of("text", "bg", "fg", "border", "soft-bg", "soft-fg", "soft-border");

	private static final Pattern CUSTOM_PROPERTY = Pattern.compile("--o-([a-z0-9-]+)\\s*:\\s*([^;}]+)");
	private static final Pattern VAR_REFERENCE = Pattern.compile("var\\(--o-([a-z0-9-]+)\\)");
	private static final Pattern ANY_VAR_REFERENCE = Pattern.compile("var\\(--o-([a-z0-9-]+)");
	private static final Pattern PALETTE_REFERENCE = Pattern.compile("var\\(--o-(white|[a-z]+\\d*-\\d+)\\)");
	// A step of the three colour scales: white, the neutral scale, the brand ramp or a chromatic hue of the palette
	private static final Pattern SCALE_REFERENCE = Pattern.compile(
			"var\\(--o-(white|neutral-\\d+|theme\\d+-\\d+|(?:red|orange|yellow|lime|green|teal|cyan|blue|violet|magenta|pink|brown)-\\d+)\\)");
	private static final Pattern HEX = Pattern.compile("#[0-9A-F]{6}");
	private static final Pattern HEX_LITERAL = Pattern.compile("#[0-9A-Fa-f]{3,8}\\b");
	private static final Pattern COLOR = Pattern.compile("#[0-9A-Fa-f]{3,8}|rgba?\\([^)]*\\)|transparent");
	// A translucent token colour: the token mixed with transparent
	private static final Pattern COLOR_MIX = Pattern.compile("color-mix\\(in srgb, var\\(--o-([a-z0-9-]+)\\) [0-9.]+%, transparent\\)");

	private static final List<String> SHADE_BUTTONS = List.of("btn-primary", "btn-default", "btn-danger", "btn-dirty",
			"coursesite-start", "coursesite-book", "btn-primary-light", "btn-danger-light", "table-filter",
			"table-filter-active");
	// The active fill of a fill with dark text is 16% darker, else 17%
	// (o-token-active-percent() in _tokens.scss): the fills of the warning
	// state, solid yellow (step 300) with dark text
	private static final Map<String, Integer> ACTIVE_PERCENT = Map.of("btn-dirty", 16, "coursesite-book", 16,
			"state-warning", 16);
	// The buttons of $o-token-state-text-buttons with a dark text in light: the hover
	// and the pressed fill go lighter (o-token-state-lighter() in _tokens.scss)
	private static final List<String> LIGHTER_TEXT_BUTTONS = List.of("btn-dirty");
	// The buttons whose pressed fill is darker by the amount of Bootstrap at most:
	// the text keeps 4.5:1 (o-contrast-shade-amount() in _functions.scss)
	private static final List<String> CONTRAST_SHADE_BUTTONS = List.of("btn-primary-light", "btn-danger-light",
			"table-filter", "table-filter-active");
	// The smallest visible amount of a hover or pressed fill: below, the fill
	// keeps the amount of Bootstrap and the text of the state is adjusted
	// (o-contrast-shade-amount(), o-contrast-shade-text())
	private static final int MIN_SHADE_AMOUNT = 3;
	// The rating buttons of selectus: fill hover and active (the border is btn-default-border)
	private static final List<String> SELECTUS_RATINGS = List.of("a", "b", "c", "abstain");
	// The shade tokens of the colour maths of the modules: the percentage of darken(), negative for lighten()
	private static final List<Shade> MODULE_SHADES = List.of(
			new Shade("chelp-bg-hover", "chelp-bg", 8), new Shade("chelp-border-hover", "chelp-border", 12),
			new Shade("content-h5-hover", "content-h5", 10), new Shade("content-dimmed-hover", "content-dimmed", 10),
			new Shade("coursesite-title-hover", "coursesite-title", 10), new Shade("focus-edge", "focus", 10),
			new Shade("file-upload-btn-bg-hover", "btn-default-bg", 5),
			new Shade("immunity-proof-validated-bg-tint", "immunity-proof-validated-bg", -31),
			new Shade("immunity-proof-validated-fg", "immunity-proof-validated-bg", -33),
			new Shade("immunity-proof-validated-bg-edge", "immunity-proof-validated-bg", 31),
			new Shade("immunity-proof-claimed-bg-tint", "immunity-proof-claimed-bg", -45),
			new Shade("immunity-proof-claimed-fg", "immunity-proof-claimed-bg", -45),
			new Shade("immunity-proof-claimed-bg-edge", "immunity-proof-claimed-bg", 23),
			new Shade("immunity-proof-none-bg-tint", "immunity-proof-none-bg", -48),
			new Shade("immunity-proof-none-fg", "immunity-proof-none-bg", -48),
			new Shade("immunity-proof-none-bg-edge", "immunity-proof-none-bg", 25),
			new Shade("tag-bg-hover", "tag-bg", 10), new Shade("tag-bg-active", "tag-bg", 15),
			new Shade("tag-selected-bg-hover", "tag-selected-bg", 10),
			new Shade("tag-selected-bg-active", "tag-selected-bg", 15),
			new Shade("tag-selection-bg-hover", "tag-selection-bg", 10),
			new Shade("tag-selection-bg-active", "tag-selection-bg", 15),
			new Shade("tag-selection-border-hover", "tag-selection-border", 12),
			new Shade("tag-selection-selected-bg-hover", "tag-selection-selected-bg", 10),
			new Shade("tag-selection-selected-bg-active", "tag-selection-selected-bg", 15),
			new Shade("tag-selection-selected-border-hover", "tag-selection-selected-border", 10),
			new Shade("tag-selection-selected-border-active", "tag-selection-selected-border", 15),
			// colour maths with a state colour as source (OO-10043, brand colours to states)
			new Shade("toolbar-close-hover", "toolbar-close", 15),
			new Shade("addremove-add-bg-hover", "addremove-add-bg", 10),
			new Shade("addremove-remove-bg-hover", "addremove-remove-bg", 10),
			new Shade("tag-selected-border-hover", "tag-selected-border", 10),
			new Shade("tag-selected-border-active", "tag-selected-border", 15),
			new Shade("tag-selection-remove-selected-bg-hover", "tag-selection-remove-selected-bg", 10),
			new Shade("tag-selection-remove-selected-bg-active", "tag-selection-remove-selected-bg", 15),
			new Shade("tag-selection-remove-selected-border-hover", "tag-selection-remove-selected-border", 10),
			new Shade("tag-selection-remove-selected-border-active", "tag-selection-remove-selected-border", 15),
			new Shade("competence-selected-border-hover", "competence-selected-border", 10),
			new Shade("competence-selected-border-active", "competence-selected-border", 15),
			new Shade("tinymce-danger-bg-edge", "tinymce-danger-bg", 25),
			// the remaining colour maths
			new Shade("registration-otp-error-bg-focus", "box-error-bg", 10),
			new Shade("page-spacer-border-hover", "border-subtle", 10),
			new Shade("editor-part-toolbar-link-hover", "editor-part-toolbar-link", 10),
			new Shade("popover-title-bg-edge", "popover-title-bg", 5),
			new Shade("im-chat-from-hover", "im-chat-from", 10),
			new Shade("panel-placeholder-border-hover", "panel-placeholder-border", 10),
			new Shade("highscore-podium-first-bg-stop-1", "highscore-podium-first-bg", -40),
			new Shade("highscore-podium-first-bg-stop-2", "highscore-podium-first-bg", -10),
			new Shade("highscore-podium-first-bg-stop-4", "highscore-podium-first-bg", -20),
			new Shade("highscore-podium-second-bg-stop-1", "highscore-podium-second-bg", -40),
			new Shade("highscore-podium-second-bg-stop-2", "highscore-podium-second-bg", -10),
			new Shade("highscore-podium-second-bg-stop-4", "highscore-podium-second-bg", -20),
			new Shade("highscore-podium-third-bg-stop-1", "highscore-podium-third-bg", -40),
			new Shade("highscore-podium-third-bg-stop-2", "highscore-podium-third-bg", -10),
			new Shade("highscore-podium-third-bg-stop-4", "highscore-podium-third-bg", -20),
			new Shade("table-sticky-col-edge", "table-border", 10),
			new Shade("competence-bg-hover", "competence-bg", 10), new Shade("competence-bg-active", "competence-bg", 15),
			new Shade("labeled-bg-hover", "labeled-bg", 10), new Shade("labeled-border-hover", "labeled-border", 12),
			new Shade("labeled-light-bg-hover", "labeled-light-bg", 10),
			new Shade("labeled-light-border-hover", "labeled-light-border", 12),
			new Shade("video-bright-edge", "video-bright", 10),
			new Shade("catalog-header-noimage-stop-1", "primary", -80),
			new Shade("catalog-header-noimage-stop-3", "primary", -55));
	// The Bootstrap state colours that a module must not read: read a state token
	private static final Pattern BOOTSTRAP_STATE_VARIABLE = Pattern.compile(
			"\\$(?:brand-(?:success|info|warning|danger)|state-[a-z]+-(?:text|bg|border)|alert-[a-z]+-(?:text|bg|border)"
			+ "|btn-(?:success|info|warning|danger)-[a-z]+|label-(?:success|info|warning|danger)-bg"
			+ "|progress-bar-(?:success|info|warning|danger)-bg|panel-(?:success|info|warning|danger)-[a-z-]+)(?![\\w-])");
	// Module rules that keep a Bootstrap state colour: none since the Bootstrap
	// rules read the state tokens
	private static final List<String> BOOTSTRAP_STATE_KEPT = List.of();
	// The shade tokens of the state tokens: the Bootstrap state buttons (the
	// percentages of the buttons), the hover of the labels, of the contextual
	// table rows, list group items and .bg-* links (soft-bg), of the .text-*
	// links and the focus border of a field with a validation state (text)
	private static final List<Shade> STATE_SHADES = stateShades();
	// The text colours that a module adjusted with o-a11y-color() for the background of one rule
	private static final List<String> ADJUSTED_TEXT_TOKENS = List.of("login-help-link", "table-link",
			"table-striped-link", "toolbar-breadcrumb-link", "badge-row-expanded-link",
			"badge-row-expanded-link-hover", "badge-status-info", "badge-status-success", "badge-status-warning",
			"badge-status-danger", "course-style-link", "course-style-peekview-link", "forum-message-header-author",
			"forum-message-header-modified", "forum-message-footer-attachment");
	// A custom property of the scales: palette step, neutral step, brand ramp, swatch role or white
	private static final Pattern SCALE_PROPERTY = Pattern.compile("(white|[a-z]+\\d*-\\d+|sw-[a-z-]+|theme\\d+)");
	// The bright fills of light (step 100 to 400 with dark text) and their hue: the hover and the
	// pressed fill go one step lighter inside the hue when no darker step keeps the text. The solid
	// yellow of the warning state (step 300) and the fixed fill of the rating button B (step 400)
	private static final Map<String, String> BRIGHT_FILLS = Map.of("coursesite-book-bg", "yellow",
			"state-warning-bg", "yellow", "selectus-rating-b-bg", "yellow");
	// The element tokens of the links that a rule of the compiled theme reads
	private static final List<String> LINK_RULE_TOKENS = List.of("toolbar-link", "toolbar-link-hover",
			"toolbar-link-active", "toolbar-nav-link", "toolbar-nav-link-hover", "tree-link-active-parent",
			"tree-link-active-parent-hover", "segments-tab-link", "selectus-review-comment-link");
	// The tokens of the Bootstrap colour variables and greys that the modules read, with the light value. The
	// greys read the neutral scale: #EEEEEE neutral-100, #F5F5F5 neutral-50, #333333
	// neutral-800, #262626 neutral-900, #E5E5E5 neutral-150, #555555 neutral-600, #999999 neutral-450,
	// #777777 neutral-500; the translucent black reads shadow-color
	private static final Map<String, String> BOOTSTRAP_COMPONENT_TOKENS = Map.ofEntries(
			Map.entry("blockquote-border", "#EAECF0"), Map.entry("code-block-bg", "#F6F7FA"),
			Map.entry("code-block-fg", "#2A3649"), Map.entry("code-block-border", "color-mix(in srgb, var(--o-shadow-color) 10%, transparent)"),
			Map.entry("dropdown-link", "#2A3649"), Map.entry("dropdown-link-hover", "#182233"),
			Map.entry("dropdown-link-hover-bg", "#F6F7FA"), Map.entry("dropdown-divider", "#E4E7EC"),
			Map.entry("input-bg", "#FFFFFF"), Map.entry("input-fg", "#344054"),
			Map.entry("input-disabled-bg", "#F9FAFB"), Map.entry("input-placeholder", "#657286"),
			Map.entry("legend", "#2A3649"), Map.entry("legend-border", "#E4E7EC"),
			Map.entry("modal-content-border", "#E4E7EC"), Map.entry("modal-header-border", "#E4E7EC"),
			Map.entry("popover-bg", "#FFFFFF"), Map.entry("popover-border", "color-mix(in srgb, var(--o-shadow-color) 20%, transparent)"),
			Map.entry("panel-bg", "#FFFFFF"), Map.entry("progress-bar-fg", "#FFFFFF"),
			Map.entry("thumbnail-border", "#DDDDDD"), Map.entry("table-border", "#DDDDDD"),
			Map.entry("table-striped-bg", "#F9FAFB"), Map.entry("table-hover-bg", "#C8DFFF"),
			Map.entry("alert-close", "#657286"), Map.entry("alert-close-hover", "#4E596D"),
			Map.entry("message-table-border", "#2A3649"), Map.entry("chart-help-line", "#657286"),
			Map.entry("content-table-header-bg", "#EAECF0"), Map.entry("editor-media-unavailable", "#8B95A7"),
			Map.entry("room-status-inactive", "#657286"), Map.entry("selectus-review-comments-bg", "#FFFFFF"),
			Map.entry("selectus-review-comments-border", "#EAECF0"), Map.entry("qti-gaptext-alternative-bg", "#EAECF0"),
			Map.entry("qti-gaptext-alternative-fg", "#4E596D"), Map.entry("qti-help", "#657286"),
			Map.entry("video-slider-bg", "#4E596D"), Map.entry("videotask-marker-assign-bg", "#2A3649"));
	private static final Pattern RULE = Pattern.compile("([^{}]+)\\{([^{}]*)\\}");
	// The compiled themes that emit the tokens (not email.css)
	private static final List<String> TOKEN_FILES = List.of("light/theme.css", "light/content.css", "light/oo-docs.css",
			"openolat/theme.css", "openolat/content.css");
	// Custom properties that are no colour tokens: a renderer sets them at runtime
	// in an inline style and the rule that reads them has a fallback.
	// onav-cols: the column count of a group of the overview navigation (OverviewNavigationRenderer)
	private static final Set<String> RUNTIME_PROPERTIES = Set.of("onav-cols");

	private final Map<String, String> tokens = loadTokens();

	/**
	 * Each semantic token reads its base colour, and the base colour of the
	 * light theme reads a step of the three scales: the neutral scale, the
	 * brand ramp or white.
	 */
	@Test
	public void semanticTokensReadBaseColors() {
		for (String name : SEMANTIC) {
			Assert.assertEquals(name, "var(--o-base-" + name + ")", tokens.get(name));
			String base = tokens.get("base-" + name);
			Assert.assertNotNull("base-" + name, base);
			Assert.assertTrue("base-" + name + ": " + base, SCALE_REFERENCE.matcher(base).matches());
			Assert.assertTrue("base-" + name + ": " + base, HEX.matcher(resolve("base-" + name)).matches());
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
			// the border of the fill has no contrast rule: one step darker than the fill
		}
	}

	@Test
	public void semanticContrast() {
		assertContrast("text", "bg", 4.5);
		assertContrast("text-muted", "bg", 4.5);
		assertContrast("text", "bg-hover", 4.5);
		assertContrast("text", "bg-selected", 4.5);
		assertContrast("text", "bg-raised", 4.5);
		assertContrast("heading", "bg", 4.5);
		assertContrast("link", "bg", 4.5);
		assertContrast("link-hover", "bg", 4.5);
		assertContrast("primary-text", "bg", 4.5);
		assertContrast("primary-fg", "primary", 4.5);
		assertContrast("focus", "bg", 3.0);
		assertContrast("focus", "bg-subtle", 3.0);
	}

	/**
	 * The pairs of the element tokens (base, framework and component layers)
	 * of the light theme. The navbar text and the navbar tools pass
	 * (adjusted to 4.5:1 on the navbar). The hover, pressed and off states have the
	 * contrast of the normal state. The focus ring of the navbar
	 * passes on the navbar. No pair of the light theme fails.
	 */
	@Test
	public void elementContrast() {
		assertContrast("btn-primary-fg", "btn-primary-bg", 4.5);
		assertContrast("btn-default-fg", "btn-default-bg", 4.5);
		assertContrast("btn-dirty-fg", "btn-dirty-bg", 4.5);
		assertContrast("btn-danger-fg", "btn-danger-bg", 4.5);
		assertContrast("btn-dirty-fg", "btn-dirty-bg-hover", 4.5);
		assertContrast("coursesite-book-fg", "coursesite-book-bg-hover", 4.5);
		assertContrast("tag-selection-remove-selected-fg", "tag-selection-remove-selected-bg", 4.5);
		for (String button : List.of("btn-primary", "btn-default", "btn-danger", "coursesite-start")) {
			assertContrast(button + "-fg", button + "-bg-hover", 4.5);
			assertContrast(button + "-fg", button + "-bg-active", 4.5);
		}
		assertContrast("navbar-link", "navbar-bg", 4.5);
		assertContrast("navbar-text", "navbar-bg", 4.5);
		assertContrast("navbar-tools", "navbar-bg", 4.5);
		assertContrast("navbar-focus", "navbar-bg", 3.0);
		assertContrast("footer-focus", "footer-inner-bg", 3.0);
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
		assertContrast("chelp-fg", "chelp-bg-hover", 4.5);
		assertContrast("coursesite-title-hover", "bg", 4.5);
		assertContrast("datecomp-day-fg", "datecomp-day-bg", 4.5);
		assertContrast("swiper-button-fg", "swiper-button-bg", 4.5);
		assertContrast("tag-selection-fg", "tag-selection-bg", 4.5);
		assertContrast("tag-selection-selected-fg", "tag-selection-selected-bg", 4.5);
		for (String state : List.of("-hover", "-active")) {
			assertContrast("tag-selection-fg", "tag-selection-bg" + state, 4.5);
			assertContrast("tag-selection-selected-fg", "tag-selection-selected-bg" + state, 4.5);
		}
		assertContrast("wizard-step-main-active-fg", "wizard-step-main-active-bg", 4.5);
		assertContrast("reminder-marker-fg", "reminder-marker-bg", 4.5);
		assertContrast("topicbroker-title-abbr-fg", "topicbroker-title-abbr-bg", 4.5);
		assertContrast("toolbar-breadcrumb-link", "toolbar-breadcrumb-bg", 4.5);
		assertContrast("forum-message-header-author", "forum-message-header-bg", 4.5);
		assertContrast("forum-message-header-modified", "forum-message-header-bg", 4.5);
		assertContrast("forum-message-footer-attachment", "forum-message-footer-bg", 4.5);
		assertContrast("link-subtle", "bg-subtle", 4.5);
		assertContrast("nav-link", "bg", 4.5);
		assertContrast("nav-link-hover", "bg", 4.5);
		assertContrast("segments-tab-link", "bg", 4.5);
		assertContrast("selectus-review-comment-link", "bg", 4.5);
		// the Bootstrap state buttons and labels on their hover and active fill
		for (String state : List.of("neutral", "success", "warning", "danger")) {
			assertContrast("state-" + state + "-fg", "state-" + state + "-bg-hover", 4.5);
		}
		assertContrast("state-neutral-fg", "state-neutral-bg-active", 4.5);
		assertContrast("state-success-fg", "state-success-bg-active", 4.5);
		// the text on the active fill of the bright buttons (16% darker)
		assertContrast("state-warning-fg", "state-warning-bg-active", 4.5);
		assertContrast("btn-dirty-fg", "btn-dirty-bg-active", 4.5);
		assertContrast("coursesite-book-fg", "coursesite-book-bg-active", 4.5);
		// the former contrast defects: the selected tag, the QTI error
		// box, the chat and the tab close icons on the navbar (3:1, non-text)
		for (String state : List.of("", "-hover", "-active")) {
			assertContrast("tag-selected-fg", "tag-selected-bg" + state, 4.5);
		}
		assertContrast("qti-error-fg", "qti-error-bg", 4.5);
		for (String icon : List.of("im-newmessage", "im-newmessage-hover", "navbar-tab-close", "navbar-tab-close-hover")) {
			assertContrast(icon, "navbar-bg", 3.0);
		}
		// the other buttons
		assertContrast("btn-ghost-fg", "bg", 4.5);
		assertContrast("btn-ghost-hover-fg", "bg", 4.5);
		for (String button : List.of("btn-primary-light", "btn-danger-light")) {
			assertContrast(button + "-fg", button + "-bg", 4.5);
			assertContrast(button + "-hover-fg", button + "-hover-bg", 4.5);
		}
		assertContrast("table-filter-fg", "table-filter-bg", 4.5);
		assertContrast("table-filter-active-fg", "table-filter-active-bg", 4.5);
		assertContrast("scope-toggle-fg", "scope-toggle-bg", 4.5);
		assertContrast("scope-toggle-fg", "scope-toggle-hover-bg", 4.5);
		assertContrast("scope-toggle-on-fg", "scope-toggle-on-bg", 4.5);
		assertContrast("toggle-on-text", "toggle-on-bg", 4.5);
		// the hover, pressed and off states: the text of the state on its fill
		for (String button : CONTRAST_SHADE_BUTTONS) {
			for (String state : List.of("-hover", "-active")) {
				assertContrast(button + "-fg" + state, button + "-bg" + state, 4.5);
			}
		}
		assertContrast("scope-toggle-on-hover-fg", "scope-toggle-on-hover-bg", 4.5);
		assertContrast("toggle-text", "toggle-bg", 4.5);
		for (String rating : SELECTUS_RATINGS) {
			for (String state : List.of("", "-hover", "-active")) {
				assertContrast("selectus-rating-" + rating + "-fg", "selectus-rating-" + rating + "-bg" + state, 4.5);
			}
		}
		// the text of a selected competence on its fill, under the pointer and pressed
		for (String state : List.of("", "-hover", "-active")) {
			assertContrast("competence-selected", "competence-bg" + state, 4.5);
		}
		// the close icon of a tab in the More dropdown
		assertContrast("dropdown-tab-close", "bg-raised", 3.0);
		assertContrast("dropdown-tab-close-hover", "bg-raised", 3.0);
		assertContrast("toggle-handle", "toggle-bg", 3.0);
		assertContrast("toggle-on-handle", "toggle-on-bg", 3.0);
		assertContrast("appointment-select-icon", "appointment-planned", 4.5);
		// coloured texts and icons of the message boxes, the evaluation form and QTI
		for (String token : List.of("box-info-text", "box-success-text", "box-warning-text", "box-error-text",
				"evaluation-insufficient-text", "evaluation-neutral-text", "evaluation-sufficient-text",
				"qti-bad-response-warning")) {
			assertContrast(token, "white", 4.5);
		}
	}

	/**
	 * The link rules of the compiled theme read the link tokens: the
	 * Bootstrap rules of a, .btn-link and the pagination hover read --o-link
	 * and --o-link-hover (_bootstrap.scss hands the tokens to the import),
	 * the linked segments read --o-link, the filter tab row above a table
	 * reads --o-segments-tab-link (text, underline of the selected tab, line
	 * under the row), and a rule reads each link token of toolbar, tree and
	 * selectus. A rule that reads the Sass variable again compiles to a
	 * colour and fails here.
	 */
	@Test
	public void linkRulesReadTokens() {
		String css = CUSTOM_PROPERTY.matcher(read(PaletteTest.LIGHT_THEME_CSS)).replaceAll("");
		assertRuleReads(css, "a", "color:var(--o-link)");
		assertRuleReads(css, "a:hover", "color:var(--o-link-hover)");
		assertRuleReads(css, "a:focus", "color:var(--o-link-hover)");
		assertRuleReads(css, ".btn-link", "color:var(--o-link)");
		assertRuleReads(css, ".btn-link:hover", "color:var(--o-link-hover)");
		assertRuleReads(css, ".pagination>li>a:hover", "color:var(--o-link-hover)");
		assertRuleReads(css, ".o_segments.o_segments_linked .btn-default", "color:var(--o-link)");
		assertRuleReads(css, ".o_segments.o_segments_tab", "border-bottom:1px solid var(--o-segments-tab-link)");
		assertRuleReads(css, ".o_segments.o_segments_tab .btn-default", "color:var(--o-segments-tab-link)");
		assertRuleReads(css, ".o_segments.o_segments_tab .btn.btn-primary", "border-color:var(--o-segments-tab-link)");
		for (String token : LINK_RULE_TOKENS) {
			Assert.assertTrue("--o-" + token + " has no definition", tokens.containsKey(token));
			Assert.assertTrue("No rule reads var(--o-" + token + ")", css.contains("var(--o-" + token + ")"));
		}
	}

	/**
	 * The new semantic tokens with the Bootstrap values of light: bg-hover
	 * ($table-bg-hover, theme1-200), bg-selected ($table-bg-active,
	 * theme1-100), bg-raised
	 * ($dropdown-bg), border-subtle ($hr-border), text-disabled
	 * ($btn-link-disabled-color), backdrop ($modal-backdrop-bg). shadow-color
	 * and scrim have no Sass variable: black and white, the colours of the
	 * Bootstrap shadows and of the light veils of the modules.
	 */
	@Test
	public void newSemanticTokenDefaults() {
		// the fills of a selection: steps 200 and 100 of the scale of the
		// focus colour (theme1-500 in light); the Bootstrap greys read the
		// neutral scale: #EEEEEE neutral-100, #777777 neutral-500
		Assert.assertEquals("#C8DFFF", resolve("bg-hover"));
		Assert.assertEquals("#E1EDFF", resolve("bg-selected"));
		Assert.assertEquals("#FFFFFF", resolve("bg-raised"));
		Assert.assertEquals("#EAECF0", resolve("border-subtle"));
		Assert.assertEquals("#657286", resolve("text-disabled"));
		Assert.assertEquals("#000000", resolve("shadow-color"));
		Assert.assertEquals("#000000", resolve("backdrop"));
		Assert.assertEquals("#FFFFFF", resolve("scrim"));
	}

	/**
	 * The rules that read the new semantic tokens: the same Sass variable as
	 * the base colour of the token and the same role (hover background,
	 * surface of a dropdown item, separator line).
	 */
	@Test
	public void surfaceRulesReadTokens() {
		String css = CUSTOM_PROPERTY.matcher(read(PaletteTest.LIGHT_THEME_CSS)).replaceAll("");
		assertRuleReads(css, ".o_highlight_on_hover:hover", "background-color:var(--o-bg-hover)");
		assertRuleReads(css, ".o_table_wrapper .table-hover>tbody>tr:not(.o_table_row_expanded):hover .o_col_sticky_left",
				"background-color:var(--o-table-hover-bg)");
		assertRuleReads(css, "ul.o_dropdown>li>a", "background:var(--o-bg-raised)");
		assertRuleReads(css, ".o_assistance_accordion .o_item", "border-top:1px solid var(--o-border-subtle)");
		assertRuleReads(css, ".o_assistance_accordion .o_item:last-child", "border-bottom:1px solid var(--o-border-subtle)");
	}

	/**
	 * The tokens of the Bootstrap colour variables and of the Bootstrap greys
	 * that the modules read: each token has the exact light
	 * value of its Sass variable, and at least one rule of the compiled theme
	 * reads it. A token with a reference default reads the semantic token
	 * whose base colour has the value of the Sass variable in every theme.
	 */
	@Test
	public void bootstrapComponentTokens() {
		String rules = CUSTOM_PROPERTY.matcher(read(PaletteTest.LIGHT_THEME_CSS)).replaceAll("");
		for (Map.Entry<String, String> token : BOOTSTRAP_COMPONENT_TOKENS.entrySet()) {
			Assert.assertEquals("--o-" + token.getKey(), token.getValue(), resolve(token.getKey()));
			Assert.assertTrue("No rule reads var(--o-" + token.getKey() + ")", rules.contains("var(--o-" + token.getKey() + ")"));
		}
		Assert.assertEquals("var(--o-bg)", tokens.get("input-bg"));
		Assert.assertEquals("var(--o-bg-subtle)", tokens.get("input-disabled-bg"));
		Assert.assertEquals("var(--o-bg-raised)", tokens.get("popover-bg"));
		Assert.assertEquals("var(--o-bg-hover)", tokens.get("table-hover-bg"));
		assertRuleReads(rules, ".form-control:focus", "border-color:var(--o-focus)");
	}

	/**
	 * The shadows, the dark modal layer and the light veils read the opaque
	 * tokens shadow-color, backdrop and scrim. The rule keeps the alpha of
	 * the old rgba() colour.
	 */
	@Test
	public void shadowAndVeilRulesReadTokens() {
		String css = CUSTOM_PROPERTY.matcher(read(PaletteTest.LIGHT_THEME_CSS)).replaceAll("");
		assertRuleReads(css, ".o_vertical_timeline .o_vertical_timeline_rows .o_vertical_timeline_row",
				"box-shadow:0 1px 5px color-mix(in srgb, var(--o-shadow-color) 6%, transparent)");
		assertRuleReads(css, "div.o_callout_overlay", "background:var(--o-backdrop)");
		assertRuleReads(css, "dialog::backdrop", "background:color-mix(in srgb, var(--o-backdrop) 50%, transparent)");
		assertRuleReads(css, ".o_singlepage .o_edit_wrapper", "background:color-mix(in srgb, var(--o-scrim) 80%, transparent)");
	}

	/**
	 * The neutral scale --o-neutral-{step}: 18 steps with the default values
	 * of the table in _palette_neutral.scss, in the themes light and openolat
	 * (both keep the default hue and chroma). The anchors are the light
	 * values of the semantic tokens bg-subtle (25), border (150),
	 * border-strong (250) and text (750).
	 */
	@Test
	public void neutralScale() {
		Path themes = PaletteTest.LIGHT_THEME_CSS.getParent().getParent();
		for (String file : TOKEN_FILES) {
			Map<String, String> defined = parseTokens(read(themes.resolve(file)));
			for (Map.Entry<Integer, String> step : NEUTRAL.entrySet()) {
				Assert.assertEquals(file + ": --o-neutral-" + step.getKey(), step.getValue(), defined.get("neutral-" + step.getKey()));
			}
		}
		Assert.assertEquals(resolve("bg-subtle"), tokens.get("neutral-25"));
		Assert.assertEquals(resolve("border"), tokens.get("neutral-150"));
		Assert.assertEquals(resolve("border-strong"), tokens.get("neutral-250"));
		Assert.assertEquals(resolve("text"), tokens.get("neutral-750"));
		// step 450 is the lightest grey with 3:1 on white, step 500 has 4.5:1
		Assert.assertTrue(ColorContrast.contrast(tokens.get("neutral-450"), "#FFFFFF") >= 3.0);
		Assert.assertTrue(ColorContrast.contrast(tokens.get("neutral-500"), "#FFFFFF") >= 4.5);
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
	 * background is 10% and 17% darker, the border 12% and 25%. The light
	 * buttons and the table filters darken the background by the largest
	 * amount up to 10% and 17% at which the text keeps 4.5:1.
	 */
	@Test
	public void buttonShadeTokens() {
		for (String button : SHADE_BUTTONS) {
			int hover = 10;
			int active = ACTIVE_PERCENT.getOrDefault(button, 17);
			if (CONTRAST_SHADE_BUTTONS.contains(button)) {
				hover = contrastShadeAmount(resolve(button + "-fg"), resolve(button + "-bg"), hover);
				active = contrastShadeAmount(resolve(button + "-fg"), resolve(button + "-bg"), active);
			}
			if (LIGHTER_TEXT_BUTTONS.contains(button)) {
				hover = -hover;
				active = -active;
			}
			assertShade(button + "-bg-hover", button + "-bg", hover);
			assertShade(button + "-bg-active", button + "-bg", active);
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
		for (String button : List.of("btn-primary", "btn-default", "btn-danger")) {
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
		// The pressed primary button shows the inset shadow of .btn:active: the
		// rule comes after the hover shadow, which has the same specificity
		Matcher hoverShadow = Pattern.compile("[},]\\.btn-primary:hover\\{box-shadow:").matcher(css);
		Matcher pressedShadow = Pattern.compile("[},]\\.btn-primary:active,\\.btn-primary\\.active\\{box-shadow:inset 0 3px 5px "
				+ "color-mix\\(in srgb, ?var\\(--o-shadow-color\\) 12\\.5%, ?transparent\\)\\}").matcher(css);
		Assert.assertTrue("No rule .btn-primary:active with the inset shadow of .btn:active after the hover shadow of .btn-primary",
				hoverShadow.find() && pressedShadow.find(hoverShadow.end()));
	}

	/**
	 * The shade tokens of the colour maths of the modules (hover and active
	 * state, edge, tint, text on a fill): a hex colour computed at compile
	 * time, the same colour as darken() or lighten() of its token with the
	 * percentage of the module. A shade can come from a semantic token
	 * (focus-edge) and lighten its token (immunity proof).
	 */
	@Test
	public void moduleShadeTokens() {
		for (Shade shade : MODULE_SHADES) {
			assertShade(shade.name(), shade.source(), shade.percent());
		}
	}

	/**
	 * The rules of the module colour maths read the tokens: a rule reads each
	 * shade token of MODULE_SHADES, the translucent colours mix the token
	 * with transparent and keep the alpha, and the muted texts that lighten()
	 * made read text-muted or text-disabled. A rule that
	 * computes the colour with Sass again compiles to a colour and fails here.
	 */
	@Test
	public void colorMathRulesReadTokens() {
		String css = CUSTOM_PROPERTY.matcher(read(PaletteTest.LIGHT_THEME_CSS)).replaceAll("");
		for (Shade shade : MODULE_SHADES) {
			Assert.assertTrue("No rule reads var(--o-" + shade.name() + ")", css.contains("var(--o-" + shade.name() + ")"));
		}
		assertRuleReads(css, ".o_tag.o_tag_clickable:hover", "background-color:var(--o-tag-bg-hover)");
		assertRuleReads(css, ".o_tag.o_selection_tag.o_tag_selected.o_tag_clickable:active",
				"border-color:var(--o-tag-selection-selected-border-active)");
		assertRuleReads(css, ".o_immunity_proof_status.validated", "-webkit-text-stroke:2px var(--o-immunity-proof-validated-bg-edge)");
		assertRuleReads(css, ".o_immunity_proof_status.validated", "background-color:var(--o-immunity-proof-validated-bg)");
		assertRuleReads(css, ".o_dashboard_edit .o_dashboard_edit_item.gu-transit", "border:1px dashed var(--o-focus-edge)");
		// translucent colours
		assertRuleReads(css, ".o_dashboard_edit .o_dashboard_edit_item.sortable-drag",
				"box-shadow:0 4px 12px color-mix(in srgb, var(--o-focus) 15%, transparent)");
		assertRuleReads(css, ".o_dashboard_edit .o_dashboard_edit_item.gu-transit",
				"background-color:color-mix(in srgb, var(--o-focus) 50%, transparent)");
		assertRuleReads(css, ".o_page_fragment_edit.o_fragment_edited>.o_fragment_selection_frame",
				"border:2px solid color-mix(in srgb, var(--o-editor-selected-border) 80%, transparent)");
		assertRuleReads(css, ".o_gr_grade_scores .o_gr_grade_score_table .o_gr_passed",
				"background-color:color-mix(in srgb, var(--o-course-state-passed) 50%, transparent)");
		assertRuleReads(css, ".btn-primary", "box-shadow:0 1px 2px color-mix(in srgb, var(--o-btn-primary-bg) 30%, transparent)");
		// muted texts
		assertRuleReads(css, ".o_document_maxsize", "color:var(--o-text-muted)");
		assertRuleReads(css, ".tox .tox-form .help-block", "color:var(--o-text-muted)");
		assertRuleReads(css, ".o_curriculum_el_listing tr.o_curriculum_element_unfiltered", "color:var(--o-text-muted)");
		assertRuleReads(css, ".o_catalog2 .o_catalog_temporarily_disabled .o_empty_state:is(.o_header_bg_image *) .o_empty_visual i:nth-child(2)",
				"color:var(--o-text-muted)");
		assertRuleReads(css, ".o_course_edit_overview_table tr.o_deleted", "color:var(--o-course-node-deleted)");
		Assert.assertEquals("var(--o-text-disabled)", tokens.get("course-node-deleted"));
	}

	/**
	 * The remaining colour maths of the modules and of the defaults in
	 * _config.scss read tokens: the gradient stops and the
	 * labels read shade tokens, the named variables with an opaque default
	 * read their own token, the translucent defaults mix a token with
	 * transparent, and the two muted texts read text-muted and text-disabled.
	 */
	@Test
	public void remainingColorMathRulesReadTokens() {
		String css = CUSTOM_PROPERTY.matcher(read(PaletteTest.LIGHT_THEME_CSS)).replaceAll("");
		// muted texts
		assertRuleReads(css, ".o_form_chelp", "color:var(--o-chelp-tooltip)");
		Assert.assertEquals("var(--o-text-muted)", tokens.get("chelp-tooltip"));
		assertRuleReads(css, ".o_toolbar .o_tools_container a.o_disabled", "color:var(--o-toolbar-link-disabled) !important");
		Assert.assertEquals("var(--o-text-disabled)", tokens.get("toolbar-link-disabled"));
		Assert.assertEquals("var(--o-toolbar-link-disabled)", tokens.get("toolbar-nav-link-disabled"));
		// shade tokens and fixed colours
		assertRuleReads(css, ".o_labeled", "background-color:var(--o-labeled-bg)");
		Assert.assertEquals("var(--o-gray-600)", tokens.get("labeled-bg"));
		Assert.assertEquals("var(--o-white)", tokens.get("labeled-light-bg"));
		// the header of a coloured table is the light fill of the hue, step 100 for yellow
		assertRuleReads(css, "table.b_yellow th", "background:var(--o-yellow-100)");
		// a label keeps the border colour of its token: no border shorthand after it (it would
		// reset the colour to currentColor), a colour category label has the swatch border
		assertRuleReads(css, ".o_labeled", "border-color:var(--o-labeled-border)");
		assertRuleReads(css, ".o_labeled", "border-style:solid");
		assertRuleReads(css, ".o_colcat_bg.o_swatch_red", "border-color:var(--o-sw-border)");
		assertRuleReads(css, ".o_colcat_bg.o_swatch_theme1", "border-color:var(--o-sw-border)");
		assertRuleReads(css, ".o_tag.o_competence.o_tag_clickable:active", "background-color:var(--o-competence-bg-active)");
		Assert.assertEquals("var(--o-white)", tokens.get("competence-bg"));
		assertRuleReads(css, ".o_page_run_code_box", "background-color:var(--o-page-code-box-bg)");
		assertRuleReads(css, ".o_catalog2 .o_catalog_header .o_catalog_header_item:not(.o_header_bg_image)",
				"background:linear-gradient(190deg, var(--o-catalog-header-noimage-stop-1) 10%, var(--o-primary) 100%),"
				+ "linear-gradient(40deg, var(--o-catalog-header-noimage-stop-3) 0%, var(--o-catalog-header-noimage-stop-4) 60%)");
		// the turn of the hue of the primary colour: a step of the brand ramp in light
		Assert.assertTrue("catalog-header-noimage-stop-4: " + tokens.get("catalog-header-noimage-stop-4"),
				HEX.matcher(tokens.get("catalog-header-noimage-stop-4")).matches()
				|| SCALE_REFERENCE.matcher(tokens.get("catalog-header-noimage-stop-4")).matches());
		// translucent colours
		assertRuleReads(css, ".milkdown table th", "background:color-mix(in srgb, var(--o-milkdown-table-header-bg) 10%, transparent)");
		// the translucent defaults of $o-cn-style-cover-text-bg and
		// $o-lp-pagination-in-progress-bgcolor feed element tokens
		assertRuleReads(css, ".o_cn_styled.o_cover .o_top .o_text_block>div:nth-child(1)",
				"background-color:var(--o-course-style-cover-text-bg)");
		Assert.assertEquals("color-mix(in srgb, var(--o-scrim) 95%, transparent)", resolve("course-style-cover-text-bg"));
		assertRuleReads(css, ".o_course_pagination_confirmation[aria-checked=false]",
				"background-color:var(--o-lp-pagination-in-progress-bg)");
		Assert.assertEquals("color-mix(in srgb, var(--o-lp-in-progress) 20%, transparent)", resolve("lp-pagination-in-progress-bg"));
		assertRuleReads(css, ".o_page_fragment_edit>.o_page_part .o_page_edit_toolbar a.o_disabled",
				"color:color-mix(in srgb, var(--o-editor-part-toolbar-link) 50%, transparent) !important");
		// named variables with an own token
		for (String token : List.of("radio-card-checked-bg", "radio-card-hover-border", "qti-match-selected-bg",
				"portfolio-lead-border", "selectus-decision-a-hover-bg", "chart-bar-dark", "box-plot-quartile-fill",
				"offcanvas-logout-hover", "im-chat-roster-bg", "highscore-bar-other-hover", "brand-box-border")) {
			Assert.assertTrue("No rule reads var(--o-" + token + ")", css.contains("var(--o-" + token + ")"));
		}
	}

	/**
	 * A client config that sets an old state colour ($brand-danger,
	 * $state-danger-text, ...) gets a compile warning; the switch
	 * $o-legacy-color-warning silences it. Static check of
	 * the sources: the compile of the warning itself is proven by compiling
	 * client themes.
	 */
	@Test
	public void legacyColorWarning() {
		String config = read(ThemeColorRatchetTest.LIGHT_MODULES.getParent().resolve("_config.scss"));
		Assert.assertTrue(config.contains("$o-legacy-color-warning: true !default;"));
		for (String name : List.of("brand-danger", "brand-warning", "brand-success", "brand-info")) {
			Assert.assertTrue(name, config.contains("meta.global-variable-exists(\"" + name + "\")"));
		}
		for (String state : List.of("danger", "warning", "success", "info")) {
			for (String role : List.of("text", "bg", "border")) {
				String name = "state-" + state + "-" + role;
				Assert.assertTrue(name, config.contains("meta.global-variable-exists(\"" + name + "\")"));
				Assert.assertTrue(name, config.contains("\"" + name + "\": #"));
			}
		}
		String tokensScss = read(ThemeColorRatchetTest.LIGHT_MODULES.resolve("_tokens.scss"));
		int warn = tokensScss.indexOf("@warn \"OpenOLAT theme: your config sets");
		Assert.assertTrue("No @warn of the old state colours in _tokens.scss", warn >= 0);
		String message = tokensScss.substring(warn, tokensScss.indexOf('\n', warn));
		Assert.assertTrue(message, message.contains("$o-palette, $o-states or $o-tokens"));
		Assert.assertTrue(message, message.contains("themes.README"));
		Assert.assertTrue(message, message.contains("$o-legacy-color-warning: false;"));
		Assert.assertFalse(message, message.contains("exception"));
		// email.css does not import _tokens.scss: no warning there
		Assert.assertFalse(read(ThemeColorRatchetTest.LIGHT_MODULES.getParent().resolve("email.scss")).contains("tokens"));
	}

	/**
	 * The module rules read the state tokens, not the Bootstrap state colours
	 * (OO-10043, brand colours to states, decisions S1 a and S2 a): no module
	 * reads $brand-success|info|warning|danger, $state-*-text|bg|border,
	 * $alert-*-text|bg|border or $btn-success|info|warning|danger-*, except
	 * BOOTSTRAP_STATE_KEPT. _bootstrap.scss hands the Sass values to the
	 * Bootstrap import and is not checked. Icons and texts read the text role,
	 * fills the soft-bg role.
	 */
	@Test
	public void stateRulesReadTokens() throws IOException {
		try (Stream<Path> modules = Files.list(ThemeColorRatchetTest.LIGHT_MODULES)) {
			for (Path module : modules.filter(m -> m.getFileName().toString().endsWith(".scss")).toList()) {
				String name = module.getFileName().toString();
				if ("_bootstrap.scss".equals(name)) {
					continue;
				}
				Matcher m = BOOTSTRAP_STATE_VARIABLE.matcher(ThemeColorRatchetTest.withoutComments(read(module)));
				while (m.find()) {
					String reference = name + ": " + m.group();
					Assert.assertTrue(reference + ": read a state token var(--o-state-{state}-{role})",
							BOOTSTRAP_STATE_KEPT.contains(reference));
				}
			}
		}
		String css = CUSTOM_PROPERTY.matcher(read(PaletteTest.LIGHT_THEME_CSS)).replaceAll("");
		assertRuleReads(css, ".o_icon_error", "color:var(--o-state-danger-text)");
		assertRuleReads(css, ".o_icon_warn", "color:var(--o-state-warning-text)");
		assertRuleReads(css, ".o_icon_important", "color:var(--o-state-warning-text)");
		assertRuleReads(css, ".o_icon_info", "color:var(--o-state-neutral-text)");
		assertRuleReads(css, "#o_msg_sticky", "background-color:var(--o-state-danger-soft-bg)");
		assertRuleReads(css, "#o_msg_sticky", "border:1px solid var(--o-state-danger-soft-border)");
		assertRuleReads(css, ".checkbox label.o_checkbox_error", "color:var(--o-state-danger-text)");
		assertRuleReads(css, ".tox-textbox.danger", "border-color:var(--o-tinymce-danger-bg-edge)");
		assertRuleReads(css, ".o_toolbar .o_breadcrumb .breadcrumb .o_breadcrumb_close a:hover", "color:var(--o-toolbar-close-hover)");
		assertRuleReads(css, ".btn-danger .o_icon_reject:before", "color:var(--o-btn-danger-fg)");
		Assert.assertEquals("var(--o-state-danger-soft-bg)", tokens.get("tinymce-danger-bg"));
	}

	/**
	 * The danger button reads the danger state (decisions S7 a and S8 a): fill,
	 * text and the border one step darker. The dirty button and the course
	 * site book button follow the warning state with the dark text of the fg
	 * role (S5 a, S3 a). The tokens of the selected tags follow the button
	 * tokens. The selected table row and the right cells of the Kprim and
	 * true/false tables have the soft fill of their state, not white (S4 a).
	 */
	/**
	 * The Bootstrap state rules read the state tokens:
	 * .btn-success|info|warning the fill, the text on
	 * the fill (fg) and the border, labels and progress bars the fill and the
	 * fg text, alerts the soft roles with the soft-fg text, panels, list
	 * groups, form validation states, contextual table rows, .text-*, .bg-*
	 * and mark the text and soft roles. info is the state neutral. A rule that
	 * reads the Sass variable again compiles to a colour and fails here.
	 */
	@Test
	public void bootstrapStateRulesReadTokens() {
		String css = CUSTOM_PROPERTY.matcher(read(PaletteTest.LIGHT_THEME_CSS)).replaceAll("");
		Map<String, String> states = Map.of("success", "success", "info", "neutral", "warning", "warning", "danger", "danger");
		for (Map.Entry<String, String> entry : states.entrySet()) {
			String bootstrap = entry.getKey();
			String token = "var(--o-state-" + entry.getValue() + "-";
			if (!"danger".equals(bootstrap)) {
				assertRuleReads(css, ".btn-" + bootstrap, "color:" + token + "fg)");
				assertRuleReads(css, ".btn-" + bootstrap, "background-color:" + token + "bg)");
				assertRuleReads(css, ".btn-" + bootstrap, "border-color:" + token + "border)");
				assertRuleReads(css, ".btn-" + bootstrap + ":hover", "background-color:" + token + "bg-hover)");
				assertRuleReads(css, ".btn-" + bootstrap + ":active:hover", "background-color:" + token + "bg-active)");
				assertRuleReads(css, ".btn-" + bootstrap + ":hover", "border-color:" + token + "border-hover)");
				assertRuleReads(css, ".btn-" + bootstrap + ":focus", "border-color:" + token + "border-focus)");
			}
			assertRuleReads(css, ".label-" + bootstrap, "background-color:" + token + "bg)");
			assertRuleReads(css, ".label-" + bootstrap, "color:" + token + "fg)");
			assertRuleReads(css, ".label-" + bootstrap + "[href]:hover", "background-color:" + token + "bg-hover)");
			assertRuleReads(css, ".progress-bar-" + bootstrap, "background-color:" + token + "bg)");
			assertRuleReads(css, ".progress-bar-" + bootstrap, "color:" + token + "fg)");
			assertRuleReads(css, ".alert-" + bootstrap, "color:" + token + "soft-fg)");
			assertRuleReads(css, ".alert-" + bootstrap, "background-color:" + token + "soft-bg)");
			assertRuleReads(css, ".alert-" + bootstrap, "border-color:" + token + "soft-border)");
			assertRuleReads(css, ".panel-" + bootstrap + ">.panel-heading", "color:" + token + "text)");
			assertRuleReads(css, ".panel-" + bootstrap + ">.panel-heading", "background-color:" + token + "soft-bg)");
			assertRuleReads(css, ".list-group-item-" + bootstrap, "background-color:" + token + "soft-bg)");
			assertRuleReads(css, "a.list-group-item-" + bootstrap + ":hover", "background-color:" + token + "soft-bg-hover)");
			assertRuleReads(css, ".table>tbody>tr." + bootstrap + ">td", "background-color:" + token + "soft-bg)");
			assertRuleReads(css, ".table-hover>tbody>tr." + bootstrap + ":hover>td", "background-color:" + token + "soft-bg-hover)");
			assertRuleReads(css, ".text-" + bootstrap, "color:" + token + "text)");
			assertRuleReads(css, "a.text-" + bootstrap + ":hover", "color:" + token + "text-hover)");
			assertRuleReads(css, ".bg-" + bootstrap, "background-color:" + token + "soft-bg)");
			assertRuleReads(css, "a.bg-" + bootstrap + ":hover", "background-color:" + token + "soft-bg-hover)");
		}
		for (String state : List.of("success", "warning", "danger")) {
			String bootstrap = "danger".equals(state) ? "error" : state;
			assertRuleReads(css, ".has-" + bootstrap + " .help-block", "color:var(--o-state-" + state + "-text)");
			assertRuleReads(css, ".has-" + bootstrap + " .form-control", "border-color:var(--o-state-" + state + "-text)");
			assertRuleReads(css, ".has-" + bootstrap + " .form-control:focus", "border-color:var(--o-state-" + state + "-text-hover)");
			assertRuleReads(css, ".has-" + bootstrap + " .input-group-addon", "background-color:var(--o-state-" + state + "-soft-bg)");
		}
		assertRuleReads(css, "mark", "background-color:var(--o-state-warning-soft-bg)");
		assertRuleReads(css, ".o_table_wrapper tr.success .o_col_sticky_left", "background-color:var(--o-state-success-soft-bg) !important");
		// the same rules in content.css (.text-*, .bg-*, mark)
		String content = CUSTOM_PROPERTY.matcher(read(PaletteTest.LIGHT_THEME_CSS.resolveSibling("content.css"))).replaceAll("");
		assertRuleReads(content, ".text-danger", "color:var(--o-state-danger-text)");
		assertRuleReads(content, ".bg-info", "background-color:var(--o-state-neutral-soft-bg)");
		assertRuleReads(content, "mark", "background-color:var(--o-state-warning-soft-bg)");
	}

	/**
	 * The shade tokens of the state tokens (STATE_SHADES): a hex colour
	 * computed at compile time, the same colour as darken() of the state
	 * token, and a rule of the compiled theme reads each of them.
	 */
	@Test
	public void stateShadeTokens() {
		String css = CUSTOM_PROPERTY.matcher(read(PaletteTest.LIGHT_THEME_CSS)).replaceAll("");
		for (Shade shade : STATE_SHADES) {
			assertShade(shade.name(), shade.source(), shade.percent());
			Assert.assertTrue("No rule reads var(--o-" + shade.name() + ")", css.contains("var(--o-" + shade.name() + ")"));
		}
	}

	/**
	 * The message boxes, the evaluation form, the portlet edit mode and the
	 * QTI bad response warning read element tokens that follow the state
	 * tokens: a message box of a state other than info has the soft fill, the
	 * soft border and the soft text of its state as icon, a coloured text the
	 * text of the state; tip and important have the fill cyan 50. The info box
	 * is checked in infoBoxHasNearWhiteFillAndBlueBorder(). The evaluation form
	 * chart colour is the fill of the state, its icons and texts the text of
	 * the state, the box plot box the light fill (palette step 200), the
	 * portlet edit mode the soft roles of the warning state.
	 */
	@Test
	public void messageBoxAndEvaluationTokens() {
		Map<String, String> boxes = Map.of("note", "inactive", "success", "success", "warning", "warning", "error", "danger");
		for (Map.Entry<String, String> box : boxes.entrySet()) {
			String state = "var(--o-state-" + box.getValue();
			Assert.assertEquals(state + "-soft-bg)", tokens.get("box-" + box.getKey() + "-bg"));
			Assert.assertEquals(state + "-soft-border)", tokens.get("box-" + box.getKey() + "-border"));
			Assert.assertEquals(state + "-soft-fg)", tokens.get("box-" + box.getKey() + "-icon"));
			if (!"note".equals(box.getKey())) {
				Assert.assertEquals(state + "-text)", tokens.get("box-" + box.getKey() + "-text"));
				assertContrast("box-" + box.getKey() + "-text", "box-" + box.getKey() + "-bg", 4.5);
			}
			assertContrast("text", "box-" + box.getKey() + "-bg", 4.5);
			assertContrast("box-" + box.getKey() + "-icon", "box-" + box.getKey() + "-bg", 3.0);
		}
		Assert.assertEquals("var(--o-cyan-50)", tokens.get("box-tip-bg"));
		Assert.assertEquals("var(--o-violet-50)", tokens.get("box-important-bg"));
		Map<String, String> ratings = Map.of("insufficient", "danger", "neutral", "warning", "sufficient", "success");
		// the light style of the state: step 200, for yellow step 100
		Map<String, String> fills = Map.of("insufficient", "var(--o-red-200)", "neutral", "var(--o-yellow-100)", "sufficient", "var(--o-green-200)");
		for (Map.Entry<String, String> rating : ratings.entrySet()) {
			String name = "evaluation-" + rating.getKey();
			Assert.assertEquals("var(--o-state-" + rating.getValue() + "-bg)", tokens.get(name));
			Assert.assertEquals("var(--o-state-" + rating.getValue() + "-text)", tokens.get(name + "-text"));
			Assert.assertEquals(fills.get(rating.getKey()), tokens.get(name + "-fill"));
		}
		Assert.assertEquals("var(--o-state-warning-soft-bg)", tokens.get("portlet-edit-bg"));
		Assert.assertEquals("var(--o-state-warning-soft-border)", tokens.get("portlet-edit-border"));
		Assert.assertEquals("var(--o-state-warning-text)", tokens.get("qti-bad-response-warning"));

		String css = CUSTOM_PROPERTY.matcher(read(PaletteTest.LIGHT_THEME_CSS)).replaceAll("");
		assertRuleReads(css, ".o_warning", "border:1px solid var(--o-box-warning-border)");
		assertRuleReads(css, ".o_warning_with_icon:before", "color:var(--o-box-warning-icon)");
		assertRuleReads(css, ".o_error_with_icon:before", "color:var(--o-box-error-icon)");
		assertRuleReads(css, ".o_note_with_icon:before", "color:var(--o-box-note-icon)");
		// tip and important: a soft border, the icon keeps its strong colour
		assertRuleReads(css, ".o_tip_with_icon:before", "color:var(--o-box-tip-icon)");
		assertRuleReads(css, ".o_important_with_icon:before", "color:var(--o-box-important-icon)");
		assertRuleReads(css, ".o-modal-alert-success .modal-header h4:before", "color:var(--o-box-success-text)");
		assertRuleReads(css, ".o_page_content_editor>.o_page_fragment_edit>.o_page_layout.o_alert_box_active.o_alert_box_type_info",
				"border-color:color-mix(in srgb, var(--o-box-info-border) 15%, transparent)");
		assertRuleReads(css, ".o_icon_rubric_neutral", "color:var(--o-evaluation-neutral-text)");
		assertRuleReads(css, ".d3chart .o_rubric_neutral", "fill:var(--o-evaluation-neutral)");
		assertRuleReads(css, "svg.o_bp.o_rubric_insufficient .o_bp_v_b", "fill:var(--o-evaluation-insufficient-fill)");
		assertRuleReads(css, ".o_portlet .o_edit_shim", "background:var(--o-portlet-edit-bg)");
		assertRuleReads(css, ".o_assessmentitem div.o_warning.badResponse", "color:var(--o-qti-bad-response-warning)");
	}

	/**
	 * The info box has a near white fill (neutral 25) and a border in the fill
	 * of the neutral state (blue 500); its icon and coloured text are the text
	 * of the neutral state, the text and the headings the normal text. The
	 * info message at the top of the page (.o_alert_info) reads the same fill
	 * and border tokens with the normal text, so both look the same. The
	 * text, the icon, the border and the close icon of the top message have
	 * their contrast on the fill.
	 */
	@Test
	public void infoBoxHasNearWhiteFillAndBlueBorder() {
		Assert.assertEquals("var(--o-neutral-25)", tokens.get("box-info-bg"));
		Assert.assertEquals("var(--o-state-neutral-bg)", tokens.get("box-info-border"));
		Assert.assertEquals("var(--o-state-neutral-text)", tokens.get("box-info-text"));
		Assert.assertEquals("var(--o-state-neutral-text)", tokens.get("box-info-icon"));
		assertContrast("text", "box-info-bg", 4.5);
		assertContrast("box-info-text", "box-info-bg", 4.5);
		assertContrast("box-info-icon", "box-info-bg", 3.0);
		assertContrast("box-info-border", "box-info-bg", 3.0);
		assertContrast("alert-close", "box-info-bg", 3.0);
		assertContrast("alert-close-hover", "box-info-bg", 3.0);

		String css = CUSTOM_PROPERTY.matcher(read(PaletteTest.LIGHT_THEME_CSS)).replaceAll("");
		assertRuleReads(css, ".o_info", "background-color:var(--o-box-info-bg)");
		assertRuleReads(css, ".o_info", "border:1px solid var(--o-box-info-border)");
		assertRuleReads(css, ".o_info_with_icon", "border:1px solid var(--o-box-info-border)");
		assertRuleReads(css, ".o_info_with_icon:before", "color:var(--o-box-info-icon)");
		assertRuleReads(css, ".o_alert_info .alert", "background-color:var(--o-box-info-bg)");
		assertRuleReads(css, ".o_alert_info .alert", "border-color:var(--o-box-info-border)");
		assertRuleReads(css, ".o_alert_info .alert", "color:var(--o-text)");
	}

	@Test
	public void stateButtonTokens() {
		Assert.assertEquals("var(--o-state-danger-bg)", tokens.get("btn-danger-bg"));
		Assert.assertEquals("var(--o-state-danger-fg)", tokens.get("btn-danger-fg"));
		Assert.assertEquals("var(--o-state-danger-border)", tokens.get("btn-danger-border"));
		Assert.assertEquals("var(--o-state-warning-bg)", tokens.get("btn-dirty-bg"));
		Assert.assertEquals("var(--o-state-warning-fg)", tokens.get("btn-dirty-fg"));
		Assert.assertEquals("var(--o-state-warning-bg)", tokens.get("coursesite-book-bg"));
		Assert.assertEquals("var(--o-state-warning-fg)", tokens.get("coursesite-book-fg"));
		Assert.assertEquals("var(--o-btn-dirty-border)", tokens.get("tag-selected-border"));
		Assert.assertEquals("var(--o-btn-dirty-border)", tokens.get("competence-selected-border"));
		Assert.assertEquals("var(--o-btn-danger-bg)", tokens.get("tag-selection-remove-selected-bg"));
		Assert.assertEquals("var(--o-btn-danger-border)", tokens.get("tag-selection-remove-selected-border"));
		Assert.assertEquals("var(--o-btn-danger-fg)", tokens.get("tag-selection-remove-selected-fg"));
		Assert.assertEquals("var(--o-state-danger-text)", tokens.get("toolbar-close"));
		for (String state : List.of("info", "success", "warning", "danger")) {
			String role = "info".equals(state) ? "neutral" : state;
			Assert.assertEquals("var(--o-state-" + role + "-text)", tokens.get("badge-status-" + state));
		}
		String css = CUSTOM_PROPERTY.matcher(read(PaletteTest.LIGHT_THEME_CSS)).replaceAll("").toLowerCase();
		// the selected row reads its token, the selected fill
		assertRuleReads(css, ".o_row_selected", "background:var(--o-table-row-selected-bg) !important");
		Assert.assertEquals("var(--o-bg-selected)", tokens.get("table-row-selected-bg"));
		// the right kprim cell reads its token, the soft fill of the success state
		assertRuleReads(css, "table.o_qti_item_kprim td.o_qti_item_kprim_input_correct",
				"background-color:var(--o-qti-match-kprim-correct-bg)");
		Assert.assertEquals("var(--o-state-success-soft-bg)", tokens.get("qti-match-kprim-correct-bg"));
	}

	/**
	 * The rating stars read the token
	 * rating-star (palette step yellow-400), the shadows with the old colour
	 * rgba(16, 24, 40, a) read shadow-color with their alpha, the selected
	 * tag and the QTI error box have the text of their fill, the chat icon
	 * and the tab close icon in the navbar read their tokens.
	 */
	@Test
	public void decisionRulesReadTokens() {
		Assert.assertEquals("var(--o-yellow-400)", tokens.get("rating-star"));
		Assert.assertEquals("var(--o-btn-dirty-fg)", tokens.get("tag-selected-fg"));
		Assert.assertEquals("var(--o-state-danger-fg)", tokens.get("qti-error-fg"));
		Assert.assertEquals("var(--o-state-warning-text)", tokens.get("im-newmessage-hover"));
		Assert.assertEquals("var(--o-state-danger-text-hover)", tokens.get("navbar-tab-close-hover"));
		String css = CUSTOM_PROPERTY.matcher(read(PaletteTest.LIGHT_THEME_CSS)).replaceAll("");
		assertRuleReads(css, ".o_rating .o_rating_items .o_icon", "color:var(--o-rating-star)");
		assertRuleReads(css, ".panel", "box-shadow:0 1px 2px color-mix(in srgb, var(--o-shadow-color) 4%, transparent)");
		assertRuleReads(css, ".o_login .o_login_box", "box-shadow:0 8px 32px color-mix(in srgb, var(--o-shadow-color) 12%, transparent),"
				+ "0 2px 8px color-mix(in srgb, var(--o-shadow-color) 6%, transparent)");
		// no rule has the old colour: the background of the peekview of a course
		// element reads its own token
		Assert.assertEquals("Rules with the old shadow colour rgba(16,24,40,a)", 0, css.split("rgba\\(16,24,40", -1).length - 1);
		assertRuleReads(css, ".dropdown-menu", "box-shadow:0 4px 16px color-mix(in srgb, var(--o-shadow-color) 10%, transparent),"
				+ "0 1px 3px color-mix(in srgb, var(--o-shadow-color) 6%, transparent)");
		assertRuleReads(css, ".o_tag.o_tag_selected", "color:var(--o-tag-selected-fg)");
		assertRuleReads(css, ".o_tag.o_tag_selected_remove", "color:var(--o-tag-selected-fg)");
		assertRuleReads(css, ".o_assessmentitem div.error", "color:var(--o-qti-error-fg)");
		assertRuleReads(css, ".o_assessmentitem div.error", "background-color:var(--o-qti-error-bg)");
		assertRuleReads(css, "#o_navbar_imclient #o_im_message .o_icon_message:hover", "color:var(--o-im-newmessage-hover)");
		assertRuleReads(css, ".o_navbar .o_navbar_tabs .o_navbar_tab_close:hover i:before", "color:var(--o-navbar-tab-close-hover)");
	}

	/**
	 * The text of a selected competence
	 * follows the text of the selected tag; the hover, pressed and off states
	 * read tokens with the contrast of the normal state; the close icon of a
	 * tab in the More dropdown reads its own tokens, the icon on the navbar is
	 * adjusted to 3:1 by the compile; the dark tints read backdrop, the
	 * immunity proof shadows shadow-color, the navbar highlight, the peekview
	 * background and the not available visual their own tokens, each with the
	 * alpha of the old colour. The dead block of _config.scss is gone.
	 */
	@Test
	public void decisionD8ToD11RulesReadTokens() {
		String css = CUSTOM_PROPERTY.matcher(read(PaletteTest.LIGHT_THEME_CSS)).replaceAll("");
		Assert.assertEquals("var(--o-tag-selected-fg)", tokens.get("competence-selected"));
		assertRuleReads(css, ".o_tag.o_competence.o_tag_selected", "color:var(--o-competence-selected)");
		// the toggle text is the muted text adjusted for the track, the rating C fill is the red of the
		// palette (#EB0000 before the scale rework)
		// the muted text read on the track: the nearest neutral step with 4.5:1
		Assert.assertEquals("var(--o-neutral-600)", tokens.get("toggle-text"));
		assertRuleReads(css, ".o_button_toggle span", "color:var(--o-toggle-text)");
		Assert.assertEquals("var(--o-white)", tokens.get("selectus-rating-c-fg"));
		Assert.assertEquals("var(--o-red-500)", tokens.get("selectus-rating-c-bg"));
		Assert.assertEquals("var(--o-state-danger-text)", tokens.get("navbar-tab-close"));
		Assert.assertEquals("var(--o-state-danger-text)", tokens.get("dropdown-tab-close"));
		Assert.assertEquals("var(--o-state-danger-text-hover)", tokens.get("dropdown-tab-close-hover"));
		assertRuleReads(css, ".o_navbar .o_navbar_tabs .o_navbar_tab_close i:before", "color:var(--o-navbar-tab-close)");
		assertRuleReads(css, ".o_dropdown_tab .o_navbar_tab_close", "color:var(--o-dropdown-tab-close)");
		assertRuleReads(css, ".o_dropdown_tab .o_navbar_tab_close:hover", "color:var(--o-dropdown-tab-close-hover)");
		// dark tints
		assertRuleReads(css, ".o_skeleton", "border-top:1px solid color-mix(in srgb, var(--o-backdrop) 6%, transparent)");
		assertRuleReads(css, ".o_skeleton_bar", "background-color:color-mix(in srgb, var(--o-backdrop) 6%, transparent)");
		assertRuleReads(css, ".o_ai_paragraph_feedback", "background:color-mix(in srgb, var(--o-backdrop) 4%, transparent)");
		assertRuleReads(css, ".o_quiz .o_quiz_question.o_essay_ai_correction_host .o_essay_ai_correction_overlay",
				"background-color:color-mix(in srgb, var(--o-backdrop) 60%, transparent)");
		assertRuleReads(css, ".twentytwenty-wrapper .twentytwenty-container .twentytwenty-handle",
				"background-color:color-mix(in srgb, var(--o-backdrop) 33%, transparent)");
		assertRuleReads(css, ".dropdown-menu", "border:1px solid color-mix(in srgb, var(--o-backdrop) 3%, transparent)");
		// the translucent default of $o-lp-bg feeds the token lp-bg
		assertRuleReads(css, ".o_lp_tree.o_tree li.children_visible", "background:var(--o-lp-bg)");
		Assert.assertEquals("color-mix(in srgb, var(--o-backdrop) 2.5%, transparent)", resolve("lp-bg"));
		assertRuleReads(css, ".o_tag_admin", "background:repeating-linear-gradient(135deg, transparent, transparent 3px, "
				+ "color-mix(in srgb, var(--o-backdrop) 10%, transparent) 3px, color-mix(in srgb, var(--o-backdrop) 10%, transparent) 6px)");
		// shadows, highlight, peekview, not available visual
		assertRuleReads(css, ".o_immunity_proof_status", "box-shadow:color-mix(in srgb, var(--o-shadow-color) 35%, transparent) 0px 0px 6px 0px inset");
		assertRuleReads(css, "#o_navbar_wrapper", "box-shadow:inset 0 1px 0 color-mix(in srgb, var(--o-navbar-highlight) 10%, transparent)");
		Assert.assertEquals("var(--o-white)", tokens.get("navbar-highlight"));
		assertRuleReads(css, ".o_cn_styled.o_cn_peekview .o_peekview", "background:var(--o-course-style-peekview-bg)");
		Assert.assertEquals("var(--o-base-course-style-peekview-bg)", tokens.get("course-style-peekview-bg"));
		Assert.assertTrue(css.contains("var(--o-not-available-dark)"));
		Assert.assertTrue(css.contains("var(--o-not-available-light)"));
		Assert.assertFalse("Old colours of the immunity proof shadows", css.contains("rgba(50,50,93") || css.contains("rgba(10,37,64"));
		// the dead block of _config.scss
		String config = read(ThemeColorRatchetTest.LIGHT_MODULES.getParent().resolve("_config.scss"));
		Assert.assertFalse(config.contains("@if contrast($brand-primary, #FFF) < 4.5"));
	}

	/**
	 * The dark layer of the video
	 * comments reads backdrop; the hover and pressed states of the light
	 * buttons, the table filters and the selected scope toggle read a text
	 * token of the state, which follows the text of the button in light, and
	 * their fills keep their values (the default themes do not change);
	 * a fill is 3% darker or lighter at least; the progress bars of the
	 * selectus ratings A, B and C read the fills of the rating marks.
	 */
	@Test
	public void decisionD12D13D15RulesReadTokens() {
		String css = CUSTOM_PROPERTY.matcher(read(PaletteTest.LIGHT_THEME_CSS)).replaceAll("");
		assertRuleReads(css, ".o_video_comment_container", "background-color:color-mix(in srgb, var(--o-backdrop) 50%, transparent)");
		// the text tokens of the states follow the text in light
		for (String button : CONTRAST_SHADE_BUTTONS) {
			for (String state : List.of("-hover", "-active")) {
				Assert.assertEquals(button + "-fg" + state, "var(--o-" + button + "-fg)", tokens.get(button + "-fg" + state));
			}
		}
		Assert.assertEquals("var(--o-scope-toggle-on-fg)", tokens.get("scope-toggle-on-hover-fg"));
		for (String button : List.of("primary", "danger")) {
			String token = "var(--o-btn-" + button + "-light-";
			String selector = ".o_button_" + button + "_light";
			assertRuleReads(css, selector + ".active", "color:" + token + "fg-hover)");
			assertRuleReads(css, selector + ".active", "background-color:" + token + "bg-hover)");
			assertRuleReads(css, selector + ":active:hover", "color:" + token + "fg-active)");
		}
		String filter = ".o_table_filters_wrapper .o_table_filters_row>ul.o_table_filters .o_table_filter";
		assertRuleReads(css, filter + ":hover", "color:var(--o-table-filter-fg-hover)");
		assertRuleReads(css, filter + ":active:hover", "color:var(--o-table-filter-fg-active)");
		assertRuleReads(css, filter + ".o_filter_active:hover", "color:var(--o-table-filter-active-fg-hover)");
		assertRuleReads(css, filter + ".o_filter_open:active:hover", "color:var(--o-table-filter-active-fg-active)");
		assertRuleReads(css, ".o_scope_toggle.o_toggle_on:hover", "color:var(--o-scope-toggle-on-hover-fg)");
		// every fill is visible; the fills of light read the neutral scale
		Assert.assertEquals("#F9FAFB", resolve("btn-primary-light-bg-hover"));
		Assert.assertEquals("#F6F7FA", resolve("btn-primary-light-bg-active"));
		Assert.assertEquals("#F9FAFB", resolve("btn-danger-light-bg-hover"));
		Assert.assertEquals("#F6F7FA", resolve("btn-danger-light-bg-active"));
		Assert.assertEquals("#D0D5DD", resolve("table-filter-bg-active"));
		// the nearest brand step with 4.5:1 for the white text that differs from the fill
		Assert.assertEquals(resolve("theme1-600"), resolve("scope-toggle-on-hover-bg"));
		for (String button : CONTRAST_SHADE_BUTTONS) {
			for (String state : List.of("-hover", "-active")) {
				Assert.assertNotEquals(button + "-bg" + state + " has the colour of the normal state",
						resolve(button + "-bg"), resolve(button + "-bg" + state));
			}
		}
		Assert.assertNotEquals(resolve("scope-toggle-on-bg"), resolve("scope-toggle-on-hover-bg"));
		// the model of o-contrast-shade-amount(): a text with 4.54:1 on
		// white has no visible amount, the state keeps 10% and 17%
		Assert.assertEquals(10, contrastShadeAmount("#DE323C", "#FFFFFF", 10));
		Assert.assertEquals(17, contrastShadeAmount("#DE323C", "#FFFFFF", 17));
		// the light danger button: red-500 of the tuned palette keeps 4.5:1 up to 4%
		Assert.assertEquals(4, contrastShadeAmount(resolve("btn-danger-light-fg"), "#FFFFFF", 10));
		// the rating bars: the solid fill of the marks
		for (String rating : List.of("a", "b", "c")) {
			assertRuleReads(css, ".progress-bar.o_bar_rating_" + rating,
					"background-color:var(--o-selectus-rating-" + rating + "-mark)");
		}
		// the fill of a styled course element (counted by the ratchet pattern)
		assertRuleReads(css, ".o_cn_styled", "background-color:var(--o-course-style-bg)");
		Assert.assertEquals("var(--o-neutral-25)", tokens.get("course-style-bg"));
	}

	/**
	 * The abstention bar of the
	 * selectus statistics reads the tokens of the abstention button, the label
	 * of bar B the text on mark B; the marks of the ratings read their mark
	 * tokens, the explanation texts the fill of the rating buttons, B its own
	 * text token with 4.5:1 on the background; the border of a styled
	 * course element reads course-style-border (the shorthand
	 * $o-cn-style-border is gone); the 8 state fills of the light buttons and
	 * the table filters get lighter when they cannot get darker (a black
	 * fill); the navbar text and tools have 4.5:1 on the navbar.
	 */
	@Test
	public void decisionD16ToD23RulesReadTokens() {
		String css = CUSTOM_PROPERTY.matcher(read(PaletteTest.LIGHT_THEME_CSS)).replaceAll("");
		String abstain = ".progress-bar.o_bar_rating_abstain";
		assertRuleReads(css, abstain, "background-image:linear-gradient(to bottom, var(--o-selectus-rating-abstain-bg) 0%, var(--o-selectus-rating-abstain-bg-hover) 100%)");
		assertRuleReads(css, abstain, "color:var(--o-selectus-rating-abstain-fg)");
		assertRuleReads(css, ".progress-bar.o_bar_rating_b", "color:var(--o-selectus-rating-b-mark-fg)");
		// marks 1 = C, 2 = B, 3 = A; the letter has the colour of the mark
		Map<String, String> marks = Map.of("div.f_rating_1", "selectus-rating-c-mark", "div.f_rating_2",
				"selectus-rating-b-mark", "div.f_rating_3", "selectus-rating-a-mark");
		for (Map.Entry<String, String> mark : marks.entrySet()) {
			assertRuleReads(css, mark.getKey(), "background-color:var(--o-" + mark.getValue() + ")");
			assertRuleReads(css, mark.getKey(), "color:var(--o-" + mark.getValue() + ")");
		}
		// the letter C reads its own token (decisionD25ToD28Selectus)
		assertRuleReads(css, "span.o_rating_expl[title=A]", "color:var(--o-selectus-rating-a-text)");
		assertRuleReads(css, "span.o_rating_expl[title=B]", "color:var(--o-selectus-rating-b-text)");
		assertContrast("selectus-rating-a-bg", "bg", 4.5);
		assertContrast("selectus-rating-b-text", "bg", 4.5);
		assertRuleReads(css, ".o_cn_styled", "border:1px solid var(--o-course-style-border)");
		assertRuleReads(css, ".o_cns_details .o_cns_descriptions", "border-top:1px solid var(--o-course-style-border)");
		Assert.assertEquals("var(--o-border)", tokens.get("course-style-border"));
		Path light = PaletteTest.LIGHT_THEME_CSS.getParent();
		String config = ThemeColorRatchetTest.withoutComments(read(light.resolve("_config.scss")));
		Assert.assertFalse("_config.scss declares the removed shorthand $o-cn-style-border",
				Pattern.compile("\\$o-cn-style-border\\s*:").matcher(config).find());
		// the state fills use the function "state" of o-token-shade(), the
		// model of o-contrast-shade(): a black fill gets lighter by the amount
		String shades = read(light.resolve("modules/_tokens.scss"));
		for (String button : CONTRAST_SHADE_BUTTONS) {
			for (String state : List.of("-hover", "-active")) {
				Matcher m = Pattern.compile("\"" + button + "-bg" + state + "\":[^\\n]*, \"state\"\\),").matcher(shades);
				Assert.assertTrue(button + "-bg" + state + " does not use the shade function \"state\"", m.find());
			}
		}
		Assert.assertEquals("#1A1A1A", stateShade("#000000", 10));
		Assert.assertEquals("#2B2B2B", stateShade("#000000", 17));
		// the computed fill, or a scale step on the side of the computed fill
		assertShade("table-filter-active-bg-hover", "table-filter-active-bg",
				contrastShadeAmount(resolve("table-filter-active-fg"), resolve("table-filter-active-bg"), 10));
		Assert.assertEquals("#657286", resolve("navbar-text"));
		Assert.assertEquals("#657286", resolve("navbar-tools"));
	}

	/**
	 * Selectus: the bar of rating A is a
	 * success segment with the class o_bar_rating_a
	 * (PositionApplicationsController); the letters B and C keep 4.5:1 on a white, a striped and a
	 * hovered table row (adjusted against the darkest of the three,
	 * o-darkest-color()), the border of the marks 3:1; the 8 state fills are the only
	 * shade tokens with the function "state"; the mark of
	 * no rating reads the fill of the default button, the mark of the
	 * abstention the fill of the abstention button.
	 */
	@Test
	public void decisionD25ToD28Selectus() {
		String css = CUSTOM_PROPERTY.matcher(read(PaletteTest.LIGHT_THEME_CSS)).replaceAll("");
		Path light = PaletteTest.LIGHT_THEME_CSS.getParent();
		// the Java code renders bar A as a success segment with the class
		// o_bar_rating_a (OO-10053: segments of the progress bar)
		Assert.assertFalse("The compiled theme has a rule of the old bar selector", css.contains("o_bar_rating_on"));
		Path controller = Path.of("src/main/java/org/olat/modules/selectus/ui/PositionApplicationsController.java");
		Matcher bar = Pattern.compile("new Segment\\(statistics\\.getRating\\((\\d)\\), BarColor\\.(\\w+), (?:\"([^\"]*)\", )?translate")
				.matcher(read(controller));
		int bars = 0;
		while (bar.find()) {
			bars++;
			if ("2".equals(bar.group(1))) {
				Assert.assertEquals("Bar A (rating.2) is a success segment", "success", bar.group(2));
				Assert.assertEquals("Bar A (rating.2)", "o_bar_rating_a", bar.group(3));
			} else {
				String cssClass = "1".equals(bar.group(1)) ? "o_bar_rating_b" : "o_bar_rating_c";
				Assert.assertEquals("Bar rating." + bar.group(1), cssClass, bar.group(3));
			}
		}
		Assert.assertEquals("Bars A, B and C of the statistics", 3, bars);
		Assert.assertTrue("The abstention bar has the class o_bar_rating_abstain",
				read(controller).contains("BarColor.primary, \"o_bar_rating_abstain\""));
		// the letters B and C on the three rows, the letter A and the mark of
		// the abstention with the fill of the button, the border of the marks
		assertRuleReads(css, "span.o_rating_expl[title=C]", "color:var(--o-selectus-rating-c-text)");
		// the nearest step of the hue with 4.5:1 on the darkest row (the row
		// under the pointer): yellow-600, red-600, green-600
		Assert.assertEquals("#7D4F00", resolve("selectus-rating-b-text"));
		Assert.assertEquals("#B50022", resolve("selectus-rating-c-text"));
		Assert.assertEquals("#016807", resolve("selectus-rating-a-text"));
		for (String row : List.of("bg", "table-striped-bg", "table-hover-bg")) {
			assertContrast("btn-default-border", row, 3.0);
			assertContrast("selectus-rating-abstain-bg", row, 3.0);
			assertContrast("selectus-rating-a-text", row, 4.5);
			assertContrast("selectus-rating-b-text", row, 4.5);
			assertContrast("selectus-rating-c-text", row, 4.5);
		}
		// the adjusted tokens read the darkest row of the theme config
		// (o-table-row-darkest-bg(), _config.scss), no hex value
		String scss = read(light.resolve("modules/_tokens.scss"));
		String darkest = "o-table-row-darkest-bg())";
		for (String token : List.of("selectus-rating-a-text", "selectus-rating-b-text", "selectus-rating-c-text")) {
			Matcher m = Pattern.compile("\"" + token + "\":\\s*\\(([^\\n]*)\\),\\s*\\n").matcher(scss);
			Assert.assertTrue(token + " not in _tokens.scss", m.find());
			Assert.assertTrue(token + " is not adjusted against the darkest row: " + m.group(1),
					m.group(1).startsWith("o-scale-a11y-color(o-palette(") && m.group(1).contains(darkest));
		}
		// the lighter black fill stays with the 8 state fills
		Matcher state = Pattern.compile("\"([a-z0-9-]+)\":[^\\n]*, \"state\"\\),").matcher(scss);
		List<String> stateFills = new ArrayList<>();
		while (state.find()) {
			stateFills.add(state.group(1));
		}
		Assert.assertEquals(stateFills.toString(), 2 * CONTRAST_SHADE_BUTTONS.size(), stateFills.size());
		// the marks of no rating and of the abstention read the fill of the buttons
		for (String selector : List.of("div.f_rating_0", "span.f_rating_0")) {
			assertRuleReads(css, selector, "background-color:var(--o-btn-default-bg)");
			assertRuleReads(css, selector, "color:var(--o-btn-default-bg)");
		}
		for (String selector : List.of("div.f_rating_abstention", "span.f_rating_abstention")) {
			assertRuleReads(css, selector, "background-color:var(--o-selectus-rating-abstain-bg)");
			assertRuleReads(css, selector, "color:var(--o-selectus-rating-abstain-bg)");
		}
		Assert.assertEquals("#4E596D", resolve("selectus-rating-abstain-bg"));
	}

	/**
	 * The literal colours of the review: each
	 * block reads the tokens of its recommended option. Texts and icons read
	 * text, heading, text-muted, input-placeholder; borders and lines
	 * border, border-strong, text, bg, bg-raised; fills bg-subtle,
	 * bg-hover, bg-raised, bg; the hover of the file upload button the
	 * default button; scroll shades, rank shadows and layers on media
	 * shadow-color, scrim and backdrop with the alpha of today; AI marks and
	 * glows element tokens with a reference to a state
	 * fill; icons and texts with a status the state tokens;
	 * decorative colours palette steps. The own tokens have
	 * the values of today; the selectus status icons
	 * read the text role of a state. The hotspot styles, the
	 * styles stored in content, brand colours, developer tools and print
	 * sheets keep their literals. The
	 * rules without a reader and the invalid declarations are removed; the
	 * styles that stored content can use (.o_blue, .o_icon_rss_unsubscribe,
	 * .itemPrompt, table.inputHelp) keep their literals with a marker.
	 */
	@Test
	public void decisionD29LiteralsReadTokens() {
		String css = CUSTOM_PROPERTY.matcher(read(PaletteTest.LIGHT_THEME_CSS)).replaceAll("");
		// texts and icons
		assertRuleReads(css, ".o_cit .publicationTitle", "color:var(--o-text)");
		assertRuleReads(css, ".o_cit .notes", "color:var(--o-text-muted)");
		assertRuleReads(css, ".ProseMirror[data-placeholder]::before", "color:var(--o-input-placeholder)");
		assertRuleReads(css, ".o_vertical_timeline .o_vertical_timeline_rows .o_vertical_timeline_row .o_vertical_timeline_year h5",
				"color:var(--o-heading)");
		assertRuleReads(css, ".o_practice_statistics span>i", "color:var(--o-practice-icon)");
		// borders and lines
		assertRuleReads(css, ".d3chart .axis path", "stroke:var(--o-text)");
		assertRuleReads(css, ".o_course_run .o_course_pagination", "border-top:1px solid var(--o-border)");
		assertRuleReads(css, ".o_empty_panel .o_empty_panel_icon", "border:2px solid var(--o-bg)");
		assertRuleReads(css, ".ui-dialog .ui-widget-content", "border-color:var(--o-bg-raised)");
		assertRuleReads(css, "div.f_rating_overview", "border:1px solid var(--o-btn-default-border)");
		assertRuleReads(css, ".o_assessmentitem .gap", "border:1px dashed var(--o-qti-text-gap-border)");
		// fills
		assertRuleReads(css, ".o_im_welcome_image", "background-color:var(--o-bg-subtle)");
		assertRuleReads(css, ".o_page_content_editor .o_page_part.o_page_part_view:hover", "background-color:var(--o-bg-hover)");
		assertRuleReads(css, ".o_badge_wiz_image_step .o_template_cards .o_template_card.o_selected",
				"background-color:var(--o-badge-template-selected-bg)");
		assertRuleReads(css, ".o_table_wrapper .o_scrollable_left .o_col_sticky_left", "background-color:var(--o-table-sticky-col-bg)");
		// one-time code field
		assertRuleReads(css, ".o_sel_registration_otp input.o_sel_registration_otp", "--otp-cell-bg-color:var(--o-otp-cell-bg)");
		assertRuleReads(css, ".o_sel_registration_otp input.o_sel_registration_otp", "--otp-cell-bg-color-focus:var(--o-otp-cell-active-bg)");
		assertRuleReads(css, ".o_sel_registration_otp input.o_sel_registration_otp", "caret-color:var(--otp-cc, var(--o-otp-caret))");
		// hover of the file upload button: long selector lists, each selector reads the default button
		for (String area : List.of(".o_folder .o_folder_navigation .o_folder_create_group .o_fileinput.o_area",
				".o_proj_title .o_proj_title_cmds .o_fileinput.o_area", ".o_media_cmd .o_media_add_cmd>div>div.o_fileinput.o_area")) {
			for (String state : List.of("hover", "focus")) {
				String selector = area + ":has(.o_realchooser:" + state + ")";
				assertRuleReads(css, selector, "color:var(--o-btn-default-fg)");
				assertRuleReads(css, selector, "background-color:var(--o-btn-default-bg-hover)");
				assertRuleReads(css, selector + " button", "color:var(--o-btn-default-fg)");
				assertRuleReads(css, selector + " button", "background-color:var(--o-btn-default-bg-hover)");
			}
		}
		assertRuleReads(css, ".o_folder .o_folder_navigation .o_folder_create_group .o_dnd .o_dnd_icon", "color:var(--o-text)");
		// scroll shades
		assertRuleReads(css, ".o_object_selection .o_object_selection_main::before",
				"background:linear-gradient(color-mix(in srgb, var(--o-shadow-color) 10%, transparent), transparent)");
		// element tokens with a reference to a state fill, the alpha of today
		assertRuleReads(css, ".o_ai_mark_wrong", "background:color-mix(in srgb, var(--o-ai-mark-wrong) 18%, transparent)");
		assertRuleReads(css, ".o_ai_mark_wrong", "text-decoration-color:color-mix(in srgb, var(--o-ai-mark-wrong) 55%, transparent)");
		assertRuleReads(css, ".o_forum .o_forum_message_new", "box-shadow:0 1px 10px color-mix(in srgb, var(--o-forum-message-new-glow) 30%, transparent)");
		assertRuleReads(css, ".o_comments .o_comment_highlight", "box-shadow:0 1px 10px color-mix(in srgb, var(--o-comment-highlight-glow) 50%, transparent)");
		Map<String, String> references = Map.of("ai-mark-correct", "state-success-bg", "ai-mark-ambiguous", "state-warning-bg",
				"ai-mark-wrong", "state-danger-bg", "forum-message-new-glow", "state-success-bg",
				"forum-message-highlight-glow", "state-warning-bg", "comment-highlight-glow", "state-warning-bg");
		for (Map.Entry<String, String> reference : references.entrySet()) {
			Assert.assertEquals(reference.getKey(), "var(--o-" + reference.getValue() + ")", tokens.get(reference.getKey()));
		}
		// status meaning
		assertRuleReads(css, ".o_icon_delete", "color:var(--o-state-danger-text)");
		assertRuleReads(css, ".o_icon_member_skipped", "color:var(--o-state-inactive-text)");
		assertRuleReads(css, ".o_ochre", "color:var(--o-state-warning-text)");
		assertRuleReads(css, ".o_dialog_card_label_new", "background:var(--o-state-warning-soft-bg)");
		assertRuleReads(css, "div.o_wiki_wrapper a.edit", "color:var(--o-state-danger-text)");
		// layers on images and video
		assertRuleReads(css, ".o_video_listing .o_timecode", "background:color-mix(in srgb, var(--o-backdrop) 80%, transparent)");
		assertRuleReads(css, ".o_video_recorder .o_av_container", "background-color:var(--o-backdrop)");
		assertRuleReads(css, ".o_forum .o_icon_enlarge",
				"text-shadow:1px 1px 2px var(--o-scrim),-1px 1px 2px var(--o-scrim),1px -1px 2px var(--o-scrim),-1px -1px 2px var(--o-scrim)");
		// off-canvas menu: the hover reads its Sass variable through the token
		assertRuleReads(css, ".o_navbar.o_navbar-offcanvas .o_navbar-right a:hover", "color:var(--o-offcanvas-link-hover)");
		assertRuleReads(css, ".o_navbar.o_navbar-offcanvas .o_navbar-right h2.o_navbar_category",
				"border-bottom:1px solid var(--o-offcanvas-category-border)");
		Assert.assertEquals("#8B95A7", resolve("offcanvas-link-hover"));
		// own tokens of selectus and the recorder
		assertRuleReads(css, ".o_position_status_filter.o_published", "color:var(--o-selectus-status-published)");
		assertRuleReads(css, ".o_reference_status_filter.o_deactivated", "color:var(--o-selectus-status-deactivated)");
		assertRuleReads(css, ".o_video_recorder .strong-symbol:hover", "fill:var(--o-recorder-record-hover)");
		assertRuleReads(css, ".o_video_recorder .symbol:active", "fill:var(--o-recorder-symbol-active)");
		// decorative colours: palette steps, rank shadows, white pen, drop target
		assertRuleReads(css, ".o_highscore .o_rank",
				"text-shadow:color-mix(in srgb, var(--o-shadow-color) 50%, transparent) 0 -1px 0,color-mix(in srgb, var(--o-scrim) 60%, transparent) 0 2px 1px");
		assertRuleReads(css, ".o_icon_provider_guest", "color:var(--o-orange-500)");
		assertRuleReads(css, ".o_cn_styled.o_cn_header .o_instructional_design",
				"background-color:color-mix(in srgb, var(--o-magenta-700) 15%, transparent)");
		assertRuleReads(css, ".white.btn>i.o_icon_circle_color:before", "color:var(--o-gray-900)");
		assertRuleReads(css, ".o_group_colors .white .o_icon:before", "color:var(--o-white)");
		assertRuleReads(css, ".o_tree ul li .o_dnd_item.o_dnd_over", "background-color:var(--o-tree-dnd-over-bg)");
		// the own tokens keep the colours of today
		String[] own = { "practice-icon", "#C6CCD6", "qti-text-gap-border", "#000000", "badge-template-selected-bg", "#DDDDDD",
				"video-category-active-bg", "#DDDDDD", "table-sticky-col-bg", "#F9FAFB", "table-sticky-col-striped-bg", "#F6F7FA",
				"otp-cell-bg", "#EAECF0", "otp-cell-active-bg", "#B1B8C5", "otp-caret", "#222222",
				"offcanvas-category-border", "#8B95A7", "tree-dnd-over-bg", "#F6C600",
				"recorder-decoration", "#000000", "recorder-recording", "#FFBBB1", "recorder-record", "#F36660",
				"recorder-record-hover", "#B50022", "recorder-record-active", "#8E0D1C", "recorder-symbol", "#000000",
				"recorder-symbol-hover", "#182233", "recorder-symbol-active", "#344054" };
		for (int i = 0; i < own.length; i += 2) {
			Assert.assertEquals(own[i], own[i + 1], resolve(own[i]));
		}
		// the new contrast pairs pass with the light defaults
		assertContrast("otp-caret", "otp-cell-bg", 4.5);
		assertContrast("otp-caret", "otp-cell-active-bg", 4.5);
		assertContrast("qti-text-gap-border", "bg", 3.0);
		for (String token : List.of("recorder-record", "recorder-record-hover", "recorder-symbol", "recorder-symbol-hover",
				"recorder-symbol-active")) {
			assertContrast(token, "bg", 3.0);
		}
		Path modules = PaletteTest.LIGHT_THEME_CSS.getParent().resolve("modules");
		// the kept literals are still there, the hotspot styles name the template
		assertSourceHas(modules.resolve("_qti21.scss"), "background-color: rgba(255, 255, 255, 0.5);", "border-color: #6E6E6E;",
				"background-color: rgba(234, 168, 255, 0.5);", "border-color: #ab47cb;", "background-color: rgba(229, 255, 204, 0.6);",
				"border-color: black;", "border: 5px solid rgba(211,211,211,0.66);");
		Assert.assertTrue("The hotspot styles do not name the template of the test run",
				read(modules.resolve("_qti21.scss")).contains("org/olat/ims/qti21/ui/components/_content/hotspotInteraction.html"));
		assertSourceHas(modules.resolve("_contenteditor.scss"), "border: solid 1px #CCC;", "border: 1px solid #dcdcdc;");
		assertSourceHas(modules.resolve("_tinymce.scss"), "background: #f5f2f0;");
		assertSourceHas(modules.resolve("_various_modules.scss"), ".o_tm_yellow { background-color: #FFFF66; }",
				".o_tm_glossary { border-bottom: 1px dotted #666699 !important; }");
		assertSourceHas(modules.resolve("_dmz.scss"), "color: #4568b2;", "color: rgb(0, 114, 198);");
		assertSourceHas(modules.resolve("_icons.scss"), "color: #cfdc00;");
		assertSourceHas(modules.resolve("_devel.scss"), "background: #f0ad4e;");
		// the styles that stored content can use stay, the rules
		// without a reader and the invalid declarations are removed
		assertSourceHas(modules.resolve("_helpers.scss"), ".o_blue { color: #12223F; }");
		assertSourceHas(modules.resolve("_icons.scss"), "color: #996633;");
		assertSourceHas(modules.resolve("_qti21.scss"), "color: #666666", "background-color: #cad8e5;");
		assertSourceLacks(modules.resolve("_selectus.scss"), "b_preparation", "b_published", "b_closed", "boxShadow");
		assertSourceLacks(modules.resolve("_user.scss"), "o_userinfo_status", "o_userinfo_roles");
		assertSourceLacks(modules.resolve("_table.scss"), "o_reset_filter_search");
		assertSourceLacks(modules.resolve("_video.scss"), "o_cosegments_SELBOX", "box-shadow: red");
		assertSourceLacks(modules.resolve("_folder.scss"), "1px solid 1px solid");
		assertSourceLacks(modules.resolve("_topicbroker.scss"), "1px solid 1px solid");
		assertSourceLacks(modules.resolve("_curriculum.scss"), "$o-curriculum-widget-template-label-border");
		assertSourceLacks(PaletteTest.LIGHT_THEME_CSS.getParent().resolve("_config.scss"), "$o-curriculum-widget-template-label-border");
		Assert.assertFalse("The compiled theme has a selectus status class b_*", css.contains(".b_preparation") || css.contains(".b_closed"));
		assertSourceHas(modules.resolve("_portfolio_v2.scss"), "pink");
		assertSourceHas(PaletteTest.LIGHT_THEME_CSS.getParent().resolve("_config.scss"), "$o-video-controls-selected", "$o-video-controls-bg");
	}

	/**
	 * The framework modules (dialog, login page, cards,
	 * navbar, table, tree, icons, helpers): the rules read element tokens fed
	 * by the Sass variables of the theme config, no colour changes. The message
	 * boxes read the existing fill tokens box-*-bg; note, tip and important
	 * boxes, message lines, dialog header and footer, login page, widgets,
	 * offcanvas menu, table, tree, status icons and drop places have own
	 * tokens. The new texts and icons pass their contrast pairs in light.
	 */
	@Test
	public void frameworkModulesReadTokens() {
		String css = CUSTOM_PROPERTY.matcher(read(PaletteTest.LIGHT_THEME_CSS)).replaceAll("");
		// message boxes, message lines, popover, dialog
		assertRuleReads(css, ".o_info", "background-color:var(--o-box-info-bg)");
		assertRuleReads(css, ".o_note", "border:1px solid var(--o-box-note-border)");
		assertRuleReads(css, ".o_tip", "background-color:var(--o-box-tip-bg)");
		assertRuleReads(css, ".o_important", "border:1px solid var(--o-box-important-border)");
		assertRuleReads(css, ".o_warning_line", "color:var(--o-message-line-warning-fg)");
		assertRuleReads(css, ".o_error_line", "background-color:var(--o-message-line-error-bg)");
		assertRuleReads(css, "dialog.popover .popover-title", "border:1px solid var(--o-popover-fallback-border)");
		assertRuleReads(css, ".modal .modal-header", "background:var(--o-dialog-header-bg)");
		assertRuleReads(css, ".modal .modal-footer", "background:var(--o-dialog-footer-bg)");
		assertRuleReads(css, ".modal .modal-footer", "box-shadow:0 -4px 4px var(--o-dialog-header-bg)");
		// login page
		assertRuleReads(css, ".o_login .o_login_intro .lead", "color:var(--o-login-lead)");
		assertRuleReads(css, ".o_login .o_login_box .o_login_help", "background-color:var(--o-login-help-bg)");
		assertRuleReads(css, ".o_login .o_login_form p.o_login_username", "border:1px solid var(--o-login-border)");
		// widgets and dashboard widgets
		assertRuleReads(css, ".o_widget .o_widget_header .o_widget_title .o_widget_subtitle", "color:var(--o-widget-subtitle)");
		assertRuleReads(css, ".o_dashboard_table_widget", "background-color:var(--o-dashboard-widget-header-footer-bg)");
		assertRuleReads(css, ".o_dashboard_table_widget .o_dashboard_widget_table", "background-color:var(--o-dashboard-widget-table-bg)");
		// navbar and offcanvas menu
		assertRuleReads(css, ".o_navbar #o_navbar_tools_personal .o_logout", "color:var(--o-navbar-logout)");
		// the login link of the guest navbar is a primary button without an own colour rule
		Assert.assertFalse("The login link has an own colour rule", css.contains("#o_navbar_login a{"));
		assertRuleReads(css, ".o_navbar #o_navbar_tools_permanent #o_navbar_login.o_navbar_button", "padding-left:7px");
		Assert.assertNull(tokens.get("navbar-login"));
		assertRuleReads(css, ".o_navbar.o_navbar-offcanvas .o_navbar-right .dropdown-menu", "background-color:var(--o-offcanvas-dropdown-bg)");
		assertRuleReads(css, ".o_navbar.o_navbar-offcanvas .o_navbar-nav .active a", "color:var(--o-offcanvas-link-active)");
		// table and tree
		assertRuleReads(css, ".o_table_wrapper.o_table_edit table tbody", "border-top:solid var(--o-table-edit-border) 4px");
		assertRuleReads(css, ".o_table_tabs", "border-bottom:1px solid var(--o-table-filter-collapse-border)");
		assertRuleReads(css, ".o_tree", "border:1px solid var(--o-tree-border)");
		// icons and drop places
		assertRuleReads(css, ".o_icon_changes", "color:var(--o-icon-changes)");
		assertRuleReads(css, ".o_icon_mandatory", "color:var(--o-icon-mandatory)");
		assertRuleReads(css, ".o_icon_status_dnd", "color:var(--o-im-status-dnd)");
		assertRuleReads(css, ".o_icon_qti_ended", "color:var(--o-qti-status-ended)");
		assertRuleReads(css, ".o_lectures_rollcall_", "color:var(--o-lecture-rollcall-pending)");
		assertRuleReads(css, ".o_icon_certification_certifying", "color:var(--o-certification-certifying)");
		assertRuleReads(css, ".panel-placeholder .panel-heading", "color:var(--o-panel-placeholder-fg)");
		// the tokens follow the Sass chain of their variable
		Assert.assertEquals("var(--o-dialog-header-bg)", tokens.get("dialog-footer-bg"));
		Assert.assertEquals("var(--o-text)", tokens.get("login-lead"));
		Assert.assertEquals("var(--o-content-header)", tokens.get("login-lead-header"));
		Assert.assertEquals("var(--o-bg-selected)", tokens.get("table-row-selected-bg"));
		Assert.assertEquals("var(--o-state-danger-text)", tokens.get("navbar-logout"));
		// the colours of today
		String[] values = { "box-tip-border", "#D5F1FE", "box-important-border", "#EFE9FE",
				"box-tip-icon", "#034D65", "box-important-icon", "#431D7E",
				"message-line-warning-bg", "#FFBE80", "message-line-error-fg", "#470A0E", "popover-fallback-border", "#C6CCD6",
				"dialog-header-bg", "#F9FAFB", "login-help-bg", "#E4E7EC", "widget-subtitle", "#657286",
				"offcanvas-link", "#B1B8C5", "table-edit-bg", "#F9FAFB", "tree-border", "#DDDDDD",
				"tree-insertion-source-bg", "#FFBE80", "icon-mandatory", "#C24A00", "lecture-rollcall-pending", "#637286" };
		for (int i = 0; i < values.length; i += 2) {
			Assert.assertEquals(values[i], values[i + 1], resolve(values[i]));
		}
		// the new contrast pairs pass with the light defaults
		assertContrast("box-tip-icon", "box-tip-bg", 3.0);
		assertContrast("box-important-icon", "box-important-bg", 3.0);
		assertContrast("message-line-warning-fg", "message-line-warning-bg", 4.5);
		assertContrast("message-line-error-fg", "message-line-error-bg", 4.5);
		assertContrast("dialog-header", "dialog-header-bg", 4.5);
		assertContrast("login-lead", "bg", 4.5);
		assertContrast("login-lead-header", "bg", 4.5);
		assertContrast("login-help-link", "login-help-bg", 4.5);
		assertContrast("widget-figure-desc", "bg", 4.5);
		assertContrast("navbar-logout", "bg-raised", 4.5);
		assertContrast("offcanvas-link", "offcanvas-dropdown-bg", 4.5);
		assertContrast("offcanvas-link-active", "offcanvas-link-active-bg", 4.5);
		assertContrast("tree-insertion-source-fg", "tree-insertion-source-bg", 4.5);
		assertContrast("panel-placeholder-fg", "panel-placeholder-bg", 4.5);
		for (String icon : List.of("icon-changes", "icon-mandatory", "certification-certified", "certification-certifying",
				"portfolio-assignment", "im-status-available", "im-status-dnd", "im-status-unavailable", "qti-status-ended",
				"qti-status-invalid", "lecture-rollcall-pending")) {
			assertContrast(icon, "bg", 3.0);
		}
	}

	/**
	 * The focus ring: the token focus is the primary
	 * colour adjusted to 3:1 on the page background ($o-focus-color, read
	 * before the Bootstrap defaults define $input-border-focus); every control
	 * has the ring outline 2px solid var(--o-focus) with the offset 2px on
	 * :focus-visible instead of the ring of the browser (tab-focus) and the
	 * halo or the glow of the form fields; the controls that touch their
	 * neighbours or that a container cuts have the ring inside the edge, on a
	 * fill in the colour of the ring in the text colour of the fill; the navbar
	 * reads navbar-focus, the footer footer-focus; a tree entry has the ring
	 * inside its row in tree-focus; the radio card shows the ring, the checked
	 * radio card has the brand colour; no theme module has the literal of the
	 * old focus colour.
	 */
	@Test
	public void decisionD24FocusRing() throws IOException {
		Assert.assertEquals("#2E6BBF", resolve("focus"));
		Assert.assertEquals("var(--o-focus)", tokens.get("navbar-focus"));
		Assert.assertEquals("var(--o-focus)", tokens.get("footer-focus"));
		assertContrast("focus", "bg", 3.0);
		String css = CUSTOM_PROPERTY.matcher(read(PaletteTest.LIGHT_THEME_CSS)).replaceAll("");
		// the ring on :focus-visible, no ring on :focus
		String ring = "outline:2px solid var(--o-focus)";
		for (String selector : List.of("a:focus:focus-visible", ".btn:focus:focus-visible",
				"input[type=checkbox]:focus:focus-visible", ".form-control:focus-visible", ":where(:focus-visible)",
				".dropdown-toggle:focus-visible", ".o_fileinput .o_realchooser:focus-visible+.o_fakechooser")) {
			assertRuleReads(css, selector, ring);
			assertRuleReads(css, selector, "outline-offset:2px");
		}
		assertRuleReads(css, "a:focus", "outline:0");
		assertRuleReads(css, ".btn:focus", "outline:0");
		// a radio card shows the ring, the radio or checkbox inside it does not
		assertRuleReads(css, "form .o_form .o_radio_cards_wrapper .o_radio_cards label input:focus-visible~.o_radio_card", ring);
		assertRuleReads(css, "form .o_form .o_radio_cards_wrapper .o_radio_cards label input:focus-visible~.o_radio_card", "outline-offset:2px");
		assertRuleReads(css, "form .o_form .o_radio_cards_wrapper .o_radio_cards label input:focus-visible", "outline:0");
		assertRuleReads(css, ".form-control:focus", "border-color:var(--o-focus)");
		assertRuleReads(css, ".form-control:focus", "box-shadow:none");
		Assert.assertFalse("The ring of the browser (tab-focus) is in the compiled theme", css.contains("-webkit-focus-ring-color"));
		// the halo and the glows of the focused fields are gone
		Matcher m = RULE.matcher(css);
		while (m.find()) {
			if (m.group(1).contains(":focus") && m.group(2).matches("(?s).*box-shadow:[^;]*var\\(--o-focus\\).*")) {
				Assert.fail("Focus rule with a halo or glow: " + m.group(1).trim());
			}
		}
		Assert.assertFalse("The 8px glow of the autocomplete menu", css.matches("(?s).*\\.tt-menu\\{[^}]*box-shadow.*"));
		// the inset ring and the ring on a fill
		assertRuleReads(css, ".nav-pills>li>a:focus:focus-visible", "outline-offset:-2px");
		assertRuleReads(css, ".input-group .form-control:focus:focus-visible", "outline-offset:-2px");
		assertRuleReads(css, ".nav-pills>li>.o_filter_active:focus:focus-visible", "outline:2px solid currentColor");
		assertRuleReads(css, ".nav-pills>li>.o_filter_active:focus:focus-visible", "outline-offset:-4px");
		assertRuleReads(css, ".o_navbar .o_navbar-nav>li>a:focus:focus-visible", "outline:2px solid var(--o-navbar-focus)");
		assertRuleReads(css, ".o_navbar .o_navbar-nav>li>a:focus:focus-visible", "outline-offset:-2px");
		assertRuleReads(css, "#o_footer_wrapper a:focus:focus-visible", "outline-color:var(--o-footer-focus)");
		assertRuleReads(css, ".o_checkbox_button_group>div:has(input:focus-visible)", "outline-offset:-2px");
		// the checked radio card is a selection in the brand colour: the border reads the primary
		// colour, the fill step 50 of the brand ramp
		Assert.assertEquals("var(--o-primary)", tokens.get("radio-card-checked-border"));
		Assert.assertEquals(resolve("theme1-50"), resolve("radio-card-checked-bg"));
		// the existence of $input-border-focus is read before the Bootstrap defaults
		Path light = PaletteTest.LIGHT_THEME_CSS.getParent();
		String config = read(light.resolve("_config.scss"));
		int check = config.indexOf("meta.global-variable-exists(\"input-border-focus\")");
		int bootstrap = config.indexOf("@import \"../../bootstrap/stylesheets/bootstrap/variables\"");
		Assert.assertTrue("$input-border-focus is not checked before the Bootstrap defaults", check > 0 && check < bootstrap);
		// no literal of the old focus colour in the theme modules, except in comments
		Pattern literal = Pattern.compile("(?i)66afe9|102,\\s*175,\\s*233");
		try (Stream<Path> files = Files.walk(light.getParent())) {
			for (Path file : files.filter(p -> p.toString().endsWith(".scss")).toList()) {
				String scss = ThemeColorRatchetTest.withoutComments(read(file));
				Assert.assertFalse(file + " has the literal of the old focus colour", literal.matcher(scss).find());
			}
		}
	}

	/**
	 * The other button rules read tokens: the disabled
	 * link button, the ghost, mega, light primary and light danger buttons,
	 * the filter buttons of a table, the toggle switch, the scope toggle,
	 * the confirm button of the appointments and the rating buttons of
	 * selectus. A rule that reads the Sass variable again compiles to a
	 * colour and fails here.
	 */
	@Test
	public void otherButtonRulesReadTokens() {
		String css = CUSTOM_PROPERTY.matcher(read(PaletteTest.LIGHT_THEME_CSS)).replaceAll("");
		assertRuleReads(css, ".btn-link[disabled]:hover", "color:var(--o-text-disabled)");
		assertRuleReads(css, ".btn.o_button_ghost:hover", "color:var(--o-btn-ghost-hover-fg)");
		assertRuleReads(css, ".btn.o_button_ghost:hover", "border-color:var(--o-btn-ghost-hover-border)");
		assertRuleReads(css, ".btn.o_button_mega", "background-color:var(--o-btn-mega-bg)");
		assertRuleReads(css, ".btn.o_button_mega:hover", "background-color:var(--o-btn-mega-hover-bg)");
		for (String button : List.of("primary", "danger")) {
			String token = "var(--o-btn-" + button + "-light-";
			String selector = ".o_button_" + button + "_light";
			assertRuleReads(css, selector, "color:" + token + "fg)");
			assertRuleReads(css, selector, "background-color:" + token + "bg)");
			assertRuleReads(css, selector, "border-color:" + token + "border)");
			assertRuleReads(css, selector + ":active:hover", "background-color:" + token + "bg-active)");
			assertRuleReads(css, selector + ":hover", "background-color:" + token + "hover-bg)");
			assertRuleReads(css, selector + ":hover", "color:" + token + "hover-fg)");
		}
		String filter = ".o_table_filters_wrapper .o_table_filters_row>ul.o_table_filters .o_table_filter";
		assertRuleReads(css, filter, "color:var(--o-table-filter-fg)");
		assertRuleReads(css, filter + ":hover", "background-color:var(--o-table-filter-bg-hover)");
		assertRuleReads(css, filter + ".o_filter_active", "background-color:var(--o-table-filter-active-bg)");
		assertRuleReads(css, filter + ".o_filter_open", "color:var(--o-table-filter-active-fg)");
		assertRuleReads(css, ".o_button_toggle", "background:var(--o-toggle-bg)");
		assertRuleReads(css, ".o_button_toggle[aria-checked=true] i", "color:var(--o-toggle-on-handle)");
		assertRuleReads(css, ".o_scope_toggle.btn:hover", "background-color:var(--o-scope-toggle-hover-bg)");
		assertRuleReads(css, ".o_scope_toggle.o_toggle_on:hover", "background-color:var(--o-scope-toggle-on-hover-bg)");
		assertRuleReads(css, ".o_appointments.o_list .o_buttons .o_button_confirm", "color:var(--o-appointment-select-icon)");
		for (String rating : SELECTUS_RATINGS) {
			String token = "selectus-rating-" + rating + "-bg";
			assertShade(token + "-hover", token, 10);
			assertShade(token + "-active", token, 17);
			Assert.assertTrue("No rule reads var(--o-" + token + "-active)", css.contains("var(--o-" + token + "-active)"));
		}
		assertRuleReads(css, ".f_rating_items.f_rating_enabled .o_btn_rating.o_btn_rating_on[title=B]",
				"background-color:var(--o-selectus-rating-b-bg)");
	}

	/**
	 * Every token of the :root block of _tokens.scss (the block with
	 * --o-white) resolves to a colour. A keyword like "none" in a token
	 * behaves differently from the same keyword in the rule. A translucent
	 * default of a named Sass variable is a color-mix() of a token that
	 * resolves to a colour.
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
			Matcher mix = COLOR_MIX.matcher(value);
			if (mix.matches()) {
				value = resolve(mix.group(1));
			}
			Assert.assertTrue("--o-" + name + ": " + value, COLOR.matcher(value).matches());
		}
	}

	/**
	 * The assessment and content modules (QTI, page
	 * editor, practice, video, video task) and the framework
	 * modules that followed: the rules read element tokens fed by the Sass variables
	 * of the theme config, no colour changes. A named variable with a
	 * translucent default feeds a token with the color-mix() default. The
	 * shorthand border variables are split into width, style and a colour
	 * token; $o-login-border stays valid for a theme that sets it. The
	 * new texts and icons pass their contrast pairs in light.
	 */
	@Test
	public void assessmentAndContentModulesReadTokens() {
		String css = CUSTOM_PROPERTY.matcher(read(PaletteTest.LIGHT_THEME_CSS)).replaceAll("");
		// translucent defaults
		assertRuleReads(css, ".o_login .o_login_box", "background-color:var(--o-login-bg)");
		assertRuleReads(css, ".o_block_imagebg span", "background-color:var(--o-imagebg-text-bg)");
		Assert.assertEquals("color-mix(in srgb, var(--o-scrim) 95%, transparent)", resolve("login-bg"));
		Assert.assertEquals("color-mix(in srgb, var(--o-primary) 50%, transparent)", resolve("drawing-select-bg"));
		// shorthand borders, width and style of the theme config and a colour token
		assertRuleReads(css, ".o_card", "border:1px solid var(--o-card-border)");
		assertRuleReads(css, ".o_fact_sheet", "border:solid 1px var(--o-fact-sheet-border)");
		assertRuleReads(css, ".o_activity_add", "border:1px solid var(--o-activity-add-border)");
		assertRuleReads(css, ".o_table_wrapper.o_table_flexi .o_table_row_details .o_table_row_details_container",
				"border:1px solid var(--o-table-details-container-border)");
		assertRuleReads(css, ".o_login .o_login_form p.o_login_username", "border:1px solid var(--o-login-border)");
		Path light = PaletteTest.LIGHT_THEME_CSS.getParent();
		String config = ThemeColorRatchetTest.withoutComments(read(light.resolve("_config.scss")));
		for (String shorthand : List.of("$o-card-border", "$o-widget-border", "$o-fact-sheet-border",
				"$o-fact-sheet-separator-border", "$o-dashboard-widget-border", "$o-table-details-container-border",
				"$o-activity-add-border", "$o-activity-modify-border", "$o-activity-remove-border",
				"$o-repo-info-border", "$o-repo-info-separator-border", "$o-editor-layout-inner-h-border",
				"$o-editor-layout-inner-v-border")) {
			Assert.assertFalse("_config.scss defines the removed shorthand " + shorthand,
					Pattern.compile(Pattern.quote(shorthand) + "\\s*:").matcher(config).find());
		}
		assertSourceHas(light.resolve("_config.scss"), "$o-login-border", "@function o-login-border()");
		assertSourceHas(light.resolve("modules/_tokens.scss"), "$o-token-removed-shorthands", "@function o-token-is-mix(");
		// part B: QTI, page editor, practice, video, video task
		assertRuleReads(css, "#o_qti_run_infos #o_qti_scoreinfo .progress-bar",
				"background-color:var(--o-qti-run-infos-score-progress-bg)");
		assertRuleReads(css, ".hottextInteraction .hottext.o_disabled.o_checked", "background:var(--o-qti-hottext-checked-bg)");
		// the slot name of the page editor had no DOM: rule and token are removed
		Assert.assertFalse(css.contains("o_hover_text"));
		Assert.assertNull(tokens.get("editor-hover-text"));
		assertRuleReads(css, ".o_ce_add .o_ce_tools .o_ce_tool a:hover", "border:1px solid var(--o-toolbar-border)");
		assertRuleReads(css, ".progress-bar.o_practice_progress_l2", "background-color:var(--o-practice-progress-l2-bg)");
		assertRuleReads(css, ".o_video_run .o_author", "color:var(--o-coursesite-meta-author)");
		assertRuleReads(css, ".o_videotask_messages>.o_note", "color:var(--o-box-note-text)");
		assertRuleReads(css, ".o_videotask_correct_segments", "color:var(--o-video-correct-segments)");
		// the tokens follow the Sass chain of their variable
		Assert.assertEquals("var(--o-toolbar-border)", tokens.get("qti-menu-border"));
		Assert.assertEquals("var(--o-qti-menu-border)", tokens.get("qti-item-title-border"));
		Assert.assertEquals("var(--o-qti-run-infos-bg)", tokens.get("qti-match-sources-bg"));
		Assert.assertEquals("var(--o-progress-bar-neutral-bg)", tokens.get("qti-run-infos-score-progress-bg"));
		Assert.assertEquals("var(--o-state-neutral-soft-border)", tokens.get("qti-hottext-checked-border"));
		Assert.assertEquals("var(--o-editor-border)", tokens.get("editor-layout-inner-h-border"));
		Assert.assertEquals("var(--o-repo-info-border)", tokens.get("fact-sheet-border"));
		Assert.assertEquals("var(--o-state-success-bg)", tokens.get("activity-add-border"));
		Assert.assertEquals("var(--o-text)", tokens.get("coursesite-meta-author"));
		Assert.assertEquals("var(--o-neutral-500)", tokens.get("box-note-text"));
		// the colours of today
		String[] values = { "card-border", "#E4E7EC", "repo-info-border", "#D0D5DD", "qti-item-max-score-border", "#000000",
				"qti-choice-tablelayout-bg", "#E4E7EC", "editor-hover-border", "#DC66D5", "editor-alert-box-active-border", "#464646",
				"practice-progress-l1-bg", "#047A9F", "video-border", "#F9FAFB", "video-controls-bg",
				"color-mix(in srgb, var(--o-shadow-color) 95%, transparent)" };
		for (int i = 0; i < values.length; i += 2) {
			Assert.assertEquals(values[i], values[i + 1], resolve(values[i]));
		}
		// the new contrast pairs pass with the light defaults
		assertContrast("box-note-text", "box-note-bg", 4.5);
		assertContrast("qti-box-text", "qti-order-sources-bg", 4.5);
		assertContrast("qti-box-text", "qti-order-target-bg", 4.5);
		assertContrast("coursesite-meta-author", "bg", 4.5);
		assertContrast("video-correct-segments", "bg", 4.5);
		assertContrast("video-notcorrect-segments", "bg", 4.5);
		assertContrast("practice-progress-l2-bg", "bg", 3.0);
	}

	/**
	 * The course, curriculum, user, chat, high score,
	 * projects, radial progress, Selectus and various modules: the rules read
	 * element tokens fed by the Sass variables of the theme config, no colour
	 * changes. The shorthand border variables of these modules are split into
	 * width, style and a colour token, the translucent
	 * defaults feed a token with the color-mix() default, the icon colours
	 * of the figure cards are tokens of the tint swatch. The other modules read
	 * the tokens of $o-toolbar-border-color, $o-slider-border,
	 * $o-portfolio-media-content-background-color,
	 * $o-coursesite-meta-author-color and $o-table-row-selected-color. The new
	 * texts and icons pass their contrast pairs in light.
	 */
	@Test
	public void courseUserAndVariousModulesReadTokens() {
		String css = CUSTOM_PROPERTY.matcher(read(PaletteTest.LIGHT_THEME_CSS)).replaceAll("");
		// shorthand borders, width and style of the theme config and a colour token
		assertRuleReads(css, ".o_empty_state", "border:1px solid var(--o-empty-border)");
		assertRuleReads(css, ".o_icon_panel", "border:1px solid var(--o-icon-panel-border)");
		assertRuleReads(css, ".o_highscore .o_first", "border:1px solid var(--o-highscore-podium-border)");
		assertRuleReads(css, ".o_user_info .o_user_info_divider", "border-right:solid 1px var(--o-user-info-divider-border)");
		assertRuleReads(css, ".o_user_info_profile_container .o_user_info_profile", "border:solid 1px var(--o-user-info-profile-border)");
		assertRuleReads(css, ".o_curriculum_element_outline .o_curriculum_element_outline_lead",
				"border-top:1px solid var(--o-curriculum-widget-border)");
		Path light = PaletteTest.LIGHT_THEME_CSS.getParent();
		String config = ThemeColorRatchetTest.withoutComments(read(light.resolve("_config.scss")));
		for (String shorthand : List.of("$o-empty-border-color", "$o-icon-panel-border", "$o-lp-border",
				"$o-curriculum-widget-border", "$o-curriculum-widget-card-border", "$o-assessment-inspection-border-color",
				"$o-hs-podium-border", "$o-users-portrait-img-border", "$o-user-info-portrait-border", "$o-user-info-border",
				"$o-user-info-profile-border", "$o-user-info-divider-border", "$o-panel-authentication-border",
				"$o-indicator-separator-border")) {
			Assert.assertFalse("_config.scss defines the removed shorthand " + shorthand,
					Pattern.compile(Pattern.quote(shorthand) + "\\s*:").matcher(config).find());
		}
		assertSourceHas(light.resolve("modules/_tokens.scss"), "$o-token-removed-shorthands-renamed");
		// translucent defaults
		Assert.assertEquals("color-mix(in srgb, var(--o-scrim) 95%, transparent)", resolve("course-style-cover-text-bg"));
		Assert.assertEquals("color-mix(in srgb, var(--o-primary) 10%, transparent)", resolve("brand-box-bg"));
		// the icon colours of the figure cards: the tint swatch of the hue
		assertRuleReads(css, ".o_figure_cards .o_figure_card_icon_red", "color:var(--o-figure-card-icon-red)");
		assertRuleReads(css, ".o_figure_cards.o_figure_cards_n1 .o_figure_card_icon_blue",
				"background-color:var(--o-figure-card-icon-blue-bg)");
		Assert.assertEquals("var(--o-blue-900)", tokens.get("figure-card-icon-blue"));
		Assert.assertEquals("var(--o-gray-300)", tokens.get("figure-card-icon-grey-bg"));
		// existing tokens of their element or of their variable
		assertRuleReads(css, ".o_progress .o_progress-bar-neutral.progress-bar", "background-color:var(--o-progress-bar-neutral-bg)");
		assertRuleReads(css, ".o_toolbar", "border:1px solid var(--o-toolbar-border)");
		assertRuleReads(css, ".ui-slider.ui-widget-content", "border-color:var(--o-slider-border)");
		assertRuleReads(css, ".o_video_segment_category_selection .o_categories .o_category_list .o_category_list_item.o_selected",
				"background-color:var(--o-table-row-selected-bg)");
		// new element tokens
		assertRuleReads(css, ".o_exam_course .o_educational_type", "color:var(--o-edu-type-exam-course)");
		assertRuleReads(css, ".radial-progress-success .radial-bar", "stroke:var(--o-radial-progress-circle-success)");
		assertRuleReads(css, ".o_im_message_group.o_im_message_me", "background-color:var(--o-im-chat-message-me-bg)");
		assertRuleReads(css, ".o_base_blue", "color:var(--o-colored-icon-blue)");
		assertRuleReads(css, ".o_authentication_level1", "color:var(--o-authentication-level1)");
		// the tokens follow the Sass chain of their variable
		Assert.assertEquals("var(--o-progress-bar-bg)", tokens.get("radial-progress-circle"));
		Assert.assertEquals("var(--o-progress-bar-neutral-bg)", tokens.get("coursesite-progress-icon"));
		Assert.assertEquals("var(--o-border)", tokens.get("indicator-separator-border"));
		Assert.assertEquals("var(--o-user-info-border)", tokens.get("user-info-divider-border"));
		Assert.assertEquals("var(--o-state-success-text)", tokens.get("lp-pagination-done-bg"));
		Assert.assertEquals("var(--o-panel-bg)", tokens.get("curriculum-widget-bg"));
		Assert.assertEquals("var(--o-state-warning-bg)", tokens.get("selectus-review-comment-glow"));
		// the colours of today
		String[] values = { "empty-border", "#E4E7EC", "highscore-podium-border", "#D9DDE3", "user-info-profile-border", "#C6CCD6",
				"lp-pagination-done-border", "#436202", "curriculum-element-inactive", "#657286", "selectus-onhold", "#4E596D",
				"authentication-level1", "#7A2F07", "user-status-inactive-border", "#637286" };
		for (int i = 0; i < values.length; i += 2) {
			Assert.assertEquals(values[i], values[i + 1], resolve(values[i]));
		}
		// the new contrast pairs pass with the light defaults
		assertContrast("datecomp-month-fg", "datecomp-month-bg", 4.5);
		assertContrast("radial-progress-number", "radial-progress-inset-bg", 4.5);
		assertContrast("radial-progress-percentage-warning", "radial-progress-inset-bg", 4.5);
		assertContrast("figure-card-icon-yellow", "figure-card-icon-yellow-bg", 3.0);
		assertContrast("colored-icon-orange", "bg", 3.0);
		assertContrast("im-summary-fg", "im-summary-bg", 4.5);
		assertContrast("edu-type-seminar-course", "bg", 4.5);
		assertContrast("lp-pagination-done-fg", "lp-pagination-done-bg", 4.5);
		assertContrast("curriculum-avatar-fg", "curriculum-avatar-bg", 4.5);
		assertContrast("highscore-rank-second", "highscore-podium-second-bg", 3.0);
		assertContrast("proj-timeline-today", "bg", 4.5);
		assertContrast("user-initials-fg", "user-initials-base-bg", 4.5);
		assertContrast("authentication-level1", "bg", 4.5);
	}

	/**
	 * The other modules read element tokens fed by their
	 * Sass variables, the toolbar reads tokens, the
	 * variables with upper-case letters have tokens and the position
	 * title of the high score has a switch and a token.
	 */
	@Test
	public void otherModulesReadTokens() {
		String css = CUSTOM_PROPERTY.matcher(read(PaletteTest.LIGHT_THEME_CSS)).replaceAll("");
		// the toolbar
		assertRuleReads(css, ".o_toolbar .o_tools_container", "background-color:var(--o-toolbar-bg)");
		assertRuleReads(css, ".o_toolbar .o_tool_next", "background-color:var(--o-toolbar-nav-bg)");
		assertRuleReads(css, ".o_edit_mode .o_toolbar .o_tools_container",
				"background:repeating-linear-gradient(300deg, var(--o-toolbar-edit-stripe), var(--o-toolbar-edit-stripe) 10px, var(--o-toolbar-edit-bg) 10px, var(--o-toolbar-edit-bg) 20px)");
		// names with upper-case letters
		assertRuleReads(css, ".o_assessmentitem div.badResponse", "color:var(--o-qti-bad-response)");
		assertRuleReads(css, ".o_icon_qti_notPresented", "color:var(--o-qti-status-not-presented)");
		Assert.assertEquals("var(--o-state-danger-text)", tokens.get("qti-bad-response"));
		Assert.assertEquals("var(--o-state-neutral-soft-bg)", tokens.get("qti-text-entry-filled-bg"));
		Assert.assertEquals("var(--o-qti-gap-border)", tokens.get("qti-text-entry-border"));
		Assert.assertEquals(1, ThemeColorRatchetTest.countOldColorReferences(".a { color: $o-qti-textEntryInteraction-color; }"));
		Assert.assertEquals(0, ThemeColorRatchetTest.countOldColorReferences(".a { width: $o-qti-textEntryInteraction-width; }"));
		// the gradient by default, the token without it; the old variable stays valid
		Path light = PaletteTest.LIGHT_THEME_CSS.getParent();
		assertSourceHas(light.resolve("_config.scss"), "$o-hs-position-title-gradient", "@function o-hs-position-title-gradient()",
				"@function o-hs-position-title-color()");
		assertSourceHas(light.resolve("modules/_highscore.scss"), "@if o-hs-position-title-gradient()", "var(--o-highscore-position-title)");
		Assert.assertEquals("var(--o-text)", tokens.get("highscore-position-title"));
		// shorthand borders, width and style of the theme config and a colour token
		assertRuleReads(css, ".o_process .o_step .o_step_block", "border:1px solid var(--o-step-border)");
		assertRuleReads(css, ".o_ac_billing_address", "border:1px solid var(--o-ac-billing-address-border)");
		assertRuleReads(css, "body .o_swiper_buttons .swiper-button-next", "border:1px solid var(--o-swiper-button-border)");
		assertRuleReads(css, ".o_tb_weighting .o_tb_weighting_slider_grid_top", "border-top:1px solid var(--o-topicbroker-weighting-grid-border)");
		String config = ThemeColorRatchetTest.withoutComments(read(light.resolve("_config.scss")));
		for (String shorthand : List.of("$o-step-border", "$o-ac-ba-border", "$o-folder-row-thumbnail-image-border",
				"$o-swiper-button-border", "$o-tb-details-separator-border-bottom", "$o-tb-topic-card-title-abbr-border",
				"$o-tb-topic-placeholder-title-abbr-border", "$o-tb-weighting-grid-border")) {
			Assert.assertFalse("_config.scss defines the removed shorthand " + shorthand,
					Pattern.compile(Pattern.quote(shorthand) + "\\s*:").matcher(config).find());
		}
		// new element tokens and existing tokens of their variables
		assertRuleReads(css, ".tt-menu", "color:var(--o-autocomplete-fg)");
		assertRuleReads(css, ".o_tag", "color:var(--o-tag-fg)");
		assertRuleReads(css, ".o_tag.o_competence", "color:var(--o-competence-fg)");
		assertRuleReads(css, "#o_main_wrapper", "background:var(--o-main-outer-bg)");
		assertRuleReads(css, ".o_wizard .o_steps ul li .chevron:before", "border-left:14px solid var(--o-wizard-step-bg)");
		assertRuleReads(css, ".o_gloss_tooltip.tooltip.top .tooltip-arrow", "border-top-color:var(--o-gloss-tooltip-border)");
		assertRuleReads(css, ".dropdown-menu>.active>label", "background-color:var(--o-dropdown-link-active-bg)");
		assertRuleReads(css, "table.b_grid td", "border:1px solid var(--o-content-table-border)");
		assertRuleReads(css, ".progress-bar-neutral", "background-color:var(--o-progress-bar-neutral-bg)");
		// names with "_" ($o-o_speedometer-*-border-color, $o_color_note): a border and a text token per state
		assertRuleReads(css, ".o_speedometer.o_speedometer_danger>.o_speedometer_value", "color:var(--o-speedometer-danger-text)");
		assertRuleReads(css, ".o_speedometer.o_speedometer_danger>span:after", "border-color:var(--o-speedometer-danger-border)");
		Assert.assertEquals("var(--o-speedometer-danger-border)", tokens.get("speedometer-danger-text"));
		Assert.assertEquals("var(--o-state-danger-border)", tokens.get("speedometer-danger-border"));
		assertContrast("speedometer-success-text", "bg", 3.0);
		assertContrast("video-recording-info-fg", "video-recording-info-bg", 4.5);
		// the mixin o-alert-variant is not part of email.css: its text reads the token
		assertRuleReads(css, ".o-modal-alert-success .alert", "color:var(--o-text)");
		// the tokens follow the Sass chain of their variable
		Assert.assertEquals("var(--o-input-bg)", tokens.get("autocomplete-bg"));
		Assert.assertEquals("var(--o-panel-bg)", tokens.get("portlet-bg"));
		Assert.assertEquals("var(--o-competence-border)", tokens.get("competence-fg"));
		Assert.assertEquals("var(--o-text)", tokens.get("content-fieldset-legend"));
		Assert.assertEquals("var(--o-primary)", tokens.get("dropdown-link-active-bg"));
		Assert.assertEquals("var(--o-step-marker)", tokens.get("step-active-marker"));
		Assert.assertEquals("var(--o-panel-default-border)", tokens.get("topicbroker-details-separator-border"));
		Assert.assertEquals("var(--o-quality-heatmap-base-bg)", tokens.get("quality-unrated"));
		// the colours of today
		String[] values = { "step-border", "#E4E7EC", "ac-billing-address-border", "#D0D5DD", "toolbar-edit-stripe", "#F9EBC5",
				"gloss-tooltip-border", "#B1B8C5", "step-notavailable", "#8B95A7", "folder-card-thumbnail-icon", "#C6CCD6",
				"qti-status-not-answered", "#C24A00" };
		for (int i = 0; i < values.length; i += 2) {
			Assert.assertEquals(values[i], values[i + 1], resolve(values[i]));
		}
		// the new contrast pairs pass with the light defaults
		assertContrast("tag-fg", "tag-bg", 4.5);
		assertContrast("tag-fg", "tag-bg-active", 4.5);
		assertContrast("competence-fg", "competence-bg-hover", 4.5);
		assertContrast("dropdown-link-active", "dropdown-link-active-bg", 4.5);
		assertContrast("autocomplete-fg", "autocomplete-bg", 4.5);
		assertContrast("color-picker-selected-fg", "primary", 4.5);
		assertContrast("qti-text-entry-filled-fg", "qti-text-entry-filled-bg", 4.5);
		assertContrast("media-version-label-fg", "media-version-label-bg", 4.5);
		assertContrast("course-quota-intern", "bg", 4.5);
		assertContrast("qti-status-not-presented", "bg", 3.0);
		assertContrast("qpool-arrow-draft-border", "qpool-arrow-bg", 3.0);
	}

	/**
	 * The element tokens, the shade tokens and the base colours of the light
	 * theme read the three colour scales: the palette, the neutral scale with
	 * white and the brand ramp. In the compiled light
	 * theme.css only the scales themselves hold a literal colour: no base
	 * colour --o-base-{name} and no shade token. _tokens.scss holds no hex
	 * colour outside the comments and the white background of a contrast
	 * pair: a default reads o-neutral(), o-palette() or a token.
	 */
	@Test
	public void tokenDefaultsReadScales() {
		Matcher property = CUSTOM_PROPERTY.matcher(read(PaletteTest.LIGHT_THEME_CSS));
		List<String> literals = new ArrayList<>();
		while (property.find()) {
			String name = property.group(1);
			String value = property.group(2).trim();
			if (COLOR.matcher(value).matches() && !"transparent".equals(value) && !SCALE_PROPERTY.matcher(name).matches()) {
				literals.add(name + ": " + value);
			}
		}
		Assert.assertTrue("Tokens with a literal colour: " + literals, literals.isEmpty());
		String scss = ThemeColorRatchetTest.withoutComments(read(ThemeColorRatchetTest.LIGHT_MODULES.resolve("_tokens.scss")));
		Pattern pairBackground = Pattern.compile("^\\s*\\(\"[a-z0-9#{}$-]+\", #FFFFFF, [0-9.]+\\),?\\s*\\)?\\)?;?\\s*$");
		List<String> hex = new ArrayList<>();
		for (String line : scss.split("\n")) {
			if (HEX_LITERAL.matcher(line).find() && !pairBackground.matcher(line).matches()) {
				hex.add(line.trim());
			}
		}
		Assert.assertTrue("Hex colours in _tokens.scss: " + hex, hex.isEmpty());
	}

	/**
	 * @return The step one lighter than the step of the hue that has the colour of the token
	 */
	private String lighterStep(String hue, String token) {
		String color = resolve(token);
		for (int i = 1; i < Palette.STEPS.size(); i++) {
			if (color.equals(resolve(hue + "-" + Palette.STEPS.get(i)))) {
				return String.valueOf(Palette.STEPS.get(i - 1));
			}
		}
		Assert.fail(token + " " + color + " is no step of " + hue);
		return null;
	}

	/**
	 * A bright fill of light (step 100 to 400 of a hue, dark text) without a
	 * darker step of its hue that keeps the text: its hover and its pressed
	 * fill read the step one lighter of the same hue (o-token-shade-lighter()).
	 * Only this rule lets the hover and the pressed fill have one colour
	 * (o-token-shade-trios()); the text keeps 4.5:1 on each fill. The other
	 * state fills are step 500 with white text: their hover and pressed fill
	 * are darker.
	 */
	@Test
	public void brightFillStatesGoLighter() {
		for (Map.Entry<String, String> fill : BRIGHT_FILLS.entrySet()) {
			String hue = fill.getValue();
			String lighter = "var(--o-" + hue + "-" + lighterStep(hue, fill.getKey()) + ")";
			Assert.assertEquals(fill.getKey() + "-hover", lighter, tokens.get(fill.getKey() + "-hover"));
			Assert.assertEquals(fill.getKey() + "-active", lighter, tokens.get(fill.getKey() + "-active"));
		}
		Assert.assertEquals("state-warning-bg", resolve("yellow-300"), resolve("state-warning-bg"));
		Assert.assertEquals("selectus-rating-b-bg", resolve("yellow-400"), resolve("selectus-rating-b-bg"));
		// the dirty button is a button of $o-token-state-text-buttons: its dark text gives a
		// lighter hover (step 200) and a lighter pressed fill (step 100)
		Assert.assertEquals("btn-dirty-bg", resolve("yellow-300"), resolve("btn-dirty-bg"));
		Assert.assertEquals("btn-dirty-bg-hover", "var(--o-yellow-200)", tokens.get("btn-dirty-bg-hover"));
		Assert.assertEquals("btn-dirty-bg-active", "var(--o-yellow-100)", tokens.get("btn-dirty-bg-active"));
		for (String state : List.of("", "-hover", "-active")) {
			assertContrast("btn-dirty-fg", "btn-dirty-bg" + state, 4.5);
			assertContrast("coursesite-book-fg", "coursesite-book-bg" + state, 4.5);
			assertContrast("tag-selected-fg", "tag-selected-bg" + state, 4.5);
			assertContrast("state-warning-fg", "state-warning-bg" + state, 4.5);
			assertContrast("state-success-fg", "state-success-bg" + state, 4.5);
			assertContrast("selectus-rating-b-fg", "selectus-rating-b-bg" + state, 4.5);
		}
		assertSourceHas(ThemeColorRatchetTest.LIGHT_MODULES.resolve("_tokens.scss"), "@function o-token-shade-lighter(");
	}

	/**
	 * The curriculum status labels: preparation neutral, provisional warning,
	 * confirmed and active success, all solid; the outline labels have the
	 * text style of the same state.
	 */
	@Test
	public void curriculumStatusLabels() {
		String scss = read(ThemeColorRatchetTest.LIGHT_MODULES.resolve("_states.scss"));
		Map<String, String> states = Map.of("preparation", "neutral", "provisional", "warning", "confirmed", "success",
				"active", "success");
		for (Map.Entry<String, String> status : states.entrySet()) {
			String cssClass = "o_curriculum_status_" + status.getKey();
			Assert.assertTrue(cssClass, scss.contains("\".o_labeled." + cssClass + "\": (\"" + status.getValue() + "\", \"solid\")"));
			Assert.assertTrue(cssClass, scss.contains("\".o_labeled_light." + cssClass + "\": (\"" + status.getValue() + "\", \"text\")"));
		}
	}

	/**
	 * Selectus: the decision rows A, B and C have the hue of their rating
	 * (green, yellow, red), the hover one step darker; the border of the
	 * rating marks and the mark of the abstention keep 3:1 on the six fills. The status icons read the text role of a state,
	 * the back circle of the screening icons the fill of the warning state.
	 */
	@Test
	public void selectusDecisionRowsAndStatusIcons() {
		Map<String, String> rows = Map.of("a", "green", "b", "yellow", "c", "red");
		for (Map.Entry<String, String> row : rows.entrySet()) {
			String fill = tokens.get("selectus-decision-" + row.getKey() + "-bg");
			String hover = tokens.get("selectus-decision-" + row.getKey() + "-hover-bg");
			Assert.assertTrue(fill, fill.startsWith("var(--o-" + row.getValue() + "-"));
			Assert.assertTrue(hover, hover.startsWith("var(--o-" + row.getValue() + "-"));
			Assert.assertTrue("The hover of row " + row.getKey() + " is not darker than its fill",
					ColorContrast.relativeLuminance(resolve("selectus-decision-" + row.getKey() + "-hover-bg"))
							< ColorContrast.relativeLuminance(resolve("selectus-decision-" + row.getKey() + "-bg")));
			for (String mark : List.of("btn-default-border", "selectus-rating-abstain-bg")) {
				assertContrast(mark, "selectus-decision-" + row.getKey() + "-bg", 3.0);
				assertContrast(mark, "selectus-decision-" + row.getKey() + "-hover-bg", 3.0);
			}
		}
		Map<String, String> status = Map.of("preparation", "neutral", "published", "success", "published-screening", "success",
				"closed-screening", "critical", "closed", "danger", "reporting", "finished", "deactivated", "inactive");
		for (Map.Entry<String, String> icon : status.entrySet()) {
			String name = "selectus-status-" + icon.getKey();
			Assert.assertEquals(name, "var(--o-state-" + icon.getValue() + "-text)", tokens.get(name));
			assertContrast(name, "bg", 3.0);
		}
		// the back circle of the two screening icons, never shown alone: the fill
		// of the warning state, 3:1 against the two front halves
		Assert.assertEquals("var(--o-state-warning-bg)", tokens.get("selectus-status-screening"));
		assertContrast("selectus-status-published-screening", "selectus-status-screening", 3.0);
		assertContrast("selectus-status-closed-screening", "selectus-status-screening", 3.0);
	}

	/**
	 * The cells of the one-time code of the registration have the border of
	 * a form field. In the error state they have the border of a form field
	 * with an error (3:1 on the cell fill and on its focus fill), not the soft
	 * border of the error box.
	 */
	@Test
	public void otpErrorCellBorder() {
		String css = CUSTOM_PROPERTY.matcher(read(PaletteTest.LIGHT_THEME_CSS)).replaceAll("");
		assertRuleReads(css, ".has-error .form-control", "border-color:var(--o-state-danger-text)");
		assertRuleReads(css, ".o_pw_otp_validation.o_validation_invalid .o_sel_registration_otp .o_background",
				"--otp-border-color:var(--o-state-danger-text)");
		// without an error the cells have the border of a form field
		assertRuleReads(css, ".o_pw_otp_validation .o_sel_registration_otp.o_container .o_background",
				"--otp-border-color:var(--o-border-strong)");
		assertContrast("state-danger-text", "box-error-bg", 3.0);
		assertContrast("state-danger-text", "registration-otp-error-bg-focus", 3.0);
	}

	/**
	 * The error message under a form field has the border of the field with
	 * the error: the text of the state danger, 3:1 on the fill of the box.
	 * The general error box and the error box with icon keep the soft
	 * border.
	 */
	@Test
	public void formErrorBorder() {
		Assert.assertEquals("var(--o-state-danger-text)", tokens.get("form-error-border"));
		assertContrast("form-error-border", "box-error-bg", 3.0);
		Assert.assertEquals("var(--o-state-danger-soft-border)", tokens.get("box-error-border"));
		String css = CUSTOM_PROPERTY.matcher(read(PaletteTest.LIGHT_THEME_CSS)).replaceAll("");
		assertRuleReads(css, "form .o_form .o_error", "border-color:var(--o-form-error-border)");
		assertRuleReads(css, "form.o_form .o_error", "border-color:var(--o-form-error-border)");
		assertRuleReads(css, ".o_error", "border:1px solid var(--o-box-error-border)");
		assertRuleReads(css, ".o_error_with_icon", "border:1px solid var(--o-box-error-border)");
	}

	/**
	 * Texts, links and shapes with too little contrast before: the labels of
	 * the high score histogram and the selectus rows on hold or with the mail
	 * disabled read the muted text; name and time of a chat message neutral
	 * 600; placeholder neutral 500 and typed text of a field the body text;
	 * dark text on the bright fills of the contact tracing confirmation; the
	 * links on a selectus decision row in the text colour, underlined; the
	 * rating star with a darker edge; the media icon without preview neutral
	 * 450; the important box with border and icon
	 * cyan 500; the declarations that read the brand colour as text or icon
	 * read primary-text.
	 */
	@Test
	public void contrastReviewColours() throws IOException {
		String css = CUSTOM_PROPERTY.matcher(read(PaletteTest.LIGHT_THEME_CSS)).replaceAll("");
		Assert.assertEquals("var(--o-text-muted)", tokens.get("highscore-histogram-text"));
		// the muted text adjusted to 4.5:1 on the row under the pointer
		Assert.assertEquals("var(--o-neutral-600)", tokens.get("selectus-onhold"));
		Assert.assertEquals("var(--o-neutral-600)", tokens.get("selectus-mail-disabled"));
		assertContrast("highscore-histogram-text", "bg", 4.5);
		for (String row : List.of("bg", "table-striped-bg", "table-hover-bg")) {
			assertContrast("selectus-onhold", row, 4.5);
			assertContrast("selectus-mail-disabled", row, 4.5);
		}
		// chat
		Assert.assertEquals("var(--o-neutral-600)", tokens.get("im-chat-from"));
		Assert.assertEquals("var(--o-im-chat-from)", tokens.get("im-chat-date"));
		assertContrast("im-chat-from", "im-chat-message-me-bg", 4.5);
		assertContrast("im-chat-date", "im-chat-message-me-bg", 4.5);
		// fields: the Bootstrap rules compile the same colours as the tokens
		Assert.assertEquals("#657286", resolve("input-placeholder"));
		Assert.assertEquals("var(--o-text)", tokens.get("input-fg"));
		Assert.assertTrue(css.contains(".form-control::-webkit-input-placeholder{color:#657286}"));
		assertRuleReads(css, ".form-control", "color:#344054");
		assertContrast("input-placeholder", "input-bg", 4.5);
		assertContrast("input-placeholder", "input-disabled-bg", 4.5);
		// contact tracing confirmation
		assertRuleReads(css, ".o_ct_confirmation_wrapper", "color:var(--o-contact-tracing-confirmation-fg)");
		assertRuleReads(css, ".o_ct_confirmation_wrapper.validated", "color:var(--o-contact-tracing-validated-fg)");
		assertRuleReads(css, ".o_ct_confirmation_wrapper.claimed", "color:var(--o-contact-tracing-claimed-fg)");
		assertRuleReads(css, ".o_ct_confirmation_wrapper.none", "color:var(--o-white)");
		assertRuleReads(css, ".o_ct_confirmation_wrapper h1", "color:inherit");
		Assert.assertEquals("var(--o-lime-900)", tokens.get("contact-tracing-confirmation-fg"));
		Assert.assertEquals("var(--o-lime-900)", tokens.get("contact-tracing-validated-fg"));
		Assert.assertEquals("var(--o-orange-900)", tokens.get("contact-tracing-claimed-fg"));
		assertContrast("contact-tracing-confirmation-fg", "contact-tracing-confirmation-bg", 4.5);
		assertContrast("contact-tracing-validated-fg", "immunity-proof-validated-bg", 4.5);
		assertContrast("contact-tracing-claimed-fg", "immunity-proof-claimed-bg", 4.5);
		// selectus decision rows
		for (String row : List.of("a", "b", "c")) {
			String link = ".o_table_wrapper table.table tbody tr.fx_r_" + row + "_decision>td a:not(.btn):not(.o_practice_play):not(.o_link_uncolored):not(.o_table_row_details_container a)";
			assertRuleReads(css, link, "color:var(--o-text)");
			assertRuleReads(css, link, "text-decoration:underline");
			assertContrast("text", "selectus-decision-" + row + "-bg", 4.5);
			assertContrast("text", "selectus-decision-" + row + "-hover-bg", 4.5);
		}
		// rating stars: the yellow fill with a darker edge, no edge on the
		// personal rating, the yes and no icons and the stars under the pointer
		// of the evaluation form
		assertRuleReads(css, ".o_rating .o_rating_items .o_icon", "-webkit-text-stroke:1px var(--o-rating-star-edge)");
		assertRuleReads(css, ".o_rating.o_rating_personal .o_rating_items .o_icon", "-webkit-text-stroke-width:0");
		assertRuleReads(css, ".o_rating.o_rating_yesno .o_rating_items .o_icon", "-webkit-text-stroke-width:0");
		// the empty star: the outline glyph in the edge colour without a stroke;
		// under the pointer of an editable rating the filled star with the edge
		assertRuleReads(css, ".o_rating .o_rating_items .o_icon_rating_off", "color:var(--o-rating-star-edge)");
		assertRuleReads(css, ".o_rating .o_rating_items .o_icon_rating_off", "-webkit-text-stroke-width:0");
		assertRuleReads(css, ".o_rating .o_rating_items.o_enabled .o_icon_rating_off:hover", "color:var(--o-rating-star)");
		assertRuleReads(css, ".o_rating .o_rating_items.o_enabled .o_icon_rating_off:hover", "-webkit-text-stroke-width:1px");
		assertRuleReads(css, ".o_rating.o_rating_personal .o_rating_items.o_enabled .o_icon_rating_off:hover", "color:var(--o-primary-text)");
		Assert.assertEquals("var(--o-yellow-600)", tokens.get("rating-star-edge"));
		assertContrast("rating-star-edge", "bg", 3.0);
		// the icon of a media without preview: neutral 450, 3:1 on the card
		Assert.assertEquals("var(--o-neutral-450)", tokens.get("editor-media-unavailable"));
		assertContrast("editor-media-unavailable", "bg", 3.0);
		assertContrast("editor-media-unavailable", "bg-raised", 3.0);
		// important box: the soft swatch of violet, the icon in the dark violet (800)
		Assert.assertEquals("var(--o-violet-50)", tokens.get("box-important-bg"));
		Assert.assertEquals("var(--o-violet-100)", tokens.get("box-important-border"));
		Assert.assertEquals("var(--o-violet-800)", tokens.get("box-important-icon"));
		assertContrast("box-important-icon", "box-important-bg", 4.5);
		// tip box: one hue, border cyan 100, icon cyan 700, 4.5:1 on cyan 50
		Assert.assertEquals("var(--o-cyan-100)", tokens.get("box-tip-border"));
		Assert.assertEquals("var(--o-cyan-700)", tokens.get("box-tip-icon"));
		assertContrast("box-tip-icon", "box-tip-bg", 4.5);
		// the brand colour as text or icon colour reads primary-text
		Pattern primaryAsColor = Pattern.compile("(?<![-\\w])color\\s*:\\s*var\\(--o-primary\\)");
		try (Stream<Path> modules = Files.list(ThemeColorRatchetTest.LIGHT_MODULES)) {
			for (Path module : modules.filter(p -> p.toString().endsWith(".scss")).toList()) {
				Assert.assertFalse(module.getFileName() + " reads primary as text colour",
						primaryAsColor.matcher(ThemeColorRatchetTest.withoutComments(read(module))).find());
			}
		}
		assertRuleReads(css, ".o_rating.o_rating_personal .o_rating_items .o_icon", "color:var(--o-primary-text)");
	}

	/**
	 * The rank numbers of the high score podium: the darkest step of the hue
	 * of their block, 3:1 on the four stops of the gradient. The compiled
	 * themes have no standard gradient with a direction keyword without "to"
	 * (the browser drops such a declaration when it reads var()).
	 */
	@Test
	public void podiumRanksAndGradientDirections() {
		Map<String, String> ranks = Map.of("first", "var(--o-yellow-900)", "second", "var(--o-neutral-900)", "third", "var(--o-orange-900)");
		for (Map.Entry<String, String> rank : ranks.entrySet()) {
			Assert.assertEquals(rank.getValue(), tokens.get("highscore-rank-" + rank.getKey()));
			for (String stop : List.of("", "-stop-1", "-stop-2", "-stop-4")) {
				assertContrast("highscore-rank-" + rank.getKey(), "highscore-podium-" + rank.getKey() + "-bg" + stop, 3.0);
			}
		}
		Pattern oldDirection = Pattern.compile("(?<![-\\w])(?:repeating-)?linear-gradient\\(\\s*(?:top|left|bottom|right)\\b");
		Path themes = PaletteTest.LIGHT_THEME_CSS.getParent().getParent();
		for (String file : List.of("light/theme.css", "openolat/theme.css")) {
			String css = read(themes.resolve(file));
			Matcher m = oldDirection.matcher(css);
			if (m.find()) {
				Assert.fail(file + ": gradient without \"to\": " + css.substring(m.start(), Math.min(css.length(), m.start() + 80)));
			}
		}
	}

	/**
	 * The fill step 450 of a palette hue or of the brand ramp: only the fill
	 * and shape tokens whose colour before the scale mapping lay between step
	 * 400 and step 500 read it (chart bars, the hover border of the page
	 * editor, the bronze podium, the record symbol). The automatic step
	 * choices (o-scale-snap(), o-scale-on-color(), the shade tokens) never
	 * pick it; the icons of the colour orange keep step 500, 3:1 on the light
	 * surfaces. Each token keeps 3:1 on white.
	 */
	@Test
	public void fillStepTokens() {
		Map<String, String> moved = Map.of("chart-bar-light", "theme1-450", "highscore-bar-myself-hover", "theme1-450",
				"editor-hover-border", "magenta-450", "highscore-podium-third-bg", "orange-450",
				"recorder-record", "red-450");
		for (Map.Entry<String, String> token : moved.entrySet()) {
			Assert.assertEquals(token.getKey(), "var(--o-" + token.getValue() + ")", tokens.get(token.getKey()));
			assertContrast(token.getKey(), "bg", 3.0);
		}
		Pattern fillStep = Pattern.compile("var\\(--o-([a-z]+\\d*)-450\\)");
		List<String> readers = new ArrayList<>();
		for (Map.Entry<String, String> token : tokens.entrySet()) {
			Matcher m = fillStep.matcher(token.getValue());
			if (m.find() && !"neutral".equals(m.group(1)) && !moved.containsKey(token.getKey())) {
				readers.add(token.getKey() + ": " + token.getValue());
			}
		}
		Assert.assertTrue("Tokens on a fill step 450 without a decision: " + readers, readers.isEmpty());
		// hover and pressed of the record symbol stay darker steps
		Assert.assertEquals("var(--o-red-600)", tokens.get("recorder-record-hover"));
		Assert.assertEquals("var(--o-red-700)", tokens.get("recorder-record-active"));
		Assert.assertEquals("var(--o-orange-500)", tokens.get("icon-mandatory"));
		Assert.assertEquals("var(--o-orange-500)", tokens.get("colored-icon-orange"));
		assertSourceHas(ThemeColorRatchetTest.LIGHT_MODULES.getParent().resolve("_palette.scss"), "$o-palette-fill-steps: (450: (400, 500))");
	}

	/**
	 * The link around the thumbnail of a course list row is a block of the size
	 * of the thumbnail: its focus ring runs along the edge of the image.
	 */
	@Test
	public void courseListThumbnailLink() {
		String css = CUSTOM_PROPERTY.matcher(read(PaletteTest.LIGHT_THEME_CSS)).replaceAll("");
		assertRuleReads(css, ".o_repo_entry_list_item .o_visual>a", "display:block");
		assertRuleReads(css, ".o_repo_entry_list_item .o_visual>a", "height:100%");
	}

	/**
	 * Every var(--o-*) in the compiled theme.css, content.css and oo-docs.css
	 * of the themes light and openolat has a definition in the same file.
	 * The compile does not check the names. Exception: the custom properties
	 * that a renderer sets at runtime (RUNTIME_PROPERTIES).
	 */
	@Test
	public void referencesHaveDefinitions() {
		Path themes = PaletteTest.LIGHT_THEME_CSS.getParent().getParent();
		for (String file : TOKEN_FILES) {
			String css = read(themes.resolve(file));
			Map<String, String> defined = parseTokens(css);
			Matcher m = ANY_VAR_REFERENCE.matcher(css);
			while (m.find()) {
				Assert.assertTrue(file + ": var(--o-" + m.group(1) + ") has no definition",
						defined.containsKey(m.group(1)) || RUNTIME_PROPERTIES.contains(m.group(1)));
			}
		}
	}

	/**
	 * @param css The compiled CSS without the custom properties
	 * @param selector One selector of a selector list, without spaces around ">"
	 * @param declaration The declaration without spaces after ":" and without ";"
	 */
	/**
	 * The focus ring of a tree entry is inside the row of the entry (li > div,
	 * the full width of the tree), not around the text of the link; the link
	 * has no ring of its own then. A browser without :has() keeps the ring of
	 * the link. The opener keeps its ring outside its icon. Both read
	 * tree-focus: the focus colour, adjusted to 3:1 on the darkest row
	 * background (in light the focus colour itself).
	 */
	@Test
	public void treeFocusRingInsideRow() {
		String css = CUSTOM_PROPERTY.matcher(read(PaletteTest.LIGHT_THEME_CSS)).replaceAll("");
		Assert.assertTrue("The row ring needs @supports selector(:has(a))",
				css.contains("@supports selector(:has(a)){.o_tree li>div:has(>.o_tree_link>a:focus-visible)"));
		assertRuleReads(css, ".o_tree li>div:has(>.o_tree_link>a:focus-visible)", "outline:2px solid var(--o-tree-focus)");
		assertRuleReads(css, ".o_tree li>div:has(>.o_tree_link>a:focus-visible)", "outline-offset:-2px");
		assertRuleReads(css, ".o_tree li>div>.o_tree_link>a:focus-visible", "outline:0");
		assertRuleReads(css, ".o_tree li>div>a.o_tree_open_close:focus-visible", "outline:2px solid var(--o-tree-focus)");
		assertRuleReads(css, ".o_tree li>div>a.o_tree_open_close:focus-visible", "outline-offset:2px");
		Assert.assertEquals("var(--o-focus)", tokens.get("tree-focus"));
		for (String bg : List.of("bg", "tree-link-hover-bg", "tree-link-active-hover-bg")) {
			assertContrast("tree-focus", bg, 3.0);
		}
	}

	/**
	 * The fills of the tree and of the centre and the right column, the
	 * border of the navigation bar and the hover colours of the content links
	 * and of the footer links read element tokens. A client theme keeps
	 * setting the Sass variables. The fills are transparent by default; the
	 * hover colours have a contrast pair.
	 */
	@Test
	public void layoutFillsAndLinkHoversReadTokens() {
		String css = CUSTOM_PROPERTY.matcher(read(PaletteTest.LIGHT_THEME_CSS)).replaceAll("");
		assertRuleReads(css, ".o_tree", "background-color:var(--o-tree-bg)");
		assertRuleReads(css, ".o_tree .o_tree_link", "background-color:var(--o-tree-link-bg)");
		assertRuleReads(css, ".o_tree ul li .active.o_tree_link", "background-color:var(--o-tree-link-active-bg)");
		assertRuleReads(css, ".o_tree.o_course_menu div.o_tree_l0>a:first-child", "background-color:var(--o-tree-link-bg)");
		assertRuleReads(css, "#o_main_wrapper #o_main_container #o_main_center", "background:var(--o-main-center-bg)");
		assertRuleReads(css, "#o_main_wrapper #o_main_container #o_main_right", "background:var(--o-main-right-bg)");
		assertRuleReads(css, "#o_navbar_wrapper", "border-top:1px solid var(--o-navbar-border)");
		assertRuleReads(css, ".o_navbar", "border:1px solid var(--o-navbar-border)");
		assertRuleReads(css, ".o_navbar-form", "border-top:1px solid var(--o-navbar-border)");
		assertRuleReads(css, ".o_navbar-form", "border-bottom:1px solid var(--o-navbar-border)");
		assertRuleReads(css, "#o_footer_wrapper a:hover", "color:var(--o-footer-link-hover)");
		assertRuleReads(css, "#o_footer_user #o_share a:hover", "color:var(--o-footer-link-hover)");
		Assert.assertTrue("The content link hover rule", css.contains(".o_user_content_block a:where(:not(.btn,.o_button_primary_light,.o_button_danger_light,.o_button_dirty)):hover,.o_user_content_block a:where(:not(.btn,.o_button_primary_light,.o_button_danger_light,.o_button_dirty)):focus{color:var(--o-content-link-hover);"));
		for (String fill : List.of("tree-bg", "tree-link-bg", "tree-link-active-bg", "main-center-bg", "main-right-bg")) {
			Assert.assertEquals(fill, "transparent", resolve(fill));
		}
		Assert.assertEquals("var(--o-link-hover)", tokens.get("content-link-hover"));
		assertContrast("content-link-hover", "bg", 4.5);
		assertContrast("footer-link-hover", "footer-inner-bg", 4.5);
	}

	/**
	 * A link with a button class in a content block keeps the colours of its
	 * button (OO-10087): the link rules of .o_user_content_block exclude the
	 * button classes with :where(), so their specificity stays (0,1,1) and
	 * (0,2,1) and the button rules win. Plain links keep the content link
	 * colours. In theme.css and in content.css.
	 */
	@Test
	public void contentBlockButtonLinksKeepButtonColours() {
		String link = ".o_user_content_block a:where(:not(.btn,.o_button_primary_light,.o_button_danger_light,.o_button_dirty))";
		for (Path file : List.of(PaletteTest.LIGHT_THEME_CSS, PaletteTest.LIGHT_THEME_CSS.resolveSibling("content.css"))) {
			String css = CUSTOM_PROPERTY.matcher(read(file)).replaceAll("");
			// the selector holds commas inside :not(): assertRuleReads() splits at them
			Assert.assertTrue(file + ": content link rule", css.contains(link + "{color:var(--o-content-link);"));
			Assert.assertTrue(file + ": content link hover rule",
					css.contains(link + ":hover," + link + ":focus{color:var(--o-content-link-hover);"));
			Assert.assertFalse(file + ": the content block link rule reaches the buttons",
					css.contains(".o_user_content_block a{") || css.contains(".o_user_content_block a:hover"));
		}
		String css = CUSTOM_PROPERTY.matcher(read(PaletteTest.LIGHT_THEME_CSS)).replaceAll("");
		assertRuleReads(css, ".btn-primary", "color:var(--o-btn-primary-fg)");
		assertRuleReads(css, ".o_button_primary_light:hover", "color:var(--o-btn-primary-light-fg-hover)");
		// the realistic style (light/styles/_realistic.scss, not part of light): the
		// light buttons and the ghost button have a flat fill, their own fills and
		// borders show without the gradient
		assertSourceHas(ThemeColorRatchetTest.LIGHT_MODULES.resolveSibling("styles").resolve("_realistic.scss"),
				"&.o_button_ghost,", "&.o_button_primary_light,", "&.o_button_danger_light {", "background-image: none;");
	}

	/**
	 * The realistic style (light/styles/_realistic.scss, not part of light)
	 * sets no colour of its own (OO-10043): the fills are the tokens of the
	 * light theme, the depth comes from translucent white and black edges and
	 * from shadows in the token shadow-color. It does not import the
	 * Bootstrap theme, reads no Bootstrap or OpenOlat colour variable and has
	 * no Sass colour maths, so the old state colours of a client config have
	 * no effect on it. An element with a light text gets the black layer, one
	 * with a dark text the flat fill.
	 */
	@Test
	public void realisticStyleReadsTokens() {
		Path realistic = ThemeColorRatchetTest.LIGHT_MODULES.resolveSibling("styles").resolve("_realistic.scss");
		assertSourceLacks(realistic, "bootstrap/theme", "$brand-", "$state-", "$btn-", "$navbar-default-",
				"$dropdown-", "$o-toolbar-", "$o-button-dirty-", "lighten(", "darken(");
		String code = ThemeColorRatchetTest.withoutComments(read(realistic));
		Assert.assertFalse("_realistic.scss has a Sass mix()", Pattern.compile("(?<![\\w-])mix\\(").matcher(code).find());
		assertSourceHas(realistic, "var(--o-shadow-color)", "@mixin o-realistic-raised()", "@mixin o-realistic-body()",
				"@function o-realistic-light-pair($fg, $bg)", "\".btn-primary\": (\"btn-primary\", true)",
				"\".o_button_dirty\": (\"btn-dirty\", true)", "@mixin btn-styles($color: #555)");
	}

	/**
	 * The elements that client themes coloured with own rules read tokens
	 * with a reference default, so the light look does not change: the
	 * navigation bar of the login page from 768px (:where() keeps the
	 * specificity of the navbar rules), the bar under the active site or tab
	 * (an inset shadow, width 0 by default) and the top border of the footer
	 * (width 0 by default).
	 */
	@Test
	public void loginNavbarTabIndicatorAndFooterBorderTokens() {
		String css = CUSTOM_PROPERTY.matcher(read(PaletteTest.LIGHT_THEME_CSS)).replaceAll("");
		Assert.assertEquals("var(--o-navbar-bg)", tokens.get("login-navbar-bg"));
		Assert.assertEquals("var(--o-navbar-link)", tokens.get("login-navbar-link"));
		Assert.assertEquals("var(--o-navbar-border)", tokens.get("login-navbar-border"));
		Assert.assertEquals("var(--o-primary)", tokens.get("navbar-tab-indicator"));
		Assert.assertEquals("var(--o-border-subtle)", tokens.get("footer-border"));
		Assert.assertTrue("The login navbar rules need @media (min-width: 768px) and :where()",
				css.contains("@media(min-width: 768px){:where(#o_body.o_dmz) .o_navbar{background-color:var(--o-login-navbar-bg);border-bottom:1px solid var(--o-login-navbar-border)}"
						+ ":where(#o_body.o_dmz) .o_navbar-nav>li>a{color:var(--o-login-navbar-link)}}"));
		assertRuleReads(css, ".o_navbar_tabs>li.active>a", "box-shadow:inset 0 0 0 var(--o-navbar-tab-indicator)");
		assertRuleReads(css, "#o_navbar_more>li>a:hover", "box-shadow:inset 0 0 0 var(--o-navbar-tab-indicator)");
		assertRuleReads(css, "#o_navbar_more .dropdown-menu .o_dropdown_tab.active>a", "box-shadow:inset 0 0 0 var(--o-navbar-tab-indicator)");
		assertRuleReads(css, "#o_footer_wrapper", "border-top:0 solid var(--o-footer-border)");
		assertContrast("login-navbar-link", "login-navbar-bg", 4.5);
		assertContrast("navbar-tab-indicator", "navbar-bg", 3.0);
		assertContrast("navbar-tab-indicator", "bg-raised", 3.0);
	}

	/**
	 * The primary button in the navbar (login button of the guest navbar and
	 * of the navbar of the login page): the navbar items have the inset ring
	 * in the focus colour, which has too little contrast on the primary fill.
	 * The button has the ring in the text colour of the fill, inside the edge,
	 * 3:1 on the fill, on its hover and on its pressed fill. The button reads
	 * the tokens navbar-login-*; in light they follow the primary button.
	 */
	@Test
	public void navbarPrimaryButtonFocusRing() {
		String css = CUSTOM_PROPERTY.matcher(read(PaletteTest.LIGHT_THEME_CSS)).replaceAll("");
		String button = ".o_navbar .o_navbar_button .btn.btn-primary:focus:focus-visible";
		assertRuleReads(css, button, "outline:2px solid currentColor");
		assertRuleReads(css, button, "outline-offset:-4px");
		assertRuleReads(css, ".o_navbar .o_navbar_button .btn.btn-primary", "color:var(--o-navbar-login-fg)");
		assertRuleReads(css, ".o_navbar .o_navbar_button .btn.btn-primary", "background-color:var(--o-navbar-login-bg)");
		for (String fill : List.of("navbar-login-bg", "navbar-login-bg-hover", "navbar-login-bg-active")) {
			assertContrast("navbar-login-fg", fill, 3.0);
		}
		// light: the login button has the colours of the primary button
		for (String token : List.of("bg", "fg", "border", "bg-hover", "bg-active", "border-hover", "border-focus")) {
			Assert.assertEquals("--o-navbar-login-" + token, "var(--o-btn-primary-" + token + ")", tokens.get("navbar-login-" + token));
		}
	}

	/**
	 * A primary button and the help button in the toolbar (the active segment
	 * of the segmented view is a primary button) keep the text colour of their
	 * fill at rest, under the pointer, with the focus, pressed and disabled.
	 * The link rules of the toolbar have the same or a lower specificity and
	 * come first; an active primary button keeps a primary fill.
	 */
	@Test
	public void toolbarPrimaryButtonKeepsItsTextColour() {
		String css = CUSTOM_PROPERTY.matcher(read(PaletteTest.LIGHT_THEME_CSS)).replaceAll("");
		String tools = ".o_toolbar .o_tools_container ";
		for (String state : List.of("", ":hover", ":focus", ":active")) {
			assertRuleReads(css, tools + "a.btn-primary" + state, "color:var(--o-btn-primary-fg)");
			assertRuleReads(css, tools + "a.o_chelp" + state, "color:var(--o-chelp-fg)");
		}
		assertRuleReads(css, tools + "a.btn-primary.o_disabled", "color:var(--o-btn-primary-fg) !important");
		assertRuleReads(css, tools + "a.btn-primary.o_disabled:hover", "color:var(--o-btn-primary-fg) !important");
		assertRuleReads(css, tools + "a.btn-primary.active", "background-color:var(--o-btn-primary-bg-hover)");
		assertRuleReads(css, tools + "a.btn-primary.active:hover", "background-color:var(--o-btn-primary-bg-active)");
		// same specificity as the hover rule of the toolbar links: the order decides
		int linkHover = css.indexOf(tools + "a:not(.o_labeled,.o_labeled_light):hover{");
		int primaryHover = css.indexOf(tools + "a.btn-primary:hover");
		Assert.assertTrue("No hover rule of the toolbar links", linkHover >= 0);
		Assert.assertTrue("The primary button rule comes before the hover rule of the toolbar links", primaryHover > linkHover);
		Assert.assertFalse("A toolbar rule reads white for the primary button", css.contains(tools + "a.btn-primary," + tools + "a.o_chelp{color:var(--o-white)}"));
		for (String fill : List.of("btn-primary-bg", "btn-primary-bg-hover", "btn-primary-bg-active")) {
			assertContrast("btn-primary-fg", fill, 4.5);
		}
		assertContrast("chelp-fg", "chelp-bg", 4.5);
		assertContrast("chelp-fg", "chelp-bg-hover", 4.5);
	}

	/**
	 * Selectus: the links of an on-hold row without a decision have the
	 * on-hold colour. The rule has the selector chain of the table link rule
	 * plus the row class, so it wins against the table link rule (all cells
	 * of the application list are links), as the link rule of the decision
	 * rows does. The links of a decision row have the text colour and an
	 * underline.
	 */
	@Test
	public void selectusOnHoldRowLinksHaveTheOnHoldColour() {
		String css = CUSTOM_PROPERTY.matcher(read(PaletteTest.LIGHT_THEME_CSS)).replaceAll("");
		String tableLink = null;
		Matcher m = RULE.matcher(css);
		while (m.find() && tableLink == null) {
			for (String part : m.group(1).split(",")) {
				if (part.trim().startsWith(".o_table_wrapper td a:not(.btn)") && m.group(2).contains("color:var(--o-table-link)")) {
					tableLink = part.trim();
				}
			}
		}
		Assert.assertNotNull("No table link rule", tableLink);
		String links = tableLink.substring(".o_table_wrapper td ".length());
		for (String table : List.of("table.o_table", "table.table")) {
			assertRuleReads(css, ".o_table_wrapper " + table + " tbody tr.fx_r_onhold>td " + links, "color:var(--o-selectus-onhold)");
			for (String decision : List.of("a", "b", "c")) {
				String row = ".o_table_wrapper " + table + " tbody tr.fx_r_" + decision + "_decision>td " + links;
				assertRuleReads(css, row, "color:var(--o-text)");
				assertRuleReads(css, row, "text-decoration:underline");
			}
		}
		for (String row : List.of("bg", "table-striped-bg", "table-hover-bg")) {
			assertContrast("selectus-onhold", row, 4.5);
		}
	}

	/**
	 * Selectus: a row on hold with a decision keeps the decision fill. Its
	 * text and its links read selectus-onhold-decision (neutral 700), which
	 * keeps 4.5:1 on the six decision fills; the links keep the underline
	 * of the decision rows. The links rule has the chain of the table link
	 * rule, so buttons, the practice play link, uncoloured links and the row
	 * details stay excluded. Every on-hold row shows its cells in italic as
	 * the second cue; the buttons keep the upright font.
	 */
	@Test
	public void selectusOnHoldRowsWithDecisionReadTheOnHoldDecisionColourInItalic() {
		String css = CUSTOM_PROPERTY.matcher(read(PaletteTest.LIGHT_THEME_CSS)).replaceAll("");
		Assert.assertEquals("selectus-onhold-decision", "var(--o-neutral-700)", tokens.get("selectus-onhold-decision"));
		String links = "a:not(.btn):not(.o_practice_play):not(.o_link_uncolored):not(.o_table_row_details_container a)";
		for (String table : List.of("table.o_table", "table.table")) {
			for (String decision : List.of("a", "b", "c")) {
				String row = table + " tr.fx_r_onhold.fx_r_" + decision + "_decision";
				for (String cell : List.of(" td", " td span", " td a")) {
					assertRuleReads(css, row + cell, "color:var(--o-selectus-onhold-decision)");
				}
				assertRuleReads(css, ".o_table_wrapper " + table + " tbody tr.fx_r_onhold.fx_r_" + decision + "_decision>td " + links,
						"color:var(--o-selectus-onhold-decision)");
				assertRuleReads(css, ".o_table_wrapper " + table + " tbody tr.fx_r_" + decision + "_decision>td",
						"background-color:var(--o-selectus-decision-" + decision + "-bg) !important");
				assertRuleReads(css, ".o_table_wrapper " + table + " tbody tr.fx_r_" + decision + "_decision>td " + links,
						"text-decoration:underline");
			}
			assertRuleReads(css, table + " tr.fx_r_onhold td", "font-style:italic");
			assertRuleReads(css, table + " tr.fx_r_onhold td .btn", "font-style:normal");
		}
		for (String decision : List.of("a", "b", "c")) {
			assertContrast("selectus-onhold-decision", "selectus-decision-" + decision + "-bg", 4.5);
			assertContrast("selectus-onhold-decision", "selectus-decision-" + decision + "-hover-bg", 4.5);
		}
	}

	/**
	 * The fills of a selection have the hue of the focus colour, in a light
	 * version (o-scale-tint()): level 1, step $o-selection-step (100), for the
	 * selected row and the opened details row; level 2, step
	 * $o-selection-hover-step (200), for a row under the pointer. In light
	 * and openolat the focus colour is a step of the brand ramp: the base
	 * colours read the brand ramp. The table tokens are own customisation
	 * points that follow the two semantic tokens. Under the pointer level 2
	 * wins over level 1: on a selected row, on an opened row and its details
	 * row, on their sticky columns and on a selected video category. Text and
	 * table links keep 4.5:1 on both levels.
	 */
	@Test
	public void selectionFillsFollowTheFocusHue() {
		Path openolatCss = Path.of("src/main/webapp/static/themes/openolat/theme.css");
		for (Map<String, String> theme : List.of(tokens, parseTokens(read(openolatCss)))) {
			Assert.assertEquals("var(--o-theme1-200)", theme.get("base-bg-hover"));
			Assert.assertEquals("var(--o-theme1-100)", theme.get("base-bg-selected"));
			Assert.assertEquals("var(--o-bg-hover)", theme.get("table-hover-bg"));
			Assert.assertEquals("var(--o-bg-selected)", theme.get("table-row-selected-bg"));
			Assert.assertEquals("var(--o-bg-selected)", theme.get("table-details-bg"));
			Assert.assertEquals("var(--o-bg-hover)", theme.get("table-details-bg-hover"));
		}
		Assert.assertEquals("#C8DFFF", resolve("bg-hover"));
		Assert.assertEquals("#E1EDFF", resolve("bg-selected"));
		// the Sass defaults: one function, the scale of the focus colour, the
		// two steps in one place
		Path light = Path.of("src/main/webapp/static/themes/light");
		assertSourceHas(light.resolve("_config.scss"),
				"$o-selection-step				: 100 !default;",
				"$o-selection-hover-step			: 200 !default;",
				"$table-bg-hover					: o-scale-tint($o-focus-color, $o-selection-hover-step) !default;",
				"$table-bg-active				: o-scale-tint($o-focus-color, $o-selection-step) !default;",
				"$o-table-row-selected-color     : $table-bg-active !default;",
				"$o-table-details-bg							: $table-bg-active !default;");
		assertSourceHas(light.resolve("modules/_tokens.scss"),
				"\"table-row-selected-bg\":                       ($o-table-row-selected-color, (\"bg-selected\",)),",
				"\"table-details-bg\":                           ($o-table-details-bg, (\"bg-selected\",)),",
				"\"table-details-bg-hover\":                     (o-table-details-bg-hover($o-table-details-bg), (\"bg-hover\",)),");
		// the rows under the pointer read the table token
		String css = CUSTOM_PROPERTY.matcher(read(PaletteTest.LIGHT_THEME_CSS)).replaceAll("");
		assertRuleReads(css, ".table-hover>tbody>tr:hover", "background-color:var(--o-table-hover-bg)");
		assertRuleReads(css, ".o_table_wrapper .table-hover>tbody>tr:not(.o_table_row_expanded):hover .o_col_sticky_right",
				"background-color:var(--o-table-hover-bg)");
		// level 2 wins over level 1: the selected row and its details row (the
		// selector lists hold commas: the compiled text is checked)
		Assert.assertTrue("No hover rule of the selected row", css.contains(
				".o_table_wrapper .table-hover>tbody>tr.o_row_selected:where(:hover,:has(+.o_table_row_details:hover))>td,"
				+ ".o_table_wrapper .table-hover>tbody>tr.o_row_selected:where(:hover)+.o_table_row_details>td,"
				+ ".o_table_wrapper .table-hover>tbody>tr.o_row_selected+.o_table_row_details:where(:hover)>td"
				+ "{background-color:var(--o-table-hover-bg) !important}"));
		assertRuleReads(css, ".o_table_wrapper .o_row_selected+.o_table_row_details>td",
				"background-color:var(--o-table-row-selected-bg) !important");
		// the opened row and its details row, and their sticky columns
		assertRuleReads(css, ".o_table_wrapper .table-striped>tbody>tr.o_table_row_expanded:hover>td",
				"background-color:var(--o-table-details-bg-hover)");
		assertRuleReads(css, ".o_table_wrapper .table-striped>tbody>tr.o_table_row_details:hover>td",
				"background-color:var(--o-table-details-bg-hover)");
		for (String side : List.of("right", "left")) {
			Assert.assertTrue("No hover rule of the sticky column " + side + " of an opened row", css.contains(
					".o_table_wrapper .o_scrollable_" + side + " .table-striped>tbody>tr:nth-child(odd) .o_col_sticky_" + side
					+ ":is(.o_table_row_expanded:hover *,.o_table_row_expanded:where(:has(+.o_table_row_details:hover)) *)"
					+ "{background:var(--o-table-details-bg-hover)}"));
		}
		// the video category: the hover rule comes after the selected rule
		String category = ".o_video_segment_category_selection .o_categories .o_category_list .o_category_list_item";
		int selected = css.indexOf(category + ".o_selected{");
		int hover = css.indexOf(category + ":hover{");
		Assert.assertTrue("No rule of the selected or the hovered video category", selected >= 0 && hover >= 0);
		Assert.assertTrue("The hover of a video category comes before the selected one", hover > selected);
		// the striped row and the two levels differ
		Assert.assertNotEquals(resolve("table-striped-bg"), resolve("table-row-selected-bg"));
		Assert.assertNotEquals(resolve("table-row-selected-bg"), resolve("table-hover-bg"));
		Assert.assertNotEquals(resolve("table-details-bg"), resolve("table-details-bg-hover"));
		// text and table links on the two levels
		for (String fill : List.of("bg-hover", "bg-selected", "table-hover-bg", "table-row-selected-bg", "table-details-bg",
				"table-details-bg-hover")) {
			assertContrast("text", fill, 4.5);
			assertContrast("table-link", fill, 4.5);
		}
		assertContrast("table-striped-link", "table-hover-bg", 4.5);
		// light: the table link is one step darker than the link (theme1-600)
		Assert.assertEquals("var(--o-theme1-600)", tokens.get("table-link"));
		// a resting fill never reads a hover token: own tokens with the light
		// grey of the neutral scale
		for (String token : List.of("video-category-edit-bg", "portfolio-publication-nested-bg")) {
			Assert.assertEquals(token, "var(--o-neutral-50)", tokens.get(token));
		}
		assertRuleReads(css, ".o_video_segment_category_edit a", "background-color:var(--o-video-category-edit-bg)");
		assertRuleReads(css, ".o_portfolio_publication ul li li li", "background:var(--o-portfolio-publication-nested-bg)");
		// the rows of the video timeline have the fills of a striped table, the
		// active channel the selected fill (it comes after the stripes)
		assertRuleReads(css, ".o_video_timeline_box", "background-color:var(--o-bg)");
		assertRuleReads(css, ".o_video_timeline_box:nth-child(odd)", "background-color:var(--o-table-striped-bg)");
		assertRuleReads(css, ".o_video_timeline_box.o_video_active", "background-color:var(--o-table-row-selected-bg)");
		Assert.assertTrue("The active channel comes before the stripes",
				css.indexOf(".o_video_timeline_box.o_video_active{") > css.indexOf(".o_video_timeline_box:nth-child(odd){"));
		assertContrast("text", "table-striped-bg", 4.5);
		assertContrast("text", "table-row-selected-bg", 4.5);
		assertContrast("btn-default-fg", "video-category-edit-bg", 4.5);
		assertContrast("text", "portfolio-publication-nested-bg", 4.5);
		// the hover tokens are read only by rules of a pointer state
		Matcher rule = RULE.matcher(css);
		while (rule.find()) {
			if (rule.group(2).matches("(?s).*var\\(--o-(bg-hover|table-hover-bg|table-details-bg-hover)\\).*")) {
				Assert.assertTrue("A resting fill reads a hover token: " + rule.group(1), rule.group(1).contains(":hover"));
			}
		}
	}

	/**
	 * Selectus: the marks of the ratings A, B and C (overview and own rating)
	 * read their mark tokens, references to the solid fill of the state
	 * success, warning and danger; the hidden letter has the colour of the
	 * fill. The fills have less than 3:1 on the rows: the border of the mark
	 * keeps 3:1 on a white, a striped and a hovered row and on the six fills
	 * of the decision rows. The rating buttons keep their own fills.
	 */
	@Test
	public void selectusRatingMarksReadStateFills() {
		String css = CUSTOM_PROPERTY.matcher(read(PaletteTest.LIGHT_THEME_CSS)).replaceAll("");
		Map<String, String> marks = Map.of("1", "c", "2", "b", "3", "a");
		Map<String, String> states = Map.of("a", "success", "b", "warning", "c", "danger");
		for (Map.Entry<String, String> mark : marks.entrySet()) {
			String token = "selectus-rating-" + mark.getValue() + "-mark";
			Assert.assertEquals(token, "var(--o-state-" + states.get(mark.getValue()) + "-bg)", tokens.get(token));
			for (String element : List.of("div", "span")) {
				String selector = element + ".f_rating_" + mark.getKey();
				assertRuleReads(css, selector, "background-color:var(--o-" + token + ")");
				assertRuleReads(css, selector, "color:var(--o-" + token + ")");
			}
		}
		Assert.assertEquals("#1F8320", resolve("selectus-rating-a-mark"));
		Assert.assertEquals("#F6C600", resolve("selectus-rating-b-mark"));
		Assert.assertEquals("#E2002D", resolve("selectus-rating-c-mark"));
		// the rating buttons keep their fills
		Assert.assertEquals("var(--o-green-500)", tokens.get("selectus-rating-a-bg"));
		Assert.assertEquals("var(--o-yellow-400)", tokens.get("selectus-rating-b-bg"));
		// the border carries the shape
		assertRuleReads(css, "div.f_rating_overview", "border:1px solid var(--o-btn-default-border)");
		assertRuleReads(css, "span.f_rating_overview", "border:1px solid var(--o-btn-default-border)");
		for (String row : List.of("bg", "table-striped-bg", "table-hover-bg", "selectus-decision-a-bg", "selectus-decision-a-hover-bg",
				"selectus-decision-b-bg", "selectus-decision-b-hover-bg", "selectus-decision-c-bg", "selectus-decision-c-hover-bg")) {
			assertContrast("btn-default-border", row, 3.0);
		}
	}

	/**
	 * Selectus: the bars of the ratings A, B and C in the statistics have the
	 * solid fills of the rating marks, without the gradient of the rating
	 * buttons, and the text of the state on the fill, 4.5:1. Bar A has an own
	 * rule, so that it follows an override of mark A. The abstention bar
	 * keeps the gradient of the abstention button.
	 */
	@Test
	public void selectusRatingBarsHaveTheFillsOfTheMarks() {
		String css = CUSTOM_PROPERTY.matcher(read(PaletteTest.LIGHT_THEME_CSS)).replaceAll("");
		Map<String, String> states = Map.of("a", "success", "b", "warning", "c", "danger");
		for (Map.Entry<String, String> rating : states.entrySet()) {
			String bar = ".progress-bar.o_bar_rating_" + rating.getKey();
			String mark = "selectus-rating-" + rating.getKey() + "-mark";
			assertRuleReads(css, bar, "background-color:var(--o-" + mark + ")");
			assertRuleReads(css, bar, "color:var(--o-" + mark + "-fg)");
			Assert.assertEquals(mark + "-fg", "var(--o-state-" + rating.getValue() + "-fg)", tokens.get(mark + "-fg"));
			assertContrast(mark + "-fg", mark, 4.5);
			Matcher m = RULE.matcher(css);
			while (m.find()) {
				if (m.group(1).trim().equals(bar)) {
					Assert.assertFalse(bar + " has a gradient: " + m.group(2), m.group(2).contains("gradient"));
				}
			}
		}
		Assert.assertEquals("#FFFFFF", resolve("selectus-rating-a-mark-fg"));
		Assert.assertEquals("#301E06", resolve("selectus-rating-b-mark-fg"));
		Assert.assertEquals("#FFFFFF", resolve("selectus-rating-c-mark-fg"));
		assertRuleReads(css, ".progress-bar.o_bar_rating_abstain",
				"background-image:linear-gradient(to bottom, var(--o-selectus-rating-abstain-bg) 0%, var(--o-selectus-rating-abstain-bg-hover) 100%)");
	}

	/**
	 * The coloured tables of the content editor and the HTML editor: one
	 * table style b_{key} per swatch key, the 14 hues and the theme colours
	 * (chooser set M). The fills read the swatch roles of the key: the cells
	 * the soft fill (step 50), the header cells the light fill (step 200,
	 * yellow step 100), the even rows white with lines in the soft border
	 * (step 100), the cell lines white. The stored styles b_blue, b_green, b_yellow and b_red are the
	 * tables of their hue. The stored style b_gray is the table of the hue
	 * slate, the pure hue gray is b_gray_hue. The text of the content keeps 4.5:1 on
	 * the three fills, the link on the cell fill and on white. The old table
	 * tokens of the message boxes and of the blue table are gone.
	 */
	@Test
	public void tableStylesReadHueSteps() {
		String css = CUSTOM_PROPERTY.matcher(read(PaletteTest.LIGHT_THEME_CSS)).replaceAll("");
		List<String> keys = new ArrayList<>(ColorService.HUES);
		keys.add("theme1");
		for (String key : keys) {
			// the pure hue gray is b_gray_hue, the stored style b_gray is the slate table
			String selector = "table.b_" + ("gray".equals(key) ? "gray_hue" : key);
			// the light fill of yellow is step 100 (its solid fill is step 300)
			String header = key + ("yellow".equals(key) ? "-100" : "-200");
			assertRuleReads(css, selector + " td", "background:var(--o-" + key + "-50)");
			assertRuleReads(css, selector + " td", "border:1px solid var(--o-white)");
			assertRuleReads(css, selector + " thead td", "background:var(--o-" + header + ")");
			assertRuleReads(css, selector + " th", "background:var(--o-" + header + ")");
			assertRuleReads(css, selector + " tbody tr:nth-child(even) td", "background:var(--o-white)");
			assertRuleReads(css, selector + " tbody tr:nth-child(even) td", "border:1px solid var(--o-" + key + "-100)");
			assertRuleReads(css, selector + ".b_no_stripes tbody tr:nth-child(even) td", "background:var(--o-" + key + "-50)");
			for (String fill : List.of(key + "-50", header, "white")) {
				assertContrast("content-text", fill, 4.5);
			}
			assertContrast("content-link", key + "-50", 4.5);
			assertContrast("content-link", "white", 4.5);
		}
		assertRuleReads(css, "table.b_gray td", "background:var(--o-slate-50)");
		assertRuleReads(css, "table.b_gray thead td", "background:var(--o-slate-200)");
		for (String old : List.of("content-table-blue-bg", "content-table-blue-header-bg", "box-note-table-header-bg",
				"box-success-table-stripe-bg", "box-warning-table-border", "box-error-table-header-bg")) {
			Assert.assertNull("Old table token " + old, tokens.get(old));
		}
	}

	/**
	 * A fill token (-bg) is never the colour of a text or an icon. A glyph
	 * with a status meaning reads the text role of its state, a letter reads
	 * a text token. Allowed: a legend mark that is a sample of its chart fill,
	 * an icon that is a sample of the fill of the progress bar it refers to
	 * and a text that is hidden on purpose by the colour of its own fill
	 * (each with a comment in the module). The pale success and failure glyph
	 * behind the number of the radial progress reads its own glyph token.
	 */
	@Test
	public void fillTokensAreNoTextColours() throws IOException {
		Map<String, Integer> allowed = Map.of("_curriculum.scss", 2, "_practice.scss", 6, "_selectus.scss", 2, "_video.scss", 2);
		Pattern fillAsColor = Pattern.compile("(?<![-\\w])color\\s*:\\s*[^;{}]*var\\(--o-[a-z0-9-]*-bg(?:-hover|-active)?\\)");
		Map<String, Integer> found = new TreeMap<>();
		try (Stream<Path> modules = Files.list(ThemeColorRatchetTest.LIGHT_MODULES)) {
			for (Path module : modules.filter(p -> p.toString().endsWith(".scss")).toList()) {
				Matcher m = fillAsColor.matcher(ThemeColorRatchetTest.withoutComments(read(module)));
				while (m.find()) {
					found.merge(module.getFileName().toString(), 1, Integer::sum);
				}
			}
		}
		Assert.assertEquals("Rules that read a fill token as text colour", new TreeMap<>(allowed), found);
		String css = CUSTOM_PROPERTY.matcher(read(PaletteTest.LIGHT_THEME_CSS)).replaceAll("");
		assertRuleReads(css, ".o_icon_progress_success", "color:var(--o-state-success-text)");
		assertRuleReads(css, ".o_icon_progress_danger", "color:var(--o-state-danger-text)");
		assertRuleReads(css, "span.o_rating_expl[title=A]", "color:var(--o-selectus-rating-a-text)");
		// the hue of the fill, the step with 4.5:1 on the rows: green-600
		Assert.assertEquals("var(--o-green-600)", tokens.get("selectus-rating-a-text"));
		for (String row : List.of("bg", "table-striped-bg", "table-hover-bg")) {
			assertContrast("selectus-rating-a-text", row, 4.5);
		}
		// the glyph behind the number of the radial progress: own tokens with a
		// reference to the soft fill of the state, also in the print sheet
		assertRuleReads(css, ".o_progress_passed .percentage", "color:var(--o-radial-progress-passed-glyph)");
		assertRuleReads(css, ".o_progress_failed .percentage", "color:var(--o-radial-progress-failed-glyph)");
		Assert.assertEquals("var(--o-state-success-soft-bg)", tokens.get("radial-progress-passed-glyph"));
		Assert.assertEquals("var(--o-state-danger-soft-bg)", tokens.get("radial-progress-failed-glyph"));
		assertSourceHas(ThemeColorRatchetTest.LIGHT_MODULES.resolve("_print.scss"),
				"color: var(--o-radial-progress-failed-glyph) !important;", "color: var(--o-radial-progress-passed-glyph) !important;");
		assertContrast("radial-progress-number", "radial-progress-passed-glyph", 4.5);
		assertContrast("radial-progress-number", "radial-progress-failed-glyph", 4.5);
	}

	/**
	 * Every contrast pair of two tokens in _tokens.scss passes with the
	 * defaults of the light theme, the pairs of the text tokens included.
	 */
	@Test
	public void contrastPairsPassInLight() {
		String scss = ThemeColorRatchetTest.withoutComments(read(ThemeColorRatchetTest.LIGHT_MODULES.resolve("_tokens.scss")));
		Matcher m = Pattern.compile("\\(\"([a-z0-9-]+)\", \"([a-z0-9-]+)\", ([0-9.]+)\\)").matcher(scss);
		int count = 0;
		while (m.find()) {
			assertContrast(m.group(1), m.group(2), Double.parseDouble(m.group(3)));
			count++;
		}
		Assert.assertTrue("Too few pairs found: " + count, count > 400);
	}

	private static void assertRuleReads(String css, String selector, String declaration) {
		Matcher m = RULE.matcher(css);
		while (m.find()) {
			for (String part : m.group(1).split(",")) {
				String normalised = part.trim().replaceAll("\\s+", " ").replaceAll("\\s*>\\s*", ">");
				if (normalised.equals(selector)) {
					String declarations = m.group(2).replaceAll(":\\s*", ":").replaceAll("\\s+", " ");
					for (String d : declarations.split(";")) {
						if (d.trim().equals(declaration)) {
							return;
						}
					}
				}
			}
		}
		Assert.fail("No rule " + selector + " with " + declaration);
	}

	/**
	 * @param scss The SCSS file
	 * @param snippets Text that the file holds outside of a comment
	 */
	private static void assertSourceHas(Path scss, String... snippets) {
		String code = ThemeColorRatchetTest.withoutComments(read(scss));
		for (String snippet : snippets) {
			Assert.assertTrue(scss.getFileName() + " has no " + snippet, code.contains(snippet));
		}
	}

	/**
	 * @param scss The SCSS file
	 * @param snippets Text that the file must not hold outside of a comment
	 */
	private static void assertSourceLacks(Path scss, String... snippets) {
		String code = ThemeColorRatchetTest.withoutComments(read(scss));
		for (String snippet : snippets) {
			Assert.assertFalse(scss.getFileName() + " still has " + snippet, code.contains(snippet));
		}
	}

	private void assertContrast(String fg, String bg, double ratio) {
		String fgHex = resolve(fg);
		String bgHex = resolve(bg);
		double contrast = ColorContrast.contrast(fgHex, bgHex);
		Assert.assertTrue(fg + " " + fgHex + " on " + bg + " " + bgHex + ": " + contrast, contrast >= ratio);
	}

	/**
	 * @param shade The shade token
	 * @param source The token it comes from
	 * @param percent The percentage of darken(), negative for lighten()
	 */
	private void assertShade(String shade, String source, int percent) {
		String shadeValue = tokens.get(shade);
		Assert.assertNotNull(shade, shadeValue);
		String sourceHex = resolve(source);
		Assert.assertTrue(source + ": " + sourceHex, HEX.matcher(sourceHex).matches());
		int[] expected = darken(sourceHex, percent);
		String function = percent < 0 ? "lightened by " + (-percent) : "darkened by " + percent;
		String shadeHex = resolve(shade);
		Assert.assertTrue(shade + ": " + shadeHex, HEX.matcher(shadeHex).matches());
		boolean computed = true;
		// The token value is a rounded colour: 2 of 255 per channel
		for (int i = 0; i < 3; i++) {
			int channel = Integer.parseInt(shadeHex.substring(1 + 2 * i, 3 + 2 * i), 16);
			computed &= Math.abs(channel - expected[i]) <= 2;
		}
		if (computed) {
			return;
		}
		if (BRIGHT_FILLS.containsKey(source) && percent > 0 && (shade.endsWith("-hover") || shade.endsWith("-active"))) {
			// a bright fill goes lighter inside its hue: hover and pressed one step lighter
			// (o-token-shade-lighter() in _tokens.scss)
			String hue = BRIGHT_FILLS.get(source);
			Assert.assertEquals(shade, "var(--o-" + hue + "-" + lighterStep(hue, source) + ")", shadeValue);
			return;
		}
		// the source is a scale step: the shade reads a step of the scale of its
		// source (a palette hue such as gray, the neutral scale or the brand ramp)
		// that differs from the source, on the side of darken() or lighten()
		// (o-token-shade-scale() in _tokens.scss)
		Assert.assertTrue(shade + " " + shadeValue + " is not " + source + " " + sourceHex + " " + function
				+ "%: rgb(" + expected[0] + ", " + expected[1] + ", " + expected[2] + "), and no scale step",
				PALETTE_REFERENCE.matcher(shadeValue).matches());
		Assert.assertNotEquals(shade + " " + shadeValue + " has the colour of " + source, sourceHex, shadeHex);
		boolean darker = ColorContrast.relativeLuminance(shadeHex) < ColorContrast.relativeLuminance(sourceHex);
		Assert.assertEquals(shade + " " + shadeValue + " " + shadeHex + " is on the wrong side of " + source + " " + sourceHex,
				percent > 0, darker);
	}

	/**
	 * The amount of o-contrast-shade-amount() in _functions.scss: the largest
	 * amount from 3% up to max at which the text keeps 4.5:1 on the darker
	 * fill; max when the text fails on the fill itself, and max when no
	 * amount from 3% passes (the text of the state is adjusted).
	 */
	private static int contrastShadeAmount(String fg, String bg, int max) {
		if (ColorContrast.contrast(fg, bg) < 4.5) {
			return max;
		}
		for (int amount = max; amount >= MIN_SHADE_AMOUNT; amount--) {
			int[] rgb = darken(bg, amount);
			if (ColorContrast.contrast(fg, String.format("#%02X%02X%02X", rgb[0], rgb[1], rgb[2])) >= 4.5) {
				return amount;
			}
		}
		return max;
	}

	/**
	 * The fill of a hover or pressed state of o-contrast-shade() in
	 * _functions.scss: darker by the amount, lighter when darken() keeps the
	 * colour.
	 */
	private static String stateShade(String hex, int percent) {
		int[] rgb = darken(hex, percent);
		String shade = String.format("#%02X%02X%02X", rgb[0], rgb[1], rgb[2]);
		if (shade.equals(hex)) {
			rgb = darken(hex, -percent);
			shade = String.format("#%02X%02X%02X", rgb[0], rgb[1], rgb[2]);
		}
		return shade;
	}

	/**
	 * The Sass function darken(): the HSL lightness minus the amount. A
	 * negative amount is the Sass function lighten().
	 *
	 * @param hex The colour as #RRGGBB
	 * @param percent The amount in percent of the lightness scale, negative to lighten
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
		lightness = Math.min(1d, Math.max(0d, lightness - percent / 100d));
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

	private record Shade(String name, String source, int percent) {
		//
	}

	private static List<Shade> stateShades() {
		List<Shade> shades = new ArrayList<>();
		for (String state : List.of("neutral", "success", "warning")) {
			String prefix = "state-" + state + "-";
			shades.add(new Shade(prefix + "bg-hover", prefix + "bg", 10));
			shades.add(new Shade(prefix + "bg-active", prefix + "bg", ACTIVE_PERCENT.getOrDefault("state-" + state, 17)));
			shades.add(new Shade(prefix + "border-hover", prefix + "border", 12));
			shades.add(new Shade(prefix + "border-focus", prefix + "border", 25));
		}
		shades.add(new Shade("state-danger-bg-hover", "state-danger-bg", 10));
		for (String state : List.of("neutral", "success", "warning", "danger")) {
			String prefix = "state-" + state + "-";
			shades.add(new Shade(prefix + "soft-bg-hover", prefix + "soft-bg", 5));
			shades.add(new Shade(prefix + "text-hover", prefix + "text", 10));
		}
		return List.copyOf(shades);
	}

	private static Map<Integer, String> neutralDefaults() {
		Map<Integer, String> steps = new LinkedHashMap<>();
		String[] values = { "0", "#FFFFFF", "25", "#F9FAFB", "50", "#F6F7FA", "100", "#EAECF0", "150", "#E4E7EC",
				"200", "#D9DDE3", "250", "#D0D5DD", "300", "#C6CCD6", "400", "#B1B8C5", "450", "#8B95A7",
				"500", "#657286", "600", "#4E596D", "700", "#3B485B", "750", "#344054", "800", "#2A3649",
				"900", "#182233", "950", "#101828", "1000", "#000000" };
		for (int i = 0; i < values.length; i += 2) {
			steps.put(Integer.valueOf(values[i]), values[i + 1]);
		}
		return steps;
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

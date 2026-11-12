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
 * tokens backdrop, shadow-color or an own element token (decisions D8 to D11),
 * the hover and pressed states read a text token of the state and stay
 * visible, and the selectus progress bars read the tokens of the rating
 * buttons (decisions D12, D13, D15), the selectus abstention bar, marks and
 * explanation texts read the tokens of the rating buttons, the course style
 * border reads a token, a black state fill gets lighter and the navbar text
 * passes (decisions D16 to D20, D23), every control has the same focus
 * ring in the adjusted primary colour (decision D24), the selectus marks
 * and letters keep their contrast on the table rows and read the tokens of
 * the rating buttons (decisions D25 to D28), and the literal colours of the
 * B4 review read semantic, state, palette or own element tokens, except the
 * kept literals and the rules without a reader (decision D29). The framework,
 * assessment and content modules read element tokens fed by their Sass
 * variables, a translucent default included; the shorthand borders are width,
 * style and a colour token (slices 16 and 17, decision D31).
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
	private static final Pattern HEX = Pattern.compile("#[0-9A-F]{6}");
	private static final Pattern COLOR = Pattern.compile("#[0-9A-Fa-f]{3,8}|rgba?\\([^)]*\\)|transparent");
	// A translucent token colour (decision M3): the token mixed with transparent
	private static final Pattern COLOR_MIX = Pattern.compile("color-mix\\(in srgb, var\\(--o-([a-z0-9-]+)\\) [0-9.]+%, transparent\\)");

	private static final List<String> SHADE_BUTTONS = List.of("btn-primary", "btn-default", "btn-danger", "btn-dirty",
			"coursesite-start", "coursesite-book", "btn-primary-light", "btn-danger-light", "table-filter",
			"table-filter-active");
	// The active fill of a bright fill with dark text is 16% darker, else 17%
	// (o-token-active-percent() in _tokens.scss, decision D4 of OO-10043)
	private static final Map<String, Integer> ACTIVE_PERCENT = Map.of("btn-dirty", 16, "coursesite-book", 16,
			"state-warning", 16);
	// The buttons whose pressed fill is darker by the amount of Bootstrap at most:
	// the text keeps 4.5:1 (o-contrast-shade-amount() in _functions.scss, decision D9)
	private static final List<String> CONTRAST_SHADE_BUTTONS = List.of("btn-primary-light", "btn-danger-light",
			"table-filter", "table-filter-active");
	// The smallest visible amount of a hover or pressed fill: below, the fill
	// keeps the amount of Bootstrap and the text of the state is adjusted
	// (o-contrast-shade-amount(), o-contrast-shade-text(), decision D13)
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
			// the remaining colour maths (OO-10043 slice 8)
			new Shade("box-info-table-border", "box-info-bg", -5), new Shade("box-info-table-stripe-bg", "box-info-bg", -5),
			new Shade("box-info-table-header-bg", "box-info-bg", 10),
			new Shade("box-note-table-border", "box-note-bg", -5), new Shade("box-note-table-stripe-bg", "box-note-bg", -5),
			new Shade("box-note-table-header-bg", "box-note-bg", 10),
			new Shade("box-success-table-border", "box-success-bg", -5),
			new Shade("box-success-table-stripe-bg", "box-success-bg", -5),
			new Shade("box-success-table-header-bg", "box-success-bg", 10),
			new Shade("box-warning-table-border", "box-warning-bg", -5),
			new Shade("box-warning-table-stripe-bg", "box-warning-bg", -5),
			new Shade("box-warning-table-header-bg", "box-warning-bg", 10),
			new Shade("box-error-table-border", "box-error-bg", -5), new Shade("box-error-table-stripe-bg", "box-error-bg", -5),
			new Shade("box-error-table-header-bg", "box-error-bg", 10),
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
			new Shade("table-details-bg-hover", "table-details-bg", 10),
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
	// rules read the state tokens (OO-10043 slice 6)
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
	// The element tokens of the links that a rule of the compiled theme reads
	private static final List<String> LINK_RULE_TOKENS = List.of("toolbar-link", "toolbar-link-hover",
			"toolbar-link-active", "toolbar-nav-link", "toolbar-nav-link-hover", "tree-link-active-parent",
			"tree-link-active-parent-hover", "segments-tab-link", "selectus-review-comment-link");
	// The tokens of the Bootstrap colour variables and greys that the modules read, with the light value
	private static final Map<String, String> BOOTSTRAP_COMPONENT_TOKENS = Map.ofEntries(
			Map.entry("blockquote-border", "#EEEEEE"), Map.entry("code-block-bg", "#F5F5F5"),
			Map.entry("code-block-fg", "#333333"), Map.entry("code-block-border", "rgba(0, 0, 0, 0.1)"),
			Map.entry("dropdown-link", "#333333"), Map.entry("dropdown-link-hover", "#262626"),
			Map.entry("dropdown-link-hover-bg", "#F5F5F5"), Map.entry("dropdown-divider", "#E5E5E5"),
			Map.entry("input-bg", "#FFFFFF"), Map.entry("input-fg", "#555555"),
			Map.entry("input-disabled-bg", "#F9FAFB"), Map.entry("input-placeholder", "#999999"),
			Map.entry("legend", "#333333"), Map.entry("legend-border", "#E5E5E5"),
			Map.entry("modal-content-border", "#E4E7EC"), Map.entry("modal-header-border", "#E5E5E5"),
			Map.entry("popover-bg", "#FFFFFF"), Map.entry("popover-border", "rgba(0, 0, 0, 0.2)"),
			Map.entry("panel-bg", "#FFFFFF"), Map.entry("progress-bar-fg", "#FFFFFF"),
			Map.entry("thumbnail-border", "#DDDDDD"), Map.entry("table-border", "#DDDDDD"),
			Map.entry("table-striped-bg", "#F9FAFB"), Map.entry("table-hover-bg", "#F5F5F5"),
			Map.entry("alert-close", "#777777"), Map.entry("alert-close-hover", "#555555"),
			Map.entry("message-table-border", "#333333"), Map.entry("chart-help-line", "#777777"),
			Map.entry("content-table-header-bg", "#EEEEEE"), Map.entry("editor-media-unavailable", "#EEEEEE"),
			Map.entry("room-status-inactive", "#777777"), Map.entry("selectus-review-comments-bg", "#FFFFFF"),
			Map.entry("selectus-review-comments-border", "#EAECF0"), Map.entry("qti-gaptext-alternative-bg", "#EEEEEE"),
			Map.entry("qti-gaptext-alternative-fg", "#555555"), Map.entry("qti-help", "#777777"),
			Map.entry("video-slider-bg", "#555555"), Map.entry("videotask-marker-assign-bg", "#333333"));
	private static final Pattern RULE = Pattern.compile("([^{}]+)\\{([^{}]*)\\}");
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
	 * of the light theme. The navbar text and the navbar tools pass since
	 * decision D23 (4.48:1 before). The hover, pressed and off states have the
	 * contrast of the normal state (decision D9). The focus ring of the navbar
	 * passes on the navbar (decision D24). No pair of the light theme fails.
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
		// the text on the active fill of the bright buttons (16% darker, decision D4)
		assertContrast("state-warning-fg", "state-warning-bg-active", 4.5);
		assertContrast("btn-dirty-fg", "btn-dirty-bg-active", 4.5);
		assertContrast("coursesite-book-fg", "coursesite-book-bg-active", 4.5);
		// the contrast defects of decision D4: the selected tag, the QTI error
		// box, the chat and the tab close icons on the navbar (3:1, non-text)
		for (String state : List.of("", "-hover", "-active")) {
			assertContrast("tag-selected-fg", "tag-selected-bg" + state, 4.5);
		}
		assertContrast("qti-error-fg", "qti-error-bg", 4.5);
		for (String icon : List.of("im-newmessage", "im-newmessage-hover", "navbar-tab-close", "navbar-tab-close-hover")) {
			assertContrast(icon, "navbar-bg", 3.0);
		}
		// the other buttons (OO-10043 slice 7)
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
		// (decisions D9 and D13)
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
		// the text of a selected competence on its fill, under the pointer and pressed (decision D8)
		for (String state : List.of("", "-hover", "-active")) {
			assertContrast("competence-selected", "competence-bg" + state, 4.5);
		}
		// the close icon of a tab in the More dropdown (decision D10)
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
	 * ($table-bg-hover), bg-selected ($table-bg-active), bg-raised
	 * ($dropdown-bg), border-subtle ($hr-border), text-disabled
	 * ($btn-link-disabled-color), backdrop ($modal-backdrop-bg). shadow-color
	 * and scrim have no Sass variable: black and white, the colours of the
	 * Bootstrap shadows and of the light veils of the modules.
	 */
	@Test
	public void newSemanticTokenDefaults() {
		Assert.assertEquals("#F5F5F5", resolve("bg-hover"));
		Assert.assertEquals("#F5F5F5", resolve("bg-selected"));
		Assert.assertEquals("#FFFFFF", resolve("bg-raised"));
		Assert.assertEquals("#EEEEEE", resolve("border-subtle"));
		Assert.assertEquals("#777777", resolve("text-disabled"));
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
				"background-color:var(--o-bg-hover)");
		assertRuleReads(css, "ul.o_dropdown>li>a", "background:var(--o-bg-raised)");
		assertRuleReads(css, ".o_assistance_accordion .o_item", "border-top:1px solid var(--o-border-subtle)");
		assertRuleReads(css, ".o_assistance_accordion .o_item:last-child", "border-bottom:1px solid var(--o-border-subtle)");
	}

	/**
	 * The tokens of the Bootstrap colour variables and of the Bootstrap greys
	 * that the modules read (decision M7 a): each token has the exact light
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
	 * the old rgba() colour (decision M8 a).
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
	 * amount up to 10% and 17% at which the text keeps 4.5:1 (decision D9).
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
	 * made read text-muted or text-disabled (decision M9). A rule that
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
	 * _config.scss read tokens (OO-10043 slice 8): the gradient stops and the
	 * labels read shade tokens, the named variables with an opaque default
	 * read their own token, the translucent defaults mix a token with
	 * transparent, and the two muted texts read text-muted and text-disabled
	 * (decision M9).
	 */
	@Test
	public void remainingColorMathRulesReadTokens() {
		String css = CUSTOM_PROPERTY.matcher(read(PaletteTest.LIGHT_THEME_CSS)).replaceAll("");
		// muted texts (M9)
		assertRuleReads(css, ".o_form_chelp", "color:var(--o-chelp-tooltip)");
		Assert.assertEquals("var(--o-text-muted)", tokens.get("chelp-tooltip"));
		assertRuleReads(css, ".o_toolbar .o_tools_container a.o_disabled", "color:var(--o-toolbar-link-disabled) !important");
		Assert.assertEquals("var(--o-text-disabled)", tokens.get("toolbar-link-disabled"));
		Assert.assertEquals("var(--o-toolbar-link-disabled)", tokens.get("toolbar-nav-link-disabled"));
		// shade tokens and fixed colours
		assertRuleReads(css, ".o_labeled", "background-color:var(--o-labeled-bg)");
		Assert.assertEquals("var(--o-gray-600)", tokens.get("labeled-bg"));
		Assert.assertEquals("var(--o-white)", tokens.get("labeled-light-bg"));
		assertRuleReads(css, "table.b_yellow th", "background:var(--o-box-warning-table-header-bg)");
		assertRuleReads(css, ".o_tag.o_competence.o_tag_clickable:active", "background-color:var(--o-competence-bg-active)");
		Assert.assertEquals("var(--o-white)", tokens.get("competence-bg"));
		assertRuleReads(css, ".o_page_run_code_box", "background-color:var(--o-page-code-box-bg)");
		assertRuleReads(css, ".o_catalog2 .o_catalog_header .o_catalog_header_item:not(.o_header_bg_image)",
				"background:linear-gradient(190deg, var(--o-catalog-header-noimage-stop-1) 10%, var(--o-primary) 100%),"
				+ "linear-gradient(40deg, var(--o-catalog-header-noimage-stop-3) 0%, var(--o-catalog-header-noimage-stop-4) 60%)");
		Assert.assertTrue("catalog-header-noimage-stop-4: " + tokens.get("catalog-header-noimage-stop-4"),
				HEX.matcher(tokens.get("catalog-header-noimage-stop-4")).matches());
		// translucent colours
		assertRuleReads(css, ".milkdown table th", "background:color-mix(in srgb, var(--o-milkdown-table-header-bg) 10%, transparent)");
		// since slice 18 the translucent defaults of $o-cn-style-cover-text-bg and
		// $o-lp-pagination-in-progress-bgcolor feed element tokens (decision D31 Q2)
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
	 * $o-legacy-color-warning silences it (OO-10043 slice 8). Static check of
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
		assertRuleReads(css, ".o_icon_warn", "color:var(--o-state-warning-bg)");
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
	 * The Bootstrap state rules read the state tokens (OO-10043 slice 6,
	 * decisions S1 a, S3 a): .btn-success|info|warning the fill, the text on
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
	 * tokens (S1 a, S2 a, S4 a): the border, the fill, the chart colour the
	 * fill of the state, the icons and texts the text of the state, the box
	 * plot box the light fill (palette step 200), the portlet edit mode the
	 * soft roles of the warning state.
	 */
	@Test
	public void messageBoxAndEvaluationTokens() {
		Map<String, String> boxes = Map.of("info", "neutral", "success", "success", "warning", "warning", "error", "danger");
		for (Map.Entry<String, String> box : boxes.entrySet()) {
			Assert.assertEquals("var(--o-state-" + box.getValue() + "-bg)", tokens.get("box-" + box.getKey() + "-border"));
			Assert.assertEquals("var(--o-state-" + box.getValue() + "-text)", tokens.get("box-" + box.getKey() + "-text"));
		}
		Map<String, String> ratings = Map.of("insufficient", "danger", "neutral", "warning", "sufficient", "success");
		Map<String, String> fills = Map.of("insufficient", "var(--o-red-200)", "neutral", "var(--o-yellow-200)", "sufficient", "var(--o-green-200)");
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
		assertRuleReads(css, ".o_warning_with_icon:before", "color:var(--o-box-warning-text)");
		assertRuleReads(css, ".o_error_with_icon:before", "color:var(--o-box-error-text)");
		assertRuleReads(css, ".o-modal-alert-success .modal-header h4:before", "color:var(--o-box-success-text)");
		assertRuleReads(css, ".o_page_content_editor>.o_page_fragment_edit>.o_page_layout.o_alert_box_active.o_alert_box_type_info",
				"border-color:color-mix(in srgb, var(--o-box-info-border) 15%, transparent)");
		assertRuleReads(css, ".o_icon_rubric_neutral", "color:var(--o-evaluation-neutral-text)");
		assertRuleReads(css, ".d3chart .o_rubric_neutral", "fill:var(--o-evaluation-neutral)");
		assertRuleReads(css, "svg.o_bp.o_rubric_insufficient .o_bp_v_b", "fill:var(--o-evaluation-insufficient-fill)");
		assertRuleReads(css, ".o_portlet .o_edit_shim", "background:var(--o-portlet-edit-bg)");
		assertRuleReads(css, ".o_assessmentitem div.o_warning.badResponse", "color:var(--o-qti-bad-response-warning)");
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
		// the selected row reads its token (OO-10043 slice 16), the soft fill of the neutral state
		assertRuleReads(css, ".o_row_selected", "background:var(--o-table-row-selected-bg) !important");
		Assert.assertEquals("var(--o-state-neutral-soft-bg)", tokens.get("table-row-selected-bg"));
		// the right kprim cell reads its token (OO-10043 slice 17), the soft fill of the success state
		assertRuleReads(css, "table.o_qti_item_kprim td.o_qti_item_kprim_input_correct",
				"background-color:var(--o-qti-match-kprim-correct-bg)");
		Assert.assertEquals("var(--o-state-success-soft-bg)", tokens.get("qti-match-kprim-correct-bg"));
	}

	/**
	 * Decisions D1, D3 and D4 of OO-10043: the rating stars read the token
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
		// element reads its own token (decision D11)
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
	 * Decisions D8 to D11 of OO-10043: the text of a selected competence
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
		// D8
		Assert.assertEquals("var(--o-tag-selected-fg)", tokens.get("competence-selected"));
		assertRuleReads(css, ".o_tag.o_competence.o_tag_selected", "color:var(--o-competence-selected)");
		// D9: the toggle text is the muted text adjusted for the track, the rating C fill is a darker red
		Assert.assertEquals("var(--o-base-toggle-text)", tokens.get("toggle-text"));
		assertRuleReads(css, ".o_button_toggle span", "color:var(--o-toggle-text)");
		Assert.assertEquals("var(--o-white)", tokens.get("selectus-rating-c-fg"));
		Assert.assertTrue("selectus-rating-c-bg: " + resolve("selectus-rating-c-bg"), resolve("selectus-rating-c-bg").startsWith("#E"));
		// D10
		Assert.assertEquals("var(--o-state-danger-text)", tokens.get("navbar-tab-close"));
		Assert.assertEquals("var(--o-state-danger-text)", tokens.get("dropdown-tab-close"));
		Assert.assertEquals("var(--o-state-danger-text-hover)", tokens.get("dropdown-tab-close-hover"));
		assertRuleReads(css, ".o_navbar .o_navbar_tabs .o_navbar_tab_close i:before", "color:var(--o-navbar-tab-close)");
		assertRuleReads(css, ".o_dropdown_tab .o_navbar_tab_close", "color:var(--o-dropdown-tab-close)");
		assertRuleReads(css, ".o_dropdown_tab .o_navbar_tab_close:hover", "color:var(--o-dropdown-tab-close-hover)");
		// D11: dark tints
		assertRuleReads(css, ".o_skeleton", "border-top:1px solid color-mix(in srgb, var(--o-backdrop) 6%, transparent)");
		assertRuleReads(css, ".o_skeleton_bar", "background-color:color-mix(in srgb, var(--o-backdrop) 6%, transparent)");
		assertRuleReads(css, ".o_ai_paragraph_feedback", "background:color-mix(in srgb, var(--o-backdrop) 4%, transparent)");
		assertRuleReads(css, ".o_quiz .o_quiz_question.o_essay_ai_correction_host .o_essay_ai_correction_overlay",
				"background-color:color-mix(in srgb, var(--o-backdrop) 60%, transparent)");
		assertRuleReads(css, ".twentytwenty-wrapper .twentytwenty-container .twentytwenty-handle",
				"background-color:color-mix(in srgb, var(--o-backdrop) 33%, transparent)");
		assertRuleReads(css, ".dropdown-menu", "border:1px solid color-mix(in srgb, var(--o-backdrop) 3%, transparent)");
		// since slice 18 the translucent default of $o-lp-bg feeds the token lp-bg (D31 Q2)
		assertRuleReads(css, ".o_lp_tree.o_tree li.children_visible", "background:var(--o-lp-bg)");
		Assert.assertEquals("color-mix(in srgb, var(--o-backdrop) 2.5%, transparent)", resolve("lp-bg"));
		assertRuleReads(css, ".o_tag_admin", "background:repeating-linear-gradient(135deg, transparent, transparent 3px, "
				+ "color-mix(in srgb, var(--o-backdrop) 10%, transparent) 3px, color-mix(in srgb, var(--o-backdrop) 10%, transparent) 6px)");
		// D11: shadows, highlight, peekview, not available visual
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
	 * Decisions D12, D13 and D15 of OO-10043: the dark layer of the video
	 * comments reads backdrop; the hover and pressed states of the light
	 * buttons, the table filters and the selected scope toggle read a text
	 * token of the state, which follows the text of the button in light, and
	 * their fills keep the values of D9 (the default themes do not change);
	 * a fill is 3% darker or lighter at least; the progress bars of the
	 * selectus ratings read the tokens of the rating buttons.
	 */
	@Test
	public void decisionD12D13D15RulesReadTokens() {
		String css = CUSTOM_PROPERTY.matcher(read(PaletteTest.LIGHT_THEME_CSS)).replaceAll("");
		// D12
		assertRuleReads(css, ".o_video_comment_container", "background-color:color-mix(in srgb, var(--o-backdrop) 50%, transparent)");
		// D13: the text tokens of the states follow the text in light
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
		// D13: the fills of light do not change, every fill is visible
		Assert.assertEquals("#EDEDED", resolve("btn-primary-light-bg-hover"));
		Assert.assertEquals("#EDEDED", resolve("btn-primary-light-bg-active"));
		Assert.assertEquals("#F7F7F7", resolve("btn-danger-light-bg-hover"));
		Assert.assertEquals("#F7F7F7", resolve("btn-danger-light-bg-active"));
		Assert.assertEquals("#DBDBDB", resolve("table-filter-bg-active"));
		Assert.assertEquals("#3374CE", resolve("scope-toggle-on-hover-bg"));
		for (String button : CONTRAST_SHADE_BUTTONS) {
			for (String state : List.of("-hover", "-active")) {
				Assert.assertNotEquals(button + "-bg" + state + " has the colour of the normal state",
						resolve(button + "-bg"), resolve(button + "-bg" + state));
			}
		}
		Assert.assertNotEquals(resolve("scope-toggle-on-bg"), resolve("scope-toggle-on-hover-bg"));
		// D13: the model of o-contrast-shade-amount(): a text with 4.54:1 on
		// white has no visible amount, the state keeps 10% and 17%
		Assert.assertEquals(10, contrastShadeAmount("#DE323C", "#FFFFFF", 10));
		Assert.assertEquals(17, contrastShadeAmount("#DE323C", "#FFFFFF", 17));
		Assert.assertEquals(MIN_SHADE_AMOUNT, contrastShadeAmount(resolve("btn-danger-light-fg"), "#FFFFFF", 10));
		// D15 (bar A has no rule, decision D25)
		for (String rating : List.of("B", "C")) {
			String token = "selectus-rating-" + rating.toLowerCase() + "-bg";
			assertRuleReads(css, ".progress-bar.o_bar_rating_" + rating.toLowerCase(),
					"background-image:linear-gradient(to bottom, var(--o-" + token + ") 0%, var(--o-" + token + "-hover) 100%)");
		}
		// the fill of a styled course element (ratchet pattern of slice 10)
		assertRuleReads(css, ".o_cn_styled", "background-color:var(--o-course-style-bg)");
		Assert.assertEquals("var(--o-base-course-style-bg)", tokens.get("course-style-bg"));
	}

	/**
	 * Decisions D16 to D20 and D23 of OO-10043: the abstention bar of the
	 * selectus statistics reads the tokens of the abstention button, the label
	 * of bar B the text of button B; the marks and the explanation texts of
	 * the ratings read the fill of the rating buttons, B its own tokens with
	 * 3:1 (mark) and 4.5:1 (text) on the background; the border of a styled
	 * course element reads course-style-border (the shorthand
	 * $o-cn-style-border is gone); the 8 state fills of the light buttons and
	 * the table filters get lighter when they cannot get darker (a black
	 * fill); the navbar text and tools have 4.5:1 on the navbar.
	 */
	@Test
	public void decisionD16ToD23RulesReadTokens() {
		String css = CUSTOM_PROPERTY.matcher(read(PaletteTest.LIGHT_THEME_CSS)).replaceAll("");
		// D16, D17
		String abstain = ".progress-bar.o_bar_rating_abstain";
		assertRuleReads(css, abstain, "background-image:linear-gradient(to bottom, var(--o-selectus-rating-abstain-bg) 0%, var(--o-selectus-rating-abstain-bg-hover) 100%)");
		assertRuleReads(css, abstain, "color:var(--o-selectus-rating-abstain-fg)");
		assertRuleReads(css, ".progress-bar.o_bar_rating_b", "color:var(--o-selectus-rating-b-fg)");
		// D18: marks 1 = C, 2 = B, 3 = A; the letter has the colour of the mark
		Map<String, String> marks = Map.of("div.f_rating_1", "selectus-rating-c-bg", "div.f_rating_2",
				"selectus-rating-b-marker", "div.f_rating_3", "selectus-rating-a-bg");
		for (Map.Entry<String, String> mark : marks.entrySet()) {
			assertRuleReads(css, mark.getKey(), "background-color:var(--o-" + mark.getValue() + ")");
			assertRuleReads(css, mark.getKey(), "color:var(--o-" + mark.getValue() + ")");
		}
		// the letter C reads its own token since D26 (decisionD25ToD28Selectus)
		assertRuleReads(css, "span.o_rating_expl[title=A]", "color:var(--o-selectus-rating-a-bg)");
		assertRuleReads(css, "span.o_rating_expl[title=B]", "color:var(--o-selectus-rating-b-text)");
		assertContrast("selectus-rating-a-bg", "bg", 4.5);
		assertContrast("selectus-rating-b-text", "bg", 4.5);
		assertContrast("selectus-rating-b-marker", "bg", 3.0);
		// D19
		assertRuleReads(css, ".o_cn_styled", "border:1px solid var(--o-course-style-border)");
		assertRuleReads(css, ".o_cns_details .o_cns_descriptions", "border-top:1px solid var(--o-course-style-border)");
		Assert.assertEquals("var(--o-border)", tokens.get("course-style-border"));
		Path light = PaletteTest.LIGHT_THEME_CSS.getParent();
		String config = ThemeColorRatchetTest.withoutComments(read(light.resolve("_config.scss")));
		Assert.assertFalse("_config.scss declares the removed shorthand $o-cn-style-border",
				Pattern.compile("\\$o-cn-style-border\\s*:").matcher(config).find());
		// D20: the state fills use the function "state" of o-token-shade(), the
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
		Assert.assertEquals(resolve("table-filter-active-bg-hover"), stateShade(resolve("table-filter-active-bg"),
				contrastShadeAmount(resolve("table-filter-active-fg"), resolve("table-filter-active-bg"), 10)));
		// D23
		Assert.assertEquals("#747474", resolve("navbar-text"));
		Assert.assertEquals("#747474", resolve("navbar-tools"));
	}

	/**
	 * Decisions D25 to D28 of OO-10043, selectus: the bar of rating A has no
	 * own rule (PositionApplicationsController renders it as a success
	 * segment); the mark B and the letters B and C keep 3:1 and
	 * 4.5:1 on a white, a striped and a hovered table row (adjusted against the
	 * darkest of the three, o-darkest-color()); the 8 state fills are the only
	 * shade tokens with the function "state" (D27, no code change); the mark of
	 * no rating reads the fill of the default button, the mark of the
	 * abstention the fill of the abstention button.
	 */
	@Test
	public void decisionD25ToD28Selectus() {
		String css = CUSTOM_PROPERTY.matcher(read(PaletteTest.LIGHT_THEME_CSS)).replaceAll("");
		Path light = PaletteTest.LIGHT_THEME_CSS.getParent();
		// D25: no rule for bar A, and the Java code renders bar A as a success
		// segment without a class of its own (OO-10053: segments of the progress bar)
		Assert.assertFalse("The compiled theme has a rule for bar A", Pattern.compile("o_bar_rating_a(?![a-z])").matcher(css).find());
		Assert.assertFalse("The compiled theme has a rule of the old bar selector", css.contains("o_bar_rating_on"));
		Path controller = Path.of("src/main/java/org/olat/modules/selectus/ui/PositionApplicationsController.java");
		Matcher bar = Pattern.compile("new Segment\\(statistics\\.getRating\\((\\d)\\), BarColor\\.(\\w+), (?:\"([^\"]*)\", )?translate")
				.matcher(read(controller));
		int bars = 0;
		while (bar.find()) {
			bars++;
			if ("2".equals(bar.group(1))) {
				Assert.assertEquals("Bar A (rating.2) needs a rule of _selectus.scss again", "success", bar.group(2));
				Assert.assertNull("Bar A (rating.2) has a class of its own", bar.group(3));
			} else {
				String cssClass = "1".equals(bar.group(1)) ? "o_bar_rating_b" : "o_bar_rating_c";
				Assert.assertEquals("Bar rating." + bar.group(1), cssClass, bar.group(3));
			}
		}
		Assert.assertEquals("Bars A, B and C of the statistics", 3, bars);
		Assert.assertTrue("The abstention bar has the class o_bar_rating_abstain",
				read(controller).contains("BarColor.primary, \"o_bar_rating_abstain\""));
		// D26: the mark B and the letters B and C on the three rows, the marks A,
		// C and abstention and the letter A with the fill of the button
		assertRuleReads(css, "span.o_rating_expl[title=C]", "color:var(--o-selectus-rating-c-text)");
		assertRuleReads(css, "div.f_rating_2", "background-color:var(--o-selectus-rating-b-marker)");
		Assert.assertEquals("#A88700", resolve("selectus-rating-b-marker"));
		Assert.assertEquals("#856B00", resolve("selectus-rating-b-text"));
		Assert.assertEquals("#E00000", resolve("selectus-rating-c-text"));
		for (String row : List.of("bg", "table-striped-bg", "table-hover-bg")) {
			assertContrast("selectus-rating-b-marker", row, 3.0);
			assertContrast("selectus-rating-c-bg", row, 3.0);
			assertContrast("selectus-rating-abstain-bg", row, 3.0);
			assertContrast("selectus-rating-a-bg", row, 4.5);
			assertContrast("selectus-rating-b-text", row, 4.5);
			assertContrast("selectus-rating-c-text", row, 4.5);
		}
		// the adjusted tokens read the darkest row of the theme config, no hex value
		String scss = read(light.resolve("modules/_tokens.scss"));
		String darkest = "o-darkest-color($body-bg, $table-bg-accent, $table-bg-hover)";
		for (String token : List.of("selectus-rating-b-text", "selectus-rating-b-marker", "selectus-rating-c-text")) {
			Matcher m = Pattern.compile("\"" + token + "\":\\s*\\(([^\\n]*)\\),\\s*\\n").matcher(scss);
			Assert.assertTrue(token + " not in _tokens.scss", m.find());
			Assert.assertTrue(token + " is not adjusted against the darkest row: " + m.group(1), m.group(1).contains(darkest));
		}
		// D27: the rule of D20 stays with the 8 state fills
		Matcher state = Pattern.compile("\"([a-z0-9-]+)\":[^\\n]*, \"state\"\\),").matcher(scss);
		List<String> stateFills = new ArrayList<>();
		while (state.find()) {
			stateFills.add(state.group(1));
		}
		Assert.assertEquals(stateFills.toString(), 2 * CONTRAST_SHADE_BUTTONS.size(), stateFills.size());
		// D28: the marks of no rating and of the abstention read the fill of the buttons
		for (String selector : List.of("div.f_rating_0", "span.f_rating_0")) {
			assertRuleReads(css, selector, "background-color:var(--o-btn-default-bg)");
			assertRuleReads(css, selector, "color:var(--o-btn-default-bg)");
		}
		for (String selector : List.of("div.f_rating_abstention", "span.f_rating_abstention")) {
			assertRuleReads(css, selector, "background-color:var(--o-selectus-rating-abstain-bg)");
			assertRuleReads(css, selector, "color:var(--o-selectus-rating-abstain-bg)");
		}
		Assert.assertEquals("#555566", resolve("selectus-rating-abstain-bg"));
	}

	/**
	 * Decision D29 of OO-10043, the literal colours of the B4 review: each
	 * block reads the tokens of its recommended option. Texts and icons read
	 * text, heading, text-muted, input-placeholder (B4-1); borders and lines
	 * border, border-strong, text, bg, bg-raised (B4-2); fills bg-subtle,
	 * bg-hover, bg-raised, bg (B4-3); the hover of the file upload button the
	 * default button (B4-5); scroll shades, rank shadows and layers on media
	 * shadow-color, scrim and backdrop with the alpha of today (B4-6, B4-10,
	 * B4-13); AI marks and glows element tokens with a reference to a state
	 * fill (B4-7, B4-8); icons and texts with a status the state tokens
	 * (B4-9); decorative colours palette steps (B4-13). The own tokens have
	 * the values of today (B4-4, B4-11, B4-12, B4-14). The hotspot styles, the
	 * styles stored in content, brand colours, developer tools and print
	 * sheets keep their literals (B4-15 to B4-17); the rules without a reader
	 * of B4-18 are not changed (they need a check of the stored content).
	 */
	@Test
	public void decisionD29LiteralsReadTokens() {
		String css = CUSTOM_PROPERTY.matcher(read(PaletteTest.LIGHT_THEME_CSS)).replaceAll("");
		// B4-1 texts and icons
		assertRuleReads(css, ".o_cit .publicationTitle", "color:var(--o-text)");
		assertRuleReads(css, ".o_cit .notes", "color:var(--o-text-muted)");
		assertRuleReads(css, ".ProseMirror[data-placeholder]::before", "color:var(--o-input-placeholder)");
		assertRuleReads(css, ".o_vertical_timeline .o_vertical_timeline_rows .o_vertical_timeline_row .o_vertical_timeline_year h5",
				"color:var(--o-heading)");
		assertRuleReads(css, ".o_practice_statistics span>i", "color:var(--o-practice-icon)");
		// B4-2 borders and lines
		assertRuleReads(css, ".d3chart .axis path", "stroke:var(--o-text)");
		assertRuleReads(css, ".o_course_run .o_course_pagination", "border-top:1px solid var(--o-border)");
		assertRuleReads(css, ".o_empty_panel .o_empty_panel_icon", "border:2px solid var(--o-bg)");
		assertRuleReads(css, ".ui-dialog .ui-widget-content", "border-color:var(--o-bg-raised)");
		assertRuleReads(css, "div.f_rating_overview", "border:1px solid var(--o-btn-default-border)");
		assertRuleReads(css, ".o_assessmentitem .gap", "border:1px dashed var(--o-qti-text-gap-border)");
		// B4-3 fills
		assertRuleReads(css, ".o_im_welcome_image", "background-color:var(--o-bg-subtle)");
		assertRuleReads(css, ".o_page_content_editor .o_page_part.o_page_part_view:hover", "background-color:var(--o-bg-hover)");
		assertRuleReads(css, ".o_badge_wiz_image_step .o_template_cards .o_template_card.o_selected",
				"background-color:var(--o-badge-template-selected-bg)");
		assertRuleReads(css, ".o_table_wrapper .o_scrollable_left .o_col_sticky_left", "background-color:var(--o-table-sticky-col-bg)");
		// B4-4 one-time code field
		assertRuleReads(css, ".o_sel_registration_otp input.o_sel_registration_otp", "--otp-cell-bg-color:var(--o-otp-cell-bg)");
		assertRuleReads(css, ".o_sel_registration_otp input.o_sel_registration_otp", "--otp-cell-bg-color-focus:var(--o-otp-cell-active-bg)");
		assertRuleReads(css, ".o_sel_registration_otp input.o_sel_registration_otp", "caret-color:var(--otp-cc, var(--o-otp-caret))");
		// B4-5 hover of the file upload button: long selector lists, each selector reads the default button
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
		// B4-6 scroll shades
		assertRuleReads(css, ".o_object_selection .o_object_selection_main::before",
				"background:linear-gradient(color-mix(in srgb, var(--o-shadow-color) 10%, transparent), transparent)");
		// B4-7 and B4-8: element tokens with a reference to a state fill, the alpha of today
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
		// B4-9 status meaning
		assertRuleReads(css, ".o_icon_delete", "color:var(--o-state-danger-text)");
		assertRuleReads(css, ".o_icon_member_skipped", "color:var(--o-state-inactive-text)");
		assertRuleReads(css, ".o_ochre", "color:var(--o-state-warning-text)");
		assertRuleReads(css, ".o_dialog_card_label_new", "background:var(--o-state-warning-soft-bg)");
		assertRuleReads(css, "div.o_wiki_wrapper a.edit", "color:var(--o-state-danger-text)");
		// B4-10 layers on images and video
		assertRuleReads(css, ".o_video_listing .o_timecode", "background:color-mix(in srgb, var(--o-backdrop) 80%, transparent)");
		assertRuleReads(css, ".o_video_recorder .o_av_container", "background-color:var(--o-backdrop)");
		assertRuleReads(css, ".o_forum .o_icon_enlarge",
				"text-shadow:1px 1px 2px var(--o-scrim),-1px 1px 2px var(--o-scrim),1px -1px 2px var(--o-scrim),-1px -1px 2px var(--o-scrim)");
		// B4-11 off-canvas menu: the hover reads its Sass variable through the token
		assertRuleReads(css, ".o_navbar.o_navbar-offcanvas .o_navbar-right a:hover", "color:var(--o-offcanvas-link-hover)");
		assertRuleReads(css, ".o_navbar.o_navbar-offcanvas .o_navbar-right h2.o_navbar_category",
				"border-bottom:1px solid var(--o-offcanvas-category-border)");
		Assert.assertEquals("#939393", resolve("offcanvas-link-hover"));
		// B4-12 and B4-14 own tokens of selectus and the recorder
		assertRuleReads(css, ".o_position_status_filter.o_published", "color:var(--o-selectus-status-published)");
		assertRuleReads(css, ".o_reference_status_filter.o_deactivated", "color:var(--o-selectus-status-deactivated)");
		assertRuleReads(css, ".o_video_recorder .strong-symbol:hover", "fill:var(--o-recorder-record-hover)");
		assertRuleReads(css, ".o_video_recorder .symbol:active", "fill:var(--o-recorder-symbol-active)");
		// B4-13 decorative colours: palette steps, rank shadows, white pen, drop target
		assertRuleReads(css, ".o_highscore .o_rank",
				"text-shadow:color-mix(in srgb, var(--o-shadow-color) 50%, transparent) 0 -1px 0,color-mix(in srgb, var(--o-scrim) 60%, transparent) 0 2px 1px");
		assertRuleReads(css, ".o_icon_provider_guest", "color:var(--o-orange-500)");
		assertRuleReads(css, ".o_cn_styled.o_cn_header .o_instructional_design",
				"background-color:color-mix(in srgb, var(--o-magenta-700) 15%, transparent)");
		assertRuleReads(css, ".white.btn>i.o_icon_circle_color:before", "color:var(--o-gray-900)");
		assertRuleReads(css, ".o_group_colors .white .o_icon:before", "color:var(--o-white)");
		assertRuleReads(css, ".o_tree ul li .o_dnd_item.o_dnd_over", "background-color:var(--o-tree-dnd-over-bg)");
		// the own tokens keep the colours of today
		String[] own = { "practice-icon", "#CCCCCC", "qti-text-gap-border", "#000000", "badge-template-selected-bg", "#DDDDDD",
				"video-category-active-bg", "#DDDDDD", "table-sticky-col-bg", "#FAFAFA", "table-sticky-col-striped-bg", "#F5F5F5",
				"otp-cell-bg", "#EEEEEE", "otp-cell-active-bg", "#BBBBBB", "otp-caret", "#222222",
				"offcanvas-category-border", "#888888", "tree-dnd-over-bg", "#FFFF60",
				"selectus-status-preparation", "#949494", "selectus-status-published", "#006E0D",
				"selectus-status-published-screening", "#17882E", "selectus-status-screening", "#E7C33F",
				"selectus-status-closed-screening", "#DE5A3E", "selectus-status-closed", "#BD1F10",
				"selectus-status-reporting", "#303030", "selectus-status-deactivated", "#CCCCCC",
				"recorder-decoration", "#000000", "recorder-recording", "#FF9999", "recorder-record", "#FF0000",
				"recorder-record-hover", "#FF4040", "recorder-record-active", "#FF6060", "recorder-symbol", "#000000",
				"recorder-symbol-hover", "#202020", "recorder-symbol-active", "#404040" };
		for (int i = 0; i < own.length; i += 2) {
			Assert.assertEquals(own[i], own[i + 1], resolve(own[i]));
		}
		// the new contrast pairs pass with the light defaults
		assertContrast("otp-caret", "otp-cell-bg", 4.5);
		assertContrast("otp-caret", "otp-cell-active-bg", 4.5);
		assertContrast("qti-text-gap-border", "bg", 3.0);
		for (String token : List.of("selectus-status-preparation", "selectus-status-published", "selectus-status-published-screening",
				"selectus-status-closed-screening", "selectus-status-closed", "selectus-status-reporting", "recorder-record",
				"recorder-record-hover", "recorder-symbol", "recorder-symbol-hover", "recorder-symbol-active")) {
			assertContrast(token, "bg", 3.0);
		}
		Path modules = PaletteTest.LIGHT_THEME_CSS.getParent().resolve("modules");
		// B4-15 to B4-17: the kept literals are still there, the hotspot styles name the template
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
		// B4-18: the rules without a reader and the invalid declarations are not changed
		assertSourceHas(modules.resolve("_selectus.scss"), "&.b_preparation_on {  @include o-add-icon('circle',true); color: #999; }",
				"&.b_closed { @include o-add-icon('circle',true); color: #de5a3e; }", "boxShadow");
		assertSourceHas(modules.resolve("_helpers.scss"), "o_blue");
		assertSourceHas(modules.resolve("_qti21.scss"), "color: #666666");
		assertSourceHas(modules.resolve("_user.scss"), "#4caf50", "#f3f3f3");
		assertSourceHas(modules.resolve("_video.scss"), "box-shadow: red");
		assertSourceHas(modules.resolve("_folder.scss"), "1px solid 1px solid #ccc");
		assertSourceHas(modules.resolve("_topicbroker.scss"), "1px solid 1px solid #ccc");
		assertSourceHas(modules.resolve("_portfolio_v2.scss"), "pink");
		assertSourceHas(PaletteTest.LIGHT_THEME_CSS.getParent().resolve("_config.scss"), "$o-video-controls-selected", "$o-video-controls-bg");
	}

	/**
	 * OO-10043 slice 16, the framework modules (dialog, login page, cards,
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
		Assert.assertEquals("var(--o-state-neutral-soft-bg)", tokens.get("table-row-selected-bg"));
		Assert.assertEquals("var(--o-state-danger-text)", tokens.get("navbar-logout"));
		// the colours of today
		String[] values = { "box-note-border", "#666666", "box-tip-border", "#345566", "box-important-border", "#41BBDB",
				"message-line-warning-bg", "#FFBD96", "message-line-error-fg", "#4A0606", "popover-fallback-border", "#CCCCCC",
				"dialog-header-bg", "#F9FAFB", "login-help-bg", "#E4E7EC", "widget-subtitle", "#777777",
				"offcanvas-link", "#ACACAC", "table-edit-bg", "#FEFBF6", "tree-border", "#DDDDDD",
				"tree-insertion-source-bg", "#FFCF91", "icon-mandatory", "#D17A00", "lecture-rollcall-pending", "#637286" };
		for (int i = 0; i < values.length; i += 2) {
			Assert.assertEquals(values[i], values[i + 1], resolve(values[i]));
		}
		// the new contrast pairs pass with the light defaults
		assertContrast("box-note-border", "box-note-bg", 3.0);
		assertContrast("box-tip-border", "box-tip-bg", 3.0);
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
	 * Decision D24 of OO-10043, the focus ring: the token focus is the primary
	 * colour adjusted to 3:1 on the page background ($o-focus-color, read
	 * before the Bootstrap defaults define $input-border-focus); every control
	 * has the ring outline 2px solid var(--o-focus) with the offset 2px on
	 * :focus-visible instead of the ring of the browser (tab-focus) and the
	 * halo or the glow of the form fields; the controls that touch their
	 * neighbours or that a container cuts have the ring inside the edge, on a
	 * fill in the colour of the ring in the text colour of the fill; the navbar
	 * reads navbar-focus, the footer footer-focus; the radio cards keep the
	 * colours of $input-border-focus; no theme module has the literal of the
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
		// D30: a radio card shows the ring, the radio or checkbox inside it does not
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
		// the radio cards keep the colours of $input-border-focus (#66AFE9)
		int[] checkedBg = darken("#66AFE9", -30);
		Assert.assertEquals(String.format("#%02X%02X%02X", checkedBg[0], checkedBg[1], checkedBg[2]), resolve("radio-card-checked-bg"));
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
	 * The other button rules read tokens (OO-10043 slice 7): the disabled
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
	 * resolves to a colour (decision D31 Q2, OO-10043 slice 17).
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
	 * OO-10043 slice 17, the assessment and content modules (QTI, page
	 * editor, practice, video, video task) and the follow-ups of slice 16
	 * (decision D31): the rules read element tokens fed by the Sass variables
	 * of the theme config, no colour changes. A named variable with a
	 * translucent default feeds a token with the color-mix() default (Q2). The
	 * shorthand border variables are split into width, style and a colour
	 * token (Q1); $o-login-border stays valid for a theme that sets it. The
	 * new texts and icons pass their contrast pairs in light.
	 */
	@Test
	public void assessmentAndContentModulesReadTokens() {
		String css = CUSTOM_PROPERTY.matcher(read(PaletteTest.LIGHT_THEME_CSS)).replaceAll("");
		// Q2: translucent defaults
		assertRuleReads(css, ".o_login .o_login_box", "background-color:var(--o-login-bg)");
		assertRuleReads(css, ".o_block_imagebg span", "background-color:var(--o-imagebg-text-bg)");
		Assert.assertEquals("color-mix(in srgb, var(--o-scrim) 95%, transparent)", resolve("login-bg"));
		Assert.assertEquals("color-mix(in srgb, var(--o-primary) 50%, transparent)", resolve("drawing-select-bg"));
		// Q1: shorthand borders, width and style of the theme config and a colour token
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
		assertRuleReads(css, ".o_page_container .o_hover_text_wrapper .o_hover_text", "color:var(--o-editor-hover-text)");
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
		Assert.assertEquals("var(--o-box-note-border)", tokens.get("box-note-text"));
		// the colours of today
		String[] values = { "card-border", "#E4E7EC", "repo-info-border", "#D4D4D4", "qti-item-max-score-border", "#000000",
				"qti-choice-tablelayout-bg", "#E7E7E7", "editor-hover-border", "#EF00EF", "editor-alert-box-active-border", "#464646",
				"practice-progress-l1-bg", "#1FC6FE", "video-border", "#FCFCFC", "video-controls-bg", "rgba(50, 50, 50, 0.95)" };
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
	 * OO-10043 slice 18, the course, curriculum, user, chat, high score,
	 * projects, radial progress, Selectus and various modules: the rules read
	 * element tokens fed by the Sass variables of the theme config, no colour
	 * changes. The shorthand border variables of these modules are split into
	 * width, style and a colour token (decision D31 Q1), the translucent
	 * defaults feed a token with the color-mix() default (Q2), the icon colours
	 * of the figure cards are tokens of the tint swatch. The other modules read
	 * the tokens of $o-toolbar-border-color, $o-slider-border,
	 * $o-portfolio-media-content-background-color,
	 * $o-coursesite-meta-author-color and $o-table-row-selected-color. The new
	 * texts and icons pass their contrast pairs in light.
	 */
	@Test
	public void courseUserAndVariousModulesReadTokens() {
		String css = CUSTOM_PROPERTY.matcher(read(PaletteTest.LIGHT_THEME_CSS)).replaceAll("");
		// Q1: shorthand borders, width and style of the theme config and a colour token
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
		// Q2: translucent defaults
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
		String[] values = { "empty-border", "#E4E7EC", "highscore-podium-border", "#D9D9D9", "user-info-profile-border", "#CCCCCC",
				"lp-pagination-done-border", "#2E5C2E", "curriculum-element-inactive", "#777777", "selectus-onhold", "#9E9E9E",
				"authentication-level1", "#793E02", "user-status-inactive-border", "#637286" };
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
	 * OO-10043 slice 19: the other modules read element tokens fed by their
	 * Sass variables, the toolbar reads tokens (decision D32 E3), the
	 * variables with upper-case letters have tokens (E1) and the position
	 * title of the high score has a switch and a token (E2).
	 */
	@Test
	public void otherModulesReadTokens() {
		String css = CUSTOM_PROPERTY.matcher(read(PaletteTest.LIGHT_THEME_CSS)).replaceAll("");
		// E3: the toolbar
		assertRuleReads(css, ".o_toolbar .o_tools_container", "background-color:var(--o-toolbar-bg)");
		assertRuleReads(css, ".o_toolbar .o_tool_next", "background-color:var(--o-toolbar-nav-bg)");
		assertRuleReads(css, ".o_edit_mode .o_toolbar .o_tools_container",
				"background:repeating-linear-gradient(300deg, var(--o-toolbar-edit-stripe), var(--o-toolbar-edit-stripe) 10px, var(--o-toolbar-edit-bg) 10px, var(--o-toolbar-edit-bg) 20px)");
		// E1: names with upper-case letters
		assertRuleReads(css, ".o_assessmentitem div.badResponse", "color:var(--o-qti-bad-response)");
		assertRuleReads(css, ".o_icon_qti_notPresented", "color:var(--o-qti-status-not-presented)");
		Assert.assertEquals("var(--o-state-danger-text)", tokens.get("qti-bad-response"));
		Assert.assertEquals("var(--o-state-neutral-soft-bg)", tokens.get("qti-text-entry-filled-bg"));
		Assert.assertEquals("var(--o-qti-gap-border)", tokens.get("qti-text-entry-border"));
		Assert.assertEquals(1, ThemeColorRatchetTest.countOldColorReferences(".a { color: $o-qti-textEntryInteraction-color; }"));
		Assert.assertEquals(0, ThemeColorRatchetTest.countOldColorReferences(".a { width: $o-qti-textEntryInteraction-width; }"));
		// E2: the gradient by default, the token without it; the old variable stays valid
		Path light = PaletteTest.LIGHT_THEME_CSS.getParent();
		assertSourceHas(light.resolve("_config.scss"), "$o-hs-position-title-gradient", "@function o-hs-position-title-gradient()",
				"@function o-hs-position-title-color()");
		assertSourceHas(light.resolve("modules/_highscore.scss"), "@if o-hs-position-title-gradient()", "var(--o-highscore-position-title)");
		Assert.assertEquals("var(--o-text)", tokens.get("highscore-position-title"));
		// Q1: shorthand borders, width and style of the theme config and a colour token
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
		String[] values = { "step-border", "#E4E7EC", "ac-billing-address-border", "#D0D5DD", "toolbar-edit-stripe", "#FFF0D9",
				"gloss-tooltip-border", "#AAAAAA", "step-notavailable", "#999999", "folder-card-thumbnail-icon", "#CCCCCC",
				"qti-status-not-answered", "#B55604" };
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

	/**
	 * @param css The compiled CSS without the custom properties
	 * @param selector One selector of a selector list, without spaces around ">"
	 * @param declaration The declaration without spaces after ":" and without ";"
	 */
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
		Assert.assertTrue(shade + ": " + shadeValue, HEX.matcher(shadeValue).matches());
		String sourceHex = resolve(source);
		Assert.assertTrue(source + ": " + sourceHex, HEX.matcher(sourceHex).matches());
		int[] expected = darken(sourceHex, percent);
		String function = percent < 0 ? "lightened by " + (-percent) : "darkened by " + percent;
		// The token value is a rounded colour: 2 of 255 per channel
		for (int i = 0; i < 3; i++) {
			int channel = Integer.parseInt(shadeValue.substring(1 + 2 * i, 3 + 2 * i), 16);
			Assert.assertTrue(shade + " " + shadeValue + " is not " + source + " " + sourceHex + " " + function
					+ "%: rgb(" + expected[0] + ", " + expected[1] + ", " + expected[2] + ")",
					Math.abs(channel - expected[i]) <= 2);
		}
	}

	/**
	 * The amount of o-contrast-shade-amount() in _functions.scss: the largest
	 * amount from 3% up to max at which the text keeps 4.5:1 on the darker
	 * fill; max when the text fails on the fill itself, and max when no
	 * amount from 3% passes (decision D13: the text of the state is adjusted).
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
	 * colour (decision D20).
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

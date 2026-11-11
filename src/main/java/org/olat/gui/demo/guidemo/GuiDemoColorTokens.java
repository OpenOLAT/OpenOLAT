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
package org.olat.gui.demo.guidemo;

import java.util.ArrayList;
import java.util.List;

import org.olat.gui.demo.guidemo.GuiDemoColorBadges.State;

/**
 * The greys and the colour tokens for the GUI demo tab "Greys and tokens"
 * (OO-10043): the neutral scale of <code>themes/light/_palette_neutral.scss</code>,
 * the semantic tokens of <code>$o-token-base</code>, the state tokens and a
 * selection of the element tokens of <code>$o-token-layer-base</code> and
 * <code>$o-token-layer-framework</code> in
 * <code>themes/light/modules/_tokens.scss</code>. The names must match the
 * Sass files: <code>GuiDemoColorTokensTest</code> checks them against the
 * compiled <code>light/theme.css</code>. The tab renders every sample with
 * the custom property, a script in the template measures the values.
 *
 * Initial date: 11 Nov 2026<br>
 * @author gnaegi, https://www.frentix.com
 */
public class GuiDemoColorTokens {

	public static final List<Integer> NEUTRAL_STEPS = List.of(0, 25, 50, 100, 150, 200, 250, 300, 400, 450, 500,
			600, 700, 750, 800, 900, 950, 1000);
	public static final List<String> STATE_ROLES = List.of("text", "bg", "fg", "border", "soft-bg", "soft-fg",
			"soft-border");

	// Measures of a sample, read by the script of the tab
	static final String MEASURE_TEXT = "text";
	static final String MEASURE_BORDER = "border";
	static final String MEASURE_OUTLINE = "outline";
	static final String MEASURE_NONE = "";

	private static final String AA_TEXT = "4.5";
	private static final String AA_NON_TEXT = "3";
	private static final String NO_RATIO = "";

	private GuiDemoColorTokens() {
		//
	}

	/**
	 * @return The 18 steps of the neutral scale with the anchor of each step:
	 * 		the token whose light default gives the colour of the step
	 * 		(_palette_neutral.scss)
	 */
	public static List<NeutralStep> neutralSteps() {
		List<NeutralStep> steps = new ArrayList<>();
		for (Integer step : NEUTRAL_STEPS) {
			steps.add(new NeutralStep(step.intValue(), neutralAnchor(step.intValue())));
		}
		return steps;
	}

	private static String neutralAnchor(int step) {
		return switch (step) {
			case 0 -> "white";
			case 25 -> "bg-subtle";
			case 150 -> "border";
			case 250 -> "border-strong";
			case 450 -> "3:1 on white";
			case 750 -> "text";
			case 950 -> "shadow anchor";
			case 1000 -> "black";
			default -> "";
		};
	}

	/**
	 * @return The semantic tokens of <code>$o-token-base</code> and the link
	 * 		token <code>link-subtle</code>, in the groups of the tab
	 */
	public static List<TokenGroup> semanticGroups() {
		List<TokenGroup> groups = new ArrayList<>();
		groups.add(new TokenGroup("Text", null, List.of(
				new Token("text", "$text-color", "Body text, labels, table cells",
						text("text", "bg", "Body text", AA_TEXT)),
				new Token("text-muted", "$text-muted", "Secondary text: hints, meta data, counts",
						text("text-muted", "bg", "Secondary text", AA_TEXT)),
				new Token("text-disabled", "$btn-link-disabled-color", "Text of a disabled control (no contrast requirement)",
						text("text-disabled", "bg", "Disabled", NO_RATIO)),
				new Token("heading", "$headings-color, else $text-color", "Headings, legend, dialog title",
						text("heading", "bg", "Heading", AA_TEXT)))));
		groups.add(new TokenGroup("Link", null, List.of(
				new Token("link", "$link-color", "Links on the page surface, .btn-link, pagination",
						text("link", "bg", "A link", AA_TEXT)),
				new Token("link-hover", "$link-hover-color", "Link under the pointer",
						text("link-hover", "bg", "A link, hover", AA_TEXT)),
				new Token("link-subtle", "$link-color adjusted to 4.5:1 on $panel-default-heading-bg, follows link",
						"Links on the subtle background: breadcrumb, table, course element, forum",
						text("link-subtle", "bg-subtle", "A link on bg-subtle", AA_TEXT)))));
		groups.add(new TokenGroup("Brand", null, List.of(
				new Token("primary", "$brand-primary", "Brand fill: primary button, selected marker, active element",
						fill("primary", "primary-fg", "Primary fill", AA_TEXT)),
				new Token("primary-fg", "Text colour on $brand-primary with 4.5:1", "Text and icons on primary",
						text("primary-fg", "primary", "Text on primary", AA_TEXT)),
				new Token("primary-hover", "$brand-primary, 10% darker", "Primary fill under the pointer",
						fill("primary-hover", "primary-fg", "Primary hover", AA_TEXT)),
				new Token("primary-text", "$o-brand-text", "Brand colour as text or icon on white",
						text("primary-text", "bg", "Brand text", AA_TEXT)))));
		groups.add(new TokenGroup("Background", null, List.of(
				new Token("bg", "$body-bg", "Page surface",
						fill("bg", "text", "Page surface", AA_TEXT)),
				new Token("bg-subtle", "$panel-default-heading-bg", "Panel heading, side column, quiet area",
						fill("bg-subtle", "text", "Subtle surface", AA_TEXT)),
				new Token("bg-hover", "$table-bg-hover", "Row or item under the pointer",
						fill("bg-hover", "text", "Hover row", AA_TEXT)),
				new Token("bg-selected", "$table-bg-active", "Selected row or item",
						fill("bg-selected", "text", "Selected row", AA_TEXT)),
				new Token("bg-raised", "$dropdown-bg", "Surface above the page: dropdown menu, popover, dialog",
						fill("bg-raised", "text", "Raised surface", AA_TEXT)))));
		groups.add(new TokenGroup("Border", null, List.of(
				new Token("border", "$panel-default-border", "Panel, card, divider (decorative, no contrast requirement)",
						border("border", "bg", "text", "Panel", NO_RATIO)),
				new Token("border-subtle", "$hr-border", "Light separator line: hr, list item, blockquote",
						border("border-subtle", "bg", "text", "Separator", NO_RATIO)),
				new Token("border-strong", "$input-border", "Form field, radio card",
						border("border-strong", "bg", "text", "Form field", NO_RATIO)))));
		groups.add(new TokenGroup("Overlay and focus", null, List.of(
				new Token("focus", "$input-border-focus", "Focus ring of a control (3:1 on its background)",
						new Sample("outline: 2px solid var(--o-focus); outline-offset: 2px; border: 1px solid var(--o-border-strong); background-color: var(--o-bg); color: var(--o-text);",
								null, "Focused field", MEASURE_OUTLINE, AA_NON_TEXT)),
				new Token("shadow-color", "black", "Shadows, opaque: the rule sets the transparency with color-mix()",
						new Sample("box-shadow: 0 4px 12px color-mix(in srgb, var(--o-shadow-color) 25%, transparent); border: 1px solid var(--o-border); background-color: var(--o-bg-raised); color: var(--o-text);",
								null, "Raised, 25% shadow", MEASURE_NONE, NO_RATIO)),
				new Token("backdrop", "$modal-backdrop-bg", "Layer behind a modal dialog, opaque: the rule sets the transparency",
						new Sample("background-color: color-mix(in srgb, var(--o-backdrop) 50%, transparent);",
								"background-color: var(--o-bg-raised); color: var(--o-text);", "Dialog on a 50% backdrop", MEASURE_TEXT, AA_TEXT)),
				new Token("scrim", "white", "Light veil over content, an image or a video below a text, opaque",
						new Sample("background-color: var(--o-primary);",
								"background-color: color-mix(in srgb, var(--o-scrim) 85%, transparent); color: var(--o-text);", "Text on an 85% veil", MEASURE_TEXT, AA_TEXT)))));
		return groups;
	}

	/**
	 * @return The states of <code>$o-states</code> with their state tokens and
	 * 		three samples: solid, soft and text on white
	 */
	public static List<StateRow> stateRows() {
		List<StateRow> rows = new ArrayList<>();
		for (State state : GuiDemoColorBadges.states()) {
			String prefix = "state-" + state.name() + "-";
			List<String> tokens = STATE_ROLES.stream().map(role -> prefix + role).toList();
			List<Sample> samples = List.of(
					new Sample(fillStyle(prefix + "bg", prefix + "fg") + borderStyle(prefix + "border", 1),
							null, "solid", MEASURE_TEXT, AA_TEXT),
					new Sample(fillStyle(prefix + "soft-bg", prefix + "soft-fg") + borderStyle(prefix + "soft-border", 1),
							null, "soft", MEASURE_TEXT, AA_TEXT),
					new Sample(fillStyle("bg", prefix + "text") + borderStyle(prefix + "border", 1),
							null, "text", MEASURE_TEXT, AA_TEXT));
			rows.add(new StateRow(state, tokens, samples));
		}
		return rows;
	}

	/**
	 * @return A selection of the element tokens of the base layer (buttons)
	 * 		and of the framework layer that a module author reuses, with the
	 * 		default of <code>_tokens.scss</code>
	 */
	public static List<TokenGroup> elementGroups() {
		List<TokenGroup> groups = new ArrayList<>();
		groups.add(new TokenGroup("Buttons", "base layer", List.of(
				new Token("btn-default-bg", "$btn-default-bg, follows bg", "Fill of the default button",
						fill("btn-default-bg", "btn-default-fg", "Button", AA_TEXT)),
				new Token("btn-default-fg", "$btn-default-color, follows text", "Text of the default button",
						text("btn-default-fg", "btn-default-bg", "Button", AA_TEXT)),
				new Token("btn-default-border", "$btn-default-border", "Border of the default button",
						border("btn-default-border", "btn-default-bg", "btn-default-fg", "Button", AA_NON_TEXT)),
				new Token("btn-default-bg-hover", "shade: btn-default-bg, 10% darker", "Fill under the pointer",
						fill("btn-default-bg-hover", "btn-default-fg", "Button, hover", AA_TEXT)),
				new Token("btn-default-bg-active", "shade: btn-default-bg, 17% darker", "Fill when pressed",
						fill("btn-default-bg-active", "btn-default-fg", "Button, active", AA_TEXT)),
				new Token("btn-default-border-hover", "shade: btn-default-border, 12% darker", "Border under the pointer",
						border("btn-default-border-hover", "btn-default-bg-hover", "btn-default-fg", "Button, hover", AA_NON_TEXT)),
				new Token("btn-default-border-focus", "shade: btn-default-border, 25% darker", "Border with the keyboard focus",
						border("btn-default-border-focus", "btn-default-bg-hover", "btn-default-fg", "Button, focus", AA_NON_TEXT)),
				new Token("btn-primary-bg", "$btn-primary-bg, follows primary", "Fill of the primary button",
						fill("btn-primary-bg", "btn-primary-fg", "Primary", AA_TEXT)),
				new Token("btn-primary-fg", "$btn-primary-color, follows primary-fg", "Text of the primary button",
						text("btn-primary-fg", "btn-primary-bg", "Primary", AA_TEXT)),
				new Token("btn-primary-border", "$btn-primary-border", "Border of the primary button",
						border("btn-primary-border", "btn-primary-bg", "btn-primary-fg", "Primary", NO_RATIO)),
				new Token("btn-primary-bg-hover", "shade: btn-primary-bg, 10% darker", "Fill under the pointer",
						fill("btn-primary-bg-hover", "btn-primary-fg", "Primary, hover", AA_TEXT)),
				new Token("btn-primary-bg-active", "shade: btn-primary-bg, 17% darker", "Fill when pressed",
						fill("btn-primary-bg-active", "btn-primary-fg", "Primary, active", AA_TEXT)),
				new Token("btn-primary-border-hover", "shade: btn-primary-border, 12% darker", "Border under the pointer",
						border("btn-primary-border-hover", "btn-primary-bg-hover", "btn-primary-fg", "Primary, hover", NO_RATIO)),
				new Token("btn-primary-border-focus", "shade: btn-primary-border, 25% darker", "Border with the keyboard focus",
						border("btn-primary-border-focus", "btn-primary-bg-hover", "btn-primary-fg", "Primary, focus", NO_RATIO)),
				new Token("btn-danger-bg", "\"state-danger-bg\"", "Fill of the danger button",
						fill("btn-danger-bg", "btn-danger-fg", "Danger", AA_TEXT)),
				new Token("btn-danger-fg", "\"state-danger-fg\"", "Text of the danger button",
						text("btn-danger-fg", "btn-danger-bg", "Danger", AA_TEXT)),
				new Token("btn-danger-border", "\"state-danger-border\"", "Border of the danger button",
						border("btn-danger-border", "btn-danger-bg", "btn-danger-fg", "Danger", NO_RATIO)),
				new Token("btn-danger-bg-hover", "shade: btn-danger-bg, 10% darker", "Fill under the pointer",
						fill("btn-danger-bg-hover", "btn-danger-fg", "Danger, hover", AA_TEXT)),
				new Token("btn-danger-bg-active", "shade: btn-danger-bg, 17% darker", "Fill when pressed",
						fill("btn-danger-bg-active", "btn-danger-fg", "Danger, active", AA_TEXT)),
				new Token("btn-danger-border-hover", "shade: btn-danger-border, 12% darker", "Border under the pointer",
						border("btn-danger-border-hover", "btn-danger-bg-hover", "btn-danger-fg", "Danger, hover", NO_RATIO)),
				new Token("btn-danger-border-focus", "shade: btn-danger-border, 25% darker", "Border with the keyboard focus",
						border("btn-danger-border-focus", "btn-danger-bg-hover", "btn-danger-fg", "Danger, focus", NO_RATIO)),
				new Token("btn-ghost-fg", "\"link\"", "Text of a button without fill and border",
						text("btn-ghost-fg", "bg", "Ghost button", AA_TEXT)))));
		groups.add(new TokenGroup("Navigation links", "framework layer", List.of(
				new Token("nav-link", "$navbar-default-link-color", "Neutral link colour of the menu tree, the toolbar and the QTI menu",
						text("nav-link", "bg", "Navigation link", AA_TEXT)),
				new Token("nav-link-hover", "$navbar-default-link-hover-color", "Navigation link under the pointer",
						text("nav-link-hover", "bg", "Navigation link, hover", AA_TEXT)))));
		groups.add(new TokenGroup("Toolbar and breadcrumb", "framework layer", List.of(
				new Token("toolbar-link", "$o-toolbar-link-color, follows nav-link", "Links and tools in the toolbar",
						text("toolbar-link", "bg", "Toolbar link", AA_TEXT)),
				new Token("toolbar-link-hover", "$o-toolbar-link-hover-color, follows nav-link-hover", "Toolbar link under the pointer",
						text("toolbar-link-hover", "bg", "Toolbar link, hover", AA_TEXT)),
				new Token("toolbar-link-active", "$o-toolbar-link-active-color, follows link", "Selected tool in the toolbar",
						text("toolbar-link-active", "bg", "Toolbar link, active", AA_TEXT)),
				new Token("toolbar-breadcrumb-bg", "$o-toolbar-breadcrumb-bg-color, follows bg-subtle", "Background of the breadcrumb bar",
						fill("toolbar-breadcrumb-bg", "toolbar-breadcrumb-link", "Breadcrumb bar", AA_TEXT)),
				new Token("toolbar-breadcrumb-link", "\"link-subtle\"", "Links in the breadcrumb",
						text("toolbar-breadcrumb-link", "toolbar-breadcrumb-bg", "Breadcrumb link", AA_TEXT)))));
		groups.add(new TokenGroup("Menu tree", "framework layer", List.of(
				new Token("tree-link", "$o-tree-link-color, follows nav-link", "Item of the menu tree",
						text("tree-link", "bg", "Tree item", AA_TEXT)),
				new Token("tree-link-hover", "$o-tree-link-hover-color, follows nav-link-hover", "Tree item under the pointer",
						text("tree-link-hover", "tree-link-hover-bg", "Tree item, hover", AA_TEXT)),
				new Token("tree-link-hover-bg", "$o-tree-link-hover-bg", "Background of a tree item under the pointer",
						fill("tree-link-hover-bg", "tree-link-hover", "Tree item, hover", AA_TEXT)),
				new Token("tree-link-active", "$o-tree-link-active-color, follows link", "Selected tree item",
						text("tree-link-active", "bg", "Tree item, selected", AA_TEXT)),
				new Token("tree-link-active-hover-bg", "$o-tree-link-active-hover-bg", "Background of the selected tree item under the pointer",
						fill("tree-link-active-hover-bg", "tree-link-active-hover", "Selected, hover", AA_TEXT)))));
		groups.add(new TokenGroup("Table", "framework layer", List.of(
				new Token("table-header", "\"text\"", "Text of the table header",
						text("table-header", "bg", "Column header", AA_TEXT)),
				new Token("table-link", "\"link-subtle\"", "Links in table cells",
						text("table-link", "bg", "Link in a cell", AA_TEXT)),
				new Token("table-striped-link", "\"link-subtle\"", "Links in the striped rows",
						text("table-striped-link", "bg-subtle", "Link in a striped row", AA_TEXT)))));
		groups.add(new TokenGroup("Segments", "framework layer", List.of(
				new Token("segments-tab-link", "\"link\"", "Filter tab row above a table: tab text, line of the selected tab, line under the row",
						text("segments-tab-link", "bg", "Segment tab", AA_TEXT)))));
		groups.add(new TokenGroup("Portal and portlets", "framework layer", List.of(
				new Token("portlet-border", "\"border\"", "Border of a portlet",
						border("portlet-border", "bg", "text", "Portlet", NO_RATIO)),
				new Token("portlet-header-bg", "\"bg-subtle\"", "Background of the portlet header",
						fill("portlet-header-bg", "text", "Portlet header", AA_TEXT)))));
		return groups;
	}

	private static String fillStyle(String bg, String fg) {
		return "background-color: var(--o-" + bg + "); color: var(--o-" + fg + ");";
	}

	private static String borderStyle(String border, int width) {
		return " border: " + width + "px solid var(--o-" + border + ");";
	}

	/**
	 * @return A text token as text on its background
	 */
	private static Sample text(String token, String bg, String text, String minRatio) {
		return new Sample(fillStyle(bg, token), null, text, MEASURE_TEXT, minRatio);
	}

	/**
	 * @return A background token as surface with a text token on it
	 */
	private static Sample fill(String token, String fg, String text, String minRatio) {
		return new Sample(fillStyle(token, fg), null, text, MEASURE_TEXT, minRatio);
	}

	/**
	 * @return A border token as bordered box on a background
	 */
	private static Sample border(String token, String bg, String fg, String text, String minRatio) {
		return new Sample(fillStyle(bg, fg) + borderStyle(token, 2), null, text, MEASURE_BORDER, minRatio);
	}

	/**
	 * @return Every token of the tab: neutral steps, semantic, state and
	 * 		element tokens, without "--o-"
	 */
	public static List<String> allTokenNames() {
		List<String> names = new ArrayList<>();
		neutralSteps().forEach(step -> names.add(step.getName()));
		semanticGroups().forEach(group -> group.tokens().forEach(token -> names.add(token.name())));
		stateRows().forEach(row -> names.addAll(row.tokens()));
		elementGroups().forEach(group -> group.tokens().forEach(token -> names.add(token.name())));
		return names;
	}

	/**
	 * @return The inline styles of every sample of the tab, to check the
	 * 		custom properties they read
	 */
	public static List<String> allSampleStyles() {
		List<String> styles = new ArrayList<>();
		List<Sample> samples = new ArrayList<>();
		semanticGroups().forEach(group -> group.tokens().forEach(token -> samples.add(token.sample())));
		stateRows().forEach(row -> samples.addAll(row.samples()));
		elementGroups().forEach(group -> group.tokens().forEach(token -> samples.add(token.sample())));
		for (Sample sample : samples) {
			styles.add(sample.style());
			if (sample.hasInner()) {
				styles.add(sample.innerStyle());
			}
		}
		return styles;
	}

	public record NeutralStep(int step, String anchor) {

		public String getName() {
			return "neutral-" + step;
		}

		public String getProperty() {
			return "--o-neutral-" + step;
		}

		/**
		 * @return The text colour of the cell: dark on the light steps up to
		 * 		450, white on the dark steps
		 */
		public String getFg() {
			return step <= 450 ? "var(--o-neutral-900)" : "var(--o-neutral-0)";
		}
	}

	/**
	 * @param name The name of the group
	 * @param layer The token layer, null for the semantic tokens
	 * @param tokens The tokens
	 */
	public record TokenGroup(String name, String layer, List<Token> tokens) {
		//
	}

	/**
	 * @param name The token name without "--o-"
	 * @param defaultValue The default in _tokens.scss: the Sass variable, the
	 * 		reference "token" or the shade
	 * @param use What to use the token for
	 * @param sample The live sample
	 */
	public record Token(String name, String defaultValue, String use, Sample sample) {

		public String getProperty() {
			return "--o-" + name;
		}
	}

	/**
	 * @param style The inline style of the sample, only var(--o-*) colours
	 * @param innerStyle The inline style of an inner box with the text, or null
	 * @param text The text of the sample
	 * @param measure What the script measures: text, border, outline or nothing
	 * @param minRatio The contrast the measure needs, empty for no requirement
	 */
	public record Sample(String style, String innerStyle, String text, String measure, String minRatio) {

		public boolean hasInner() {
			return innerStyle != null;
		}
	}

	/**
	 * @param state The state
	 * @param tokens The state tokens in the order of {@link GuiDemoColorTokens#STATE_ROLES}
	 * @param samples Solid, soft and text on white
	 */
	public record StateRow(State state, List<String> tokens, List<Sample> samples) {
		//
	}
}

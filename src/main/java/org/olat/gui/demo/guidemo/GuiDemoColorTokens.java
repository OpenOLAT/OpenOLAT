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
				new Token("focus", "$o-focus-color: $brand-primary adjusted to 3:1 on $body-bg, or $input-border-focus of the theme",
						"Focus ring of every control on :focus-visible (3:1 on the page background)",
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
						text("btn-ghost-fg", "bg", "Ghost button", AA_TEXT)),
				new Token("btn-ghost-bg", "$o-button-ghost-background-color", "Fill of the ghost button (transparent)", fill("btn-ghost-bg", "btn-ghost-fg", "Ghost button", NO_RATIO)),
				new Token("btn-ghost-border", "$o-button-ghost-border-color", "Border of the ghost button (transparent)", border("btn-ghost-border", "bg", "btn-ghost-fg", "Ghost button", NO_RATIO)),
				new Token("btn-ghost-hover-fg", "$o-button-ghost-hover-color, follows link", "Text of the ghost button under the pointer", text("btn-ghost-hover-fg", "bg", "Ghost button, hover", AA_TEXT)),
				new Token("btn-ghost-hover-bg", "$o-button-ghost-hover-background-color", "Fill of the ghost button under the pointer", fill("btn-ghost-hover-bg", "btn-ghost-hover-fg", "Ghost button, hover", NO_RATIO)),
				new Token("btn-ghost-hover-border", "$o-button-ghost-hover-border-color, follows link", "Border of the ghost button under the pointer", border("btn-ghost-hover-border", "bg", "btn-ghost-hover-fg", "Ghost button, hover", NO_RATIO)),
				new Token("btn-mega-bg", "$o-button-mega-bg-color", "Fill of the mega button", fill("btn-mega-bg", "btn-default-fg", "Mega button", AA_TEXT)),
				new Token("btn-mega-hover-bg", "$o-button-mega-hover-bg-color", "Fill of the mega button under the pointer", fill("btn-mega-hover-bg", "btn-default-fg", "Mega button, hover", AA_TEXT)),
				new Token("btn-primary-light-bg", "$o-button-primary-light-bg", "Fill of the light primary button", fill("btn-primary-light-bg", "btn-primary-light-fg", "Primary light", AA_TEXT)),
				new Token("btn-primary-light-fg", "$o-button-primary-light-color", "Text of the light primary button", text("btn-primary-light-fg", "btn-primary-light-bg", "Primary light", AA_TEXT)),
				new Token("btn-primary-light-border", "$o-button-primary-light-border, follows btn-primary-light-fg", "Border of the light primary button", border("btn-primary-light-border", "btn-primary-light-bg", "btn-primary-light-fg", "Primary light", NO_RATIO)),
				new Token("btn-primary-light-hover-bg", "$o-button-primary-light-hover-bg, follows btn-primary-light-fg", "Fill under the pointer and with the focus", fill("btn-primary-light-hover-bg", "btn-primary-light-hover-fg", "Primary light, hover", AA_TEXT)),
				new Token("btn-primary-light-hover-fg", "$o-button-primary-light-hover-color, follows btn-primary-light-bg", "Text under the pointer and with the focus", text("btn-primary-light-hover-fg", "btn-primary-light-hover-bg", "Primary light, hover", AA_TEXT)),
				new Token("btn-primary-light-hover-border", "$o-button-primary-light-hover-border", "Border under the pointer and with the focus", border("btn-primary-light-hover-border", "btn-primary-light-hover-bg", "btn-primary-light-hover-fg", "Primary light, hover", NO_RATIO)),
				new Token("btn-primary-light-bg-hover", "shade: btn-primary-light-bg, 10% darker at most, 3% at least, the text keeps 4.5:1, lighter when it cannot get darker", "Fill when pressed", fill("btn-primary-light-bg-hover", "btn-primary-light-fg-hover", "Primary light, active", AA_TEXT)),
				new Token("btn-primary-light-bg-active", "shade: btn-primary-light-bg, 17% darker at most, 3% at least, the text keeps 4.5:1, lighter when it cannot get darker", "Fill when pressed under the pointer", fill("btn-primary-light-bg-active", "btn-primary-light-fg-active", "Primary light, active", AA_TEXT)),
				new Token("btn-primary-light-fg-hover", "$o-button-primary-light-color on the fill 10% darker, adjusted below 3%, follows btn-primary-light-fg", "Text when pressed", text("btn-primary-light-fg-hover", "btn-primary-light-bg-hover", "Primary light, active", AA_TEXT)),
				new Token("btn-primary-light-fg-active", "$o-button-primary-light-color on the fill 17% darker, adjusted below 3%, follows btn-primary-light-fg", "Text when pressed under the pointer", text("btn-primary-light-fg-active", "btn-primary-light-bg-active", "Primary light, active", AA_TEXT)),
				new Token("btn-primary-light-border-hover", "shade: btn-primary-light-border, 12% darker", "Border when pressed", border("btn-primary-light-border-hover", "btn-primary-light-bg-hover", "btn-primary-light-fg-hover", "Primary light, active", NO_RATIO)),
				new Token("btn-primary-light-border-focus", "shade: btn-primary-light-border, 25% darker", "Border when pressed with the focus", border("btn-primary-light-border-focus", "btn-primary-light-bg-hover", "btn-primary-light-fg-hover", "Primary light, focus", NO_RATIO)),
				new Token("btn-danger-light-bg", "$o-button-danger-light-bg", "Fill of the light danger button", fill("btn-danger-light-bg", "btn-danger-light-fg", "Danger light", AA_TEXT)),
				new Token("btn-danger-light-fg", "$o-button-danger-light-color", "Text of the light danger button", text("btn-danger-light-fg", "btn-danger-light-bg", "Danger light", AA_TEXT)),
				new Token("btn-danger-light-border", "$o-button-danger-light-border, follows btn-danger-light-fg", "Border of the light danger button", border("btn-danger-light-border", "btn-danger-light-bg", "btn-danger-light-fg", "Danger light", NO_RATIO)),
				new Token("btn-danger-light-hover-bg", "$o-button-danger-light-hover-bg, follows btn-danger-light-fg", "Fill under the pointer and with the focus", fill("btn-danger-light-hover-bg", "btn-danger-light-hover-fg", "Danger light, hover", AA_TEXT)),
				new Token("btn-danger-light-hover-fg", "$o-button-danger-light-hover-color, follows btn-danger-light-bg", "Text under the pointer and with the focus", text("btn-danger-light-hover-fg", "btn-danger-light-hover-bg", "Danger light, hover", AA_TEXT)),
				new Token("btn-danger-light-hover-border", "$o-button-danger-light-hover-border, follows btn-danger-light-fg", "Border under the pointer and with the focus", border("btn-danger-light-hover-border", "btn-danger-light-hover-bg", "btn-danger-light-hover-fg", "Danger light, hover", NO_RATIO)),
				new Token("btn-danger-light-bg-hover", "shade: btn-danger-light-bg, 10% darker at most, 3% at least, the text keeps 4.5:1, lighter when it cannot get darker", "Fill when pressed", fill("btn-danger-light-bg-hover", "btn-danger-light-fg-hover", "Danger light, active", AA_TEXT)),
				new Token("btn-danger-light-bg-active", "shade: btn-danger-light-bg, 17% darker at most, 3% at least, the text keeps 4.5:1, lighter when it cannot get darker", "Fill when pressed under the pointer", fill("btn-danger-light-bg-active", "btn-danger-light-fg-active", "Danger light, active", AA_TEXT)),
				new Token("btn-danger-light-fg-hover", "$o-button-danger-light-color on the fill 10% darker, adjusted below 3%, follows btn-danger-light-fg", "Text when pressed", text("btn-danger-light-fg-hover", "btn-danger-light-bg-hover", "Danger light, active", AA_TEXT)),
				new Token("btn-danger-light-fg-active", "$o-button-danger-light-color on the fill 17% darker, adjusted below 3%, follows btn-danger-light-fg", "Text when pressed under the pointer", text("btn-danger-light-fg-active", "btn-danger-light-bg-active", "Danger light, active", AA_TEXT)),
				new Token("btn-danger-light-border-hover", "shade: btn-danger-light-border, 12% darker", "Border when pressed", border("btn-danger-light-border-hover", "btn-danger-light-bg-hover", "btn-danger-light-fg-hover", "Danger light, active", NO_RATIO)),
				new Token("btn-danger-light-border-focus", "shade: btn-danger-light-border, 25% darker", "Border when pressed with the focus", border("btn-danger-light-border-focus", "btn-danger-light-bg-hover", "btn-danger-light-fg-hover", "Danger light, focus", NO_RATIO)))));
		groups.add(new TokenGroup("Content", "base layer", List.of(
				new Token("content-table-bordered-header-bg", "$gray-base", "Header cells of a bordered table in the HTML editor, the rule mixes it at 10%",
						new Sample("background-color: color-mix(in srgb, var(--o-content-table-bordered-header-bg) 10%, transparent); color: var(--o-text);",
								null, "Header cell", MEASURE_TEXT, AA_TEXT)))));
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
						text("toolbar-breadcrumb-link", "toolbar-breadcrumb-bg", "Breadcrumb link", AA_TEXT)),
				new Token("toolbar-link-disabled", "$o-toolbar-link-disabled-color, follows text-disabled", "Disabled tool in the toolbar",
						text("toolbar-link-disabled", "bg", "Disabled tool", NO_RATIO)),
				new Token("toolbar-nav-link-disabled", "$o-toolbar-nav-link-disabled-color, follows toolbar-link-disabled", "Disabled link of the toolbar navigation",
						text("toolbar-nav-link-disabled", "bg", "Disabled link", NO_RATIO)),
				new Token("toolbar-border", "$o-toolbar-border-color", "Border of the toolbar; the QTI menu and the page editor read it",
						border("toolbar-border", "bg", "text", "Toolbar", NO_RATIO)))));
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
						fill("tree-link-active-hover-bg", "tree-link-active-hover", "Selected, hover", AA_TEXT)),
				new Token("tree-border", "$o-tree-border-color", "Border of the tree, of its items and of the drag proxy",
						border("tree-border", "bg", "tree-link", "Tree", NO_RATIO)),
				new Token("tree-insertion-source-bg", "$o-tree-insertion-source-bg", "Fill of the item to move in the move dialog of a tree",
						fill("tree-insertion-source-bg", "tree-insertion-source-fg", "Item to move", AA_TEXT)))));
		groups.add(new TokenGroup("Table", "framework layer", List.of(
				new Token("table-header", "\"text\"", "Text of the table header",
						text("table-header", "bg", "Column header", AA_TEXT)),
				new Token("table-link", "\"link-subtle\"", "Links in table cells",
						text("table-link", "bg", "Link in a cell", AA_TEXT)),
				new Token("table-striped-link", "\"link-subtle\"", "Links in the striped rows",
						text("table-striped-link", "bg-subtle", "Link in a striped row", AA_TEXT)),
				new Token("table-details-bg", "$o-table-details-bg", "Fill of an expanded row and of its details row",
						fill("table-details-bg", "text", "Details row", AA_TEXT)),
				new Token("table-details-bg-hover", "shade: table-details-bg, 10% darker", "Expanded row and details row under the pointer",
						fill("table-details-bg-hover", "text", "Details row, hover", AA_TEXT)),
				new Token("table-sticky-col-edge", "shade: table-border, 10% darker", "Shadow line of a sticky column",
						border("table-sticky-col-edge", "bg", "text", "Sticky column", NO_RATIO)),
				new Token("table-row-selected-bg", "$o-table-row-selected-color, follows state-neutral-soft-bg", "Fill of a selected row",
						fill("table-row-selected-bg", "text", "Selected row", AA_TEXT)),
				new Token("table-edit-bg", "$o-table-edit-bg", "Fill of a table in the edit mode",
						fill("table-edit-bg", "text", "Edit mode", AA_TEXT)))));
		groups.add(new TokenGroup("Form elements", "framework layer", List.of(
				new Token("radio-card-hover-bg", "$o-radio-card-bg-hover", "Fill of a radio card under the pointer",
						fill("radio-card-hover-bg", "text", "Radio card, hover", AA_TEXT)),
				new Token("radio-card-hover-border", "$o-radio-card-border-color-hover", "Border of a radio card under the pointer",
						border("radio-card-hover-border", "radio-card-hover-bg", "text", "Radio card, hover", NO_RATIO)),
				new Token("radio-card-checked-bg", "$o-radio-card-bg-checked", "Fill of a checked radio card",
						fill("radio-card-checked-bg", "radio-card-checked-label", "Checked", NO_RATIO)),
				new Token("radio-card-checked-border", "$o-radio-card-border-color-checked", "Border of a checked radio card",
						border("radio-card-checked-border", "radio-card-checked-bg", "radio-card-checked-label", "Checked", NO_RATIO)),
				new Token("radio-card-checked-label", "$o-radio-card-label-color-checked", "Label of a checked radio card (3.88:1 on its fill in light)",
						text("radio-card-checked-label", "radio-card-checked-bg", "Checked", NO_RATIO)),
				new Token("panel-placeholder-border", "$o-panel-placeholder-border-color", "Dashed border of a drop place",
						border("panel-placeholder-border", "bg", "text", "Drop place", NO_RATIO)),
				new Token("panel-placeholder-border-hover", "shade: panel-placeholder-border, 10% darker", "Border of a drop place under the pointer",
						border("panel-placeholder-border-hover", "bg", "text", "Drop place, hover", NO_RATIO)),
				new Token("panel-placeholder-fg", "$o-panel-placeholder-color", "Heading text of a drop place",
						text("panel-placeholder-fg", "panel-placeholder-bg", "Drop place", AA_TEXT)),
				new Token("icon-mandatory", "$o-base-orange-color", "Mandatory marker next to a label",
						text("icon-mandatory", "bg", "*", AA_NON_TEXT)),
				new Token("chelp-tooltip", "$o-chelp-tooltip-color, follows text-muted", "Help icon next to a label and help tooltip",
						text("chelp-tooltip", "bg", "Help", AA_TEXT)))));
		groups.add(new TokenGroup("Popover, offcanvas and brand box", "framework layer", List.of(
				new Token("popover-title-bg", "$popover-title-bg", "Fill of the title of a popover",
						fill("popover-title-bg", "text", "Popover title", AA_TEXT)),
				new Token("popover-title-bg-edge", "shade: popover-title-bg, 5% darker", "Line under the title of a popover",
						border("popover-title-bg-edge", "popover-title-bg", "text", "Popover title", NO_RATIO)),
				new Token("offcanvas-logout", "$o-offcanvas-logout-color", "Logout link of the offcanvas menu (dark menu)",
						text("offcanvas-logout", "text", "Logout", NO_RATIO)),
				new Token("offcanvas-logout-hover", "$o-offcanvas-logout-hover-color", "Logout link under the pointer",
						text("offcanvas-logout-hover", "text", "Logout, hover", NO_RATIO)),
				new Token("offcanvas-link-hover", "$o-offcanvas-link-hover-color", "Link of the offcanvas menu under the pointer (5.18:1 on the menu in light)",
						text("offcanvas-link-hover", "text", "Link, hover", NO_RATIO)),
				new Token("offcanvas-link", "$o-offcanvas-link-color", "Link of the offcanvas menu (7.01:1 on the menu in light)",
						text("offcanvas-link", "offcanvas-dropdown-bg", "Link", AA_TEXT)),
				new Token("brand-box-border", "$o-brand-box-border", "Border of the brand box (file suffix, missing visual)",
						border("brand-box-border", "bg", "primary-text", "Brand box", NO_RATIO)))));
		groups.add(new TokenGroup("Charts", "framework layer", List.of(
				new Token("chart-bar-light", "$o-d3chart-light", "Light bar of the default bar chart",
						fill("chart-bar-light", "text", "Bar", NO_RATIO)),
				new Token("chart-bar-dark", "$o-d3chart-dark", "Dark bar of the default bar chart",
						fill("chart-bar-dark", "white", "Bar", NO_RATIO)),
				new Token("box-plot-quartile-fill", "$o-box-plot-quartile-fill", "Box between the quartiles of a box plot",
						fill("box-plot-quartile-fill", "text", "Quartiles", NO_RATIO)))));
		groups.add(new TokenGroup("Segments", "framework layer", List.of(
				new Token("segments-tab-link", "\"link\"", "Filter tab row above a table: tab text, line of the selected tab, line under the row",
						text("segments-tab-link", "bg", "Segment tab", AA_TEXT)))));
		groups.add(new TokenGroup("Message boxes", "framework layer", List.of(
				new Token("box-info-border", "$o-box-info-color, follows state-neutral-bg", "Border of the info box (.o_info)",
						border("box-info-border", "bg", "text", "Info box", NO_RATIO)),
				new Token("box-info-text", "$o-box-info-color, text role, follows state-neutral-text", "Icon and coloured text of the info box",
						text("box-info-text", "bg", "Info", AA_TEXT)),
				new Token("box-success-border", "$o-box-success-color, follows state-success-bg", "Border of the success box (.o_success)",
						border("box-success-border", "bg", "text", "Success box", NO_RATIO)),
				new Token("box-success-text", "$o-box-success-color, text role, follows state-success-text", "Icon and coloured text of the success box",
						text("box-success-text", "bg", "Success", AA_TEXT)),
				new Token("box-warning-border", "$o-box-warning-color, follows state-warning-bg", "Border of the warning box (.o_warning)",
						border("box-warning-border", "bg", "text", "Warning box", NO_RATIO)),
				new Token("box-warning-text", "$o-box-warning-color, text role, follows state-warning-text", "Icon and coloured text of the warning box",
						text("box-warning-text", "bg", "Warning", AA_TEXT)),
				new Token("box-error-border", "$o-box-error-color, follows state-danger-bg", "Border of the error box (.o_error)",
						border("box-error-border", "bg", "text", "Error box", NO_RATIO)),
				new Token("box-error-text", "$o-box-error-color, text role, follows state-danger-text", "Icon and coloured text of the error box",
						text("box-error-text", "bg", "Error", AA_TEXT)),
				new Token("box-info-bg", "$o-box-info-bg", "Fill of the info box and of the grey table (b_gray)",
						fill("box-info-bg", "text", "Info box", AA_TEXT)),
				new Token("box-note-bg", "$o-box-note-bg", "Fill of the note box and of the blue table (b_blue)",
						fill("box-note-bg", "text", "Note box", AA_TEXT)),
				new Token("box-success-bg", "$o-box-success-bg", "Fill of the success box and of the green table (b_green)",
						fill("box-success-bg", "text", "Success box", AA_TEXT)),
				new Token("box-warning-bg", "$o-box-warning-bg", "Fill of the warning box and of the yellow table (b_yellow)",
						fill("box-warning-bg", "text", "Warning box", AA_TEXT)),
				new Token("box-error-bg", "$o-box-error-bg", "Fill of the error box and of the red table (b_red)",
						fill("box-error-bg", "text", "Error box", AA_TEXT)),
				new Token("box-error-table-header-bg", "shade: box-error-bg, 10% darker", "Header cells of a table in a box (one per box)",
						fill("box-error-table-header-bg", "text", "Header cell", AA_TEXT)),
				new Token("box-note-border", "$o-box-note-color", "Border and icon of the note box (.o_note)",
						border("box-note-border", "box-note-bg", "text", "Note box", AA_NON_TEXT)),
				new Token("box-note-text", "$o-box-note-color, follows box-note-border", "Text of a note message of the video task",
						text("box-note-text", "box-note-bg", "Note", AA_TEXT)),
				new Token("box-tip-border", "$o-box-tip-color", "Border and icon of the tip box (.o_tip)",
						border("box-tip-border", "box-tip-bg", "text", "Tip box", AA_NON_TEXT)),
				new Token("box-important-border", "$o-box-important-color", "Border and icon of the important box (.o_important, 2.13:1 on its fill in light)",
						border("box-important-border", "box-important-bg", "text", "Important box", NO_RATIO)),
				new Token("message-line-warning-bg", "$o-line-warning-bg", "Fill of the warning line (.o_warning_line)",
						fill("message-line-warning-bg", "message-line-warning-fg", "Warning line", AA_TEXT)),
				new Token("message-line-error-bg", "$o-line-error-bg", "Fill of the error line (.o_error_line)",
						fill("message-line-error-bg", "message-line-error-fg", "Error line", AA_TEXT)))));
		groups.add(new TokenGroup("Portal and portlets", "framework layer", List.of(
				new Token("portlet-border", "\"border\"", "Border of a portlet",
						border("portlet-border", "bg", "text", "Portlet", NO_RATIO)),
				new Token("portlet-header-bg", "\"bg-subtle\"", "Background of the portlet header",
						fill("portlet-header-bg", "text", "Portlet header", AA_TEXT)),
				new Token("portlet-edit-bg", "$o-portlet-edit-bg, follows state-warning-soft-bg", "Background of a portlet in the edit mode",
						fill("portlet-edit-bg", "text", "Portlet, edit mode", AA_TEXT)),
				new Token("portlet-edit-border", "$o-portlet-edit-border, follows state-warning-soft-border", "Border of a portlet in the edit mode",
						border("portlet-edit-border", "portlet-edit-bg", "text", "Portlet, edit mode", NO_RATIO)))));
		groups.add(new TokenGroup("Table filter", "framework layer", List.of(
				new Token("table-filter-fg", "$o-table-filter-color, follows btn-primary-border", "Text of a filter button above a table", text("table-filter-fg", "table-filter-bg", "Filter", AA_TEXT)),
				new Token("table-filter-bg", "$o-table-filter-bg", "Fill of a filter button", fill("table-filter-bg", "table-filter-fg", "Filter", AA_TEXT)),
				new Token("table-filter-border", "$o-table-filter-border, follows btn-primary-border", "Border of a filter button", border("table-filter-border", "table-filter-bg", "table-filter-fg", "Filter", NO_RATIO)),
				new Token("table-filter-active-fg", "$o-table-filter-active-color", "Text of an active or open filter", text("table-filter-active-fg", "table-filter-active-bg", "Active filter", AA_TEXT)),
				new Token("table-filter-active-bg", "$o-table-filter-active-bg, follows btn-primary-border", "Fill of an active or open filter", fill("table-filter-active-bg", "table-filter-active-fg", "Active filter", AA_TEXT)),
				new Token("table-filter-active-border", "$o-table-filter-active-border, follows btn-primary-border", "Border of an active or open filter", border("table-filter-active-border", "table-filter-active-bg", "table-filter-active-fg", "Active filter", NO_RATIO)),
				new Token("table-filter-bg-hover", "shade: table-filter-bg, 10% darker at most, 3% at least, the text keeps 4.5:1, lighter when it cannot get darker", "Fill under the pointer", fill("table-filter-bg-hover", "table-filter-fg-hover", "Filter, hover", AA_TEXT)),
				new Token("table-filter-bg-active", "shade: table-filter-bg, 17% darker at most, 3% at least, the text keeps 4.5:1, lighter when it cannot get darker", "Fill when pressed", fill("table-filter-bg-active", "table-filter-fg-active", "Filter, active", AA_TEXT)),
				new Token("table-filter-fg-hover", "$o-table-filter-color on the fill 10% darker, adjusted below 3%, follows table-filter-fg", "Text under the pointer", text("table-filter-fg-hover", "table-filter-bg-hover", "Filter, hover", AA_TEXT)),
				new Token("table-filter-fg-active", "$o-table-filter-color on the fill 17% darker, adjusted below 3%, follows table-filter-fg", "Text when pressed", text("table-filter-fg-active", "table-filter-bg-active", "Filter, active", AA_TEXT)),
				new Token("table-filter-border-hover", "shade: table-filter-border, 12% darker", "Border under the pointer", border("table-filter-border-hover", "table-filter-bg-hover", "table-filter-fg-hover", "Filter, hover", NO_RATIO)),
				new Token("table-filter-border-focus", "shade: table-filter-border, 25% darker", "Border with the keyboard focus", border("table-filter-border-focus", "table-filter-bg-hover", "table-filter-fg-hover", "Filter, focus", NO_RATIO)),
				new Token("table-filter-active-bg-hover", "shade: table-filter-active-bg, 10% darker at most, 3% at least, the text keeps 4.5:1, lighter when it cannot get darker", "Fill under the pointer", fill("table-filter-active-bg-hover", "table-filter-active-fg-hover", "Active filter, hover", AA_TEXT)),
				new Token("table-filter-active-bg-active", "shade: table-filter-active-bg, 17% darker at most, 3% at least, the text keeps 4.5:1, lighter when it cannot get darker", "Fill when pressed", fill("table-filter-active-bg-active", "table-filter-active-fg-active", "Active filter, active", AA_TEXT)),
				new Token("table-filter-active-fg-hover", "$o-table-filter-active-color on the fill 10% darker, adjusted below 3%, follows table-filter-active-fg", "Text under the pointer", text("table-filter-active-fg-hover", "table-filter-active-bg-hover", "Active filter, hover", AA_TEXT)),
				new Token("table-filter-active-fg-active", "$o-table-filter-active-color on the fill 17% darker, adjusted below 3%, follows table-filter-active-fg", "Text when pressed", text("table-filter-active-fg-active", "table-filter-active-bg-active", "Active filter, active", AA_TEXT)),
				new Token("table-filter-active-border-hover", "shade: table-filter-active-border, 12% darker", "Border under the pointer", border("table-filter-active-border-hover", "table-filter-active-bg-hover", "table-filter-active-fg-hover", "Active filter, hover", NO_RATIO)),
				new Token("table-filter-active-border-focus", "shade: table-filter-active-border, 25% darker", "Border with the keyboard focus", border("table-filter-active-border-focus", "table-filter-active-bg-hover", "table-filter-active-fg-hover", "Active filter, focus", NO_RATIO)))));
		groups.add(new TokenGroup("Toggle switch", "framework layer", List.of(
				new Token("toggle-border", "$o-toggle-border-color", "Border of the toggle switch", border("toggle-border", "toggle-bg", "toggle-text", "Off", NO_RATIO)),
				new Token("toggle-bg", "$o-toggle-bg", "Track of the toggle switch in the off position", fill("toggle-bg", "toggle-text", "Off", AA_TEXT)),
				new Token("toggle-handle", "$o-toggle-color", "Handle of the toggle switch in the off position", text("toggle-handle", "toggle-bg", "Handle", AA_NON_TEXT)),
				new Token("toggle-on-handle", "$o-toggle-on-color", "Handle of the toggle switch in the on position", text("toggle-on-handle", "toggle-on-bg", "Handle", AA_NON_TEXT)),
				new Token("toggle-on-text", "$o-toggle-on-text-color", "Text of the toggle switch in the on position", text("toggle-on-text", "toggle-on-bg", "On", AA_TEXT)))));
		groups.add(new TokenGroup("Scope toggle", "framework layer", List.of(
				new Token("scope-toggle-bg", "$o-scope-toggle-bg-color", "Fill of a scope toggle", fill("scope-toggle-bg", "scope-toggle-fg", "Scope", AA_TEXT)),
				new Token("scope-toggle-hover-bg", "$o-scope-toggle-bg-hover-color", "Fill of a scope toggle under the pointer", fill("scope-toggle-hover-bg", "scope-toggle-fg", "Scope, hover", AA_TEXT)),
				new Token("scope-toggle-on-fg", "$o-scope-toggle-on-color", "Text of the selected scope", text("scope-toggle-on-fg", "scope-toggle-on-bg", "Selected scope", AA_TEXT)),
				new Token("scope-toggle-on-border", "$o-scope-toggle-on-border-color", "Border of the selected scope", border("scope-toggle-on-border", "scope-toggle-on-bg", "scope-toggle-on-fg", "Selected scope", NO_RATIO)),
				new Token("scope-toggle-on-hover-bg", "$o-scope-toggle-on-bg-hover-color", "Fill of the selected scope under the pointer", fill("scope-toggle-on-hover-bg", "scope-toggle-on-hover-fg", "Selected, hover", AA_TEXT)),
				new Token("scope-toggle-on-hover-fg", "$o-scope-toggle-on-color on the hover fill, adjusted when the fill is 10% lighter, follows scope-toggle-on-fg", "Text of the selected scope under the pointer", text("scope-toggle-on-hover-fg", "scope-toggle-on-hover-bg", "Selected, hover", AA_TEXT)))));
		groups.add(new TokenGroup("Navigation bar icons, focus rings of navigation bar and footer", "framework layer", List.of(
				new Token("navbar-tab-close", "$o-navbar-tab-close-color, follows state-danger-text", "Close icon of a tab in the navigation bar, adjusted to 3:1 on the navigation bar", text("navbar-tab-close", "navbar-bg", "Close", AA_NON_TEXT)),
				new Token("navbar-tab-close-hover", "$o-navbar-tab-close-hover-color, follows state-danger-text-hover", "Close icon under the pointer: darker, on a dark navigation bar the close colour, adjusted to 3:1", text("navbar-tab-close-hover", "navbar-bg", "Close, hover", AA_NON_TEXT)),
				new Token("dropdown-tab-close", "$o-navbar-tab-close-color, follows state-danger-text", "Close icon of a tab in the More dropdown", text("dropdown-tab-close", "bg-raised", "Close", AA_NON_TEXT)),
				new Token("dropdown-tab-close-hover", "$o-navbar-tab-close-hover-color, follows state-danger-text-hover", "Close icon of a tab in the More dropdown under the pointer", text("dropdown-tab-close-hover", "bg-raised", "Close, hover", AA_NON_TEXT)),
				new Token("navbar-focus", "$o-focus-color adjusted to 3:1 on the navigation bar, follows focus", "Focus ring of the items of the navigation bar (inset)",
						new Sample("background-color: var(--o-navbar-bg); padding: 6px;",
								"display: inline-block; padding: 4px 8px; outline: 2px solid var(--o-navbar-focus); outline-offset: -2px; color: var(--o-navbar-link);",
								"Navbar item, focus", MEASURE_OUTLINE, AA_NON_TEXT)),
				new Token("footer-focus", "$o-focus-color, else $o-footer-default-link-color, adjusted to 3:1 on the footer, follows focus or footer-link", "Focus ring of the links in the footer",
						new Sample("background-color: var(--o-footer-inner-bg); padding: 6px;",
								"display: inline-block; padding: 2px 6px; outline: 2px solid var(--o-footer-focus); outline-offset: 2px; color: var(--o-footer-link);",
								"Footer link, focus", MEASURE_OUTLINE, AA_NON_TEXT)))));
		groups.add(new TokenGroup("Tags", "framework layer", List.of(
				new Token("tag-selected-fg", "\"btn-dirty-fg\"", "Text of a selected tag on the fill of the dirty button", text("tag-selected-fg", "tag-selected-bg", "Selected tag", AA_TEXT)))));
		groups.add(new TokenGroup("Empty state, icon panel, date, figure cards, day navigation and radial progress", "framework layer", List.of(
				new Token("icon-panel-content-bg", "$o-icon-panel-content-bg-color, follows panel-bg", "Fill of the content column of an icon panel",
						fill("icon-panel-content-bg", "text", "Content column", AA_TEXT)),
				new Token("datecomp-month-fg", "$o-datecomp-month-color", "Text of the month of the date component",
						text("datecomp-month-fg", "datecomp-month-bg", "Nov", AA_TEXT)),
				new Token("icon-panel-icon-bg", "$o-icon-panel-icon-bg-color", "Fill of the icon column of an icon panel",
						fill("icon-panel-icon-bg", "text", "Icon column", AA_TEXT)),
				new Token("figure-card-bg", "$o-figure-card-bg-color", "Fill of a figure card",
						fill("figure-card-bg", "text", "Figure card", AA_TEXT)),
				new Token("day-nav-day-selected-bg", "$o-day-nav-day-selected-bg-color", "Fill and border of the selected day of the day navigation",
						fill("day-nav-day-selected-bg", "day-nav-day-selected-fg", "Selected day", AA_TEXT)),
				new Token("radial-progress-number", "$o-radial-progress-number-color, follows progress-bar-bg", "Number inside a radial progress",
						text("radial-progress-number", "radial-progress-inset-bg", "75%", AA_TEXT)),
				new Token("colored-icon-orange", "$o-base-orange-color", "Icon with the class o_base_orange",
						text("colored-icon-orange", "bg", "Icon", AA_NON_TEXT)))));
		groups.add(new TokenGroup("Toolbar, tags, competences, dropdown and test texts", "framework and component layer", List.of(
				new Token("tag-fg", "$o-tag-font-color", "Text of a tag",
						text("tag-fg", "tag-bg", "Tag", AA_TEXT)),
				new Token("competence-fg", "$o-competences-font-color, follows competence-border", "Text of a competence",
						text("competence-fg", "competence-bg", "Competence", AA_TEXT)),
				new Token("dropdown-link-active", "$dropdown-link-active-color", "Text of the active item of a dropdown menu with a label",
						text("dropdown-link-active", "dropdown-link-active-bg", "Active item", AA_TEXT)),
				new Token("toolbar-nav-bg", "$o-toolbar-nav-bg-color", "Fill of the next and previous buttons of the toolbar",
						fill("toolbar-nav-bg", "toolbar-nav-link", "Next", AA_TEXT)),
				new Token("qti-text-entry-filled-bg", "$o-qti-textEntryInteraction-filled-bg, follows state-neutral-soft-bg", "Fill of a text entry field with a value in the HTML editor",
						fill("qti-text-entry-filled-bg", "qti-text-entry-filled-fg", "Answer", AA_TEXT)),
				new Token("qti-bad-response", "$o-qti-badResponse-color, follows state-danger-text", "Text of a bad response of a test question",
						text("qti-bad-response", "bg", "Bad response", AA_TEXT)),
				new Token("lecture-excluded", "$o-lm-excluded-color, follows state-danger-text", "Text of a participant excluded from the lectures",
						text("lecture-excluded", "bg", "Excluded", AA_TEXT)))));
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

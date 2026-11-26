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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.core.commons.services.color.ColorContrast;
import org.olat.core.commons.services.color.ColorFamily;
import org.olat.core.commons.services.color.ColorService;
import org.olat.core.commons.services.color.ColorSet;
import org.olat.core.commons.services.color.Palette;
import org.olat.core.commons.services.color.Swatch;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.modules.forms.ui.ReportHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Shows the colour palette of the current theme, the neutral scale and the
 * colour tokens, the swatch roles, how to use them, and which palette
 * colours each colour chooser uses. The texts
 * are not translated, the page is for developers and designers.
 *
 * Initial date: 1 Nov 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class GuiDemoColorsController extends BasicController {

	private GuiDemoColorChoosersController choosersCtrl;

	@Autowired
	private ColorService colorService;

	public GuiDemoColorsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		VelocityContainer mainVC = createVelocityContainer("guidemo-colors");
		putInitialPanel(mainVC);

		Palette palette = colorService.getPalette();
		mainVC.contextPut("hasPalette", Boolean.valueOf(palette != null));
		if (palette == null) {
			return;
		}

		List<Chooser> choosers = initChoosers(palette);
		List<HueRow> rows = initRows(palette);

		TabbedPane tabPane = new TabbedPane("tabs", getLocale());
		mainVC.put("tabs", tabPane);

		VelocityContainer paletteVC = createVelocityContainer("guidemo-colors-palette");
		paletteVC.contextPut("rows", rows);
		paletteVC.contextPut("steps", Palette.ALL_STEPS);
		paletteVC.contextPut("neutral", GuiDemoColorTokens.neutralSteps());
		tabPane.addTab("Palette", paletteVC);

		VelocityContainer tokensVC = createVelocityContainer("guidemo-colors-tokens");
		tokensVC.contextPut("neutral", GuiDemoColorTokens.neutralSteps());
		tokensVC.contextPut("semantic", GuiDemoColorTokens.semanticGroups());
		tokensVC.contextPut("roles", GuiDemoColorTokens.STATE_ROLES);
		tokensVC.contextPut("states", GuiDemoColorTokens.stateRows());
		tokensVC.contextPut("elements", GuiDemoColorTokens.elementGroups());
		tabPane.addTab("Tokens", tokensVC);

		VelocityContainer swatchesVC = createVelocityContainer("guidemo-colors-swatches");
		swatchesVC.contextPut("rows", rows);
		tabPane.addTab("Swatches", swatchesVC);

		VelocityContainer badgesVC = createVelocityContainer("guidemo-colors-badges");
		badgesVC.contextPut("states", GuiDemoColorBadges.states());
		badgesVC.contextPut("groups", GuiDemoColorBadges.groups());
		tabPane.addTab("Badges / Status", badgesVC);

		VelocityContainer usageVC = createVelocityContainer("guidemo-colors-usage");
		tabPane.addTab("Sources and usage", usageVC);

		choosersCtrl = new GuiDemoColorChoosersController(ureq, wControl, rows, choosers);
		listenTo(choosersCtrl);
		tabPane.addTab("Choosers", choosersCtrl.getInitialComponent());
	}

	private List<HueRow> initRows(Palette palette) {
		List<String> themeColors = palette.getThemeColors();
		List<String> hues = new ArrayList<>(palette.getHues());
		hues.addAll(themeColors);
		List<HueRow> rows = new ArrayList<>();
		for (String hue : hues) {
			// yellow and a theme colour with a light brand colour have dark text on the solid fill
			boolean darkText = !Palette.WHITE.equals(palette.fg(new Swatch(hue, Swatch.SOLID)));
			List<Cell> cells = new ArrayList<>();
			for (Integer step : Palette.ALL_STEPS) {
				if (!palette.getSteps(hue).contains(step)) {
					continue;
				}
				String hex = palette.hex(hue, step);
				boolean fill = Palette.isFillStep(step.intValue());
				String dark = palette.hex(hue, 900);
				// a fill step has no text colour: the label takes the better of white and step 900
				String fg = ColorContrast.contrast(Palette.WHITE, hex) >= Palette.MIN_CONTRAST
						|| (fill && ColorContrast.contrast(Palette.WHITE, hex) > ColorContrast.contrast(dark, hex))
						? Palette.WHITE : dark;
				String ratio = String.format(Locale.ENGLISH, "%.1f", ColorContrast.contrast(fg, hex));
				String onWhite = String.format(Locale.ENGLISH, "%.1f", ColorContrast.contrast(Palette.WHITE, hex));
				String roles = fill ? "" : roles(palette, hue, step.intValue());
				Cell cell = new Cell(hue, step.intValue(), hex, fg, ratio, onWhite, roles, fill);
				cells.add(cell);
			}
			String label = hue;
			boolean themeColor = themeColors.contains(hue);
			if (themeColor) {
				label = themeColors.size() == 1 ? "Theme" : "Theme " + hue.substring(Palette.THEME_COLOR_PREFIX.length());
			}
			rows.add(new HueRow(hue, label, themeColor, darkText, cells));
		}
		return rows;
	}

	private String roles(Palette palette, String hue, int step) {
		List<String> roles = new ArrayList<>();
		if (step == 50) roles.add("soft-bg");
		if (step == 100) roles.add("soft-border");
		if (step == palette.getBgStep(new Swatch(hue, Swatch.TINT))) roles.add("tint");
		if (step == palette.getBgStep(new Swatch(hue, Swatch.LIGHT))) roles.add("light");
		if (step == palette.getBgStep(new Swatch(hue, Swatch.SOLID))) roles.add(Palette.isThemeColor(hue) ? "solid brand" : "solid");
		if (step == palette.getBgStep(new Swatch(hue, Swatch.DARK))) roles.add("dark");
		if (palette.hex(hue, step).equals(palette.text(hue))) roles.add("text");
		if (step == 700) roles.add("soft-fg");
		return String.join(" ", roles);
	}

	private List<Chooser> initChoosers(Palette palette) {
		List<Chooser> choosers = new ArrayList<>();

		// The three chooser sets, each with a real picker: the stored value is the key of the set
		Chooser setS = new Chooser("set_s", "Set S", "ColorSet.S, solid swatch of 7 hues and theme1",
				List.of("No chooser uses it yet"));
		addSetKeys(setS, palette, ColorSet.S);
		choosers.add(setS);

		Chooser setM = new Chooser("set_m", "Set M", "ColorSet.M, solid swatch of 14 hues and all theme colors",
				List.of("Course and course element: color category; the administration gives each category a color"
						+ " of the set M (o_swatch_{key}), the predefined categories keep their old classes",
						"Administration: Courses, color categories",
						"Calendar: event color and color of a calendar in the personal settings",
						"Projects: appointments and milestones, also through the MCP tools appointment_modify"
						+ " and milestone_modify",
						"Random at creation, hues of the set M only: project avatars and user initials",
						"Room management: building color",
						"Video editor: annotations, comments, questions and segment categories",
						"Page and form editor: block background, custom alert box",
						"Page editor and HTML editor: table style b_{key} (b_gray_hue for gray, b_gray is the old style)",
						"Open Badges: badge color, after gold, silver and bronze",
						"HTML editor (TinyMCE): color map of the text and background colors, the neutral steps"
						+ " and the colors of the set M"));
		addSetKeys(setM, palette, ColorSet.M);
		choosers.add(setM);

		Chooser setL = new Chooser("set_l", "Set L", "ColorSet.L, 5 variants of 14 hues and all theme colors",
				List.of("No chooser uses it yet"));
		addSetKeys(setL, palette, ColorSet.L);
		choosers.add(setL);

		// No chooser: a fixed use of the palette
		Chooser series = new Chooser("series", "Report chart series (no chooser)", "ReportHelper, solid fill of 13 hues",
				List.of("Evaluation forms and surveys: report charts, one color per series"));
		series.setNoPickerHint("No chooser: the series take the hues in a fixed order.");
		for (String hue : ReportHelper.getSeriesHues()) {
			series.addKey(palette, hue, "o_swatch_" + hue, new Swatch(hue, Swatch.SOLID));
		}
		choosers.add(series);

		return choosers;
	}

	private void addSetKeys(Chooser chooser, Palette palette, ColorSet colorSet) {
		for (String key : colorService.getColors(colorSet)) {
			chooser.addKey(palette, key, colorService.toSwatchCssClass(key), colorService.resolve(key, ColorFamily.SWATCH));
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// the chooser selection runs in the browser
	}

	public static class HueRow {

		private final String hue;
		private final String label;
		private final boolean themeColor;
		private final boolean darkText;
		private final List<Cell> cells;

		public HueRow(String hue, String label, boolean themeColor, boolean darkText, List<Cell> cells) {
			this.hue = hue;
			this.label = label;
			this.themeColor = themeColor;
			this.darkText = darkText;
			this.cells = cells;
		}

		public String getHue() {
			return hue;
		}

		public String getLabel() {
			return label;
		}

		/**
		 * @return true for a brand colour of the theme (theme1 ... themeN)
		 */
		public boolean isThemeColor() {
			return themeColor;
		}

		/**
		 * @return true if the solid fill has dark text (yellow, a theme colour
		 * 		with a light brand colour)
		 */
		public boolean isDarkText() {
			return darkText;
		}

		public List<Cell> getCells() {
			return cells;
		}
	}

	public static class Cell {

		private final String hue;
		private final int step;
		private final String hex;
		private final String fg;
		private final String ratio;
		private final String onWhite;
		private final String roles;
		private final boolean fill;

		public Cell(String hue, int step, String hex, String fg, String ratio, String onWhite, String roles, boolean fill) {
			this.hue = hue;
			this.step = step;
			this.hex = hex;
			this.fg = fg;
			this.ratio = ratio;
			this.onWhite = onWhite;
			this.roles = roles;
			this.fill = fill;
		}

		public String getId() {
			return hue + "-" + step;
		}

		public String getHue() {
			return hue;
		}

		public int getStep() {
			return step;
		}

		public String getHex() {
			return hex;
		}

		public String getFg() {
			return fg;
		}

		public String getRatio() {
			return ratio;
		}

		public String getOnWhite() {
			return onWhite;
		}

		public String getRoles() {
			return roles;
		}

		/**
		 * @return true for a fill step (450): a fill or shape colour only, no
		 * 		swatch role and no colour chooser colour
		 */
		public boolean isFill() {
			return fill;
		}
	}

	public static class Chooser {

		private final String id;
		private final String name;
		private final String source;
		private final List<String> usedIn;
		private String picker;
		private String noPickerHint;
		private final List<ChooserKey> keys = new ArrayList<>();
		private final List<ChooserKey> unmapped = new ArrayList<>();
		private final Map<String,List<String>> cellKeys = new LinkedHashMap<>();

		public Chooser(String id, String name, String source, List<String> usedIn) {
			this.id = id;
			this.name = name;
			this.source = source;
			this.usedIn = usedIn;
		}

		void addKey(Palette palette, String key, String cssClass, Swatch swatch) {
			if (swatch == null || !palette.contains(swatch.hue())) {
				unmapped.add(new ChooserKey(key, cssClass, null, 0, null));
				return;
			}
			int step = palette.getBgStep(swatch);
			ChooserKey chooserKey = new ChooserKey(key, cssClass, swatch, step, palette.bg(swatch));
			keys.add(chooserKey);
			cellKeys.computeIfAbsent(chooserKey.getCellId(), k -> new ArrayList<>()).add(key);
		}

		public String getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public String getSource() {
			return source;
		}

		public List<String> getUsedIn() {
			return usedIn;
		}

		/**
		 * @return The name of the form item with the real color picker, or null
		 */
		public String getPicker() {
			return picker;
		}

		public void setPicker(String picker) {
			this.picker = picker;
		}

		public String getNoPickerHint() {
			return noPickerHint;
		}

		public void setNoPickerHint(String noPickerHint) {
			this.noPickerHint = noPickerHint;
		}

		public List<ChooserKey> getKeys() {
			return keys;
		}

		public List<ChooserKey> getUnmapped() {
			return unmapped;
		}

		public int getNumOfCells() {
			return cellKeys.size();
		}

		/**
		 * @return The cells and their keys, e.g. "blue-500:lightblue blue-700:navy",
		 * 		read by the script of the chooser tab
		 */
		public String getCellKeysAsString() {
			StringBuilder sb = new StringBuilder();
			for (Map.Entry<String,List<String>> entry : cellKeys.entrySet()) {
				if (sb.length() > 0) sb.append(' ');
				sb.append(entry.getKey()).append(':').append(String.join(",", entry.getValue()));
			}
			return sb.toString();
		}
	}

	public static class ChooserKey {

		private final String key;
		private final String cssClass;
		private final Swatch swatch;
		private final int step;
		private final String hex;

		public ChooserKey(String key, String cssClass, Swatch swatch, int step, String hex) {
			this.key = key;
			this.cssClass = cssClass;
			this.swatch = swatch;
			this.step = step;
			this.hex = hex;
		}

		public String getKey() {
			return key;
		}

		public String getCssClass() {
			return cssClass;
		}

		public String getHue() {
			return swatch == null ? null : swatch.hue();
		}

		public String getVariant() {
			return swatch == null ? null : swatch.variant();
		}

		public int getStep() {
			return step;
		}

		public String getHex() {
			return hex;
		}

		public String getCellId() {
			return swatch == null ? null : swatch.hue() + "-" + step;
		}
	}
}

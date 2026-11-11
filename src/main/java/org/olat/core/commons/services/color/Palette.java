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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;

/**
 * The colour palette for places where a CSS class can not be used, e.g. the
 * background of a badge SVG or the series of a chart. Java reads the values
 * from the custom properties <code>--o-{hue}-{step}</code> in the
 * <code>theme.css</code> of the selected theme, fallback <code>light</code>
 * (see {@link org.olat.core.gui.themes.Theme#getPalette()}). The theme emits
 * them from <code>$o-palette</code> in <code>themes/light/_palette.scss</code>.
 * The role rules are the same as in <code>o-palette-swatches()</code>.
 * <p>
 * In addition to the hues, a theme defines 1 to n theme colours
 * <code>theme1</code> ... <code>themeN</code> (its brand colours, see
 * <code>themes/light/_palette_theme.scss</code>). The palette counts the
 * theme colours that the CSS defines with all steps, starting at
 * <code>theme1</code>; a gap in the numbers ends the list. The solid fill
 * of a theme colour is <code>--o-theme{n}</code>, one of its steps.
 * <p>
 * An instance is immutable.
 *
 * Initial date: 29 Oct 2026<br>
 * @author gnaegi, https://www.frentix.com
 */
public class Palette {

	private static final Logger log = Tracing.createLoggerFor(Palette.class);

	public static final String WHITE = "#FFFFFF";
	public static final double MIN_CONTRAST = 4.5;

	/** Hues which carry dark text on their solid fill */
	public static final Set<String> BRIGHT_HUES = Set.of("yellow", "orange", "lime", "green", "cyan");

	/** The steps every hue must have */
	public static final List<Integer> STEPS = List.of(50, 100, 200, 300, 400, 500, 600, 700, 800, 900);

	/** The prefix of the theme colours, e.g. "theme1" */
	public static final String THEME_COLOR_PREFIX = "theme";

	/**
	 * The neutral scale <code>--o-neutral-{step}</code> (the greys of the theme,
	 * <code>themes/light/_palette_neutral.scss</code>): no hue of the palette
	 */
	public static final String NEUTRAL = "neutral";

	private static final Pattern CUSTOM_PROPERTY = Pattern.compile("--o-([a-z]+\\d*)-(\\d+)\\s*:\\s*(#[0-9A-Fa-f]{6})(?![0-9A-Fa-f])");
	private static final Pattern THEME_COLOR_FILL = Pattern.compile("--o-(" + THEME_COLOR_PREFIX + "\\d+)\\s*:\\s*(#[0-9A-Fa-f]{6})(?![0-9A-Fa-f])");
	private static final Pattern THEME_COLOR_NAME = Pattern.compile(THEME_COLOR_PREFIX + "\\d+");

	private final Map<String,Map<Integer,String>> hues;
	private final Map<String,Map<Integer,String>> themeColors;
	private final Map<String,Integer> themeColorFillSteps;

	private Palette(Map<String,Map<Integer,String>> hues, Map<String,Map<Integer,String>> themeColors,
			Map<String,Integer> themeColorFillSteps) {
		this.hues = hues;
		this.themeColors = themeColors;
		this.themeColorFillSteps = themeColorFillSteps;
	}

	/**
	 * Reads the palette from the custom properties <code>--o-{hue}-{step}: #RRGGBB</code>
	 * of a compiled theme CSS. A hue without all of the {@link #STEPS} is
	 * dropped with a warning. The neutral scale {@link #NEUTRAL} is no hue.
	 * If a property is declared twice, the last declaration wins, as in CSS.
	 *
	 * @param css The content of a CSS file, e.g. theme.css
	 * @return The palette, or null if the CSS has no complete hue
	 */
	public static Palette fromCss(String css) {
		if (css == null || css.isEmpty()) {
			return null;
		}

		Map<String,Map<Integer,String>> parsed = new LinkedHashMap<>();
		Matcher matcher = CUSTOM_PROPERTY.matcher(css);
		while (matcher.find()) {
			String hue = matcher.group(1);
			Integer step = Integer.valueOf(matcher.group(2));
			String hex = matcher.group(3).toUpperCase(Locale.ROOT);
			parsed.computeIfAbsent(hue, h -> new TreeMap<>()).put(step, hex);
		}

		Map<String,Map<Integer,String>> hues = new LinkedHashMap<>();
		for (Map.Entry<String,Map<Integer,String>> hue : parsed.entrySet()) {
			if (isThemeColor(hue.getKey()) || NEUTRAL.equals(hue.getKey())) {
				continue;
			}
			Map<Integer,String> steps = hue.getValue();
			if (steps.keySet().containsAll(STEPS)) {
				Map<Integer,String> paletteSteps = new LinkedHashMap<>();
				for (Integer step : STEPS) {
					paletteSteps.put(step, steps.get(step));
				}
				hues.put(hue.getKey(), Collections.unmodifiableMap(paletteSteps));
			} else {
				log.warn("Colour palette: hue {} has the steps {}, not {}. The hue is dropped.", hue.getKey(), steps.keySet(), STEPS);
			}
		}
		if (hues.isEmpty()) {
			return null;
		}

		Map<String,String> fills = new LinkedHashMap<>();
		Matcher fillMatcher = THEME_COLOR_FILL.matcher(css);
		while (fillMatcher.find()) {
			fills.put(fillMatcher.group(1), fillMatcher.group(2).toUpperCase(Locale.ROOT));
		}

		// theme1, theme2, ... as long as the CSS defines all steps
		Map<String,Map<Integer,String>> themeColors = new LinkedHashMap<>();
		Map<String,Integer> fillSteps = new LinkedHashMap<>();
		for (int n = 1; ; n++) {
			String themeColor = THEME_COLOR_PREFIX + n;
			Map<Integer,String> steps = parsed.get(themeColor);
			if (steps == null || !steps.keySet().containsAll(STEPS)) {
				break;
			}
			Map<Integer,String> paletteSteps = new LinkedHashMap<>();
			for (Integer step : STEPS) {
				paletteSteps.put(step, steps.get(step));
			}
			themeColors.put(themeColor, Collections.unmodifiableMap(paletteSteps));
			fillSteps.put(themeColor, fillStep(paletteSteps, fills.get(themeColor)));
		}
		return new Palette(Collections.unmodifiableMap(hues), Collections.unmodifiableMap(themeColors),
				Collections.unmodifiableMap(fillSteps));
	}

	/**
	 * @return The step with the fill colour, 500 if the fill is missing or not a step
	 */
	private static Integer fillStep(Map<Integer,String> steps, String fill) {
		for (Map.Entry<Integer,String> step : steps.entrySet()) {
			if (step.getValue().equals(fill)) {
				return step.getKey();
			}
		}
		return Integer.valueOf(500);
	}

	/**
	 * @return The same hues without theme colours, e.g. for the fallback to
	 * 		the light palette of a theme that has no palette of its own
	 */
	public Palette withoutThemeColors() {
		return themeColors.isEmpty() ? this : new Palette(hues, Map.of(), Map.of());
	}

	/**
	 * @param hue A hue or theme colour name
	 * @return true if the name has the form of a theme colour, e.g. "theme1"
	 */
	public static boolean isThemeColor(String hue) {
		return hue != null && THEME_COLOR_NAME.matcher(hue).matches();
	}

	/**
	 * @return The hues in the order of the palette, e.g. "red", "orange",
	 * 		without the theme colours
	 */
	public Set<String> getHues() {
		return hues.keySet();
	}

	/**
	 * @return The theme colours the theme defines, in their order, e.g.
	 * 		"theme1", "theme2". Empty if the theme defines none.
	 */
	public List<String> getThemeColors() {
		return List.copyOf(themeColors.keySet());
	}

	/**
	 * @return true if the palette has the hue or the theme colour
	 */
	public boolean contains(String hue) {
		return hue != null && (hues.containsKey(hue) || themeColors.containsKey(hue));
	}

	/**
	 * @return The steps of a hue, e.g. 50 to 900
	 * @throws IllegalArgumentException if the hue does not exist
	 */
	public Set<Integer> getSteps(String hue) {
		return steps(hue).keySet();
	}

	/**
	 * @param hue The hue, e.g. "blue"
	 * @param step The step, e.g. 500
	 * @return The colour as "#RRGGBB"
	 * @throws IllegalArgumentException if the hue or the step does not exist
	 */
	public String hex(String hue, int step) {
		String hex = steps(hue).get(Integer.valueOf(step));
		if (hex == null) {
			throw new IllegalArgumentException("Unknown step " + step + " of the hue " + hue);
		}
		return hex;
	}

	public static boolean isBright(String hue) {
		return BRIGHT_HUES.contains(hue);
	}

	/**
	 * @return true if the solid fill carries dark text: a bright hue, or a
	 * 		theme colour with its fill at step 400 or lighter
	 */
	public boolean isBrightColor(String hue) {
		if (themeColors.containsKey(hue)) {
			return themeColorFillSteps.get(hue).intValue() <= 400;
		}
		return isBright(hue);
	}

	/**
	 * @return The step of the fill, for the hues and the theme colours. A theme
	 * 		colour has the solid fill at its own step (the brand colour), the
	 * 		tint at 300 and light at 200 (200 and 100 if the solid fill is 300
	 * 		or lighter), the dark variant two steps darker than the solid
	 * 		fill, 900 at most.
	 */
	public int getBgStep(Swatch swatch) {
		Integer fill = themeColorFillSteps.get(swatch.hue());
		if (fill == null) {
			return bgStep(swatch);
		}
		int solid = fill.intValue();
		return switch (swatch.variant()) {
			case Swatch.SOLID -> solid;
			case Swatch.TINT -> solid <= 300 ? 200 : 300;
			case Swatch.LIGHT -> solid <= 300 ? 100 : 200;
			case Swatch.DARK -> STEPS.get(Math.min(STEPS.indexOf(fill) + 2, STEPS.size() - 1)).intValue();
			default -> throw new IllegalArgumentException("Unknown variant " + swatch.variant());
		};
	}

	/**
	 * @return The step of the fill: tint 300; light 200; solid 400 (bright hues) or 500;
	 * 		dark 500 (bright hues) or 700
	 */
	public static int bgStep(Swatch swatch) {
		boolean bright = isBright(swatch.hue());
		return switch (swatch.variant()) {
			case Swatch.SOLID -> bright ? 400 : 500;
			case Swatch.TINT -> 300;
			case Swatch.LIGHT -> 200;
			case Swatch.DARK -> bright ? 500 : 700;
			default -> throw new IllegalArgumentException("Unknown variant " + swatch.variant());
		};
	}

	/**
	 * @return The fill colour of the swatch
	 */
	public String bg(Swatch swatch) {
		return hex(swatch.hue(), getBgStep(swatch));
	}

	/**
	 * @return The text colour on the fill: white if its contrast is at
	 * 		least {@link #MIN_CONTRAST}, else step 900 of the hue
	 */
	public String fg(Swatch swatch) {
		String bg = bg(swatch);
		return ColorContrast.contrast(WHITE, bg) >= MIN_CONTRAST ? WHITE : hex(swatch.hue(), 900);
	}

	/**
	 * @return The colour for text and icons of the hue on white: step 500, for
	 * 		a theme colour its fill if the fill is 500 or darker
	 */
	public String text(String hue) {
		Integer fill = themeColorFillSteps.get(hue);
		if (fill != null && fill.intValue() >= 500) {
			return hex(hue, fill.intValue());
		}
		return hex(hue, 500);
	}

	private Map<Integer,String> steps(String hue) {
		Map<Integer,String> steps = hue == null ? null : hues.getOrDefault(hue, themeColors.get(hue));
		if (steps == null) {
			throw new IllegalArgumentException("Unknown hue " + hue);
		}
		return steps;
	}
}

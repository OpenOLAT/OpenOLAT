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

	/**
	 * The text steps every hue must have: each has a text colour (white or
	 * step 900) and the swatch roles read them
	 */
	public static final List<Integer> STEPS = List.of(50, 100, 200, 300, 400, 500, 600, 700, 800, 900);

	/**
	 * The fill steps: the midpoint of step 400 and step 500 in OKLab
	 * (<code>$o-palette-fill-steps</code> in <code>_palette.scss</code>). A
	 * fill or shape colour without a text colour at 4.5:1, no swatch role and
	 * no colour chooser colour. Optional: a CSS without them still loads.
	 */
	public static final List<Integer> FILL_STEPS = List.of(450);

	/** The text steps and the fill steps, light to dark */
	public static final List<Integer> ALL_STEPS = List.of(50, 100, 200, 300, 400, 450, 500, 600, 700, 800, 900);

	/**
	 * The hue with own fill steps of its variants: step 500 of yellow with
	 * white text is an ochre, the solid fill of yellow is step 300 with dark
	 * text ($o-palette-variant-steps-of-hue in _palette.scss)
	 */
	public static final String YELLOW = "yellow";

	/** The step of the tinted fill, role soft-bg */
	private static final int SOFT_BG_STEP = 50;
	/** The candidates of the text on the tinted fill, role soft-fg */
	private static final int[] SOFT_FG_STEPS = { 700, 800, 900 };
	/** The step of the border of the tinted fill, role soft-border */
	private static final int SOFT_BORDER_STEP = 100;

	/** The prefix of the theme colours, e.g. "theme1" */
	public static final String THEME_COLOR_PREFIX = "theme";

	/**
	 * The neutral scale <code>--o-neutral-{step}</code> (the greys of the theme,
	 * <code>themes/light/_palette_neutral.scss</code>): no hue of the palette
	 */
	public static final String NEUTRAL = "neutral";

	/*
	 * The names and steps are bounded: a name is a valid colour key and a
	 * CSS class name (ColorValidator), a step always fits an int. Longer
	 * names and numbers in the CSS do not match and are ignored.
	 */
	private static final Pattern CUSTOM_PROPERTY = Pattern.compile("--o-([a-z]{1,32}\\d{0,3})-(\\d{1,4})\\s*:\\s*(#[0-9A-Fa-f]{6})(?![0-9A-Fa-f])");
	private static final Pattern THEME_COLOR_FILL = Pattern.compile("--o-(" + THEME_COLOR_PREFIX + "\\d{1,3})\\s*:\\s*(#[0-9A-Fa-f]{6})(?![0-9A-Fa-f])");
	private static final Pattern THEME_COLOR_NAME = Pattern.compile(THEME_COLOR_PREFIX + "\\d{1,3}");

	private final Map<String,Map<Integer,String>> hues;
	private final Map<String,Map<Integer,String>> themeColors;
	private final Map<String,Integer> themeColorFillSteps;
	private final Map<Integer,String> neutralSteps;

	private Palette(Map<String,Map<Integer,String>> hues, Map<String,Map<Integer,String>> themeColors,
			Map<String,Integer> themeColorFillSteps, Map<Integer,String> neutralSteps) {
		this.hues = hues;
		this.themeColors = themeColors;
		this.themeColorFillSteps = themeColorFillSteps;
		this.neutralSteps = neutralSteps;
	}

	/**
	 * Reads the palette from the custom properties <code>--o-{hue}-{step}: #RRGGBB</code>
	 * of a compiled theme CSS. A hue without all of the {@link #STEPS} is
	 * dropped with a warning; the {@link #FILL_STEPS} are read when the CSS
	 * has them. The neutral scale {@link #NEUTRAL} is no hue: its steps are
	 * read as they are, without a check of the steps, see {@link #neutral(int)}.
	 * If a property is declared twice, the last declaration wins, as in CSS.
	 * The method never throws: junk in the CSS is ignored, a failure is
	 * logged and returns null.
	 *
	 * @param css The content of a CSS file, e.g. theme.css
	 * @return The palette, or null if the CSS has no complete hue
	 */
	public static Palette fromCss(String css) {
		if (css == null || css.isEmpty()) {
			return null;
		}
		try {
			return parse(css);
		} catch (RuntimeException e) {
			log.warn("Colour palette: the CSS can not be read, no palette", e);
			return null;
		}
	}

	private static Palette parse(String css) {
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
				hues.put(hue.getKey(), paletteSteps(steps));
			} else {
				log.warn("Colour palette: hue {} has the steps {}, not {}. The hue is dropped.",
						ColorValidator.toLogValue(hue.getKey()), steps.keySet(), STEPS);
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
			Map<Integer,String> paletteSteps = paletteSteps(steps);
			themeColors.put(themeColor, paletteSteps);
			fillSteps.put(themeColor, fillStep(paletteSteps, fills.get(themeColor)));
		}
		Map<Integer,String> neutral = parsed.get(NEUTRAL);
		Map<Integer,String> neutralSteps = neutral == null ? Map.of() : Collections.unmodifiableMap(new TreeMap<>(neutral));
		return new Palette(Collections.unmodifiableMap(hues), Collections.unmodifiableMap(themeColors),
				Collections.unmodifiableMap(fillSteps), neutralSteps);
	}

	/**
	 * @return The steps of {@link #ALL_STEPS} that the parsed steps hold, light to dark
	 */
	private static Map<Integer,String> paletteSteps(Map<Integer,String> steps) {
		Map<Integer,String> paletteSteps = new LinkedHashMap<>();
		for (Integer step : ALL_STEPS) {
			String hex = steps.get(step);
			if (hex != null) {
				paletteSteps.put(step, hex);
			}
		}
		return Collections.unmodifiableMap(paletteSteps);
	}

	/**
	 * @param step A step, e.g. 450
	 * @return true if the step is a fill step ({@link #FILL_STEPS})
	 */
	public static boolean isFillStep(int step) {
		return FILL_STEPS.contains(Integer.valueOf(step));
	}

	/**
	 * @return The text step with the solid fill colour, 500 if the fill is
	 * 		missing or not a text step
	 */
	private static Integer fillStep(Map<Integer,String> steps, String fill) {
		for (Integer step : STEPS) {
			if (steps.get(step).equals(fill)) {
				return step;
			}
		}
		return Integer.valueOf(500);
	}

	/**
	 * @return The same hues and neutral steps without theme colours, e.g. for
	 * 		the fallback to the light palette of a theme that has no palette
	 * 		of its own
	 */
	public Palette withoutThemeColors() {
		return themeColors.isEmpty() ? this : new Palette(hues, Map.of(), Map.of(), neutralSteps);
	}

	/**
	 * @return The steps of the neutral scale {@link #NEUTRAL} the CSS defines,
	 * 		light to dark, e.g. 0, 25, 50, ..., 1000. Empty if the CSS has none.
	 */
	public Set<Integer> getNeutralSteps() {
		return neutralSteps.keySet();
	}

	/**
	 * @param step A step of the neutral scale, e.g. 750
	 * @return The colour <code>--o-neutral-{step}</code> as "#RRGGBB", or null
	 * 		if the CSS does not define the step. Never throws.
	 */
	public String neutral(int step) {
		return neutralSteps.get(Integer.valueOf(step));
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
	 * @return The steps of a hue, light to dark, e.g. 50 to 900 with the fill
	 * 		step 450 when the CSS has it
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

	/**
	 * @return The step of the fill, for the hues and the theme colours. A theme
	 * 		colour has the solid fill at its own step (the brand colour), the
	 * 		tint at 300 and light at 200 (200 and 100 if the solid fill is 300
	 * 		or lighter), the dark variant two steps darker than the solid
	 * 		fill, 900 at most. The soft variant is the soft-bg, step 50.
	 */
	public int getBgStep(Swatch swatch) {
		Integer fill = themeColorFillSteps.get(swatch.hue());
		if (fill == null) {
			return bgStep(swatch);
		}
		int solid = fill.intValue();
		return switch (swatch.variant()) {
			case Swatch.SOFT -> SOFT_BG_STEP;
			case Swatch.SOLID -> solid;
			case Swatch.TINT -> solid <= 300 ? 200 : 300;
			case Swatch.LIGHT -> solid <= 300 ? 100 : 200;
			case Swatch.DARK -> STEPS.get(Math.min(STEPS.indexOf(fill) + 2, STEPS.size() - 1)).intValue();
			default -> throw new IllegalArgumentException("Unknown variant " + swatch.variant());
		};
	}

	/**
	 * @return The step of the fill of a hue: soft 50; light 200; tint 300;
	 * 		solid 500; dark 700. Yellow is the exception ({@link #YELLOW}):
	 * 		soft 50; light 100; tint 200; solid 300 with dark text; dark 500
	 */
	public static int bgStep(Swatch swatch) {
		if (YELLOW.equals(swatch.hue())) {
			return switch (swatch.variant()) {
				case Swatch.SOFT -> SOFT_BG_STEP;
				case Swatch.SOLID -> 300;
				case Swatch.TINT -> 200;
				case Swatch.LIGHT -> 100;
				case Swatch.DARK -> 500;
				default -> throw new IllegalArgumentException("Unknown variant " + swatch.variant());
			};
		}
		return switch (swatch.variant()) {
			case Swatch.SOFT -> SOFT_BG_STEP;
			case Swatch.SOLID -> 500;
			case Swatch.TINT -> 300;
			case Swatch.LIGHT -> 200;
			case Swatch.DARK -> 700;
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
	 * 		least {@link #MIN_CONTRAST}, else step 900 of the hue. The soft
	 * 		variant has the soft-fg, see {@link #softFg(String)}.
	 */
	public String fg(Swatch swatch) {
		if (Swatch.SOFT.equals(swatch.variant())) {
			return softFg(swatch.hue());
		}
		String bg = bg(swatch);
		return ColorContrast.contrast(WHITE, bg) >= MIN_CONTRAST ? WHITE : hex(swatch.hue(), 900);
	}

	/**
	 * @return The tinted fill of the hue or theme colour (role soft-bg): step 50
	 */
	public String softBg(String hue) {
		return hex(hue, SOFT_BG_STEP);
	}

	/**
	 * @return The text colour on the tinted fill (role soft-fg): the first of
	 * 		step 700, 800, 900 with {@link #MIN_CONTRAST} on step 50, else the
	 * 		one with the highest contrast
	 */
	public String softFg(String hue) {
		String softBg = softBg(hue);
		String best = null;
		double bestContrast = 0.0d;
		for (int step : SOFT_FG_STEPS) {
			String candidate = hex(hue, step);
			double contrast = ColorContrast.contrast(candidate, softBg);
			if (contrast >= MIN_CONTRAST) {
				return candidate;
			}
			if (best == null || contrast > bestContrast) {
				best = candidate;
				bestContrast = contrast;
			}
		}
		return best;
	}

	/**
	 * @return The border of the tinted fill (role soft-border): step 100, the
	 * 		next step darker than the fill
	 */
	public String softBorder(String hue) {
		return hex(hue, SOFT_BORDER_STEP);
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

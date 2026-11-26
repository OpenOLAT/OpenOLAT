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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.components.form.flexible.elements.ColorPickerElement;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nModule;

/**
 * The colour map of the text colour and the background colour of the HTML
 * editor (TinyMCE options <code>color_map</code> and <code>color_cols</code>).
 * The colours come from the palette of the active theme, in this order:
 * <ol>
 * <li>the 6 steps of the neutral scale {@link #NEUTRAL_STEPS}: white, light,
 * 		mid and dark neutral, the text colour, black</li>
 * <li>the solid fill of the hues of {@link ColorSet#M}, the 14
 * 		{@link ColorService#HUES}</li>
 * <li>the solid fill of the theme colours of {@link ColorSet#M}, all theme
 * 		colours of the theme</li>
 * </ol>
 * The grid has {@link #COLUMNS} columns: the neutral steps fill the first row.
 * The editor writes the chosen colour as a hex value into the content.
 * <p>
 * The map must never break the editor: a colour that is missing or is no
 * hex value <code>#RRGGBB</code> is dropped and logged once. Without a
 * palette or without a valid colour, {@link #tinyMceOptions(Locale)} returns
 * an empty string and the editor keeps its default map.
 *
 * Initial date: 25 Nov 2026<br>
 * @author gnaegi, https://www.frentix.com
 */
public final class EditorColorMap {

	private static final Logger log = Tracing.createLoggerFor(EditorColorMap.class);

	/** The steps of the neutral scale: white, light, mid, dark neutral, the text colour, black */
	public static final List<Integer> NEUTRAL_STEPS = List.of(0, 200, 450, 600, 750, 1000);

	/**
	 * The columns of the grid. The 6 neutral steps fill the first row; the
	 * 14 hues fill the next two rows and start the fourth row, the theme
	 * colours follow.
	 */
	public static final int COLUMNS = 6;

	/** The only form of a colour written into the editor configuration */
	public static final Pattern HEX = Pattern.compile("#[0-9A-F]{6}");

	/** The i18n key prefix of the neutral steps, e.g. "color.neutral.750" */
	static final String NEUTRAL_KEY_PREFIX = "color.neutral.";

	private static final String LOG_SOURCE = "editor colour map";
	private static final AtomicBoolean NO_MAP_LOGGED = new AtomicBoolean(false);

	private EditorColorMap() {
		//
	}

	/**
	 * A colour of the map.
	 *
	 * @param hex The colour, "#RRGGBB"
	 * @param label The label of the colour in the language of the user
	 */
	public record Entry(String hex, String label) {
		//
	}

	/**
	 * The options of the editor for the colour palette of the active GUI
	 * theme. Never throws.
	 *
	 * @param locale The language of the labels, the default language if null
	 * @return The options <code>color_map</code> and <code>color_cols</code>,
	 * 		each followed by ",\n", ready to append to the init object of
	 * 		TinyMCE. An empty string if the theme has no palette, the palette
	 * 		has no valid colour or the map fails; the failure is logged once.
	 */
	public static String tinyMceOptions(Locale locale) {
		try {
			Palette palette = CoreSpringFactory.getImpl(ColorService.class).getPalette();
			if (palette == null) {
				logNoMap("the active theme has no colour palette", null);
				return "";
			}
			Locale labelLocale = locale == null ? I18nModule.getDefaultLocale() : locale;
			Translator translator = Util.createPackageTranslator(ColorServiceImpl.class, labelLocale);
			String options = toTinyMceOptions(create(palette, translator));
			if (options.isEmpty()) {
				logNoMap("the colour palette has no valid colour", null);
			}
			return options;
		} catch (RuntimeException | LinkageError e) {
			logNoMap("building the colour map failed", e);
			return "";
		}
	}

	/**
	 * @param palette The palette of the theme
	 * @param translator A translator of the colour package
	 * @return The colours of the map in their order, see the class comment.
	 * 		A colour that is missing in the palette or is no hex value is
	 * 		dropped and logged once. Empty if the palette or the translator
	 * 		is null.
	 */
	public static List<Entry> create(Palette palette, Translator translator) {
		if (palette == null || translator == null) {
			return List.of();
		}
		List<Entry> entries = new ArrayList<>();
		for (Integer step : NEUTRAL_STEPS) {
			String name = Palette.NEUTRAL + "-" + step;
			add(entries, name, () -> palette.neutral(step.intValue()), translator, NEUTRAL_KEY_PREFIX + step);
		}
		for (String key : ColorSet.M.getHueKeys()) {
			// the key of the solid swatch is the hue, e.g. "blue"
			Swatch swatch = Swatch.fromKey(key);
			add(entries, key, () -> solid(palette, swatch), translator, "color." + key);
		}
		List<String> themeColors = ColorSet.M.getThemeColors(palette.getThemeColors());
		for (String themeColor : themeColors) {
			Swatch swatch = new Swatch(themeColor, Swatch.SOLID);
			String label = labelOf(() -> ColorUIFactory.themeColorLabel(themeColor, themeColors.size(), translator));
			addEntry(entries, themeColor, () -> solid(palette, swatch), label);
		}
		return Collections.unmodifiableList(entries);
	}

	/**
	 * @param entries The colours of the map
	 * @return The options <code>color_map: ['#RRGGBB','Label',...],\n</code>
	 * 		and <code>color_cols: 6,\n</code>. The colour is written only if
	 * 		it matches {@link #HEX}, the label as a JavaScript string with
	 * 		every character except letters, digits and space as an
	 * 		<code>\\uXXXX</code> escape. An empty string if no colour is valid.
	 */
	public static String toTinyMceOptions(List<Entry> entries) {
		if (entries == null || entries.isEmpty()) {
			return "";
		}
		StringBuilder sb = new StringBuilder(64 + entries.size() * 32);
		sb.append("color_map: [");
		boolean first = true;
		for (Entry entry : entries) {
			if (entry == null || !isValidHex(entry.hex())) {
				if (entry != null) {
					ColorValidator.logInvalid(LOG_SOURCE, entry.hex());
				}
				continue;
			}
			if (!first) {
				sb.append(',');
			}
			first = false;
			sb.append('\'').append(entry.hex()).append("','");
			appendJsString(sb, entry.label() == null ? entry.hex() : entry.label());
			sb.append('\'');
		}
		if (first) {
			return "";
		}
		sb.append("],\n")
		  .append("color_cols: ").append(COLUMNS).append(",\n");
		return sb.toString();
	}

	/**
	 * The coloured table styles of the HTML editor, entries of the TinyMCE
	 * option <code>table_class_list</code>: one per colour of the set
	 * {@link ColorSet#M} with the theme colours of the active theme.
	 *
	 * @param locale The locale of the labels, the default locale if null
	 * @return The entries <code>{title: 'Red', value: 'b_red'},\n</code>, each
	 * 		with a trailing comma, see {@link #toTableClassListEntries(List)}.
	 * 		Empty if building them fails. Never throws.
	 */
	public static String tableClassListEntries(Locale locale) {
		try {
			Locale labelLocale = locale == null ? I18nModule.getDefaultLocale() : locale;
			return toTableClassListEntries(ColorUIFactory.createColors(ColorSet.M, labelLocale));
		} catch (RuntimeException | LinkageError e) {
			log.warn("Building the coloured table styles of the HTML editor failed", e);
			return "";
		}
	}

	/**
	 * @param colors The colours of a chooser set, e.g. of {@link ColorUIFactory#createColors(ColorSet, Locale)}
	 * @return Per colour with a table style ({@link ColorUIFactory#toTableCssClass(String)})
	 * 		the entry <code>{title: 'Label', value: 'b_key'},\n</code>. The label
	 * 		is a JavaScript string (see {@link #appendJsString(StringBuilder, String)}),
	 * 		the class is checked. Empty if no colour has a table style.
	 */
	static String toTableClassListEntries(List<ColorPickerElement.Color> colors) {
		if (colors == null || colors.isEmpty()) {
			return "";
		}
		StringBuilder sb = new StringBuilder(colors.size() * 40);
		for (ColorPickerElement.Color color : colors) {
			String cssClass = color == null ? null : ColorUIFactory.toTableCssClass(color.id());
			if (cssClass == null || !ColorValidator.isValidCssClass(cssClass)) {
				continue;
			}
			String label = color.translatedName() == null ? color.id() : color.translatedName();
			sb.append("  {title: '");
			appendJsString(sb, label);
			sb.append("', value: '").append(cssClass).append("'},\n");
		}
		return sb.toString();
	}

	/**
	 * @return true if the value is a colour "#RRGGBB" in upper case, see {@link #HEX}
	 */
	public static boolean isValidHex(String value) {
		return value != null && HEX.matcher(value).matches();
	}

	/**
	 * Appends the value for a JavaScript string in single or double quotes,
	 * also inside a HTML script element: every character except the ASCII
	 * letters, the digits and the space is a <code>\\uXXXX</code> escape.
	 */
	static void appendJsString(StringBuilder sb, String value) {
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == ' ') {
				sb.append(c);
			} else {
				sb.append(String.format("\\u%04X", Integer.valueOf(c)));
			}
		}
	}

	private static String solid(Palette palette, Swatch swatch) {
		return swatch != null && palette.contains(swatch.hue()) ? palette.bg(swatch) : null;
	}

	private static void add(List<Entry> entries, String name, Supplier<String> hex, Translator translator, String i18nKey) {
		addEntry(entries, name, hex, labelOf(() -> translator.translate(i18nKey)));
	}

	/**
	 * Adds the colour if it is a valid hex value, else logs the name once.
	 * A palette that throws for one colour drops this colour only.
	 */
	private static void addEntry(List<Entry> entries, String name, Supplier<String> hex, String label) {
		String value;
		try {
			value = hex.get();
		} catch (RuntimeException e) {
			value = null;
		}
		if (isValidHex(value)) {
			entries.add(new Entry(value, label == null || label.isBlank() ? value : label));
		} else {
			ColorValidator.logInvalid(LOG_SOURCE, name);
		}
	}

	private static String labelOf(Supplier<String> label) {
		try {
			return label.get();
		} catch (RuntimeException e) {
			return null;
		}
	}

	private static void logNoMap(String reason, Throwable e) {
		if (NO_MAP_LOGGED.compareAndSet(false, true)) {
			if (e == null) {
				log.warn("The HTML editor has no colour map of the theme, {}. It shows its default colours.", reason);
			} else {
				log.warn("The HTML editor has no colour map of the theme, {}. It shows its default colours.", reason, e);
			}
		}
	}
}

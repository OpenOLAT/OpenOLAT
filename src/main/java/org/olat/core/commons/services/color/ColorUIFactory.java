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
import java.util.List;
import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.components.form.flexible.elements.ColorPickerElement;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;

/**
 * Initial date: Aug 18, 2023
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class ColorUIFactory {

	/** Prefix of the table style classes of the page editor and the HTML editor, e.g. "b_blue" */
	public static final String TABLE_CSS_CLASS_PREFIX = "b_";
	/** The table style class of the hue gray: "b_gray" is the old table style that renders slate */
	static final String TABLE_GRAY_HUE_CSS_CLASS = "b_gray_hue";
	/** The old table style "b_gray", rendered as the hue slate */
	static final String TABLE_LEGACY_GRAY_CSS_CLASS = "b_gray";

	// Utility class should not have public constructor
	private ColorUIFactory() {
	}

	/**
	 * create color objects with given names and cssPrefix. A name that is no
	 * colour key or gives no valid CSS class is dropped and logged, see
	 * {@link ColorValidator}.
	 *
	 * @param colorNames
	 * @param locale
	 * @param cssClassPrefix
	 * @return list of color objects
	 */
	public static List<ColorPickerElement.Color> createColors(List<String> colorNames, Locale locale, String cssClassPrefix) {
		Translator translator = Util.createPackageTranslator(ColorServiceImpl.class, locale);
		return createColors(colorNames, translator, cssClassPrefix);
	}

	static List<ColorPickerElement.Color> createColors(List<String> colorNames, Translator translator, String cssClassPrefix) {
		List<ColorPickerElement.Color> colors = new ArrayList<>();
		if (colorNames == null) {
			return colors;
		}
		String prefix = cssClassPrefix == null ? "" : cssClassPrefix;
		for (String colorName : colorNames) {
			String cssClass = prefix + colorName;
			if (!ColorValidator.isValidKey(colorName) || !ColorValidator.isValidCssClass(cssClass)) {
				ColorValidator.logInvalid("colour picker", colorName);
				continue;
			}
			String translatedColorName = translator.translate("color.".concat(colorName));
			colors.add(new ColorPickerElement.Color(colorName, translatedColorName, cssClass));
		}
		return colors;
	}

	/**
	 * create color objects with fixed cssClassPrefix
	 *
	 * @param colorNames
	 * @param locale
	 * @return list of color objects
	 */
	public static List<ColorPickerElement.Color> createColors(List<String> colorNames, Locale locale) {
		return createColors(colorNames, locale, ColorService.CSS_CLASS_PREFIX);
	}

	/**
	 * The colours of a chooser set for a colour picker: one entry per colour
	 * of the set with its solid swatch, the hues first, then the theme colours
	 * of the set in the active GUI theme after a gap with a heading (see
	 * {@link #appendThemeColors(List, Locale)}). The id of an entry is the
	 * key of the set (e.g. "blue", "theme2"), the label is the label of the
	 * hue (color.{hue}) or of the theme colour, the CSS class the swatch class
	 * (e.g. "o_swatch_blue").
	 * <p>
	 * For the sets with the solid variant only, {@link ColorSet#S} and
	 * {@link ColorSet#M}. The set {@link ColorSet#L} gets the solid swatch of
	 * each colour too, not its variants.
	 *
	 * @param colorSet The set
	 * @param locale The locale of the labels
	 * @return The colours of the picker
	 */
	public static List<ColorPickerElement.Color> createColors(ColorSet colorSet, Locale locale) {
		List<String> themeColors = CoreSpringFactory.getImpl(ColorService.class).getThemeColors();
		Translator translator = Util.createPackageTranslator(ColorServiceImpl.class, locale);
		return createColors(colorSet, themeColors, translator);
	}

	static List<ColorPickerElement.Color> createColors(ColorSet colorSet, List<String> themeColors, Translator translator) {
		List<ColorPickerElement.Color> colors = createColors(colorSet.getHues(), translator, ColorService.SWATCH_CSS_CLASS_PREFIX);
		return appendThemeColors(colors, colorSet.getThemeColors(themeColors), translator);
	}

	/**
	 * The colours of the badge chooser: the badge colours gold, silver and
	 * bronze ({@link ColorService#BADGE_COLORS}, classes of the aliases, e.g.
	 * "o_color_gold"), then the colours of the set {@link ColorSet#M} with
	 * the theme colours, see {@link #createColors(ColorSet, Locale)}.
	 *
	 * @param locale The locale of the labels
	 * @return The colours of the picker, the ids are the keys of
	 * 		{@link ColorService#getColorsForBadges()}
	 */
	public static List<ColorPickerElement.Color> createBadgeColors(Locale locale) {
		List<String> themeColors = CoreSpringFactory.getImpl(ColorService.class).getThemeColors();
		Translator translator = Util.createPackageTranslator(ColorServiceImpl.class, locale);
		return createBadgeColors(themeColors, translator);
	}

	static List<ColorPickerElement.Color> createBadgeColors(List<String> themeColors, Translator translator) {
		List<ColorPickerElement.Color> colors = createColors(ColorService.BADGE_COLORS, translator, ColorService.CSS_CLASS_PREFIX);
		colors.addAll(createColors(ColorSet.M, themeColors, translator));
		return colors;
	}

	/**
	 * The colour value to save for an item of a colour chooser. A chooser may
	 * preselect the key nearest to an old stored value (e.g. "blue" for the old
	 * calendar colour "navy"). The old value stays byte-identical until the user
	 * picks a colour: only an active pick writes the key.
	 *
	 * @param storedValue The stored value of the item, null for a new item or
	 * 		an item without colour
	 * @param colorEl The chooser, see {@link ColorPickerElement#isColorPicked()}
	 * @return The stored value if there is one and the user did not pick a
	 * 		colour, else the key of the colour of the chooser, null for no colour
	 */
	public static String getValueToSave(String storedValue, ColorPickerElement colorEl) {
		ColorPickerElement.Color color = colorEl.getColor();
		return getValueToSave(storedValue, colorEl.isColorPicked(), color == null ? null : color.id());
	}

	/**
	 * The colour value to save for an item of a colour chooser, see
	 * {@link #getValueToSave(String, ColorPickerElement)}, for a chooser that
	 * is no {@link ColorPickerElement} or has more controls, e.g. a link to
	 * reset the colour.
	 *
	 * @param storedValue The stored value of the item, null for a new item or
	 * 		an item without colour
	 * @param changed true if the user changed the colour: picked a colour or
	 * 		reset it
	 * @param value The value of the colour of the chooser, e.g. the key
	 * 		"blue" or the table style "b_blue", null for no colour
	 * @return The stored value if there is one and the user did not change the
	 * 		colour, else the value
	 */
	public static String getValueToSave(String storedValue, boolean changed, String value) {
		return storedValue != null && !changed ? storedValue : value;
	}

	/**
	 * @param key A key of the chooser sets, e.g. "blue", "theme2"
	 * @return The table style class, e.g. "b_blue", "b_theme2"; "b_gray_hue"
	 * 		for "gray", because "b_gray" is the old table style that renders
	 * 		slate. Null if the key is no solid swatch key ({@link Swatch#fromKey(String)}).
	 */
	public static String toTableCssClass(String key) {
		Swatch swatch = Swatch.fromKey(key);
		if (swatch == null || !Swatch.SOLID.equals(swatch.variant())) {
			return null;
		}
		return "gray".equals(key) ? TABLE_GRAY_HUE_CSS_CLASS : TABLE_CSS_CLASS_PREFIX + key;
	}

	/**
	 * The key of a table style class, e.g. to preselect a stored table style
	 * in a chooser of the set {@link ColorSet#M}.
	 *
	 * @param cssClass A table style class, e.g. "b_blue", "b_gray_hue", "b_theme2"
	 * @return The key, e.g. "blue", "gray", "theme2"; "slate" for the old
	 * 		style "b_gray", which renders slate. Null for a class that is no
	 * 		table style of a solid swatch, e.g. "b_default" or a custom style.
	 */
	public static String toTableColorKey(String cssClass) {
		if (cssClass == null) {
			return null;
		}
		String value = cssClass.trim();
		if (TABLE_GRAY_HUE_CSS_CLASS.equals(value)) {
			return "gray";
		}
		if (TABLE_LEGACY_GRAY_CSS_CLASS.equals(value)) {
			return "slate";
		}
		if (!value.startsWith(TABLE_CSS_CLASS_PREFIX)) {
			return null;
		}
		String key = value.substring(TABLE_CSS_CLASS_PREFIX.length());
		Swatch swatch = Swatch.fromKey(key);
		return swatch != null && Swatch.SOLID.equals(swatch.variant()) ? key : null;
	}

	/**
	 * Appends the theme colours of the active GUI theme, e.g. after the 14
	 * hues. The picker draws them after a gap with a heading. The label is
	 * "Theme" when the theme has one theme colour, else "Theme 1" ... "Theme n".
	 * The id is the theme colour, e.g. "theme2", the CSS class its swatch
	 * class, e.g. "o_swatch_theme2".
	 *
	 * @param colors The colours of the picker, not changed
	 * @param locale
	 * @return A new list with the colours and the theme colours. The same
	 * 		colours if the theme has no theme colours.
	 */
	public static List<ColorPickerElement.Color> appendThemeColors(List<ColorPickerElement.Color> colors, Locale locale) {
		List<String> themeColors = CoreSpringFactory.getImpl(ColorService.class).getThemeColors();
		Translator translator = Util.createPackageTranslator(ColorServiceImpl.class, locale);
		return appendThemeColors(colors, themeColors, translator);
	}

	static List<ColorPickerElement.Color> appendThemeColors(List<ColorPickerElement.Color> colors,
			List<String> themeColors, Translator translator) {
		List<ColorPickerElement.Color> allColors = new ArrayList<>(colors);
		List<String> validThemeColors = new ArrayList<>();
		if (themeColors != null) {
			for (String themeColor : themeColors) {
				if (Palette.isThemeColor(themeColor)) {
					validThemeColors.add(themeColor);
				} else {
					ColorValidator.logInvalid("theme colour", themeColor);
				}
			}
		}
		if (validThemeColors.isEmpty()) {
			return allColors;
		}
		String group = translator.translate("color.theme.group");
		for (String themeColor : validThemeColors) {
			String label = themeColorLabel(themeColor, validThemeColors.size(), translator);
			allColors.add(new ColorPickerElement.Color(themeColor, label,
					ColorService.SWATCH_CSS_CLASS_PREFIX + themeColor, group));
		}
		return allColors;
	}

	/**
	 * @param themeColor A theme colour, e.g. "theme2"
	 * @param themeColorCount The number of theme colours of the theme
	 * @param translator A translator of the colour package
	 * @return "Theme" when the theme has one theme colour, else "Theme 1" ... "Theme n"
	 */
	static String themeColorLabel(String themeColor, int themeColorCount, Translator translator) {
		return themeColorCount == 1
				? translator.translate("color.theme")
				: translator.translate("color.theme.n", themeColor.substring(Palette.THEME_COLOR_PREFIX.length()));
	}
}

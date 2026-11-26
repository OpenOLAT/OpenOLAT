/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.core.commons.services.color;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.themes.Theme;
import org.olat.core.helpers.GUISettings;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.event.FrameworkStartedEvent;
import org.olat.core.util.event.FrameworkStartupEventChannel;
import org.olat.core.util.event.GenericEventListener;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Initial date: 2023-03-23<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Service
public class ColorServiceImpl implements ColorService, InitializingBean, SmartInitializingSingleton, GenericEventListener {
	
	private static final Logger log = Tracing.createLoggerFor(ColorServiceImpl.class);
	
	/*
	 * The aliases of the old colour keys. The hue follows the name of the old
	 * key. The variant is the one of the five variants (soft, light, tint,
	 * solid, dark) of that hue whose fill is nearest (OKLab distance) to the
	 * colour of the key before the palette (OO-9993). Keep in sync with
	 * $o-swatch-alias-keys-* in _palette.scss.
	 */
	
	/**
	 * Keys of the old color.list ({@link ColorService#LEGACY_COLORS}), same
	 * order. The prefixes "o_video_marker_" and "o_alert_box_color_".
	 */
	static final Map<String,Swatch> COLOR_LIST_ALIASES = Collections.unmodifiableMap(aliases(
			"lightgray", "gray", Swatch.DARK,
			"yellow", "yellow", Swatch.SOLID,
			"orange", "orange", Swatch.SOLID,
			"brown", "brown", Swatch.SOLID,
			"red", "red", Swatch.SOLID,
			"orchid", "pink", Swatch.SOLID,
			"purple", "violet", Swatch.DARK,
			"lightblue", "cyan", Swatch.SOLID,
			"cobaltblue", "blue", Swatch.DARK,
			"darkblue", "blue", Swatch.DARK,
			"lightgreen", "lime", Swatch.SOLID,
			"seagreen", "green", Swatch.DARK));
	
	/**
	 * Keys of the old color.list and the badge colours gold, silver, bronze
	 * ({@link ColorService#BADGE_COLORS}): the family {@link ColorFamily#COLOR_SERVICE}
	 * and the prefix "o_color_"
	 */
	static final Map<String,Swatch> COLOR_SERVICE_ALIASES = Collections.unmodifiableMap(withBadgeColors(COLOR_LIST_ALIASES));
	
	/**
	 * Keys of {@link ColorService#CALENDAR_COLORS} that are no swatch key,
	 * same order. The other calendar colours (yellow, orange, red, blue, lime,
	 * green) are swatch keys and render as that swatch, see
	 * {@link ColorService#resolveStored(String, ColorFamily)}.
	 */
	static final Map<String,Swatch> CALENDAR_ALIASES = Collections.unmodifiableMap(aliases(
			"grey", "gray", Swatch.SOLID,
			"maroon", "red", Swatch.DARK,
			"navy", "blue", Swatch.DARK,
			"fuchsia", "magenta", Swatch.SOLID,
			"rebeccapurple", "violet", Swatch.DARK,
			"olive", "lime", Swatch.SOLID));
	
	/** Prefixes of the legacy CSS classes and the aliases of each one */
	private static final Map<String,Map<String,Swatch>> PREFIXES = Collections.unmodifiableMap(prefixes());

	/**
	 * The removed properties color.list and color.default: read only to log
	 * that an installation still sets them and that they are ignored. The
	 * choosers use the sets {@link ColorSet} in code.
	 */
	@Value("${color.list:}")
	private String ignoredColorList;
	@Value("${color.default:}")
	private String ignoredDefaultColor;
	
	@Autowired
	private GUISettings guiSettings;
	
	/** The failure to read the palette is logged once */
	private volatile boolean paletteFailureLogged;
	/** True if the palette check runs on the first use of the palette, see {@link #getPalette()} */
	private final AtomicBoolean lazyPaletteCheck = new AtomicBoolean(false);
	
	@Override
	public List<String> getColors(ColorSet colorSet) {
		return colorSet.getKeysWithThemeColors(getThemeColors());
	}

	@Override
	public List<String> getHueColors(ColorSet colorSet) {
		return colorSet.getHueKeys();
	}

	@Override
	public String toSwatchCssClass(String key) {
		Swatch swatch = resolve(key, ColorFamily.SWATCH);
		return swatch == null ? null : swatch.cssClass();
	}

	@Override
	public String getNearestKey(ColorSet colorSet, String storedValue, ColorFamily legacyFamily) {
		if (colorSet == null) {
			return null;
		}
		return colorSet.nearestKey(resolveStored(storedValue, legacyFamily));
	}

	@Override
	public String toStoredValueCssClass(String storedValue) {
		if (!StringHelper.containsNonWhitespace(storedValue)) {
			return null;
		}
		String value = storedValue.trim();
		if (Swatch.fromKey(value) != null) {
			String swatchCssClass = toSwatchCssClass(value);
			if (swatchCssClass != null) {
				return swatchCssClass;
			}
		}
		return ColorValidator.validCssClasses(value, "stored colour");
	}

	@Override
	public String toStoredValueCssClass(String storedValue, String legacyPrefix) {
		if (!StringHelper.containsNonWhitespace(storedValue)) {
			return null;
		}
		String value = storedValue.trim();
		if (Swatch.fromKey(value) != null) {
			// a key of the chooser sets is no old value: null for a theme colour
			// if the theme has no theme colours
			return toSwatchCssClass(value);
		}
		String prefix = legacyPrefix == null ? "" : legacyPrefix;
		String cssClass = value.startsWith(prefix) ? value : prefix + value;
		if (ColorValidator.isValidCssClass(cssClass)) {
			return cssClass;
		}
		ColorValidator.logInvalid("stored colour " + prefix, value);
		return null;
	}

	@Override
	public List<String> getColorsForBadges() {
		List<String> badgeColors = new ArrayList<>(BADGE_COLORS);
		badgeColors.addAll(getColors(ColorSet.M));
		return badgeColors;
	}

	@Override
	public String toCssClass(String colorKey) {
		if (colorKey == null || colorKey.isBlank()) {
			return null;
		}
		String key = colorKey.trim();
		String cssClass = CSS_CLASS_PREFIX + key;
		if (ColorValidator.isValidCssClass(cssClass)) {
			return cssClass;
		}
		ColorValidator.logInvalid("colour class " + CSS_CLASS_PREFIX, key);
		return null;
	}

	@Override
	public String toColorKey(String cssClass) {
		return cssClass != null && cssClass.startsWith(CSS_CLASS_PREFIX)
				? cssClass.substring(CSS_CLASS_PREFIX.length()) : cssClass;
	}

	@Override
	public List<String> getCalendarColors() {
		return CALENDAR_COLORS;
	}

	@Override
	public boolean isCalendarCssClass(String cssClass) {
		return cssClass != null && cssClass.startsWith(CALENDAR_CSS_CLASS_PREFIX)
				&& CALENDAR_ALIASES.containsKey(cssClass.substring(CALENDAR_CSS_CLASS_PREFIX.length()));
	}

	/**
	 * The colours are not needed to start: nothing here may stop the start.
	 * Every step catches its exceptions and logs a warning. The catch includes
	 * {@link LinkageError}: a failed static initializer of a colour class
	 * (ExceptionInInitializerError, NoClassDefFoundError) breaks the colours
	 * only. Other errors (OutOfMemoryError, StackOverflowError) are no
	 * colour problem and leave the JVM in an unknown state; they propagate.
	 */
	@Override
	public void afterPropertiesSet() {
		try {
			logIgnoredProperties(ignoredColorList, ignoredDefaultColor);
		} catch (RuntimeException | LinkageError e) {
			log.warn("Checking the removed colour properties failed", e);
		}
		try {
			checkColorKeys();
		} catch (RuntimeException | LinkageError e) {
			log.warn("Checking the colour keys failed", e);
		}
	}

	/**
	 * The palette is checked after the start, see {@link #event(Event)}: the
	 * GUI theme can not be loaded while Spring starts. The registration waits
	 * until all singletons exist: the bean FrameworkStartupEventChannel sets
	 * its channel and its coordinator in static fields when Spring creates it,
	 * and Spring can create this bean first. If the registration fails, the
	 * palette is checked once on its first use.
	 */
	@Override
	public void afterSingletonsInstantiated() {
		try {
			FrameworkStartupEventChannel.registerForStartupEvent(this);
		} catch (RuntimeException | LinkageError e) {
			log.warn("The colour palette is checked on its first use, the registration for the start event failed", e);
			lazyPaletteCheck.set(true);
		}
	}

	@Override
	public void event(Event event) {
		if (event instanceof FrameworkStartedEvent) {
			try {
				checkColorSets();
			} catch (RuntimeException | LinkageError e) {
				log.warn("Checking the colour sets against the palette failed", e);
			}
		}
	}

	/**
	 * Logs one line if an installation still sets the removed properties
	 * color.list or color.default, e.g. in olat.local.properties.
	 *
	 * @return true if a property is set and the line is logged
	 */
	static boolean logIgnoredProperties(String colorList, String defaultColor) {
		if (StringHelper.containsNonWhitespace(colorList) || StringHelper.containsNonWhitespace(defaultColor)) {
			log.info("The properties color.list and color.default are ignored: the colour choosers use the colour sets in code (ColorSet)");
			return true;
		}
		return false;
	}

	/**
	 * Checks the old keys of color.list, the badge colours and the calendar
	 * colours against the aliases and the form of the keys of the chooser
	 * sets. Needs no theme.
	 */
	void checkColorKeys() {
		for (String color : LEGACY_COLORS) {
			if (resolve(color, ColorFamily.COLOR_SERVICE) == null) {
				log.warn("Old color {} of color.list has no swatch in the palette", color);
			}
		}
		for (String color : BADGE_COLORS) {
			if (resolve(color, ColorFamily.COLOR_SERVICE) == null) {
				log.warn("Badge color {} has no swatch in the palette", color);
			}
		}
		for (String color : getCalendarColors()) {
			if (resolveStored(color, ColorFamily.CALENDAR) == null) {
				log.warn("Calendar color {} has no swatch in the palette", color);
			}
		}
		// the form of the keys
		for (ColorSet colorSet : ColorSet.values()) {
			if (!HUES.containsAll(colorSet.getHues())) {
				log.warn("The color set {} has hues that are no hues of the palette: {}", colorSet, colorSet.getHues());
			}
			List<String> keys = colorSet.getKeysWithThemeColors(List.of(Palette.THEME_COLOR_PREFIX + "1", Palette.THEME_COLOR_PREFIX + "2"));
			for (String key : keys) {
				Swatch swatch = Swatch.fromKey(key);
				if (swatch == null || !key.equals(swatch.key())) {
					log.warn("Color {} of the color set {} is no swatch key", key, colorSet);
				}
			}
		}
	}

	/**
	 * Checks the keys of all chooser sets against the palette of the GUI theme
	 * and logs each missing key once. Runs once after the start of the framework.
	 *
	 * @return The missing keys, empty if all resolve or if there is no palette
	 */
	Set<String> checkColorSets() {
		Palette palette = getPalette();
		if (palette == null) {
			return Set.of();
		}
		Set<String> missing = missingColorSetKeys(palette);
		if (!missing.isEmpty()) {
			String theme = ColorValidator.toLogValue(guiSettings.getGuiThemeIdentifyer());
			for (String key : missing) {
				log.warn("Color {} of the color sets has no swatch in the palette of the theme {}", key, theme);
			}
		}
		return missing;
	}

	/**
	 * @param palette The palette
	 * @return The keys of the chooser sets {@link ColorSet} with the hues and
	 * 		the theme colours of the palette that have no swatch in the palette,
	 * 		each key once, in the order of the sets. Empty if all resolve.
	 */
	static Set<String> missingColorSetKeys(Palette palette) {
		Set<String> missing = new LinkedHashSet<>();
		for (ColorSet colorSet : ColorSet.values()) {
			for (String key : colorSet.getKeysWithThemeColors(palette.getThemeColors())) {
				if (!hasSwatch(palette, Swatch.fromKey(key))) {
					missing.add(key);
				}
			}
		}
		return missing;
	}

	private static boolean hasSwatch(Palette palette, Swatch swatch) {
		if (swatch == null || !palette.contains(swatch.hue())) {
			return false;
		}
		try {
			return palette.bg(swatch) != null && palette.fg(swatch) != null;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	/**
	 * @return The palette, null if there are no GUI settings or reading the
	 * 		theme fails. A failure is logged once.
	 */
	@Override
	public Palette getPalette() {
		if (guiSettings == null) {
			return null;
		}
		if (lazyPaletteCheck.compareAndSet(true, false)) {
			try {
				checkColorSets();
			} catch (RuntimeException | LinkageError e) {
				log.warn("Checking the colour sets against the palette failed", e);
			}
		}
		try {
			Theme theme = guiSettings.getGuiTheme();
			return theme == null ? null : theme.getPalette();
		} catch (RuntimeException | LinkageError e) {
			if (!paletteFailureLogged) {
				paletteFailureLogged = true;
				log.warn("Reading the colour palette of the GUI theme failed, the colours have no palette", e);
			}
			return null;
		}
	}

	@Override
	public List<String> getThemeColors() {
		Palette palette = getPalette();
		return palette == null ? List.of() : palette.getThemeColors();
	}

	@Override
	public String getBackgroundColor(Swatch swatch, String fallback) {
		Palette palette = getPalette();
		if (swatch == null || palette == null || !palette.contains(swatch.hue())) {
			return fallback;
		}
		try {
			return palette.bg(swatch);
		} catch (IllegalArgumentException e) {
			// a swatch with an unknown variant
			return fallback;
		}
	}

	@Override
	public Swatch resolve(String storedValue, ColorFamily family) {
		if (!StringHelper.containsNonWhitespace(storedValue)) {
			return null;
		}
		
		String key = storedValue.trim();
		if (!ColorValidator.isValidColorValue(key)) {
			ColorValidator.logInvalid("stored colour", key);
			return null;
		}
		boolean swatchClass = key.startsWith(SWATCH_CSS_CLASS_PREFIX);
		ColorFamily keyFamily = family;
		if (swatchClass) {
			key = key.substring(SWATCH_CSS_CLASS_PREFIX.length());
			keyFamily = ColorFamily.SWATCH;
		}
		// theme colours in every family, e.g. "theme2", "theme2-light"
		Swatch swatch = Swatch.fromKey(key);
		if (swatch != null && Palette.isThemeColor(swatch.hue())) {
			return resolveThemeColor(swatch);
		}
		if (!swatchClass) {
			for (Map.Entry<String,Map<String,Swatch>> prefix : PREFIXES.entrySet()) {
				if (key.startsWith(prefix.getKey())) {
					return prefix.getValue().get(key.substring(prefix.getKey().length()));
				}
			}
		}
		return keyFamily == null ? null : resolveKey(key, keyFamily);
	}
	
	@Override
	public Swatch resolveStored(String storedValue, ColorFamily legacyFamily) {
		// the new key wins: "blue" is the solid blue in every family
		Swatch swatch = resolve(storedValue, ColorFamily.SWATCH);
		// an invalid value is logged once, see ColorValidator.logInvalid()
		if (swatch == null && legacyFamily != null && legacyFamily != ColorFamily.SWATCH) {
			swatch = resolve(storedValue, legacyFamily);
		}
		return swatch;
	}

	@Override
	public String toCalendarEventCssClass(String colorKey) {
		if (!StringHelper.containsNonWhitespace(colorKey)) {
			return null;
		}
		String key = colorKey.trim();
		if (Swatch.fromKey(key) != null) {
			String swatchCssClass = toSwatchCssClass(key);
			if (swatchCssClass != null) {
				return CALENDAR_SWATCH_CSS_CLASS + " " + swatchCssClass;
			}
		}
		String calendarCssClass = CALENDAR_CSS_CLASS_PREFIX + key;
		return isCalendarCssClass(calendarCssClass) ? calendarCssClass : null;
	}

	/**
	 * @return The theme colour with its variant, theme1 with the variant if the
	 * 		current theme does not define it, null if the theme has no theme colours
	 */
	private Swatch resolveThemeColor(Swatch swatch) {
		List<String> themeColors = getThemeColors();
		if (themeColors.contains(swatch.hue())) {
			return swatch;
		}
		return themeColors.isEmpty() ? null : new Swatch(themeColors.get(0), swatch.variant());
	}

	private Swatch resolveKey(String key, ColorFamily family) {
		return switch (family) {
			case COLOR_SERVICE -> COLOR_SERVICE_ALIASES.get(key);
			case CALENDAR -> CALENDAR_ALIASES.get(key);
			// theme colours are resolved before
			case SWATCH -> Swatch.fromKey(key);
		};
	}
	
	private static Map<String,Swatch> withBadgeColors(Map<String,Swatch> colorListAliases) {
		Map<String,Swatch> aliases = new LinkedHashMap<>(colorListAliases);
		aliases.putAll(aliases(
				"gold", "yellow", Swatch.SOLID,
				"silver", "gray", Swatch.TINT,
				"bronze", "orange", Swatch.TINT));
		return aliases;
	}
	
	private static Map<String,Swatch> aliases(String... keyHueVariant) {
		Map<String,Swatch> aliases = new LinkedHashMap<>();
		for (int i = 0; i < keyHueVariant.length; i += 3) {
			aliases.put(keyHueVariant[i], new Swatch(keyHueVariant[i + 1], keyHueVariant[i + 2]));
		}
		return aliases;
	}
	
	private static Map<String,Map<String,Swatch>> prefixes() {
		Map<String,Map<String,Swatch>> prefixes = new LinkedHashMap<>();
		// the custom alert box and the video markers never stored a badge colour
		prefixes.put("o_alert_box_color_", COLOR_LIST_ALIASES);
		prefixes.put("o_video_marker_", COLOR_LIST_ALIASES);
		prefixes.put(CSS_CLASS_PREFIX, COLOR_SERVICE_ALIASES);
		prefixes.put(CALENDAR_CSS_CLASS_PREFIX, CALENDAR_ALIASES);
		return prefixes;
	}

}

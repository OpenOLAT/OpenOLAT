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
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.olat.core.helpers.GUISettings;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Initial date: 2023-03-23<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Service
public class ColorServiceImpl implements ColorService, InitializingBean {
	
	private static final Logger log = Tracing.createLoggerFor(ColorServiceImpl.class);
	
	/** Keys of color.list and the badge colours gold, silver, bronze */
	static final Map<String,Swatch> COLOR_SERVICE_ALIASES = Collections.unmodifiableMap(aliases(
			"lightgray", "gray", Swatch.DARK,
			"yellow", "yellow", Swatch.SOLID,
			"orange", "orange", Swatch.DARK,
			"brown", "brown", Swatch.SOLID,
			"red", "red", Swatch.SOLID,
			"orchid", "pink", Swatch.SOLID,
			"purple", "violet", Swatch.DARK,
			"lightblue", "blue", Swatch.SOLID,
			"cobaltblue", "indigo", Swatch.SOLID,
			"darkblue", "indigo", Swatch.DARK,
			"lightgreen", "lime", Swatch.DARK,
			"seagreen", "green", Swatch.DARK,
			"gold", "yellow", Swatch.SOLID,
			"silver", "gray", Swatch.TINT,
			"bronze", "orange", Swatch.SOLID));
	
	/** Keys of CalendarColors */
	static final Map<String,Swatch> CALENDAR_ALIASES = Collections.unmodifiableMap(aliases(
			"grey", "gray", Swatch.DARK,
			"yellow", "yellow", Swatch.TINT,
			"orange", "orange", Swatch.TINT,
			"red", "red", Swatch.TINT,
			"maroon", "red", Swatch.DARK,
			"blue", "blue", Swatch.DARK,
			"navy", "indigo", Swatch.DARK,
			"fuchsia", "magenta", Swatch.SOLID,
			"rebeccapurple", "violet", Swatch.DARK,
			"lime", "lime", Swatch.SOLID,
			"green", "green", Swatch.SOLID,
			"olive", "lime", Swatch.DARK));
	
	/** Predefined colour categories, identifier with "_" instead of "." */
	static final Map<String,Swatch> CATEGORY_ALIASES = Collections.unmodifiableMap(aliases(
			"dark_blue", "indigo", Swatch.DARK,
			"light_blue", "blue", Swatch.SOLID,
			"purple", "magenta", Swatch.DARK,
			"red", "red", Swatch.SOLID,
			"orange", "orange", Swatch.SOLID,
			"yellow", "yellow", Swatch.SOLID,
			"light_green", "lime", Swatch.SOLID,
			"dark_green", "green", Swatch.DARK));
	
	/** Project avatars and user initials, same colours as the colour categories today */
	static final Map<String,Swatch> AVATAR_ALIASES = CATEGORY_ALIASES;
	
	/** Prefix of the generic swatch classes, e.g. "o_swatch_theme1" */
	private static final String SWATCH_CLASS_PREFIX = "o_swatch_";

	/** Prefixes of the CSS classes and the family each one defines */
	private static final Map<String,ColorFamily> PREFIXES = Collections.unmodifiableMap(prefixes());

	@Value("${color.list}")
	private String colorList;
	
	@Value("${color.default}")
	private String defaultColor;
	
	@Autowired
	private GUISettings guiSettings;
	
	@Override
	public List<String> getColors() {
		return listStringToList(colorList);
	}

	@Override
	public String getDefaultColor() {
		return defaultColor;
	}

	@Override
	public List<String> getColorsForBadges() {
		List<String> mutableList = getColors();
		mutableList.add(0, "gold");
		mutableList.add(1, "silver");
		mutableList.add(2, "bronze");
		return mutableList;
	}

	@Override
	public String toCssClass(String colorKey) {
		return colorKey == null ? null : CSS_CLASS_PREFIX + colorKey;
	}

	@Override
	public String toColorKey(String cssClass) {
		return cssClass != null && cssClass.startsWith(CSS_CLASS_PREFIX)
				? cssClass.substring(CSS_CLASS_PREFIX.length()) : cssClass;
	}

	@Override
	public void afterPropertiesSet() {
		for (String color : getColors()) {
			if (resolve(color, ColorFamily.COLOR_SERVICE) == null) {
				log.warn("Color {} of color.list has no swatch in the palette", color);
			}
		}
	}

	@Override
	public Palette getPalette() {
		return guiSettings == null ? null : guiSettings.getGuiTheme().getPalette();
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
		return palette.bg(swatch);
	}

	@Override
	public Swatch resolve(String storedValue, ColorFamily family) {
		if (!StringHelper.containsNonWhitespace(storedValue)) {
			return null;
		}
		
		String key = storedValue.trim();
		if (key.startsWith(SWATCH_CLASS_PREFIX)) {
			key = key.substring(SWATCH_CLASS_PREFIX.length());
		}
		if (Palette.isThemeColor(key)) {
			return resolveThemeColor(key);
		}
		for (Map.Entry<String,ColorFamily> prefix : PREFIXES.entrySet()) {
			if (key.startsWith(prefix.getKey())) {
				return resolveKey(key.substring(prefix.getKey().length()), prefix.getValue());
			}
		}
		return family == null ? null : resolveKey(key, family);
	}
	
	/**
	 * @return The theme colour, theme1 if the current theme does not define it,
	 * 		null if the theme has no theme colours
	 */
	private Swatch resolveThemeColor(String themeColor) {
		List<String> themeColors = getThemeColors();
		if (themeColors.contains(themeColor)) {
			return new Swatch(themeColor, Swatch.SOLID);
		}
		return themeColors.isEmpty() ? null : new Swatch(themeColors.get(0), Swatch.SOLID);
	}

	private Swatch resolveKey(String key, ColorFamily family) {
		return switch (family) {
			case COLOR_SERVICE -> COLOR_SERVICE_ALIASES.get(key);
			case CALENDAR -> CALENDAR_ALIASES.get(key);
			case CATEGORY -> CATEGORY_ALIASES.get(key.replace('.', '_'));
			case AVATAR -> AVATAR_ALIASES.get(key.replace('.', '_'));
		};
	}
	
	private static Map<String,Swatch> aliases(String... keyHueVariant) {
		Map<String,Swatch> aliases = new LinkedHashMap<>();
		for (int i = 0; i < keyHueVariant.length; i += 3) {
			aliases.put(keyHueVariant[i], new Swatch(keyHueVariant[i + 1], keyHueVariant[i + 2]));
		}
		return aliases;
	}
	
	private static Map<String,ColorFamily> prefixes() {
		Map<String,ColorFamily> prefixes = new LinkedHashMap<>();
		prefixes.put("o_alert_box_color_", ColorFamily.COLOR_SERVICE);
		prefixes.put("o_video_marker_", ColorFamily.COLOR_SERVICE);
		prefixes.put(CSS_CLASS_PREFIX, ColorFamily.COLOR_SERVICE);
		prefixes.put("o_cal_", ColorFamily.CALENDAR);
		prefixes.put("o_colcat_", ColorFamily.CATEGORY);
		prefixes.put("o_proj_avatar_", ColorFamily.AVATAR);
		prefixes.put("o_user_initials_", ColorFamily.AVATAR);
		return prefixes;
	}

	private List<String> listStringToList(String listString) {
		List<String> list = new ArrayList<>();
		if (StringHelper.containsNonWhitespace(listString)) {
			String[] itemArray = listString.split(",");
			for (String item : itemArray) {
				if (StringHelper.containsNonWhitespace(item)) {
					list.add(item);
				}
			}
		}
		return list;
	}
}

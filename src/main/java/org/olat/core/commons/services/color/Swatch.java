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

import java.util.List;

/**
 * A swatch of the {@link Palette}: a hue (e.g. "blue") or a theme colour
 * (e.g. "theme1") and a variant ({@link #SOFT}, {@link #LIGHT}, {@link #TINT},
 * {@link #SOLID} or {@link #DARK}). The colour values of the roles are
 * resolved by the {@link Palette}.
 * <p>
 * The key of a swatch is the hue for the solid variant, else the hue and the
 * variant separated by "-", e.g. "blue", "blue-light", "theme1-dark". The
 * chooser sets ({@link ColorSet}) store these keys.
 *
 * Initial date: 29 Oct 2026<br>
 * @author gnaegi, https://www.frentix.com
 */
public record Swatch(String hue, String variant) {

	/** The tinted fill: the roles soft-bg, soft-fg and soft-border */
	public static final String SOFT = "soft";
	public static final String LIGHT = "light";
	public static final String TINT = "tint";
	public static final String SOLID = "solid";
	public static final String DARK = "dark";

	/** The variants, light to dark */
	public static final List<String> VARIANTS = List.of(SOFT, LIGHT, TINT, SOLID, DARK);

	/** The separator of the hue and the variant in a key, e.g. "blue-light" */
	public static final String KEY_SEPARATOR = "-";

	/**
	 * @return The key, e.g. "blue" for the solid variant, "blue-light" for the
	 * 		light variant
	 */
	public String key() {
		return SOLID.equals(variant) ? hue : hue + KEY_SEPARATOR + variant;
	}

	/**
	 * @return The CSS classes, e.g. "o_swatch_blue" for the solid variant,
	 * 		"o_swatch_blue o_swatch_light" for the light variant. The class of
	 * 		the variant is the swatch prefix and the variant.
	 */
	public String cssClass() {
		String hueClass = ColorService.SWATCH_CSS_CLASS_PREFIX + hue;
		return SOLID.equals(variant) ? hueClass : hueClass + " " + ColorService.SWATCH_CSS_CLASS_PREFIX + variant;
	}

	/**
	 * Parses the key of a chooser set. The method checks the form of the key,
	 * not the palette: a theme colour that the current theme does not define
	 * is returned as it is.
	 *
	 * @param key A key, e.g. "blue", "blue-light", "theme2", "theme2-dark"
	 * @return The swatch, or null if the key is not a hue of
	 * 		{@link ColorService#HUES} or a theme colour (e.g. "theme2"), with
	 * 		no suffix or one of the suffixes of {@link #VARIANTS} except
	 * 		"solid"
	 */
	public static Swatch fromKey(String key) {
		if (key == null || key.isEmpty()) {
			return null;
		}
		String hue = key;
		String variant = SOLID;
		int index = key.lastIndexOf(KEY_SEPARATOR);
		if (index >= 0) {
			hue = key.substring(0, index);
			variant = key.substring(index + KEY_SEPARATOR.length());
			// one key per swatch: the solid variant has no suffix
			if (SOLID.equals(variant) || !VARIANTS.contains(variant)) {
				return null;
			}
		}
		if (!ColorService.HUES.contains(hue) && !Palette.isThemeColor(hue)) {
			return null;
		}
		return new Swatch(hue, variant);
	}
}

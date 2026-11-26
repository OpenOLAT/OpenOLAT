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

/**
 * The colour sets of the choosers. A set defines its hues, its theme colours
 * and the variants per colour. The keys of a set are the keys of its hues,
 * followed by the keys of its theme colours of the active theme, see
 * {@link ColorService#getColors(ColorSet)}. The keys have the form of
 * {@link Swatch#key()}.
 * <ul>
 * <li>{@link #S}: the solid variant of 7 hues and of the first theme colour,
 * 		e.g. "red", ..., "slate", "theme1"</li>
 * <li>{@link #M}: the solid variant of all {@link ColorService#HUES} and of
 * 		all theme colours, e.g. "red", ..., "gray", "theme1", "theme2"</li>
 * <li>{@link #L}: all variants, light to dark, of all hues and of all theme
 * 		colours, e.g. "blue-soft", "blue-light", "blue-tint", "blue", "blue-dark"</li>
 * </ul>
 *
 * Initial date: 24 Nov 2026<br>
 * @author gnaegi, https://www.frentix.com
 */
public enum ColorSet {

	/** The solid swatch of 7 hues and of the first theme colour (the primary colour of the theme) */
	S(List.of("red", "orange", "yellow", "green", "blue", "pink", "slate"), true, Swatch.SOLID),
	/** The solid swatch of all hues and of all theme colours */
	M(ColorService.HUES, false, Swatch.SOLID),
	/** All swatches of all hues and of all theme colours, light to dark, the order of the colour grid */
	L(ColorService.HUES, false, Swatch.SOFT, Swatch.LIGHT, Swatch.TINT, Swatch.SOLID, Swatch.DARK);

	/** The first theme colour, the primary colour of the theme */
	static final String FIRST_THEME_COLOR = Palette.THEME_COLOR_PREFIX + "1";

	private final List<String> hues;
	private final boolean firstThemeColorOnly;
	private final List<String> variants;

	private ColorSet(List<String> hues, boolean firstThemeColorOnly, String... variants) {
		this.hues = List.copyOf(hues);
		this.firstThemeColorOnly = firstThemeColorOnly;
		this.variants = List.of(variants);
	}

	/**
	 * @return The hues of the set, in the order of the set, a subset of
	 * 		{@link ColorService#HUES} in the same order
	 */
	public List<String> getHues() {
		return hues;
	}

	/**
	 * @return The variants per colour, in the order of the set
	 */
	public List<String> getVariants() {
		return variants;
	}

	/**
	 * @param themeColors The theme colours of a theme, e.g. "theme1", "theme2"
	 * @return The theme colours of the set: {@link #S} only "theme1" if the
	 * 		theme defines it, the other sets all theme colours. Empty if the
	 * 		theme has no theme colours.
	 */
	public List<String> getThemeColors(List<String> themeColors) {
		if (themeColors == null || themeColors.isEmpty()) {
			return List.of();
		}
		if (firstThemeColorOnly) {
			return themeColors.contains(FIRST_THEME_COLOR) ? List.of(FIRST_THEME_COLOR) : List.of();
		}
		return List.copyOf(themeColors);
	}

	/**
	 * @return The keys of the hues of the set, without the theme colours
	 */
	public List<String> getHueKeys() {
		return getKeys(hues);
	}

	/**
	 * @param themeColors The theme colours of a theme, e.g. "theme1", "theme2"
	 * @return The keys of the set: the keys of its hues, then the keys of its
	 * 		theme colours ({@link #getThemeColors(List)})
	 */
	public List<String> getKeysWithThemeColors(List<String> themeColors) {
		List<String> setThemeColors = getThemeColors(themeColors);
		if (setThemeColors.isEmpty()) {
			return getHueKeys();
		}
		List<String> keys = new ArrayList<>(getHueKeys());
		keys.addAll(getKeys(setThemeColors));
		return Collections.unmodifiableList(keys);
	}

	/**
	 * The key of the set nearest to a swatch, e.g. to preselect the colour of
	 * an item with an old stored value in a chooser of the set. The hue stays,
	 * the variant is the variant of the swatch if the set has it, else the
	 * solid variant if the set has it, else the first variant of the set.
	 *
	 * @param swatch A swatch, e.g. the result of {@link ColorService#resolveStored(String, ColorFamily)}
	 * @return The key, e.g. "blue" in {@link #M} for the swatch blue dark. Null
	 * 		if the swatch is null or the set has not its hue: a hue that is not
	 * 		in {@link #getHues()}, for {@link #S} a theme colour other than "theme1".
	 */
	public String nearestKey(Swatch swatch) {
		if (swatch == null || swatch.hue() == null) {
			return null;
		}
		String hue = swatch.hue();
		boolean themeColor = Palette.isThemeColor(hue);
		if (themeColor ? firstThemeColorOnly && !FIRST_THEME_COLOR.equals(hue) : !hues.contains(hue)) {
			return null;
		}
		String variant;
		if (variants.contains(swatch.variant())) {
			variant = swatch.variant();
		} else if (variants.contains(Swatch.SOLID)) {
			variant = Swatch.SOLID;
		} else {
			variant = variants.get(0);
		}
		return new Swatch(hue, variant).key();
	}

	/**
	 * @param colors Hues or theme colours, e.g. "red", "blue", "theme1". The
	 * 		colours are not checked against the hues of the set.
	 * @return The keys of the variants of the set for these colours: per
	 * 		colour the keys of all variants, e.g. for {@link #L} "red-soft",
	 * 		..., "red-dark", "blue-soft", ..., "blue-dark"
	 */
	public List<String> getKeys(List<String> colors) {
		if (colors == null || colors.isEmpty()) {
			return List.of();
		}
		List<String> keys = new ArrayList<>(colors.size() * variants.size());
		for (String color : colors) {
			for (String variant : variants) {
				keys.add(new Swatch(color, variant).key());
			}
		}
		return Collections.unmodifiableList(keys);
	}
}

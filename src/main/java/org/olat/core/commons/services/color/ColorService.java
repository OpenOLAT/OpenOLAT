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

import java.util.List;

/**
 * Initial date: 2023-03-23<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public interface ColorService {

	/** Prefix of the color CSS classes, e.g. "o_color_lightblue". */
	String CSS_CLASS_PREFIX = "o_color_";

	List<String> getColors();

	String getDefaultColor();

	List<String> getColorsForBadges();

	/**
	 * @param colorKey The bare color key, e.g. "lightblue"
	 * @return The color CSS class, e.g. "o_color_lightblue", or null if the key is null
	 */
	String toCssClass(String colorKey);

	/**
	 * @param cssClass The color CSS class, e.g. "o_color_lightblue"
	 * @return The bare color key, e.g. "lightblue". A value without the prefix is
	 * 		returned unchanged, null returns null.
	 */
	String toColorKey(String cssClass);

	/**
	 * Maps a stored colour value to its swatch of the {@link Palette}. The
	 * stored value is not changed.
	 * 
	 * @param storedValue A bare key (e.g. "lightblue", "light.blue", "blue") or a
	 * 		prefixed CSS class (e.g. "o_color_lightblue", "o_video_marker_lightblue",
	 * 		"o_alert_box_color_lightblue", "o_cal_blue", "o_colcat_light_blue",
	 * 		"o_proj_avatar_light_blue", "o_user_initials_light_blue"), or a theme
	 * 		colour ("theme2", "o_swatch_theme2"). A theme colour that the current
	 * 		theme does not define resolves to "theme1".
	 * @param family The family of a bare key. A prefixed class defines its
	 * 		family itself. If null, only prefixed classes are resolved.
	 * @return The swatch, or null if the value is unknown
	 */
	Swatch resolve(String storedValue, ColorFamily family);

	/**
	 * @return The colour palette of the current GUI theme, read from its
	 * 		theme.css, fallback the light theme. Null if no palette is available.
	 */
	Palette getPalette();

	/**
	 * @return The theme colours of the current GUI theme, e.g. "theme1",
	 * 		"theme2". Only the colours its theme.css defines; empty if none.
	 */
	List<String> getThemeColors();

	/**
	 * @param swatch The swatch, e.g. the result of {@link #resolve(String, ColorFamily)}
	 * @param fallback The colour if the swatch is null or the palette has no such hue
	 * @return The fill colour of the swatch in the palette of the current GUI theme
	 */
	String getBackgroundColor(Swatch swatch, String fallback);
}

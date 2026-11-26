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

	/** Prefix of the swatch CSS classes, e.g. "o_swatch_theme1". */
	String SWATCH_CSS_CLASS_PREFIX = "o_swatch_";

	/** Prefix of the old calendar color CSS classes, e.g. "o_cal_navy". */
	String CALENDAR_CSS_CLASS_PREFIX = "o_cal_";

	/**
	 * Class of a calendar element with the colour of a swatch, e.g.
	 * "o_cal_swatch o_swatch_blue": the calendar rules read the swatch tokens.
	 */
	String CALENDAR_SWATCH_CSS_CLASS = "o_cal_swatch";

	/**
	 * The keys of the old calendar colors. Calendar events, project
	 * appointments and milestones stored them before the chooser set
	 * {@link ColorSet#M} (the calendar configurations as class "o_cal_{key}",
	 * converted to the keys of the set M by the upgrade to 21.2). No chooser
	 * offers them any more. The keys yellow, orange, red, blue, lime and
	 * green are swatch keys and render as that swatch; the other keys render
	 * through the aliases of {@link ColorFamily#CALENDAR}, see
	 * {@link #toCalendarEventCssClass(String)}.
	 */
	List<String> CALENDAR_COLORS = List.of(
			"grey",
			"yellow",
			"orange",
			"red",
			"maroon",
			"blue",
			"navy",
			"fuchsia",
			"rebeccapurple",
			"lime",
			"green",
			"olive");

	/**
	 * The hues of the palette, in the order of the choosers. Every theme
	 * defines them. The chooser sets {@link ColorSet} start with these hues
	 * or a subset of them in the same order, the theme colours follow.
	 */
	List<String> HUES = List.of(
			"red",
			"orange",
			"yellow",
			"lime",
			"green",
			"teal",
			"cyan",
			"blue",
			"violet",
			"magenta",
			"pink",
			"brown",
			"slate",
			"gray");

	/**
	 * The default key of the chooser sets {@link ColorSet}, e.g. of a building.
	 */
	String DEFAULT_COLOR_KEY = "blue";

	/**
	 * The neutral key of the chooser sets {@link ColorSet}: the default of the
	 * choosers that took the first colour of the old color.list ("lightgray"),
	 * e.g. a new video annotation or the custom alert box of the page editor.
	 */
	String NEUTRAL_COLOR_KEY = "gray";

	/**
	 * The keys of the old color.list (olat.properties, removed). The page
	 * editor (block background, custom alert box), the video editor, the room
	 * management and the badges stored them before the chooser set
	 * {@link ColorSet#M}. No chooser offers them any more; the stored values
	 * render through the aliases of {@link ColorFamily#COLOR_SERVICE}.
	 */
	List<String> LEGACY_COLORS = List.of(
			"lightgray",
			"yellow",
			"orange",
			"brown",
			"red",
			"orchid",
			"purple",
			"lightblue",
			"cobaltblue",
			"darkblue",
			"lightgreen",
			"seagreen");

	/**
	 * The badge colours before the keys of {@link ColorSet#M}, see
	 * {@link #getColorsForBadges()}. Aliases of {@link ColorFamily#COLOR_SERVICE}.
	 */
	List<String> BADGE_COLORS = List.of(
			"gold",
			"silver",
			"bronze");

	/**
	 * @param colorSet The set
	 * @return The keys of the set: the keys of the hues of the set, then the
	 * 		keys of the theme colours of the set in the current GUI theme, e.g.
	 * 		for {@link ColorSet#M} "red", "orange", ..., "gray", "theme1", "theme2";
	 * 		for {@link ColorSet#S} "red", "orange", ..., "slate", "theme1"
	 */
	List<String> getColors(ColorSet colorSet);

	/**
	 * @param colorSet The set
	 * @return The keys of the set for the hues of the set only, without the
	 * 		theme colours, e.g. for random colours of avatars and user initials
	 */
	List<String> getHueColors(ColorSet colorSet);

	/**
	 * @param key A key of a chooser set, e.g. "blue", "blue-light", "theme2-dark"
	 * @return The swatch CSS classes, e.g. "o_swatch_blue",
	 * 		"o_swatch_blue o_swatch_light". A theme colour that the current
	 * 		theme does not define has the classes of "theme1". Null if the key
	 * 		does not resolve in the family {@link ColorFamily#SWATCH}.
	 */
	String toSwatchCssClass(String key);

	/**
	 * The key of a chooser set to preselect for a stored colour value: the
	 * value renders as {@link #resolveStored(String, ColorFamily)}, the key is
	 * the key of the set nearest to this swatch ({@link ColorSet#nearestKey(Swatch)}).
	 * The stored value is not changed.
	 *
	 * @param colorSet The set of the chooser
	 * @param storedValue The stored value, e.g. "blue", "navy", "o_cal_navy", "theme2"
	 * @param legacyFamily The family of the old keys of the stored value, may be null
	 * @return The key of the set, e.g. "blue" in {@link ColorSet#M} for "navy"
	 * 		(blue dark). Null if the value does not resolve or the set has not
	 * 		its hue. Never throws.
	 */
	String getNearestKey(ColorSet colorSet, String storedValue, ColorFamily legacyFamily);

	/**
	 * The CSS classes of a stored colour value that is a CSS class or a key of
	 * the chooser sets, e.g. the colour of a project avatar, of user initials,
	 * of a building or of a calendar. A legacy class stays: the alias classes
	 * of the theme render it.
	 *
	 * @param storedValue A key of the chooser sets (e.g. "blue", "theme2") or a
	 * 		CSS class (e.g. "o_color_lightblue", "o_user_initials_grey")
	 * @return The swatch classes of a key of the chooser sets (e.g.
	 * 		"o_swatch_blue", see {@link #toSwatchCssClass(String)}), else the
	 * 		trimmed value if it is a valid list of CSS class names, else null.
	 * 		Never throws.
	 */
	String toStoredValueCssClass(String storedValue);

	/**
	 * The CSS classes of a stored colour value of a chooser whose old values
	 * are bare keys or classes with a prefix, e.g. the background of a page
	 * editor block ("o_color_"), the custom alert box ("o_alert_box_color_")
	 * or a video marker ("o_video_marker_").
	 *
	 * @param storedValue A key of the chooser sets (e.g. "blue", "theme2"), an
	 * 		old bare key (e.g. "lightblue") or an old class with the prefix
	 * 		(e.g. "o_video_marker_lightblue")
	 * @param legacyPrefix The prefix of the old classes, e.g. "o_color_"
	 * @return The swatch classes of a key of the chooser sets (e.g.
	 * 		"o_swatch_blue", see {@link #toSwatchCssClass(String)}, null for a
	 * 		theme colour if the theme has none), the value if it starts with the
	 * 		prefix, else the prefix and the value; null if the value is blank
	 * 		or the result is no valid CSS class name
	 * 		({@link ColorValidator#isValidCssClass(String)}). The alias classes
	 * 		of the theme render an old value. Never throws.
	 */
	String toStoredValueCssClass(String storedValue, String legacyPrefix);

	/**
	 * @return The badge colours: {@link #BADGE_COLORS} ("gold", "silver",
	 * 		"bronze"), then the keys of {@link ColorSet#M}, a new list
	 */
	List<String> getColorsForBadges();

	/**
	 * @param colorKey The bare color key, e.g. "lightblue"
	 * @return The color CSS class, e.g. "o_color_lightblue", or null if the key
	 * 		is null, blank or the class is no valid CSS class name
	 * 		({@link ColorValidator#isValidCssClass(String)})
	 */
	String toCssClass(String colorKey);

	/**
	 * @param cssClass The color CSS class, e.g. "o_color_lightblue"
	 * @return The bare color key, e.g. "lightblue". A value without the prefix is
	 * 		returned unchanged, null returns null.
	 */
	String toColorKey(String cssClass);

	/**
	 * @return The keys of the old calendar colors, {@link #CALENDAR_COLORS}
	 */
	List<String> getCalendarColors();

	/**
	 * @param cssClass A CSS class, e.g. "o_cal_navy"
	 * @return true if the CSS class is exactly the class of one of the
	 * 		{@link #CALENDAR_COLORS} (lower case) that is no swatch key and
	 * 		renders through its alias: grey, maroon, navy, fuchsia,
	 * 		rebeccapurple, olive. False for the classes of the swatch keys,
	 * 		e.g. "o_cal_blue".
	 */
	boolean isCalendarCssClass(String cssClass);

	/**
	 * Maps a stored colour value to its swatch of the {@link Palette}. The
	 * stored value is not changed.
	 * 
	 * @param storedValue A bare key (e.g. "lightblue", "navy", "blue") or a
	 * 		prefixed CSS class (e.g. "o_color_lightblue", "o_video_marker_lightblue",
	 * 		"o_alert_box_color_lightblue", "o_cal_navy", "o_swatch_blue"), or a
	 * 		theme colour with an optional variant ("theme2", "theme2-light",
	 * 		"o_swatch_theme2"). A theme colour resolves
	 * 		in every family. A theme colour that the current theme does not
	 * 		define resolves to "theme1" with the same variant.
	 * @param family The family of a bare key. A prefixed class defines its
	 * 		family itself, "o_swatch_" the family {@link ColorFamily#SWATCH}.
	 * 		If null, only prefixed classes and theme colours are resolved.
	 * @return The swatch, or null if the value is unknown or is neither a
	 * 		colour key nor a CSS class name ({@link ColorValidator#isValidColorValue(String)}).
	 * 		Never throws.
	 */
	Swatch resolve(String storedValue, ColorFamily family);

	/**
	 * Maps a stored colour value to the swatch it renders as. Only swatches are
	 * rendered, an old key is an alias of a swatch:
	 * <ol>
	 * <li>A key of the chooser sets ({@link ColorFamily#SWATCH}, e.g. "blue",
	 * 		"blue-light", "theme2-dark") is that swatch, also if it is a key of
	 * 		the legacy family: the calendar colour "blue" is the solid blue.</li>
	 * <li>Else the value is resolved as alias of the legacy family, e.g.
	 * 		"navy" in {@link ColorFamily#CALENDAR}. A prefixed legacy CSS class
	 * 		(e.g. "o_cal_navy") is an alias of its own family.</li>
	 * </ol>
	 *
	 * @param storedValue The stored value, see {@link #resolve(String, ColorFamily)}
	 * @param legacyFamily The family of the old keys of the stored value, may be null
	 * @return The swatch, or null if the value is no key of the chooser sets and
	 * 		no alias. Never throws.
	 */
	Swatch resolveStored(String storedValue, ColorFamily legacyFamily);

	/**
	 * The CSS classes of the colour of a calendar event: a key of the chooser
	 * sets renders as its swatch, a calendar colour key as its calendar class.
	 *
	 * @param colorKey The stored colour key of the event, e.g. "blue-light", "navy"
	 * @return The classes {@link #CALENDAR_SWATCH_CSS_CLASS} and the swatch
	 * 		classes for a key of the chooser sets (e.g. "o_cal_swatch o_swatch_blue
	 * 		o_swatch_light"), the calendar class for a key of the
	 * 		{@link #CALENDAR_COLORS} (e.g. "o_cal_navy"), else null
	 */
	String toCalendarEventCssClass(String colorKey);

	/**
	 * @return The colour palette of the current GUI theme, read from its
	 * 		theme.css, fallback the light theme. Null if no palette is available
	 * 		or reading the theme fails. Never throws.
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

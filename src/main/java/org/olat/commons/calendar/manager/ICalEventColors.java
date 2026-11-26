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
package org.olat.commons.calendar.manager;

import java.util.Locale;

import org.olat.core.commons.services.color.ColorFamily;
import org.olat.core.commons.services.color.ColorService;
import org.olat.core.commons.services.color.ColorValidator;
import org.olat.core.commons.services.color.CssColorNames;
import org.olat.core.commons.services.color.Swatch;
import org.olat.core.util.StringHelper;

/**
 * Maps the colour key of a calendar event to the value of the iCalendar
 * property <code>COLOR</code> (RFC 7986), which takes a CSS colour name.
 * <p>
 * A key that is a CSS colour name (e.g. the calendar colours "blue", "navy")
 * is used unchanged. Any other key (e.g. "teal", "blue-light", "theme1") is
 * resolved to its swatch, and the fill colour of the swatch in the palette
 * of the current GUI theme is mapped to the nearest CSS colour name.
 *
 * Initial date: 24 Nov 2026<br>
 * @author gnaegi, https://www.frentix.com
 */
public class ICalEventColors {

	private ICalEventColors() {
		//
	}

	/**
	 * @param colorKey The colour key of the event, e.g. "blue", "cobaltblue", "theme1"
	 * @return The trimmed key in lower case if it is a valid colour value
	 * 		({@link ColorValidator#isValidColorValue(String)}), else null. An
	 * 		invalid value is logged. The colour keys are lower case: the lower
	 * 		case lets "Blue" of another application match the key "blue".
	 */
	public static String validColorKey(String colorKey) {
		if (!StringHelper.containsNonWhitespace(colorKey)) {
			return null;
		}
		String key = colorKey.trim().toLowerCase(Locale.ROOT);
		if (ColorValidator.isValidColorValue(key)) {
			return key;
		}
		ColorValidator.logInvalid("calendar event colour", key);
		return null;
	}

	/**
	 * Reads the colour key of an event of an iCalendar file. The file can be
	 * an imported or a synchronized calendar of another application: both
	 * values are untrusted. An invalid value is dropped and logged.
	 *
	 * @param xOlatColor The value of the property X-OLAT-COLOR, the colour key
	 * 		that OpenOlat writes, e.g. "cobaltblue"
	 * @param cssColor The value of the property COLOR, a CSS colour name, e.g. "Blue"
	 * @return The value of X-OLAT-COLOR if it is valid, else the value of COLOR
	 * 		in lower case if it is a valid colour key, else null
	 */
	public static String fromICal(String xOlatColor, String cssColor) {
		String key = validColorKey(xOlatColor);
		if (key != null) {
			return key;
		}
		if (!StringHelper.containsNonWhitespace(cssColor)) {
			return null;
		}
		String colorName = cssColor.trim().toLowerCase(Locale.ROOT);
		if (ColorValidator.isValidKey(colorName)) {
			return colorName;
		}
		ColorValidator.logInvalid("iCalendar COLOR", cssColor);
		return null;
	}

	/**
	 * @param colorKey The colour key of the event, e.g. "blue", "teal", "theme1"
	 * @param colorService The colour service, resolves the keys which are no CSS colour name
	 * @return The CSS colour name, or null if the key is empty, invalid or unknown
	 */
	public static String toCssColorName(String colorKey, ColorService colorService) {
		String key = validColorKey(colorKey);
		if (key == null) {
			return null;
		}
		if (CssColorNames.isColorName(key)) {
			return key;
		}
		if (colorService == null) {
			return null;
		}
		
		Swatch swatch = resolve(key, colorService);
		String hex = colorService.getBackgroundColor(swatch, null);
		return CssColorNames.nearestColorName(hex);
	}

	/**
	 * Resolves the key as the event renders it: a key of the chooser sets as
	 * its swatch (e.g. "teal", "blue-light", "theme1"), else as alias of a
	 * calendar colour (e.g. "navy"). Other keys have no colour.
	 */
	private static Swatch resolve(String colorKey, ColorService colorService) {
		return colorService.resolveStored(colorKey, ColorFamily.CALENDAR);
	}
}

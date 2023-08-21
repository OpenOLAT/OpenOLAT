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

import org.olat.core.gui.components.form.flexible.elements.ColorPickerElement;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;

/**
 * Initial date: Aug 18, 2023
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class ColorUIFactory {

	// Utility class should not have public constructor
	private ColorUIFactory() {
	}

	/**
	 * create color objects with given names and cssPrefix
	 *
	 * @param colorNames
	 * @param locale
	 * @param cssClassPrefix
	 * @return list of color objects
	 */
	public static List<ColorPickerElement.Color> createColors(List<String> colorNames, Locale locale, String cssClassPrefix) {
		List<ColorPickerElement.Color> colors = new ArrayList<>();
		Translator translator = Util.createPackageTranslator(ColorServiceImpl.class, locale);
		for (String colorName : colorNames) {
			String translatedColorName = translator.translate("color.".concat(colorName));
			colors.add(new ColorPickerElement.Color(colorName, translatedColorName, cssClassPrefix + colorName));
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
		return createColors(colorNames, locale, "o_color_");
	}
}

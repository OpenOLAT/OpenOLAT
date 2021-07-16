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
package org.olat.course.style.ui;

import java.util.Locale;

import org.olat.core.gui.control.Event;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.style.ColorCategory;
import org.olat.course.style.Header;
import org.olat.course.style.TeaserImageStyle;

/**
 * 
 * Initial date: 23 Jun 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CourseStyleUIFactory {
	
	public static final Event HEADER_CHANGED_EVENT = new Event("header-changed");
	
	public static Translator getTranslator(Locale locale) {
		return Util.createPackageTranslator(CourseStyleUIFactory.class, locale);
	}
	
	/**
	 * Translates the color category and adds a prefix if disabled.
	 *
	 * @param translator the translator of this package
	 * @param colorCategory
	 * @return
	 */
	public static String translate(Translator translator, ColorCategory colorCategory) {
		String name = translator.translate(getI18nKey(colorCategory));
		return colorCategory.isEnabled()
				? name
				: translator.translate("color.category.name.disabled", new String[] {name});
	}

	public static String translateInherited(Translator translator, ColorCategory colorCategory) {
		String name = translate(translator, colorCategory);
		return translator.translate("color.category.inherited", new String[] {name});
	}
	
	public static String getI18nKey(ColorCategory colorCategory) {
		return "color.category.id." + colorCategory.getIdentifier();
	}

	public static String getIconLeftCss(ColorCategory colorCategory) {
		return "o_square o_colcat_bg " + colorCategory.getCssClass();
	}
	
	public static String getI18nKey(TeaserImageStyle style) {
		return "teaser.image.style." + style.name();
	}
	
	public static boolean hasValues(Header header) {
		return StringHelper.containsNonWhitespace(header.getTitle())
				|| StringHelper.containsNonWhitespace(header.getObjectives())
				|| StringHelper.containsNonWhitespace(header.getInstruction())
				|| StringHelper.containsNonWhitespace(header.getInstructionalDesign());
	}

}

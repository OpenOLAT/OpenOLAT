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
import org.olat.course.nodes.CourseNode;
import org.olat.course.style.ColorCategory;
import org.olat.course.style.Header;
import org.olat.course.style.Header.Builder;
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
	public static String translateDisabled(Translator translator, ColorCategory colorCategory) {
		String name = translate(translator, colorCategory);
		return colorCategory.isEnabled()
				? name
				: translator.translate("color.category.name.disabled", new String[] {name});
	}

	public static String translateInherited(Translator translator, ColorCategory colorCategory) {
		String name = translateDisabled(translator, colorCategory);
		return translator.translate("color.category.inherited", new String[] {name});
	}
	
	public static String getI18nKey(ColorCategory colorCategory) {
		return "color.category.id." + colorCategory.getIdentifier();
	}
	
	public static String translate(Translator translator, ColorCategory colorCategory) {
		String i18nKey = getI18nKey(colorCategory);
		String translation = translator.translate(i18nKey);
		if(i18nKey.equals(translation) || translation.length() > 256) {
			translation = colorCategory.getIdentifier();
		}
		return translation;
	}

	public static String getIconLeftCss(ColorCategory colorCategory) {
		return "o_square o_square_border o_colcat_bg " + colorCategory.getCssClass();
	}
	
	public static String getI18nKey(TeaserImageStyle style) {
		return "teaser.image.style." + style.name();
	}
	
	public static String getSystemImageI18nKey(String filename) {
		return "system.image.id." + filename;
	}
	
	public static String translateSystemImage(Translator translator, String filename) {
		String i18nKey = getSystemImageI18nKey(filename);
		String translation = translator.translate(i18nKey);
		if(i18nKey.equals(translation) || translation.length() > 256) {
			translation = filename;
		}
		return translation;
	}
	
	public static void addMetadata(Header.Builder builder, CourseNode courseNode, String displayOption, boolean coach) {
		if (CourseNode.DISPLAY_OPTS_SHORT_TITLE_CONTENT.equals(displayOption)) {
			builder.withTitle(courseNode.getShortTitle());
		} else if (CourseNode.DISPLAY_OPTS_TITLE_CONTENT.equals(displayOption)) {
			builder.withTitle(courseNode.getLongTitle());
		} else if (CourseNode.DISPLAY_OPTS_SHORT_TITLE_DESCRIPTION_CONTENT.equals(displayOption)) {
			builder.withTitle(courseNode.getShortTitle());
			addExtendedHeader(builder, courseNode, coach);
		} else if (CourseNode.DISPLAY_OPTS_TITLE_DESCRIPTION_CONTENT.equals(displayOption)) {
			builder.withTitle(courseNode.getLongTitle());
			addExtendedHeader(builder, courseNode, coach);
		} else if (CourseNode.DISPLAY_OPTS_DESCRIPTION_CONTENT.equals(displayOption)) {
			addExtendedHeader(builder, courseNode, coach);
		}
	}
	
	private static void addExtendedHeader(Builder builder, CourseNode courseNode, boolean coach) {
		builder.withDescription(courseNode.getDescription());
		builder.withObjectives(courseNode.getObjectives());
		builder.withInstruction(courseNode.getInstruction());
		if (coach) {
			builder.withInstrucionalDesign(courseNode.getInstructionalDesign());
		}
	}
	
	public static boolean hasValues(Header header) {
		return StringHelper.containsNonWhitespace(header.getTitle())
				|| StringHelper.containsNonWhitespace(header.getDescription())
				|| StringHelper.containsNonWhitespace(header.getObjectives())
				|| StringHelper.containsNonWhitespace(header.getInstruction())
				|| StringHelper.containsNonWhitespace(header.getInstructionalDesign());
	}

}

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

import java.util.Date;
import java.util.Locale;

import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.duedate.DueDateConfig;
import org.olat.course.duedate.ui.DueDateConfigFormatter;
import org.olat.course.learningpath.LearningPathStatus;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
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
		if (CourseNode.DISPLAY_OPTS_TITLE_CONTENT.equals(displayOption)) {
			builder.withTitle(courseNode.getLongTitle());
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
	
	public static void addHandlingRangeData(Header.Builder builder, AssessmentEvaluation evaluation) {
		LearningPathStatus learningPathStatus = LearningPathStatus.of(evaluation);
		if (LearningPathStatus.done != learningPathStatus) {
			builder.withDuration(evaluation.getDuration());
			
			Date startDate = evaluation.getStartDate();
			if (startDate != null && startDate.after(new Date())) {
				builder.withStartDateConfig(DueDateConfig.absolute(startDate));
			}
			Date currentEndDate = evaluation.getEndDate().getCurrent();
			if (currentEndDate != null) {
				builder.withEndDateConfig(DueDateConfig.absolute(currentEndDate));
			}
		}
	}
	
	public static String formatHandlingRangeDate(Translator translator, DueDateConfig startDateConfig, DueDateConfig endDateConfig, Integer duration) {
		DueDateConfigFormatter dueDateConfigFormatter = DueDateConfigFormatter.create(translator.getLocale());
		StringBuilder sb = new StringBuilder();
		String formattedStartDate = dueDateConfigFormatter.formatDueDateConfig(startDateConfig);
		String formattedEndDate = dueDateConfigFormatter.formatDueDateConfig(endDateConfig);
		if (StringHelper.containsNonWhitespace(formattedStartDate) && StringHelper.containsNonWhitespace(formattedEndDate)) {
			sb.append(translator.translate("todo.range", new String[] {formattedStartDate, formattedEndDate} ));
		} else if (StringHelper.containsNonWhitespace(formattedStartDate)) {
			sb.append(translator.translate("table.header.start")).append(": ").append(formattedStartDate);
		} else if (StringHelper.containsNonWhitespace(formattedEndDate)) {
			sb.append(translator.translate("table.header.end")).append(": ").append(formattedEndDate);
		}
		if (duration != null) {
			if (sb.length() > 0) {
				sb.append(" | ");
			}
			sb.append(translator.translate("table.header.duration")).append(": ").append(translator.translate("minutes", new String[] {duration.toString()}));
		}
		
		return sb.toString();
	}
	
	public static boolean hasValues(Header header) {
		return StringHelper.containsNonWhitespace(header.getTitle())
				|| StringHelper.containsNonWhitespace(header.getDescription())
				|| StringHelper.containsNonWhitespace(header.getObjectives())
				|| StringHelper.containsNonWhitespace(header.getInstruction())
				|| StringHelper.containsNonWhitespace(header.getInstructionalDesign())
				|| DueDateConfig.isDueDate(header.getStartDateConfig())
				|| DueDateConfig.isDueDate(header.getEndDateConfig())
				|| header.getDuration() != null;
	}

}

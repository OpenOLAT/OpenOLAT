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
package org.olat.course.duedate.ui;

import java.util.List;
import java.util.Locale;

import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.course.duedate.AbsoluteDueDateConfig;
import org.olat.course.duedate.DueDateConfig;
import org.olat.course.duedate.DueDateService;
import org.olat.course.duedate.RelativeDueDateConfig;
import org.olat.course.nodes.GTACourseNode;

/**
 * 
 * Initial date: 5 Nov 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DueDateConfigFormatter {
	
	private final Translator translator;
	
	public static DueDateConfigFormatter create(Locale locale) {
		return new DueDateConfigFormatter(locale);
	}
	
	private DueDateConfigFormatter(Locale locale) {
		this.translator = Util.createPackageTranslator(DueDateConfigFormatter.class, locale);
	}
	
	public String getRelativeToTypeName(String type) {
		return translator.translate(getI18nKey(type));
	}
	
	private String getI18nKey(String type) {
		switch(type) {
		case DueDateService.TYPE_COURSE_START: return "relative.to.course.start";
		case DueDateService.TYPE_COURSE_LAUNCH: return "relative.to.course.launch";
		case DueDateService.TYPE_ENROLLMENT: return "relative.to.enrollment";
		case GTACourseNode.TYPE_RELATIVE_TO_ASSIGNMENT: return "relative.to.assignment";
		default:
		}
		return null;
	}
	
	public String getRelativeToTypeNameAfter(String type) {
		return translator.translate(getI18nKeyAfter(type));
	}
	
	private String getI18nKeyAfter(String type) {
		switch(type) {
		case DueDateService.TYPE_COURSE_START: return "relative.to.course.start.after";
		case DueDateService.TYPE_COURSE_LAUNCH: return "relative.to.course.launch.after";
		case DueDateService.TYPE_ENROLLMENT: return "relative.to.enrollment.after";
		case GTACourseNode.TYPE_RELATIVE_TO_ASSIGNMENT: return "relative.to.assignment.after";
		default:
		}
		return null;
	}
	
	public String formatDueDateConfig(DueDateConfig config) {
		if (DueDateConfig.isAbsolute(config)) {
			return formatAbsoluteDateConfig(config);
		} else if (DueDateConfig.isRelative(config)) {
			return formatRelativDateConfig(config);
		}
		return null;
	}
	
	public String formatAbsoluteDateConfig(AbsoluteDueDateConfig config) {
		return Formatter.getInstance(translator.getLocale()).formatDateAndTime(config.getAbsoluteDate());
	}

	public String formatRelativDateConfig(RelativeDueDateConfig config) {
		return concatRelativeDateConfig(config.getNumOfDays(), getRelativeToTypeNameAfter(config.getRelativeToType()));
	}
	
	public String concatRelativeDateConfig(int numOfDays, String relativeToTypeName) {
		return new StringBuilder()
				.append(Integer.toString(numOfDays)).append(" ")
				.append(translator.translate("days.after")).append(" ")
				.append(relativeToTypeName).toString();
	}

	public void addCourseRelativeToDateTypes(SelectionValues selectionValues, List<String> courseRelativeToDateTypes) {
		for (String type : courseRelativeToDateTypes) {
			selectionValues.add(SelectionValues.entry(type, getRelativeToTypeName(type)));
		}
	}

}

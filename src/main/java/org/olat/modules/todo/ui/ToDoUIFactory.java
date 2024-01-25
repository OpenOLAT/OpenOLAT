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
package org.olat.modules.todo.ui;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.modules.todo.ToDoExpenditureOfWork;
import org.olat.modules.todo.ToDoPriority;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ToDoTask;
import org.olat.modules.todo.model.ToDoExpenditureOfWorkImpl;

/**
 * 
 * Initial date: 28 Mar 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ToDoUIFactory {
	
	private static final Pattern WEEK_PATTERN = Pattern.compile("(\\d+)w");
	private static final Pattern DAY_PATTERN = Pattern.compile("(\\d+)d");
	private static final Pattern HOUR_PATTERN = Pattern.compile("(\\d+)h");
	
	public static String getDisplayName(Translator translator, ToDoTask toDoTask) {
		return StringHelper.containsNonWhitespace(toDoTask.getTitle())
				? toDoTask.getTitle()
				: getNoTitle(translator);
	}
	
	public static String getNoTitle(Translator translator) {
		return translator.translate("no.title");
	}
	
	public static String getDisplayName(Translator translator, ToDoStatus status) {
		if (status == null) return null;
		
		return switch (status) {
		case open -> translator.translate("task.status.open");
		case inProgress -> translator.translate("task.status.in.progress");
		case done -> translator.translate("task.status.done");
		case deleted -> translator.translate("task.status.deleted");
		default -> null;
		};
	}
	
	public final static String getIconCss(ToDoStatus status) {
		if (status == null) return null;
		
		return switch (status) {
		case open -> "o_icon_todo_status_open";
		case inProgress -> "o_icon_todo_status_in_progress";
		case done -> "o_icon_todo_status_done";
		case deleted -> "o_icon_todo_status_deleted";
		default -> null;
		};
	}
	
	public static String getDisplayName(Translator translator, ToDoPriority priority) {
		if (priority == null) return null;
		
		return switch (priority) {
		case urgent -> translator.translate("task.priority.urgent");
		case high -> translator.translate("task.priority.high");
		case medium -> translator.translate("task.priority.medium");
		case low -> translator.translate("task.priority.low");
		default -> null;
		};
	}
	
	public final static String getIconCss(ToDoPriority priority) {
		if (priority == null) return null;
				
		return switch (priority) {
		case urgent -> "o_icon_todo_priority_urgent";
		case high -> "o_icon_todo_priority_high";
		case medium -> "o_icon_todo_priority_medium";
		case low -> "o_icon_todo_priority_low";
		default -> null;
		};
	}
	
	public static String getDateOrAnytime(Translator translator, Date date) {
		return date == null
				? translator.translate("task.due.anytime")
				: Formatter.getInstance(translator.getLocale()).formatDate(date);
	}
	
	public static String getDetailsDescription(ToDoTask toDoTask) {
		if (toDoTask != null) {
			String description = toDoTask.getDescription();
			if (StringHelper.containsNonWhitespace(description)) {
				return Formatter.stripTabsAndReturns(toDoTask.getDescription()).toString();
			}
		}
		return null;
	}
	
	public static Due getDue(Translator translator, LocalDate dueDate, LocalDate now, ToDoStatus status) {
		if (dueDate == null || now == null) {
			return new Due(translator.translate("task.due.anytime"), null);
		}
		
		long days = ChronoUnit.DAYS.between(now, dueDate);
		if (days < -1) {
			return new Due(translator.translate("task.due.overdue.days", String.valueOf(-days)), ToDoStatus.STATUS_OVERDUE.contains(status)? Boolean.TRUE: null);
		} else if (days == -1) {
			return new Due(translator.translate("yesterday"), ToDoStatus.STATUS_OVERDUE.contains(status)? Boolean.TRUE: null);
		} else if (days == 0) {
			return new Due(translator.translate("today"), ToDoStatus.STATUS_OVERDUE.contains(status)? Boolean.FALSE: null);
		} else if (days == 1) {
			return new Due(translator.translate("tomorrow"), ToDoStatus.STATUS_OVERDUE.contains(status)? Boolean.FALSE: null);
		} else {
			return new Due(translator.translate("task.due.left.days", String.valueOf(days)),ToDoStatus.STATUS_OVERDUE.contains(status)? Boolean.FALSE: null);
		}
	}
	
	public static record Due(String name, Boolean overdue) { }

	public static String formatLong(Translator translator, ToDoExpenditureOfWork expenditureOfWork) {
		if (expenditureOfWork == null) return translator.translate("task.expenditure.of.work.not.available");
		
		StringBuilder sb = new StringBuilder();
		if (expenditureOfWork.getWeeks() > 0) {
			sb.append(String.valueOf(expenditureOfWork.getWeeks())).append(" ");
			sb.append(translator.translate("task.expenditure.of.work.weeks")).append(" ");
		}
		if (expenditureOfWork.getDays() > 0) {
			sb.append(String.valueOf(expenditureOfWork.getDays())).append(" ");
			sb.append(translator.translate("task.expenditure.of.work.days")).append(" ");
		}
		if (expenditureOfWork.getHours() > 0) {
			sb.append(String.valueOf(expenditureOfWork.getHours())).append(" ");
			sb.append(translator.translate("task.expenditure.of.work.hours"));
		}
		
		return sb.length() != 0? sb.toString(): translator.translate("task.expenditure.of.work.not.available");
	}

	public static String format(ToDoExpenditureOfWork expenditureOfWork) {
		if (expenditureOfWork == null) return null;
		
		StringBuilder sb = new StringBuilder();
		if (expenditureOfWork.getWeeks() > 0) {
			sb.append(String.valueOf(expenditureOfWork.getWeeks()));
			sb.append("w ");
		}
		if (expenditureOfWork.getDays() > 0) {
			sb.append(String.valueOf(expenditureOfWork.getDays()));
			sb.append("d ");
		}
		if (expenditureOfWork.getHours() > 0) {
			sb.append(String.valueOf(expenditureOfWork.getHours()));
			sb.append("h");
		}
		
		return sb.length() != 0? sb.toString(): null;
	}
	
	public static ToDoExpenditureOfWork parseHours(String hoursStr) {
		if (!StringHelper.containsNonWhitespace(hoursStr)) {
			return null;
		}
		
		String hoursStrIntern = hoursStr.trim().toLowerCase().replaceAll("\\s", "");
		
		Matcher matcher = WEEK_PATTERN.matcher(hoursStrIntern);
		long weeks = matcher.find()? Long.parseLong(matcher.group(1)): 0;
		matcher = DAY_PATTERN.matcher(hoursStrIntern);
		long days = matcher.find()? Long.parseLong(matcher.group(1)): 0;
		matcher = HOUR_PATTERN.matcher(hoursStrIntern);
		long hours = matcher.find()? Long.parseLong(matcher.group(1)): 0;
		
		if (weeks == 0 && days == 0 && hours == 0 && StringHelper.isLong(hoursStrIntern)) {
			// If the sting is a plan number we treat it as hours
			hours = Long.valueOf(hoursStrIntern);
		}
		
		return new ToDoExpenditureOfWorkImpl(weeks, days, hours);
	}

	public static String format(Translator translator, Formatter formatter, VariousDate variousDate) {
		return variousDate.various()
				? various(translator)
				: formatter.formatDate(variousDate.date());
	}

	public static String various(Translator translator) {
		return translator.translate("various");
	}
	
	static final record VariousDate(boolean various, Date date) { }

}

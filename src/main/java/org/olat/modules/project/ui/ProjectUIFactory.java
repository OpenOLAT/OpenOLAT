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
package org.olat.modules.project.ui;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.media.StringMediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.modules.project.ProjActivity.Action;
import org.olat.modules.project.ProjAppointment;
import org.olat.modules.project.ProjFile;
import org.olat.modules.project.ProjMilestone;
import org.olat.modules.project.ProjMilestoneStatus;
import org.olat.modules.project.ProjNote;
import org.olat.modules.project.ProjectRole;
import org.olat.modules.project.ProjectStatus;
import org.olat.modules.project.model.ProjFormattedDateRange;
import org.olat.user.UserManager;

/**
 * 
 * Initial date: 21 Nov 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjectUIFactory {
	
	public static final String COLOR_APPOINTMENT = KalendarRenderWrapper.CALENDAR_COLOR_BLUE;
	public static final String COLOR_MILESTONE = KalendarRenderWrapper.CALENDAR_COLOR_ORANGE;
	
	public static String getStatusIconCss(ProjectStatus status) {
		if (status != null) {
			switch (status) {
			case active: return "o_icon_proj_project_status_active";
			case done: return "o_icon_proj_project_status_done";
			case deleted: return "o_icon_proj_project_status_deleted";
			default:
			}
		}
		return null;
	}

	public static String translateStatus(Translator translator, ProjectStatus status) {
		if (status != null) {
			switch (status) {
			case active: return translator.translate("status.active");
			case done: return translator.translate("status.done");
			case deleted: return translator.translate("status.deleted");
			default:
			}
		}
		return null;
	}

	public static String translateRole(Translator translator, ProjectRole role) {
		if (role != null) {
			return translator.translate("role." + role.name());
		}
		return null;
	}
	
	public static String getDisplayName(ProjFile file) {
		return StringHelper.containsNonWhitespace(file.getVfsMetadata().getTitle())
				? file.getVfsMetadata().getTitle()
				: file.getVfsMetadata().getFilename();
	}

	public static String getDisplayName(Translator translator, ProjNote note) {
		return StringHelper.containsNonWhitespace(note.getTitle())
				? note.getTitle()
				: getNoTitle(translator);
	}
	
	public static String getDisplayName(Translator translator, ProjAppointment appointment) {
		return StringHelper.containsNonWhitespace(appointment.getSubject())
				? appointment.getSubject()
				: getNoTitle(translator);
	}
	
	public static String getDisplayName(Translator translator, ProjMilestone milestone) {
		return StringHelper.containsNonWhitespace(milestone.getSubject())
				? milestone.getSubject()
				: getNoTitle(translator);
	}
	
	public static String getNoTitle(Translator translator) {
		return translator.translate("no.title");
	}
	
	public static ProjFormattedDateRange formatRange(Translator translator, Date start, Date end) {
		ProjFormattedDateRange formatedRange = new ProjFormattedDateRange();
		
		boolean sameDay = DateUtils.isSameDay(start, end);
		boolean sameTime = DateUtils.isSameTime(start, end);
		String startDate = StringHelper.formatLocaleDateFull(start.getTime(), translator.getLocale());
		String startTime = StringHelper.formatLocaleTime(start.getTime(), translator.getLocale());
		String endDate = StringHelper.formatLocaleDateFull(end.getTime(), translator.getLocale());
		String endTime = StringHelper.formatLocaleTime(end.getTime(), translator.getLocale());
		if (sameDay) {
			formatedRange.setDate(startDate);
			StringBuilder timeSb = new StringBuilder();
			if (sameTime) {
				timeSb.append(translator.translate("full.day"));
			} else {
				timeSb.append(startTime);
				timeSb.append(" - ");
				timeSb.append(endTime);
			}
			formatedRange.setTime(timeSb.toString());
		} else {
			StringBuilder dateSbShort1 = new StringBuilder();
			dateSbShort1.append(startDate);
			dateSbShort1.append(" ");
			dateSbShort1.append(startTime);
			dateSbShort1.append(" -");
			formatedRange.setDate(dateSbShort1.toString());
			StringBuilder dateSb2 = new StringBuilder();
			dateSb2.append(endDate);
			dateSb2.append(" ");
			dateSb2.append(endTime);
			formatedRange.setDate2(dateSb2.toString());
		}
		
		return formatedRange;
	}
	
	public static MultipleSelectionElement createMembersElement(FormUIFactory uifactory, FormItemContainer formLayout,
			UserManager userManager, Collection<Identity> projectMembers, Collection<Identity> currentMembers) {
		Set<Identity> allMembers = new HashSet<>(projectMembers);
		allMembers.addAll(currentMembers);
		
		SelectionValues membersSV = new SelectionValues();
		allMembers.forEach(member -> membersSV.add(
				SelectionValues.entry(
						member.getKey().toString(),
						userManager.getUserDisplayName(member.getKey()))));
		membersSV.sort(SelectionValues.VALUE_ASC);
		
		MultipleSelectionElement membersEl = uifactory.addCheckboxesDropdown("involved", "involved", formLayout,
				membersSV.keys(), membersSV.values());
		currentMembers.forEach(member -> membersEl.select(member.getKey().toString(), true));
		
		return membersEl;
	}
	
	public static String getDisplayName(Translator translator, ProjMilestoneStatus status) {
		return switch (status) {
		case open -> translator.translate("milestone.status.open");
		case achieved -> translator.translate("milestone.status.achieved");
		default -> null;
		};
	}
	
	public static String getMilestoneStatusIconCss(ProjMilestoneStatus status) {
		return switch (status) {
		case open -> "o_icon_proj_milestone_status_open";
		case achieved -> "o_icon_proj_milestone_status_achieved";
		default -> null;
		};
	}

	public static String getActionIconCss(Action action) {
		switch (action.getTarget()) {
		case project: return "o_icon_proj_project";
		case file: return "o_icon_proj_file";
		case toDo: return "o_icon_todo_task";
		case note: return "o_icon_proj_note";
		case appointment: return "o_icon_proj_appointment";
		case milestone: return "o_icon_proj_milestone";
		default: return null;
		}
	}
	
	public static StringMediaResource createMediaResource(ProjNote note) {
		StringMediaResource resource = new StringMediaResource();
		
		StringBuilder sb = new StringBuilder();
		boolean newLine = false;
		if (StringHelper.containsNonWhitespace(note.getTitle())) {
			sb.append(note.getTitle());
			newLine = true;
		}
		if (StringHelper.containsNonWhitespace(note.getText())) {
			if (newLine) {
				sb.append("\n\n");
			}
			sb.append(note.getText());
		}
		resource.setData(sb.toString());
		
		String downloadFileName = StringHelper.containsNonWhitespace(note.getTitle())
				? note.getTitle()
				: "note_" + note.getKey();
		downloadFileName += "_";
		downloadFileName += Formatter.formatDatetimeFilesystemSave(new Date());
		downloadFileName = FileUtils.normalizeFilename(downloadFileName);
		downloadFileName += ".txt";
		resource.setDownloadable(true, downloadFileName);
		
		resource.setContentType("text/txt");
		resource.setEncoding("UTF-8");
		
		return resource;
	}

}

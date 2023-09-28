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

import static org.olat.modules.project.ui.ProjectUIFactory.templateSuffix;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.translator.Translator;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.modules.project.ProjActivity;
import org.olat.modules.project.ProjAppointment;
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.ProjArtefactItems;
import org.olat.modules.project.ProjDecision;
import org.olat.modules.project.ProjFile;
import org.olat.modules.project.ProjMilestone;
import org.olat.modules.project.ProjNote;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjToDo;
import org.olat.modules.project.manager.ProjectXStream;
import org.olat.modules.todo.ToDoTask;
import org.olat.modules.todo.ui.ToDoUIFactory;
import org.olat.user.UserManager;

/**
 * 
 * Initial date: 5 Jun 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjTimelineActivityRowsFactory {
	
	private final Translator translator;
	private final Formatter formatter;
	private final UserManager userManager;
	private final ProjTimelineUIFactory uifactory;

	public ProjTimelineActivityRowsFactory(Translator translator, Formatter formatter, UserManager userManager) {
		this(translator, formatter, userManager, null);
	}
	
	public ProjTimelineActivityRowsFactory(Translator translator, Formatter formatter, UserManager userManager, ProjTimelineUIFactory uifactory) {
		this.translator = translator;
		this.formatter = formatter;
		this.userManager = userManager;
		this.uifactory = uifactory;
	}
	
	public void addActivityRows(UserRequest ureq, List<ProjTimelineRow> rows, ActivityRowData activityRowData,
			ProjArtefactItems artefactItems, Map<Long, Set<Long>> artefactKeyToIdentityKeys) {
		switch (activityRowData.lastActivity().getActionTarget()) {
		case project: addActivityProjectRows(ureq, rows, activityRowData);
		case file: addActivityFileRows(rows, activityRowData, artefactItems, artefactKeyToIdentityKeys);
		case toDo: addActivityToDoRows(rows, activityRowData, artefactItems, artefactKeyToIdentityKeys);
		case decision: addActivityDecisionRows(rows, activityRowData, artefactItems, artefactKeyToIdentityKeys);
		case note: addActivityNoteRows(rows, activityRowData, artefactItems, artefactKeyToIdentityKeys);
		case appointment: addActivityAppointmentRows(rows, activityRowData, artefactItems, artefactKeyToIdentityKeys);
		case milestone: addActivityMilestoneRows(rows, activityRowData, artefactItems, artefactKeyToIdentityKeys);
		default: //
		}
	}

	private void addActivityProjectRows(UserRequest ureq, List<ProjTimelineRow> rows, ActivityRowData activityRowData) {
		ProjActivity activity = activityRowData.lastActivity();
		ProjProject project = activity.getProject();
		ProjTimelineRow row = new ProjTimelineRow(activity);
		String message = null;
		switch (activity.getAction()) {
		case projectCreate: message = translator.translate(templateSuffix("timeline.activity.project.create", project)); break;
		case projectContentUpdate: message = translator.translate(templateSuffix("timeline.activity.project.content.update", project)); break;
		case projectStatusActive: message = translator.translate(templateSuffix("timeline.activity.project.status.active", project)); break;
		case projectStatusDone: message = translator.translate(templateSuffix("timeline.activity.project.status.done", project)); break;
		case projectStatusDelete: message = translator.translate(templateSuffix("timeline.activity.project.status.deleted", project)); break;
		case projectImageAvatarUpdate: message = translator.translate(templateSuffix("timeline.activity.project.image.update.avatar", project)); break;
		case projectImageBackgroundUpdate: message = translator.translate(templateSuffix("timeline.activity.project.image.update.background", project)); break;
		case projectMemberAdd: {
			Identity member = activity.getMember();
			if (member != null) {
				message = translator.translate(templateSuffix("timeline.activity.project.member.add", project), userManager.getUserDisplayName(member.getKey()));
				if (uifactory != null) {
					uifactory.addAvatarIcon(ureq, row, member);
				}
			}
			break;
		}
		case projectMemberRemove: {
			Identity member = activity.getMember();
			if (member != null) {
				message = translator.translate(templateSuffix("timeline.activity.project.member.remove", project), userManager.getUserDisplayName(member.getKey()));
				if (uifactory != null) {
					uifactory.addAvatarIcon(ureq, row, member);
				}
			}
			break;
		}
		default: //
		}
		
		if (message == null) {
			return;
		}
		row.setMessage(getMessageWithCount(message, activityRowData.numActivities()));
		
		Set<Long> identityKeys = new HashSet<>(2);
		identityKeys.addAll(activityRowData.doerKeys);
		if (activity.getMember() != null) {
			identityKeys.add(activity.getMember().getKey());
		}
		row.setIdentityKeys(identityKeys);
		
		row.setDate(activity.getCreationDate());
		row.setFormattedDate(getFormattedDate(row.getDate(), true));
		row.setToday(DateUtils.isSameDay(new Date(), row.getDate()));
		row.setDoerDisplyName(getDoerDisplayName(activityRowData.doerKeys()));
		row.setIconCssClass("o_icon_proj_project");

		if (uifactory != null) {
			uifactory.addStaticMessageItem(row);
			if (row.getIconItem() == null) {
				uifactory.addActionIconItem(row, activity);
			}
		}
		
		rows.add(row);
	}
	
	private void addActivityFileRows(List<ProjTimelineRow> rows, ActivityRowData activityRowData, ProjArtefactItems artefactItems,
			Map<Long, Set<Long>> artefactKeyToIdentityKeys) {
		ProjActivity activity = activityRowData.lastActivity();
		if (activity.getArtefact() == null) {
			return;
		}
		ProjFile file = artefactItems.getFile(activity.getArtefact());
		if (file == null) {
			return;
		}
		
		Long businesssPathKey = file.getKey();
		String displayName = ProjectUIFactory.getDisplayName(file);
		String iconCSS = CSSHelper.createFiletypeIconCssClassFor(file.getVfsMetadata().getFilename());
		switch (activity.getAction()) {
		case fileCreate: addArtefactRow(rows, activityRowData, businesssPathKey, artefactKeyToIdentityKeys, translator.translate("timeline.activity.file.create", displayName), iconCSS); break;
		case fileUpload: addArtefactRow(rows, activityRowData, businesssPathKey, artefactKeyToIdentityKeys, translator.translate("timeline.activity.file.upload", displayName), iconCSS); break;
		case fileEdit: addArtefactRow(rows, activityRowData, businesssPathKey, artefactKeyToIdentityKeys, translator.translate("timeline.activity.file.edit", displayName), iconCSS); break;
		case fileStatusDelete: addArtefactRow(rows, activityRowData, businesssPathKey, artefactKeyToIdentityKeys, translator.translate("timeline.activity.file.delete", displayName), iconCSS); break;
		case fileContentUpdate: {
			List<ProjActivity> changedActivities = activityRowData.activities().stream()
					.filter(a -> {
						String after = ProjectXStream.fromXml(a.getAfter(), ProjFile.class).getVfsMetadata().getTitle();
						String before = ProjectXStream.fromXml(a.getBefore(), ProjFile.class).getVfsMetadata().getTitle();
						return !Objects.equals(after, before);
					})
					.collect(Collectors.toList());
			if (!changedActivities.isEmpty()) {
				addArtefactRow(rows, createActivityRowData(changedActivities), businesssPathKey, artefactKeyToIdentityKeys, translator.translate("timeline.activity.file.update.title", displayName), iconCSS);
				
			}
			changedActivities = activityRowData.activities().stream()
					.filter(a -> {
						String after = ProjectXStream.fromXml(a.getAfter(), ProjFile.class).getVfsMetadata().getFilename();
						String before = ProjectXStream.fromXml(a.getBefore(), ProjFile.class).getVfsMetadata().getFilename();
						return !Objects.equals(after, before);
					})
					.collect(Collectors.toList());
			if (!changedActivities.isEmpty()) {
				addArtefactRow(rows, createActivityRowData(changedActivities), businesssPathKey, artefactKeyToIdentityKeys, translator.translate("timeline.activity.file.update.filename", displayName), iconCSS);
				
			}
			break;
		}
		default: //
		}
	}
	
	private void addActivityToDoRows(List<ProjTimelineRow> rows, ActivityRowData activityRowData, ProjArtefactItems artefactItems,
			Map<Long, Set<Long>> artefactKeyToIdentityKeys) {
		ProjActivity activity = activityRowData.lastActivity();
		if (activity.getArtefact() == null) {
			return;
		}
		ProjToDo toDo = artefactItems.getToDo(activity.getArtefact());
		if (toDo == null) {
			return;
		}
		
		Long businessPathKey = toDo.getKey();
		String displayName = ToDoUIFactory.getDisplayName(translator, toDo.getToDoTask());
		switch (activity.getAction()) {
		case toDoCreate: addArtefactRow(rows, activityRowData, businessPathKey, artefactKeyToIdentityKeys, translator.translate("timeline.activity.todo.create", displayName)); break;
		case toDoStatusDelete: addArtefactRow(rows, activityRowData, businessPathKey, artefactKeyToIdentityKeys, translator.translate("timeline.activity.todo.delete", displayName)); break;
		case toDoContentUpdate: {
			List<ProjActivity> changedActivities = activityRowData.activities().stream()
					.filter(a -> {
						ToDoTask before = ProjectXStream.fromXml(a.getBefore(), ProjToDo.class).getToDoTask();
						ToDoTask after = ProjectXStream.fromXml(a.getAfter(), ProjToDo.class).getToDoTask();
						Date beforeDueDate = before.getDueDate() != null? new Date(before.getDueDate().getTime()): null;
						Date afterDueDate = after.getDueDate() != null? new Date(after.getDueDate().getTime()): null;
						return !Objects.equals(afterDueDate, beforeDueDate);
					})
					.collect(Collectors.toList());
			if (!changedActivities.isEmpty()) {
				addArtefactRow(rows, createActivityRowData(changedActivities), businessPathKey, artefactKeyToIdentityKeys, translator.translate("timeline.activity.todo.edit.due.date", displayName));
			}
			break;
		}
		default: //
		}
	}
	
	private void addActivityDecisionRows(List<ProjTimelineRow> rows, ActivityRowData activityRowData, ProjArtefactItems artefactItems,
			Map<Long, Set<Long>> artefactKeyToIdentityKeys) {
		ProjActivity activity = activityRowData.lastActivity();
		if (activity.getArtefact() == null) {
			return;
		}
		ProjDecision decision = artefactItems.getDecision(activity.getArtefact());
		if (decision == null) {
			return;
		}
		
		Long businessPathKey = decision.getKey();
		String displayName = ProjectUIFactory.getDisplayName(translator, decision);
		switch (activity.getAction()) {
		case decisionCreate: addArtefactRow(rows, activityRowData, businessPathKey, artefactKeyToIdentityKeys, translator.translate("timeline.activity.decision.create", displayName)); break;
		case decisionContentUpdate: addArtefactRow(rows, activityRowData, businessPathKey, artefactKeyToIdentityKeys, translator.translate("timeline.activity.decision.update.content", displayName)); break;
		case decisionStatusDelete: addArtefactRow(rows, activityRowData, businessPathKey, artefactKeyToIdentityKeys, translator.translate("timeline.activity.decision.delete", displayName)); break;
		default: //
		}
	}
	
	private void addActivityNoteRows(List<ProjTimelineRow> rows, ActivityRowData activityRowData, ProjArtefactItems artefactItems,
			Map<Long, Set<Long>> artefactKeyToIdentityKeys) {
		ProjActivity activity = activityRowData.lastActivity();
		if (activity.getArtefact() == null) {
			return;
		}
		ProjNote note = artefactItems.getNote(activity.getArtefact());
		if (note == null) {
			return;
		}
		
		Long businessPathKey = note.getKey();
		String displayName = ProjectUIFactory.getDisplayName(translator, note);
		switch (activity.getAction()) {
		case noteCreate: addArtefactRow(rows, activityRowData, businessPathKey, artefactKeyToIdentityKeys, translator.translate("timeline.activity.note.create", displayName)); break;
		case noteContentUpdate: addArtefactRow(rows, activityRowData, businessPathKey, artefactKeyToIdentityKeys, translator.translate("timeline.activity.note.update.content", displayName)); break;
		case noteStatusDelete: addArtefactRow(rows, activityRowData, businessPathKey, artefactKeyToIdentityKeys, translator.translate("timeline.activity.note.delete", displayName)); break;
		default: //
		}
	}
	
	private void addActivityAppointmentRows(List<ProjTimelineRow> rows, ActivityRowData activityRowData, ProjArtefactItems artefactItems,
			Map<Long, Set<Long>> artefactKeyToIdentityKeys) {
		ProjActivity activity = activityRowData.lastActivity();
		if (activity.getArtefact() == null) {
			return;
		}
		ProjAppointment appointment = artefactItems.getAppointment(activity.getArtefact());
		if (appointment == null) {
			return;
		}
		
		Long businessPathKey = appointment.getKey();
		String displayName = ProjectUIFactory.getDisplayName(translator, appointment);
		switch (activity.getAction()) {
		case appointmentCreate: addArtefactRow(rows, activityRowData, businessPathKey, artefactKeyToIdentityKeys, translator.translate("timeline.activity.appointment.create", displayName)); break;
		case appointmentStatusDelete: addArtefactRow(rows, activityRowData, businessPathKey, artefactKeyToIdentityKeys, translator.translate("timeline.activity.appointment.delete", displayName)); break;
		default: //
		}
	}
	
	private void addActivityMilestoneRows(List<ProjTimelineRow> rows, ActivityRowData activityRowData, ProjArtefactItems artefactItems,
			Map<Long, Set<Long>> artefactKeyToIdentityKeys) {
		ProjActivity activity = activityRowData.lastActivity();
		if (activity.getArtefact() == null) {
			return;
		}
		ProjMilestone milestone = artefactItems.getMilestone(activity.getArtefact());
		if (milestone == null) {
			return;
		}
		
		Long businessPathKey = milestone.getKey();
		String displayName = ProjectUIFactory.getDisplayName(translator, milestone);
		switch (activity.getAction()) {
		case milestoneCreate: addArtefactRow(rows, activityRowData, businessPathKey, artefactKeyToIdentityKeys, translator.translate("timeline.activity.milestone.create", displayName)); break;
		case milestoneStatusDelete: addArtefactRow(rows, activityRowData, businessPathKey, artefactKeyToIdentityKeys, translator.translate("timeline.activity.milestone.delete", displayName)); break;
		case milestoneContentUpdate: {
			List<ProjActivity> changedActivities = activityRowData.activities().stream()
					.filter(a -> {
						Date after = ProjectXStream.fromXml(a.getAfter(), ProjMilestone.class).getDueDate();
						Date before = ProjectXStream.fromXml(a.getBefore(), ProjMilestone.class).getDueDate();
						return !Objects.equals(after, before);
					})
					.collect(Collectors.toList());
			if (!changedActivities.isEmpty()) {
				addArtefactRow(rows, createActivityRowData(changedActivities), businessPathKey, artefactKeyToIdentityKeys, translator.translate("timeline.activity.milestone.edit.due.date", displayName));
			}
			break;
		}
		default: //
		}
	}
	
	private void addArtefactRow(List<ProjTimelineRow> rows, ActivityRowData activityRowData, Long businessPathKey,
			Map<Long, Set<Long>> artefactKeyToIdentityKeys, String message) {
		addArtefactRow(rows, activityRowData, businessPathKey, artefactKeyToIdentityKeys, message, null);
	}
	
	private void addArtefactRow(List<ProjTimelineRow> rows, ActivityRowData activityRowData, Long businessPathKey,
			Map<Long, Set<Long>> artefactKeyToIdentityKeys, String message, String iconCss) {
		ProjActivity activity = activityRowData.lastActivity();
		ProjTimelineRow row = new ProjTimelineRow(activity);
		row.setBusinessPathKey(businessPathKey);
		
		Set<Long> identityKeys = new HashSet<>(2);
		identityKeys.addAll(activityRowData.doerKeys);
		identityKeys.addAll(artefactKeyToIdentityKeys.getOrDefault(activity.getArtefact().getKey(), Set.of()));
		row.setIdentityKeys(identityKeys);
		
		row.setMessage(getMessageWithCount(message, activityRowData.numActivities()));
		row.setDate(activity.getCreationDate());
		row.setToday(DateUtils.isSameDay(new Date(), row.getDate()));
		row.setFormattedDate(getFormattedDate(row.getDate(), true));
		row.setDoerDisplyName(getDoerDisplayName(activityRowData.doerKeys()));
		String iconCssClass = iconCss;
		if (!StringHelper.containsNonWhitespace(iconCssClass)) {
			iconCssClass = ProjectUIFactory.getActionIconCss(activity.getAction());
		}
		row.setIconCssClass(iconCssClass);
		
		if (uifactory != null) {
			uifactory.addArtefactMesssageItem(row,activity.getArtefact());
			uifactory.addActionIconItem(row, activity);
		}
		
		rows.add(row);
	}
	
	private String getDoerDisplayName(Set<Long> doerKeys) {
		return doerKeys.size() == 1
				? userManager.getUserDisplayName(new ArrayList<>(doerKeys).get(0))
				: translator.translate("timeline.doers.multi");
	}
	
	private String getMessageWithCount(String message, int numActivities) {
		return numActivities == 1
				? message
				: translator.translate("timeline.message.num", message, String.valueOf(numActivities));
	}

	public String getFormattedDate(Date date, boolean todayWithTime) {
		if (DateUtils.isSameDay(new Date(), date)) {
			if (todayWithTime) {
				return translator.translate("today") + "<br>" + formatter.formatTimeShort(date);
			}
			return translator.translate("today");
		}
		return formatter.formatDate(date);
	}
	
	public ActivityRowData createActivityRowData(List<ProjActivity> activities) {
		activities.sort((a1, a2) -> a2.getCreationDate().compareTo(a1.getCreationDate()));
		Set<Long> doers = activities.stream().map(activity -> activity.getDoer().getKey()).collect(Collectors.toSet());
		return new ActivityRowData(activities, activities.get(0), activities.get(activities.size()-1), activities.size(), doers);
	}

	public static record ActivityKey(ProjActivity.Action action, Date date, Long projectKey, Long artefactKey, Long memeberKey) {}
	
	public static ActivityKey keyWithDate(ProjActivity activity) {
		return new ActivityKey(
				activity.getAction(),
				DateUtils.setTime(activity.getCreationDate(), 0, 0, 0),
				activity.getProject().getKey(),
				activity.getArtefact() != null? activity.getArtefact().getKey(): null,
				activity.getMember() != null? activity.getMember().getKey(): null);
	}
	
	public static ActivityKey keyWithoutDate(ProjActivity activity) {
		return new ActivityKey(
				activity.getAction(),
				null,
				activity.getProject().getKey(),
				activity.getArtefact() != null? activity.getArtefact().getKey(): null,
				activity.getMember() != null? activity.getMember().getKey(): null);
	}
	
	public static record ActivityRowData(List<ProjActivity> activities, ProjActivity lastActivity, ProjActivity earliestActivity, int numActivities, Set<Long> doerKeys) {}
	
	public static interface ProjTimelineUIFactory {

		void addAvatarIcon(UserRequest ureq, ProjTimelineRow row, Identity member);

		void addArtefactMesssageItem(ProjTimelineRow row, ProjArtefact artefact);

		void addActionIconItem(ProjTimelineRow row, ProjActivity activity);

		void addStaticMessageItem(ProjTimelineRow row);
		
	}
	
}

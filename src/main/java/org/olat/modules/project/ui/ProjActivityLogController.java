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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.core.commons.controllers.activity.ActivityLogController;
import org.olat.core.commons.controllers.activity.ActivityLogRow;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValuesSupplier;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.DateRange;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.project.ProjActivity;
import org.olat.modules.project.ProjActivitySearchParams;
import org.olat.modules.project.ProjAppointment;
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.ProjArtefactItems;
import org.olat.modules.project.ProjArtefactSearchParams;
import org.olat.modules.project.ProjDecision;
import org.olat.modules.project.ProjFile;
import org.olat.modules.project.ProjMilestone;
import org.olat.modules.project.ProjNote;
import org.olat.modules.project.ProjToDo;
import org.olat.modules.project.ProjectRole;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.manager.ProjectXStream;
import org.olat.modules.todo.ToDoService;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ToDoTask;
import org.olat.modules.todo.ui.ToDoUIFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.mapper.CannotResolveClassException;
import com.thoughtworks.xstream.security.ForbiddenClassException;

/**
 * 
 * Initial date: 19 Apr 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjActivityLogController extends ActivityLogController {

	private static final Logger log = Tracing.createLoggerFor(ProjActivityLogController.class);
	
	private final ProjArtefact artefact;
	private final List<Identity> members;

	@Autowired
	private ProjectService projectService;
	@Autowired
	private CalendarManager calendarManager;
	@Autowired
	private ToDoService toDoService;
	
	public ProjActivityLogController(UserRequest ureq, WindowControl wControl, Form mainForm, ProjArtefact artefact) {
		super(ureq, wControl, mainForm);
		setTranslator(Util.createPackageTranslator(CalendarManager.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(ToDoUIFactory.class, getLocale(), getTranslator()));
		
		this.artefact = artefact;
		this.members = projectService.getMembers(artefact.getProject(), ProjectRole.PROJECT_ROLES);
		
		initForm(ureq);
		loadModel();
	}
	
	@Override
	protected List<Identity> getFilterIdentities() {
		return members;
	}

	@Override
	protected List<ActivityLogRow> loadRows(DateRange dateRange, Set<Long> doerKeys) {
		ProjActivitySearchParams searchParams = new ProjActivitySearchParams();
		searchParams.setProject(artefact.getProject());
		searchParams.setArtefacts(List.of(artefact));
		searchParams.setFetchDoer(true);
		if (dateRange != null) {
			searchParams.setCreatedDateRanges(List.of(dateRange));
		}
		searchParams.setDoerKeys(doerKeys);
		List<ProjActivity> activities = projectService.getActivities(searchParams, 0, -1);
		
		List<ProjArtefact> artefactReferences = activities.stream()
				.map(ProjActivity::getArtefactReference)
				.filter(Objects::nonNull)
				.distinct()
				.toList();
		ProjArtefactSearchParams artefactSearchParams = new ProjArtefactSearchParams();
		artefactSearchParams.setArtefacts(artefactReferences);
		ProjArtefactItems artefactReferenceItems = projectService.getArtefactItems(artefactSearchParams);
		
		List<ActivityLogRow> rows = new ArrayList<>(activities.size());
		for (ProjActivity activity : activities) {
			try {
				addActivityRows(rows, activity, artefactReferenceItems);
			} catch (ConversionException | ForbiddenClassException | CannotResolveClassException e) {
				log.error("Corrupt XML in project activity log", e);
			}
		}
		return rows;
	}
	
	@Override
	protected SelectionValuesSupplier getActivityFilterValues() {
		return switch (artefact.getType()) {
				case ProjFile.TYPE -> getActivityFilterFileValues();
				case ProjToDo.TYPE -> getActivityFilterToDoValues();
				case ProjDecision.TYPE -> getActivityFilterDecisionValues();
				case ProjNote.TYPE -> getActivityFilterNoteValues();
				case ProjAppointment.TYPE -> getActivityFilterAppointmentValues();
				case ProjMilestone.TYPE -> getActivityFilterMilestoneValues();
				default -> new SelectionValues();
			};
	}

	private void addActivityRows(List<ActivityLogRow> rows, ProjActivity activity, ProjArtefactItems artefactReferenceItems) {
		switch (activity.getActionTarget()) {
		case file: addActivityFileRows(rows, activity, artefactReferenceItems);
		case toDo: addActivityToDoRows(rows, activity, artefactReferenceItems);
		case decision: addActivityDecisionRows(rows, activity, artefactReferenceItems);
		case note: addActivityNoteRows(rows, activity, artefactReferenceItems);
		case appointment: addActivityAppointmentRows(rows, activity, artefactReferenceItems);
		case milestone: addActivityMilestoneRows(rows, activity);
		default: //
		}
	}

	private SelectionValues getActivityFilterFileValues() {
		SelectionValues filterSV = new SelectionValues();
		addActivityFilterValue(filterSV, "activity.log.message.copy.init");
		addActivityFilterValue(filterSV, "activity.log.message.create");
		addActivityFilterValue(filterSV, "activity.log.message.upload");
		addActivityFilterValue(filterSV, "activity.log.message.read");
		addActivityFilterValue(filterSV, "activity.log.message.download");
		addActivityFilterValue(filterSV, "activity.log.message.edit.file");
		addActivityFilterValue(filterSV, "activity.log.message.edit.title");
		addActivityFilterValue(filterSV, "activity.log.message.edit.description");
		addActivityFilterValue(filterSV, "activity.log.message.edit.filename");
		addActivityFilterValue(filterSV, "activity.log.message.member.add");
		addActivityFilterValue(filterSV, "activity.log.message.member.remove");
		addActivityFilterValue(filterSV, "activity.log.message.reference.add");
		addActivityFilterValue(filterSV, "activity.log.message.reference.remove");
		addActivityFilterValue(filterSV, "activity.log.message.tag.add");
		addActivityFilterValue(filterSV, "activity.log.message.tag.remove");
		addActivityFilterValue(filterSV, "activity.log.message.delete");
		return filterSV;
	}

	private void addActivityFileRows(List<ActivityLogRow> rows, ProjActivity activity, ProjArtefactItems artefactReferenceItems) {
		switch (activity.getAction()) {
		case fileCopyInitialized: addRow(rows, activity, "activity.log.message.copy.init"); break;
		case fileRead: addRow(rows, activity, "activity.log.message.read"); break;
		case fileDownload: addRow(rows, activity, "activity.log.message.download"); break;
		case fileCreate: addRow(rows, activity, "activity.log.message.create"); break;
		case fileUpload: addRow(rows, activity, "activity.log.message.upload"); break;
		case fileEdit: addRow(rows, activity, "activity.log.message.edit.file"); break;
		case fileRestore: addRow(rows, activity, "activity.log.message.restore"); break;
		case fileStatusDelete: addRow(rows, activity, "activity.log.message.delete"); break;
		case fileMemberAdd: addRow(rows, activity, "activity.log.message.member.add", null, userManager.getUserDisplayName(activity.getMember().getKey())); break;
		case fileMemberRemove: addRow(rows, activity, "activity.log.message.member.remove", userManager.getUserDisplayName(activity.getMember().getKey()), null); break;
		case fileReferenceAdd: addActivityReferenceAddRow(rows, activity, artefactReferenceItems); break;
		case fileReferenceRemove: addActivityReferenceRemoveRow(rows, activity, artefactReferenceItems); break;
		case fileTagsUpdate: addActivityTagsUpdateRows(rows, activity); break;
		case fileContentUpdate: {
			if (StringHelper.containsNonWhitespace(activity.getBefore()) && StringHelper.containsNonWhitespace(activity.getAfter())) {
				ProjFile before = ProjectXStream.fromXml(activity.getBefore(), ProjFile.class);
				ProjFile after = ProjectXStream.fromXml(activity.getAfter(), ProjFile.class);
				VFSMetadata beforeMetadata = before.getVfsMetadata();
				VFSMetadata afterMetadata = after.getVfsMetadata();
				if (!Objects.equals(beforeMetadata.getTitle(), afterMetadata.getTitle())) {
					addRow(rows, activity, "activity.log.message.edit.title", beforeMetadata.getTitle(), afterMetadata.getTitle());
				}
				if (!Objects.equals(beforeMetadata.getComment(), afterMetadata.getComment())) {
					addRow(rows, activity, "activity.log.message.edit.description", beforeMetadata.getComment(), afterMetadata.getComment());
				}
				if (!Objects.equals(beforeMetadata.getFilename(), afterMetadata.getFilename())) {
					addRow(rows, activity, "activity.log.message.edit.filename", beforeMetadata.getFilename(), afterMetadata.getFilename());
				}
			}
			break;
		}
		default: //
		}
	}
	
	private SelectionValues getActivityFilterToDoValues() {
		SelectionValues filterSV = new SelectionValues();
		addActivityFilterValue(filterSV, "activity.log.message.copy.init");
		addActivityFilterValue(filterSV, "activity.log.message.create");
		addActivityFilterValue(filterSV, "activity.log.message.delete");
		addActivityFilterValue(filterSV, "activity.log.message.member.add");
		addActivityFilterValue(filterSV, "activity.log.message.member.remove");
		addActivityFilterValue(filterSV, "activity.log.message.reference.add");
		addActivityFilterValue(filterSV, "activity.log.message.reference.remove");
		addActivityFilterValue(filterSV, "activity.log.message.tag.add");
		addActivityFilterValue(filterSV, "activity.log.message.tag.remove");
		addActivityFilterValue(filterSV, "activity.log.message.todo.task.title");
		addActivityFilterValue(filterSV, "activity.log.message.todo.task.description");
		addActivityFilterValue(filterSV, "activity.log.message.todo.task.status");
		addActivityFilterValue(filterSV, "activity.log.message.todo.task.priority");
		addActivityFilterValue(filterSV, "activity.log.message.todo.task.expenditure.of.work");
		addActivityFilterValue(filterSV, "activity.log.message.todo.task.start.date");
		addActivityFilterValue(filterSV, "activity.log.message.todo.task.due.date");
		return filterSV;
	}
	
	private void addActivityToDoRows(List<ActivityLogRow> rows, ProjActivity activity, ProjArtefactItems artefactReferenceItems) {
		switch (activity.getAction()) {
		case toDoCopyInitialized: addRow(rows, activity, "activity.log.message.copy.init"); break;
		case toDoCreate: addRow(rows, activity, "activity.log.message.create"); break;
		case toDoStatusDelete: addRow(rows, activity, "activity.log.message.delete"); break;
		case toDoMemberAdd: addRow(rows, activity, "activity.log.message.member.add", null, userManager.getUserDisplayName(activity.getMember().getKey())); break;
		case toDoMemberRemove: addRow(rows, activity, "activity.log.message.member.remove", userManager.getUserDisplayName(activity.getMember().getKey()), null); break;
		case toDoReferenceAdd: addActivityReferenceAddRow(rows, activity, artefactReferenceItems); break;
		case toDoReferenceRemove: addActivityReferenceRemoveRow(rows, activity, artefactReferenceItems); break;
		case toDoTagsUpdate: addActivityTagsUpdateRows(rows, activity); break;
		case toDoContentUpdate: {
			if (StringHelper.containsNonWhitespace(activity.getBefore()) && StringHelper.containsNonWhitespace(activity.getAfter())) {
				ToDoTask before = ProjectXStream.fromXml(activity.getBefore(), ProjToDo.class).getToDoTask();
				ToDoTask after = ProjectXStream.fromXml(activity.getAfter(), ProjToDo.class).getToDoTask();
				if (!Objects.equals(before.getTitle(), after.getTitle())) {
					addRow(rows, activity, "activity.log.message.todo.task.title", before.getTitle(), after.getTitle());
				}
				if (!Objects.equals(before.getDescription(), after.getDescription())) {
					addRow(rows, activity, "activity.log.message.todo.task.description", before.getDescription(), after.getDescription());
				}
				if (!Objects.equals(before.getStatus(), after.getStatus()) && after.getStatus() != ToDoStatus.deleted) {
					addRow(rows, activity, "activity.log.message.todo.task.status",
							ToDoUIFactory.getDisplayName(getTranslator(), before.getStatus()),
							ToDoUIFactory.getDisplayName(getTranslator(), after.getStatus()));
				}
				if (!Objects.equals(before.getPriority(), after.getPriority())) {
					addRow(rows, activity, "activity.log.message.todo.task.priority",
							ToDoUIFactory.getDisplayName(getTranslator(), before.getPriority()),
							ToDoUIFactory.getDisplayName(getTranslator(), after.getPriority()));
				}
				if (!Objects.equals(before.getExpenditureOfWork(), after.getExpenditureOfWork())) {
					addRow(rows, activity, "activity.log.message.todo.task.expenditure.of.work",
							ToDoUIFactory.format(toDoService.getExpenditureOfWork(before.getExpenditureOfWork())),
							ToDoUIFactory.format(toDoService.getExpenditureOfWork(after.getExpenditureOfWork())));
				}
				Date beforeStartDate = before.getStartDate() != null? new Date(before.getStartDate().getTime()): null;
				Date afterStartDate = after.getStartDate() != null? new Date(after.getStartDate().getTime()): null;
				if (!Objects.equals(beforeStartDate, afterStartDate)) {
					addRow(rows, activity, "activity.log.message.todo.task.start.date",
							formatter.formatDateAndTime(beforeStartDate),
							formatter.formatDateAndTime(afterStartDate));
				}
				Date beforeDueDate = before.getDueDate() != null? new Date(before.getDueDate().getTime()): null;
				Date afterDueDate = after.getDueDate() != null? new Date(after.getDueDate().getTime()): null;
				if (!Objects.equals(beforeDueDate, afterDueDate)) {
					addRow(rows, activity, "activity.log.message.todo.task.due.date",
							formatter.formatDateAndTime(beforeDueDate),
							formatter.formatDateAndTime(afterDueDate));
				}
			}
			break;
		}
		default: //
		}
	}
	
	private SelectionValues getActivityFilterDecisionValues() {
		SelectionValues filterSV = new SelectionValues();
		addActivityFilterValue(filterSV, "activity.log.message.copy.init");
		addActivityFilterValue(filterSV, "activity.log.message.create");
		addActivityFilterValue(filterSV, "activity.log.message.edit.title");
		addActivityFilterValue(filterSV, "activity.log.message.edit.details");
		addActivityFilterValue(filterSV, "activity.log.message.edit.decision.date");
		addActivityFilterValue(filterSV, "activity.log.message.member.add");
		addActivityFilterValue(filterSV, "activity.log.message.member.remove");
		addActivityFilterValue(filterSV, "activity.log.message.reference.add");
		addActivityFilterValue(filterSV, "activity.log.message.reference.remove");
		addActivityFilterValue(filterSV, "activity.log.message.tag.add");
		addActivityFilterValue(filterSV, "activity.log.message.tag.remove");
		addActivityFilterValue(filterSV, "activity.log.message.delete");
		return filterSV;
	}
	
	private void addActivityDecisionRows(List<ActivityLogRow> rows, ProjActivity activity, ProjArtefactItems artefactReferenceItems) {
		switch (activity.getAction()) {
		case decisionCopyInitialized: addRow(rows, activity, "activity.log.message.copy.init"); break;
		case decisionCreate: addRow(rows, activity, "activity.log.message.create"); break;
		case decisionStatusDelete: addRow(rows, activity, "activity.log.message.delete"); break;
		case decisionMemberAdd: addRow(rows, activity, "activity.log.message.member.add", null, userManager.getUserDisplayName(activity.getMember().getKey())); break;
		case decisionMemberRemove: addRow(rows, activity, "activity.log.message.member.remove", userManager.getUserDisplayName(activity.getMember().getKey()), null); break;
		case decisionReferenceAdd: addActivityReferenceAddRow(rows, activity, artefactReferenceItems); break;
		case decisionReferenceRemove: addActivityReferenceRemoveRow(rows, activity, artefactReferenceItems); break;
		case decisionTagsUpdate: addActivityTagsUpdateRows(rows, activity); break;
		case decisionContentUpdate: {
			if (StringHelper.containsNonWhitespace(activity.getBefore()) && StringHelper.containsNonWhitespace(activity.getAfter())) {
				ProjDecision before = ProjectXStream.fromXml(activity.getBefore(), ProjDecision.class);
				ProjDecision after = ProjectXStream.fromXml(activity.getAfter(), ProjDecision.class);
				if (!Objects.equals(before.getTitle(), after.getTitle())) {
					addRow(rows, activity, "activity.log.message.edit.title", before.getTitle(), after.getTitle());
				}
				if (!Objects.equals(before.getDetails(), after.getDetails())) {
					addRow(rows, activity, "activity.log.message.edit.details", before.getDetails(), after.getDetails());
				}
				Date beforeDecisionDate = before.getDecisionDate() != null? new Date(before.getDecisionDate().getTime()): null;
				Date afterDecisionDate = after.getDecisionDate() != null? new Date(after.getDecisionDate().getTime()): null;
				if (!Objects.equals(beforeDecisionDate, afterDecisionDate)) {
					addRow(rows, activity, "activity.log.message.edit.decision.date",
							formatter.formatDateAndTime(beforeDecisionDate),
							formatter.formatDateAndTime(afterDecisionDate));
				}
			}
			break;
		}
		default: //
		}
	}
	
	private SelectionValues getActivityFilterNoteValues() {
		SelectionValues filterSV = new SelectionValues();
		addActivityFilterValue(filterSV, "activity.log.message.copy.init");
		addActivityFilterValue(filterSV, "activity.log.message.create");
		addActivityFilterValue(filterSV, "activity.log.message.read");
		addActivityFilterValue(filterSV, "activity.log.message.download");
		addActivityFilterValue(filterSV, "activity.log.message.edit.title");
		addActivityFilterValue(filterSV, "activity.log.message.edit.text");
		addActivityFilterValue(filterSV, "activity.log.message.member.add");
		addActivityFilterValue(filterSV, "activity.log.message.member.remove");
		addActivityFilterValue(filterSV, "activity.log.message.reference.add");
		addActivityFilterValue(filterSV, "activity.log.message.reference.remove");
		addActivityFilterValue(filterSV, "activity.log.message.tag.add");
		addActivityFilterValue(filterSV, "activity.log.message.tag.remove");
		addActivityFilterValue(filterSV, "activity.log.message.delete");
		return filterSV;
	}
	
	private void addActivityNoteRows(List<ActivityLogRow> rows, ProjActivity activity, ProjArtefactItems artefactReferenceItems) {
		switch (activity.getAction()) {
		case noteCopyInitialized: addRow(rows, activity, "activity.log.message.copy.init"); break;
		case noteRead: addRow(rows, activity, "activity.log.message.read"); break;
		case noteDownload: addRow(rows, activity, "activity.log.message.download"); break;
		case noteCreate: addRow(rows, activity, "activity.log.message.create"); break;
		case noteStatusDelete: addRow(rows, activity, "activity.log.message.delete"); break;
		case noteMemberAdd: addRow(rows, activity, "activity.log.message.member.add", null, userManager.getUserDisplayName(activity.getMember().getKey())); break;
		case noteMemberRemove: addRow(rows, activity, "activity.log.message.member.remove", userManager.getUserDisplayName(activity.getMember().getKey()), null); break;
		case noteReferenceAdd: addActivityReferenceAddRow(rows, activity, artefactReferenceItems); break;
		case noteReferenceRemove: addActivityReferenceRemoveRow(rows, activity, artefactReferenceItems); break;
		case noteTagsUpdate: addActivityTagsUpdateRows(rows, activity); break;
		case noteContentUpdate: {
			if (StringHelper.containsNonWhitespace(activity.getBefore()) && StringHelper.containsNonWhitespace(activity.getAfter())) {
				ProjNote before = ProjectXStream.fromXml(activity.getBefore(), ProjNote.class);
				ProjNote after = ProjectXStream.fromXml(activity.getAfter(), ProjNote.class);
				if (!Objects.equals(before.getTitle(), after.getTitle())) {
					addRow(rows, activity, "activity.log.message.edit.title", before.getTitle(), after.getTitle());
				}
				if (!Objects.equals(before.getText(), after.getText())) {
					addRow(rows, activity, "activity.log.message.edit.text", before.getText(), after.getText());
				}
			}
			break;
		}
		default: //
		}
	}

	private SelectionValues getActivityFilterAppointmentValues() {
		SelectionValues filterSV = new SelectionValues();
		addActivityFilterValue(filterSV, "activity.log.message.create");
		addActivityFilterValue(filterSV, "activity.log.message.edit.start.date");
		addActivityFilterValue(filterSV, "activity.log.message.edit.end.date");
		addActivityFilterValue(filterSV, "activity.log.message.edit.subject.appointment");
		addActivityFilterValue(filterSV, "activity.log.message.edit.description");
		addActivityFilterValue(filterSV, "activity.log.message.edit.location");
		addActivityFilterValue(filterSV, "activity.log.message.edit.color");
		addActivityFilterValue(filterSV, "activity.log.message.edit.all.day");
		addActivityFilterValue(filterSV, "activity.log.message.edit.recurrence.rule");
		addActivityFilterValue(filterSV, "activity.log.message.edit.recurrence.end");
		addActivityFilterValue(filterSV, "activity.log.message.member.add");
		addActivityFilterValue(filterSV, "activity.log.message.member.remove");
		addActivityFilterValue(filterSV, "activity.log.message.reference.add");
		addActivityFilterValue(filterSV, "activity.log.message.reference.remove");
		addActivityFilterValue(filterSV, "activity.log.message.tag.add");
		addActivityFilterValue(filterSV, "activity.log.message.tag.remove");
		addActivityFilterValue(filterSV, "activity.log.message.delete.occurrence");
		addActivityFilterValue(filterSV, "activity.log.message.delete");
		return filterSV;
	}
	
	private void addActivityAppointmentRows(List<ActivityLogRow> rows, ProjActivity activity, ProjArtefactItems artefactReferenceItems) {
		switch (activity.getAction()) {
		case appointmentCreate: addRow(rows, activity, "activity.log.message.create"); break;
		case appointmentOccurrenceDelete:
			addRow(rows, activity, "activity.log.message.delete.occurrence",
					formatter.formatDateAndTime(ProjectXStream.fromXml(activity.getBefore(), Date.class)), null);
			break;
		case appointmentStatusDelete: addRow(rows, activity, "activity.log.message.delete"); break;
		case appointmentMemberAdd: addRow(rows, activity, "activity.log.message.member.add", null, userManager.getUserDisplayName(activity.getMember().getKey())); break;
		case appointmentMemberRemove: addRow(rows, activity, "activity.log.message.member.remove", userManager.getUserDisplayName(activity.getMember().getKey()), null); break;
		case appointmentReferenceAdd: addActivityReferenceAddRow(rows, activity, artefactReferenceItems); break;
		case appointmentReferenceRemove: addActivityReferenceRemoveRow(rows, activity, artefactReferenceItems); break;
		case appointmentTagsUpdate: addActivityTagsUpdateRows(rows, activity); break;
		case appointmentContentUpdate: {
			if (StringHelper.containsNonWhitespace(activity.getBefore()) && StringHelper.containsNonWhitespace(activity.getAfter())) {
				ProjAppointment before = ProjectXStream.fromXml(activity.getBefore(), ProjAppointment.class);
				ProjAppointment after = ProjectXStream.fromXml(activity.getAfter(), ProjAppointment.class);
				Date beforeStartDate = before.getStartDate() != null? new Date(before.getStartDate().getTime()): null;
				Date afterStartDate = after.getStartDate() != null? new Date(after.getStartDate().getTime()): null;
				if (!Objects.equals(beforeStartDate, afterStartDate)) {
					addRow(rows, activity, "activity.log.message.edit.start.date",
							formatter.formatDateAndTime(beforeStartDate),
							formatter.formatDateAndTime(afterStartDate));
				}
				Date beforeEndDate = before.getEndDate() != null? new Date(before.getEndDate().getTime()): null;
				Date afterEndDate = after.getEndDate() != null? new Date(after.getEndDate().getTime()): null;
				if (!Objects.equals(beforeEndDate, after.getEndDate())) {
					addRow(rows, activity, "activity.log.message.edit.end.date",
							formatter.formatDateAndTime(beforeEndDate),
							formatter.formatDateAndTime(afterEndDate));
				}
				if (!Objects.equals(before.getSubject(), after.getSubject())) {
					addRow(rows, activity, "activity.log.message.edit.subject.appointment", before.getSubject(), after.getSubject());
				}
				if (!Objects.equals(before.getDescription(), after.getDescription())) {
					addRow(rows, activity, "activity.log.message.edit.description", before.getDescription(), after.getDescription());
				}
				if (!Objects.equals(before.getLocation(), after.getLocation())) {
					addRow(rows, activity, "activity.log.message.edit.location", before.getLocation(), after.getLocation());
				}
				if (!Objects.equals(before.getColor(), after.getColor())) {
					addRow(rows, activity, "activity.log.message.edit.color", before.getColor(), after.getColor());
				}
				if (before.isAllDay() != after.isAllDay()) {
					addRow(rows, activity, "activity.log.message.edit.all.day",
							Boolean.valueOf(before.isAllDay()).toString(),
							Boolean.valueOf(after.isAllDay()).toString());
				}
				
				String beforeRecurrence = CalendarUtils.getRecurrence(before.getRecurrenceRule());
				String afterRecurrence = CalendarUtils.getRecurrence(after.getRecurrenceRule());
				if (!Objects.equals(beforeRecurrence, afterRecurrence)) {
					addRow(rows, activity, "activity.log.message.edit.recurrence.rule",
							getTranslatedRecurrenceRule(beforeRecurrence),
							getTranslatedRecurrenceRule(afterRecurrence));
				}
				
				Date beforeRecurrenceEnd = calendarManager.getRecurrenceEndDate(before.getRecurrenceRule());
				Date afterRecurrenceEnd = calendarManager.getRecurrenceEndDate(after.getRecurrenceRule());
				if (!Objects.equals(beforeRecurrenceEnd, afterRecurrenceEnd)) {
					addRow(rows, activity, "activity.log.message.edit.recurrence.end",
							formatter.formatDate(beforeRecurrenceEnd),
							formatter.formatDate(afterRecurrenceEnd));
				}
			}
			break;
		}
		default: //
		}
	}

	private SelectionValues getActivityFilterMilestoneValues() {
		SelectionValues filterSV = new SelectionValues();
		addActivityFilterValue(filterSV, "activity.log.message.create");
		addActivityFilterValue(filterSV, "activity.log.message.delete");
		addActivityFilterValue(filterSV, "activity.log.message.edit.due.date");
		addActivityFilterValue(filterSV, "activity.log.message.edit.subject.milestone");
		addActivityFilterValue(filterSV, "activity.log.message.edit.description");
		addActivityFilterValue(filterSV, "activity.log.message.edit.status");
		addActivityFilterValue(filterSV, "activity.log.message.edit.color");
		addActivityFilterValue(filterSV, "activity.log.message.tag.add");
		addActivityFilterValue(filterSV, "activity.log.message.tag.remove");
		return filterSV;
	}
	
	private void addActivityMilestoneRows(List<ActivityLogRow> rows, ProjActivity activity) {
		switch (activity.getAction()) {
		case milestoneCreate: addRow(rows, activity, "activity.log.message.create"); break;
		case milestoneStatusDelete: addRow(rows, activity, "activity.log.message.delete"); break;
		case milestoneTagsUpdate: addActivityTagsUpdateRows(rows, activity); break;
		case milestoneContentUpdate: {
			if (StringHelper.containsNonWhitespace(activity.getBefore()) && StringHelper.containsNonWhitespace(activity.getAfter())) {
				ProjMilestone before = ProjectXStream.fromXml(activity.getBefore(), ProjMilestone.class);
				ProjMilestone after = ProjectXStream.fromXml(activity.getAfter(), ProjMilestone.class);
				Date beforeDueDate = before.getDueDate() != null? new Date(before.getDueDate().getTime()): null;
				Date afterDueDate = after.getDueDate() != null? new Date(after.getDueDate().getTime()): null;
				if (!Objects.equals(beforeDueDate, after.getDueDate())) {
					addRow(rows, activity, "activity.log.message.edit.due.date",
							formatter.formatDateAndTime(beforeDueDate),
							formatter.formatDateAndTime(afterDueDate));
				}
				if (!Objects.equals(before.getSubject(), after.getSubject())) {
					addRow(rows, activity, "activity.log.message.edit.subject.milestone", before.getSubject(), after.getSubject());
				}
				if (!Objects.equals(before.getDescription(), after.getDescription())) {
					addRow(rows, activity, "activity.log.message.edit.description", before.getDescription(), after.getDescription());
				}
				if(before.getStatus() != after.getStatus()) {
					addRow(rows, activity, "activity.log.message.status",
							ProjectUIFactory.getDisplayName(getTranslator(), before.getStatus()),
							ProjectUIFactory.getDisplayName(getTranslator(), after.getStatus()));
				}
				if (!Objects.equals(before.getColor(), after.getColor())) {
					addRow(rows, activity, "activity.log.message.edit.color", before.getColor(), after.getColor());
				}
			}
			break;
		}
		default: //
		}
	}
	
	private void addActivityReferenceAddRow(List<ActivityLogRow> rows, ProjActivity activity,
			ProjArtefactItems artefactReferenceItems) {
		String value = getArtefactValue(activity.getArtefactReference(), artefactReferenceItems);
		if (StringHelper.containsNonWhitespace(value)) {
			addRow(rows, activity, "activity.log.message.reference.add", null, value);
		}
	}

	private void addActivityReferenceRemoveRow(List<ActivityLogRow> rows, ProjActivity activity,
			ProjArtefactItems artefactReferenceItems) {
		String value = getArtefactValue(activity.getArtefactReference(), artefactReferenceItems);
		if (StringHelper.containsNonWhitespace(value)) {
			addRow(rows, activity, "activity.log.message.reference.remove", value, null); 
		}
	}
	
	private void addActivityTagsUpdateRows(List<ActivityLogRow> rows, ProjActivity activity) {
		List<String> tagsBefore = ProjectXStream.tagsFromXml(activity.getBefore());
		List<String> tagsAfter = ProjectXStream.tagsFromXml(activity.getAfter());
		for (String tagAfter : tagsAfter) {
			if (!tagsBefore.contains(tagAfter)) {
				addRow(rows, activity, "activity.log.message.tag.add", null, tagAfter); 
			}
		}
		for (String tagBefore : tagsBefore) {
			if (!tagsAfter.contains(tagBefore)) {
				addRow(rows, activity, "activity.log.message.tag.remove", tagBefore, null); 
			}
		}
	}

	private void addRow(List<ActivityLogRow> rows, ProjActivity activity, String messageI18n) {
		addRow(rows, activity, messageI18n, null, null);
	}
	
	private void addRow(List<ActivityLogRow> rows, ProjActivity activity, String messageI18nKey, String originalValue, String newValue) {
		ActivityLogRow row = createRow(activity.getDoer());
		row.setDate(activity.getCreationDate());
		row.setMessageI18nKey(messageI18nKey);
		row.setMessage(translate(messageI18nKey));
		row.setOriginalValue(originalValue);
		row.setNewValue(newValue);
		rows.add(row);
	}
	
	private String getArtefactValue(ProjArtefact artefact, ProjArtefactItems artefactItems) {
		if (ProjFile.TYPE.equals(artefact.getType())) {
			ProjFile file = artefactItems.getFile(artefact);
			if (file != null) {
				return translate("activity.log.file", ProjectUIFactory.getDisplayName(file));
			}
		} else if (ProjNote.TYPE.equals(artefact.getType())) {
			ProjNote note = artefactItems.getNote(artefact);
			if (note != null) {
				return translate("activity.log.note", ProjectUIFactory.getDisplayName(getTranslator(), note));
			}
		} else if (ProjFile.TYPE.equals(artefact.getType())) {
			ProjAppointment appointment = artefactItems.getAppointment(artefact);
			if (appointment != null) {
				return translate("activity.log.appointment", ProjectUIFactory.getDisplayName(getTranslator(), appointment));
			}
		}
		return null;
	}
	
	private String getTranslatedRecurrenceRule(String beforeRecurrence) {
		if (KalendarEvent.DAILY.equals(beforeRecurrence)) {
			return translate("cal.form.recurrence.daily");
		} else if (KalendarEvent.WORKDAILY.equals(beforeRecurrence)) {
			return translate("cal.form.recurrence.workdaily");
		} else if (KalendarEvent.WEEKLY.equals(beforeRecurrence)) {
			return translate("cal.form.recurrence.weekly");
		} else if (KalendarEvent.BIWEEKLY.equals(beforeRecurrence)) {
			return translate("cal.form.recurrence.biweekly");
		} else if (KalendarEvent.MONTHLY.equals(beforeRecurrence)) {
			return translate("cal.form.recurrence.monthly");
		} else if (KalendarEvent.YEARLY.equals(beforeRecurrence)) {
			return translate("cal.form.recurrence.yearly");
		}
		return null;
	}

}

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
package org.olat.modules.project.manager;

import java.io.OutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.GroupMembership;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.model.Kalendar;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.DateRange;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.openxml.OpenXMLDocument;
import org.olat.core.util.openxml.OpenXMLDocument.Style;
import org.olat.core.util.openxml.OpenXMLDocumentWriter;
import org.olat.modules.project.ProjAppointment;
import org.olat.modules.project.ProjAppointmentInfo;
import org.olat.modules.project.ProjAppointmentSearchParams;
import org.olat.modules.project.ProjArtefactInfoParams;
import org.olat.modules.project.ProjDecision;
import org.olat.modules.project.ProjDecisionInfo;
import org.olat.modules.project.ProjDecisionSearchParams;
import org.olat.modules.project.ProjFile;
import org.olat.modules.project.ProjFileInfo;
import org.olat.modules.project.ProjFileSearchParams;
import org.olat.modules.project.ProjMemberInfoSearchParameters;
import org.olat.modules.project.ProjMilestone;
import org.olat.modules.project.ProjMilestoneInfo;
import org.olat.modules.project.ProjMilestoneSearchParams;
import org.olat.modules.project.ProjNote;
import org.olat.modules.project.ProjNoteInfo;
import org.olat.modules.project.ProjNoteSearchParams;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjProjectSecurityCallback;
import org.olat.modules.project.ProjToDo;
import org.olat.modules.project.ProjToDoInfo;
import org.olat.modules.project.ProjToDoSearchParams;
import org.olat.modules.project.ProjWordReportGrouping;
import org.olat.modules.project.ProjectRole;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ProjectStatus;
import org.olat.modules.project.ui.ProjectUIFactory;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ui.ToDoUIFactory;
import org.olat.user.UserManager;

/**
 * 
 * Initial date: 25 Sep 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjReportWordExport {
	
	private static final Logger log = Tracing.createLoggerFor(ProjReportWordExport.class);
	
	private final ProjectService projectService;
	private final ProjMemberQueries memberQueries;
	private final UserManager userManager;
	private final ProjProject project;
	private final ProjProjectSecurityCallback secCallback;
	private final Collection<String> artefactTypes;
	private final ProjWordReportGrouping grouping;
	private final DateRange dateRange;
	private final Translator translator;
	private final Formatter formatter;
	private List<ProjNoteInfo> notes;
	private List<ProjFileInfo> files;
	private Map<Long, String> noteKeyToFilename;
	private Map<Long, String> fileKeyToFilename;

	public ProjReportWordExport(ProjectService projectService, ProjMemberQueries memberQueries, ProjProject project,
			ProjProjectSecurityCallback secCallback, Collection<String> artefactTypes, ProjWordReportGrouping grouping,
			DateRange dateRange, Locale locale) {
		this.projectService = projectService;
		this.memberQueries = memberQueries;
		this.userManager = CoreSpringFactory.getImpl(UserManager.class);
		this.project = project;
		this.secCallback = secCallback;
		this.artefactTypes = artefactTypes;
		this.grouping = grouping;
		this.dateRange = dateRange;
		this.translator = Util.createPackageTranslator(ProjectUIFactory.class, locale);
		this.formatter = Formatter.getInstance(locale);
	}
	
	public String getFilename() {
		return StringHelper.transformDisplayNameToFileSystemName(project.getTitle()) + ".docx";
	}
	
	public List<ProjNote> getNotes() {
		if (notes == null) {
			if (artefactTypes.contains(ProjNote.TYPE) && secCallback.canViewNotes()) {
				ProjNoteSearchParams searchParams = new ProjNoteSearchParams();
				searchParams.setProject(project);
				searchParams.setStatus(List.of(ProjectStatus.active));
				notes = projectService.getNoteInfos(searchParams, ProjArtefactInfoParams.MEMBERS).stream()
						.filter(info -> filterDateRange(info.getNote().getArtefact().getContentModifiedDate()))
						.collect(Collectors.toList());
			} else {
				notes = List.of();
			}
		}
		
		return notes.stream().map(ProjNoteInfo::getNote).collect(Collectors.toList());
	}
	
	public List<ProjFile> getFiles() {
		if (files == null) {
			if (artefactTypes.contains(ProjFile.TYPE) && secCallback.canViewFiles()) {
				ProjFileSearchParams searchParams = new ProjFileSearchParams();
				searchParams.setProject(project);
				searchParams.setStatus(List.of(ProjectStatus.active));
				files = projectService.getFileInfos(searchParams, ProjArtefactInfoParams.MEMBERS).stream()
						.filter(info -> filterDateRange(info.getFile().getVfsMetadata().getFileLastModified()))
						.collect(Collectors.toList());
			} else {
				files = List.of();
			}
		}
		
		return files.stream().map(ProjFileInfo::getFile).collect(Collectors.toList());
	}

	public void putNoteFilename(ProjNote note, String noteFilename) {
		if (noteKeyToFilename == null) {
			noteKeyToFilename = new HashMap<>();
		}
		noteKeyToFilename.put(note.getKey(), noteFilename);
	}

	public void putFileFilename(ProjFile file, String fileFilename) {
		if (fileKeyToFilename == null) {
			fileKeyToFilename = new HashMap<>();
		}
		fileKeyToFilename.put(file.getKey(), fileFilename);
	}

	public void export(OutputStream out) {
		try(ZipOutputStream zout = new ZipOutputStream(out)) {
			zout.setLevel(9);
			
			OpenXMLDocument document = new OpenXMLDocument();
			String formatedDay = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(translator.getLocale()).format(LocalDate.now());
			document.setDocumentHeader(formatedDay);
			
			exportProject(document);
			exportMembers(document);
			switch (grouping) {
			case chronological -> exportArtefactsChronological(document);
			case type -> exportArtefactsByType(document);
			}
			
			OpenXMLDocumentWriter writer = new OpenXMLDocumentWriter();
			writer.createDocument(zout, document);
		} catch (Exception e) {
			log.error("", e);
		}
		
	}

	private void exportDateRange(OpenXMLDocument document) {
		String dateRangeText = null;
		if (dateRange.getFrom() != null) {
			if (dateRange.getTo() != null) {
				dateRangeText = translator.translate("report.date.range.from.to", formatter.formatDate(dateRange.getFrom()), formatter.formatDate(dateRange.getTo()));
			} else {
				dateRangeText = translator.translate("report.date.range.from", formatter.formatDate(dateRange.getFrom()));
			}
		} else if (dateRange.getTo() != null) {
			dateRangeText = translator.translate("report.date.range.to", formatter.formatDate(dateRange.getTo()));
		}
		if (dateRangeText != null) {
			document.appendText(dateRangeText, true, Style.bold);
			document.appendBreak(false);
		}
	}

	private void exportProject(OpenXMLDocument document) {
		document.appendTitle(translator.translate("report.project.title", project.getTitle()));
		
		exportDateRange(document);
		
		if (StringHelper.containsNonWhitespace(project.getTeaser())) {
			document.appendSubtitle(translator.translate("report.project.teaser.title"));
			document.appendText(project.getTeaser(), true);
			document.appendBreak(false);
		}
		
		if (StringHelper.containsNonWhitespace(project.getDescription())) {
			document.appendSubtitle(translator.translate("report.project.description.title"));
			document.appendText(project.getDescription(), true);
			document.appendBreak(false);
		}
	}

	private void exportMembers(OpenXMLDocument document) {
		if (!secCallback.canViewMembers()) return;
		
		ProjMemberInfoSearchParameters params = new ProjMemberInfoSearchParameters();
		params.setProject(project);
		Map<String, List<String>> roleToUsersDisplayName = memberQueries.getProjMemberships(params).stream()
				.collect(Collectors.groupingBy(
						GroupMembership::getRole, 
						Collectors.collectingAndThen(
								Collectors.toList(),
								memberships -> memberships.stream()
										.map(GroupMembership::getIdentity)
										.map(Identity::getKey)
										.map(userManager::getUserDisplayName)
										.sorted()
										.collect(Collectors.toList()))));
		
		document.appendHeading1(translator.translate("report.members.title"), null);
		ProjectRole.PROJECT_ROLES.forEach(role -> exportMembers(document, roleToUsersDisplayName, role));
	}
	
	private void exportMembers(OpenXMLDocument document, Map<String, List<String>> roleToUsersDisplayName, ProjectRole role) {
		List<String> userDisplayNames = roleToUsersDisplayName.get(role.name());
		if (userDisplayNames != null && !userDisplayNames.isEmpty()) {
			document.appendSubtitle(ProjectUIFactory.translateRole(translator, role));
			userDisplayNames.forEach(userDisplayName -> document.appendText(userDisplayName, true));
			document.appendBreak(false);
		}
	}

	private void exportArtefactsChronological(OpenXMLDocument document) {
		//
	}

	private void exportArtefactsByType(OpenXMLDocument document) {
		if (artefactTypes.contains(ProjAppointment.TYPE) && secCallback.canViewAppointments()) {
			exportArtefactsByTypeAppointment(document);
		}
		if (artefactTypes.contains(ProjMilestone.TYPE) && secCallback.canViewMilestones()) {
			exportArtefactsByTypeMilestones(document);
		}
		if (artefactTypes.contains(ProjToDo.TYPE) && secCallback.canViewToDos()) {
			exportArtefactsByTypeToDos(document);
		}
		if (artefactTypes.contains(ProjDecision.TYPE) && secCallback.canViewDecisions()) {
			exportArtefactsByTypeDecisions(document);
		}
		if (artefactTypes.contains(ProjNote.TYPE) && secCallback.canViewNotes()) {
			exportNotes(document);
		}
		if (artefactTypes.contains(ProjFile.TYPE) && secCallback.canViewFiles()) {
			exportFiles(document);
		}
	}

	private void exportArtefactsByTypeAppointment(OpenXMLDocument document) {
		ProjAppointmentSearchParams searchParams = new ProjAppointmentSearchParams();
		searchParams.setProject(project);
		searchParams.setStatus(List.of(ProjectStatus.active));
		searchParams.setDatesNull(Boolean.FALSE);
		List<ProjAppointmentInfo> appointmentInfos = projectService.getAppointmentInfos(searchParams, ProjArtefactInfoParams.MEMBERS);
		if (appointmentInfos.isEmpty()) {
			return;
		}
		
		CalendarManager calendarManager = CoreSpringFactory.getImpl(CalendarManager.class);
		Map<String, ProjAppointmentInfo> appointmentIdentToAppointment = appointmentInfos.stream()
				.collect(Collectors.toMap(info -> info.getAppointment().getIdentifier(), Function.identity()));
		List<ProjAppointment> appointments = appointmentIdentToAppointment.values().stream()
				.map(ProjAppointmentInfo::getAppointment)
				.collect(Collectors.toList());
		Kalendar kalendar = projectService.getAppointmentsKalendar(appointments);
		Date from = dateRange.getFrom() != null? dateRange.getFrom(): project.getCreationDate();
		Date to = dateRange.getTo() != null? dateRange.getTo(): DateUtils.addYears(new Date(), 10);
		List<KalendarEvent> appointmentEvents = calendarManager.getEvents(kalendar, from, to, true);
		if (appointmentEvents.isEmpty()) {
			return;
		}
		
		document.appendHeading1(translator.translate("report.appointments.title"), null);
		
		appointmentEvents.sort((e1, e2) -> e2.getBegin().compareTo(e1.getBegin()));
		if (appointmentEvents.get(0).getBegin().after(new Date())) {
			document.appendHeading2(translator.translate("report.appointments.future"), null);
		}
		
		boolean inPast = false;
		for (KalendarEvent event : appointmentEvents) {
			if (!inPast && event.getBegin().before(new Date())) {
				document.appendHeading2(translator.translate("report.appointments.past"), null);
				inPast = true;
			}
			exportAppointment(document, event, appointmentIdentToAppointment.get(event.getExternalId()));
		}
	}

	private void exportAppointment(OpenXMLDocument document, KalendarEvent event, ProjAppointmentInfo info) {
		String dateTitle = event.isAllDayEvent()
				? formatter.formatDate(event.getBegin())
				: formatter.formatDateAndTime(event.getBegin()) + " - " + formatter.formatDateAndTime(event.getEnd());
		dateTitle += ": " + ProjectUIFactory.getDisplayName(translator, event);
		document.appendText(dateTitle, true, Style.bold);
		
		exportArtefactMembers(document, info.getMembers());
		
		if (StringHelper.containsNonWhitespace(event.getDescription())) {
			document.appendText(event.getDescription(), true);
		}
		document.appendBreak(false);
	}

	private void exportArtefactsByTypeMilestones(OpenXMLDocument document) {
		ProjMilestoneSearchParams searchParams = new ProjMilestoneSearchParams();
		searchParams.setProject(project);
		searchParams.setStatus(List.of(ProjectStatus.active));
		List<ProjMilestoneInfo> milestoneInfos = projectService.getMilestoneInfos(searchParams, ProjArtefactInfoParams.MEMBERS).stream()
				.filter(info -> info.getMilestone().getDueDate() != null)
				.filter(info -> filterDateRange(info.getMilestone().getDueDate()))
				.sorted((i1, i2) -> i2.getMilestone().getDueDate().compareTo(i1.getMilestone().getDueDate()))
				.collect(Collectors.toList());
		if (milestoneInfos.isEmpty()) {
			return;
		}
		
		document.appendHeading1(translator.translate("report.milestones.title"), null);
		
		if (milestoneInfos.get(0).getMilestone().getDueDate().after(new Date())) {
			document.appendHeading2(translator.translate("report.milestones.future"), null);
		}
		
		boolean inPast = false;
		for (ProjMilestoneInfo info : milestoneInfos) {
			if (!inPast && info.getMilestone().getDueDate().before(new Date())) {
				document.appendHeading2(translator.translate("report.milestones.past"), null);
				inPast = true;
			}
			exportMilestone(document, info);
		}
	}

	private void exportMilestone(OpenXMLDocument document, ProjMilestoneInfo info) {
		String dateTitle = formatter.formatDate(info.getMilestone().getDueDate());
		dateTitle += ": " + ProjectUIFactory.getDisplayName(translator, info.getMilestone());
		document.appendText(dateTitle, true, Style.bold);
		
		exportArtefactMembers(document, info.getMembers());
		
		if (StringHelper.containsNonWhitespace(info.getMilestone().getDescription())) {
			document.appendText(info.getMilestone().getDescription(), true);
		}
		document.appendBreak(false);
	}

	private void exportArtefactsByTypeToDos(OpenXMLDocument document) {
		ProjToDoSearchParams searchParams = new ProjToDoSearchParams();
		searchParams.setProject(project);
		searchParams.setStatus(List.of(ProjectStatus.active));
		List<ProjToDoInfo> toDoInfos = projectService.getToDoInfos(searchParams, ProjArtefactInfoParams.MEMBERS);
		if (dateRange.getFrom() != null || dateRange.getTo() != null) {
			toDoInfos = toDoInfos.stream()
					.filter(info -> info.getToDo().getToDoTask().getDueDate() != null)
					.filter(info -> filterDateRange(info.getToDo().getToDoTask().getDueDate()))
					.collect(Collectors.toList());
		}
		if (toDoInfos.isEmpty()) {
			return;
		}
		
		List<ProjToDoInfo> toDoDoneInfos = new ArrayList<>();
		List<ProjToDoInfo> toDoOpenInfos = new ArrayList<>();
		for (ProjToDoInfo info: toDoInfos) {
			if (ToDoStatus.done == info.getToDo().getToDoTask().getStatus()) {
				toDoDoneInfos.add(info);
			} else if (ToDoStatus.open == info.getToDo().getToDoTask().getStatus() || ToDoStatus.inProgress == info.getToDo().getToDoTask().getStatus()) {
				toDoOpenInfos.add(info);
			}
		}
		
		document.appendHeading1(translator.translate("report.todos.title"), null);
		
		if (!toDoOpenInfos.isEmpty()) {
			document.appendHeading2(translator.translate("report.todos.open"), null);
			exportArtefactsByTypeToDos(document, toDoOpenInfos);
		}
		if (!toDoDoneInfos.isEmpty()) {
			document.appendHeading2(translator.translate("report.todos.done"), null);
			exportArtefactsByTypeToDos(document, toDoDoneInfos);
		}
	}

	private void exportArtefactsByTypeToDos(OpenXMLDocument document, List<ProjToDoInfo> infos) {
		List<ProjToDoInfo> toDoWithDateInfos = new ArrayList<>();
		List<ProjToDoInfo> toDoWithoutDateInfos = new ArrayList<>();
		
		for (ProjToDoInfo info: infos) {
			if (info.getToDo().getToDoTask().getDueDate() != null) {
				toDoWithDateInfos.add(info);
			} else if (StringHelper.containsNonWhitespace(info.getToDo().getToDoTask().getTitle())) {
				toDoWithoutDateInfos.add(info);
			}
		}
		
		toDoWithDateInfos.stream()
				.sorted((i1, i2) -> i2.getToDo().getToDoTask().getDueDate().compareTo(i1.getToDo().getToDoTask().getDueDate()))
				.forEach(info -> exportToDo(document, info));
		toDoWithoutDateInfos.stream()
				.sorted((i1, i2) -> i1.getToDo().getToDoTask().getTitle().compareTo(i2.getToDo().getToDoTask().getTitle()))
				.forEach(info -> exportToDo(document, info));
	}

	private void exportToDo(OpenXMLDocument document, ProjToDoInfo info) {
		String dateTitle = info.getToDo().getToDoTask().getDueDate() != null
				? formatter.formatDate(info.getToDo().getToDoTask().getDueDate()) + ": "
				: "";
		dateTitle += ToDoUIFactory.getDisplayName(translator, info.getToDo().getToDoTask());
		document.appendText(dateTitle, true, Style.bold);
		
		exportArtefactMembers(document, info.getMembers());
		
		if (StringHelper.containsNonWhitespace(info.getToDo().getToDoTask().getDescription())) {
			document.appendText(info.getToDo().getToDoTask().getDescription(), true);
		}
		document.appendBreak(false);
	}

	private void exportArtefactsByTypeDecisions(OpenXMLDocument document) {
		ProjDecisionSearchParams searchParams = new ProjDecisionSearchParams();
		searchParams.setProject(project);
		searchParams.setStatus(List.of(ProjectStatus.active));
		List<ProjDecisionInfo> decisionInfos = projectService.getDecisionInfos(searchParams, ProjArtefactInfoParams.MEMBERS).stream()
				.filter(info -> info.getDecision().getDecisionDate() != null)
				.filter(info -> filterDateRange(info.getDecision().getDecisionDate()))
				.sorted((i1, i2) -> i2.getDecision().getDecisionDate().compareTo(i1.getDecision().getDecisionDate()))
				.collect(Collectors.toList());
		if (decisionInfos.isEmpty()) {
			return;
		}
		
		document.appendHeading1(translator.translate("report.decisions.title"), null);
		
		if (decisionInfos.get(0).getDecision().getDecisionDate().after(new Date())) {
			document.appendHeading2(translator.translate("report.decisions.future"), null);
		}
		
		boolean inPast = false;
		for (ProjDecisionInfo info : decisionInfos) {
			if (!inPast && info.getDecision().getDecisionDate().before(new Date())) {
				document.appendHeading2(translator.translate("report.decisions.past"), null);
				inPast = true;
			}
			exportDecision(document, info);
		}
	}

	private void exportDecision(OpenXMLDocument document, ProjDecisionInfo info) {
		String dateTitle = formatter.formatDate(info.getDecision().getDecisionDate());
		dateTitle += ": " + ProjectUIFactory.getDisplayName(translator, info.getDecision());
		document.appendText(dateTitle, true, Style.bold);
		
		exportArtefactMembers(document, info.getMembers());
		
		if (StringHelper.containsNonWhitespace(info.getDecision().getDetails())) {
			document.appendText(info.getDecision().getDetails(), true);
		}
		document.appendBreak(false);
	}
	
	private void exportNotes(OpenXMLDocument document) {
		if (noteKeyToFilename == null || noteKeyToFilename.isEmpty()) {
			return;
		}
		
		document.appendHeading1(translator.translate("report.notes.title"), null);
		
		notes.sort((i1, i2) -> i2.getNote().getArtefact().getContentModifiedDate().compareTo(i1.getNote().getArtefact().getContentModifiedDate()));
		notes.forEach(info -> exportNote(document, info));
	}

	private void exportNote(OpenXMLDocument document, ProjNoteInfo info) {
		String filename = noteKeyToFilename.get(info.getNote().getKey());
		if (filename != null) {
			document.appendText(formatter.formatDate(info.getNote().getArtefact().getContentModifiedDate()) + ": ", true);
			
			document.appendHyperlink(ProjectUIFactory.getDisplayName(translator, info.getNote()), filename, false);
			
			exportArtefactMembers(document, info.getMembers());
			
			document.appendBreak(false);
		}
	}
	
	private void exportFiles(OpenXMLDocument document) {
		if (fileKeyToFilename == null || fileKeyToFilename.isEmpty()) {
			return;
		}
		
		document.appendHeading1(translator.translate("report.files.title"), null);
		
		files.sort((i1, i2) -> i2.getFile().getArtefact().getContentModifiedDate().compareTo(i1.getFile().getArtefact().getContentModifiedDate()));
		files.forEach(info -> exportFile(document, info));
	}

	private void exportFile(OpenXMLDocument document, ProjFileInfo info) {
		String filename = fileKeyToFilename.get(info.getFile().getKey());
		if (filename != null) {
			document.appendText(formatter.formatDate(info.getFile().getVfsMetadata().getFileLastModified()) + ": ", true);
			
			document.appendHyperlink(ProjectUIFactory.getDisplayName(info.getFile()), filename, false);
			
			exportArtefactMembers(document, info.getMembers());
			
			document.appendBreak(false);
		}
	}

	private void exportArtefactMembers(OpenXMLDocument document, Set<Identity> members) {
		if (members == null || members.isEmpty()) return;
		
		String userDisplayNames = members.stream()
				.map(identity -> userManager.getUserDisplayName(identity))
				.sorted()
				.collect(Collectors.joining(", "));
		document.appendText(userDisplayNames, true, Style.italic);
	}

	private boolean filterDateRange(Date date) {
		if (dateRange.getFrom() != null && date.before(dateRange.getFrom())) {
			return false;
		}
		if (dateRange.getTo() != null && date.after(dateRange.getTo())) {
			return false;
		}
		
		return true;
	}

}

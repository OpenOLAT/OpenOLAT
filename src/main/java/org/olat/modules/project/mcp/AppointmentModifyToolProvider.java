/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.project.mcp;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.basesecurity.IdentityRef;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.ui.CalendarColors;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.mcp.tool.McpInvalidArgumentException;
import org.olat.mcp.tool.McpPermissionException;
import org.olat.mcp.tool.McpToolContext;
import org.olat.mcp.tool.McpToolDefinition;
import org.olat.mcp.tool.McpToolProvider;
import org.olat.mcp.tool.McpToolResult;
import org.olat.mcp.tool.McpToolScope;
import org.olat.mcp.tool.McpToolSupport;
import org.olat.modules.project.ProjAppointment;
import org.olat.modules.project.ProjAppointmentInfo;
import org.olat.modules.project.ProjAppointmentSearchParams;
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.ProjArtefactInfoParams;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjProjectSecurityCallback;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ui.ProjectBCFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Creates, edits and deletes the appointments of a project through the
 * ProjectService, the sequence of ProjAppointmentEditController with its
 * sub controllers (content with ProjAppointmentContentEditForm, references,
 * members). A series is edited and deleted as a whole; an occurrence of a
 * series changed in the calendar is edited in OpenOlat only.
 *
 * Initial date: 3 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
@Service
public class AppointmentModifyToolProvider implements McpToolProvider {

	public static final String NAME = "appointment_modify";

	static final String ACTION_CREATE = "create";
	static final String ACTION_UPDATE = "update";
	static final String ACTION_DELETE = "delete";
	private static final List<String> ACTIONS = List.of(ACTION_CREATE, ACTION_UPDATE, ACTION_DELETE);
	/** The fields of the result, the full appointment comes from appointment_query get */
	static final List<String> RESULT_FIELDS = List.of("key", "subject", "status", "url");
	/** The text fields of ProjAppointmentContentEditForm */
	static final int SUBJECT_MAX_LENGTH = 256;
	static final int LOCATION_MAX_LENGTH = 256;
	static final int DESCRIPTION_MAX_LENGTH = 4000;
	private static final List<String> CONTENT_ARGUMENTS = List.of("subject", "description", "location", "start", "end",
			"allDay", "color", "recurrence", "recurrenceEnd");

	private static final McpToolDefinition DEFINITION = new McpToolDefinition(
			"OpenOlat: change project appointments",
			"Appointments",
			"Creates, edits or deletes an appointment in the calendar of an OpenOlat project and returns its key, subject, status and URL; appointment_query get returns the full appointment. "
			+ "create needs projectKey, subject and start (ISO date-time, e.g. 2026-10-05T14:00 in the server zone or with offset; "
			+ "a date for allDay); end defaults to start plus one hour, for allDay to the same day. "
			+ "recurrence repeats the appointment (daily, workdaily, weekly, biweekly, monthly, yearly) until recurrenceEnd (yyyy-MM-dd, required with a recurrence). "
			+ "The metadata are tags, the involved project members (they see the appointment in their calendar) and links to other artefacts "
			+ "of the same project (ToDos by the key of todo_query, notes, files, appointments, decisions; milestones cannot be linked). "
			+ "In a template project an appointment has no dates and no involved members: create needs no start, start and end are refused. "
			+ "Call project_query for the projectKey and appointment_query for the appointmentKey. "
			+ "update changes only the arguments you pass; a new start moves the end by the same time unless you pass end; "
			+ "tags, members and links replace the current lists, an empty array clears tags or links, members needs at least one member; an empty string clears the description, "
			+ "the location or the color; recurrence none ends the repetition. "
			+ "update and delete act on the whole series of a recurring appointment; an occurrence changed in the calendar is edited in OpenOlat. "
			+ "delete is a soft delete.",
			inputSchema(), outputSchema(), true, false);

	@Autowired
	private ProjectService projectService;
	@Autowired
	private CalendarManager calendarManager;
	@Autowired
	private ProjectToolSupport support;

	@Override
	public String getToolName() {
		return NAME;
	}

	@Override
	public String getPack() {
		return ProjectMcpPack.NAME;
	}

	@Override
	public McpToolScope getScope() {
		return McpToolScope.WRITE;
	}

	@Override
	public boolean isAvailable() {
		return support.isPackAvailable();
	}

	@Override
	public McpToolDefinition getDefinition() {
		return DEFINITION;
	}

	@Override
	public McpToolResult execute(McpToolContext context, Map<String,Object> arguments) {
		String action = McpToolSupport.string(arguments, "action", null);
		if(action == null) {
			return McpToolResult.error("The argument action is required. Use one of: " + String.join(", ", ACTIONS) + ".");
		}
		return switch(action) {
			case ACTION_CREATE -> create(context, arguments);
			case ACTION_UPDATE -> update(context, arguments);
			case ACTION_DELETE -> delete(context, arguments);
			default -> McpToolResult.error("Unknown action " + action + ". Use one of: " + String.join(", ", ACTIONS) + ".");
		};
	}

	private McpToolResult create(McpToolContext context, Map<String,Object> arguments) {
		Long projectKey = McpToolSupport.longValue(arguments, "projectKey");
		if(projectKey == null) {
			return McpToolResult.error("The action create needs the argument projectKey. Call project_query to list your projects.");
		}
		ProjProject project = projectService.getProject(() -> projectKey);
		// an unknown key gets the answer of a foreign project
		if(project == null) {
			throw new McpPermissionException(ProjectToolSupport.describeProject(projectKey), "create appointments");
		}
		ProjProjectSecurityCallback secCallback = support.projectCallback(context, project);
		McpToolResult stateError = secCallback.canViewAppointments() ? ProjectToolSupport.stateError(project, null, null) : null;
		if(stateError != null) {
			return stateError;
		}
		if(!secCallback.canCreateAppointments()) {
			throw new McpPermissionException(ProjectToolSupport.describeProject(projectKey), "create appointments");
		}
		// Validate every argument before anything is written
		Content content = content(arguments, null, ProjectToolSupport.isTemplate(project));
		List<String> tags = support.tags(arguments, project);
		List<Identity> members = arguments.containsKey("members") ? support.resolveMembers(arguments, "members", project, "this appointment") : null;
		Set<ProjArtefact> links = support.resolveLinks(arguments, project, null);

		// The sequence of ProjAppointmentContentEditController.formOK and ProjAppointmentEditController.formOK
		Identity doer = context.identity();
		ProjectBCFactory bcFactory = ProjectBCFactory.createFactory(project);
		ProjAppointment appointment = projectService.createAppointment(doer, bcFactory, project, content.start(), content.end());
		update(doer, bcFactory, appointment, content);
		if(!tags.isEmpty()) {
			projectService.updateTags(doer, appointment.getArtefact(), tags);
		}
		if(!links.isEmpty()) {
			projectService.updateLinkedArtefacts(doer, appointment.getArtefact(), links);
		}
		if(members != null) {
			projectService.updateMembers(doer, bcFactory, appointment.getArtefact(), new ArrayList<IdentityRef>(members));
		}
		return result(appointment.getKey(), "Created: ");
	}

	private McpToolResult update(McpToolContext context, Map<String,Object> arguments) {
		ProjAppointment appointment = loadAppointment(arguments);
		ProjProject project = appointment.getArtefact().getProject();
		ProjProjectSecurityCallback secCallback = support.projectCallback(context, project);
		if(!secCallback.canViewAppointments()) {
			throw new McpPermissionException("appointment with key " + appointment.getKey(), "view the appointment");
		}
		McpToolResult stateError = ProjectToolSupport.stateError(project, appointment.getArtefact(), "appointment " + appointment.getKey());
		if(stateError != null) {
			return stateError;
		}
		if(!secCallback.canEditAppointment(appointment)) {
			throw new McpPermissionException("appointment with key " + appointment.getKey(), "edit the appointment");
		}
		if(StringHelper.containsNonWhitespace(appointment.getRecurrenceId())) {
			return McpToolResult.error("The appointment " + appointment.getKey() + " is a single occurrence of a recurring appointment, "
					+ "changed in the calendar. Edit it in OpenOlat: " + url(appointment));
		}
		// Validate every argument before anything is written; an absent argument keeps the value, an empty string clears it
		boolean contentChanged = CONTENT_ARGUMENTS.stream().anyMatch(arguments::containsKey);
		Content content = contentChanged ? content(arguments, appointment, ProjectToolSupport.isTemplate(project)) : null;
		List<String> tags = support.tags(arguments, project);
		List<Identity> members = arguments.containsKey("members") ? support.resolveMembers(arguments, "members", project, "this appointment") : null;
		Set<ProjArtefact> links = arguments.containsKey("links") ? support.resolveLinks(arguments, project, appointment.getArtefact()) : null;

		// The sequence of ProjAppointmentEditController.formOK
		Identity doer = context.identity();
		ProjectBCFactory bcFactory = ProjectBCFactory.createFactory(project);
		if(content != null) {
			update(doer, bcFactory, appointment, content);
		}
		if(arguments.containsKey("tags")) {
			projectService.updateTags(doer, appointment.getArtefact(), tags);
		}
		if(links != null) {
			projectService.updateLinkedArtefacts(doer, appointment.getArtefact(), links);
		}
		if(members != null) {
			projectService.updateMembers(doer, bcFactory, appointment.getArtefact(), new ArrayList<IdentityRef>(members));
		}
		return result(appointment.getKey(), "Updated: ");
	}

	private McpToolResult delete(McpToolContext context, Map<String,Object> arguments) {
		ProjAppointment appointment = loadAppointment(arguments);
		ProjProject project = appointment.getArtefact().getProject();
		ProjProjectSecurityCallback secCallback = support.projectCallback(context, project);
		if(!secCallback.canViewAppointments()) {
			throw new McpPermissionException("appointment with key " + appointment.getKey(), "view the appointment");
		}
		McpToolResult stateError = ProjectToolSupport.stateError(project, appointment.getArtefact(), "appointment " + appointment.getKey());
		if(stateError != null) {
			return stateError;
		}
		if(!secCallback.canDeleteAppointment(appointment, context.identity())) {
			throw new McpPermissionException("appointment with key " + appointment.getKey(), "delete the appointment");
		}
		if(StringHelper.containsNonWhitespace(appointment.getRecurrenceId())) {
			return McpToolResult.error("The appointment " + appointment.getKey() + " is a single occurrence of a recurring appointment, "
					+ "changed in the calendar. Delete it in OpenOlat: " + url(appointment));
		}
		// ProjCalendarAllController.doDeleteAppointment with the cascade all: the series with its changed occurrences
		projectService.deleteAppointmentSoftly(context.identity(), ProjectBCFactory.createFactory(project), appointment.getIdentifier(), null);
		return result(appointment.getKey(), "Deleted: ");
	}

	private void update(Identity doer, ProjectBCFactory bcFactory, ProjAppointment appointment, Content content) {
		projectService.updateAppointment(doer, bcFactory, appointment, content.start(), content.end(), content.subject(),
				content.description(), content.location(), content.color(), content.allDay(), content.recurrenceRule());
	}

	/**
	 * The validation of ProjAppointmentContentEditForm.validateFormLogic and the
	 * recurrence rule of ProjAppointmentContentEditForm.getRecurrenceRule.
	 * In a template project the form disables start and end and does not
	 * require them (ProjectCopyService copies template appointments without
	 * dates): the tool refuses both arguments and keeps the stored dates.
	 *
	 * @param arguments The arguments
	 * @param current The appointment to update or null for create
	 * @param template true for an appointment of a template project
	 * @return The complete content of the appointment
	 */
	private Content content(Map<String,Object> arguments, ProjAppointment current, boolean template) {
		if(template && (arguments.containsKey("start") || arguments.containsKey("end"))) {
			throw new McpInvalidArgumentException("An appointment of a template project has no dates. Omit start and end.");
		}
		boolean allDay = arguments.containsKey("allDay")
				? McpToolSupport.bool(arguments, "allDay", false)
				: current != null && current.isAllDay();

		String subject = textArgument(arguments, "subject", current == null ? null : current.getSubject(), SUBJECT_MAX_LENGTH);
		if(subject == null && (current == null || arguments.containsKey("subject"))) {
			// ProjAppointmentContentEditForm: the subject is mandatory
			throw new McpInvalidArgumentException("An appointment needs a subject.");
		}
		String description = textArgument(arguments, "description", current == null ? null : current.getDescription(), DESCRIPTION_MAX_LENGTH);
		String location = textArgument(arguments, "location", current == null ? null : current.getLocation(), LOCATION_MAX_LENGTH);
		String color = textArgument(arguments, "color", current == null ? null : current.getColor(), Integer.MAX_VALUE);
		if(arguments.containsKey("color") && color != null && !CalendarColors.getColorsList().contains(color)) {
			throw new McpInvalidArgumentException("Unknown color " + color + ". Use one of: " + String.join(", ", CalendarColors.getColorsList()) + ".");
		}

		// dates
		Date currentStart = current == null ? null : current.getStartDate();
		Date currentEnd = current == null ? null : current.getEndDate();
		Date start = arguments.containsKey("start") ? AppointmentToolSupport.dateTime(arguments, "start", allDay) : currentStart;
		if(start == null && current == null && !template) {
			throw new McpInvalidArgumentException("The action create needs the argument start, e.g. 2026-10-05T14:00.");
		}
		Date end;
		if(arguments.containsKey("end")) {
			end = AppointmentToolSupport.dateTime(arguments, "end", allDay);
		} else if(currentEnd != null && currentStart != null && start != null) {
			// ProjAppointmentContentEditForm.syncEndDate: a new start moves the end by the same time
			Duration duration = Duration.between(DateUtils.toLocalDateTime(currentStart), DateUtils.toLocalDateTime(start));
			end = DateUtils.toDate(DateUtils.toLocalDateTime(currentEnd).plus(duration));
		} else if(start != null) {
			end = allDay ? start : DateUtils.addHours(start, 1);
		} else {
			end = currentEnd;
		}
		if(allDay) {
			start = start == null ? null : AppointmentToolSupport.startOfDay(start);
			end = end == null ? null : AppointmentToolSupport.startOfDay(end);
		}
		if(start == null && end != null) {
			throw new McpInvalidArgumentException("An appointment with an end needs a start.");
		}
		if(start != null && end != null && end.before(start)) {
			throw new McpInvalidArgumentException("The end " + McpToolSupport.iso(end) + " is before the start " + McpToolSupport.iso(start) + ".");
		}

		String recurrenceRule = recurrenceRule(arguments, current, start, allDay, template);
		return new Content(start, end, subject, description, location, color, allDay, recurrenceRule);
	}

	/**
	 * @param template true in a template project: the form offers the
	 *        recurrence without dates, a rule needs no start there
	 */
	private String recurrenceRule(Map<String,Object> arguments, ProjAppointment current, Date start, boolean allDay, boolean template) {
		String currentRule = current == null ? null : current.getRecurrenceRule();
		boolean allDayChanged = current != null && current.isAllDay() != allDay;
		boolean changed = arguments.containsKey("recurrence") || arguments.containsKey("recurrenceEnd")
				|| (allDayChanged && StringHelper.containsNonWhitespace(currentRule));
		if(!changed) {
			if(StringHelper.containsNonWhitespace(currentRule) && start == null && !template) {
				throw new McpInvalidArgumentException("A recurring appointment needs a start.");
			}
			return currentRule;
		}

		String recurrence = arguments.containsKey("recurrence")
				? McpToolSupport.string(arguments, "recurrence", AppointmentToolSupport.RECURRENCE_NONE)
				: AppointmentToolSupport.recurrence(currentRule);
		if(AppointmentToolSupport.RECURRENCE_OTHER.equals(recurrence) && !arguments.containsKey("recurrence")) {
			throw new McpInvalidArgumentException("The repetition of this appointment cannot be changed via MCP, pass a recurrence of: "
					+ String.join(", ", AppointmentToolSupport.RECURRENCES) + ".");
		}
		if(!AppointmentToolSupport.RECURRENCES.contains(recurrence)) {
			throw new McpInvalidArgumentException("Unknown recurrence " + recurrence + ". Use one of: "
					+ String.join(", ", AppointmentToolSupport.RECURRENCES) + ".");
		}
		if(AppointmentToolSupport.RECURRENCE_NONE.equals(recurrence)) {
			if(arguments.containsKey("recurrenceEnd") && McpToolSupport.string(arguments, "recurrenceEnd", null) != null) {
				throw new McpInvalidArgumentException("The argument recurrenceEnd needs a recurrence other than none.");
			}
			return null;
		}

		Date recurrenceEnd = arguments.containsKey("recurrenceEnd")
				? McpToolSupport.date(arguments, "recurrenceEnd")
				: currentRecurrenceEnd(currentRule);
		if(recurrenceEnd == null) {
			throw new McpInvalidArgumentException("The recurrence " + recurrence + " needs the argument recurrenceEnd, the last day of the series (yyyy-MM-dd).");
		}
		if(start == null && !template) {
			throw new McpInvalidArgumentException("A recurring appointment needs a start.");
		}
		// ProjAppointmentContentEditForm compares the last day (start of the day) with the start date-time:
		// a series that ends on its start day is refused unless it starts at midnight
		if(start != null && recurrenceEnd.before(start)) {
			throw new McpInvalidArgumentException("The recurrenceEnd " + McpToolSupport.isoDate(recurrenceEnd) + " must be after the start "
					+ McpToolSupport.iso(start) + ". Use a later day for the end of the series.");
		}
		return calendarManager.getRecurrenceRule(AppointmentToolSupport.recurrenceKey(recurrence), recurrenceEnd, allDay);
	}

	/**
	 * @param rule The current rule
	 * @return The last day of the series at the start of the day in the server zone, or null
	 */
	private Date currentRecurrenceEnd(String rule) {
		ZonedDateTime recurrenceEnd = calendarManager.getRecurrenceEndDate(rule);
		if(recurrenceEnd == null) {
			return null;
		}
		ZoneId zone = ZoneId.systemDefault();
		return Date.from(recurrenceEnd.toLocalDate().atStartOfDay(zone).toInstant());
	}

	/**
	 * @return The value of the argument, the current value when absent, null for an empty string
	 */
	private static String textArgument(Map<String,Object> arguments, String name, String currentValue, int maxLength) {
		String value = arguments.containsKey(name) ? McpToolSupport.string(arguments, name, null) : currentValue;
		McpToolSupport.checkLength(name, value, maxLength);
		return value;
	}

	private ProjAppointment loadAppointment(Map<String,Object> arguments) {
		Long appointmentKey = McpToolSupport.longValue(arguments, "appointmentKey");
		if(appointmentKey == null) {
			throw new McpInvalidArgumentException("This action needs the argument appointmentKey. Call appointment_query to find it.");
		}
		ProjAppointment appointment = projectService.getAppointment(() -> appointmentKey);
		// an unknown key gets the answer of a foreign appointment
		if(appointment == null) {
			throw new McpPermissionException("appointment with key " + appointmentKey, "view the appointment");
		}
		return appointment;
	}

	private static String url(ProjAppointment appointment) {
		return ProjectToolSupport.url(appointment.getArtefact().getProject(), ProjAppointment.TYPE, appointment.getKey());
	}

	private McpToolResult result(Long appointmentKey, String prefix) {
		ProjAppointmentSearchParams searchParams = new ProjAppointmentSearchParams();
		searchParams.setAppointments(List.of(() -> appointmentKey));
		ProjAppointmentInfo info = projectService.getAppointmentInfos(searchParams, ProjArtefactInfoParams.of(true, false, true)).get(0);
		Map<String,Object> item = AppointmentToolSupport.appointmentItem(support, calendarManager, info, false);
		return McpToolResult.of(McpToolSupport.modifyResult(item, RESULT_FIELDS),
				prefix + AppointmentToolSupport.appointmentLine(item) + McpToolSupport.fullItemHint(AppointmentQueryToolProvider.NAME));
	}

	static Map<String,Object> outputSchema() {
		return McpToolSupport.modifyOutputSchema(AppointmentToolSupport.appointmentItemSchema(), RESULT_FIELDS, List.of("key", "status", "url"));
	}

	private static Map<String,Object> inputSchema() {
		List<String> colors = CalendarColors.getColorsList();
		List<String> colorValues = new ArrayList<>(colors);
		colorValues.add("");
		Map<String,Object> properties = new LinkedHashMap<>();
		properties.put("action", Map.of("type", "string", "enum", ACTIONS,
				"description", "create: a new appointment in a project; update: change the passed fields; delete: soft delete of the appointment or the whole series"));
		properties.put("projectKey", Map.of("type", "integer", "description", "create only, required: the key of the project"));
		properties.put("appointmentKey", Map.of("type", "integer", "description", "update and delete, required: the key of the appointment"));
		properties.put("subject", Map.of("type", "string", "maxLength", SUBJECT_MAX_LENGTH, "description", "create: required; cannot be cleared"));
		properties.put("start", Map.of("type", "string",
				"description", "create: required, except in a template project; ISO date-time with offset or local in the server zone (yyyy-MM-ddTHH:mm); "
						+ "a date (yyyy-MM-dd) for allDay; not allowed in a template project"));
		properties.put("end", Map.of("type", "string",
				"description", "as start, not before start; create defaults to start plus one hour, for allDay to the same day; not allowed in a template project"));
		properties.put("allDay", Map.of("type", "boolean", "description", "true: an all day appointment, start and end are days; create defaults to false"));
		properties.put("location", Map.of("type", "string", "maxLength", LOCATION_MAX_LENGTH, "description", "an empty string clears it"));
		properties.put("description", Map.of("type", "string", "maxLength", DESCRIPTION_MAX_LENGTH, "description", "plain text, an empty string clears it"));
		properties.put("color", Map.of("type", "string", "enum", colorValues,
				"description", "the calendar color; an empty string resets the default color"));
		properties.put("recurrence", Map.of("type", "string", "enum", AppointmentToolSupport.RECURRENCES,
				"description", "repeats the appointment until recurrenceEnd; workdaily is Monday to Friday, biweekly every second week; "
						+ "create defaults to none, none ends the repetition"));
		properties.put("recurrenceEnd", Map.of("type", "string", "format", "date",
				"description", "the last day of the series (yyyy-MM-dd), required with a recurrence other than none; a later day than the start day, "
						+ "except for an all day appointment"));
		properties.put("tags", ProjectToolSupport.tagsInputSchema());
		properties.put("members", ProjectToolSupport.membersInputSchema("appointment"));
		properties.put("links", ProjectToolSupport.linksInputSchema());
		return Map.of("type", "object", "properties", properties, "required", List.of("action"), "additionalProperties", Boolean.FALSE);
	}

	private record Content(Date start, Date end, String subject, String description, String location, String color,
			boolean allDay, String recurrenceRule) {
		//
	}
}

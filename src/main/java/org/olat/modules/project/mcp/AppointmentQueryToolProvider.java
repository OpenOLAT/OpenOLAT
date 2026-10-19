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

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.olat.commons.calendar.CalendarManager;
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
import org.olat.modules.project.ProjArtefactInfoParams;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ProjectStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The appointments of one project in a time window, as the calendar of the
 * project shows them (ProjCalendarAllController), and one appointment with
 * its links to other artefacts of the project. A series is listed once with
 * its rule, the occurrences are not expanded.
 *
 * Initial date: 3 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
@Service
public class AppointmentQueryToolProvider implements McpToolProvider {

	public static final String NAME = "appointment_query";

	static final String ACTION_LIST = "list";
	static final String ACTION_GET = "get";
	private static final List<String> ACTIONS = List.of(ACTION_LIST, ACTION_GET);
	/** Default length of the window in days after from */
	static final int WINDOW_DEFAULT_DAYS = 30;
	/** Maximum length of the window in days */
	static final int WINDOW_MAX_DAYS = 366;

	private static final McpToolDefinition DEFINITION = new McpToolDefinition(
			"OpenOlat: project appointments",
			"Appointments",
			"Returns the appointments of an OpenOlat project calendar: subject, start and end, location, description, repetition, "
			+ "with the metadata tags, involved members and links to other artefacts of the project "
			+ "(ToDos, notes, files, appointments, milestones, decisions). "
			+ "Use action list with projectKey (from project_query) and a window from and to (ISO dates, default today plus "
			+ WINDOW_DEFAULT_DAYS + " days, at most " + WINDOW_MAX_DAYS + " days); an appointment matches when it overlaps the window. "
			+ "A recurring appointment is listed once with its rule: start and end of the first occurrence, the repetition and its last day; "
			+ "the occurrences are not expanded. datesOnly=false adds the appointments without dates. "
			+ "Further filters: status and createdByMe; list is sorted by start. "
			+ "Use action get with appointmentKey for one appointment with its links; to change an appointment call appointment_modify.",
			inputSchema(), outputSchema(), false, true);

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
		return McpToolScope.READ;
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
		String action = McpToolSupport.string(arguments, "action", ACTION_LIST);
		return switch(action) {
			case ACTION_LIST -> list(context, arguments);
			case ACTION_GET -> get(context, arguments);
			default -> McpToolResult.error("Unknown action " + action + ". Use one of: " + String.join(", ", ACTIONS) + ".");
		};
	}

	private McpToolResult list(McpToolContext context, Map<String,Object> arguments) {
		Long projectKey = McpToolSupport.longValue(arguments, "projectKey");
		if(projectKey == null) {
			return McpToolResult.error("The action list needs the argument projectKey. Call project_query to list your projects.");
		}
		String statusValue = McpToolSupport.string(arguments, "status", ProjectStatus.active.name());
		List<ProjectStatus> status = ProjectToolSupport.statusFilter(statusValue, ProjectToolSupport.ARTEFACT_STATUS);
		if(status == null) {
			return McpToolResult.error(ProjectToolSupport.unknownStatusFilter(statusValue, ProjectToolSupport.ARTEFACT_STATUS));
		}
		Date fromArgument = McpToolSupport.date(arguments, "from");
		Date toArgument = McpToolSupport.date(arguments, "to");
		ZoneId zone = ZoneId.systemDefault();
		LocalDate from = fromArgument == null ? LocalDate.now(zone) : fromArgument.toInstant().atZone(zone).toLocalDate();
		LocalDate to = toArgument == null ? from.plusDays(WINDOW_DEFAULT_DAYS) : toArgument.toInstant().atZone(zone).toLocalDate();
		if(to.isBefore(from)) {
			return McpToolResult.error("The argument to (" + to + ") is before from (" + from + ").");
		}
		if(ChronoUnit.DAYS.between(from, to) > WINDOW_MAX_DAYS) {
			return McpToolResult.error("The window from " + from + " to " + to + " is longer than " + WINDOW_MAX_DAYS + " days.");
		}
		boolean datesOnly = McpToolSupport.bool(arguments, "datesOnly", true);
		boolean createdByMe = McpToolSupport.bool(arguments, "createdByMe", false);
		int limit = McpToolSupport.limit(arguments);

		ProjProject project = projectService.getProject(() -> projectKey);
		// an unknown key gets the answer of a foreign project
		if(project == null || !support.projectCallback(context, project).canViewAppointments()) {
			throw new McpPermissionException(ProjectToolSupport.describeProject(projectKey), "view the appointments");
		}

		// The search parameters of ProjCalendarAllController: the series masters, not the edited occurrences
		ProjAppointmentSearchParams searchParams = new ProjAppointmentSearchParams();
		searchParams.setProject(project);
		if(!status.isEmpty()) {
			searchParams.setStatus(status);
		}
		searchParams.setRecurrenceIdAvailable(Boolean.FALSE);
		if(datesOnly) {
			searchParams.setDatesNull(Boolean.FALSE);
		}
		// ProjectService has neither a date window nor a creator filter for appointments; the calendar loads the same set
		Date windowStart = Date.from(from.atStartOfDay(zone).toInstant());
		Date windowEnd = Date.from(to.plusDays(1).atStartOfDay(zone).toInstant());
		List<ProjAppointmentInfo> infos = new ArrayList<>();
		for(ProjAppointmentInfo info:projectService.getAppointmentInfos(searchParams, ProjArtefactInfoParams.of(true, false, true))) {
			ProjAppointment appointment = info.getAppointment();
			if(createdByMe && !context.identity().getKey().equals(appointment.getArtefact().getCreator().getKey())) {
				continue;
			}
			Date start = appointment.getStartDate();
			Date end = appointment.getEndDate();
			boolean withDates = start != null && end != null;
			if((withDates && start.before(windowEnd) && !end.before(windowStart)) || (!withDates && !datesOnly)) {
				infos.add(info);
			}
		}
		infos.sort(Comparator.comparing((ProjAppointmentInfo info) -> info.getAppointment().getStartDate(), Comparator.nullsLast(Comparator.naturalOrder()))
				.thenComparing(info -> info.getAppointment().getKey()));
		int total = infos.size();

		List<Map<String,Object>> items = new ArrayList<>();
		List<String> lines = new ArrayList<>();
		for(ProjAppointmentInfo info:infos.subList(0, Math.min(limit, total))) {
			Map<String,Object> item = AppointmentToolSupport.appointmentItem(support, calendarManager, info, false);
			items.add(item);
			lines.add(AppointmentToolSupport.appointmentLine(item));
		}
		String text = "Window " + from + " to " + to + ". " + McpToolSupport.listText(lines, total, "appointments");
		return McpToolResult.of(McpToolSupport.listResult(items, total, limit), text);
	}

	private McpToolResult get(McpToolContext context, Map<String,Object> arguments) {
		Long appointmentKey = McpToolSupport.longValue(arguments, "appointmentKey");
		if(appointmentKey == null) {
			return McpToolResult.error("The action get needs the argument appointmentKey. Call appointment_query with action list to find it.");
		}
		ProjAppointmentSearchParams searchParams = new ProjAppointmentSearchParams();
		searchParams.setAppointments(List.of(() -> appointmentKey));
		List<ProjAppointmentInfo> infos = projectService.getAppointmentInfos(searchParams, ProjArtefactInfoParams.of(true, false, true));
		// an unknown key gets the answer of a foreign appointment
		if(infos.isEmpty()) {
			throw new McpPermissionException("appointment with key " + appointmentKey, "view the appointment");
		}
		ProjAppointmentInfo info = infos.get(0);
		ProjProject project = info.getAppointment().getArtefact().getProject();
		if(!support.projectCallback(context, project).canViewAppointments()) {
			throw new McpPermissionException("appointment with key " + appointmentKey, "view the appointment");
		}
		Map<String,Object> item = AppointmentToolSupport.appointmentItem(support, calendarManager, info, true);
		Map<String,Object> result = new LinkedHashMap<>();
		result.put("item", item);
		StringBuilder text = new StringBuilder("Appointment ").append(AppointmentToolSupport.appointmentLine(item));
		if(item.containsKey("description")) {
			text.append("\n\n").append(item.get("description"));
		}
		return McpToolResult.of(result, text.toString());
	}

	private static Map<String,Object> inputSchema() {
		Map<String,Object> properties = new LinkedHashMap<>();
		properties.put("action", Map.of("type", "string", "enum", ACTIONS, "default", ACTION_LIST,
				"description", "list: the appointments of a project in a window; get: one appointment with the links"));
		properties.put("projectKey", Map.of("type", "integer", "description", "list only, required: the key of the project"));
		properties.put("from", Map.of("type", "string", "format", "date",
				"description", "list only: first day of the window (yyyy-MM-dd), default today"));
		properties.put("to", Map.of("type", "string", "format", "date",
				"description", "list only: last day of the window (yyyy-MM-dd), default from plus " + WINDOW_DEFAULT_DAYS
				+ " days, at most " + WINDOW_MAX_DAYS + " days after from"));
		properties.put("datesOnly", Map.of("type", "boolean", "default", Boolean.TRUE,
				"description", "list only: false adds the appointments without start and end"));
		properties.put("status", ProjectToolSupport.artefactStatusFilterSchema("appointments"));
		properties.put("createdByMe", Map.of("type", "boolean", "default", Boolean.FALSE,
				"description", "list only: only the appointments the account created"));
		properties.put("appointmentKey", Map.of("type", "integer", "description", "get only, required: the key of the appointment"));
		properties.put("limit", Map.of("type", "integer", "minimum", 1, "maximum", McpToolSupport.MAX_LIMIT,
				"default", McpToolSupport.DEFAULT_LIMIT, "description", "list only: maximum number of appointments returned"));
		return Map.of("type", "object", "properties", properties, "additionalProperties", Boolean.FALSE);
	}

	static Map<String,Object> outputSchema() {
		Map<String,Object> itemSchema = AppointmentToolSupport.appointmentItemSchema();
		return McpToolSupport.queryOutputSchema(itemSchema, ProjectToolSupport.outputDefs());
	}
}

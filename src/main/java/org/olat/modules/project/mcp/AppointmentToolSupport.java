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

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.core.util.StringHelper;
import org.olat.mcp.tool.McpInvalidArgumentException;
import org.olat.mcp.tool.McpToolSupport;
import org.olat.modules.project.ProjAppointment;
import org.olat.modules.project.ProjAppointmentInfo;
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.ProjProject;

/**
 * The appointment of the wire format, shared by appointment_query and
 * appointment_modify: the item, its line in the text result, its schema,
 * the recurrence words of ProjAppointmentContentEditForm and the parsing
 * of the date-time arguments.
 *
 * Initial date: 3 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
class AppointmentToolSupport {

	static final String RECURRENCE_NONE = "none";
	/** The choices of the recurrence dropdown of ProjAppointmentContentEditForm, as lower case words */
	static final List<String> RECURRENCES = List.of(RECURRENCE_NONE,
			KalendarEvent.DAILY.toLowerCase(Locale.ROOT), KalendarEvent.WORKDAILY.toLowerCase(Locale.ROOT),
			KalendarEvent.WEEKLY.toLowerCase(Locale.ROOT), KalendarEvent.BIWEEKLY.toLowerCase(Locale.ROOT),
			KalendarEvent.MONTHLY.toLowerCase(Locale.ROOT), KalendarEvent.YEARLY.toLowerCase(Locale.ROOT));
	/** A rule the dropdown cannot show, e.g. from an import */
	static final String RECURRENCE_OTHER = "other";

	private AppointmentToolSupport() {
		//
	}

	/**
	 * @param rule The RRULE of an appointment or null
	 * @return The word of the dropdown, none without rule, other for a rule the dropdown cannot show
	 */
	static String recurrence(String rule) {
		if(!StringHelper.containsNonWhitespace(rule)) {
			return RECURRENCE_NONE;
		}
		String recurrence = CalendarUtils.getRecurrence(rule);
		if(recurrence == null) {
			return RECURRENCE_OTHER;
		}
		String word = recurrence.toLowerCase(Locale.ROOT);
		return RECURRENCES.contains(word) ? word : RECURRENCE_OTHER;
	}

	/**
	 * @param word A word of {@link #RECURRENCES} except none
	 * @return The key of the dropdown for CalendarManager.getRecurrenceRule, e.g. WORKDAILY
	 */
	static String recurrenceKey(String word) {
		return word.toUpperCase(Locale.ROOT);
	}

	/**
	 * Parses a date-time argument: ISO date-time with offset, or local
	 * (yyyy-MM-ddTHH:mm) in the server zone. An all day appointment also
	 * accepts a bare date; its date-time is the start of the day.
	 *
	 * @param arguments The arguments
	 * @param name The argument name
	 * @param allDay true for an all day appointment
	 * @return The date, null when absent
	 */
	static Date dateTime(Map<String,Object> arguments, String name, boolean allDay) {
		String value = McpToolSupport.string(arguments, name, null);
		if(value == null) {
			if(arguments.containsKey(name)) {
				throw new McpInvalidArgumentException("The argument " + name + " cannot be empty.");
			}
			return null;
		}
		ZoneId zone = ZoneId.systemDefault();
		ZonedDateTime dateTime = null;
		try {
			TemporalAccessor parsed = DateTimeFormatter.ISO_DATE_TIME.parse(value);
			dateTime = parsed.isSupported(ChronoField.OFFSET_SECONDS)
					? Instant.from(parsed).atZone(zone)
					: LocalDateTime.from(parsed).atZone(zone);
		} catch (DateTimeException e) {
			try {
				dateTime = LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME).atZone(zone);
			} catch (DateTimeParseException e2) {
				if(allDay) {
					try {
						dateTime = LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay(zone);
					} catch (DateTimeParseException e3) {
						// error below
					}
				}
			}
		}
		if(dateTime == null) {
			throw new McpInvalidArgumentException("The argument " + name + " must be an ISO date-time (yyyy-MM-ddTHH:mm, optional offset)"
					+ (allDay ? " or an ISO date (yyyy-MM-dd)" : "") + ", got: " + value);
		}
		if(allDay) {
			dateTime = dateTime.toLocalDate().atStartOfDay(zone);
		}
		return Date.from(dateTime.toInstant());
	}

	/**
	 * @param date A date
	 * @return The start of the day of the date in the server zone
	 */
	static Date startOfDay(Date date) {
		ZoneId zone = ZoneId.systemDefault();
		return Date.from(date.toInstant().atZone(zone).toLocalDate().atStartOfDay(zone).toInstant());
	}

	/**
	 * @param support The shared project tool support for persons and links
	 * @param calendarManager For the end of the recurrence
	 * @param info The appointment with members and tags
	 * @param withLinks true for the links (get and every modify result)
	 * @return The item
	 */
	static Map<String,Object> appointmentItem(ProjectToolSupport support, CalendarManager calendarManager,
			ProjAppointmentInfo info, boolean withLinks) {
		ProjAppointment appointment = info.getAppointment();
		ProjArtefact artefact = appointment.getArtefact();
		ProjProject project = artefact.getProject();
		Map<String,Object> item = new LinkedHashMap<>();
		item.put("key", appointment.getKey());
		item.put("projectKey", project.getKey());
		if(StringHelper.containsNonWhitespace(appointment.getSubject())) {
			item.put("subject", appointment.getSubject());
		}
		if(StringHelper.containsNonWhitespace(appointment.getDescription())) {
			item.put("description", appointment.getDescription());
		}
		if(StringHelper.containsNonWhitespace(appointment.getLocation())) {
			item.put("location", appointment.getLocation());
		}
		if(appointment.getStartDate() != null) {
			item.put("start", McpToolSupport.iso(appointment.getStartDate()));
		}
		if(appointment.getEndDate() != null) {
			item.put("end", McpToolSupport.iso(appointment.getEndDate()));
		}
		item.put("allDay", Boolean.valueOf(appointment.isAllDay()));
		if(StringHelper.containsNonWhitespace(appointment.getColor())) {
			item.put("color", appointment.getColor());
		}
		String rule = appointment.getRecurrenceRule();
		item.put("recurrence", recurrence(rule));
		if(StringHelper.containsNonWhitespace(rule)) {
			item.put("recurrenceRule", rule);
			ZonedDateTime recurrenceEnd = calendarManager.getRecurrenceEndDate(rule);
			if(recurrenceEnd != null) {
				item.put("recurrenceEnd", DateTimeFormatter.ISO_LOCAL_DATE.format(recurrenceEnd));
			}
		}
		item.put("status", artefact.getStatus().name());
		item.put("created", McpToolSupport.iso(artefact.getCreationDate()));
		item.put("lastModified", McpToolSupport.iso(artefact.getContentModifiedDate()));
		item.put("creator", support.toPerson(artefact.getCreator()));
		item.put("tags", ProjectToolSupport.tagNames(info));
		item.put("members", support.toPersons(info.getMembers()));
		if(withLinks) {
			item.put("links", support.links(artefact));
		}
		item.put("url", ProjectToolSupport.url(project, ProjAppointment.TYPE, appointment.getKey()));
		return item;
	}

	static String appointmentLine(Map<String,Object> item) {
		StringBuilder sb = new StringBuilder();
		sb.append(item.containsKey("subject") ? item.get("subject") : "(no subject)").append(" (key ").append(item.get("key"));
		if(item.containsKey("start")) {
			sb.append(", ").append(item.get("start")).append(" to ").append(item.containsKey("end") ? item.get("end") : "(no end)");
		} else {
			sb.append(", no dates");
		}
		if(Boolean.TRUE.equals(item.get("allDay"))) {
			sb.append(", all day");
		}
		if(!RECURRENCE_NONE.equals(item.get("recurrence"))) {
			sb.append(", ").append(item.get("recurrence"));
			if(item.containsKey("recurrenceEnd")) {
				sb.append(" until ").append(item.get("recurrenceEnd"));
			}
		}
		if(item.containsKey("location")) {
			sb.append(", at ").append(item.get("location"));
		}
		sb.append(", ").append(item.get("status"));
		@SuppressWarnings("unchecked")
		List<String> tags = (List<String>)item.get("tags");
		if(!tags.isEmpty()) {
			sb.append(", tags ").append(String.join(", ", tags));
		}
		return sb.append(") ").append(item.get("url")).toString();
	}

	static Map<String,Object> appointmentItemSchema() {
		List<String> recurrences = new ArrayList<>(RECURRENCES);
		recurrences.add(RECURRENCE_OTHER);
		Map<String,Object> properties = new LinkedHashMap<>();
		properties.put("key", Map.of("type", "integer", "description", "The appointment key, the appointmentKey of get, update and delete"));
		properties.put("projectKey", Map.of("type", "integer"));
		properties.put("subject", Map.of("type", "string", "description", "absent for an appointment without subject"));
		properties.put("description", Map.of("type", "string", "description", "plain text; absent when empty"));
		properties.put("location", Map.of("type", "string", "description", "absent when empty"));
		properties.put("start", Map.of("type", "string", "format", "date-time",
				"description", "start of the appointment, of the first occurrence of a series; absent for an appointment without dates"));
		properties.put("end", Map.of("type", "string", "format", "date-time",
				"description", "end of the appointment, of the first occurrence of a series; absent for an appointment without dates"));
		properties.put("allDay", Map.of("type", "boolean", "description", "true: start and end are days, the time is the start of the day"));
		properties.put("color", Map.of("type", "string", "description", "the calendar color; absent for the default color"));
		properties.put("recurrence", Map.of("type", "string", "enum", recurrences,
				"description", "the repetition as OpenOlat offers it; other for a rule OpenOlat cannot edit"));
		properties.put("recurrenceRule", Map.of("type", "string", "description", "the iCalendar RRULE of a series; absent without repetition"));
		properties.put("recurrenceEnd", Map.of("type", "string", "format", "date", "description", "the last day of the series (yyyy-MM-dd)"));
		properties.put("status", ProjectToolSupport.artefactStatusSchema());
		properties.put("created", Map.of("type", "string", "format", "date-time"));
		properties.put("lastModified", Map.of("type", "string", "format", "date-time", "description", "last change of content or metadata"));
		properties.put("creator", McpToolSupport.personSchema());
		properties.put("tags", Map.of("type", "array", "items", Map.of("type", "string")));
		properties.put("members", Map.of("type", "array", "items", McpToolSupport.personSchema(),
				"description", "The project members involved in the appointment, they see it in their calendar"));
		properties.put("links", Map.of("type", "array", "items", ProjectToolSupport.linkSchema(),
				"description", "get, create, update and delete: the linked artefacts of the project"));
		properties.put("url", Map.of("type", "string", "description", "Link to the appointment in OpenOlat"));
		return Map.of("type", "object", "properties", properties,
				"required", List.of("key", "projectKey", "allDay", "recurrence", "status", "created", "lastModified", "creator", "tags", "members", "url"));
	}
}

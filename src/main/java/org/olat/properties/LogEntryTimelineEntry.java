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
package org.olat.properties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.core.gui.components.timeline.TimelineEntry;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;

/**
 * Initial date: Mar 18, 2025
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class LogEntryTimelineEntry implements TimelineEntry {

	private final LogEntry logEntry;
	private final Formatter formatter;
	private final Translator translator;


	public LogEntryTimelineEntry(LogEntry logEntry, Locale locale) {
		this.logEntry = logEntry;
		this.formatter = Formatter.getInstance(locale);
		this.translator = Util.createPackageTranslator(LogEntryTimelineEntry.class, locale);
	}

	@Override
	public String getTitle() {
		// Combine author name and role in brackets (author (role))
		// since a role can be empty, only display it if there is a role, same goes for orgUnit
		StringBuilder title = new StringBuilder(logEntry.author());

		if (logEntry.orgUnit() != null && !logEntry.orgUnit().isEmpty()) {
			title.append(" (").append(logEntry.orgUnit()).append(")");
		}
		if (logEntry.role() != null && !logEntry.role().isEmpty()) {
			title.append(" (").append(logEntry.role()).append(")");
		}

		return title.toString();
	}


	@Override
	public String getTimePeriod() {
		String formattedTime = formatter.formatTimeShort(logEntry.timestamp());
		return translator.translate("log.time.period", formattedTime);
	}

	@Override
	public String getLocation() {
		return null; // No need for this in Logs
	}

	public String getTranslatedAction() {
		String[] actions = logEntry.action().split("\n");
		List<String> translatedActions = new ArrayList<>();

		for (String action : actions) {
			String actionKey = "log.action." + action.toLowerCase()
					.replace(" ", ".")
					.replace("-", ".")
					.replace(":", "")
					.replace("evaluation.finshed", "evaluation.finished");
			translatedActions.add(translator.translate(actionKey));
		}

		return String.join("\n", translatedActions);
	}

	public String getDetails() {
		String action = logEntry.action() != null ? logEntry.action().trim() : "";
		String details = logEntry.details() != null ? logEntry.details() : "";

		if (action.equalsIgnoreCase("COMMENT set to")) {
			return details.replace("\n", "</br>");
		}

		// Handle "Deadline extension for assignment" specially
		if (action.equalsIgnoreCase("Deadline extension for assignment")) {
			Map<String, String> detailMap = Arrays.stream(details.split(";"))
					.map(kv -> kv.split("="))
					.filter(pair -> pair.length == 2)
					.collect(Collectors.toMap(pair -> pair[0], pair -> pair[1]));

			String userId = detailMap.getOrDefault("userId", "-");
			String fromDate = detailMap.getOrDefault("from", "-");
			String toDate = detailMap.getOrDefault("to", "-");

			return translator.translate("log.action.deadline.extension.details", userId, fromDate, toDate);
		}

		// Handle grouped entries
		String[] detailParts = details.split("\n");
		// Only translate boolean strings if present (found in passed set to)
		if (details.toLowerCase().contains("true") || details.toLowerCase().contains("false")) {
			detailParts = Arrays.stream(detailParts)
					.map(part -> {
						String trimmed = part.trim().toLowerCase();
						if (trimmed.equals("true")) {
							return translator.translate("log.action.passed.true");
						} else if (trimmed.equals("false")) {
							return translator.translate("log.action.passed.false");
						}
						return part;
					})
					.toArray(String[]::new);
		}

		return String.join("\n", detailParts);
	}
}


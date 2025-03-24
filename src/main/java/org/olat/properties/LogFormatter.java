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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;

/**
 * Initial date: Mar 04, 2025
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class LogFormatter {

	private static final Logger log = Tracing.createLoggerFor(LogFormatter.class);

	/**
	 * Parses the log content and returns a LogParseResult containing valid log entries
	 * and any unknown/malformed log blocks or lines
	 *
	 * @param logContent the log content to parse as a String
	 * @return a LogParseResult with two lists: valid LogEntry objects and unknown lines.
	 */
	public LogParseResultWrapper parseLog(String logContent) {
		List<LogEntry> logEntries = new ArrayList<>();
		List<String> unknownLines = new ArrayList<>();

		// Wrap the log content with a BufferedReader to process it line by line
		try (BufferedReader reader = new BufferedReader(new StringReader(logContent))) {
			String line;
			while ((line = reader.readLine()) != null) {
				// Check if the current line begins with "Date:" (indicates a new log entry)
				if (!line.startsWith("Date:")) {
					unknownLines.add(line);
					continue;
				}
				StringBuilder entryBlock = new StringBuilder(line).append("\n");
				try {
					// Parse Date
					Date date = parseDate(line, entryBlock, unknownLines);
					if (date == null) {
						continue;
					}

					// Parse Identity
					Identity identity = parseIdentity(reader, entryBlock, unknownLines);
					if (identity == null) {
						continue;
					}

					// Parse Action/Details
					ActionDetails actionDetails = parseAction(reader, entryBlock, unknownLines);
					if (actionDetails == null) {
						continue;
					}

					logEntries.add(new LogEntry(date, identity.author, identity.orgUnit, identity.userId, identity.role,
							actionDetails.action, actionDetails.details));

					// Consume optional separator (which sometimes appears)
					consumeSeparator(reader, entryBlock);
				} catch (UnexpectedEOFException e) {
					log.error("Error while parsing. End-of-file unexpectedly reached with entryBlock: {}", entryBlock);
					break;
				}
			}
		} catch (IOException e) {
			log.error("Error parsing log with BufferedReader", e);
		}

		logEntries = filterAllDuplicates(logEntries);
		logEntries = groupLogsByTimeAndAuthor(logEntries);

		return new LogParseResultWrapper(logEntries, unknownLines);
	}

	/**
	 * Parses the date from a log entry line that starts with "Date:"
	 *
	 * @param line
	 * @param entryBlock
	 * @param unknownLines
	 * @return the parsed Date object, or null if parsing fails.
	 */
	private static Date parseDate(String line, StringBuilder entryBlock, List<String> unknownLines) {
		try {
			return new SimpleDateFormat("yyyy-MM-dd HH:mm")
					.parse(line.substring("Date:".length()).trim());
		} catch (ParseException e) {
			unknownLines.add(entryBlock.toString());
			return null;
		}
	}

	/**
	 * Parses the identity of a log entry.
	 * <p>
	 * The identity line must start with "Identity:" or "User:" and is expected to be in the format:
	 * "Name (OrgUnit) (UserID) (Role)". If the expected format is not met, the block is marked as unknown;
	 * Exception: Role is sometimes optional and still allowed
	 *
	 * @param reader
	 * @param entryBlock
	 * @param unknownLines
	 * @return an Identity object containing the author, userId, and role, or null if the identity is malformed.
	 * @throws IOException
	 */
	private static Identity parseIdentity(BufferedReader reader, StringBuilder entryBlock, List<String> unknownLines) throws IOException {
		String identityLine = reader.readLine();
		// Check if the identity line is missing (EOF reached unexpectedly)
		if (identityLine == null) {
			unknownLines.add(entryBlock.toString());
			throw new UnexpectedEOFException();
		}
		entryBlock.append(identityLine).append("\n");

		// Normalize double-prefix case
		if (identityLine.startsWith("Identity: Identity: automatic")) {
			return new Identity("Identity", "", "system", "automatic");
		}

		String identityData = null;
		// Remove the "Identity/User" prefix and trim any extra spaces
		if (identityLine.startsWith("Identity:")) {
			identityData = identityLine.substring("Identity:".length()).trim();
		} else if (identityLine.startsWith("User:")) {
			identityData = identityLine.substring("User:".length()).trim();
		} else {
			unknownLines.add(entryBlock.toString());
			return null;
		}

		String author = identityData;
		String userId = "";
		String role = "";
		// orgUnit field if needed
		String orgUnit = "";

		int idx = 0;

		List<String> tokens = new ArrayList<>();
		while (true) {
			int open = identityData.indexOf('(', idx);
			int close = identityData.indexOf(')', open);
			if (open < 0 || close < 0) break;

			tokens.add(identityData.substring(open + 1, close).trim());
			idx = close + 1;
		}

		// Map tokens based on count
		if (tokens.size() == 1) {
			userId = tokens.get(0);
		} else if (tokens.size() == 2) {
			userId = tokens.get(0);
			role = tokens.get(1);
		} else if (tokens.size() >= 3) {
			orgUnit = tokens.get(0);
			userId = tokens.get(1);
			role = tokens.get(2);
		}

		// The author is always before first '(' if exists
		if (idx > 0) {
			author = identityData.substring(0, identityData.indexOf('(')).trim();
		}

		return new Identity(author, orgUnit, userId, role);
	}

	/**
	 * Parses the action and details from the log entry.
	 * <p>
	 * This method skips any blank lines, then reads the first non-empty line as the action line.
	 * It extracts the action and initial details using "set to:" or colon as delimiters.
	 * Next, it collects any additional lines (which do not signal a new log entry or a separator)
	 * as potential extra details, but it removes trailing blank lines so that only meaningful text
	 * is appended. Finally, the combined details are trimmed.
	 *
	 * @param reader
	 * @param entryBlock   accumulates the current log entry block.
	 * @param unknownLines list to store unknown or malformed log blocks.
	 * @return an ActionDetails object containing the action and details; or null if parsing fails.
	 * @throws IOException
	 */
	private static ActionDetails parseAction(BufferedReader reader, StringBuilder entryBlock, List<String> unknownLines) throws IOException {
		String actionLine;
		// Skip any empty or whitespace-only lines
		while ((actionLine = reader.readLine()) != null && actionLine.trim().isEmpty()) {
			entryBlock.append(actionLine).append("\n");
		}

		// If no non-empty line is found, record the entry block and throw an EOF exception
		if (actionLine == null) {
			unknownLines.add(entryBlock.toString());
			throw new UnexpectedEOFException();
		}

		// If the line is just a separator (e.g., "-----"), mark this entry as malformed
		if (actionLine.trim().matches("^-{3,}$")) {
			unknownLines.add(entryBlock.toString());
			return null;
		}

		// Append the found action line to the entry block
		entryBlock.append(actionLine).append("\n");

		// Parse the action and initial details from the action line
		ParsedAction parsed = parseActionLine(actionLine);

		// Special handling for "null" cases and "undefined"
		parsed = handleSpecialCases(parsed, actionLine);
		String action = parsed.action;
		String details = parsed.details;

		// Now, gather additional lines that might be part of the details (e.g. Comments have multiple lines)
		List<String> additionalLines = readAdditionalLines(reader, entryBlock);

		// Remove any trailing blank lines from the additional lines list
		while (!additionalLines.isEmpty() && additionalLines.get(additionalLines.size() - 1).trim().isEmpty()) {
			additionalLines.remove(additionalLines.size() - 1);
		}

		// If there are any additional (non-blank) lines, append them to the initial details
		if (!additionalLines.isEmpty()) {
			details = details + "\n" + String.join("\n", additionalLines);
		}

		// Finally, trim any leading or trailing whitespace from the combined details
		details = details.strip();

		// Return a new ActionDetails object if an action was successfully parsed, else null
		return action.isEmpty() ? null : new ActionDetails(action, details);
	}

	/**
	 * Parses the action line to extract the action and initial details
	 *
	 * @param actionLine the first non-empty log line containing the action
	 * @return a ParsedAction object containing the action and initial details
	 */
	private static ParsedAction parseActionLine(String actionLine) {
		String action;
		String details = "";

		// Determine if the delimiter "set to:" is present
		int setToIdx = actionLine.indexOf("set to:");
		if (setToIdx >= 0) {
			// Everything before "set to:" is the action, after is the initial details
			action = actionLine.substring(0, setToIdx + "set to".length()).trim();
			details = actionLine.substring(setToIdx + "set to:".length()).trim();
			return new ParsedAction(action, details);
		} else {
			// If "set to:" is not found, check for the first colon ':'
			int colonIdx = actionLine.indexOf(':');
			if (colonIdx >= 0) {
				action = actionLine.substring(0, colonIdx).trim();
				details = actionLine.substring(colonIdx + 1).trim();

				// special case submit of <filename>: create/upload/edit etc. remains unchanged
				String lowerAction = action.toLowerCase();
				if (lowerAction.startsWith("submit of")) {
					String rest = actionLine.substring("submit of".length()).trim(); // everything after "submit of"
					if (rest.toLowerCase().startsWith("null:")) {
						String extracted = extractFileFromDetail(rest.substring("null:".length()).trim());
						return new ParsedAction("Submit of:", extracted);
					}
					return new ParsedAction("Submit of", rest);
				}

				if (lowerAction.startsWith("revision of")) {
					String rest = actionLine.substring("revision of".length()).trim();
					if (rest.toLowerCase().startsWith("null:")) {
						String extracted = extractFileFromDetail(rest.substring("null:".length()).trim());
						return new ParsedAction("Revision of:", extracted);
					}
					return new ParsedAction("Revision of", rest);
				}

				if (action.startsWith("Deadline extension for assignment of")) {
					// Handle deadline extension special case
					return handleDeadlineExtension(action, details);
				} else if (action.startsWith("Back to submission of")) {
					// Handle back to submission special case
					return handleBackToSubmission(action, details);
				} else {
					// Handle generic case with possible " of " suffix
					return handleGenericSuffix(action, details);
				}
			} else {
				// If no delimiter is present, treat the whole line as the action
				action = actionLine.trim();
				return new ParsedAction(action, details);
			}
		}
	}

	/**
	 * Handles the special case for deadline extension
	 *
	 * @param action  the initial action extracted
	 * @param details the initial details extracted
	 * @return a ParsedAction object with normalized action and details
	 */
	private static ParsedAction handleDeadlineExtension(String action, String details) {
		int ofIdx = action.lastIndexOf("of");
		String userId = "";
		if (ofIdx > 0 && ofIdx + 3 < action.length()) {
			userId = action.substring(ofIdx + 3).trim();
		}
		action = "Deadline extension for assignment"; // normalize

		// Keep the real 'details' for date parsing
		String[] dateParts = details.replace("Standard date", "").trim().split(" to ");
		if (dateParts.length == 2) {
			String oldDate = dateParts[0].trim();
			String newDate = dateParts[1].trim();
			details = "userId=" + userId + ";from=" + oldDate + ";to=" + newDate;
		} else {
			details = "userId=" + userId + ";raw=" + details;
		}
		return new ParsedAction(action, details);
	}

	/**
	 * Handles the special case for "Back to submission" actions
	 *
	 * @param action  the initial action extracted
	 * @param details the initial details extracted
	 * @return a ParsedAction object with normalized action and details
	 */
	private static ParsedAction handleBackToSubmission(String action, String details) {
		// Instead of using lastIndexOf("of"), directly remove the fixed prefix.
		final String prefix = "Back to submission of ";
		if (action.startsWith(prefix)) {
			// Extract filename using the known length of the prefix.
			String filename = action.substring(prefix.length()).trim();
			// Normalize the action to the fixed string.
			action = "Back to submission of";
			// Use the correctly extracted filename as details.
			details = filename;
		}
		return new ParsedAction(action, details);
	}

	/**
	 * Handles generic cases where the action might have an ' of ' suffix
	 *
	 * @param action  the initial action extracted
	 * @param details the initial details extracted
	 * @return a ParsedAction object with possibly modified action and details
	 */
	private static ParsedAction handleGenericSuffix(String action, String details) {
		int lastOfIdx = action.toLowerCase().lastIndexOf(" of ");
		if (lastOfIdx > 0) {
			String possibleAction = action.substring(0, lastOfIdx + 3).trim();
			String possibleSuffix = action.substring(lastOfIdx + 4).trim();
			action = possibleAction;

			// Special case: Always try to take the filename from the possibleSuffix (if present)
			if (!possibleSuffix.isEmpty() && !possibleSuffix.equalsIgnoreCase("null")) {
				details = possibleSuffix;
			}

			// Additional logic: if details still looks like "allow reset" / "revert status" / "submit documents", discard
			if (isNoisyDetail(details)) {
				details = possibleSuffix;
			}
		}
		return new ParsedAction(action, details);
	}

	private static boolean isNoisyDetail(String details) {
		String noise = details.toLowerCase();
		return noise.equals("submit documents")
				|| noise.equals("revert status")
				|| noise.equals("allow reset")
				|| noise.equals("documents reviewed");
	}

	/**
	 * Handles special cases such as "null" values and "undefined" actions
	 *
	 * @param parsed     the ParsedAction object containing action and details
	 * @param actionLine the original action line for reference
	 * @return a ParsedAction object with modifications for special cases
	 */
	private static ParsedAction handleSpecialCases(ParsedAction parsed, String actionLine) {
		String action = parsed.action;
		String details = parsed.details;

		// Special handling for "null" cases
		if (action.equalsIgnoreCase("Submit of null")) {
			action = "Submit of:";
			details = extractFileFromDetail(details);
		} else if (action.equalsIgnoreCase("Revision of null")) {
			action = "Revision of:";
			details = extractFileFromDetail(details);
		} else if (action.equalsIgnoreCase("SCORE set to") && details.equalsIgnoreCase("null")) {
			action = "score reset";
			details = "";
		} else if (actionLine.replace("\"", "").trim().contains("undefined")) {
			// Check if the action (with any quotes removed) equal "undefined". If so, skip this entry.
			action = "passed undefined";
			details = "";
		}
		return new ParsedAction(action, details);
	}

	/**
	 * Reads additional lines from the reader that might be part of the details
	 *
	 * @param reader
	 * @param entryBlock accumulates the current log entry block
	 * @return a list of additional lines
	 * @throws IOException
	 */
	private static List<String> readAdditionalLines(BufferedReader reader, StringBuilder entryBlock) throws IOException {
		List<String> additionalLines = new ArrayList<>();
		reader.mark(1000);  // Mark the current reader position.
		String nextLine = reader.readLine();

		// Continue reading lines until a new entry or separator is encountered
		while (nextLine != null && !nextLine.startsWith("Date:") && !nextLine.trim().matches("^-{3,}$")) {
			additionalLines.add(nextLine);
			entryBlock.append(nextLine).append("\n");
			reader.mark(1000);
			nextLine = reader.readLine();
		}

		// If a line was read that does not belong to the details, reset the reader to that line
		if (nextLine != null) {
			reader.reset();
		}
		return additionalLines;
	}

	private static String extractFileFromDetail(String detailLine) {
		String lowerDetail = detailLine.toLowerCase();
		if (lowerDetail.contains("upload document")) {
			return detailLine.substring(lowerDetail.indexOf("upload document") + "upload document".length()).trim();
		} else if (lowerDetail.contains("delete document")) {
			return detailLine.substring(lowerDetail.indexOf("delete document") + "delete document".length()).trim();
		} else if (lowerDetail.contains("create document")) {
			return detailLine.substring(lowerDetail.indexOf("create document") + "create document".length()).trim();
		} else if (lowerDetail.contains("update document")) {
			return detailLine.substring(lowerDetail.indexOf("update document") + "update document".length()).trim();
		} else if (lowerDetail.contains("edit document")) {
			return detailLine.substring(lowerDetail.indexOf("edit document") + "edit document".length()).trim();
		} else {
			return detailLine; // fallback if detail doesn't match known patterns
		}
	}

	// key is a unique combination of timestamp + author + role, to identify specific entries to group
	private static String generateKey(LogEntry log) {
		return log.timestamp() + "|" + log.author() + "|" + log.role();
	}

	/**
	 * Comma separates each action
	 *
	 * @param existing
	 * @param log
	 * @return merged actions, concatenated by line breaks with \n
	 */
	private static String mergeActions(LogEntry existing, LogEntry log) {
		return existing.action() + "\n" + log.action();
	}

	/**
	 * Comma separates each detail of an action
	 *
	 * @param existing
	 * @param logEntry
	 * @return merged details, concatenated by line breaks with \n
	 */
	private static String mergeDetails(LogEntry existing, LogEntry logEntry) {
		String existingDetails = existing.details();
		String newDetails = logEntry.details();

		if (existingDetails.isEmpty()) {
			existingDetails = "";
		}
		if (newDetails.isEmpty()) {
			newDetails = "";
		}

		return existingDetails + "\n" + newDetails;
	}


	private static LogEntry createMergedLogEntry(LogEntry logEntry, String newAction, String newDetails) {
		return new LogEntry(logEntry.timestamp(), logEntry.author(), logEntry.orgUnit(), logEntry.userId(), logEntry.role(), newAction, newDetails);
	}

	/**
	 * Consumes an optional separator line after a log entry
	 *
	 * @param reader
	 * @param entryBlock
	 * @throws IOException
	 */
	private void consumeSeparator(BufferedReader reader, StringBuilder entryBlock) throws IOException {
		// Mark the current position in the reader; the parameter (1000) is the read-ahead limit
		reader.mark(1000);
		// Read the next line which might be a separator
		String separator = reader.readLine();
		// Check if the line exists and matches the pattern for a separator ("-----")
		if (separator != null && separator.matches("^-{3,}.*")) {
			entryBlock.append(separator).append("\n");
		} else {
			// If not, reset the reader back to the previously marked position so that the line is not lost
			reader.reset();
		}
	}

	public Map<String, List<LogEntry>> groupLogsByTime(List<LogEntry> logEntries) {
		Map<String, List<LogEntry>> groupedLogs = new LinkedHashMap<>();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

		for (LogEntry logEntry : logEntries) {
			String formattedTime = formatter.format(logEntry.timestamp());
			groupedLogs.computeIfAbsent(formattedTime, k -> new ArrayList<>()).add(logEntry);
		}
		return groupedLogs;
	}

	public List<LogEntry> groupLogsByTimeAndAuthor(List<LogEntry> logEntries) {
		Map<String, LogEntry> groupedLogs = new LinkedHashMap<>();

		for (LogEntry logEntry : logEntries) {
			String key = generateKey(logEntry);

			// If the key already exists, it means we have already seen a LogEntry with the same key
			if (groupedLogs.containsKey(key)) {
				LogEntry existing = groupedLogs.get(key);

				// Merge actions and details from same key into one entry
				String newAction = mergeActions(existing, logEntry);
				String newDetails = mergeDetails(existing, logEntry);

				groupedLogs.put(key, createMergedLogEntry(logEntry, newAction, newDetails));
			} else {
				groupedLogs.put(key, logEntry);
			}
		}

		return new ArrayList<>(groupedLogs.values());
	}

	/**
	 * Filters out logEntries by eliminating all duplicates based on key data points,
	 * regardless of their position in the list.
	 * <p>
	 * This method uses a Set to track a unique key generated from the log entry’s
	 * "action" and "details". Only the first occurrence of a particular key is kept.
	 *
	 * @param logEntries
	 * @return a new List of LogEntry objects containing only unique logEntries.
	 */
	public List<LogEntry> filterAllDuplicates(List<LogEntry> logEntries) {
		// If logEntries are null or empty, return immediately -> because nothing to filter
		if (logEntries == null || logEntries.isEmpty()) {
			return logEntries;
		}

		List<LogEntry> uniqueEntries = new ArrayList<>();
		// Use a Set to store a unique key for each entry based on action and details
		Set<String> seenKeys = new HashSet<>();

		// Iterate over each entry
		for (LogEntry entry : logEntries) {
			// Create a key that uniquely represents the important data points
			String key = entry.action() + "|" + entry.details();
			// If this key hasn't been seen yet, add it and include the entry in the result
			if (!seenKeys.contains(key)) {
				seenKeys.add(key);
				uniqueEntries.add(entry);
			}
		}

		return uniqueEntries;
	}

	/**
	 * Helper class to hold the parsed action and details.
	 */
	private record ParsedAction(String action, String details) {
	}

	/**
	 * Helper record to encapsulate identity information from a log entry.
	 */
	private record Identity(String author, String orgUnit, String userId, String role) {
	}

	/**
	 * Helper record to encapsulate the action and details from a log entry.
	 */
	private record ActionDetails(String action, String details) {
	}

	// Custom unchecked exception to signal unexpected end-of-file.
	private static class UnexpectedEOFException extends RuntimeException {
	}
}
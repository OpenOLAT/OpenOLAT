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
package org.olat.modules.ceditor.manager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Pre-processes MkDocs / Python-Markdown style admonitions into
 * GitHub-style blockquote admonitions so they can be handled by the
 * existing {@link MarkdownAdmonitionMapping} infrastructure.
 *
 * Supported input syntax:
 * <pre>
 * !!! type "Optional title"
 *     content line 1
 *     content line 2
 * </pre>
 *
 * Content lines must be indented by at least 4 spaces or one tab.
 * An empty quoted title ({@code ""}) suppresses the default title.
 * Omitting the title falls back to the default (translated type name).
 *
 * Transformed output:
 * <pre>
 * &gt; [!TYPE|Optional title]
 * &gt; content line 1
 * &gt; content line 2
 * </pre>
 *
 * When no title is given: {@code > [!TYPE]}<br>
 * When an empty title is given: {@code > [!TYPE|]}
 *
 * Code fences ({@code ```}) are detected and skipped to avoid
 * touching literal {@code !!!} inside code blocks.
 *
 * Initial date: 2026-04-13<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 */
public class MarkdownMkDocsAdmonitionPreprocessor {

	// Opening line: optional leading spaces (max 3 to not be a code block), !!! type [ "title" ]
	private static final Pattern OPENING_PATTERN = Pattern.compile(
		"^ {0,3}!!!\\s+([A-Za-z][A-Za-z0-9_-]*)(?:\\s+\"([^\"\\r\\n]*)\")?\\s*$");

	// Fence start/end: ``` or ~~~ (at most 3 leading spaces)
	private static final Pattern FENCE_PATTERN = Pattern.compile("^ {0,3}(```+|~~~+)");

	public static String preprocess(String markdown) {
		if (markdown == null || !markdown.contains("!!!")) {
			return markdown;
		}

		String[] lines = markdown.split("\\r?\\n", -1);
		StringBuilder out = new StringBuilder();
		int i = 0;
		String fence = null; // the active fence marker (```, ```` or ~~~) or null

		while (i < lines.length) {
			String line = lines[i];

			// Track fenced code block state — never transform content inside code
			if (fence == null) {
				Matcher fm = FENCE_PATTERN.matcher(line);
				if (fm.find()) {
					fence = fm.group(1).substring(0, 1); // just need the char
					appendLine(out, line, i, lines.length);
					i++;
					continue;
				}
			} else {
				String trimmed = line.stripLeading();
				char fenceChar = fence.charAt(0);
				if (trimmed.startsWith(fence) && trimmed.chars().allMatch(c -> c == fenceChar)) {
					fence = null;
				}
				appendLine(out, line, i, lines.length);
				i++;
				continue;
			}

			Matcher m = OPENING_PATTERN.matcher(line);
			if (m.matches()) {
				String type = m.group(1).toUpperCase();
				String title = m.group(2); // null = no title; "" = empty title marker
				int consumed = emitAdmonition(out, lines, i, type, title);
				i += consumed;
				continue;
			}

			appendLine(out, line, i, lines.length);
			i++;
		}

		return out.toString();
	}

	/**
	 * Emit the transformed blockquote for one admonition starting at the opening
	 * {@code !!!} line, consuming all indented content that follows (but not any
	 * trailing blank lines — those are left for the main loop to handle).
	 *
	 * <p>A blank separator line is appended after the blockquote unconditionally
	 * so that two adjacent admonitions don't collapse into a single CommonMark
	 * blockquote (lazy continuation).
	 *
	 * @return number of source lines consumed (opening line + indented content
	 *         lines + any blank lines interleaved between content lines)
	 */
	private static int emitAdmonition(StringBuilder out, String[] lines, int start,
			String type, String title) {
		// Emit marker line. The title is sanitized to plain text because the
		// admonition title is rendered by the alert-box styling and must not
		// carry any markup (HTML or markdown). Stripping also avoids CommonMark
		// splitting the marker across multiple inline nodes, which would break
		// admonition detection.
		if (title == null) {
			out.append("> [!").append(type).append("]");
		} else {
			out.append("> [!").append(type).append("|").append(sanitizeTitle(title)).append("]");
		}
		out.append('\n');

		int i = start + 1;
		int lastContentIdx = start;
		StringBuilder pending = new StringBuilder(); // buffered blank lines

		while (i < lines.length) {
			String line = lines[i];
			if (line.isBlank()) {
				pending.append("> \n");
				i++;
				continue;
			}
			// Indented by at least 4 spaces or tab?
			int indent = leadingIndent(line);
			if (indent >= 4) {
				// Blank lines that were between content lines belong inside
				// the blockquote — flush them now.
				out.append(pending);
				pending.setLength(0);
				String content = line.startsWith("\t")
					? line.substring(1)
					: line.substring(Math.min(4, line.length()));
				out.append("> ").append(content).append('\n');
				lastContentIdx = i;
				i++;
			} else {
				// Non-indented, non-blank line — block ends before this. Any
				// buffered blank lines are trailing and do NOT belong to the
				// blockquote; leave them for the main loop to re-emit.
				break;
			}
		}

		// Blank separator so a following blockquote-like line (`>` or another
		// admonition we just transformed) is parsed as a separate block.
		out.append('\n');

		return lastContentIdx - start + 1;
	}

	/**
	 * Strip all markup from an admonition title so it is safe to embed in the
	 * generated marker and survives as a single CommonMark Text node.
	 *
	 * Removes HTML tags entirely (including the tag brackets) and strips
	 * CommonMark inline-formatting characters that would otherwise split the
	 * title across multiple AST nodes ({@code `}, {@code *}, {@code _},
	 * {@code [}, {@code ]}, {@code <}, {@code >}, {@code \}, {@code ~},
	 * {@code =}). The result is plain text only.
	 */
	static String sanitizeTitle(String raw) {
		if (raw == null) return null;
		// Strip HTML tags like <b>, <span ...>, </b>
		String s = raw.replaceAll("<[^>]*>", "");
		// Strip CommonMark inline-formatting characters that would split the
		// title across multiple inline nodes or break the admonition regex.
		s = s.replaceAll("[`*_\\[\\]<>\\\\~=]", "");
		return s.strip();
	}

	private static int leadingIndent(String line) {
		int n = 0;
		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			if (c == ' ') {
				n++;
			} else if (c == '\t') {
				return 4;
			} else {
				break;
			}
		}
		return n;
	}

	/** Append a line to the output with a terminating newline unless it is the final element. */
	private static void appendLine(StringBuilder out, String line, int index, int total) {
		out.append(line);
		if (index < total - 1) {
			out.append('\n');
		}
	}

	private MarkdownMkDocsAdmonitionPreprocessor() {
		// utility class
	}
}

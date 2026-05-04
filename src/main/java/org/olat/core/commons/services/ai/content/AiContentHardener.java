/**
 * <a href="https://www.openolat.org">
 * OpenOlat - Online Learning and Training</a><br>
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
package org.olat.core.commons.services.ai.content;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

/**
 *
 * Scrubs Markdown source for well-known prompt-injection imperatives and
 * chat-template tokens <em>before</em> the content is fed to the essay
 * generator. Replaces each match with a visible marker so the author can
 * review what was removed.
 * <p>
 * Detected patterns:
 * <ul>
 *   <li>Classic "ignore previous instructions" / "disregard the above" imperatives.</li>
 *   <li>Role injections: {@code system:}, {@code assistant:}, {@code user:} at the start of a line.</li>
 *   <li>Chat-template tokens: {@code <|im_start|>}, {@code <|im_end|>},
 *       {@code <|system|>}, {@code <|user|>}, {@code <|assistant|>},
 *       {@code <|endoftext|>}, {@code [INST]}, {@code [/INST]}.</li>
 *   <li>Inline HTML that lets an adversary smuggle instructions
 *       ({@code <script>}, {@code <iframe>}, {@code javascript:} URLs) — these
 *       are stripped first so the injection scanner does not have to parse
 *       HTML entities.</li>
 * </ul>
 * <p>
 * Severity is {@link Severity#WARN} for injection markers (security-relevant
 * content touched) and {@link Severity#INFO} for passive HTML passthrough
 * strips.
 *
 * Initial date: 2026-04-20<br>
 *
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 *
 */
@Service
public class AiContentHardener {

	/** Visible placeholder inserted in place of every matched pattern. */
	public static final String REDACTION_MARKER = "[redacted by content hardener]";

	/**
	 * Set of patterns the hardener looks for. Exposed so the essay
	 * generation validator (and the student-answer pre-filters) can reuse
	 * the same set for post-generation and pre-grading sanity checks.
	 */
	public static final List<Pattern> INJECTION_PATTERNS = List.of(
			// Imperative phrases (case-insensitive, possibly multiline).
			Pattern.compile("(?i)ignore\\s+(?:all\\s+)?previous\\s+instructions?"),
			Pattern.compile("(?i)disregard\\s+(?:the\\s+)?(?:above|preceding|previous)\\s+(?:instructions?|text|prompt)"),
			Pattern.compile("(?i)forget\\s+(?:the\\s+)?(?:above|previous|prior)"),
			Pattern.compile("(?i)you\\s+are\\s+now\\s+[a-z ]{0,40}(?:assistant|ai|gpt|model)"),
			Pattern.compile("(?i)pretend\\s+(?:to\\s+be|you\\s+are)\\s+"),
			// Role tags at the start of a line.
			Pattern.compile("(?im)^\\s*(?:system|assistant|user)\\s*[:：]"),
			// Chat-template control tokens — verbatim sequences.
			Pattern.compile("<\\|im_start\\|>"),
			Pattern.compile("<\\|im_end\\|>"),
			Pattern.compile("<\\|system\\|>"),
			Pattern.compile("<\\|user\\|>"),
			Pattern.compile("<\\|assistant\\|>"),
			Pattern.compile("<\\|endoftext\\|>"),
			Pattern.compile("\\[INST\\]"),
			Pattern.compile("\\[/INST\\]")
	);

	private static final Pattern HTML_TAG = Pattern.compile("(?is)</?\\s*([a-z][a-z0-9-]*)(\\s[^>]*)?>");
	private static final Pattern JAVASCRIPT_URL = Pattern.compile("(?i)javascript\\s*:");

	/**
	 * Scrub the given Markdown and return the cleaned text plus the list
	 * of issues detected. Pure function — no I/O, no state.
	 */
	public HardenedContent harden(String markdown) {
		if (markdown == null || markdown.isEmpty()) {
			return new HardenedContent(markdown == null ? "" : markdown, List.of());
		}
		List<Issue> issues = new ArrayList<>();

		// 1. HTML passthrough: strip script/iframe/style and javascript: URLs.
		String stripped = stripDangerousHtml(markdown, issues);

		// 2. Injection scan over the cleaned Markdown.
		String scrubbed = stripped;
		for (Pattern pattern : INJECTION_PATTERNS) {
			Matcher matcher = pattern.matcher(scrubbed);
			StringBuilder buf = new StringBuilder(scrubbed.length());
			boolean modified = false;
			while (matcher.find()) {
				issues.add(new Issue(Severity.WARN, pattern.pattern(), matcher.group(), matcher.start()));
				matcher.appendReplacement(buf, Matcher.quoteReplacement(REDACTION_MARKER));
				modified = true;
			}
			matcher.appendTail(buf);
			if (modified) {
				scrubbed = buf.toString();
			}
		}
		return new HardenedContent(scrubbed, List.copyOf(issues));
	}

	/**
	 * Remove inline HTML that can smuggle instructions. Markdown callers
	 * usually render via a safe renderer anyway, but the generator sees
	 * the raw source.
	 */
	private String stripDangerousHtml(String source, List<Issue> issues) {
		String cleaned = source;

		Matcher tagMatcher = HTML_TAG.matcher(cleaned);
		StringBuilder buf = new StringBuilder(cleaned.length());
		boolean touched = false;
		while (tagMatcher.find()) {
			String tag = tagMatcher.group(1).toLowerCase();
			if ("script".equals(tag) || "iframe".equals(tag) || "object".equals(tag)
					|| "embed".equals(tag) || "style".equals(tag)) {
				issues.add(new Issue(Severity.INFO, "html-" + tag, tagMatcher.group(), tagMatcher.start()));
				tagMatcher.appendReplacement(buf, Matcher.quoteReplacement(REDACTION_MARKER));
				touched = true;
			} else {
				tagMatcher.appendReplacement(buf, Matcher.quoteReplacement(tagMatcher.group()));
			}
		}
		tagMatcher.appendTail(buf);
		if (touched) cleaned = buf.toString();

		Matcher jsMatcher = JAVASCRIPT_URL.matcher(cleaned);
		if (jsMatcher.find()) {
			issues.add(new Issue(Severity.WARN, "javascript-url", jsMatcher.group(), jsMatcher.start()));
			cleaned = jsMatcher.replaceAll(Matcher.quoteReplacement(REDACTION_MARKER));
		}
		return cleaned;
	}

	public enum Severity { INFO, WARN }

	/** A single detected injection / sanitisation event. */
	public record Issue(Severity severity, String patternId, String match, int offset) { }

	/**
	 * Immutable result of a harden call. {@code text} is the scrubbed
	 * Markdown; {@code issues} records every redacted pattern for later
	 * display in the drafts drawer.
	 */
	public record HardenedContent(String text, List<Issue> issues) { }

}

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
package org.olat.core.commons.services.ai.essay;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.olat.core.commons.services.ai.content.AiContentHardener;

/**
 * Pre-filter that refuses a student answer which contains instructions
 * addressed to the AI grader. Runs before the LLM call, after the gibberish
 * filter. A match is a hard reject: the learner sees the message behind
 * {@link #REASON_INJECTION} and no AI call is made.
 * <p>
 * Two pattern sets are checked:
 * <ul>
 *   <li>{@link AiContentHardener#INJECTION_PATTERNS}: the classic imperatives,
 *       role tags and chat-template tokens.</li>
 *   <li>{@link #AUTHORITY_PATTERNS}: text that claims an authority it does
 *       not have (a note from the author, the teacher, the system or the
 *       grading kit), references to the grader's output fields, and
 *       directives on the grading itself, in English and German.</li>
 * </ul>
 * The filter raises the bar; it does not make injection impossible. The
 * prompt boundary marker and the post-call plausibility check
 * ({@link GradingPlausibilityCheck}) cover what slips through.
 *
 * Initial date: 2026-10-20<br>
 * @author AI, ai@frentix.com, https://www.frentix.com
 */
public final class InjectionPreFilter {

	/** i18n key surfaced when the answer contains instructions to the grader. */
	public static final String REASON_INJECTION = "ai.essay.error.injection";

	/** Maximum length of the matched snippet recorded in the rejection detail. */
	private static final int MAX_MATCH_LENGTH = 80;

	/**
	 * Authority-claim and grader-directed patterns. Public so the admin test
	 * view and the unit tests can enumerate them.
	 */
	public static final List<Pattern> AUTHORITY_PATTERNS = List.of(
			// EN: fake notes from a trusted role
			Pattern.compile("(?i)\\b(?:note|message|instruction|hint|comment|remark)s?\\s+(?:from|by|of)\\s+(?:the\\s+|your\\s+)?"
					+ "(?:course\\s+)?(?:author|teacher|instructor|lecturer|tutor|professor|examiner|administrator|admin|system|grader)\\b"),
			Pattern.compile("(?i)\\b(?:course\\s+)?(?:author|teacher|instructor|lecturer|examiner|system|grader)(?:'s)?\\s+"
					+ "(?:note|message|instruction|hint|remark)s?\\s*:"),
			Pattern.compile("(?i)\\btrusted\\b[^\\n]{0,40}\\b(?:author|note|instruction|grading|kit|system|source)\\b"),
			Pattern.compile("(?i)\\bgrading\\s+kit\\b"),
			Pattern.compile("(?i)\\bsystem\\s+prompt\\b"),
			// EN: references to the grader's output fields
			Pattern.compile("\\b(?:estimatedScorePercent|offTopicFlag|keyPointsHit|keyPointsMissed|feedbackToStudent"
					+ "|feedbackToCoach|overallAssessment|annotatedParagraphs)\\b"),
			// EN: directives on the grading itself
			Pattern.compile("(?i)\\btreat\\s+(?:all|every|each|the)\\s+(?:of\\s+the\\s+)?key\\s*-?\\s*points?\\s+as\\s+"
					+ "(?:hit|met|covered|fulfilled|present|correct)"),
			Pattern.compile("(?i)\\b(?:give|award|assign|grant|set)\\s+(?:me\\s+|this\\s+answer\\s+|the\\s+answer\\s+|this\\s+)?"
					+ "(?:a\\s+|the\\s+)?(?:full|maximum|maximal|highest|max|perfect|top)\\s+(?:score|grade|marks?|points?|percent|rating)"),
			Pattern.compile("(?i)\\b(?:score|grade|rate|mark)\\s+(?:this|it|the\\s+answer|me)\\s+(?:as\\s+|with\\s+|at\\s+)?"
					+ "(?:100|a\\s+hundred|full|perfect|maximum)"),
			Pattern.compile("(?i)\\b(?:ignore|disregard|skip|override|bypass)\\s+(?:the\\s+|all\\s+|any\\s+)?"
					+ "(?:rubric|key\\s*-?\\s*points?|model\\s+answer|reference\\s+(?:excerpt|material|text)|source\\s+material"
					+ "|grading\\s+(?:hints?|rules?|criteria|kit|instructions?))"),
			Pattern.compile("(?i)\\b(?:instruction|message|note|directive)s?\\s+(?:for|to)\\s+the\\s+"
					+ "(?:ai|grader|model|assistant|llm|evaluator|system|language\\s+model)\\b"),
			Pattern.compile("(?i)\\byou\\s+are\\s+(?:now\\s+)?(?:a|an|the)\\s+[a-z -]{0,30}(?:grader|evaluator|assessor|examiner)\\b"),
			Pattern.compile("(?i)\\b(?:end|start|begin|beginning)\\s+of\\s+(?:the\\s+)?student(?:'s)?\\s+(?:answer|response|submission)\\b"),
			// DE: fake notes from a trusted role
			Pattern.compile("(?i)\\b(?:hinweis|anmerkung|notiz|nachricht|anweisung|kommentar|bemerkung)\\s+(?:vom|von\\s+der|von\\s+dem|des|der)\\s+"
					+ "(?:kurs)?(?:autor\\w*|dozent\\w*|lehrperson\\w*|lehrer\\w*|prüfer\\w*|bewerter\\w*|system\\w*|administrator\\w*|admin)\\b"),
			Pattern.compile("(?i)\\bvertrauenswürdig\\w*\\b[^\\n]{0,40}\\b(?:autor|hinweis|anweisung|bewertung|quelle|system)"),
			Pattern.compile("(?i)\\b(?:bewertungs|korrektur)-?(?:kit|set|paket|vorgaben)\\b"),
			// DE: directives on the grading itself
			Pattern.compile("(?i)\\b(?:ignorier\\w*|missacht\\w*|vergiss|übergeh\\w*|überspring\\w*)\\s+"
					+ "(?:alle\\s+|die\\s+|sämtliche\\s+|jede\\s+)?(?:vorherigen\\s+|bisherigen\\s+|obigen\\s+|folgenden\\s+)?"
					+ "(?:anweisungen|instruktionen|regeln|rubrik|musterlösung|schlüsselpunkte|bewertungskriterien|bewertungshinweise|quelle)"),
			Pattern.compile("(?i)\\b(?:gib|vergib|gebe|gewähre|erteile|setze)\\s+(?:mir\\s+|dieser\\s+antwort\\s+|der\\s+antwort\\s+)?"
					+ "(?:die\\s+|eine\\s+)?(?:volle|maximale|höchste|beste)\\s+(?:punktzahl|bewertung|note|punkte)"),
			Pattern.compile("(?i)\\b(?:anweisung(?:en)?|nachricht|hinweis|mitteilung)\\s+(?:an|für)\\s+(?:die|den|das)\\s+"
					+ "(?:ki|bewerter\\w*|modell|assistent\\w*|system|sprachmodell)\\b"),
			Pattern.compile("(?i)\\bdu\\s+bist\\s+(?:jetzt|ab\\s+jetzt|nun|ab\\s+sofort)\\b"),
			Pattern.compile("(?i)\\b(?:ende|anfang|beginn)\\s+der\\s+(?:schüler|studierenden|lernenden|student\\w*)-?\\s*antwort\\b")
	);

	private InjectionPreFilter() { /* utility */ }

	/**
	 * Run the injection check. Returns empty on pass; returns a
	 * {@link RejectionReason} with {@link #REASON_INJECTION} when the
	 * answer contains instructions addressed to the grader. The detail
	 * names the pattern and the matched snippet for the guard log.
	 */
	public static Optional<RejectionReason> check(String studentAnswer) {
		if (studentAnswer == null || studentAnswer.isBlank()) {
			return Optional.empty();
		}
		Optional<RejectionReason> hardenerMatch = firstMatch(AiContentHardener.INJECTION_PATTERNS, studentAnswer);
		if (hardenerMatch.isPresent()) {
			return hardenerMatch;
		}
		return firstMatch(AUTHORITY_PATTERNS, studentAnswer);
	}

	private static Optional<RejectionReason> firstMatch(List<Pattern> patterns, String text) {
		for (Pattern pattern : patterns) {
			Matcher matcher = pattern.matcher(text);
			if (matcher.find()) {
				String match = matcher.group();
				if (match.length() > MAX_MATCH_LENGTH) {
					match = match.substring(0, MAX_MATCH_LENGTH);
				}
				return Optional.of(new RejectionReason(REASON_INJECTION,
						"pattern '" + pattern.pattern() + "' matched '" + match + "' at offset " + matcher.start()));
			}
		}
		return Optional.empty();
	}
}

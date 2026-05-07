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
package org.olat.modules.ceditor.ui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.commons.services.ai.essay.AnnotatedParagraph;
import org.olat.core.commons.services.ai.essay.AnnotatedSpan;
import org.olat.core.commons.services.ai.essay.FormativeFeedback;
import org.olat.core.commons.services.ai.essay.GradingSuggestion;
import org.olat.core.commons.services.ai.essay.MarkKind;
import org.olat.core.commons.services.ai.essay.RejectionReason;
import org.olat.core.gui.translator.Translator;

/**
 *
 * Flatten a {@link FormativeFeedback} value into a plain
 * {@code Map<String,Object>} shape the quiz_run.html Velocity template
 * can render without calling enum methods in {@code #foreach} loops.
 * Strips administrator-only fields (corrector-oriented notes, coach
 * feedback, internal SPI identifiers) so only student-relevant content
 * reaches the learner UI.
 * <p>
 * Returns {@code null} if the feedback is unusable (null input or empty
 * payload) so the caller can fall through to the error branch.
 *
 * Initial date: 2026-04-20<br>
 *
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 *
 */
final class AiEssayFeedbackViewFlattener {

	private AiEssayFeedbackViewFlattener() {
		// utility
	}

	static Map<String, Object> flatten(FormativeFeedback feedback, Translator translator) {
		if (feedback == null) {
			return null;
		}
		Map<String, Object> v = new LinkedHashMap<>();
		v.put("type", feedback.type() == null ? "" : feedback.type().name());

		if (feedback.type() == FormativeFeedback.Type.REJECTED
				|| feedback.type() == FormativeFeedback.Type.REFUSED_LONG) {
			RejectionReason r = feedback.rejection();
			if (r != null) {
				v.put("rejectionMessageKey", nullToEmpty(r.messageKey()));
				String localized = translator == null || r.messageKey() == null
						? nullToEmpty(r.detail()) : translator.translate(r.messageKey());
				v.put("rejectionMessage", localized);
			}
			return v;
		}

		GradingSuggestion s = feedback.suggestion();
		if (s == null) {
			return null;
		}

		v.put("overallAssessment", nullToEmpty(s.overallAssessment()));
		v.put("offTopicFlag", s.offTopicFlag() == null ? "" : s.offTopicFlag().name());
		String confidenceLabel = "";
		String confidenceClass = "";
		if (s.confidence() != null) {
			String key = "ai.essay.correction.confidence." + s.confidence().name();
			confidenceLabel = translator == null ? s.confidence().name() : translator.translate(key);
			confidenceClass = confidenceClass(s.confidence());
		}
		v.put("confidence", confidenceLabel);
		v.put("confidenceClass", confidenceClass);

		// Assessment bucket derived from the model's self-estimated score.
		// Empty string when the percent is out of the [0,100] range so the
		// template can guard with a simple non-empty check.
		int percent = s.estimatedScorePercent();
		v.put("assessmentLabel", buildAssessmentLabel(percent, translator));
		v.put("assessmentClass", assessmentClass(percent));

		// Student-facing feedback block
		GradingSuggestion.StudentFeedback fb = s.feedbackToStudent();
		Map<String, String> fv = new LinkedHashMap<>();
		if (fb == null) {
			fv.put("whatWentWell", "");
			fv.put("whatIsMissing", "");
			fv.put("nextStep", "");
		} else {
			fv.put("whatWentWell", nullToEmpty(fb.whatWentWell()));
			fv.put("whatIsMissing", nullToEmpty(fb.whatIsMissing()));
			fv.put("nextStep", nullToEmpty(fb.nextStep()));
		}
		v.put("feedbackToStudent", fv);

		// Content signals — keep only the student-relevant key-point lists.
		// relevanceNote / coherenceNote / argumentNote are corrector-oriented
		// and intentionally dropped here.
		GradingSuggestion.ContentSignals c = s.contentSignals();
		List<Map<String, String>> keyPointsHit = new ArrayList<>();
		List<Map<String, String>> keyPointsMissed = new ArrayList<>();
		if (c != null) {
			if (c.keyPointsHit() != null) {
				for (GradingSuggestion.KeyPointHit h : c.keyPointsHit()) {
					Map<String, String> m = new LinkedHashMap<>();
					m.put("id", nullToEmpty(h.id()));
					m.put("evidence", nullToEmpty(h.evidence()));
					keyPointsHit.add(m);
				}
			}
			if (c.keyPointsMissed() != null) {
				for (GradingSuggestion.KeyPointMissed m : c.keyPointsMissed()) {
					Map<String, String> row = new LinkedHashMap<>();
					row.put("id", nullToEmpty(m.id()));
					row.put("reason", nullToEmpty(m.reason()));
					keyPointsMissed.add(row);
				}
			}
		}
		v.put("keyPointsHit", keyPointsHit);
		v.put("keyPointsMissed", keyPointsMissed);

		// Language signals — collapsed, student can expand on click.
		GradingSuggestion.LanguageSignals l = s.languageSignals();
		List<Map<String, String>> grammarIssues = new ArrayList<>();
		List<Map<String, String>> spellingIssues = new ArrayList<>();
		if (l != null) {
			if (l.grammarIssues() != null) {
				for (GradingSuggestion.GrammarIssue g : l.grammarIssues()) {
					Map<String, String> m = new LinkedHashMap<>();
					m.put("span", nullToEmpty(g.span()));
					m.put("note", nullToEmpty(g.note()));
					m.put("severity", g.severity() == null ? "" : g.severity().name());
					grammarIssues.add(m);
				}
			}
			if (l.spellingIssues() != null) {
				for (GradingSuggestion.SpellingIssue sp : l.spellingIssues()) {
					Map<String, String> m = new LinkedHashMap<>();
					m.put("word", nullToEmpty(sp.word()));
					m.put("suggestion", nullToEmpty(sp.suggestion()));
					spellingIssues.add(m);
				}
			}
		}
		v.put("grammarIssues", grammarIssues);
		v.put("spellingIssues", spellingIssues);

		// Non-fatal warnings emitted by the pre-filter (e.g. language mismatch)
		List<Map<String, String>> warnings = new ArrayList<>();
		if (feedback.warnings() != null) {
			for (RejectionReason w : feedback.warnings()) {
				if (w == null) continue;
				Map<String, String> m = new LinkedHashMap<>();
				m.put("messageKey", nullToEmpty(w.messageKey()));
				m.put("message", translator == null || w.messageKey() == null
						? nullToEmpty(w.detail()) : translator.translate(w.messageKey()));
				warnings.add(m);
			}
		}
		v.put("warnings", warnings);

		// Annotated paragraphs for the direct inline view.
		// Each entry: { "feedback": String, "spans": List<Map> }
		// Each span:  { "text": String, "kind": String (lowercase), "comment": String, "cssClass": String }
		List<Map<String, Object>> annotatedParagraphs = new ArrayList<>();
		List<AnnotatedParagraph> apList = s.annotatedParagraphs();
		if (apList != null) {
			for (AnnotatedParagraph ap : apList) {
				if (ap == null) continue;
				Map<String, Object> paraMap = new LinkedHashMap<>();
				paraMap.put("feedback", nullToEmpty(ap.paragraphFeedback()));
				List<Map<String, String>> spanMaps = new ArrayList<>();
				if (ap.spans() != null) {
					for (AnnotatedSpan span : ap.spans()) {
						if (span == null) continue;
						Map<String, String> sm = new LinkedHashMap<>();
						sm.put("text", nullToEmpty(span.text()));
						String kindStr = span.kind() == null ? MarkKind.NEUTRAL.name().toLowerCase()
								: span.kind().name().toLowerCase();
						sm.put("kind", kindStr);
						sm.put("comment", nullToEmpty(span.comment()));
						sm.put("cssClass", markCssClass(span.kind()));
						spanMaps.add(sm);
					}
				}
				paraMap.put("spans", spanMaps);
				annotatedParagraphs.add(paraMap);
			}
		}
		v.put("annotatedParagraphs", annotatedParagraphs);

		return v;
	}

	/**
	 * CSS class for an inline mark span.
	 * NEUTRAL maps to an empty string (plain text, no badge styling).
	 */
	static String markCssClass(MarkKind kind) {
		if (kind == null) return "";
		return switch (kind) {
			case CORRECT -> "o_ai_mark_correct";
			case AMBIGUOUS -> "o_ai_mark_ambiguous";
			case WRONG -> "o_ai_mark_wrong";
			case NEUTRAL -> "";
		};
	}

	private static String nullToEmpty(String s) {
		return s == null ? "" : s;
	}

	/**
	 * Map a 0..100 estimated score percent to a coarse bucket key and produce
	 * a label of the form "<assessment-prefix>: <bucket>". When the percent
	 * is out of range (negative or above 100) an empty string is returned so
	 * the caller can hide the badge.
	 */
	static String buildAssessmentLabel(int estimatedScorePercent, Translator translator) {
		String bucketKey = bucketKey(estimatedScorePercent);
		if (bucketKey == null) {
			return "";
		}
		if (translator == null) {
			return bucketKey;
		}
		String prefix = translator.translate("ai.essay.correction.assessment");
		String bucket = translator.translate(bucketKey);
		return prefix + ": " + bucket;
	}

	/**
	 * Bucket boundaries (inclusive lower, inclusive upper):
	 * <ul>
	 *   <li>85..100 → ai.essay.correction.assessment.verygood</li>
	 *   <li>70..84  → ai.essay.correction.assessment.good</li>
	 *   <li>50..69  → ai.essay.correction.assessment.mediocre</li>
	 *   <li>25..49  → ai.essay.correction.assessment.insufficient</li>
	 *   <li>0..24   → ai.essay.correction.assessment.wrong</li>
	 * </ul>
	 * Returns {@code null} for values outside [0,100].
	 */
	static String bucketKey(int percent) {
		if (percent < 0 || percent > 100) {
			return null;
		}
		if (percent >= 85) {
			return "ai.essay.correction.assessment.verygood";
		}
		if (percent >= 70) {
			return "ai.essay.correction.assessment.good";
		}
		if (percent >= 50) {
			return "ai.essay.correction.assessment.mediocre";
		}
		if (percent >= 25) {
			return "ai.essay.correction.assessment.insufficient";
		}
		return "ai.essay.correction.assessment.wrong";
	}

	/**
	 * Bootstrap label class colour mapping for the confidence badge:
	 * HIGH → info (blue), MEDIUM → default (grey), LOW → warning (yellow).
	 */
	static String confidenceClass(GradingSuggestion.Confidence confidence) {
		if (confidence == null) return "";
		return switch (confidence) {
			case HIGH -> "label-info";
			case MEDIUM -> "label-default";
			case LOW -> "label-warning";
		};
	}

	/**
	 * Bootstrap label class colour mapping for the assessment badge:
	 * very good → success (green), gut/mittelmässig → info (blue),
	 * ungenügend → warning (yellow), falsch → danger (red).
	 * Empty when out of [0,100].
	 */
	static String assessmentClass(int percent) {
		if (percent < 0 || percent > 100) return "";
		if (percent >= 85) return "label-success";
		if (percent >= 50) return "label-info";
		if (percent >= 25) return "label-warning";
		return "label-danger";
	}
}

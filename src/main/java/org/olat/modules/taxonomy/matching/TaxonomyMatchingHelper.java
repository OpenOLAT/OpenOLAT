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
package org.olat.modules.taxonomy.matching;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.services.ai.AiModule;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.modules.taxonomy.TaxonomyRef;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.matching.model.TaxonomyMatch;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;

/**
 * Shared helper for AI-generated subject-to-taxonomy matching.
 * <p>
 * When taxonomy matching is enabled in {@link AiModule} the embedding-based
 * {@link TaxonomyMatchingService} is used. Otherwise falls back to the legacy
 * case-insensitive exact-match on the translated display name or identifier.
 *
 * Initial date: 2026-06-05<br>
 * @author uhensler, https://www.frentix.com
 */
public class TaxonomyMatchingHelper {

	private TaxonomyMatchingHelper() {
	}

	/**
	 * Find taxonomy levels matching the given subject text.
	 *
	 * @param subject          free-text subject from the AI generator
	 * @param taxonomyRefs     taxonomies to search within
	 * @param taxonomyService  for loading levels (legacy path)
	 * @param matchingService  the embedding matching service (may be null when disabled)
	 * @param aiModule         provides the enabled-flag and min-score
	 * @param locale           locale for legacy translated-name matching
	 * @return matched taxonomy level refs, ordered best-first; never null
	 */
	public static List<TaxonomyLevelRef> matchTaxonomyLevels(String subject,
			Collection<? extends TaxonomyRef> taxonomyRefs,
			TaxonomyService taxonomyService,
			TaxonomyMatchingService matchingService,
			AiModule aiModule,
			Locale locale) {

		if (!StringHelper.containsNonWhitespace(subject) || taxonomyRefs == null || taxonomyRefs.isEmpty()) {
			return List.of();
		}

		if (aiModule != null && aiModule.isTaxonomyMatchingEnabled() && matchingService != null) {
			return embeddingMatch(subject, taxonomyRefs, matchingService, aiModule);
		}
		return legacyMatch(subject, taxonomyRefs, taxonomyService, locale);
	}

	private static List<TaxonomyLevelRef> embeddingMatch(String subject,
			Collection<? extends TaxonomyRef> taxonomyRefs,
			TaxonomyMatchingService matchingService,
			AiModule aiModule) {
		double minScore = aiModule.getTaxonomyMatchingMinScore();
		List<TaxonomyMatch> allMatches = new ArrayList<>();
		for (TaxonomyRef ref : taxonomyRefs) {
			allMatches.addAll(matchingService.suggestLevels(subject, ref, 3, minScore));
		}
		if (allMatches.isEmpty()) {
			return List.of();
		}
		return allMatches.stream()
				.sorted()
				.map(TaxonomyMatch::level)
				.map(TaxonomyLevelRef.class::cast)
				.toList();
	}

	private static List<TaxonomyLevelRef> legacyMatch(String subject,
			Collection<? extends TaxonomyRef> taxonomyRefs,
			TaxonomyService taxonomyService,
			Locale locale) {
		String subjectLower = subject.trim().toLowerCase();
		List<TaxonomyLevel> levels = taxonomyService.getTaxonomyLevels(taxonomyRefs);
		Translator translator = buildTranslator(locale);
		for (TaxonomyLevel level : levels) {
			if (translator != null) {
				String displayName = TaxonomyUIFactory.translateDisplayName(translator, level);
				if (displayName != null && subjectLower.equals(displayName.trim().toLowerCase())) {
					return List.of(level);
				}
			}
			String identifier = level.getIdentifier();
			if (identifier != null && subjectLower.equals(identifier.trim().toLowerCase())) {
				return List.of(level);
			}
		}
		return List.of();
	}

	private static Translator buildTranslator(Locale locale) {
		if (locale == null) {
			return null;
		}
		try {
			return Util.createPackageTranslator(TaxonomyUIFactory.class, locale);
		} catch (Exception e) {
			return null;
		}
	}
}

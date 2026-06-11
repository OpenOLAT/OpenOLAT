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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Locale;

import org.junit.Test;
import org.olat.core.commons.services.ai.AiModule;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.modules.taxonomy.TaxonomyRef;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.matching.model.TaxonomyMatch;

/**
 * Unit tests for {@link TaxonomyMatchingHelper}.
 *
 * Initial date: 2026-06-05<br>
 * @author uhensler, https://www.frentix.com
 */
public class TaxonomyMatchingHelperTest {

	@Test
	public void shouldMatchWithEmbeddingWhenEnabled() {
		TaxonomyRef taxonomy = mock(TaxonomyRef.class);
		TaxonomyLevel level = mock(TaxonomyLevel.class);
		TaxonomyMatch match = new TaxonomyMatch(level, 0.85, "de", "Biologie", false);

		TaxonomyMatchingService matchingService = mock(TaxonomyMatchingService.class);
		when(matchingService.suggestLevels(eq("Biologie"), eq(taxonomy), anyInt(), anyDouble()))
				.thenReturn(List.of(match));

		AiModule aiModule = mock(AiModule.class);
		when(aiModule.isTaxonomyMatchingEnabled()).thenReturn(true);
		when(aiModule.getTaxonomyMatchingMinScore()).thenReturn(0.65);

		TaxonomyService taxonomyService = mock(TaxonomyService.class);

		List<TaxonomyLevelRef> result = TaxonomyMatchingHelper.matchTaxonomyLevels(
				"Biologie", List.of(taxonomy), taxonomyService, matchingService, aiModule, Locale.GERMAN);

		assertThat(result).hasSize(1);
		assertThat(result.get(0)).isEqualTo(level);
		verify(taxonomyService, never()).getTaxonomyLevels(any(List.class));
	}

	@Test
	public void shouldFallBackToSubstringWhenDisabled() {
		TaxonomyRef taxonomy = mock(TaxonomyRef.class);
		when(taxonomy.getKey()).thenReturn(1L);
		TaxonomyLevel level = mock(TaxonomyLevel.class);
		when(level.getIdentifier()).thenReturn("Biology");

		TaxonomyMatchingService matchingService = mock(TaxonomyMatchingService.class);

		AiModule aiModule = mock(AiModule.class);
		when(aiModule.isTaxonomyMatchingEnabled()).thenReturn(false);

		TaxonomyService taxonomyService = mock(TaxonomyService.class);
		when(taxonomyService.getTaxonomyLevels(any(java.util.Collection.class))).thenReturn(List.of(level));

		List<TaxonomyLevelRef> result = TaxonomyMatchingHelper.matchTaxonomyLevels(
				"biology", List.of(taxonomy), taxonomyService, matchingService, aiModule, Locale.ENGLISH);

		assertThat(result).hasSize(1);
		assertThat(result.get(0)).isEqualTo(level);
		verify(matchingService, never()).suggestLevels(any(), any(), anyInt(), anyDouble());
	}

	@Test
	public void shouldReturnEmptyWhenNoMatch() {
		TaxonomyRef taxonomy = mock(TaxonomyRef.class);

		TaxonomyMatchingService matchingService = mock(TaxonomyMatchingService.class);
		when(matchingService.suggestLevels(any(), any(), anyInt(), anyDouble()))
				.thenReturn(List.of());

		AiModule aiModule = mock(AiModule.class);
		when(aiModule.isTaxonomyMatchingEnabled()).thenReturn(true);
		when(aiModule.getTaxonomyMatchingMinScore()).thenReturn(0.9);

		TaxonomyService taxonomyService = mock(TaxonomyService.class);

		List<TaxonomyLevelRef> result = TaxonomyMatchingHelper.matchTaxonomyLevels(
				"very obscure subject", List.of(taxonomy), taxonomyService, matchingService, aiModule, Locale.ENGLISH);

		assertThat(result).isEmpty();
	}
}

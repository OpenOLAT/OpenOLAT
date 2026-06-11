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
package org.olat.modules.taxonomy.matching.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.junit.Test;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.matching.TaxonomyEmbeddingTextVariant;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;

/**
 * Unit tests for {@link TaxonomyEmbeddingTextBuilder}.
 * Uses mocks — no Spring context required.
 *
 * Initial date: 2026-06-05<br>
 * @author uhensler, https://www.frentix.com
 */
public class TaxonomyEmbeddingTextBuilderTest {

	@Test
	public void shouldBuildTextWithTranslatedPath() {
		TaxonomyLevel sciences = mockLevel("Sciences", null);
		TaxonomyLevel biology = mockLevel("Biology", sciences);
		TaxonomyLevel plantBiology = mockLevel("PlantBiology", biology);

		Translator translator = mock(Translator.class);
		when(translator.getLocale()).thenReturn(Locale.ENGLISH);
		mockTranslation(translator, sciences, "Sciences");
		mockTranslation(translator, biology, "Biology");
		mockTranslation(translator, plantBiology, "Plant Biology");

		String text = TaxonomyEmbeddingTextBuilder.build(plantBiology, translator, null, TaxonomyEmbeddingTextVariant.PATH_NAME);

		assertThat(text).isEqualTo("Sciences, Biology, Plant Biology");
	}

	@Test
	public void shouldIncludeDescription() {
		TaxonomyLevel biology = mockLevel("Biology", null);
		TaxonomyLevel plantBiology = mockLevel("PlantBiology", biology);

		Translator translator = mock(Translator.class);
		when(translator.getLocale()).thenReturn(Locale.ENGLISH);
		mockTranslation(translator, biology, "Biology");
		mockTranslation(translator, plantBiology, "Plant Biology");
		String descKey = TaxonomyUIFactory.PREFIX_DESCRIPTION + plantBiology.getI18nSuffix();
		when(translator.translate(descKey, null, 0, true)).thenReturn("The biology of plants.");

		String text = TaxonomyEmbeddingTextBuilder.build(plantBiology, translator);

		assertThat(text).contains("Plant Biology");
		assertThat(text).contains(". The biology of plants.");
	}

	@Test
	public void shouldHandleRootLevel() {
		TaxonomyLevel level = mockLevel("Sciences", null);

		Translator translator = mock(Translator.class);
		when(translator.getLocale()).thenReturn(Locale.ENGLISH);
		mockTranslation(translator, level, "Sciences");

		String text = TaxonomyEmbeddingTextBuilder.build(level, translator);

		assertThat(text).isNull();
		assertThat(TaxonomyEmbeddingTextBuilder.build(level, translator, null, TaxonomyEmbeddingTextVariant.PATH_NAME))
				.isEqualTo("Sciences");
	}

	@Test
	public void shouldBuildNameForm() {
		TaxonomyLevel sciences = mockLevel("Sciences", null);
		TaxonomyLevel biology = mockLevel("Biology", sciences);

		Translator translator = mock(Translator.class);
		when(translator.getLocale()).thenReturn(Locale.ENGLISH);
		mockTranslation(translator, sciences, "Sciences");
		mockTranslation(translator, biology, "Biology");

		String text = TaxonomyEmbeddingTextBuilder.build(biology, translator, null, TaxonomyEmbeddingTextVariant.NAME);

		assertThat(text).isEqualTo("Biology");
	}

	@Test
	public void shouldBuildPathNameForm() {
		TaxonomyLevel sciences = mockLevel("Sciences", null);
		TaxonomyLevel biology = mockLevel("Biology", sciences);

		Translator translator = mock(Translator.class);
		when(translator.getLocale()).thenReturn(Locale.ENGLISH);
		mockTranslation(translator, sciences, "Sciences");
		mockTranslation(translator, biology, "Biology");

		String text = TaxonomyEmbeddingTextBuilder.build(biology, translator, null, TaxonomyEmbeddingTextVariant.PATH_NAME);

		assertThat(text).isEqualTo("Sciences, Biology");
		assertThat(text).doesNotContain(">");
	}

	@Test
	public void shouldBuildFullForm() {
		TaxonomyLevel sciences = mockLevel("Sciences", null);
		TaxonomyLevel biology = mockLevel("Biology", sciences);

		Translator translator = mock(Translator.class);
		when(translator.getLocale()).thenReturn(Locale.ENGLISH);
		mockTranslation(translator, sciences, "Sciences");
		mockTranslation(translator, biology, "Biology");
		String descKey = TaxonomyUIFactory.PREFIX_DESCRIPTION + biology.getI18nSuffix();
		when(translator.translate(descKey, null, 0, true)).thenReturn("Study of living organisms.");

		String text = TaxonomyEmbeddingTextBuilder.build(biology, translator, null, TaxonomyEmbeddingTextVariant.FULL);

		assertThat(text).isEqualTo("Sciences, Biology. Study of living organisms.");
	}

	@Test
	public void fullFormShouldReturnNullWhenNoDescription() {
		TaxonomyLevel sciences = mockLevel("Sciences", null);
		TaxonomyLevel biology = mockLevel("Biology", sciences);

		Translator translator = mock(Translator.class);
		when(translator.getLocale()).thenReturn(Locale.ENGLISH);
		mockTranslation(translator, sciences, "Sciences");
		mockTranslation(translator, biology, "Biology");

		String text = TaxonomyEmbeddingTextBuilder.build(biology, translator, null, TaxonomyEmbeddingTextVariant.FULL);

		assertThat(text).isNull();
	}

	@Test
	public void rootLevelPathNameShouldEqualName() {
		TaxonomyLevel root = mockLevel("Sciences", null);

		Translator translator = mock(Translator.class);
		when(translator.getLocale()).thenReturn(Locale.ENGLISH);
		mockTranslation(translator, root, "Sciences");

		String name = TaxonomyEmbeddingTextBuilder.build(root, translator, null, TaxonomyEmbeddingTextVariant.NAME);
		String pathName = TaxonomyEmbeddingTextBuilder.build(root, translator, null, TaxonomyEmbeddingTextVariant.PATH_NAME);

		assertThat(name).isEqualTo("Sciences");
		assertThat(pathName).isEqualTo("Sciences");
	}

	// --- helpers ---

	private TaxonomyLevel mockLevel(String identifier, TaxonomyLevel parent) {
		TaxonomyLevel level = mock(TaxonomyLevel.class);
		when(level.getIdentifier()).thenReturn(identifier);
		when(level.getI18nSuffix()).thenReturn(identifier.toLowerCase() + "-suffix");
		when(level.getParent()).thenReturn(parent);
		return level;
	}

	private void mockTranslation(Translator translator, TaxonomyLevel level, String displayName) {
		String key = TaxonomyUIFactory.PREFIX_DISPLAY_NAME + level.getI18nSuffix();
		when(translator.translate(key, null, 0, true)).thenReturn(displayName);
	}
}

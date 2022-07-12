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
package org.olat.modules.qpool.ui.metadata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertArrayEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionPoolModule;
import org.olat.modules.qpool.ui.tree.QPoolTaxonomyTreeBuilder;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.model.TaxonomyLevelImpl;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.test.KeyTranslator;


/**
 * 
 * Initial date: 12.12.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QPoolTaxonomyTreeBuilderTest {

	private static final String INTENDENTION = "\u00a0\u00a0\u00a0\u00a0";
	private static final Long MATH_KEY = 2l;
	private static final String MATH = "math";
	private static final Long GEOM_KEY = 3l;
	private static final String GEOM = "geom";
	private static final Long ALGEBRA_KEY = 4l;
	private static final String ALGEBRA = "algebra";
	private static final Long LANGUAGE_KEY = 10l;
	private static final String LANGUAGE = "language";
	private static final Long ENGLISH_KEY = 9l;
	private static final String ENGLISH = "english";
	private static final Long HINDI_KEY = 8l;
	private static final String HINDI = "hindi";
	private static final Long RUSSIAN_KEY = 7l;
	private static final String RUSSIAN = "russian";
	private static final Long LATIN_KEY = 6l;
	private static final String LATIN = "latin";
	
	private KeyTranslator translator;
	
	@Mock
	private QPoolService qpoolService;
	@Mock
	private QuestionPoolModule qpoolModule;
	
	@InjectMocks
	private QPoolTaxonomyTreeBuilder sut;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		
		when(qpoolService.getTaxonomyLevels()).thenReturn(createLevelsOfClassicSchoolTaxonomy());
		when(qpoolService.getTaxonomyLevel(any(), any())).thenReturn(createLevelsWithCompetence());
		
		when(qpoolModule.isReviewProcessEnabled()).thenReturn(false);
		
		translator = new KeyTranslator(Locale.ENGLISH);
		translator.setPrefix("prefix");
	}
	
	@Test
	public void shouldGetSelectableKeysIfProzessless() {
		sut.loadTaxonomyLevelsMy(translator, null);
		String[] keys = sut.getSelectableKeys();
		
		String[] expectedKeys = {
				Long.toString(LANGUAGE_KEY),
				Long.toString(ENGLISH_KEY),
				Long.toString(HINDI_KEY),
				Long.toString(LATIN_KEY),
				Long.toString(RUSSIAN_KEY),
				Long.toString(MATH_KEY),
				Long.toString(ALGEBRA_KEY),
				Long.toString(GEOM_KEY)};
		assertArrayEquals(expectedKeys, keys);
	}
	
	@Test
	public void shouldGetSelectableKeysWithEmptyEntryIfProzessless() {
		sut.loadTaxonomyLevelsSelection(translator, null, true, false);
		String[] keys = sut.getSelectableKeys();
		
		String[] expectedKeys = {
				"-1",
				Long.toString(LANGUAGE_KEY),
				Long.toString(ENGLISH_KEY),
				Long.toString(HINDI_KEY),
				Long.toString(LATIN_KEY),
				Long.toString(RUSSIAN_KEY),
				Long.toString(MATH_KEY),
				Long.toString(ALGEBRA_KEY),
				Long.toString(GEOM_KEY)};
		assertArrayEquals(expectedKeys, keys);
	}
	
	@Test
	public void shouldGetSelectableValuesIfProzessless() {
		sut.loadTaxonomyLevelsMy(translator, null);
		String[] values = sut.getSelectableValues();
	
		String[] expectedValues = {
				translated(LANGUAGE), 
				INTENDENTION + translated(ENGLISH),
				INTENDENTION + translated(HINDI),
				INTENDENTION + translated(LATIN),
				INTENDENTION + translated(RUSSIAN),
				translated(MATH),
				INTENDENTION + translated(ALGEBRA),
				INTENDENTION + translated(GEOM)};
		assertArrayEquals(expectedValues, values);
	}
	
	@Test
	public void shouldGetSelectableValuesWithEmptyEntryIfProzesless() {
		sut.loadTaxonomyLevelsSelection(translator, null, true, false);
		String[] values = sut.getSelectableValues();
	
		String[] expectedValues = {
				"-",
				translated(LANGUAGE),
				INTENDENTION + translated(ENGLISH),
				INTENDENTION + translated(HINDI),
				INTENDENTION + translated(LATIN),
				INTENDENTION + translated(RUSSIAN),
				translated(MATH),
				INTENDENTION + translated(ALGEBRA),
				INTENDENTION + translated(GEOM)};
		assertArrayEquals(expectedValues, values);
	}
	
	@Test
	public void shouldGetSelectableKeysIfReviewProzess() {
		when(qpoolModule.isReviewProcessEnabled()).thenReturn(true);
		
		sut.loadTaxonomyLevelsMy(translator, null);
		String[] keys = sut.getSelectableKeys();
		
		String[] expectedKeys = {
				Long.toString(HINDI_KEY),
				Long.toString(MATH_KEY),
				Long.toString(ALGEBRA_KEY),
				Long.toString(GEOM_KEY)};
		assertArrayEquals(expectedKeys, keys);
	}
	
	@Test
	public void shouldGetSelectableValuesIfReviewProzess() {
		when(qpoolModule.isReviewProcessEnabled()).thenReturn(true);
		
		sut.loadTaxonomyLevelsMy(translator, null);
		String[] values = sut.getSelectableValues();
	
		String[] expectedValues = {
				translated(HINDI), 
				translated(MATH),
				INTENDENTION + translated(ALGEBRA),
				INTENDENTION + translated(GEOM)};
		assertArrayEquals(expectedValues, values);
	}
	
	@Test
	public void shouldGetTreeKeysIfReviewProzess() {
		when(qpoolModule.isReviewProcessEnabled()).thenReturn(true);
		
		sut.loadTaxonomyLevelsMy(translator, null);
		List<TaxonomyLevel> keys = sut.getTreeTaxonomyLevels();
		
		assertThat(keys.get(0).getKey()).isEqualTo(HINDI_KEY);
		assertThat(keys.get(1).getKey()).isEqualTo(MATH_KEY);
	}
	
	@Test
	public void shouldFindTaxonomyLevel() {
		sut.loadTaxonomyLevelsMy(translator, null);
		TaxonomyLevel taxonomyLevel = sut.getTaxonomyLevel(Long.toString(RUSSIAN_KEY));
		
		assertThat(taxonomyLevel.getKey()).isEqualTo(RUSSIAN_KEY);
	}
	
	@Test
	public void shouldFindTaxonomyLevelExcludesEmptyEntry() {
		sut.loadTaxonomyLevelsMy(translator, null);
		TaxonomyLevel taxonomyLevel = sut.getTaxonomyLevel("-1");
		
		assertThat(taxonomyLevel).isNull();
	}
	
	private List<TaxonomyLevel> createLevelsOfClassicSchoolTaxonomy() {
		List<TaxonomyLevel> levels = new ArrayList<>();
		TaxonomyLevel math = new TestableTaxonomyLevel(MATH_KEY, MATH, null);
		levels.add(math);
		TaxonomyLevel geom = new TestableTaxonomyLevel(GEOM_KEY, GEOM, math);
		levels.add(geom);
		TaxonomyLevel algebra = new TestableTaxonomyLevel(ALGEBRA_KEY, ALGEBRA, math);
		levels.add(algebra);
		TaxonomyLevel language = new TestableTaxonomyLevel(LANGUAGE_KEY, LANGUAGE, null);
		levels.add(language);
		TaxonomyLevel english = new TestableTaxonomyLevel(ENGLISH_KEY, ENGLISH, language);
		levels.add(english);
		TaxonomyLevel hindi = new TestableTaxonomyLevel(HINDI_KEY, HINDI, language);
		levels.add(hindi);
		TaxonomyLevel russian = new TestableTaxonomyLevel(RUSSIAN_KEY, RUSSIAN, language);
		levels.add(russian);
		TaxonomyLevel latin = new TestableTaxonomyLevel(LATIN_KEY, LATIN, language);
		levels.add(latin);
		
		return levels;
	}

	private List<TaxonomyLevel> createLevelsWithCompetence() {
		List<Long> levelsWithCompetenceKeys = Arrays.asList(MATH_KEY, GEOM_KEY, HINDI_KEY);
		List<TaxonomyLevel> levelsWithCompetence = createLevelsOfClassicSchoolTaxonomy().stream()
				.filter(level -> levelsWithCompetenceKeys.contains(level.getKey()))
				.collect(Collectors.toList());
		return levelsWithCompetence;
	}
	
	private String translated(String displayName) {
		return "prefix" + TaxonomyUIFactory.PREFIX_DISPLAY_NAME + displayName;
	}
	
	@SuppressWarnings("serial")
	private static class TestableTaxonomyLevel extends TaxonomyLevelImpl {
		private final Long key;

		public TestableTaxonomyLevel(Long key, String displayName, TaxonomyLevel parent) {
			this.key = key;
			setI18nSuffix(displayName);
			setParent(parent);
			String parentPathKeys = parent != null? parent.getMaterializedPathKeys(): "";
			setMaterializedPathKeys(parentPathKeys + "/" + key);
		}

		@Override
		public Long getKey() {
			return key;
		}
		
		
	}
}

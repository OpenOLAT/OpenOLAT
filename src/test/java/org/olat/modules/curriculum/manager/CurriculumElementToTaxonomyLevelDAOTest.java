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
package org.olat.modules.curriculum.manager;

import static org.olat.test.JunitTestHelper.random;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumElementToTaxonomyLevel;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.manager.TaxonomyDAO;
import org.olat.modules.taxonomy.manager.TaxonomyLevelDAO;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 15 juin 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementToTaxonomyLevelDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private TaxonomyDAO taxonomyDao;
	@Autowired
	private TaxonomyLevelDAO taxonomyLevelDao;
	@Autowired
	private CurriculumDAO curriculumDao;
	@Autowired
	private CurriculumElementDAO curriculumElementDao;
	@Autowired
	private CurriculumElementToTaxonomyLevelDAO curriculumElementToTaxonomyLevelDao;
	
	@Test
	public void createRelation() {
		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-rela-1", "Curriculum for relation to taxonomy", "Curriculum", false, null);
		CurriculumElement element = curriculumElementDao.createCurriculumElement("Element-1", "1. Element",
				CurriculumElementStatus.active, new Date(), new Date(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);

		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-98", "Leveled taxonomy", null, null);
		TaxonomyLevel level = taxonomyLevelDao.createTaxonomyLevel("ID-Level-0", random(), "My first taxonomy level", "A basic level", null, null, null, null, taxonomy);
	
		curriculumElementToTaxonomyLevelDao.createRelation(element, level);
		dbInstance.commit();
	}
	
	@Test
	public void createAndGetRelation() {
		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-rela-2", "Curriculum for relation to taxonomy", "Curriculum", false, null);
		CurriculumElement element = curriculumElementDao.createCurriculumElement("Element-2", "2. Element",
				CurriculumElementStatus.active, new Date(), new Date(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);

		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-301", "Leveled taxonomy", null, null);
		TaxonomyLevel level = taxonomyLevelDao.createTaxonomyLevel("ID-Level-0", random(), "My first taxonomy level", "A basic level", null, null, null, null, taxonomy);
		curriculumElementToTaxonomyLevelDao.createRelation(element, level);
		dbInstance.commitAndCloseSession();
		
		CurriculumElement reloadedElement = curriculumElementDao.loadByKey(element.getKey());
		Set<CurriculumElementToTaxonomyLevel> relationToLevels = reloadedElement.getTaxonomyLevels();
		Assert.assertNotNull(relationToLevels);
		Assert.assertEquals(1, relationToLevels.size());
		CurriculumElementToTaxonomyLevel relationToLevel = relationToLevels.iterator().next();
		Assert.assertEquals(level, relationToLevel.getTaxonomyLevel());
		Assert.assertEquals(element, relationToLevel.getCurriculumElement());
	}
	
	@Test
	public void getTaxonomyLevels() {
		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-rela-3", "Curriculum for relation to taxonomy", "Curriculum", false, null);
		CurriculumElement element = curriculumElementDao.createCurriculumElement("Element-3", "3. Element",
				CurriculumElementStatus.active, new Date(), new Date(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);

		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-302", "Leveled taxonomy", null, null);
		TaxonomyLevel level = taxonomyLevelDao.createTaxonomyLevel("ID-Level-0", random(), "My first taxonomy level", "A basic level", null, null, null, null, taxonomy);
		curriculumElementToTaxonomyLevelDao.createRelation(element, level);
		dbInstance.commitAndCloseSession();
		
		List<TaxonomyLevel> loadedLevels = curriculumElementToTaxonomyLevelDao.getTaxonomyLevels(element);
		Assert.assertNotNull(loadedLevels);
		Assert.assertEquals(1, loadedLevels.size());
		Assert.assertEquals(level, loadedLevels.get(0));
	}
	
	@Test
	public void getCurriculumElements() {
		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-rela-4", "Curriculum for relation to taxonomy", "Curriculum", false, null);
		CurriculumElement element = curriculumElementDao.createCurriculumElement("Element-4", "4. Element",
				CurriculumElementStatus.active, new Date(), new Date(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);

		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-303", "Leveled taxonomy", null, null);
		TaxonomyLevel level = taxonomyLevelDao.createTaxonomyLevel("ID-Level-0", random(), "My first taxonomy level", "A basic level", null, null, null, null, taxonomy);
		curriculumElementToTaxonomyLevelDao.createRelation(element, level);
		dbInstance.commitAndCloseSession();
		
		List<CurriculumElement> loadedElements = curriculumElementToTaxonomyLevelDao.getCurriculumElements(level);
		Assert.assertNotNull(loadedElements);
		Assert.assertEquals(1, loadedElements.size());
		Assert.assertEquals(element, loadedElements.get(0));
	}
	
	@Test
	public void getCurriculumElementKeyToTaxonomyLevels() {
		Curriculum curriculum = curriculumDao.createAndPersist(random(), random(), "Curriculum", false, null);
		CurriculumElement element1 = curriculumElementDao.createCurriculumElement(random(), random(),
				CurriculumElementStatus.active, new Date(), new Date(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element2 = curriculumElementDao.createCurriculumElement(random(), random(),
				CurriculumElementStatus.active, new Date(), new Date(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element3 = curriculumElementDao.createCurriculumElement(random(), random(),
				CurriculumElementStatus.active, new Date(), new Date(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);

		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-305", "Leveled taxonomy", null, null);
		TaxonomyLevel level1 = taxonomyLevelDao.createTaxonomyLevel(random(), random(), random(), random(), null, null, null, null, taxonomy);
		TaxonomyLevel level2 = taxonomyLevelDao.createTaxonomyLevel(random(), random(), random(), random(), null, null, null, null, taxonomy);
		curriculumElementToTaxonomyLevelDao.createRelation(element1, level1);
		curriculumElementToTaxonomyLevelDao.createRelation(element1, level2);
		curriculumElementToTaxonomyLevelDao.createRelation(element2, level1);
		
		List<? extends CurriculumElementRef> curriculumElements = List.of(element1, element2, element3);
		Map<Long, List<TaxonomyLevel>> taxonomyLevels = curriculumElementToTaxonomyLevelDao.getCurriculumElementKeyToTaxonomyLevels(curriculumElements);
		
		Assert.assertEquals(2, taxonomyLevels.size());
		Assert.assertEquals(2, taxonomyLevels.get(element1.getKey()).size());
		Assert.assertEquals(1, taxonomyLevels.get(element2.getKey()).size());
		Assert.assertNull(taxonomyLevels.get(element3.getKey()));
	}
	
	@Test
	public void deleteTaxonomyLevelRelation() {
		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-rela-5", "Curriculum for relation to taxonomy", "Curriculum", false, null);
		CurriculumElement element = curriculumElementDao.createCurriculumElement("Element-5", "5. Element",
				CurriculumElementStatus.active, new Date(), new Date(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);

		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-305", "Leveled taxonomy", null, null);
		TaxonomyLevel level = taxonomyLevelDao.createTaxonomyLevel("ID-Level-0", random(), "My taxonomy level n.5", "A basic level", null, null, null, null, taxonomy);
		curriculumElementToTaxonomyLevelDao.createRelation(element, level);
		dbInstance.commitAndCloseSession();
		
		curriculumElementToTaxonomyLevelDao.deleteRelation(level);
		dbInstance.commitAndCloseSession();
		
		List<CurriculumElement> elements = curriculumElementToTaxonomyLevelDao.getCurriculumElements(level);
		Assert.assertNotNull(elements);
		Assert.assertTrue(elements.isEmpty());
	}
	
	@Test
	public void replaceTaxonomyLevelRelation() {
		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-rela-6", "Curriculum for relation to taxonomy", "Curriculum", false, null);
		CurriculumElement element1 = curriculumElementDao.createCurriculumElement("Element-6.1", "6.1 Element",
				CurriculumElementStatus.active, new Date(), new Date(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element2 = curriculumElementDao.createCurriculumElement("Element-6.2", "6.2 Element",
				CurriculumElementStatus.active, new Date(), new Date(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);

		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-306", "Leveled taxonomy", null, null);
		TaxonomyLevel level1 = taxonomyLevelDao.createTaxonomyLevel("ID-Level-6", random(), "My taxonomy level n.6", "A basic level", null, null, null, null, taxonomy);
		TaxonomyLevel level2 = taxonomyLevelDao.createTaxonomyLevel("ID-Level-6", random(), "My taxonomy level n.6", "A basic level", null, null, null, null, taxonomy);
		curriculumElementToTaxonomyLevelDao.createRelation(element1, level1);
		curriculumElementToTaxonomyLevelDao.createRelation(element2, level2);
		dbInstance.commitAndCloseSession();
		
		curriculumElementToTaxonomyLevelDao.replace(level1, level2);
		dbInstance.commitAndCloseSession();
		
		List<CurriculumElement> replacedElements = curriculumElementToTaxonomyLevelDao.getCurriculumElements(level1);
		Assert.assertNotNull(replacedElements);
		Assert.assertTrue(replacedElements.isEmpty());
		
		List<CurriculumElement> elements = curriculumElementToTaxonomyLevelDao.getCurriculumElements(level2);
		Assert.assertNotNull(elements);
		Assert.assertEquals(2, elements.size());
	}

}

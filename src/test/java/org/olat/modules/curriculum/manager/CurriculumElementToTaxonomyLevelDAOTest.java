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
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
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
		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-rela-1", "Curriculum for relation to taxonomy", "Curriculum", null);
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
		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-rela-2", "Curriculum for relation to taxonomy", "Curriculum", null);
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
		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-rela-3", "Curriculum for relation to taxonomy", "Curriculum", null);
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
		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-rela-4", "Curriculum for relation to taxonomy", "Curriculum", null);
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
	

}

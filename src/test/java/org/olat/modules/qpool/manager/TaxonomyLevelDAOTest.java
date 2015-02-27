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
package org.olat.modules.qpool.manager;

import java.util.List;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.ims.qti.QTIConstants;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionType;
import org.olat.modules.qpool.TaxonomyLevel;
import org.olat.modules.qpool.model.QItemType;
import org.olat.modules.qpool.model.TaxonomyLevelImpl;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21.01.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaxonomyLevelDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private QItemTypeDAO qItemTypeDao;
	@Autowired
	private QuestionItemDAO questionDao;
	@Autowired
	private TaxonomyLevelDAO taxonomyLevelDao;
	
	@Test
	public void createStudyField() {
		TaxonomyLevel taxonomyLevel = taxonomyLevelDao.createAndPersist(null, "Astronomy");
		Assert.assertNotNull(taxonomyLevel);
		Assert.assertNotNull(taxonomyLevel.getKey());
		Assert.assertNotNull(taxonomyLevel.getCreationDate());
		Assert.assertEquals("Astronomy", taxonomyLevel.getField());
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void loadStudyFieldById() {
		TaxonomyLevel taxonomyLevel = taxonomyLevelDao.createAndPersist(null, "Astronautics");
		dbInstance.commitAndCloseSession();

		TaxonomyLevel reloadedLevel = taxonomyLevelDao.loadLevelById(taxonomyLevel.getKey());
		Assert.assertNotNull(reloadedLevel);
		Assert.assertNotNull(reloadedLevel.getKey());
		Assert.assertNotNull(reloadedLevel.getCreationDate());
		Assert.assertEquals("Astronautics", reloadedLevel.getField());
		Assert.assertEquals(taxonomyLevel.getKey(), reloadedLevel.getKey());
	}
	
	@Test
	public void loadAllStudyFields() {
		TaxonomyLevel taxonomyLevel = taxonomyLevelDao.createAndPersist(null, "Mechanics");
		dbInstance.commitAndCloseSession();

		List<TaxonomyLevel> levels = taxonomyLevelDao.loadAllLevels();
		Assert.assertNotNull(levels);
		Assert.assertTrue(levels.size() >= 1);
		Assert.assertTrue(levels.contains(taxonomyLevel));
	}
	
	@Test
	public void buildHierarchyStudyField() {
		TaxonomyLevel science = taxonomyLevelDao.createAndPersist(null, "Science");
		TaxonomyLevel mathematics = taxonomyLevelDao.createAndPersist(science, "Mathematics");
		TaxonomyLevel physics = taxonomyLevelDao.createAndPersist(science, "Physics");
		TaxonomyLevel chemistry = taxonomyLevelDao.createAndPersist(science, "Chemistry");
		dbInstance.commitAndCloseSession();
		
		//reload and check parents
		TaxonomyLevelImpl reloadPhysics = (TaxonomyLevelImpl)taxonomyLevelDao.loadLevelById(physics.getKey());
		Assert.assertNotNull(reloadPhysics);
		Assert.assertEquals(science, reloadPhysics.getParentField());

		List<TaxonomyLevel> subLevels = taxonomyLevelDao.loadTaxonomicPath(science);
		Assert.assertNotNull(subLevels);
		Assert.assertEquals(3, subLevels.size());
		Assert.assertTrue(subLevels.contains(mathematics));
		Assert.assertTrue(subLevels.contains(physics));
		Assert.assertTrue(subLevels.contains(chemistry));
	}
	
	@Test
	public void getMateriliazedPath() {
		TaxonomyLevel science = taxonomyLevelDao.createAndPersist(null, "Science");
		TaxonomyLevel mathematics = taxonomyLevelDao.createAndPersist(science, "Mathematics");
		TaxonomyLevel topology = taxonomyLevelDao.createAndPersist(mathematics, "Topology");
		TaxonomyLevel graph = taxonomyLevelDao.createAndPersist(topology, "Graph theory");
		dbInstance.commitAndCloseSession();
		
		//reload and check parents
		TaxonomyLevel path = taxonomyLevelDao.loadLevelById(graph.getKey());
		Assert.assertNotNull(path);
		Assert.assertNotNull(path.getMaterializedPathNames());
		Assert.assertEquals("/Science/Mathematics/Topology", path.getMaterializedPathNames());
	}
	
	@Test
	public void getDescendants() {
		TaxonomyLevel science = taxonomyLevelDao.createAndPersist(null, "Science");
		TaxonomyLevel mathematics = taxonomyLevelDao.createAndPersist(science, "Mathematics");
		TaxonomyLevel numerical = taxonomyLevelDao.createAndPersist(mathematics, "Numerical");
		TaxonomyLevel topology = taxonomyLevelDao.createAndPersist(mathematics, "Topology");
		TaxonomyLevel graph = taxonomyLevelDao.createAndPersist(topology, "Graph theory");
		dbInstance.commitAndCloseSession();
		
		//load the descendants of mathematics
		List<TaxonomyLevel> descendants = taxonomyLevelDao.getDescendants(mathematics);
		Assert.assertNotNull(descendants);
		Assert.assertEquals(3, descendants.size());
		Assert.assertTrue(descendants.contains(numerical));
		Assert.assertTrue(descendants.contains(topology));
		Assert.assertTrue(descendants.contains(graph));
		
		//load the descendants of topology
		List<TaxonomyLevel> topologyDescendants = taxonomyLevelDao.getDescendants(topology);
		Assert.assertNotNull(topologyDescendants);
		Assert.assertEquals(1, topologyDescendants.size());
		Assert.assertTrue(topologyDescendants.contains(graph));
		
		//load the descendants of mathematics
		List<TaxonomyLevel> graphDescendants = taxonomyLevelDao.getDescendants(graph);
		Assert.assertNotNull(descendants);
		Assert.assertTrue(graphDescendants.isEmpty());
	}
	
	@Test
	public void updateWithDescendants() {
		TaxonomyLevel animals = taxonomyLevelDao.createAndPersist(null, "Animals");
		TaxonomyLevel cats = taxonomyLevelDao.createAndPersist(animals, "Cats");
		TaxonomyLevel dogs = taxonomyLevelDao.createAndPersist(animals, "Dogs");
		TaxonomyLevel huskies = taxonomyLevelDao.createAndPersist(dogs, "Huskies");
		TaxonomyLevel lion = taxonomyLevelDao.createAndPersist(cats, "Lion");
		TaxonomyLevel mountainLion = taxonomyLevelDao.createAndPersist(lion, "Mountain Lion");
		TaxonomyLevel tiger = taxonomyLevelDao.createAndPersist(cats, "Tiger");
		dbInstance.commitAndCloseSession();
		
		//update the cats
		taxonomyLevelDao.update("Felids", cats);
		dbInstance.commit();
		
		//check if descendants are correctly updated
		TaxonomyLevel reloadedLion = taxonomyLevelDao.loadLevelById(lion.getKey());
		Assert.assertEquals("/Animals/Felids", reloadedLion.getMaterializedPathNames());
		TaxonomyLevel reloadedMountainLion = taxonomyLevelDao.loadLevelById(mountainLion.getKey());
		Assert.assertEquals("/Animals/Felids/Lion", reloadedMountainLion.getMaterializedPathNames());
		TaxonomyLevel reloadedTiger = taxonomyLevelDao.loadLevelById(tiger.getKey());
		Assert.assertEquals("/Animals/Felids", reloadedTiger.getMaterializedPathNames());
		
		//dogs are not changed
		TaxonomyLevel reloadedDogs = taxonomyLevelDao.loadLevelById(dogs.getKey());
		Assert.assertEquals("/Animals", reloadedDogs.getMaterializedPathNames());
		TaxonomyLevel reloadedHuskies = taxonomyLevelDao.loadLevelById(huskies.getKey());
		Assert.assertEquals("/Animals/Dogs", reloadedHuskies.getMaterializedPathNames());	
	}
	
	@Test
	public void countItemUsing_TaxonomyLevel() {
		TaxonomyLevel level = taxonomyLevelDao.createAndPersist(null, "I'm in use");
		QItemType fibType = qItemTypeDao.loadByType(QuestionType.FIB.name());
		QuestionItem item1 = questionDao.createAndPersist(null, "Nebula", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), level, null, null, fibType);
		QuestionItem item2 = questionDao.createAndPersist(null, "Cluster", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), level, null, null, fibType);
		Assert.assertNotNull(item1);
		Assert.assertNotNull(item2);
		dbInstance.commitAndCloseSession();
		
		//check count
		int numOfItems = taxonomyLevelDao.countItemUsing(level);
		Assert.assertEquals(2, numOfItems);
	}
	
	
	@Test
	public void countChildren_TaxonomyLevel() {
		TaxonomyLevel galaxy = taxonomyLevelDao.createAndPersist(null, "Galaxy");
		TaxonomyLevel andromeda = taxonomyLevelDao.createAndPersist(galaxy, "Andromeda");
		TaxonomyLevel ngc = taxonomyLevelDao.createAndPersist(galaxy, "NGC 2502");
		Assert.assertNotNull(andromeda);
		Assert.assertNotNull(ngc);
		dbInstance.commitAndCloseSession();
		
		//check count
		int numOfChildrenGalaxy = taxonomyLevelDao.countChildren(galaxy);
		Assert.assertEquals(2, numOfChildrenGalaxy);
		int numOfChildrenAdnromeda = taxonomyLevelDao.countChildren(andromeda);
		Assert.assertEquals(0, numOfChildrenAdnromeda);
	}
}

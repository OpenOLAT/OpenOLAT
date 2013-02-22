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

import junit.framework.Assert;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.qpool.StudyField;
import org.olat.modules.qpool.model.StudyFieldImpl;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21.01.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class StudyFieldDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private StudyFieldDAO studyFieldDao;
	
	@Test
	public void createStudyField() {
		StudyField studyField = studyFieldDao.createAndPersist(null, "Astronomy");
		Assert.assertNotNull(studyField);
		Assert.assertNotNull(studyField.getKey());
		Assert.assertNotNull(studyField.getCreationDate());
		Assert.assertEquals("Astronomy", studyField.getField());
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void loadStudyFieldById() {
		StudyField studyField = studyFieldDao.createAndPersist(null, "Astronautics");
		dbInstance.commitAndCloseSession();

		StudyField reloadedField = studyFieldDao.loadStudyFieldById(studyField.getKey());
		Assert.assertNotNull(reloadedField);
		Assert.assertNotNull(reloadedField.getKey());
		Assert.assertNotNull(reloadedField.getCreationDate());
		Assert.assertEquals("Astronautics", reloadedField.getField());
		Assert.assertEquals(studyField.getKey(), reloadedField.getKey());
	}
	
	@Test
	public void loadAllStudyFields() {
		StudyField studyField = studyFieldDao.createAndPersist(null, "Mechanics");
		dbInstance.commitAndCloseSession();

		List<StudyField> fields = studyFieldDao.loadAllFields();
		Assert.assertNotNull(fields);
		Assert.assertTrue(fields.size() >= 1);
		Assert.assertTrue(fields.contains(studyField));
	}
	
	@Test
	public void buildHierarchyStudyField() {
		StudyField science = studyFieldDao.createAndPersist(null, "Science");
		StudyField mathematics = studyFieldDao.createAndPersist(science, "Mathematics");
		StudyField physics = studyFieldDao.createAndPersist(science, "Physics");
		StudyField chemistry = studyFieldDao.createAndPersist(science, "Chemistry");
		dbInstance.commitAndCloseSession();
		
		//reload and check parents
		StudyFieldImpl reloadPhysics = (StudyFieldImpl)studyFieldDao.loadStudyFieldById(physics.getKey());
		Assert.assertNotNull(reloadPhysics);
		Assert.assertEquals(science, reloadPhysics.getParentField());

		List<StudyField> subFields = studyFieldDao.loadFields(science);
		Assert.assertNotNull(subFields);
		Assert.assertEquals(3, subFields.size());
		Assert.assertTrue(subFields.contains(mathematics));
		Assert.assertTrue(subFields.contains(physics));
		Assert.assertTrue(subFields.contains(chemistry));
	}
	
	@Test
	public void getMateriliazedPath() {
		StudyField science = studyFieldDao.createAndPersist(null, "Science");
		StudyField mathematics = studyFieldDao.createAndPersist(science, "Mathematics");
		StudyField topology = studyFieldDao.createAndPersist(mathematics, "Topology");
		StudyField graph = studyFieldDao.createAndPersist(topology, "Graph theory");
		dbInstance.commitAndCloseSession();
		
		//reload and check parents
		String path = studyFieldDao.getMaterializedPath(graph);
		Assert.assertNotNull(path);
		
		Assert.assertEquals("/Science/Mathematics/Topology/Graph theory", path);
		
	}
	

}

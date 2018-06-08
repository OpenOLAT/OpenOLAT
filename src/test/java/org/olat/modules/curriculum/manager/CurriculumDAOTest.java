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

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.manager.OrganisationDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Organisation;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.model.CurriculumImpl;
import org.olat.modules.curriculum.model.CurriculumSearchParameters;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 9 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CurriculumDAO curriculumDao;
	@Autowired
	private OrganisationDAO organisationDao;
	
	@Test
	public void createCurriculum() {
		Organisation organisation = organisationDao.createAndPersistOrganisation("Curriculum org.", "CUR-1", "", null, null);
		Curriculum curriculum = curriculumDao.createAndPersist("CUR-1", "Curriculum 1", "Short desc.", organisation);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(curriculum);
		Assert.assertNotNull(curriculum.getKey());
		Assert.assertNotNull(curriculum.getCreationDate());
		Assert.assertNotNull(curriculum.getLastModified());
		Assert.assertEquals("Curriculum 1", curriculum.getDisplayName());
		Assert.assertEquals("CUR-1", curriculum.getIdentifier());
		Assert.assertEquals("Short desc.", curriculum.getDescription());
		Assert.assertEquals(organisation, curriculum.getOrganisation());
	}
	
	@Test
	public void loadByKey() {
		Organisation organisation = organisationDao.createAndPersistOrganisation("Curriculum org.", "CUR-2", "", null, null);
		Curriculum curriculum = curriculumDao.createAndPersist("CUR-2", "Curriculum 2", "Short desc.", organisation);
		dbInstance.commitAndCloseSession();
		
		Curriculum reloadedCurriculum = curriculumDao.loadByKey(curriculum.getKey());
		Assert.assertNotNull(reloadedCurriculum);
		Assert.assertEquals(curriculum, reloadedCurriculum);
		Assert.assertNotNull(reloadedCurriculum.getCreationDate());
		Assert.assertNotNull(reloadedCurriculum.getLastModified());
		Assert.assertEquals("Curriculum 2", reloadedCurriculum.getDisplayName());
		Assert.assertEquals("CUR-2", reloadedCurriculum.getIdentifier());
		Assert.assertEquals("Short desc.", reloadedCurriculum.getDescription());
		Assert.assertEquals(organisation, reloadedCurriculum.getOrganisation());
	}
	
	@Test
	public void search_all() {
		Curriculum curriculum = curriculumDao.createAndPersist("CUR-3", "Curriculum 3", "Short desc.", null);
		dbInstance.commitAndCloseSession();
		
		CurriculumSearchParameters params = new CurriculumSearchParameters();
		List<Curriculum> curriculums = curriculumDao.search(params);
		Assert.assertNotNull(curriculums);
		Assert.assertFalse(curriculums.isEmpty());
		Assert.assertTrue(curriculums.contains(curriculum));
	}
	
	@Test
	public void search_org() {
		Organisation organisation = organisationDao.createAndPersistOrganisation("Curriculum org.", "CUR-4", "", null, null);
		Curriculum curriculum = curriculumDao.createAndPersist("CUR-4", "Curriculum 4", "Short desc.", organisation);
		dbInstance.commitAndCloseSession();
		
		CurriculumSearchParameters params = new CurriculumSearchParameters();
		params.setOrganisations(Collections.singletonList(organisation));
		List<Curriculum> curriculums = curriculumDao.search(params);
		dbInstance.commitAndCloseSession();
		
		//check the result
		Assert.assertNotNull(curriculums);
		Assert.assertEquals(1, curriculums.size());
		Assert.assertTrue(curriculums.contains(curriculum));
		//check the fetch
		Assert.assertEquals(organisation, curriculums.get(0).getOrganisation());
		Assert.assertNotNull(((CurriculumImpl)curriculums.get(0)).getGroup().getKey());
	}
	
	@Test
	public void search_searchString() {
		String identifier = UUID.randomUUID().toString();
		String displayName = UUID.randomUUID().toString();
		Curriculum curriculum = curriculumDao.createAndPersist(identifier, displayName, "Short desc.", null);
		dbInstance.commitAndCloseSession();
		
		// search something which doesn't exists
		CurriculumSearchParameters params = new CurriculumSearchParameters();
		params.setSearchString("AAAAAAAA");
		List<Curriculum> curriculums = curriculumDao.search(params);
		Assert.assertTrue(curriculums.isEmpty());
		
		// search by identifier
		params.setSearchString(identifier);
		List<Curriculum> curriculumById = curriculumDao.search(params);
		Assert.assertEquals(1, curriculumById.size());
		Assert.assertEquals(curriculum, curriculumById.get(0));
		
		// search by identifier
		params.setSearchString(displayName.substring(0, 25).toUpperCase());
		List<Curriculum> curriculumByName = curriculumDao.search(params);
		Assert.assertTrue(curriculumByName.contains(curriculum));
		
		// search by primary key
		params.setSearchString(curriculum.getKey().toString());
		List<Curriculum> curriculumByKey = curriculumDao.search(params);
		Assert.assertTrue(curriculumByKey.contains(curriculum));
	}
}

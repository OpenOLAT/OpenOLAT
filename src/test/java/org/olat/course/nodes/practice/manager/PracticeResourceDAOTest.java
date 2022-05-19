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
package org.olat.course.nodes.practice.manager;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.course.nodes.practice.PracticeResource;
import org.olat.modules.qpool.Pool;
import org.olat.modules.qpool.QuestionItemCollection;
import org.olat.modules.qpool.manager.CollectionDAO;
import org.olat.modules.qpool.manager.PoolDAO;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 5 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PracticeResourceDAOTest extends OlatTestCase {
	
	private static RepositoryEntry courseEntry;
	private static RepositoryEntry testEntry;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private PoolDAO poolDao;
	@Autowired
	private CollectionDAO collectionDao;
	@Autowired
	private PracticeResourceDAO practiceResourceDao;
	
	@Before
	public void setup() {
		if(courseEntry == null) {
			Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("mem-1");
			courseEntry = JunitTestHelper.deployBasicCourse(author);
			
			testEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		}
	}
	
	@Test
	public void createPracticeResource() {
		String subIdent = UUID.randomUUID().toString();
		PracticeResource resource = practiceResourceDao.createResource(courseEntry, subIdent, testEntry, null, null, null);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(resource);
		Assert.assertNotNull(resource.getCreationDate());
		Assert.assertNotNull(resource.getLastModified());
		
		Assert.assertEquals(courseEntry, resource.getRepositoryEntry());
		Assert.assertEquals(subIdent, resource.getSubIdent());
		Assert.assertEquals(testEntry, resource.getTestEntry());
	}
	
	@Test
	public void getResourcesByRepositoryEntry() {
		String subIdent = UUID.randomUUID().toString();
		PracticeResource resource = practiceResourceDao.createResource(courseEntry, subIdent, testEntry, null, null, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(resource);
		
		List<PracticeResource> resources = practiceResourceDao.getResources(courseEntry, subIdent);
		assertThat(resources)
			.isNotNull()
			.isNotEmpty()
			.containsExactly(resource);	
	}
	
	@Test
	public void getResourcesByPool() {
		String subIdent = UUID.randomUUID().toString();
		Pool pool = poolDao.createPool(null, "Practice-1", false);
		PracticeResource resource = practiceResourceDao.createResource(courseEntry, subIdent, null, pool, null, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(resource);
		
		List<PracticeResource> resources = practiceResourceDao.getResources(pool);
		assertThat(resources)
			.isNotNull()
			.isNotEmpty()
			.containsExactly(resource);	
	}
	
	@Test
	public void getResourcesByCollection() {
		String subIdent = UUID.randomUUID().toString();
		//create an owner and its collection
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("Practice-Coll-Onwer-");
		QuestionItemCollection coll = collectionDao.createCollection("Practice collection", id);
		dbInstance.commitAndCloseSession();
		
		PracticeResource resource = practiceResourceDao.createResource(courseEntry, subIdent, null, null, coll, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(resource);
		
		List<PracticeResource> resources = practiceResourceDao.getResources(coll);
		assertThat(resources)
			.isNotNull()
			.isNotEmpty()
			.containsExactly(resource);	
	}
	
	@Test
	public void getResourcesByTest() {
		String subIdent = UUID.randomUUID().toString();
		PracticeResource resource = practiceResourceDao.createResource(courseEntry, subIdent, testEntry, null, null, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(resource);
		
		List<PracticeResource> resources = practiceResourceDao.getResourcesOfTest(testEntry);
		assertThat(resources)
			.isNotNull()
			.isNotEmpty()
			.contains(resource);
	}

}

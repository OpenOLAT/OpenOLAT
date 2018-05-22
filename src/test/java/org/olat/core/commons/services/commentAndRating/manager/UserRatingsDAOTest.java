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
package org.olat.core.commons.services.commentAndRating.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Assert;
import org.junit.Test;
import org.olat.admin.user.delete.service.UserDeletionManager;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingService;
import org.olat.core.commons.services.commentAndRating.model.UserRating;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.RepositoryEntryStatistics;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 31.01.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserRatingsDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private UserRatingsDAO userRatingsDao;
	@Autowired
	private CommentAndRatingService service;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private UserDeletionManager userDeletionManager;
	
	@Test
	public void should_service_present() {
		Assert.assertNotNull(service);
		Assert.assertNotNull(userRatingsDao);
	}
	
	@Test
	public void createRating() {
		OLATResourceable ores = JunitTestHelper.createRandomResource();
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("ucar-crud-1");
		dbInstance.commitAndCloseSession();

		//create
		UserRating newRating = userRatingsDao.createRating(id, ores, "test-1", 5);
		dbInstance.commit();
		Assert.assertNotNull(newRating);
		Assert.assertNotNull(newRating.getKey());
		Assert.assertNotNull(newRating.getCreationDate());
		Assert.assertNotNull(newRating.getLastModified());
		Assert.assertEquals(ores.getResourceableTypeName(), newRating.getResName());
		Assert.assertEquals(ores.getResourceableId(), newRating.getResId());
		Assert.assertEquals("test-1", newRating.getResSubPath());
		Assert.assertEquals(id, newRating.getCreator());
		Assert.assertEquals(new Integer(5), newRating.getRating());
	}
	
	@Test
	public void createUpdateRating() {
		OLATResourceable ores = JunitTestHelper.createRandomResource();
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("ucar-crud-1");
		dbInstance.commitAndCloseSession();

		//create
		UserRating newRating = userRatingsDao.createRating(id, ores, "test-2", 5);
		dbInstance.commitAndCloseSession();
		
		UserRating updatedRating = userRatingsDao.updateRating(newRating, 4);
		dbInstance.commit();
		
		Assert.assertNotNull(updatedRating);
		Assert.assertEquals(newRating, updatedRating);
		Assert.assertNotNull(updatedRating.getCreationDate());
		Assert.assertNotNull(updatedRating.getLastModified());
		Assert.assertEquals(ores.getResourceableTypeName(), updatedRating.getResName());
		Assert.assertEquals(ores.getResourceableId(), updatedRating.getResId());
		Assert.assertEquals("test-2", updatedRating.getResSubPath());
		Assert.assertEquals(id, updatedRating.getCreator());
		Assert.assertEquals(new Integer(4), updatedRating.getRating());
	}
	

	@Test
	public void testCRUDRating() {
		//init
		OLATResourceable ores = JunitTestHelper.createRandomResource();
		Identity ident1 = JunitTestHelper.createAndPersistIdentityAsRndUser("ucar-crud-1");
		Identity ident2 = JunitTestHelper.createAndPersistIdentityAsRndUser("ucar-crud-2");
		Identity ident3 = JunitTestHelper.createAndPersistIdentityAsRndUser("ucar-crud-3");

		
		assertEquals(0.0f, userRatingsDao.getRatingAverage(ores, null), 0.01f);
		assertEquals(0.0f, userRatingsDao.getRatingAverage(ores, "blubli"), 0.01f);
		assertEquals(0l, userRatingsDao.countRatings(ores, null));
		assertEquals(0l, userRatingsDao.countRatings(ores, "blubli"));
		
		UserRating r1 = userRatingsDao.createRating(ident1, ores, null, 2);
		Assert.assertNotNull(r1);
		UserRating r2 = userRatingsDao.createRating(ident1, ores, "blubli", 2);
		Assert.assertNotNull(r2);
		assertEquals(2.0f, userRatingsDao.getRatingAverage(ores, null), 0.01f);
		assertEquals(2.0f, userRatingsDao.getRatingAverage(ores, "blubli"), 0.01f);
		assertEquals(1l, userRatingsDao.countRatings(ores, null));
		assertEquals(1l, userRatingsDao.countRatings(ores, "blubli"));
		//
		UserRating r3 = userRatingsDao.createRating(ident2, ores, null, 4);
		Assert.assertNotNull(r3);
		UserRating r4 = userRatingsDao.createRating(ident2, ores, "blubli", 4);
		Assert.assertNotNull(r4);
		assertEquals(3.0f, userRatingsDao.getRatingAverage(ores, null), 0.01f);
		assertEquals(3.0f, userRatingsDao.getRatingAverage(ores, "blubli"), 0.01f);
		assertEquals(2l, userRatingsDao.countRatings(ores, null));
		assertEquals(2l, userRatingsDao.countRatings(ores, "blubli"));
		// 
		UserRating r5 = userRatingsDao.createRating(ident3, ores, null, 1);
		Assert.assertNotNull(r5);
		UserRating r6 = userRatingsDao.createRating(ident3, ores, "blubli", 1);
		Assert.assertNotNull(r6);
		assertEquals(2.33f, userRatingsDao.getRatingAverage(ores, null), 0.01f);
		assertEquals(2.33f, userRatingsDao.getRatingAverage(ores, "blubli"), 0.01f);
		assertEquals(3l, userRatingsDao.countRatings(ores, null));
		assertEquals(3l, userRatingsDao.countRatings(ores, "blubli"));
		//
		assertNotNull(userRatingsDao.getRating(ident1, ores, null));
		assertNotNull(userRatingsDao.getRating(ident2, ores, null));
		assertNotNull(userRatingsDao.getRating(ident3, ores, null));
		// can !!not!! create two ratings per person
		r1 = userRatingsDao.createRating(ident1, ores, null, 2);
		r2 = userRatingsDao.createRating(ident1, ores, "blubli", 2);
		assertEquals(2.25f, userRatingsDao.getRatingAverage(ores, null), 0.01f);
		assertEquals(2.25f, userRatingsDao.getRatingAverage(ores, "blubli"), 0.01f);
		//can create 2 ratings
		assertEquals(4l, userRatingsDao.countRatings(ores, null));
		assertEquals(4l, userRatingsDao.countRatings(ores, "blubli"));
		
		// Delete ratings without subpath
		userRatingsDao.deleteAllRatings(ores, null);
		assertEquals(0f, userRatingsDao.getRatingAverage(ores, null), 0.01f);
		assertEquals(2.25f, userRatingsDao.getRatingAverage(ores, "blubli"), 0.01f);
		assertEquals(0l, userRatingsDao.countRatings(ores, null));
		assertEquals(4l, userRatingsDao.countRatings(ores, "blubli"));

		// Recreate and delete ignoring subpath
		r1 = userRatingsDao.createRating(ident1, ores, null, 2);
		r2 = userRatingsDao.createRating(ident1, ores, "blubli", 2);
		assertEquals(2f, userRatingsDao.getRatingAverage(ores, null), 0.01f);
		assertEquals(2.2f, userRatingsDao.getRatingAverage(ores, "blubli"), 0.01f);
		assertEquals(1, userRatingsDao.countRatings(ores, null));
		assertEquals(5l, userRatingsDao.countRatings(ores, "blubli"));
		userRatingsDao.deleteAllRatingsIgnoringSubPath(ores);
		assertEquals(0f, userRatingsDao.getRatingAverage(ores, null), 0.01f);
		assertEquals(0f, userRatingsDao.getRatingAverage(ores, "blubli"), 0.01f);
		assertEquals(0l, userRatingsDao.countRatings(ores, null));
		assertEquals(0l, userRatingsDao.countRatings(ores, "blubli"));
	}
	
	@Test
	public void deleteUser() {
		Identity identToDelete = JunitTestHelper.createAndPersistIdentityAsRndUser("ucar-crud-4");
		Identity ident2 = JunitTestHelper.createAndPersistIdentityAsRndUser("ucar-crud-5");
		Identity ident3 = JunitTestHelper.createAndPersistIdentityAsRndUser("ucar-crud-6");
		OLATResourceable randomOres = JunitTestHelper.createRandomResource();
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(ident2);
		dbInstance.commitAndCloseSession();

		// make some rating
		UserRating r1 = userRatingsDao.createRating(identToDelete, randomOres, "rate", 2);
		UserRating r2 = userRatingsDao.createRating(identToDelete, entry, null, 2);
		UserRating r3 = userRatingsDao.createRating(ident2, randomOres, "rate", 4);
		UserRating r4 = userRatingsDao.createRating(ident2, entry, null, 4);
		userRatingsDao.createRating(ident3, randomOres, "rate", 3);
		userRatingsDao.createRating(ident3, entry, null, 3);
		dbInstance.commitAndCloseSession();
		// check average before deletion
		float randomAverage = userRatingsDao.getRatingAverage(randomOres, "rate");
		Assert.assertEquals(3.0f, randomAverage, 0.001f);
		float courseAverage = userRatingsDao.getRatingAverage(entry, null);
		Assert.assertEquals(3.0f, courseAverage, 0.001f);

		// delete first user
		userDeletionManager.deleteIdentity(identToDelete, null);
		dbInstance.commitAndCloseSession();
		
		//check that rating of the first user are deleted
		UserRating deletedRating1 = userRatingsDao.reloadRating(r1);
		Assert.assertNull(deletedRating1);
		UserRating deletedRating2 = userRatingsDao.reloadRating(r2);
		Assert.assertNull(deletedRating2);
		// but not the rating of the other users
		UserRating reloadedRating3 = userRatingsDao.reloadRating(r3);
		Assert.assertNotNull(reloadedRating3);
		UserRating reloadedRating4 = userRatingsDao.reloadRating(r4);
		Assert.assertNotNull(reloadedRating4);

		// check averages
		float afterRandomAverage = userRatingsDao.getRatingAverage(randomOres, "rate");
		Assert.assertEquals(3.5f, afterRandomAverage, 0.001f);
		float afterCourseAverage = userRatingsDao.getRatingAverage(entry, null);
		Assert.assertEquals(3.5f, afterCourseAverage, 0.001f);
		
		// check repository
		RepositoryEntry reloadedEntry = repositoryService.loadByKey(entry.getKey());
		RepositoryEntryStatistics entryStatistics = reloadedEntry.getStatistics();
		Assert.assertEquals(2l, entryStatistics.getNumOfRatings());
		Assert.assertEquals(3.5d, entryStatistics.getRating().doubleValue(), 0.001d);
	}
	
	
	
}
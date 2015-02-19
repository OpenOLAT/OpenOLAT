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
package org.olat.core.commons.services.commentAndRating;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingService;
import org.olat.core.commons.services.commentAndRating.manager.UserRatingsDAO;
import org.olat.core.commons.services.commentAndRating.model.UserRating;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
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
	
	@Test
	public void should_service_present() {
		Assert.assertNotNull(service);
		Assert.assertNotNull(userRatingsDao);
	}
	
	@Test
	public void createRating() {
		OLATResourceable ores = JunitTestHelper.createRandomResource();
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("ucar-crud-1-" + UUID.randomUUID().toString());
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
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("ucar-crud-1-" + UUID.randomUUID().toString());
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
		Identity ident1 = JunitTestHelper.createAndPersistIdentityAsUser("ucar-crud-1-" + UUID.randomUUID().toString());
		Identity ident2 = JunitTestHelper.createAndPersistIdentityAsUser("ucar-crud-2-" + UUID.randomUUID().toString());
		Identity ident3 = JunitTestHelper.createAndPersistIdentityAsUser("ucar-crud-3-" + UUID.randomUUID().toString());

		
		assertEquals(0, userRatingsDao.getRatingAverage(ores, null), 0.01);
		assertEquals(0, userRatingsDao.getRatingAverage(ores, "blubli"), 0.01);
		assertEquals(0l, userRatingsDao.countRatings(ores, null));
		assertEquals(0l, userRatingsDao.countRatings(ores, "blubli"));
		
		UserRating r1 = userRatingsDao.createRating(ident1, ores, null, 2);
		Assert.assertNotNull(r1);
		UserRating r2 = userRatingsDao.createRating(ident1, ores, "blubli", 2);
		Assert.assertNotNull(r2);
		assertEquals(2, userRatingsDao.getRatingAverage(ores, null), 0.01);
		assertEquals(2, userRatingsDao.getRatingAverage(ores, "blubli"), 0.01);
		assertEquals(1l, userRatingsDao.countRatings(ores, null));
		assertEquals(1l, userRatingsDao.countRatings(ores, "blubli"));
		//
		UserRating r3 = userRatingsDao.createRating(ident2, ores, null, 4);
		Assert.assertNotNull(r3);
		UserRating r4 = userRatingsDao.createRating(ident2, ores, "blubli", 4);
		Assert.assertNotNull(r4);
		assertEquals(3, userRatingsDao.getRatingAverage(ores, null), 0.01);
		assertEquals(3, userRatingsDao.getRatingAverage(ores, "blubli"), 0.01);
		assertEquals(2l, userRatingsDao.countRatings(ores, null));
		assertEquals(2l, userRatingsDao.countRatings(ores, "blubli"));
		// 
		UserRating r5 = userRatingsDao.createRating(ident3, ores, null, 1);
		Assert.assertNotNull(r5);
		UserRating r6 = userRatingsDao.createRating(ident3, ores, "blubli", 1);
		Assert.assertNotNull(r6);
		assertEquals(2.33f, userRatingsDao.getRatingAverage(ores, null), 0.01);
		assertEquals(2.33f, userRatingsDao.getRatingAverage(ores, "blubli"), 0.01);
		assertEquals(3l, userRatingsDao.countRatings(ores, null));
		assertEquals(3l, userRatingsDao.countRatings(ores, "blubli"));
		//
		assertNotNull(userRatingsDao.getRating(ident1, ores, null));
		assertNotNull(userRatingsDao.getRating(ident2, ores, null));
		assertNotNull(userRatingsDao.getRating(ident3, ores, null));
		// can !!not!! create two ratings per person
		r1 = userRatingsDao.createRating(ident1, ores, null, 2);
		r2 = userRatingsDao.createRating(ident1, ores, "blubli", 2);
		assertEquals(2.25f, userRatingsDao.getRatingAverage(ores, null), 0.01);
		assertEquals(2.25f, userRatingsDao.getRatingAverage(ores, "blubli"), 0.01);
		//can create 2 ratings
		assertEquals(4l, userRatingsDao.countRatings(ores, null));
		assertEquals(4l, userRatingsDao.countRatings(ores, "blubli"));
		
		// Delete ratings without subpath
		userRatingsDao.deleteAllRatings(ores, null);
		assertEquals(0f, userRatingsDao.getRatingAverage(ores, null), 0.01);
		assertEquals(2.25f, userRatingsDao.getRatingAverage(ores, "blubli"), 0.01);
		assertEquals(0l, userRatingsDao.countRatings(ores, null));
		assertEquals(4l, userRatingsDao.countRatings(ores, "blubli"));

		// Recreate and delete ignoring subpath
		r1 = userRatingsDao.createRating(ident1, ores, null, 2);
		r2 = userRatingsDao.createRating(ident1, ores, "blubli", 2);
		assertEquals(2f, userRatingsDao.getRatingAverage(ores, null), 0.01);
		assertEquals(2.2f, userRatingsDao.getRatingAverage(ores, "blubli"), 0.01);
		assertEquals(1, userRatingsDao.countRatings(ores, null));
		assertEquals(5l, userRatingsDao.countRatings(ores, "blubli"));
		userRatingsDao.deleteAllRatingsIgnoringSubPath(ores);
		assertEquals(0f, userRatingsDao.getRatingAverage(ores, null), 0.01);
		assertEquals(0f, userRatingsDao.getRatingAverage(ores, "blubli"), 0.01);
		assertEquals(0l, userRatingsDao.countRatings(ores, null));
		assertEquals(0l, userRatingsDao.countRatings(ores, "blubli"));
	}
}
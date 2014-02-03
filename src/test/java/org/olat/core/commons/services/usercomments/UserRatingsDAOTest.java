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
package org.olat.core.commons.services.usercomments;

import java.util.UUID;

import junit.framework.Assert;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.commentAndRating.impl.UserRatingsDAO;
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
	
}

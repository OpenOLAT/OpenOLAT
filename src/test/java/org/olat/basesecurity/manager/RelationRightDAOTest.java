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
package org.olat.basesecurity.manager;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.RelationRight;
import org.olat.core.commons.persistence.DB;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 28 janv. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RelationRightDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private RelationRightDAO relationRightDao;
	
	@Test
	public void createRelationRight() {
		String right = UUID.randomUUID().toString();
		RelationRight relationRight = relationRightDao.createRelationRight(right);
		dbInstance.commit();
		
		Assert.assertNotNull(relationRight);
		Assert.assertNotNull(relationRight.getKey());
		Assert.assertNotNull(relationRight.getCreationDate());
		Assert.assertEquals(right, relationRight.getRight());
	}
	
	@Test
	public void loadRelationRight_byKey() {
		String right = UUID.randomUUID().toString();
		RelationRight relationRight = relationRightDao.createRelationRight(right);
		dbInstance.commitAndCloseSession();
		
		RelationRight loadedRelationRight = relationRightDao.loadRelationRightByKey(relationRight.getKey());
		Assert.assertNotNull(loadedRelationRight);
		Assert.assertEquals(relationRight, loadedRelationRight);
		Assert.assertEquals(right, loadedRelationRight.getRight());
		Assert.assertEquals(relationRight.getKey(), loadedRelationRight.getKey());
	}
	
	@Test
	public void loadRelationRight_byRight() {
		String right = UUID.randomUUID().toString();
		RelationRight relationRight = relationRightDao.createRelationRight(right);
		dbInstance.commitAndCloseSession();
		
		RelationRight loadedRelationRight = relationRightDao.loadRelationRightByRight(right);
		Assert.assertNotNull(loadedRelationRight);
		Assert.assertEquals(relationRight, loadedRelationRight);
		Assert.assertEquals(right, loadedRelationRight.getRight());
		Assert.assertEquals(relationRight.getKey(), loadedRelationRight.getKey());
	}
	
	@Test
	public void loadRelationRights() {
		String right = UUID.randomUUID().toString();
		RelationRight relationRight = relationRightDao.createRelationRight(right);
		dbInstance.commitAndCloseSession();
		
		List<RelationRight> allRights = relationRightDao.loadRelationRights();
		Assert.assertNotNull(allRights);
		Assert.assertTrue(allRights.contains(relationRight));
	}
	
	@Test
	public void ensureRightsExists() {
		String right = "unitTestRight";
		relationRightDao.ensureRightExists(right);
		dbInstance.commitAndCloseSession();
		
		RelationRight loadedRight = relationRightDao.loadRelationRightByRight(right);
		Assert.assertNotNull(loadedRight);
	}
	
}

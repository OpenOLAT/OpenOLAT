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
import org.olat.basesecurity.IdentityToIdentityRelation;
import org.olat.basesecurity.RelationRole;
import org.olat.basesecurity.model.IdentityToIdentityRelationImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 28 janv. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IdentityToIdentityRelationDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private RelationRoleDAO relationRoleDao;
	@Autowired
	private IdentityToIdentityRelationDAO identityToIdentityRelationDao;
	
	@Test
	public void createIdentityToIdentityRelation() {
		String role = UUID.randomUUID().toString();
		RelationRole relationRole = relationRoleDao.createRelationRole(role, null, null, null);
		Identity idSource = JunitTestHelper.createAndPersistIdentityAsRndUser("id-2-id-1");
		Identity idTarget = JunitTestHelper.createAndPersistIdentityAsRndUser("id-2-id-2");
		dbInstance.commitAndCloseSession();
		
		IdentityToIdentityRelation relation = identityToIdentityRelationDao.createRelation(idSource, idTarget, relationRole, "External-id", "all");
		dbInstance.commit();
		
		Assert.assertNotNull(relation);
		Assert.assertNotNull(relation.getKey());
		Assert.assertNotNull(relation.getCreationDate());
		Assert.assertEquals(idSource, relation.getSource());
		Assert.assertEquals(idTarget, relation.getTarget());
		Assert.assertEquals(relationRole, relation.getRole());
	}
	
	@Test
	public void isUsed_yes() {
		String role = UUID.randomUUID().toString();
		RelationRole relationRole = relationRoleDao.createRelationRole(role, null, null, null);
		Identity idSource = JunitTestHelper.createAndPersistIdentityAsRndUser("id-2-id-1");
		Identity idTarget = JunitTestHelper.createAndPersistIdentityAsRndUser("id-2-id-2");
		IdentityToIdentityRelation relation = identityToIdentityRelationDao.createRelation(idSource, idTarget, relationRole, null, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(relation);
		
		boolean used = identityToIdentityRelationDao.isUsed(relationRole);
		Assert.assertTrue(used);
	}
	
	@Test
	public void isUsed_no() {
		String role = UUID.randomUUID().toString();
		RelationRole relationRole = relationRoleDao.createRelationRole(role, null, null, null);
		dbInstance.commitAndCloseSession();
		
		boolean used = identityToIdentityRelationDao.isUsed(relationRole);
		Assert.assertFalse(used);
	}
	
	@Test
	public void hasRelation() {
		String role = UUID.randomUUID().toString();
		RelationRole relationRole = relationRoleDao.createRelationRole(role, null, null, null);
		Identity idSource = JunitTestHelper.createAndPersistIdentityAsRndUser("id-2-id-1");
		Identity idTarget = JunitTestHelper.createAndPersistIdentityAsRndUser("id-2-id-2");
		IdentityToIdentityRelation relation = identityToIdentityRelationDao.createRelation(idSource, idTarget, relationRole, null, null);
		dbInstance.commitAndCloseSession();
		
		// check
		boolean hasRelation = identityToIdentityRelationDao.hasRelation(idSource, idTarget, relationRole);
		Assert.assertTrue(hasRelation);
		// double check
		boolean hasRelationAlt = identityToIdentityRelationDao.hasRelation(relation.getSource(), relation.getTarget(), relation.getRole());
		Assert.assertTrue(hasRelationAlt);
		// direction is important
		boolean hasReverseRelation = identityToIdentityRelationDao.hasRelation(idTarget, idSource, relationRole);
		Assert.assertFalse(hasReverseRelation);
	}
	
	@Test
	public void getRelation() {
		String role = UUID.randomUUID().toString();
		RelationRole relationRole = relationRoleDao.createRelationRole(role, null, null, null);
		Identity idSource = JunitTestHelper.createAndPersistIdentityAsRndUser("id-2-id-1");
		Identity idTarget = JunitTestHelper.createAndPersistIdentityAsRndUser("id-2-id-2");
		IdentityToIdentityRelation relation = identityToIdentityRelationDao.createRelation(idSource, idTarget, relationRole, "External-id", "all");
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(relation);
		
		IdentityToIdentityRelation loadedRelation = identityToIdentityRelationDao.getRelation(idSource, idTarget, relationRole);
		Assert.assertNotNull(loadedRelation);
		Assert.assertEquals(relation, loadedRelation);
		Assert.assertEquals("External-id", loadedRelation.getExternalId());
		Assert.assertEquals("all", ((IdentityToIdentityRelationImpl)loadedRelation).getManagedFlagsString());
		
		// direction is important
		IdentityToIdentityRelation reversedRelation = identityToIdentityRelationDao.getRelation(idTarget, idSource, relationRole);
		Assert.assertNull(reversedRelation);
	}
	
	@Test
	public void getRelationsAsSource() {
		String role = UUID.randomUUID().toString();
		RelationRole relationRole = relationRoleDao.createRelationRole(role, null, null, null);
		Identity idSource = JunitTestHelper.createAndPersistIdentityAsRndUser("id-2-id-1");
		Identity idTarget = JunitTestHelper.createAndPersistIdentityAsRndUser("id-2-id-2");
		IdentityToIdentityRelation relation = identityToIdentityRelationDao.createRelation(idSource, idTarget, relationRole, null, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(relation);
		
		List<IdentityToIdentityRelation> relations = identityToIdentityRelationDao.getRelationsAsSource(idSource);
		Assert.assertNotNull(relations);
		Assert.assertEquals(1, relations.size());
		Assert.assertEquals(relation, relations.get(0));
		
		// direction is important
		List<IdentityToIdentityRelation> reversedRelations = identityToIdentityRelationDao.getRelationsAsSource(idTarget);
		Assert.assertNotNull(reversedRelations);
		Assert.assertTrue(reversedRelations.isEmpty());
	}
	
	@Test
	public void getRelationsAsTarget() {
		String role = UUID.randomUUID().toString();
		RelationRole relationRole = relationRoleDao.createRelationRole(role, null, null, null);
		Identity idSource = JunitTestHelper.createAndPersistIdentityAsRndUser("id-2-id-1");
		Identity idTarget = JunitTestHelper.createAndPersistIdentityAsRndUser("id-2-id-2");
		IdentityToIdentityRelation relation = identityToIdentityRelationDao.createRelation(idSource, idTarget, relationRole, null, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(relation);
		
		List<IdentityToIdentityRelation> relations = identityToIdentityRelationDao.getRelationsAsTarget(idTarget);
		Assert.assertNotNull(relations);
		Assert.assertEquals(1, relations.size());
		Assert.assertEquals(relation, relations.get(0));
		
		// direction is important
		List<IdentityToIdentityRelation> reversedRelations = identityToIdentityRelationDao.getRelationsAsTarget(idSource);
		Assert.assertNotNull(reversedRelations);
		Assert.assertTrue(reversedRelations.isEmpty());
	}

}

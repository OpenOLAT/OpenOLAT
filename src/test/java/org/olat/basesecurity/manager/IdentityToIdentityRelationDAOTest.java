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

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.IdentityToIdentityRelation;
import org.olat.basesecurity.RelationRight;
import org.olat.basesecurity.RelationRole;
import org.olat.basesecurity.RelationSearchParams;
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
	private RelationRightDAO relationRightDao;
	@Autowired
	private IdentityToIdentityRelationDAO identityToIdentityRelationDao;
	
	@Test
	public void createIdentityToIdentityRelation() {
		String role = random();
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
		String role = random();
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
		String role = random();
		RelationRole relationRole = relationRoleDao.createRelationRole(role, null, null, null);
		dbInstance.commitAndCloseSession();
		
		boolean used = identityToIdentityRelationDao.isUsed(relationRole);
		Assert.assertFalse(used);
	}
	
	@Test
	public void hasRelation() {
		String role = random();
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
		String role = random();
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
		String role = random();
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
		String role = random();
		RelationRole relationRole = relationRoleDao.createRelationRole(role, null, null, null);
		Identity idSource = JunitTestHelper.createAndPersistIdentityAsRndUser("id-2-id-1");
		Identity idTarget = JunitTestHelper.createAndPersistIdentityAsRndUser("id-2-id-2");
		IdentityToIdentityRelation relation = identityToIdentityRelationDao.createRelation(idSource, idTarget, relationRole, null, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(relation);
		
		RelationSearchParams searchParams = new RelationSearchParams();
		List<IdentityToIdentityRelation> relations = identityToIdentityRelationDao.getRelationsAsTarget(idTarget, searchParams);
		Assert.assertNotNull(relations);
		Assert.assertEquals(1, relations.size());
		Assert.assertEquals(relation, relations.get(0));
		
		// direction is important
		List<IdentityToIdentityRelation> reversedRelations = identityToIdentityRelationDao.getRelationsAsTarget(idSource, searchParams);
		Assert.assertNotNull(reversedRelations);
		Assert.assertTrue(reversedRelations.isEmpty());
	}
	
	@Test
	public void getRelationsAsTarget_filterRight() {
		// Init roles
		RelationRole roleA = relationRoleDao.createRelationRole(random(), null, null, null);
		RelationRole roleB = relationRoleDao.createRelationRole(random(), null, null, null);
		RelationRole roleNoRight = relationRoleDao.createRelationRole(random(), null, null, null);
		// Init rights
		relationRightDao.ensureRightExists("testRight");
		RelationRight right = relationRightDao.loadRelationRightByRight("testRight");
		relationRightDao.ensureRightExists("testRightOther");
		RelationRight rightOther = relationRightDao.loadRelationRightByRight("testRightOther");
		relationRoleDao.addRight(roleA, right);
		relationRoleDao.addRight(roleB, right);
		relationRoleDao.addRight(roleNoRight, rightOther);
		// Init identities and relations
		Identity idTarget = JunitTestHelper.createAndPersistIdentityAsRndUser("target");
		Identity idSourceRoleA = JunitTestHelper.createAndPersistIdentityAsRndUser("idSourceA");
		identityToIdentityRelationDao.createRelation(idSourceRoleA, idTarget, roleA, null, null);
		Identity idSourceRoleB = JunitTestHelper.createAndPersistIdentityAsRndUser("idSourceB");
		identityToIdentityRelationDao.createRelation(idSourceRoleB, idTarget, roleB, null, null);
		Identity idSourceRoleOtherNoRight = JunitTestHelper.createAndPersistIdentityAsRndUser("idSourceRoleNoRight");
		identityToIdentityRelationDao.createRelation(idSourceRoleOtherNoRight, idTarget, roleNoRight, null, null);
		Identity idSourceOtherTarget = JunitTestHelper.createAndPersistIdentityAsRndUser("idSourceB");
		Identity idTargetOther = JunitTestHelper.createAndPersistIdentityAsRndUser("targetOther");
		identityToIdentityRelationDao.createRelation(idSourceOtherTarget, idTargetOther, roleA, null, null);
		dbInstance.commitAndCloseSession();
		
		RelationSearchParams searchParams = new RelationSearchParams();
		searchParams.setRight(right);
		List<IdentityToIdentityRelation> relations = identityToIdentityRelationDao.getRelationsAsTarget(idTarget, searchParams);
		
		assertThat(relations)
				.extracting(IdentityToIdentityRelation:: getSource)
				.containsExactlyInAnyOrder(
						idSourceRoleA,
						idSourceRoleB)
				.doesNotContain(
						idSourceRoleOtherNoRight,
						idSourceOtherTarget);
	}
	
	@Test
	public void getSources() {
		String role = random();
		RelationRole relationRole = relationRoleDao.createRelationRole(role, null, null, null);
		Identity idSource = JunitTestHelper.createAndPersistIdentityAsRndUser("id-2-id-8");
		Identity idTarget = JunitTestHelper.createAndPersistIdentityAsRndUser("id-2-id-9");
		IdentityToIdentityRelation relation = identityToIdentityRelationDao.createRelation(idSource, idTarget, relationRole, null, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(relation);
		
		List<Identity> targets = identityToIdentityRelationDao.getSources(relationRole);
		assertThat(targets)
			.containsExactly(idSource);
	}
	
	@Test
	public void getTargets() {
		String role = random();
		RelationRole relationRole = relationRoleDao.createRelationRole(role, null, null, null);
		Identity idSource = JunitTestHelper.createAndPersistIdentityAsRndUser("id-2-id-8");
		Identity idTarget = JunitTestHelper.createAndPersistIdentityAsRndUser("id-2-id-9");
		IdentityToIdentityRelation relation = identityToIdentityRelationDao.createRelation(idSource, idTarget, relationRole, null, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(relation);
		
		List<Identity> targets = identityToIdentityRelationDao.getTargets(relationRole);
		assertThat(targets)
			.containsExactly(idTarget);
	}

}

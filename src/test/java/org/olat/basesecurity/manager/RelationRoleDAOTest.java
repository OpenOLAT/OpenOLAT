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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.RelationRight;
import org.olat.basesecurity.RelationRole;
import org.olat.basesecurity.RelationRoleToRight;
import org.olat.core.commons.persistence.DB;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 28 janv. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RelationRoleDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private RelationRoleDAO relationRoleDao;
	@Autowired
	private RelationRightDAO relationRightDao;
	
	@Test
	public void createRelationRole() {
		String role = UUID.randomUUID().toString();
		RelationRole relationRole = relationRoleDao.createRelationRole(role, "External-id", "External-ref", null);
		dbInstance.commit();
		
		Assert.assertNotNull(relationRole);
		Assert.assertNotNull(relationRole.getKey());
		Assert.assertNotNull(relationRole.getCreationDate());
		Assert.assertEquals(role, relationRole.getRole());
		Assert.assertEquals("External-ref", relationRole.getExternalRef());
		Assert.assertEquals("External-id", relationRole.getExternalId());
	}
	
	@Test
	public void loadRelationRole_byKey() {
		String role = UUID.randomUUID().toString();
		RelationRole relationRole = relationRoleDao.createRelationRole(role, null, null, null);
		dbInstance.commitAndCloseSession();
		
		RelationRole loadedRelationRole = relationRoleDao.loadRelationRoleByKey(relationRole.getKey());
		
		Assert.assertNotNull(loadedRelationRole);
		Assert.assertEquals(relationRole, loadedRelationRole);
		Assert.assertEquals(role, loadedRelationRole.getRole());
	}	
	
	@Test
	public void loadRelationRole_byRole() {
		String role = UUID.randomUUID().toString();
		RelationRole relationRole = relationRoleDao.createRelationRole(role, null, null, null);
		dbInstance.commitAndCloseSession();
		
		RelationRole loadedRelationRole = relationRoleDao.loadRelationRoleByRole(role);
		
		Assert.assertNotNull(loadedRelationRole);
		Assert.assertEquals(relationRole, loadedRelationRole);
		Assert.assertEquals(role, loadedRelationRole.getRole());
	}
	
	@Test
	public void loadRelationRole_byRight() {
		String role1 = UUID.randomUUID().toString();
		String role2 = UUID.randomUUID().toString();
		String roleOther = UUID.randomUUID().toString();
		RelationRole relationRole1 = relationRoleDao.createRelationRole(role1, null, null, null);
		RelationRole relationRole2 = relationRoleDao.createRelationRole(role2, null, null, null);
		RelationRole relationRole3 = relationRoleDao.createRelationRole(roleOther, null, null, null);
		String right1 = UUID.randomUUID().toString();
		RelationRight relationRight1 = relationRightDao.createRelationRight(right1);
		String rightOther = UUID.randomUUID().toString();
		RelationRight relationRightOther = relationRightDao.createRelationRight(rightOther);
		relationRoleDao.addRight(relationRole1, relationRight1);
		relationRoleDao.addRight(relationRole1, relationRightOther);
		relationRoleDao.addRight(relationRole2, relationRight1);
		relationRoleDao.addRight(relationRole3, relationRightOther);
		dbInstance.commitAndCloseSession();
		
		List<RelationRole> byRight = relationRoleDao.loadRelationRolesByRight(right1);
		
		assertThat(byRight).containsExactlyInAnyOrder(relationRole1, relationRole2);
	}

	@Test
	public void addRight() {
		String role = UUID.randomUUID().toString();
		RelationRole relationRole = relationRoleDao.createRelationRole(role, null, null, null);
		String right = UUID.randomUUID().toString();
		RelationRight relationRight = relationRightDao.createRelationRight(right);
		relationRoleDao.addRight(relationRole, relationRight);
		dbInstance.commitAndCloseSession();
		
		RelationRole loadedRelationRole = relationRoleDao.loadRelationRoleByRole(role);
		
		Assert.assertNotNull(loadedRelationRole);
		Assert.assertEquals(relationRole, loadedRelationRole);
		Assert.assertEquals(role, loadedRelationRole.getRole());
		Assert.assertEquals(1, loadedRelationRole.getRights().size());
		
		RelationRoleToRight roleToRight = loadedRelationRole.getRights().iterator().next();
		Assert.assertEquals(relationRole, roleToRight.getRelationRole());
		Assert.assertEquals(relationRight, roleToRight.getRelationRight());
	}
	
	@Test
	public void setRights_once() {
		String role = UUID.randomUUID().toString();
		RelationRole relationRole = relationRoleDao.createRelationRole(role, null, null, null);
		String right = UUID.randomUUID().toString();
		RelationRight relationRight1 = relationRightDao.createRelationRight(right + "-1");
		RelationRight relationRight2 = relationRightDao.createRelationRight(right + "-2");
		dbInstance.commit();
		List<RelationRight> relationRights = new ArrayList<>();
		relationRights.add(relationRight1);
		relationRights.add(relationRight2);
		relationRoleDao.setRights(relationRole, relationRights);
		dbInstance.commitAndCloseSession();
		
		RelationRole loadedRelationRole = relationRoleDao.loadRelationRoleByRole(role);
		
		Assert.assertNotNull(loadedRelationRole);
		Assert.assertEquals(relationRole, loadedRelationRole);
		Assert.assertEquals(role, loadedRelationRole.getRole());
		Assert.assertEquals(2, loadedRelationRole.getRights().size());
		
		Set<RelationRoleToRight> roleToRights = loadedRelationRole.getRights();
		List<RelationRight> savedRights = roleToRights.stream()
				.map(RelationRoleToRight::getRelationRight).collect(Collectors.toList());
		Assert.assertTrue(savedRights.contains(relationRight1));
		Assert.assertTrue(savedRights.contains(relationRight2));
	}
	
	
	@Test
	public void setRights_twice() {
		String role = UUID.randomUUID().toString();
		RelationRole relationRole = relationRoleDao.createRelationRole(role, null, null, null);
		String right = UUID.randomUUID().toString();
		RelationRight relationRight1 = relationRightDao.createRelationRight(right + "-1");
		RelationRight relationRight2 = relationRightDao.createRelationRight(right + "-2");
		RelationRight relationRight3 = relationRightDao.createRelationRight(right + "-2");
		dbInstance.commit();
		List<RelationRight> relationRights = new ArrayList<>();
		relationRights.add(relationRight1);
		relationRights.add(relationRight2);
		relationRoleDao.setRights(relationRole, relationRights);
		dbInstance.commitAndCloseSession();
		
		// load with the first 2 rights
		RelationRole loadedRelationRole = relationRoleDao.loadRelationRoleByRole(role);
		
		Assert.assertNotNull(loadedRelationRole);
		Assert.assertEquals(relationRole, loadedRelationRole);
		Assert.assertEquals(role, loadedRelationRole.getRole());
		Assert.assertEquals(2, loadedRelationRole.getRights().size());
		
		Set<RelationRoleToRight> roleToRights = loadedRelationRole.getRights();
		List<RelationRight> savedRights = roleToRights.stream()
				.map(RelationRoleToRight::getRelationRight).collect(Collectors.toList());
		Assert.assertTrue(savedRights.contains(relationRight1));
		Assert.assertTrue(savedRights.contains(relationRight2));
		
		// set the 2 last rights
		List<RelationRight> updatedRelationRights = new ArrayList<>();
		updatedRelationRights.add(relationRight2);
		updatedRelationRights.add(relationRight3);
		relationRoleDao.setRights(loadedRelationRole, updatedRelationRights);
		dbInstance.commitAndCloseSession();
		
		// check the rights
		RelationRole reloadedRelationRole = relationRoleDao.loadRelationRoleByRole(role);
		Assert.assertNotNull(reloadedRelationRole);
		Assert.assertEquals(relationRole, reloadedRelationRole);
		Assert.assertEquals(role, reloadedRelationRole.getRole());
		Assert.assertEquals(2, reloadedRelationRole.getRights().size());
		
		Set<RelationRoleToRight> updatedRoleToRights = reloadedRelationRole.getRights();
		List<RelationRight> updatededRights = updatedRoleToRights.stream()
				.map(RelationRoleToRight::getRelationRight).collect(Collectors.toList());
		Assert.assertTrue(updatededRights.contains(relationRight2));
		Assert.assertTrue(updatededRights.contains(relationRight3));
	}
	
	@Test
	public void deleteRelationRole() {
		String role = UUID.randomUUID().toString();
		RelationRole relationRole = relationRoleDao.createRelationRole(role, null, null, null);
		String right = UUID.randomUUID().toString();
		RelationRight relationRight1 = relationRightDao.createRelationRight(right + "-1");
		RelationRight relationRight2 = relationRightDao.createRelationRight(right + "-2");
		RelationRight relationRight3 = relationRightDao.createRelationRight(right + "-2");
		dbInstance.commit();
		List<RelationRight> relationRights = new ArrayList<>();
		relationRights.add(relationRight1);
		relationRights.add(relationRight2);
		relationRights.add(relationRight3);
		relationRoleDao.setRights(relationRole, relationRights);
		dbInstance.commitAndCloseSession();
		
		// delete
		RelationRole loadedRole = relationRoleDao.loadRelationRoleByKey(relationRole.getKey());
		relationRoleDao.delete(loadedRole);
		dbInstance.commit();
		
		// check
		RelationRole deletedRole = relationRoleDao.loadRelationRoleByKey(relationRole.getKey());
		Assert.assertNull(deletedRole);
	}
}

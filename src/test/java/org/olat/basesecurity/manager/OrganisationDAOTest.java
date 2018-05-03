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
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.OrganisationType;
import org.olat.basesecurity.model.OrganisationImpl;
import org.olat.basesecurity.model.OrganisationMember;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 9 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OrganisationDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private OrganisationDAO organisationDao;
	@Autowired
	private OrganisationTypeDAO organisationTypeDao;
	@Autowired
	private OrganisationService organisationService;
	
	@Test
	public void createOrganisation() {
		Organisation organisation = organisationDao.createAndPersistOrganisation("Org-4", null, null, null, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(organisation);
		Assert.assertNotNull(organisation.getKey());
		Assert.assertNotNull(organisation.getCreationDate());
		Assert.assertNotNull(organisation.getLastModified());
	}
	
	@Test
	public void createOrganisation_allAttributes() {
		OrganisationType type = organisationTypeDao.createAndPersist("Org-Type", "OT");
		Organisation organisation = organisationDao
				.createAndPersistOrganisation("Org-5", "ORG-5", null, null, type);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(organisation);
		
		OrganisationImpl reloadedOrganisation = (OrganisationImpl)organisationDao.loadByKey(organisation.getKey());
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(reloadedOrganisation.getKey());
		Assert.assertNotNull(reloadedOrganisation.getCreationDate());
		Assert.assertNotNull(reloadedOrganisation.getLastModified());
		Assert.assertNotNull(reloadedOrganisation.getGroup());
		Assert.assertEquals("Org-5", reloadedOrganisation.getDisplayName());
		Assert.assertEquals("ORG-5", reloadedOrganisation.getIdentifier());
		Assert.assertEquals(type, reloadedOrganisation.getType());
	}
	
	@Test
	public void createOrganisationWithParent() {
		Organisation parentOrganisation = organisationDao.createAndPersistOrganisation("Org-10", null, null, null, null);
		Organisation organisation = organisationDao.createAndPersistOrganisation("Org-10", null, null, parentOrganisation, null);
		dbInstance.commitAndCloseSession();

		Assert.assertNotNull(organisation);
		Assert.assertNotNull(organisation.getKey());
		Assert.assertNotNull(organisation.getCreationDate());
		Assert.assertNotNull(organisation.getLastModified());
		
		// check the ad-hoc parent line
		List<OrganisationRef> parentLine = organisation.getParentLine();
		Assert.assertNotNull(parentLine);
		Assert.assertEquals(1, parentLine.size());
		Assert.assertEquals(parentOrganisation.getKey(), parentLine.get(0).getKey());
		
	}
	
	@Test
	public void loadByIdentifier() {
		String identifier = UUID.randomUUID().toString();
		Organisation organisation = organisationDao.createAndPersistOrganisation("Org-2", identifier, null, null, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(organisation);
		
		List<Organisation> organisations = organisationDao.loadByIdentifier(identifier);
		Assert.assertNotNull(organisations);
		Assert.assertEquals(1, organisations.size());
		Assert.assertEquals(organisation, organisations.get(0));
	}
	
	@Test
	public void findAllOrganisations() {
		String identifier = UUID.randomUUID().toString();
		Organisation organisation = organisationDao.createAndPersistOrganisation("Org-1", identifier, null, null, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(organisation);
		
		List<Organisation> allOrganisations = organisationDao.find();
		Assert.assertNotNull(allOrganisations);
		Assert.assertFalse(allOrganisations.isEmpty());
		Assert.assertTrue(allOrganisations.contains(organisation));
	}
	
	@Test
	public void getMembers() {
		Identity member = JunitTestHelper.createAndPersistIdentityAsRndUser("Member-1");
		String identifier = UUID.randomUUID().toString();
		Organisation organisation = organisationDao.createAndPersistOrganisation("OpenOLAT EE", identifier, null, null, null);
		dbInstance.commit();
		organisationService.addMember(organisation, member, OrganisationRoles.user);
		dbInstance.commitAndCloseSession();
		
		List<OrganisationMember> members = organisationDao.getMembers(organisation);
		Assert.assertNotNull(members);
		Assert.assertEquals(1, members.size());
		OrganisationMember organisationMember = members.get(0);
		Assert.assertEquals(member, organisationMember.getIdentity());
		Assert.assertEquals(OrganisationRoles.user.name(), organisationMember.getRole());
	}
	
	@Test
	public void getIdentities() {
		Identity member1 = JunitTestHelper.createAndPersistIdentityAsRndUser("Member-2");
		Identity member2 = JunitTestHelper.createAndPersistIdentityAsRndUser("Member-3");
		String identifier = UUID.randomUUID().toString();
		Organisation organisation = organisationDao.createAndPersistOrganisation("Org 6", identifier, null, null, null);
		dbInstance.commit();
		organisationService.addMember(organisation, member1, OrganisationRoles.groupmanager);
		organisationService.addMember(organisation, member2, OrganisationRoles.usermanager);
		dbInstance.commitAndCloseSession();
		
		List<Identity> userManagers = organisationDao.getIdentities(identifier, OrganisationRoles.usermanager.name());
		Assert.assertNotNull(userManagers);
		Assert.assertEquals(1, userManagers.size());
		Assert.assertEquals(member2, userManagers.get(0));
		Assert.assertFalse(userManagers.contains(member1));
	}
	
	@Test
	public void hasRole() {
		Identity member = JunitTestHelper.createAndPersistIdentityAsRndUser("Member-4");
		String identifier = UUID.randomUUID().toString();
		Organisation organisation = organisationDao.createAndPersistOrganisation("OpenOLAT E2E", identifier, null, null, null);
		dbInstance.commit();
		organisationService.addMember(organisation, member, OrganisationRoles.poolmanager);
		dbInstance.commitAndCloseSession();
		
		boolean isPoolManager = organisationDao.hasRole(member, identifier, OrganisationRoles.poolmanager.name());
		Assert.assertTrue(isPoolManager);
		boolean isUserManager = organisationDao.hasRole(member, identifier, OrganisationRoles.usermanager.name());
		Assert.assertFalse(isUserManager);
		boolean isNotPoolManager = organisationDao.hasRole(member, "something else", OrganisationRoles.poolmanager.name());
		Assert.assertFalse(isNotPoolManager);
	}
}

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
package org.olat.portfolio.manager;

import static org.junit.Assert.assertNotNull;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.Invitation;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.portfolio.model.structel.PortfolioStructure;
import org.olat.portfolio.model.structel.PortfolioStructureMap;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EPPolicyManagerTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private InvitationDAO invitationDao;
	@Autowired
	private EPPolicyManager policyManager;
	@Autowired
	private EPFrontendManager epFrontendManager;
	
	@Test
	public void getOwners() {
		//create a map
		Identity user = JunitTestHelper.createAndPersistIdentityAsRndUser("Policy-User-1-");
		PortfolioStructureMap originalMap = epFrontendManager.createAndPersistPortfolioDefaultMap(user, "Title", "Description");
		PortfolioStructure page1 = epFrontendManager.createAndPersistPortfolioPage(originalMap, "Page title", "Page description");
		assertNotNull(page1);
		dbInstance.commitAndCloseSession();

		List<Identity> owners = policyManager.getOwners(originalMap);
		Assert.assertNotNull(owners);
		Assert.assertEquals(1, owners.size());
		Assert.assertEquals(user, owners.get(0));
	}
	
	@Test
	public void isMapShared_HQL() {
		Identity user = JunitTestHelper.createAndPersistIdentityAsRndUser("Policy-User-2-");
		PortfolioStructureMap map = epFrontendManager.createAndPersistPortfolioDefaultMap(user, "Title", "Description");
		dbInstance.commitAndCloseSession();
		
		boolean shared = policyManager.isMapShared(map.getOlatResource());
		Assert.assertFalse(shared);
	}
	
	@Test
	public void createPolicy_invitation() {
		Identity user = JunitTestHelper.createAndPersistIdentityAsRndUser("Policy-User-2-");
		PortfolioStructureMap map = epFrontendManager.createAndPersistPortfolioDefaultMap(user, "Title", "Description");
		Invitation invitation = invitationDao.createAndPersistInvitation();
		dbInstance.commit();
		
		invitation.setFirstName("John");
		invitation.setLastName("Smith Portfolio");
		EPMapPolicy policy = new EPMapPolicy();
		policy.setType(EPMapPolicy.Type.invitation);
		policy.setInvitation(invitation);
		
		policyManager.updateMapPolicies(map, Collections.singletonList(policy));
		dbInstance.commitAndCloseSession();
		
		//check that the policy is saved
		List<EPMapPolicy> policies = policyManager.getMapPolicies(map);
		Assert.assertNotNull(policies);
		Assert.assertEquals(1, policies.size());
		EPMapPolicy invitationPolicy = policies.get(0);
		Assert.assertEquals(EPMapPolicy.Type.invitation, invitationPolicy.getType());
		
		//convert invitation to identity
		Identity invitee = invitationDao.createIdentityFrom(invitation, Locale.ENGLISH);
		dbInstance.commitAndCloseSession();

		//check is shared
		boolean shared = policyManager.isMapShared(map.getOlatResource());
		Assert.assertTrue(shared);
		
		boolean visible = epFrontendManager.isMapVisible(invitee, map.getOlatResource());
		Assert.assertTrue(visible);
	}
}
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
package org.olat.modules.invitation.manager;

import java.util.Locale;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.Invitation;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.invitation.InvitationService;
import org.olat.modules.invitation.InvitationTypeEnum;
import org.olat.modules.invitation.model.InvitationImpl;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InvitationDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GroupDAO groupDao;
	@Autowired
	private InvitationDAO invitationDao;
	@Autowired
	private InvitationService invitationService;
	
	@Test
	public void createAndPersistInvitation() {
		Invitation invitation = createDummyInvitation();
		Assert.assertNotNull(invitation);
		dbInstance.commit();
		
		Assert.assertNotNull(invitation);
		Assert.assertNotNull(invitation.getKey());
		Assert.assertNotNull(invitation.getBaseGroup());
		Assert.assertNotNull(invitation.getToken());
	}
	
	@Test
	public void findInvitation_token() {
		Invitation invitation = createDummyInvitation();
		Assert.assertNotNull(invitation);
		dbInstance.commitAndCloseSession();
		
		Invitation reloadedInvitation = invitationDao.findInvitation(invitation.getToken());
		Assert.assertNotNull(reloadedInvitation);
		Assert.assertNotNull(reloadedInvitation.getKey());
		Assert.assertNotNull(reloadedInvitation.getBaseGroup());
		Assert.assertEquals(invitation, reloadedInvitation);
		Assert.assertEquals(invitation.getToken(), reloadedInvitation.getToken());
	}
	
	@Test
	public void hasInvitationTestHQL() {
		String token = UUID.randomUUID().toString();
		boolean hasInvitation = invitationDao.hasInvitations(token);
		Assert.assertFalse(hasInvitation);
	}
	
	@Test
	public void isInvitee() {
		Identity admin = JunitTestHelper.createAndPersistIdentityAsRndAdmin("invitation-admin-1");
		
		Invitation invitation = invitationDao.createInvitation(InvitationTypeEnum.binder);
		String uuid = UUID.randomUUID().toString().replace("-", "");
		invitation.setFirstName("Fiona");
		invitation.setLastName("Laurence".concat(uuid));
		invitation.setMail(uuid.concat("@frentix.com"));

		Group group = groupDao.createGroup();
		Identity id2 = invitationService.getOrCreateIdentityAndPersistInvitation(invitation, group, Locale.ENGLISH, admin);
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("Invitee-2");
		dbInstance.commitAndCloseSession();
		
		boolean invitee = invitationDao.isInvitee(id2);
		Assert.assertTrue(invitee);
		boolean notInvitee = invitationDao.isInvitee(id1);
		Assert.assertFalse(notInvitee);
	}
	
	@Test
	public void countInvitations() {
		Invitation invitation = createDummyInvitation();
		dbInstance.commit();
		Assert.assertNotNull(invitation);
		
		long numOfInvitations = invitationDao.countInvitations();
		Assert.assertTrue(numOfInvitations > 0l);
	}
	
	@Test
	public void deleteInvitationByGroup() {
		Invitation invitation = createDummyInvitation();
		dbInstance.commit();
		Assert.assertNotNull(invitation);
		
		invitationDao.deleteInvitation(invitation.getBaseGroup());
		dbInstance.commit();
		
		Invitation deletedInvitation = invitationDao.findInvitation(invitation.getToken());
		Assert.assertNull(deletedInvitation);
	}
	

	private Invitation createDummyInvitation() {
		Group group = groupDao.createGroup();
		InvitationImpl invitation = (InvitationImpl)invitationDao.createInvitation(InvitationTypeEnum.binder);
		invitation.setBaseGroup(group);
		dbInstance.getCurrentEntityManager().persist(invitation);
		return invitation;
	}
}

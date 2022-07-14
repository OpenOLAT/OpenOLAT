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
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.Invitation;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.invitation.InvitationService;
import org.olat.modules.invitation.InvitationTypeEnum;
import org.olat.modules.invitation.model.InvitationImpl;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 8 juil. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InvitationServiceTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GroupDAO groupDao;
	@Autowired
	private InvitationDAO invitationDao;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private InvitationService invitationService;
	
	@Test
	public void createIdentityFrom_invitation() {
		Invitation invitation = createDummyInvitation();
		String uuid = UUID.randomUUID().toString().replace("-", "");
		invitation = invitationService.update(invitation, "Clara", uuid, uuid + "@frentix.com");
		dbInstance.commit();
		
		// create the identity of the invitee
		Identity invitee = invitationService.createIdentityFrom(invitation, Locale.ENGLISH);
		Assert.assertNotNull(invitee);
		Assert.assertNotNull(invitee.getKey());
		dbInstance.commitAndCloseSession();
		
		// reload and check
		Identity reloadIdentity = securityManager.loadIdentityByKey(invitee.getKey());
		Assert.assertNotNull(reloadIdentity);
		Assert.assertNotNull(reloadIdentity.getUser());
		Assert.assertEquals(invitee.getKey(), reloadIdentity.getKey());
		Assert.assertEquals("Clara", reloadIdentity.getUser().getFirstName());
		Assert.assertEquals(uuid, reloadIdentity.getUser().getLastName());
		Assert.assertEquals(uuid + "@frentix.com", reloadIdentity.getUser().getEmail());
	}
	
	@Test
	public void createAndUpdateInvitation() {
		Invitation invitation = createDummyInvitation();
		dbInstance.commit();

		Invitation updatedInvitation = invitationService.update(invitation, "Kanu", "Unchou", "kanu.unchou@frentix.com");
		dbInstance.commit();
		
		Assert.assertEquals("Kanu", updatedInvitation.getFirstName());
		Assert.assertEquals("Unchou", updatedInvitation.getLastName());
		Assert.assertEquals("kanu.unchou@frentix.com", updatedInvitation.getMail());
		
		Invitation reloadedInvitation = invitationDao.findInvitation(invitation.getToken());
		Assert.assertEquals("Kanu", reloadedInvitation.getFirstName());
		Assert.assertEquals("Unchou", reloadedInvitation.getLastName());
		Assert.assertEquals("kanu.unchou@frentix.com", reloadedInvitation.getMail());
	}
	
	@Test
	public void loadOrCreateIdentityAndPersistInvitation() {
		Invitation invitation = createDummyInvitation();
		String uuid = UUID.randomUUID().toString().replace("-", "");
		invitation = invitationService.update(invitation, "Flora", uuid, uuid + "@frentix.com");
		Group group = groupDao.createGroup();
		dbInstance.commit();
		
		// use the create part of the method
		Identity identity = invitationService.getOrCreateIdentityAndPersistInvitation(invitation, group, Locale.ENGLISH);
		Assert.assertNotNull(identity);
		Assert.assertNotNull(identity.getKey());
		dbInstance.commitAndCloseSession();
		
		// reload and check
		Identity reloadIdentity = securityManager.loadIdentityByKey(identity.getKey());
		Assert.assertNotNull(reloadIdentity);
		Assert.assertNotNull(reloadIdentity.getUser());
		Assert.assertEquals(identity.getKey(), reloadIdentity.getKey());
		Assert.assertEquals("Flora", reloadIdentity.getUser().getFirstName());
		Assert.assertEquals(uuid, reloadIdentity.getUser().getLastName());
		Assert.assertEquals(uuid + "@frentix.com", reloadIdentity.getUser().getEmail());
	}
	
	private Invitation createDummyInvitation() {
		Group group = groupDao.createGroup();
		InvitationImpl invitation = (InvitationImpl)invitationDao.createInvitation(InvitationTypeEnum.binder);
		invitation.setBaseGroup(group);
		dbInstance.getCurrentEntityManager().persist(invitation);
		return invitation;
	}

}

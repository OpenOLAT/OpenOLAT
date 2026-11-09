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
package org.olat.restapi;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.Invitation;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.modules.invitation.InvitationService;
import org.olat.modules.invitation.InvitationTypeEnum;
import org.olat.modules.invitation.restapi.InvitationVO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 16 déc. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BusinessGroupInvitationsWebServiceTest extends OlatRestTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private InvitationService invitationService;
	@Autowired
	private BusinessGroupService businessGroupService;
	
	@Test
	public void createInvitationInBusinessGroup()
	throws IOException, URISyntaxException, InterruptedException {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("Coach-inv-1");
		BusinessGroup group = businessGroupService.createBusinessGroup(owner, "Invitations-1", "REST invitation",
				BusinessGroup.BUSINESS_TYPE, null, null, false, false, null);
		dbInstance.commit();
	
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		String email = "cedric.ferro@" + UUID.randomUUID();
		URI uri = UriBuilder.fromUri(getContextURI())
				.path("groups").path(group.getKey().toString()).path("invitations")
				.queryParam("firstName", "Cedric").queryParam("lastName", "Ferro")
				.queryParam("email", email).queryParam("registrationRequired", "false")
				.build();
		
		HttpRequest method = conn.createPost(uri, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());
		InvitationVO invitation = conn.parse(response, InvitationVO.class);
		
		Assert.assertNotNull(invitation);
		Assert.assertNotNull(invitation.getToken());
		Assert.assertEquals("Cedric", invitation.getFirstName());
		Assert.assertEquals("Ferro", invitation.getLastName());
		Assert.assertEquals(email, invitation.getEmail());
		Assert.assertNotNull(invitation.getUrl());

		Invitation savedInvitation = invitationService.findInvitation(invitation.getToken());
		Assert.assertNotNull(savedInvitation);
		Assert.assertEquals(invitation.getKey(), savedInvitation.getKey());
		conn.close();
	}
	
	@Test
	public void createInvitationVOInBusinessGroup()
	throws IOException, URISyntaxException, InterruptedException {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("Coach-inv-2");
		BusinessGroup businessGroup = businessGroupService.createBusinessGroup(owner, "Invitations-2", "REST invitation",
				BusinessGroup.BUSINESS_TYPE, null, null, false, false, null);
		Assert.assertNotNull(businessGroup);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection("administrator", "openolat");

		String email = "hal.f@" + UUID.randomUUID();
		InvitationVO invitationVo = new InvitationVO();
		invitationVo.setFirstName("Hal");
		invitationVo.setLastName("Ferro");
		invitationVo.setEmail(email);
		invitationVo.setRegistration(Boolean.FALSE);

		URI uri = UriBuilder.fromUri(getContextURI())
				.path("groups").path(businessGroup.getKey().toString()).path("invitations")
				.build();
		
		HttpRequest method = conn.createPost(uri, invitationVo, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());
		InvitationVO savedInvitationVo = conn.parse(response, InvitationVO.class);
		
		Assert.assertNotNull(savedInvitationVo);
		Assert.assertNotNull(savedInvitationVo.getToken());
		Assert.assertEquals("Hal", savedInvitationVo.getFirstName());
		Assert.assertEquals("Ferro", savedInvitationVo.getLastName());
		Assert.assertEquals(email, savedInvitationVo.getEmail());
		Assert.assertNotNull(savedInvitationVo.getUrl());

		Invitation savedInvitation = invitationService.findInvitation(savedInvitationVo.getToken());
		Assert.assertNotNull(savedInvitation);
		conn.close();
	}
	
	@Test
	public void updateInvitationVOInBusinessGroup()
	throws IOException, URISyntaxException, InterruptedException {	
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("Coach-inv-3");
		BusinessGroup businessGroup = businessGroupService.createBusinessGroup(owner, "Invitations-3", "REST invitation",
				BusinessGroup.BUSINESS_TYPE, null, null, false, false, null);
		
		Invitation tmpInvitation = invitationService.createInvitation(InvitationTypeEnum.businessGroup);
		
		String email = "jeremy.f@" + UUID.randomUUID();
		tmpInvitation.setFirstName("Jeremy");
		tmpInvitation.setLastName("Ferro");
		tmpInvitation.setMail(email);
		
		Group baseGroup = businessGroup.getBaseGroup();
		Identity id = invitationService.getOrCreateIdentityAndPersistInvitation(tmpInvitation, baseGroup, Locale.GERMAN, owner);
		
		Invitation invitation = invitationService.findInvitation(tmpInvitation.getToken());
		Assert.assertNotNull(invitation);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		InvitationVO invitationVo = new InvitationVO();
		invitationVo.setKey(invitation.getKey());
		invitationVo.setFirstName("JJ");
		invitationVo.setLastName("FF");
		String newEmail = "jj.ff@" + UUID.randomUUID();
		invitationVo.setEmail(newEmail);
		invitationVo.setRegistration(Boolean.FALSE);

		URI uri = UriBuilder.fromUri(getContextURI())
				.path("groups").path(businessGroup.getKey().toString()).path("invitations")
				.build();
		
		HttpRequest method = conn.createPost(uri, invitationVo, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());
		InvitationVO updatedInvitationVo = conn.parse(response, InvitationVO.class);
		
		Assert.assertNotNull(updatedInvitationVo);
		Assert.assertNotNull(updatedInvitationVo.getToken());
		Assert.assertEquals(invitation.getKey(), updatedInvitationVo.getKey());
		
		Invitation updatedInvitation = invitationService.getInvitationByKey(invitation.getKey());
		Assert.assertNotNull(updatedInvitation);
		Assert.assertEquals("JJ", updatedInvitation.getFirstName());
		Assert.assertEquals("FF", updatedInvitation.getLastName());
		Assert.assertEquals(newEmail, updatedInvitation.getMail());
		
		Identity updatedIdentity = securityManager.loadIdentityByKey(id.getKey());
		Assert.assertNotNull(updatedIdentity);
		Assert.assertEquals("JJ", updatedIdentity.getUser().getFirstName());
		Assert.assertEquals("FF", updatedIdentity.getUser().getLastName());
		Assert.assertEquals(newEmail, updatedIdentity.getUser().getEmail());
		conn.close();
	}
	
	@Test
	public void getListOfInvitationsInBusinessGroup()
	throws IOException, URISyntaxException, InterruptedException {
		Identity admin = JunitTestHelper.findIdentityByLogin("administrator");
		
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("Coach-inv-4");
		BusinessGroup businessGroup = businessGroupService.createBusinessGroup(owner, "Invitations-4", "REST invitation",
				BusinessGroup.BUSINESS_TYPE, null, null, false, false, null);
		Invitation tmpInvitation = invitationService.createInvitation(InvitationTypeEnum.businessGroup);
		Group baseGroup = businessGroup.getBaseGroup();
		Identity id = invitationService.getOrCreateIdentityAndPersistInvitation(tmpInvitation, baseGroup, Locale.GERMAN, admin);
		dbInstance.commitAndCloseSession();
		
		URI uri = UriBuilder.fromUri(getContextURI())
				.path("groups").path(businessGroup.getKey().toString()).path("invitations")
				.build();
		
		RestConnection conn = new RestConnection("administrator", "openolat");
		HttpRequest method = conn.createGet(uri, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());
		List<InvitationVO> invitations = conn.parseList(response, InvitationVO.class);
		Assert.assertNotNull(invitations);
		Assert.assertEquals(1, invitations.size());
		
		InvitationVO invitationVo = invitations.get(0);
		Assert.assertEquals(id.getKey(), invitationVo.getIdentityKey());
		Assert.assertEquals(id.getUser().getFirstName(), invitationVo.getFirstName());
		Assert.assertEquals(id.getUser().getLastName(), invitationVo.getLastName());
		Assert.assertEquals(id.getUser().getEmail(), invitationVo.getEmail());
		Assert.assertNotNull(invitationVo.getToken());
		Assert.assertNotNull(invitationVo.getUrl());
		conn.close();
	}

	@Test
	public void getInvitationInBusinessGroupByKey()
	throws IOException, URISyntaxException, InterruptedException {
		Identity admin = JunitTestHelper.findIdentityByLogin("administrator");
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("Coach-inv-5");
		BusinessGroup businessGroup = businessGroupService.createBusinessGroup(owner, "Invitations-5", "REST invitation",
				BusinessGroup.BUSINESS_TYPE, null, null, false, false, null);
		Invitation tmpInvitation = invitationService.createInvitation(InvitationTypeEnum.businessGroup);
		Group baseGroup = businessGroup.getBaseGroup();
		Identity id = invitationService.getOrCreateIdentityAndPersistInvitation(tmpInvitation, baseGroup, Locale.GERMAN, admin);
		dbInstance.commitAndCloseSession();
		
		Invitation invitation = invitationService.findInvitation(tmpInvitation.getToken());
		
		URI uri = UriBuilder.fromUri(getContextURI())
				.path("groups").path(businessGroup.getKey().toString())
				.path("invitations").path(invitation.getKey().toString())
				.build();
		
		RestConnection conn = new RestConnection("administrator", "openolat");
		HttpRequest method = conn.createGet(uri, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());
		InvitationVO invitationVo = conn.parse(response, InvitationVO.class);
		Assert.assertEquals(invitation.getKey(), invitationVo.getKey());
		Assert.assertEquals(id.getKey(), invitationVo.getIdentityKey());
		Assert.assertEquals(id.getUser().getFirstName(), invitationVo.getFirstName());
		Assert.assertEquals(id.getUser().getLastName(), invitationVo.getLastName());
		Assert.assertEquals(id.getUser().getEmail(), invitationVo.getEmail());
		Assert.assertNotNull(invitationVo.getToken());
		Assert.assertNotNull(invitationVo.getUrl());
		conn.close();
	}
	
	@Test
	public void deleteInvitationInBusinessgroupByKey()
	throws IOException, URISyntaxException, InterruptedException {
		Identity admin = JunitTestHelper.findIdentityByLogin("administrator");
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("Coach-inv-6");
		BusinessGroup businessGroup = businessGroupService.createBusinessGroup(owner, "Invitations-6", "REST invitation",
				BusinessGroup.BUSINESS_TYPE, null, null, false, false, null);
		
		Invitation tmpInvitation = invitationService.createInvitation(InvitationTypeEnum.businessGroup);
		Group baseGroup = businessGroup.getBaseGroup();
		Identity id = invitationService.getOrCreateIdentityAndPersistInvitation(tmpInvitation, baseGroup, Locale.GERMAN, admin);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(id);
		
		Invitation invitation = invitationService.findInvitation(tmpInvitation.getToken());
		Assert.assertNotNull(invitation);
		
		URI uri = UriBuilder.fromUri(getContextURI())
				.path("groups").path(businessGroup.getKey().toString())
				.path("invitations").path(invitation.getKey().toString())
				.build();
		
		RestConnection conn = new RestConnection("administrator", "openolat");
		HttpRequest method = conn.createDelete(uri, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());
		RestConnection.consume(response);
		
		Invitation deletedInvitation = invitationService.findInvitation(tmpInvitation.getToken());
		Assert.assertNull(deletedInvitation);
		conn.close();
	}
}

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.olat.admin.securitygroup.gui.IdentitiesAddEvent;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.util.mail.MailPackage;
import org.olat.fileresource.types.ImsQTI21Resource;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.manager.TaxonomyDAO;
import org.olat.modules.taxonomy.manager.TaxonomyLevelDAO;
import org.olat.modules.taxonomy.restapi.TaxonomyLevelVO;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryEducationalType;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryEntryToTaxonomyLevel;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.manager.RepositoryEntryToTaxonomyLevelDAO;
import org.olat.restapi.support.vo.RepositoryEntryEducationalTypeVO;
import org.olat.restapi.support.vo.RepositoryEntryMetadataVO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatRestTestCase;
import org.olat.user.restapi.UserVO;
import org.olat.user.restapi.UserVOFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * Initial date: 2 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryWebServiceTest extends OlatRestTestCase {

	private static final Logger log = Tracing.createLoggerFor(RepositoryEntryWebServiceTest.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private TaxonomyDAO taxonomyDao;
	@Autowired
	private TaxonomyLevelDAO taxonomyLevelDao;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private RepositoryEntryToTaxonomyLevelDAO repositoryEntryToTaxonomyLevelDao;
	
	@Test
	public void exportCourse()
	throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("course-owner");
		RepositoryEntry course = JunitTestHelper.deployBasicCourse(author);
		dbInstance.closeSession();
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo").path("entries")
				.path(course.getKey().toString()).path("file").build();
		HttpGet method = conn.createGet(request, "application/zip", true);
		HttpResponse response = conn.execute(method);

		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		byte[] exportedFile = EntityUtils.toByteArray(response.getEntity());
		Assert.assertTrue(exportedFile.length > 1000);	
	}
	
	@Test
	public void exportQTI21Test()
	throws IOException, URISyntaxException {
		//deploy QTI 2.1 test
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("test-owner");
		URL testUrl = JunitTestHelper.class.getResource("file_resources/qti21/simple_QTI_21_hotspot.zip");
		File testFile = new File(testUrl.toURI());		
		RepositoryHandler courseHandler = RepositoryHandlerFactory.getInstance()
						.getRepositoryHandler(ImsQTI21Resource.TYPE_NAME);
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry testEntry = courseHandler.importResource(author, null, "Test QTI 2.1", "", true, defOrganisation, Locale.ENGLISH, testFile, null);
		dbInstance.closeSession();
		
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo").path("entries")
				.path(testEntry.getKey().toString()).path("file").build();
		HttpGet method = conn.createGet(request, "application/zip", true);
		HttpResponse response = conn.execute(method);

		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		byte[] exportedFile = EntityUtils.toByteArray(response.getEntity());
		Assert.assertTrue(exportedFile.length > 1000);
	}
	

	@Test
	public void getOwners() throws IOException, URISyntaxException {
		Identity owner1 = JunitTestHelper.createAndPersistIdentityAsRndAuthor("author-1");
		Identity owner2 = JunitTestHelper.createAndPersistIdentityAsRndAuthor("author-2");
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryManager.addOwners(owner1, new IdentitiesAddEvent(owner1), re, null);
		repositoryManager.addOwners(owner1, new IdentitiesAddEvent(owner2), re, null);
		dbInstance.commitAndCloseSession();

		//get the owners
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo/entries").path(re.getKey().toString()).path("owners").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<UserVO> users = parseUserArray(response.getEntity());
		Assert.assertNotNull(users);
		Assert.assertEquals(2, users.size());//our 2
		
		int found = 0;
		for(UserVO user:users) {
			Long userKey = user.getKey();
			Assert.assertNotNull(userKey);
			if(owner1.getKey().equals(userKey) || owner2.getKey().equals(userKey)) {
				found++;
			}
		}
		Assert.assertEquals(2, found);
		
		conn.shutdown();
	}
	
	@Test
	public void addOwner() throws IOException, URISyntaxException {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndAuthor("author-3");
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();

		//add an owner
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI())
				.path("repo/entries").path(re.getKey().toString()).path("owners").path(owner.getKey().toString())
				.build();
		
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());

		conn.shutdown();
		
		//check
		List<Identity> owners = repositoryService.getMembers(re, RepositoryEntryRelationType.defaultGroup, GroupRoles.owner.name());
		Assert.assertNotNull(owners);
		Assert.assertEquals(1, owners.size());
		Assert.assertTrue(owners.contains(owner));
	}
	
	@Test
	public void addOwners() throws IOException, URISyntaxException {
		Identity owner1 = JunitTestHelper.createAndPersistIdentityAsRndUser("author-3b-");
		Identity owner2 = JunitTestHelper.createAndPersistIdentityAsRndUser("author-3c-");
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();

		//add an owner
		UserVO[] newOwners = new UserVO[2];
		newOwners[0] = UserVOFactory.get(owner1);
		newOwners[1] = UserVOFactory.get(owner2);
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI())
				.path("repo").path("entries").path(re.getKey().toString()).path("owners").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, newOwners);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());

		conn.shutdown();
		
		//check
		List<Identity> owners = repositoryService.getMembers(re, RepositoryEntryRelationType.defaultGroup, GroupRoles.owner.name());
		Assert.assertNotNull(owners);
		Assert.assertEquals(2, owners.size());
		Assert.assertTrue(owners.contains(owner1));
		Assert.assertTrue(owners.contains(owner2));
	}
	
	@Test
	public void removeOwner() throws IOException, URISyntaxException {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndAuthor("author-4-");
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryManager.addOwners(owner, new IdentitiesAddEvent(owner), re, new MailPackage(false));
		dbInstance.commitAndCloseSession();

		//remove the owner
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI())
				.path("repo/entries").path(re.getKey().toString()).path("owners").path(owner.getKey().toString()).build();
		HttpDelete method = conn.createDelete(request, MediaType.APPLICATION_JSON);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		conn.shutdown();
		
		//check
		List<Identity> owners = repositoryService.getMembers(re, RepositoryEntryRelationType.defaultGroup, GroupRoles.owner.name());
		Assert.assertNotNull(owners);
		Assert.assertEquals(0, owners.size());
		Assert.assertFalse(owners.contains(owner));
	}
	
	@Test
	public void getCoaches() throws IOException, URISyntaxException {
		Identity coach1 = JunitTestHelper.createAndPersistIdentityAsRndAuthor("coach-1");
		Identity coach2 = JunitTestHelper.createAndPersistIdentityAsRndAuthor("coach-2");
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryManager.addTutors(coach1, Roles.administratorRoles(), new IdentitiesAddEvent(coach1), re, new MailPackage(false));
		repositoryManager.addTutors(coach1, Roles.administratorRoles(), new IdentitiesAddEvent(coach2), re, new MailPackage(false));
		dbInstance.commitAndCloseSession();

		//get the coaches
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo/entries").path(re.getKey().toString()).path("coaches").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<UserVO> users = parseUserArray(response.getEntity());
		Assert.assertNotNull(users);
		Assert.assertEquals(2, users.size());//our 2
		
		int found = 0;
		for(UserVO user:users) {
			Long identityKey = user.getKey();
			Assert.assertNotNull(identityKey);
			if(coach1.getKey().equals(identityKey) || coach2.getKey().equals(identityKey)) {
				found++;
			}
		}
		Assert.assertEquals(2, found);
		
		conn.shutdown();
	}
	
	@Test
	public void addCoach() throws IOException, URISyntaxException {
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndAuthor("coach-3");
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();

		//add an owner
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI())
				.path("repo/entries").path(re.getKey().toString()).path("coaches").path(coach.getKey().toString())
				.build();
		
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());

		conn.shutdown();
		
		//check
		List<Identity> coaches = repositoryService.getMembers(re, RepositoryEntryRelationType.defaultGroup, GroupRoles.coach.name());
		Assert.assertNotNull(coaches);
		Assert.assertEquals(1, coaches.size());
		Assert.assertTrue(coaches.contains(coach));
	}
	
	@Test
	public void addCoaches() throws IOException, URISyntaxException {
		Identity coach1 = JunitTestHelper.createAndPersistIdentityAsRndUser("coach-3b-");
		Identity coach2 = JunitTestHelper.createAndPersistIdentityAsRndUser("coach-3c-");
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();

		//add an owner
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));
		
		UserVO[] newCoaches = new UserVO[2];
		newCoaches[0] = UserVOFactory.get(coach1);
		newCoaches[1] = UserVOFactory.get(coach2);
		
		URI request = UriBuilder.fromUri(getContextURI())
				.path("repo").path("entries").path(re.getKey().toString()).path("coaches").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, newCoaches);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());

		conn.shutdown();
		
		//check
		List<Identity> coaches = repositoryService.getMembers(re, RepositoryEntryRelationType.defaultGroup, GroupRoles.coach.name());
		Assert.assertNotNull(coaches);
		Assert.assertEquals(2, coaches.size());
		Assert.assertTrue(coaches.contains(coach1));
		Assert.assertTrue(coaches.contains(coach2));
	}
	
	@Test
	public void removeCoach() throws IOException, URISyntaxException {
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndAuthor("coach-4");
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryManager.addTutors(coach, Roles.administratorRoles(), new IdentitiesAddEvent(coach), re, new MailPackage(false));
		dbInstance.commitAndCloseSession();

		//remove the owner
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI())
				.path("repo/entries").path(re.getKey().toString()).path("coaches").path(coach.getKey().toString()).build();
		HttpDelete method = conn.createDelete(request, MediaType.APPLICATION_JSON);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		conn.shutdown();
		
		//check
		List<Identity> coaches = repositoryService.getMembers(re, RepositoryEntryRelationType.defaultGroup, GroupRoles.coach.name());
		Assert.assertNotNull(coaches);
		Assert.assertTrue(coaches.isEmpty());
		Assert.assertFalse(coaches.contains(coach));
	}
	
	@Test
	public void getParticipants() throws IOException, URISyntaxException {
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndAuthor("participant-1");
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndAuthor("participant-2");
		Roles part1Roles = securityManager.getRoles(participant1);
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryManager.addParticipants(participant1, part1Roles, new IdentitiesAddEvent(participant1), re, null);
		repositoryManager.addParticipants(participant1, part1Roles, new IdentitiesAddEvent(participant2), re, null);
		dbInstance.commitAndCloseSession();

		//get the coaches
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo/entries").path(re.getKey().toString()).path("participants").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<UserVO> users = parseUserArray(response.getEntity());
		Assert.assertNotNull(users);
		Assert.assertEquals(2, users.size());//our 2 
		
		int found = 0;
		for(UserVO user:users) {
			Long identityKey = user.getKey();
			Assert.assertNotNull(identityKey);
			if(participant1.getKey().equals(identityKey) || participant2.getKey().equals(identityKey)) {
				found++;
			}
		}
		Assert.assertEquals(2, found);
		conn.shutdown();
	}
	
	@Test
	public void addParticipant() throws IOException, URISyntaxException {
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndAuthor("participant-3");
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();

		//add an owner
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI())
				.path("repo/entries").path(re.getKey().toString()).path("participants").path(participant.getKey().toString())
				.build();
		
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());

		conn.shutdown();
		
		//check
		List<Identity> participants = repositoryService.getMembers(re, RepositoryEntryRelationType.defaultGroup, GroupRoles.participant.name());
		Assert.assertNotNull(participants);
		Assert.assertEquals(1, participants.size());
		Assert.assertTrue(participants.contains(participant));
	}
	
	@Test
	public void addParticipants() throws IOException, URISyntaxException {
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("participant-3b-");
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("participant-3c-");
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();

		//add an owner
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));

		//add the 2 participants to the course
		UserVO[] newParticipants = new UserVO[2];
		newParticipants[0] = UserVOFactory.get(participant1);
		newParticipants[1] = UserVOFactory.get(participant2);
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo/entries")
				.path(re.getKey().toString()).path("participants").build();
		
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, newParticipants);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());

		conn.shutdown();
		
		//check
		List<Identity> participants = repositoryService.getMembers(re, RepositoryEntryRelationType.defaultGroup, GroupRoles.participant.name());
		Assert.assertNotNull(participants);
		Assert.assertEquals(2, participants.size());
		Assert.assertTrue(participants.contains(participant1));
		Assert.assertTrue(participants.contains(participant2));
	}
	
	@Test
	public void testRemoveParticipant() throws IOException, URISyntaxException {
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndAuthor("participant-4");
		Roles partRoles = securityManager.getRoles(participant);
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryManager.addParticipants(participant, partRoles, new IdentitiesAddEvent(participant), re, null);
		dbInstance.commitAndCloseSession();

		//remove the owner
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI())
				.path("repo/entries").path(re.getKey().toString()).path("participants").path(participant.getKey().toString()).build();
		HttpDelete method = conn.createDelete(request, MediaType.APPLICATION_JSON);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		conn.shutdown();
		
		//check
		List<Identity> participants = repositoryService.getMembers(re, RepositoryEntryRelationType.defaultGroup, GroupRoles.participant.name());
		Assert.assertNotNull(participants);
		Assert.assertTrue(participants.isEmpty());
		Assert.assertFalse(participants.contains(participant));
	}
	
	
	@Test
	public void getMetadata() throws IOException, URISyntaxException {
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();
		
		List<RepositoryEntryEducationalType> educationalTypes = repositoryManager.getAllEducationalTypes();
		RepositoryEntryEducationalType educationalType = educationalTypes.get(0);
		re = repositoryManager.setDescriptionAndName(re, re.getDisplayname(), "Ext-REF", "Auth",
				"RE description", "RE teaser", "RE objectives", "RE requirements", "RE credits", "DE", "Zurich", "3 days",
				null, null, null, educationalType);
		dbInstance.commitAndCloseSession();

		//remove the owner
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI())
				.path("repo/entries").path(re.getKey().toString()).path("metadata").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		RepositoryEntryMetadataVO metadataVo = conn.parse(response, RepositoryEntryMetadataVO.class);
		conn.shutdown();
		
		//check
		Assert.assertNotNull(metadataVo);
		Assert.assertEquals(re.getKey(), metadataVo.getKey());
		Assert.assertEquals(re.getDisplayname(), metadataVo.getDisplayname());
		Assert.assertEquals("Ext-REF", metadataVo.getExternalRef());
		Assert.assertEquals("Auth", metadataVo.getAuthors());
		Assert.assertEquals("RE description", metadataVo.getDescription());
		Assert.assertEquals("RE teaser", metadataVo.getTeaser());
		Assert.assertEquals("RE objectives", metadataVo.getObjectives());
		Assert.assertEquals("RE requirements", metadataVo.getRequirements());
		Assert.assertEquals("RE credits", metadataVo.getCredits());
		Assert.assertEquals("DE", metadataVo.getMainLanguage());
		Assert.assertEquals("Zurich", metadataVo.getLocation());
		Assert.assertEquals("3 days", metadataVo.getExpenditureOfWork());
		
		RepositoryEntryEducationalTypeVO educationTypeVo = metadataVo.getEducationalType();
		Assert.assertNotNull(educationTypeVo);
		Assert.assertEquals(educationalType.getKey(), educationTypeVo.getKey());
		Assert.assertEquals(educationalType.getIdentifier(), educationTypeVo.getIdentifier());
	}
	
	
	@Test
	public void updateMetadata() throws IOException, URISyntaxException {
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI())
				.path("repo/entries").path(re.getKey().toString()).path("metadata").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		RepositoryEntryMetadataVO metadataVo = conn.parse(response, RepositoryEntryMetadataVO.class);
		
		// fill the metadata
		metadataVo.setAuthors("Authors");
		metadataVo.setCredits("The credits");
		metadataVo.setDescription("A description");
		metadataVo.setExpenditureOfWork("4 weeks");
		metadataVo.setExternalRef("Reference");
		metadataVo.setLocation("Biel/Bienne");
		metadataVo.setMainLanguage("French");
		metadataVo.setObjectives("Our objectives");
		metadataVo.setRequirements("Their requirements");
		
		List<RepositoryEntryEducationalType> educationalTypes = repositoryManager.getAllEducationalTypes();
		RepositoryEntryEducationalType educationalType = educationalTypes.get(0);
		metadataVo.setEducationalType(RepositoryEntryEducationalTypeVO.valueOf(educationalType));

		URI updateRequest = UriBuilder.fromUri(getContextURI())
				.path("repo/entries").path(re.getKey().toString()).path("metadata").build();
		HttpPost updateMethod = conn.createPost(updateRequest, MediaType.APPLICATION_JSON);
		conn.addJsonEntity(updateMethod, metadataVo);
		HttpResponse updateResponse = conn.execute(updateMethod);
		Assert.assertEquals(200, updateResponse.getStatusLine().getStatusCode());
		RepositoryEntryMetadataVO updatedMetadataVo = conn.parse(updateResponse, RepositoryEntryMetadataVO.class);

		//check the response
		Assert.assertNotNull(metadataVo);
		Assert.assertEquals(re.getKey(), updatedMetadataVo.getKey());
		Assert.assertEquals(re.getDisplayname(), updatedMetadataVo.getDisplayname());
		Assert.assertEquals("Reference", updatedMetadataVo.getExternalRef());
		Assert.assertEquals("Authors", updatedMetadataVo.getAuthors());
		Assert.assertEquals("A description", updatedMetadataVo.getDescription());
		Assert.assertEquals("Our objectives", updatedMetadataVo.getObjectives());
		Assert.assertEquals("Their requirements", updatedMetadataVo.getRequirements());
		Assert.assertEquals("The credits", updatedMetadataVo.getCredits());
		Assert.assertEquals("French", updatedMetadataVo.getMainLanguage());
		Assert.assertEquals("Biel/Bienne", updatedMetadataVo.getLocation());
		Assert.assertEquals("4 weeks", updatedMetadataVo.getExpenditureOfWork());
		
		RepositoryEntryEducationalTypeVO educationTypeVo = updatedMetadataVo.getEducationalType();
		Assert.assertNotNull(educationTypeVo);
		Assert.assertEquals(educationalType.getKey(), educationTypeVo.getKey());
		Assert.assertEquals(educationalType.getIdentifier(), educationTypeVo.getIdentifier());

		RepositoryEntry updatedRe = repositoryService.loadByKey(re.getKey());
		Assert.assertEquals(re.getKey(), updatedRe.getKey());
		Assert.assertEquals(re.getDisplayname(), updatedRe.getDisplayname());
		Assert.assertEquals("Reference", updatedRe.getExternalRef());
		Assert.assertEquals("Authors", updatedRe.getAuthors());
		Assert.assertEquals("A description", updatedRe.getDescription());
		Assert.assertEquals("Our objectives", updatedRe.getObjectives());
		Assert.assertEquals("Their requirements", updatedRe.getRequirements());
		Assert.assertEquals("The credits", updatedRe.getCredits());
		Assert.assertEquals("French", updatedRe.getMainLanguage());
		Assert.assertEquals("Biel/Bienne", updatedRe.getLocation());
		Assert.assertEquals("4 weeks", updatedRe.getExpenditureOfWork());
		Assert.assertEquals(educationalType, updatedRe.getEducationalType());
	}
	
	@Test
	public void getTaxonomylevels() throws IOException, URISyntaxException {
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry(false);
		dbInstance.commit();
		
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-500", "Leveled taxonomy", null, null);
		TaxonomyLevel level = taxonomyLevelDao.createTaxonomyLevel("ID-Level-0", "My first taxonomy level", "A basic level", null, null, null, null, taxonomy);
		repositoryEntryToTaxonomyLevelDao.createRelation(re, level);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI())
				.path("repo/entries").path(re.getKey().toString())
				.path("taxonomy").path("levels").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		
		List<TaxonomyLevelVO> levels = parseTaxonomyLevelVOArray(response.getEntity());
		Assert.assertNotNull(levels);
		Assert.assertEquals(1, levels.size());
		Assert.assertEquals(level.getKey(), levels.get(0).getKey());
	}
	
	@Test
	public void addTaxonomyLevels()
	throws IOException, URISyntaxException {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry(false);
		dbInstance.commit();
		
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-501", "Leveled taxonomy", null, null);
		TaxonomyLevel level = taxonomyLevelDao.createTaxonomyLevel("ID-Level-0", "My first taxonomy level", "A basic level", null, null, null, null, taxonomy);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));

		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("entries")
				.path(entry.getKey().toString())
				.path("taxonomy").path("levels").path(level.getKey().toString()).build();
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		RepositoryEntry reloadedEntry = repositoryService.loadByKey(entry.getKey());
		Set<RepositoryEntryToTaxonomyLevel> relationToLevels = reloadedEntry.getTaxonomyLevels();
		Assert.assertNotNull(relationToLevels);
		Assert.assertEquals(1, relationToLevels.size());
		RepositoryEntryToTaxonomyLevel relationToLevel = relationToLevels.iterator().next();
		Assert.assertEquals(level, relationToLevel.getTaxonomyLevel());
	}
	
	@Test
	public void addTwiceTaxonomyLevels()
	throws IOException, URISyntaxException {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry(false);
		dbInstance.commit();
		
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-502", "Leveled taxonomy", null, null);
		TaxonomyLevel level = taxonomyLevelDao.createTaxonomyLevel("ID-Level-0", "My first taxonomy level", "A basic level", null, null, null, null, taxonomy);
		repositoryEntryToTaxonomyLevelDao.createRelation(entry, level);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));

		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("entries")
				.path(entry.getKey().toString())
				.path("taxonomy").path("levels").path(level.getKey().toString()).build();
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(304, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		RepositoryEntry reloadedEntry = repositoryService.loadByKey(entry.getKey());
		Set<RepositoryEntryToTaxonomyLevel> relationToLevels = reloadedEntry.getTaxonomyLevels();
		Assert.assertNotNull(relationToLevels);
		Assert.assertEquals(1, relationToLevels.size());
		RepositoryEntryToTaxonomyLevel relationToLevel = relationToLevels.iterator().next();
		Assert.assertEquals(level, relationToLevel.getTaxonomyLevel());
	}
	
	@Test
	public void deleteTaxonomyLevel()
	throws IOException, URISyntaxException {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry(false);
		dbInstance.commit();
		
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-202", "Leveled taxonomy", null, null);
		TaxonomyLevel level1 = taxonomyLevelDao.createTaxonomyLevel("ID-Level-0", "My first taxonomy level", "A basic level", null, null, null, null, taxonomy);
		TaxonomyLevel level2 = taxonomyLevelDao.createTaxonomyLevel("ID-Level-0", "My first taxonomy level", "A basic level", null, null, null, null, taxonomy);
		repositoryEntryToTaxonomyLevelDao.createRelation(entry, level1);
		repositoryEntryToTaxonomyLevelDao.createRelation(entry, level2);
		dbInstance.commitAndCloseSession();
		
		// make sure we have something to delete
		List<TaxonomyLevel> levels = repositoryEntryToTaxonomyLevelDao.getTaxonomyLevels(entry);
		Assert.assertEquals(2, levels.size());
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));

		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("entries")
				.path(entry.getKey().toString())
				.path("taxonomy").path("levels").path(level1.getKey().toString()).build();
		HttpDelete method = conn.createDelete(uri, MediaType.APPLICATION_JSON);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		// check that the right relation was deleted
		List<TaxonomyLevel> survivingLevels = repositoryEntryToTaxonomyLevelDao.getTaxonomyLevels(entry);
		Assert.assertEquals(1, survivingLevels.size());
		Assert.assertEquals(level2, survivingLevels.get(0));
	}
	
	private List<TaxonomyLevelVO> parseTaxonomyLevelVOArray(HttpEntity entity) {
		try(InputStream in=entity.getContent()) {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(in, new TypeReference<List<TaxonomyLevelVO>>(){/* */});
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	private List<UserVO> parseUserArray(HttpEntity entity) {
		try(InputStream in=entity.getContent()) {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(in, new TypeReference<List<UserVO>>(){/* */});
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
}

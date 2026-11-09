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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Locale;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.logging.Tracing;
import org.olat.core.util.httpclient.ConnectionUtilities;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.sharedfolder.SharedFolderManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.handlers.SharedFolderHandler;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.restapi.support.vo.FileVO;
import org.olat.restapi.support.vo.LinkVO;
import org.olat.test.JunitTestHelper;
import org.olat.test.JunitTestHelper.IdentityWithLogin;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 mai 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SharedFolderTest extends OlatRestTestCase {
	
	private static final Logger log = Tracing.createLoggerFor(SharedFolderTest.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	
	/**
	 * Check simple GET for the directory.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void getDirectories() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("shared-owner-");
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry sharedFolder = new SharedFolderHandler().createResource(owner, "Shared 1", "A shared folder", null, defOrganisation, Locale.ENGLISH);
		VFSContainer container = SharedFolderManager.getInstance().getNamedSharedFolder(sharedFolder, true);
		copyFileInResourceFolder(container, "portrait.jpg", "1_");
		
		URI uri = UriBuilder.fromUri(getFolderURI(sharedFolder)).build();
		HttpRequest method = conn.createGet(uri, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());
		
		List<LinkVO> links = parseLinkArray(response);
		Assert.assertNotNull(links);
		Assert.assertEquals(1, links.size());
		Assert.assertTrue(links.get(0).getHref().contains("1_portrait.jpg"));
		conn.close();

	}
	
	/**
	 * The root /** is read only
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void putDirectories_owner() throws IOException, URISyntaxException, InterruptedException {
		IdentityWithLogin owner = JunitTestHelper.createAndPersistRndUser("shared-owner-");
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry sharedFolder = new SharedFolderHandler()
				.createResource(owner.getIdentity(), "Shared 2", "A shared folder", null, defOrganisation, Locale.ENGLISH);
		VFSContainer container = SharedFolderManager.getInstance()
				.getNamedSharedFolder(sharedFolder, true);
		copyFileInResourceFolder(container, "portrait.jpg", "2_");
		
		// owner want to upload a file
		RestConnection conn = new RestConnection(owner);
		
		//create single page
		URL fileUrl = CoursesFoldersTest.class.getResource("certificate.pdf");
		File file = new File(fileUrl.toURI());
		
		URI uri = UriBuilder.fromUri(getFolderURI(sharedFolder)).build();
		HttpRequest method = conn.createPut(uri, file, file.getName(), List.of(), MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(405, response.statusCode());
		RestConnection.consume(response);
		conn.close();
	}
	
	/**
	 * Check simple GET for the directory.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void getFiles() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("shared-owner-");
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry sharedFolder = new SharedFolderHandler().createResource(owner, "Shared 2", "Shared files", null, defOrganisation, Locale.ENGLISH);
		VFSContainer container = SharedFolderManager.getInstance().getNamedSharedFolder(sharedFolder, true);
		copyFileInResourceFolder(container, "portrait.jpg", "2_");
		
		URI uri = UriBuilder.fromUri(getFolderURI(sharedFolder)).path("files").build();
		HttpRequest method = conn.createGet(uri, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());

		List<FileVO> links = parseFileArray(response);
		
		Assert.assertNotNull(links);
		Assert.assertEquals(1, links.size());
		Assert.assertTrue(links.get(0).getHref().contains("2_portrait.jpg"));
		conn.close();

	}
	
	/**
	 * GET for directory but a little deeper.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void getFolders_deep() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("shared-owner-");
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry sharedFolder = new SharedFolderHandler().createResource(owner, "Shared 5", "Shared files", null, defOrganisation, Locale.ENGLISH);
		VFSContainer container = SharedFolderManager.getInstance().getNamedSharedFolder(sharedFolder, true);
		VFSContainer firstContainer = container.createChildContainer("First");
		VFSContainer secondContainer = firstContainer.createChildContainer("Second");
		VFSContainer thirdContainer = secondContainer.createChildContainer("Third");
		copyFileInResourceFolder(thirdContainer, "portrait.jpg", "2_");
		
		URI uri = UriBuilder.fromUri(getFolderURI(sharedFolder)).path("files")
				.path("First").path("Second").build();
		HttpRequest method = conn.createGet(uri, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());

		List<FileVO> links = parseFileArray(response);
		Assert.assertNotNull(links);
		Assert.assertEquals(1, links.size());
		Assert.assertEquals("Third", links.get(0).getTitle());
		conn.close();

	}
	
	/**
	 * GET for directory but a little deeper.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void getFolders_notFound() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("shared-owner-");
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry sharedFolder = new SharedFolderHandler().createResource(owner, "Shared 5", "Shared files", null, defOrganisation, Locale.ENGLISH);
		VFSContainer container = SharedFolderManager.getInstance().getNamedSharedFolder(sharedFolder, true);
		VFSContainer firstContainer = container.createChildContainer("First");
		VFSContainer secondContainer = firstContainer.createChildContainer("Second");
		VFSContainer thirdContainer = secondContainer.createChildContainer("Third");
		copyFileInResourceFolder(thirdContainer, "portrait.jpg", "2_");
		
		URI uri = UriBuilder.fromUri(getFolderURI(sharedFolder)).path("files")
				.path("First").path("Second").path("Trois").build();
		HttpRequest method = conn.createGet(uri, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(404, response.statusCode());
		conn.close();

	}
	
	/**
	 * Owner of the shared folder want to put a file.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void putFiles_owner() throws IOException, URISyntaxException, InterruptedException {
		//create a shared folder
		IdentityWithLogin owner = JunitTestHelper.createAndPersistRndUser("shared-owner-");
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry sharedFolder = new SharedFolderHandler().createResource(owner.getIdentity(), "Shared 2", "Shared files", null, defOrganisation, Locale.ENGLISH);
		VFSContainer container = SharedFolderManager.getInstance().getNamedSharedFolder(sharedFolder, true);
		copyFileInResourceFolder(container, "certificate.pdf", "2_");
		
		// owner want to upload a file
		RestConnection conn = new RestConnection(owner);
		
		//create single page
		URL fileUrl = CoursesFoldersTest.class.getResource("certificate.pdf");
		File file = new File(fileUrl.toURI());
		
		URI uri = UriBuilder.fromUri(getFolderURI(sharedFolder)).path("files").build();
		HttpRequest method = conn.createPut(uri, file, file.getName(), List.of(), MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());
		ConnectionUtilities.consume(response);
		
		//check the reality of the file
		VFSItem item = container.resolve("certificate.pdf");
		Assert.assertNotNull(item);
		Assert.assertTrue(item instanceof VFSLeaf);
		VFSLeaf leaf = (VFSLeaf)item;
		Assert.assertTrue(leaf.getSize() > 10);
		conn.close();
	}
	
	/**
	 * Participant can read the files and download them.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void getFiles_participant() throws IOException, URISyntaxException, InterruptedException {
		//a shared folder with a participant
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("shared-owner-");
		IdentityWithLogin participant = JunitTestHelper.createAndPersistRndUser("shared-part-");
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry sharedFolder = new SharedFolderHandler().createResource(owner, "Shared 2", "Shared files", null, defOrganisation, Locale.ENGLISH);
		VFSContainer container = SharedFolderManager.getInstance().getNamedSharedFolder(sharedFolder, true);
		copyFileInResourceFolder(container, "portrait.jpg", "3_");
		repositoryEntryRelationDao.addRole(participant.getIdentity(), sharedFolder, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		// participant want to see the file
		RestConnection conn = new RestConnection(participant);

		// check directories
		URI uri = UriBuilder.fromUri(getFolderURI(sharedFolder)).path("files").build();
		HttpRequest method = conn.createGet(uri, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());

		List<FileVO> links = parseFileArray(response);
		
		Assert.assertNotNull(links);
		Assert.assertEquals(1, links.size());
		Assert.assertTrue(links.get(0).getHref().contains("3_portrait.jpg"));
		
		// download the file
		URI fileUri = UriBuilder.fromUri(getFolderURI(sharedFolder)).path("files").path("3_portrait.jpg").build();
		HttpRequest fileMethod = conn.createGet(fileUri, "*/*");
		HttpResponse<InputStream> fileResponse = conn.execute(fileMethod);
		Assert.assertEquals(200, fileResponse.statusCode());
		byte[] fileBytes = ConnectionUtilities.toByteArray(fileResponse);
		Assert.assertNotNull(fileBytes);
		Assert.assertTrue(fileBytes.length > 10);
		conn.close();
	
	}
	
	/**
	 * Participants have only read-only access to the shared folder.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void putFiles_participant() throws IOException, URISyntaxException, InterruptedException {
		//a shared folder with a participant
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("shared-owner-");
		IdentityWithLogin participant = JunitTestHelper.createAndPersistRndUser("shared-part-");
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry sharedFolder = new SharedFolderHandler().createResource(owner, "Shared 5", "Shared files", null, defOrganisation, Locale.ENGLISH);
		VFSContainer container = SharedFolderManager.getInstance().getNamedSharedFolder(sharedFolder, true);
		copyFileInResourceFolder(container, "portrait.jpg", "5_");
		repositoryEntryRelationDao.addRole(participant.getIdentity(), sharedFolder, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		// participant want to upload a file
		RestConnection conn = new RestConnection(participant);
		
		URL fileUrl = CoursesFoldersTest.class.getResource("certificate.pdf");
		File file = new File(fileUrl.toURI());
		
		URI uri = UriBuilder.fromUri(getFolderURI(sharedFolder)).path("files").build();
		HttpRequest method = conn.createPut(uri, file, file.getName(), List.of(), MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(Status.FORBIDDEN.getStatusCode(), response.statusCode());
		ConnectionUtilities.consume(response);
		
		//check the absence of the file
		VFSItem item = container.resolve("certificate.pdf");
		Assert.assertNull(item);
		conn.close();
	}
	
	private URI getFolderURI(RepositoryEntry entry) {
		return UriBuilder.fromUri(getContextURI()).path("repo").path("sharedfolder").path(entry.getKey().toString())
			.build();
	}
	
	private void copyFileInResourceFolder(VFSContainer container, String filename, String prefix)
	throws IOException {
		VFSLeaf item = container.createChildLeaf(prefix + filename);
		try(InputStream pageStream = SharedFolderTest.class.getResourceAsStream(filename);
				OutputStream outStream = item.getOutputStream(false)) {
			IOUtils.copy(pageStream, outStream);
		} catch(IOException e) {
			log.error("", e);
			Assert.fail();
		}
	}
}

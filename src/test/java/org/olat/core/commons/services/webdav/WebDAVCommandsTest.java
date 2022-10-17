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
package org.olat.core.commons.services.webdav;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;

import jakarta.ws.rs.core.UriBuilder;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSLockApplicationType;
import org.olat.core.util.vfs.VFSLockManager;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.lock.LockInfo;
import org.olat.core.util.vfs.lock.LockResult;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.restapi.CoursePublishTest;
import org.olat.test.JunitTestHelper;
import org.olat.test.JunitTestHelper.IdentityWithLogin;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Test the commands against the WedDAV implementation of OpenOLAT
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class WebDAVCommandsTest extends WebDAVTestCase {
	
	private static final Logger log = Tracing.createLoggerFor(WebDAVCommandsTest.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private WebDAVModule webDAVModule;
	@Autowired
	private VFSLockManager lockManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	
	@After
	public void resetWebDAVModule() {
		webDAVModule.setEnableLearnersBookmarksCourse(false);
		webDAVModule.setEnableLearnersParticipatingCourses(false);
	}
	
	/**
	 * Check the DAV, Ms-Author and Allow header
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void testOptions()
	throws IOException, URISyntaxException {
		//create a user
		IdentityWithLogin user = JunitTestHelper.createAndPersistRndUser("webdav-1-");
		
		//list root content of its webdav folder
		WebDAVConnection conn = new WebDAVConnection();
		conn.setCredentials(user);
		
		URI baseUri = conn.getBaseURI().build();
		HttpOptions optionsRoot = conn.createOptions(baseUri);
		HttpResponse optionsResponse = conn.execute(optionsRoot);
		Assert.assertEquals(200, optionsResponse.getStatusLine().getStatusCode());
		//check DAV header
		Header davHeader = optionsResponse.getFirstHeader("DAV");
		String davHeaderValue = davHeader.getValue();
		Assert.assertTrue(davHeaderValue.contains("1"));
		Assert.assertTrue(davHeaderValue.contains("2"));
		//check ms author
		Header msHeader = optionsResponse.getFirstHeader("MS-Author-Via");
		Assert.assertEquals("DAV", msHeader.getValue());
		//check methods
		Header allowHeader = optionsResponse.getFirstHeader("Allow");
		String allowValue = allowHeader.getValue();
		
		String[] allowedMethods = new String[] {
				"OPTIONS", "GET", "HEAD", "POST", "DELETE",
				"TRACE", "PROPPATCH", "COPY", "MOVE", "LOCK", "UNLOCK"
		};
		for(String allowedMethod:allowedMethods) {
			Assert.assertTrue(allowValue.contains(allowedMethod));
		}

		conn.close();
	}
	
	@Test
	public void testHead()
	throws IOException, URISyntaxException {
		//create a user
		IdentityWithLogin user = JunitTestHelper.createAndPersistRndUser("webdav-2-");

		WebDAVConnection conn = new WebDAVConnection();
		conn.setCredentials(user);
		
		//create a file
		String publicPath = FolderConfig.getUserHome(user.getIdentity()) + "/public";
		VFSContainer vfsPublic = VFSManager.olatRootContainer(publicPath, null);
		createFile(vfsPublic, "test_head.txt");
		
		//head file
		URI publicUri = conn.getBaseURI().path("webdav").path("home").path("public").path("test_head.txt").build();
		HttpResponse response = conn.head(publicUri);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		Header lengthHeader = response.getFirstHeader("Content-Length");
		Assert.assertNotNull(lengthHeader);
		Assert.assertEquals("10", lengthHeader.getValue());
		Header typeHeader = response.getFirstHeader("Content-Type");
		Assert.assertNotNull(typeHeader);
		Assert.assertEquals("text/plain", typeHeader.getValue());
		EntityUtils.consume(response.getEntity());
		
		conn.close();
	}
	
	@Test
	public void testPropFind()
	throws IOException, URISyntaxException {
		//create a user
		IdentityWithLogin user = JunitTestHelper.createAndPersistRndUser("webdav-2-");

		WebDAVConnection conn = new WebDAVConnection();
		conn.setCredentials(user);
		
		//list root content of its webdav folder
		URI uri = conn.getBaseURI().build();
		String xml = conn.propfind(uri, 1);
		Assert.assertTrue(xml.indexOf("<D:multistatus") > 0);//Windows need the D namespace
		Assert.assertTrue(xml.indexOf("<D:href>/</D:href>") > 0);//check the root
		Assert.assertTrue(xml.indexOf("<D:href>/webdav/</D:href>") > 0);//check the webdav folder

		//check public folder
		URI publicUri = conn.getBaseURI().path("webdav").path("home").path("public").build();
		String publicXml = conn.propfind(publicUri, 1);
		Assert.assertTrue(publicXml.indexOf("<D:multistatus") > 0);//Windows need the D namespace
		Assert.assertTrue(publicXml.indexOf("<D:href>/webdav/home/public/</D:href>") > 0);//check the root

		conn.close();
	}
	
	@Test
	public void testMkcol_public()
	throws IOException, URISyntaxException {
		//create a user
		IdentityWithLogin user = JunitTestHelper.createAndPersistRndAuthor("webdav-2a-");

		//create a file
		String publicPath = FolderConfig.getUserHome(user.getIdentity()) + "/public";
		VFSContainer vfsPublic = VFSManager.olatRootContainer(publicPath, null);
		Assert.assertTrue(vfsPublic.exists());

		
		WebDAVConnection conn = new WebDAVConnection();
		conn.setCredentials(user);

		//author check course folder
		URI publicUri = conn.getBaseURI().path("webdav").path("home").path("public").build();
		String publicXml = conn.propfind(publicUri, 2);
		Assert.assertTrue(publicXml.indexOf("<D:href>/webdav/home/public/</D:href>") > 0);

		//make a folder
		URI newUri = UriBuilder.fromUri(publicUri).path("newFolder").build();
		int returnMkcol = conn.mkcol(newUri);
		Assert.assertEquals(201, returnMkcol);
		
		//check if folder exists
		VFSItem newItem = vfsPublic.resolve("newFolder");
		Assert.assertNotNull(newItem);
		Assert.assertTrue(newItem instanceof VFSContainer);
		Assert.assertTrue(newItem.exists());
	
		conn.close();
	}
	
	@Test
	public void testMove_public()
	throws IOException, URISyntaxException {
		//create a user
		IdentityWithLogin user = JunitTestHelper.createAndPersistRndAuthor("webdav-2b-");

		//create a file
		String publicPath = FolderConfig.getUserHome(user.getIdentity()) + "/public";
		VFSContainer vfsPublic = VFSManager.olatRootContainer(publicPath, null);
		createFile(vfsPublic, "test.txt");
		VFSContainer subPublic = vfsPublic.createChildContainer("moveto");

		WebDAVConnection conn = new WebDAVConnection();
		conn.setCredentials(user);

		//author check course folder
		URI publicUri = conn.getBaseURI().path("webdav").path("home").path("public").build();
		URI fileUri = UriBuilder.fromUri(publicUri).path("test.txt").build();
		String destination = UriBuilder.fromUri(publicUri).path("moveto").path("test.txt").build().toString();
		int returnMove = conn.move(fileUri, destination);
		Assert.assertEquals(201, returnMove);

		//check move
		VFSItem movedItem = subPublic.resolve("test.txt");
		Assert.assertNotNull(movedItem);
		Assert.assertTrue(movedItem instanceof VFSLeaf);
		Assert.assertTrue(movedItem.exists());

		VFSItem sourceItem = vfsPublic.resolve("test.txt");
		Assert.assertNull(sourceItem);
	
		conn.close();
	}
	
	@Test
	public void testCopy_public()
	throws IOException, URISyntaxException {
		//create a user
		IdentityWithLogin user = JunitTestHelper.createAndPersistRndAuthor("webdav-2b-");

		//create a file
		String publicPath = FolderConfig.getUserHome(user.getIdentity()) + "/public";
		VFSContainer vfsPublic = VFSManager.olatRootContainer(publicPath, null);
		createFile(vfsPublic, "test.txt");
		VFSContainer subPublic = vfsPublic.createChildContainer("copyto");

		WebDAVConnection conn = new WebDAVConnection();
		conn.setCredentials(user);

		//author check course folder
		URI publicUri = conn.getBaseURI().path("webdav").path("home").path("public").build();
		URI fileUri = UriBuilder.fromUri(publicUri).path("test.txt").build();
		String destination = UriBuilder.fromUri(publicUri).path("copyto").path("copy.txt").build().toString();
		int returnMove = conn.copy(fileUri, destination);
		Assert.assertEquals(201, returnMove);

		//check move
		VFSItem movedItem = subPublic.resolve("copy.txt");
		Assert.assertNotNull(movedItem);
		Assert.assertTrue(movedItem instanceof VFSLeaf);
		Assert.assertTrue(movedItem.exists());

		VFSItem sourceItem = vfsPublic.resolve("test.txt");
		Assert.assertNotNull(sourceItem);
		Assert.assertTrue(sourceItem instanceof VFSLeaf);
		Assert.assertTrue(sourceItem.exists());
	
		conn.close();
	}
	
	@Test
	public void testPut_course()
	throws IOException, URISyntaxException {
		//create a user
		IdentityWithLogin author = JunitTestHelper.createAndPersistRndAuthor("webdav-3-");
		deployTestCourse(author.getIdentity(), null);

		WebDAVConnection conn = new WebDAVConnection();
		conn.setCredentials(author);

		//author check course folder
		URI courseUri = conn.getBaseURI().path("webdav").path("coursefolders").build();
		String publicXml = conn.propfind(courseUri, 2);
		Assert.assertTrue(publicXml.indexOf("<D:href>/webdav/coursefolders/_other/Kurs/_courseelementdata/</D:href>") > 0);

		//PUT in the folder
		URI putUri = UriBuilder.fromUri(courseUri).path("_other").path("Kurs").path("test.txt").build();
		HttpPut put = conn.createPut(putUri);
		InputStream dataStream = WebDAVCommandsTest.class.getResourceAsStream("text.txt");
		InputStreamEntity entity = new InputStreamEntity(dataStream, -1);
		put.setEntity(entity);
		HttpResponse putResponse = conn.execute(put);
		Assert.assertEquals(201, putResponse.getStatusLine().getStatusCode());
		
		//GET
		HttpGet get = conn.createGet(putUri);
		HttpResponse getResponse = conn.execute(get);
		Assert.assertEquals(200, getResponse.getStatusLine().getStatusCode());
		String text = EntityUtils.toString(getResponse.getEntity());
		Assert.assertEquals("Small text", text);
	
		conn.close();
	}
	
	/**
	 * PROPPATCH is essential for Windows, the content of the response
	 * is not important but it must not return an error.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void testPut_PropPatch_home()
	throws IOException, URISyntaxException {
		//create a user
		IdentityWithLogin author = JunitTestHelper.createAndPersistRndAuthor("webdav-3-");
		deployTestCourse(author.getIdentity(), null);

		WebDAVConnection conn = new WebDAVConnection();
		conn.setCredentials(author);

		//author check course folder
		URI privateUri = conn.getBaseURI().path("webdav").path("home").path("private").build();
		conn.propfind(privateUri, 2);

		//PUT in the folder
		URI putUri = UriBuilder.fromUri(privateUri).path("test.txt").build();
		HttpPut put = conn.createPut(putUri);
		InputStream dataStream = WebDAVCommandsTest.class.getResourceAsStream("text.txt");
		InputStreamEntity entity = new InputStreamEntity(dataStream, -1);
		put.setEntity(entity);
		HttpResponse putResponse = conn.execute(put);
		Assert.assertEquals(201, putResponse.getStatusLine().getStatusCode());
		EntityUtils.consume(putResponse.getEntity());
		
		//PROPPATCH
		URI patchUri = UriBuilder.fromUri(privateUri).path("test.txt").build();
		HttpPropPatch patch = conn.createPropPatch(patchUri);
		
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"utf-8\" ?>")
		  .append("<D:propertyupdate xmlns:D=\"DAV:\"")
		  .append("  xmlns:Z=\"http://www.w3.com/standards/z39.50/\">")
		  .append("  <D:set>")
		  .append("      <D:prop>")
		  .append("           <Z:authors>")
		  .append("                <Z:Author>Jim Whitehead</Z:Author>")
		  .append("                <Z:Author>Roy Fielding</Z:Author>")
		  .append("           </Z:authors>")
		  .append("      </D:prop>")
		  .append("  </D:set>")
		  .append("  <D:remove>")
		  .append("      <D:prop><Z:Copyright-Owner/></D:prop>")
		  .append("   </D:remove>")
		  .append(" </D:propertyupdate>");
		
		patch.setEntity(new StringEntity(sb.toString()));
		
		HttpResponse patchResponse = conn.execute(patch);
		Assert.assertEquals(207, patchResponse.getStatusLine().getStatusCode());
	
		conn.close();
	}
	
	/**
	 * In the this test, an author and its assistant try to concurrently
	 * lock a file.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void testLock()
	throws IOException, URISyntaxException {
		//create a user
		IdentityWithLogin author = JunitTestHelper.createAndPersistRndAuthor("webdav-4-");
		IdentityWithLogin assistant = JunitTestHelper.createAndPersistRndAuthor("webdav-5-");
		deployTestCourse(author.getIdentity(), assistant.getIdentity());

		WebDAVConnection authorConn = new WebDAVConnection();
		authorConn.setCredentials(author);
		
		WebDAVConnection assistantConn = new WebDAVConnection();
		assistantConn.setCredentials(assistant);
		
		//author check course folder
		URI courseUri = authorConn.getBaseURI().path("webdav").path("coursefolders").build();
		String publicXml = authorConn.propfind(courseUri, 2);
		Assert.assertTrue(publicXml.indexOf("<D:href>/webdav/coursefolders/_other/Kurs/_courseelementdata/</D:href>") > 0);

		//coauthor check course folder
		String assistantPublicXml = assistantConn.propfind(courseUri, 2);
		Assert.assertTrue(assistantPublicXml.indexOf("<D:href>/webdav/coursefolders/_other/Kurs/_courseelementdata/</D:href>") > 0);

		//PUT a file to lock
		URI putUri = UriBuilder.fromUri(courseUri).path("_other").path("Kurs").path("test.txt").build();
		HttpPut put = authorConn.createPut(putUri);
		InputStream dataStream = WebDAVCommandsTest.class.getResourceAsStream("text.txt");
		InputStreamEntity entity = new InputStreamEntity(dataStream, -1);
		put.setEntity(entity);
		HttpResponse putResponse = authorConn.execute(put);
		Assert.assertEquals(201, putResponse.getStatusLine().getStatusCode());
		EntityUtils.consume(putResponse.getEntity());

		//author lock the file in the course folder
		String authorLockToken = UUID.randomUUID().toString().replace("-", "").toLowerCase();
		String authorResponseLockToken = authorConn.lock(putUri, authorLockToken);
		Assert.assertNotNull(authorResponseLockToken);
		
		//coauthor try to lock the same file
		String coauthorLockToken = UUID.randomUUID().toString().replace("-", "").toLowerCase();
		int coauthorLock = assistantConn.lockTry(putUri, coauthorLockToken);
		Assert.assertEquals(423, coauthorLock);// it's lock
		
		//author unlock the file
		int unlockCode = authorConn.unlock(putUri, authorResponseLockToken);
		Assert.assertEquals(204, unlockCode);
		
		//coauthor try a second time to lock the file
		String coauthorLockToken_2 = UUID.randomUUID().toString().replace("-", "").toLowerCase();
		int coauthorLock_2 = assistantConn.lockTry(putUri, coauthorLockToken_2);
		Assert.assertEquals(200, coauthorLock_2);// it's lock
		
		authorConn.close();
		assistantConn.close();
	}
	
	/**
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void testLock_propfind_lockedInOpenOLAT()
	throws IOException, URISyntaxException {
		//create a user
		IdentityWithLogin author = JunitTestHelper.createAndPersistRndAuthor("webdav-4-");
		Identity assistant = JunitTestHelper.createAndPersistIdentityAsRndAuthor("webdav-5-");
		RepositoryEntry re = deployTestCourse(author.getIdentity(), assistant);
		ICourse course = CourseFactory.loadCourse(re.getOlatResource());
		Assert.assertNotNull(course);
		
		//the assistant lock the file as in OpenOLAT GUI
		VFSContainer folderContainer = course.getCourseFolderContainer();
		createFile(folderContainer, "tolock.txt");
		VFSItem itemToLock = folderContainer.resolve("tolock.txt");
		Assert.assertNotNull(itemToLock);
		LockResult locked = lockManager.lock(itemToLock, assistant, VFSLockApplicationType.vfs, null);
		Assert.assertTrue(locked.isAcquired());
		
		//author make a propfind in the locked resource
		WebDAVConnection conn = new WebDAVConnection();
		conn.setCredentials(author);
		
		URI toLockUri = conn.getBaseURI().path("webdav").path("coursefolders").path("_other").path("Kurs").path("tolock.txt").build();
		String propfindXml = conn.propfind(toLockUri, 2);

		Assert.assertTrue(propfindXml.indexOf("<D:lockscope><D:exclusive/></D:lockscope>") > 0);//not really a test
		Assert.assertTrue(propfindXml.indexOf("/Identity/" + assistant.getKey() + "</D:owner>") > 0);
		Assert.assertTrue(propfindXml.indexOf("<D:locktoken><D:href>opaquelocktoken:") > 0);
		
		LockInfo lock = lockManager.getLock(itemToLock);
		Assert.assertNotNull(lock);
		Assert.assertNotNull(lock.getScope());
		Assert.assertNotNull(lock.getType());
		Assert.assertNotNull(lock.getOwner());
		Assert.assertTrue(lock.getOwner().length() > 0);
		Assert.assertTrue(lock.isVfsLock());
		Assert.assertFalse(lock.isWebDAVLock());
		Assert.assertEquals(assistant.getKey(), lock.getLockedBy());
		Assert.assertEquals(1, lock.getTokensSize());

		conn.close();
	}
	
	/**
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void testLock_guilike_lockedWithWebdAV()
	throws IOException, URISyntaxException {
		//create a user
		IdentityWithLogin user = JunitTestHelper.createAndPersistRndAuthor("webdav-2c-");

		//create a file
		String publicPath = FolderConfig.getUserHome(user.getIdentity()) + "/public";
		VFSContainer vfsPublic = VFSManager.olatRootContainer(publicPath, null);
		VFSItem item = createFile(vfsPublic, "test.txt");
		
		//lock the item with WebDAV
		WebDAVConnection conn = new WebDAVConnection();
		conn.setCredentials(user);

		//author check file 
		URI textUri = conn.getBaseURI().path("webdav").path("home").path("public").path("test.txt").build();
		String textPropfind = conn.propfind(textUri, 0);
		log.info(textPropfind);
		
		//author lock the file
		String lockToken = conn.lock(textUri, UUID.randomUUID().toString());
		Assert.assertNotNull(lockToken);

		//check vfs lock
		boolean lockedForMe = lockManager.isLockedForMe(item, user.getIdentity(), VFSLockApplicationType.vfs, null);
		Assert.assertTrue(lockedForMe);
		LockInfo lock = lockManager.getLock(item);
		Assert.assertNotNull(lock);
		Assert.assertNotNull(lock.getScope());
		Assert.assertNotNull(lock.getType());
		Assert.assertNotNull(lock.getOwner());
		Assert.assertTrue(lock.getOwner().length() > 0);
		Assert.assertFalse(lock.isVfsLock());
		Assert.assertTrue(lock.isWebDAVLock());
		Assert.assertEquals(user.getKey(), lock.getLockedBy());
		Assert.assertEquals(1, lock.getTokensSize());
		
		//try to unlock which should not be possible
		boolean unlocked = lockManager.unlock(item, VFSLockApplicationType.vfs);
		Assert.assertFalse(unlocked);
		//check that nothing changed
		LockInfo lockAfterUnlock = lockManager.getLock(item);
		Assert.assertNotNull(lockAfterUnlock);
		Assert.assertNotNull(lockAfterUnlock.getScope());
		Assert.assertNotNull(lockAfterUnlock.getType());
		Assert.assertNotNull(lockAfterUnlock.getOwner());
		Assert.assertTrue(lockAfterUnlock.getOwner().length() > 0);
		Assert.assertFalse(lockAfterUnlock.isVfsLock());
		Assert.assertTrue(lockAfterUnlock.isWebDAVLock());
		Assert.assertEquals(user.getKey(), lockAfterUnlock.getLockedBy());
		Assert.assertEquals(1, lock.getTokensSize());
		
		conn.close();
	}
	
	@Test
	public void testLock_public_samePathLock()
	throws IOException, URISyntaxException {
		//create a user
		IdentityWithLogin user1 = JunitTestHelper.createAndPersistRndUser("webdav-2d");
		IdentityWithLogin user2 = JunitTestHelper.createAndPersistRndUser("webdav-2e");

		//create a file
		String publicPath1 = FolderConfig.getUserHome(user1.getIdentity()) + "/public";
		VFSContainer vfsPublic1 = VFSManager.olatRootContainer(publicPath1, null);
		VFSItem item1 = createFile(vfsPublic1, "test.txt");
		Assert.assertNotNull(item1);
		
		String publicPath2 = FolderConfig.getUserHome(user2.getIdentity()) + "/public";
		VFSContainer vfsPublic2 = VFSManager.olatRootContainer(publicPath2, null);
		VFSItem item2 = createFile(vfsPublic2, "test.txt");
		Assert.assertNotNull(item2);
		
		//lock the item with WebDAV
		WebDAVConnection conn1 = new WebDAVConnection();
		conn1.setCredentials(user1);

		//user 1 lock the file
		URI textUri = conn1.getBaseURI().path("webdav").path("home").path("public").path("test.txt").build();
		String textPropfind1 = conn1.propfind(textUri, 0);
		Assert.assertNotNull(textPropfind1);
		
		// lock the path /webdav/home/public/test.txt
		String lockToken1 = conn1.lock(textUri, UUID.randomUUID().toString());
		Assert.assertNotNull(lockToken1);

		//user 2 lock its own file
		WebDAVConnection conn2 = new WebDAVConnection();
		conn2.setCredentials(user2);
		String textPropfind2 = conn2.propfind(textUri, 0);
		Assert.assertNotNull(textPropfind2);
		
		// lock the path /webdav/home/public/test.txt
		String lockToken2 = conn2.lock(textUri, UUID.randomUUID().toString());
		Assert.assertNotNull(lockToken2);
		
		//closes
		conn1.close();
		conn2.close();
	}
	
	@Test
	public void testDelete()
	throws IOException, URISyntaxException {
		//create a user
		IdentityWithLogin user = JunitTestHelper.createAndPersistRndUser("webdav-6");
		
		//create a file
		String publicPath = FolderConfig.getUserHome(user.getIdentity()) + "/public";
		VFSContainer vfsPublic = VFSManager.olatRootContainer(publicPath, null);
		createFile(vfsPublic, "testDelete.txt");
		
		//check
		VFSItem item = vfsPublic.resolve("testDelete.txt");
		Assert.assertTrue(item instanceof VFSLeaf);
		Assert.assertTrue(item.exists());
		Assert.assertTrue(((VFSLeaf)item).getSize() > 0);

		//delete the file
		WebDAVConnection conn = new WebDAVConnection();
		conn.setCredentials(user);
	
		//check public folder
		URI checkUri = conn.getBaseURI().path("webdav").path("home").path("public").path("testDelete.txt").build();
		String publicXml = conn.propfind(checkUri, 1);
		Assert.assertTrue(publicXml.indexOf("<D:multistatus") > 0);//Windows need the D namespace
		Assert.assertTrue(publicXml.indexOf("<D:href>/webdav/home/public/testDelete.txt</D:href>") > 0);//check the root

		//delete the file
		HttpDelete delete = conn.createDelete(checkUri);
		HttpResponse deleteResponse = conn.execute(delete);
		Assert.assertEquals(204, deleteResponse.getStatusLine().getStatusCode());
		EntityUtils.consume(deleteResponse.getEntity());
		
		//check if really deleted
		VFSItem reloadTestLeaf = vfsPublic.resolve("testDelete.txt");
		Assert.assertNull(reloadTestLeaf);

		conn.close();
	}
	
	@Test
	public void testDeleteUnlock()
	throws IOException, URISyntaxException {
		//create a user
		IdentityWithLogin user = JunitTestHelper.createAndPersistRndUser("webdav-12");
		
		//create a file
		String publicPath = FolderConfig.getUserHome(user.getIdentity()) + "/public";
		VFSContainer vfsPublic = VFSManager.olatRootContainer(publicPath, null);
		createFile(vfsPublic, "testDeleteUnlock.txt");
		
		//check
		VFSItem item = vfsPublic.resolve("testDeleteUnlock.txt");
		Assert.assertTrue(item instanceof VFSLeaf);
		Assert.assertTrue(((VFSLeaf)item).getSize() > 0);

		WebDAVConnection conn = new WebDAVConnection();
		conn.setCredentials(user);

		//lock the file
		URI textUri = conn.getBaseURI().path("webdav").path("home").path("public").path("testDeleteUnlock.txt").build();
		String lockToken = conn.lock(textUri, UUID.randomUUID().toString());
		Assert.assertNotNull(lockToken);

		//delete the file
		HttpDelete delete = conn.createDelete(textUri);
		delete.addHeader("Lock-Token", lockToken);
		HttpResponse deleteResponse = conn.execute(delete);

		Assert.assertEquals(204, deleteResponse.getStatusLine().getStatusCode());
		EntityUtils.consume(deleteResponse.getEntity());
		
		//check if really deleted
		VFSItem reloadTestLeaf = vfsPublic.resolve("testDelete.txt");
		Assert.assertNull(reloadTestLeaf);
		
		boolean locked = lockManager.isLocked(item, VFSLockApplicationType.webdav, null);
		Assert.assertFalse(locked);

		conn.close();
	}
	
	@Test
	public void coursePermissions_participant()
	throws IOException, URISyntaxException {
		webDAVModule.setEnableLearnersBookmarksCourse(true);
		webDAVModule.setEnableLearnersParticipatingCourses(true);
		
		IdentityWithLogin author = JunitTestHelper.createAndPersistRndAuthor("auth-webdav");
		IdentityWithLogin participant = JunitTestHelper.createAndPersistRndUser("participant-webdav");
		URL courseWithForumsUrl = WebDAVCommandsTest.class.getResource("webdav_course.zip");
		RepositoryEntry course = deployTestCourse(author.getIdentity(), null, "WebDAV course", courseWithForumsUrl);
		repositoryEntryRelationDao.addRole(participant.getIdentity(), course, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		WebDAVConnection conn = new WebDAVConnection();
		conn.setCredentials(participant);

		URI courseUri = conn.getBaseURI().path("webdav").path("coursefolders").build();
		String publicXml = conn.propfind(courseUri, 2);
		//cannot access course storage
		Assert.assertFalse(publicXml.contains("<D:href>/webdav/coursefolders/_other/WebDAV%20course/Course%20storage/</D:href>"));
		//can access course elements
		Assert.assertTrue(publicXml.contains("<D:href>/webdav/coursefolders/_other/WebDAV%20course/_courseelementdata/</D:href>"));

		URI courseElementUri = conn.getBaseURI().path("webdav").path("coursefolders")
				.path("_other").path("WebDAV%20course").path("_courseelementdata").build();
		String publicElementXml = conn.propfind(courseElementUri, 2);
		Assert.assertTrue(publicElementXml.contains("<D:href>/webdav/coursefolders/_other/WebDAV%20course/_courseelementdata/Folder%20for%20all/</D:href>"));
		Assert.assertFalse(publicElementXml.contains("<D:href>/webdav/coursefolders/_other/WebDAV%20course/_courseelementdata/Student%20read-only/Readonly%20students/</D:href>"));
		Assert.assertFalse(publicElementXml.contains("<D:href>/webdav/coursefolders/_other/WebDAV%20course/_courseelementdata/Not%20for%20students/Not%20for%20students/</D:href>"));

		conn.close();
	}
	
	@Test
	public void coursePermissions_owner()
	throws IOException, URISyntaxException {
		webDAVModule.setEnableLearnersBookmarksCourse(true);
		webDAVModule.setEnableLearnersParticipatingCourses(true);
		
		IdentityWithLogin author = JunitTestHelper.createAndPersistRndAuthor("auth-webdav");
		URL courseWithForumsUrl = WebDAVCommandsTest.class.getResource("webdav_course.zip");
		deployTestCourse(author.getIdentity(), null, "WebDAV course", courseWithForumsUrl);
		dbInstance.commitAndCloseSession();
		
		WebDAVConnection conn = new WebDAVConnection();
		conn.setCredentials(author);

		URI courseUri = conn.getBaseURI().path("webdav").path("coursefolders").build();
		String publicXml = conn.propfind(courseUri, 2);
		//cane access course storage
		Assert.assertTrue(publicXml.contains("<D:href>/webdav/coursefolders/_other/WebDAV%20course/Course%20storage/</D:href>"));
		//can access course elements
		Assert.assertTrue(publicXml.contains("<D:href>/webdav/coursefolders/_other/WebDAV%20course/_courseelementdata/</D:href>"));

		URI courseElementUri = conn.getBaseURI().path("webdav").path("coursefolders")
				.path("_other").path("WebDAV%20course").path("_courseelementdata").build();
		String publicElementXml = conn.propfind(courseElementUri, 2);
		//can access all 3 course nodes
		Assert.assertTrue(publicElementXml.contains("<D:href>/webdav/coursefolders/_other/WebDAV%20course/_courseelementdata/Folder%20for%20all/</D:href>"));
		Assert.assertTrue(publicElementXml.contains("<D:href>/webdav/coursefolders/_other/WebDAV%20course/_courseelementdata/Student%20read-only/Readonly%20students/</D:href>"));
		Assert.assertTrue(publicElementXml.contains("<D:href>/webdav/coursefolders/_other/WebDAV%20course/_courseelementdata/Not%20for%20students/Not%20for%20students/</D:href>"));

		conn.close();
	}
	
	/**
	 * Check that different methods doesn't create directory
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void coursePermission_participantDirectory()
	throws IOException, URISyntaxException {
		webDAVModule.setEnableLearnersBookmarksCourse(true);
		webDAVModule.setEnableLearnersParticipatingCourses(true);
		
		//create a user
		Identity auth = JunitTestHelper.createAndPersistIdentityAsRndAuthor("webdav-4-");
		IdentityWithLogin participant = JunitTestHelper.createAndPersistRndUser("webdav-5");
		RepositoryEntry courseEntry = deployMkdirsCourse(auth);
		repositoryEntryRelationDao.addRole(participant.getIdentity(), courseEntry, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		//put a reference file there
		ICourse course = CourseFactory.loadCourse(courseEntry.getOlatResource());
		VFSContainer elements = (VFSContainer)course.getCourseFolderContainer().resolve("_courseelementdata");
		Assert.assertNotNull(elements);
		VFSContainer directory = (VFSContainer)elements.resolve("Directory");
		VFSContainer level_1_Container = directory.createChildContainer("DT_01");
		
		VFSLeaf readonlyLeaf = level_1_Container.createChildLeaf("readonly.txt");
		InputStream in = WebDAVCommandsTest.class.getResourceAsStream("text.txt");
		OutputStream out = readonlyLeaf.getOutputStream(false);
		FileUtils.copy(in, out);
		
		//check
		VFSItem readonlyLeafBis = level_1_Container.resolve("readonly.txt");
		Assert.assertNotNull(readonlyLeafBis);
		
		
		//participant try to put a file
		WebDAVConnection conn = new WebDAVConnection();
		conn.setCredentials(participant);
		URI courseUri = conn.getBaseURI().path("webdav").path("coursefolders").path("_other").path("Mkdirs").build();
		
		
		//MKCOL in the folder at the second level
		URI level2Uri = UriBuilder.fromUri(courseUri).path("_courseelementdata")
				.path("Directory").path("DT_01").path("DT_11").build();
		int mkcol2Code = conn.mkcol(level2Uri);
		Assert.assertEquals(409, mkcol2Code);		
		//check	
		VFSItem level2Mkcol = level_1_Container.resolve("DT_11");
		Assert.assertNull(level2Mkcol);
		
		
		//MKCOL in the folder at the first level
		URI level1Uri = UriBuilder.fromUri(courseUri).path("_courseelementdata")
						.path("Directory").path("DT_02").build();
		int mkcol1Code = conn.mkcol(level1Uri);
		Assert.assertEquals(409, mkcol1Code);
		VFSItem level1Mkcol = directory.resolve("DT_02");
		Assert.assertNull(level1Mkcol);
		
		
		//PROPFIND in second level
		int propfind2Code = conn.propfindTry(level2Uri, 1);
		Assert.assertEquals(404, propfind2Code);	
		//check	
		VFSItem level2Propfind = level_1_Container.resolve("DT_11");
		Assert.assertNull(level2Propfind);

		
		//PROPFIND in first level
		int propfind1Code = conn.propfindTry(level2Uri, 1);
		Assert.assertEquals(404, propfind1Code);	
		//check	
		VFSItem level1Propfind = level_1_Container.resolve("DT_02");
		Assert.assertNull(level1Propfind);
		
		
		//LOCK in the second level
		int lock2Code = conn.lockTry(level2Uri, UUID.randomUUID().toString());
		Assert.assertEquals(403, lock2Code);	
		//check	
		VFSItem level2Lock = level_1_Container.resolve("DT_11");
		Assert.assertNull(level2Lock);

		
		//LOCK in the first level
		int lock1Code = conn.lockTry(level2Uri, UUID.randomUUID().toString());
		Assert.assertEquals(403, lock1Code);	
		//check	
		VFSItem level1Lock = level_1_Container.resolve("DT_02");
		Assert.assertNull(level1Lock);
		
		
		//MKCOL in the folder deeper
		VFSContainer level_2_Container = level_1_Container.createChildContainer("DT2_01");
		VFSContainer level_3_Container = level_2_Container.createChildContainer("DT3_01");
		VFSContainer level_4_Container = level_3_Container.createChildContainer("DT4_01");
		Assert.assertNotNull(level_4_Container);

		URI level4Uri = UriBuilder.fromUri(courseUri).path("_courseelementdata")
				.path("Directory").path("DT_01").path("DT2_01").path("DT3_01").path("DT4_01").build();
		int propfind4Code = conn.propfindTry(level4Uri, 1);
		Assert.assertEquals(207, propfind4Code);
		
		URI level5Uri = UriBuilder.fromUri(level4Uri).path("DT5_01").build();
		
		int mkcol5Code = conn.mkcol(level5Uri);
		Assert.assertEquals(409, mkcol5Code);		
		//check	
		VFSItem level5Mkcol = level_1_Container.resolve("DT2_01").resolve("DT3_01").resolve("DT4_01").resolve("DT5_01");
		Assert.assertNull(level5Mkcol);
	
		conn.close();
	}
	
	@Test
	public void customizingFolder()
	throws IOException, URISyntaxException {
		IdentityWithLogin admin = JunitTestHelper.createAndPersistRndAdmin("admin-webdav");
		dbInstance.commitAndCloseSession();
		
		WebDAVConnection conn = new WebDAVConnection();
		conn.setCredentials(admin);

		//Has access?
		URI customizingUri = conn.getBaseURI().path("webdav").path("customizing").build();
		String customizingXml = conn.propfind(customizingUri, 2);
		Assert.assertTrue(customizingXml.contains("<D:href>/webdav/customizing/</D:href>"));

		//PUT in the folder
		String randomFilename = "infos" + UUID.randomUUID() + ".txt";
		URI textUri = conn.getBaseURI().path("webdav").path("customizing").path(randomFilename).build();
		HttpPut put = conn.createPut(textUri);
		InputStream dataStream = WebDAVCommandsTest.class.getResourceAsStream("text.txt");
		InputStreamEntity entity = new InputStreamEntity(dataStream, -1);
		put.setEntity(entity);
		HttpResponse putResponse = conn.execute(put);
		Assert.assertEquals(201, putResponse.getStatusLine().getStatusCode());
		
		//GET
		HttpGet get = conn.createGet(textUri);
		HttpResponse getResponse = conn.execute(get);
		Assert.assertEquals(200, getResponse.getStatusLine().getStatusCode());
		String text = EntityUtils.toString(getResponse.getEntity());
		Assert.assertEquals("Small text", text);

		conn.close();
	}
	
	@Test
	public void customizingFolder_permission()
	throws IOException, URISyntaxException {
		IdentityWithLogin user = JunitTestHelper.createAndPersistRndUser("user-webdav");
		dbInstance.commitAndCloseSession();
		
		WebDAVConnection conn = new WebDAVConnection();
		conn.setCredentials(user);

		URI customizingUri = conn.getBaseURI().path("webdav").path("customizing").build();
		HttpPropFind propfind = new HttpPropFind(customizingUri);
		propfind.addHeader("Depth", Integer.toString(2));
		HttpResponse response = conn.execute(propfind);
		Assert.assertEquals(404, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());

		conn.close();
	}
	
	/**
	 * Default are the following User-Agent forbidden: empty, -
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void forbiddenUserAgent()
	throws IOException, URISyntaxException {
		IdentityWithLogin user = JunitTestHelper.createAndPersistRndUser("webdav-2-");
		
		//create a file
		String publicPath = FolderConfig.getUserHome(user.getIdentity()) + "/public";
		VFSContainer vfsPublic = VFSManager.olatRootContainer(publicPath, null);
		createFile(vfsPublic, "test_head.txt");
		
		WebDAVConnection conn = new WebDAVConnection();
		URI publicUri = conn.getBaseURI().path("webdav").path("home").path("public").path("test_head.txt").build();

		//head file with standard Apache User Agent -> Ok
		conn.setCredentials(user);
		HttpResponse response = conn.head(publicUri);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		conn.close();
		
		// check with  "-" as User-Agent -> Forbidden
		WebDAVConnection hyphenConn = new WebDAVConnection("-");
		hyphenConn.setCredentials(user);
		HttpResponse hyphenResponse = hyphenConn.head(publicUri);
		Assert.assertEquals(403, hyphenResponse.getStatusLine().getStatusCode());
		EntityUtils.consume(hyphenResponse.getEntity());
		hyphenConn.close();
		
		// check with  "" as User-Agent -> Forbidden
		WebDAVConnection emptyConn = new WebDAVConnection("-");
		emptyConn.setCredentials(user);
		HttpResponse emptyResponse = emptyConn.head(publicUri);
		Assert.assertEquals(403, emptyResponse.getStatusLine().getStatusCode());
		EntityUtils.consume(emptyResponse.getEntity());
		emptyConn.close();
	}
	
	private VFSItem createFile(VFSContainer container, String filename) throws IOException {
		VFSLeaf testLeaf = container.createChildLeaf(filename);
		try(InputStream in = WebDAVCommandsTest.class.getResourceAsStream("text.txt");
				OutputStream out = testLeaf.getOutputStream(false)) {
			FileUtils.copy(in, out);
			out.flush();
		} catch(IOException e) {
			log.error("", e);
		}
		return container.resolve(filename);
	}
	
	private RepositoryEntry deployMkdirsCourse(Identity author) 
	throws URISyntaxException {
		URL courseWithForumsUrl = WebDAVCommandsTest.class.getResource("mkdirs.zip");
		return deployTestCourse(author, null, "Mkdirs", courseWithForumsUrl);
	}

	private RepositoryEntry deployTestCourse(Identity author, Identity coAuthor)
	throws URISyntaxException {
		URL courseWithForumsUrl = CoursePublishTest.class.getResource("myCourseWS.zip");
		return deployTestCourse(author, coAuthor, "Kurs", courseWithForumsUrl);
	}
	
	private RepositoryEntry deployTestCourse(Identity author, Identity coAuthor, String displayName, URL courseWithForumsUrl)
	throws URISyntaxException {
		Assert.assertNotNull(courseWithForumsUrl);
		File courseWithForums = new File(courseWithForumsUrl.toURI());
		RepositoryEntry re = JunitTestHelper.deployCourse(author, displayName, courseWithForums);
		repositoryService.addRole(author, re, GroupRoles.owner.name());
		if(coAuthor != null) {
			repositoryService.addRole(coAuthor, re, GroupRoles.owner.name());
		}
		dbInstance.commitAndCloseSession();
		return re;
	}
}

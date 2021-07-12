/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/


package org.olat.restapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.BCCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeConfiguration;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.restapi.support.vo.FolderVO;
import org.olat.restapi.support.vo.FolderVOes;
import org.olat.test.JunitTestHelper;
import org.olat.test.JunitTestHelper.IdentityWithLogin;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

public class CoursesFoldersTest extends OlatRestTestCase {

	private RestConnection conn;
	
	@Autowired
	private DB dbInstance;
	
	@Before
	public void setUp() throws Exception {
		conn = new RestConnection();
		dbInstance.intermediateCommit();
	}
	
  @After
	public void tearDown() throws Exception {
		try {
			if(conn != null) {
				conn.shutdown();
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	@Test
	public void testGetFolderInfo() throws IOException, URISyntaxException {
		assertTrue(conn.login("administrator", "openolat"));
		
		CourseWithBC courseWithBc = deployCourse();
		URI uri = UriBuilder.fromUri(getNodeURI(courseWithBc)).build();
		HttpGet get = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(get);
		assertEquals(200, response.getStatusLine().getStatusCode());
		FolderVO folder = conn.parse(response, FolderVO.class);
		assertNotNull(folder);
	}
	
	/**
	 * Check that the permission check are not to restrictive
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void testGetFolderInfoByUser() throws IOException, URISyntaxException {
		IdentityWithLogin user = JunitTestHelper.createAndPersistRndUser("rest-user-bc");
		assertTrue(conn.login(user));
		
		CourseWithBC courseWithBc = deployCourse();
		URI uri = UriBuilder.fromUri(getNodeURI(courseWithBc)).build();
		HttpGet get = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(get);
		assertEquals(200, response.getStatusLine().getStatusCode());
		FolderVO folder = conn.parse(response, FolderVO.class);
		assertNotNull(folder);
	}
	
	@Test
	public void testGetFoldersInfo() throws IOException, URISyntaxException {
		assertTrue(conn.login("administrator", "openolat"));
		CourseWithBC courseWithBc = deployCourse();

		URI uri = UriBuilder.fromUri(getNodesURI(courseWithBc)).build();
		HttpGet get = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(get);
		assertEquals(200, response.getStatusLine().getStatusCode());
		FolderVOes folders = conn.parse(response, FolderVOes.class);
		assertNotNull(folders);
		assertEquals(1, folders.getTotalCount());
		assertNotNull(folders.getFolders());
		assertEquals(1, folders.getFolders().length);
	}
	
	@Test
	public void testUploadFile() throws IOException, URISyntaxException {
		assertTrue(conn.login("administrator", "openolat"));
		CourseWithBC courseWithBc = deployCourse();
		
		URI uri = UriBuilder.fromUri(getNodeURI(courseWithBc)).path("files").build();
		
		//create single page
		URL fileUrl = CoursesFoldersTest.class.getResource("singlepage.html");
		assertNotNull(fileUrl);
		File file = new File(fileUrl.toURI());
		
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		conn.addMultipart(method, file.getName(), file);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());

		VFSContainer folder = BCCourseNode.getNodeFolderContainer((BCCourseNode)courseWithBc.bcNode, courseWithBc.course.getCourseEnvironment());
		VFSItem item = folder.resolve(file.getName());
		assertNotNull(item);
	}
	
	@Test
	public void testUploadFile_withSpecialCharacter() throws IOException, URISyntaxException {
		assertTrue(conn.login("administrator", "openolat"));
		CourseWithBC courseWithBc = deployCourse();
		
		URI uri = UriBuilder.fromUri(getNodeURI(courseWithBc)).path("files").build();
		
		//create single page
		URL fileUrl = CoursesFoldersTest.class.getResource("singlepage.html");
		assertNotNull(fileUrl);
		File file = new File(fileUrl.toURI());
		String filename = "SingleP\u00E4ge.html";
		
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		conn.addMultipart(method, filename, file);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());

		VFSContainer folder = BCCourseNode.getNodeFolderContainer((BCCourseNode)courseWithBc.bcNode, courseWithBc.course.getCourseEnvironment());
		VFSItem item = folder.resolve(filename);
		assertNotNull(item);
		assertEquals(filename, item.getName());
	}
	
	@Test
	public void testCreateFolder() throws IOException, URISyntaxException {
		assertTrue(conn.login("administrator", "openolat"));
		CourseWithBC courseWithBc = deployCourse();
		
		URI uri = UriBuilder.fromUri(getNodeURI(courseWithBc)).path("files").path("RootFolder").build();
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		
		VFSContainer folder = BCCourseNode.getNodeFolderContainer((BCCourseNode)courseWithBc.bcNode, courseWithBc.course.getCourseEnvironment());
		VFSItem item = folder.resolve("RootFolder");
		assertNotNull(item);
		assertTrue(item instanceof VFSContainer);
	}
	
	@Test
	public void testCreateFolders() throws IOException, URISyntaxException {
		assertTrue(conn.login("administrator", "openolat"));
		CourseWithBC courseWithBc = deployCourse();
		
		URI uri = UriBuilder.fromUri(getNodeURI(courseWithBc)).path("files").path("NewFolder1").path("NewFolder2").build();
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		
		VFSContainer folder = BCCourseNode.getNodeFolderContainer((BCCourseNode)courseWithBc.bcNode, courseWithBc.course.getCourseEnvironment());
		VFSItem item = folder.resolve("NewFolder1");
		assertNotNull(item);
		assertTrue(item instanceof VFSContainer);
		
		VFSContainer newFolder1 = (VFSContainer)item;
		VFSItem item2 = newFolder1.resolve("NewFolder2");
		assertNotNull(item2);
		assertTrue(item2 instanceof VFSContainer);
	}
	
	@Test
	public void testCreateFolders_tooMany() throws IOException, URISyntaxException {
		assertTrue(conn.login("administrator", "openolat"));
		CourseWithBC courseWithBc = deployCourse();
		URI uri = UriBuilder.fromUri(getNodeURI(courseWithBc)).path("files").path("RootFolder")
				.path("Folder").path("Folder").path("Folder").path("Folder").path("Folder")
				.path("Folder").path("Folder").path("Folder").path("Folder").path("Folder")
				.path("Folder").path("Folder").path("Folder").path("Folder").path("Folder")
				.path("Folder").path("Folder").path("Folder").path("Folder").path("Folder").build();
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(406, response.getStatusLine().getStatusCode());
	}
	
	@Test
	public void testCreateFolders_withSpecialCharacters() throws IOException, URISyntaxException {
		assertTrue(conn.login("administrator", "openolat"));
		
		CourseWithBC courseWithBc = deployCourse();
		
		URI uri = UriBuilder.fromUri(getNodeURI(courseWithBc)).path("files").path("RootFolder")
				.path("F\u00FClder").build();
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		
		VFSContainer folder = BCCourseNode.getNodeFolderContainer((BCCourseNode)courseWithBc.bcNode, courseWithBc.course.getCourseEnvironment());
		VFSItem item = folder.resolve("RootFolder/F\u00FClder");
		assertNotNull(item);
		assertTrue(item instanceof VFSContainer);
	}
	
	@Test
	public void deleteFolder() throws IOException, URISyntaxException {
		CourseWithBC courseWithBc = deployCourse();
		
		//add some folders
		VFSContainer folder = BCCourseNode.getNodeFolderContainer((BCCourseNode)courseWithBc.bcNode, courseWithBc.course.getCourseEnvironment());
		VFSItem item = folder.resolve("FolderToDelete");
		if(item == null) {
			folder.createChildContainer("FolderToDelete");
		}
		
		assertTrue(conn.login("administrator", "openolat"));
		
		URI uri = UriBuilder.fromUri(getNodeURI(courseWithBc)).path("files").path("FolderToDelete").build();
		HttpDelete method = conn.createDelete(uri, MediaType.APPLICATION_JSON);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		
		VFSItem deletedItem = folder.resolve("FolderToDelete");
		assertNull(deletedItem);
	}
	
	private URI getNodeURI(CourseWithBC courseWithBc) {
		return UriBuilder.fromUri(getContextURI()).path("repo").path("courses").path(courseWithBc.course.getResourceableId().toString())
			.path("elements").path("folder").path(courseWithBc.bcNode.getIdent()).build();
	}
	
	private URI getNodesURI(CourseWithBC courseWithBc) {
		return UriBuilder.fromUri(getContextURI()).path("repo").path("courses").path(courseWithBc.course.getResourceableId().toString())
			.path("elements").path("folder").build();
	}
	
	private CourseWithBC deployCourse() {
		Identity admin = JunitTestHelper.findIdentityByLogin("administrator");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(admin);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		dbInstance.intermediateCommit();
		
		//create a folder
		CourseNode rootNode = course.getRunStructure().getRootNode();
		CourseNodeConfiguration newNodeConfig = CourseNodeFactory.getInstance().getCourseNodeConfiguration("bc");
		CourseNode bcNode = newNodeConfig.getInstance(rootNode);
		bcNode.setShortTitle("Folder");
		bcNode.setNoAccessExplanation("You don't have access");
		course.getEditorTreeModel().addCourseNode(bcNode, rootNode);

		CourseFactory.publishCourse(course, RepositoryEntryStatusEnum.published, true, false, admin, Locale.ENGLISH);
		return new CourseWithBC(course, bcNode);
	}
	
	private static class CourseWithBC {
		private final ICourse course;
		private final CourseNode bcNode;
		
		public CourseWithBC(ICourse course, CourseNode bcNode) {
			this.course = course;
			this.bcNode = bcNode;
		}
		
	}
}

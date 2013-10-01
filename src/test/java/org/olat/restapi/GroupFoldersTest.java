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
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.Constants;
import org.olat.basesecurity.SecurityGroup;
import org.olat.collaboration.CollaborationTools;
import org.olat.collaboration.CollaborationToolsFactory;
import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.FileUtils;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.DisplayMembers;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.restapi.support.vo.FileVO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatJerseyTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  5 avr. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class GroupFoldersTest extends OlatJerseyTestCase {
	
	private Identity owner1, owner2, part1, part2;
	private BusinessGroup g1, g2;
	private OLATResource course;
	private RestConnection conn;
	
	@Autowired
	private BusinessGroupService businessGroupService;
	
	/**
	 * Set up a course with learn group and group area
	 * EXACTLY THE SAME AS GroupMgmTest
	 * @see org.olat.test.OlatJerseyTestCase#setUp()
	 */
	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
		conn = new RestConnection();
		//create a course with learn group
		
		owner1 = JunitTestHelper.createAndPersistIdentityAsUser("rest-one");
		owner2 = JunitTestHelper.createAndPersistIdentityAsUser("rest-two");
		part1 = JunitTestHelper.createAndPersistIdentityAsUser("rest-four");
		part2 = JunitTestHelper.createAndPersistIdentityAsUser("rest-five");
		
		OLATResourceManager rm = OLATResourceManager.getInstance();
		// create course and persist as OLATResourceImpl
		OLATResourceable resourceable = OresHelper.createOLATResourceableInstance("junitcourse",System.currentTimeMillis());
		RepositoryEntry re = RepositoryManager.getInstance().createRepositoryEntryInstance("administrator");
		re.setCanDownload(false);
		re.setCanLaunch(true);
		re.setDisplayname("rest-re");
		re.setResourcename("-");
		re.setAccess(0);// Access for nobody
		re.setOwnerGroup(null);
		
		// create security group
		BaseSecurity securityManager = BaseSecurityManager.getInstance();
		SecurityGroup newGroup = securityManager.createAndPersistSecurityGroup();
		// member of this group may modify member's membership
		securityManager.createAndPersistPolicy(newGroup, Constants.PERMISSION_ACCESS, newGroup);
		// members of this group are always authors also
		securityManager.createAndPersistPolicy(newGroup, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_AUTHOR);
		securityManager.addIdentityToSecurityGroup(owner1, newGroup);
		re.setOwnerGroup(newGroup);
		
		course =  rm.createOLATResourceInstance(resourceable);
		DBFactory.getInstance().saveObject(course);
		DBFactory.getInstance().intermediateCommit();

		OLATResource ores = OLATResourceManager.getInstance().findOrPersistResourceable(resourceable);
		re.setOlatResource(ores);
		RepositoryManager.getInstance().saveRepositoryEntry(re);
		DBFactory.getInstance().intermediateCommit();
		
		//create learn group
	    BaseSecurity secm = BaseSecurityManager.getInstance();
			
	    // 1) context one: learning groups
			RepositoryEntry c1 =  JunitTestHelper.createAndPersistRepositoryEntry();
	    // create groups without waiting list
	    g1 = businessGroupService.createBusinessGroup(null, "rest-g1", null, 0, 10, false, false, c1);
	    g2 = businessGroupService.createBusinessGroup(null, "rest-g2", null, 0, 10, false, false, c1);
	    
	    //permission to see owners and participants
	    businessGroupService.updateDisplayMembers(g1, new DisplayMembers(false, false, false));
	    businessGroupService.updateDisplayMembers(g2, new DisplayMembers(true, true, false));
	    
	    // members g1
	    secm.addIdentityToSecurityGroup(owner1, g1.getOwnerGroup());
	    secm.addIdentityToSecurityGroup(owner2, g1.getOwnerGroup());
	    secm.addIdentityToSecurityGroup(part1, g1.getPartipiciantGroup());
	    secm.addIdentityToSecurityGroup(part2, g1.getPartipiciantGroup());
	    
	    // members g2
	    secm.addIdentityToSecurityGroup(owner1, g2.getOwnerGroup());
	    secm.addIdentityToSecurityGroup(part1, g2.getPartipiciantGroup());
	    
	    DBFactory.getInstance().closeSession(); // simulate user clicks
    
	    //3) collaboration tools
	    CollaborationTools collabTools1 = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(g1);
	    collabTools1.setToolEnabled(CollaborationTools.TOOL_FOLDER, true);
  
    	CollaborationTools collabTools2 = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(g2);
    	collabTools2.setToolEnabled(CollaborationTools.TOOL_FOLDER, true);
    
    	DBFactory.getInstance().closeSession(); // simulate user clicks
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
	public void testGetFolder() throws IOException, URISyntaxException {
		assertTrue(conn.login("rest-one", "A6B7C8"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("/groups/" + g1.getKey() + "/folder").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		InputStream body = response.getEntity().getContent();
		assertNotNull(body);
	}
	
	@Test
	public void testGetSubFolder() throws IOException, URISyntaxException {
		//create some sub folders
		CollaborationTools collabTools1 = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(g1);
		String folderRelPath = collabTools1.getFolderRelPath();
		OlatRootFolderImpl folder = new OlatRootFolderImpl(folderRelPath, null);
		VFSContainer newFolder1 = folder.createChildContainer("New folder 1");
		if(newFolder1 == null) {
			newFolder1 = (VFSContainer)folder.resolve("New folder 1");
		}
		assertNotNull(newFolder1);
		VFSContainer newFolder11 = newFolder1.createChildContainer("New folder 1_1");
		if(newFolder11 == null) {
			newFolder11 = (VFSContainer)newFolder1.resolve("New folder 1_1");
		}
		assertNotNull(newFolder11);
		
		
		assertTrue(conn.login("rest-one", "A6B7C8"));
		
		//get root folder
		URI request0 = UriBuilder.fromUri(getContextURI()).path("/groups/" + g1.getKey() + "/folder/").build();
		HttpGet method0 = conn.createGet(request0, MediaType.APPLICATION_JSON, true);
		HttpResponse code0 = conn.execute(method0);
		assertEquals(200, code0.getStatusLine().getStatusCode());
		InputStream body0 = code0.getEntity().getContent();
		assertNotNull(body0);
		List<FileVO> fileVos0 = parseFileArray(body0);
		assertNotNull(fileVos0);
		assertEquals(1, fileVos0.size());
		
		//get sub folder
		URI request1 = UriBuilder.fromUri(getContextURI()).path("/groups/" + g1.getKey() + "/folder/New_folder_1").build();
		HttpGet method1 = conn.createGet(request1, MediaType.APPLICATION_JSON, true);
		HttpResponse code1 = conn.execute(method1);
		assertEquals(200, code1.getStatusLine().getStatusCode());
		InputStream body1 = code1.getEntity().getContent();
		assertNotNull(body1);
		List<FileVO> fileVos1 = parseFileArray(body1);
		assertNotNull(fileVos1);
		assertEquals(1, fileVos1.size());
		
		//get sub folder by link
		FileVO fileVO = fileVos1.get(0);
		URI fileUri = new URI(fileVO.getHref());
		HttpGet brutMethod = conn.createGet(fileUri, "*/*", true);
		brutMethod.addHeader("Accept", MediaType.APPLICATION_JSON);
		HttpResponse codeBrut = conn.execute(brutMethod);
		assertEquals(200, codeBrut.getStatusLine().getStatusCode());
		EntityUtils.consume(codeBrut.getEntity());

		// get sub sub folder
		URI request2 = UriBuilder.fromUri(getContextURI()).path("/groups/" + g1.getKey() + "/folder/New_folder_1/New_folder_1_1").build();
		HttpGet method2 = conn.createGet(request2, MediaType.APPLICATION_JSON, true);
		HttpResponse code2 = conn.execute(method2);
		assertEquals(200, code2.getStatusLine().getStatusCode());
		InputStream body2 = code2.getEntity().getContent();
		assertNotNull(body2);
		EntityUtils.consume(code2.getEntity());
		
		//get sub folder with end /
		URI request3 = UriBuilder.fromUri(getContextURI()).path("/groups/" + g1.getKey() + "/folder/New_folder_1/").build();
		HttpGet method3 = conn.createGet(request3, MediaType.APPLICATION_JSON, true);
		HttpResponse code3 = conn.execute(method3);
		assertEquals(200, code3.getStatusLine().getStatusCode());
		InputStream body3 = code3.getEntity().getContent();
		assertNotNull(body3);
		List<FileVO> fileVos3 = parseFileArray(body3);
		assertNotNull(fileVos3);
		assertEquals(1, fileVos3.size());
	}
	
	@Test
	public void testGetFile() throws IOException, URISyntaxException {
		//create some sub folders and copy file
		CollaborationTools collabTools2 = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(g2);
		String folderRelPath = collabTools2.getFolderRelPath();
		OlatRootFolderImpl folder = new OlatRootFolderImpl(folderRelPath, null);
		VFSContainer newFolder1 = folder.createChildContainer("New folder 2");
		if(newFolder1 == null) {
			newFolder1 = (VFSContainer)folder.resolve("New folder 2");
		}
		VFSLeaf file = (VFSLeaf)newFolder1.resolve("portrait.jpg");
		if(file == null) {
			file = newFolder1.createChildLeaf("portrait.jpg");
			OutputStream out = file.getOutputStream(true);
			InputStream in = GroupFoldersTest.class.getResourceAsStream("portrait.jpg");
			FileUtils.copy(in, out, file.getSize());
			FileUtils.closeSafely(in);
			FileUtils.closeSafely(out);
		}
		
		// get the file
		assertTrue(conn.login("rest-one", "A6B7C8"));
		URI request = UriBuilder.fromUri(getContextURI()).path("/groups/" + g2.getKey() + "/folder/New_folder_2/portrait.jpg").build();
		HttpGet method = conn.createGet(request, "*/*", true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		InputStream body = response.getEntity().getContent();
		assertNotNull(body);
		assertTrue(10 > body.available());
		assertEquals(new Long(file.getSize()), new Long(body.available()));
	}
	
	@Test
	public void testCreateFolder() throws IOException, URISyntaxException {
		assertTrue(conn.login("rest-one", "A6B7C8"));
		URI request = UriBuilder.fromUri(getContextURI()).path("/groups/" + g1.getKey() + "/folder/New_folder_1/New_folder_1_1/New_folder_1_1_1").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		InputStream body = response.getEntity().getContent();
		assertEquals(200, response.getStatusLine().getStatusCode());
		assertNotNull(body);
	}

	//@Test not working -> Jersey ignore the request and return 200 (why?)
	public void testCreateFoldersWithSpecialCharacter() throws IOException, URISyntaxException {
		assertTrue(conn.login("rest-one", "A6B7C8"));
		URI request = UriBuilder.fromUri(getContextURI()).path("/groups/" + g1.getKey() + "/folder/New_folder_1/New_folder_1_1/New_folder_1 1 2").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		FileVO file = conn.parse(response, FileVO.class);
		assertNotNull(file);
	}
	
	@Test
	public void testCreateFoldersWithSpecialCharacter2() throws IOException, URISyntaxException {
		assertTrue(conn.login("rest-one", "A6B7C8"));
		URI request = UriBuilder.fromUri(getContextURI()).path("/groups/" + g1.getKey() + "/folder/New_folder_1/New_folder_1_1/").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		HttpEntity entity = MultipartEntityBuilder.create()
				.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
				.addTextBody("foldername", "New folder 1 2 3")
				.build();
		method.setEntity(entity);

		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		FileVO file = conn.parse(response, FileVO.class);
		assertNotNull(file);
		assertNotNull(file.getHref());
		assertNotNull(file.getTitle());
		assertEquals("New folder 1 2 3", file.getTitle());
	}
	
	protected <T> T parse(InputStream body, Class<T> cl) {
		try {
			ObjectMapper mapper = new ObjectMapper(jsonFactory);
			T obj = mapper.readValue(body, cl);
			return obj;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	protected List<FileVO> parseFileArray(InputStream body) {
		try {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(body, new TypeReference<List<FileVO>>(){/* */});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}

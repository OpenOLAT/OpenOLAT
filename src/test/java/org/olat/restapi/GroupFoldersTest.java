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
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
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
import org.olat.group.BusinessGroupManager;
import org.olat.group.BusinessGroupManagerImpl;
import org.olat.group.context.BGContext;
import org.olat.group.context.BGContextManager;
import org.olat.group.context.BGContextManagerImpl;
import org.olat.group.properties.BusinessGroupPropertyManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.restapi.UserMgmtTest;
import org.olat.restapi.support.vo.FileVO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatJerseyTestCase;

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
	
	/**
	 * Set up a course with learn group and group area
	 * EXACTLY THE SAME AS GroupMgmTest
	 * @see org.olat.test.OlatJerseyTestCase#setUp()
	 */
	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
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
	    BGContextManager cm = BGContextManagerImpl.getInstance();
	    BusinessGroupManager bgm = BusinessGroupManagerImpl.getInstance();
	    BaseSecurity secm = BaseSecurityManager.getInstance();
			
	    // 1) context one: learning groups
	    BGContext c1 = cm.createAndAddBGContextToResource("c1name-learn", course, BusinessGroup.TYPE_LEARNINGROUP, owner1, true);
	    // create groups without waiting list
	    g1 = bgm.createAndPersistBusinessGroup(BusinessGroup.TYPE_LEARNINGROUP, null, "rest-g1", null, new Integer(0), new Integer(10), false, false, c1);
	    g2 = bgm.createAndPersistBusinessGroup(BusinessGroup.TYPE_LEARNINGROUP, null, "rest-g2", null, new Integer(0), new Integer(10), false, false, c1);
	    
	    //permission to see owners and participants
	    BusinessGroupPropertyManager bgpm1 = new BusinessGroupPropertyManager(g1);
	    bgpm1.updateDisplayMembers(false, false, false);
	    BusinessGroupPropertyManager bgpm2 = new BusinessGroupPropertyManager(g2);
	    bgpm2.updateDisplayMembers(true, true, false);
	    
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
	
	@Test
	public void testGetFolder() throws IOException {
		HttpClient c = loginWithCookie("rest-one", "A6B7C8");
		
		String request = "/groups/" + g1.getKey() + "/folder";
		GetMethod method = createGet(request, MediaType.APPLICATION_JSON, true);
		int code = c.executeMethod(method);
		assertEquals(200, code);
		InputStream body = method.getResponseBodyAsStream();
		assertNotNull(body);
		
		method.releaseConnection();
	}
	
	@Test
	public void testGetSubFolder() throws IOException {
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


		HttpClient c = loginWithCookie("rest-one", "A6B7C8");
		
		//get root folder
		String request0 = "/groups/" + g1.getKey() + "/folder/";
		GetMethod method0 = createGet(request0, MediaType.APPLICATION_JSON, true);
		int code0 = c.executeMethod(method0);
		assertEquals(200, code0);
		InputStream body0 = method0.getResponseBodyAsStream();
		assertNotNull(body0);
		List<FileVO> fileVos0 = parseFileArray(body0);
		assertNotNull(fileVos0);
		assertEquals(1, fileVos0.size());
		method0.releaseConnection();
		
		//get sub folder
		String request1 = "/groups/" + g1.getKey() + "/folder/New_folder_1";
		GetMethod method1 = createGet(request1, MediaType.APPLICATION_JSON, true);
		int code1 = c.executeMethod(method1);
		assertEquals(200, code1);
		InputStream body1 = method1.getResponseBodyAsStream();
		assertNotNull(body1);
		List<FileVO> fileVos1 = parseFileArray(body1);
		assertNotNull(fileVos1);
		assertEquals(1, fileVos1.size());
		method1.releaseConnection();
		
		//get sub folder by link
		FileVO fileVO = fileVos1.get(0);
		GetMethod brutMethod = new GetMethod(fileVO.getHref());
		brutMethod.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
		brutMethod.addRequestHeader("Accept", MediaType.APPLICATION_JSON);
		int codeBrut = c.executeMethod(brutMethod);
		assertEquals(200, codeBrut);
		

		// get sub sub folder
		String request2 = "/groups/" + g1.getKey() + "/folder/New_folder_1/New_folder_1_1";
		GetMethod method2 = createGet(request2, MediaType.APPLICATION_JSON, true);
		int code2 = c.executeMethod(method2);
		assertEquals(200, code2);
		InputStream body2 = method2.getResponseBodyAsStream();
		assertNotNull(body2);
		method2.releaseConnection();
		
		//get sub folder with end /
		String request3 = "/groups/" + g1.getKey() + "/folder/New_folder_1/";
		GetMethod method3 = createGet(request3, MediaType.APPLICATION_JSON, true);
		int code3 = c.executeMethod(method3);
		assertEquals(200, code3);
		InputStream body3 = method3.getResponseBodyAsStream();
		assertNotNull(body3);
		List<FileVO> fileVos3 = parseFileArray(body3);
		assertNotNull(fileVos3);
		assertEquals(1, fileVos3.size());
		method3.releaseConnection();
	}
	
	@Test
	public void testGetFile() throws IOException {
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
			InputStream in = UserMgmtTest.class.getResourceAsStream("portrait.jpg");
			FileUtils.copy(in, out, file.getSize());
			FileUtils.closeSafely(in);
			FileUtils.closeSafely(out);
		}
		
		// get the file
		HttpClient c = loginWithCookie("rest-one", "A6B7C8");
		String request = "/groups/" + g2.getKey() + "/folder/New_folder_2/portrait.jpg";
		GetMethod method = createGet(request, "*/*", true);
		int code = c.executeMethod(method);
		assertEquals(200, code);
		InputStream body = method.getResponseBodyAsStream();
		assertNotNull(body);
		assertTrue(10 > body.available());
		assertEquals(new Long(file.getSize()), new Long(body.available()));

		method.releaseConnection();
	}
	
	@Test
	public void testCreateFolder() throws IOException {
		HttpClient c = loginWithCookie("rest-one", "A6B7C8");
		String request = "/groups/" + g1.getKey() + "/folder/New_folder_1/New_folder_1_1/New_folder_1_1_1";
		
		PutMethod method = createPut(request, MediaType.APPLICATION_JSON, true);
		int code = c.executeMethod(method);
		InputStream body = method.getResponseBodyAsStream();
		assertEquals(200, code);
		assertNotNull(body);
		
		method.releaseConnection();
	}

	//@Test not working -> Jersey ignore the request and return 200 (why?)
	public void testCreateFoldersWithSpecialCharacter() throws IOException {
		HttpClient c = loginWithCookie("rest-one", "A6B7C8");
		String request = "/groups/" + g1.getKey() + "/folder/New_folder_1/New_folder_1_1/New_folder_1 1 2";
		PutMethod method = createPut(request, MediaType.APPLICATION_JSON, true);
		int code = c.executeMethod(method);
		assertEquals(200, code);
		String body = method.getResponseBodyAsString();
		FileVO file = parse(body, FileVO.class);
		assertNotNull(file);
	}
	
	@Test
	public void testCreateFoldersWithSpecialCharacter2() throws IOException {
		HttpClient c = loginWithCookie("rest-one", "A6B7C8");
		String request = "/groups/" + g1.getKey() + "/folder/New_folder_1/New_folder_1_1/";
		PutMethod method = createPut(request, MediaType.APPLICATION_JSON, true);
		method.addRequestHeader("Content-Type", MediaType.MULTIPART_FORM_DATA);
		Part[] parts = { 
				new StringPart("foldername","New folder 1 2 3")
		};
		method.setRequestEntity(new MultipartRequestEntity(parts, method.getParams()));

		int code = c.executeMethod(method);
		assertEquals(200, code);
		String body = method.getResponseBodyAsString();
		FileVO file = parse(body, FileVO.class);
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

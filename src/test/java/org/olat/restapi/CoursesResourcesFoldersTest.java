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
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.util.FileUtils;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.ICourse;
import org.olat.restapi.repository.course.CoursesWebService;
import org.olat.restapi.support.vo.LinkVO;
import org.olat.test.OlatJerseyTestCase;

public class CoursesResourcesFoldersTest extends OlatJerseyTestCase {

	private static ICourse course1;
	private static Identity admin;
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		
		admin = BaseSecurityManager.getInstance().findIdentityByName("administrator");
		course1 = CoursesWebService.createEmptyCourse(admin, "course1", "course1 long name", null);
		
		//copy a couple of files in the resource folder
		VFSContainer container = course1.getCourseFolderContainer();
		copyFileInResourceFolder(container, "singlepage.html", "1_");
		copyFileInResourceFolder(container, "cp-demo.zip", "1_");
		VFSContainer subContainer = container.createChildContainer("SubDir");
		copyFileInResourceFolder(subContainer, "singlepage.html", "2_");
		VFSContainer subSubContainer = subContainer.createChildContainer("SubSubDir");
		copyFileInResourceFolder(subSubContainer, "singlepage.html", "3_");
		
		DBFactory.getInstance().intermediateCommit();
	}
	
	private void copyFileInResourceFolder(VFSContainer container, String filename, String prefix) {
		InputStream pageStream = CoursesElementsTest.class.getResourceAsStream(filename);
		VFSLeaf item = container.createChildLeaf(prefix + filename);
		OutputStream outStream = item.getOutputStream(false);
		FileUtils.copy(pageStream, outStream);
		FileUtils.closeSafely(pageStream);
		FileUtils.closeSafely(outStream);
	}
	
	@Test
	public void testGetFiles() throws IOException {
		HttpClient c = loginWithCookie("administrator", "openolat");
		URI uri = UriBuilder.fromUri(getCourseFolderURI()).build();
		GetMethod method = createGet(uri, MediaType.APPLICATION_JSON, true);
		int code = c.executeMethod(method);
		assertEquals(code, 200);
		
		String body = method.getResponseBodyAsString();
		List<LinkVO> links = parseLinkArray(body);
		assertNotNull(links);
		assertEquals(3, links.size());
	}
	
	@Test
	public void testGetFilesDeeper() throws IOException {
		HttpClient c = loginWithCookie("administrator", "openolat");
		URI uri = UriBuilder.fromUri(getCourseFolderURI()).path("SubDir").path("SubSubDir").path("SubSubSubDir").build();
		GetMethod method = createGet(uri, MediaType.APPLICATION_JSON, true);
		int code = c.executeMethod(method);
		assertEquals(code, 200);
		
		String body = method.getResponseBodyAsString();
		List<LinkVO> links = parseLinkArray(body);
		assertNotNull(links);
		assertEquals(1, links.size());
		assertEquals("3_singlepage.html", links.get(0).getTitle());
	}
	
	@Test
	public void testGetFileDeep() throws IOException {
		HttpClient c = loginWithCookie("administrator", "openolat");
		URI uri = UriBuilder.fromUri(getCourseFolderURI()).path("SubDir").path("SubSubDir").path("SubSubSubDir")
			.path("3_singlepage.html").build();
		GetMethod method = createGet(uri, "*/*", true);
		int code = c.executeMethod(method);
		assertEquals(code, 200);
		
		String body = method.getResponseBodyAsString();
		assertNotNull(body);
		assertTrue(body.startsWith("<html>"));
		
		String contentType = null;
		for(Header header:method.getResponseHeaders()){
			if("Content-Type".equals(header.getName())) {
				contentType = header.getValue();
				break;
			}
		}
		assertNotNull(contentType);
		assertEquals("text/html", contentType);
	}
	
	private URI getCourseFolderURI() {
		return UriBuilder.fromUri(getContextURI()).path("repo").path("courses").path(course1.getResourceableId().toString())
			.path("resourcefolders").path("coursefolder").build();
	}
}

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

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.restapi.support.vo.LinkVO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatRestTestCase;

public class CoursesResourcesFoldersTest extends OlatRestTestCase {
	
	private static final Logger log = Tracing.createLoggerFor(CoursesResourcesFoldersTest.class);

	private static ICourse course1;
	private static Identity admin;
	
	@Before
	public void setUp() throws Exception {
		admin = JunitTestHelper.findIdentityByLogin("administrator");
		RepositoryEntry courseEntry = JunitTestHelper.deployEmptyCourse(admin, "Empty course",
				RepositoryEntryStatusEnum.preparation);
		course1 = CourseFactory.loadCourse(courseEntry);
		
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
	
	private void copyFileInResourceFolder(VFSContainer container, String filename, String prefix)
	throws IOException {
		VFSLeaf item = container.createChildLeaf(prefix + filename);	
		try(InputStream pageStream = CoursesElementsTest.class.getResourceAsStream(filename);
				OutputStream outStream = item.getOutputStream(false);) {
			IOUtils.copy(pageStream, outStream);
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	@Test
	public void testGetFiles() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		URI uri = UriBuilder.fromUri(getCourseFolderURI()).build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		
		List<LinkVO> links = parseLinkArray(response.getEntity());
		assertNotNull(links);
		assertEquals(3, links.size());
		
		conn.shutdown();
	}
	
	@Test
	public void testGetFilesDeeper() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		URI uri = UriBuilder.fromUri(getCourseFolderURI()).path("SubDir").path("SubSubDir").path("SubSubSubDir").build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		
		List<LinkVO> links = parseLinkArray(response.getEntity());
		assertNotNull(links);
		assertEquals(1, links.size());
		assertEquals("3_singlepage.html", links.get(0).getTitle());
		
		conn.shutdown();
	}
	
	@Test
	public void testGetFileDeep() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		URI uri = UriBuilder.fromUri(getCourseFolderURI()).path("SubDir").path("SubSubDir").path("SubSubSubDir")
			.path("3_singlepage.html").build();
		HttpGet method = conn.createGet(uri, "*/*", true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		
		String body = EntityUtils.toString(response.getEntity());
		assertNotNull(body);
		assertTrue(body.startsWith("<html>"));
		
		String contentType = null;
		for(Header header:response.getAllHeaders()){
			if("Content-Type".equals(header.getName())) {
				contentType = header.getValue();
				break;
			}
		}
		assertNotNull(contentType);
		conn.shutdown();
	}
	
	private URI getCourseFolderURI() {
		return UriBuilder.fromUri(getContextURI()).path("repo").path("courses").path(course1.getResourceableId().toString())
			.path("resourcefolders").path("coursefolder").build();
	}
}

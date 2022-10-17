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
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.admin.securitygroup.gui.IdentitiesAddEvent;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * Test the security of a course
 * 
 * <P>
 * Initial Date:  6 mai 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class CourseSecurityTest extends OlatRestTestCase {

	private static final Logger log = Tracing.createLoggerFor(CourseTest.class);
	
	private Identity admin, id1, auth1, auth2;
	private ICourse course;
	private RestConnection conn;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private RepositoryManager repositoryManager;
	
	/**
	 * SetUp is called before each test.
	 */
	@Before
	public void setUp() throws Exception {
		conn = new RestConnection();
		try {
			// create course and persist as OLATResourceImpl
			admin = JunitTestHelper.findIdentityByLogin("administrator");
			id1 = JunitTestHelper.createAndPersistIdentityAsUser("id-c-s-0");
			Assert.assertNotNull(id1);
			auth1 = JunitTestHelper.createAndPersistIdentityAsAuthor("id-c-s-1");
			Assert.assertNotNull(auth1);
			auth2 = JunitTestHelper.createAndPersistIdentityAsAuthor("id-c-s-2");
			Assert.assertNotNull(auth2);
			
			RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(admin);
			course = CourseFactory.loadCourse(courseEntry);
			dbInstance.intermediateCommit();

			RepositoryEntry re = repositoryManager.lookupRepositoryEntry(course, false);
			IdentitiesAddEvent identitiesAddEvent = new IdentitiesAddEvent(Collections.singletonList(auth2));
			repositoryManager.addOwners(admin, identitiesAddEvent, re, null);
			
			dbInstance.closeSession();
		} catch (Exception e) {
			log.error("Exception in setUp(): " + e);
		}
	}
	
	@After
	public void tearDown() throws Exception {
		try {
			if(conn != null) {
				conn.shutdown();
			}
		} catch (Exception e) {
			log.error("Exception in tearDown(): " + e);
			e.printStackTrace();
			throw e;
		}
	}
	
	@Test
	public void testAdminCanEditCourse() throws IOException, URISyntaxException {
		assertTrue(conn.login("administrator", "openolat"));
		
		//create an structure node
		URI newStructureUri = getElementsUri(course).path("structure")
				.queryParam("position", "0")
				.queryParam("shortTitle", "Structure-admin-0")
				.queryParam("longTitle", "Structure-long-admin-0")
				.queryParam("objectives", "Structure-objectives-admin-0").build();
		HttpPut method = conn.createPut(newStructureUri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
	}
	
	@Test
	public void testIdCannotEditCourse() throws IOException, URISyntaxException {
		assertTrue(conn.login("id-c-s-0", "A6B7C8"));
		
		//create an structure node
		URI newStructureUri = getElementsUri(course).path("structure")
				.queryParam("position", "0")
				.queryParam("shortTitle", "Structure-id-0")
				.queryParam("longTitle", "Structure-long-id-0")
				.queryParam("objectives", "Structure-objectives-id-0").build();
		HttpPut method = conn.createPut(newStructureUri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(401, response.getStatusLine().getStatusCode());
	}
	
	@Test
	public void testAuthorCannotEditCourse() throws IOException, URISyntaxException {
		//author but not owner
		assertTrue(conn.login("id-c-s-1", "A6B7C8"));
		
		//create an structure node
		URI newStructureUri = getElementsUri(course).path("structure")
				.queryParam("position", "0")
				.queryParam("shortTitle", "Structure-id-0")
				.queryParam("longTitle", "Structure-long-id-0")
				.queryParam("objectives", "Structure-objectives-id-0").build();
		HttpPut method = conn.createPut(newStructureUri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(401, response.getStatusLine().getStatusCode());
	}
	
	@Test
	public void testAuthorCanEditCourse() throws IOException, URISyntaxException {
		//author and owner
		assertTrue(conn.login("id-c-s-2", "A6B7C8"));
		
		//create an structure node
		URI newStructureUri = getElementsUri(course).path("structure")
				.queryParam("position", "0")
				.queryParam("shortTitle", "Structure-id-0")
				.queryParam("longTitle", "Structure-long-id-0")
				.queryParam("objectives", "Structure-objectives-id-0").build();
		HttpPut method = conn.createPut(newStructureUri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
	}
	
	private UriBuilder getCoursesUri() {
		return UriBuilder.fromUri(getContextURI()).path("repo").path("courses");
	}
	
	private UriBuilder getElementsUri(ICourse c) {
		return getCoursesUri().path(c.getResourceableId().toString()).path("elements");
	}
}
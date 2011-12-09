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

import java.io.IOException;
import java.net.URI;
import java.util.Collections;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PutMethod;
import org.junit.Before;
import org.junit.Test;
import org.olat.admin.securitygroup.gui.IdentitiesAddEvent;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.course.ICourse;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.restapi.repository.course.CoursesWebService;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatJerseyTestCase;

/**
 * 
 * Description:<br>
 * Test the security of a course
 * 
 * <P>
 * Initial Date:  6 mai 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class CourseSecurityTest extends OlatJerseyTestCase {

	private static final OLog log = Tracing.createLoggerFor(CourseTest.class);
	
	private Identity admin, id1, auth1, auth2;
	private ICourse course;
	
	/**
	 * SetUp is called before each test.
	 */
	@Before
	public void setUp() throws Exception {
		super.setUp();
		try {
			// create course and persist as OLATResourceImpl
			admin = BaseSecurityManager.getInstance().findIdentityByName("administrator");
			id1 = JunitTestHelper.createAndPersistIdentityAsUser("id-c-s-0");
			auth1 = JunitTestHelper.createAndPersistIdentityAsAuthor("id-c-s-1");
			auth2 = JunitTestHelper.createAndPersistIdentityAsAuthor("id-c-s-2");
			
			course = CoursesWebService.createEmptyCourse(admin, "course-security-2", "Test course for the security test", null);
			DBFactory.getInstance().intermediateCommit();

			RepositoryManager rm = RepositoryManager.getInstance();
			RepositoryEntry re = rm.lookupRepositoryEntry(course, false);
			IdentitiesAddEvent identitiesAddEvent = new IdentitiesAddEvent(Collections.singletonList(auth2));
			rm.addOwners(admin, identitiesAddEvent, re);
			
			DBFactory.getInstance().closeSession();
		} catch (Exception e) {
			log.error("Exception in setUp(): " + e);
		}
	}
	
	@Test
	public void testAdminCanEditCourse() throws IOException {
		HttpClient c = loginWithCookie("administrator", "olat");
		
		//create an structure node
		URI newStructureUri = getElementsUri(course).path("structure").build();
		PutMethod method = createPut(newStructureUri, MediaType.APPLICATION_JSON, true);
		method.setQueryString(new NameValuePair[]{
				new NameValuePair("position", "0"),
				new NameValuePair("shortTitle", "Structure-admin-0"),
				new NameValuePair("longTitle", "Structure-long-admin-0"),
				new NameValuePair("objectives", "Structure-objectives-admin-0")
		});
		int code = c.executeMethod(method);
		assertEquals(200, code);
	}
	
	@Test
	public void testIdCannotEditCourse() throws IOException {
		HttpClient c = loginWithCookie("id-c-s-0", "A6B7C8");
		
		//create an structure node
		URI newStructureUri = getElementsUri(course).path("structure").build();
		PutMethod method = createPut(newStructureUri, MediaType.APPLICATION_JSON, true);
		method.setQueryString(new NameValuePair[]{
				new NameValuePair("position", "0"),
				new NameValuePair("shortTitle", "Structure-id-0"),
				new NameValuePair("longTitle", "Structure-long-id-0"),
				new NameValuePair("objectives", "Structure-objectives-id-0")
		});
		int code = c.executeMethod(method);
		assertEquals(401, code);
	}
	
	@Test
	public void testAuthorCannotEditCourse() throws IOException {
		//author but not owner
		HttpClient c = loginWithCookie("id-c-s-1", "A6B7C8");
		
		//create an structure node
		URI newStructureUri = getElementsUri(course).path("structure").build();
		PutMethod method = createPut(newStructureUri, MediaType.APPLICATION_JSON, true);
		method.setQueryString(new NameValuePair[]{
				new NameValuePair("position", "0"),
				new NameValuePair("shortTitle", "Structure-id-0"),
				new NameValuePair("longTitle", "Structure-long-id-0"),
				new NameValuePair("objectives", "Structure-objectives-id-0")
		});
		int code = c.executeMethod(method);
		assertEquals(401, code);
	}
	
	@Test
	public void testAuthorCanEditCourse() throws IOException {
		//author and owner
		HttpClient c = loginWithCookie("id-c-s-2", "A6B7C8");
		
		//create an structure node
		URI newStructureUri = getElementsUri(course).path("structure").build();
		PutMethod method = createPut(newStructureUri, MediaType.APPLICATION_JSON, true);
		method.setQueryString(new NameValuePair[]{
				new NameValuePair("position", "0"),
				new NameValuePair("shortTitle", "Structure-id-0"),
				new NameValuePair("longTitle", "Structure-long-id-0"),
				new NameValuePair("objectives", "Structure-objectives-id-0")
		});
		int code = c.executeMethod(method);
		assertEquals(200, code);
	}
	
	
	private UriBuilder getCoursesUri() {
		return UriBuilder.fromUri(getContextURI()).path("repo").path("courses");
	}
	
	private UriBuilder getElementsUri(ICourse c) {
		return getCoursesUri().path(c.getResourceableId().toString()).path("elements");
	}
}
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

import jakarta.ws.rs.core.MediaType;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.id.Identity;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.restapi.support.vo.CourseInfoVO;
import org.olat.restapi.support.vo.CourseInfoVOes;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatRestTestCase;

/**
 * 
 * Initial date: 08.05.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CoursesInfosTest extends OlatRestTestCase {
	
	@Test
	public void testGetCourseInfos() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));

		URI uri = conn.getContextURI().path("repo").path("courses").path("infos").build();

		HttpGet get = conn.createGet(uri, MediaType.APPLICATION_JSON + ";pagingspec=1.0", true);
		HttpResponse response = conn.execute(get);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		CourseInfoVOes infos = conn.parse(response, CourseInfoVOes.class);
		Assert.assertNotNull(infos);
		
		conn.shutdown();
	}
	
	@Test
	public void testGetCourseInfos_byId() throws IOException, URISyntaxException {
		Identity admin = JunitTestHelper.findIdentityByLogin("administrator");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(admin, "course-info 1",
				RepositoryEntryStatusEnum.preparation, false, false);
		ICourse course = CourseFactory.loadCourse(courseEntry);

		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI uri = conn.getContextURI().path("repo").path("courses").path("infos").path(course.getResourceableId().toString()).build();

		HttpGet get = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(get);
		assertEquals(200, response.getStatusLine().getStatusCode());
		CourseInfoVO infos = conn.parse(response, CourseInfoVO.class);
		Assert.assertNotNull(infos);
		Assert.assertEquals("course-info 1", infos.getTitle());
		Assert.assertEquals("course-info 1", infos.getDisplayName());

		conn.shutdown();
	}
}
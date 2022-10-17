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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.repository.RepositoryEntry;
import org.olat.restapi.support.vo.CourseVO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Test the publish process via REST API
 * 
 * Initial date: 06.11.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CoursePublishTest extends OlatRestTestCase {	
	
	@Autowired
	private DB dbInstance;
	
	@Test
	public void testGetCourse() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		//deploy a test course
		URL courseUrl = CoursePublishTest.class.getResource("myCourseWS.zip");
		RepositoryEntry re = JunitTestHelper.deployCourse(null, "My WS course", courseUrl); // 4);	
		Assert.assertNotNull(re);
		ICourse course = CourseFactory.loadCourse(re.getOlatResource().getResourceableId());
		CourseNode rootNode = course.getRunStructure().getRootNode();
		Assert.assertEquals(2, rootNode.getChildCount());
		
		dbInstance.commitAndCloseSession();
		
		//get the course 
		URI uri = conn.getContextURI().path("repo").path("courses").path(course.getResourceableId().toString()).build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		CourseVO courseVo = conn.parse(response, CourseVO.class);
		Assert.assertNotNull(courseVo);
		
		//update the root node
		URI rootUri = getElementsUri(courseVo).path("structure").path(courseVo.getEditorRootNodeId()).build();
		HttpPost updateMethod = conn.createPost(rootUri, MediaType.APPLICATION_JSON);
		HttpEntity entity = MultipartEntityBuilder.create()
				.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
				.addTextBody("shortTitle", "Change it short")
				.addTextBody("longTitle", "Change it long")
				.build();
		updateMethod.setEntity(entity);
		HttpResponse updateRootResponse = conn.execute(updateMethod);
		int updateRootCode = updateRootResponse.getStatusLine().getStatusCode();
		assertTrue(updateRootCode == 200 || updateRootCode == 201);
		EntityUtils.consume(updateRootResponse.getEntity());
	
		//publish
		URI publishUri = getCoursesUri().path(courseVo.getKey().toString()).path("publish").build();
		HttpPost publishMethod = conn.createPost(publishUri, MediaType.APPLICATION_JSON);
		HttpResponse publishResponse = conn.execute(publishMethod);
		int publishCode = publishResponse.getStatusLine().getStatusCode();
		assertTrue(publishCode == 200 || publishCode == 201);
		EntityUtils.consume(publishResponse.getEntity());
		
		//reload the course
		ICourse reloadedCourse = CourseFactory.loadCourse(re.getOlatResource().getResourceableId());
		CourseNode reloadRootNode = reloadedCourse.getRunStructure().getRootNode();
		Assert.assertEquals(2, reloadRootNode.getChildCount());
	}
	
	private UriBuilder getCoursesUri() {
		return UriBuilder.fromUri(getContextURI()).path("repo").path("courses");
	}
	
	private UriBuilder getElementsUri(CourseVO course) {
		return getCoursesUri().path(course.getKey().toString()).path("elements");
	}
	
}
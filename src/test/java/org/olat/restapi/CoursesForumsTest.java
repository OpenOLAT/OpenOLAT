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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeConfiguration;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.modules.fo.restapi.ForumVO;
import org.olat.modules.fo.restapi.ForumVOes;
import org.olat.modules.fo.restapi.MessageVOes;
import org.olat.repository.RepositoryEntry;
import org.olat.restapi.repository.course.CoursesWebService;
import org.olat.test.OlatJerseyTestCase;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  6 févr. 2012 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class CoursesForumsTest  extends OlatJerseyTestCase {

	private static ICourse course1;
	private static CourseNode forumNode;
	private static Identity admin;
	
	private RestConnection conn;
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		conn = new RestConnection();
		
		admin = BaseSecurityManager.getInstance().findIdentityByName("administrator");
		course1 = CoursesWebService.createEmptyCourse(admin, "Course forum 1", "Course forum 1 long name", null);
		DBFactory.getInstance().intermediateCommit();
		
		//create a folder
		CourseNodeConfiguration newNodeConfig = CourseNodeFactory.getInstance().getCourseNodeConfiguration("fo");
		forumNode = newNodeConfig.getInstance();
		forumNode.setShortTitle("Forum");
		forumNode.setLearningObjectives("forum objectives");
		forumNode.setNoAccessExplanation("You don't have access");
		course1.getEditorTreeModel().addCourseNode(forumNode, course1.getRunStructure().getRootNode());
		
		CourseFactory.publishCourse(course1, RepositoryEntry.ACC_USERS, false, admin, Locale.ENGLISH);
		
		DBFactory.getInstance().intermediateCommit();
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
	public void testGetForumInfo() throws IOException, URISyntaxException {
		boolean loggedIN = conn.login("administrator", "openolat");
		assertTrue(loggedIN);

		URI uri = UriBuilder.fromUri(getNodeURI()).build();
		HttpGet get = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(get);
		assertEquals(200, response.getStatusLine().getStatusCode());
		ForumVO forum = conn.parse(response, ForumVO.class);
		assertNotNull(forum);
	}
	
	@Test
	public void testGetForumsInfo() throws IOException, URISyntaxException {
		boolean loggedIN = conn.login("administrator", "openolat");
		assertTrue(loggedIN);

		URI uri = UriBuilder.fromUri(getNodesURI()).build();
		HttpGet get = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(get);
		assertEquals(200, response.getStatusLine().getStatusCode());
		ForumVOes forums = conn.parse(response, ForumVOes.class);
		assertNotNull(forums);
		assertEquals(1, forums.getTotalCount());
		assertNotNull(forums.getForums());
		assertEquals(1, forums.getForums().length);
	}
	
	@Test
	public void testGetForum() throws IOException, URISyntaxException {
		boolean loggedIN = conn.login("administrator", "openolat");
		assertTrue(loggedIN);

		URI uri = UriBuilder.fromUri(getForumURI()).path("threads").build();
		HttpGet get = conn.createGet(uri, MediaType.APPLICATION_JSON + ";pagingspec=1.0", true);
		HttpResponse response = conn.execute(get);
		assertEquals(200, response.getStatusLine().getStatusCode());
		MessageVOes threads = conn.parse(response, MessageVOes.class);
		assertNotNull(threads);
	}
	
	private URI getNodeURI() {
		return UriBuilder.fromUri(getContextURI()).path("repo").path("courses").path(course1.getResourceableId().toString())
			.path("elements").path("forum").path(forumNode.getIdent()).build();
	}
	
	private URI getForumURI() {
		return UriBuilder.fromUri(getContextURI()).path("repo").path("courses").path(course1.getResourceableId().toString())
			.path("elements").path("forum").path(forumNode.getIdent()).path("forum").build();
	}
	
	private URI getNodesURI() {
		return UriBuilder.fromUri(getContextURI()).path("repo").path("courses").path(course1.getResourceableId().toString())
			.path("elements").path("forum").build();
	}
}

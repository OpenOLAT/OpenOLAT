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
import java.net.URL;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.Roles;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.BCCourseNode;
import org.olat.course.run.userview.CourseTreeVisitor;
import org.olat.repository.RepositoryEntry;
import org.olat.restapi.support.vo.FolderVOes;
import org.olat.test.JunitTestHelper;
import org.olat.test.JunitTestHelper.IdentityWithLogin;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserFoldersTest extends OlatRestTestCase {

	private static boolean setup;
	private static ICourse myCourse;
	private static RepositoryEntry myCourseRe;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private NotificationsManager notificationsManager;
	
	@Before
	public void setUp() throws Exception {
		if(setup) return;
		
		URL courseUrl = UserFoldersTest.class.getResource("myCourseWS.zip");
		myCourseRe = JunitTestHelper.deployCourse(null, "My course", courseUrl);// 4);
		Assert.assertNotNull(myCourseRe);
		myCourse = CourseFactory.loadCourse(myCourseRe);

		setup = true;
	}
	
	/**
	 * Test retrieve the folder which the user subscribe in a course.
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void myFolders() throws IOException, URISyntaxException {
		final IdentityWithLogin id = JunitTestHelper.createAndPersistRndUser("my");
		dbInstance.commitAndCloseSession();
		
		//load my forums
		RestConnection conn = new RestConnection();
		assertTrue(conn.login(id));
		
		//subscribed to nothing
		URI uri = UriBuilder.fromUri(getContextURI()).path("users").path(id.getKey().toString()).path("folders").build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		FolderVOes folders = conn.parse(response.getEntity(), FolderVOes.class);
		Assert.assertNotNull(folders);
		Assert.assertNotNull(folders.getFolders());
		Assert.assertEquals(0, folders.getFolders().length);
		
		//subscribe to the forum
		IdentityEnvironment ienv = new IdentityEnvironment(id.getIdentity(), Roles.userRoles());
		new CourseTreeVisitor(myCourse, ienv).visit(node -> {
			if(node instanceof BCCourseNode) {
				BCCourseNode folderNode = (BCCourseNode)node;	
				String relPath = BCCourseNode.getFoldernodePathRelToFolderBase(myCourse.getCourseEnvironment(), folderNode);
				String businessPath = "[RepositoryEntry:" + myCourseRe.getKey() + "][CourseNode:" + folderNode.getIdent() + "]";
				SubscriptionContext folderSubContext = new SubscriptionContext("CourseModule", myCourse.getResourceableId(), folderNode.getIdent());
				PublisherData folderPdata = new PublisherData("FolderModule", relPath, businessPath);
				notificationsManager.subscribe(id.getIdentity(), folderSubContext, folderPdata);
			}
		});
		dbInstance.commitAndCloseSession();
		
		//retrieve my folders
		HttpGet method2 = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response2 = conn.execute(method2);
		assertEquals(200, response2.getStatusLine().getStatusCode());
		FolderVOes folders2 = conn.parse(response2.getEntity(), FolderVOes.class);
		Assert.assertNotNull(folders2);
		Assert.assertNotNull(folders2.getFolders());
		Assert.assertEquals(1, folders2.getFolders().length);
	}
}

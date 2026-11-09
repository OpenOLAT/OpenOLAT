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

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.httpclient.ConnectionUtilities.NameValuePair;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.db.CourseDBEntry;
import org.olat.course.db.CourseDBManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.restapi.support.vo.KeyValuePair;
import org.olat.test.JunitTestHelper;
import org.olat.test.JunitTestHelper.IdentityWithLogin;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 03.03.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseDBTest extends OlatRestTestCase {
	
	private IdentityWithLogin auth;
	private ICourse course;
	
	private boolean initialized = false;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CourseDBManager courseDbManager;
	@Autowired
	private RepositoryManager repositoryManager;

	@Before
	public void setUp() throws Exception {
		// create course and persist as OLATResourceImpl
		if(!initialized) {
			auth = JunitTestHelper.createAndPersistRndUser("rest-course-cal-one");
			RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(auth.getIdentity());
			course = CourseFactory.loadCourse(courseEntry);
			initialized = true;
		}
	}
	
	@Test
	public void createEntry_putQuery() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection(auth);
		
		String category = createRndCategory();
		String key = "myKeyName";
		String value = "an interessant value";
		
		UriBuilder uri = getUriBuilder(course.getResourceableId(), category).path("values")
				.path(key).queryParam("value", value);
		HttpRequest put = conn.createPut(uri.build(), MediaType.APPLICATION_JSON);
	
		HttpResponse<InputStream> response = conn.execute(put);
		assertEquals(200, response.statusCode());
		RestConnection.consume(response);

		
		CourseDBEntry entry = courseDbManager.getValue(course, auth.getIdentity(), category, key);
		Assert.assertNotNull(entry);
		Assert.assertEquals(key, entry.getName());
		Assert.assertEquals(value, entry.getValue());
		Assert.assertEquals(category, entry.getCategory());
		conn.close();
	}
	
	@Test
	public void createEntry_putQuery_repoKey() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection(auth);
		
		OLATResourceable courseOres = OresHelper.createOLATResourceableInstance("CourseModule", course.getResourceableId());
		RepositoryEntry courseRe = repositoryManager.lookupRepositoryEntry(courseOres, true);
		
		String category = createRndCategory();
		String key = "myKeyName";
		String value = "an interessant value";
		
		UriBuilder uri = getUriBuilder(courseRe.getKey(), category).path("values")
				.path(key).queryParam("value", value);
		HttpRequest put = conn.createPut(uri.build(), MediaType.APPLICATION_JSON);
	
		HttpResponse<InputStream> response = conn.execute(put);
		assertEquals(200, response.statusCode());
		RestConnection.consume(response);

		
		CourseDBEntry entry = courseDbManager.getValue(course, auth.getIdentity(), category, key);
		Assert.assertNotNull(entry);
		Assert.assertEquals(key, entry.getName());
		Assert.assertEquals(value, entry.getValue());
		Assert.assertEquals(category, entry.getCategory());
		conn.close();
	}
	
	@Test
	public void createEntry_putJsonEntity() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection(auth);
		
		String category = createRndCategory();
		
		KeyValuePair keyValuePair = new KeyValuePair();
		keyValuePair.setKey("firstKey");
		keyValuePair.setValue("first value");
		
		UriBuilder uri = getUriBuilder(course.getResourceableId(), category).path("values");
		HttpRequest put = conn.createPut(uri.build(), keyValuePair, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(put);
		assertEquals(200, response.statusCode());
		RestConnection.consume(response);
		conn.close();

	}
	
	@Test
	public void createEntry_post() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection(auth);
		
		String category = createRndCategory();
		String key = "postit";
		String value = "create the value by POST";
		
		UriBuilder uri = getUriBuilder(course.getResourceableId(), category).path("values").path(key);
		List<NameValuePair> formParameters = List.of(new NameValuePair("val", value));
		HttpRequest put = conn.createPost(uri.build(), formParameters, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(put);
		assertEquals(200, response.statusCode());
		RestConnection.consume(response);

		
		CourseDBEntry entry = courseDbManager.getValue(course, auth.getIdentity(), category, key);
		Assert.assertNotNull(entry);
		Assert.assertEquals(key, entry.getName());
		Assert.assertEquals(value, entry.getValue());
		Assert.assertEquals(category, entry.getCategory());
		conn.close();
	}
	
	@Test
	public void createEntry_get() throws IOException, URISyntaxException, InterruptedException {
		String category = createRndCategory();
		String key = "getit";
		String value = "get a value";
		
		CourseDBEntry entry = courseDbManager.setValue(course, auth.getIdentity(), category, key, value);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(entry);
		
		RestConnection conn = new RestConnection(auth);

		UriBuilder uri = getUriBuilder(course.getResourceableId(), category).path("values").path(key);
		HttpRequest get = conn.createGet(uri.build(), MediaType.APPLICATION_JSON);
		
		HttpResponse<InputStream> response = conn.execute(get);
		assertEquals(200, response.statusCode());
		KeyValuePair savedEntry = conn.parse(response, KeyValuePair.class);

		
		Assert.assertNotNull(savedEntry);
		Assert.assertEquals(key, savedEntry.getKey());
		Assert.assertEquals(value, savedEntry.getValue());
		conn.close();
	}
	
	@Test
	public void getUsedCategories() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection(auth);
		
		String category = createRndCategory();
		
		KeyValuePair keyValuePair = new KeyValuePair();
		keyValuePair.setKey("catKey");
		keyValuePair.setValue("category value");
		
		UriBuilder uri = getUriBuilder(course.getResourceableId(), category).path("values");
		HttpRequest put = conn.createPut(uri.build(), keyValuePair, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(put);
		assertEquals(200, response.statusCode());
		RestConnection.consume(response);

		
		List<String> categories = courseDbManager.getUsedCategories(course);
		Assert.assertNotNull(categories);
		Assert.assertTrue(categories.contains(category));
		conn.close();
	}
	
	private String createRndCategory() {
		return UUID.randomUUID().toString().replace("-", "").substring(0, 32);
	}
	
	private UriBuilder getUriBuilder(Long courseId, String category) {
		return UriBuilder.fromUri(getContextURI()).path("repo").path("courses")
				.path(courseId.toString()).path("db").path(category);
	}
			

}

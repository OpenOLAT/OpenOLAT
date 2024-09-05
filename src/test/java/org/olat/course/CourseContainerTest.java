/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import jakarta.ws.rs.core.UriBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.webdav.WebDAVCommandsTest;
import org.olat.core.commons.services.webdav.WebDAVConnection;
import org.olat.core.commons.services.webdav.WebDAVModule;
import org.olat.core.commons.services.webdav.WebDAVTestCase;
import org.olat.core.id.Identity;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.test.JunitTestHelper;
import org.olat.test.JunitTestHelper.IdentityWithLogin;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26 avr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseContainerTest extends WebDAVTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private WebDAVModule webDavModule;
	@Autowired
	private RepositoryService repositoryService;
	
	private boolean enableLearnersParticipatingCoursesBackup;
	
	@Before
	public void setup() {
		enableLearnersParticipatingCoursesBackup = webDavModule.isEnableLearnersParticipatingCourses();
		webDavModule.setEnableLearnersParticipatingCourses(true);
	}
	
	@After
	public void after() {
		webDavModule.setEnableLearnersParticipatingCourses(enableLearnersParticipatingCoursesBackup);
	}
	
	@Test
	public void putCourseFolderAsOwner() 
	throws IOException, URISyntaxException {
		//create a user
		IdentityWithLogin author = createAndPersistRndUser("course-webdav-1-");
		deployCourse("Kurs", author.getIdentity(), null, null);

		WebDAVConnection conn = new WebDAVConnection();
		conn.setCredentials(author);

		//author check course folder
		URI courseUri = conn.getBaseURI().path("webdav").path("coursefolders").build();
		String publicXml = conn.propfind(courseUri, 2);
		Assert.assertTrue(publicXml.indexOf("<D:href>/webdav/coursefolders/_other/Kurs/</D:href>") > 0);

		//PUT in the course folder
		URI putUri = UriBuilder.fromUri(courseUri).path("_other").path("Kurs").path("test.txt").build();
		HttpPut put = conn.createPut(putUri);
		InputStream dataStream = WebDAVCommandsTest.class.getResourceAsStream("text.txt");
		InputStreamEntity entity = new InputStreamEntity(dataStream, -1);
		put.setEntity(entity);
		HttpResponse putResponse = conn.execute(put);
		Assert.assertEquals(201, putResponse.getStatusLine().getStatusCode());

		//GET
		HttpGet get = conn.createGet(putUri);
		HttpResponse getResponse = conn.execute(get);
		Assert.assertEquals(200, getResponse.getStatusLine().getStatusCode());
		String text = EntityUtils.toString(getResponse.getEntity());
		Assert.assertEquals("Small text", text);
	
		conn.close();
	}
	
	@Test
	public void putFolderElementAsOwner() 
	throws IOException, URISyntaxException {
		//create a user
		IdentityWithLogin author = createAndPersistRndUser("course-webdav-1-");
		deployCourse("Kurs", author.getIdentity(), null, null);

		WebDAVConnection conn = new WebDAVConnection();
		conn.setCredentials(author);

		//author check course folder
		URI courseUri = conn.getBaseURI().path("webdav").path("coursefolders").build();
		String publicXml = conn.propfind(courseUri, 2);
		Assert.assertTrue(publicXml.indexOf("<D:href>/webdav/coursefolders/_other/Kurs/</D:href>") > 0);

		//PUT in the folder element allowed
		URI textElementUri = UriBuilder.fromUri(courseUri).path("_other").path("Kurs").path("_courseelementdata").path("Folder").path("test.txt").build();
		HttpPut putElement = conn.createPut(textElementUri);
		InputStream dataStreamElement = WebDAVCommandsTest.class.getResourceAsStream("text.txt");
		putElement.setEntity(new InputStreamEntity(dataStreamElement, -1));
		HttpResponse putElementResponse = conn.execute(putElement);
		Assert.assertEquals(201, putElementResponse.getStatusLine().getStatusCode());
		
		//GET
		HttpGet get = conn.createGet(textElementUri);
		HttpResponse getResponse = conn.execute(get);
		Assert.assertEquals(200, getResponse.getStatusLine().getStatusCode());
		String text = EntityUtils.toString(getResponse.getEntity());
		Assert.assertEquals("Small text", text);
	
		conn.close();
	}
	
	@Test
	public void getNotCourseFolderAsParticipant() 
	throws IOException, URISyntaxException {
		//create a user
		IdentityWithLogin author = createAndPersistRndAuthor("course-webdav-2-");
		IdentityWithLogin participant = createAndPersistRndUser("course-webdav-3-");
		deployCourse("Teilnehmer", author.getIdentity(), null, participant.getIdentity());

		WebDAVConnection conn = new WebDAVConnection();
		conn.setCredentials(participant);

		//author check course folder
		URI courseUri = conn.getBaseURI().path("webdav").path("coursefolders").build();
		String publicXml = conn.propfind(courseUri, 2);
		Assert.assertTrue(publicXml.indexOf("<D:href>/webdav/coursefolders/_other/Teilnehmer/</D:href>") > 0);

		//Not GET in the course folder
		URI getUri = UriBuilder.fromUri(courseUri).path("_other").path("Teilnehmer").path("IMG_1492.png").build();
		HttpGet get = conn.createGet(getUri);
		HttpResponse getResponse = conn.execute(get);
		Assert.assertEquals(404, getResponse.getStatusLine().getStatusCode());
		EntityUtils.consumeQuietly(getResponse.getEntity());

		conn.close();
	}
	
	@Test
	public void putNotCourseFolderAsParticipant() 
	throws IOException, URISyntaxException {
		//create a user
		IdentityWithLogin author = createAndPersistRndAuthor("course-webdav-2-");
		IdentityWithLogin participant = createAndPersistRndUser("course-webdav-3-");
		deployCourse("Teilnehmer", author.getIdentity(), null, participant.getIdentity());

		WebDAVConnection conn = new WebDAVConnection();
		conn.setCredentials(participant);

		//author check course folder
		URI courseUri = conn.getBaseURI().path("webdav").path("coursefolders").build();
		String publicXml = conn.propfind(courseUri, 2);
		Assert.assertTrue(publicXml.indexOf("<D:href>/webdav/coursefolders/_other/Teilnehmer/</D:href>") > 0);
		
		//Not PUT in the course folder allowed
		URI putUri = UriBuilder.fromUri(courseUri).path("_other").path("Teilnehmer").path("test.txt").build();
		HttpPut put = conn.createPut(putUri);
		InputStream dataStream = WebDAVCommandsTest.class.getResourceAsStream("text.txt");
		InputStreamEntity entity = new InputStreamEntity(dataStream, -1);
		put.setEntity(entity);
		HttpResponse putResponse = conn.execute(put);
		Assert.assertEquals(403, putResponse.getStatusLine().getStatusCode());

		conn.close();
	}
	
	@Test
	public void getFolderElementAsParticipant() 
	throws IOException, URISyntaxException {
		//create a user
		IdentityWithLogin author = createAndPersistRndAuthor("course-webdav-2-");
		IdentityWithLogin participant = createAndPersistRndUser("course-webdav-3-");
		deployCourse("Teilnehmer", author.getIdentity(), null, participant.getIdentity());

		WebDAVConnection conn = new WebDAVConnection();
		conn.setCredentials(participant);

		//author check course folder
		URI courseUri = conn.getBaseURI().path("webdav").path("coursefolders").build();
		String publicXml = conn.propfind(courseUri, 2);
		Assert.assertTrue(publicXml.indexOf("<D:href>/webdav/coursefolders/_other/Teilnehmer/</D:href>") > 0);

		// Successful GET in a folder element
		URI getElementUri = UriBuilder.fromUri(courseUri).path("_other").path("Teilnehmer").path("_courseelementdata").path("Folder").path("house.jpg").build();
		HttpGet getElement = conn.createGet(getElementUri);
		HttpResponse getElementResponse = conn.execute(getElement);
		Assert.assertEquals(200, getElementResponse.getStatusLine().getStatusCode());
		byte[] images = EntityUtils.toByteArray(getElementResponse.getEntity());
		Assert.assertTrue(images.length > 5000);

		conn.close();
	}
	
	@Test
	public void getNotCoachFolderAsParticipant() 
	throws IOException, URISyntaxException {
		//create a n author which is coach too
		IdentityWithLogin author = createAndPersistRndAuthor("course-webdav-4-");
		IdentityWithLogin participant = createAndPersistRndUser("course-webdav-5-");
		deployCourse("Coaches", author.getIdentity(), null, participant.getIdentity());

		WebDAVConnection conn = new WebDAVConnection();
		conn.setCredentials(participant);

		//author check course folder
		URI courseUri = conn.getBaseURI().path("webdav").path("coursefolders").build();
		String publicXml = conn.propfind(courseUri, 2);
		Assert.assertTrue(publicXml.indexOf("<D:href>/webdav/coursefolders/_other/Coaches/</D:href>") > 0);

		// Load an image from the coach folder
		URI getUri = UriBuilder.fromUri(courseUri).path("_other").path("Coaches").path("_coachdocuments").path("IMG_1482.jpg").build();
		HttpGet get = conn.createGet(getUri);
		HttpResponse getResponse = conn.execute(get);
		Assert.assertEquals(404, getResponse.getStatusLine().getStatusCode());
		EntityUtils.consumeQuietly(getResponse.getEntity());

		conn.close();
	}
	
	@Test
	public void getCoachFolderAsCoach() 
	throws IOException, URISyntaxException {
		//create a n author which is coach too
		IdentityWithLogin coach = createAndPersistRndUser("course-webdav-3-");
		deployCourse("Coaches", coach.getIdentity(), coach.getIdentity(), null);

		WebDAVConnection conn = new WebDAVConnection();
		conn.setCredentials(coach);

		//author check course folder
		URI courseUri = conn.getBaseURI().path("webdav").path("coursefolders").build();
		String publicXml = conn.propfind(courseUri, 2);
		Assert.assertTrue(publicXml.indexOf("<D:href>/webdav/coursefolders/_other/Coaches/</D:href>") > 0);

		// Load an image from the coach folder
		URI getUri = UriBuilder.fromUri(courseUri).path("_other").path("Coaches").path("_coachdocuments").path("IMG_1482.jpg").build();
		HttpGet get = conn.createGet(getUri);
		HttpResponse getResponse = conn.execute(get);
		Assert.assertEquals(200, getResponse.getStatusLine().getStatusCode());
		byte[] images = EntityUtils.toByteArray(getResponse.getEntity());
		Assert.assertTrue(images.length > 5000);
	
		conn.close();
	}
	
	@Test
	public void putNotCourseFolderAsCoach() 
	throws IOException, URISyntaxException {
		//create a user
		IdentityWithLogin author = createAndPersistRndAuthor("course-webdav-2-");
		IdentityWithLogin participant = createAndPersistRndUser("course-webdav-3-");
		deployCourse("Teilnehmer", author.getIdentity(), null, participant.getIdentity());

		WebDAVConnection conn = new WebDAVConnection();
		conn.setCredentials(participant);

		//author check course folder
		URI courseUri = conn.getBaseURI().path("webdav").path("coursefolders").build();
		String publicXml = conn.propfind(courseUri, 2);
		Assert.assertTrue(publicXml.indexOf("<D:href>/webdav/coursefolders/_other/Teilnehmer/</D:href>") > 0);
		
		//Not PUT in the course folder allowed
		URI putUri = UriBuilder.fromUri(courseUri).path("_other").path("Teilnehmer").path("test.txt").build();
		HttpPut put = conn.createPut(putUri);
		InputStream dataStream = WebDAVCommandsTest.class.getResourceAsStream("text.txt");
		InputStreamEntity entity = new InputStreamEntity(dataStream, -1);
		put.setEntity(entity);
		HttpResponse putResponse = conn.execute(put);
		Assert.assertEquals(403, putResponse.getStatusLine().getStatusCode());

		conn.close();
	}
	
	@Test
	public void putNotFolderElementAsCoach() 
	throws IOException, URISyntaxException {
		//create a user
		IdentityWithLogin author = createAndPersistRndAuthor("course-webdav-2-");
		IdentityWithLogin participant = createAndPersistRndUser("course-webdav-3-");
		deployCourse("Coaching", author.getIdentity(), null, participant.getIdentity());

		WebDAVConnection conn = new WebDAVConnection();
		conn.setCredentials(participant);

		//author check course folder
		URI courseUri = conn.getBaseURI().path("webdav").path("coursefolders").build();
		String publicXml = conn.propfind(courseUri, 2);
		Assert.assertTrue(publicXml.indexOf("<D:href>/webdav/coursefolders/_other/Coaching/</D:href>") > 0);
		
		//Not PUT in the course element allowed
		URI putUri = UriBuilder.fromUri(courseUri).path("_other").path("Coaching").path("_courseelementdata").path("Folder").path("test.txt").build();
		HttpPut put = conn.createPut(putUri);
		InputStream dataStream = WebDAVCommandsTest.class.getResourceAsStream("text.txt");
		InputStreamEntity entity = new InputStreamEntity(dataStream, -1);
		put.setEntity(entity);
		HttpResponse putResponse = conn.execute(put);
		Assert.assertEquals(403, putResponse.getStatusLine().getStatusCode());

		conn.close();
	}
	
	/**
	 * Security check 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void getNotAsRandom() 
	throws IOException, URISyntaxException {
		//create a user
		IdentityWithLogin author = createAndPersistRndAuthor("course-webdav-2-");
		IdentityWithLogin user = createAndPersistRndUser("course-webdav-3-");
		deployCourse("Coaching", author.getIdentity(), null, null);

		WebDAVConnection conn = new WebDAVConnection();
		conn.setCredentials(user);

		//author check course folder
		URI courseUri = conn.getBaseURI().path("webdav").path("coursefolders").build();
		String publicXml = conn.propfind(courseUri, 2);
		Assert.assertTrue(publicXml.indexOf("<D:href>/webdav/coursefolders/</D:href>") > 0);
		Assert.assertFalse(publicXml.indexOf("<D:href>/webdav/coursefolders/_other/Coaching/</D:href>") > 0);

		conn.close();
	}
	
	private RepositoryEntry deployCourse(String displayName, Identity author, Identity coach, Identity participant)
	throws URISyntaxException {
		URL courseWithForumsUrl = CourseContainerTest.class.getResource("CourseWebDAV.zip");
		Assert.assertNotNull(courseWithForumsUrl);
		RepositoryEntry re = JunitTestHelper.deployCourse(author, displayName, courseWithForumsUrl);
		if(coach != null) {
			repositoryService.addRole(coach, re, GroupRoles.coach.name());
		}
		if(participant != null) {
			repositoryService.addRole(participant, re, GroupRoles.participant.name());
		}
		dbInstance.commitAndCloseSession();
		return re;
	}
	
	private IdentityWithLogin createAndPersistRndUser(String prefixLogin) {
		return JunitTestHelper.createAndPersistWebDAVAuthentications(
				JunitTestHelper.createAndPersistRndUser(prefixLogin));
	}
	
	private IdentityWithLogin createAndPersistRndAuthor(String prefixLogin) {
		return JunitTestHelper.createAndPersistWebDAVAuthentications(
				JunitTestHelper.createAndPersistRndAuthor(prefixLogin));
	}
	
	private IdentityWithLogin createAndPersistRndAdmin(String prefixLogin) {
		return JunitTestHelper.createAndPersistWebDAVAuthentications(
				JunitTestHelper.createAndPersistRndAdmin(prefixLogin));
	}	
}

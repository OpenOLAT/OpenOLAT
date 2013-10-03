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
package org.olat.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.olat.restapi.RestConnection;
import org.olat.restapi.support.vo.CourseVO;
import org.olat.restapi.support.vo.GroupConfigurationVO;
import org.olat.restapi.support.vo.GroupVO;
import org.olat.restapi.support.vo.RepositoryEntryVO;
import org.olat.user.restapi.RolesVO;
import org.olat.user.restapi.UserVO;

/**
 * 
 * @author jkraehemann, joel.kraehemann@frentix.com, frentix.com
 */
public class FunctionalVOUtil {
	
	public final static String WAIT_LIMIT = "15000";
	
	public final static String ALL_ELEMENTS_COURSE_DISPLAYNAME = "All Elements Course Without External Content";
	public final static String ALL_ELEMENTS_COURSE_FILENAME = "All_Elements_Course_Without_External_Content.zip";

	public enum SysGroups{
		USERMANAGERS,
		GROUPMANAGERS,
		AUTHORS,
		ADMIN,
		USERS,
		ANONYMOUS,
		INSTITUTIONAL_RESOURCE_MANAGER,
	};
	
	private String username;
	private String password;
	
	private String allElementsCourseDisplayname;
	private String allElementsCourseFilename;
	private String waitLimit;
	
	public FunctionalVOUtil(String username, String password){
		setUsername(username);
		setPassword(password);
		
		setAllElementsCourseDisplayname(ALL_ELEMENTS_COURSE_DISPLAYNAME);
		setAllElementsCourseFilename(ALL_ELEMENTS_COURSE_FILENAME);

		waitLimit = WAIT_LIMIT;
	}
	
	public RepositoryEntryVO getRepositoryEntryByKey(URL deploymentUrl, long key) throws IOException, URISyntaxException{
		RestConnection restConnection = new RestConnection(deploymentUrl);

		restConnection.login(getUsername(), getPassword());
		
		URI request = UriBuilder.fromUri(deploymentUrl.toURI()).path("restapi").path("repo/entries").path(Long.toString(key)).build();
		HttpGet method = restConnection.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = restConnection.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		RepositoryEntryVO vo = restConnection.parse(response, RepositoryEntryVO.class);

		restConnection.shutdown();
		
		return(vo);
	}
	
	/**
	 * Creates the selenium test users with random passwords and
	 * writes it to credentials.properties.
	 * 
	 * @param deploymentUrl
	 * @param count
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public List<UserVO> createTestUsers(URL deploymentUrl, int count) throws IOException, URISyntaxException{
		RestConnection restConnection = new RestConnection(deploymentUrl);
		assertTrue(restConnection.login(getUsername(), getPassword()));
		
		List<UserVO> user = new ArrayList<UserVO>(count);
		for(int i = 0; i < count; i++){
			UserVO vo = new UserVO();
			String username = ("selenium_" + i + "_" + UUID.randomUUID().toString()).substring(0, 24);
			vo.setLogin(username);
			String password = ("passwd_" + i + "_" + UUID.randomUUID().toString()).substring(0, 24);
			vo.setPassword(password);
			vo.setFirstName("John_" + i);
			vo.setLastName("Smith");
			vo.setEmail(username + "@frentix.com");
			vo.putProperty("telOffice", "39847592");
			vo.putProperty("telPrivate", "39847592");
			vo.putProperty("telMobile", "39847592");
			vo.putProperty("gender", "Female");//male or female
			vo.putProperty("birthDay", "12/12/2009");

			URI request = UriBuilder.fromUri(deploymentUrl.toURI()).path("restapi").path("users").build();
			HttpPut method = restConnection.createPut(request, MediaType.APPLICATION_JSON, true);
			restConnection.addJsonEntity(method, vo);
			method.addHeader("Accept-Language", "en");

			HttpResponse response = restConnection.execute(method);
			int responseCode = response.getStatusLine().getStatusCode();
			assertTrue(responseCode == 200 || responseCode == 201);
			InputStream body = response.getEntity().getContent();
			UserVO current = restConnection.parse(body, UserVO.class);
			Assert.assertNotNull(current);
			
			current.setPassword(vo.getPassword());
			user.add(current);
		}

		restConnection.shutdown();
		
		return(user);
	}
	
	/**
	 * Creates test authors.
	 * 
	 * @param deploymentUrl
	 * @param count
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public List<UserVO> createTestAuthors(URL deploymentUrl, int count) throws IOException, URISyntaxException{
		/* create ordinary users */
		List<UserVO> user = createTestUsers(deploymentUrl, count);

		/* make a tutor */
		RestConnection restConnection = new RestConnection(deploymentUrl);

		Assert.assertTrue(restConnection.login(getUsername(), getPassword()));
		
		for(UserVO currentUser: user){
			/* retrieve roles */
			URI request = UriBuilder.fromUri(deploymentUrl.toURI()).path("restapi").path("users/" + currentUser.getKey() + "/roles").build();
			HttpGet method = restConnection.createGet(request, MediaType.APPLICATION_JSON, true);
			HttpResponse response = restConnection.execute(method);
			assertEquals(200, response.getStatusLine().getStatusCode());
			InputStream body = response.getEntity().getContent();
			RolesVO roles = restConnection.parse(body, RolesVO.class);
			
			/* send appropriate role */
			roles.setAuthor(true);
			roles.setUserManager(true);
			
			request = UriBuilder.fromUri(deploymentUrl.toURI()).path("restapi").path("users/" + currentUser.getKey() + "/roles").build();
			HttpPost postMethod = restConnection.createPost(request, MediaType.APPLICATION_JSON);
			restConnection.addJsonEntity(postMethod, roles);
			response = restConnection.execute(postMethod);
			assertEquals(200, response.getStatusLine().getStatusCode());
			
			EntityUtils.consume(response.getEntity());
		}
		
		restConnection.shutdown();
		
		return(user);
	}
	
	/**
	 * Creates course groups.
	 * 
	 * @param deploymentUrl
	 * @param count
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public List<GroupVO> createTestCourseGroups(URL deploymentUrl, int count) throws IOException, URISyntaxException{
		RestConnection restConnection = new RestConnection(deploymentUrl);

		restConnection.login(getUsername(), getPassword());
		
		List<GroupVO> group = new ArrayList<GroupVO>();
		
		for(int i = 0; i < count; i++){
			GroupVO vo = new GroupVO();
			String groupname = ("selgrp_" + i + "_" + UUID.randomUUID().toString()).substring(0, 24);
			vo.setName(groupname);
			vo.setDescription("group#" + i + " created for selenium test case.");
			vo.setType("BuddyGroup");
			vo.setMinParticipants(new Integer(-1));
			vo.setMaxParticipants(new Integer(-1));
			
			URI request = UriBuilder.fromUri(deploymentUrl.toURI()).path("restapi").path("groups").build();
			HttpPut method = restConnection.createPut(request, MediaType.APPLICATION_JSON, true);
			restConnection.addJsonEntity(method, vo);

			HttpResponse response = restConnection.execute(method);
			assertTrue(response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201);
			
			vo = restConnection.parse(response, GroupVO.class);
			group.add(vo);
		}

		restConnection.shutdown();
		
		return(group);
	}
	
	/**
	 * Adds owner to group.
	 * 
	 * @param deploymentUrl
	 * @param group
	 * @param owner
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public void addOwnerToGroup(URL deploymentUrl, GroupVO group, UserVO owner) throws IOException, URISyntaxException{
		//add an owner
		RestConnection restConnection = new RestConnection(deploymentUrl);
		assertTrue(restConnection.login(getUsername(), getPassword()));

		URI request = UriBuilder.fromUri(deploymentUrl.toURI())
				.path("restapi")
				.path("groups").path(group.getKey().toString())
				.path("owners").path(owner.getKey().toString())
				.build();

		HttpPut method = restConnection.createPut(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = restConnection.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());

		restConnection.shutdown();
	}
	
	/**
	 * Adds participant to group.
	 * 
	 * @param deploymentUrl
	 * @param group
	 * @param owner
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public void addParticipantToGroup(URL deploymentUrl, GroupVO group, UserVO participant) throws IOException, URISyntaxException{
		//add an owner
		RestConnection restConnection = new RestConnection(deploymentUrl);
		assertTrue(restConnection.login(getUsername(), getPassword()));

		URI request = UriBuilder.fromUri(deploymentUrl.toURI())
				.path("restapi")
				.path("groups").path(group.getKey().toString())
				.path("participants").path(participant.getKey().toString())
				.build();

		HttpPut method = restConnection.createPut(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = restConnection.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());

		restConnection.shutdown();
	}
	
	public void setGroupConfiguration(URL deploymentUrl, GroupVO group,
			String[] tools,
			boolean ownersPublic, boolean ownersVisible,
			boolean participatnsPublic, boolean participantsVisible,
			boolean waitingListPublic, boolean waitingListVisible)
					throws IOException, URISyntaxException{
		RestConnection restConnection = new RestConnection(deploymentUrl);
		assertTrue(restConnection.login(getUsername(), getPassword()));

		/* retrieve existing configuration */
		URI request = UriBuilder.fromUri(deploymentUrl.toURI())
				.path("restapi")
				.path("groups").path(group.getKey().toString())
				.path("configuration")
				.build();

		GroupConfigurationVO config = new GroupConfigurationVO();
		
		config.setTools(tools);
		config.setOwnersPublic(ownersPublic);
		config.setOwnersVisible(ownersVisible);
		config.setParticipantsPublic(participatnsPublic);
		config.setParticipantsVisible(participantsVisible);
		config.setWaitingListPublic(waitingListPublic);
		config.setWaitingListVisible(waitingListVisible);
		
		/* post modified configuration */
		HttpPost postMethod = restConnection.createPost(request, MediaType.APPLICATION_JSON);
		restConnection.addJsonEntity(postMethod, config);
		
		HttpResponse response = restConnection.execute(postMethod);
		assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());

		restConnection.shutdown();
	}
	
	/**
	 * Imports the specified course via REST.
	 * 
	 * @param deploymentUrl
	 * @param path
	 * @param filename
	 * @param resourcename
	 * @param displayname
	 * @return CourseVO
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public CourseVO importCourse(URL deploymentUrl, String path, String filename, String resourcename, String displayname) throws URISyntaxException, IOException{
		URL cpUrl = FunctionalVOUtil.class.getResource(path);
		assertNotNull(cpUrl);
		File cp = new File(cpUrl.toURI());

		RestConnection conn = new RestConnection(deploymentUrl);
		assertTrue(conn.login(getUsername(), getPassword()));
		
		URI request = UriBuilder.fromUri(deploymentUrl.toURI()).path("restapi").path("repo/courses").build();
		HttpPost method = conn.createPost(request, MediaType.APPLICATION_JSON);
		String softKey = UUID.randomUUID().toString().replace("-", "").substring(0, 30);
		HttpEntity entity = MultipartEntityBuilder.create()
				.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
				.addBinaryBody("file", cp, ContentType.APPLICATION_OCTET_STREAM, cp.getName())
				.addTextBody("filename", filename)
				.addTextBody("resourcename", resourcename)
				.addTextBody("displayname", displayname)
				.addTextBody("access", "3")
				.addTextBody("softkey", softKey)
				.build();
		method.setEntity(entity);
		
		HttpResponse response = conn.execute(method);
		assertTrue(response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201);
		
		CourseVO vo = conn.parse(response, CourseVO.class);
		assertNotNull(vo);
		assertNotNull(vo.getRepoEntryKey());
		assertNotNull(vo.getKey());
		
		return(vo);
	}
	
	/**
	 * 
	 * @param deploymentUrl
	 * @param courseId
	 * @param group
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public List<GroupVO> addGroupToCourse(URL deploymentUrl, long courseId, List<GroupVO> group) throws IOException, URISyntaxException{
		RestConnection restConnection = new RestConnection(deploymentUrl);
		restConnection.login(getUsername(), getPassword());

		List<GroupVO> newGroup = new ArrayList<GroupVO>();

		for(GroupVO currentGroup: group){
			URI request = UriBuilder.fromUri(deploymentUrl.toURI()).path("restapi").path("repo").path("courses").path(Long.toString(courseId)).path("groups").build();
			HttpPut method = restConnection.createPut(request, MediaType.APPLICATION_JSON, true);
			restConnection.addJsonEntity(method, currentGroup);

			HttpResponse response = restConnection.execute(method);
			assertEquals(200, response.getStatusLine().getStatusCode());
			
			GroupVO vo = restConnection.parse(response, GroupVO.class);
			newGroup.add(vo);
		}

		restConnection.shutdown();
		
		return(newGroup);
	}
	
	/**
	 * 
	 * @param deploymentUrl
	 * @return
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public CourseVO importEmptyCourse(URL deploymentUrl) throws URISyntaxException, IOException{
		return(importCourse(deploymentUrl, "/org/olat/course/Empty_Course.zip", "Empty_Course.zip", "Empty Course", "Empty Course"));
	}
	
	/**
	 * Imports the "All Elements Course" via REST.
	 * 
	 * @param deploymentUrl
	 * @return
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public CourseVO importAllElementsCourse(URL deploymentUrl) throws URISyntaxException, IOException{
		return(importCourse(deploymentUrl, "/org/olat/course/All_Elements_Course_Without_External_Content.zip", "All_Elements_Course_Without_External_Content.zip", "All Elements Course Without External Content", "All Elements Course Without External Content"));
	}

	/**
	 * Imports the "All Elements Course" via REST.
	 * 
	 * @param deploymentUrl
	 * @return
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public CourseVO importAllElementsCourse(URL deploymentUrl, String resourceName, String displayName) throws URISyntaxException, IOException{
		return(importCourse(deploymentUrl, "/org/olat/course/All_Elements_Course_Without_External_Content.zip", "All_Elements_Course_Without_External_Content.zip", resourceName, displayName));
	}
	
	/**
	 * Imports the "Course including Forum" via REST.
	 * 
	 * @param deploymentUrl
	 * @return
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public CourseVO importCourseIncludingForum(URL deploymentUrl) throws URISyntaxException, IOException{
		return(importCourse(deploymentUrl, "/org/olat/portfolio/Course_including_Forum.zip", "Course_including_Forum.zip", "Course including Forum", "Course including Forum"));
	}
	
	/**
	 * Imports the "Course including Blog" via REST.
	 * 
	 * @param deploymentUrl
	 * @return
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public CourseVO importCourseIncludingBlog(URL deploymentUrl) throws URISyntaxException, IOException{
		return(importCourse(deploymentUrl, "/org/olat/portfolio/Course_including_Blog.zip", "Course_including_Blog.zip", "Course including Blog", "Course including Blog"));
	}
	
	/**
	 * imports a wiki via REST.
	 * 
	 * @param deploymentUrl
	 * @return
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public RepositoryEntryVO importWiki(URL deploymentUrl) throws URISyntaxException, IOException{
		URL wikiUrl = FunctionalVOUtil.class.getResource("/org/olat/portfolio/wiki.zip");
		Assert.assertNotNull(wikiUrl);
		
		File wiki = new File(wikiUrl.toURI());
		
		RestConnection restConnection = new RestConnection(deploymentUrl);

		assertTrue(restConnection.login(getUsername(), getPassword()));
		
		URI request = UriBuilder.fromUri(deploymentUrl.toURI()).path("restapi").path("repo/entries").build();
		HttpPut method = restConnection.createPut(request, MediaType.APPLICATION_JSON, true);
		HttpEntity entity = MultipartEntityBuilder.create()
				.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
				.addBinaryBody("file", wiki, ContentType.APPLICATION_OCTET_STREAM, wiki.getName())
				.addTextBody("filename", "wiki.zip")
				.addTextBody("resourcename", "Wiki")
				.addTextBody("displayname", "Wiki")
				.addTextBody("access", "3")
				.build();
		method.setEntity(entity);
		
		HttpResponse response = restConnection.execute(method);
		assertTrue(response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201);
		
		InputStream body = response.getEntity().getContent();
		
		RepositoryEntryVO vo = restConnection.parse(body, RepositoryEntryVO.class);
		assertNotNull(vo);
		
		return(vo);
	}
	
	/**
	 * Imports a blog via REST.
	 * 
	 * @param deploymentUrl
	 * @param login
	 * @param password
	 * @return
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public RepositoryEntryVO importBlog(URL deploymentUrl, String path, String filename, String resourcename, String displayname) throws URISyntaxException, IOException{
		URL blogUrl = FunctionalVOUtil.class.getResource(path);
		Assert.assertNotNull(blogUrl);
		
		File blog = new File(blogUrl.toURI());
		
		RestConnection restConnection = new RestConnection(deploymentUrl);

		assertTrue(restConnection.login(getUsername(), getPassword()));
		
		URI request = UriBuilder.fromUri(deploymentUrl.toURI()).path("restapi").path("repo/entries").build();
		HttpPut method = restConnection.createPut(request, MediaType.APPLICATION_JSON, true);
		HttpEntity entity = MultipartEntityBuilder.create()
				.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
				.addBinaryBody("file", blog, ContentType.APPLICATION_OCTET_STREAM, blog.getName())
				.addTextBody("filename", filename)
				.addTextBody("resourcename", resourcename)
				.addTextBody("displayname", displayname)
				.addTextBody("access", "3")
				.build();
		method.setEntity(entity);
		
		HttpResponse response = restConnection.execute(method);
		assertTrue(response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201);
		
		InputStream body = response.getEntity().getContent();
		
		RepositoryEntryVO vo = restConnection.parse(body, RepositoryEntryVO.class);
		assertNotNull(vo);
		
		return(vo);
	}
	
	/**
	 * Adds owner as owner to repoEntry.
	 * 
	 * @param deploymentUrl
	 * @param repoEntry
	 * @param owner
	 * @throws URISyntaxException
	 * @throws IOException 
	 */
	public void addOwnerToRepositoryEntry(URL deploymentUrl, RepositoryEntryVO repoEntry, UserVO owner) throws IOException, URISyntaxException{
		//add an owner
		RestConnection restConnection = new RestConnection(deploymentUrl);
		assertTrue(restConnection.login(getUsername(), getPassword()));

		URI request = UriBuilder.fromUri(deploymentUrl.toURI())
				.path("restapi")
				.path("repo/entries").path(repoEntry.getKey().toString())
				.path("owners").path(owner.getKey().toString())
				.build();

		HttpPut method = restConnection.createPut(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = restConnection.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());

		restConnection.shutdown();
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getAllElementsCourseDisplayname() {
		return allElementsCourseDisplayname;
	}

	public void setAllElementsCourseDisplayname(String allElementsCourseDisplayname) {
		this.allElementsCourseDisplayname = allElementsCourseDisplayname;
	}

	public String getAllElementsCourseFilename() {
		return allElementsCourseFilename;
	}

	public void setAllElementsCourseFilename(String allElementsCourseFilename) {
		this.allElementsCourseFilename = allElementsCourseFilename;
	}

	public String getWaitLimit() {
		return waitLimit;
	}

	public void setWaitLimit(String waitLimit) {
		this.waitLimit = waitLimit;
	}
}

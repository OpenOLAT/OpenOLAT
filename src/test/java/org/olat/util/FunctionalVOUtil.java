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

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.junit.Assert;
import org.olat.restapi.CoursesTest;
import org.olat.restapi.RestConnection;
import org.olat.restapi.support.vo.CourseVO;
import org.olat.restapi.support.vo.RepositoryEntryVO;
import org.olat.user.restapi.UserVO;

public class FunctionalVOUtil {
	public final static String ALL_ELEMENTS_COURSE_DISPLAYNAME = "All Elements Course";
	public final static String ALL_ELEMENTS_COURSE_FILENAME = "All_Elements_Course.zip";

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
	
	public FunctionalVOUtil(String username, String password){
		setUsername(username);
		setPassword(password);
		
		setAllElementsCourseDisplayname(ALL_ELEMENTS_COURSE_DISPLAYNAME);
		setAllElementsCourseFilename(ALL_ELEMENTS_COURSE_FILENAME);
	}
	
	/**
	 * @param deploymentUrl
	 * @param count
	 * @throws IOException
	 * @throws URISyntaxException
	 * 
	 * Creates the selenium test users with random passwords and
	 * writes it to credentials.properties.
	 */
	public List<UserVO> createTestUsers(URL deploymentUrl, int count) throws IOException, URISyntaxException{
		RestConnection restConnection = new RestConnection(deploymentUrl);

		restConnection.login(getUsername(), getPassword());
		
		List<UserVO> user = new ArrayList<UserVO>();
		
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
			assertTrue(response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201);
			InputStream body = response.getEntity().getContent();
			
			UserVO current = restConnection.parse(body, UserVO.class);
			Assert.assertNotNull(current);
			
			user.add(vo);
		}

		restConnection.shutdown();
		
		return(user);
	}
	
	public void addUserToSysGroup(UserVO user, SysGroups group){
		//TODO:JK: implement me
	}
	
	/**
	 * @param deploymentUrl
	 * @return
	 * @throws URISyntaxException
	 * @throws IOException
	 * 
	 * Imports the "All Elements Course" via REST.
	 */
	public CourseVO importAllElementsCourse(URL deploymentUrl) throws URISyntaxException, IOException{
		URL cpUrl = FunctionalVOUtil.class.getResource("/org/olat/course/All_Elements_Course.zip");
		assertNotNull(cpUrl);
		File cp = new File(cpUrl.toURI());

		RestConnection conn = new RestConnection(deploymentUrl);
		assertTrue(conn.login(getUsername(), getPassword()));
		
		URI request = UriBuilder.fromUri(deploymentUrl.toURI()).path("restapi").path("repo/courses").build();
		HttpPost method = conn.createPost(request, MediaType.APPLICATION_JSON, true);
		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		entity.addPart("file", new FileBody(cp));
		entity.addPart("filename", new StringBody("All_Elements_Course.zip"));
		entity.addPart("resourcename", new StringBody("All Elements Course"));
		entity.addPart("displayname", new StringBody("All Elements Course"));
		entity.addPart("access", new StringBody("3"));
		String softKey = UUID.randomUUID().toString().replace("-", "").substring(0, 30);
		entity.addPart("softkey", new StringBody(softKey));
		method.setEntity(entity);
		
		HttpResponse response = conn.execute(method);
		assertTrue(response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201);
		
		CourseVO vo = conn.parse(response, CourseVO.class);
		assertNotNull(vo);
		assertNotNull(vo.getRepoEntryKey());
		assertNotNull(vo.getKey());
		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			//TODO:JK Auto-generated catch block
			e.printStackTrace();
		}

		return(vo);
	}

	/**
	 * @param deploymentUrl
	 * @return
	 * @throws URISyntaxException
	 * @throws IOException
	 * 
	 * imports a wiki via REST.
	 */
	public RepositoryEntryVO importWiki(URL deploymentUrl) throws URISyntaxException, IOException{
		URL wikiUrl = FunctionalVOUtil.class.getResource("/org/olat/portfolio/wiki.zip");
		Assert.assertNotNull(wikiUrl);
		
		File wiki = new File(wikiUrl.toURI());
		
		RestConnection restConnection = new RestConnection(deploymentUrl);

		assertTrue(restConnection.login(getUsername(), getPassword()));
		
		URI request = UriBuilder.fromUri(deploymentUrl.toURI()).path("restapi").path("repo/entries").build();
		HttpPut method = restConnection.createPut(request, MediaType.APPLICATION_JSON, true);
		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		entity.addPart("file", new FileBody(wiki));
		entity.addPart("filename", new StringBody("wiki.zip"));
		entity.addPart("resourcename", new StringBody("Wiki"));
		entity.addPart("displayname", new StringBody("Wiki"));
		entity.addPart("access", new StringBody("3"));
		method.setEntity(entity);
		
		HttpResponse response = restConnection.execute(method);
		assertTrue(response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201);
		
		InputStream body = response.getEntity().getContent();
		
		RepositoryEntryVO vo = restConnection.parse(body, RepositoryEntryVO.class);
		assertNotNull(vo);
		
		return(vo);
	}
	
	/**
	 * @param deploymentUrl
	 * @param login
	 * @param password
	 * @return
	 * @throws URISyntaxException
	 * @throws IOException
	 * 
	 * Imports a blog via REST.
	 */
	public RepositoryEntryVO importBlog(URL deploymentUrl, String login, String password) throws URISyntaxException, IOException{
		URL blogUrl = FunctionalVOUtil.class.getResource("/org/olat/portfolio/blog.zip");
		Assert.assertNotNull(blogUrl);
		
		File blog = new File(blogUrl.toURI());
		
		RestConnection restConnection = new RestConnection(deploymentUrl);

		assertTrue(restConnection.login(login, password));
		
		URI request = UriBuilder.fromUri(deploymentUrl.toURI()).path("restapi").path("repo/entries").build();
		HttpPut method = restConnection.createPut(request, MediaType.APPLICATION_JSON, true);
		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		entity.addPart("file", new FileBody(blog));
		entity.addPart("filename", new StringBody("blog.zip"));
		entity.addPart("resourcename", new StringBody("Blog"));
		entity.addPart("displayname", new StringBody("Blog"));
		entity.addPart("access", new StringBody("3"));
		method.setEntity(entity);
		
		HttpResponse response = restConnection.execute(method);
		assertTrue(response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201);
		
		InputStream body = response.getEntity().getContent();
		
		RepositoryEntryVO vo = restConnection.parse(body, RepositoryEntryVO.class);
		assertNotNull(vo);
		
		return(vo);
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
}

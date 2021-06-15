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
package org.olat.test.rest;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.olat.restapi.RestConnection;
import org.olat.restapi.support.vo.CourseVO;
import org.olat.restapi.support.vo.RepositoryEntryVO;
import org.olat.test.ArquillianDeployments;
import org.olat.user.restapi.UserVO;

/**
 * 
 * Initial date: 19.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryRestClient {
	

	private final URL deploymentUrl;
	private final String username;
	private final String password;
	
	public RepositoryRestClient(URL deploymentUrl) {
		this(deploymentUrl, "administrator", "openolat");
	}
	
	public RepositoryRestClient(URL deploymentUrl, UserVO author) {
		this.deploymentUrl = deploymentUrl;
		this.username = author.getLogin();
		this.password = author.getPassword();
	}
	
	public RepositoryRestClient(URL deploymentUrl, String username, String password) {
		this.deploymentUrl = deploymentUrl;
		this.username = username;
		this.password = password;
	}
	
	public CourseVO deployDemoCourse()
	throws URISyntaxException, IOException {
		URL url = ArquillianDeployments.class.getResource("file_resources/Demo-Kurs-16.0.zip");
		File archive = new File(url.toURI());
		
		String displayname = "Demo-Kurs-" + UUID.randomUUID().toString();
		return deployCourse(archive, "-", displayname);
	}
	
	public RepositoryEntryVO deployResource(File archive, String resourcename, String displayname)
	throws URISyntaxException, IOException {
		RestConnection conn = new RestConnection(deploymentUrl);
		assertTrue(conn.login(username, password));
		
		URI request = UriBuilder.fromUri(deploymentUrl.toURI()).path("restapi").path("repo").path("entries").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		String softKey = UUID.randomUUID().toString();
		HttpEntity entity = MultipartEntityBuilder.create()
				.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
				.addBinaryBody("file", archive, ContentType.APPLICATION_OCTET_STREAM, archive.getName())
				.addTextBody("filename", archive.getName())
				.addTextBody("resourcename", resourcename)
				.addTextBody("displayname", displayname)
				.addTextBody("access", "3")
				.addTextBody("softkey", softKey)
				.build();
		method.setEntity(entity);
		
		HttpResponse response = conn.execute(method);
		assertTrue(response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201);
		
		RepositoryEntryVO vo = conn.parse(response, RepositoryEntryVO.class);
		assertNotNull(vo);
		assertNotNull(vo.getDisplayname());
		assertNotNull(vo.getKey());
		conn.shutdown();
		return vo;
	}
	
	public CourseVO deployCourse(File archive, String resourcename, String displayname)
	throws URISyntaxException, IOException {
		
		RestConnection conn = new RestConnection(deploymentUrl);
		assertTrue(conn.login(username, password));
		
		URI request = UriBuilder.fromUri(deploymentUrl.toURI()).path("restapi").path("repo/courses").build();
		HttpPost method = conn.createPost(request, MediaType.APPLICATION_JSON);
		String softKey = UUID.randomUUID().toString();
		HttpEntity entity = MultipartEntityBuilder.create()
				.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
				.addBinaryBody("file", archive, ContentType.APPLICATION_OCTET_STREAM, archive.getName())
				.addTextBody("filename", archive.getName())
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
		conn.shutdown();
		return vo;
	}
}

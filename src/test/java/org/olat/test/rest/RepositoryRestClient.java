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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.olat.core.util.StringHelper;
import org.olat.repository.RepositoryEntryImportExportLinkEnum;
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
	
	public static final Long extractRepositoryEntryKey(String url) {
		final String repoMarker = "RepositoryEntry";
		int index = url.indexOf(repoMarker);
		if(index >= 0) {
			url = url.substring(index + repoMarker.length() + 1);
			int endIndex = url.indexOf('/');
			if(endIndex > 0) {
				url = url.substring(0, endIndex);
			}
			if(StringHelper.isLong(url)) {
				return Long.valueOf(url);
			}
		}
		return null;
	}
	
	public CourseVO deployDemoCourse()
	throws URISyntaxException, IOException {
		URL url = ArquillianDeployments.class.getResource("file_resources/Demo-Kurs-16.0.zip");
		File archive = new File(url.toURI());
		
		String displayname = "Demo-Kurs-" + UUID.randomUUID().toString();
		return deployCourse(archive, "-", displayname);
	}
	
	/**
	 * Import a learn resource or a course with a specific soft key. If
	 * the soft key is already used, returns the repository entry which uses
	 * it and don't import again the file.
	 * 
	 * @param archive The ZIP archive
	 * @param displayname The name of the resource
	 * @param softKey The soft key
	 * @return The repository entry
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public RepositoryEntryVO deployResourceBySoftKey(File archive, String displayname, String softKey)
	throws URISyntaxException, IOException {
		RestConnection conn = new RestConnection(deploymentUrl);
		assertTrue(conn.login(username, password));
		
		// Check first if ressource already exists
		List<RepositoryEntryVO> entries = getResourceByExternalId(conn, softKey);
		if(entries != null && entries.size() == 1) {
			return entries.get(0);
		}

		URI request = UriBuilder.fromUri(deploymentUrl.toURI()).path("restapi").path("repo").path("entries").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		HttpEntity entity = MultipartEntityBuilder.create()
				.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
				.addBinaryBody("file", archive, ContentType.APPLICATION_OCTET_STREAM, archive.getName())
				.addTextBody("filename", archive.getName())
				.addTextBody("resourcename", "-")
				.addTextBody("displayname", displayname)
				.addTextBody("softkey", softKey)
				.addTextBody("externalId", softKey)
				.addTextBody("withLinkedReferences", RepositoryEntryImportExportLinkEnum.WITH_SOFT_KEY.name())
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
	
	private List<RepositoryEntryVO> getResourceByExternalId(RestConnection conn, String externalId)
	throws URISyntaxException, IOException {
		URI request = UriBuilder.fromUri(deploymentUrl.toURI()).path("restapi").path("repo").path("entries")
				.queryParam("externalId", externalId).build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		if(response.getStatusLine().getStatusCode() == 200) {
			return conn.parseList(response, RepositoryEntryVO.class);
		}
		return new ArrayList<>();
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

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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.Tracing;
import org.olat.modules.bigbluebutton.BigBlueButtonManager;
import org.olat.modules.bigbluebutton.BigBlueButtonServer;
import org.olat.modules.bigbluebutton.restapi.BigBlueButtonServerVO;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * Initial date: 29 oct. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BigBlueButtonServerWebServiceTest extends OlatRestTestCase {
	
	private static final Logger log = Tracing.createLoggerFor(BigBlueButtonServerWebServiceTest.class);

	@Autowired
	private DB dbInstance;
	@Autowired
	private BigBlueButtonManager bigBlueButtonManager;
	
	@Test
	public void getServers()
	throws IOException, URISyntaxException  {
		
		String name = UUID.randomUUID().toString();
		String url = "https://" + name + "/bigbluebutton";
		String recordingUrl = "https://" + name + "/bigbluebutton/recordings";
		BigBlueButtonServer server = bigBlueButtonManager.createServer(url, recordingUrl, name);
		server.setName(name);
		server = bigBlueButtonManager.updateServer(server);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));			
		
		URI request = UriBuilder.fromUri(getContextURI()).path("bigbluebutton").path("servers").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		
		List<BigBlueButtonServerVO> serverVoes = parseCourseArray(response.getEntity());
		assertThat(serverVoes)
			.isNotNull()
			.isNotEmpty()
			.extracting(vo -> vo.getName())
			.containsAnyOf(name);
	}
	
	@Test
	public void getServer()
	throws IOException, URISyntaxException  {
		
		String name = UUID.randomUUID().toString();
		String url = "https://" + name + "/bigbluebutton";
		String recordingUrl = "https://" + name + "/bigbluebutton/recordings";
		BigBlueButtonServer server = bigBlueButtonManager.createServer(url, recordingUrl, name);
		server.setName(name);
		server = bigBlueButtonManager.updateServer(server);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));			
		
		URI request = UriBuilder.fromUri(getContextURI()).path("bigbluebutton")
				.path("servers").path(server.getKey().toString()).build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		
		BigBlueButtonServerVO serverVo = conn.parse(response.getEntity(), BigBlueButtonServerVO.class);
		Assert.assertNotNull(serverVo);
		Assert.assertEquals(name, serverVo.getName());
		Assert.assertEquals(server.getKey(), serverVo.getKey());
	}
	
	@Test
	public void createServer()
	throws IOException, URISyntaxException  {
		String name = UUID.randomUUID().toString();
		String url = "https://" + name + "/bigbluebutton";
		String recordingUrl = "https://" + name + "/bigbluebutton/recordings";
		
		BigBlueButtonServerVO vo = new BigBlueButtonServerVO();
		vo.setName(name);
		vo.setRecordingUrl(recordingUrl);
		vo.setUrl(url);
		vo.setSharedSecret("secret-" + name);
		vo.setEnabled(Boolean.TRUE);
		vo.setCapacityFactory(Double.valueOf(2.1));
		
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));		

		URI request = UriBuilder.fromUri(getContextURI()).path("bigbluebutton").path("servers").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, vo);
		
		HttpResponse response = conn.execute(method);
		assertThat(response.getStatusLine().getStatusCode())
			.isIn(200, 201);
		
		BigBlueButtonServerVO savedVo = conn.parse(response.getEntity(), BigBlueButtonServerVO.class);
		Assert.assertNotNull(savedVo);
		Assert.assertEquals(name, savedVo.getName());
		Assert.assertEquals(url, savedVo.getUrl());
		Assert.assertEquals(recordingUrl, savedVo.getRecordingUrl());
		Assert.assertEquals("secret-" + name, savedVo.getSharedSecret());
		Assert.assertEquals(Boolean.TRUE, savedVo.getEnabled());
		Assert.assertEquals(2.1d, savedVo.getCapacityFactory().doubleValue(), 0.0001);	
	}
	
	@Test
	public void updateServer()
	throws IOException, URISyntaxException  {
		String name = UUID.randomUUID().toString();
		String url = "https://" + name + "/bigbluebutton";
		String recordingUrl = "https://" + name + "/bigbluebutton/recordings";
		
		BigBlueButtonServer server = bigBlueButtonManager.createServer(url, recordingUrl, name);
		dbInstance.commitAndCloseSession();
		
		String newName = UUID.randomUUID().toString();
		String newUrl = "https://" + newName + "/bigbluebutton";
		String newRecordingUrl = "https://" + newName + "/bigbluebutton/recordings";
		
		BigBlueButtonServerVO vo = new BigBlueButtonServerVO();
		vo.setKey(server.getKey());
		vo.setName(newName);
		vo.setRecordingUrl(newRecordingUrl);
		vo.setUrl(newUrl);
		vo.setSharedSecret("secret-" + newName);
		vo.setEnabled(Boolean.FALSE);
		vo.setCapacityFactory(Double.valueOf(12.5));
		
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));		

		URI request = UriBuilder.fromUri(getContextURI()).path("bigbluebutton").path("servers").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, vo);
		
		HttpResponse response = conn.execute(method);
		assertThat(response.getStatusLine().getStatusCode())
			.isIn(200, 201);
		
		BigBlueButtonServerVO savedVo = conn.parse(response.getEntity(), BigBlueButtonServerVO.class);
		Assert.assertNotNull(savedVo);
		Assert.assertEquals(newName, savedVo.getName());
		Assert.assertEquals(newUrl, savedVo.getUrl());
		Assert.assertEquals(newRecordingUrl, savedVo.getRecordingUrl());
		Assert.assertEquals("secret-" + newName, savedVo.getSharedSecret());
		Assert.assertEquals(Boolean.FALSE, savedVo.getEnabled());
		Assert.assertEquals(12.5d, savedVo.getCapacityFactory().doubleValue(), 0.0001);	
	}
	
	protected List<BigBlueButtonServerVO> parseCourseArray(HttpEntity entity) {
		try(InputStream in=entity.getContent()) {
			ObjectMapper mapper = new ObjectMapper(jsonFactory);
			return mapper.readValue(in, new TypeReference<List<BigBlueButtonServerVO>>(){/* */});
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}

}

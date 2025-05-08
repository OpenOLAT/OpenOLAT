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
package org.olat.test.rest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.junit.Assert;
import org.olat.modules.curriculum.restapi.CurriculumElementTypeVO;
import org.olat.restapi.RestConnection;

/**
 * 
 * Initial date: 30 avr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumRestClient {
	
	private final URL deploymentUrl;
	private final String username;
	private final String password;
	
	public CurriculumRestClient(URL deploymentUrl) {
		this(deploymentUrl, "administrator", "openolat");
	}
	
	public CurriculumRestClient(URL deploymentUrl, String username, String password) {
		this.deploymentUrl = deploymentUrl;
		this.username = username;
		this.password = password;
	}
	
	public CurriculumElementTypeVO createSingleCourseImplementationType(String displayName, String identifier)
	throws URISyntaxException, IOException {
		return createCurriculumElementType(displayName, identifier, Boolean.TRUE, Integer.valueOf(1), Boolean.TRUE);
	}
	
	public CurriculumElementTypeVO createCurriculumElementType(String displayName, String identifier,
			Boolean singleElement, Integer maxRepositoryEntryRelations, Boolean allowedAsRootElement)
	throws URISyntaxException, IOException {
		RestConnection conn = new RestConnection(deploymentUrl, username, password);
		
		CurriculumElementTypeVO vo = new CurriculumElementTypeVO();
		vo.setDisplayName(displayName);
		vo.setIdentifier(identifier);
		vo.setDescription("Selenium type");
		vo.setCssClass("o_icon_selenium");
		vo.setSingleElement(singleElement);
		vo.setMaxRepositoryEntryRelations(maxRepositoryEntryRelations);
		vo.setAllowedAsRootElement(allowedAsRootElement);

		URI request = UriBuilder.fromUri(deploymentUrl.toURI()).path("restapi").path("curriculum").path("types").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, vo);
		
		HttpResponse response = conn.execute(method);
		Assert.assertTrue(response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201);
		
		CurriculumElementTypeVO pvo = conn.parse(response, CurriculumElementTypeVO.class);
		conn.shutdown();
		return pvo;
	}
}

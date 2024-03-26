/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.edusharing.manager;

import org.apache.http.HttpRequest;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.httpclient.HttpClientService;
import org.olat.modules.edusharing.CreateUsageParameter;
import org.olat.modules.edusharing.EdusharingException;
import org.olat.modules.edusharing.EdusharingModule;
import org.olat.modules.edusharing.EdusharingSecurityService;
import org.olat.modules.edusharing.EdusharingSignature;
import org.olat.modules.edusharing.NodeIdentifier;
import org.olat.modules.edusharing.Ticket;
import org.olat.modules.edusharing.model.Usages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * 
 * Initial date: 19 Mar 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
@Service
public class EdusharingRestClient {
	
	private static final Logger log = Tracing.createLoggerFor(EdusharingRestClient.class);
	
	@Autowired
	private EdusharingModule edusharingModule;
	@Autowired
	private EdusharingSecurityService edusharingSecurityService;
	@Autowired
	private EdusharingUserFactory edusharingUserFactory;
	@Autowired
	private HttpClientService httpClientService;
	
	private static final ObjectMapper objectMapper = new ObjectMapper();
	

	public String createTicket(Identity identity) throws EdusharingException {
		String userIdentifier = edusharingUserFactory.getUserIdentifier(identity);
		String url = edusharingModule.getBaseUrl() + "rest/authentication/v1/appauth/" + userIdentifier;
		HttpPost request = new HttpPost(url);
		decorateSignature(request, userIdentifier);
		
		JsonNode userProfile = edusharingUserFactory.getUserProfile(identity);
		String userProfileJson = userProfile.toString();
		log.debug("edu-sharing rest createTicket request: url={}, json={}", url, userProfileJson);
		StringEntity stringEntity = new StringEntity(userProfileJson, ContentType.APPLICATION_JSON);
		request.setEntity(stringEntity);
		
		try (CloseableHttpClient httpClient = httpClientService.createHttpClient();
				CloseableHttpResponse httpResponse = httpClient.execute(request);) {
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			String json = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
			log.debug("edu-sharing rest createTicket response: {}", json);
			
			if (statusCode == HttpStatus.SC_OK) {
				JsonNode jsonNode = objectMapper.readTree(json);
				return jsonNode.get("ticket").asText();
			}
			logUnsuccessful(httpResponse, url, json);
			throw new EdusharingException();
		} catch(Exception e) {
			log.error("", e);
			throw new EdusharingException(e);
		}
	}

	public boolean validateTicket(Ticket ticket) {
		String url = edusharingModule.getBaseUrl() + "rest/authentication/v1/validateSession";
		HttpGet request = new HttpGet(url);
		decorateTicket(request, ticket);
		
		try (CloseableHttpClient httpClient = httpClientService.createHttpClient();
				CloseableHttpResponse httpResponse = httpClient.execute(request);) {
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			String json = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
			log.debug("edu-sharing rest validateTicket response: {}", json);
			
			if (statusCode == HttpStatus.SC_OK) {
				return true;
			}
		} catch(Exception e) {
			log.error("", e);
			throw new EdusharingException(e);
		}
		return false;
	}

	public void createUsage(Ticket ticket, CreateUsageParameter parameter) {
		String url = edusharingModule.getBaseUrl() + "rest/usage/v1/usages/repository/" + parameter.getNodeIdentifier().getRepositoryId();
		HttpPost request = new HttpPost(url);
		decorateSignature(request, ticket.getTooken());
		decorateTicket(request, ticket);

		ObjectNode payload = objectMapper.createObjectNode();
		payload.put("appId", edusharingModule.getAppId());
		payload.put("courseId", parameter.getCourseId());
		payload.put("resourceId", parameter.getResourceId());
		payload.put("nodeId", parameter.getNodeIdentifier().getNodeId());
		payload.put("nodeVersion", parameter.getVersion());
		
		String json = payload.toString();
		log.debug("edu-sharing rest createUsage request: url={}, json={}", url, json);
		StringEntity stringEntity = new StringEntity(json, ContentType.APPLICATION_JSON);
		request.setEntity(stringEntity);
		
		try (CloseableHttpClient httpClient = httpClientService.createHttpClient();
				CloseableHttpResponse httpResponse = httpClient.execute(request);) {
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			json = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
			log.debug("edu-sharing rest createUsage response: {}", json);
			
			if (statusCode == HttpStatus.SC_OK) {
				
				getUsages( ticket, parameter.getNodeIdentifier());
				
				return;
			}
			logUnsuccessful(httpResponse, url, json);
			throw new EdusharingException();
		} catch(Exception e) {
			log.error("", e);
			throw new EdusharingException(e);
		}
	}
	
	public Usages getUsages(Ticket ticket, NodeIdentifier nodeIdentifier) throws EdusharingException {
		String url = edusharingModule.getBaseUrl() + "rest/usage/v1/usages/node/" + nodeIdentifier.getNodeId();
		HttpGet request = new HttpGet(url);
		decorateSignature(request, ticket.getTooken());
		decorateTicket(request, ticket);
		
		try (CloseableHttpClient httpClient = httpClientService.createHttpClient();
				CloseableHttpResponse httpResponse = httpClient.execute(request);) {
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			String json = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
			log.debug("edu-sharing rest getUsages response: {}", json);

			if (statusCode == HttpStatus.SC_OK) {
				return objectMapper.readValue(json, Usages.class);
			}
			logUnsuccessful(httpResponse, url, json);
			throw new EdusharingException();
		} catch(Exception e) {
			log.error("", e);
			throw new EdusharingException(e);
		}
	}

	public void deleteUsage(Ticket ticket, NodeIdentifier nodeIdentifier, String usageId) {
		String url = edusharingModule.getBaseUrl() + "rest/usage/v1/usages/node/" + nodeIdentifier.getNodeId() + "/" + usageId;
		HttpDelete request = new HttpDelete(url);
		decorateSignature(request, nodeIdentifier.getNodeId() + usageId);
		decorateTicket(request, ticket);
		
		try (CloseableHttpClient httpClient = httpClientService.createHttpClient();
				CloseableHttpResponse httpResponse = httpClient.execute(request);) {
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			
			if (statusCode == HttpStatus.SC_OK) {
				return;
			}
			logUnsuccessful(httpResponse, url, null);
			throw new EdusharingException();
		} catch(Exception e) {
			log.error("", e);
			throw new EdusharingException(e);
		}
	}

	private void decorateSignature(HttpRequest request, String signatureData) {
		EdusharingSignature signature = edusharingSecurityService.createSignature(signatureData);
		request.setHeader("X-Edu-App-Id", signature.getAppId());
		request.setHeader("X-Edu-App-Sig", signature.getSignature());
		request.setHeader("X-Edu-App-Signed", signature.getSigned());
		request.setHeader("X-Edu-App-Ts", signature.getTimeStamp());
	}

	private void decorateTicket(HttpRequest request, Ticket ticket) {
		request.setHeader("Authorization", "EDU-TICKET " + ticket.getTooken());
	}
	
	private void logUnsuccessful(CloseableHttpResponse httpResponse, String url, String json) {
		if (httpResponse.getStatusLine().getStatusCode() != 200) {
			log.warn("edu-sharing rest error. status={}, message={}, url={}, json={}",
					httpResponse.getStatusLine().getStatusCode(),
					httpResponse.getStatusLine().getReasonPhrase(),
					url,
					json);
		}
	}

}

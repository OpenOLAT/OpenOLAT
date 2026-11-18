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
package org.olat.modules.opencast.manager.client;

import java.net.URI;
import java.nio.charset.StandardCharsets;

import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.httpclient.HttpClientService;
import org.olat.core.util.httpclient.ProtectionProfile;
import org.olat.modules.opencast.AuthDelegate;
import org.olat.modules.opencast.OpencastModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * Initial date: 4 Aug 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class OpencastRestClient {

	private static final Logger log = Tracing.createLoggerFor(OpencastRestClient.class);
	private static final ProtectionProfile PROTECTION_PROFILE = ProtectionProfile.CONFIGURED;
	
	private static final Event[] NO_EVENTS = new Event[]{};
	private static final Series[] NO_SERIES = new Series[]{};
	
	@Autowired
	private OpencastModule opencastModule;
	@Autowired
	private HttpClientService httpClientService;
	
	private static final ObjectMapper objectMapper = new ObjectMapper();

	public Api getApi() {
		URI uri = URI.create(opencastModule.getApiUrl());
		HttpGet request = new HttpGet(uri);
		decorateRequest(request);
		
		try(CloseableHttpClient client = httpClientService.createHttpClient(PROTECTION_PROFILE)) {
			return client.execute(request, response -> {
				int statusCode = response.getCode();
				log.debug("Status code of: {} {}", uri, statusCode);
			
				if (statusCode == HttpStatus.SC_OK) {
					String json = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
					return objectMapper.readValue(json, Api.class);
				}
				return null;
			});
		} catch(Exception e) {
			log.error("Cannot send: {}", uri, e);
		}
		return null;
	}

	public Event getEvent(String identifier) {
		URI uri = URI.create(opencastModule.getApiUrl() + "/events/" + identifier);
		HttpGet request = new HttpGet(uri);
		decorateRequest(request);
		
		try(CloseableHttpClient client = httpClientService.createHttpClient(PROTECTION_PROFILE)) {
			return client.execute(request, response -> {
				int statusCode = response.getCode();
				log.debug("Status code of: {} {}", uri, statusCode);
				if (statusCode == HttpStatus.SC_NO_CONTENT || statusCode == HttpStatus.SC_OK) {
					String json = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
					return objectMapper.readValue(json, Event.class);
				}
				return null;
			});
		} catch(Exception e) {
			log.error("Cannot send: {}", uri, e);
		}
		return null;
	}
	
	public Event[] getEvents(GetEventsParams params) {
		URI builtUri;
		try {
			URIBuilder builder = new URIBuilder(opencastModule.getApiUrl() + "/events");
			String filterParam = params.getFilterParam();
			if (StringHelper.containsNonWhitespace(filterParam)) {
				builder.addParameter("filter", filterParam);
			}
			String sortParam = params.getSortParam();
			if (StringHelper.containsNonWhitespace(sortParam)) {
				builder.addParameter("sort", sortParam);
			}
			builtUri = builder.build();
		} catch (Exception e) {
			log.error("Cannot get Opencast events.", e);
			return NO_EVENTS;
		}

		URI uri = builtUri;
		HttpGet request = new HttpGet(uri);
		decorateRequest(request, params.getAuthDelegate());
		
		try(CloseableHttpClient client = httpClientService.createHttpClient(PROTECTION_PROFILE)) {
			return client.execute(request, response -> {
				int statusCode = response.getCode();
				log.debug("Status code of: {} {}", uri, statusCode);
				if (statusCode == HttpStatus.SC_OK) {
					String json = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
					return objectMapper.readValue(json, Event[].class);
				}
				return NO_EVENTS;
			});
		} catch(Exception e) {
			log.error("Cannot send: {}", uri, e);
		}
		return NO_EVENTS;
	}

	public boolean deleteEvent(String identifier) {
		boolean deleted = true;
		Event event = getEvent(identifier);
		if (event != null) {
			deleted &= deleteEventFromAdmin(identifier);
			
		}
		return deleted;
	}

	private boolean deleteEventFromAdmin(String identifier) {
		URI uri = URI.create(opencastModule.getApiUrl() + "/events/" + identifier);
		HttpDelete request = new HttpDelete(uri);
		decorateRequest(request);
		
		try(CloseableHttpClient client = httpClientService.createHttpClient(PROTECTION_PROFILE)) {
			return client.execute(request, response -> {
				int statusCode = response.getCode();
				log.debug("Status code of: {} {}", uri, statusCode);
				if (statusCode == HttpStatus.SC_NO_CONTENT || statusCode == HttpStatus.SC_ACCEPTED || statusCode == HttpStatus.SC_OK) {
					return true;
				}
				return false;
			});
		} catch(Exception e) {
			log.error("Cannot send: {}", uri, e);
		}
		return false;
	}
	
	public Series getSeries(String identifier) {
		URI uri = URI.create(opencastModule.getApiUrl() + "/series/" + identifier);
		HttpGet request = new HttpGet(uri);
		decorateRequest(request);
		
		try(CloseableHttpClient client = httpClientService.createHttpClient(PROTECTION_PROFILE)) {
			return client.execute(request, response -> {
				int statusCode = response.getCode();
				log.debug("Status code of: {} {}", uri, statusCode);
				if (statusCode == HttpStatus.SC_NO_CONTENT || statusCode == HttpStatus.SC_OK) {
					String json = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
					return objectMapper.readValue(json, Series.class);
				}
				return null;
			});
		} catch(Exception e) {
			log.error("Cannot send: {}", uri, e);
		}
		return null;
	}
	
	public Series[] getSeries(GetSeriesParams params) {
		URI builtUri;
		try {
			URIBuilder builder = new URIBuilder(opencastModule.getApiUrl() + "/series");
			String filterParam = params.getFilterParam();
			if (StringHelper.containsNonWhitespace(filterParam)) {
				builder.addParameter("filter", filterParam);
			}
			String sortParam = params.getSortParam();
			if (StringHelper.containsNonWhitespace(sortParam)) {
				builder.addParameter("sort", sortParam);
			}
			builtUri = builder.build();
		} catch (Exception e) {
			log.error("Cannot get Opencast series.", e);
			return NO_SERIES;
		}

		URI uri = builtUri;
		HttpGet request = new HttpGet(uri);
		decorateRequest(request, params.getAuthDelegate());
		
		try(CloseableHttpClient client = httpClientService.createHttpClient(PROTECTION_PROFILE)) {
			return client.execute(request, response -> {
				int statusCode = response.getCode();
				log.debug("Status code of: {} {}", uri, statusCode);
				if (statusCode == HttpStatus.SC_OK) {
					String json = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
					return objectMapper.readValue(json, Series[].class);
				}
				return NO_SERIES;
			});
		} catch(Exception e) {
			log.error("Cannot send: {}", uri, e);
		}
		return NO_SERIES;
	}
	
	public boolean isEpisodeExisting(String identifier) {
		URI builtUri;
		try {
			builtUri = new URIBuilder(opencastModule.getApiPresentationUrl() + "/episode.json")
				.addParameter("id", identifier)
				.build();
		} catch (Exception e) {
			log.error("Cannot get Opencast episode.", e);
			return false;
		}
		
		URI uri = builtUri;
		HttpGet request = new HttpGet(uri);
		decorateRequest(request);
		
		try(CloseableHttpClient client = httpClientService.createHttpClient(PROTECTION_PROFILE)) {
			return client.execute(request, response -> {
				int statusCode = response.getCode();
				log.debug("Status code of: {} {}", uri, statusCode);
				if (statusCode == HttpStatus.SC_OK) {
					String json = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
					SearchResult result = objectMapper.readValue(json, SearchResult.class);
					return result.getTotal() > 0;
				}
				return false;
			});
		} catch(Exception e) {
			log.error("Cannot send: {}", uri, e);
		}
		return false;
	}
	
	private void decorateRequest(HttpGet request, AuthDelegate authDelegate) {
		decorateRequest(request);
		if (AuthDelegate.Type.User == authDelegate.getType()) {
			request.setHeader("X-RUN-AS-USER", authDelegate.getValue());
		} else if (AuthDelegate.Type.Roles == authDelegate.getType()) {
			request.setHeader("X-RUN-WITH-ROLES", authDelegate.getValue());
		}
	}

	private void decorateRequest(HttpUriRequestBase request) {
		request.setHeader(HttpHeaders.AUTHORIZATION, opencastModule.getApiAuthorizationHeader());
	}
}

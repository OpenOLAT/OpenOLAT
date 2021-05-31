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
package org.olat.modules.edusharing.manager;

import static org.olat.core.util.StringHelper.blankIfNull;

import java.io.IOException;
import java.util.Properties;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.httpclient.HttpClientService;
import org.olat.modules.edusharing.EdusharingException;
import org.olat.modules.edusharing.EdusharingModule;
import org.olat.modules.edusharing.EdusharingProperties;
import org.olat.modules.edusharing.EdusharingResponse;
import org.olat.modules.edusharing.GetPreviewParameter;
import org.olat.modules.edusharing.GetRenderedParameter;
import org.olat.modules.edusharing.model.EdusharingHttpResponse;
import org.olat.modules.edusharing.model.EdusharingPropertiesImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 14 Dec 2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
class EdusharingHttpClient {
	
	private static final Logger log = Tracing.createLoggerFor(EdusharingHttpClient.class);
	
	
	@Autowired
	private EdusharingModule edusharingModule;
	@Autowired
	private HttpClientService httpClientService;

	EdusharingProperties getMetadata() throws EdusharingException {
		// Usually LMS uses the metadata from format=lms. But usually LMS are
		// written in php. We have to use format=repository because the public key is
		// here formated in a more Java friendly manner.
		String url = edusharingModule.getBaseUrl() + "metadata?format=repository";
		HttpGet request = new HttpGet(url);
		
		try (CloseableHttpClient httpClient = httpClientService.createHttpClient();
				CloseableHttpResponse httpResponse = httpClient.execute(request);) {
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if (statusCode == 200) {
				Properties props = new Properties();
				props.loadFromXML(httpResponse.getEntity().getContent());
				if (log.isDebugEnabled()) log.debug("edu-sharing metadata imported: " + props.toString());
				return new EdusharingPropertiesImpl(props);
			}
			logUnsuccessful(httpResponse, url);
			throw new EdusharingException();
		} catch(Exception e) {
			log.error("", e);
			throw new EdusharingException(e);
		}
	}

	EdusharingResponse getPreview(GetPreviewParameter parameter) throws ClientProtocolException, IOException {
		String url = new StringBuilder()
				.append(edusharingModule.getBaseUrl())
				.append("preview")
				.append("?repoId=").append(blankIfNull(parameter.getRepoId()))
				.append("&nodeId=").append(blankIfNull(parameter.getNodeId()))
				.append("&ticket=").append(blankIfNull(parameter.getTicket()))
				.toString();

		return getEdusharingResponse(url);
	}
	
	EdusharingResponse getRendered(GetRenderedParameter parameter) throws ClientProtocolException, IOException {
		String url = getRenderUrl(parameter);

		return getEdusharingResponse(url);
	}

	String getRenderUrl(GetRenderedParameter parameter) {
		StringBuilder sb =  new StringBuilder();
		sb.append(edusharingModule.getBaseUrl());
		sb.append("renderingproxy");
		sb.append("?app_id=").append(parameter.getAppId());
		sb.append("&rep_id=").append(parameter.getRepoId());
		sb.append("&obj_id=").append(parameter.getNodeId());
		sb.append("&resource_id=").append(parameter.getResourceId());
		sb.append("&course_id=").append(parameter.getCourseId());
		sb.append("&version=").append(parameter.getVersion());
		sb.append("&locale=").append(parameter.getLocale());
		sb.append("&language=").append(parameter.getLanguage());
		sb.append("&signed=").append(parameter.getSigned());
		sb.append("&sig=").append(StringHelper.urlEncodeUTF8(parameter.getSignature()));
		sb.append("&ts=").append(parameter.getTimestamp());
		sb.append("&u=").append(StringHelper.urlEncodeUTF8(parameter.getEncryptedUserIdentifier()));
		sb.append("&display=").append(parameter.getDisplayMode()); // inline / dynamic / window
		if (StringHelper.containsNonWhitespace(parameter.getWidth())) {
			sb.append("&width=").append(parameter.getWidth());
		}
		if (StringHelper.containsNonWhitespace(parameter.getHeight())) {
			sb.append("&height=").append(parameter.getHeight());
		}
		if (StringHelper.containsNonWhitespace(parameter.getEncryptedTicket())) {
			sb.append("&ticket=").append(StringHelper.urlEncodeUTF8(parameter.getEncryptedTicket()));
		}
		return sb.toString();
	}

	private EdusharingResponse getEdusharingResponse(String url) throws IOException, ClientProtocolException {
		log.debug("edu-sharing: get from " + url);
		HttpGet request = new HttpGet(url);
		CloseableHttpClient httpClient = httpClientService.createHttpClient();
		CloseableHttpResponse httpResponse = httpClient.execute(request);
		logUnsuccessful(httpResponse, url);
		return new EdusharingHttpResponse(httpClient, httpResponse);
	}

	private void logUnsuccessful(CloseableHttpResponse httpResponse, String url) {
		if (httpResponse.getStatusLine().getStatusCode() != 200) {
			log.warn("edu-sharing: got http status " + httpResponse.getStatusLine().getStatusCode() + "/"
					+ httpResponse.getStatusLine().getReasonPhrase() + ". URL: " + url);
		}
	}

}

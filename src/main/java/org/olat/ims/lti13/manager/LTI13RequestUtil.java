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
package org.olat.ims.lti13.manager;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.logging.Tracing;
import org.olat.core.util.httpclient.HttpClientService;
import org.olat.ims.lti13.LTI13JsonUtil;

/**
 * 
 * Initial date: 8 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LTI13RequestUtil {
	
	private static final Logger log = Tracing.createLoggerFor(LTI13RequestUtil.class);
	
	private LTI13RequestUtil() {
		//
	}
	
	public static String scoreUrl(String endpointUrl) {
		String scoreUrl;
		int index = endpointUrl.indexOf('?');
		if(index >= 0) {
			String url = endpointUrl.substring(0, index);
			String query = endpointUrl.substring(index);
			scoreUrl = url + "/scores" + query;
		} else {
			scoreUrl = endpointUrl + "/scores";
		}
		return scoreUrl;
	}
	
	public static String post(String accessToken, String contentType, String url, Object payload) throws Exception {
		URI uri = new URI(url);
		HttpPost post = new HttpPost(uri);
		return push(post, accessToken, contentType, payload);
	}

	public static String put(String accessToken, String contentType, String url, Object payload) throws Exception {
		URI uri = new URI(url);
		HttpPut post = new HttpPut(uri);
		return push(post, accessToken, contentType, payload);
	}

	public static String push(HttpEntityEnclosingRequestBase method, String accessToken, String contentType, Object payload) throws Exception {
	
		method.addHeader("Authorization", "Bearer " + accessToken);
		ContentType cType = ContentType.create(contentType);
		
		String payloadString = LTI13JsonUtil.prettyPrint(payload);
		HttpEntity myEntity = new StringEntity(payloadString, cType);
		method.setEntity(myEntity);

		try(CloseableHttpClient httpClient = CoreSpringFactory.getImpl(HttpClientService.class)
				.createHttpClientBuilder()
				.disableAutomaticRetries()
				.build();
				CloseableHttpResponse response = httpClient.execute(method)) {
			int statusCode = response.getStatusLine().getStatusCode();
			String message = response.getStatusLine().getReasonPhrase();
			log.debug("Status code of: {} {} {}", statusCode, message, method.getURI());
			return EntityUtils.toString(response.getEntity());
		} catch(Exception e) {
			log.error("Cannot send: {}", method.getURI(), e);
			return null;
		}
	}
	
	public static String execute(String accessToken, String accept, String url) throws URISyntaxException {
		URI uri = new URI(url);
		HttpGet get = new HttpGet(uri);
		get.addHeader("Authorization", "Bearer " + accessToken);
		get.addHeader("Accept", accept);

		try(CloseableHttpClient httpClient = CoreSpringFactory.getImpl(HttpClientService.class)
				.createHttpClientBuilder()
				.disableAutomaticRetries()
				.build();
				CloseableHttpResponse response = httpClient.execute(get)) {
			int statusCode = response.getStatusLine().getStatusCode();
			String message = response.getStatusLine().getReasonPhrase();
			log.debug("Status code of: {} {} {}", statusCode, message, get.getURI());
			return EntityUtils.toString(response.getEntity());
		} catch(Exception e) {
			log.error("Cannot send: {}", uri, e);
			return null;
		}
	}

}

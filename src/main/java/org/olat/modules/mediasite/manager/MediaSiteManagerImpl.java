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
package org.olat.modules.mediasite.manager;

import java.util.Map;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.httpclient.HttpClientService;
import org.olat.ims.lti.LTIManager;
import org.olat.modules.mediasite.MediaSiteManager;
import org.olat.modules.mediasite.MediaSiteModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Initial date: 25.10.2021<br>
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
@Service
public class MediaSiteManagerImpl implements MediaSiteManager {
	
	private static final Logger log = Tracing.createLoggerFor(MediaSiteManagerImpl.class);

	@Autowired
	private MediaSiteModule mediaSiteModule;
	@Autowired
	private LTIManager ltiManager;
	@Autowired
	private HttpClientService httpClientService;

	private ObjectMapper mapper;

	public MediaSiteManagerImpl() {
		mapper = new ObjectMapper();
	}
	
	@Override
	public boolean checkModuleId(String moduleId) {
		boolean moduleIdExists = false;

		String url = String.format(mediaSiteModule.getBaseURL(), moduleId);

		HttpGet request;
		try {
			request = new HttpGet(url);
		} catch (Exception e) {
			log.warn("card2brain alias with illegal characters: {}", url);
			return false;
		}
		
		try(CloseableHttpClient httpclient = httpClientService.createHttpClient();
				CloseableHttpResponse response = httpclient.execute(request);) {
			try {
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK && EntityUtils.toByteArray(response.getEntity()).length > 0) {
					moduleIdExists = true;
				}
			} catch (Exception e) {
				// nothing to do: moduleIdExists is false.
			}
		} catch(Exception e) {
			log.warn("", e);
		}
		
		log.debug("Check MediaSite ID ({}): {}", url, moduleIdExists);
		
		return moduleIdExists;
	}

	@Override
	public MediaSiteVerificationResult checkEnterpriseLogin(String url, String key, String secret) {
		MediaSiteVerificationResult result = null;
		
		try {
			Map<String,String> signedPros = ltiManager.sign(null, url, key, secret);
			String content = ltiManager.post(signedPros, url);
			result = mapper.readValue(content, MediaSiteVerificationResult.class);
		} catch (JsonParseException jsonParseException) {
			// ignore and return null
		} catch (Exception e) {
			log.error("", e);
		}

		return result;
	}

	@Override
	public String parseAlias(String identifier) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
}

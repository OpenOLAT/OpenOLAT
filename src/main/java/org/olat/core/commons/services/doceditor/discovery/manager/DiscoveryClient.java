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
package org.olat.core.commons.services.doceditor.discovery.manager;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.doceditor.discovery.Discovery;
import org.olat.core.commons.services.doceditor.discovery.model.DiscoveryImpl;
import org.olat.core.logging.Tracing;
import org.olat.core.util.httpclient.HttpClientService;
import org.olat.core.util.httpclient.ProtectionProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 1 Mar 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
class DiscoveryClient {
	
	private static final Logger log = Tracing.createLoggerFor(DiscoveryClient.class);
	
	@Autowired
	private HttpClientService HttpClientService;

	String getRegularDiscoveryPath() {
		return "hosting/discovery";
	}
	
	Discovery getDiscovery(String discoveryUrl) {
		HttpGet request = new HttpGet(discoveryUrl);
		try (CloseableHttpClient httpClient = HttpClientService.createHttpClient(ProtectionProfile.CONFIGURED)) {
			return httpClient.execute(request, httpResponse -> {
				int statusCode = httpResponse.getCode();
				if (statusCode == 200) {
					HttpEntity entity = httpResponse.getEntity();
					String xml = EntityUtils.toString(entity);
					return DiscoveryXStream.fromXml(xml, DiscoveryImpl.class);
				}
				log.warn("Fetching document editor discovery file return with status " + statusCode + ". URL: " + discoveryUrl);
				return null;
			});
		} catch(Exception e) {
			log.error("Error while fetching document editor discovery file. URL: " + discoveryUrl, e);
		}
		return null;
	}

}

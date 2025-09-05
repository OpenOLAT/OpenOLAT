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
package org.olat.core.util.httpclient;

import java.util.Collection;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.protocol.HttpContext;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;

/**
 * 
 * Initial date: Sep 5, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class ProxyRoutePlanner extends DefaultProxyRoutePlanner {

	private static final Logger log = Tracing.createLoggerFor(ProxyRoutePlanner.class);

	private final Collection<String> httpProxyExclusionUrls;

	public ProxyRoutePlanner(HttpHost proxy, Collection<String> httpProxyExclusionUrls) {
		super(proxy);
		this.httpProxyExclusionUrls = httpProxyExclusionUrls;
	}
	
	@Override
	protected HttpHost determineProxy(HttpHost target, HttpRequest request, HttpContext context) throws HttpException {
		if (httpProxyExclusionUrls.contains(target.getHostName().toLowerCase())) {
			log.debug("Proxy bypass to {}", target.getHostName());
			return null; // Bypass proxy
		}
		
		log.debug("Proxy to {}", target.getHostName());
		return super.determineProxy(target, request, context);
	}

}

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
package org.olat.core.commons.services.doceditor.office365.manager;

import java.net.URI;

import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 2 May 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
class UrlParser {

	private static final OLog log = Tracing.createLoggerFor(UrlParser.class);

	String getProtocolAndDomain(String url) {
		URI uri;
		try {
			String stripped = stripQuery(url);
			uri = new URI(stripped);
			return uri.getScheme() + "://" + uri.getHost();
		} catch (Exception e) {
			log.error("", e);
		}
		return null;
	}

	private String stripQuery(String url) {
		return url!= null? url.substring(0, url.indexOf("?")): null;
	}

}

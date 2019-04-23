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
package org.olat.modules.adobeconnect.model;

import org.apache.http.Header;
import org.olat.modules.adobeconnect.manager.AbstractAdobeConnectProvider;

/**
 * 
 * Initial date: 17 avr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BreezeSession {
	
	private static final long EXPIRATION_NS = 5l * 60l * 1000l * 1000l * 1000l;// % Minutes in nanoseconds
	
	private final String cookie;
	private final String session;
	private final long creationTime;
	
	private BreezeSession(String cookie, String session) {
		this.cookie = cookie;
		this.session = session;
		creationTime = System.nanoTime();
	}

	public String getCookie() {
		return cookie;
	}

	public String getSession() {
		return session;
	}
	
	public boolean isValid() {
		return System.nanoTime() - creationTime < EXPIRATION_NS;
	}
	
	/**
	 * BREEZESESSION=na2breezc8ewb75gvq5wdooa; HttpOnly; domain=adobeconnect.com; secure; path=/
	 * @param header
	 * @return
	 */
	public static BreezeSession valueOf(Header header) {
		String value = header.getValue();
		String cookie = value;
		int index = cookie.indexOf(AbstractAdobeConnectProvider.COOKIE) + AbstractAdobeConnectProvider.COOKIE.length();
		int lastIndex = cookie.indexOf(';', index);
		String session = cookie.substring(index, lastIndex);
		return new BreezeSession(cookie, session);
	}

}

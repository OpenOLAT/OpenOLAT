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
package org.olat.core.gui.util;

import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.DispatchResult;
import org.olat.core.id.Identity;
import org.olat.core.util.UserSession;

/**
 * 
 * Initial date: 07.12.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SyntheticUserRequest implements UserRequest {
	
	private final String uuid;
	private final Locale locale;
	private final Identity identity;
	private final Date requestTimestamp;
	private UserSession userSession = null;
	private static AtomicInteger count = new AtomicInteger(0);
	
	public SyntheticUserRequest(Identity identity, Locale locale) {
		this.identity = identity;
		this.locale = locale;
		requestTimestamp = new Date();
		uuid = "syn-" + count.incrementAndGet();
	}
	
	public SyntheticUserRequest(Identity identity, Locale locale, UserSession userSession) {
		this(identity, locale);
		this.userSession = userSession;
	} 
	
	@Override
	public String getUuid() {
		return uuid;
	}
	
	@Override
	public Date getRequestTimestamp() {
		return requestTimestamp;
	}

	@Override
	public String getUriPrefix() {
		return null;
	}

	@Override
	public String getParameter(String key) {
		return null;
	}

	@Override
	public Set<String> getParameterSet() {
		return Collections.emptySet();
	}

	@Override
	public HttpServletRequest getHttpReq() {
		return null;
	}

	@Override
	public UserSession getUserSession() {
		return userSession;
	}

	@Override
	public HttpServletResponse getHttpResp() {
		return null;
	}

	@Override
	public Locale getLocale() {
		return locale;
	}

	@Override
	public Identity getIdentity() {
		return identity;
	}

	@Override
	public String getModuleURI() {
		return null;
	}

	@Override
	public String getWindowID() {
		return null;
	}
	
	@Override
	public String getWindowComponentID() {
		return null;
	}
	
	@Override
	public void overrideWindowComponentID(String dispatchId) {
		//
	}

	@Override
	public String getTimestampID() {
		return null;
	}

	@Override
	public String getComponentID() {
		return null;
	}

	@Override
	public String getComponentTimestamp() {
		return null;
	}

	@Override
	public String getRequestCsrfToken() {
		return null;
	}

	@Override
	public void setRequestCsrfToken(String token) {
		//
	}

	@Override
	public boolean isValidDispatchURI() {
		return false;
	}

	@Override
	public String getNonParsedUri() {
		return null;
	}

	@Override
	public DispatchResult getDispatchResult() {
		return null;
	}

	@Override
	public int getMode() {
		return 0;
	}
}

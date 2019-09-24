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
package org.olat.core.util.session;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 15.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class UserSessionModule extends AbstractSpringModule {
	

	private static final String SESSION_TIMEOUT = "session.timeout";
	private static final String SESSION_TIMEOUT_AUTH = "session.timeout.authenticated";
	
	@Value("${session.timeout}")
	private int sessionTimeout;
	@Value("${session.timeout.authenticated}")
	private int sessionTimeoutAuthenticated;
	@Value("${session.timeout.extended.for}")
	private String sessionTimeoutExtendedFor;
	
	@Autowired
	public UserSessionModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		String timeoutObj = getStringPropertyValue(SESSION_TIMEOUT, true);
		if(StringHelper.isLong(timeoutObj)) {
			sessionTimeout = Integer.parseInt(timeoutObj);
		}
		
		String timeoutAuthObj = getStringPropertyValue(SESSION_TIMEOUT_AUTH, true);
		if(StringHelper.isLong(timeoutAuthObj)) {
			sessionTimeoutAuthenticated = Integer.parseInt(timeoutAuthObj);
		}
	}

	@Override
	protected void initFromChangedProperties() {
		init();
	}

	public int getSessionTimeout() {
		return sessionTimeout;
	}

	public void setSessionTimeout(int timeout) {
		setStringProperty(SESSION_TIMEOUT, Integer.toString(timeout), true);
	}

	public int getSessionTimeoutAuthenticated() {
		return sessionTimeoutAuthenticated;
	}

	public void setSessionTimeoutAuthenticated(int timeout) {
		setStringProperty(SESSION_TIMEOUT_AUTH, Integer.toString(timeout), true);
	}

	public String[] getSessionTimeoutExtendedFor() {
		if(StringHelper.containsNonWhitespace(sessionTimeoutExtendedFor)) {
			return sessionTimeoutExtendedFor.split("[,]");
		}
		return new String[0];
	}

	public void setSessionTimeoutExtendedFor(String sessionTimeoutExtendedFor) {
		this.sessionTimeoutExtendedFor = sessionTimeoutExtendedFor;
	}
}

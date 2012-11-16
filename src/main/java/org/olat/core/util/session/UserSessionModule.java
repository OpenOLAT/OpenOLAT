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

import org.olat.core.configuration.AbstractOLATModule;
import org.olat.core.configuration.PersistedProperties;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 15.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserSessionModule extends AbstractOLATModule {
	

	private static final String SESSION_TIMEOUT = "session.timeout";
	private static final String SESSION_TIMEOUT_AUTH = "session.timeout.authenticated";
	
	private int sessionTimeout;
	private int sessionTimeoutAuthenticated;

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
	protected void initDefaultProperties() {
		sessionTimeout = getIntConfigParameter(SESSION_TIMEOUT, 7200);
		sessionTimeoutAuthenticated = getIntConfigParameter(SESSION_TIMEOUT_AUTH, 300);
	}

	@Override
	protected void initFromChangedProperties() {
		init();
	}

	@Override
	public void setPersistedProperties(PersistedProperties persistedProperties) {
		this.moduleConfigProperties = persistedProperties;
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
}

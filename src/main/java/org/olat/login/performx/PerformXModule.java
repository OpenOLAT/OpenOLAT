/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.login.performx;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 28.07.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class PerformXModule extends AbstractSpringModule implements ConfigOnOff {
	
	public static final String PERFORMX_AUTH = "PERFORMX";
	
	@Value("${performx.enable:true}")
	private boolean enabled;	
	@Value("${performx.server.url:true}")
	private String performxServerUrl;
	@Value("${performx.server.username}")
	private String performxServerUsername;
	@Value("${performx.server.password}")
	private String performxServerPassword;
	@Value("${performx.clientIdCheck.enable:true}")
	private boolean performxClientIdCheckEnabled;	
	
	@Autowired
	private DispatcherModule dispatcherModule;
	@Autowired
	private PerformXDispatcher performXDispatcher;
	
	@Autowired
	public PerformXModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		dispatcherModule.getDispatchers().put("/performx/", performXDispatcher);
		dispatcherModule.getDispatchers().put("/performx", performXDispatcher);
	}

	@Override
	protected void initFromChangedProperties() {
		//
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	public String getPerformxServerUrl() {
		return performxServerUrl;
	}

	public String getPerformxServerUsername() {
		return performxServerUsername;
	}

	public String getPerformxServerPassword() {
		return performxServerPassword;
	}

	public boolean performxClientIdCheckEnabled() {
		return performxClientIdCheckEnabled;
	}
}

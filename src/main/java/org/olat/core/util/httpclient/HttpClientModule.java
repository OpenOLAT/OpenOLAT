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
package org.olat.core.util.httpclient;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 31.05.2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class HttpClientModule extends AbstractSpringModule {

	@Value("${http.connect.timeout:30000}")
	private int httpConnectTimeout;
	@Value("${http.connect.request.timeout:30000}")
	private int httpConnectRequestTimeout;
	@Value("${http.connect.socket.timeout:30000}")
	private int httpSocketTimeout;
	
	@Autowired
	private HttpClientModule(CoordinatorManager coordinateManager) {
		super(coordinateManager);
	}

	@Override
	public void init() {
		updateProperties();
	}

	@Override
	protected void initFromChangedProperties() {
		updateProperties();
	}
	
	private void updateProperties() {
		//
	}
	
	public int getHttpConnectTimeout() {
		return httpConnectTimeout;
	}

	public int getHttpConnectRequestTimeout() {
		return httpConnectRequestTimeout;
	}

	public int getHttpSocketTimeout() {
		return httpSocketTimeout;
	}

}

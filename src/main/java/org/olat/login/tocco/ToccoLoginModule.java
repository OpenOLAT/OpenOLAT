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
package org.olat.login.tocco;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 22 avr. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ToccoLoginModule extends AbstractSpringModule implements ConfigOnOff  {
	
	public static final String TOCCO_PROVIDER = "TOCCO";
	
	@Value("${tocco.enable:false}")
	private boolean enabled;	
	@Value("${tocco.server.url:true}")
	private String toccoServerUrl;
	@Value("${tocco.changePasswordUrl}")
	private String changePasswordUrl;
	
	@Autowired
	public ToccoLoginModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		//
	}

	@Override
	protected void initFromChangedProperties() {
		// 
	}
	
	@Override
	public boolean isEnabled() {
		return enabled;
	}
	
	public String getToccoServerUrl() {
		return toccoServerUrl;
	}

	public void setToccoServerUrl(String toccoServerUrl) {
		this.toccoServerUrl = toccoServerUrl;
	}

	public String getChangePasswordUrl() {
		return changePasswordUrl;
	}

}

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
package org.olat.core.commons.services.help;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.help.spi.ConfluenceLinkSPI;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 07.01.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class HelpModule extends AbstractSpringModule {
	
	@Value("${help.enabled:true}")
	private boolean helpEnabled;
	@Value("${help.plugin:ooConfluenceHelp}")
	private String providerId;
	
	@Autowired
	private ConfluenceLinkSPI confluenceSpi;
	
	@Autowired
	public HelpModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}
	
	public HelpLinkSPI getHelpProvider( ) {
		Object provider = CoreSpringFactory.getBean(providerId);
		if(provider instanceof HelpLinkSPI) {
			return(HelpLinkSPI)provider;
		}
		return confluenceSpi;
	}

	public boolean isHelpEnabled() {
		return helpEnabled;
	}

	public void setHelpEnabled(boolean helpEnabled) {
		this.helpEnabled = helpEnabled;
	}

	@Override
	public void init() {
		//
	}

	@Override
	protected void initFromChangedProperties() {
		//
	}
}
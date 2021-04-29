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

import org.olat.login.auth.AuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22 avr. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ToccoAuthenticationProvider extends AuthenticationProvider {
	
	@Autowired
	private ToccoLoginModule toccoLoginModule;
	
	protected ToccoAuthenticationProvider(String name, String clazz, boolean enabled, boolean isDefault,
			String iconCssClass) {
		super(name, clazz, enabled, isDefault, iconCssClass);
	}
	
	@Override
	public boolean isEnabled() {
		return toccoLoginModule.isEnabled();
	}
}

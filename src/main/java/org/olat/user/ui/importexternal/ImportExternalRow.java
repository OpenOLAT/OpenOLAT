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
package org.olat.user.ui.importexternal;

import org.olat.core.id.Identity;
import org.olat.core.id.User;

/**
 * 
 * Initial Date:  5 mar. 2020 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ImportExternalRow {
	
	private String authenticationProvider;
	private final Identity identity;
	
	public ImportExternalRow(Identity identity) {
		this.identity = identity;
	}
	
	public Long getKey() {
		return identity.getKey();
	}
	
	public String getUsername() {
		return identity.getName();
	}
	
	public Identity getIdentity() {
		return identity;
	}
	
	public User getUser() {
		return identity.getUser();
	}
	
	public String getAuthenticationProvider() {
		return authenticationProvider;
	}
	
	public void setAuthenticationProvider(String provider) {
		this.authenticationProvider = provider;
	}

}

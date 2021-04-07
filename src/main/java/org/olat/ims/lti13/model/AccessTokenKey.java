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
package org.olat.ims.lti13.model;

import java.util.List;

import org.olat.ims.lti13.LTI13Platform;

/**
 * 
 * Initial date: 12 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AccessTokenKey {
	
	private final String scopes;
	private final Long sharedToolKey;
	
	public AccessTokenKey(LTI13Platform sharedTool, List<String> scopeList) {
		this.sharedToolKey = sharedTool.getKey();
		this.scopes = scopeList == null ? "" : String.join("|", scopeList);
	}

	@Override
	public int hashCode() {
		return (scopes == null ? -82638 : scopes.hashCode())
				+ (sharedToolKey == null ? -9348 : sharedToolKey.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof AccessTokenKey) {
			AccessTokenKey key = (AccessTokenKey)obj;
			return sharedToolKey != null && sharedToolKey.equals(key.sharedToolKey)
					&& scopes != null && scopes.equals(key.scopes);
		}
		return false;
	}
}

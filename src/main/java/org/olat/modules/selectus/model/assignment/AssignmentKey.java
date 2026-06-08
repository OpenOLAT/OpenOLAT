/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.model.assignment;

/**
 * 
 * Initial date: 25 oct. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssignmentKey {
	
	private final Long identityKey;
	private final Long applicationKey;
	
	public AssignmentKey(Long identityKey, Long applicationKey) {
		this.identityKey = identityKey;
		this.applicationKey = applicationKey;
	}

	public Long getIdentityKey() {
		return identityKey;
	}

	public Long getApplicationKey() {
		return applicationKey;
	}

	@Override
	public int hashCode() {
		return identityKey.hashCode() + applicationKey.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof AssignmentKey) {
			AssignmentKey assignmentKey = (AssignmentKey)obj;
			return identityKey.equals(assignmentKey.identityKey)
					&& applicationKey.equals(assignmentKey.applicationKey);
		}
		return false;
	}
}

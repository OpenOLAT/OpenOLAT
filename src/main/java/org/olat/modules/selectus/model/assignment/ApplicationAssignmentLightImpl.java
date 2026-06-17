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

import org.olat.modules.selectus.model.ApplicationAssignmentLight;

/**
 * 
 * Initial date: 25 oct. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationAssignmentLightImpl implements ApplicationAssignmentLight {

	private final Long key;
	private final Long applicationKey;
	private final Long assigneeKey;
	
	public ApplicationAssignmentLightImpl(Long key, Long applicationKey, Long assigneeKey) {
		this.key = key;
		this.applicationKey = applicationKey;
		this.assigneeKey = assigneeKey;
	}

	@Override
	public Long getKey() {
		return key;
	}


	@Override
	public Long getApplicationKey() {
		return applicationKey;
	}


	@Override
	public Long getAssigneeKey() {
		return assigneeKey;
	}
	
	@Override
	public int hashCode() {
		return getKey() == null ? 394857 : getKey().intValue();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof ApplicationAssignmentLightImpl) {
			ApplicationAssignmentLightImpl cat = (ApplicationAssignmentLightImpl)obj;
			return getKey() != null && getKey().equals(cat.getKey());
		}
		return false;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("assignmentLight[key=").append(getKey() == null ? "" : getKey()).append(";")
			.append("applicationKey=").append(applicationKey == null ? "" : applicationKey).append(";")
			.append("assigneeKey=").append(assigneeKey == null ? "" : assigneeKey).append("]");
		return sb.toString();
	}
}

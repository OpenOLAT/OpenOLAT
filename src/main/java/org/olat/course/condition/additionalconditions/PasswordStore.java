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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package org.olat.course.condition.additionalconditions;

public class PasswordStore {
	private String password;
	private Long nodeIdent;
	private Long courseId;
	public String getPassword(){
		return this.password;
	}
	
	public void setPassword(String password){
		this.password = password;
	}
	
	public Long getNodeIdent() {
		return nodeIdent;
	}

	public void setNodeIdent(Long nodeIdent) {
		this.nodeIdent = nodeIdent;
	}

	public Long getCourseId() {
		return courseId;
	}

	public void setCourseId(Long courseId) {
		this.courseId = courseId;
	}
	
	@Override
	public boolean equals(Object object) {
		if(object == null) return false;
		boolean isEquals = false;
		PasswordStore pws = (PasswordStore) object;
		if(password.equals(pws.getPassword())
				&& nodeIdent.equals(pws.getNodeIdent())
				&& courseId.equals(pws.getCourseId())) {
			isEquals = true;
		}
		return isEquals;
	}
}

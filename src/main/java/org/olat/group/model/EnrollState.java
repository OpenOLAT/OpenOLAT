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
package org.olat.group.model;

import org.olat.basesecurity.GroupRoles;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class EnrollState {
	
	private boolean failed = false;
	private String i18nErrorMessage;
	private GroupRoles enrolled;
	
	public boolean isFailed() {
		return failed;
	}

	public void setFailed(boolean failed) {
		this.failed = failed;
	}

	public String getI18nErrorMessage() {
		return i18nErrorMessage;
	}

	public void setI18nErrorMessage(String i18nErrorMessage) {
		this.i18nErrorMessage = i18nErrorMessage;
	}

	public GroupRoles getEnrolled() {
		return enrolled;
	}

	public void setEnrolled(GroupRoles enrolled) {
		this.enrolled = enrolled;
	}
}
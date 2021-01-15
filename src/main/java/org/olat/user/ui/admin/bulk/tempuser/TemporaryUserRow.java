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
package org.olat.user.ui.admin.bulk.tempuser;

import org.olat.admin.user.imp.TransientIdentity;

/**
 * 
 * Initial date: 14 janv. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TemporaryUserRow {
	
	private final String errorMsg;
	private final boolean alreadyExists;
	private final TransientIdentity identity;
	
	public TemporaryUserRow(TransientIdentity identity, boolean alreadyExists, String errorMsg) {
		this.identity = identity;
		this.errorMsg = errorMsg;
		this.alreadyExists = alreadyExists;
	}
	
	public TransientIdentity getIdentity() {
		return identity;
	}
	
	public boolean isAlreadyExists() {
		return alreadyExists;
	}
	
	public String getErrorMsg() {
		return errorMsg;
	}

}

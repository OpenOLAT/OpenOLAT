/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.admin.user.delete.service;

import org.olat.core.commons.services.taskexecutor.LongRunnable;

/**
 * This is only a placeholder for 15.5.3 and older OpenOlat version.
 * 
 * Initial date: 02.07.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DeleteUserDataTask implements LongRunnable {
	private static final long serialVersionUID = 4278304131373256050L;

	private final Long identityKey;
	private final String newDeletedUserName;//it's the used username, not the one (let the name because of XStream)
	
	public DeleteUserDataTask(Long identityKey, String deletedUserName) {
		this.identityKey = identityKey;
		this.newDeletedUserName = deletedUserName;
	}
	
	public Long getIdentityKey() {
		return identityKey;
	}
	
	public String getNewDeletedUserName() {
		return newDeletedUserName;
	}
	
	@Override
	public Queue getExecutorsQueue() {
		return Queue.lowPriority;
	}

	@Override
	public void run() {
		//
	}
}

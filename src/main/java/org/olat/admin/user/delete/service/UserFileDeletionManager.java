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

import java.io.File;
import java.io.FilenameFilter;

import org.olat.core.commons.services.taskexecutor.TaskExecutorManager;
import org.olat.core.id.Identity;
import org.olat.core.manager.BasicManager;
import org.olat.user.UserDataDeletable;


/**
 * Manager to delete all files for user-deletion workflow.
 * 
 * @author Christian Guretzki  
 */
public class UserFileDeletionManager extends BasicManager implements UserDataDeletable{

	private TaskExecutorManager taskExecutorManager;


	/**
	 * [spring]
	 * @param userDeletionManager
	 */
	private UserFileDeletionManager() {
		//
	}

	/**
	 * [spring]
	 * @param taskExecutorManager
	 */
	public void setTaskExecutorManager(TaskExecutorManager taskExecutorManager) {
		this.taskExecutorManager = taskExecutorManager;
	}

  public void deleteUserData(final Identity identity, final String newDeletedUserName) {
  	taskExecutorManager.execute(new DeleteUserDataTask(identity.getKey(), newDeletedUserName));
  }
}
	
class UserFileFilter implements FilenameFilter  {
	
	private String username;

	UserFileFilter(String username) {
		this.username = username;
	}

	@Override
	public boolean accept(File dir, String name) {
		// don't add overlayLocales as selectable availableLanguages
		// (LocaleStrings_de__VENDOR.properties)
		if (   name.equals(username) ) { 
			return true; 
		} else {
			return false;
		}
	}
}


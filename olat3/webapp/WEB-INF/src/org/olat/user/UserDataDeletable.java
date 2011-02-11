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
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.user;

import org.olat.core.id.Identity;


/**
 * Used to delete user-data in delete-user-workflow.
 * All managers with deletable user-data must implement this interface 
 * and register themself at user-manager as deletable-resource.
 * @author Christian Guretzki
 */
public interface UserDataDeletable {
	/**
	 * Delete user data for certain user
	 * @param identity  Data for this identity will be deleted 
	 */
	public void deleteUserData(Identity identity, String newDeletedUserName);
}
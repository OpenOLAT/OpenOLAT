/**
 * OLAT - Online Learning and Training<br />
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br />
 * you may not use this file except in compliance with the License.<br />
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br />
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * <br />
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br />
 * See the License for the specific language governing permissions and <br />
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br />
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.instantMessaging.syncservice;

/**
 * Interface for account creation of an instant messaging account on a suitable
 * server. Change spring config at module instant messaging to choose an other
 * implementation at startup. An instance of the implemenation is created at
 * startup by the InstantMessagingModule.init() method.
 * 
 * @author guido
 */
public interface RemoteAccountCreation {
	
	/**
	 * @param username
	 * @param password
	 * @param name
	 * @param email
	 * @return true if successfull account creation
	 */
	public boolean createAccount(String username, String password, String name, String email);
	
	/**
	 * delete an account on the IM server
	 * @param username
	 * @return
	 */
	public boolean deleteAccount(String username);

	/**
	 * check wheter the user has already an account on the IM server
	 * @param username
	 * @return
	 */
	public boolean hasAccount(String username);
}
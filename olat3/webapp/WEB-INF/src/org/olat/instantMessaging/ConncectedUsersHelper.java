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
* software distributed under the License is distributed on an "AS IS" BASIS, <br />
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br />
* See the License for the specific language governing permissions and <br />
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br />
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.instantMessaging;

/**
 * Helper object that delivers a dynamic numer of the current available
 * connected instant messaging objects.
 * <P>
 *          Initial Date: 01.02.2005 <br />
 * @author guido
 */
public class ConncectedUsersHelper {

	private final InstantMessaging im;

	/**
	 * constructor
	 */
	public ConncectedUsersHelper() {
		im = InstantMessagingModule.getAdapter();
	}

	/**
	 * [used by velocity]
	 * @return number of connected users
	 */
	public int countConnectedUsers() {
		return im.countConnectedUsers();
	}

}
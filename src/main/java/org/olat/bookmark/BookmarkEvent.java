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
* <p>
*/ 

package org.olat.bookmark;

import org.olat.core.util.event.MultiUserEvent;

/**
 * 
 * Description:<br>
 * Any add/modified/delete bookmark event. <p>
 * The event could be targeted at a specific user or at all users.
 * 
 * <P>
 * Initial Date:  23.09.2008 <br>
 * @author Lavinia Dumitrescu
 */
public class BookmarkEvent extends MultiUserEvent {
	
	private static final String ALL_USERS = "all";
	private final String username;
	
	/**
	 * 
	 * @param username
	 */
	public BookmarkEvent(String username) {
		super("bookmark_event");		
		this.username = username;
	}
	
	/**
	 * No argument constructor - this event concerns all users.
	 *
	 */
	public BookmarkEvent() {
		this(ALL_USERS);
	}
	
	public String getUsername() {
		return username;
	}
	
	/**
	 * 
	 * @return true if the event is intended for all users, else false
	 */
	public boolean isAllUsersEvent() {
		return ALL_USERS.equals(getUsername()); 
	}
	
}

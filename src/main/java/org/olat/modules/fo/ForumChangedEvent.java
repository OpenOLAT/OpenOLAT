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
package org.olat.modules.fo;

import org.olat.core.id.Identity;
import org.olat.core.util.event.MultiUserEvent;

/**
 * 
 * Description:<br>
 * MultiUserEvent fired at close/open or hide/show forum thread, 
 * or add/delete thread.
 * 
 * <P>
 * Initial Date:  09.07.2009 <br>
 * @author Lavinia Dumitrescu
 */
public class ForumChangedEvent extends MultiUserEvent {

	private static final long serialVersionUID = -6798225990538608024L;
	
	public static final String SPLIT = "split";
	public static final String CLOSE = "close";
	public static final String OPEN = "open";
	public static final String HIDE = "hide";
	public static final String SHOW = "show";
	public static final String NEW = "new";
	public static final String STICKY = "sticky";
	public static final String NEW_MESSAGE = "new-message";
	public static final String CHANGED_MESSAGE = "changed-message";
	public static final String DELETED_MESSAGE = "deleted-message";
	public static final String DELETED_THREAD = "deleted-thread";
	
	private Long threadtopKey;
	private Long messageKey;
	private Long sendByIdentityKey;

	public ForumChangedEvent(String command, Long threadtopKey, Long messageKey, Identity sendByIdentity) {
		super(command);	
		this.threadtopKey = threadtopKey;
		this.messageKey = messageKey;
		if(sendByIdentity != null) {
			this.sendByIdentityKey = sendByIdentity.getKey();
		}
	}

	public Long getThreadtopKey() {
		return threadtopKey;
	}

	public Long getMessageKey() {
		return messageKey;
	}

	public Long getSendByIdentityKey() {
		return sendByIdentityKey;
	}

	public void setSendByIdentityKey(Long sendByIdentityKey) {
		this.sendByIdentityKey = sendByIdentityKey;
	}
}

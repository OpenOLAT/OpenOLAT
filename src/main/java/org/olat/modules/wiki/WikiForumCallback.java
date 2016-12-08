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

package org.olat.modules.wiki;

import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.modules.fo.ForumCallback;

/**
 * Description:<br>
 * Security callback for enabling disabling some functions in the forums used in the wiki
 * 
 * <P>
 * Initial Date:  May 24, 2006 <br>
 * @author guido
 */
public class WikiForumCallback implements ForumCallback {

	private boolean isGuestOnly;
	private boolean isModerator;
	private SubscriptionContext context;

	public WikiForumCallback(boolean isGuestOnly, boolean isModerator, SubscriptionContext context) {
		this.isGuestOnly = isGuestOnly;
		this.isModerator = isModerator;
		this.context = context;
	}

	@Override
	public boolean mayUsePseudonym() {
		return false;
	}

	@Override
	public boolean mayOpenNewThread() {
		return !isGuestOnly ;
	}

	@Override
	public boolean mayReplyMessage() {
		return !isGuestOnly;
	}
	
	@Override
	public boolean mayEditOwnMessage() {
		return!isGuestOnly;
	}

	@Override
	public boolean mayDeleteOwnMessage() {
		return !isGuestOnly;
	}

	@Override
	public boolean mayEditMessageAsModerator() {		
		return !isGuestOnly && isModerator;
	}

	@Override
	public boolean mayDeleteMessageAsModerator() {		
		return !isGuestOnly && isModerator;
	}

	@Override
	public boolean mayArchiveForum() {
		return !isGuestOnly;
	}

	@Override
	public boolean mayFilterForUser() {
		return !isGuestOnly && isModerator;
	}

	@Override
	public SubscriptionContext getSubscriptionContext() {
		return context;
	}
}

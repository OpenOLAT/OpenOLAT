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

package org.olat.modules.fo;

import org.olat.core.util.notifications.SubscriptionContext;
/**
 * @author schneider
 */
public class DemoForumCallback implements ForumCallback {

	/* (non-Javadoc)
	 * @see org.olat.modules.fo.ForumCallback#mayOpenNewThread()
	 */
	public boolean mayOpenNewThread() {
		return true;
	}
	
	public boolean mayReplyMessage() {
		return true;
	}

	public boolean mayEditMessageAsModerator() {
		
		return true;
	}
	
	public boolean mayDeleteMessageAsModerator() {

		return false;
	}
	
	public boolean mayArchiveForum() {
		
		return true;
	}

	public boolean mayFilterForUser() {
		return true;
	}

	/**
	 * @see org.olat.modules.fo.ForumCallback#getSubscriptionContext()
	 */
	public SubscriptionContext getSubscriptionContext() {
		return null;
	}
}

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

package org.olat.course.nodes.dialog;

import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.modules.fo.ForumCallback;

/**
 * 
 * Initial date: 25 nov. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReadOnlyDialogNodeForumCallback implements ForumCallback {

	private NodeEvaluation ne;
	private boolean isOlatAdmin;
	private boolean isGuestOnly;
	private final SubscriptionContext subscriptionContext;

	/**
	 * @param ne the nodeevaluation for this coursenode
	 * @param isOlatAdmin true if the user is olat-admin
	 * @param isGuestOnly true if the user is olat-guest
	 * @param subscriptionContext
	 */
	public ReadOnlyDialogNodeForumCallback(NodeEvaluation ne, boolean isOlatAdmin, boolean isGuestOnly, SubscriptionContext subscriptionContext) {
		this.ne = ne;
		this.isOlatAdmin = isOlatAdmin;
		this.isGuestOnly = isGuestOnly;
		this.subscriptionContext = subscriptionContext;
	}

	@Override
	public boolean mayUsePseudonym() {
		return false;
	}

	/**
	 * @see org.olat.modules.fo.ForumCallback#mayOpenNewThread()
	 */
	@Override
	public boolean mayOpenNewThread() {
		return false;
	}

	/**
	 * @see org.olat.modules.fo.ForumCallback#mayReplyMessage()
	 */
	@Override
	public boolean mayReplyMessage() {
		return false;
	}

	@Override
	public boolean mayEditOwnMessage() {
		return false;
	}

	@Override
	public boolean mayDeleteOwnMessage() {
		return false;
	}

	/**
	 * @see org.olat.modules.fo.ForumCallback#mayEditMessageAsModerator()
	 */
	@Override
	public boolean mayEditMessageAsModerator() {
		return false;
	}

	/**
	 * @see org.olat.modules.fo.ForumCallback#mayDeleteMessageAsModerator()
	 */
	@Override
	public boolean mayDeleteMessageAsModerator() {
		return false;
	}

	/**
	 * 
	 * @see org.olat.modules.fo.ForumCallback#mayArchiveForum()
	 */
	@Override
	public boolean mayArchiveForum() {
		if (isGuestOnly) return false;
		else return true;
	}
	
	/**
	 * @see org.olat.modules.fo.ForumCallback#mayFilterForUser()
	 */
	@Override
	public boolean mayFilterForUser() {
		if (isGuestOnly) return false;
		return ne.isCapabilityAccessible("moderator") || isOlatAdmin;
	}

	/**
	 * @see org.olat.modules.fo.ForumCallback#getSubscriptionContext()
	 */
	@Override
	public SubscriptionContext getSubscriptionContext() {
		return (isGuestOnly ? null : subscriptionContext);
	}

}

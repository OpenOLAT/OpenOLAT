/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.fo;

import org.olat.core.commons.services.notifications.SubscriptionContext;

/**
 * Initial Date: 17.02.2017
 * @author fkiefer, fabian.kiefer@frentix.com, www.frentix.com
 */
public class ReadOnlyForumCallback implements ForumCallback {

	public ReadOnlyForumCallback() {
		//
	}


	@Override
	public boolean mayUsePseudonym() {
		return false;
	}

	@Override
	public boolean mayOpenNewThread() {
		return false;
	}

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

	@Override
	public boolean mayEditMessageAsModerator() {
		return false;
	}

	@Override
	public boolean mayDeleteMessageAsModerator() {
		return false;
	}
	@Override
	public boolean mayArchiveForum() {
		return false;
	}

	@Override
	public boolean mayFilterForUser() {
		return false;
	}

	@Override
	public SubscriptionContext getSubscriptionContext() {
		return null;
	}

}

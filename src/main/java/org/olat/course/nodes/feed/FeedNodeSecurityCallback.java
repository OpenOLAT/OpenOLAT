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
package org.olat.course.nodes.feed;

import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.modules.webFeed.FeedSecurityCallback;

/**
 * Feed node security callback based on course node access conditions and user
 * status (admin, guest)
 * 
 * <P>
 * Initial Date: Aug 10, 2009 <br>
 * 
 * @author gwassmann
 */
public class FeedNodeSecurityCallback implements FeedSecurityCallback {

	private NodeEvaluation ne;
	private boolean isAdministrator;
	private boolean isGuestOnly;
	private boolean isOwner;
	private SubscriptionContext subsContext;

	public FeedNodeSecurityCallback(NodeEvaluation ne, boolean isAdministrator, boolean isOwner, boolean isGuestOnly) {
		this.ne = ne;
		this.isAdministrator = isAdministrator;
		this.isGuestOnly = isGuestOnly;
		this.isOwner = isOwner;
	}
	
	@Override
	public boolean mayEditMetadata() {
		if (isGuestOnly) return false;
		return ne.isCapabilityAccessible("moderator") || isAdministrator;
	}

	@Override
	public boolean mayCreateItems() {
		if (isGuestOnly) return false;
		return ne.isCapabilityAccessible("poster") || ne.isCapabilityAccessible("moderator") || isAdministrator;
	}

	@Override
	public boolean mayDeleteItems() {
		if (isGuestOnly) return false;
		return ne.isCapabilityAccessible("moderator") || isAdministrator;
	}

	@Override
	public boolean mayDeleteOwnItems() {
		return true;
	}

	@Override
	public boolean mayEditItems() {
		if (isGuestOnly) return false;
		return ne.isCapabilityAccessible("moderator") || isAdministrator;
	}

	@Override
	public boolean mayEditOwnItems() {
		return true;
	}

	@Override
	public boolean mayViewAllDrafts() {
		return isOwner || isAdministrator;
	}

	@Override
	public SubscriptionContext getSubscriptionContext() {
		return subsContext;
	}

	@Override
	public void setSubscriptionContext(SubscriptionContext subsContext) {
		this.subsContext = subsContext;
	}	
}

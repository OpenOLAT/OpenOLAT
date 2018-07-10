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
package org.olat.modules.webFeed;

import org.olat.core.commons.services.notifications.SubscriptionContext;

/**
 * Feed resource security callback.
 * 
 * <P>
 * Initial Date: Aug 11, 2009 <br>
 * 
 * @author gwassmann
 */
public class FeedResourceSecurityCallback implements FeedSecurityCallback {

	private boolean isAdministrator;
	
	private SubscriptionContext subsContext;

	public FeedResourceSecurityCallback(boolean isAdministrator) {
		this.isAdministrator = isAdministrator;
	}

	@Override
	public boolean mayCreateItems() {
		return isAdministrator;
	}

	@Override
	public boolean mayDeleteItems() {
		return isAdministrator;
	}
	
	@Override
	public boolean mayDeleteOwnItems() {
		return true;
	}

	@Override
	public boolean mayEditItems() {
		return isAdministrator;
	}

	@Override
	public boolean mayEditOwnItems() {
		return true;
	}

	@Override
	public boolean mayEditMetadata() {
		return isAdministrator;
	}

	@Override
	public boolean mayViewAllDrafts() {
		return isAdministrator;
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

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
 * Initial Date:  Nov 28, 2006 <br>
 * @author guido
 */
public class WikiSecurityCallbackImpl implements WikiSecurityCallback {
	
	private Boolean courseEditRight;
	private boolean isAdministator;
	private boolean isGuestOnly;
	private boolean isGroupWiki;
	private boolean isResourceOwner;
	private SubscriptionContext subscriptionContext;

	public WikiSecurityCallbackImpl(Boolean courseEditRight, boolean isAdministator, 
			boolean isGuestOnly, boolean isGroupWiki, 
			boolean isResourceOwner, SubscriptionContext subscriptionContext){
		this.courseEditRight = courseEditRight;
		this.isAdministator = isAdministator;
		this.isGuestOnly = isGuestOnly;
		this.isGroupWiki  = isGroupWiki;
		this.isResourceOwner = isResourceOwner;
		this.subscriptionContext = subscriptionContext;
	}
	
	@Override
	public boolean mayEditAndCreateArticle() {
		if(isGuestOnly) return false;
		if(isGroupWiki || isAdministator) {
			return true;
		}
		if(courseEditRight != null && courseEditRight.booleanValue()) {
			return true;
		}
		//wiki is started from repo, and it's visible to this user, so creating pages is allowed
		return courseEditRight == null; 
	}
	
	@Override
	public boolean mayEditWikiMenu(){
		if(isGuestOnly) return false;
		return isGroupWiki || isAdministator || isResourceOwner;
	}
	
	@Override
	public SubscriptionContext getSubscriptionContext() {
		return (isGuestOnly ? null : subscriptionContext);
	}

	@Override
	public boolean mayModerateForum()	{
		return !isGuestOnly && (isAdministator || isResourceOwner);
	}

	@Override
	public ForumCallback getForumCallback() {
		boolean isModerator = mayModerateForum();
		return new WikiForumCallback(isGuestOnly, isModerator, subscriptionContext);
	}
}

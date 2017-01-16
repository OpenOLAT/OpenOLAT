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
 * 
 * Description:<br>
 * callback for security calls. Wiki page creation and editing a page are restricted and bypass this callback
 * 
 * <P>
 * Initial Date:  Feb 19, 2007 <br>
 * @author guido
 */
public interface WikiSecurityCallback {

	/**
	 * 
	 * @return true if admin or allowed by preconditions of used in 
	 * collaboration group context where everything is allowed
	 */
	public boolean mayEditAndCreateArticle();

	/**
	 * @return true if admin or resource owner or used in 
	 * collaboration group context where everything is allowed
	 */
	public boolean mayEditWikiMenu();
	
	/**
	 * @return the subscriptionContext. if null, then no subscription must be offered
	 */
	public SubscriptionContext getSubscriptionContext();
	
	/**
	 * 
	 * @return true if admin or resource owner.
	 */
	public boolean mayModerateForum();
	
	public ForumCallback getForumCallback();

}
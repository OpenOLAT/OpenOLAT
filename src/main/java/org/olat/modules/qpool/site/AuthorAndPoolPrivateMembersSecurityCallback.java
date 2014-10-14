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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.modules.qpool.site;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.navigation.SiteSecurityCallback;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionPoolModule;

/**
 * 
 * Initial date: 14.10.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AuthorAndPoolPrivateMembersSecurityCallback implements SiteSecurityCallback {
	
	private QPoolService qPoolService;
	private QuestionPoolModule questionPoolModule;
	
	/**
	 * [used by Spring]
	 * @param qPoolService
	 */
	public void setQPoolService(QPoolService qPoolService) {
		this.qPoolService = qPoolService;
	}
	
	/**
	 * [used by Spring]
	 * @param questionPoolModule
	 */
	public void setQuestionPoolModule(QuestionPoolModule questionPoolModule) {
		this.questionPoolModule = questionPoolModule;
	}

	/**
	 * @see com.frentix.olat.coursesite.SiteSecurityCallback#isAllowedToLaunchSite(org.olat.core.gui.UserRequest)
	 */
	@Override
	public boolean isAllowedToLaunchSite(UserRequest ureq) {
		if (!questionPoolModule.isEnabled() || ureq == null || ureq.getUserSession() == null || ureq.getUserSession().getRoles() == null
				|| ureq.getIdentity() == null
				|| ureq.getUserSession().getRoles().isInvitee() || ureq.getUserSession().getRoles().isGuestOnly()) {
			return false;
		}
		Roles roles = ureq.getUserSession().getRoles();
		if (roles.isOLATAdmin() || roles.isPoolAdmin() || roles.isAuthor()) {
			return true;
		}
		Identity identity = ureq.getIdentity();
		return qPoolService.isMemberOfPrivatePools(identity);
	}
}

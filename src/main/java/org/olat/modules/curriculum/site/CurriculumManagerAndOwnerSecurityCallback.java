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
package org.olat.modules.curriculum.site;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.navigation.SiteSecurityCallback;
import org.olat.core.id.Roles;
import org.olat.core.util.UserSession;
import org.olat.modules.curriculum.CurriculumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Course planners (curriculum managers), product (curriculum) and element owners
 * 
 * 
 * Initial date: 12 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Service("curriculumManagerAndOwnerSiteSecurityCallback")
public class CurriculumManagerAndOwnerSecurityCallback implements SiteSecurityCallback {
	
	@Autowired
	private CurriculumService curriculumService;

	@Override
	public boolean isAllowedToLaunchSite(UserRequest ureq) {
		UserSession usess = ureq.getUserSession();
		if(usess == null ) return false;
		
		Roles roles = usess.getRoles();
		return roles != null && (roles.isAdministrator() || roles.isCurriculumManager()
				|| curriculumService.isCurriculumOrElementOwner(ureq.getIdentity()));
	}
}

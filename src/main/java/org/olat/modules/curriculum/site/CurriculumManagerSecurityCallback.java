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
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.UserSession;
import org.olat.modules.certificationprogram.CertificationProgramService;
import org.olat.modules.curriculum.CurriculumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 12 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Service("curriculumManagerSiteSecurityCallback")
public class CurriculumManagerSecurityCallback implements SiteSecurityCallback {
	
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private CertificationProgramService certificationProgramService;

	@Override
	public boolean isAllowedToLaunchSite(UserRequest ureq) {
		UserSession usess = ureq.getUserSession();
		if(usess == null ) return false;
		Roles roles = usess.getRoles();
		if(roles == null || roles.isGuestOnly()) return false;
		
		Identity id = ureq.getIdentity();
		return roles.isAdministrator() || roles.isPrincipal() || roles.isCurriculumManager()
				|| curriculumService.isCurriculumOwner(id)
				|| certificationProgramService.isCertificationProgramOwner(id);
	}
}

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
package org.olat.modules.coach.site;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.navigation.AbstractSiteDefinition;
import org.olat.core.gui.control.navigation.SiteConfiguration;
import org.olat.core.gui.control.navigation.SiteDefinition;
import org.olat.core.gui.control.navigation.SiteInstance;
import org.olat.core.id.Roles;
import org.olat.core.util.UserSession;
import org.olat.modules.coach.CoachingModule;
import org.olat.modules.coach.CoachingService;
import org.olat.modules.coach.model.CoachingSecurity;
import org.olat.modules.grading.GradingService;
import org.olat.modules.grading.model.GradingSecurity;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  7 févr. 2012 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class CoachSiteDef extends AbstractSiteDefinition implements SiteDefinition {

	@Override
	protected SiteInstance createSite(UserRequest ureq, WindowControl wControl, SiteConfiguration config) {
		UserSession usess = ureq.getUserSession();
		if(usess == null || usess.getRoles() == null || usess.getRoles().isGuestOnly()) {
			return null;
		}

		// these are the minimal security
		Roles roles = usess.getRoles();
		if(roles.isAdministrator() || roles.isPrincipal() || roles.isLearnResourceManager() || roles.isLectureManager()) {
			return new CoachSite(this, null, null, ureq.getLocale());
		}	
		CoachingSecurity coachingSec = CoreSpringFactory.getImpl(CoachingService.class).isCoach(ureq.getIdentity());
		GradingSecurity gradingSec = CoreSpringFactory.getImpl(GradingService.class).isGrader(ureq.getIdentity(), roles);
		if(coachingSec.isCoach() || coachingSec.isMasterCoachForLectures() || coachingSec.isTeacher() || coachingSec.isUserRelationSource()
				|| gradingSec.isGrader() || gradingSec.isGradedResourcesManager()) {
			return new CoachSite(this, coachingSec, gradingSec, ureq.getLocale());
		}
		return null;
	}
	
	@Override
	public boolean isEnabled() {
		CoachingModule module = CoreSpringFactory.getImpl(CoachingModule.class);
		return module.isEnabled() && super.isEnabled();
	}

	@Override
	public boolean isFeatureEnabled() {
		CoachingModule module = CoreSpringFactory.getImpl(CoachingModule.class);
		return module.isEnabled();
	}
}

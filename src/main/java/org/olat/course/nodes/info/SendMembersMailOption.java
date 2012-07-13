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

package org.olat.course.nodes.info;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.commons.info.ui.SendMailOption;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.course.ICourse;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.group.BusinessGroup;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;

/**
 * 
 * Description:<br>
 * Send mails to members, coaches and owner of the course
 * 
 * <P>
 * Initial Date:  29 jul. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class SendMembersMailOption implements SendMailOption {
	
	private final ICourse course;
	private final RepositoryManager rm;
	
	public SendMembersMailOption(ICourse course, RepositoryManager rm) {
		this.course = course;
		this.rm = rm;
	}

	@Override
	public String getOptionKey() {
		return "send-mail-course-members";
	}

	@Override
	public String getOptionTranslatedName(Locale locale) {
		Translator translator = Util.createPackageTranslator(SendMembersMailOption.class, locale);
		return translator.translate("wizard.step1.send_option.member");
	}

	@Override
	public List<Identity> getSelectedIdentities() {
		Set<Identity> identities = new HashSet<Identity>();
		CourseGroupManager cgm = course.getCourseEnvironment().getCourseGroupManager();
		List<BusinessGroup> learningGroups = cgm.getAllBusinessGroups();
		for(BusinessGroup bg:learningGroups) {
			List<Identity> participants = cgm.getParticipantsFromBusinessGroup(bg.getName());
			identities.addAll(participants);
			List<Identity> coaches = cgm.getCoachesFromBusinessGroup(bg.getName());
			identities.addAll(coaches);
		}
		//fxdiff VCRP-1,2: access control of resources
		RepositoryEntry repositoryEntry = rm.lookupRepositoryEntry(course, true);
		BaseSecurity securityManager = BaseSecurityManager.getInstance();
		if(repositoryEntry.getParticipantGroup() != null) {
			identities.addAll(securityManager.getIdentitiesOfSecurityGroup(repositoryEntry.getParticipantGroup()));
		}
		if(repositoryEntry.getTutorGroup() != null) {
			identities.addAll(securityManager.getIdentitiesOfSecurityGroup(repositoryEntry.getTutorGroup()));
		}
		if(repositoryEntry.getOwnerGroup() != null) {
			identities.addAll(securityManager.getIdentitiesOfSecurityGroup(repositoryEntry.getOwnerGroup()));
		}
		return new ArrayList<Identity>(identities);
	}
}

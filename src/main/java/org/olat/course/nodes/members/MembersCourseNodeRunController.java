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
package org.olat.course.nodes.members;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.BaseSecurity;
import org.olat.commons.memberlist.manager.MembersExportManager;
import org.olat.commons.memberlist.model.CurriculumMemberInfos;
import org.olat.commons.memberlist.ui.MembersDisplayRunController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.nodes.MembersCourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.curriculum.CurriculumModule;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * The run controller show the list of members of the course
 * 
 * <P>
 * Initial Date:  11 mars 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 * @author fkiefer
 */
public class MembersCourseNodeRunController extends BasicController {
	
	private MembersDisplayRunController membersDisplayRunController;
	
	@Autowired
	private MembersManager membersManager;
	@Autowired
	protected BaseSecurity securityManager;
	@Autowired
	private CurriculumModule curriculumModule;
	@Autowired
	private MembersExportManager exportManager;
	
	public MembersCourseNodeRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, ModuleConfiguration config) {
		super(ureq, wControl);
		
		CourseEnvironment courseEnv = userCourseEnv.getCourseEnvironment();
		RepositoryEntry courseRepositoryEntry = courseEnv.getCourseGroupManager().getCourseEntry();

		boolean showOwners = config.getBooleanSafe(MembersCourseNode.CONFIG_KEY_SHOWOWNER);
		
		MembersCourseNodeConfiguration nodeConfig = (MembersCourseNodeConfiguration)CourseNodeFactory.getInstance().getCourseNodeConfiguration("cmembers");
		boolean deduplicateList = nodeConfig.isDeduplicateList();
		
		String emailFct = config.getStringValue(MembersCourseNode.CONFIG_KEY_EMAIL_FUNCTION, MembersCourseNode.EMAIL_FUNCTION_COACH_ADMIN);
		boolean canEmail = MembersCourseNode.EMAIL_FUNCTION_ALL.equals(emailFct) || userCourseEnv.isAdmin() || userCourseEnv.isCoach();
		
		String downloadFct = config.getStringValue(MembersCourseNode.CONFIG_KEY_DOWNLOAD_FUNCTION, MembersCourseNode.EMAIL_FUNCTION_COACH_ADMIN);
		boolean canDownload = MembersCourseNode.EMAIL_FUNCTION_ALL.equals(downloadFct) || userCourseEnv.isAdmin() || userCourseEnv.isCoach();

		List<Identity> owners;
		if(showOwners) {
			owners = membersManager.getOwners(courseRepositoryEntry);
		} else {
			owners = Collections.emptyList();
		}

		List<Identity> coaches;
		boolean showCoaches = false;
		if(config.anyTrue(MembersCourseNode.CONFIG_KEY_COACHES_ALL, MembersCourseNode.CONFIG_KEY_COACHES_COURSE)		
				|| config.hasAnyOf(MembersCourseNode.CONFIG_KEY_COACHES_GROUP, MembersCourseNode.CONFIG_KEY_COACHES_AREA,
						MembersCourseNode.CONFIG_KEY_COACHES_CUR_ELEMENT)) {
			coaches = membersManager.getCoaches(courseRepositoryEntry, config);
			showCoaches = true;
		} else {
			coaches = Collections.emptyList();
		}

		List<Identity> participants;
		boolean showParticipants = false;
		if(config.anyTrue(MembersCourseNode.CONFIG_KEY_PARTICIPANTS_ALL, MembersCourseNode.CONFIG_KEY_PARTICIPANTS_COURSE)
				|| config.hasAnyOf(MembersCourseNode.CONFIG_KEY_PARTICIPANTS_GROUP, MembersCourseNode.CONFIG_KEY_PARTICIPANTS_AREA,
						MembersCourseNode.CONFIG_KEY_PARTICIPANTS_CUR_ELEMENT)) {
			participants = membersManager.getParticipants(courseRepositoryEntry, config);
			showParticipants = true;
		} else {
			participants = Collections.emptyList();
		}
		
		Map<Long,CurriculumMemberInfos> curriculumInfos = null;
		if(curriculumModule.isEnabled()) {
			curriculumInfos = exportManager.getCurriculumMemberInfos(courseRepositoryEntry);
		}
		
		membersDisplayRunController = new MembersDisplayRunController(ureq, wControl, getTranslator(), userCourseEnv, null,
				owners, coaches, participants, new ArrayList<>(), curriculumInfos, canEmail, canDownload, deduplicateList,
				showOwners, showCoaches, showParticipants, false, true);
		listenTo(membersDisplayRunController);
		
		putInitialPanel(membersDisplayRunController.getInitialComponent());
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}

	
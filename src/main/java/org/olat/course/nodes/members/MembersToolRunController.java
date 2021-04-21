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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.olat.commons.memberlist.manager.MembersExportManager;
import org.olat.commons.memberlist.model.CurriculumMemberInfos;
import org.olat.commons.memberlist.ui.MembersDisplayRunController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.curriculum.CurriculumModule;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 6 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class MembersToolRunController extends BasicController {

private MembersDisplayRunController membersDisplayRunController;

	@Autowired
	private MembersManager membersManager;
	@Autowired
	private CurriculumModule curriculumModule;
	@Autowired
	private MembersExportManager exportManager;
	
	public MembersToolRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv) {
		super(ureq, wControl);
		
		CourseEnvironment courseEnv = userCourseEnv.getCourseEnvironment();
		RepositoryEntry courseRepositoryEntry = courseEnv.getCourseGroupManager().getCourseEntry();
		
		List<Identity> owners = membersManager.getOwners(courseRepositoryEntry);
		List<Identity> coaches = membersManager.getCoaches(courseRepositoryEntry, null);
		List<Identity> participants = membersManager.getParticipants(courseRepositoryEntry, null);
		List<Identity> waiting = Collections.emptyList();
		
		Map<Long,CurriculumMemberInfos> curriculumInfos = null;
		if(curriculumModule.isEnabled()) {
			curriculumInfos = exportManager.getCurriculumMemberInfos(courseRepositoryEntry);
		}
		
		boolean canEmail = true;
		boolean canDownload = false;
		boolean deduplicateList = true;
		boolean showOwners = true;
		boolean showCoaches = true;
		boolean showParticipants = true;
		boolean showWaiting = false;
		boolean editable = true;
		membersDisplayRunController = new MembersDisplayRunController(ureq, wControl, getTranslator(), userCourseEnv,
				null, owners, coaches, participants, waiting, curriculumInfos, canEmail, canDownload, deduplicateList,
				showOwners, showCoaches, showParticipants, showWaiting, editable);
		listenTo(membersDisplayRunController);
		
		putInitialPanel(membersDisplayRunController.getInitialComponent());
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
}

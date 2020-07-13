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
package org.olat.course.nodes.ms;

import java.util.List;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.course.groupsandrights.CourseRights;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.modules.assessment.ui.AssessmentToolContainer;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.RepositoryManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 Jun 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class MSCoachRunController extends BasicController {

	private MSIdentityListCourseNodeController identitityListCtrl;
	
	@Autowired
	private RepositoryManager repositoryManager;

	public MSCoachRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, MSCourseNode msCourseNode) {
		super(ureq, wControl);

		TooledStackedPanel stackPanel = new TooledStackedPanel("msCoachStackPanel", getTranslator(), this);
		stackPanel.setToolbarAutoEnabled(false);
		stackPanel.setToolbarEnabled(false);
		stackPanel.setShowCloseLink(true, false);
		stackPanel.setCssClass("o_ms_stack_panel");
		putInitialPanel(stackPanel);
		
		GroupRoles role = userCourseEnv.isCoach()? GroupRoles.coach: GroupRoles.owner;
		// see CourseRuntimeController.doAssessmentTool(ureq);
		boolean hasAssessmentRight = userCourseEnv.getCourseEnvironment().getCourseGroupManager()
				.hasRight(getIdentity(), CourseRights.RIGHT_ASSESSMENT, role);

		RepositoryEntry courseEntry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		RepositoryEntrySecurity reSecurity = repositoryManager.isAllowed(ureq, courseEntry);
		boolean admin = userCourseEnv.isAdmin() || hasAssessmentRight;

		boolean nonMembers = reSecurity.isEntryAdmin();
		List<BusinessGroup> coachedGroups = null;
		if (reSecurity.isGroupCoach()) {
			coachedGroups = userCourseEnv.getCoachedGroups();
		}
		AssessmentToolSecurityCallback secCallBack = new AssessmentToolSecurityCallback(admin, nonMembers,
				reSecurity.isCourseCoach(), reSecurity.isGroupCoach(), reSecurity.isCurriculumCoach(), coachedGroups);

		identitityListCtrl = new MSIdentityListCourseNodeController(ureq, wControl, stackPanel, courseEntry, null,
				msCourseNode, userCourseEnv, new AssessmentToolContainer(), secCallBack, false);
		listenTo(identitityListCtrl);
		identitityListCtrl.activate(ureq, null, null);
		
		stackPanel.pushController(translate("breadcrumb.users"), identitityListCtrl);
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

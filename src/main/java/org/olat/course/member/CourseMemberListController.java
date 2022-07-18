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
package org.olat.course.member;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.ui.tool.AssessmentIdentityCourseController;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.ui.main.AbstractMemberListController;
import org.olat.group.ui.main.MemberListSecurityCallback;
import org.olat.group.ui.main.MemberRow;
import org.olat.group.ui.main.SearchMembersParams;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class CourseMemberListController extends AbstractMemberListController {
	
	private AssessmentIdentityCourseController identityAssessmentController;
	
	private final SearchMembersParams searchParams;
	private final UserCourseEnvironment coachCourseEnv;
	
	public CourseMemberListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbarPanel,
			RepositoryEntry repoEntry, UserCourseEnvironment coachCourseEnv, MemberListSecurityCallback secCallback, SearchMembersParams searchParams, String infos) {
		super(ureq, wControl, repoEntry, "member_list_origin_filter", secCallback, toolbarPanel);
		this.searchParams = searchParams;
		this.coachCourseEnv = coachCourseEnv;
		
		if(StringHelper.containsNonWhitespace(infos)) {
			flc.contextPut("infos", infos);
		}
	}
	
	@Override
	protected void doOpenAssessmentTool(UserRequest ureq, MemberRow member) {
		removeAsListenerAndDispose(identityAssessmentController);
		
		Identity assessedIdentity = securityManager.loadIdentityByKey(member.getIdentityKey());
		identityAssessmentController = new AssessmentIdentityCourseController(ureq, getWindowControl(), toolbarPanel,
				repoEntry, coachCourseEnv, assessedIdentity, true);
		listenTo(identityAssessmentController);
		
		String displayName = userManager.getUserDisplayName(assessedIdentity);
		toolbarPanel.pushController(displayName, identityAssessmentController);
	}

	@Override
	public SearchMembersParams getSearchParams() {
		return searchParams;
	}
}
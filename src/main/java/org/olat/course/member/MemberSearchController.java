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
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
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
public class MemberSearchController extends AbstractMemberListController {
	
	private final UserCourseEnvironment coachCourseEnv;
	private SearchMembersParams searchParams = new SearchMembersParams();
	
	private MemberSearchForm searchForm;
	private AssessmentIdentityCourseController identityAssessmentController;
	
	public MemberSearchController(UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbarPanel,
			RepositoryEntry repoEntry, UserCourseEnvironment coachCourseEnv, MemberListSecurityCallback secCallback) {
		super(ureq, wControl, repoEntry, "all_member_list", secCallback, toolbarPanel);
		this.coachCourseEnv = coachCourseEnv;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		super.initForm(formLayout, listener, ureq);
		
		searchForm = new MemberSearchForm(ureq, getWindowControl(), mainForm);
		searchForm.setEnabled(true);
		listenTo(searchForm);
		membersTable.setSearchEnabled(true);
		membersTable.setExtendedSearch(searchForm);
		membersTable.expandExtendedSearch(ureq);
	}

	@Override
	public SearchMembersParams getSearchParams() {
		return searchParams;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == searchForm) {
			if(event instanceof SearchMembersParams) {
				searchParams = (SearchMembersParams)event;
				reloadModel();
			}
		}
		super.event(ureq, source, event);
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
}

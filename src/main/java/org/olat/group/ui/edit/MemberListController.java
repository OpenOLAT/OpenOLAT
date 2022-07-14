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
package org.olat.group.ui.edit;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.WindowControl;
import org.olat.group.BusinessGroup;
import org.olat.group.ui.main.AbstractMemberListController;
import org.olat.group.ui.main.MemberListSecurityCallback;
import org.olat.group.ui.main.MemberRow;
import org.olat.group.ui.main.SearchMembersParams;

/**
 * The list of members specific to the business groups.
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class MemberListController extends AbstractMemberListController {
	
	private final SearchMembersParams searchParams;
	
	public MemberListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			BusinessGroup group, SearchMembersParams searchParams, MemberListSecurityCallback secCallback) {
		super(ureq, wControl, group, "all_member_list", secCallback, stackPanel);
		this.searchParams = searchParams;
	}
	
	@Override
	protected void doOpenAssessmentTool(UserRequest ureq, MemberRow member) {
		//
	}

	@Override
	public SearchMembersParams getSearchParams() {
		return searchParams;
	}
}

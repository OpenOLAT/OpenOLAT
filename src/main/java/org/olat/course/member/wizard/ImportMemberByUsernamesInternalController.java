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
package org.olat.course.member.wizard;

import java.util.List;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.model.FindNamedIdentityCollection;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.course.member.MemberSearchConfig;
import org.olat.group.manager.MemberViewQueries;
import org.olat.group.ui.main.SearchMembersParams;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Restrict the list to course members.
 * 
 * Initial date: 19 août 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ImportMemberByUsernamesInternalController extends ImportMemberByUsernamesController {

	private final MemberSearchConfig config;
	
	@Autowired
	private MemberViewQueries memberQueries;

	public ImportMemberByUsernamesInternalController(UserRequest ureq, WindowControl wControl, Form rootForm,
			StepsRunContext runContext, String runContextKey, MemberSearchConfig config) {
		super(ureq, wControl, rootForm, runContext, runContextKey, null);
		this.config = config;
	}
	
	@Override
	protected FindNamedIdentityCollection findNamedIdentityCollection(List<String> identList) {
		SearchMembersParams params = new SearchMembersParams();
		params.setSearchAsRole(getIdentity(), config.searchAsRole());
		params.setPending(true);
		params.setRoles(new GroupRoles[] { GroupRoles.owner, GroupRoles.coach, GroupRoles.participant, GroupRoles.waiting } );

		RepositoryEntry entry = config.repositoryEntry();
		return memberQueries.findNamedRepositoryEntryMembers(identList, entry, params);
	}
}
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
import java.util.Set;

import org.olat.basesecurity.GroupRoles;
import org.olat.commons.info.ui.SendMailOption;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryService;

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
	
	private final String label;
	private final GroupRoles role;
	private final RepositoryEntry repositoryEntry;
	
	public SendMembersMailOption(RepositoryEntry repositoryEntry, GroupRoles role, String label) {
		this.role = role;
		this.label = label;
		this.repositoryEntry = repositoryEntry;
	}

	@Override
	public String getOptionKey() {
		return "send-mail-course-members-" + role.name();
	}

	@Override
	public String getOptionName() {
		return label;
	}

	@Override
	public List<Identity> getSelectedIdentities() {
		Set<Identity> identities = new HashSet<>();
		List<Identity> reMembers = CoreSpringFactory.getImpl(RepositoryService.class)
				.getMembers(repositoryEntry, RepositoryEntryRelationType.all, role.name());
		identities.addAll(reMembers);
		return new ArrayList<>(identities);
	}
}

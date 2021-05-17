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
package org.olat.course.nodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.appointments.OrganizerCandidateSupplier;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryService;

/**
 * 
 * Initial date: 14 May 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CourseNodeOrganizerCandidateSupplier implements OrganizerCandidateSupplier {
	
	private final RepositoryEntry entry;
	private final String[] roles;
	
	private RepositoryService repositoryService;

	public CourseNodeOrganizerCandidateSupplier(RepositoryEntry entry, AppointmentsCourseNode courseNode) {
		this.entry = entry;
		ModuleConfiguration config = courseNode.getModuleConfiguration();
		List<String> roleList = new ArrayList<>(2);
		if (config.getBooleanSafe(AppointmentsCourseNode.CONFIG_KEY_ORGANIZER_OWNER)) {
			roleList.add(GroupRoles.owner.name());
		}
		if (config.getBooleanSafe(AppointmentsCourseNode.CONFIG_KEY_ORGANIZER_COACH)) {
			roleList.add(GroupRoles.coach.name());
		}
		roles = roleList.toArray(String[]::new);
	}

	@Override
	public List<Identity> getOrganizerCandidates() {
		return roles.length > 0
				? getRepositoryService().getMembers(entry, RepositoryEntryRelationType.all, roles)
				: Collections.emptyList();
	}

	private RepositoryService getRepositoryService() {
		if (repositoryService == null) {
			repositoryService = CoreSpringFactory.getImpl(RepositoryService.class);
		}
		return repositoryService;
	}

}

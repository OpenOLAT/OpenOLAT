/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.topicbroker;

import java.util.List;
import java.util.stream.Collectors;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.modules.topicbroker.TBParticipantCandidates;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryService;

/**
 * 
 * Initial date: 17 Jun 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TopicBrokerCourseNodeParticipantCandidates implements TBParticipantCandidates {
	
	private final RepositoryService repositoryService;
	private final Identity doer;
	private final RepositoryEntry repositoryEntry;
	private final boolean admin;
	private List<Identity> allIdentities;
	private List<Identity> visibleIdentities;
	
	public TopicBrokerCourseNodeParticipantCandidates(Identity doer,
			RepositoryEntry repositoryEntry, boolean admin) {
		this.repositoryService = CoreSpringFactory.getImpl(RepositoryService.class);
		this.doer = doer;
		this.repositoryEntry = repositoryEntry;
		this.admin = admin;
	}

	@Override
	public List<Identity> getAllIdentities() {
		if (allIdentities == null) {
			allIdentities = repositoryService.getMembers(repositoryEntry, RepositoryEntryRelationType.all, GroupRoles.participant.name())
					.stream()
					.distinct()
					.collect(Collectors.toList());
		}
		return allIdentities;
	}

	@Override
	public boolean isAllIdentitiesVisible() {
		return admin;
	}

	@Override
	public List<Identity> getVisibleIdentities() {
		if (visibleIdentities == null) {
			visibleIdentities = admin
					? getAllIdentities()
					: repositoryService.getCoachedParticipants(doer, repositoryEntry);
		}
		return visibleIdentities;
	}

	@Override
	public void refresh() {
		allIdentities = null;
		visibleIdentities = null;
	}

}

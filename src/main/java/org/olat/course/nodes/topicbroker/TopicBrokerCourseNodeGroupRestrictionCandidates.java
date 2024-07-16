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

import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.CoreSpringFactory;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupOrder;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.modules.topicbroker.TBGroupRestrictionCandidates;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 15 Jul 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TopicBrokerCourseNodeGroupRestrictionCandidates implements TBGroupRestrictionCandidates {
	
	private final RepositoryEntryRef repositoryEntry;
	private Set<Long> businessGrupKeys;

	public TopicBrokerCourseNodeGroupRestrictionCandidates(RepositoryEntryRef repositoryEntry) {
		this.repositoryEntry = repositoryEntry;
	}

	@Override
	public Set<Long> getBusinessGroupKeys() {
		if (businessGrupKeys == null) {
			BusinessGroupService businessGroupService = CoreSpringFactory.getImpl(BusinessGroupService.class);
			SearchBusinessGroupParams params = new SearchBusinessGroupParams();
			businessGrupKeys = businessGroupService.findBusinessGroups(params, repositoryEntry, 0, -1, BusinessGroupOrder.nameAsc)
					.stream()
					.map(BusinessGroup::getKey)
					.collect(Collectors.toSet());
		}
		return businessGrupKeys;
	}

}

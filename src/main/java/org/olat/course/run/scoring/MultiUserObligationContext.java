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
package org.olat.course.run.scoring;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.OrganisationRef;
import org.olat.group.BusinessGroupRef;
import org.olat.group.BusinessGroupService;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumService;

/**
 * 
 * Initial date: 2 Sep 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class MultiUserObligationContext implements ObligationContext {
	
	private Map<Long, Set<Long>> businessGroupKeyToParticipantIdentityKeys;
	private Map<Long, Set<Long>> organisationKeyToMembersIdentityKeys;
	private Map<Long, Set<Long>> curriculumElementKeyToParticipantIdentityKeys;
	
	private BusinessGroupService businessGroupService;
	private OrganisationService organisationService;
	private CurriculumService curriculumService;

	@Override
	public boolean isParticipant(Identity identity, BusinessGroupRef businessGroupRef) {
		if (businessGroupKeyToParticipantIdentityKeys == null) {
			businessGroupKeyToParticipantIdentityKeys = new HashMap<>();
		}
		businessGroupKeyToParticipantIdentityKeys.computeIfAbsent(businessGroupRef.getKey(),
				key -> getBusinessGroupService()
					.getMemberKeys(businessGroupRef, GroupRoles.participant.name())
					.stream().collect(Collectors.toSet()));
		
		return businessGroupKeyToParticipantIdentityKeys.get(businessGroupRef.getKey()).contains(identity.getKey());
	}
	
	private BusinessGroupService getBusinessGroupService() {
		if (businessGroupService == null) {
			businessGroupService = CoreSpringFactory.getImpl(BusinessGroupService.class);
		}
		return businessGroupService;
	}

	@Override
	public boolean isMember(Identity identity, OrganisationRef organisationRef) {
		if (organisationKeyToMembersIdentityKeys == null) {
			organisationKeyToMembersIdentityKeys = new HashMap<>();
		}
		organisationKeyToMembersIdentityKeys.computeIfAbsent(organisationRef.getKey(),
				key -> getOrganisationService()
						.getMemberKeys(organisationRef, OrganisationRoles.valuesWithoutGuestAndInvitee())
						.stream().collect(Collectors.toSet()));
		
		return organisationKeyToMembersIdentityKeys.get(organisationRef.getKey()).contains(identity.getKey());
	}
	
	private OrganisationService getOrganisationService() {
		if (organisationService == null) {
			organisationService = CoreSpringFactory.getImpl(OrganisationService.class);
		}
		return organisationService;
	}

	@Override
	public boolean isParticipant(Identity identity, CurriculumElementRef curriculumElementRef) {
		if (curriculumElementKeyToParticipantIdentityKeys == null) {
			curriculumElementKeyToParticipantIdentityKeys = new HashMap<>();
		}
		curriculumElementKeyToParticipantIdentityKeys.computeIfAbsent(curriculumElementRef.getKey(),
				key -> getCurriculumService()
						.getMemberKeys(Collections.singletonList(curriculumElementRef), GroupRoles.participant.name())
						.stream().collect(Collectors.toSet()));
		
		return curriculumElementKeyToParticipantIdentityKeys.get(curriculumElementRef.getKey()).contains(identity.getKey());
	}
	
	private CurriculumService getCurriculumService() {
		if (curriculumService == null) {
			curriculumService = CoreSpringFactory.getImpl(CurriculumService.class);
		}
		return curriculumService;
	}

}

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
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.OrganisationRef;
import org.olat.core.logging.AssertException;
import org.olat.group.BusinessGroupRef;
import org.olat.group.BusinessGroupService;
import org.olat.modules.curriculum.CurriculumElementMembership;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumService;

/**
 * 
 * Initial date: 2 Sep 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SingleUserObligationContext implements ObligationContext {
	
	private Identity contextIdentity;
	private Map<Long, Boolean> businessGroupKeyToParticipant;
	private Map<Long, Boolean> organisationKeyToMember;
	private Map<Long, Boolean> curriculumElementToParticipant;
	
	private BusinessGroupService businessGroupService;
	private OrganisationService organisationService;
	private CurriculumService curriculumService;
	
	private void checkIdentity(Identity identity) {
		if (contextIdentity == null) {
			contextIdentity = identity;
		}
		if (!contextIdentity.getKey().equals(identity.getKey())) {
			throw new AssertException("SingleUserObligationContext is only for one user.");
		}
	}

	@Override
	public boolean isParticipant(Identity identity, BusinessGroupRef businessGroupRef) {
		checkIdentity(identity);
		
		if (businessGroupKeyToParticipant == null) {
			businessGroupKeyToParticipant = new HashMap<>();
		}
		businessGroupKeyToParticipant.computeIfAbsent(businessGroupRef.getKey(), key -> {
			boolean participant = getBusinessGroupService().hasRoles(identity, businessGroupRef, GroupRoles.participant.name());
			return Boolean.valueOf(participant);
		});
		
		return businessGroupKeyToParticipant.get(businessGroupRef.getKey()).booleanValue();
	}
	
	private BusinessGroupService getBusinessGroupService() {
		if (businessGroupService == null) {
			businessGroupService = CoreSpringFactory.getImpl(BusinessGroupService.class);
		}
		return businessGroupService;
	}

	@Override
	public boolean isMember(Identity identity, OrganisationRef organisationRef) {
		checkIdentity(identity);
		
		if (organisationKeyToMember == null) {
			organisationKeyToMember = new HashMap<>();
		}
		organisationKeyToMember.computeIfAbsent(organisationRef.getKey(), key -> {
			boolean member = getOrganisationService().hasRole(identity, organisationRef, OrganisationRoles.valuesWithoutGuestAndInvitee());
			return Boolean.valueOf(member);
		});
		
		return organisationKeyToMember.get(organisationRef.getKey()).booleanValue();
	}
	
	private OrganisationService getOrganisationService() {
		if (organisationService == null) {
			organisationService = CoreSpringFactory.getImpl(OrganisationService.class);
		}
		return organisationService;
	}

	@Override
	public boolean isParticipant(Identity identity, CurriculumElementRef curriculumElementRef) {
		checkIdentity(identity);
		
		if (curriculumElementToParticipant == null) {
			curriculumElementToParticipant = new HashMap<>();
		}
		curriculumElementToParticipant.computeIfAbsent(curriculumElementRef.getKey(), key -> {
			List<CurriculumElementMembership> memberships = getCurriculumService().getCurriculumElementMemberships(Collections.singletonList(curriculumElementRef), identity);
			boolean participant = !memberships.isEmpty() && memberships.get(0).isParticipant();
			return Boolean.valueOf(participant);
		});
		
		return curriculumElementToParticipant.get(curriculumElementRef.getKey()).booleanValue();
	}
	
	private CurriculumService getCurriculumService() {
		if (curriculumService == null) {
			curriculumService = CoreSpringFactory.getImpl(CurriculumService.class);
		}
		return curriculumService;
	}

}

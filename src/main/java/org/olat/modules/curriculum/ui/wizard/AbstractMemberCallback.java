/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.curriculum.ui.wizard;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.olat.basesecurity.GroupMembershipStatus;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementMembership;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementMembershipChange;
import org.olat.modules.curriculum.ui.member.MembershipModification;
import org.olat.modules.curriculum.ui.member.ResourceToRoleKey;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.ResourceReservation;
import org.olat.resource.accesscontrol.model.SearchReservationParameters;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12 déc. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractMemberCallback implements StepRunnerCallback {

	@Autowired
	protected ACService acService;
	@Autowired
	protected CurriculumService curriculumService;
	
	AbstractMemberCallback() {
		CoreSpringFactory.autowireObject(this);
	}
	
	protected List<CurriculumElementMembershipChange> applyModification(List<Identity> identities, List<CurriculumElement> curriculumElements, List<MembershipModification> modifications) {
		List<CurriculumElementMembership> memberships = curriculumService.getCurriculumElementMemberships(curriculumElements, identities);
		Map<IdentityToCurriculumElementKey, CurriculumElementMembership> membershipsMap = memberships.stream()
				.collect(Collectors.toMap(membership -> new IdentityToCurriculumElementKey(membership.getIdentityKey(), membership.getCurriculumElementKey()) , u -> u, (u, v) -> u));
		
		// Reservations
		List<OLATResource> elementsResources = curriculumElements.stream()
				.map(CurriculumElement::getResource)
				.filter(Objects::nonNull)
				.toList();
		SearchReservationParameters searchParams = new SearchReservationParameters(elementsResources);
		List<ResourceReservation> reservations = acService.getReservations(searchParams);
		Map<IdentityToReservationKey,ResourceReservation> reservationsMap = reservations.stream()
				.filter(reservation -> StringHelper.containsNonWhitespace(reservation.getType()))
				.collect(Collectors.toMap(reservation ->
				new IdentityToReservationKey(reservation.getIdentity().getKey(), ResourceToRoleKey.reservationToRole(reservation.getType()), reservation.getResource().getKey()), r -> r, (u, v) -> u));

		List<CurriculumElementMembershipChange> changes = new ArrayList<>();
		for(Identity identity:identities) {
			for(MembershipModification modification:modifications) {
				final CurriculumElement curriculumElement = modification.curriculumElement();
				final OLATResource resource = curriculumElement.getResource();
				
				CurriculumElementMembership membership = membershipsMap.get(new IdentityToCurriculumElementKey(identity.getKey(), curriculumElement.getKey()));
				ResourceReservation reservation = reservationsMap.get(new IdentityToReservationKey(identity.getKey(), modification.role(), resource.getKey()));
				if(allowModification(modification, membership, reservation)) {
					changes.add(applyModification(identity, modification));
				}
			}
		}
		return changes;
	}
	
	protected abstract boolean allowModification(MembershipModification modification, CurriculumElementMembership membership, ResourceReservation reservation);
	
	protected CurriculumElementMembershipChange applyModification(Identity member, MembershipModification modification) {
		final CurriculumRoles role = modification.role();
		final String adminNote = modification.adminNote();
		final GroupMembershipStatus nextStatus = modification.nextStatus();
		final CurriculumElement curriculumElement = modification.curriculumElement();
			
		CurriculumElementMembershipChange change = CurriculumElementMembershipChange.valueOf(member, curriculumElement);
		change.setNextStatus(role, nextStatus);
		change.setAdminNote(role, adminNote);
			
		if(nextStatus == GroupMembershipStatus.reservation) {
			change.setConfirmation(modification.confirmation());
			change.setConfirmationBy(modification.confirmationBy());
			change.setConfirmUntil(modification.confirmUntil());
		}
		
		return change;
	}
	
	public record IdentityToReservationKey(Long identityKey, CurriculumRoles role, Long resourceKey) {
		
		@Override
		public int hashCode() {
			return identityKey.hashCode()
					+ role.hashCode()
					+ resourceKey.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if(obj == this) {
				return true;
			}
			if(obj instanceof IdentityToReservationKey key) {
				return identityKey.equals(key.identityKey)
						&& role.equals(key.role)
						&& resourceKey.equals(key.resourceKey);
			}
			return false;
		}
	}
	
	public record IdentityToCurriculumElementKey(Long identityKey, Long curriculumElementKey) {
		
		@Override
		public int hashCode() {
			return identityKey.hashCode() + curriculumElementKey.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if(obj == this) {
				return true;
			}
			if(obj instanceof IdentityToCurriculumElementKey key) {
				return identityKey.equals(key.identityKey)
						&& curriculumElementKey.equals(key.curriculumElementKey);
			}
			return false;
		}
	}
	
	
}

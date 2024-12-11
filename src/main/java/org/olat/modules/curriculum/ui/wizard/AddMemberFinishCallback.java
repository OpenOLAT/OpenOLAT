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
import java.util.stream.Collectors;

import org.olat.basesecurity.GroupMembershipStatus;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailPackage;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementMembership;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementMembershipChange;
import org.olat.modules.curriculum.ui.member.MembershipModification;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.ResourceReservation;
import org.olat.resource.accesscontrol.model.SearchReservationParameters;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 6 déc. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AddMemberFinishCallback implements StepRunnerCallback {
	
	private final MembersContext membersContext;

	@Autowired
	private ACService acService;
	@Autowired
	private CurriculumService curriculumService;
	
	public AddMemberFinishCallback(MembersContext membersContext) {
		CoreSpringFactory.autowireObject(this);
		this.membersContext = membersContext;
	}

	@Override
	public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
		final CurriculumRoles roleToModify = membersContext.getRoleToModify();
		final List<Identity> identities = membersContext.getSelectedIdentities();
		final List<CurriculumElement> curriculumElements = membersContext.getAllCurriculumElements();
		List<CurriculumElementMembership> memberships = curriculumService.getCurriculumElementMemberships(curriculumElements, identities);
		Map<IdentityToObject, CurriculumElementMembership> membershipsMap = memberships.stream()
				.collect(Collectors.toMap(membership -> new IdentityToObject(membership.getIdentityKey(), membership.getCurriculumElementKey()) , u -> u, (u, v) -> u));
		
		// Reservations
		String roleKeyWord = CurriculumService.RESERVATION_PREFIX.concat(roleToModify.name());
		List<OLATResource> elementsResources = membersContext.getAllCurriculumElementResources();
		SearchReservationParameters searchParams = new SearchReservationParameters(elementsResources);
		List<ResourceReservation> reservations = acService.getReservations(searchParams);
		Map<IdentityToObject,ResourceReservation> reservationsMap = reservations.stream()
				.filter(reservation -> StringHelper.containsNonWhitespace(reservation.getType()))
				.filter(reservation -> reservation.getType().equals(roleKeyWord))
				.collect(Collectors.toMap(reservation -> new IdentityToObject(reservation.getIdentity().getKey(), reservation.getResource().getKey()), r -> r, (u, v) -> u));

		List<MembershipModification> modifications = membersContext.getModifications();
		List<CurriculumElementMembershipChange> changes = new ArrayList<>();
		for(Identity identity:identities) {
			for(MembershipModification modification:modifications) {
				final CurriculumElement curriculumElement = modification.curriculumElement();
				final OLATResource resource = curriculumElement.getResource();
				
				CurriculumElementMembership membership = membershipsMap.get(new IdentityToObject(identity.getKey(), curriculumElement.getKey()));
				ResourceReservation reservation = reservationsMap.get(new IdentityToObject(identity.getKey(), resource.getKey()));
				if((membership == null || !membership.getRoles().contains(roleToModify))
						&& (reservation == null || modification.nextStatus() != GroupMembershipStatus.reservation)) {
					changes.add(applyModification(identity, modification));
				}
			}
		}
		
		if(!changes.isEmpty()) {
			MailerResult result = new MailerResult();
			MailTemplate template = membersContext.getMailTemplate();
			MailPackage mailPackage = new MailPackage(template, result, (MailContext)null, template != null);
			curriculumService.updateCurriculumElementMemberships(ureq.getIdentity(), ureq.getUserSession().getRoles(), changes, mailPackage);
		}
		return StepsMainRunController.DONE_MODIFIED;
	}
	
	private CurriculumElementMembershipChange applyModification(Identity member, MembershipModification modification) {
		final CurriculumRoles role = modification.role();
		final GroupMembershipStatus nextStatus = modification.nextStatus();
		final CurriculumElement curriculumElement = modification.curriculumElement();
			
		CurriculumElementMembershipChange change = CurriculumElementMembershipChange.valueOf(member, curriculumElement);
		change.setNextStatus(role, nextStatus);
			
		if(nextStatus == GroupMembershipStatus.reservation) {
			change.setConfirmation(modification.confirmation());
			change.setConfirmationBy(modification.confirmationBy());
			change.setConfirmUntil(modification.confirmUntil());
		}
		
		return change;
	}
	
	private record IdentityToObject(Long identityKey, Long elementKey) {
		
		@Override
		public int hashCode() {
			return identityKey.hashCode() + elementKey.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if(obj == this) {
				return true;
			}
			if(obj instanceof IdentityToObject key) {
				return identityKey.equals(key.identityKey)
						&& elementKey.equals(key.elementKey);
			}
			return false;
		}
	}
}

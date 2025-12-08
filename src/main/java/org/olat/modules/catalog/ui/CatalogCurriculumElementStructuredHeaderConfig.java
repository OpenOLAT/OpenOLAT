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
package org.olat.modules.catalog.ui;

import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementMembership;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessResult;
import org.olat.resource.accesscontrol.ParticipantsAvailability;
import org.olat.resource.accesscontrol.ParticipantsAvailability.ParticipantsAvailabilityNum;
import org.olat.resource.accesscontrol.ResourceReservation;
import org.olat.resource.accesscontrol.manager.ACReservationDAO;
import org.olat.resource.accesscontrol.model.SearchReservationParameters;

/**
 * 
 * Initial date: Dec 2, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CatalogCurriculumElementStructuredHeaderConfig extends CatalogCurriculumElementBasicHeaderConfig {
	
	private static final ParticipantsAvailabilityNum PARTICIPANTS_AVAILABILITY_NUM
			= new ParticipantsAvailabilityNum(ParticipantsAvailability.manyLeft, Long.MAX_VALUE);

	public CatalogCurriculumElementStructuredHeaderConfig(CurriculumElement curriculumElement, Identity identity) {
		super(CoreSpringFactory.getImpl(CurriculumService.class), CoreSpringFactory.getImpl(ACService.class), curriculumElement, identity);
		
		participantsAvailability = PARTICIPANTS_AVAILABILITY_NUM;
		initOpenBookOffers();
	}
	
	private void initOpenBookOffers() {
		List<CurriculumElementMembership> memberships = curriculumService.getCurriculumElementMemberships(List.of(curriculumElement), List.of(identity));
		if (memberships.size() == 1) {
			CurriculumElementMembership membership = memberships.get(0);
			if (membership.isParticipant()) {
				openWithStatusCheck();
				if (openAvailable && openEnabled) {
					return;
				}
			}
			if (membership.isRepositoryEntryOwner() || membership.isCoach()) {
				boolean openAvailableBefore = openAvailable;
				boolean openEnabledBefore = openEnabled;
				openEnabled();
				if (openEnabled) {
					notPublishedYetMessage = false;
				}
				ownerCoachMessage = openAvailableBefore != openAvailable || openEnabledBefore != openEnabled;
			}
		}
		
		if (openAvailable) {
			return;
		}
		
		SearchReservationParameters searchParams = new SearchReservationParameters(List.of(curriculumElement.getResource()));
		searchParams.setIdentities(List.of(identity));
		List<ResourceReservation> reservations =  CoreSpringFactory.getImpl(ACReservationDAO.class).loadReservations(searchParams);
		if (!reservations.isEmpty()) {
			openDisabledConfirmationPending();
			return;
		}
		
		participantsAvailability = loadParticipantsAvailabilityNum();
		if (participantsAvailability.availability() == ParticipantsAvailability.fullyBooked) {
			bookDisabledAvailability();
			return;
		}
		
		AccessResult acResult = acService.isAccessible(curriculumElement, identity, Boolean.FALSE, false, null, false);
		if (acResult.isAccessible()) {
			openWithStatusCheck();
			if (openAvailable) {
				return;
			}
		}
		
		availableMethods = acResult.getAvailableMethods();
		if (participantsAvailability.availability() == ParticipantsAvailability.fewLeft) {
			availabilityMessage = true;
		}
	}

	private void openWithStatusCheck() {
		if (CurriculumElementStatus.isInArray(curriculumElement.getElementStatus(), CurriculumElementStatus.visibleUser())) {
			openEnabled();
		} else {
			openDisabledNotPublishedYet();
		}
	}

}

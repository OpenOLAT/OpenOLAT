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

import java.util.Date;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.DateUtils;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementMembership;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessResult;
import org.olat.resource.accesscontrol.Order;
import org.olat.resource.accesscontrol.OrderStatus;
import org.olat.resource.accesscontrol.ParticipantsAvailability;
import org.olat.resource.accesscontrol.ParticipantsAvailability.ParticipantsAvailabilityNum;
import org.olat.resource.accesscontrol.Price;
import org.olat.resource.accesscontrol.ResourceReservation;
import org.olat.resource.accesscontrol.manager.ACReservationDAO;
import org.olat.resource.accesscontrol.model.SearchReservationParameters;

/**
 * 
 * Initial date: Dec 1, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CatalogCurriculumElementSingleCourseHeaderConfig extends CatalogCurriculumElementBasicHeaderConfig {
	
	private static final ParticipantsAvailabilityNum PARTICIPANTS_AVAILABILITY_NUM
			= new ParticipantsAvailabilityNum(ParticipantsAvailability.manyLeft, Long.MAX_VALUE);
	
	private final RepositoryEntry repositoryEntry;
	private final Roles roles;
	private RepositoryEntrySecurity reSecurity;
	private boolean isParticipant = false;
	private boolean isReservationAvailable = false;
	
	public CatalogCurriculumElementSingleCourseHeaderConfig(CurriculumElement curriculumElement,
			RepositoryEntry repositoryEntry, Identity identity, Roles roles) {
		super(CoreSpringFactory.getImpl(CurriculumService.class), CoreSpringFactory.getImpl(ACService.class), curriculumElement, identity);
		this.repositoryEntry = repositoryEntry;
		this.roles = roles;
		
		participantsAvailability = PARTICIPANTS_AVAILABILITY_NUM;
		
		initOpenBookOffers();
		initAdminAccess();
		initLeave();
	}
	
	private void initOpenBookOffers() {
		if (repositoryEntry == null) {
			List<CurriculumElementMembership> memberships = curriculumService.getCurriculumElementMemberships(List.of(curriculumElement), List.of(identity));
			if (memberships.size() == 1) {
				CurriculumElementMembership membership = memberships.get(0);
				if (membership.isParticipant()) {
					isParticipant = true;
					openDisabledNoContentYet();
				} else if (membership.isRepositoryEntryOwner() || membership.isCoach()) {
					openDisabledNoContentYet();
					ownerCoachMessage = true;
				}
			}
		} else {
			// Content available
			reSecurity = CoreSpringFactory.getImpl(RepositoryManager.class).isAllowed(identity, roles, repositoryEntry);
			if (reSecurity.isParticipant()) {
				isParticipant = true;
				openWithStatusCheck(repositoryEntry, RepositoryEntryStatusEnum.publishedAndClosed());
			}
			if (openAvailable && openEnabled) {
				return;
			}
			
			if (reSecurity.isOwner() || reSecurity.isCoach()) {
				boolean openAvailableBefore = openAvailable;
				boolean openEnabledBefore = openEnabled;
				
				if (reSecurity.isCoach()) {
					openWithStatusCheck(repositoryEntry, RepositoryEntryStatusEnum.coachPublishedToClosed());
				}
				if (reSecurity.isOwner()) {
					openWithStatusCheck(repositoryEntry, RepositoryEntryStatusEnum.preparationToClosed());
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
			isReservationAvailable = true;
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
			if (repositoryEntry != null) {
				openWithStatusCheck(repositoryEntry, RepositoryEntryStatusEnum.publishedAndClosed());
			} else {
				openDisabledNoContentYet();
			}
			if (openAvailable) {
				return;
			}
		}
		
		availableMethods = acResult.getAvailableMethods();
		if (participantsAvailability.availability() == ParticipantsAvailability.fewLeft) {
			availabilityMessage = true;
		}
	}

	private void initAdminAccess() {
		if (openAvailable && openEnabled) {
			return;
		}
		
		if (reSecurity == null) {
			return;
		}
		
		if (reSecurity.canLaunch()) {
			if (reSecurity.isAdministrator() || reSecurity.isLearnResourceManager() || reSecurity.isPrincipal() || reSecurity.isCurriculumManager()) {
				administrativOpenAvailable = true;
			}
		}
	}

	private void initLeave() {
		// Leave not allowed if not a participant
		if (!isParticipant && !isReservationAvailable) {
			return;
		}
		
		// Leave only allowed before start
		if (curriculumElement.getBeginDate() == null || DateUtils.getStartOfDay(curriculumElement.getBeginDate()).before(new Date())) {
			return;
		}
		
		List<Order> orders = acService.findOrders(identity, curriculumElement.getResource(),
				OrderStatus.NEW, OrderStatus.PREPAYMENT, OrderStatus.PAYED);
		if (orders.isEmpty()) {
			return;
		}
		
		leaveAvailable = true;
		Price cancellationFee = acService.getCancellationFee(curriculumElement.getResource(), curriculumElement.getBeginDate(), orders);
		if (cancellationFee != null) {
			leaveWithCancellationFee = true;
		}
	}
	
	private void openWithStatusCheck(RepositoryEntry repositoryEntry, RepositoryEntryStatusEnum[] statusArray) {
		if (RepositoryEntryStatusEnum.isInArray(repositoryEntry.getEntryStatus(), statusArray)) {
			openEnabled();
			notPublishedYetMessage = false;
		} else {
			openDisabledNotPublishedYet();
		}
	}
}

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

import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.repository.ui.list.BasicDetailsHeaderConfig;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.ConfirmationByEnum;
import org.olat.resource.accesscontrol.Order;
import org.olat.resource.accesscontrol.OrderStatus;
import org.olat.resource.accesscontrol.ParticipantsAvailability.ParticipantsAvailabilityNum;
import org.olat.resource.accesscontrol.Price;
import org.olat.resource.accesscontrol.ResourceReservation;
import org.olat.resource.accesscontrol.model.SearchReservationParameters;

/**
 * 
 * Initial date: Dec 2, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CatalogCurriculumElementBasicHeaderConfig extends BasicDetailsHeaderConfig {

	protected final CurriculumElement curriculumElement;
	protected final CurriculumService curriculumService;
	protected final ACService acService;
	protected boolean isParticipant = false;
	protected boolean isReservationAvailable = false;

	public CatalogCurriculumElementBasicHeaderConfig(CurriculumService curriculumService, ACService acService,
			CurriculumElement curriculumElement, Identity identity) {
		super(identity);
		this.curriculumService = curriculumService;
		this.acService = acService;
		this.curriculumElement = curriculumElement;
	}

	protected ParticipantsAvailabilityNum loadParticipantsAvailabilityNum() {
		Long numParticipants = curriculumService.getCurriculumElementKeyToNumParticipants(List.of(curriculumElement), true).get(curriculumElement.getKey());
		return acService.getParticipantsAvailability(curriculumElement.getMaxParticipants(), numParticipants, false);
	}

	protected void initReservations() {
		if (identity == null) {
			return;
		}
		SearchReservationParameters searchParams = new SearchReservationParameters(List.of(curriculumElement.getResource()));
		searchParams.setIdentities(List.of(identity));
		List<ResourceReservation> reservations = acService.getReservations(searchParams);
		boolean participantConfirmation = false;
		boolean adminConfirmation = false;
		for (ResourceReservation reservation : reservations) {
			isReservationAvailable = true;
			if (reservation.getConfirmableBy() == ConfirmationByEnum.PARTICIPANT) {
				participantConfirmation = true;
			} else {
				adminConfirmation = true;
			}
		}
		if (participantConfirmation) {
			openDisabledParticipantConfirmationPending();
		} else if (adminConfirmation) {
			openDisabledAdminConfirmationPending();
		}
	}

	protected void initLeave() {
		if (!isParticipant && !isReservationAvailable) {
			return;
		}

		if (curriculumElement.getBeginDate() == null || DateUtils.getStartOfDay(curriculumElement.getBeginDate()).before(new Date())) {
			return;
		}

		List<Order> orders = acService.findOrders(identity, curriculumElement.getResource(),
				OrderStatus.NEW, OrderStatus.PREPAYMENT, OrderStatus.PAYED);
		if (orders.isEmpty()) {
			return;
		}

		leaveAvailable = true;
		leaveEnabled = true;
		Price cancellationFee = acService.getCancellationFee(curriculumElement.getResource(), curriculumElement.getBeginDate(), orders);
		if (cancellationFee != null) {
			leaveWithCancellationFee = true;
		}
	}

}
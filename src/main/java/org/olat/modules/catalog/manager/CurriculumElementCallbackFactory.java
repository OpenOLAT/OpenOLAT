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
package org.olat.modules.catalog.manager;

import java.util.List;
import java.util.function.Supplier;

import org.olat.modules.catalog.CatalogEntrySecurityCallback;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.resource.accesscontrol.ParticipantsAvailability;
import org.olat.resource.accesscontrol.model.OLATResourceAccess;
import org.olat.resource.accesscontrol.model.PriceMethodBundle;
import org.olat.resource.accesscontrol.provider.free.FreeAccessHandler;

/**
 * 
 * Initial date: Oct 22, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public abstract class CurriculumElementCallbackFactory {

	protected final CurriculumElementStatus curriculumElementStatus;
	protected final boolean isMember;
	protected final boolean isParticipant;
	protected final Supplier<Boolean> isReservation;
	protected final List<OLATResourceAccess> resourceAccesses;
	protected final ParticipantsAvailability participantsAvailability;
	private CatalogEntrySecurityCallback secCallback;

	public CurriculumElementCallbackFactory(CurriculumElementStatus curriculumElementStatus, boolean isMember, boolean isParticipant,
			Supplier<Boolean> isReservation, List<OLATResourceAccess> resourceAccesses, ParticipantsAvailability participantsAvailability) {
		this.curriculumElementStatus = curriculumElementStatus;
		this.isMember = isMember;
		this.isParticipant = isParticipant;
		this.isReservation = isReservation;
		this.resourceAccesses = resourceAccesses;
		this.participantsAvailability = participantsAvailability;
	}
	
	public final CatalogEntrySecurityCallback getSecurityCallback() {
		if (secCallback == null) {
			secCallback = createSecCallback();
		}
		return secCallback;
	}
	
	private CatalogEntrySecurityCallback createSecCallback() {
		if (isParticipant) {
			return createMemberCallback();
		} else if (isReservation.get()) {
			return CatalogEntrySecurityCallback.CONFIRMATION_OUTSTANDING;
		} else if (!ParticipantsAvailability.isParticipantsAvailable(participantsAvailability)) {
			return CatalogEntrySecurityCallback.FULLY_BOOKED_CALLBACK;
		} else if (isAutoBooking()) {
			return CatalogEntrySecurityCallback.AUTO_BOOKING_CALLBACK;
		}
		return CatalogEntrySecurityCallback.BOOKING_CALLBACK;
	}

	protected abstract CatalogEntrySecurityCallback createMemberCallback();
	
	protected final boolean isAutoBooking() {
		if (isMember) {
			return false;
		}
		
		// If an offer leads to a reservation, auto booking is not available.
		// Since not both options can be true at the same time,
		// it is not validated at this point.
		
		List<PriceMethodBundle> bundles = resourceAccesses.stream()
			.flatMap(ra -> ra.getMethods().stream())
			.toList();
		
		if (bundles.size() == 1) {
			PriceMethodBundle bundle = bundles.get(0);
			if (FreeAccessHandler.METHOD_TYPE.equals(bundle.getMethod().getType()) && bundle.isAutoBooking()) {
				return true;
			}
		}
		
		return false;
	}

}

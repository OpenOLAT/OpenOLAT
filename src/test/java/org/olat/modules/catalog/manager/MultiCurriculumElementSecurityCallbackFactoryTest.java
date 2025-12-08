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

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.olat.modules.catalog.CatalogEntrySecurityCallback;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.resource.accesscontrol.ParticipantsAvailability;
import org.olat.resource.accesscontrol.model.FreeAccessMethod;
import org.olat.resource.accesscontrol.model.OLATResourceAccess;

/**
 * 
 * Initial date: Dec 3, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class MultiCurriculumElementSecurityCallbackFactoryTest {

	@Test
	public void shouldCreateCallback_participant_access() {
		CatalogEntrySecurityCallback secCallback = secCallback(CurriculumElementStatus.active, true, true, () -> false, oneAccess(), ParticipantsAvailability.manyLeft);
		assertCallback(secCallback, true, true, false, false, false);
	}
	
	@Test
	public void shouldCreateCallback_participant_noaccess_status() {
		CatalogEntrySecurityCallback secCallback = secCallback(CurriculumElementStatus.preparation, true, true, () -> false, oneAccess(), ParticipantsAvailability.manyLeft);
		assertCallback(secCallback, true, false, false, false, false);
	}
	
	@Test
	public void shouldCreateCallback_reservation() {
		CatalogEntrySecurityCallback secCallback = secCallback(CurriculumElementStatus.active, false, false, () -> true, oneAccess(), ParticipantsAvailability.manyLeft);
		assertCallback(secCallback, true, false, false, false, false);
	}
	
	@Test
	public void shouldCreateCallback_fully_booked() {
		CatalogEntrySecurityCallback secCallback = secCallback(CurriculumElementStatus.active, true, true, () -> false, oneAccess(), ParticipantsAvailability.fullyBooked);
		assertCallback(secCallback, true, true, false, false, false);
		secCallback = secCallback(CurriculumElementStatus.preparation, false, false, () -> false, oneAccess(), ParticipantsAvailability.fullyBooked);
		assertCallback(secCallback, false, false, true, false, false);
	}
	
	@Test
	public void shouldCreateCallback_offer() {
		CatalogEntrySecurityCallback secCallback = secCallback(CurriculumElementStatus.preparation, false, false, () -> false, oneAccess(), ParticipantsAvailability.manyLeft);
		assertCallback(secCallback, false, false, true, true, false);
	}
	
	@Test
	public void shouldCreateCallback_offers() {
		CatalogEntrySecurityCallback secCallback = secCallback(CurriculumElementStatus.preparation, false, false, () -> false, toAccess(List.of(false, false)), ParticipantsAvailability.manyLeft);
		assertCallback(secCallback, false, false, true, true, false);
	}
	
	@Test
	public void shouldCreateCallback_autoBooking() {
		CatalogEntrySecurityCallback secCallback = secCallback(CurriculumElementStatus.preparation, false, false, () -> false, toAccess(List.of(true)), ParticipantsAvailability.manyLeft);
		assertCallback(secCallback, false, false, true, true, true);
		secCallback = secCallback(CurriculumElementStatus.preparation, false, false, () -> false,  toAccess(List.of(true, true)), ParticipantsAvailability.manyLeft);
		assertCallback(secCallback, false, false, true, true, false);
		secCallback = secCallback(CurriculumElementStatus.preparation, false, false, () -> false, toAccess(List.of(false, true)), ParticipantsAvailability.manyLeft);
		assertCallback(secCallback, false, false, true, true, false);
	}

	private CatalogEntrySecurityCallback secCallback(CurriculumElementStatus curriculumElementStatus, boolean isMember,
			boolean isParticipant, Supplier<Boolean> isReservation, List<OLATResourceAccess> resourceAccesses,
			ParticipantsAvailability participantsAvailability) {
		return new MultiCurriculumElementSecurityCallbackFactory(curriculumElementStatus, isMember, isParticipant,
				isReservation, resourceAccesses, participantsAvailability).getSecurityCallback();
	}
	
	private void assertCallback(CatalogEntrySecurityCallback secCallback, boolean openAvailable, boolean openEnabled,
			boolean bookAvailable, boolean bookEnabled, boolean autoBooking) {
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(secCallback.isOpenAvailable()).isEqualTo(openAvailable);
		softly.assertThat(secCallback.isOpenEnabled()).isEqualTo(openEnabled);
		softly.assertThat(secCallback.isBookAvailable()).isEqualTo(bookAvailable);
		softly.assertThat(secCallback.isBookEnabled()).isEqualTo(bookEnabled);
		softly.assertThat(secCallback.isAutoBooking()).isEqualTo(autoBooking);
		softly.assertAll();
	}

	private List<OLATResourceAccess> oneAccess() {
		return toAccess(List.of(false));
	}
	
	
	private List<OLATResourceAccess> toAccess(List<Boolean> autoBookings) {
		if (autoBookings == null || autoBookings.isEmpty()) {
			return List.of();
		}
		
		return autoBookings.stream()
				.map(autoBooking -> new OLATResourceAccess(null, null, new FreeAccessMethod(), autoBooking))
				.toList();
	}
	
}

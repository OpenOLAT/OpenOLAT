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

import static org.olat.test.JunitTestHelper.random;

import java.util.Date;
import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.repository.ui.list.DetailsHeaderConfig;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.ConfirmationByEnum;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.ParticipantsAvailability;
import org.olat.resource.accesscontrol.manager.ACReservationDAO;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.model.FreeAccessMethod;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: Dec 3, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CatalogCurriculumElementStructuredHeaderConfigTest extends OlatTestCase {
	
	private static final String USER_PREFIX = random();
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private ACService acService;
	@Autowired
	private ACReservationDAO reservationDao;

	@Test
	public void shouldGetConfig_noAccess_bookable() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(USER_PREFIX);
		CurriculumElement curriculumElement = createCurriculumElement(CurriculumElementStatus.active);
		dbInstance.commitAndCloseSession();
		
		CatalogCurriculumElementStructuredHeaderConfig sut = sut(curriculumElement, identity);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(sut.isOpenAvailable()).isFalse();
		softly.assertThat(sut.isOpenEnabled()).isFalse();
		softly.assertThat(sut.isBookAvailable()).isFalse();
		softly.assertThat(sut.isBookEnabled()).isFalse();
		softly.assertThat(sut.isOffersPreview()).isFalse();
		softly.assertThat(sut.isOffersWebPublish()).isFalse();
		softly.assertThat(sut.isOffersAvailable()).isTrue();
		softly.assertThat(sut.getAvailableMethods()).hasSize(1);
		softly.assertThat(sut.isLeaveAvailable()).isFalse();
		softly.assertThat(sut.isLeaveWithCancellationFee()).isFalse();
		softly.assertThat(sut.getParticipantsAvailabilityNum().availability()).isEqualTo(ParticipantsAvailability.manyLeft);
		softly.assertThat(sut.getParticipantsAvailabilityNum().numAvailable()).isGreaterThan(10000);
		softly.assertThat(sut.isNotPublishedYetMessage()).isFalse();
		softly.assertThat(sut.isNoContentYetMessage()).isFalse();
		softly.assertThat(sut.isConfirmationPendingMessage()).isFalse();
		softly.assertThat(sut.isAvailabilityMessage()).isFalse();
		softly.assertThat(sut.isOwnerCoachMessage()).isFalse();
		softly.assertThat(sut.isAdministrativOpenAvailable()).isFalse();
		softly.assertThat(sut.isAdministrativOpenEnabled()).isFalse();
		softly.assertAll();
	}
	
	@Test
	public void shouldGetConfig_noContentAvailable() {
		// participant
		DetailsHeaderConfig sut = sut(true, true, true, false, false, CurriculumElementStatus.preparation);
		assertParticipant(sut, true, true, false, false, false, false, false, true);
		sut = sut(true, true, true, false, false, CurriculumElementStatus.active);
		assertParticipant(sut, true, true, false, false, false, false, false, false);
		
		sut = sut(true, true, false, false, false, CurriculumElementStatus.preparation);
		assertParticipant(sut, true, true, false, false, false, false, false, true);
		sut = sut(true, true, false, false, false, CurriculumElementStatus.active);
		assertParticipant(sut, true, true, false, false, false, false, false, false);
		
		sut = sut(true, false, true, false, false, CurriculumElementStatus.preparation);
		assertParticipant(sut, true, true, false, false, false, false, false, true);
		sut = sut(true, false, true, false, false, CurriculumElementStatus.active);
		assertParticipant(sut, true, true, false, false, false, false, false, false);
		
		sut = sut(true, false, false, false, false, CurriculumElementStatus.preparation);
		assertParticipant(sut, true, false, false, false, false, true, false, false);
		sut = sut(true, false, false, false, false, CurriculumElementStatus.active);
		assertParticipant(sut, true, true, false, false, false, false, false, false);
		
		// not participant
		sut = sut(false, true, true, false, false, CurriculumElementStatus.preparation);
		assertParticipant(sut, true, true, false, false, false, false, false, true);
		sut = sut(false, true, true, false, false, CurriculumElementStatus.active);
		assertParticipant(sut, true, true, false, false, false, false, false, true);
		
		sut = sut(false, true, false, false, false, CurriculumElementStatus.preparation);
		assertParticipant(sut, true, true, false, false, false, false, false, true);
		sut = sut(false, true, false, false, false, CurriculumElementStatus.active);
		assertParticipant(sut, true, true, false, false, false, false, false, true);
		
		sut = sut(false, false, true, false, false, CurriculumElementStatus.preparation);
		assertParticipant(sut, true, true, false, false, false, false, false, true);
		sut = sut(false, false, true, false, false, CurriculumElementStatus.active);
		assertParticipant(sut, true, true, false, false, false, false, false, true);
		
		sut = sut(false, false, false, false, false, CurriculumElementStatus.preparation);
		assertParticipant(sut, false, false, false, true, false, false, false, false);
		sut = sut(false, false, false, false, false, CurriculumElementStatus.active);
		assertParticipant(sut, false, false, false, true, false, false, false, false);
	}
	
	@Test
	public void shouldGetConfig_noContentAvailable_reservationAvailable() {
		// participant
		DetailsHeaderConfig sut = sut(true, true, true, true, false, CurriculumElementStatus.preparation);
		assertParticipant(sut, true, true, false, false, false, false, false, true);
		sut = sut(true, true, true, true, false, CurriculumElementStatus.active);
		assertParticipant(sut, true, true, false, false, false, false, false, false);
		
		sut = sut(true, true, false, true, false, CurriculumElementStatus.preparation);
		assertParticipant(sut, true, true, false, false, false, false, false, true);
		sut = sut(true, true, false, true, false, CurriculumElementStatus.active);
		assertParticipant(sut, true, true, false, false, false, false, false, false);
		
		sut = sut(true, false, true, true, false, CurriculumElementStatus.preparation);
		assertParticipant(sut, true, true, false, false, false, false, false, true);
		sut = sut(true, false, true, true, false, CurriculumElementStatus.active);
		assertParticipant(sut, true, true, false, false, false, false, false, false);
		
		sut = sut(true, false, false, true, false, CurriculumElementStatus.preparation);
		assertParticipant(sut, true, false, false, false, false, true, false, false);
		sut = sut(true, false, false, true, false, CurriculumElementStatus.active);
		assertParticipant(sut, true, true, false, false, false, false, false, false);
		
		// not participant
		sut = sut(false, true, true, true, false, CurriculumElementStatus.preparation);
		assertParticipant(sut, true, true, false, false, false, false, false, true);
		sut = sut(false, true, true, true, false, CurriculumElementStatus.active);
		assertParticipant(sut, true, true, false, false, false, false, false, true);
		
		sut = sut(false, true, false, true, false, CurriculumElementStatus.preparation);
		assertParticipant(sut, true, true, false, false, false, false, false, true);
		sut = sut(false, true, false, true, false, CurriculumElementStatus.active);
		assertParticipant(sut, true, true, false, false, false, false, false, true);
		
		sut = sut(false, false, true, true, false, CurriculumElementStatus.preparation);
		assertParticipant(sut, true, true, false, false, false, false, false, true);
		sut = sut(false, false, true, true, false, CurriculumElementStatus.active);
		assertParticipant(sut, true, true, false, false, false, false, false, true);
		
		sut = sut(false, false, false, true, false, CurriculumElementStatus.preparation);
		assertParticipant(sut, true, false, false, false, false, false, true, false);
		sut = sut(false, false, false, true, false, CurriculumElementStatus.active);
		assertParticipant(sut, true, false, false, false, false, false, true, false);
		
		// ... and fully booked
		sut = sut(false, false, false, true, true, CurriculumElementStatus.preparation);
		assertParticipant(sut, true, false, false, false, false, false, true, false);
		sut = sut(false, false, false, true, true, CurriculumElementStatus.active);
		assertParticipant(sut, true, false, false, false, false, false, true, false);
	}
	
	@Test
	public void shouldGetConfig_noContentAvailable_fullyBooked() {
		// participant
		DetailsHeaderConfig sut = sut(true, true, true, false, true, CurriculumElementStatus.preparation);
		assertParticipant(sut, true, true, false, false, false, false, false, true);
		sut = sut(true, true, true, false, true, CurriculumElementStatus.active);
		assertParticipant(sut, true, true, false, false, false, false, false, false);
		
		sut = sut(true, true, false, false, true, CurriculumElementStatus.preparation);
		assertParticipant(sut, true, true, false, false, false, false, false, true);
		sut = sut(true, true, false, false, true, CurriculumElementStatus.active);
		assertParticipant(sut, true, true, false, false, false, false, false, false);
		
		sut = sut(true, false, true, false, true, CurriculumElementStatus.preparation);
		assertParticipant(sut, true, true, false, false, false, false, false, true);
		sut = sut(true, false, true, false, true, CurriculumElementStatus.active);
		assertParticipant(sut, true, true, false, false, false, false, false, false);
		
		sut = sut(true, false, false, false, true, CurriculumElementStatus.preparation);
		assertParticipant(sut, true, false, false, false, false, true, false, false);
		sut = sut(true, false, false, false, true, CurriculumElementStatus.active);
		assertParticipant(sut, true, true, false, false, false, false, false, false);
		
		// not participant
		sut = sut(false, true, true, false, true, CurriculumElementStatus.preparation);
		assertParticipant(sut, true, true, false, false, false, false, false, true);
		sut = sut(false, true, true, false, true, CurriculumElementStatus.active);
		assertParticipant(sut, true, true, false, false, false, false, false, true);
		
		sut = sut(false, true, false, false, true, CurriculumElementStatus.preparation);
		assertParticipant(sut, true, true, false, false, false, false, false, true);
		sut = sut(false, true, false, false, true, CurriculumElementStatus.active);
		assertParticipant(sut, true, true, false, false, false, false, false, true);
		
		sut = sut(false, false, true, false, true, CurriculumElementStatus.preparation);
		assertParticipant(sut, true, true, false, false, false, false, false, true);
		sut = sut(false, false, true, false, true, CurriculumElementStatus.active);
		assertParticipant(sut, true, true, false, false, false, false, false, true);
		
		sut = sut(false, false, false, false, true, CurriculumElementStatus.preparation);
		assertParticipant(sut, false, false, true, false, true, false, false, false);
		sut = sut(false, false, false, false, true, CurriculumElementStatus.active);
		assertParticipant(sut, false, false, true, false, true, false, false, false);
	}
	
	private void assertParticipant(DetailsHeaderConfig sut, boolean openAvailable, boolean openEnabled,
			boolean bookAvailable, boolean offersAvailable, boolean fullyBookedMessage,
			boolean notPublishedYetMessage, boolean confirmationPendingMessage, boolean ownerCoachMessage) {
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(sut.isOpenAvailable()).isEqualTo(openAvailable);
		softly.assertThat(sut.isOpenEnabled()).isEqualTo(openEnabled);
		softly.assertThat(sut.isBookAvailable()).isEqualTo(bookAvailable);
		softly.assertThat(sut.isBookEnabled()).isFalse();
		softly.assertThat(sut.isOffersPreview()).isFalse();
		softly.assertThat(sut.isOffersWebPublish()).isFalse();
		if (offersAvailable) {
			softly.assertThat(sut.isOffersAvailable()).isTrue();
			softly.assertThat(sut.getAvailableMethods()).isNotEmpty();
		} else {
			softly.assertThat(sut.isOffersAvailable()).isFalse();
			softly.assertThat(sut.getAvailableMethods()).isNullOrEmpty();
		}
		//softly.assertThat(sut.isLeaveAvailable()).isTrue();
		softly.assertThat(sut.isLeaveWithCancellationFee()).isFalse();
		if (fullyBookedMessage) {
			softly.assertThat(sut.getParticipantsAvailabilityNum().availability()).isEqualTo(ParticipantsAvailability.fullyBooked);
			softly.assertThat(sut.getParticipantsAvailabilityNum().numAvailable()).isEqualTo(0);
			softly.assertThat(sut.isAvailabilityMessage()).isTrue();
		} else {
			softly.assertThat(sut.getParticipantsAvailabilityNum().availability()).isEqualTo(ParticipantsAvailability.manyLeft);
			softly.assertThat(sut.getParticipantsAvailabilityNum().numAvailable()).isGreaterThan(10000);
			softly.assertThat(sut.isAvailabilityMessage()).isFalse();
		}
		softly.assertThat(sut.isNotPublishedYetMessage()).isEqualTo(notPublishedYetMessage);
		softly.assertThat(sut.isNoContentYetMessage()).isFalse();
		softly.assertThat(sut.isConfirmationPendingMessage()).isEqualTo(confirmationPendingMessage);
		softly.assertThat(sut.isOwnerCoachMessage()).isEqualTo(ownerCoachMessage);
		softly.assertThat(sut.isAdministrativOpenAvailable()).isFalse();
		softly.assertThat(sut.isAdministrativOpenEnabled()).isFalse();
		softly.assertAll();
	}
	
	private DetailsHeaderConfig sut(boolean participant, boolean coach, boolean owner, boolean reservationAvailable,
			boolean fullyBooked, CurriculumElementStatus status) {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(USER_PREFIX);
		CurriculumElement curriculumElement = createCurriculumElement(status);
		dbInstance.commitAndCloseSession();
		
		if (participant) {
			curriculumService.addMember(curriculumElement, identity, CurriculumRoles.participant, identity);
		}
		if (coach) {
			curriculumService.addMember(curriculumElement, identity, CurriculumRoles.coach, identity);
		}
		if (owner) {
			curriculumService.addMember(curriculumElement, identity, CurriculumRoles.owner, identity);
		}
		
		if (reservationAvailable) {
			reservationDao.createReservation(identity, null, DateUtils.addDays(new Date(), 3),
					ConfirmationByEnum.ADMINISTRATIVE_ROLE, curriculumElement.getResource());
		}
		
		if (fullyBooked) {
			curriculumElement.setMaxParticipants(Long.valueOf(0));
			curriculumElement = curriculumService.updateCurriculumElement(curriculumElement);
		}
		
		dbInstance.commitAndCloseSession();
		
		return sut(curriculumElement, identity);
	}
	
	private CatalogCurriculumElementStructuredHeaderConfig sut(CurriculumElement curriculumElement, Identity identity) {
		return new CatalogCurriculumElementStructuredHeaderConfig(curriculumElement, identity);
	}

	private CurriculumElement createCurriculumElement(CurriculumElementStatus status) {
		Curriculum curriculum = curriculumService.createCurriculum(random(), random(), null, false, JunitTestHelper.getDefaultOrganisation());
		CurriculumElementType elementType = curriculumService.createCurriculumElementType(random(), random(), null, null);
		elementType.setSingleElement(false);
		elementType = curriculumService.updateCurriculumElementType(elementType);
		Date beginDate = null;
		Date endDate = null;
		CurriculumElement curriculumElement = curriculumService.createCurriculumElement(random(), random(), status,
				beginDate, endDate, null, elementType, null, null, null, curriculum);
		
		Offer offer = acService.createOffer(curriculumElement.getResource(), curriculumElement.getDisplayName());
		offer.setValidFrom(DateUtils.addDays(new Date(), -10));
		offer.setValidTo(DateUtils.addDays(new Date(), 10));
		AccessMethod method = acService.getAvailableMethodsByType(FreeAccessMethod.class).get(0);
		OfferAccess offerAccess = acService.createOfferAccess(offer, method);
		acService.saveOfferAccess(offerAccess);
		acService.updateOfferOrganisations(offer, List.of(JunitTestHelper.getDefaultOrganisation()));
		
		return curriculumElement;
	}

}

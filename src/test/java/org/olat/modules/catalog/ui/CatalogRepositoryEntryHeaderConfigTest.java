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
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.util.DateUtils;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.list.DetailsHeaderConfig;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.ParticipantsAvailability;
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
public class CatalogRepositoryEntryHeaderConfigTest extends OlatTestCase {

	private static final String USER_PREFIX = random();
	private static final String ADMIN_PREFIX = random();
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private ACService acService;
	@Autowired
	private OrganisationService organisationService;

	@Test
	public void shouldGetConfig_noAccess_bookable() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(USER_PREFIX);
		RepositoryEntry repositoryEntry = createRepositoryEntry(RepositoryEntryStatusEnum.published);
		dbInstance.commitAndCloseSession();
		
		DetailsHeaderConfig sut = sut(repositoryEntry, identity);
		
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
	public void shouldGetConfig_admin_hasNoAccessOtherOrganisation() {
		Organisation organisation = organisationService.createOrganisation(random(), random(), null, null, null, JunitTestHelper.getDefaultActor());
		Identity admin = JunitTestHelper.createAndPersistIdentityAsRndAdmin(random(), organisation, random());
		RepositoryEntry repositoryEntry = createRepositoryEntry(RepositoryEntryStatusEnum.preparation);
		dbInstance.commitAndCloseSession();
		
		DetailsHeaderConfig sut = sut(repositoryEntry, admin);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(sut.isOpenAvailable()).isFalse();
		softly.assertThat(sut.isOpenEnabled()).isFalse();
		softly.assertThat(sut.isBookAvailable()).isFalse();
		softly.assertThat(sut.isBookEnabled()).isFalse();
		softly.assertThat(sut.isOffersPreview()).isFalse();
		softly.assertThat(sut.isOffersWebPublish()).isFalse();
		softly.assertThat(sut.isOffersAvailable()).isFalse();
		softly.assertThat(sut.getAvailableMethods()).isNullOrEmpty();
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
	public void shouldGetConfig() {
		// participant
		DetailsHeaderConfig sut = sut(true, true, true, true, RepositoryEntryStatusEnum.preparation);
		assertParticipant(sut, true, true, false, true, false, false);
		sut = sut(true, true, true, true, RepositoryEntryStatusEnum.coachpublished);
		assertParticipant(sut, true, true, false, true, false, false);
		sut = sut(true, true, true, true, RepositoryEntryStatusEnum.published);
		assertParticipant(sut, true, true, false, false, false, false);
		
		sut = sut(true, true, true, false, RepositoryEntryStatusEnum.preparation);
		assertParticipant(sut, true, true, false, true, false, false);
		sut = sut(true, true, true, false, RepositoryEntryStatusEnum.coachpublished);
		assertParticipant(sut, true, true, false, true, false, false);
		sut = sut(true, true, true, false, RepositoryEntryStatusEnum.published);
		assertParticipant(sut, true, true, false, false, false, false);
		
		sut = sut(true, true, false, true, RepositoryEntryStatusEnum.preparation);
		assertParticipant(sut, true, false, true, false, true, false);
		sut = sut(true, true, false, true, RepositoryEntryStatusEnum.coachpublished);
		assertParticipant(sut, true, true, false, true, false, false);
		sut = sut(true, true, false, true, RepositoryEntryStatusEnum.published);
		assertParticipant(sut, true, true, false, false, false, false);
		
		sut = sut(true, true, false, false, RepositoryEntryStatusEnum.preparation);
		assertParticipant(sut, true, false, true, false, false, false);
		sut = sut(true, true, false, false, RepositoryEntryStatusEnum.coachpublished);
		assertParticipant(sut, true, true, false, true, false, false);
		sut = sut(true, true, false, false, RepositoryEntryStatusEnum.published);
		assertParticipant(sut, true, true, false, false, false, false);
		
		sut = sut(true, false, true, true, RepositoryEntryStatusEnum.preparation);
		assertParticipant(sut, true, true, false, true, false, false);
		sut = sut(true, false, true, true, RepositoryEntryStatusEnum.coachpublished);
		assertParticipant(sut, true, true, false, true, false, false);
		sut = sut(true, false, true, true, RepositoryEntryStatusEnum.published);
		assertParticipant(sut, true, true, false, false, false, false);
		
		sut = sut(true, false, true, false, RepositoryEntryStatusEnum.preparation);
		assertParticipant(sut, true, true, false, true, false, false);
		sut = sut(true, false, true, false, RepositoryEntryStatusEnum.coachpublished);
		assertParticipant(sut, true, true, false, true, false, false);
		sut = sut(true, false, true, false, RepositoryEntryStatusEnum.published);
		assertParticipant(sut, true, true, false, false, false, false);
		
		sut = sut(true, false, false, true, RepositoryEntryStatusEnum.preparation);
		assertParticipant(sut, true, false, true, false, true, false);
		sut = sut(true, false, false, true, RepositoryEntryStatusEnum.coachpublished);
		assertParticipant(sut, true, false, true, false, true, false);
		sut = sut(true, false, false, true, RepositoryEntryStatusEnum.published);
		assertParticipant(sut, true, true, false, false, false, false);
		
		sut = sut(true, false, false, false, RepositoryEntryStatusEnum.preparation);
		assertParticipant(sut, true, false, true, false, false, false);
		sut = sut(true, false, false, false, RepositoryEntryStatusEnum.coachpublished);
		assertParticipant(sut, true, false, true, false, false, false);
		sut = sut(true, false, false, false, RepositoryEntryStatusEnum.published);
		assertParticipant(sut, true, true, false, false, false, false);
		
		// not participant
		sut = sut(false, true, true, true, RepositoryEntryStatusEnum.preparation);
		assertParticipant(sut, true, true, false, true, false, false);
		sut = sut(false, true, true, true, RepositoryEntryStatusEnum.coachpublished);
		assertParticipant(sut, true, true, false, true, false, false);
		sut = sut(false, true, true, true, RepositoryEntryStatusEnum.published);
		assertParticipant(sut, true, true, false, true, false, false);
		
		sut = sut(false, true, true, false, RepositoryEntryStatusEnum.preparation);
		assertParticipant(sut, true, true, false, true, false, false);
		sut = sut(false, true, true, false, RepositoryEntryStatusEnum.coachpublished);
		assertParticipant(sut, true, true, false, true, false, false);
		sut = sut(false, true, true, false, RepositoryEntryStatusEnum.published);
		assertParticipant(sut, true, true, false, true, false, false);
		
		sut = sut(false, true, false, true, RepositoryEntryStatusEnum.preparation);
		assertParticipant(sut, true, false, true, true, true, false);
		sut = sut(false, true, false, true, RepositoryEntryStatusEnum.coachpublished);
		assertParticipant(sut, true, true, false, true, false, false);
		sut = sut(false, true, false, true, RepositoryEntryStatusEnum.published);
		assertParticipant(sut, true, true, false, true, false, false);
		
		sut = sut(false, true, false, false, RepositoryEntryStatusEnum.preparation);
		assertParticipant(sut, true, false, true, true, false, false);
		sut = sut(false, true, false, false, RepositoryEntryStatusEnum.coachpublished);
		assertParticipant(sut, true, true, false, true, false, false);
		sut = sut(false, true, false, false, RepositoryEntryStatusEnum.published);
		assertParticipant(sut, true, true, false, true, false, false);
		
		sut = sut(false, false, true, true, RepositoryEntryStatusEnum.preparation);
		assertParticipant(sut, true, true, false, true, false, false);
		sut = sut(false, false, true, true, RepositoryEntryStatusEnum.coachpublished);
		assertParticipant(sut, true, true, false, true, false, false);
		sut = sut(false, false, true, true, RepositoryEntryStatusEnum.published);
		assertParticipant(sut, true, true, false, true, false, false);
		
		sut = sut(false, false, true, false, RepositoryEntryStatusEnum.preparation);
		assertParticipant(sut, true, true, false, true, false, false);
		sut = sut(false, false, true, false, RepositoryEntryStatusEnum.coachpublished);
		assertParticipant(sut, true, true, false, true, false, false);
		sut = sut(false, false, true, false, RepositoryEntryStatusEnum.published);
		assertParticipant(sut, true, true, false, true, false, false);
		
		sut = sut(false, false, false, true, RepositoryEntryStatusEnum.preparation);
		assertParticipant(sut, false, false, false, false, true, true);
		sut = sut(false, false, false, true, RepositoryEntryStatusEnum.coachpublished);
		assertParticipant(sut, false, false, false, false, true, true);
		sut = sut(false, false, false, true, RepositoryEntryStatusEnum.published);
		assertParticipant(sut, false, false, false, false, true, true);
		
		sut = sut(false, false, false, false, RepositoryEntryStatusEnum.preparation);
		assertParticipant(sut, false, false, false, false, false, true);
		sut = sut(false, false, false, false, RepositoryEntryStatusEnum.coachpublished);
		assertParticipant(sut, false, false, false, false, false, true);
		sut = sut(false, false, false, false, RepositoryEntryStatusEnum.published);
		assertParticipant(sut, false, false, false, false, false, true);
	}
	
	private void assertParticipant(DetailsHeaderConfig sut, boolean openAvailable, boolean openEnabled,
			boolean notPublishedYetMessaage, boolean ownerCoachMessage, boolean adminOpenAvailable,
			boolean offersAvailable) {
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(sut.isOpenAvailable()).isEqualTo(openAvailable);
		softly.assertThat(sut.isOpenEnabled()).isEqualTo(openEnabled);
		softly.assertThat(sut.isBookAvailable()).isFalse();
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
		softly.assertThat(sut.getParticipantsAvailabilityNum().availability()).isEqualTo(ParticipantsAvailability.manyLeft);
		softly.assertThat(sut.getParticipantsAvailabilityNum().numAvailable()).isGreaterThan(10000);
		softly.assertThat(sut.isNotPublishedYetMessage()).isEqualTo(notPublishedYetMessaage);
		softly.assertThat(sut.isNoContentYetMessage()).isFalse();
		softly.assertThat(sut.isConfirmationPendingMessage()).isFalse();
		softly.assertThat(sut.isAvailabilityMessage()).isFalse();
		softly.assertThat(sut.isOwnerCoachMessage()).isEqualTo(ownerCoachMessage);
		softly.assertThat(sut.isAdministrativOpenAvailable()).isEqualTo(adminOpenAvailable);
		softly.assertThat(sut.isAdministrativOpenEnabled()).isEqualTo(adminOpenAvailable);
		softly.assertAll();
	}
	
	private DetailsHeaderConfig sut(boolean participant, boolean coach, boolean owner, boolean admin,
			RepositoryEntryStatusEnum status) {
		Identity identity = admin
				? JunitTestHelper.createAndPersistIdentityAsRndAdmin(ADMIN_PREFIX)
				: JunitTestHelper.createAndPersistIdentityAsRndUser(USER_PREFIX);
		RepositoryEntry repositoryEntry = createRepositoryEntry(status);
		dbInstance.commitAndCloseSession();
		
		if (participant) {
			repositoryService.addRole(identity, repositoryEntry, GroupRoles.participant.name());
		}
		if (coach) {
			repositoryService.addRole(identity, repositoryEntry, GroupRoles.coach.name());
		}
		if (owner) {
			repositoryService.addRole(identity, repositoryEntry, GroupRoles.owner.name());
		}
		dbInstance.commitAndCloseSession();
		
		return sut(repositoryEntry, identity);
	}
	
	private DetailsHeaderConfig sut(RepositoryEntry repositoryEntry, Identity identity) {
		return new CatalogRepositoryEntryHeaderConfig(repositoryEntry, identity, Roles.userRoles());
	}

	private RepositoryEntry createRepositoryEntry(RepositoryEntryStatusEnum status) {
		RepositoryEntry repositoryEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryEntry.setEntryStatus(status);
		repositoryEntry = repositoryService.update(repositoryEntry);
		
		Offer offer = acService.createOffer(repositoryEntry.getOlatResource(), repositoryEntry.getDisplayname());
		offer.setValidFrom(DateUtils.addDays(new Date(), -10));
		offer.setValidTo(DateUtils.addDays(new Date(), 10));
		AccessMethod method = acService.getAvailableMethodsByType(FreeAccessMethod.class).get(0);
		OfferAccess offerAccess = acService.createOfferAccess(offer, method);
		acService.saveOfferAccess(offerAccess);
		acService.updateOfferOrganisations(offer, List.of(JunitTestHelper.getDefaultOrganisation()));
		
		return repositoryEntry;
	}

}

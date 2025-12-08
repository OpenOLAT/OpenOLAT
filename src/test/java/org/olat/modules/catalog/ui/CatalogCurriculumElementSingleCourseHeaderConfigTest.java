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
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.util.DateUtils;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryService;
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
public class CatalogCurriculumElementSingleCourseHeaderConfigTest extends OlatTestCase {
	
	private static final String USER_PREFIX = random();
	private static final String ADMIN_PREFIX = random();
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private ACService acService;
	@Autowired
	private ACReservationDAO reservationDao;
	@Autowired
	private OrganisationService organisationService;

	@Test
	public void shouldGetConfig_noAccess_bookable() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(USER_PREFIX);
		CurriculumElement curriculumElement = createCurriculumElement(CurriculumElementStatus.active);
		RepositoryEntry repositoryEntry = createRepositoryEntry(curriculumElement, RepositoryEntryStatusEnum.preparation);
		dbInstance.commitAndCloseSession();
		
		CatalogCurriculumElementSingleCourseHeaderConfig sut = sut(curriculumElement, repositoryEntry, identity);
		
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
		
		CurriculumElement curriculumElement = createCurriculumElement(CurriculumElementStatus.active);
		RepositoryEntry repositoryEntry = createRepositoryEntry(curriculumElement, RepositoryEntryStatusEnum.preparation);
		dbInstance.commitAndCloseSession();
		
		CatalogCurriculumElementSingleCourseHeaderConfig sut = sut(curriculumElement, repositoryEntry, admin);
		
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
	public void shouldGetConfig_contentAvailable() {
		// participant
		DetailsHeaderConfig sut = sut(true, true, true, true, RepositoryEntryStatusEnum.preparation, false, false);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(true, true, true, true, RepositoryEntryStatusEnum.coachpublished, false, false);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(true, true, true, true, RepositoryEntryStatusEnum.published, false, false);
		assertParticipant(sut, true, true, false, false, false, false, false, false, false);
		
		sut = sut(true, true, true, false, RepositoryEntryStatusEnum.preparation, false, false);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(true, true, true, false, RepositoryEntryStatusEnum.coachpublished, false, false);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(true, true, true, false, RepositoryEntryStatusEnum.published, false, false);
		assertParticipant(sut, true, true, false, false, false, false, false, false, false);
		
		sut = sut(true, true, false, true, RepositoryEntryStatusEnum.preparation, false, false);
		assertParticipant(sut, true, false, false, false, false, true, false, false, true);
		sut = sut(true, true, false, true, RepositoryEntryStatusEnum.coachpublished, false, false);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(true, true, false, true, RepositoryEntryStatusEnum.published, false, false);
		assertParticipant(sut, true, true, false, false, false, false, false, false, false);
		
		sut = sut(true, true, false, false, RepositoryEntryStatusEnum.preparation, false, false);
		assertParticipant(sut, true, false, false, false, false, true, false, false, false);
		sut = sut(true, true, false, false, RepositoryEntryStatusEnum.coachpublished, false, false);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(true, true, false, false, RepositoryEntryStatusEnum.published, false, false);
		assertParticipant(sut, true, true, false, false, false, false, false, false, false);
		
		sut = sut(true, false, true, true, RepositoryEntryStatusEnum.preparation, false, false);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(true, false, true, true, RepositoryEntryStatusEnum.coachpublished, false, false);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(true, false, true, true, RepositoryEntryStatusEnum.published, false, false);
		assertParticipant(sut, true, true, false, false, false, false, false, false, false);
		
		sut = sut(true, false, true, false, RepositoryEntryStatusEnum.preparation, false, false);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(true, false, true, false, RepositoryEntryStatusEnum.coachpublished, false, false);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(true, false, true, false, RepositoryEntryStatusEnum.published, false, false);
		assertParticipant(sut, true, true, false, false, false, false, false, false, false);
		
		sut = sut(true, false, false, true, RepositoryEntryStatusEnum.preparation, false, false);
		assertParticipant(sut, true, false, false, false, false, true, false, false, true);
		sut = sut(true, false, false, true, RepositoryEntryStatusEnum.coachpublished, false, false);
		assertParticipant(sut, true, false, false, false, false, true, false, false, true);
		sut = sut(true, false, false, true, RepositoryEntryStatusEnum.published, false, false);
		assertParticipant(sut, true, true, false, false, false, false, false, false, false);
		
		sut = sut(true, false, false, false, RepositoryEntryStatusEnum.preparation, false, false);
		assertParticipant(sut, true, false, false, false, false, true, false, false, false);
		sut = sut(true, false, false, false, RepositoryEntryStatusEnum.coachpublished, false, false);
		assertParticipant(sut, true, false, false, false, false, true, false, false, false);
		sut = sut(true, false, false, false, RepositoryEntryStatusEnum.published, false, false);
		assertParticipant(sut, true, true, false, false, false, false, false, false, false);
		
		// not participant
		sut = sut(false, true, true, true, RepositoryEntryStatusEnum.preparation, false, false);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(false, true, true, true, RepositoryEntryStatusEnum.coachpublished, false, false);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(false, true, true, true, RepositoryEntryStatusEnum.published, false, false);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		
		sut = sut(false, true, true, false, RepositoryEntryStatusEnum.preparation, false, false);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(false, true, true, false, RepositoryEntryStatusEnum.coachpublished, false, false);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(false, true, true, false, RepositoryEntryStatusEnum.published, false, false);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		
		sut = sut(false, true, false, true, RepositoryEntryStatusEnum.preparation, false, false);
		assertParticipant(sut, true, false, false, false, false, true, false, true, true);
		sut = sut(false, true, false, true, RepositoryEntryStatusEnum.coachpublished, false, false);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(false, true, false, true, RepositoryEntryStatusEnum.published, false, false);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		
		sut = sut(false, true, false, false, RepositoryEntryStatusEnum.preparation, false, false);
		assertParticipant(sut, true, false, false, false, false, true, false, true, false);
		sut = sut(false, true, false, false, RepositoryEntryStatusEnum.coachpublished, false, false);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(false, true, false, false, RepositoryEntryStatusEnum.published, false, false);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		
		sut = sut(false, false, true, true, RepositoryEntryStatusEnum.preparation, false, false);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(false, false, true, true, RepositoryEntryStatusEnum.coachpublished, false, false);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(false, false, true, true, RepositoryEntryStatusEnum.published, false, false);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		
		sut = sut(false, false, true, false, RepositoryEntryStatusEnum.preparation, false, false);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(false, false, true, false, RepositoryEntryStatusEnum.coachpublished, false, false);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(false, false, true, false, RepositoryEntryStatusEnum.published, false, false);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		
		sut = sut(false, false, false, true, RepositoryEntryStatusEnum.preparation, false, false);
		assertParticipant(sut, false, false, false, true, false, false, false, false, true);
		sut = sut(false, false, false, true, RepositoryEntryStatusEnum.coachpublished, false, false);
		assertParticipant(sut, false, false, false, true, false, false, false, false, true);
		sut = sut(false, false, false, true, RepositoryEntryStatusEnum.published, false, false);
		assertParticipant(sut, false, false, false, true, false, false, false, false, true);
		
		sut = sut(false, false, false, false, RepositoryEntryStatusEnum.preparation, false, false);
		assertParticipant(sut, false, false, false, true, false, false, false, false, false);
		sut = sut(false, false, false, false, RepositoryEntryStatusEnum.coachpublished, false, false);
		assertParticipant(sut, false, false, false, true, false, false, false, false, false);
		sut = sut(false, false, false, false, RepositoryEntryStatusEnum.published, false, false);
		assertParticipant(sut, false, false, false, true, false, false, false, false, false);
	}
	
	@Test
	public void shouldGetConfig_contentAvailable_reservationAvailable() {
		// participant
		DetailsHeaderConfig sut = sut(true, true, true, true, RepositoryEntryStatusEnum.preparation, true, false);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(true, true, true, true, RepositoryEntryStatusEnum.coachpublished, true, false);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(true, true, true, true, RepositoryEntryStatusEnum.published, true, false);
		assertParticipant(sut, true, true, false, false, false, false, false, false, false);
		
		sut = sut(true, true, true, false, RepositoryEntryStatusEnum.preparation, true, false);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(true, true, true, false, RepositoryEntryStatusEnum.coachpublished, true, false);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(true, true, true, false, RepositoryEntryStatusEnum.published, true, false);
		assertParticipant(sut, true, true, false, false, false, false, false, false, false);
		
		sut = sut(true, true, false, true, RepositoryEntryStatusEnum.preparation, true, false);
		assertParticipant(sut, true, false, false, false, false, true, false, false, true);
		sut = sut(true, true, false, true, RepositoryEntryStatusEnum.coachpublished, true, false);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(true, true, false, true, RepositoryEntryStatusEnum.published, true, false);
		assertParticipant(sut, true, true, false, false, false, false, false, false, false);
		
		sut = sut(true, true, false, false, RepositoryEntryStatusEnum.preparation, true, false);
		assertParticipant(sut, true, false, false, false, false, true, false, false, false);
		sut = sut(true, true, false, false, RepositoryEntryStatusEnum.coachpublished, true, false);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(true, true, false, false, RepositoryEntryStatusEnum.published, true, false);
		assertParticipant(sut, true, true, false, false, false, false, false, false, false);
		
		sut = sut(true, false, true, true, RepositoryEntryStatusEnum.preparation, true, false);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(true, false, true, true, RepositoryEntryStatusEnum.coachpublished, true, false);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(true, false, true, true, RepositoryEntryStatusEnum.published, true, false);
		assertParticipant(sut, true, true, false, false, false, false, false, false, false);
		
		sut = sut(true, false, true, false, RepositoryEntryStatusEnum.preparation, true, false);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(true, false, true, false, RepositoryEntryStatusEnum.coachpublished, true, false);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(true, false, true, false, RepositoryEntryStatusEnum.published, true, false);
		assertParticipant(sut, true, true, false, false, false, false, false, false, false);
		
		sut = sut(true, false, false, true, RepositoryEntryStatusEnum.preparation, true, false);
		assertParticipant(sut, true, false, false, false, false, true, false, false, true);
		sut = sut(true, false, false, true, RepositoryEntryStatusEnum.coachpublished, true, false);
		assertParticipant(sut, true, false, false, false, false, true, false, false, true);
		sut = sut(true, false, false, true, RepositoryEntryStatusEnum.published, true, false);
		assertParticipant(sut, true, true, false, false, false, false, false, false, false);
		
		sut = sut(true, false, false, false, RepositoryEntryStatusEnum.preparation, true, false);
		assertParticipant(sut, true, false, false, false, false, true, false, false, false);
		sut = sut(true, false, false, false, RepositoryEntryStatusEnum.coachpublished, true, false);
		assertParticipant(sut, true, false, false, false, false, true, false, false, false);
		sut = sut(true, false, false, false, RepositoryEntryStatusEnum.published, true, false);
		assertParticipant(sut, true, true, false, false, false, false, false, false, false);
		
		// not participant
		sut = sut(false, true, true, true, RepositoryEntryStatusEnum.preparation, true, false);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(false, true, true, true, RepositoryEntryStatusEnum.coachpublished, true, false);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(false, true, true, true, RepositoryEntryStatusEnum.published, true, false);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		
		sut = sut(false, true, true, false, RepositoryEntryStatusEnum.preparation, true, false);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(false, true, true, false, RepositoryEntryStatusEnum.coachpublished, true, false);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(false, true, true, false, RepositoryEntryStatusEnum.published, true, false);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		
		sut = sut(false, true, false, true, RepositoryEntryStatusEnum.preparation, true, false);
		assertParticipant(sut, true, false, false, false, false, true, false, true, true);
		sut = sut(false, true, false, true, RepositoryEntryStatusEnum.coachpublished, true, false);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(false, true, false, true, RepositoryEntryStatusEnum.published, true, false);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		
		sut = sut(false, true, false, false, RepositoryEntryStatusEnum.preparation, true, false);
		assertParticipant(sut, true, false, false, false, false, true, false, true, false);
		sut = sut(false, true, false, false, RepositoryEntryStatusEnum.coachpublished, true, false);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(false, true, false, false, RepositoryEntryStatusEnum.published, true, false);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		
		sut = sut(false, false, true, true, RepositoryEntryStatusEnum.preparation, true, false);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(false, false, true, true, RepositoryEntryStatusEnum.coachpublished, true, false);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(false, false, true, true, RepositoryEntryStatusEnum.published, true, false);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		
		sut = sut(false, false, true, false, RepositoryEntryStatusEnum.preparation, true, false);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(false, false, true, false, RepositoryEntryStatusEnum.coachpublished, true, false);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(false, false, true, false, RepositoryEntryStatusEnum.published, true, false);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		
		sut = sut(false, false, false, true, RepositoryEntryStatusEnum.preparation, true, false);
		assertParticipant(sut, true, false, false, false, false, false, true, false, true);
		sut = sut(false, false, false, true, RepositoryEntryStatusEnum.coachpublished, true, false);
		assertParticipant(sut, true, false, false, false, false, false, true, false, true);
		sut = sut(false, false, false, true, RepositoryEntryStatusEnum.published, true, false);
		assertParticipant(sut, true, false, false, false, false, false, true, false, true);
		
		sut = sut(false, false, false, false, RepositoryEntryStatusEnum.preparation, true, false);
		assertParticipant(sut, true, false, false, false, false, false, true, false, false);
		sut = sut(false, false, false, false, RepositoryEntryStatusEnum.coachpublished, true, false);
		assertParticipant(sut, true, false, false, false, false, false, true, false, false);
		sut = sut(false, false, false, false, RepositoryEntryStatusEnum.published, true, false);
		assertParticipant(sut, true, false, false, false, false, false, true, false, false);
		
		// ... and fully booked
		sut = sut(false, false, false, false, RepositoryEntryStatusEnum.preparation, true, true);
		assertParticipant(sut, true, false, false, false, false, false, true, false, false);
		sut = sut(false, false, false, false, RepositoryEntryStatusEnum.coachpublished, true, true);
		assertParticipant(sut, true, false, false, false, false, false, true, false, false);
		sut = sut(false, false, false, false, RepositoryEntryStatusEnum.published, true, true);
		assertParticipant(sut, true, false, false, false, false, false, true, false, false);
	}
	
	@Test
	public void shouldGetConfig_contentAvailable_fullyBooked() {
		// participant
		DetailsHeaderConfig sut = sut(true, true, true, true, RepositoryEntryStatusEnum.preparation, false, true);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(true, true, true, true, RepositoryEntryStatusEnum.coachpublished, false, true);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(true, true, true, true, RepositoryEntryStatusEnum.published, false, true);
		assertParticipant(sut, true, true, false, false, false, false, false, false, false);
		
		sut = sut(true, true, true, false, RepositoryEntryStatusEnum.preparation, false, true);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(true, true, true, false, RepositoryEntryStatusEnum.coachpublished, false, true);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(true, true, true, false, RepositoryEntryStatusEnum.published, false, true);
		assertParticipant(sut, true, true, false, false, false, false, false, false, false);
		
		sut = sut(true, true, false, true, RepositoryEntryStatusEnum.preparation, false, true);
		assertParticipant(sut, true, false, false, false, false, true, false, false, true);
		sut = sut(true, true, false, true, RepositoryEntryStatusEnum.coachpublished, false, true);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(true, true, false, true, RepositoryEntryStatusEnum.published, false, true);
		assertParticipant(sut, true, true, false, false, false, false, false, false, false);
		
		sut = sut(true, true, false, false, RepositoryEntryStatusEnum.preparation, false, true);
		assertParticipant(sut, true, false, false, false, false, true, false, false, false);
		sut = sut(true, true, false, false, RepositoryEntryStatusEnum.coachpublished, false, true);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(true, true, false, false, RepositoryEntryStatusEnum.published, false, true);
		assertParticipant(sut, true, true, false, false, false, false, false, false, false);
		
		sut = sut(true, false, true, true, RepositoryEntryStatusEnum.preparation, false, true);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(true, false, true, true, RepositoryEntryStatusEnum.coachpublished, false, true);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(true, false, true, true, RepositoryEntryStatusEnum.published, false, true);
		assertParticipant(sut, true, true, false, false, false, false, false, false, false);
		
		sut = sut(true, false, true, false, RepositoryEntryStatusEnum.preparation, false, true);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(true, false, true, false, RepositoryEntryStatusEnum.coachpublished, false, true);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(true, false, true, false, RepositoryEntryStatusEnum.published, false, true);
		assertParticipant(sut, true, true, false, false, false, false, false, false, false);
		
		sut = sut(true, false, false, true, RepositoryEntryStatusEnum.preparation, false, true);
		assertParticipant(sut, true, false, false, false, false, true, false, false, true);
		sut = sut(true, false, false, true, RepositoryEntryStatusEnum.coachpublished, false, true);
		assertParticipant(sut, true, false, false, false, false, true, false, false, true);
		sut = sut(true, false, false, true, RepositoryEntryStatusEnum.published, false, true);
		assertParticipant(sut, true, true, false, false, false, false, false, false, false);
		
		sut = sut(true, false, false, false, RepositoryEntryStatusEnum.preparation, false, true);
		assertParticipant(sut, true, false, false, false, false, true, false, false, false);
		sut = sut(true, false, false, false, RepositoryEntryStatusEnum.coachpublished, false, true);
		assertParticipant(sut, true, false, false, false, false, true, false, false, false);
		sut = sut(true, false, false, false, RepositoryEntryStatusEnum.published, false, true);
		assertParticipant(sut, true, true, false, false, false, false, false, false, false);
		
		// not participant
		sut = sut(false, true, true, true, RepositoryEntryStatusEnum.preparation, false, true);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(false, true, true, true, RepositoryEntryStatusEnum.coachpublished, false, true);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(false, true, true, true, RepositoryEntryStatusEnum.published, false, true);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		
		sut = sut(false, true, true, false, RepositoryEntryStatusEnum.preparation, false, true);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(false, true, true, false, RepositoryEntryStatusEnum.coachpublished, false, true);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(false, true, true, false, RepositoryEntryStatusEnum.published, false, true);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		
		sut = sut(false, true, false, true, RepositoryEntryStatusEnum.preparation, false, true);
		assertParticipant(sut, true, false, false, false, false, true, false, true, true);
		sut = sut(false, true, false, true, RepositoryEntryStatusEnum.coachpublished, false, true);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(false, true, false, true, RepositoryEntryStatusEnum.published, false, true);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		
		sut = sut(false, true, false, false, RepositoryEntryStatusEnum.preparation, false, true);
		assertParticipant(sut, true, false, false, false, false, true, false, true, false);
		sut = sut(false, true, false, false, RepositoryEntryStatusEnum.coachpublished, false, true);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(false, true, false, false, RepositoryEntryStatusEnum.published, false, true);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		
		sut = sut(false, false, true, true, RepositoryEntryStatusEnum.preparation, false, true);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(false, false, true, true, RepositoryEntryStatusEnum.coachpublished, false, true);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(false, false, true, true, RepositoryEntryStatusEnum.published, false, true);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		
		sut = sut(false, false, true, false, RepositoryEntryStatusEnum.preparation, false, true);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(false, false, true, false, RepositoryEntryStatusEnum.coachpublished, false, true);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		sut = sut(false, false, true, false, RepositoryEntryStatusEnum.published, false, true);
		assertParticipant(sut, true, true, false, false, false, false, false, true, false);
		
		sut = sut(false, false, false, true, RepositoryEntryStatusEnum.preparation, false, true);
		assertParticipant(sut, false, false, true, false, true, false, false, false, true);
		sut = sut(false, false, false, true, RepositoryEntryStatusEnum.coachpublished, false, true);
		assertParticipant(sut, false, false, true, false, true, false, false, false, true);
		sut = sut(false, false, false, true, RepositoryEntryStatusEnum.published, false, true);
		assertParticipant(sut, false, false, true, false, true, false, false, false, true);
		
		sut = sut(false, false, false, false, RepositoryEntryStatusEnum.preparation, false, true);
		assertParticipant(sut, false, false, true, false, true, false, false, false, false);
		sut = sut(false, false, false, false, RepositoryEntryStatusEnum.coachpublished, false, true);
		assertParticipant(sut, false, false, true, false, true, false, false, false, false);
		sut = sut(false, false, false, false, RepositoryEntryStatusEnum.published, false, true);
		assertParticipant(sut, false, false, true, false, true, false, false, false, false);
	}
	
	@Test
	public void shouldGetConfig_noContentAvailable() {
		// participant
		DetailsHeaderConfig sut = sut(true, true, true, true, null, false, false);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		sut = sut(true, true, true, true, null, false, false);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		sut = sut(true, true, true, true, null, false, false);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		
		sut = sut(true, true, true, false, null, false, false);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		sut = sut(true, true, true, false, null, false, false);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		sut = sut(true, true, true, false, null, false, false);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		
		sut = sut(true, true, false, true, null, false, false);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		sut = sut(true, true, false, true, null, false, false);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		sut = sut(true, true, false, true, null, false, false);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		
		sut = sut(true, true, false, false, null, false, false);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		sut = sut(true, true, false, false, null, false, false);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		sut = sut(true, true, false, false, null, false, false);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		
		sut = sut(true, false, true, true, null, false, false);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		sut = sut(true, false, true, true, null, false, false);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		sut = sut(true, false, true, true, null, false, false);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		
		sut = sut(true, false, true, false, null, false, false);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		sut = sut(true, false, true, false, null, false, false);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		sut = sut(true, false, true, false, null, false, false);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		
		sut = sut(true, false, false, true, null, false, false);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		sut = sut(true, false, false, true, null, false, false);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		sut = sut(true, false, false, true, null, false, false);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		
		sut = sut(true, false, false, false, null, false, false);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		sut = sut(true, false, false, false, null, false, false);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		sut = sut(true, false, false, false, null, false, false);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		
		// not participant
		sut = sut(false, true, true, true, null, false, false);
		assertParticipantNo(sut, true, false, false, false, true, false, true);
		sut = sut(false, true, true, true, null, false, false);
		assertParticipantNo(sut, true, false, false, false, true, false, true);
		sut = sut(false, true, true, true, null, false, false);
		assertParticipantNo(sut, true, false, false, false, true, false, true);
		
		sut = sut(false, true, true, false, null, false, false);
		assertParticipantNo(sut, true, false, false, false, true, false, true);
		sut = sut(false, true, true, false, null, false, false);
		assertParticipantNo(sut, true, false, false, false, true, false, true);
		sut = sut(false, true, true, false, null, false, false);
		assertParticipantNo(sut, true, false, false, false, true, false, true);
		
		sut = sut(false, true, false, true, null, false, false);
		assertParticipantNo(sut, true, false, false, false, true, false, true);
		sut = sut(false, true, false, true, null, false, false);
		assertParticipantNo(sut, true, false, false, false, true, false, true);
		sut = sut(false, true, false, true, null, false, false);
		assertParticipantNo(sut, true, false, false, false, true, false, true);
		
		sut = sut(false, true, false, false, null, false, false);
		assertParticipantNo(sut, true, false, false, false, true, false, true);
		sut = sut(false, true, false, false, null, false, false);
		assertParticipantNo(sut, true, false, false, false, true, false, true);
		sut = sut(false, true, false, false, null, false, false);
		assertParticipantNo(sut, true, false, false, false, true, false, true);
		
		sut = sut(false, false, true, true, null, false, false);
		assertParticipantNo(sut, true, false, false, false, true, false, true);
		sut = sut(false, false, true, true, null, false, false);
		assertParticipantNo(sut, true, false, false, false, true, false, true);
		sut = sut(false, false, true, true, null, false, false);
		assertParticipantNo(sut, true, false, false, false, true, false, true);
		
		sut = sut(false, false, true, false, null, false, false);
		assertParticipantNo(sut, true, false, false, false, true, false, true);
		sut = sut(false, false, true, false, null, false, false);
		assertParticipantNo(sut, true, false, false, false, true, false, true);
		sut = sut(false, false, true, false, null, false, false);
		assertParticipantNo(sut, true, false, false, false, true, false, true);
		
		sut = sut(false, false, false, true, null, false, false);
		assertParticipantNo(sut, false, false, true, false, false, false, false);
		sut = sut(false, false, false, true, null, false, false);
		assertParticipantNo(sut, false, false, true, false, false, false, false);
		sut = sut(false, false, false, true, null, false, false);
		assertParticipantNo(sut, false, false, true, false, false, false, false);
		
		sut = sut(false, false, false, false, null, false, false);
		assertParticipantNo(sut, false, false, true, false, false, false, false);
		sut = sut(false, false, false, false, null, false, false);
		assertParticipantNo(sut, false, false, true, false, false, false, false);
		sut = sut(false, false, false, false, null, false, false);
		assertParticipantNo(sut, false, false, true, false, false, false, false);
	}
	
	@Test
	public void shouldGetConfig_noContentAvailable_reservationAvailable() {
		// participant
		DetailsHeaderConfig sut = sut(true, true, true, true, null, true, false);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		sut = sut(true, true, true, true, null, true, false);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		sut = sut(true, true, true, true, null, true, false);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		
		sut = sut(true, true, true, false, null, true, false);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		sut = sut(true, true, true, false, null, true, false);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		sut = sut(true, true, true, false, null, true, false);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		
		sut = sut(true, true, false, true, null, true, false);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		sut = sut(true, true, false, true, null, true, false);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		sut = sut(true, true, false, true, null, true, false);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		
		sut = sut(true, true, false, false, null, true, false);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		sut = sut(true, true, false, false, null, true, false);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		sut = sut(true, true, false, false, null, true, false);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		
		sut = sut(true, false, true, true, null, true, false);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		sut = sut(true, false, true, true, null, true, false);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		sut = sut(true, false, true, true, null, true, false);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		
		sut = sut(true, false, true, false, null, true, false);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		sut = sut(true, false, true, false, null, true, false);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		sut = sut(true, false, true, false, null, true, false);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		
		sut = sut(true, false, false, true, null, true, false);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		sut = sut(true, false, false, true, null, true, false);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		sut = sut(true, false, false, true, null, true, false);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		
		sut = sut(true, false, false, false, null, true, false);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		sut = sut(true, false, false, false, null, true, false);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		sut = sut(true, false, false, false, null, true, false);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		
		// not participant
		sut = sut(false, true, true, true, null, true, false);
		assertParticipantNo(sut, true, false, false, false, true, false, true);
		sut = sut(false, true, true, true, null, true, false);
		assertParticipantNo(sut, true, false, false, false, true, false, true);
		sut = sut(false, true, true, true, null, true, false);
		assertParticipantNo(sut, true, false, false, false, true, false, true);
		
		sut = sut(false, true, true, false, null, true, false);
		assertParticipantNo(sut, true, false, false, false, true, false, true);
		sut = sut(false, true, true, false, null, true, false);
		assertParticipantNo(sut, true, false, false, false, true, false, true);
		sut = sut(false, true, true, false, null, true, false);
		assertParticipantNo(sut, true, false, false, false, true, false, true);
		
		sut = sut(false, true, false, true, null, true, false);
		assertParticipantNo(sut, true, false, false, false, true, false, true);
		sut = sut(false, true, false, true, null, true, false);
		assertParticipantNo(sut, true, false, false, false, true, false, true);
		sut = sut(false, true, false, true, null, true, false);
		assertParticipantNo(sut, true, false, false, false, true, false, true);
		
		sut = sut(false, true, false, false, null, true, false);
		assertParticipantNo(sut, true, false, false, false, true, false, true);
		sut = sut(false, true, false, false, null, true, false);
		assertParticipantNo(sut, true, false, false, false, true, false, true);
		sut = sut(false, true, false, false, null, true, false);
		assertParticipantNo(sut, true, false, false, false, true, false, true);
		
		sut = sut(false, false, true, true, null, true, false);
		assertParticipantNo(sut, true, false, false, false, true, false, true);
		sut = sut(false, false, true, true, null, true, false);
		assertParticipantNo(sut, true, false, false, false, true, false, true);
		sut = sut(false, false, true, true, null, true, false);
		assertParticipantNo(sut, true, false, false, false, true, false, true);
		
		sut = sut(false, false, true, false, null, true, false);
		assertParticipantNo(sut, true, false, false, false, true, false, true);
		sut = sut(false, false, true, false, null, true, false);
		assertParticipantNo(sut, true, false, false, false, true, false, true);
		sut = sut(false, false, true, false, null, true, false);
		assertParticipantNo(sut, true, false, false, false, true, false, true);
		
		sut = sut(false, false, false, true, null, true, false);
		assertParticipantNo(sut, true, false, false, false, false, true, false);
		sut = sut(false, false, false, true, null, true, false);
		assertParticipantNo(sut, true, false, false, false, false, true, false);
		sut = sut(false, false, false, true, null, true, false);
		assertParticipantNo(sut, true, false, false, false, false, true, false);
		
		sut = sut(false, false, false, false, null, true, false);
		assertParticipantNo(sut, true, false, false, false, false, true, false);
		sut = sut(false, false, false, false, null, true, false);
		assertParticipantNo(sut, true, false, false, false, false, true, false);
		sut = sut(false, false, false, false, null, true, false);
		assertParticipantNo(sut, true, false, false, false, false, true, false);
		
		// ... and fully booked
		sut = sut(false, false, false, false, null, true, true);
		assertParticipantNo(sut, true, false, false, false, false, true, false);
		sut = sut(false, false, false, false, null, true, true);
		assertParticipantNo(sut, true, false, false, false, false, true, false);
		sut = sut(false, false, false, false, null, true, true);
		assertParticipantNo(sut, true, false, false, false, false, true, false);
	}
	
	@Test
	public void shouldGetConfig_noContentAvailable_fullyBooked() {
		// participant
		DetailsHeaderConfig sut = sut(true, true, true, true, null, false, true);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		sut = sut(true, true, true, true, null, false, true);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		sut = sut(true, true, true, true, null, false, true);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		
		sut = sut(true, true, true, false, null, false, true);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		sut = sut(true, true, true, false, null, false, true);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		sut = sut(true, true, true, false, null, false, true);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		
		sut = sut(true, true, false, true, null, false, true);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		sut = sut(true, true, false, true, null, false, true);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		sut = sut(true, true, false, true, null, false, true);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		
		sut = sut(true, true, false, false, null, false, true);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		sut = sut(true, true, false, false, null, false, true);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		sut = sut(true, true, false, false, null, false, true);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		
		sut = sut(true, false, true, true, null, false, true);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		sut = sut(true, false, true, true, null, false, true);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		sut = sut(true, false, true, true, null, false, true);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		
		sut = sut(true, false, true, false, null, false, true);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		sut = sut(true, false, true, false, null, false, true);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		sut = sut(true, false, true, false, null, false, true);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		
		sut = sut(true, false, false, true, null, false, true);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		sut = sut(true, false, false, true, null, false, true);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		sut = sut(true, false, false, true, null, false, true);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		
		sut = sut(true, false, false, false, null, false, true);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		sut = sut(true, false, false, false, null, false, true);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		sut = sut(true, false, false, false, null, false, true);
		assertParticipantNo(sut, true, false, false, false, true, false, false);
		
		// not participant
		sut = sut(false, true, true, true, null, false, true);
		assertParticipantNo(sut, true, false, false, false, true, false, true);
		sut = sut(false, true, true, true, null, false, true);
		assertParticipantNo(sut, true, false, false, false, true, false, true);
		sut = sut(false, true, true, true, null, false, true);
		assertParticipantNo(sut, true, false, false, false, true, false, true);
		
		sut = sut(false, true, true, false, null, false, true);
		assertParticipantNo(sut, true, false, false, false, true, false, true);
		sut = sut(false, true, true, false, null, false, true);
		assertParticipantNo(sut, true, false, false, false, true, false, true);
		sut = sut(false, true, true, false, null, false, true);
		assertParticipantNo(sut, true, false, false, false, true, false, true);
		
		sut = sut(false, true, false, true, null, false, true);
		assertParticipantNo(sut, true, false, false, false, true, false, true);
		sut = sut(false, true, false, true, null, false, true);
		assertParticipantNo(sut, true, false, false, false, true, false, true);
		sut = sut(false, true, false, true, null, false, true);
		assertParticipantNo(sut, true, false, false, false, true, false, true);
		
		sut = sut(false, true, false, false, null, false, true);
		assertParticipantNo(sut, true, false, false, false, true, false, true);
		sut = sut(false, true, false, false, null, false, true);
		assertParticipantNo(sut, true, false, false, false, true, false, true);
		sut = sut(false, true, false, false, null, false, true);
		assertParticipantNo(sut, true, false, false, false, true, false, true);
		
		sut = sut(false, false, true, true, null, false, true);
		assertParticipantNo(sut, true, false, false, false, true, false, true);
		sut = sut(false, false, true, true, null, false, true);
		assertParticipantNo(sut, true, false, false, false, true, false, true);
		sut = sut(false, false, true, true, null, false, true);
		assertParticipantNo(sut, true, false, false, false, true, false, true);
		
		sut = sut(false, false, true, false, null, false, true);
		assertParticipantNo(sut, true, false, false, false, true, false, true);
		sut = sut(false, false, true, false, null, false, true);
		assertParticipantNo(sut, true, false, false, false, true, false, true);
		sut = sut(false, false, true, false, null, false, true);
		assertParticipantNo(sut, true, false, false, false, true, false, true);
		
		sut = sut(false, false, false, true, null, false, true);
		assertParticipantNo(sut, false, true, false, true, false, false, false);
		sut = sut(false, false, false, false, null, false, true);
		assertParticipantNo(sut, false, true, false, true, false, false, false);
		sut = sut(false, false, false, false, null, false, true);
		assertParticipantNo(sut, false, true, false, true, false, false, false);
		
		sut = sut(false, false, false, false, null, false, true);
		assertParticipantNo(sut, false, true, false, true, false, false, false);
		sut = sut(false, false, false, false, null, false, true);
		assertParticipantNo(sut, false, true, false, true, false, false, false);
		sut = sut(false, false, false, false, null, false, true);
		assertParticipantNo(sut, false, true, false, true, false, false, false);
	}
	
	private void assertParticipant(DetailsHeaderConfig sut, boolean openAvailable, boolean openEnabled,
			boolean bookAvailable, boolean offersAvailable, boolean fullyBookedMessage, boolean notPublishedYetMessaage,
			boolean confirmationPendingMessage, boolean ownerCoachMessage, boolean adminOpenAvailable) {
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(sut.isOpenAvailable()).isEqualTo(openAvailable);
		softly.assertThat(sut.isOpenEnabled()).isEqualTo(openEnabled);
		softly.assertThat(sut.isBookAvailable()).isEqualTo(bookAvailable);
		softly.assertThat(sut.isBookEnabled()).isEqualTo(false);
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
		softly.assertThat(sut.isNotPublishedYetMessage()).isEqualTo(notPublishedYetMessaage);
		softly.assertThat(sut.isNoContentYetMessage()).isFalse();
		softly.assertThat(sut.isConfirmationPendingMessage()).isEqualTo(confirmationPendingMessage);
		softly.assertThat(sut.isOwnerCoachMessage()).isEqualTo(ownerCoachMessage);
		softly.assertThat(sut.isAdministrativOpenAvailable()).isEqualTo(adminOpenAvailable);
		softly.assertThat(sut.isAdministrativOpenEnabled()).isEqualTo(adminOpenAvailable);
		softly.assertAll();
	}
	
	private void assertParticipantNo(DetailsHeaderConfig sut, boolean openAvailable, boolean bookAvailable,
			boolean offersAvailable, boolean fullyBookedMessage, boolean noContentMessaage, boolean confirmationPendingMessage,
			boolean ownerCoachMessage) {
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(sut.isOpenAvailable()).isEqualTo(openAvailable);
		softly.assertThat(sut.isOpenEnabled()).isFalse();
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
		softly.assertThat(sut.isNotPublishedYetMessage()).isFalse();
		softly.assertThat(sut.isNoContentYetMessage()).isEqualTo(noContentMessaage);
		softly.assertThat(sut.isConfirmationPendingMessage()).isEqualTo(confirmationPendingMessage);
		softly.assertThat(sut.isOwnerCoachMessage()).isEqualTo(ownerCoachMessage);
		softly.assertThat(sut.isAdministrativOpenAvailable()).isFalse();
		softly.assertThat(sut.isAdministrativOpenEnabled()).isFalse();
		softly.assertAll();
	}
	
	private DetailsHeaderConfig sut(boolean participant, boolean coach, boolean owner, boolean admin,
			RepositoryEntryStatusEnum status, boolean reservationAvailable, boolean fullyBooked) {
		Identity identity = admin
				? JunitTestHelper.createAndPersistIdentityAsRndAdmin(ADMIN_PREFIX)
				: JunitTestHelper.createAndPersistIdentityAsRndUser(USER_PREFIX);
		CurriculumElement curriculumElement = createCurriculumElement(CurriculumElementStatus.active);
		RepositoryEntry repositoryEntry = null;
		if (status != null) {
			repositoryEntry = createRepositoryEntry(curriculumElement, status);
		}
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
		
		return sut(curriculumElement, repositoryEntry, identity);
	}
	
	private CatalogCurriculumElementSingleCourseHeaderConfig sut(CurriculumElement curriculumElement, RepositoryEntry repositoryEntry, Identity identity) {
		return new CatalogCurriculumElementSingleCourseHeaderConfig(curriculumElement, repositoryEntry, identity, Roles.userRoles());
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
	
	private RepositoryEntry createRepositoryEntry(CurriculumElement curriculumElement, RepositoryEntryStatusEnum status) {
		RepositoryEntry repositoryEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryEntry.setEntryStatus(status);
		repositoryEntry = repositoryService.update(repositoryEntry);
		curriculumService.addRepositoryEntry(curriculumElement, repositoryEntry, false);
		return repositoryEntry;
	}

}

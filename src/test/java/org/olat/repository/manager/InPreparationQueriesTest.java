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
package org.olat.repository.manager;

import static org.olat.test.JunitTestHelper.random;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
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
import org.olat.repository.model.CurriculumElementInPreparation;
import org.olat.repository.model.RepositoryEntryInPreparation;
import org.olat.resource.accesscontrol.ConfirmationByEnum;
import org.olat.resource.accesscontrol.manager.ACReservationDAO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14 mars 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class InPreparationQueriesTest extends OlatTestCase {
	
	private static final String USER_PREFIX = random();
	private static final List<CurriculumRoles> CE_PARTICIPANT = List.of(CurriculumRoles.participant);
	private static final List<GroupRoles> RE_PARTICIPANT = List.of(GroupRoles.participant);
	private static final List<GroupRoles> RE_COACH = List.of(GroupRoles.coach);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private InPreparationQueries inPreparationQueries;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private ACReservationDAO reservationDao;

	@Test
	public void searchRepositoryEntriesInPreparation() {
		// It does not matter whether the entry is part of an element or whether the
		// user has a membership in the entry or element! So no according tests.
		
		// No membership
		searchEntry(RepositoryEntryStatusEnum.preparation, List.of(), false, false);
		searchEntry(RepositoryEntryStatusEnum.preparation, List.of(), true, false);
		
		// Participant (participants only)
		searchEntry(RepositoryEntryStatusEnum.preparation, RE_PARTICIPANT, true, true);
		searchEntry(RepositoryEntryStatusEnum.review, RE_PARTICIPANT, true, true);
		searchEntry(RepositoryEntryStatusEnum.coachpublished, RE_PARTICIPANT, true, true);
		searchEntry(RepositoryEntryStatusEnum.published, RE_PARTICIPANT, false, false);
		searchEntry(RepositoryEntryStatusEnum.closed, RE_PARTICIPANT, false, false);
		
		// Participant (not participants only)
		searchEntry(RepositoryEntryStatusEnum.preparation, RE_PARTICIPANT, false, true);
		searchEntry(RepositoryEntryStatusEnum.review, RE_PARTICIPANT, false, true);
		searchEntry(RepositoryEntryStatusEnum.coachpublished, RE_PARTICIPANT, false, true);
		searchEntry(RepositoryEntryStatusEnum.published, RE_PARTICIPANT, false, false);
		searchEntry(RepositoryEntryStatusEnum.closed, RE_PARTICIPANT, false, false);
		
		// Coach (participants only)
		searchEntry(RepositoryEntryStatusEnum.preparation, RE_COACH, true, false);
		searchEntry(RepositoryEntryStatusEnum.review, RE_COACH, true, false);
		searchEntry(RepositoryEntryStatusEnum.coachpublished, RE_COACH, true, false);
		searchEntry(RepositoryEntryStatusEnum.published, RE_COACH, false, false);
		searchEntry(RepositoryEntryStatusEnum.closed, RE_COACH, false, false);
		
		// Coach (not participants only)
		searchEntry(RepositoryEntryStatusEnum.preparation, RE_COACH, false, true);
		searchEntry(RepositoryEntryStatusEnum.review, RE_COACH, false, true);
		searchEntry(RepositoryEntryStatusEnum.coachpublished, RE_COACH, false, true);
		searchEntry(RepositoryEntryStatusEnum.published, RE_COACH, false, false);
		searchEntry(RepositoryEntryStatusEnum.closed, RE_COACH, false, false);
	}
	
	private void searchEntry(RepositoryEntryStatusEnum entryStatus, Collection<GroupRoles> entryRoles,
			boolean participantsOnly, boolean assertFound) {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(USER_PREFIX);
		RepositoryEntry repositoryEntry = create(null, null, null, entryStatus, identity, List.of(), false, entryRoles, false).entry();
		
		List<RepositoryEntryInPreparation> entriesInPreparation = inPreparationQueries.searchRepositoryEntriesInPreparation(identity, participantsOnly);
		if (assertFound) {
			Assertions.assertThat(entriesInPreparation)
				.extracting(RepositoryEntryInPreparation::entry)
				.contains(repositoryEntry);
		} else {
			Assertions.assertThat(entriesInPreparation)
				.extracting(RepositoryEntryInPreparation::entry)
				.doesNotContain(repositoryEntry);
		}
	}
	
	@Test
	public void searchRepositoryEntriesInPreparation_reservation() {
		// No reservation
		searchEntryReservation(RepositoryEntryStatusEnum.published, false, false, false);
		
		// Reservation (participants only)
		searchEntryReservation(RepositoryEntryStatusEnum.preparation, true, false, true);
		searchEntryReservation(RepositoryEntryStatusEnum.review, true, false, true);
		searchEntryReservation(RepositoryEntryStatusEnum.coachpublished, true, false, true);
		searchEntryReservation(RepositoryEntryStatusEnum.published, true, false, false);
		searchEntryReservation(RepositoryEntryStatusEnum.closed, true, false, false);
		
		// Reservation (not participants only)
		searchEntryReservation(RepositoryEntryStatusEnum.preparation, true, true, true);
		searchEntryReservation(RepositoryEntryStatusEnum.review, true, true, true);
		searchEntryReservation(RepositoryEntryStatusEnum.coachpublished, true, true, true);
		searchEntryReservation(RepositoryEntryStatusEnum.published, true, true, false);
		searchEntryReservation(RepositoryEntryStatusEnum.closed, true, true, false);
	}
	
	private void searchEntryReservation(RepositoryEntryStatusEnum entryStatus, boolean reservation,
			boolean participantsOnly, boolean assertFound) {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(USER_PREFIX);
		RepositoryEntry repositoryEntry = create(null, null, null, entryStatus, identity, List.of(), false, List.of(), reservation).entry();
		
		List<RepositoryEntryInPreparation> entriesInPreparation = inPreparationQueries.searchRepositoryEntriesInPreparation(identity, participantsOnly);
		if (assertFound) {
			Assertions.assertThat(entriesInPreparation)
				.extracting(RepositoryEntryInPreparation::entry)
				.contains(repositoryEntry);
		} else {
			Assertions.assertThat(entriesInPreparation)
				.extracting(RepositoryEntryInPreparation::entry)
				.doesNotContain(repositoryEntry);
		}
	}
	
	@Test
	public void searchCurriculumElementsInPreparation() {
		Curriculum curriculum = curriculumService.createCurriculum(random(), random(), null, false, JunitTestHelper.getDefaultOrganisation());
		CurriculumElementType singleCourseType = curriculumService.createCurriculumElementType(random(), "Single Course", null, null);
		singleCourseType.setMaxRepositoryEntryRelations(1);
		singleCourseType.setSingleElement(true);
		singleCourseType = curriculumService.updateCurriculumElementType(singleCourseType);
		CurriculumElementType bundleType = curriculumService.createCurriculumElementType(random(), "Bundle", null, null);
		bundleType.setMaxRepositoryEntryRelations(3);
		bundleType.setSingleElement(true);
		bundleType = curriculumService.updateCurriculumElementType(bundleType);
		
		// No membership (and no reservation)
		searchElement(curriculum, singleCourseType, CurriculumElementStatus.preparation, RepositoryEntryStatusEnum.preparation, List.of(), false);
		searchElement(curriculum, bundleType, CurriculumElementStatus.preparation, RepositoryEntryStatusEnum.preparation, List.of(), false);
		
		// Participant (Single course, no content)
		searchElement(curriculum, singleCourseType, CurriculumElementStatus.preparation, null, CE_PARTICIPANT, true);
		searchElement(curriculum, singleCourseType, CurriculumElementStatus.provisional, null, CE_PARTICIPANT, true);
		searchElement(curriculum, singleCourseType, CurriculumElementStatus.confirmed, null, CE_PARTICIPANT, true);
		searchElement(curriculum, singleCourseType, CurriculumElementStatus.cancelled, null, CE_PARTICIPANT, false);
		searchElement(curriculum, singleCourseType, CurriculumElementStatus.finished, null, CE_PARTICIPANT, false);
		
		// Participant (Single course, content available)
		searchElement(curriculum, singleCourseType, CurriculumElementStatus.preparation, RepositoryEntryStatusEnum.preparation, CE_PARTICIPANT, true);
		searchElement(curriculum, singleCourseType, CurriculumElementStatus.preparation, RepositoryEntryStatusEnum.review, CE_PARTICIPANT, true);
		searchElement(curriculum, singleCourseType, CurriculumElementStatus.preparation, RepositoryEntryStatusEnum.coachpublished, CE_PARTICIPANT, true);
		searchElement(curriculum, singleCourseType, CurriculumElementStatus.preparation, RepositoryEntryStatusEnum.published, CE_PARTICIPANT, false);
		searchElement(curriculum, singleCourseType, CurriculumElementStatus.preparation, RepositoryEntryStatusEnum.closed, CE_PARTICIPANT, false);
		searchElement(curriculum, singleCourseType, CurriculumElementStatus.provisional, RepositoryEntryStatusEnum.preparation, CE_PARTICIPANT, true);
		searchElement(curriculum, singleCourseType, CurriculumElementStatus.provisional, RepositoryEntryStatusEnum.review, CE_PARTICIPANT, true);
		searchElement(curriculum, singleCourseType, CurriculumElementStatus.provisional, RepositoryEntryStatusEnum.coachpublished, CE_PARTICIPANT, true);
		searchElement(curriculum, singleCourseType, CurriculumElementStatus.provisional, RepositoryEntryStatusEnum.published, CE_PARTICIPANT, false);
		searchElement(curriculum, singleCourseType, CurriculumElementStatus.provisional, RepositoryEntryStatusEnum.closed, CE_PARTICIPANT, false);
		searchElement(curriculum, singleCourseType, CurriculumElementStatus.confirmed, RepositoryEntryStatusEnum.preparation, CE_PARTICIPANT, true);
		searchElement(curriculum, singleCourseType, CurriculumElementStatus.confirmed, RepositoryEntryStatusEnum.review, CE_PARTICIPANT, true);
		searchElement(curriculum, singleCourseType, CurriculumElementStatus.confirmed, RepositoryEntryStatusEnum.coachpublished, CE_PARTICIPANT, true);
		searchElement(curriculum, singleCourseType, CurriculumElementStatus.confirmed, RepositoryEntryStatusEnum.published, CE_PARTICIPANT, false);
		searchElement(curriculum, singleCourseType, CurriculumElementStatus.confirmed, RepositoryEntryStatusEnum.closed, CE_PARTICIPANT, false);
		searchElement(curriculum, singleCourseType, CurriculumElementStatus.cancelled, RepositoryEntryStatusEnum.preparation, CE_PARTICIPANT, true);
		searchElement(curriculum, singleCourseType, CurriculumElementStatus.cancelled, RepositoryEntryStatusEnum.review, CE_PARTICIPANT, true);
		searchElement(curriculum, singleCourseType, CurriculumElementStatus.cancelled, RepositoryEntryStatusEnum.coachpublished, CE_PARTICIPANT, true);
		searchElement(curriculum, singleCourseType, CurriculumElementStatus.cancelled, RepositoryEntryStatusEnum.published, CE_PARTICIPANT, false);
		searchElement(curriculum, singleCourseType, CurriculumElementStatus.cancelled, RepositoryEntryStatusEnum.closed, CE_PARTICIPANT, false);
		searchElement(curriculum, singleCourseType, CurriculumElementStatus.finished, RepositoryEntryStatusEnum.preparation, CE_PARTICIPANT, true);
		searchElement(curriculum, singleCourseType, CurriculumElementStatus.finished, RepositoryEntryStatusEnum.review, CE_PARTICIPANT, true);
		searchElement(curriculum, singleCourseType, CurriculumElementStatus.finished, RepositoryEntryStatusEnum.coachpublished, CE_PARTICIPANT, true);
		searchElement(curriculum, singleCourseType, CurriculumElementStatus.finished, RepositoryEntryStatusEnum.published, CE_PARTICIPANT, false);
		searchElement(curriculum, singleCourseType, CurriculumElementStatus.finished, RepositoryEntryStatusEnum.closed, CE_PARTICIPANT, false);
		
		// Participant (Bundle, no content)
		searchElement(curriculum, bundleType, CurriculumElementStatus.preparation, null, CE_PARTICIPANT, true);
		searchElement(curriculum, bundleType, CurriculumElementStatus.provisional, null, CE_PARTICIPANT, false);
		searchElement(curriculum, bundleType, CurriculumElementStatus.confirmed, null, CE_PARTICIPANT, false);
		searchElement(curriculum, bundleType, CurriculumElementStatus.cancelled, null, CE_PARTICIPANT, false);
		searchElement(curriculum, bundleType, CurriculumElementStatus.finished, null, CE_PARTICIPANT, false);
		
		// Participant (Bundle, content available)
		searchElement(curriculum, bundleType, CurriculumElementStatus.preparation, RepositoryEntryStatusEnum.preparation, CE_PARTICIPANT, true);
		searchElement(curriculum, bundleType, CurriculumElementStatus.preparation, RepositoryEntryStatusEnum.review, CE_PARTICIPANT, true);
		searchElement(curriculum, bundleType, CurriculumElementStatus.preparation, RepositoryEntryStatusEnum.coachpublished, CE_PARTICIPANT, true);
		searchElement(curriculum, bundleType, CurriculumElementStatus.preparation, RepositoryEntryStatusEnum.published, CE_PARTICIPANT, true);
		searchElement(curriculum, bundleType, CurriculumElementStatus.preparation, RepositoryEntryStatusEnum.closed, CE_PARTICIPANT, true);
		searchElement(curriculum, bundleType, CurriculumElementStatus.provisional, RepositoryEntryStatusEnum.preparation, CE_PARTICIPANT, false);
		searchElement(curriculum, bundleType, CurriculumElementStatus.provisional, RepositoryEntryStatusEnum.review, CE_PARTICIPANT, false);
		searchElement(curriculum, bundleType, CurriculumElementStatus.provisional, RepositoryEntryStatusEnum.coachpublished, CE_PARTICIPANT, false);
		searchElement(curriculum, bundleType, CurriculumElementStatus.provisional, RepositoryEntryStatusEnum.published, CE_PARTICIPANT, false);
		searchElement(curriculum, bundleType, CurriculumElementStatus.provisional, RepositoryEntryStatusEnum.closed, CE_PARTICIPANT, false);
		searchElement(curriculum, bundleType, CurriculumElementStatus.confirmed, RepositoryEntryStatusEnum.preparation, CE_PARTICIPANT, false);
		searchElement(curriculum, bundleType, CurriculumElementStatus.confirmed, RepositoryEntryStatusEnum.review, CE_PARTICIPANT, false);
		searchElement(curriculum, bundleType, CurriculumElementStatus.confirmed, RepositoryEntryStatusEnum.coachpublished, CE_PARTICIPANT, false);
		searchElement(curriculum, bundleType, CurriculumElementStatus.confirmed, RepositoryEntryStatusEnum.published, CE_PARTICIPANT, false);
		searchElement(curriculum, bundleType, CurriculumElementStatus.confirmed, RepositoryEntryStatusEnum.closed, CE_PARTICIPANT, false);
		searchElement(curriculum, bundleType, CurriculumElementStatus.cancelled, RepositoryEntryStatusEnum.preparation, CE_PARTICIPANT, false);
		searchElement(curriculum, bundleType, CurriculumElementStatus.cancelled, RepositoryEntryStatusEnum.review, CE_PARTICIPANT, false);
		searchElement(curriculum, bundleType, CurriculumElementStatus.cancelled, RepositoryEntryStatusEnum.coachpublished, CE_PARTICIPANT, false);
		searchElement(curriculum, bundleType, CurriculumElementStatus.cancelled, RepositoryEntryStatusEnum.published, CE_PARTICIPANT, false);
		searchElement(curriculum, bundleType, CurriculumElementStatus.cancelled, RepositoryEntryStatusEnum.closed, CE_PARTICIPANT, false);
		searchElement(curriculum, bundleType, CurriculumElementStatus.finished, RepositoryEntryStatusEnum.preparation, CE_PARTICIPANT, false);
		searchElement(curriculum, bundleType, CurriculumElementStatus.finished, RepositoryEntryStatusEnum.review, CE_PARTICIPANT, false);
		searchElement(curriculum, bundleType, CurriculumElementStatus.finished, RepositoryEntryStatusEnum.coachpublished, CE_PARTICIPANT, false);
		searchElement(curriculum, bundleType, CurriculumElementStatus.finished, RepositoryEntryStatusEnum.published, CE_PARTICIPANT, false);
		searchElement(curriculum, bundleType, CurriculumElementStatus.finished, RepositoryEntryStatusEnum.closed, CE_PARTICIPANT, false);
		
		// Coach / Owner elements are never found.
		searchElement(curriculum, bundleType, CurriculumElementStatus.preparation, RepositoryEntryStatusEnum.preparation, List.of(CurriculumRoles.coach), false);
		searchElement(curriculum, bundleType, CurriculumElementStatus.preparation, RepositoryEntryStatusEnum.preparation, List.of(CurriculumRoles.owner), false);
	}
	
	private void searchElement(Curriculum curriculum, CurriculumElementType elementType,
			CurriculumElementStatus elementStatus, RepositoryEntryStatusEnum entryStatus,
			Collection<CurriculumRoles> roles, boolean assertFound) {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(USER_PREFIX);
		CurriculumElement element = create(curriculum, elementType, elementStatus, entryStatus, identity, roles, false, List.of(), false).element();
		
		List<CurriculumElementInPreparation> elementsInPreparation = inPreparationQueries.searchCurriculumElementsInPreparation(identity);
		if (assertFound) {
			Assertions.assertThat(elementsInPreparation)
				.extracting(CurriculumElementInPreparation::element)
				.contains(element);
		} else {
			Assertions.assertThat(elementsInPreparation)
				.extracting(CurriculumElementInPreparation::element)
				.doesNotContain(element);
		}
	}
	
	@Test
	public void searchCurriculumElementsInPreparation_reservations() {
		Curriculum curriculum = curriculumService.createCurriculum(random(), random(), null, false, JunitTestHelper.getDefaultOrganisation());
		CurriculumElementType singleCourseType = curriculumService.createCurriculumElementType(random(), "Single Course", null, null);
		singleCourseType.setMaxRepositoryEntryRelations(1);
		singleCourseType.setSingleElement(true);
		singleCourseType = curriculumService.updateCurriculumElementType(singleCourseType);
		CurriculumElementType bundleType = curriculumService.createCurriculumElementType(random(), "Bundle", null, null);
		bundleType.setMaxRepositoryEntryRelations(3);
		bundleType.setSingleElement(true);
		bundleType = curriculumService.updateCurriculumElementType(bundleType);
		
		// Participant (Single course, no content)
		searchElementReservation(curriculum, singleCourseType, CurriculumElementStatus.preparation, null, true);
		searchElementReservation(curriculum, singleCourseType, CurriculumElementStatus.provisional, null, true);
		searchElementReservation(curriculum, singleCourseType, CurriculumElementStatus.confirmed, null, true);
		searchElementReservation(curriculum, singleCourseType, CurriculumElementStatus.cancelled, null, false);
		searchElementReservation(curriculum, singleCourseType, CurriculumElementStatus.finished, null, false);
		
		// Participant (Single course, content available)
		searchElementReservation(curriculum, singleCourseType, CurriculumElementStatus.preparation, RepositoryEntryStatusEnum.preparation, true);
		searchElementReservation(curriculum, singleCourseType, CurriculumElementStatus.preparation, RepositoryEntryStatusEnum.review, true);
		searchElementReservation(curriculum, singleCourseType, CurriculumElementStatus.preparation, RepositoryEntryStatusEnum.coachpublished, true);
		searchElementReservation(curriculum, singleCourseType, CurriculumElementStatus.preparation, RepositoryEntryStatusEnum.published, true);
		searchElementReservation(curriculum, singleCourseType, CurriculumElementStatus.preparation, RepositoryEntryStatusEnum.closed, true);
		searchElementReservation(curriculum, singleCourseType, CurriculumElementStatus.provisional, RepositoryEntryStatusEnum.preparation, true);
		searchElementReservation(curriculum, singleCourseType, CurriculumElementStatus.provisional, RepositoryEntryStatusEnum.review, true);
		searchElementReservation(curriculum, singleCourseType, CurriculumElementStatus.provisional, RepositoryEntryStatusEnum.coachpublished, true);
		searchElementReservation(curriculum, singleCourseType, CurriculumElementStatus.provisional, RepositoryEntryStatusEnum.published, true);
		searchElementReservation(curriculum, singleCourseType, CurriculumElementStatus.provisional, RepositoryEntryStatusEnum.closed, true);
		searchElementReservation(curriculum, singleCourseType, CurriculumElementStatus.confirmed, RepositoryEntryStatusEnum.preparation, true);
		searchElementReservation(curriculum, singleCourseType, CurriculumElementStatus.confirmed, RepositoryEntryStatusEnum.review, true);
		searchElementReservation(curriculum, singleCourseType, CurriculumElementStatus.confirmed, RepositoryEntryStatusEnum.coachpublished, true);
		searchElementReservation(curriculum, singleCourseType, CurriculumElementStatus.confirmed, RepositoryEntryStatusEnum.published, true);
		searchElementReservation(curriculum, singleCourseType, CurriculumElementStatus.confirmed, RepositoryEntryStatusEnum.closed, true);
		searchElementReservation(curriculum, singleCourseType, CurriculumElementStatus.cancelled, RepositoryEntryStatusEnum.preparation, true);
		searchElementReservation(curriculum, singleCourseType, CurriculumElementStatus.cancelled, RepositoryEntryStatusEnum.review, true);
		searchElementReservation(curriculum, singleCourseType, CurriculumElementStatus.cancelled, RepositoryEntryStatusEnum.coachpublished, true);
		searchElementReservation(curriculum, singleCourseType, CurriculumElementStatus.cancelled, RepositoryEntryStatusEnum.published, true);
		searchElementReservation(curriculum, singleCourseType, CurriculumElementStatus.cancelled, RepositoryEntryStatusEnum.closed, true);
		searchElementReservation(curriculum, singleCourseType, CurriculumElementStatus.finished, RepositoryEntryStatusEnum.preparation, true);
		searchElementReservation(curriculum, singleCourseType, CurriculumElementStatus.finished, RepositoryEntryStatusEnum.review, true);
		searchElementReservation(curriculum, singleCourseType, CurriculumElementStatus.finished, RepositoryEntryStatusEnum.coachpublished, true);
		searchElementReservation(curriculum, singleCourseType, CurriculumElementStatus.finished, RepositoryEntryStatusEnum.published, true);
		searchElementReservation(curriculum, singleCourseType, CurriculumElementStatus.finished, RepositoryEntryStatusEnum.closed, true);
		
		// Participant (Bundle, no content)
		searchElementReservation(curriculum, bundleType, CurriculumElementStatus.preparation, null, true);
		searchElementReservation(curriculum, bundleType, CurriculumElementStatus.provisional, null, false);
		searchElementReservation(curriculum, bundleType, CurriculumElementStatus.confirmed, null, false);
		searchElementReservation(curriculum, bundleType, CurriculumElementStatus.cancelled, null, false);
		searchElementReservation(curriculum, bundleType, CurriculumElementStatus.finished, null, false);
		
		// Participant (Bundle, content available)
		searchElementReservation(curriculum, bundleType, CurriculumElementStatus.preparation, RepositoryEntryStatusEnum.preparation, true);
		searchElementReservation(curriculum, bundleType, CurriculumElementStatus.preparation, RepositoryEntryStatusEnum.review, true);
		searchElementReservation(curriculum, bundleType, CurriculumElementStatus.preparation, RepositoryEntryStatusEnum.coachpublished, true);
		searchElementReservation(curriculum, bundleType, CurriculumElementStatus.preparation, RepositoryEntryStatusEnum.published, true);
		searchElementReservation(curriculum, bundleType, CurriculumElementStatus.preparation, RepositoryEntryStatusEnum.closed, true);
		searchElementReservation(curriculum, bundleType, CurriculumElementStatus.provisional, RepositoryEntryStatusEnum.preparation, false);
		searchElementReservation(curriculum, bundleType, CurriculumElementStatus.provisional, RepositoryEntryStatusEnum.review, false);
		searchElementReservation(curriculum, bundleType, CurriculumElementStatus.provisional, RepositoryEntryStatusEnum.coachpublished, false);
		searchElementReservation(curriculum, bundleType, CurriculumElementStatus.provisional, RepositoryEntryStatusEnum.published, false);
		searchElementReservation(curriculum, bundleType, CurriculumElementStatus.provisional, RepositoryEntryStatusEnum.closed, false);
		searchElementReservation(curriculum, bundleType, CurriculumElementStatus.confirmed, RepositoryEntryStatusEnum.preparation, false);
		searchElementReservation(curriculum, bundleType, CurriculumElementStatus.confirmed, RepositoryEntryStatusEnum.review, false);
		searchElementReservation(curriculum, bundleType, CurriculumElementStatus.confirmed, RepositoryEntryStatusEnum.coachpublished, false);
		searchElementReservation(curriculum, bundleType, CurriculumElementStatus.confirmed, RepositoryEntryStatusEnum.published, false);
		searchElementReservation(curriculum, bundleType, CurriculumElementStatus.confirmed, RepositoryEntryStatusEnum.closed, false);
		searchElementReservation(curriculum, bundleType, CurriculumElementStatus.cancelled, RepositoryEntryStatusEnum.preparation, false);
		searchElementReservation(curriculum, bundleType, CurriculumElementStatus.cancelled, RepositoryEntryStatusEnum.review, false);
		searchElementReservation(curriculum, bundleType, CurriculumElementStatus.cancelled, RepositoryEntryStatusEnum.coachpublished, false);
		searchElementReservation(curriculum, bundleType, CurriculumElementStatus.cancelled, RepositoryEntryStatusEnum.published, false);
		searchElementReservation(curriculum, bundleType, CurriculumElementStatus.cancelled, RepositoryEntryStatusEnum.closed, false);
		searchElementReservation(curriculum, bundleType, CurriculumElementStatus.finished, RepositoryEntryStatusEnum.preparation, false);
		searchElementReservation(curriculum, bundleType, CurriculumElementStatus.finished, RepositoryEntryStatusEnum.review, false);
		searchElementReservation(curriculum, bundleType, CurriculumElementStatus.finished, RepositoryEntryStatusEnum.coachpublished, false);
		searchElementReservation(curriculum, bundleType, CurriculumElementStatus.finished, RepositoryEntryStatusEnum.published, false);
		searchElementReservation(curriculum, bundleType, CurriculumElementStatus.finished, RepositoryEntryStatusEnum.closed, false);
		
		// No reservation for coaches / owners.
	}
	
	private void searchElementReservation(Curriculum curriculum, CurriculumElementType elementType,
			CurriculumElementStatus elementStatus, RepositoryEntryStatusEnum entryStatus,
			boolean assertFound) {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(USER_PREFIX);
		CurriculumElement element = create(curriculum, elementType, elementStatus, entryStatus, identity, CE_PARTICIPANT, true, List.of(), false).element();
		
		List<CurriculumElementInPreparation> elementsInPreparation = inPreparationQueries.searchCurriculumElementsInPreparation(identity);
		if (assertFound) {
			Assertions.assertThat(elementsInPreparation)
				.extracting(CurriculumElementInPreparation::element)
				.contains(element);
		} else {
			Assertions.assertThat(elementsInPreparation)
				.extracting(CurriculumElementInPreparation::element)
				.doesNotContain(element);
		}
	}
	
	@Test
	public void searchCurriculumElementsInPreparation_subElementMember() {
		Curriculum curriculum = curriculumService.createCurriculum(random(), random(), null, false, JunitTestHelper.getDefaultOrganisation());
		CurriculumElementType singleCourseType = curriculumService.createCurriculumElementType(random(), "Single Course", null, null);
		singleCourseType.setMaxRepositoryEntryRelations(1);
		singleCourseType.setSingleElement(true);
		singleCourseType = curriculumService.updateCurriculumElementType(singleCourseType);
		CurriculumElementType structureType = curriculumService.createCurriculumElementType(random(), "Structure", null, null);
		structureType.setMaxRepositoryEntryRelations(3);
		structureType.setSingleElement(false);
		structureType = curriculumService.updateCurriculumElementType(structureType);
		
		// Participant: Single course must never have sub elements
		searchElementSubMember(curriculum, singleCourseType, CurriculumElementStatus.preparation, structureType, CurriculumElementStatus.preparation, false);
		
		// Participant: Structure type
		searchElementSubMember(curriculum, structureType, CurriculumElementStatus.preparation, structureType, CurriculumElementStatus.preparation, true);
		searchElementSubMember(curriculum, structureType, CurriculumElementStatus.preparation, structureType, CurriculumElementStatus.provisional, true);
		searchElementSubMember(curriculum, structureType, CurriculumElementStatus.preparation, structureType, CurriculumElementStatus.confirmed, true);
		searchElementSubMember(curriculum, structureType, CurriculumElementStatus.preparation, structureType, CurriculumElementStatus.cancelled, true);
		searchElementSubMember(curriculum, structureType, CurriculumElementStatus.preparation, structureType, CurriculumElementStatus.finished, true);
		searchElementSubMember(curriculum, structureType, CurriculumElementStatus.provisional, structureType, CurriculumElementStatus.preparation, false);
		searchElementSubMember(curriculum, structureType, CurriculumElementStatus.provisional, structureType, CurriculumElementStatus.provisional, false);
		searchElementSubMember(curriculum, structureType, CurriculumElementStatus.provisional, structureType, CurriculumElementStatus.confirmed, false);
		searchElementSubMember(curriculum, structureType, CurriculumElementStatus.provisional, structureType, CurriculumElementStatus.cancelled, false);
		searchElementSubMember(curriculum, structureType, CurriculumElementStatus.provisional, structureType, CurriculumElementStatus.finished, false);
		searchElementSubMember(curriculum, structureType, CurriculumElementStatus.confirmed, structureType, CurriculumElementStatus.preparation, false);
		searchElementSubMember(curriculum, structureType, CurriculumElementStatus.confirmed, structureType, CurriculumElementStatus.provisional, false);
		searchElementSubMember(curriculum, structureType, CurriculumElementStatus.confirmed, structureType, CurriculumElementStatus.confirmed, false);
		searchElementSubMember(curriculum, structureType, CurriculumElementStatus.confirmed, structureType, CurriculumElementStatus.cancelled, false);
		searchElementSubMember(curriculum, structureType, CurriculumElementStatus.confirmed, structureType, CurriculumElementStatus.finished, false);
		searchElementSubMember(curriculum, structureType, CurriculumElementStatus.cancelled, structureType, CurriculumElementStatus.preparation, false);
		searchElementSubMember(curriculum, structureType, CurriculumElementStatus.cancelled, structureType, CurriculumElementStatus.provisional, false);
		searchElementSubMember(curriculum, structureType, CurriculumElementStatus.cancelled, structureType, CurriculumElementStatus.confirmed, false);
		searchElementSubMember(curriculum, structureType, CurriculumElementStatus.cancelled, structureType, CurriculumElementStatus.cancelled, false);
		searchElementSubMember(curriculum, structureType, CurriculumElementStatus.cancelled, structureType, CurriculumElementStatus.finished, false);
		searchElementSubMember(curriculum, structureType, CurriculumElementStatus.finished, structureType, CurriculumElementStatus.preparation, false);
		searchElementSubMember(curriculum, structureType, CurriculumElementStatus.finished, structureType, CurriculumElementStatus.provisional, false);
		searchElementSubMember(curriculum, structureType, CurriculumElementStatus.finished, structureType, CurriculumElementStatus.confirmed, false);
		searchElementSubMember(curriculum, structureType, CurriculumElementStatus.finished, structureType, CurriculumElementStatus.cancelled, false);
		searchElementSubMember(curriculum, structureType, CurriculumElementStatus.finished, structureType, CurriculumElementStatus.finished, false);
	}
	
	private void searchElementSubMember(Curriculum curriculum, CurriculumElementType implementationType,
			CurriculumElementStatus implementationStatus, CurriculumElementType elementType,
			CurriculumElementStatus elementStatus, boolean assertFound) {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(USER_PREFIX);
		CurriculumElement implementation = create(curriculum, implementationType, implementationStatus, elementType, elementStatus, identity, false);
		
		List<CurriculumElementInPreparation> elementsInPreparation = inPreparationQueries.searchCurriculumElementsInPreparation(identity);
		if (assertFound) {
			Assertions.assertThat(elementsInPreparation)
				.extracting(CurriculumElementInPreparation::element)
				.contains(implementation);
		} else {
			Assertions.assertThat(elementsInPreparation)
				.extracting(CurriculumElementInPreparation::element)
				.doesNotContain(implementation);
		}
	}
	
	@Test
	public void searchCurriculumElementsInPreparation_subElementMember_reservation() {
		Curriculum curriculum = curriculumService.createCurriculum(random(), random(), null, false, JunitTestHelper.getDefaultOrganisation());
		CurriculumElementType singleCourseType = curriculumService.createCurriculumElementType(random(), "Single Course", null, null);
		singleCourseType.setMaxRepositoryEntryRelations(1);
		singleCourseType.setSingleElement(true);
		singleCourseType = curriculumService.updateCurriculumElementType(singleCourseType);
		CurriculumElementType structureType = curriculumService.createCurriculumElementType(random(), "Structure", null, null);
		structureType.setMaxRepositoryEntryRelations(3);
		structureType.setSingleElement(false);
		structureType = curriculumService.updateCurriculumElementType(structureType);
		
		// Participant: Single course must never have sub elements
		searchElementSubMemberReservation(curriculum, singleCourseType, CurriculumElementStatus.preparation, structureType, CurriculumElementStatus.preparation, false);
		
		// Participant: Structure type
		searchElementSubMemberReservation(curriculum, structureType, CurriculumElementStatus.preparation, structureType, CurriculumElementStatus.preparation, true);
		searchElementSubMemberReservation(curriculum, structureType, CurriculumElementStatus.preparation, structureType, CurriculumElementStatus.provisional, true);
		searchElementSubMemberReservation(curriculum, structureType, CurriculumElementStatus.preparation, structureType, CurriculumElementStatus.confirmed, true);
		searchElementSubMemberReservation(curriculum, structureType, CurriculumElementStatus.preparation, structureType, CurriculumElementStatus.cancelled, true);
		searchElementSubMemberReservation(curriculum, structureType, CurriculumElementStatus.preparation, structureType, CurriculumElementStatus.finished, true);
		searchElementSubMemberReservation(curriculum, structureType, CurriculumElementStatus.provisional, structureType, CurriculumElementStatus.preparation, true);
		searchElementSubMemberReservation(curriculum, structureType, CurriculumElementStatus.provisional, structureType, CurriculumElementStatus.provisional, true);
		searchElementSubMemberReservation(curriculum, structureType, CurriculumElementStatus.provisional, structureType, CurriculumElementStatus.confirmed, true);
		searchElementSubMemberReservation(curriculum, structureType, CurriculumElementStatus.provisional, structureType, CurriculumElementStatus.cancelled, true);
		searchElementSubMemberReservation(curriculum, structureType, CurriculumElementStatus.provisional, structureType, CurriculumElementStatus.finished, true);
		searchElementSubMemberReservation(curriculum, structureType, CurriculumElementStatus.confirmed, structureType, CurriculumElementStatus.preparation, true);
		searchElementSubMemberReservation(curriculum, structureType, CurriculumElementStatus.confirmed, structureType, CurriculumElementStatus.provisional, true);
		searchElementSubMemberReservation(curriculum, structureType, CurriculumElementStatus.confirmed, structureType, CurriculumElementStatus.confirmed, true);
		searchElementSubMemberReservation(curriculum, structureType, CurriculumElementStatus.confirmed, structureType, CurriculumElementStatus.cancelled, true);
		searchElementSubMemberReservation(curriculum, structureType, CurriculumElementStatus.confirmed, structureType, CurriculumElementStatus.finished, true);
		searchElementSubMemberReservation(curriculum, structureType, CurriculumElementStatus.cancelled, structureType, CurriculumElementStatus.preparation, true);
		searchElementSubMemberReservation(curriculum, structureType, CurriculumElementStatus.cancelled, structureType, CurriculumElementStatus.provisional, true);
		searchElementSubMemberReservation(curriculum, structureType, CurriculumElementStatus.cancelled, structureType, CurriculumElementStatus.confirmed, true);
		searchElementSubMemberReservation(curriculum, structureType, CurriculumElementStatus.cancelled, structureType, CurriculumElementStatus.cancelled, true);
		searchElementSubMemberReservation(curriculum, structureType, CurriculumElementStatus.cancelled, structureType, CurriculumElementStatus.finished, true);
		searchElementSubMemberReservation(curriculum, structureType, CurriculumElementStatus.finished, structureType, CurriculumElementStatus.preparation, true);
		searchElementSubMemberReservation(curriculum, structureType, CurriculumElementStatus.finished, structureType, CurriculumElementStatus.provisional, true);
		searchElementSubMemberReservation(curriculum, structureType, CurriculumElementStatus.finished, structureType, CurriculumElementStatus.confirmed, true);
		searchElementSubMemberReservation(curriculum, structureType, CurriculumElementStatus.finished, structureType, CurriculumElementStatus.cancelled, true);
		searchElementSubMemberReservation(curriculum, structureType, CurriculumElementStatus.finished, structureType, CurriculumElementStatus.finished, true);
	}
	
	private void searchElementSubMemberReservation(Curriculum curriculum, CurriculumElementType implementationType,
			CurriculumElementStatus implementationStatus, CurriculumElementType elementType,
			CurriculumElementStatus elementStatus, boolean assertFound) {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(USER_PREFIX);
		CurriculumElement implementation = create(curriculum, implementationType, implementationStatus, elementType, elementStatus, identity, true);
		
		List<CurriculumElementInPreparation> elementsInPreparation = inPreparationQueries.searchCurriculumElementsInPreparation(identity);
		if (assertFound) {
			Assertions.assertThat(elementsInPreparation)
				.extracting(CurriculumElementInPreparation::element)
				.contains(implementation);
		} else {
			Assertions.assertThat(elementsInPreparation)
				.extracting(CurriculumElementInPreparation::element)
				.doesNotContain(implementation);
		}
	}
	
	@Test
	public void searchCurriculumElementsInPreparation_subElementMember_multi() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(USER_PREFIX);
		Curriculum curriculum = curriculumService.createCurriculum(random(), random(), null, false, JunitTestHelper.getDefaultOrganisation());
		CurriculumElementType structureType = curriculumService.createCurriculumElementType(random(), "Structure", null, null);
		structureType.setMaxRepositoryEntryRelations(3);
		structureType.setSingleElement(false);
		structureType = curriculumService.updateCurriculumElementType(structureType);
		
		// Implementation in preparation
		CurriculumElement implementation = create(curriculum, structureType, CurriculumElementStatus.preparation, null, identity, List.of(), false, null, false).element();
		// Sub element membership
		CurriculumElement subMember = create(curriculum, structureType, CurriculumElementStatus.preparation, null, identity, CE_PARTICIPANT, false, null, false).element();
		curriculumService.moveCurriculumElement(subMember, implementation, null, curriculum);
		// Sub element reservation
		CurriculumElement subReservation = create(curriculum, structureType, CurriculumElementStatus.preparation, null, identity, List.of(), true, null, false).element();
		curriculumService.moveCurriculumElement(subReservation, implementation, null, curriculum);
		dbInstance.commitAndCloseSession();
		
		List<CurriculumElementInPreparation> elementsInPreparation = inPreparationQueries.searchCurriculumElementsInPreparation(identity);
		Assertions.assertThat(elementsInPreparation)
				.extracting(CurriculumElementInPreparation::element)
				.contains(implementation);
		
		// Implementation confirmed
		curriculumService.updateCurriculumElementStatus(identity, implementation, CurriculumElementStatus.confirmed, false, null);
		dbInstance.commitAndCloseSession();
		
		elementsInPreparation = inPreparationQueries.searchCurriculumElementsInPreparation(identity);
		Assertions.assertThat(elementsInPreparation)
				.extracting(CurriculumElementInPreparation::element)
				.doesNotContain(implementation);
	}
	
	private ElementEntry create(Curriculum curriculum, CurriculumElementType elementType,
			CurriculumElementStatus elementStatus, RepositoryEntryStatusEnum entryStatus, Identity identity,
			Collection<CurriculumRoles> elementRoles, boolean elementReservation, Collection<GroupRoles> entryRoles,
			boolean entryReservation) {
		CurriculumElement curriculumElement = null;
		if (elementStatus != null) {
			curriculumElement = curriculumService.createCurriculumElement(random(), random(), elementStatus,
					null, null, null, elementType, null, null, null, curriculum);
			
			for (CurriculumRoles role : elementRoles) {
				curriculumService.addMember(curriculumElement, identity, role, identity);
			}
			
			if (elementReservation) {
				reservationDao.createReservation(identity, null, DateUtils.addDays(new Date(), 3),
						ConfirmationByEnum.ADMINISTRATIVE_ROLE, curriculumElement.getResource());
			}
		}
		
		RepositoryEntry repositoryEntry = null;
		if (entryStatus != null) {
			repositoryEntry = JunitTestHelper.createAndPersistRepositoryEntry();
			repositoryEntry.setEntryStatus(entryStatus);
			repositoryEntry = repositoryService.update(repositoryEntry);
			
			for (GroupRoles role : entryRoles) {
				repositoryService.addRole(identity, repositoryEntry, role.name());
			}
			
			if (curriculumElement != null) {
				curriculumService.addRepositoryEntry(curriculumElement, repositoryEntry, false);
			}
			
			if (entryReservation) {
				reservationDao.createReservation(identity, null, DateUtils.addDays(new Date(), 3),
						ConfirmationByEnum.PARTICIPANT, repositoryEntry.getOlatResource());
			}
		}
		
		dbInstance.commitAndCloseSession();
		
		return new ElementEntry(curriculumElement, repositoryEntry);
	}
	
	private CurriculumElement create(Curriculum curriculum, CurriculumElementType implementationType,
			CurriculumElementStatus implementationStatus, CurriculumElementType elementType,
			CurriculumElementStatus elementStatus, Identity identity,
			boolean reservationAvailable) {
		
		CurriculumElement implementationElement = create(curriculum, implementationType, implementationStatus, null, identity, List.of(), false, null, false).element();
		CurriculumElement subElement = create(curriculum, elementType, elementStatus, null, identity, !reservationAvailable? CE_PARTICIPANT: List.of(), reservationAvailable, null, false).element();
		curriculumService.moveCurriculumElement(subElement, implementationElement, null, curriculum);
		dbInstance.commitAndCloseSession();
		
		return implementationElement;
	}
	
	private final static record ElementEntry(CurriculumElement element, RepositoryEntry entry) {}
	
}

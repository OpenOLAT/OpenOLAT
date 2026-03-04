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

import static org.olat.repository.manager.RepositoryEntryMyImplementationsQueries.STATUS_WITHOUT_PREPARATION;
import static org.olat.test.JunitTestHelper.random;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.manager.CurriculumDAO;
import org.olat.modules.curriculum.manager.CurriculumElementDAO;
import org.olat.modules.curriculum.manager.CurriculumElementTypeDAO;
import org.olat.resource.accesscontrol.ConfirmationByEnum;
import org.olat.resource.accesscontrol.manager.ACReservationDAO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 1 avr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class RepositoryEntryMyImplementationsQueriesTest extends OlatTestCase {
	
	private static final String USER_PREFIX = random();
	private static final List<CurriculumRoles> CE_PARTICIPANT = List.of(CurriculumRoles.participant);
	private static final List<GroupRoles> PARTICIPANTS_ONLY = List.of(GroupRoles.participant);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CurriculumDAO curriculumDao;
	@Autowired
	private ACReservationDAO reservationDao;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private CurriculumElementDAO curriculumElementDao;
	@Autowired
	private CurriculumElementTypeDAO curriculumElementTypeDao;
	@Autowired
	private RepositoryEntryMyImplementationsQueries myImplementationsQueries;
	
	@Test
	public void searchImplementationsBookmarks() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("my-implementations-view-2");
		dbInstance.commit();
		
		List<CurriculumElement> list = myImplementationsQueries.searchImplementations(id, true, PARTICIPANTS_ONLY, STATUS_WITHOUT_PREPARATION);
		Assert.assertNotNull(list);
	}
	
	@Test
	public void searchMultipleCoursesImplementations() {
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("my-implementations-view-2");
		
		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-impl-1", "Curriculum for implementation", "Curriculum", false,
				JunitTestHelper.getDefaultOrganisation());
		CurriculumElementType multipleCoursesType = curriculumElementTypeDao.createCurriculumElementType("typ-multiple-cur-el-1", "Type for multiple courses", "", "");
		multipleCoursesType.setAllowedAsRootElement(true);
		multipleCoursesType.setMaxRepositoryEntryRelations(-1);
		multipleCoursesType.setSingleElement(true);
		multipleCoursesType = curriculumElementTypeDao.update(multipleCoursesType);
			
		CurriculumElement element = curriculumElementDao.createCurriculumElement("Element-1", "1. Element",
				CurriculumElementStatus.active, new Date(), new Date(), null, multipleCoursesType, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		curriculumService.addMember(element, participant, CurriculumRoles.participant, JunitTestHelper.getDefaultActor());
		dbInstance.commitAndCloseSession();

		List<CurriculumElement> list = myImplementationsQueries.searchImplementations(participant, false, PARTICIPANTS_ONLY, STATUS_WITHOUT_PREPARATION);
		Assertions.assertThat(list)
			.containsExactly(element);
	}
	
	@Test
	public void searchStructuredImplementations() {
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("my-implementations-view-2");
		
		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-impl-1", "Curriculum for implementation", "Curriculum", false,
				JunitTestHelper.getDefaultOrganisation());
		CurriculumElementType structuredType = curriculumElementTypeDao.createCurriculumElementType("typ-structred-cur-el-1", "Structured type", "", "");
		structuredType.setAllowedAsRootElement(true);
		structuredType.setMaxRepositoryEntryRelations(0);
		structuredType.setSingleElement(false);
		structuredType = curriculumElementTypeDao.update(structuredType);
			
		CurriculumElement element = curriculumElementDao.createCurriculumElement("Element-1", "1. Element",
				CurriculumElementStatus.active, new Date(), new Date(), null, structuredType, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		curriculumService.addMember(element, participant, CurriculumRoles.participant, JunitTestHelper.getDefaultActor());
		dbInstance.commitAndCloseSession();

		List<CurriculumElement> list = myImplementationsQueries.searchImplementations(participant, false, PARTICIPANTS_ONLY, STATUS_WITHOUT_PREPARATION);
		Assertions.assertThat(list)
			.containsExactly(element);
	}
	
	@Test
	public void searchSingleCourseImplementations() {
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("my-implementations-view-2");
		
		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-impl-3", "Curriculum for implementation", "Curriculum", false,
				JunitTestHelper.getDefaultOrganisation());
		CurriculumElementType structuredType = curriculumElementTypeDao.createCurriculumElementType("typ-single-cur-el-3", "Single course implementation", "", "");
		structuredType.setAllowedAsRootElement(true);
		structuredType.setMaxRepositoryEntryRelations(1);
		structuredType.setSingleElement(true);
		structuredType = curriculumElementTypeDao.update(structuredType);
			
		CurriculumElement element = curriculumElementDao.createCurriculumElement("Element-3", "3. Element",
				CurriculumElementStatus.active, new Date(), new Date(), null, structuredType, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		curriculumService.addMember(element, participant, CurriculumRoles.participant, JunitTestHelper.getDefaultActor());
		dbInstance.commitAndCloseSession();

		List<CurriculumElement> list = myImplementationsQueries.searchImplementations(participant, false, PARTICIPANTS_ONLY, STATUS_WITHOUT_PREPARATION);
		Assertions.assertThat(list)
			.isEmpty();
	}
	
	@Test
	public void hasImplementations() {
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("my-implementations-view-4");
		
		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-impl-4", "Curriculum for implementation", "Curriculum", false,
				JunitTestHelper.getDefaultOrganisation());
		CurriculumElementType multipleCoursesType = curriculumElementTypeDao.createCurriculumElementType("typ-multiple-cur-el-4", "Type for multiple courses", "", "");
		multipleCoursesType.setAllowedAsRootElement(true);
		multipleCoursesType.setMaxRepositoryEntryRelations(-1);
		multipleCoursesType.setSingleElement(true);
		multipleCoursesType = curriculumElementTypeDao.update(multipleCoursesType);
			
		CurriculumElement element = curriculumElementDao.createCurriculumElement("Element-4", "4. Element",
				CurriculumElementStatus.active, new Date(), new Date(), null, multipleCoursesType, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		curriculumService.addMember(element, participant, CurriculumRoles.participant, JunitTestHelper.getDefaultActor());
		dbInstance.commitAndCloseSession();

		boolean hasOneImplementation = myImplementationsQueries.hasImplementations(participant, PARTICIPANTS_ONLY, STATUS_WITHOUT_PREPARATION);
		Assert.assertTrue(hasOneImplementation);
	}
	
	@Test
	public void getCurriculums() {
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("my-implementations-view-5");
		
		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-impl-5", "Curriculum for implementation", "Curriculum", false,
				JunitTestHelper.getDefaultOrganisation());
		CurriculumElementType multipleCoursesType = curriculumElementTypeDao.createCurriculumElementType("typ-multiple-cur-el-5", "Type for multiple courses", "", "");
		multipleCoursesType.setAllowedAsRootElement(true);
		multipleCoursesType.setMaxRepositoryEntryRelations(-1);
		multipleCoursesType.setSingleElement(true);
		multipleCoursesType = curriculumElementTypeDao.update(multipleCoursesType);
			
		CurriculumElement element = curriculumElementDao.createCurriculumElement("Element-5", "5. Element",
				CurriculumElementStatus.active, new Date(), new Date(), null, multipleCoursesType, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		curriculumService.addMember(element, participant, CurriculumRoles.participant, JunitTestHelper.getDefaultActor());
		dbInstance.commitAndCloseSession();

		List<Curriculum> list = myImplementationsQueries.getCurriculums(participant, PARTICIPANTS_ONLY, STATUS_WITHOUT_PREPARATION);
		Assertions.assertThat(list)
			.containsExactly(curriculum);
	}
	
	@Test
	public void searchImplementations() {
		Curriculum curriculum = curriculumService.createCurriculum(random(), random(), null, false, JunitTestHelper.getDefaultOrganisation());
		CurriculumElementType singleCourseType = curriculumService.createCurriculumElementType(random(), "Single Course", null, null);
		singleCourseType.setMaxRepositoryEntryRelations(1);
		singleCourseType.setSingleElement(true);
		singleCourseType = curriculumService.updateCurriculumElementType(singleCourseType);
		CurriculumElementType structureType = curriculumService.createCurriculumElementType(random(), "Structure", null, null);
		structureType.setMaxRepositoryEntryRelations(3);
		structureType.setSingleElement(false);
		structureType = curriculumService.updateCurriculumElementType(structureType);
		
		// No membership (and no reservation)
		searchElement(curriculum, singleCourseType, CurriculumElementStatus.preparation, List.of(), false);
		searchElement(curriculum, structureType, CurriculumElementStatus.preparation, List.of(), false);
		
		// Participant (Single course, no content)
		searchElement(curriculum, singleCourseType, CurriculumElementStatus.preparation, CE_PARTICIPANT, false);
		searchElement(curriculum, singleCourseType, CurriculumElementStatus.provisional, CE_PARTICIPANT, false);
		searchElement(curriculum, singleCourseType, CurriculumElementStatus.confirmed, CE_PARTICIPANT, false);
		searchElement(curriculum, singleCourseType, CurriculumElementStatus.cancelled, CE_PARTICIPANT, false);
		searchElement(curriculum, singleCourseType, CurriculumElementStatus.finished, CE_PARTICIPANT, false);
		
		// Participant (Structure)
		searchElement(curriculum, structureType, CurriculumElementStatus.preparation, CE_PARTICIPANT, false);
		searchElement(curriculum, structureType, CurriculumElementStatus.provisional, CE_PARTICIPANT, true);
		searchElement(curriculum, structureType, CurriculumElementStatus.confirmed, CE_PARTICIPANT, true);
		searchElement(curriculum, structureType, CurriculumElementStatus.cancelled, CE_PARTICIPANT, true);
		searchElement(curriculum, structureType, CurriculumElementStatus.finished, CE_PARTICIPANT, true);
	}
	
	private void searchElement(Curriculum curriculum, CurriculumElementType elementType,
			CurriculumElementStatus elementStatus, Collection<CurriculumRoles> roles,
			boolean assertFound) {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(USER_PREFIX);
		CurriculumElement element = create(curriculum, elementType, elementStatus,identity, roles, false);
		
		List<GroupRoles> groupRoles = roles.stream().map(CurriculumRoles::name).map(GroupRoles::valueOf).toList();
		List<CurriculumElement> implementations = myImplementationsQueries.searchImplementations(identity, false, groupRoles, STATUS_WITHOUT_PREPARATION);
		if (assertFound) {
			Assertions.assertThat(implementations)
				.contains(element);
		} else {
			Assertions.assertThat(implementations)
				.doesNotContain(element);
		}
	}
	
	@Test
	public void searchImplementations_reservations() {
		Curriculum curriculum = curriculumService.createCurriculum(random(), random(), null, false, JunitTestHelper.getDefaultOrganisation());
		CurriculumElementType structureType = curriculumService.createCurriculumElementType(random(), "Structure", null, null);
		structureType.setMaxRepositoryEntryRelations(3);
		structureType.setSingleElement(false);
		structureType = curriculumService.updateCurriculumElementType(structureType);
	
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(USER_PREFIX);
		CurriculumElement element = create(curriculum, structureType, CurriculumElementStatus.confirmed, identity, List.of(), true);
		
		// Elements with reservation are display "in preparation"
		List<CurriculumElement> implementations = myImplementationsQueries.searchImplementations(identity, false, PARTICIPANTS_ONLY, STATUS_WITHOUT_PREPARATION);
		Assertions.assertThat(implementations).doesNotContain(element);
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
		searchElementSubMember(curriculum, singleCourseType, CurriculumElementStatus.confirmed, structureType, CurriculumElementStatus.active, false);
		
		// Participant: Structure type
		searchElementSubMember(curriculum, structureType, CurriculumElementStatus.preparation, structureType, CurriculumElementStatus.preparation, false);
		searchElementSubMember(curriculum, structureType, CurriculumElementStatus.preparation, structureType, CurriculumElementStatus.provisional, false);
		searchElementSubMember(curriculum, structureType, CurriculumElementStatus.preparation, structureType, CurriculumElementStatus.active, false);
		searchElementSubMember(curriculum, structureType, CurriculumElementStatus.preparation, structureType, CurriculumElementStatus.cancelled, false);
		searchElementSubMember(curriculum, structureType, CurriculumElementStatus.preparation, structureType, CurriculumElementStatus.finished, false);
		searchElementSubMember(curriculum, structureType, CurriculumElementStatus.provisional, structureType, CurriculumElementStatus.preparation, true);
		searchElementSubMember(curriculum, structureType, CurriculumElementStatus.provisional, structureType, CurriculumElementStatus.provisional, true);
		searchElementSubMember(curriculum, structureType, CurriculumElementStatus.provisional, structureType, CurriculumElementStatus.active, true);
		searchElementSubMember(curriculum, structureType, CurriculumElementStatus.provisional, structureType, CurriculumElementStatus.cancelled, true);
		searchElementSubMember(curriculum, structureType, CurriculumElementStatus.provisional, structureType, CurriculumElementStatus.finished, true);
		searchElementSubMember(curriculum, structureType, CurriculumElementStatus.confirmed, structureType, CurriculumElementStatus.preparation, true);
		searchElementSubMember(curriculum, structureType, CurriculumElementStatus.confirmed, structureType, CurriculumElementStatus.provisional, true);
		searchElementSubMember(curriculum, structureType, CurriculumElementStatus.confirmed, structureType, CurriculumElementStatus.active, true);
		searchElementSubMember(curriculum, structureType, CurriculumElementStatus.confirmed, structureType, CurriculumElementStatus.cancelled, true);
		searchElementSubMember(curriculum, structureType, CurriculumElementStatus.confirmed, structureType, CurriculumElementStatus.finished, true);
		searchElementSubMember(curriculum, structureType, CurriculumElementStatus.cancelled, structureType, CurriculumElementStatus.preparation, true);
		searchElementSubMember(curriculum, structureType, CurriculumElementStatus.cancelled, structureType, CurriculumElementStatus.provisional, true);
		searchElementSubMember(curriculum, structureType, CurriculumElementStatus.cancelled, structureType, CurriculumElementStatus.active, true);
		searchElementSubMember(curriculum, structureType, CurriculumElementStatus.cancelled, structureType, CurriculumElementStatus.cancelled, true);
		searchElementSubMember(curriculum, structureType, CurriculumElementStatus.cancelled, structureType, CurriculumElementStatus.finished, true);
		searchElementSubMember(curriculum, structureType, CurriculumElementStatus.finished, structureType, CurriculumElementStatus.preparation, true);
		searchElementSubMember(curriculum, structureType, CurriculumElementStatus.finished, structureType, CurriculumElementStatus.provisional, true);
		searchElementSubMember(curriculum, structureType, CurriculumElementStatus.finished, structureType, CurriculumElementStatus.active, true);
		searchElementSubMember(curriculum, structureType, CurriculumElementStatus.finished, structureType, CurriculumElementStatus.cancelled, true);
		searchElementSubMember(curriculum, structureType, CurriculumElementStatus.finished, structureType, CurriculumElementStatus.finished, true);
	}
	
	private void searchElementSubMember(Curriculum curriculum, CurriculumElementType implementationType,
			CurriculumElementStatus implementationStatus, CurriculumElementType elementType,
			CurriculumElementStatus elementStatus, boolean assertFound) {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(USER_PREFIX);
		CurriculumElements elements = create(curriculum, implementationType, implementationStatus, elementType, elementStatus, identity, false);
		
		List<GroupRoles> groupRoles = List.of(GroupRoles.participant);
		List<CurriculumElement> implementations = myImplementationsQueries.searchImplementations(identity, false, groupRoles, STATUS_WITHOUT_PREPARATION);
		if (assertFound) {
			Assertions.assertThat(implementations)
				.contains(elements.implementation())
				.doesNotContain(elements.subElement());
		} else {
			Assertions.assertThat(implementations)
				.doesNotContain(elements.implementation(), elements.subElement());
		}
	}
	
	private CurriculumElement create(Curriculum curriculum, CurriculumElementType elementType,
			CurriculumElementStatus elementStatus, Identity identity, Collection<CurriculumRoles> elementRoles,
			boolean elementReservation) {
		CurriculumElement curriculumElement = curriculumService.createCurriculumElement(random(), random(), elementStatus,
				null, null, null, elementType, null, null, null, curriculum);
		
		for (CurriculumRoles role : elementRoles) {
			curriculumService.addMember(curriculumElement, identity, role, identity);
		}
		
		if (elementReservation) {
			reservationDao.createReservation(identity, null, DateUtils.addDays(new Date(), 3),
					ConfirmationByEnum.ADMINISTRATIVE_ROLE, curriculumElement.getResource());
		}
		
		dbInstance.commitAndCloseSession();
		
		return curriculumElement;
	}
	
	private CurriculumElements create(Curriculum curriculum, CurriculumElementType implementationType,
			CurriculumElementStatus implementationStatus, CurriculumElementType elementType,
			CurriculumElementStatus elementStatus, Identity identity,
			boolean reservationAvailable) {
		
		CurriculumElement implementationElement = create(curriculum, implementationType, implementationStatus, identity, List.of(), false);
		CurriculumElement subElement = create(curriculum, elementType, elementStatus,  identity, !reservationAvailable? CE_PARTICIPANT: List.of(), false);
		curriculumService.moveCurriculumElement(subElement, implementationElement, null, curriculum);
		dbInstance.commitAndCloseSession();
		
		return new CurriculumElements(implementationElement, subElement);
	}
	
	private record CurriculumElements(CurriculumElement implementation, CurriculumElement subElement) {}
	
}
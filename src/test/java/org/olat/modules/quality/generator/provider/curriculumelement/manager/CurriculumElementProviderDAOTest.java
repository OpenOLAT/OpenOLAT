/**
 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.quality.generator.provider.curriculumelement.manager;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Organisation;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.quality.QualityService;
import org.olat.modules.quality.generator.QualityGenerator;
import org.olat.modules.quality.generator.QualityGeneratorService;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17.08.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementProviderDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private QualityGeneratorService generatorService;
	@Autowired
	private QualityService qualityService;

	@Autowired
	private CurriculumElementProviderDAO sut;
	
	@Test
	public void shouldLoadCurriculumElements() {
		Organisation organisation = organisationService.createOrganisation(random(), random(), null, null, null);
		CurriculumElementType ceType = curriculumService.createCurriculumElementType(random(), random(), null, null);
		Curriculum curriculum = curriculumService.createCurriculum(random(), random(), null, false, organisation);
		CurriculumElement curriculumElement = curriculumService.createCurriculumElement(random(), random(),
				CurriculumElementStatus.active, oneDayAgo(), inOneDay(), null, ceType, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commitAndCloseSession();

		SearchParameters searchParams = new SearchParameters();
		List<CurriculumElement> pending = sut.loadPending(searchParams);

		assertThat(pending).contains(curriculumElement);
	}
	
	@Test
	public void shouldFilterByAlreadyCreated() {
		Organisation organisation = organisationService.createOrganisation(random(), random(), null, null, null);
		List<Organisation> organisations = Collections.singletonList(organisation);
		Curriculum curriculum = curriculumService.createCurriculum(random(), random(), null, false, organisation);
		CurriculumElement curriculumElement = curriculumService.createCurriculumElement(random(), random(),
				CurriculumElementStatus.active, oneDayAgo(), inOneDay(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		QualityGenerator generator = generatorService.createGenerator(random(), organisations);
		dbInstance.commitAndCloseSession();

		CurriculumElement curriculumElementCreated = curriculumService.createCurriculumElement(random(), random(),
				CurriculumElementStatus.active, oneDayAgo(), inOneDay(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		qualityService.createDataCollection(organisations, formEntry, generator, curriculumElementCreated.getKey());
		dbInstance.commitAndCloseSession();

		SearchParameters searchParams = new SearchParameters();
		searchParams.setGeneratorRef(generator);
		List<CurriculumElement> pending = sut.loadPending(searchParams);

		assertThat(pending).contains(curriculumElement).doesNotContain(curriculumElementCreated);
	}
	
	@Test
	public void shouldFilterByOrganisation() {
		Organisation organisationSuper = organisationService.createOrganisation(random(), random(), null, null, null);
		Curriculum curriculumSuper = curriculumService.createCurriculum(random(), random(), null, false, organisationSuper);
		CurriculumElement curriculumElementSuperOrg = curriculumService.createCurriculumElement(random(), random(),
				CurriculumElementStatus.active, oneDayAgo(), inOneDay(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculumSuper);

		Organisation organisation = organisationService.createOrganisation(random(), random(), null, organisationSuper,
				null);
		Curriculum curriculum = curriculumService.createCurriculum(random(), random(), null, false, organisation);
		CurriculumElement curriculumElement = curriculumService.createCurriculumElement(random(), random(),
				CurriculumElementStatus.active, oneDayAgo(), inOneDay(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);

		Organisation organisationSub = organisationService.createOrganisation(random(), random(), null, organisation,
				null);
		Curriculum curriculumSubOrg = curriculumService.createCurriculum(random(), random(), null, false, organisationSub);
		CurriculumElement curriculumElementSubOrg = curriculumService.createCurriculumElement(random(), random(),
				CurriculumElementStatus.active, oneDayAgo(), inOneDay(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculumSubOrg);

		Organisation otherOrganisation = organisationService.createOrganisation(random(), random(), null, null, null);
		Curriculum otherCurriculum = curriculumService.createCurriculum(random(), random(), null, false, otherOrganisation);
		CurriculumElement otherCurriculumElement = curriculumService.createCurriculumElement(random(), random(),
				CurriculumElementStatus.active, oneDayAgo(), inOneDay(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, otherCurriculum);
		dbInstance.commitAndCloseSession();

		SearchParameters searchParams = new SearchParameters();
		searchParams.setOrganisationRefs(asList(organisation));
		List<CurriculumElement> pending = sut.loadPending(searchParams);

		assertThat(pending)
				.contains(curriculumElement, curriculumElementSubOrg)
				.doesNotContain(curriculumElementSuperOrg, otherCurriculumElement);
	}
	
	@Test
	public void shouldFilterByCurriculumElementType() {
		Organisation organisation = organisationService.createOrganisation(random(), random(), null, null, null);
		CurriculumElementType ceType = curriculumService.createCurriculumElementType(random(), random(), null, null);
		Curriculum curriculum = curriculumService.createCurriculum(random(), random(), null, false, organisation);
		CurriculumElement curriculumElement = curriculumService.createCurriculumElement(random(), random(),
				CurriculumElementStatus.active, oneDayAgo(), inOneDay(), null, ceType, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commitAndCloseSession();

		CurriculumElementType otherCeType = curriculumService.createCurriculumElementType(random(), random(), null,
				null);
		CurriculumElement otherCurriculumElement = curriculumService.createCurriculumElement(random(), random(),
				CurriculumElementStatus.active, oneDayAgo(), inOneDay(), null, otherCeType,
				CurriculumCalendars.disabled, CurriculumLectures.disabled, CurriculumLearningProgress.disabled,
				curriculum);
		dbInstance.commitAndCloseSession();

		SearchParameters searchParams = new SearchParameters();
		searchParams.setCeTypeKey(ceType.getKey());
		List<CurriculumElement> pending = sut.loadPending(searchParams);

		assertThat(pending).contains(curriculumElement).doesNotContain(otherCurriculumElement);
	}
	
	@Test
	public void shouldFilterByWhiteList() {
		Organisation organisation = organisationService.createOrganisation(random(), random(), null, null, null);
		Curriculum curriculum = curriculumService.createCurriculum(random(), random(), null, false, organisation);
		dbInstance.commitAndCloseSession();

		CurriculumElement curriculumElement1 = curriculumService.createCurriculumElement(random(), random(),
				CurriculumElementStatus.active, oneDayAgo(), inOneDay(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement curriculumElement2 = curriculumService.createCurriculumElement(random(), random(),
				CurriculumElementStatus.active, oneDayAgo(), inOneDay(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement otherCurriculumElement = curriculumService.createCurriculumElement(random(), random(),
				CurriculumElementStatus.active, oneDayAgo(), inOneDay(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commitAndCloseSession();

		SearchParameters searchParams = new SearchParameters();
		searchParams.setWhiteListRefs(asList(curriculumElement1, curriculumElement2));
		List<CurriculumElement> pending = sut.loadPending(searchParams);

		assertThat(pending)
				.containsExactlyInAnyOrder(curriculumElement1, curriculumElement2)
				.doesNotContain(otherCurriculumElement);
	}
	
	@Test
	public void shouldFilterByBlackList() {
		Organisation organisation = organisationService.createOrganisation(random(), random(), null, null, null);
		Curriculum curriculum = curriculumService.createCurriculum(random(), random(), null, false, organisation);
		CurriculumElementType type = curriculumService.createCurriculumElementType(random(), random(), random(), null);
		dbInstance.commitAndCloseSession();

		CurriculumElement curriculumElement1 = curriculumService.createCurriculumElement(random(), random(),
				CurriculumElementStatus.active, oneDayAgo(), inOneDay(), null, type, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement curriculumElement2 = curriculumService.createCurriculumElement(random(), random(),
				CurriculumElementStatus.active, oneDayAgo(), inOneDay(), null, type, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement otherCurriculumElement = curriculumService.createCurriculumElement(random(), random(),
				CurriculumElementStatus.active, oneDayAgo(), inOneDay(), null, type, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commitAndCloseSession();

		SearchParameters searchParams = new SearchParameters();
		searchParams.setCeTypeKey(type.getKey());
		searchParams.setBlackListRefs(asList(otherCurriculumElement));
		List<CurriculumElement> pending = sut.loadPending(searchParams);

		assertThat(pending)
				.containsExactlyInAnyOrder(curriculumElement1, curriculumElement2)
				.doesNotContain(otherCurriculumElement);
	}
	
	@Test
	public void shouldFilterByBeginDate() {
		Organisation organisation = organisationService.createOrganisation(random(), random(), null, null, null);
		Curriculum curriculum = curriculumService.createCurriculum(random(), random(), null, false, organisation);
		dbInstance.commitAndCloseSession();

		CurriculumElement beginBeforeFrom = curriculumService.createCurriculumElement(random(), random(),
				CurriculumElementStatus.active, oneYearAgo(), oneYearAgo(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement beginBetweenFromAndTo = curriculumService.createCurriculumElement(random(), random(),
				CurriculumElementStatus.active, oneDayAgo(), oneDayAgo(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement beginAfterTo = curriculumService.createCurriculumElement(random(), random(),
				CurriculumElementStatus.active, inOneDay(), inOneDay(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement beginNull = curriculumService.createCurriculumElement(random(), random(),
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		beginAfterTo.setBeginDate(inOneDay());
		dbInstance.commitAndCloseSession();

		SearchParameters searchParams = new SearchParameters();
		searchParams.setFrom(oneMonthAgo());
		searchParams.setTo(today());
		searchParams.setStartDate(true);
		List<CurriculumElement> pending = sut.loadPending(searchParams);

		assertThat(pending).contains(beginBetweenFromAndTo).doesNotContain(beginBeforeFrom, beginAfterTo, beginNull);
	}
	
	@Test
	public void shouldFilterByEndDate() {
		Organisation organisation = organisationService.createOrganisation(random(), random(), null, null, null);
		Curriculum curriculum = curriculumService.createCurriculum(random(), random(), null, false, organisation);
		dbInstance.commitAndCloseSession();

		CurriculumElement endBeforeFrom = curriculumService.createCurriculumElement(random(), random(),
				CurriculumElementStatus.active, oneYearAgo(), oneYearAgo(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement endBetweenFromAndTo = curriculumService.createCurriculumElement(random(), random(),
				CurriculumElementStatus.active, oneDayAgo(), oneDayAgo(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement endAfterTo = curriculumService.createCurriculumElement(random(), random(),
				CurriculumElementStatus.active, inOneDay(), inOneDay(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement endNull = curriculumService.createCurriculumElement(random(), random(),
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		endAfterTo.setBeginDate(inOneDay());
		dbInstance.commitAndCloseSession();

		SearchParameters searchParams = new SearchParameters();
		searchParams.setFrom(oneMonthAgo());
		searchParams.setTo(today());
		searchParams.setStartDate(false);
		List<CurriculumElement> pending = sut.loadPending(searchParams);

		assertThat(pending).contains(endBetweenFromAndTo).doesNotContain(endBeforeFrom, endAfterTo, endNull);
	}
	
	@Test
	public void shouldFilterActiveOnly() {
		Organisation organisation = organisationService.createOrganisation(random(), random(), null, null, null);
		Curriculum curriculum = curriculumService.createCurriculum(random(), random(), null, false, organisation);
		CurriculumElement active = curriculumService.createCurriculumElement(random(), random(),
				CurriculumElementStatus.active, oneDayAgo(), inOneDay(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commitAndCloseSession();

		CurriculumElement inactive = curriculumService.createCurriculumElement(random(), random(),
				CurriculumElementStatus.active, oneDayAgo(), inOneDay(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		inactive.setElementStatus(CurriculumElementStatus.inactive);
		inactive = curriculumService.updateCurriculumElement(inactive);
		CurriculumElement deleted = curriculumService.createCurriculumElement(random(), random(),
				CurriculumElementStatus.active, oneDayAgo(), inOneDay(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		deleted.setElementStatus(CurriculumElementStatus.deleted);
		deleted = curriculumService.updateCurriculumElement(deleted);
		dbInstance.commitAndCloseSession();

		SearchParameters searchParams = new SearchParameters();
		List<CurriculumElement> pending = sut.loadPending(searchParams);

		assertThat(pending).contains(active).doesNotContain(inactive, deleted);
	}
	
	private String random() {
		return UUID.randomUUID().toString();
	}
	
	private Date oneDayAgo() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, -1);
		return calendar.getTime();
	}
	
	private Date oneMonthAgo() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, -1);
		return calendar.getTime();
	}
	
	private Date oneYearAgo() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -1);
		return calendar.getTime();
	}

	private Date inOneDay() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, 1);
		return calendar.getTime();
	}
	
	private Date today() {
		Calendar calendar = Calendar.getInstance();
		return calendar.getTime();
	}

}

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
package org.olat.modules.curriculum.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.util.List;

import org.junit.Test;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumMember;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 18 May 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public class CurriculumElementToDoProviderGetCandidatesTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private CurriculumElementToDoProvider curriculumElementToDoProvider;

	@Test
	public void shouldReturnCurriculumElementMembers() {
		CurriculumElement element = createCurriculumElement();
		Identity member = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		curriculumService.addMember(element, member, CurriculumRoles.curriculumelementowner, JunitTestHelper.getDefaultActor());
		dbInstance.commitAndCloseSession();

		List<CurriculumMember> candidates = curriculumElementToDoProvider.getCandidates(element);

		assertThat(candidates).extracting(CurriculumMember::getIdentity).contains(member);
	}

	@Test
	public void shouldReturnCurriculumOwners() {
		CurriculumElement element = createCurriculumElement();
		Identity member = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		curriculumService.addMember(element.getCurriculum(), member, CurriculumRoles.curriculumowner);
		dbInstance.commitAndCloseSession();

		List<CurriculumMember> candidates = curriculumElementToDoProvider.getCandidates(element);

		assertThat(candidates).extracting(CurriculumMember::getIdentity).contains(member);
	}

	@Test
	public void shouldReturnOrgCurriculumManagers() {
		CurriculumElement element = createCurriculumElement();
		Identity member = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		organisationService.addMember(element.getCurriculum().getOrganisation(), member,
				OrganisationRoles.curriculummanager, JunitTestHelper.getDefaultActor());
		dbInstance.commitAndCloseSession();

		List<CurriculumMember> candidates = curriculumElementToDoProvider.getCandidates(element);

		assertThat(candidates).extracting(CurriculumMember::getIdentity).contains(member);
	}

	@Test
	public void shouldReturnAdministrators() {
		CurriculumElement element = createCurriculumElement();
		Identity member = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		organisationService.addMember(element.getCurriculum().getOrganisation(), member,
				OrganisationRoles.administrator, JunitTestHelper.getDefaultActor());
		dbInstance.commitAndCloseSession();

		List<CurriculumMember> candidates = curriculumElementToDoProvider.getCandidates(element);

		assertThat(candidates).extracting(CurriculumMember::getIdentity).contains(member);
	}

	@Test
	public void shouldReturnOnlyMembersInAllElements() {
		Organisation org = organisationService.createOrganisation(random(), random(), null, null, null,
				JunitTestHelper.getDefaultActor());
		Curriculum curriculum = curriculumService.createCurriculum(random(), random(), null, false, org);
		CurriculumElement e1 = curriculumService.createCurriculumElement(random(), random(),
				CurriculumElementStatus.active, null, null, null, null,
				CurriculumCalendars.disabled, CurriculumLectures.disabled, CurriculumLearningProgress.disabled,
				curriculum);
		CurriculumElement e2 = curriculumService.createCurriculumElement(random(), random(),
				CurriculumElementStatus.active, null, null, null, null,
				CurriculumCalendars.disabled, CurriculumLectures.disabled, CurriculumLearningProgress.disabled,
				curriculum);
		Identity ownerBoth = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Identity ownerOne = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		curriculumService.addMember(e1, ownerBoth, CurriculumRoles.curriculumelementowner, JunitTestHelper.getDefaultActor());
		curriculumService.addMember(e2, ownerBoth, CurriculumRoles.curriculumelementowner, JunitTestHelper.getDefaultActor());
		curriculumService.addMember(e1, ownerOne, CurriculumRoles.curriculumelementowner, JunitTestHelper.getDefaultActor());
		dbInstance.commitAndCloseSession();

		List<CurriculumMember> candidates = curriculumElementToDoProvider.getCandidates(List.of(e1, e2));

		assertThat(candidates).extracting(CurriculumMember::getIdentity)
				.contains(ownerBoth)
				.doesNotContain(ownerOne);
	}

	@Test
	public void shouldNotReturnMembersOfOtherOrg() {
		CurriculumElement element = createCurriculumElement();
		Organisation otherOrg = organisationService.createOrganisation(random(), random(), null, null, null,
				JunitTestHelper.getDefaultActor());
		Identity otherOrgAdmin = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		organisationService.addMember(otherOrg, otherOrgAdmin, OrganisationRoles.administrator,
				JunitTestHelper.getDefaultActor());
		dbInstance.commitAndCloseSession();

		List<CurriculumMember> candidates = curriculumElementToDoProvider.getCandidates(element);

		assertThat(candidates).extracting(CurriculumMember::getIdentity).doesNotContain(otherOrgAdmin);
	}

	private CurriculumElement createCurriculumElement() {
		Organisation org = organisationService.createOrganisation(random(), random(), null, null, null,
				JunitTestHelper.getDefaultActor());
		Curriculum curriculum = curriculumService.createCurriculum(random(), random(), null, false, org);
		CurriculumElement element = curriculumService.createCurriculumElement(random(), random(),
				CurriculumElementStatus.active, null, null, null, null,
				CurriculumCalendars.disabled, CurriculumLectures.disabled, CurriculumLearningProgress.disabled,
				curriculum);
		dbInstance.commitAndCloseSession();
		return element;
	}

}

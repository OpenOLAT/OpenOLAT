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
package org.olat.modules.curriculum.manager;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumMember;
import org.olat.modules.curriculum.model.SearchMemberParameters;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 juil. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumMemberQueriesTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private CurriculumMemberQueries memberQueries;
	
	@Test
	public void getCurriculumMembers() {
		// add a curriculum manager
		Identity manager = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-manager-1");
		Curriculum curriculum = curriculumService.createCurriculum("CUR-1", "Curriculum 1", "Short desc.", false, null);
		dbInstance.commitAndCloseSession();
		curriculumService.addMember(curriculum, manager, CurriculumRoles.curriculumowner);
		dbInstance.commitAndCloseSession();
		
		// get memberships
		SearchMemberParameters params = new SearchMemberParameters();
		List<CurriculumMember> members = memberQueries.getMembers(curriculum, params);
		Assert.assertNotNull(members);
		Assert.assertEquals(1, members.size());
		Assert.assertEquals(CurriculumRoles.curriculumowner.name(), members.get(0).getRole());
		Assert.assertEquals(manager, members.get(0).getIdentity());	
	}
	
	@Test
	public void getCurriculumElementMembers() {
		Identity supervisor = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-supervisor-1");
		Curriculum curriculum = curriculumService.createCurriculum("cur-for-el-4", "Curriculum for element", "Curriculum", false, null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-4", "4. Element",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		curriculumService.addMember(element, supervisor, CurriculumRoles.curriculumelementowner);
		dbInstance.commitAndCloseSession();
		
		SearchMemberParameters params = new SearchMemberParameters();
		List<CurriculumMember> members = memberQueries.getMembers(element, params);
		Assert.assertNotNull(members);
		Assert.assertEquals(1, members.size());
		CurriculumMember member = members.get(0);
		Assert.assertEquals(supervisor, member.getIdentity());
		Assert.assertEquals(CurriculumRoles.curriculumelementowner.name(), member.getRole());
	}
	
	@Test
	public void getCurriculumElementMembersByLogin() {
		Identity masterCoach = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-supervisor-5");
		Curriculum curriculum = curriculumService.createCurriculum("cur-for-el-5", "Curriculum for element", "Curriculum", false, null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-5", "5. Element",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		curriculumService.addMember(element, masterCoach, CurriculumRoles.mastercoach);
		dbInstance.commitAndCloseSession();
		
		SearchMemberParameters params = new SearchMemberParameters();
		params.setLogin("cur-supervisor-5");
		List<CurriculumMember> members = memberQueries.getMembers(element, params);
		Assert.assertNotNull(members);
		Assert.assertEquals(1, members.size());
		CurriculumMember member = members.get(0);
		Assert.assertEquals(masterCoach, member.getIdentity());
		Assert.assertEquals(CurriculumRoles.mastercoach.name(), member.getRole());
		
		// negative test
		SearchMemberParameters negativeParams = new SearchMemberParameters();
		negativeParams.setLogin("cur-five");
		List<CurriculumMember> notFoundMembers = memberQueries.getMembers(element, negativeParams);
		Assert.assertNotNull(notFoundMembers);
		Assert.assertTrue(notFoundMembers.isEmpty());
	}

	@Test
	public void getCurriculumElementMembersByRoles() {
		Identity masterCoach = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-mastercoach-7");
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-owner-8");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-participant-9");
		Curriculum curriculum = curriculumService.createCurriculum("cur-for-el-6", "Curriculum for element", "Curriculum", false, null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-6", "6. Element",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		curriculumService.addMember(element, masterCoach, CurriculumRoles.mastercoach);
		curriculumService.addMember(element, owner, CurriculumRoles.owner);
		curriculumService.addMember(element, participant, CurriculumRoles.participant);
		dbInstance.commitAndCloseSession();
		
		// 1 role
		SearchMemberParameters ownerParams = new SearchMemberParameters();
		ownerParams.setRoles(List.of(CurriculumRoles.owner));
		List<CurriculumMember> owners = memberQueries.getMembers(element, ownerParams);
		assertThat(owners)
			.hasSize(1)
			.extracting(CurriculumMember::getIdentity)
			.containsExactly(owner);
		
		// 2 roles
		SearchMemberParameters twoParams = new SearchMemberParameters();
		twoParams.setRoles(List.of(CurriculumRoles.mastercoach, CurriculumRoles.participant));
		List<CurriculumMember> towRolesMembers = memberQueries.getMembers(element, twoParams);
		assertThat(towRolesMembers)
			.hasSize(2)
			.extracting(CurriculumMember::getIdentity)
			.containsExactlyInAnyOrder(masterCoach, participant);
		
		// negative
		SearchMemberParameters noParams = new SearchMemberParameters();
		noParams.setRoles(List.of(CurriculumRoles.curriculumelementowner));
		List<CurriculumMember> noMembers = memberQueries.getMembers(element, noParams);
		Assert.assertTrue(noMembers.isEmpty());
	}
}

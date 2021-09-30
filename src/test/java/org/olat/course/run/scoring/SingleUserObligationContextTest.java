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
package org.olat.course.run.scoring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.miniRandom;
import static org.olat.test.JunitTestHelper.random;

import java.util.Collections;

import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 2 Sep 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SingleUserObligationContextTest extends OlatTestCase {
	
	private Identity admin;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private AssessmentService assessmentService;
	
	@Before
	public void setUp() {
		admin = JunitTestHelper.createAndPersistIdentityAsUser("suoct-admin");
	}

	@Test
	public void shouldBeNotGroupParticipantIfNotGroupMember() {
		BusinessGroup businessGroup = createBusinessGroup();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser(random());
		dbInstance.commitAndCloseSession();
		
		SingleUserObligationContext sut = new SingleUserObligationContext();
		boolean participant = sut.isParticipant(identity, businessGroup);
		
		assertThat(participant).isFalse();
	}
	
	@Test
	public void shouldBeNotGroupParticipantIfGroupOwner() {
		BusinessGroup businessGroup = createBusinessGroup();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser(random());
		businessGroupService.addOwners(admin, Roles.administratorRoles(), Collections.singletonList(identity), businessGroup, null);
		dbInstance.commitAndCloseSession();
		
		SingleUserObligationContext sut = new SingleUserObligationContext();
		boolean participant = sut.isParticipant(identity, businessGroup);
		
		assertThat(participant).isFalse();
	}
	
	@Test
	public void shouldBeGroupParticipantIfGroupParticipant() {
		BusinessGroup businessGroup = createBusinessGroup();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser(random());
		businessGroupService.addParticipants(admin, Roles.administratorRoles(), Collections.singletonList(identity), businessGroup, null);
		dbInstance.commitAndCloseSession();
		
		SingleUserObligationContext sut = new SingleUserObligationContext();
		boolean participant = sut.isParticipant(identity, businessGroup);
		
		assertThat(participant).isTrue();
	}

	private BusinessGroup createBusinessGroup() {
		return businessGroupService.createBusinessGroup(admin, random(), random(), miniRandom(), null, null, null, null, false, false, null);
	}
	
	@Test
	public void shouldBeOrganisationMember() {
		Organisation organisation = organisationService.createOrganisation(random(), miniRandom(), random(), organisationService.getDefaultOrganisation(), null);
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser(random());
		organisationService.addMember(organisation, identity, OrganisationRoles.author);
		Organisation organisationOther = organisationService.createOrganisation(random(), miniRandom(), random(), organisationService.getDefaultOrganisation(), null);
		Identity identityOther = JunitTestHelper.createAndPersistIdentityAsUser(random());
		organisationService.addMember(organisationOther, identityOther, OrganisationRoles.author);
		dbInstance.commitAndCloseSession();
		
		SingleUserObligationContext sut = new SingleUserObligationContext();
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(sut.isMember(identity, organisation)).isTrue();
		softly.assertThat(sut.isMember(identity, organisationOther)).isFalse();
		softly.assertThat(sut.isMember(identityOther, organisation)).isFalse();
		softly.assertAll();
	}
	
	@Test
	public void shouldBeCurriculumElementParticipant() {
		Curriculum curriculum = curriculumService.createCurriculum(random(), random(), random(), organisationService.getDefaultOrganisation());
		CurriculumElement curriculumElement = curriculumService.createCurriculumElement(random(), random(), CurriculumElementStatus.active, null, null, null, null, null, null, null, curriculum);
		CurriculumElement curriculumElementOther = curriculumService.createCurriculumElement(random(), random(), CurriculumElementStatus.active, null, null, null, null, null, null, null, curriculum);
		Identity participant = JunitTestHelper.createAndPersistIdentityAsUser(random());
		curriculumService.addMember(curriculumElement, participant, CurriculumRoles.participant);
		Identity coach = JunitTestHelper.createAndPersistIdentityAsUser(random());
		curriculumService.addMember(curriculumElement, coach, CurriculumRoles.coach);
		Identity participantOther = JunitTestHelper.createAndPersistIdentityAsUser(random());
		curriculumService.addMember(curriculumElementOther, participantOther, CurriculumRoles.participant);
		dbInstance.commitAndCloseSession();
		
		SingleUserObligationContext sut = new SingleUserObligationContext();
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(sut.isParticipant(participant, curriculumElement)).isTrue();
		softly.assertThat(sut.isParticipant(participant, curriculumElementOther)).isFalse();
		softly.assertThat(sut.isParticipant(coach, curriculumElement)).isFalse();
		softly.assertThat(sut.isParticipant(participantOther, curriculumElement)).isFalse();
		softly.assertAll();
	}
	
	@Test
	public void shouldCheckIfPassed() {

	}


}

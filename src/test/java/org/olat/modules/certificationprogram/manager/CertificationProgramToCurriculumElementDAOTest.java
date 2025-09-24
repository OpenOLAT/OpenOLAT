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
package org.olat.modules.certificationprogram.manager;

import java.util.Date;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramToCurriculumElement;
import org.olat.modules.certificationprogram.model.CertificationCurriculumElementWithInfos;
import org.olat.modules.certificationprogram.model.CertificationProgramMemberSearchParameters;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.manager.CurriculumDAO;
import org.olat.modules.curriculum.manager.CurriculumElementDAO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 1 sept. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificationProgramToCurriculumElementDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CurriculumDAO curriculumDao;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private CurriculumElementDAO curriculumElementDao;
	@Autowired
	private CertificationProgramDAO certificationProgramDao;
	@Autowired
	private CertificationProgramToCurriculumElementDAO certificationProgramToCurriculumElementDao;
	
	
	@Test
	public void createRelation() {
		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-el-1", "Curriculum for element", "Curriculum", false, null);
		CurriculumElement element = curriculumElementDao.createCurriculumElement("Element-1", "1. Element",
				CurriculumElementStatus.active, new Date(), new Date(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CertificationProgram program = certificationProgramDao.createCertificationProgram("program-to-curriculum-1", "Program to curriculum");
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(element);
		Assert.assertNotNull(program);
	
		CertificationProgramToCurriculumElement relation = certificationProgramToCurriculumElementDao.createRelation(program, element);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(relation);
		Assert.assertNotNull(relation.getKey());
		Assert.assertNotNull(relation.getCreationDate());
		Assert.assertEquals(program, relation.getCertificationProgram());
		Assert.assertEquals(element, relation.getCurriculumElement());
	}
	
	@Test
	public void hasRelation() {
		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-el-2", "Curriculum for element", "Curriculum", false, null);
		CurriculumElement element = curriculumElementDao.createCurriculumElement("Element-2", "2. Element",
				CurriculumElementStatus.active, new Date(), new Date(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CertificationProgram program = certificationProgramDao.createCertificationProgram("program-to-curriculum-2", "Program to curriculum");
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(element);
		Assert.assertNotNull(program);
	
		CertificationProgramToCurriculumElement relation = certificationProgramToCurriculumElementDao.createRelation(program, element);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(relation);
		
		boolean hasRelation = certificationProgramToCurriculumElementDao.hasCurriculumElement(program, element);
		Assert.assertTrue(hasRelation);	
	}
	
	@Test
	public void hasNotRelation() {
		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-el-3", "Curriculum for element", "Curriculum", false, null);
		CurriculumElement element = curriculumElementDao.createCurriculumElement("Element-3", "3. Element",
				CurriculumElementStatus.active, new Date(), new Date(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CertificationProgram program = certificationProgramDao.createCertificationProgram("program-to-curriculum-3", "Program to curriculum");
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(element);
		Assert.assertNotNull(program);
	
		boolean hasNotRelation = certificationProgramToCurriculumElementDao.hasCurriculumElement(program, element);
		Assert.assertFalse(hasNotRelation);	
	}
	
	@Test
	public void getCurriculumElementsForProgram() {
		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-el-4", "Curriculum for element", "Curriculum", false, null);
		CurriculumElement element = curriculumElementDao.createCurriculumElement("Element-4", "4. Element",
				CurriculumElementStatus.active, new Date(), new Date(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CertificationProgram program = certificationProgramDao.createCertificationProgram("program-to-curriculum-4", "Program to curriculum");
		CertificationProgramToCurriculumElement relation = certificationProgramToCurriculumElementDao.createRelation(program, element);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(element);
		Assert.assertNotNull(program);
		Assert.assertNotNull(relation);
		
		List<CertificationCurriculumElementWithInfos> elementsInfosList = certificationProgramToCurriculumElementDao.getCurriculumElementsFor(program, new Date());
		dbInstance.commitAndCloseSession();
		Assertions.assertThat(elementsInfosList)
			.hasSize(1)
			.map(CertificationCurriculumElementWithInfos::curriculumElement)
			.containsExactly(element);
	}
	
	@Test
	public void getCertificationProgramForCurriculumElement() {
		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-el-5", "Curriculum for element", "Curriculum", false, null);
		CurriculumElement element = curriculumElementDao.createCurriculumElement("Element-5", "5. Element",
				CurriculumElementStatus.active, new Date(), new Date(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CertificationProgram program = certificationProgramDao.createCertificationProgram("program-to-curriculum-5", "Program to curriculum");
		CertificationProgramToCurriculumElement relation = certificationProgramToCurriculumElementDao.createRelation(program, element);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(element);
		Assert.assertNotNull(program);
		Assert.assertNotNull(relation);
		
		CertificationProgram elementsProgram = certificationProgramToCurriculumElementDao.getCertificationProgram(element);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(elementsProgram);
		Assert.assertEquals(program, elementsProgram);
	}
	
	@Test
	public void getRelationsOfCurriculumElement() {
		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-el-5", "Curriculum for element", "Curriculum", false, null);
		CurriculumElement element = curriculumElementDao.createCurriculumElement("Element-5", "5. Element",
				CurriculumElementStatus.active, new Date(), new Date(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CertificationProgram program = certificationProgramDao.createCertificationProgram("program-to-curriculum-5", "Program to curriculum");
		CertificationProgramToCurriculumElement relation = certificationProgramToCurriculumElementDao.createRelation(program, element);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(element);
		Assert.assertNotNull(program);
		Assert.assertNotNull(relation);
		
		List<CertificationProgramToCurriculumElement> relations = certificationProgramToCurriculumElementDao.getRelations(element);
		Assertions.assertThat(relations)
			.hasSize(1)
			.map(CertificationProgramToCurriculumElement::getCertificationProgram)
			.containsExactly(program);
	}
	
	@Test
	public void getMembersByProgram() {
		Identity actor = JunitTestHelper.getDefaultActor();
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("prog-coach-1");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("prog-participant-1");
		
		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-el-4", "Curriculum for element", "Curriculum", false, null);
		CurriculumElement element = curriculumElementDao.createCurriculumElement("Element-4", "4. Element",
				CurriculumElementStatus.active, new Date(), new Date(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CertificationProgram program = certificationProgramDao.createCertificationProgram("program-to-curriculum-4", "Program to curriculum");
		CertificationProgramToCurriculumElement relation = certificationProgramToCurriculumElementDao.createRelation(program, element);
		curriculumService.addMember(element, coach, CurriculumRoles.coach, actor);
		curriculumService.addMember(element, participant, CurriculumRoles.participant, actor);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(element);
		Assert.assertNotNull(program);
		Assert.assertNotNull(relation);
		
		CertificationProgramMemberSearchParameters searchParams = new CertificationProgramMemberSearchParameters(program);
		List<Identity> membersInfosList = certificationProgramToCurriculumElementDao.getMembers(searchParams);
		dbInstance.commitAndCloseSession();
		Assertions.assertThat(membersInfosList)
			.hasSize(1)
			.containsExactly(participant);
	}
}

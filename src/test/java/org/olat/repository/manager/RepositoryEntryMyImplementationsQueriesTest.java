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

import java.util.Date;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
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
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CurriculumDAO curriculumDao;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private CurriculumElementDAO curriculumElementDao;
	@Autowired
	private CurriculumElementTypeDAO curriculumElementTypeDao;
	@Autowired
	private RepositoryEntryMyImplementationsQueries myImplementationsQueries;
	
	@Test
	public void searchImplementations() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("my-implementations-view-1");
		dbInstance.commit();
		
		List<CurriculumElement> list = myImplementationsQueries.searchImplementations(id, false, true);
		Assert.assertNotNull(list);
	}
	
	@Test
	public void searchImplementationsBookmarks() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("my-implementations-view-2");
		dbInstance.commit();
		
		List<CurriculumElement> list = myImplementationsQueries.searchImplementations(id, true, true);
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

		List<CurriculumElement> list = myImplementationsQueries.searchImplementations(participant, false, true);
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

		List<CurriculumElement> list = myImplementationsQueries.searchImplementations(participant, false, true);
		Assertions.assertThat(list)
			.containsExactly(element);
	}
	
	@Test
	public void searchSingleCourseImplementations() {
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("my-implementations-view-2");
		
		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-impl-1", "Curriculum for implementation", "Curriculum", false,
				JunitTestHelper.getDefaultOrganisation());
		CurriculumElementType structuredType = curriculumElementTypeDao.createCurriculumElementType("typ-single-cur-el-1", "Single course implementation", "", "");
		structuredType.setAllowedAsRootElement(true);
		structuredType.setMaxRepositoryEntryRelations(1);
		structuredType.setSingleElement(true);
		structuredType = curriculumElementTypeDao.update(structuredType);
			
		CurriculumElement element = curriculumElementDao.createCurriculumElement("Element-1", "1. Element",
				CurriculumElementStatus.active, new Date(), new Date(), null, structuredType, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		curriculumService.addMember(element, participant, CurriculumRoles.participant, JunitTestHelper.getDefaultActor());
		dbInstance.commitAndCloseSession();

		List<CurriculumElement> list = myImplementationsQueries.searchImplementations(participant, false, true);
		Assertions.assertThat(list)
			.isEmpty();
	}
	
	
	
}

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
package org.olat.modules.curriculum.ui.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.manager.CurriculumDAO;
import org.olat.modules.curriculum.manager.CurriculumElementDAO;
import org.olat.modules.curriculum.ui.CurriculumElementWithViewsRow;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.model.RepositoryEntryMyCourseImpl;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;



/**
 * 
 * Initial date: 26 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementViewsRowComparatorTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CurriculumDAO curriculumDao;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private CurriculumElementDAO curriculumElementDao;
	
	@Test
	public void testCurriculumElementActiveInactive() {
		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-el-1", "Curriculum for element", "Curriculum", false, null);
		CurriculumElement element1 = curriculumElementDao.createCurriculumElement("Element-1", "1. Element",
				CurriculumElementStatus.inactive, new Date(), new Date(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element2 = curriculumElementDao.createCurriculumElement("Element-1", "2. Element",
				CurriculumElementStatus.inactive, new Date(), new Date(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element3 = curriculumElementDao.createCurriculumElement("Element-1", "3. Element",
				CurriculumElementStatus.active, new Date(), new Date(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element4 = curriculumElementDao.createCurriculumElement("Element-1", "4. Element",
				CurriculumElementStatus.active, new Date(), new Date(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commitAndCloseSession();
		
		CurriculumElementWithViewsRow row1 = new CurriculumElementWithViewsRow(element1, null, 0);
		CurriculumElementWithViewsRow row2 = new CurriculumElementWithViewsRow(element2, null, 0);
		CurriculumElementWithViewsRow row3 = new CurriculumElementWithViewsRow(element3, null, 0);
		CurriculumElementWithViewsRow row4 = new CurriculumElementWithViewsRow(element4, null, 0);

		List<CurriculumElementWithViewsRow> rows = new ArrayList<>();
		rows.add(row1);
		rows.add(row2);
		rows.add(row3);
		rows.add(row4);
		
		Collections.sort(rows, new CurriculumElementViewsRowComparator(Locale.ENGLISH));
		
		Assert.assertEquals(element3.getKey(), rows.get(0).getCurriculumElementKey());
		Assert.assertEquals(element4.getKey(), rows.get(1).getCurriculumElementKey());
		Assert.assertEquals(element1.getKey(), rows.get(2).getCurriculumElementKey());
		Assert.assertEquals(element2.getKey(), rows.get(3).getCurriculumElementKey());
	}
	
	/**
	 * Simulate a list of repository entries under the same curriculum element.
	 * 
	 */
	@Test
	public void testRepositoryEntryActiveInactive() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("sort-cur-el");

		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-el-1", "Curriculum for element", "Curriculum", false, null);
		CurriculumElement element = curriculumElementDao.createCurriculumElement("Element-1", "1. Element",
				CurriculumElementStatus.inactive, new Date(), new Date(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commitAndCloseSession();

		RepositoryEntry entry1 = JunitTestHelper.deployBasicCourse(author, "1 course", RepositoryEntryStatusEnum.closed, false, false);
		RepositoryEntry entry2 = JunitTestHelper.deployBasicCourse(author, "2 course", RepositoryEntryStatusEnum.trash, false, false);
		RepositoryEntry entry3 = JunitTestHelper.deployBasicCourse(author, "3 course", RepositoryEntryStatusEnum.published, false, false);
		RepositoryEntry entry4 = JunitTestHelper.deployBasicCourse(author, "4 course", RepositoryEntryStatusEnum.published, false, false);
		// add the course and a participant to the curriculum
		curriculumService.addRepositoryEntry(element, entry1, false);
		curriculumService.addRepositoryEntry(element, entry2, false);
		curriculumService.addRepositoryEntry(element, entry3, false);
		curriculumService.addRepositoryEntry(element, entry4, false);
		dbInstance.commitAndCloseSession();
		
		CurriculumElementWithViewsRow row1 = new CurriculumElementWithViewsRow(element, null,
				new RepositoryEntryMyCourseImpl(entry1, null, false, 0, 0), false);
		CurriculumElementWithViewsRow row2 = new CurriculumElementWithViewsRow(element, null,
				new RepositoryEntryMyCourseImpl(entry2, null, false, 0, 0), false);
		CurriculumElementWithViewsRow row3 = new CurriculumElementWithViewsRow(element, null,
				new RepositoryEntryMyCourseImpl(entry3, null, false, 0, 0), false);
		CurriculumElementWithViewsRow row4 = new CurriculumElementWithViewsRow(element, null,
				new RepositoryEntryMyCourseImpl(entry4, null, false, 0, 0), false);

		List<CurriculumElementWithViewsRow> rows = new ArrayList<>();
		rows.add(row1);
		rows.add(row2);
		rows.add(row3);
		rows.add(row4);
		
		Collections.sort(rows, new CurriculumElementViewsRowComparator(Locale.ENGLISH));

		Assert.assertEquals(entry3.getKey(), rows.get(0).getRepositoryEntryKey());
		Assert.assertEquals(entry4.getKey(), rows.get(1).getRepositoryEntryKey());
		Assert.assertEquals(entry1.getKey(), rows.get(2).getRepositoryEntryKey());
		Assert.assertEquals(entry2.getKey(), rows.get(3).getRepositoryEntryKey());
	}
	
	@Test
	public void testRepositoryEntryClosed_underParent() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("sort-cur-el");

		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-el-1", "Curriculum for element", "Curriculum", false, null);
		CurriculumElement element = curriculumElementDao.createCurriculumElement("Element-1", "1. Element",
				CurriculumElementStatus.inactive, new Date(), new Date(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commitAndCloseSession();

		RepositoryEntry entry1 = JunitTestHelper.deployBasicCourse(author, "1 course", RepositoryEntryStatusEnum.closed, false, false);
		RepositoryEntry entry2 = JunitTestHelper.deployBasicCourse(author, "2 course", RepositoryEntryStatusEnum.trash, false, false);
		RepositoryEntry entry3 = JunitTestHelper.deployBasicCourse(author, "3 course", RepositoryEntryStatusEnum.published, false, false);
		RepositoryEntry entry4 = JunitTestHelper.deployBasicCourse(author, "4 course", RepositoryEntryStatusEnum.published, false, false);
		// add the course and a participant to the curriculum
		curriculumService.addRepositoryEntry(element, entry1, false);
		curriculumService.addRepositoryEntry(element, entry2, false);
		curriculumService.addRepositoryEntry(element, entry3, false);
		curriculumService.addRepositoryEntry(element, entry4, false);
		dbInstance.commitAndCloseSession();
		

		CurriculumElementWithViewsRow parent = new CurriculumElementWithViewsRow(element, null, 4);
		
		CurriculumElementWithViewsRow row1 = new CurriculumElementWithViewsRow(element, null,
				new RepositoryEntryMyCourseImpl(entry1, null, false, 0, 0), false);
		row1.setParent(parent);
		CurriculumElementWithViewsRow row2 = new CurriculumElementWithViewsRow(element, null,
				new RepositoryEntryMyCourseImpl(entry2, null, false, 0, 0), false);
		row2.setParent(parent);
		CurriculumElementWithViewsRow row3 = new CurriculumElementWithViewsRow(element, null,
				new RepositoryEntryMyCourseImpl(entry3, null, false, 0, 0), false);
		row3.setParent(parent);
		CurriculumElementWithViewsRow row4 = new CurriculumElementWithViewsRow(element, null,
				new RepositoryEntryMyCourseImpl(entry4, null, false, 0, 0), false);
		row4.setParent(parent);

		List<CurriculumElementWithViewsRow> rows = new ArrayList<>();
		rows.add(parent);
		rows.add(row1);
		rows.add(row2);
		rows.add(row3);
		rows.add(row4);
		
		Collections.sort(rows, new CurriculumElementViewsRowComparator(Locale.ENGLISH));

		Assert.assertEquals(entry3.getKey(), rows.get(1).getRepositoryEntryKey());
		Assert.assertEquals(entry4.getKey(), rows.get(2).getRepositoryEntryKey());
		Assert.assertEquals(entry1.getKey(), rows.get(3).getRepositoryEntryKey());
		Assert.assertEquals(entry2.getKey(), rows.get(4).getRepositoryEntryKey());
	}
	
	/**
	 * Simulate a list of repository entries under their own curriculum element.
	 * 
	 */
	@Test
	public void testActiveInactiveClosedOrNot() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("sort-cur-el");

		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-el-1", "Curriculum for element", "Curriculum", false, null);
		CurriculumElement element1 = curriculumElementDao.createCurriculumElement("Element-1", "1. Element",
				CurriculumElementStatus.inactive, new Date(), new Date(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element2 = curriculumElementDao.createCurriculumElement("Element-2", "2. Element",
				CurriculumElementStatus.inactive, new Date(), new Date(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element3 = curriculumElementDao.createCurriculumElement("Element-3", "3. Element",
				CurriculumElementStatus.active, new Date(), new Date(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.enabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element4 = curriculumElementDao.createCurriculumElement("Element-4", "4. Element",
				CurriculumElementStatus.active, new Date(), new Date(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.enabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commitAndCloseSession();

		RepositoryEntry entry1 = JunitTestHelper.deployBasicCourse(author, "1 course", RepositoryEntryStatusEnum.closed, false, false);
		RepositoryEntry entry2 = JunitTestHelper.deployBasicCourse(author, "2 course", RepositoryEntryStatusEnum.published, false, false);
		RepositoryEntry entry3 = JunitTestHelper.deployBasicCourse(author, "3 course", RepositoryEntryStatusEnum.closed, false, false);
		RepositoryEntry entry4 = JunitTestHelper.deployBasicCourse(author, "4 course", RepositoryEntryStatusEnum.published, false, false);
		// add the course and a participant to the curriculum
		curriculumService.addRepositoryEntry(element1, entry1, false);
		curriculumService.addRepositoryEntry(element2, entry2, false);
		curriculumService.addRepositoryEntry(element3, entry3, false);
		curriculumService.addRepositoryEntry(element4, entry4, false);
		dbInstance.commitAndCloseSession();

		CurriculumElementWithViewsRow row1 = new CurriculumElementWithViewsRow(element1, null,
				new RepositoryEntryMyCourseImpl(entry1, null, false, 0, 0), true);
		CurriculumElementWithViewsRow row2 = new CurriculumElementWithViewsRow(element2, null,
				new RepositoryEntryMyCourseImpl(entry2, null, false, 0, 0), true);
		CurriculumElementWithViewsRow row3 = new CurriculumElementWithViewsRow(element3, null,
				new RepositoryEntryMyCourseImpl(entry3, null, false, 0, 0), true);
		CurriculumElementWithViewsRow row4 = new CurriculumElementWithViewsRow(element4, null,
				new RepositoryEntryMyCourseImpl(entry4, null, false, 0, 0), true);

		List<CurriculumElementWithViewsRow> rows = new ArrayList<>();
		rows.add(row1);
		rows.add(row2);
		rows.add(row3);
		rows.add(row4);
		
		Collections.sort(rows, new CurriculumElementViewsRowComparator(Locale.ENGLISH));

		Assert.assertEquals(entry4.getKey(), rows.get(0).getRepositoryEntryKey());
		Assert.assertEquals(entry1.getKey(), rows.get(1).getRepositoryEntryKey());
		Assert.assertEquals(entry2.getKey(), rows.get(2).getRepositoryEntryKey());
		Assert.assertEquals(entry3.getKey(), rows.get(3).getRepositoryEntryKey());
	}
}

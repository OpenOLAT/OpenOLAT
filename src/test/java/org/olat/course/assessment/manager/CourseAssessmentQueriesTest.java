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
package org.olat.course.assessment.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.manager.RepositoryEntryLifecycleDAO;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 May 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CourseAssessmentQueriesTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryEntryLifecycleDAO lifecycleDao;
	
	@Autowired
	private CourseAssessmentQueries sut;

	@Test
	public void shouldLoadLpCoursesLifecycle() {
		Identity author = JunitTestHelper.createAndPersistRndAuthor(random()).getIdentity();
		RepositoryEntry courseEntryNoLifecycle = createLpCourseEntryLifecycleOver(author, RepositoryEntryStatusEnum.published);
		courseEntryNoLifecycle = repositoryManager.setDescriptionAndName(courseEntryNoLifecycle, null, null, null, null, null, null, null, null, null);
		RepositoryEntry courseEntryLifecycleNotOver = createLpCourseEntryLifecycleOver(author, RepositoryEntryStatusEnum.published);
		RepositoryEntryLifecycle cycleNotOver = courseEntryLifecycleNotOver.getLifecycle();
		cycleNotOver.setValidTo(DateUtils.addDays(new Date(), 20));
		courseEntryNoLifecycle = repositoryManager.setDescriptionAndName(courseEntryNoLifecycle, null, null, null, null, null, null, null, null, cycleNotOver);
		RepositoryEntry courseEntryNoLp = createLpCourseEntryLifecycleOver(author, RepositoryEntryStatusEnum.published);
		repositoryManager.setTechnicalType(courseEntryNoLp, random());
		RepositoryEntry courseEntryPreperation = createLpCourseEntryLifecycleOver(author, RepositoryEntryStatusEnum.preparation);
		RepositoryEntry courseEntryReview = createLpCourseEntryLifecycleOver(author, RepositoryEntryStatusEnum.review);
		RepositoryEntry courseEntryCoachPublished = createLpCourseEntryLifecycleOver(author, RepositoryEntryStatusEnum.coachpublished);
		RepositoryEntry courseEntryPublished = createLpCourseEntryLifecycleOver(author, RepositoryEntryStatusEnum.published);
		RepositoryEntry courseEntryClosed = createLpCourseEntryLifecycleOver(author, RepositoryEntryStatusEnum.closed);
		RepositoryEntry courseEntryTrash = createLpCourseEntryLifecycleOver(author, RepositoryEntryStatusEnum.trash);
		RepositoryEntry courseEntryDeleted = createLpCourseEntryLifecycleOver(author, RepositoryEntryStatusEnum.deleted);
		dbInstance.commitAndCloseSession();
		
		List<RepositoryEntry> courseEntries = sut.loadLpCoursesLifecycle(new Date());
		
		assertThat(courseEntries)
				.contains(
						courseEntryPreperation,
						courseEntryReview,
						courseEntryCoachPublished,
						courseEntryPublished,
						courseEntryClosed
				).doesNotContain(
						courseEntryNoLifecycle,
						courseEntryLifecycleNotOver,
						courseEntryNoLp,
						courseEntryTrash,
						courseEntryDeleted
				);
	}

	private RepositoryEntry createLpCourseEntryLifecycleOver(Identity identity, RepositoryEntryStatusEnum status) {
		RepositoryEntry repositoryEntry = JunitTestHelper.deployBasicCourse(identity);
		repositoryManager.setTechnicalType(repositoryEntry, LearningPathNodeAccessProvider.TYPE);
		repositoryManager.setStatus(repositoryEntry, status);
		String softKey = "lf_" + repositoryEntry.getSoftkey();
		RepositoryEntryLifecycle lifecycle = lifecycleDao.create(repositoryEntry.getDisplayname(), softKey, true,
				DateUtils.addDays(new Date(), -100), DateUtils.addDays(new Date(), -10));
		repositoryEntry = repositoryManager.setDescriptionAndName(repositoryEntry, null, null, null, null, null, null, null, null, lifecycle);
		return repositoryEntry;
	}

}

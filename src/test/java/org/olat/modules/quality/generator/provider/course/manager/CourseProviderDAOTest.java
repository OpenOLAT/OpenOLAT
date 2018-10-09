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
package org.olat.modules.quality.generator.provider.course.manager;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.course.CourseModule;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityService;
import org.olat.modules.quality.generator.QualityGenerator;
import org.olat.modules.quality.generator.QualityGeneratorService;
import org.olat.modules.webFeed.manager.FeedManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryService;
import org.olat.repository.manager.RepositoryEntryLifecycleDAO;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 09.10.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CourseProviderDAOTest extends OlatTestCase {
	
	private Date hour = new Date();
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private QualityService qualityService;
	@Autowired
	private QualityGeneratorService generatorService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryEntryLifecycleDAO lifecycleDao;
	@Autowired
	private OrganisationService organisationService;
	
	@Autowired
	private CourseProviderDAO sut;
	
	@Test
	public void shouldFilterByCourse() {
		OLATResourceable ores = FeedManager.getInstance().createBlogResource();
		OLATResource blogOres = OLATResourceManager.getInstance().findOrPersistResourceable(ores);
		RepositoryEntry blog = createEntry(blogOres, null, null);
		RepositoryEntry course1 = createEntry(null, null, null);
		RepositoryEntry course2 = createEntry(null, null, null);
		dbInstance.commitAndCloseSession();
		
		SearchParameters seachParameters = new SearchParameters();
		List<RepositoryEntry> courses = sut.loadCourses(seachParameters);

		assertThat(courses)
				.contains(course1, course2)
				.doesNotContain(blog);
	}

	@Test
	public void shouldFilterByPublishedCourses() {
		RepositoryEntry preparation = createEntry(null, RepositoryEntryStatusEnum.preparation, null);
		RepositoryEntry coachpublished = createEntry(null, RepositoryEntryStatusEnum.coachpublished, null);
		RepositoryEntry published = createEntry(null, RepositoryEntryStatusEnum.published, null);
		RepositoryEntry review = createEntry(null, RepositoryEntryStatusEnum.review, null);
		RepositoryEntry trash = createEntry(null, RepositoryEntryStatusEnum.trash, null);
		RepositoryEntry deleted = createEntry(null, RepositoryEntryStatusEnum.deleted, null);
		dbInstance.commitAndCloseSession();
		
		SearchParameters seachParameters = new SearchParameters();
		List<RepositoryEntry> courses = sut.loadCourses(seachParameters);

		assertThat(courses)
				.contains(published)
				.doesNotContain(preparation, coachpublished, review, trash, deleted);
	}
	
	@Test
	public void shouldFilterByNotAlreadyGenerated() {
		Organisation organisation = organisationService.createOrganisation("", "o1", null, null, null);
		RepositoryEntry courseWithoutDataCollection = createEntry(null, null, organisation);
		RepositoryEntry courseWithDataCollection = createEntry(null, null, organisation);
		QualityGenerator generator = generatorService.createGenerator("", singletonList(organisation));
		createDataCollection(organisation, courseWithDataCollection, generator);
		RepositoryEntry courseWithOtherDataCollection = createEntry(null, null, organisation);
		QualityGenerator generatorOther = generatorService.createGenerator("", singletonList(organisation));
		createDataCollection(organisation, courseWithOtherDataCollection, generatorOther);
		dbInstance.commitAndCloseSession();
		
		SearchParameters seachParameters = new SearchParameters();
		seachParameters.setGeneratorRef(generator);
		List<RepositoryEntry> courses = sut.loadCourses(seachParameters);

		assertThat(courses)
				.contains(courseWithoutDataCollection, courseWithOtherDataCollection)
				.doesNotContain(courseWithDataCollection);
	}
	
	@Test
	public void shouldFilterByOrganisation() {
		Organisation organisation = organisationService.createOrganisation("", "o1", null, null, null);
		Organisation organisationOther = organisationService.createOrganisation("", "o2", null, null, null);
		RepositoryEntry course1 = createEntry(null, null, organisation);
		RepositoryEntry course2 = createEntry(null, null, organisation);
		RepositoryEntry other = createEntry(null, null, organisationOther);
		dbInstance.commitAndCloseSession();
		
		SearchParameters seachParameters = new SearchParameters();
		seachParameters.setOrganisationRefs(asList(organisation));
		List<RepositoryEntry> courses = sut.loadCourses(seachParameters);

		assertThat(courses)
				.containsExactlyInAnyOrder(course1, course2)
				.doesNotContain(other);
	}
	
	@Test
	public void shouldFilterByBeginFrom() {
		RepositoryEntry courseWithoutBegin = createEntry(null, null, null);
		RepositoryEntry courseBeginToEarly = createEntry(null, null, null);
		Date before = nextHour();
		setLifecycle(courseBeginToEarly, before, null);
		Date beginFrom = nextHour();
		RepositoryEntry course = createEntry(null, null, null);
		Date after = nextHour();
		setLifecycle(course, after, null);
		dbInstance.commitAndCloseSession();
		
		SearchParameters seachParameters = new SearchParameters();
		seachParameters.setBeginFrom(beginFrom);
		List<RepositoryEntry> courses = sut.loadCourses(seachParameters);

		assertThat(courses)
				.contains(course)
				.doesNotContain(courseWithoutBegin, courseBeginToEarly);
	}
	
	@Test
	public void shouldFilterByBeginTo() {
		RepositoryEntry courseWithoutBegin = createEntry(null, null, null);
		RepositoryEntry course = createEntry(null, null, null);
		Date before = nextHour();
		setLifecycle(course, before, null);
		Date beginTo = nextHour();
		RepositoryEntry courseBeginToLate = createEntry(null, null, null);
		Date after = nextHour();
		setLifecycle(courseBeginToLate, after, null);
		dbInstance.commitAndCloseSession();
		
		SearchParameters seachParameters = new SearchParameters();
		seachParameters.setBeginTo(beginTo);
		List<RepositoryEntry> courses = sut.loadCourses(seachParameters);

		assertThat(courses)
				.contains(course)
				.doesNotContain(courseWithoutBegin, courseBeginToLate);
	}
	
	@Test
	public void shouldFilterByEndFrom() {
		RepositoryEntry courseWithoutEnd = createEntry(null, null, null);
		RepositoryEntry courseEndToEarly = createEntry(null, null, null);
		Date before = nextHour();
		setLifecycle(courseEndToEarly, null, before);
		Date endFrom = nextHour();
		RepositoryEntry course = createEntry(null, null, null);
		Date after = nextHour();
		setLifecycle(course, null, after);
		dbInstance.commitAndCloseSession();
		
		SearchParameters seachParameters = new SearchParameters();
		seachParameters.setEndFrom(endFrom);
		List<RepositoryEntry> courses = sut.loadCourses(seachParameters);

		assertThat(courses)
				.contains(course)
				.doesNotContain(courseWithoutEnd, courseEndToEarly);
	}
	
	@Test
	public void shouldFilterByEndTo() {
		RepositoryEntry courseWithoutEnd = createEntry(null, null, null);
		RepositoryEntry course = createEntry(null, null, null);
		Date before = nextHour();
		setLifecycle(course, null, before);
		Date endTo = nextHour();
		RepositoryEntry courseEndToLate = createEntry(null, null, null);
		Date after = nextHour();
		setLifecycle(courseEndToLate, null, after);
		dbInstance.commitAndCloseSession();
		
		SearchParameters seachParameters = new SearchParameters();
		seachParameters.setEndTo(endTo);
		List<RepositoryEntry> courses = sut.loadCourses(seachParameters);

		assertThat(courses)
				.contains(course)
				.doesNotContain(courseWithoutEnd, courseEndToLate);
	}
	
	@Test
	public void shouldFilterByRepositoryEntryRefs() {
		RepositoryEntry course1 = createEntry(null, null, null);
		RepositoryEntry course2 = createEntry(null, null, null);
		RepositoryEntry other = createEntry(null, null, null);
		dbInstance.commitAndCloseSession();
		
		SearchParameters seachParameters = new SearchParameters();
		seachParameters.setRepositoryEntryRefs(asList(course1, course2));
		List<RepositoryEntry> courses = sut.loadCourses(seachParameters);

		assertThat(courses)
				.contains(course1, course2)
				.doesNotContain(other);
	}

	
	private RepositoryEntry createEntry(OLATResource resource, RepositoryEntryStatusEnum status, Organisation organisation) {
		String initialAuthorAlt = UUID.randomUUID().toString();
		String displayname = UUID.randomUUID().toString();
		resource = resource != null? resource: OLATResourceManager.getInstance().createOLATResourceInstance(CourseModule.class);
		status = status != null? status: RepositoryEntryStatusEnum.published;
		organisation = organisation != null? organisation: organisationService.getDefaultOrganisation();
		return repositoryService.create(null, initialAuthorAlt , null, displayname, null, resource, status, organisation);
	}
	
	private RepositoryEntry setLifecycle(RepositoryEntry entry, Date begin, Date end) {
		String softKey = "lf_" + entry.getSoftkey();
		RepositoryEntryLifecycle cycle = lifecycleDao.create(entry.getDisplayname(), softKey, true, begin, end);
		entry.setLifecycle(cycle);
		dbInstance.getCurrentEntityManager().merge(entry);
		return entry;
	}

	private void createDataCollection(Organisation organisation, RepositoryEntry entry, QualityGenerator generator) {
		RepositoryEntry formEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		QualityDataCollection runningDataCollection = qualityService.createDataCollection(singletonList(organisation), formEntry, generator, entry.getKey());
		qualityService.updateDataCollection(runningDataCollection);
	}
	
	private Date nextHour() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(hour);
		calendar.add(Calendar.HOUR, 1);
		hour = calendar.getTime();
		return hour;
	}
	
}

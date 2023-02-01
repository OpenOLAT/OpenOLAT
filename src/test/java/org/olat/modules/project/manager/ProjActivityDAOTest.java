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
package org.olat.modules.project.manager;

import static org.apache.commons.lang3.time.DateUtils.addDays;
import static org.apache.commons.lang3.time.DateUtils.addHours;
import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.modules.project.ProjActivity;
import org.olat.modules.project.ProjActivity.Action;
import org.olat.modules.project.ProjActivity.ActionTarget;
import org.olat.modules.project.ProjActivitySearchParams;
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.ProjDateRange;
import org.olat.modules.project.ProjFile;
import org.olat.modules.project.ProjProject;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 16 Jan 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjActivityDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private ProjProjectDAO projectDao;
	@Autowired
	private ProjArtefactDAO artefactDao;
	@Autowired
	private GroupDAO groupDao;
	@Autowired
	private OrganisationService organisationService;
	
	@Autowired
	private ProjActivityDAO sut;

	@Test
	public void shouldCreate() {
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Identity member = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ProjProject project = createProject(creator);
		ProjArtefact artefact1 = createArtefact(project, creator);
		ProjArtefact artefact2 = createArtefact(project, creator);
		Organisation organisation = organisationService.createOrganisation(random(), random(), random(), null, null);
		dbInstance.commitAndCloseSession();
		
		Action action = Action.noteCreate;
		ActionTarget actionTarget = ActionTarget.note;
		String before = random();
		String after = random();
		String tempIdentifier = random();
		ProjActivity activity = sut.create(action, before, after, tempIdentifier, creator, project, artefact1, artefact2, member, organisation);
		dbInstance.commitAndCloseSession();
		
		assertThat(activity.getKey()).isNotNull();
		assertThat(activity.getCreationDate()).isNotNull();
		assertThat(activity.getAction()).isEqualTo(action);
		assertThat(activity.getActionTarget()).isEqualTo(actionTarget);
		assertThat(activity.getBefore()).isEqualTo(before);
		assertThat(activity.getAfter()).isEqualTo(after);
		assertThat(activity.getTempIdentifier()).isEqualTo(tempIdentifier);
		assertThat(activity.getDoer()).isEqualTo(creator);
		assertThat(activity.getProject()).isEqualTo(project);
		assertThat(activity.getArtefact()).isEqualTo(artefact1);
		assertThat(activity.getArtefactReference()).isEqualTo(artefact2);
		assertThat(activity.getMember()).isEqualTo(member);
		assertThat(activity.getOrganisation()).isEqualTo(organisation);
	}
	
	@Test
	public void shouldDelete() {
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ProjProject project = createProject(creator);
		ProjActivity activity1 = sut.create(Action.noteCreate, null, null, creator, project);
		ProjActivity activity2 = sut.create(Action.noteCreate, null, null, creator, project);
		ProjActivity activity3 = sut.create(Action.noteStatusDelete, null, null, creator, project);
		dbInstance.commitAndCloseSession();
		
		sut.delete(List.of(activity1, activity2));
		dbInstance.commitAndCloseSession();
		
		ProjActivitySearchParams searchParams = new ProjActivitySearchParams();
		searchParams.setProject(project);
		List<ProjActivity> activities = sut.loadActivities(searchParams, 0, -1);
		assertThat(activities).containsExactlyInAnyOrder(activity3);
	}
	
	@Test
	public void shouldLoadByTempIdentifier() {
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ProjProject project = createProject(creator);
		ProjArtefact artefact = createArtefact(project, creator);
		String tempIdentifier = random();
		ProjActivity activity1 = sut.create(Action.noteCreate, null, null, tempIdentifier, creator, artefact);
		ProjActivity activity2 = sut.create(Action.noteCreate, null, null, tempIdentifier, creator, artefact);
		sut.create(Action.noteContentUpdate, null, null, tempIdentifier, creator, artefact);
		sut.create(Action.projectCreate, null, null, random(), creator, artefact);
		sut.create(Action.projectCreate, null, null, null, creator, artefact);
		dbInstance.commitAndCloseSession();
		
		List<ProjActivity> activities = sut.loadActivities(tempIdentifier, Action.noteCreate);
		
		assertThat(activities).containsExactly(activity1, activity2);
	}
	
	@Test
	public void shouldLoad_filter_actions() {
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ProjProject project = createProject(creator);
		ProjActivity activity1 = sut.create(Action.projectCreate, null, null, creator, project);
		ProjActivity activity2 = sut.create(Action.noteCreate, null, null, creator, project);
		sut.create(Action.noteStatusDelete, null, null, creator, project);
		dbInstance.commitAndCloseSession();
		
		ProjActivitySearchParams searchParams = new ProjActivitySearchParams();
		searchParams.setProject(project);
		searchParams.setActions(List.of(Action.projectCreate, Action.noteCreate));
		List<ProjActivity> activities = sut.loadActivities(searchParams, 0, -1);
		
		assertThat(activities).containsExactlyInAnyOrder(activity1, activity2);
	}
	
	@Test
	public void shouldLoad_filter_targets() {
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ProjProject project = createProject(creator);
		ProjActivity activity1 = sut.create(Action.projectCreate, null, null, creator, project);
		ProjActivity activity2 = sut.create(Action.fileCreate, null, null, creator, project);
		sut.create(Action.noteCreate, null, null, creator, project);
		dbInstance.commitAndCloseSession();
		
		ProjActivitySearchParams searchParams = new ProjActivitySearchParams();
		searchParams.setProject(project);
		searchParams.setTargets(List.of(ActionTarget.project, ActionTarget.file));
		List<ProjActivity> activities = sut.loadActivities(searchParams, 0, -1);
		
		assertThat(activities).containsExactlyInAnyOrder(activity1, activity2);
	}
	
	@Test
	public void shouldLoad_filter_doer() {
		Identity doer1 = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Identity doer2 = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ProjProject project1 = createProject(doer1);
		ProjProject project2 = createProject(doer2);
		ProjActivity activity1 = sut.create(Action.projectCreate, null, null, doer1, project1);
		ProjActivity activity2 = sut.create(Action.projectCreate, null, null, doer1, project2);
		sut.create(Action.projectCreate, null, null, doer2, project1);
		dbInstance.commitAndCloseSession();
		
		ProjActivitySearchParams searchParams = new ProjActivitySearchParams();
		searchParams.setDoer(doer1);
		List<ProjActivity> activities = sut.loadActivities(searchParams, 0, -1);
		
		assertThat(activities).containsExactlyInAnyOrder(activity1, activity2);
	}
	
	@Test
	public void shouldLoad_filter_project() {
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ProjProject project1 = createProject(creator);
		ProjProject project2 = createProject(creator);
		ProjActivity activity1 = sut.create(Action.projectCreate, null, null, creator, project1);
		sut.create(Action.projectCreate, null, null, creator, project2);
		dbInstance.commitAndCloseSession();
		
		ProjActivitySearchParams searchParams = new ProjActivitySearchParams();
		searchParams.setProject(project1);
		List<ProjActivity> activities = sut.loadActivities(searchParams, 0, -1);
		
		assertThat(activities).containsExactlyInAnyOrder(activity1);
	}
	
	@Test
	public void shouldLoad_filter_artefacts() {
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ProjProject project = createProject(creator);
		ProjArtefact artefact1 = createArtefact(project, creator);
		ProjArtefact artefact2 = createArtefact(project, creator);
		ProjActivity activity11 = sut.create(Action.noteCreate, null, null, null, creator, artefact1);
		ProjActivity activity12 = sut.create(Action.noteCreate, null, null, null, creator, artefact1);
		ProjActivity activity21 = sut.create(Action.noteCreate, null, null, null, creator, artefact2);
		sut.create(Action.noteCreate, null, null, null, creator, createArtefact(project, creator));
		dbInstance.commitAndCloseSession();
		
		ProjActivitySearchParams searchParams = new ProjActivitySearchParams();
		searchParams.setArtefacts(List.of(artefact1, artefact2));
		List<ProjActivity> activities = sut.loadActivities(searchParams, 0, -1);
		
		assertThat(activities).containsExactlyInAnyOrder(activity11, activity12, activity21);
	}
	
	@Test
	public void shouldLoad_filter_created_range() {
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ProjProject project = createProject(doer);
		Date dueDate = new Date();
		createActivity(doer, project, DateUtils.addDays(dueDate, 1));
		ProjActivity activity2 = createActivity(doer, project, DateUtils.addDays(dueDate, 2));
		ProjActivity activity3 = createActivity(doer, project, DateUtils.addDays(dueDate, 3));
		createActivity(doer, project, DateUtils.addDays(dueDate, 4));
		createActivity(doer, project, DateUtils.addDays(dueDate, 5));
		ProjActivity activity6 = createActivity(doer, project, DateUtils.addDays(dueDate, 6));
		ProjActivity activity7 = createActivity(doer, project, DateUtils.addDays(dueDate, 7));
		ProjActivity activity8 = createActivity(doer, project, DateUtils.addDays(dueDate, 8));
		createActivity(doer, project, DateUtils.addDays(dueDate, 9));
		dbInstance.commitAndCloseSession();
		
		ProjActivitySearchParams searchParams = new ProjActivitySearchParams();
		searchParams.setProject(project);
		searchParams.setCreatedDateRanges(List.of(
				new ProjDateRange(addHours(addDays(dueDate, 2), -1), addHours(addDays(dueDate, 3), 1)),
				new ProjDateRange(addHours(addDays(dueDate, 6), -1), addHours(addDays(dueDate, 8), 1))));
		List<ProjActivity> activities = sut.loadActivities(searchParams, 0, -1);
		
		assertThat(activities).containsExactlyInAnyOrder(activity2, activity3, activity6, activity7, activity8);
	}
	
	@Test
	public void shouldLoadProkectKeyToLastActivity() {
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ProjProject project1 = createProject(doer);
		ProjProject project2 = createProject(doer);
		Date dueDate = new Date();
		createActivity(doer, project1, DateUtils.addDays(dueDate, 1));
		createActivity(doer, project1, DateUtils.addDays(dueDate, 2));
		ProjActivity activity3 = createActivity(doer, project1, DateUtils.addDays(dueDate, 3));
		createActivity(doer, project2, DateUtils.addDays(dueDate, 4));
		createActivity(doer, project2, DateUtils.addDays(dueDate, 5));
		ProjActivity activity6 = createActivity(doer, project2, DateUtils.addDays(dueDate, 6));
		
		ProjActivitySearchParams searchParams = new ProjActivitySearchParams();
		searchParams.setProjects(List.of(project1, project2));
		Map<Long, ProjActivity> projectKeyToLastActivity = sut.loadProjectKeyToLastActivity(searchParams);
		assertThat(projectKeyToLastActivity).hasSize(2);
		assertThat(projectKeyToLastActivity.get(project1.getKey())).isEqualTo(activity3);
		assertThat(projectKeyToLastActivity.get(project2.getKey())).isEqualTo(activity6);
	}


	private ProjActivity createActivity(Identity doer, ProjProject project, Date creationDate) {
		return sut.create(Action.projectCreate, null, null, null, doer, project, null, null, null, null, creationDate);
	}
	
	private ProjProject createProject(Identity creator) {
		Group baseGroup = groupDao.createGroup();
		return projectDao.create(creator, baseGroup);
	}
	
	private ProjArtefact createArtefact(ProjProject project, Identity createdBy) {
		return artefactDao.create(ProjFile.TYPE, project, createdBy);
	}

}

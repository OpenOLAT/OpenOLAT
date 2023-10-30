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

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.ProjMilestone;
import org.olat.modules.project.ProjMilestoneSearchParams;
import org.olat.modules.project.ProjMilestoneStatus;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ProjectStatus;
import org.olat.modules.project.ui.ProjectBCFactory;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 9 Mar 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjMilestoneDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private ProjArtefactDAO artefactDao;
	@Autowired
	private ProjectService projectService;
	
	@Autowired
	private ProjMilestoneDAO sut;
	
	@Test
	public void shouldCreateMilestone() {
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ProjProject project = projectService.createProject(creator, new ProjectBCFactory(), creator);
		ProjArtefact artefact = artefactDao.create(ProjMilestone.TYPE, project, creator);
		dbInstance.commitAndCloseSession();
		
		Date dueDate = DateUtils.addDays(new Date(), 2);
		ProjMilestone milestone = sut.create(artefact, dueDate);
		dbInstance.commitAndCloseSession();
		
		assertThat(milestone).isNotNull();
		assertThat(milestone.getCreationDate()).isNotNull();
		assertThat(milestone.getLastModified()).isNotNull();
		assertThat(milestone.getIdentifier()).isNotNull();
		assertThat(milestone.getStatus()).isEqualTo(ProjMilestoneStatus.open);
		assertThat(milestone.getDueDate()).isCloseTo(dueDate, 1000);
		assertThat(milestone.getArtefact()).isEqualTo(artefact);
	}
	
	@Test
	public void shouldSaveMilestone() {
		ProjMilestone milestone = createRandomMilestone();
		dbInstance.commitAndCloseSession();
		
		ProjMilestoneStatus status = ProjMilestoneStatus.achieved;
		milestone.setStatus(status);
		Date dueDate  = DateUtils.addDays(new Date(), 1);
		milestone.setDueDate(dueDate);
		String subject = random();
		milestone.setSubject(subject);
		String description = random();
		milestone.setDescription(description);
		String color =  random();
		milestone.setColor(color);
		sut.save(milestone);
		dbInstance.commitAndCloseSession();
		
		ProjMilestoneSearchParams params = new ProjMilestoneSearchParams();
		params.setMilestones(List.of(milestone));
		milestone = sut.loadMilestones(params).get(0);
		
		assertThat(milestone.getStatus()).isEqualTo(status);
		assertThat(milestone.getDueDate()).isCloseTo(dueDate, 1000);
		assertThat(milestone.getSubject()).isEqualTo(subject);
		assertThat(milestone.getDescription()).isEqualTo(description);
		assertThat(milestone.getColor()).isEqualTo(color);
	}
	
	@Test
	public void shouldDelete() {
		ProjMilestone milestone = createRandomMilestone();
		
		sut.delete(milestone);
		dbInstance.commitAndCloseSession();
		
		ProjMilestoneSearchParams searchParams = new ProjMilestoneSearchParams();
		searchParams.setProject(milestone.getArtefact().getProject());
		List<ProjMilestone> milestones = sut.loadMilestones(searchParams);
		assertThat(milestones).isEmpty();
	}
	
	@Test
	public void shouldCount() {
		ProjMilestone milestone1 = createRandomMilestone();
		ProjMilestone milestone2 = createRandomMilestone();
		createRandomMilestone();
		
		ProjMilestoneSearchParams params = new ProjMilestoneSearchParams();
		params.setMilestones(List.of(milestone1, milestone2));
		long count = sut.loadMilestonesCount(params);
		
		assertThat(count).isEqualTo(2);
	}
	
	@Test
	public void shouldLoad_filter_project() {
		ProjMilestone milestone = createRandomMilestone();
		createRandomMilestone();
		
		ProjMilestoneSearchParams params = new ProjMilestoneSearchParams();
		params.setProject(milestone.getArtefact().getProject());
		List<ProjMilestone> milestones = sut.loadMilestones(params);
		
		assertThat(milestones).containsExactlyInAnyOrder(milestone);
	}
	
	@Test
	public void shouldLoad_filter_milestoneKeys() {
		ProjMilestone milestone1 = createRandomMilestone();
		ProjMilestone milestone2 = createRandomMilestone();
		createRandomMilestone();
		
		ProjMilestoneSearchParams params = new ProjMilestoneSearchParams();
		params.setMilestones(List.of(milestone1, milestone2));
		List<ProjMilestone> milestones = sut.loadMilestones(params);
		
		assertThat(milestones).containsExactlyInAnyOrder(milestone1, milestone2);
	}
	
	@Test
	public void shouldLoad_filter_identifiers() {
		ProjMilestone milestone1 = createRandomMilestone();
		ProjMilestone milestone2 = createRandomMilestone();
		createRandomMilestone();
		
		ProjMilestoneSearchParams params = new ProjMilestoneSearchParams();
		params.setIdentifiers(List.of(milestone1.getIdentifier(), milestone2.getIdentifier()));
		List<ProjMilestone> milestones = sut.loadMilestones(params);
		
		assertThat(milestones).containsExactlyInAnyOrder(milestone1, milestone2);
	}
	
	@Test
	public void shouldLoad_filter_artefacts() {
		ProjMilestone milestone1 = createRandomMilestone();
		ProjMilestone milestone2 = createRandomMilestone();
		createRandomMilestone();
		
		ProjMilestoneSearchParams params = new ProjMilestoneSearchParams();
		params.setArtefacts(List.of(milestone1.getArtefact(), milestone2.getArtefact()));
		List<ProjMilestone> milestones = sut.loadMilestones(params);
		
		assertThat(milestones).containsExactlyInAnyOrder(milestone1, milestone2);
	}
	
	@Test
	public void shouldLoad_filter_artefact_status() {
		ProjMilestone milestone1 = createRandomMilestone();
		projectService.deleteMilestoneSoftly(milestone1.getArtefact().getCreator(), milestone1);
		ProjMilestone milestone2 = createRandomMilestone();
		projectService.deleteMilestoneSoftly(milestone2.getArtefact().getCreator(), milestone2);
		ProjMilestone milestone3 = createRandomMilestone();
		
		ProjMilestoneSearchParams params = new ProjMilestoneSearchParams();
		params.setMilestones(List.of(milestone1, milestone2, milestone3));
		params.setStatus(List.of(ProjectStatus.deleted));
		List<ProjMilestone> milestones = sut.loadMilestones(params);
		
		assertThat(milestones).containsExactlyInAnyOrder(milestone1, milestone2);
	}
	
	@Test
	public void shouldLoad_filter_createdAfter() {
		ProjMilestone milestone = createRandomMilestone();
		
		ProjMilestoneSearchParams params = new ProjMilestoneSearchParams();
		params.setMilestones(List.of(milestone));
		params.setCreatedAfter(new Date());
		sut.loadMilestones(params);
		
		// Just syntax check because created date can't be modified.
	}
	
	@Test
	public void shouldLoad_filter_dueDateNull() {
		ProjMilestone milestone1 = createRandomMilestone();
		ProjMilestone milestone2 = createRandomMilestone();
		milestone2.setDueDate(null);
		sut.save(milestone2);
		dbInstance.commitAndCloseSession();
		
		ProjMilestoneSearchParams params = new ProjMilestoneSearchParams();
		params.setMilestones(List.of(milestone1, milestone2));
		List<ProjMilestone> milestones = sut.loadMilestones(params);
		assertThat(milestones).containsExactlyInAnyOrder(milestone1, milestone2);
		
		params.setDueDateNull(Boolean.TRUE);
		milestones = sut.loadMilestones(params);
		assertThat(milestones).containsExactlyInAnyOrder(milestone2);
		
		params.setDueDateNull(Boolean.FALSE);
		milestones = sut.loadMilestones(params);
		assertThat(milestones).containsExactlyInAnyOrder(milestone1);
	}
	
	private ProjMilestone createRandomMilestone() {
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		return createRandomMilestone(creator);
	}

	private ProjMilestone createRandomMilestone(Identity creator) {
		ProjProject project = projectService.createProject(creator, new ProjectBCFactory(), creator);
		ProjArtefact artefact = artefactDao.create(ProjMilestone.TYPE, project, creator);
		ProjMilestone milestone = sut.create(artefact, DateUtils.addDays(new Date(), 2));
		dbInstance.commitAndCloseSession();
		return milestone;
	}

}

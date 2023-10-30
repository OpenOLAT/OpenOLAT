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
import org.olat.modules.project.ProjDecision;
import org.olat.modules.project.ProjDecisionSearchParams;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ProjectStatus;
import org.olat.modules.project.ui.ProjectBCFactory;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 9 Jun 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjDecisionDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private ProjArtefactDAO artefactDao;
	@Autowired
	private ProjectService projectService;
	
	@Autowired
	private ProjDecisionDAO sut;
	
	@Test
	public void shouldCreateDecision() {
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ProjProject project = projectService.createProject(creator, new ProjectBCFactory(), creator);
		ProjArtefact artefact = artefactDao.create(ProjDecision.TYPE, project, creator);
		dbInstance.commitAndCloseSession();
		
		ProjDecision decision = sut.create(artefact);
		dbInstance.commitAndCloseSession();
		
		assertThat(decision).isNotNull();
		assertThat(decision.getCreationDate()).isNotNull();
		assertThat(decision.getLastModified()).isNotNull();
		assertThat(decision.getArtefact()).isEqualTo(artefact);
	}
	
	@Test
	public void shouldSaveDecision() {
		ProjDecision decision = createRandomDecision();
		dbInstance.commitAndCloseSession();
		
		String title = random();
		decision.setTitle(title);
		String details = random();
		decision.setDetails(details);
		Date decisionDate = DateUtils.addDays(new Date(), 2);
		decision.setDecisionDate(decisionDate);
		sut.save(decision);
		dbInstance.commitAndCloseSession();
		
		ProjDecisionSearchParams params = new ProjDecisionSearchParams();
		params.setDecisions(List.of(decision));
		decision = sut.loadDecisions(params).get(0);
		
		assertThat(decision.getTitle()).isEqualTo(title);
		assertThat(decision.getDetails()).isEqualTo(details);
		assertThat(decision.getDecisionDate()).isCloseTo(decisionDate, 1000);
	}
	
	@Test
	public void shouldDelete() {
		ProjDecision decision = createRandomDecision();
		
		sut.delete(decision);
		dbInstance.commitAndCloseSession();
		
		ProjDecisionSearchParams searchParams = new ProjDecisionSearchParams();
		searchParams.setProject(decision.getArtefact().getProject());
		List<ProjDecision> decisions = sut.loadDecisions(searchParams);
		assertThat(decisions).isEmpty();
	}
	
	@Test
	public void shouldCount() {
		ProjDecision decision1 = createRandomDecision();
		ProjDecision decision2 = createRandomDecision();
		createRandomDecision();
		
		ProjDecisionSearchParams params = new ProjDecisionSearchParams();
		params.setDecisions(List.of(decision1, decision2));
		long count = sut.loadDecisionsCount(params);
		
		assertThat(count).isEqualTo(2);
	}
	
	@Test
	public void shouldLoad_filter_project() {
		ProjDecision decision = createRandomDecision();
		createRandomDecision();
		
		ProjDecisionSearchParams params = new ProjDecisionSearchParams();
		params.setProject(decision.getArtefact().getProject());
		List<ProjDecision> decisions = sut.loadDecisions(params);
		
		assertThat(decisions).containsExactlyInAnyOrder(decision);
	}
	
	@Test
	public void shouldLoad_filter_decisionKeys() {
		ProjDecision decision1 = createRandomDecision();
		ProjDecision decision2 = createRandomDecision();
		createRandomDecision();
		
		ProjDecisionSearchParams params = new ProjDecisionSearchParams();
		params.setDecisions(List.of(decision1, decision2));
		List<ProjDecision> decisions = sut.loadDecisions(params);
		
		assertThat(decisions).containsExactlyInAnyOrder(decision1, decision2);
	}
	
	@Test
	public void shouldLoad_filter_artefacts() {
		ProjDecision decision1 = createRandomDecision();
		ProjDecision decision2 = createRandomDecision();
		createRandomDecision();
		
		ProjDecisionSearchParams params = new ProjDecisionSearchParams();
		params.setArtefacts(List.of(decision1.getArtefact(), decision2.getArtefact()));
		List<ProjDecision> decisions = sut.loadDecisions(params);
		
		assertThat(decisions).containsExactlyInAnyOrder(decision1, decision2);
	}
	
	@Test
	public void shouldLoad_filter_artefact_status() {
		ProjDecision decision1 = createRandomDecision();
		projectService.deleteDecisionSoftly(decision1.getArtefact().getCreator(), decision1);
		ProjDecision decision2 = createRandomDecision();
		projectService.deleteDecisionSoftly(decision2.getArtefact().getCreator(), decision2);
		ProjDecision decision3 = createRandomDecision();
		
		ProjDecisionSearchParams params = new ProjDecisionSearchParams();
		params.setDecisions(List.of(decision1, decision2, decision3));
		params.setStatus(List.of(ProjectStatus.deleted));
		List<ProjDecision> decisions = sut.loadDecisions(params);
		
		assertThat(decisions).containsExactlyInAnyOrder(decision1, decision2);
	}
	
	@Test
	public void shouldLoad_filter_createdAfter() {
		ProjDecision decision1 = createRandomDecision();
		
		ProjDecisionSearchParams params = new ProjDecisionSearchParams();
		params.setDecisions(List.of(decision1));
		params.setCreatedAfter(new Date());
		sut.loadDecisions(params);
		
		// Just syntax check because created date can't be modified.
	}
	
	@Test
	public void shouldLoad_numLastModified() {
		Date now = new Date();
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ProjDecision decision1 = createRandomDecision(creator);
		ProjArtefact artefact1 = decision1.getArtefact();
		artefact1.setContentModifiedDate(DateUtils.addDays(now, 3));
		artefactDao.save(artefact1);
		ProjDecision decision2 = createRandomDecision(creator);
		ProjArtefact artefact2 = decision2.getArtefact();
		artefact2.setContentModifiedDate(DateUtils.addDays(now, 2));
		artefactDao.save(artefact2);
		ProjDecision decision3 = createRandomDecision(creator);
		ProjArtefact artefact3 = decision3.getArtefact();
		artefact3.setContentModifiedDate(DateUtils.addDays(now, 4));
		artefactDao.save(artefact3);
		ProjDecision decision4 = createRandomDecision(creator);
		ProjArtefact artefact4 = decision4.getArtefact();
		artefact4.setContentModifiedDate(DateUtils.addDays(now, 1));
		artefactDao.save(artefact4);
		dbInstance.commitAndCloseSession();
		
		ProjDecisionSearchParams params = new ProjDecisionSearchParams();
		params.setArtefacts(List.of(artefact1, artefact2, artefact3, artefact4));
		params.setNumLastModified(3);
		List<ProjDecision> decisions = sut.loadDecisions(params);
		
		assertThat(decisions).containsExactly(decision3, decision1, decision2);
	}
	
	private ProjDecision createRandomDecision() {
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		return createRandomDecision(creator);
	}

	private ProjDecision createRandomDecision(Identity creator) {
		ProjProject project = projectService.createProject(creator, new ProjectBCFactory(), creator);
		ProjArtefact artefact = artefactDao.create(ProjDecision.TYPE, project, creator);
		ProjDecision decision = sut.create(artefact);
		dbInstance.commitAndCloseSession();
		return decision;
	}

}

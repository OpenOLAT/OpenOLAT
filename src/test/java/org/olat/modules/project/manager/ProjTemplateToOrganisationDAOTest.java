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

import java.util.List;

import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjTemplateToOrganisation;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.model.ProjTemplateToOrganisationImpl;
import org.olat.modules.project.ui.ProjectBCFactory;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12 Jun 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjTemplateToOrganisationDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private OrganisationService organisationService;
	
	@Autowired
	private ProjTemplateToOrganisationDAO sut;
	
	@Test
	public void shouldCreateRelation() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ProjProject project = projectService.createProject(identity, new ProjectBCFactory(), identity);
		Organisation organisation = organisationService.createOrganisation(random(), null, null, null,null);
		dbInstance.commitAndCloseSession();
		
		ProjTemplateToOrganisation templateToOrganisation = sut.createRelation(project, organisation);
		dbInstance.commitAndCloseSession();
		
		assertThat(((ProjTemplateToOrganisationImpl)templateToOrganisation).getCreationDate()).isNotNull();
		assertThat(templateToOrganisation.getProject()).isEqualTo(project);
		assertThat(templateToOrganisation.getOrganisation()).isEqualTo(organisation);
	}
	
	@Test
	public void shouldLoadRelationsByProjProjectOrgOrganistion() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ProjProject project1 = projectService.createProject(identity, new ProjectBCFactory(), identity);
		ProjProject project2 = projectService.createProject(identity, new ProjectBCFactory(), identity);
		Organisation organisation1 = organisationService.createOrganisation(random(), null, null, null,null);
		Organisation organisation2 = organisationService.createOrganisation(random(), null, null, null,null);
		ProjTemplateToOrganisation templateToOrganisation11 = sut.createRelation(project1, organisation1);
		ProjTemplateToOrganisation templateToOrganisation12 = sut.createRelation(project1, organisation2);
		sut.createRelation(project2, organisation2);
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.loadRelations(project1, null)).hasSize(2).containsExactlyInAnyOrder(templateToOrganisation11, templateToOrganisation12);
		assertThat(sut.loadRelations(null, organisation1)).hasSize(1).containsExactlyInAnyOrder(templateToOrganisation11);
		assertThat(sut.loadRelations(project1, organisation2)).hasSize(1).containsExactlyInAnyOrder(templateToOrganisation12);
	}
	
	@Test
	public void shouldLoadRelationsByProjProjects() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ProjProject project1 = projectService.createProject(identity, new ProjectBCFactory(), identity);
		ProjProject project2 = projectService.createProject(identity, new ProjectBCFactory(), identity);
		ProjProject project3 = projectService.createProject(identity, new ProjectBCFactory(), identity);
		Organisation organisation1 = organisationService.createOrganisation(random(), null, null, null,null);
		Organisation organisation2 = organisationService.createOrganisation(random(), null, null, null,null);
		ProjTemplateToOrganisation templateToOrganisation11 = sut.createRelation(project1, organisation1);
		ProjTemplateToOrganisation templateToOrganisation12 = sut.createRelation(project1, organisation2);
		ProjTemplateToOrganisation templateToOrganisation21 = sut.createRelation(project2, organisation1);
		sut.createRelation(project3, organisation2);
		dbInstance.commitAndCloseSession();
		
		List<ProjTemplateToOrganisation> relations = sut.loadRelations(List.of(project1,  project2));
		
		assertThat(relations).containsExactlyInAnyOrder(
				templateToOrganisation11,
				templateToOrganisation12,
				templateToOrganisation21
				);
	}
	
	@Test
	public void shouldLoadOrganisationsByProjProject() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ProjProject project1 = projectService.createProject(identity, new ProjectBCFactory(), identity);
		ProjProject project2 = projectService.createProject(identity, new ProjectBCFactory(), identity);
		Organisation organisation1 = organisationService.createOrganisation(random(), null, null, null,null);
		Organisation organisation2 = organisationService.createOrganisation(random(), null, null, null,null);
		sut.createRelation(project1, organisation1);
		sut.createRelation(project1, organisation2);
		sut.createRelation(project2, organisation2);
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.loadOrganisations(project1)).containsExactlyInAnyOrder(organisation1, organisation2);
		assertThat(sut.loadOrganisations(project2)).containsExactlyInAnyOrder(organisation2);
	}
	
	@Test
	public void shouldDeleteReleation() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ProjProject project1 = projectService.createProject(identity, new ProjectBCFactory(), identity);
		ProjProject project2 = projectService.createProject(identity, new ProjectBCFactory(), identity);
		Organisation organisation1 = organisationService.createOrganisation(random(), null, null, null,null);
		Organisation organisation2 = organisationService.createOrganisation(random(), null, null, null,null);
		ProjTemplateToOrganisation templateToOrganisation11 = sut.createRelation(project1, organisation1);
		ProjTemplateToOrganisation templateToOrganisation12 = sut.createRelation(project1, organisation2);
		ProjTemplateToOrganisation templateToOrganisation21 = sut.createRelation(project2, organisation2);
		dbInstance.commitAndCloseSession();
		
		sut.delete(templateToOrganisation12);
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.loadRelations(project1, null)).containsExactlyInAnyOrder(templateToOrganisation11);
		assertThat(sut.loadRelations(project2, null)).containsExactlyInAnyOrder(templateToOrganisation21);
	}
	
	@Test
	public void shouldDeleteByProjProject() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ProjProject project1 = projectService.createProject(identity, new ProjectBCFactory(), identity);
		ProjProject project2 = projectService.createProject(identity, new ProjectBCFactory(), identity);
		Organisation organisation1 = organisationService.createOrganisation(random(), null, null, null,null);
		Organisation organisation2 = organisationService.createOrganisation(random(), null, null, null,null);
		sut.createRelation(project1, organisation1);
		sut.createRelation(project1, organisation2);
		ProjTemplateToOrganisation templateToOrganisation21 = sut.createRelation(project2, organisation2);
		dbInstance.commitAndCloseSession();
		
		sut.delete(project1);
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.loadRelations(project1, null)).isEmpty();
		assertThat(sut.loadRelations(project2, null)).containsExactlyInAnyOrder(templateToOrganisation21);
	}

}

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
package org.olat.modules.portfolio.manager;

import java.util.List;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.modules.forms.manager.EvaluationFormTestsHelper;
import org.olat.modules.portfolio.Assignment;
import org.olat.modules.portfolio.AssignmentStatus;
import org.olat.modules.portfolio.AssignmentType;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PageBody;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.SectionRef;
import org.olat.modules.portfolio.model.SynchedBinder;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssignmentDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private AssignmentDAO assignmentDao;
	@Autowired
	private PortfolioService portfolioService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private EvaluationFormTestsHelper evaTestHelper;
	
	@Test
	public void createBinderWithAssignment() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-1");
		Binder binder = portfolioService.createNewBinder("Assignment binder 1", "Difficult!", null, owner);
		dbInstance.commit();
		portfolioService.appendNewSection("Section", "Assignment section", null, null, binder);
		dbInstance.commit();
		
		//create assignment
		List<Section> sections = portfolioService.getSections(binder);
		Assignment assignment = assignmentDao.createAssignment("Difficult", "Very difficult", "The difficult content",
				null, AssignmentType.essay, false, AssignmentStatus.template, sections.get(0), null, false, false, false, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(assignment);
		Assert.assertNotNull(assignment.getKey());
		Assert.assertNotNull(assignment.getCreationDate());
		Assert.assertNotNull(assignment.getLastModified());
	}

	@Test
	public void loadAssignments_binder() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-2");
		Binder binder = portfolioService.createNewBinder("Assignment binder 2", "Difficult!", null, owner);
		dbInstance.commit();
		portfolioService.appendNewSection("Section", "Assignment section", null, null, binder);
		dbInstance.commit();
		//create assignment
		List<Section> sections = portfolioService.getSections(binder);
		Assignment assignment = assignmentDao.createAssignment("Load assignment", "Load by binder", "The difficult content",
				null, AssignmentType.essay, false, AssignmentStatus.template, sections.get(0), null, false, false, false, null);
		dbInstance.commitAndCloseSession();
	
		//load the assignment
		List<Assignment> assignments = assignmentDao.loadAssignments(binder, null);
		Assert.assertNotNull(assignments);
		Assert.assertEquals(1, assignments.size());
		Assert.assertEquals(assignment, assignments.get(0));
	}
	
	/**
	 * the method doesn't load the assignment templates on binder level.
	 * This test checks that's the case.
	 */
	@Test
	public void loadAssignments_binder_excludedTemplates() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-3");
		Binder binder = portfolioService.createNewBinder("Assignment binder 3", "Difficult!", null, owner);
		dbInstance.commit();
		portfolioService.appendNewSection("Section", "Assignment section", null, null, binder);
		dbInstance.commit();
		//create assignment
		List<Section> sections = portfolioService.getSections(binder);
		Assignment assignment = assignmentDao.createAssignment("Load assignment", "Load by binder", "The difficult content",
				null, AssignmentType.essay, false, AssignmentStatus.template, sections.get(0), null, false, false, false, null);
		dbInstance.commitAndCloseSession();
		
		//create assignment (need to relad the binder to have an up-to-date sections list)
		binder = portfolioService.getBinderByKey(binder.getKey());
		Assignment template = assignmentDao.createAssignment("Load assignment", "Load by binder", "The difficult content",
				null, AssignmentType.document, true, AssignmentStatus.template, null, binder, false, false, false, null);
		dbInstance.commitAndCloseSession();
	
		//load the assignment
		List<Assignment> assignments = assignmentDao.loadAssignments(binder, null);
		Assert.assertNotNull(assignments);
		Assert.assertEquals(1, assignments.size());
		Assert.assertEquals(assignment, assignments.get(0));
		Assert.assertNotEquals(template, assignments.get(0));
	}

	@Test
	public void loadBinderAssignmentsTemplates_binder() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-template-1");
		Binder binder = portfolioService.createNewBinder("Assignment template binder", "Difficult templates!", null, owner);
		dbInstance.commit();
		portfolioService.appendNewSection("Section", "Assignment section", null, null, binder);
		//create assignment
		Assignment assignment = assignmentDao.createAssignment("Load assignment", "Load by binder", "The difficult content",
				null, AssignmentType.document, true, AssignmentStatus.template, null, binder, false, false, false, null);
		dbInstance.commitAndCloseSession();

		List<Assignment> assignmentTemplates = assignmentDao.loadBinderAssignmentsTemplates(binder);
		Assert.assertNotNull(assignmentTemplates);
		Assert.assertEquals(1, assignmentTemplates.size());
		Assert.assertEquals(assignment, assignmentTemplates.get(0));
	}
	
	/**
	 * The method only load assignment templates. It must ignore the section's
	 * assignments.
	 */
	@Test
	public void loadBinderAssignmentsTemplates_binder_excludeSectionAssignment() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-template-1");
		Binder binder = portfolioService.createNewBinder("Assignment template binder", "Difficult templates!", null, owner);
		dbInstance.commit();
		portfolioService.appendNewSection("Section", "Assignment section", null, null, binder);
		//create assignment
		Assignment template = assignmentDao.createAssignment("Load assignment", "Load by binder", "The difficult content",
				null, AssignmentType.document, true, AssignmentStatus.template, null, binder, false, false, false, null);
		dbInstance.commitAndCloseSession();
		
		//create a section's assignment
		List<Section> sections = portfolioService.getSections(binder);
		Assignment assignment = assignmentDao.createAssignment("Load assignment", "Load by binder", "The difficult content",
				null, AssignmentType.essay, false, AssignmentStatus.template, sections.get(0), null, false, false, false, null);
		dbInstance.commitAndCloseSession();

		List<Assignment> assignmentTemplates = assignmentDao.loadBinderAssignmentsTemplates(binder);
		Assert.assertNotNull(assignmentTemplates);
		Assert.assertEquals(1, assignmentTemplates.size());
		Assert.assertEquals(template, assignmentTemplates.get(0));
		Assert.assertNotEquals(assignment, assignmentTemplates.get(0));
	}

	@Test
	public void hasBinderAssignmentTemplate_binderRef() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-template-2");
		Binder binder = portfolioService.createNewBinder("Assignment template binder", "Difficult templates!", null, owner);
		dbInstance.commit();
		portfolioService.appendNewSection("Section", "Assignment section", null, null, binder);
		dbInstance.commit();
		
		// check if there is an assignment, no
		boolean forgotenAssignmentTemplates = assignmentDao.hasBinderAssignmentTemplate(binder);
		Assert.assertFalse(forgotenAssignmentTemplates);
		
		//create an assignment
		Assignment assignment = assignmentDao.createAssignment("Load assignment", "Load by binder", "The difficult content",
				null, AssignmentType.document, true, AssignmentStatus.template, null, binder, false, false, false, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(assignment);

		boolean assignmentTemplates = assignmentDao.hasBinderAssignmentTemplate(binder);
		Assert.assertTrue(assignmentTemplates);
	}
	
	
	@Test
	public void loadAssignments_binder_search() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-3");
		Binder binder = portfolioService.createNewBinder("Assignment binder 3", "Difficult!", null, owner);
		dbInstance.commit();
		portfolioService.appendNewSection("Section", "Assignment section", null, null, binder);
		dbInstance.commit();
		//create assignment
		List<Section> sections = portfolioService.getSections(binder);
		Assignment assignment = assignmentDao.createAssignment("Load assignment", "Load by binder", "The content unkown search",
				null, AssignmentType.essay, false, AssignmentStatus.template, sections.get(0), null, false, false, false, null);
		dbInstance.commitAndCloseSession();
	
		//search the assignment
		List<Assignment> assignments = assignmentDao.loadAssignments(binder, "unkown");
		Assert.assertNotNull(assignments);
		Assert.assertEquals(1, assignments.size());
		Assert.assertEquals(assignment, assignments.get(0));
		
		//dummy search
		List<Assignment> emptyAssignments = assignmentDao.loadAssignments(binder, "sdhfks");
		Assert.assertNotNull(emptyAssignments);
		Assert.assertEquals(0, emptyAssignments.size());
	}
	
	@Test
	public void loadAssignments_section() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-4");
		Binder binder = portfolioService.createNewBinder("Assignment binder 4", "Difficult!", null, owner);
		dbInstance.commit();
		portfolioService.appendNewSection("Section", "Assignment section", null, null, binder);
		dbInstance.commit();
		//create assignment
		List<Section> sections = portfolioService.getSections(binder);
		Section section = sections.get(0);
		Assignment assignment = assignmentDao.createAssignment("Load assignment", "Load by section", "The another content",
				null, AssignmentType.essay, false, AssignmentStatus.template, section, null, false, false, false, null);
		dbInstance.commitAndCloseSession();
	
		//load the assignment
		List<Assignment> assignments = assignmentDao.loadAssignments(section, null);
		Assert.assertNotNull(assignments);
		Assert.assertEquals(1, assignments.size());
		Assert.assertEquals(assignment, assignments.get(0));
	}
	
	@Test
	public void loadAssignments_section_search() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-5");
		Binder binder = portfolioService.createNewBinder("Assignment binder 5", "Difficult!", null, owner);
		dbInstance.commit();
		portfolioService.appendNewSection("Section", "Assignment section", null, null, binder);
		dbInstance.commit();
		//create assignment
		List<Section> sections = portfolioService.getSections(binder);
		Section section = sections.get(0);
		Assignment assignment = assignmentDao.createAssignment("Load assignment", "Load by binder", "The little blabla to search",
				null, AssignmentType.essay, false, AssignmentStatus.template, section, null, false, false, false, null);
		dbInstance.commitAndCloseSession();
	
		//search the assignment
		List<Assignment> assignments = assignmentDao.loadAssignments(section, "blabla");
		Assert.assertNotNull(assignments);
		Assert.assertEquals(1, assignments.size());
		Assert.assertEquals(assignment, assignments.get(0));
		
		//dummy search
		List<Assignment> emptyAssignments = assignmentDao.loadAssignments(section, "wezruiwezi");
		Assert.assertNotNull(emptyAssignments);
		Assert.assertEquals(0, emptyAssignments.size());
	}
	
	@Test
	public void isAssignmentInUse() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("port-u-10");
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("port-u-11");
		RepositoryEntry templateEntry = createTemplate(owner, "Template", "TE");
		dbInstance.commitAndCloseSession();
		
		//1 section
		Binder templateBinder = portfolioService.getBinderByResource(templateEntry.getOlatResource());
		SectionRef sectionRef = portfolioService.getSections(templateBinder).get(0);
		dbInstance.commit();
		
		//make 1 assignment
		Section templateSection = portfolioService.getSection(sectionRef);
		Assignment assignment = portfolioService.addAssignment("1 Assignment", "", "", AssignmentType.essay, false, templateSection, null, false, false, false, null);
		dbInstance.commit();
		
		// check the method
		boolean assignmentNotInUse = assignmentDao.isAssignmentInUse(assignment);
		Assert.assertFalse(assignmentNotInUse);

		// synched and check the sections order
		Binder binder = portfolioService.assignBinder(id, templateBinder, templateEntry, null, null);
		SynchedBinder synchedBinder = portfolioService.loadAndSyncBinder(binder);
		binder = synchedBinder.getBinder();
		dbInstance.commit();
		
		List<Assignment> assignments = portfolioService.getSectionsAssignments(binder, null);
		Assert.assertEquals(1, assignments.size());
		portfolioService.startAssignment(assignments.get(0).getKey(), id);
		dbInstance.commitAndCloseSession();
		
		// check the method
		boolean assignmentInUse = assignmentDao.isAssignmentInUse(assignment);
		Assert.assertTrue(assignmentInUse);
	}
	
	@Test
	public void isFormEntryInUse() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("port-u-10");
		RepositoryEntry templateEntry = createTemplate(owner, "Template", "TE");
		RepositoryEntry formEntry = evaTestHelper.createSurvey().getFormEntry();
		dbInstance.commitAndCloseSession();
		
		//1 section
		Binder templateBinder = portfolioService.getBinderByResource(templateEntry.getOlatResource());
		SectionRef sectionRef = portfolioService.getSections(templateBinder).get(0);
		dbInstance.commit();
		
		//make 1 assignment
		Section templateSection = portfolioService.getSection(sectionRef);
		portfolioService.addAssignment("1 Assignment", "", "", AssignmentType.essay, false, templateSection, null, false, false, false, formEntry);
		dbInstance.commit();
		
		// check the method
		boolean formEntryNotInUse = assignmentDao.isFormEntryInUse(formEntry);
		Assert.assertTrue(formEntryNotInUse);
	}
	
	@Test
	public void isFormEntryInUse_notUsed() {
		RepositoryEntry notUsedFormEntry = evaTestHelper.createSurvey().getFormEntry();
		dbInstance.commitAndCloseSession();

		// check the method
		boolean formEntryNotInUse = assignmentDao.isFormEntryInUse(notUsedFormEntry);
		Assert.assertFalse(formEntryNotInUse);
	}
	
	@Test
	public void loadAssignment_pageBody() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("port-u-10");
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("port-u-11");
		RepositoryEntry templateEntry = createTemplate(owner, "Template", "TE");
		dbInstance.commitAndCloseSession();
		
		//1 section
		Binder templateBinder = portfolioService.getBinderByResource(templateEntry.getOlatResource());
		SectionRef sectionRef = portfolioService.getSections(templateBinder).get(0);
		dbInstance.commit();
		
		//make 1 assignment
		Section templateSection = portfolioService.getSection(sectionRef);
		Assignment assignment = portfolioService.addAssignment("1 Assignment", "", "", AssignmentType.essay, false, templateSection, null, false, false, false, null);
		dbInstance.commit();
		Assert.assertNotNull(assignment);

		// synched and check the sections order
		Binder binder = portfolioService.assignBinder(id, templateBinder, templateEntry, null, null);
		SynchedBinder synchedBinder = portfolioService.loadAndSyncBinder(binder);
		binder = synchedBinder.getBinder();
		dbInstance.commit();
		
		List<Assignment> assignments = portfolioService.getSectionsAssignments(binder, null);
		Assert.assertEquals(1, assignments.size());
		Assignment startedAssignment = portfolioService.startAssignment(assignments.get(0).getKey(), id);
		dbInstance.commit();
		
		// get the unique page body
		List<Section> sections = portfolioService.getSections(binder);
		PageBody body = sections.get(0).getPages().get(0).getBody();
		
		// check the method
		Assignment assignmentInUse = assignmentDao.loadAssignment(body);
		Assert.assertNotNull(assignmentInUse);
		Assert.assertEquals(startedAssignment, assignmentInUse);
	}
	
	/**
	 * Create a portfolio template with an assignment's template,
	 * use it and the assignment as template. Check that the synched
	 * binder has a copy of the template, check that the instantiated
	 * assignment as a reference to the synched template (not the original).
	 */
	@Test
	public void loadAssignmentTemplate_pageBody() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("port-u-10");
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("port-u-11");
		RepositoryEntry templateEntry = createTemplate(owner, "Template", "TE");
		dbInstance.commitAndCloseSession();

		//make 1 assignment
		Binder templateBinder = portfolioService.getBinderByResource(templateEntry.getOlatResource());
		Assignment template = portfolioService.addAssignment("1 Assignment", "", "", AssignmentType.essay, true, null, templateBinder, false, false, false, null);
		dbInstance.commit();
		
		// synched and check the sections order
		Binder binder = portfolioService.assignBinder(id, templateBinder, templateEntry, null, null);
		SynchedBinder synchedBinder = portfolioService.loadAndSyncBinder(binder);
		binder = synchedBinder.getBinder();
		dbInstance.commit();
		
		List<Assignment> templates = assignmentDao.loadBinderAssignmentsTemplates(binder);
		Assert.assertEquals(1, templates.size());
		Assert.assertEquals(template, templates.get(0).getTemplateReference());
		
		// new page based on an assignment template
		List<Section> sections = portfolioService.getSections(binder);
		Page page = portfolioService.startAssignmentFromTemplate(template.getKey(), id,
				"From template", "Froma template", null, null, sections.get(0), null, null);
		dbInstance.commit();

		// check the method
		Assignment assignmentInUse = assignmentDao.loadAssignment(page.getBody());
		Assert.assertNotNull(assignmentInUse);
		Assert.assertEquals(template, assignmentInUse.getTemplateReference());
	}
	
	private RepositoryEntry createTemplate(Identity initialAuthor,  String displayname, String description) {
		return createTemplate(initialAuthor, RepositoryEntryStatusEnum.preparation, displayname, description);
	}
	
	private RepositoryEntry createTemplate(Identity initialAuthor, RepositoryEntryStatusEnum status, String displayname, String description) {
		OLATResource resource = portfolioService.createBinderTemplateResource();
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry re = repositoryService.create(initialAuthor, null, "", displayname, description,
				resource, status, defOrganisation);
		portfolioService.createAndPersistBinderTemplate(initialAuthor, re, Locale.ENGLISH);
		return re;
	}
	
}
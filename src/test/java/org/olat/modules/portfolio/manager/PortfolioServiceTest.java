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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.commentAndRating.manager.UserCommentsDAO;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.portfolio.Assignment;
import org.olat.modules.portfolio.AssignmentType;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.Category;
import org.olat.modules.portfolio.Media;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PageBody;
import org.olat.modules.portfolio.PortfolioRoles;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.SectionRef;
import org.olat.modules.portfolio.handler.TextHandler;
import org.olat.modules.portfolio.model.AccessRights;
import org.olat.modules.portfolio.model.BinderStatistics;
import org.olat.modules.portfolio.model.MediaPart;
import org.olat.modules.portfolio.model.PageImpl;
import org.olat.modules.portfolio.model.SectionImpl;
import org.olat.modules.portfolio.model.SynchedBinder;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.olat.user.UserLifecycleManager;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 * Initial date: 07.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PortfolioServiceTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private PageDAO pageDao;
	@Autowired
	private MediaDAO mediaDao;
	@Autowired
	private BinderDAO binderDao;
	@Autowired
	private CategoryDAO categoryDao;
	@Autowired
	private AssignmentDAO assignmentDao;
	@Autowired
	private UserCommentsDAO userCommentsDao;
	@Autowired
	private PortfolioService portfolioService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private UserLifecycleManager userLifecycleManager;
	@Autowired
	private OrganisationService organisationService;
	
	@Test
	public void createNewOwnedPorfolio() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("port-u-1");
		String title = "My portfolio";
		String summary = "My live";
		
		Binder binder = portfolioService.createNewBinder(title, summary, null, id);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(binder);
		Assert.assertNotNull(binder.getKey());
		Assert.assertNotNull(binder.getCreationDate());
		Assert.assertNotNull(binder.getLastModified());
		Assert.assertEquals(title, binder.getTitle());
		Assert.assertEquals(summary, binder.getSummary());
		
		List<Binder> ownedBinders = portfolioService.getOwnedBinders(id);
		Assert.assertNotNull(ownedBinders);
		Assert.assertEquals(1, ownedBinders.size());
		Binder ownedBinder = ownedBinders.get(0);
		Assert.assertNotNull(ownedBinder);
		Assert.assertEquals(binder, ownedBinder);
	}
	
	@Test
	public void binderAccessRights() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("port-u-2");
		String title = "My private binder";
		String summary = "My live";
		Binder binder = portfolioService.createNewBinder(title, summary, null, owner);
		dbInstance.commitAndCloseSession();
		
		// load right
		List<AccessRights> rights = portfolioService.getAccessRights(binder);
		Assert.assertNotNull(rights);
		Assert.assertEquals(1, rights.size());
		AccessRights ownerRight = rights.get(0);
		Assert.assertEquals(binder.getKey(), ownerRight.getBinderKey());
		Assert.assertEquals(owner, ownerRight.getIdentity());
		Assert.assertEquals(PortfolioRoles.owner, ownerRight.getRole());
	}

	@Test
	public void binderAndSectionAndPageAccessRights() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("port-u-3");
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("port-u-4");
		Identity reviewer = JunitTestHelper.createAndPersistIdentityAsRndUser("port-u-5");
		String title = "My published binder";
		String summary = "My live";
		Binder binder = portfolioService.createNewBinder(title, summary, null, owner);
		dbInstance.commit();
		portfolioService.appendNewSection("Section", "Coached section", null, null, binder);
		dbInstance.commit();
		List<Section> sections = portfolioService.getSections(binder);
		Section section = sections.get(0);
		portfolioService.appendNewPage(owner, "Reviewed page", "", null, null, section);
		portfolioService.addAccessRights(section, coach, PortfolioRoles.coach);
		
		dbInstance.commit();
		List<Page> pages = portfolioService.getPages(section);
		Page page = pages.get(0);
		portfolioService.addAccessRights(page, reviewer, PortfolioRoles.reviewer);

		// load right
		List<AccessRights> rights = portfolioService.getAccessRights(binder);
		Assert.assertNotNull(rights);
		Assert.assertEquals(4, rights.size());
		
		boolean foundOwner = false;
		boolean foundCoach = false;
		boolean foundReviewer = false;
		
		for(AccessRights right:rights) {
			if(PortfolioRoles.owner.equals(right.getRole()) && owner.equals(right.getIdentity())) {
				foundOwner = true;
			} else if(PortfolioRoles.coach.equals(right.getRole()) && coach.equals(right.getIdentity())) {
				foundCoach = true;
			} else if(PortfolioRoles.reviewer.equals(right.getRole()) && reviewer.equals(right.getIdentity())) {
				foundReviewer = true;
			}
		}
		
		Assert.assertTrue(foundOwner);
		Assert.assertTrue(foundCoach);
		Assert.assertTrue(foundReviewer);
	}
	
	@Test
	public void binderAndSectionAndPageAccessRights_byIdentity() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("port-u-5");
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("port-u-6");
		String title = "My published binder";
		String summary = "My live";
		Binder binder = portfolioService.createNewBinder(title, summary, null, owner);
		dbInstance.commit();
		portfolioService.appendNewSection("Section", "Coached section", null, null, binder);
		dbInstance.commit();
		List<Section> sections = portfolioService.getSections(binder);
		Section section = sections.get(0);
		portfolioService.appendNewPage(owner, "Reviewed page", "", null, null, section);
		portfolioService.addAccessRights(section, identity, PortfolioRoles.coach);
		
		dbInstance.commit();
		List<Page> pages = portfolioService.getPages(section);
		Page page = pages.get(0);
		portfolioService.addAccessRights(page, identity, PortfolioRoles.reviewer);

		// load right
		List<AccessRights> rights = portfolioService.getAccessRights(binder, identity);
		Assert.assertNotNull(rights);
		Assert.assertEquals(2, rights.size());
	}
	
	
	@Test
	public void searchOwnedBinders() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("binder-owner-");
		Binder binder = portfolioService.createNewBinder("Binder 2", "Binder with one section.", null, owner);
		dbInstance.commitAndCloseSession();
		portfolioService.appendNewSection("First section", "My first section.", null, null, binder);
		dbInstance.commitAndCloseSession();
		portfolioService.appendNewSection("Second section", "My second section.", null, null, binder);
		dbInstance.commitAndCloseSession();
		
		List<Section> sections = portfolioService.getSections(binder);
		for(int i=0; i<2; i++) {
			Section section = sections.get(1);
			portfolioService.appendNewPage(owner, "Page-1-" + i, "", null, null, section);
			portfolioService.appendNewPage(owner, "Page-2-" + i, "", null, null, section);
		}

		List<BinderStatistics> rows = portfolioService.searchOwnedBinders(owner);
		Assert.assertNotNull(rows);
		Assert.assertEquals(1, rows.size());
		
		BinderStatistics myBinder = rows.get(0);
		Assert.assertEquals(2, myBinder.getNumOfSections());
		Assert.assertEquals(4, myBinder.getNumOfPages());
	}
	
	@Test
	public void assignTemplate() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("port-u-7");
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("port-u-8");
		
		//create a template
		String title = "Template binder";
		String summary = "Binder used as a template";
		Binder template = portfolioService.createNewBinder(title, summary, null, owner);
		dbInstance.commit();
		for(int i=0; i<4; i++) {
			portfolioService.appendNewSection("Section " + i, "Section " + i, null, null, template);
			dbInstance.commit();
		}
		dbInstance.commitAndCloseSession();
		List<Section> templateSections = portfolioService.getSections(template);
		Assert.assertNotNull(templateSections);
		Assert.assertEquals(4, templateSections.size());
		
		
		//user copy the template
		Binder binder = portfolioService.assignBinder(id, template, null, null, new Date());
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(binder);
		Assert.assertNotNull(binder.getKey());
		Assert.assertNotNull(binder.getCopyDate());
		Assert.assertNotNull(template.getTitle(), binder.getTitle());
		
		List<Section> reloadedSections = portfolioService.getSections(binder);
		Assert.assertNotNull(reloadedSections);
		Assert.assertEquals(4, reloadedSections.size());
		Assert.assertEquals(templateSections.get(0).getTitle(), reloadedSections.get(0).getTitle());
		Assert.assertEquals("Section 1", reloadedSections.get(1).getTitle());
		Assert.assertEquals(templateSections.get(2).getTitle(), reloadedSections.get(2).getTitle());
		Assert.assertEquals("Section 3", reloadedSections.get(3).getTitle());
		
		Assert.assertEquals(templateSections.get(0), ((SectionImpl)reloadedSections.get(0)).getTemplateReference());
		Assert.assertEquals(templateSections.get(1), ((SectionImpl)reloadedSections.get(1)).getTemplateReference());
		Assert.assertEquals(templateSections.get(2), ((SectionImpl)reloadedSections.get(2)).getTemplateReference());
		Assert.assertEquals(templateSections.get(3), ((SectionImpl)reloadedSections.get(3)).getTemplateReference());
	}

	@Test
	public void isTemplateInUse() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("port-u-9");
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("port-u-10");
		RepositoryEntry templateEntry = createTemplate(owner, "Template", "TE");
		dbInstance.commitAndCloseSession();

		//assign a template
		Binder templateBinder = portfolioService.getBinderByResource(templateEntry.getOlatResource());
		Binder template = portfolioService.assignBinder(id, templateBinder, templateEntry, null, null);
		dbInstance.commit();
		Assert.assertNotNull(template);

		boolean inUse = portfolioService.isTemplateInUse(templateBinder, templateEntry, null);
		Assert.assertTrue(inUse);
	}

	@Test
	public void syncBinder() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("port-u-10");
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("port-u-11");
		RepositoryEntry templateEntry = createTemplate(owner, "Template", "TE");
		dbInstance.commitAndCloseSession();

		//make 2 sections
		Binder templateBinder = portfolioService.getBinderByResource(templateEntry.getOlatResource());
		//add 2 sections
		for(int i=0; i<2; i++) {
			portfolioService.appendNewSection("Section " + i, "Section " + i, null, null, templateBinder);
			dbInstance.commit();
		}
		
		List<Section> templateSections = portfolioService.getSections(templateBinder);
		Assert.assertNotNull(templateSections);
		Assert.assertEquals(3, templateSections.size());
		
		//user get a the binder from the template
		Binder binder = portfolioService.assignBinder(id, templateBinder, templateEntry, "ac-234", new Date());
		dbInstance.commit();
		Assert.assertNotNull(binder);
		boolean inUse = portfolioService.isTemplateInUse(templateBinder, templateEntry, "ac-234");
		Assert.assertTrue(inUse);
		
		//update the template with 2 more sections
		for(int i=2; i<4; i++) {
			portfolioService.appendNewSection("Section " + i, "Section " + i, null, null, templateBinder);
			dbInstance.commit();
		}
		
		SynchedBinder synchedBinder = portfolioService.loadAndSyncBinder(binder);
		Assert.assertNotNull(synchedBinder);
		dbInstance.commit();
		Assert.assertTrue(synchedBinder.isChanged());
		Assert.assertEquals(binder, synchedBinder.getBinder());
		List<Section> synchedSections = portfolioService.getSections(synchedBinder.getBinder());
		Assert.assertEquals(5, synchedSections.size());
	}
	
	@Test
	public void syncBinder_move() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("port-u-10");
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("port-u-11");
		RepositoryEntry templateEntry = createTemplate(owner, "Template", "TE");
		dbInstance.commitAndCloseSession();
		
		//make 2 sections
		Binder templateBinder = portfolioService.getBinderByResource(templateEntry.getOlatResource());
		SectionRef sectionRef0 = portfolioService.getSections(templateBinder).get(0);
		//add 2 sections
		SectionRef sectionRef1 = portfolioService.appendNewSection("1 section ", "Section 1", null, null, templateBinder);
		SectionRef sectionRef2 = portfolioService.appendNewSection("2 section ", "Section 2", null, null, templateBinder);
		dbInstance.commit();
		
		//make 4 assigments
		Section templateSection0 = portfolioService.getSection(sectionRef0);
		Section templateSection1 = portfolioService.getSection(sectionRef1);
		Section templateSection2 = portfolioService.getSection(sectionRef2);
		Assignment assignment1_1 = portfolioService.addAssignment("1.1 Assignment", "", "", AssignmentType.essay, false, templateSection1, null, false, false, false, null);
		Assignment assignment1_2 = portfolioService.addAssignment("1.2 Assignment", "", "", AssignmentType.essay, false, templateSection1, null, false, false, false, null);
		Assignment assignment2_1 = portfolioService.addAssignment("2.1 Assignment", "", "", AssignmentType.essay, false, templateSection2, null, false, false, false, null);
		Assignment assignment2_2 = portfolioService.addAssignment("2.2 Assignment", "", "", AssignmentType.essay, false, templateSection2, null, false, false, false, null);
		dbInstance.commit();
		List<Assignment> templateAssignments = portfolioService.getSectionsAssignments(templateBinder, null);
		Assert.assertEquals(4, templateAssignments.size());
		
		// a user take the binder and synched it a first time
		Binder binder = portfolioService.assignBinder(id, templateBinder, templateEntry, "72", null);
		dbInstance.commit();
		SynchedBinder synchedBinder = portfolioService.loadAndSyncBinder(binder);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(synchedBinder);
		Assert.assertEquals(binder, synchedBinder.getBinder());
		
		//start all assigments
		List<Assignment> assignments = portfolioService.getSectionsAssignments(binder, null);
		Assert.assertEquals(4, assignments.size());
		for(Assignment assignment:assignments) {
			portfolioService.startAssignment(assignment.getKey(), id);
			dbInstance.commit();
		}
		dbInstance.commit();
		
		List<Page> pages = portfolioService.getPages(binder, null);
		Assert.assertEquals(4, pages.size());
		
		//the author move an assigment
		portfolioService.moveAssignment(templateSection1, assignment1_1, templateSection2);
		dbInstance.commit();
		portfolioService.moveAssignment(templateSection2, assignment2_1, templateSection1);
		dbInstance.commitAndCloseSession();
		
		//check the move
		List<Assignment> templateAssignmentsSection1 = portfolioService.getSectionsAssignments(templateSection1, null);
		Assert.assertTrue(templateAssignmentsSection1.contains(assignment1_2));
		Assert.assertTrue(templateAssignmentsSection1.contains(assignment2_1));
		List<Assignment> templateAssignmentsSection2 = portfolioService.getSectionsAssignments(templateSection2, null);
		Assert.assertTrue(templateAssignmentsSection2.contains(assignment2_2));
		Assert.assertTrue(templateAssignmentsSection2.contains(assignment1_1));
		
		// synched and check the sections order
		SynchedBinder synchedBinder2 = portfolioService.loadAndSyncBinder(binder);
		Binder freshBinder = synchedBinder2.getBinder();
		List<Section> sections = portfolioService.getSections(freshBinder);
		Assert.assertEquals(3, sections.size());
		Section section0 = sections.get(0);
		Section section1 = sections.get(1);
		Section section2 = sections.get(2);
		Assert.assertEquals(templateSection0, section0.getTemplateReference());
		Assert.assertEquals(templateSection1, section1.getTemplateReference());
		Assert.assertEquals(templateSection2, section2.getTemplateReference());
		
		// load pages from section 1
		List<Page> pagesSection1 = portfolioService.getPages(section1);
		Assert.assertEquals(2, pagesSection1.size());
		
		Page page1_2 = pagesSection1.get(0);
		Page page2_1 = pagesSection1.get(1);
		Assert.assertTrue(page1_2.getTitle().equals("2.1 Assignment") || page1_2.getTitle().equals("1.2 Assignment"));
		Assert.assertTrue(page2_1.getTitle().equals("2.1 Assignment") || page2_1.getTitle().equals("1.2 Assignment"));
		
		// and pages from section 2
		List<Page> pagesSection2 = portfolioService.getPages(section2);
		Assert.assertEquals(2, pagesSection2.size());
		Page page2_2 = pagesSection2.get(0);
		Page page1_1 = pagesSection2.get(1);
		Assert.assertTrue(page2_2.getTitle().equals("1.1 Assignment") || page2_2.getTitle().equals("2.2 Assignment"));
		Assert.assertTrue(page1_1.getTitle().equals("1.1 Assignment") || page1_1.getTitle().equals("2.2 Assignment"));
	}
	
	@Test
	public void syncBinder_moveInNewSection() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("port-u-10");
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("port-u-11");
		RepositoryEntry templateEntry = createTemplate(owner, "Template", "TE");
		dbInstance.commitAndCloseSession();
		
		//make 2 sections
		Binder templateBinder = portfolioService.getBinderByResource(templateEntry.getOlatResource());
		SectionRef sectionRef0 = portfolioService.getSections(templateBinder).get(0);
		//add 2 sections
		SectionRef sectionRef1 = portfolioService.appendNewSection("1 section ", "Section 1", null, null, templateBinder);
		SectionRef sectionRef2 = portfolioService.appendNewSection("2 section ", "Section 2", null, null, templateBinder);
		dbInstance.commit();
		
		//make 4 assignments
		Section templateSection0 = portfolioService.getSection(sectionRef0);
		Section templateSection1 = portfolioService.getSection(sectionRef1);
		Section templateSection2 = portfolioService.getSection(sectionRef2);
		Assignment assignment1_1 = portfolioService.addAssignment("1.1 Assignment", "", "", AssignmentType.essay, false, templateSection1, null, false, false, false, null);
		Assignment assignment1_2 = portfolioService.addAssignment("1.2 Assignment", "", "", AssignmentType.essay, false, templateSection1, null, false, false, false, null);
		Assignment assignment2_1 = portfolioService.addAssignment("2.1 Assignment", "", "", AssignmentType.essay, false, templateSection2, null, false, false, false, null);
		Assignment assignment2_2 = portfolioService.addAssignment("2.2 Assignment", "", "", AssignmentType.essay, false, templateSection2, null, false, false, false, null);
		dbInstance.commit();
		List<Assignment> templateAssignments = portfolioService.getSectionsAssignments(templateBinder, null);
		Assert.assertEquals(4, templateAssignments.size());
		
		// a user take the binder and synched it a first time
		Binder binder = portfolioService.assignBinder(id, templateBinder, templateEntry, "72", null);
		dbInstance.commit();
		SynchedBinder synchedBinder = portfolioService.loadAndSyncBinder(binder);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(synchedBinder);
		Assert.assertEquals(binder, synchedBinder.getBinder());
		
		//start all assignments
		List<Assignment> assignments = portfolioService.getSectionsAssignments(binder, null);
		Assert.assertEquals(4, assignments.size());
		for(Assignment assignment:assignments) {
			portfolioService.startAssignment(assignment.getKey(), id);
			dbInstance.commit();
		}
		dbInstance.commit();
		
		// check that the student has it's 4 pages
		List<Page> pages = portfolioService.getPages(binder, null);
		Assert.assertEquals(4, pages.size());
		
		//author create a new section and move an assignment
		SectionRef sectionRef3 = portfolioService.appendNewSection("3 section ", "Section 3", null, null, templateBinder);
		dbInstance.commit();

		Section templateSection3 = portfolioService.getSection(sectionRef3);
		Assignment assignment3_1 = portfolioService.addAssignment("3.1 Assignment", "", "", AssignmentType.essay, false, templateSection3, null, false, false, false, null);
		dbInstance.commit();
		
		//the author move an assigment
		portfolioService.moveAssignment(templateSection1, assignment1_1, templateSection2);
		dbInstance.commit();
		portfolioService.moveAssignment(templateSection2, assignment2_1, templateSection3);
		dbInstance.commitAndCloseSession();
		
		//check the move
		List<Assignment> templateAssignmentsSection1 = portfolioService.getSectionsAssignments(templateSection1, null);
		Assert.assertTrue(templateAssignmentsSection1.contains(assignment1_2));
		List<Assignment> templateAssignmentsSection2 = portfolioService.getSectionsAssignments(templateSection2, null);
		Assert.assertTrue(templateAssignmentsSection2.contains(assignment2_2));
		Assert.assertTrue(templateAssignmentsSection2.contains(assignment1_1));
		List<Assignment> templateAssignmentsSection3 = portfolioService.getSectionsAssignments(templateSection3, null);
		Assert.assertTrue(templateAssignmentsSection3.contains(assignment2_1));
		Assert.assertTrue(templateAssignmentsSection3.contains(assignment3_1));
		
		// synched and check the sections order
		SynchedBinder synchedBinder2 = portfolioService.loadAndSyncBinder(binder);
		Binder freshBinder = synchedBinder2.getBinder();
		List<Section> sections = portfolioService.getSections(freshBinder);
		Assert.assertEquals(4, sections.size());
		Section section0 = sections.get(0);
		Section section1 = sections.get(1);
		Section section2 = sections.get(2);
		Section section3 = sections.get(3);
		Assert.assertEquals(templateSection0, section0.getTemplateReference());
		Assert.assertEquals(templateSection1, section1.getTemplateReference());
		Assert.assertEquals(templateSection2, section2.getTemplateReference());
		Assert.assertEquals(templateSection3, section3.getTemplateReference());
		
		// load pages from section 1
		List<Page> pagesSection1 = portfolioService.getPages(section1);
		Assert.assertEquals(1, pagesSection1.size());
		Page page1_2 = pagesSection1.get(0);
		Assert.assertTrue(page1_2.getTitle().equals("1.2 Assignment"));
		
		// and pages from section 2
		List<Page> pagesSection2 = portfolioService.getPages(section2);
		Assert.assertEquals(2, pagesSection2.size());
		Page page2_2 = pagesSection2.get(0);
		Page page1_1 = pagesSection2.get(1);
		Assert.assertTrue(page2_2.getTitle().equals("1.1 Assignment") || page2_2.getTitle().equals("2.2 Assignment"));
		Assert.assertTrue(page1_1.getTitle().equals("1.1 Assignment") || page1_1.getTitle().equals("2.2 Assignment"));
		
		// and pages from section 3
		List<Page> pagesSection3 = portfolioService.getPages(section3);
		Assert.assertEquals(1, pagesSection3.size());
		Page page2_1 = pagesSection3.get(0);
		Assert.assertTrue(page2_1.getTitle().equals("2.1 Assignment"));		
		
		List<Assignment> assignmentsSection3 = section3.getAssignments();
		Assert.assertEquals(2, assignmentsSection3.size());
		
		Assignment templateSynchedSection3a = assignmentsSection3.get(0).getTemplateReference();
		Assignment templateSynchedSection3b = assignmentsSection3.get(1).getTemplateReference();
		Assert.assertTrue(assignment3_1.equals(templateSynchedSection3a) || assignment3_1.equals(templateSynchedSection3b));
		Assert.assertTrue(assignment2_1.equals(templateSynchedSection3a) || assignment2_1.equals(templateSynchedSection3b));
	}
	
	@Test
	public void syncBinder_moveInNewSection_moreComplexCase() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("port-u-10");
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("port-u-11");
		RepositoryEntry templateEntry = createTemplate(owner, "Template", "TE");
		dbInstance.commitAndCloseSession();
		
		//make 2 sections
		Binder templateBinder = portfolioService.getBinderByResource(templateEntry.getOlatResource());
		SectionRef sectionRef0 = portfolioService.getSections(templateBinder).get(0);
		//add 2 sections
		SectionRef sectionRef1 = portfolioService.appendNewSection("1 section ", "Section 1", null, null, templateBinder);
		SectionRef sectionRef2 = portfolioService.appendNewSection("2 section ", "Section 2", null, null, templateBinder);
		dbInstance.commit();
		
		//make 4 assignments
		Section templateSection0 = portfolioService.getSection(sectionRef0);
		Section templateSection1 = portfolioService.getSection(sectionRef1);
		Section templateSection2 = portfolioService.getSection(sectionRef2);
		Assignment assignment0_1 = portfolioService.addAssignment("0.1 Assignment", "", "", AssignmentType.essay, false, templateSection0, null, false, false, false, null);
		Assignment assignment1_1 = portfolioService.addAssignment("1.1 Assignment", "", "", AssignmentType.essay, false, templateSection1, null, false, false, false, null);
		Assignment assignment1_2 = portfolioService.addAssignment("1.2 Assignment", "", "", AssignmentType.essay, false, templateSection1, null, false, false, false, null);
		Assignment assignment2_1 = portfolioService.addAssignment("2.1 Assignment", "", "", AssignmentType.essay, false, templateSection2, null, false, false, false, null);
		Assignment assignment2_2 = portfolioService.addAssignment("2.2 Assignment", "", "", AssignmentType.essay, false, templateSection2, null, false, false, false, null);
		dbInstance.commit();
		List<Assignment> templateAssignments = portfolioService.getSectionsAssignments(templateBinder, null);
		Assert.assertEquals(5, templateAssignments.size());
		
		// a user take the binder and synched it a first time
		Binder binder = portfolioService.assignBinder(id, templateBinder, templateEntry, "74", null);
		dbInstance.commit();
		SynchedBinder synchedBinder = portfolioService.loadAndSyncBinder(binder);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(synchedBinder);
		Assert.assertEquals(binder, synchedBinder.getBinder());
		
		//start all assignments
		List<Assignment> assignments = portfolioService.getSectionsAssignments(binder, null);
		Assert.assertEquals(5, assignments.size());
		for(Assignment assignment:assignments) {
			portfolioService.startAssignment(assignment.getKey(), id);
			dbInstance.commit();
		}
		dbInstance.commit();
		
		// check that the student has it's 4 pages
		List<Page> pages = portfolioService.getPages(binder, null);
		Assert.assertEquals(5, pages.size());
		
		//author create 2 new sections, move the 4 to the top
		SectionRef sectionRef3 = portfolioService.appendNewSection("3 section ", "Section 3", null, null, templateBinder);
		SectionRef sectionRef4 = portfolioService.appendNewSection("4 section ", "Section 4", null, null, templateBinder);
		dbInstance.commit();
		
		Section templateSection3 = portfolioService.getSection(sectionRef3);
		Section templateSection4 = portfolioService.getSection(sectionRef4);
		
		templateBinder = portfolioService.moveUpSection(templateBinder, templateSection4);
		dbInstance.commit();
		templateBinder = portfolioService.moveUpSection(templateBinder, templateSection4);
		dbInstance.commit();
		templateBinder = portfolioService.moveUpSection(templateBinder, templateSection4);
		dbInstance.commit();
		templateBinder = portfolioService.moveUpSection(templateBinder, templateSection4);
		dbInstance.commit();

		// add new assignment
		Assignment assignment3_1 = portfolioService.addAssignment("3.1 Assignment", "", "", AssignmentType.essay, false, templateSection3, null, false, false, false, null);
		Assignment assignment4_1 = portfolioService.addAssignment("4.1 Assignment", "", "", AssignmentType.essay, false, templateSection4, null, false, false, false, null);
		dbInstance.commit();
		
		//the author move some assignments
		portfolioService.moveAssignment(templateSection1, assignment1_1, templateSection3);
		dbInstance.commit();
		portfolioService.moveAssignment(templateSection1, assignment1_2, templateSection2);
		dbInstance.commit();
		portfolioService.moveAssignment(templateSection2, assignment2_1, templateSection3);
		dbInstance.commit();
		portfolioService.moveAssignment(templateSection2, assignment2_2, templateSection4);
		dbInstance.commitAndCloseSession();
		
		//update the data of some assignments
		assignment2_1 = assignmentDao.loadAssignmentByKey(assignment2_1.getKey());
		assignment4_1 = assignmentDao.loadAssignmentByKey(assignment4_1.getKey());
		assignment2_1 = portfolioService.updateAssignment(assignment2_1, "2.1 Assignment", "Assignment 2 description", "", AssignmentType.essay, false, false, false, null);
		assignment4_1 = portfolioService.updateAssignment(assignment4_1, "4.1 Assignment", "Assignment 4 description", "", AssignmentType.document, false, false, false, null);
		dbInstance.commit();
		
		//check the move
		List<Assignment> templateAssignmentsSection0 = portfolioService.getSectionsAssignments(templateSection0, null);
		Assert.assertTrue(templateAssignmentsSection0.contains(assignment0_1));
		
		List<Assignment> templateAssignmentsSection1 = portfolioService.getSectionsAssignments(templateSection1, null);
		Assert.assertTrue(templateAssignmentsSection1.isEmpty());
		List<Assignment> templateAssignmentsSection2 = portfolioService.getSectionsAssignments(templateSection2, null);
		Assert.assertEquals(1, templateAssignmentsSection2.size());
		Assert.assertTrue(templateAssignmentsSection2.contains(assignment1_2));
		List<Assignment> templateAssignmentsSection3 = portfolioService.getSectionsAssignments(templateSection3, null);
		Assert.assertEquals(3, templateAssignmentsSection3.size());
		Assert.assertTrue(templateAssignmentsSection3.contains(assignment1_1));
		Assert.assertTrue(templateAssignmentsSection3.contains(assignment2_1));
		Assert.assertTrue(templateAssignmentsSection3.contains(assignment3_1));
		List<Assignment> templateAssignmentsSection4 = portfolioService.getSectionsAssignments(templateSection4, null);
		Assert.assertEquals(2, templateAssignmentsSection4.size());
		Assert.assertTrue(templateAssignmentsSection4.contains(assignment2_2));
		Assert.assertTrue(templateAssignmentsSection4.contains(assignment4_1));
		

		// synched and check the sections order
		SynchedBinder synchedBinder2 = portfolioService.loadAndSyncBinder(binder);
		Binder freshBinder = synchedBinder2.getBinder();
		dbInstance.commitAndCloseSession();
		
		List<Section> sections = portfolioService.getSections(freshBinder);
		Assert.assertEquals(5, sections.size());
		Section section4 = sections.get(0);
		Section section0 = sections.get(1);
		Section section1 = sections.get(2);
		Section section2 = sections.get(3);
		Section section3 = sections.get(4);
		Assert.assertEquals(templateSection0, section0.getTemplateReference());
		Assert.assertEquals(templateSection1, section1.getTemplateReference());
		Assert.assertEquals(templateSection2, section2.getTemplateReference());
		Assert.assertEquals(templateSection3, section3.getTemplateReference());
		Assert.assertEquals(templateSection4, section4.getTemplateReference());
		
		// load pages from section 0
		List<Page> pagesSection0 = portfolioService.getPages(section0);
		Assert.assertEquals(1, pagesSection0.size());
		Page page0_1 = pagesSection0.get(0);
		Assert.assertTrue(page0_1.getTitle().equals("0.1 Assignment"));
		// load pages from section 1
		List<Page> pagesSection1 = portfolioService.getPages(section1);
		Assert.assertTrue(pagesSection1.isEmpty());
		// and pages from section 2
		List<Page> pagesSection2 = portfolioService.getPages(section2);
		Assert.assertEquals(1, pagesSection2.size());
		Page page1_2 = pagesSection2.get(0);
		Assert.assertTrue(page1_2.getTitle().equals("1.2 Assignment"));
		// and pages from section 3
		List<Page> pagesSection3 = portfolioService.getPages(section3);
		Assert.assertEquals(2, pagesSection3.size());
		Page page1_1 = pagesSection3.get(0);
		Page page2_1 = pagesSection3.get(1);
		Assert.assertTrue(page1_1.getTitle().equals("1.1 Assignment") || page1_1.getTitle().equals("2.1 Assignment"));
		Assert.assertTrue(page2_1.getTitle().equals("1.1 Assignment") || page2_1.getTitle().equals("2.1 Assignment"));
		// and pages from section 4
		List<Page> pagesSection4 = portfolioService.getPages(section4);
		Assert.assertEquals(1, pagesSection4.size());
		Page page2_2 = pagesSection4.get(0);
		Assert.assertTrue(page2_2.getTitle().equals("2.2 Assignment"));

		
		//check the assignments
		//section 0
		List<Assignment> assignmentsSection0 = section0.getAssignments();
		Assert.assertEquals(1, assignmentsSection0.size());
		Assignment templateSynchedSection0a = assignmentsSection0.get(0).getTemplateReference();
		Assert.assertTrue(assignment0_1.equals(templateSynchedSection0a));
		//section 1
		List<Assignment> assignmentsSection1 = section1.getAssignments();
		Assert.assertTrue(assignmentsSection1.isEmpty());
		//section 2
		List<Assignment> assignmentsSection2= section2.getAssignments();
		Assert.assertEquals(1, assignmentsSection2.size());
		Assignment templateSynchedSection2a = assignmentsSection2.get(0).getTemplateReference();
		Assert.assertTrue(assignment1_2.equals(templateSynchedSection2a));
		//section 3
		List<Assignment> assignmentsSection3 = section3.getAssignments();
		Assert.assertEquals(3, assignmentsSection3.size());
		Assignment templateSynchedSection3a = assignmentsSection3.get(0).getTemplateReference();
		Assignment templateSynchedSection3b = assignmentsSection3.get(1).getTemplateReference();
		Assignment templateSynchedSection3c = assignmentsSection3.get(2).getTemplateReference();
		Assert.assertTrue(assignment3_1.equals(templateSynchedSection3a) || assignment3_1.equals(templateSynchedSection3b) || assignment3_1.equals(templateSynchedSection3c));
		Assert.assertTrue(assignment2_1.equals(templateSynchedSection3a) || assignment2_1.equals(templateSynchedSection3b) || assignment2_1.equals(templateSynchedSection3c));
		Assert.assertTrue(assignment1_1.equals(templateSynchedSection3a) || assignment1_1.equals(templateSynchedSection3b) || assignment1_1.equals(templateSynchedSection3c));
		//section 4
		List<Assignment> assignmentsSection4 = section4.getAssignments();
		Assert.assertEquals(2, assignmentsSection4.size());
		Assignment templateSynchedSection4a = assignmentsSection4.get(0).getTemplateReference();
		Assignment templateSynchedSection4b = assignmentsSection4.get(1).getTemplateReference();
		Assert.assertTrue(assignment2_2.equals(templateSynchedSection4a) || assignment2_2.equals(templateSynchedSection4b));
		Assert.assertTrue(assignment4_1.equals(templateSynchedSection4a) || assignment4_1.equals(templateSynchedSection4b));
		
		//check update of assignments
		Assert.assertEquals("Assignment 2 description", assignment2_1.getSummary());
		Assert.assertEquals("Assignment 4 description", assignment4_1.getSummary());
	}
	

	@Test
	public void syncBinder_moveAssignmentsInSection_multiple() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("port-u-10");
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("port-u-11");
		RepositoryEntry templateEntry = createTemplate(owner, "Template", "TE");
		dbInstance.commitAndCloseSession();
		
		//make 2 sections
		Binder templateBinder = portfolioService.getBinderByResource(templateEntry.getOlatResource());
		SectionRef sectionRef1 = portfolioService.getSections(templateBinder).get(0);
		dbInstance.commit();
		
		//make 4 assignments
		Section templateSection1 = portfolioService.getSection(sectionRef1);
		Assignment assignment_1_1 = portfolioService.addAssignment("1.1 Assignment", "", "", AssignmentType.essay, false, templateSection1, null, false, false, false, null);
		Assignment assignment_1_2 = portfolioService.addAssignment("1.2 Assignment", "", "", AssignmentType.essay, false, templateSection1, null, false, false, false, null);
		Assignment assignment_1_3 = portfolioService.addAssignment("1.3 Assignment", "", "", AssignmentType.essay, false, templateSection1, null, false, false, false, null);
		Assignment assignment_1_4 = portfolioService.addAssignment("1.4 Assignment", "", "", AssignmentType.essay, false, templateSection1, null, false, false, false, null);
		Assignment assignment_1_5 = portfolioService.addAssignment("1.5 Assignment", "", "", AssignmentType.essay, false, templateSection1, null, false, false, false, null);
		Assignment assignment_1_6 = portfolioService.addAssignment("1.6 Assignment", "", "", AssignmentType.essay, false, templateSection1, null, false, false, false, null);
		dbInstance.commit();
		List<Assignment> templateAssignments = portfolioService.getSectionsAssignments(templateBinder, null);
		Assert.assertEquals(6, templateAssignments.size());
		
		// a user take the binder and synched it a first time
		Binder binder = portfolioService.assignBinder(id, templateBinder, templateEntry, "74", null);
		dbInstance.commit();
		SynchedBinder synchedBinder = portfolioService.loadAndSyncBinder(binder);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(synchedBinder);
		Assert.assertEquals(binder, synchedBinder.getBinder());
		
		//start all assignments
		List<Assignment> assignments = portfolioService.getSectionsAssignments(binder, null);
		Assert.assertEquals(6, assignments.size());
		for(Assignment assignment:assignments) {
			portfolioService.startAssignment(assignment.getKey(), id);
			dbInstance.commit();
		}
		dbInstance.commit();
		
		// check that the student has it's 8 pages
		List<Page> pages = portfolioService.getPages(binder, null);
		Assert.assertEquals(6, pages.size());
		dbInstance.commit();

		// synched and check the sections order
		SynchedBinder synchedBinder2 = portfolioService.loadAndSyncBinder(binder);
		Binder freshBinder = synchedBinder2.getBinder();
		dbInstance.commitAndCloseSession();
		
		//ooops, someone deleted the assignment (simulate jump in the numbering of the list)
		List<Section> sections = portfolioService.getSections(freshBinder);
		List<Assignment> firstSectionAssignments = portfolioService.getSectionsAssignments(sections.get(0), null);
		dbInstance.getCurrentEntityManager().remove(firstSectionAssignments.get(1));
		dbInstance.getCurrentEntityManager().remove(firstSectionAssignments.get(2));
		dbInstance.getCurrentEntityManager().remove(firstSectionAssignments.get(3));
		dbInstance.getCurrentEntityManager().remove(firstSectionAssignments.get(5));
		dbInstance.commit();

		List<Assignment> firstSectionDeletedAssignments = portfolioService.getSectionsAssignments(sections.get(0), null);
		Assert.assertEquals(2, firstSectionDeletedAssignments.size());

		// synched and check the sections order
		SynchedBinder synchedBinder3 = portfolioService.loadAndSyncBinder(binder);
		Binder freshBinder3 = synchedBinder3.getBinder();
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(freshBinder3);
		
		List<Assignment> firstSectionAssignmentResynched = portfolioService.getSectionsAssignments(sections.get(0), null);
		List<Assignment> templateAssignmentsSynched= new ArrayList<>();
		Assert.assertEquals(6, firstSectionAssignmentResynched.size());
		for(Assignment firstSectionAssignment:firstSectionAssignmentResynched) {
			Assert.assertEquals(firstSectionAssignment.getTitle(), firstSectionAssignment.getTemplateReference().getTitle());
			templateAssignmentsSynched.add(firstSectionAssignment.getTemplateReference());
		}
		
		Assert.assertTrue(templateAssignmentsSynched.contains(assignment_1_1));
		Assert.assertTrue(templateAssignmentsSynched.contains(assignment_1_2));
		Assert.assertTrue(templateAssignmentsSynched.contains(assignment_1_3));
		Assert.assertTrue(templateAssignmentsSynched.contains(assignment_1_4));
		Assert.assertTrue(templateAssignmentsSynched.contains(assignment_1_5));
		Assert.assertTrue(templateAssignmentsSynched.contains(assignment_1_6));
	}
	
	@Test
	public void removeAssignment() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("port-u-10");
		RepositoryEntry templateEntry = createTemplate(owner, "Template", "TE");
		dbInstance.commitAndCloseSession();
			
		//make the binder and the section
		Binder templateBinder = portfolioService.getBinderByResource(templateEntry.getOlatResource());
		SectionRef sectionRef = portfolioService.getSections(templateBinder).get(0);
		dbInstance.commit();
			
		//make 4 assignments
		Section templateSection = portfolioService.getSection(sectionRef);
		Assignment assignment_1 = portfolioService.addAssignment("1 Assignment", "", "", AssignmentType.essay, false, templateSection, null, false, false, false, null);
		Assignment assignment_2 = portfolioService.addAssignment("2 Assignment", "", "", AssignmentType.essay, false, templateSection, null, false, false, false, null);
		Assignment assignment_3 = portfolioService.addAssignment("3 Assignment", "", "", AssignmentType.essay, false, templateSection, null, false, false, false, null);
		Assignment assignment_4 = portfolioService.addAssignment("3 Assignment", "", "", AssignmentType.essay, false, templateSection, null, false, false, false, null);
		dbInstance.commitAndCloseSession();
		
		boolean ok = portfolioService.deleteAssignment(assignment_3);
		Assert.assertTrue(ok);
		dbInstance.commitAndCloseSession();
		
		List<Assignment> assignments = portfolioService.getSection(sectionRef).getAssignments();
		Assert.assertNotNull(assignments);
		Assert.assertEquals(3, assignments.size());
		Assert.assertTrue(assignments.contains(assignment_1));
		Assert.assertTrue(assignments.contains(assignment_2));
		Assert.assertFalse(assignments.contains(assignment_3));
		Assert.assertTrue(assignments.contains(assignment_4));
	}
	
	@Test
	public void removeAssignment_usedOne() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("port-u-10");
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("port-u-11");
		RepositoryEntry templateEntry = createTemplate(owner, "Template", "TE");
		dbInstance.commitAndCloseSession();
		
		//make 2 sections
		Binder templateBinder = portfolioService.getBinderByResource(templateEntry.getOlatResource());
		SectionRef sectionRef = portfolioService.getSections(templateBinder).get(0);
		dbInstance.commit();
		
		//make 4 assignments
		Section templateSection = portfolioService.getSection(sectionRef);
		Assignment assignment_1 = portfolioService.addAssignment("1 Assignment", "", "", AssignmentType.essay, false, templateSection, null, false, false, false, null);
		Assignment assignment_2 = portfolioService.addAssignment("2 Assignment", "", "", AssignmentType.essay, false, templateSection, null, false, false, false, null);
		Assignment assignment_3 = portfolioService.addAssignment("3 Assignment", "", "", AssignmentType.essay, false, templateSection, null, false, false, false, null);
		Assignment assignment_4 = portfolioService.addAssignment("4 Assignment", "", "", AssignmentType.essay, false, templateSection, null, false, false, false, null);
		dbInstance.commit();
		List<Assignment> templateAssignments = portfolioService.getSectionsAssignments(templateBinder, null);
		Assert.assertEquals(4, templateAssignments.size());
		Assert.assertTrue(templateAssignments.contains(assignment_1));
		Assert.assertTrue(templateAssignments.contains(assignment_2));
		Assert.assertTrue(templateAssignments.contains(assignment_3));
		Assert.assertTrue(templateAssignments.contains(assignment_4));
		
		// synched and check the sections order
		Binder binder = portfolioService.assignBinder(id, templateBinder, templateEntry, null, null);
		SynchedBinder synchedBinder = portfolioService.loadAndSyncBinder(binder);
		binder = synchedBinder.getBinder();
		dbInstance.commitAndCloseSession();
		
		List<Assignment> assignments = portfolioService.getSectionsAssignments(binder, null);
		portfolioService.startAssignment(assignments.get(0).getKey(), id);
		portfolioService.startAssignment(assignments.get(1).getKey(), id);
		portfolioService.startAssignment(assignments.get(2).getKey(), id);
		portfolioService.startAssignment(assignments.get(3).getKey(), id);
		dbInstance.commit();
		
		List<Section> sections = portfolioService.getSections(binder);
		List<Page> pages = portfolioService.getPages(sections.get(0));
		Assert.assertEquals(4, pages.size());
		
		//delete an assignment
		boolean ok = portfolioService.deleteAssignment(assignment_3);
		Assert.assertTrue(ok);
		dbInstance.commitAndCloseSession();
		
		//sync the binder
		SynchedBinder reSynchedBinder = portfolioService.loadAndSyncBinder(binder);
		binder = reSynchedBinder.getBinder();
		dbInstance.commitAndCloseSession();
		
		//deleting an assignment doesn't delete the pages
		List<Page> allPages = portfolioService.getPages(sections.get(0));
		Assert.assertEquals(4, allPages.size());
		
		//sync twice
		SynchedBinder reReSynchedBinder = portfolioService.loadAndSyncBinder(binder);
		binder = reReSynchedBinder.getBinder();
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void deleteBinder() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("del-binder-");
		Binder binder = portfolioService.createNewBinder("Binder to delete", "Deletion", "", owner);
		SectionRef sectionRef1 = portfolioService.appendNewSection("1. section ", "Section 1", null, null, binder);
		dbInstance.commit();
		SectionRef sectionRef2 = portfolioService.appendNewSection("2. section ", "Section 2", null, null, binder);
		dbInstance.commit();
		portfolioService.updateBinderUserInformations(binder, owner);
		dbInstance.commit();
		
		Section reloadedSection1 = portfolioService.getSection(sectionRef1);
		Page page1 = portfolioService.appendNewPage(owner, "New page", "A brand new page.", null, null, reloadedSection1);
		Section reloadedSection2 = portfolioService.getSection(sectionRef2);
		Page page2 = portfolioService.appendNewPage(owner, "New page", "A brand new page.", null, null, reloadedSection2);
		Assert.assertNotNull(page1);
		Assert.assertNotNull(page2);
		dbInstance.commitAndCloseSession();
		
		//delete
		boolean deleted = portfolioService.deleteBinder(binder);
		dbInstance.commit();
		Assert.assertTrue(deleted);
	}
	
	@Test
	public void deleteTemplateBinder() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("port-u-20");
		RepositoryEntry templateEntry = createTemplate(owner, "Template", "TE");
		dbInstance.commitAndCloseSession();

		Binder templateBinder = portfolioService.getBinderByResource(templateEntry.getOlatResource());
		List<SectionRef> sections = new ArrayList<>();
		for(int i=0; i<10; i++) {
			SectionRef templateSectionRef = portfolioService.appendNewSection(i + ". section ", "Section " + i, null, null, templateBinder);
			dbInstance.commit();
			sections.add(templateSectionRef);
			
			Section templateSection = portfolioService.getSection(templateSectionRef);
			for(int j=0; j<10; j++) {
				Assignment assignment = portfolioService.addAssignment(i + "_" + j + " Assignment", "", "", AssignmentType.essay, false, templateSection, null, false, false, false, null);
				Assert.assertNotNull(assignment);
			}
			dbInstance.commit();
		}
		
		//collect the page
		List<Page> assignmentPages = new ArrayList<>();
		for(int k=0; k<10; k++) {
			// get a binder from the template
			Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("port-u-13" + k);
			Binder binder = portfolioService.assignBinder(id, templateBinder, templateEntry, null, null);
			SynchedBinder synchedBinder = portfolioService.loadAndSyncBinder(binder);
			binder = synchedBinder.getBinder();
			dbInstance.commitAndCloseSession();
			
			List<Assignment> assignments = portfolioService.getSectionsAssignments(binder, null);
			for(Assignment assignment:assignments) {
				Assignment startedAssignment = portfolioService.startAssignment(assignment.getKey(), id);
				Assert.assertNotNull(startedAssignment);
				
				Page page = startedAssignment.getPage();
				assignmentPages.add(page);
				Assert.assertNotNull(page.getBody());
				dbInstance.commit();
			}
			dbInstance.commitAndCloseSession();
		}
		
		//delete
		boolean deleted = portfolioService.deleteBinderTemplate(templateBinder, templateEntry);
		dbInstance.commit();
		Assert.assertTrue(deleted);
		
		//check that the pages exists
		Assert.assertFalse(assignmentPages.isEmpty());
		for(Page page:assignmentPages) {
			Page reloadedPage = portfolioService.getPageByKey(page.getKey());
			Assert.assertNotNull(reloadedPage);
			Section section = reloadedPage.getSection();
			Assert.assertNotNull(section);
		}
	}
	
	@Test
	public void deleteSynchedBinder() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("port-u-12");
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("port-u-13");
		RepositoryEntry templateEntry = createTemplate(owner, "Template", "TE");
		dbInstance.commitAndCloseSession();
		
		//get section
		Binder templateBinder = portfolioService.getBinderByResource(templateEntry.getOlatResource());
		SectionRef sectionRef = portfolioService.getSections(templateBinder).get(0);
		dbInstance.commit();
		
		//make 2 assignments
		Section templateSection = portfolioService.getSection(sectionRef);
		Assignment assignment_1 = portfolioService.addAssignment("1 Assignment", "", "", AssignmentType.essay, false, templateSection, null, false, false, false, null);
		Assignment assignment_2 = portfolioService.addAssignment("2 Assignment", "", "", AssignmentType.essay, false, templateSection, null, false, false, false, null);
		dbInstance.commit();
		List<Assignment> templateAssignments = portfolioService.getSectionsAssignments(templateBinder, null);
		Assert.assertEquals(2, templateAssignments.size());
		
		// synched and check the sections order
		Binder binder = portfolioService.assignBinder(id, templateBinder, templateEntry, null, null);
		SynchedBinder synchedBinder = portfolioService.loadAndSyncBinder(binder);
		binder = synchedBinder.getBinder();
		dbInstance.commitAndCloseSession();
		
		List<Assignment> assignments = portfolioService.getSectionsAssignments(binder, null);
		Assignment startedAssignment_1 = portfolioService.startAssignment(assignments.get(0).getKey(), id);
		Assignment startedAssignment_2 = portfolioService.startAssignment(assignments.get(1).getKey(), id);
		Long startedPageKey_1 = startedAssignment_1.getPage().getKey();
		Long startedPageKey_2 = startedAssignment_2.getPage().getKey();
		dbInstance.commitAndCloseSession();
		
		//add some comments
		OLATResourceable startedPageOres_1 = OresHelper.createOLATResourceableInstance(Page.class, startedPageKey_1);
		userCommentsDao.createComment(id, startedPageOres_1, null, "Hello");

		//delete
		boolean deleted = portfolioService.deleteBinder(binder);
		dbInstance.commit();
		Assert.assertTrue(deleted);
		
		//check that the template is save
		Assignment reloadAssignment_1 = assignmentDao.loadAssignmentByKey(assignment_1.getKey());
		Assert.assertNotNull(reloadAssignment_1);
		Assignment reloadAssignment_2 = assignmentDao.loadAssignmentByKey(assignment_2.getKey());
		Assert.assertNotNull(reloadAssignment_2);
		
		//check that the binder is really deleted
		Binder reloadedBinder = binderDao.loadByKey(binder.getKey());
		Assert.assertNull(reloadedBinder);
		
		Assignment reloadStartedAssignment_1 = assignmentDao.loadAssignmentByKey(startedAssignment_1.getKey());
		Assert.assertNull(reloadStartedAssignment_1);
		Assignment reloadStartedAssignment_2 = assignmentDao.loadAssignmentByKey(startedAssignment_2.getKey());
		Assert.assertNull(reloadStartedAssignment_2);
		
		Page reloadedStartedPage_1 = pageDao.loadByKey(startedPageKey_1);
		Assert.assertNull(reloadedStartedPage_1);
		Page reloadedStartedPage_2 = pageDao.loadByKey(startedPageKey_2);
		Assert.assertNull(reloadedStartedPage_2);
	}
	
	@Test
	public void deleteSectionWithPages() {
		// prepare a binder with 2 sections and some pages
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("del-binder-");
		Binder binder = portfolioService.createNewBinder("Binder to delete", "Deletion", "", owner);
		SectionRef sectionRef1 = portfolioService.appendNewSection("1. section ", "Section 1", null, null, binder);
		dbInstance.commit();
		SectionRef sectionRef2 = portfolioService.appendNewSection("2. section ", "Section 2", null, null, binder);
		dbInstance.commit();
		portfolioService.updateBinderUserInformations(binder, owner);
		dbInstance.commit();
		
		Section reloadedSection1 = portfolioService.getSection(sectionRef1);
		List<Page> pagesSection1 = new ArrayList<>();
		for(int i=0; i<10; i++) {
			Page page = portfolioService.appendNewPage(owner, "New page", "A brand new page.", null, null, reloadedSection1);
			pagesSection1.add(page);
		}
		
		Section reloadedSection2 = portfolioService.getSection(sectionRef2);
		Page page2 = portfolioService.appendNewPage(owner, "New page", "A brand new page.", null, null, reloadedSection2);
		
		Assert.assertNotNull(page2);
		dbInstance.commitAndCloseSession();
	
		// delete the section
		portfolioService.deleteSection(binder, reloadedSection1);
		dbInstance.commit();
		
		//check if section 2 is still around
		Section section2 = binderDao.loadSectionByKey(sectionRef2.getKey());
		Assert.assertEquals(reloadedSection2, section2);
		Page reloadedPage2 = pageDao.loadByKey(page2.getKey());
		Assert.assertNotNull(reloadedPage2);
		Assert.assertEquals(page2, reloadedPage2);
		
		// check if section 1 is deleted
		Section deletedSection1 = binderDao.loadSectionByKey(sectionRef1.getKey());
		Assert.assertNull(deletedSection1);
		for(Page pageSection1:pagesSection1) {
			Page deletedPage = pageDao.loadByKey(pageSection1.getKey());
			Assert.assertNull(deletedPage);
		}
	}
	
	/**
	 * Check if we can delete a section where there is an error of numbering
	 * in the list of pages.
	 * 
	 */
	@Test
	public void deleteSectionWithPages_errorInNumbering() {
		// prepare a binder with 2 sections and some pages
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("del-binder-");
		Binder binder = portfolioService.createNewBinder("Binder to delete", "Deletion", "", owner);
		SectionRef sectionRef = portfolioService.appendNewSection("1. section ", "Section 1", null, null, binder);
		dbInstance.commit();
		portfolioService.updateBinderUserInformations(binder, owner);
		dbInstance.commit();
		
		Section reloadedSection = portfolioService.getSection(sectionRef);
		List<Page> pagesSection = new ArrayList<>();
		for(int i=0; i<10; i++) {
			Page page = portfolioService.appendNewPage(owner, "New page", "A brand new page.", null, null, reloadedSection);
			pagesSection.add(page);
		}
		dbInstance.commitAndCloseSession();
		
		//simulate a gap in numbering of the list
		Page pageToDelete = dbInstance.getCurrentEntityManager().getReference(PageImpl.class, pagesSection.get(4).getKey());
		dbInstance.getCurrentEntityManager().remove(pageToDelete);
		dbInstance.commitAndCloseSession();

		// delete the section
		portfolioService.deleteSection(binder, reloadedSection);
		dbInstance.commit();
		
		//check if the section and the pages are deleted
		Section deletedSection = binderDao.loadSectionByKey(sectionRef.getKey());
		Assert.assertNull(deletedSection);
		Page deletedPage = pageDao.loadByKey(pagesSection.get(4).getKey());
		Assert.assertNull(deletedPage);
		List<Page> deletedPages = pageDao.getPages(sectionRef);
		Assert.assertNotNull(deletedPages);
		Assert.assertTrue(deletedPages.isEmpty());
	}
	
	/**
	 * Check if the delete user method do its job.
	 * 
	 */
	@Test
	public void deleteUser() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("port-u-21");
		RepositoryEntry templateEntry = createTemplate(owner, "Template", "TE");
		dbInstance.commitAndCloseSession();

		// the user make a template
		Binder templateBinder = portfolioService.getBinderByResource(templateEntry.getOlatResource());
		SectionRef templateSectionRef = portfolioService.appendNewSection("1. section ", "Section 1", null, null, templateBinder);
		dbInstance.commit();
		Section templateSection = portfolioService.getSection(templateSectionRef);
		Assignment assignment = portfolioService.addAssignment("1 Assignment", "", "", AssignmentType.essay, false, templateSection, null, false, false, false, null);
		Assert.assertNotNull(assignment);
		dbInstance.commit();

		// the user use the template
		Binder binder = portfolioService.assignBinder(owner, templateBinder, templateEntry, null, null);
		SynchedBinder synchedBinder = portfolioService.loadAndSyncBinder(binder);
		binder = synchedBinder.getBinder();
		dbInstance.commitAndCloseSession();
		
		// add a media to a page
		Section reloadedSection = portfolioService.getSections(binder).get(0);
		Page page = pageDao.createAndPersist("Page 1", "A page with content.", null, null, true, reloadedSection, null);
		Media media = mediaDao.createMedia("To delete", "Binder", "A media to delete promptly", TextHandler.TEXT_MEDIA, "[Media:0]", null, 10, owner);
		dbInstance.commitAndCloseSession();
		MediaPart mediaPart = new MediaPart();
		mediaPart.setMedia(media);
		PageBody reloadedBody = pageDao.loadPageBodyByKey(page.getBody().getKey());
		pageDao.persistPart(reloadedBody, mediaPart);
		dbInstance.commitAndCloseSession();
		
		// add some markers
		Identity permanentUser = JunitTestHelper.createAndPersistIdentityAsRndUser("port-u-22");
		Media permanentMedia = mediaDao.createMedia("Permanent", "Binder", "A media to stay", TextHandler.TEXT_MEDIA, "[Media:0]", null, 10, permanentUser);
		Binder permanentBinder = portfolioService.assignBinder(permanentUser, templateBinder, templateEntry, null, null);
		dbInstance.commitAndCloseSession();
		
		// delete the user
		userLifecycleManager.deleteIdentity(owner, null);
		
		// the template is a learn ressource and will not be deleted
		Binder reloadedtemplateBinder = portfolioService.getBinderByKey(templateBinder.getKey());
		Assert.assertNotNull(reloadedtemplateBinder);
		// the binder 
		Binder deletedBinder = portfolioService.getBinderByKey(binder.getKey());
		Assert.assertNull(deletedBinder);
		// the media
		Media deletedMedia = portfolioService.getMediaByKey(media.getKey());
		Assert.assertNull(deletedMedia);
		
		// check that the method doesn't delete stuff of other users
		Binder reloadedPermanentBinder = portfolioService.getBinderByKey(permanentBinder.getKey());
		Assert.assertNotNull(reloadedPermanentBinder);
		// the media
		Media reloadedPermanentMedia = portfolioService.getMediaByKey(permanentMedia.getKey());
		Assert.assertNotNull(reloadedPermanentMedia);
	}
	
	@Test
	public void getCategoriesAndUsage() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("category-owner");
		Binder binder = portfolioService.createNewBinder("Binder Category", "Binder with two sections.", null, owner);
		dbInstance.commitAndCloseSession();
		portfolioService.appendNewSection("First section", "My first section.", null, null, binder);
		dbInstance.commitAndCloseSession();
		portfolioService.appendNewSection("Second section", "My second section.", null, null, binder);
		dbInstance.commitAndCloseSession();
		
		Category category1 = categoryDao.createAndPersistCategory("Cat1");
		dbInstance.commitAndCloseSession();
		Category category2 = categoryDao.createAndPersistCategory("Cat2");
		dbInstance.commitAndCloseSession();
		
		List<Section> sections = portfolioService.getSections(binder);
		List<String> categoriesList1 = new ArrayList<>();
		categoriesList1.add(category1.getName());
		List<String> categoriesList2 = new ArrayList<>();
		categoriesList2.add(category1.getName());
		categoriesList2.add(category2.getName());
		
		Section section1 = sections.get(0);
		Page page11 = portfolioService.appendNewPage(owner, "Page-1-1", "", null, null, section1);
		
		portfolioService.updateCategories(page11, categoriesList1);
		dbInstance.commitAndCloseSession();
		
		Page page12 = portfolioService.appendNewPage(owner, "Page-1-2", "", null, null, section1);
		portfolioService.updateCategories(page12, categoriesList2);
		
		Section section2 = sections.get(1);
		Page page21 = portfolioService.appendNewPage(owner, "Page-2-1", "", null, null, section2);
		
		portfolioService.updateCategories(page21, categoriesList1);
		dbInstance.commitAndCloseSession();
		
		Page page22 = portfolioService.appendNewPage(owner, "Page-2-2", "", null, null, section2);
		portfolioService.updateCategories(page22, categoriesList2);
		
		Map<Category, Long> categoryUsageSection1 = portfolioService.getCategoriesAndUsage(section1);
		Map<Category, Long> categoryUsageSection2 = portfolioService.getCategoriesAndUsage(section2);
		
		Assert.assertNotNull(categoryUsageSection1);
		Assert.assertEquals(Long.valueOf(2), categoryUsageSection1.get(category1));
		Assert.assertEquals(Long.valueOf(1), categoryUsageSection1.get(category2));
		
		Assert.assertNotNull(categoryUsageSection2);
		Assert.assertEquals(Long.valueOf(2), categoryUsageSection2.get(category1));
		Assert.assertEquals(Long.valueOf(1), categoryUsageSection2.get(category2));
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

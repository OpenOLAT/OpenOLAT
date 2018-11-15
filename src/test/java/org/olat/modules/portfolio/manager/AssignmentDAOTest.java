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

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.portfolio.Assignment;
import org.olat.modules.portfolio.AssignmentStatus;
import org.olat.modules.portfolio.AssignmentType;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.Section;
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
}
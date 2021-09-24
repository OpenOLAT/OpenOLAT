/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 

package org.olat.course.nodes.projectbroker;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.SecurityGroupImpl;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.course.nodes.projectbroker.datamodel.Project;
import org.olat.course.nodes.projectbroker.datamodel.ProjectBroker;
import org.olat.course.nodes.projectbroker.datamodel.ProjectEvent;
import org.olat.course.nodes.projectbroker.service.ProjectBrokerManager;
import org.olat.course.nodes.projectbroker.service.ProjectBrokerModuleConfiguration;
import org.olat.course.nodes.projectbroker.service.ProjectGroupManager;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupImpl;
import org.olat.group.BusinessGroupService;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *   
 * @author Christian Guretzki
 */

public class ProjectBrokerManagerTest extends OlatTestCase {
	private static final Logger log = Tracing.createLoggerFor(ProjectBrokerManagerTest.class);

	private static Identity id1;
	private static Identity id2;
	private static Long resourceableId;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private ProjectGroupManager projectGroupManager;
	@Autowired
	private ProjectBrokerManager projectBrokerManager;

	@Before
	public void setup() throws Exception {
		try {
			id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("project-id1");
			id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("project-id2");

			if (resourceableId == null) {
				Identity author = JunitTestHelper.createAndPersistIdentityAsUser("project-auth-" + UUID.randomUUID());
				RepositoryEntry repositoryEntry = JunitTestHelper.deployBasicCourse(author);
				resourceableId = repositoryEntry.getOlatResource().getResourceableId();
				log.info("Demo course imported - resourceableId: " + resourceableId);
			}
			DBFactory.getInstance().closeSession();
		} catch (Exception e) {
			log.error("", e);
			fail(e.getMessage());
		}
	}

	@Test
	public void testCreateListDeleteProjects() throws Exception {
		// create ProjectBroker A + B
		ProjectBroker projectBrokerA = projectBrokerManager.createAndSaveProjectBroker();
		Long idProjectBrokerA = projectBrokerA.getKey();
		ProjectBroker projectBrokerB = projectBrokerManager.createAndSaveProjectBroker();
		Long idProjectBrokerB = projectBrokerB.getKey();
		// add project to ProjectBroker A
		createProject("thema A1", id1, idProjectBrokerA, resourceableId );
		createProject("thema A2", id1, idProjectBrokerA, resourceableId );
		// add project to ProjectBroker B
		createProject("thema B1", id1, idProjectBrokerB, resourceableId );
		createProject("thema B2", id1, idProjectBrokerB, resourceableId );
		dbInstance.commitAndCloseSession();
		
		// get project list and check content
		List<Project> projectListA = projectBrokerManager.getProjectListBy(idProjectBrokerA);
		assertEquals("Wrong projectList.size for project-broker A",2, projectListA.size());
		assertTrue("Wrong thema in project list A, title must start with 'thema A'", projectListA.get(0).getTitle().startsWith("thema A"));
		assertTrue("Wrong thema in project list A, title must start with 'thema A'", projectListA.get(1).getTitle().startsWith("thema A"));
		
		List<Project> projectListB = projectBrokerManager.getProjectListBy(idProjectBrokerB);
		assertEquals("Wrong projectList.size for project-broker B",2, projectListB.size());
		assertTrue("Wrong thema in project list B, title must start with 'thema B'", projectListB.get(0).getTitle().startsWith("thema B"));
		assertTrue("Wrong thema in project list B, title must start with 'thema B'", projectListB.get(1).getTitle().startsWith("thema B"));
		
		if (projectListA.get(0).getTitle().equals("thema A1")) {
			assertEquals("Wrong thema in project list A, title must be 'thema A2'", "thema A2", projectListA.get(1).getTitle());
		} else if (projectListA.get(0).getTitle().equals("thema A2")) {
			assertEquals("Wrong thema in project list A, title must be 'thema A1'", "thema A1", projectListA.get(1).getTitle());
		}
		if (projectListB.get(0).getTitle().equals("thema B1")) {
			assertEquals("Wrong thema in project list B, title must be 'thema B2'", "thema B2", projectListB.get(1).getTitle());
		} else if (projectListB.get(0).getTitle().equals("thema B2")) {
			assertEquals("Wrong thema in project list B, title must be 'thema B1'", "thema B1", projectListB.get(1).getTitle());
		}
		
		// delete project 
		long candiadteGroupKey = projectListA.get(0).getCandidateGroup().getKey();
		long projectGroupKey = projectListA.get(0).getProjectGroup().getKey();
		assertNotNull("CandidateGroup does not exist before delete project", dbInstance.getCurrentEntityManager().find(SecurityGroupImpl.class, candiadteGroupKey));
		assertNotNull("ProjectGroup does not exist before delete project", dbInstance.getCurrentEntityManager().find(BusinessGroupImpl.class, projectGroupKey));
		projectBrokerManager.deleteProject(projectListA.get(0), true, null, null, null);
		assertNull("CandidateGroup still exists after delete project", dbInstance.getCurrentEntityManager().find(SecurityGroupImpl.class, candiadteGroupKey));
		assertNull("ProjectGroup still exists after delete project", dbInstance.getCurrentEntityManager().find(BusinessGroupImpl.class, projectGroupKey));

		// get project list and check content
		projectListA = projectBrokerManager.getProjectListBy(idProjectBrokerA);
		projectListB = projectBrokerManager.getProjectListBy(idProjectBrokerB);
		assertEquals("Wrong projectList.size for project-broker A after delete 'thema A1'",1, projectListA.size());
		assertEquals("Wrong projectList.size for project-broker B after delete 'thema A1'",2, projectListB.size());
		// delete project 
		projectBrokerManager.deleteProject(projectListB.get(1), true, null, null, null);
		// get project list and check content
		projectListA = projectBrokerManager.getProjectListBy(idProjectBrokerA);
		projectListB = projectBrokerManager.getProjectListBy(idProjectBrokerB);
		assertEquals("Wrong projectList.size for project-broker A after delete 'thema B2'",1, projectListA.size());
		assertEquals("Wrong projectList.size for project-broker B after delete 'thema B2'",1, projectListB.size());
		
		// delete project
		projectBrokerManager.deleteProject(projectListA.get(0), true, null, null, null);
		projectListA = projectBrokerManager.getProjectListBy(idProjectBrokerA);
		projectListB = projectBrokerManager.getProjectListBy(idProjectBrokerB);
		log.info("testCreateListDeleteProjects: projectListA=" + projectListA);
		assertEquals("Wrong projectList.size for project-broker A after delete all thema",0, projectListA.size());
		assertEquals("Wrong projectList.size for project-broker B after delete all thema",1, projectListB.size());
	}

	@Test
	public void testPerformanceGetProjectList() throws Exception {
		int FIRST_ITERATION = 10;
		int SECOND_ITERATION = 90;
		int THIRD_ITERATION = 400;
		// create ProjectBroker C
		ProjectBroker projectBrokerC = projectBrokerManager.createAndSaveProjectBroker();
		Long idProjectBrokerC = projectBrokerC.getKey();
		dbInstance.closeSession();
		for (int i = 0; i < FIRST_ITERATION; i++) {
			createProject("thema C1_" + i, id1, idProjectBrokerC, resourceableId );		
		}
		dbInstance.closeSession();
		
		long startTime = System.currentTimeMillis();		
		List<Project> projectListC = projectBrokerManager.getProjectListBy(idProjectBrokerC);
		long endTime = System.currentTimeMillis();
		assertEquals("Wrong projectList.size for project-broker C after first iteration",FIRST_ITERATION, projectListC.size());
		long duration = endTime - startTime; 
		log.info("getProjectListBy takes " + duration + "ms with " + FIRST_ITERATION + " projects");

		for (int i = 0; i < SECOND_ITERATION; i++) {
			createProject("thema C1_" + i, id1, idProjectBrokerC, resourceableId );			
		}
		dbInstance.closeSession();
		
		startTime = System.currentTimeMillis();
		projectListC = projectBrokerManager.getProjectListBy(idProjectBrokerC);
		endTime = System.currentTimeMillis();
		int numberOfProjects = FIRST_ITERATION + SECOND_ITERATION;
		assertEquals("Wrong projectList.size for project-broker C", numberOfProjects, projectListC.size());
		duration = endTime - startTime; 
		log.info("getProjectListBy takes " + duration + "ms with " + numberOfProjects + " projects");

		for (int i = 0; i < THIRD_ITERATION; i++) {
			createProject("thema C1_" + i, id1, idProjectBrokerC, resourceableId );			
		}
		dbInstance.closeSession();
		
		startTime = System.currentTimeMillis();
		projectListC = projectBrokerManager.getProjectListBy(idProjectBrokerC);
		endTime = System.currentTimeMillis();
		numberOfProjects = FIRST_ITERATION + SECOND_ITERATION + THIRD_ITERATION;
		assertEquals("Wrong projectList.size for project-broker C", numberOfProjects, projectListC.size());
		duration = endTime - startTime; 
		log.info("getProjectListBy takes " + duration + "ms with " + numberOfProjects + " projects");
	}

	@Test
	public void testPerformanceTableModel() throws Exception {
		int ITERATION = 300;
		int START_PAGE_INDEX = 100;
		int PAGE_SIZE = 20;
		Translator translator = Util.createPackageTranslator(this.getClass(), Locale.GERMAN);

		ProjectBroker projectBrokerD = projectBrokerManager.createAndSaveProjectBroker();
		Long idProjectBrokerD = projectBrokerD.getKey();
		ProjectBrokerModuleConfiguration moduleConfig = new ProjectBrokerModuleConfiguration( new ModuleConfiguration() );

		for (int i = 0; i < ITERATION; i++) {
			createProject("thema D1_" + i, id1, idProjectBrokerD, resourceableId );			
		}
		List<Project> projectListD = projectBrokerManager.getProjectListBy(idProjectBrokerD);
		ProjectListTableModel tableModel = new ProjectListTableModel(projectListD, id1, translator, moduleConfig, 0, 0, 0, false);
		
		// loop over table like rendering loop
		long startTime = System.currentTimeMillis();
		for (int row = START_PAGE_INDEX; row < START_PAGE_INDEX+PAGE_SIZE; row++) {
			for (int col = 0; col < tableModel.getColumnCount(); col++) {
				Object element = tableModel.getValueAt(row, col);
				Assert.assertNotNull(element);
			}
		}
		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime; 
		log.info("tableModel.getValueAt(row, col) for " + PAGE_SIZE + "elements (of " + ITERATION + ") takes " + duration + "ms with " + ITERATION + " projects");
	}

	@Test
	public void testIsProjectManager() throws Exception {
		ProjectBroker projectBrokerD = projectBrokerManager.createAndSaveProjectBroker();
		Long idProjectBrokerD = projectBrokerD.getKey();
		
		Project testProjectA = createProject("thema A", id1, idProjectBrokerD, resourceableId );
		List<Identity> projectManagerList = new ArrayList<>();
		projectManagerList.add(id1);
		Project testProjectB = createProject("thema B", id2, idProjectBrokerD, resourceableId );
		// check project leader in ProjectA
		assertTrue("Must be project-leader of project A", projectGroupManager.isProjectManager(id1, testProjectA));
		assertFalse("Can not be project leader of project B",projectGroupManager.isProjectManager(id1, testProjectB));
		assertTrue("Must be project-leader of project A", projectGroupManager.isProjectManager(id2, testProjectB));

		CoreSpringFactory.getImpl(BusinessGroupService.class).removeOwners(id1, projectManagerList, testProjectA.getProjectGroup());
		// check no project leader anymore
		assertFalse("Can not be project leader of project A",projectGroupManager.isProjectManager(id1, testProjectA));
		assertFalse("Can not be project leader of project B",projectGroupManager.isProjectManager(id1, testProjectB));
	}

	@Test
	public void testExistsProject() throws Exception {
		// 1. test project does not exists
		assertFalse("Wrong return value true, project does not exist", projectBrokerManager.existsProject(39927492743L));
		// 2. test project exists
		ProjectBroker projectBrokerD = projectBrokerManager.createAndSaveProjectBroker();
		Long idProjectBrokerD = projectBrokerD.getKey();
		Project testProjectA = createProject("thema existsProject-Test", id1, idProjectBrokerD, resourceableId );
		dbInstance.closeSession();
		assertTrue("Wrong return value false, project exists", projectBrokerManager.existsProject(testProjectA.getKey()));		
	}
	
	@Test
	public void getProjectsWith() {
		ProjectBroker projectBroker = projectBrokerManager.createAndSaveProjectBroker();
		BusinessGroup projectGroup = projectGroupManager
				.createProjectGroupFor(projectBroker.getKey(), id1, "getProjectsWith", "getProjectsWithGroupDescription", resourceableId);
		Project project = projectBrokerManager
				.createAndSaveProjectFor("getProjectsWith", "getProjectsWith", projectBroker.getKey(), projectGroup);
		dbInstance.commitAndCloseSession();
		
		List<Project> projects = projectBrokerManager.getProjectsWith(projectGroup);
		Assert.assertNotNull(projects);
		Assert.assertEquals(1, projects.size());
		Assert.assertEquals(project, projects.get(0));
	}

	@Test
	public void testUpdateProject() throws Exception {
		ProjectBroker projectBroker = projectBrokerManager.createAndSaveProjectBroker();
		Long idProjectBroker = projectBroker.getKey();
		Project testProjectA = createProject("updateTest", id1, idProjectBroker, resourceableId );
		DBFactory.getInstance().closeSession();
		// testProjectA is now a detached-object
		// Update 1
		String updateTitle = "thema updateProject-Test update1";
		testProjectA.setTitle(updateTitle);
		String updateDescription = "description update1";
		testProjectA.setDescription(updateDescription);
		String updateState = "state update1";
		testProjectA.setState(updateState);
		projectBrokerManager.updateProject(testProjectA);
		dbInstance.closeSession();
		// testProjectA is now a detached-object again
		Project reloadedProject = (Project) dbInstance.loadObject(testProjectA, true);
		assertEquals("Wrong updated title 1",updateTitle,reloadedProject.getTitle());
		// Update 2
		String updateTitle2 = "thema updateProject-Test update2";
		testProjectA.setTitle(updateTitle2);
		int updateMaxMembers = 3;
		testProjectA.setMaxMembers(updateMaxMembers);
		String updateAttachmentFileName = "attachmentFile.txt";
		testProjectA.setAttachedFileName(updateAttachmentFileName);
		boolean updateMailNotification = Boolean.TRUE;
		testProjectA.setMailNotificationEnabled(updateMailNotification);
		String updateCustomField0 = "CustomField0";
		testProjectA.setCustomFieldValue(0, updateCustomField0);
		String updateCustomField1 = "CustomField1";
		testProjectA.setCustomFieldValue(1, updateCustomField1);
		projectBrokerManager.updateProject(testProjectA);
		dbInstance.closeSession();
		
		// Update 3
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.set(2010, 11, 15, 15, 30, 45);
		Date startDate = cal.getTime();
		cal.clear();
		cal.set(2010, 11, 20, 15, 30, 45);
		Date endDate = cal.getTime();
		ProjectEvent projectEventEnroll = new ProjectEvent(Project.EventType.ENROLLMENT_EVENT, startDate, endDate);
		testProjectA.setProjectEvent(projectEventEnroll);
		ProjectEvent projectEventHandout = new ProjectEvent(Project.EventType.HANDOUT_EVENT, startDate, endDate);
		testProjectA.setProjectEvent(projectEventHandout);
		dbInstance.closeSession();
		
		reloadedProject = (Project) DBFactory.getInstance().loadObject(testProjectA, true);
		assertEquals("Wrong updated title 2",updateTitle2,reloadedProject.getTitle());
		assertEquals("Wrong description",updateDescription,reloadedProject.getDescription());
		assertEquals("Wrong state",updateState,reloadedProject.getState());
		assertEquals("Wrong maxMembers",updateMaxMembers,reloadedProject.getMaxMembers());
		assertEquals("Wrong AttachmentFileName",updateAttachmentFileName,reloadedProject.getAttachmentFileName());
		assertEquals("Wrong MailNotification",updateMailNotification,reloadedProject.isMailNotificationEnabled());
		assertEquals("Wrong CustomField 0",updateCustomField0,reloadedProject.getCustomFieldValue(0));
		assertEquals("Wrong CustomField 1",updateCustomField1,reloadedProject.getCustomFieldValue(1));
		assertEquals("Wrong customField Size",2,reloadedProject.getCustomFieldSize());
		assertEquals("Wrong event Type (Handout)",Project.EventType.HANDOUT_EVENT,reloadedProject.getProjectEvent(Project.EventType.HANDOUT_EVENT).getEventType());
		assertEquals("Wrong event start-date (Handout)",startDate.getTime(),reloadedProject.getProjectEvent(Project.EventType.HANDOUT_EVENT).getStartDate().getTime());
		assertEquals("Wrong event end-date (Handout)",endDate.getTime(),reloadedProject.getProjectEvent(Project.EventType.HANDOUT_EVENT).getEndDate().getTime());
		assertEquals("Wrong event Type (Enroll)",Project.EventType.ENROLLMENT_EVENT,reloadedProject.getProjectEvent(Project.EventType.ENROLLMENT_EVENT).getEventType());
		assertEquals("Wrong event start-date (Enroll)",startDate.getTime(),reloadedProject.getProjectEvent(Project.EventType.ENROLLMENT_EVENT).getStartDate().getTime());
		assertEquals("Wrong event end-date (Enroll)",endDate.getTime(),reloadedProject.getProjectEvent(Project.EventType.ENROLLMENT_EVENT).getEndDate().getTime());
	}
	
	private Project createProject(String projectName, Identity creator, Long projectBrokerId, Long courseId) {
		BusinessGroup projectGroup = projectGroupManager.createProjectGroupFor(projectBrokerId, creator, projectName + "_Group", projectName + "GroupDescription", courseId);
		return projectBrokerManager.createAndSaveProjectFor(projectName + "title", projectName + "description1", projectBrokerId, projectGroup);
	}
}


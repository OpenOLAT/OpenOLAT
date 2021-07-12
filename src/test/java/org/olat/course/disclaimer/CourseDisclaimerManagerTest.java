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
*/
package org.olat.course.disclaimer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.config.CourseConfig;
import org.olat.course.config.CourseConfigManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/* 
 * Date: 20 Mar 2020<br>
 * @author aboeckle, alexander.boeckle@frentix.com
 */
public class CourseDisclaimerManagerTest extends OlatTestCase {
	
	private String disclaimer1Title = "Course 1 Title";
	private String disclaimer1Terms = "Course 1 Terms";
	private String disclaimer1Label1 = "Course 1 Label 1";
	private String disclaimer1Label2 = "Course 1 Label 2";
	
	private String disclaimer2Title = "Course 2 Title";
	private String disclaimer2Terms = "Course 2 Terms";
	private String disclaimer2Label1 = "Course 2 Label 1";
	private String disclaimer2Label2 = "Course 2 Label 2";
	
	private Identity id1;
	private Roles roles1;
	private Identity id2;
	private Roles roles2;

	private RepositoryEntry repositoryEntry;
	private ICourse course;
		
	@Autowired
	private DB dbInstance;
	@Autowired
	private CourseDisclaimerManager courseDisclaimerManager;
	@Autowired
	private CourseConfigManager courseConfigManager;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private OLATResourceManager resourceManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private BaseSecurityManager baseSecurityManager;
	
	
	@Before
	public void initCourse() {
		// create course and persist as OLATResourceImpl
		OLATResource resource = resourceManager.createOLATResourceInstance(CourseModule.class);

		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		repositoryEntry = repositoryService.create(null, "UnitTester", "-", "JUnit course disclaimer configuration course", "A JUnit course disclaimer test course",
				resource, RepositoryEntryStatusEnum.trash, defOrganisation);
		course = CourseFactory.createCourse(repositoryEntry, "Course Disclaimer Test", "Course Disclaimer Test Course");
		
		id1 = JunitTestHelper.createAndPersistIdentityAsUser("id1");
		roles1 = baseSecurityManager.getRoles(id1);
		id2 = JunitTestHelper.createAndPersistIdentityAsUser("id2");
		roles2 = baseSecurityManager.getRoles(id2);
		dbInstance.commitAndCloseSession();
	}
	
	private void initDisclaimer() {
		// Load initial config
		course = CourseFactory.openCourseEditSession(repositoryEntry.getOlatResource().getResourceableId());
		CourseConfig config = course.getCourseEnvironment().getCourseConfig();
		
		// Set disclaimer
		config.setDisclaimerEnabled(1, true);
		config.setDisclaimerTitle(1, disclaimer1Title);
		config.setDisclaimerTerms(1, disclaimer1Terms);
		config.setDisclaimerLabel(1, 1, disclaimer1Label1);
		config.setDisclaimerLabel(1, 2, disclaimer1Label2);
		
		config.setDisclaimerEnabled(2, true);
		config.setDisclaimerTitle(2, disclaimer2Title);
		config.setDisclaimerTerms(2, disclaimer2Terms);
		config.setDisclaimerLabel(2, 1, disclaimer2Label1);
		config.setDisclaimerLabel(2, 2, disclaimer2Label2);
		
		// Save to config
		CourseFactory.setCourseConfig(repositoryEntry.getOlatResource().getResourceableId(), config);
		CourseFactory.saveCourse(repositoryEntry.getOlatResource().getResourceableId());
		CourseFactory.closeCourseEditSession(repositoryEntry.getOlatResource().getResourceableId(), true);
	}
	
	@Test
	public void setDisclaimer() {
		// Load initial config
		CourseConfig config = CourseFactory.loadCourse(repositoryEntry.getOlatResource().getResourceableId()).getCourseConfig();
		Assert.assertFalse(config.isDisclaimerEnabled());
		
		initDisclaimer();
		
		// Reload config
		config = courseConfigManager.loadConfigFor(course);
		
		// Test if disclaimer is set
		Assert.assertTrue(config.getDisclaimerTitel(1).equals(disclaimer1Title));
		Assert.assertTrue(config.getDisclaimerTerms(1).equals(disclaimer1Terms));
		Assert.assertTrue(config.getDisclaimerLabel(1, 1).equals(disclaimer1Label1));
		Assert.assertTrue(config.getDisclaimerLabel(1, 2).equals(disclaimer1Label2));
		Assert.assertTrue(config.getDisclaimerTitel(2).equals(disclaimer2Title));
		Assert.assertTrue(config.getDisclaimerTerms(2).equals(disclaimer2Terms));
		Assert.assertTrue(config.getDisclaimerLabel(2, 1).equals(disclaimer2Label1));
		Assert.assertTrue(config.getDisclaimerLabel(2, 2).equals(disclaimer2Label2));
	}
	
	@Test
	public void acceptDisclaimer() {
		initDisclaimer();
		
		Assert.assertFalse(courseDisclaimerManager.hasAnyConsent(repositoryEntry));
		
		courseDisclaimerManager.acceptDisclaimer(repositoryEntry, id1, roles1, true, true);
		courseDisclaimerManager.acceptDisclaimer(repositoryEntry, id2, roles2, true, false);
		
		dbInstance.commitAndCloseSession();
		
		Assert.assertTrue(courseDisclaimerManager.isAccessGranted(repositoryEntry, id1, roles1));
		Assert.assertFalse(courseDisclaimerManager.isAccessGranted(repositoryEntry, id2, roles2));
		
		assertThat(courseDisclaimerManager.getConsents(repositoryEntry)).hasSize(2);
	}
	
	@Test
	public void revokeAllConsents() {
		initDisclaimer();
		
		courseDisclaimerManager.acceptDisclaimer(repositoryEntry, id1, roles1, true, true);
		courseDisclaimerManager.acceptDisclaimer(repositoryEntry, id2, roles2, true, true);
		
		dbInstance.commitAndCloseSession();
		
		Assert.assertTrue(courseDisclaimerManager.isAccessGranted(repositoryEntry, id1, roles1));
		Assert.assertTrue(courseDisclaimerManager.isAccessGranted(repositoryEntry, id2, roles2));
		
		courseDisclaimerManager.revokeAllConsents(repositoryEntry);
		
		dbInstance.commitAndCloseSession();
		
		Assert.assertFalse(courseDisclaimerManager.isAccessGranted(repositoryEntry, id1, roles1));
		Assert.assertFalse(courseDisclaimerManager.isAccessGranted(repositoryEntry, id2, roles2));
	}
	
	@Test
	public void removeConsents() {
		initDisclaimer();
		
		courseDisclaimerManager.acceptDisclaimer(repositoryEntry, id1, roles1, true, true);
		courseDisclaimerManager.acceptDisclaimer(repositoryEntry, id2, roles2, true, true);
		
		dbInstance.commitAndCloseSession();
		
		assertThat(courseDisclaimerManager.getConsents(repositoryEntry)).hasSize(2);
		
		List<Long> identitiesToRemove = new ArrayList<>();
		identitiesToRemove.add(id1.getKey());
		courseDisclaimerManager.removeConsents(repositoryEntry, identitiesToRemove);
		
		dbInstance.commitAndCloseSession();
		
		assertThat(courseDisclaimerManager.getConsents(repositoryEntry)).hasSize(1);
	}
	
	@Test
	public void revokeConsents() {
		initDisclaimer();
		
		courseDisclaimerManager.acceptDisclaimer(repositoryEntry, id1, roles1, true, true);
		courseDisclaimerManager.acceptDisclaimer(repositoryEntry, id2, roles2, true, true);
		
		dbInstance.commitAndCloseSession();
		
		Assert.assertTrue(courseDisclaimerManager.isAccessGranted(repositoryEntry, id1, roles1));
		Assert.assertTrue(courseDisclaimerManager.isAccessGranted(repositoryEntry, id2, roles2));
		
		List<Long> identitiesToRevoke = new ArrayList<>();
		identitiesToRevoke.add(id1.getKey());
		courseDisclaimerManager.revokeConsents(repositoryEntry, identitiesToRevoke);
		
		dbInstance.commitAndCloseSession();
		
		Assert.assertFalse(courseDisclaimerManager.isAccessGranted(repositoryEntry, id1, roles1));
		Assert.assertTrue(courseDisclaimerManager.isAccessGranted(repositoryEntry, id2, roles2));
		assertThat(courseDisclaimerManager.getConsents(repositoryEntry)).hasSize(2);
	}
	
	@Test
	public void hasAnyEntry() {
		initDisclaimer();
		
		Assert.assertFalse(courseDisclaimerManager.hasAnyEntry(repositoryEntry));
		
		courseDisclaimerManager.acceptDisclaimer(repositoryEntry, id1, roles1, true, true);
		courseDisclaimerManager.acceptDisclaimer(repositoryEntry, id2, roles2, true, true);
		
		dbInstance.commitAndCloseSession();
		
		Assert.assertTrue(courseDisclaimerManager.hasAnyEntry(repositoryEntry));
		
	}
	
	@Test
	public void hasAnyConsent() {
		initDisclaimer();
		
		Assert.assertFalse(courseDisclaimerManager.hasAnyConsent(repositoryEntry));
		
		courseDisclaimerManager.acceptDisclaimer(repositoryEntry, id1, roles1, true, true);
		courseDisclaimerManager.acceptDisclaimer(repositoryEntry, id2, roles2, true, true);
		
		dbInstance.commitAndCloseSession();
		
		Assert.assertTrue(courseDisclaimerManager.hasAnyConsent(repositoryEntry));
		
		courseDisclaimerManager.revokeAllConsents(repositoryEntry);
		
		dbInstance.commitAndCloseSession();
		
		Assert.assertFalse(courseDisclaimerManager.hasAnyConsent(repositoryEntry));
	}
}

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
import org.olat.course.config.CourseConfigLockedException;
import org.olat.course.disclaimer.model.CourseDisclaimerError;
import org.olat.course.disclaimer.model.CourseDisclaimerSettings;
import org.olat.course.disclaimer.model.CourseDisclaimerSettings.Disclaimer;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRuntimeType;
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
				resource, RepositoryEntryStatusEnum.trash, RepositoryEntryRuntimeType.standalone, defOrganisation);
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
	
	@Test
	public void getSettings() {
		initDisclaimer();
		
		CourseDisclaimerSettings settings = courseDisclaimerManager.getSettings(repositoryEntry);
		Assert.assertTrue(settings.getTermsOfUse().isEnabled());
		Assert.assertEquals(disclaimer1Title, settings.getTermsOfUse().getTitle());
		Assert.assertEquals(disclaimer1Terms, settings.getTermsOfUse().getTerms());
		Assert.assertEquals(disclaimer1Label1, settings.getTermsOfUse().getLabel1());
		Assert.assertEquals(disclaimer1Label2, settings.getTermsOfUse().getLabel2());
		Assert.assertTrue(settings.getDataProtection().isEnabled());
		Assert.assertEquals(disclaimer2Title, settings.getDataProtection().getTitle());
		Assert.assertEquals(disclaimer2Terms, settings.getDataProtection().getTerms());
		Assert.assertEquals(disclaimer2Label1, settings.getDataProtection().getLabel1());
		Assert.assertEquals(disclaimer2Label2, settings.getDataProtection().getLabel2());
	}
	
	@Test
	public void validate() {
		CourseDisclaimerSettings settings = new CourseDisclaimerSettings(
				new Disclaimer(true, "Title", "Terms", "Label", null),
				new Disclaimer(false, null, null, null, null));
		assertThat(courseDisclaimerManager.validate(settings, ConsentHandling.KEEP)).isEmpty();
		assertThat(courseDisclaimerManager.validate(settings, ConsentHandling.REVOKE)).isEmpty();
	}
	
	@Test
	public void validateMandatory() {
		CourseDisclaimerSettings settings = new CourseDisclaimerSettings(
				new Disclaimer(true, " ", null, "", "Optional"),
				new Disclaimer(true, "Title", "Terms", null, null));
		List<CourseDisclaimerError> errors = courseDisclaimerManager.validate(settings, ConsentHandling.KEEP);
		assertThat(errors)
			.containsExactlyInAnyOrder(
				new CourseDisclaimerError(1, "title", CourseDisclaimerError.Type.MANDATORY, -1),
				new CourseDisclaimerError(1, "terms", CourseDisclaimerError.Type.MANDATORY, -1),
				new CourseDisclaimerError(1, "label1", CourseDisclaimerError.Type.MANDATORY, -1),
				new CourseDisclaimerError(2, "label1", CourseDisclaimerError.Type.MANDATORY, -1));
	}
	
	@Test
	public void validateLength() {
		CourseDisclaimerSettings settings = new CourseDisclaimerSettings(
				new Disclaimer(true, "t".repeat(256), "x".repeat(30001), "l".repeat(256), "l".repeat(256)),
				new Disclaimer(true, "t".repeat(255), "x".repeat(30000), "l".repeat(255), "l".repeat(255)));
		List<CourseDisclaimerError> errors = courseDisclaimerManager.validate(settings, ConsentHandling.KEEP);
		assertThat(errors)
			.containsExactlyInAnyOrder(
				new CourseDisclaimerError(1, "title", CourseDisclaimerError.Type.TOO_LONG, 255),
				new CourseDisclaimerError(1, "terms", CourseDisclaimerError.Type.TOO_LONG, 30000),
				new CourseDisclaimerError(1, "label1", CourseDisclaimerError.Type.TOO_LONG, 255),
				new CourseDisclaimerError(1, "label2", CourseDisclaimerError.Type.TOO_LONG, 255));
	}
	
	@Test
	public void validateLengthAfterFilter() {
		// 29'999 characters before the filter, more than 30'000 after (& is escaped as &amp;)
		String terms = "x".repeat(29990) + "&".repeat(9);
		CourseDisclaimerSettings settings = new CourseDisclaimerSettings(
				new Disclaimer(true, "Title", terms, "Label", null),
				new Disclaimer(false, null, null, null, null));
		List<CourseDisclaimerError> errors = courseDisclaimerManager.validate(settings, ConsentHandling.KEEP);
		assertThat(errors)
			.containsExactly(new CourseDisclaimerError(1, "terms", CourseDisclaimerError.Type.TOO_LONG, 30000));
	}
	
	@Test
	public void validateTermsEmptyAfterFilter() {
		CourseDisclaimerSettings settings = new CourseDisclaimerSettings(
				new Disclaimer(true, "Title", "<script>alert('XSS');</script>", "Label", null),
				new Disclaimer(false, null, null, null, null));
		List<CourseDisclaimerError> errors = courseDisclaimerManager.validate(settings, ConsentHandling.KEEP);
		assertThat(errors)
			.containsExactly(new CourseDisclaimerError(1, "terms", CourseDisclaimerError.Type.MANDATORY, -1));
	}
	
	@Test
	public void validateDisabledNotChecked() {
		CourseDisclaimerSettings settings = new CourseDisclaimerSettings(
				new Disclaimer(false, null, null, null, null),
				new Disclaimer(false, "t".repeat(300), null, null, null));
		assertThat(courseDisclaimerManager.validate(settings, ConsentHandling.REMOVE)).isEmpty();
	}
	
	@Test
	public void validateRemoveNotAllowed() {
		CourseDisclaimerSettings settings = new CourseDisclaimerSettings(
				new Disclaimer(false, null, null, null, null),
				new Disclaimer(true, "Title", "Terms", "Label", null));
		assertThat(courseDisclaimerManager.validate(settings, ConsentHandling.REMOVE))
			.containsExactly(new CourseDisclaimerError(0, "consents", CourseDisclaimerError.Type.REMOVE_NOT_ALLOWED, -1));
	}
	
	@Test
	public void updateSettings() throws CourseConfigLockedException {
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser("disc-doer");
		CourseDisclaimerSettings settings = new CourseDisclaimerSettings(
				new Disclaimer(true, "New title", "<p>New terms</p>", "I accept", "I really accept"),
				new Disclaimer(false, null, null, null, null));
		CourseDisclaimerSettings savedSettings = courseDisclaimerManager.updateSettings(repositoryEntry, settings, ConsentHandling.KEEP, doer);
		dbInstance.commitAndCloseSession();
		
		Assert.assertTrue(savedSettings.getTermsOfUse().isEnabled());
		Assert.assertFalse(savedSettings.getDataProtection().isEnabled());
		
		CourseConfig config = CourseFactory.loadCourse(repositoryEntry).getCourseEnvironment().getCourseConfig();
		Assert.assertTrue(config.isDisclaimerEnabled(1));
		Assert.assertFalse(config.isDisclaimerEnabled(2));
		Assert.assertEquals("New title", config.getDisclaimerTitel(1));
		Assert.assertEquals("<p>New terms</p>", config.getDisclaimerTerms(1));
		Assert.assertEquals("I accept", config.getDisclaimerLabel(1, 1));
		Assert.assertEquals("I really accept", config.getDisclaimerLabel(1, 2));
		Assert.assertFalse(CourseFactory.isCourseEditSessionOpen(repositoryEntry.getOlatResource().getResourceableId()));
	}
	
	@Test
	public void updateSettingsDisabledKeepsTexts() throws CourseConfigLockedException {
		initDisclaimer();
		
		CourseDisclaimerSettings settings = courseDisclaimerManager.getSettings(repositoryEntry);
		settings.setDataProtection(new Disclaimer(false, null, null, null, null));
		courseDisclaimerManager.updateSettings(repositoryEntry, settings, ConsentHandling.KEEP, null);
		dbInstance.commitAndCloseSession();
		
		CourseConfig config = CourseFactory.loadCourse(repositoryEntry).getCourseEnvironment().getCourseConfig();
		Assert.assertTrue(config.isDisclaimerEnabled(1));
		Assert.assertFalse(config.isDisclaimerEnabled(2));
		Assert.assertEquals(disclaimer2Title, config.getDisclaimerTitel(2));
		Assert.assertEquals(disclaimer2Terms, config.getDisclaimerTerms(2));
		Assert.assertEquals(disclaimer2Label1, config.getDisclaimerLabel(2, 1));
	}
	
	@Test
	public void updateSettingsXss() throws CourseConfigLockedException {
		CourseDisclaimerSettings settings = new CourseDisclaimerSettings(
				new Disclaimer(true, "Title", "<p>Terms</p><script>alert('XSS');</script>", "I accept", null),
				new Disclaimer(false, null, null, null, null));
		CourseDisclaimerSettings savedSettings = courseDisclaimerManager.updateSettings(repositoryEntry, settings, ConsentHandling.KEEP, null);
		dbInstance.commitAndCloseSession();
		
		String terms = savedSettings.getTermsOfUse().getTerms();
		Assert.assertTrue(terms.contains("<p>Terms</p>"));
		Assert.assertFalse(terms.contains("<script"));
		Assert.assertFalse(terms.contains("alert"));
	}
	
	@Test
	public void updateSettingsKeepConsents() throws CourseConfigLockedException {
		initDisclaimer();
		courseDisclaimerManager.acceptDisclaimer(repositoryEntry, id1, roles1, true, true);
		dbInstance.commitAndCloseSession();
		
		CourseDisclaimerSettings settings = courseDisclaimerManager.getSettings(repositoryEntry);
		settings.getTermsOfUse().setTitle("Changed title");
		courseDisclaimerManager.updateSettings(repositoryEntry, settings, ConsentHandling.KEEP, null);
		dbInstance.commitAndCloseSession();
		
		Assert.assertTrue(courseDisclaimerManager.isAccessGranted(repositoryEntry, id1, roles1));
		Assert.assertEquals("Changed title", courseDisclaimerManager.getSettings(repositoryEntry).getTermsOfUse().getTitle());
	}
	
	@Test
	public void updateSettingsRevokeConsents() throws CourseConfigLockedException {
		initDisclaimer();
		courseDisclaimerManager.acceptDisclaimer(repositoryEntry, id1, roles1, true, true);
		dbInstance.commitAndCloseSession();
		Assert.assertTrue(courseDisclaimerManager.isAccessGranted(repositoryEntry, id1, roles1));
		
		CourseDisclaimerSettings settings = courseDisclaimerManager.getSettings(repositoryEntry);
		settings.getTermsOfUse().setTerms("<p>Changed terms</p>");
		courseDisclaimerManager.updateSettings(repositoryEntry, settings, ConsentHandling.REVOKE, null);
		dbInstance.commitAndCloseSession();
		
		Assert.assertFalse(courseDisclaimerManager.isAccessGranted(repositoryEntry, id1, roles1));
		Assert.assertFalse(courseDisclaimerManager.hasAnyConsent(repositoryEntry));
		Assert.assertTrue(courseDisclaimerManager.hasAnyEntry(repositoryEntry));
	}
	
	@Test
	public void updateSettingsRemoveConsents() throws CourseConfigLockedException {
		initDisclaimer();
		courseDisclaimerManager.acceptDisclaimer(repositoryEntry, id1, roles1, true, true);
		dbInstance.commitAndCloseSession();
		
		CourseDisclaimerSettings settings = courseDisclaimerManager.getSettings(repositoryEntry);
		settings.getTermsOfUse().setEnabled(false);
		settings.getDataProtection().setEnabled(false);
		courseDisclaimerManager.updateSettings(repositoryEntry, settings, ConsentHandling.REMOVE, null);
		dbInstance.commitAndCloseSession();
		
		Assert.assertFalse(courseDisclaimerManager.hasAnyEntry(repositoryEntry));
		Assert.assertFalse(courseDisclaimerManager.getSettings(repositoryEntry).isAnyEnabled());
	}
	
	@Test
	public void updateSettingsLocked() {
		initDisclaimer();
		
		Long courseResId = repositoryEntry.getOlatResource().getResourceableId();
		CourseFactory.openCourseEditSession(courseResId);
		try {
			CourseDisclaimerSettings settings = courseDisclaimerManager.getSettings(repositoryEntry);
			settings.getTermsOfUse().setTitle("Changed title");
			Assert.assertThrows(CourseConfigLockedException.class,
					() -> courseDisclaimerManager.updateSettings(repositoryEntry, settings, ConsentHandling.KEEP, null));
		} finally {
			CourseFactory.closeCourseEditSession(courseResId, true);
		}
		
		Assert.assertEquals(disclaimer1Title, courseDisclaimerManager.getSettings(repositoryEntry).getTermsOfUse().getTitle());
	}
}

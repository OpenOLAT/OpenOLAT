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
package org.olat.modules.assessment.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;
import static org.olat.test.JunitTestHelper.miniRandom;
import static org.olat.test.JunitTestHelper.random;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.assertj.core.api.SoftAssertions;
import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.course.assessment.AssessmentConfigMock;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.core.CourseElement;
import org.olat.course.core.manager.CourseElementDAO;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.SPCourseNode;
import org.olat.group.BusinessGroup;
import org.olat.group.manager.BusinessGroupDAO;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentEntryCompletion;
import org.olat.modules.assessment.AssessmentEntryScoring;
import org.olat.modules.assessment.ObligationOverridable;
import org.olat.modules.assessment.Overridable;
import org.olat.modules.assessment.model.AssessmentEntryImpl;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.modules.assessment.model.AssessmentRunStatus;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryService;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 * Initial date: 20.07.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentEntryDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BusinessGroupDAO businessGroupDao;
	@Autowired
	private CourseElementDAO couurseElementDao;
	@Autowired
	private AssessmentEntryDAO assessmentEntryDao;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private OrganisationService organisationService;
	
	@Test
	public void createAssessmentEntry() {
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-1");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = "39485349759";
		Boolean entryRoot = Boolean.TRUE;

		AssessmentEntry nodeAssessment = assessmentEntryDao
				.createAssessmentEntry(assessedIdentity, null, entry, subIdent,entryRoot, entry);
		Assert.assertNotNull(nodeAssessment);
		dbInstance.commitAndCloseSession();
		
		//check values
		Assert.assertNotNull(nodeAssessment.getKey());
		Assert.assertNotNull(nodeAssessment.getCreationDate());
		Assert.assertNotNull(nodeAssessment.getLastModified());
		Assert.assertEquals(assessedIdentity, nodeAssessment.getIdentity());
		Assert.assertEquals(entry, nodeAssessment.getRepositoryEntry());
		Assert.assertEquals(subIdent, nodeAssessment.getSubIdent());
		Assert.assertEquals(entryRoot, nodeAssessment.getEntryRoot());
	}
	
	@Test
	public void createAssessmentEntry_anonymous() {
		String anonymousIdentifier = UUID.randomUUID().toString();
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = "39485349759";
		Boolean entryRoot = Boolean.TRUE;

		AssessmentEntry nodeAssessment = assessmentEntryDao
				.createAssessmentEntry(null, anonymousIdentifier, entry, subIdent, entryRoot, entry);
		Assert.assertNotNull(nodeAssessment);
		dbInstance.commitAndCloseSession();
		
		//check values
		Assert.assertNotNull(nodeAssessment.getKey());
		Assert.assertNotNull(nodeAssessment.getCreationDate());
		Assert.assertNotNull(nodeAssessment.getLastModified());
		Assert.assertEquals(anonymousIdentifier, nodeAssessment.getAnonymousIdentifier());
		Assert.assertEquals(entry, nodeAssessment.getRepositoryEntry());
		Assert.assertEquals(subIdent, nodeAssessment.getSubIdent());
		Assert.assertEquals(entryRoot, nodeAssessment.getEntryRoot());
	}
	
	@Test
	public void loadCourseNodeAssessmentById() {
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-2");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		Boolean entryRoot = Boolean.TRUE;
		AssessmentEntry nodeAssessment = assessmentEntryDao
				.createAssessmentEntry(assessedIdentity, null, entry, subIdent, entryRoot, entry);
		dbInstance.commitAndCloseSession();
		
		AssessmentEntry reloadedAssessment = assessmentEntryDao.loadAssessmentEntryById(nodeAssessment.getKey());
		Assert.assertEquals(nodeAssessment.getKey(), reloadedAssessment.getKey());
		Assert.assertEquals(nodeAssessment, reloadedAssessment);
		Assert.assertEquals(assessedIdentity, reloadedAssessment.getIdentity());
		Assert.assertEquals(entry, reloadedAssessment.getRepositoryEntry());
		Assert.assertEquals(subIdent, reloadedAssessment.getSubIdent());
		Assert.assertEquals(entryRoot, reloadedAssessment.getEntryRoot());
	}
	
	@Test
	public void loadAssessmentEntry() {
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-3");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		Boolean entryRoot = Boolean.TRUE;
		AssessmentEntry nodeAssessment = assessmentEntryDao
				.createAssessmentEntry(assessedIdentity, null, entry, subIdent, entryRoot, entry);
		dbInstance.commitAndCloseSession();
		
		AssessmentEntry reloadedAssessment = assessmentEntryDao
				.loadAssessmentEntry(assessedIdentity, null, entry, subIdent);
		Assert.assertEquals(nodeAssessment.getKey(), reloadedAssessment.getKey());
		Assert.assertEquals(nodeAssessment, reloadedAssessment);
		Assert.assertEquals(assessedIdentity, reloadedAssessment.getIdentity());
		Assert.assertEquals(entry, reloadedAssessment.getRepositoryEntry());
		Assert.assertEquals(subIdent, reloadedAssessment.getSubIdent());
		Assert.assertEquals(entryRoot, reloadedAssessment.getEntryRoot());
	}
	
	@Test
	public void loadAssessmentEntry_anonymous() {
		String anonymousIdentifier = UUID.randomUUID().toString();
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		Boolean entryRoot = Boolean.TRUE;
		AssessmentEntry nodeAssessment = assessmentEntryDao
				.createAssessmentEntry(null, anonymousIdentifier, entry, subIdent, entryRoot, entry);
		dbInstance.commitAndCloseSession();
		
		AssessmentEntry reloadedAssessment = assessmentEntryDao
				.loadAssessmentEntry(null, anonymousIdentifier, entry, subIdent);
		Assert.assertEquals(nodeAssessment.getKey(), reloadedAssessment.getKey());
		Assert.assertEquals(nodeAssessment, reloadedAssessment);
		Assert.assertEquals(anonymousIdentifier, reloadedAssessment.getAnonymousIdentifier());
		Assert.assertEquals(entry, reloadedAssessment.getRepositoryEntry());
		Assert.assertEquals(subIdent, reloadedAssessment.getSubIdent());
		Assert.assertEquals(entryRoot, reloadedAssessment.getEntryRoot());
	}
	
	@Test
	public void loadAssessmentEntry_specificTest() {
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-5");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry refEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		Boolean entryRoot = Boolean.TRUE;
		AssessmentEntry nodeAssessmentRef = assessmentEntryDao
				.createAssessmentEntry(assessedIdentity, null, entry, subIdent, entryRoot, refEntry);
		dbInstance.commitAndCloseSession();
		
		AssessmentEntry reloadedAssessmentRef = assessmentEntryDao
				.loadAssessmentEntry(assessedIdentity, entry, subIdent, refEntry);
		Assert.assertEquals(nodeAssessmentRef.getKey(), reloadedAssessmentRef.getKey());
		Assert.assertEquals(nodeAssessmentRef, reloadedAssessmentRef);
		Assert.assertEquals(assessedIdentity, reloadedAssessmentRef.getIdentity());
		Assert.assertEquals(entry, reloadedAssessmentRef.getRepositoryEntry());
		Assert.assertEquals(subIdent, reloadedAssessmentRef.getSubIdent());
		Assert.assertEquals(entryRoot, reloadedAssessmentRef.getEntryRoot());
	}
	
	@Test
	public void shouldGetHasGrades() {
		// No Grade
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-6");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = random();
		AssessmentEntry ae = assessmentEntryDao.createAssessmentEntry(assessedIdentity, null, entry, subIdent, null, null);
		dbInstance.commitAndCloseSession();
		
		assertThat(assessmentEntryDao.hasGrades(entry, subIdent)).isFalse();
		
		// Grade
		ae.setGrade("Grade A");
		assessmentEntryDao.updateAssessmentEntry(ae);
		dbInstance.commitAndCloseSession();
		
		assertThat(assessmentEntryDao.hasGrades(entry, subIdent)).isTrue();
	}
	
	@Test
	public void shouldGetScoreCount() {
		// No score
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-6");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = random();
		AssessmentEntry ae = assessmentEntryDao.createAssessmentEntry(assessedIdentity, null, entry, subIdent, null, null);
		dbInstance.commitAndCloseSession();
		
		assertThat(assessmentEntryDao.getScoreCount(entry, subIdent)).isEqualTo(0);
		
		// Score
		ae.setScore(new BigDecimal("22"));
		assessmentEntryDao.updateAssessmentEntry(ae);
		dbInstance.commitAndCloseSession();
		
		assertThat(assessmentEntryDao.getScoreCount(entry, subIdent)).isEqualTo(1);
	}
	
	@Test
	public void resetAssessmentEntry() {
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-6");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry refEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		AssessmentEntry ae = assessmentEntryDao.createAssessmentEntry(assessedIdentity, null, entry, subIdent, null,
				refEntry);
		ae.setScore(BigDecimal.valueOf(2.0));
		ae.setPassed(Boolean.TRUE);
		ae.getPassedOverridable().override(Boolean.FALSE, assessedIdentity, new Date());
		Identity assessmentDoneBy = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-6");
		ae.setAssessmentDoneBy(assessmentDoneBy);
		assessmentEntryDao.updateAssessmentEntry(ae);
		dbInstance.commitAndCloseSession();
		
		AssessmentEntry resetedAssessmentRef = assessmentEntryDao
				.resetAssessmentEntry(ae);
		dbInstance.commitAndCloseSession();
		
		Assert.assertEquals(ae, resetedAssessmentRef);
		Assert.assertEquals(assessedIdentity, resetedAssessmentRef.getIdentity());
		Assert.assertNull(resetedAssessmentRef.getScore());
		Assert.assertNull(resetedAssessmentRef.getPassed());
		Assert.assertNull(resetedAssessmentRef.getPassedOverridable().getCurrent());
		Assert.assertNull(resetedAssessmentRef.getPassedOverridable().getOriginal());
		Assert.assertNull(resetedAssessmentRef.getPassedOverridable().getModBy());
		Assert.assertNull(resetedAssessmentRef.getPassedOverridable().getModDate());
		Assert.assertEquals(Integer.valueOf(0), resetedAssessmentRef.getAttempts());
		Assert.assertNull(resetedAssessmentRef.getLastAttempt());
		Assert.assertNull(resetedAssessmentRef.getCompletion());
		Assert.assertNull(resetedAssessmentRef.getAssessmentDoneBy());
		
		// double check by reloading the entry
		AssessmentEntry reloadedAssessmentRef = assessmentEntryDao
				.loadAssessmentEntryById(resetedAssessmentRef.getKey());
		dbInstance.commitAndCloseSession();

		Assert.assertEquals(ae, reloadedAssessmentRef);
		Assert.assertNull(reloadedAssessmentRef.getScore());
		Assert.assertNull(reloadedAssessmentRef.getMaxScore());
		Assert.assertNull(reloadedAssessmentRef.getPassed());
		Assert.assertNull(reloadedAssessmentRef.getPassedOverridable().getCurrent());
		Assert.assertNull(reloadedAssessmentRef.getPassedOverridable().getOriginal());
		Assert.assertNull(reloadedAssessmentRef.getPassedOverridable().getModBy());
		Assert.assertNull(reloadedAssessmentRef.getPassedOverridable().getModDate());
		Assert.assertEquals(Integer.valueOf(0), reloadedAssessmentRef.getAttempts());
		Assert.assertNull(reloadedAssessmentRef.getCompletion());
		Assert.assertNull(reloadedAssessmentRef.getAssessmentDoneBy());
	}
	
	@Test
	public void shouldUpdateScore() {
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-6");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry refEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		AssessmentEntry ae = assessmentEntryDao.createAssessmentEntry(assessedIdentity, null, entry, subIdent, null,
				refEntry);
		dbInstance.commitAndCloseSession();

		Date lastCoachModified = new GregorianCalendar(2919, 5, 1, 10, 10, 0).getTime();
		ae.setLastCoachModified(lastCoachModified);
		Date lastUserModified = new GregorianCalendar(2919, 5, 1, 11, 10, 0).getTime();
		ae.setLastUserModified(lastUserModified);
		ae.setAttempts(Integer.valueOf(3));
		Date lastAttempt = new GregorianCalendar(2020, 5, 1).getTime();
		ae.setLastAttempt(lastAttempt);
		ae.setScore(BigDecimal.valueOf(2.0));
		ae.setMaxScore(BigDecimal.valueOf(6.0));
		String grade = random();
		ae.setGrade(grade);
		String performanceClassIdent = random();
		ae.setPerformanceClassIdent(performanceClassIdent);
		ae.setPassed(Boolean.TRUE);
		ae.setUserVisibility(Boolean.TRUE);
		ae.setCompletion(Double.valueOf(0.5));
		ae.setCurrentRunCompletion(Double.valueOf(0.3));
		ae.setCurrentRunStatus(AssessmentRunStatus.running);
		ae.setNumberOfAssessmentDocuments(10);
		ae.setComment("very good");
		ae.setCoachComment("coach comment");
		Identity assessmentDoneBy = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-6a");
		ae.setAssessmentDoneBy(assessmentDoneBy);
		assessmentEntryDao.updateAssessmentEntry(ae);
		dbInstance.commitAndCloseSession();
		
		AssessmentEntry reloaded = assessmentEntryDao.loadAssessmentEntryById(ae.getKey());
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(reloaded.getLastCoachModified()).isCloseTo(lastCoachModified, Duration.ofSeconds(2).toMillis());
		softly.assertThat(reloaded.getLastUserModified()).isCloseTo(lastUserModified, Duration.ofSeconds(2).toMillis());
		softly.assertThat(reloaded.getAttempts()).isEqualTo(3);
		softly.assertThat(reloaded.getLastAttempt()).isCloseTo(lastAttempt, Duration.ofSeconds(2).toMillis());
		softly.assertThat(reloaded.getScore()).isEqualByComparingTo(BigDecimal.valueOf(2.0));
		softly.assertThat(reloaded.getMaxScore()).isEqualByComparingTo(BigDecimal.valueOf(6.0));
		softly.assertThat(reloaded.getGrade()).isEqualTo(grade);
		softly.assertThat(reloaded.getPerformanceClassIdent()).isEqualTo(performanceClassIdent);
		softly.assertThat(reloaded.getPassed()).isTrue();
		softly.assertThat(reloaded.getUserVisibility()).isTrue();
		softly.assertThat(reloaded.getCompletion()).isEqualTo(0.5d);
		softly.assertThat(reloaded.getCurrentRunCompletion()).isEqualTo(0.3d, offset(0.001d));
		softly.assertThat(reloaded.getCurrentRunStatus()).isEqualTo(AssessmentRunStatus.running);
		softly.assertThat(reloaded.getNumberOfAssessmentDocuments()).isEqualTo(10);
		softly.assertThat(reloaded.getComment()).isEqualTo("very good");
		softly.assertThat(reloaded.getCoachComment()).isEqualTo("coach comment");
		softly.assertThat(reloaded.getAssessmentDoneBy()).isEqualTo(assessmentDoneBy);
		softly.assertAll();
	}
	
	@Test
	public void setLastVisit() {
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-30");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry refEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		AssessmentEntry nodeAssessment = assessmentEntryDao.createAssessmentEntry(assessedIdentity, null, entry,
				subIdent, null, refEntry);
		dbInstance.commitAndCloseSession();
		
		Date firstDate = new GregorianCalendar(2013,1,28,13,24,56).getTime();
		Date secondDate = new GregorianCalendar(2014,1,1,1,1,1).getTime();
		
		nodeAssessment = assessmentEntryDao.setLastVisit(nodeAssessment, firstDate);
		dbInstance.commitAndCloseSession();
		
		assertThat(nodeAssessment.getFirstVisit())
			.hasSameTimeAs(firstDate);
		assertThat(nodeAssessment.getLastVisit())
			.hasSameTimeAs(firstDate);
		Assert.assertEquals(1l, nodeAssessment.getNumberOfVisits().longValue());
		
		nodeAssessment = assessmentEntryDao.setLastVisit(nodeAssessment, secondDate);
		dbInstance.commitAndCloseSession();
		
		assertThat(nodeAssessment.getFirstVisit())
			.hasSameTimeAs(firstDate);
		assertThat(nodeAssessment.getLastVisit())
			.hasSameTimeAs(secondDate);
		Assert.assertEquals(nodeAssessment.getNumberOfVisits().intValue(), 2);
	}
	
	@Test
	public void setPassedOveridable() {
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-30a");
		Identity modIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-30a-end");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry refEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		AssessmentEntryImpl nodeAssessment = (AssessmentEntryImpl) assessmentEntryDao
				.createAssessmentEntry(assessedIdentity, null, entry, subIdent, null, refEntry);
		dbInstance.commitAndCloseSession();
		
		nodeAssessment.setPassed(Boolean.FALSE);
		assessmentEntryDao.updateAssessmentEntry(nodeAssessment);
		dbInstance.commitAndCloseSession();
		
		Overridable<Boolean> passedOverridable = nodeAssessment.getPassedOverridable();
		Assert.assertEquals(Boolean.FALSE, nodeAssessment.getPassed());
		Assert.assertEquals(Boolean.FALSE, passedOverridable.getCurrent());
		Assert.assertNull(passedOverridable.getOriginal());
		Assert.assertNull(passedOverridable.getModBy());
		Assert.assertNull(passedOverridable.getModDate());
		
		Date at = new GregorianCalendar(2014,1,1,1,1,2).getTime();
		passedOverridable.override(Boolean.TRUE, modIdentity, at);
		nodeAssessment.setPassedOverridable(passedOverridable);
		assessmentEntryDao.updateAssessmentEntry(nodeAssessment);
		dbInstance.commitAndCloseSession();
		
		AssessmentEntry reloadedEntry = assessmentEntryDao.loadAssessmentEntryById(nodeAssessment.getKey());
		
		passedOverridable = reloadedEntry.getPassedOverridable();
		Assert.assertEquals(Boolean.TRUE, nodeAssessment.getPassed());
		Assert.assertEquals(Boolean.TRUE, passedOverridable.getCurrent());
		Assert.assertEquals(Boolean.FALSE, passedOverridable.getOriginal());
		Assert.assertEquals(modIdentity.getKey(), passedOverridable.getModBy().getKey());
		Assert.assertNotNull(passedOverridable.getModDate());
	}
	
	@Test
	public void setLearningPathAttributes() {
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-31");
		Identity modIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-31-end");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry refEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		AssessmentEntryImpl nodeAssessment = (AssessmentEntryImpl) assessmentEntryDao
				.createAssessmentEntry(assessedIdentity, null, entry, subIdent, null, refEntry);
		dbInstance.commitAndCloseSession();
		
		Date startDate = new GregorianCalendar(2014,1,1,1,1,2).getTime();
		nodeAssessment.setStartDate(startDate);
		Date endDate = new GregorianCalendar(2014,1,1,1,1,3).getTime();
		nodeAssessment.setEndDate(endDate);
		Date endDateOriginal = new GregorianCalendar(2014,1,1,1,1,4).getTime();
		nodeAssessment.setEndDateOriginal(endDateOriginal);
		Date endDateModifiactionDate = new GregorianCalendar(2014,1,1,1,1,5).getTime();
		nodeAssessment.setEndDateModificationDate(endDateModifiactionDate);
		nodeAssessment.setEndDateModificationIdentity(modIdentity);
		Integer duration = 10;
		nodeAssessment.setDuration(duration);
		AssessmentObligation obligation = AssessmentObligation.optional;
		nodeAssessment.setObligation(obligation);
		AssessmentObligation obligationInherited = AssessmentObligation.excluded;
		nodeAssessment.setObligationInherited(obligationInherited);
		AssessmentObligation obligationEvaluated = AssessmentObligation.optional;
		nodeAssessment.setObligationEvaluated(obligationEvaluated);
		AssessmentObligation obligationConfig = AssessmentObligation.mandatory;
		nodeAssessment.setObligationConfig(obligationConfig);
		AssessmentObligation obligationOriginal = AssessmentObligation.evaluated;
		nodeAssessment.setObligationOriginal(obligationOriginal);
		Date obligationModDate = new GregorianCalendar(2014,1,1,1,1,5).getTime();
		nodeAssessment.setObligationModDate(obligationModDate);
		nodeAssessment.setObligationModIdentity(modIdentity);
		assessmentEntryDao.updateAssessmentEntry(nodeAssessment);
		dbInstance.commitAndCloseSession();
		
		AssessmentEntry reloadedEntry = assessmentEntryDao.loadAssessmentEntryById(nodeAssessment.getKey());
		
		assertThat(reloadedEntry.getStartDate()).isCloseTo(startDate, Duration.ofMinutes(2).toMillis());
		assertThat(reloadedEntry.getEndDate().getCurrent()).isCloseTo(endDate, Duration.ofMinutes(2).toMillis());
		assertThat(reloadedEntry.getEndDate().getOriginal()).isCloseTo(endDateOriginal, Duration.ofMinutes(2).toMillis());
		assertThat(reloadedEntry.getEndDate().getModDate()).isCloseTo(endDateModifiactionDate, Duration.ofMinutes(2).toMillis());
		assertThat(reloadedEntry.getEndDate().getModBy()).isEqualTo(modIdentity);
		assertThat(reloadedEntry.getDuration()).isEqualTo(duration);
		assertThat(reloadedEntry.getObligation().getCurrent()).isEqualTo(obligation);
		assertThat(reloadedEntry.getObligation().getInherited()).isEqualTo(obligationInherited);
		assertThat(reloadedEntry.getObligation().getEvaluated()).isEqualTo(obligationEvaluated);
		assertThat(reloadedEntry.getObligation().getConfigCurrent()).isEqualTo(obligationConfig);
		assertThat(reloadedEntry.getObligation().getConfigOriginal()).isEqualTo(obligationOriginal);
		assertThat(reloadedEntry.getObligation().getModBy()).isEqualTo(modIdentity);
		assertThat(reloadedEntry.getObligation().getModDate()).isCloseTo(obligationModDate, Duration.ofMinutes(2).toMillis());
	}
	
	@Test
	public void setAssessmentDone() {
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-32");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry refEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		AssessmentEntry nodeAssessment = assessmentEntryDao.createAssessmentEntry(assessedIdentity, null, entry,
				subIdent, null, refEntry);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNull(nodeAssessment.getAssessmentDone());
		
		nodeAssessment.setAssessmentStatus(AssessmentEntryStatus.done);
		nodeAssessment = assessmentEntryDao.updateAssessmentEntry(nodeAssessment);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(nodeAssessment.getAssessmentDone());
		
		nodeAssessment.setAssessmentStatus(null);
		nodeAssessment = assessmentEntryDao.updateAssessmentEntry(nodeAssessment);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNull(nodeAssessment.getAssessmentDone());
		
		nodeAssessment.setAssessmentStatus(AssessmentEntryStatus.inProgress);
		nodeAssessment = assessmentEntryDao.updateAssessmentEntry(nodeAssessment);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNull(nodeAssessment.getAssessmentDone());
		
		nodeAssessment.setAssessmentStatus(AssessmentEntryStatus.done);
		nodeAssessment = assessmentEntryDao.updateAssessmentEntry(nodeAssessment);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(nodeAssessment.getAssessmentDone());
		
		nodeAssessment.setAssessmentStatus(AssessmentEntryStatus.done);
		nodeAssessment = assessmentEntryDao.updateAssessmentEntry(nodeAssessment);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(nodeAssessment.getAssessmentDone());
		
		nodeAssessment.setAssessmentStatus(null);
		nodeAssessment = assessmentEntryDao.updateAssessmentEntry(nodeAssessment);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNull(nodeAssessment.getAssessmentDone());
	}
	
	@Test
	public void setFullyAssessedDate() {
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-33");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry refEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		AssessmentEntry nodeAssessment = assessmentEntryDao.createAssessmentEntry(assessedIdentity, null, entry,
				subIdent, null, refEntry);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNull(nodeAssessment.getFullyAssessedDate());
		
		nodeAssessment.setFullyAssessed(Boolean.TRUE);
		nodeAssessment = assessmentEntryDao.updateAssessmentEntry(nodeAssessment);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(nodeAssessment.getFullyAssessedDate());
		
		nodeAssessment.setFullyAssessed(Boolean.FALSE);
		nodeAssessment = assessmentEntryDao.updateAssessmentEntry(nodeAssessment);
		dbInstance.commitAndCloseSession();
		Assert.assertNull(nodeAssessment.getFullyAssessedDate());
		
		nodeAssessment.setFullyAssessed(Boolean.TRUE);
		nodeAssessment = assessmentEntryDao.updateAssessmentEntry(nodeAssessment);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(nodeAssessment.getFullyAssessedDate());
		
		nodeAssessment.setFullyAssessed(null);
		nodeAssessment = assessmentEntryDao.updateAssessmentEntry(nodeAssessment);
		dbInstance.commitAndCloseSession();
		Assert.assertNull(nodeAssessment.getFullyAssessedDate());
	}
	
	@Test
	public void shouldResetAllRootPassed() {
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("coach");
		Identity assessedIdentity1 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-6a");
		Identity assessedIdentity2 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-6b");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		// Assessment entry
		AssessmentEntry ae = assessmentEntryDao.createAssessmentEntry(assessedIdentity1, null, entry,
				null, Boolean.TRUE, null);
		ae.setPassed(Boolean.FALSE);
		ae = assessmentEntryDao.updateAssessmentEntry(ae);
		// Overriden
		AssessmentEntry aeOverriden = assessmentEntryDao.createAssessmentEntry(assessedIdentity2, null, entry,
				null, Boolean.TRUE, null);
		aeOverriden.setPassed(Boolean.FALSE);
		aeOverriden = assessmentEntryDao.updateAssessmentEntry(aeOverriden);
		aeOverriden.getPassedOverridable().override(Boolean.TRUE, coach, new Date());
		aeOverriden = assessmentEntryDao.updateAssessmentEntry(aeOverriden);
		// Do not change assessment entries of other repository entries
		RepositoryEntry entryOther = JunitTestHelper.createAndPersistRepositoryEntry();
		AssessmentEntry aeOther = assessmentEntryDao.createAssessmentEntry(assessedIdentity1, null, entryOther,
				null, Boolean.TRUE, null);
		aeOther.setPassed(Boolean.FALSE);
		aeOther = assessmentEntryDao.updateAssessmentEntry(aeOther);
		dbInstance.commitAndCloseSession();
		
		assessmentEntryDao.resetAllRootPassed(entry);
		dbInstance.commitAndCloseSession();
		
		SoftAssertions softly = new SoftAssertions();
		ae = assessmentEntryDao.loadAssessmentEntryById(ae.getKey());
		softly.assertThat(ae.getPassed()).isNull();
		softly.assertThat(ae.getPassedOverridable().getCurrent()).isNull();
		aeOverriden = assessmentEntryDao.loadAssessmentEntryById(aeOverriden.getKey());
		softly.assertThat(aeOverriden.getPassedOverridable().getCurrent()).isEqualTo(Boolean.TRUE);
		softly.assertThat(aeOverriden.getPassedOverridable().getOriginal()).isNull();
		softly.assertThat(aeOverriden.getPassedOverridable().getModBy()).isNotNull();
		softly.assertThat(aeOverriden.getPassedOverridable().getModDate()).isNotNull();
		aeOther = assessmentEntryDao.loadAssessmentEntryById(aeOther.getKey());
		softly.assertThat(aeOther.getPassed()).isNotNull();
		softly.assertThat(aeOther.getPassedOverridable().getCurrent()).isNotNull();
		softly.assertAll();
	}
	
	@Test
	public void shouldResetOverridenAllRootPassed() {
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("coach");
		Identity assessedIdentity1 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-6a");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		AssessmentEntry ae1 = assessmentEntryDao.createAssessmentEntry(assessedIdentity1, null, entry,
				null, Boolean.TRUE, null);
		ae1.setPassed(Boolean.FALSE);
		ae1 = assessmentEntryDao.updateAssessmentEntry(ae1);
		ae1.getPassedOverridable().override(Boolean.TRUE, coach, new Date());
		ae1 = assessmentEntryDao.updateAssessmentEntry(ae1);
		// Do not change assessment entries of other repository entries
		RepositoryEntry entryOther = JunitTestHelper.createAndPersistRepositoryEntry();
		AssessmentEntry aeOther = assessmentEntryDao.createAssessmentEntry(assessedIdentity1, null, entryOther,
				null, Boolean.TRUE, null);
		aeOther.setPassed(Boolean.FALSE);
		aeOther = assessmentEntryDao.updateAssessmentEntry(aeOther);
		aeOther.getPassedOverridable().override(Boolean.TRUE, coach, new Date());
		aeOther = assessmentEntryDao.updateAssessmentEntry(aeOther);
		dbInstance.commitAndCloseSession();
		
		assessmentEntryDao.resetAllOverridenRootPassed(entry);
		dbInstance.commitAndCloseSession();
		
		SoftAssertions softly = new SoftAssertions();
		ae1 = assessmentEntryDao.loadAssessmentEntryById(ae1.getKey());
		softly.assertThat(ae1.getPassedOverridable().getCurrent()).isEqualTo(Boolean.FALSE);
		softly.assertThat(ae1.getPassedOverridable().getOriginal()).isNull();
		softly.assertThat(ae1.getPassedOverridable().getModBy()).isNull();
		softly.assertThat(ae1.getPassedOverridable().getModDate()).isNull();
		aeOther = assessmentEntryDao.loadAssessmentEntryById(aeOther.getKey());
		softly.assertThat(aeOther.getPassedOverridable().getCurrent()).isEqualTo(Boolean.TRUE);
		softly.assertThat(aeOther.getPassedOverridable().getOriginal()).isNotNull();
		softly.assertThat(aeOther.getPassedOverridable().getModBy()).isNotNull();
		softly.assertThat(aeOther.getPassedOverridable().getModDate()).isNotNull();
		softly.assertAll();
	}
	
	@Test
	public void loadAssessmentEntries_subIdent() {
		Identity assessedIdentity1 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-7");
		Identity assessedIdentity2 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-8");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry refEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		AssessmentEntry nodeAssessmentId1 = assessmentEntryDao.createAssessmentEntry(assessedIdentity1, null, entry,
				subIdent, null, refEntry);
		AssessmentEntry nodeAssessmentId2 = assessmentEntryDao.createAssessmentEntry(assessedIdentity2, null, entry,
				subIdent, null, refEntry);
		AssessmentEntry nodeAssessmentId3 = assessmentEntryDao.createAssessmentEntry(assessedIdentity2, null, entry,
				null, null, entry);
		AssessmentEntry nodeAssessmentId4 = assessmentEntryDao.createAssessmentEntry(assessedIdentity2, null, refEntry,
				subIdent, null, refEntry);
		dbInstance.commitAndCloseSession();
		
		// load with our subIdent above
		List<AssessmentEntry> assessmentEntries = assessmentEntryDao
				.loadAssessmentEntryBySubIdent(entry, subIdent);
		Assert.assertNotNull(assessmentEntries);
		Assert.assertEquals(2, assessmentEntries.size());
		Assert.assertTrue(assessmentEntries.contains(nodeAssessmentId1));
		Assert.assertTrue(assessmentEntries.contains(nodeAssessmentId2));
		Assert.assertFalse(assessmentEntries.contains(nodeAssessmentId3));
		Assert.assertFalse(assessmentEntries.contains(nodeAssessmentId4));
	}
	
	@Test
	public void getAllIdentitiesWithAssessmentData() {
		Identity assessedIdentity1 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-9");
		Identity assessedIdentity2 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-10");
		Identity assessedIdentity3 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-11");
		Identity assessedIdentity4 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-12");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry refEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		
		String subIdent = UUID.randomUUID().toString();
		assessmentEntryDao.createAssessmentEntry(assessedIdentity1, null, entry, subIdent, null, refEntry);
		assessmentEntryDao.createAssessmentEntry(assessedIdentity2, null, entry, subIdent, null, refEntry);
		assessmentEntryDao.createAssessmentEntry(assessedIdentity3, null, entry, null, null, entry);
		assessmentEntryDao.createAssessmentEntry(assessedIdentity4, null, refEntry, subIdent, null, refEntry);
		dbInstance.commitAndCloseSession();

		// id 1,2,3 are in the entry, but 4 is in an other entry and must not appears in the list
		List<Identity> assessedIdentities = assessmentEntryDao.getAllIdentitiesWithAssessmentData(entry);
		Assert.assertNotNull(assessedIdentities);
		Assert.assertEquals(3, assessedIdentities.size());
		Assert.assertTrue(assessedIdentities.contains(assessedIdentity1));
		Assert.assertTrue(assessedIdentities.contains(assessedIdentity2));
		Assert.assertTrue(assessedIdentities.contains(assessedIdentity3));
		Assert.assertFalse(assessedIdentities.contains(assessedIdentity4));
	}
	
	@Test
	public void loadAssessmentEntriesByAssessedIdentity() {
		Identity assessedIdentity1 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-13");
		Identity assessedIdentity2 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-14");
		Identity assessedIdentity3 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-15");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry refEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		AssessmentEntry nodeAssessmentId1 = assessmentEntryDao.createAssessmentEntry(assessedIdentity1, null, entry,
				subIdent, null, refEntry);
		AssessmentEntry nodeAssessmentId2 = assessmentEntryDao.createAssessmentEntry(assessedIdentity2, null, entry,
				subIdent, null, refEntry);
		AssessmentEntry nodeAssessmentId3 = assessmentEntryDao.createAssessmentEntry(assessedIdentity2, null, entry,
				null, null, entry);
		AssessmentEntry nodeAssessmentId4 = assessmentEntryDao.createAssessmentEntry(assessedIdentity1, null, refEntry,
				subIdent, null, refEntry);
		dbInstance.commitAndCloseSession();
		
		// load for identity 1
		List<AssessmentEntry> assessmentEntriesId1 = assessmentEntryDao
				.loadAssessmentEntriesByAssessedIdentity(assessedIdentity1, entry);
		Assert.assertNotNull(assessmentEntriesId1);
		Assert.assertEquals(1, assessmentEntriesId1.size());
		Assert.assertTrue(assessmentEntriesId1.contains(nodeAssessmentId1));
		Assert.assertFalse(assessmentEntriesId1.contains(nodeAssessmentId2));
		Assert.assertFalse(assessmentEntriesId1.contains(nodeAssessmentId3));
		Assert.assertFalse(assessmentEntriesId1.contains(nodeAssessmentId4));
		
		//load for identity 2
		List<AssessmentEntry> assessmentEntriesId2 = assessmentEntryDao
				.loadAssessmentEntriesByAssessedIdentity(assessedIdentity2, entry);
		Assert.assertNotNull(assessmentEntriesId2);
		Assert.assertEquals(2, assessmentEntriesId2.size());
		Assert.assertFalse(assessmentEntriesId2.contains(nodeAssessmentId1));
		Assert.assertTrue(assessmentEntriesId2.contains(nodeAssessmentId2));
		Assert.assertTrue(assessmentEntriesId2.contains(nodeAssessmentId3));
		Assert.assertFalse(assessmentEntriesId2.contains(nodeAssessmentId4));
		
		//load for identity 3
		List<AssessmentEntry> assessmentEntriesId3 = assessmentEntryDao
				.loadAssessmentEntriesByAssessedIdentity(assessedIdentity3, entry);
		Assert.assertNotNull(assessmentEntriesId3);
		Assert.assertEquals(0, assessmentEntriesId3.size());
	}
	
	@Test
	public void loadAssessmentEntryByGroup() {
		// a simulated course with 2 groups
		Identity assessedIdentity1 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-16");
		Identity assessedIdentity2 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-17");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry refEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "assessment-bg-1", "assessment-bg-1-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		businessGroupRelationDao.addRelationToResource(group1, entry);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "assessment-bg-2", "assessment-bg-2-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		businessGroupRelationDao.addRelationToResource(group2, entry);
		
		businessGroupRelationDao.addRole(assessedIdentity1, group1, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(assessedIdentity2, group1, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		// some assessment entries
		String subIdent = UUID.randomUUID().toString();
		AssessmentEntry nodeAssessmentId1 = assessmentEntryDao.createAssessmentEntry(assessedIdentity1, null, entry,
				subIdent, null, refEntry);
		AssessmentEntry nodeAssessmentId2 = assessmentEntryDao.createAssessmentEntry(assessedIdentity2, null, entry,
				subIdent, null, refEntry);
		AssessmentEntry nodeAssessmentId3 = assessmentEntryDao.createAssessmentEntry(assessedIdentity2, null, entry,
				null, null, entry);
		AssessmentEntry nodeAssessmentId4 = assessmentEntryDao.createAssessmentEntry(assessedIdentity1, null, refEntry,
				subIdent, null, refEntry);
		dbInstance.commitAndCloseSession();
		
		//load the assessment entries of entry
		List<AssessmentEntry> assessmentEntries = assessmentEntryDao.loadAssessmentEntryByGroup(group1.getBaseGroup(), entry, subIdent);
		
		Assert.assertNotNull(assessmentEntries);
		Assert.assertEquals(2, assessmentEntries.size());
		Assert.assertTrue(assessmentEntries.contains(nodeAssessmentId1));
		Assert.assertTrue(assessmentEntries.contains(nodeAssessmentId2));
		Assert.assertFalse(assessmentEntries.contains(nodeAssessmentId3));
		Assert.assertFalse(assessmentEntries.contains(nodeAssessmentId4));
	}
	
	@Test
	public void getRootAssessmentEntriesByRepositoryEntry() {
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-18a");
		Identity assessedIdentityOther = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-18b");
		RepositoryEntry entry1 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry entry2 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry entryOther = JunitTestHelper.createAndPersistRepositoryEntry();
		
		AssessmentEntry nodeAssessment1Root = assessmentEntryDao.createAssessmentEntry(assessedIdentity, null, entry1,
				random(), Boolean.TRUE, null);
		nodeAssessment1Root.setCompletion(1.0);
		assessmentEntryDao.updateAssessmentEntry(nodeAssessment1Root);
		AssessmentEntry nodeAssessment2Root = assessmentEntryDao.createAssessmentEntry(assessedIdentity, null, entry2,
				random(), Boolean.TRUE, null);
		nodeAssessment2Root.setCompletion(0.0);
		assessmentEntryDao.updateAssessmentEntry(nodeAssessment2Root);
		// not entry root
		AssessmentEntry nodeAssessment1Child = assessmentEntryDao.createAssessmentEntry(assessedIdentity, null, entry2,
				random(), Boolean.FALSE, null);
		nodeAssessment1Child.setCompletion(1.1);
		assessmentEntryDao.updateAssessmentEntry(nodeAssessment1Child);
		// other identity
		AssessmentEntry nodeAssessment1OtherIdent = assessmentEntryDao.createAssessmentEntry(assessedIdentityOther, null,
				entry2, random(), Boolean.TRUE, null);
		nodeAssessment1OtherIdent.setCompletion(1.2);
		assessmentEntryDao.updateAssessmentEntry(nodeAssessment1OtherIdent);
		// other repository entry
		AssessmentEntry nodeAssessment1OtherRepo = assessmentEntryDao.createAssessmentEntry(assessedIdentity, null,
				entryOther, random(), Boolean.TRUE, null);
		nodeAssessment1OtherRepo.setCompletion(1.3);
		assessmentEntryDao.updateAssessmentEntry(nodeAssessment1OtherRepo);
		dbInstance.commitAndCloseSession();
		
		List<Long> entryKeys = Arrays.asList(entry1.getKey(), entry2.getKey());
		List<AssessmentEntryScoring> assessmentEnries = assessmentEntryDao.loadRootAssessmentEntriesByAssessedIdentity(assessedIdentity, entryKeys);
		
		Assert.assertEquals(2, assessmentEnries.size());
		Map<Long, AssessmentEntryScoring> keysToCompletion = assessmentEnries.stream()
				.collect(Collectors.toMap(AssessmentEntryScoring::getRepositoryEntryKey, Function.identity()));
		Assert.assertEquals(1, keysToCompletion.get(entry1.getKey()).getCompletion().intValue());
		Assert.assertEquals(0, keysToCompletion.get(entry2.getKey()).getCompletion().intValue());
	}
	
	@Test
	public void getEntryRootCompletionsByIdentitesAndRepositoryEntry() {
		Identity assessedIdentity1 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-19a");
		Identity assessedIdentity2 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-19b");
		Identity assessedIdentityOther = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-19o");
		RepositoryEntry entry1 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry entryOther = JunitTestHelper.createAndPersistRepositoryEntry();
		
		AssessmentEntry nodeAssessment1Root = assessmentEntryDao.createAssessmentEntry(assessedIdentity1, null, entry1,
				random(), Boolean.TRUE, null);
		nodeAssessment1Root.setCompletion(1.0);
		assessmentEntryDao.updateAssessmentEntry(nodeAssessment1Root);
		AssessmentEntry nodeAssessment2Root = assessmentEntryDao.createAssessmentEntry(assessedIdentity2, null, entry1,
				random(), Boolean.TRUE, null);
		nodeAssessment2Root.setCompletion(0.0);
		assessmentEntryDao.updateAssessmentEntry(nodeAssessment2Root);
		// not entry root
		AssessmentEntry nodeAssessment1Child = assessmentEntryDao.createAssessmentEntry(assessedIdentity1, null, entry1,
				random(), Boolean.FALSE, null);
		nodeAssessment1Child.setCompletion(1.1);
		assessmentEntryDao.updateAssessmentEntry(nodeAssessment1Child);
		// other identity
		AssessmentEntry nodeAssessment1OtherIdent = assessmentEntryDao.createAssessmentEntry(assessedIdentityOther, null,
				entry1, random(), Boolean.TRUE, null);
		nodeAssessment1OtherIdent.setCompletion(1.2);
		assessmentEntryDao.updateAssessmentEntry(nodeAssessment1OtherIdent);
		// other repository entry
		AssessmentEntry nodeAssessment1OtherRepo = assessmentEntryDao.createAssessmentEntry(assessedIdentity1, null,
				entryOther, random(), Boolean.TRUE, null);
		nodeAssessment1OtherRepo.setCompletion(1.3);
		assessmentEntryDao.updateAssessmentEntry(nodeAssessment1OtherRepo);
		dbInstance.commitAndCloseSession();
		
		List<Long> identityKeys = Arrays.asList(assessedIdentity1.getKey(), assessedIdentity2.getKey());
		List<AssessmentEntryCompletion> completions = assessmentEntryDao.loadAvgCompletionsByIdentities(entry1, identityKeys);
		
		Assert.assertEquals(2, completions.size());
		Map<Long, Double> keysToCompletion = completions.stream()
				.collect(Collectors.toMap(AssessmentEntryCompletion::getKey, AssessmentEntryCompletion::getCompletion));
		Assert.assertEquals(1, keysToCompletion.get(assessedIdentity1.getKey()).intValue());
		Assert.assertEquals(0, keysToCompletion.get(assessedIdentity2.getKey()).intValue());
	}
	
	@Test
	public void getEntryRootCompletionsByCurriculumElement() {
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-18a");
		Identity assessedIdentityOther = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-18b");
		RepositoryEntry entry1 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry entry2 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry entry3 = JunitTestHelper.createAndPersistRepositoryEntry();
		//exclude deleted courses
		RepositoryEntry entry4 = JunitTestHelper.createAndPersistRepositoryEntry();
		entry4.setEntryStatus(RepositoryEntryStatusEnum.trash);
		entry4 = repositoryService.update(entry4);
		RepositoryEntry entryOther = JunitTestHelper.createAndPersistRepositoryEntry();
		
		Organisation organisation = organisationService.getDefaultOrganisation();
		Curriculum curriculum = curriculumService.createCurriculum(random(), random(), random(), false, organisation);
		CurriculumElement curEle1 = curriculumService.createCurriculumElement(random(), random(),
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		curriculumService.addRepositoryEntry(curEle1, entry1, false);
		curriculumService.addRepositoryEntry(curEle1, entry2, false);
		curriculumService.addRepositoryEntry(curEle1, entry4, false);
		curriculumService.addMember(curEle1, assessedIdentity, CurriculumRoles.participant);
		curriculumService.addMember(curEle1, assessedIdentityOther, CurriculumRoles.participant);
		
		CurriculumElement curEle12 = curriculumService.createCurriculumElement(random(), random(),
				CurriculumElementStatus.active, null, null, curEle1, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		curriculumService.addRepositoryEntry(curEle12, entry1, false);
		curriculumService.addRepositoryEntry(curEle12, entry3, false);
		curriculumService.addMember(curEle12, assessedIdentity, CurriculumRoles.participant);
		
		AssessmentEntry nodeAssessment1Root = assessmentEntryDao.createAssessmentEntry(assessedIdentity, null, entry1,
				random(), Boolean.TRUE, null);
		nodeAssessment1Root.setCompletion(1.0);
		assessmentEntryDao.updateAssessmentEntry(nodeAssessment1Root);
		AssessmentEntry nodeAssessment2Root = assessmentEntryDao.createAssessmentEntry(assessedIdentity, null, entry2,
				random(), Boolean.TRUE, null);
		nodeAssessment2Root.setCompletion(0.2);
		assessmentEntryDao.updateAssessmentEntry(nodeAssessment2Root);
		AssessmentEntry nodeAssessment3Root = assessmentEntryDao.createAssessmentEntry(assessedIdentity, null, entry3,
				random(), Boolean.TRUE, null);
		nodeAssessment3Root.setCompletion(0.3);
		assessmentEntryDao.updateAssessmentEntry(nodeAssessment3Root);
		AssessmentEntry nodeAssessment4Root = assessmentEntryDao.createAssessmentEntry(assessedIdentity, null, entry4,
				random(), Boolean.TRUE, null);
		nodeAssessment4Root.setCompletion(1.0);
		assessmentEntryDao.updateAssessmentEntry(nodeAssessment4Root);
		// not entry root
		AssessmentEntry nodeAssessment1Child = assessmentEntryDao.createAssessmentEntry(assessedIdentity, null, entry2,
				random(), Boolean.FALSE, null);
		nodeAssessment1Child.setCompletion(1.1);
		assessmentEntryDao.updateAssessmentEntry(nodeAssessment1Child);
		// other identity
		AssessmentEntry nodeAssessment1OtherIdent = assessmentEntryDao.createAssessmentEntry(assessedIdentityOther, null,
				entry2, random(), Boolean.TRUE, null);
		nodeAssessment1OtherIdent.setCompletion(1.2);
		assessmentEntryDao.updateAssessmentEntry(nodeAssessment1OtherIdent);
		// other repository entry
		AssessmentEntry nodeAssessment1OtherRepo = assessmentEntryDao.createAssessmentEntry(assessedIdentity, null,
				entryOther, random(), Boolean.TRUE, null);
		nodeAssessment1OtherRepo.setCompletion(1.3);
		assessmentEntryDao.updateAssessmentEntry(nodeAssessment1OtherRepo);
		dbInstance.commitAndCloseSession();
		
		List<Long> curEleKeys = Arrays.asList(curEle1.getKey());
		List<AssessmentEntryCompletion> completions = assessmentEntryDao.loadAvgCompletionsByCurriculumElements(assessedIdentity, curEleKeys);
		
		Assert.assertEquals(1, completions.size());
		Map<Long, Double> keysToCompletion = completions.stream()
				.collect(Collectors.toMap(AssessmentEntryCompletion::getKey, AssessmentEntryCompletion::getCompletion));
		Assert.assertEquals(0.5, keysToCompletion.get(curEle1.getKey()).doubleValue(), 0.00001);
	}
	
	@Test
	public void getEntryRootCompletionsByIdenttiyAndCurriculumElement() {
		Identity assessedIdentity1 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-18a");
		Identity assessedIdentity2 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-18b");
		Identity assessedIdentityOther = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-18o");
		RepositoryEntry entry1 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry entry2 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry entry3 = JunitTestHelper.createAndPersistRepositoryEntry();
		//exclude deleted courses
		RepositoryEntry entry4 = JunitTestHelper.createAndPersistRepositoryEntry();
		entry4.setEntryStatus(RepositoryEntryStatusEnum.trash);
		entry4 = repositoryService.update(entry4);
		RepositoryEntry entryOther = JunitTestHelper.createAndPersistRepositoryEntry();
		
		Organisation organisation = organisationService.getDefaultOrganisation();
		Curriculum curriculum = curriculumService.createCurriculum(random(), random(), random(), false, organisation);
		CurriculumElement curEle1 = curriculumService.createCurriculumElement(random(), random(),
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		curriculumService.addRepositoryEntry(curEle1, entry1, false);
		curriculumService.addRepositoryEntry(curEle1, entry2, false);
		curriculumService.addRepositoryEntry(curEle1, entry4, false);
		curriculumService.addMember(curEle1, assessedIdentity1, CurriculumRoles.participant);
		curriculumService.addMember(curEle1, assessedIdentity2, CurriculumRoles.participant);
		curriculumService.addMember(curEle1, assessedIdentityOther, CurriculumRoles.participant);
		
		CurriculumElement curEle12 = curriculumService.createCurriculumElement(random(), random(),
				CurriculumElementStatus.active, null, null, curEle1, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		curriculumService.addRepositoryEntry(curEle12, entry1, false);
		curriculumService.addRepositoryEntry(curEle12, entry3, false);
		curriculumService.addMember(curEle12, assessedIdentity1, CurriculumRoles.participant);
		
		CurriculumElement curEle2 = curriculumService.createCurriculumElement(random(), random(),
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		curriculumService.addRepositoryEntry(curEle2, entryOther, false);
		curriculumService.addMember(curEle12, assessedIdentity1, CurriculumRoles.participant);
		
		// identity 1
		AssessmentEntry nodeAssessment1Root = assessmentEntryDao.createAssessmentEntry(assessedIdentity1, null, entry1,
				random(), Boolean.TRUE, null);
		nodeAssessment1Root.setCompletion(1.0);
		assessmentEntryDao.updateAssessmentEntry(nodeAssessment1Root);
		AssessmentEntry nodeAssessment2Root = assessmentEntryDao.createAssessmentEntry(assessedIdentity1, null, entry2,
				random(), Boolean.TRUE, null);
		nodeAssessment2Root.setCompletion(0.2);
		assessmentEntryDao.updateAssessmentEntry(nodeAssessment2Root);
		AssessmentEntry nodeAssessment3Root = assessmentEntryDao.createAssessmentEntry(assessedIdentity1, null, entry3,
				random(), Boolean.TRUE, null);
		nodeAssessment3Root.setCompletion(0.3);
		assessmentEntryDao.updateAssessmentEntry(nodeAssessment3Root);
		// deleted entry
		AssessmentEntry nodeAssessment4Root = assessmentEntryDao.createAssessmentEntry(assessedIdentity1, null, entry4,
				random(), Boolean.TRUE, null);
		nodeAssessment4Root.setCompletion(1.0);
		assessmentEntryDao.updateAssessmentEntry(nodeAssessment4Root);
		// not entry root
		AssessmentEntry nodeAssessment1Child = assessmentEntryDao.createAssessmentEntry(assessedIdentity1, null, entry2,
				random(), Boolean.FALSE, null);
		nodeAssessment1Child.setCompletion(1.1);
		assessmentEntryDao.updateAssessmentEntry(nodeAssessment1Child);
		// other repository entry
		AssessmentEntry nodeAssessment1OtherRepo = assessmentEntryDao.createAssessmentEntry(assessedIdentity1, null,
				entryOther, random(), Boolean.TRUE, null);
		nodeAssessment1OtherRepo.setCompletion(1.3);
		assessmentEntryDao.updateAssessmentEntry(nodeAssessment1OtherRepo);
		
		// identity 2
		AssessmentEntry nodeAssessment2_1Root = assessmentEntryDao.createAssessmentEntry(assessedIdentity2, null, entry1,
				random(), Boolean.TRUE, null);
		nodeAssessment2_1Root.setCompletion(1.0);
		assessmentEntryDao.updateAssessmentEntry(nodeAssessment2_1Root);
		AssessmentEntry nodeAssessment2_2Root = assessmentEntryDao.createAssessmentEntry(assessedIdentity2, null, entry2,
				random(), Boolean.TRUE, null);
		nodeAssessment2_2Root.setCompletion(0.2);
		assessmentEntryDao.updateAssessmentEntry(nodeAssessment2_2Root);
		AssessmentEntry nodeAssessment2_3Root = assessmentEntryDao.createAssessmentEntry(assessedIdentity2, null, entry3,
				random(), Boolean.TRUE, null);
		nodeAssessment2_3Root.setCompletion(null);
		assessmentEntryDao.updateAssessmentEntry(nodeAssessment2_3Root);
		
		// other identity
		AssessmentEntry nodeAssessment1OtherIdent = assessmentEntryDao.createAssessmentEntry(assessedIdentityOther, null,
				entry2, random(), Boolean.TRUE, null);
		nodeAssessment1OtherIdent.setCompletion(1.2);
		assessmentEntryDao.updateAssessmentEntry(nodeAssessment1OtherIdent);
		dbInstance.commitAndCloseSession();
		
		List<Long> identityKeys = Arrays.asList(assessedIdentity1.getKey(), assessedIdentity2.getKey());
		List<AssessmentEntryCompletion> completions = assessmentEntryDao.loadAvgCompletionsByIdentities(curEle1, identityKeys);
		
		Assert.assertEquals(2, completions.size());
		Map<Long, Double> keysToCompletion = completions.stream()
				.collect(Collectors.toMap(AssessmentEntryCompletion::getKey, AssessmentEntryCompletion::getCompletion));
		Assert.assertEquals(0.5, keysToCompletion.get(assessedIdentity1.getKey()).doubleValue(), 0.00001);
		Assert.assertEquals(0.6, keysToCompletion.get(assessedIdentity2.getKey()).doubleValue(), 0.00001);
	}
	
	@Test
	public void getRootEntriesWithStartOverSubEntries() {
		Identity identity1 = JunitTestHelper.createAndPersistIdentityAsRndUser("start-date-1");
		Identity identity2 = JunitTestHelper.createAndPersistIdentityAsRndUser("start-date-2");
		Identity identity3 = JunitTestHelper.createAndPersistIdentityAsRndUser("start-date-3");
		Identity identity4 = JunitTestHelper.createAndPersistIdentityAsRndUser("start-date-4");
		RepositoryEntry re1 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry reDeleted = JunitTestHelper.createAndPersistRepositoryEntry();
		
		Date before = new GregorianCalendar(2010, 2, 8).getTime();
		Date start = new GregorianCalendar(2010, 2, 10).getTime();
		Date after = new GregorianCalendar(2010, 2, 12).getTime();
		
		String re1RootElIdent = createCourseElement(re1, random()).getSubIdent();
		String re1SubOver1ElIdent = createCourseElement(re1, random()).getSubIdent();
		String re1SubOver2ElIdent = createCourseElement(re1, random()).getSubIdent();
		
		String reDeletedRootElIdent = createCourseElement(reDeleted, random()).getSubIdent();
		String reDeletedSubOver1ElIdent = createCourseElement(reDeleted, random()).getSubIdent();
		
		// Root is (only once) in results
		AssessmentEntry ae1Root = assessmentEntryDao.createAssessmentEntry(identity1, null, re1, re1RootElIdent, Boolean.TRUE,
				null);
		AssessmentEntry ae1SubOver1 = assessmentEntryDao.createAssessmentEntry(identity1, null, re1, re1SubOver1ElIdent,
				Boolean.FALSE, null);
		ae1SubOver1.setStartDate(before);
		assessmentEntryDao.updateAssessmentEntry(ae1SubOver1);
		AssessmentEntry ae1SubOver2 = assessmentEntryDao.createAssessmentEntry(identity1, null, re1, re1SubOver2ElIdent,
				Boolean.FALSE, null);
		ae1SubOver2.setStartDate(before);
		assessmentEntryDao.updateAssessmentEntry(ae1SubOver2);

		// Root is in results: second identity
		AssessmentEntry ae2Root = assessmentEntryDao.createAssessmentEntry(identity2, null, re1, re1RootElIdent, Boolean.TRUE,
				null);
		AssessmentEntry ae2SubOver1 = assessmentEntryDao.createAssessmentEntry(identity2, null, re1, re1SubOver1ElIdent,
				Boolean.FALSE, null);
		ae2SubOver1.setStartDate(before);
		assessmentEntryDao.updateAssessmentEntry(ae2SubOver1);

		// Root is not in list: no start date, start date not over
		AssessmentEntry ae3Root = assessmentEntryDao.createAssessmentEntry(identity3, null, re1, re1RootElIdent, Boolean.TRUE,
				null);
		AssessmentEntry ae3SubOver1 = assessmentEntryDao.createAssessmentEntry(identity3, null, re1, re1SubOver1ElIdent,
				Boolean.FALSE, null);
		ae3SubOver1.setStartDate(null);
		assessmentEntryDao.updateAssessmentEntry(ae3SubOver1);
		AssessmentEntry ae3SubOver2 = assessmentEntryDao.createAssessmentEntry(identity3, null, re1, re1SubOver2ElIdent,
				Boolean.FALSE, null);
		ae3SubOver2.setStartDate(after);
		assessmentEntryDao.updateAssessmentEntry(ae3SubOver2);
		
		// Root is not in results: RepositoryEntry is deleted
		repositoryService.deleteSoftly(reDeleted, identity3, false, false);
		AssessmentEntry ae4Root = assessmentEntryDao.createAssessmentEntry(identity2, null, reDeleted, reDeletedRootElIdent, Boolean.TRUE, null);
		AssessmentEntry ae4SubOver1 = assessmentEntryDao.createAssessmentEntry(identity2, null, reDeleted, reDeletedSubOver1ElIdent, Boolean.FALSE, null);
		ae4SubOver1.setStartDate(before);
		assessmentEntryDao.updateAssessmentEntry(ae4SubOver1);
		
		// As start date on element which doesn't exists in course
		AssessmentEntry ae5Root = assessmentEntryDao.createAssessmentEntry(identity4, null, re1, re1RootElIdent, Boolean.TRUE,
				null);
		AssessmentEntry ae5SubOver1 = assessmentEntryDao.createAssessmentEntry(identity4, null, re1, random(),
				Boolean.FALSE, null);
		ae5SubOver1.setStartDate(before);
		assessmentEntryDao.updateAssessmentEntry(ae5SubOver1);
		
		dbInstance.commitAndCloseSession();
		
		List<AssessmentEntry> rootEntries = assessmentEntryDao.getRootEntriesWithStartOverSubEntries(start);
		
		assertThat(rootEntries)
				.contains(ae1Root, ae2Root)
				.doesNotContain(ae1SubOver1, ae1SubOver2, ae2SubOver1, ae3Root, ae3SubOver1, ae3SubOver2, ae4Root, ae4SubOver1, ae5Root, ae5SubOver1);
	}
	
	@Test
	public void loadRootEntriesWithoutPassed() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity assessedIdentity1 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-18");
		Identity assessedIdentity2 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-19");
		
		AssessmentEntry aeRootNullPassed1 = assessmentEntryDao.createAssessmentEntry(assessedIdentity1, null, entry, random(), Boolean.TRUE, null);
		aeRootNullPassed1.setPassed(null);
		aeRootNullPassed1 = assessmentEntryDao.updateAssessmentEntry(aeRootNullPassed1);
		AssessmentEntry aeRootNullPassed2 = assessmentEntryDao.createAssessmentEntry(assessedIdentity2, null, entry, random(), Boolean.TRUE, null);
		aeRootNullPassed2.setPassed(null);
		aeRootNullPassed2 = assessmentEntryDao.updateAssessmentEntry(aeRootNullPassed2);
		AssessmentEntry aeNotRootNullPassed = assessmentEntryDao.createAssessmentEntry(assessedIdentity1, null, entry, random(), Boolean.FALSE, null);
		aeNotRootNullPassed.setPassed(null);
		aeNotRootNullPassed = assessmentEntryDao.updateAssessmentEntry(aeNotRootNullPassed);
		AssessmentEntry aeRootPassed = assessmentEntryDao.createAssessmentEntry(assessedIdentity1, null, entry, random(), Boolean.TRUE, null);
		aeRootPassed.setPassed(Boolean.TRUE);
		aeRootPassed = assessmentEntryDao.updateAssessmentEntry(aeRootPassed);
		
		List<AssessmentEntry> assessmentEntries = assessmentEntryDao.loadRootEntriesWithoutPassed(entry);
		
		assertThat(assessmentEntries)
				.contains(aeRootNullPassed1, aeRootNullPassed2)
				.doesNotContain(aeNotRootNullPassed, aeRootPassed);
	}
	
	@Test
	public void removeEntryForReferenceEntry() {
		Identity assessedIdentity1 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-18");
		Identity assessedIdentity2 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-19");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry refEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		
		String subIdent = UUID.randomUUID().toString();
		AssessmentEntry nodeAssessment1 = assessmentEntryDao.createAssessmentEntry(assessedIdentity1, null, entry,
				subIdent, null, refEntry);
		AssessmentEntry nodeAssessment2 = assessmentEntryDao.createAssessmentEntry(assessedIdentity2, null, entry,
				subIdent, null, refEntry);
		AssessmentEntry nodeAssessment3 = assessmentEntryDao.createAssessmentEntry(assessedIdentity2, null, entry, null,
				null, entry);
		AssessmentEntry nodeAssessment4 = assessmentEntryDao.createAssessmentEntry(assessedIdentity1, null, refEntry,
				subIdent, null, refEntry);
		dbInstance.commitAndCloseSession();
		
		// delete by reference
		int affectedRows = assessmentEntryDao.removeEntryForReferenceEntry(refEntry);
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(3, affectedRows);

		//check
		AssessmentEntry deletedAssessmentEntry1 = assessmentEntryDao.loadAssessmentEntryById(nodeAssessment1.getKey());
		Assert.assertNotNull(deletedAssessmentEntry1);
		Assert.assertNull(deletedAssessmentEntry1.getReferenceEntry());
		AssessmentEntry deletedAssessmentEntry2 = assessmentEntryDao.loadAssessmentEntryById(nodeAssessment2.getKey());
		Assert.assertNotNull(deletedAssessmentEntry2);
		Assert.assertNull(deletedAssessmentEntry2.getReferenceEntry());
		AssessmentEntry deletedAssessmentEntry3 = assessmentEntryDao.loadAssessmentEntryById(nodeAssessment3.getKey());
		Assert.assertNotNull(deletedAssessmentEntry3);
		Assert.assertNotNull(deletedAssessmentEntry3.getReferenceEntry());
		Assert.assertEquals(entry, deletedAssessmentEntry3.getReferenceEntry());
		AssessmentEntry deletedAssessmentEntry4 = assessmentEntryDao.loadAssessmentEntryById(nodeAssessment4.getKey());
		Assert.assertNotNull(deletedAssessmentEntry4);
		Assert.assertNull(deletedAssessmentEntry4.getReferenceEntry());
	}
	
	@Test
	public void deleteEntryForRepositoryEntry() {
		Identity assessedIdentity1 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-20");
		Identity assessedIdentity2 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-21");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry refEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		
		String subIdent = UUID.randomUUID().toString();
		AssessmentEntry nodeAssessment1 = assessmentEntryDao.createAssessmentEntry(assessedIdentity1, null, entry,
				subIdent, null, refEntry);
		AssessmentEntry nodeAssessment2 = assessmentEntryDao.createAssessmentEntry(assessedIdentity2, null, entry,
				subIdent, null, refEntry);
		AssessmentEntry nodeAssessment3 = assessmentEntryDao.createAssessmentEntry(assessedIdentity2, null, entry, null,
				null, entry);
		AssessmentEntry nodeAssessment4 = assessmentEntryDao.createAssessmentEntry(assessedIdentity1, null, refEntry,
				subIdent, null, refEntry);
		dbInstance.commitAndCloseSession();
		
		// delete by reference
		assessmentEntryDao.deleteEntryForRepositoryEntry(entry);
		dbInstance.commitAndCloseSession();

		//check
		AssessmentEntry deletedAssessmentEntry1 = assessmentEntryDao.loadAssessmentEntryById(nodeAssessment1.getKey());
		Assert.assertNull(deletedAssessmentEntry1);
		AssessmentEntry deletedAssessmentEntry2 = assessmentEntryDao.loadAssessmentEntryById(nodeAssessment2.getKey());
		Assert.assertNull(deletedAssessmentEntry2);
		AssessmentEntry deletedAssessmentEntry3 = assessmentEntryDao.loadAssessmentEntryById(nodeAssessment3.getKey());
		Assert.assertNull(deletedAssessmentEntry3);
		AssessmentEntry deletedAssessmentEntry4 = assessmentEntryDao.loadAssessmentEntryById(nodeAssessment4.getKey());
		Assert.assertNotNull(deletedAssessmentEntry4);
	}
	
	@Test
	public void loadAssessmentEntryBySubIdentWithStatus() {
		Identity assessedIdentity1 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-22");
		Identity assessedIdentity2 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-23");
		Identity assessedIdentity3 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-24");
		Identity assessedIdentity4 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-25");
		Identity assessedIdentity5 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-25a");
		Identity assessedIdentity6 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-25b");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry refEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		BusinessGroup group = businessGroupDao.createAndPersist(null, "rel-bg-part-1", "rel-bgis-1-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		businessGroupRelationDao.addRelationToResource(group, entry);
		businessGroupRelationDao.addRelationToResource(group, refEntry);
		businessGroupRelationDao.addRole(assessedIdentity1, group, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(assessedIdentity2, group, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(assessedIdentity3, group, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(assessedIdentity4, group, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(assessedIdentity6, group, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(assessedIdentity5, group, GroupRoles.coach.name());
		
		AssessmentEntry nodeAssessmentId1 = assessmentEntryDao.createAssessmentEntry(assessedIdentity1, null, entry,
				subIdent, null, refEntry);
		nodeAssessmentId1.setScore(null);
		nodeAssessmentId1.setUserVisibility(Boolean.TRUE);
		assessmentEntryDao.updateAssessmentEntry(nodeAssessmentId1);
		AssessmentEntry nodeAssessmentId2 = assessmentEntryDao.createAssessmentEntry(assessedIdentity2, null, entry,
				subIdent, null, refEntry);
		nodeAssessmentId2.setScore(BigDecimal.valueOf(0));
		nodeAssessmentId2.setUserVisibility(Boolean.TRUE);
		assessmentEntryDao.updateAssessmentEntry(nodeAssessmentId2);
		AssessmentEntry nodeAssessmentId3 = assessmentEntryDao.createAssessmentEntry(assessedIdentity2, null, entry,
				null, null, entry);
		nodeAssessmentId3.setScore(BigDecimal.valueOf(2));
		nodeAssessmentId3.setUserVisibility(Boolean.TRUE);
		assessmentEntryDao.updateAssessmentEntry(nodeAssessmentId3);
		AssessmentEntry nodeAssessmentId4 = assessmentEntryDao.createAssessmentEntry(assessedIdentity2, null, refEntry,
				subIdent, null, refEntry);
		nodeAssessmentId4.setScore(BigDecimal.valueOf(5));
		nodeAssessmentId4.setUserVisibility(Boolean.TRUE);
		assessmentEntryDao.updateAssessmentEntry(nodeAssessmentId4);
		AssessmentEntry nodeAssessmentId5 = assessmentEntryDao.createAssessmentEntry(assessedIdentity3, null, entry,
				subIdent, null, refEntry);
		nodeAssessmentId5.setScore(BigDecimal.valueOf(1));
		nodeAssessmentId5.setUserVisibility(Boolean.TRUE);
		assessmentEntryDao.updateAssessmentEntry(nodeAssessmentId5);
		AssessmentEntry nodeAssessmentId6 = assessmentEntryDao.createAssessmentEntry(assessedIdentity4, null, entry,
				subIdent, null, refEntry);
		nodeAssessmentId6.setScore(BigDecimal.valueOf(3.2));
		nodeAssessmentId6.setUserVisibility(Boolean.TRUE);
		assessmentEntryDao.updateAssessmentEntry(nodeAssessmentId6);
		AssessmentEntry nodeAssessmentId7 = assessmentEntryDao.createAssessmentEntry(assessedIdentity5, null, entry,
				subIdent, null, refEntry);
		nodeAssessmentId7.setScore(BigDecimal.valueOf(99));
		nodeAssessmentId7.setUserVisibility(Boolean.TRUE);
		assessmentEntryDao.updateAssessmentEntry(nodeAssessmentId7);
		AssessmentEntry nodeAssessmentId8 = assessmentEntryDao.createAssessmentEntry(assessedIdentity6, null, entry,
				subIdent, null, refEntry);
		nodeAssessmentId8.setScore(BigDecimal.valueOf(99));
		nodeAssessmentId8.setUserVisibility(Boolean.FALSE);
		assessmentEntryDao.updateAssessmentEntry(nodeAssessmentId8);
		dbInstance.commitAndCloseSession();
		// load with our subIdent above
		List<AssessmentEntry> assessmentEntries = assessmentEntryDao
				.loadAssessmentEntryBySubIdentWithStatus(entry, subIdent, null, true, true);
		Assert.assertNotNull(assessmentEntries);
		Assert.assertEquals(2, assessmentEntries.size());
		Assert.assertFalse(assessmentEntries.contains(nodeAssessmentId1));
		Assert.assertFalse(assessmentEntries.contains(nodeAssessmentId2));
		Assert.assertFalse(assessmentEntries.contains(nodeAssessmentId3));
		Assert.assertFalse(assessmentEntries.contains(nodeAssessmentId4));
		Assert.assertTrue(assessmentEntries.contains(nodeAssessmentId5));
		Assert.assertTrue(assessmentEntries.contains(nodeAssessmentId6));
		Assert.assertFalse(assessmentEntries.contains(nodeAssessmentId7));
		Assert.assertFalse(assessmentEntries.contains(nodeAssessmentId8));
	}
	
	@Test
	public void hasAssessmentEntry() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity assessedIdentity1 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-24");
		Identity assessedIdentity2 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-25");
		assessmentEntryDao.createAssessmentEntry(assessedIdentity1, null, entry, random(), null, null);
		dbInstance.commitAndCloseSession();
		
		Assert.assertTrue(assessmentEntryDao.hasAssessmentEntry(assessedIdentity1, entry));
		Assert.assertFalse(assessmentEntryDao.hasAssessmentEntry(assessedIdentity2, entry));
	}
	
	@Test
	public void getIdentityKeys() {
		Identity assessedIdentity1 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-26a");
		Identity assessedIdentity2 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-26b");
		Identity assessedIdentity3 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-26c");
		Identity assessedIdentity4 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-26d");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = random();
		dbInstance.commitAndCloseSession();
		
		AssessmentEntry nodeAssessmentId1 = assessmentEntryDao.createAssessmentEntry(assessedIdentity1, null, entry,
				subIdent, null, null);
		nodeAssessmentId1.setObligation(null);
		assessmentEntryDao.updateAssessmentEntry(nodeAssessmentId1);
		AssessmentEntry nodeAssessmentId2 = assessmentEntryDao.createAssessmentEntry(assessedIdentity2, null, entry,
				subIdent, null, null);
		nodeAssessmentId2.setObligation(ObligationOverridable.of(AssessmentObligation.mandatory));
		assessmentEntryDao.updateAssessmentEntry(nodeAssessmentId2);
		AssessmentEntry nodeAssessmentId3 = assessmentEntryDao.createAssessmentEntry(assessedIdentity3, null, entry,
				subIdent, null, null);
		nodeAssessmentId3.setObligation(ObligationOverridable.of(AssessmentObligation.optional));
		assessmentEntryDao.updateAssessmentEntry(nodeAssessmentId3);
		AssessmentEntry nodeAssessmentId4 = assessmentEntryDao.createAssessmentEntry(assessedIdentity4, null, entry,
				subIdent, null, null);
		nodeAssessmentId4.setObligation(ObligationOverridable.of(AssessmentObligation.excluded));
		assessmentEntryDao.updateAssessmentEntry(nodeAssessmentId4);
		dbInstance.commitAndCloseSession();
		
		Collection<AssessmentObligation> obligations = List.of(AssessmentObligation.excluded);
		List<Long> identityKeys = assessmentEntryDao.loadIdentityKeys(entry, subIdent, obligations);
		
		Assert.assertNotNull(identityKeys);
		Assert.assertEquals(1, identityKeys.size());
		Assert.assertTrue(identityKeys.contains(assessedIdentity4.getKey()));
		
		obligations = List.of(AssessmentObligation.mandatory);
		identityKeys = assessmentEntryDao.loadIdentityKeys(entry, subIdent, obligations);
		
		Assert.assertNotNull(identityKeys);
		Assert.assertEquals(2, identityKeys.size());
		Assert.assertTrue(identityKeys.contains(assessedIdentity1.getKey()));
		Assert.assertTrue(identityKeys.contains(assessedIdentity2.getKey()));
	}
	
	private CourseElement createCourseElement(RepositoryEntry entry, String subIdent) {
		CourseNode courseNode = new SPCourseNode();
		courseNode.setIdent(subIdent);
		courseNode.setShortTitle(miniRandom());
		courseNode.setLongTitle(random());
		
		AssessmentConfigMock assessmentConfig = new AssessmentConfigMock();
		assessmentConfig.setAssessable(true);
		assessmentConfig.setScoreMode(Mode.setByNode);
		assessmentConfig.setPassedMode(Mode.setByNode);
		assessmentConfig.setCutValue(Float.valueOf(2.4f));
		
		return couurseElementDao.create(entry, courseNode, assessmentConfig);
	}

}
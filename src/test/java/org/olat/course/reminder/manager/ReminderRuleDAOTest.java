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
package org.olat.course.reminder.manager;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.math.BigDecimal;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.SPCourseNode;
import org.olat.course.reminder.rule.PassedRuleSPI.Status;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 Sep 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ReminderRuleDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private AssessmentService assessmentService;
	
	@Autowired
	private ReminderRuleDAO sut;
	
	@Test
	public void shouldGetScores() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry entryOther = JunitTestHelper.createAndPersistRepositoryEntry();
		CourseNode node = new SPCourseNode();
		String subIdent = node.getIdent();
		String subIdenOther = random();
		Identity identity1 = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Identity identity2 = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Identity identityNotUserVisible = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Identity identityNoUserVisibility = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Identity identityNoScore = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Identity identityOtherEntry = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Identity identityOtherSubEntry = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Identity identityNotTarget = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		List<Identity> targetIdentities = asList(identity1, identity2, identityNotUserVisible, identityNoUserVisibility,
				identityNoScore, identityOtherEntry, identityOtherSubEntry);
		
		AssessmentEntry ae1 = assessmentService.getOrCreateAssessmentEntry(identity1, null, entry, subIdent, Boolean.FALSE, null);
		ae1.setScore(BigDecimal.valueOf(5));
		ae1.setUserVisibility(Boolean.TRUE);
		assessmentService.updateAssessmentEntry(ae1);
		AssessmentEntry ae2 = assessmentService.getOrCreateAssessmentEntry(identity2, null, entry, subIdent, Boolean.FALSE, null);
		ae2.setScore(BigDecimal.valueOf(6));
		ae2.setUserVisibility(Boolean.TRUE);
		assessmentService.updateAssessmentEntry(ae2);
		AssessmentEntry aeNotUserVisible = assessmentService.getOrCreateAssessmentEntry(identityNotUserVisible, null, entry, subIdent, Boolean.FALSE, null);
		aeNotUserVisible.setScore(BigDecimal.valueOf(10));
		aeNotUserVisible.setUserVisibility(Boolean.FALSE);
		assessmentService.updateAssessmentEntry(aeNotUserVisible);
		AssessmentEntry aeNoUserVisibility = assessmentService.getOrCreateAssessmentEntry(identityNoUserVisibility, null, entry, subIdent, Boolean.FALSE, null);
		aeNoUserVisibility.setScore(BigDecimal.valueOf(11));
		aeNoUserVisibility.setUserVisibility(null);
		assessmentService.updateAssessmentEntry(aeNoUserVisibility);
		AssessmentEntry aeNoScore = assessmentService.getOrCreateAssessmentEntry(identityNoScore, null, entry, subIdent, Boolean.FALSE, null);
		aeNoScore.setScore(null);
		aeNoScore.setUserVisibility(Boolean.TRUE);
		assessmentService.updateAssessmentEntry(aeNoScore);
		AssessmentEntry aeOtherEntry = assessmentService.getOrCreateAssessmentEntry(identityOtherEntry, null, entryOther, subIdent, Boolean.FALSE, null);
		aeOtherEntry.setScore(BigDecimal.valueOf(7));
		aeOtherEntry.setUserVisibility(Boolean.TRUE);
		assessmentService.updateAssessmentEntry(aeOtherEntry);
		AssessmentEntry aeOtherSubIdent = assessmentService.getOrCreateAssessmentEntry(identityOtherSubEntry, null, entry, subIdenOther, Boolean.FALSE, null);
		aeOtherSubIdent.setScore(BigDecimal.valueOf(8));
		aeOtherSubIdent.setUserVisibility(Boolean.TRUE);
		assessmentService.updateAssessmentEntry(aeOtherSubIdent);
		AssessmentEntry aeOtherIdentity = assessmentService.getOrCreateAssessmentEntry(identityNotTarget, null, entry, subIdent, Boolean.FALSE, null);
		aeOtherIdentity.setScore(BigDecimal.valueOf(9));
		aeOtherIdentity.setUserVisibility(Boolean.TRUE);
		assessmentService.updateAssessmentEntry(aeOtherIdentity);
		
		Map<Long, Float> scores = sut.getScores(entry, node, targetIdentities);
		
		assertThat(scores.keySet()).containsExactlyInAnyOrder(
						identity1.getKey(),
						identity2.getKey())
				.doesNotContain(
						identityNotUserVisible.getKey(),
						identityNoUserVisibility.getKey(),
						identityNoScore.getKey(),
						identityOtherEntry.getKey(),
						identityOtherSubEntry.getKey(),
						identityNotTarget.getKey()
				);
		assertThat(scores.get(identity1.getKey())).isEqualTo(5);
		assertThat(scores.get(identity2.getKey())).isEqualTo(6);
	}
	
	@Test
	public void shouldGetAttempts() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry entryOther = JunitTestHelper.createAndPersistRepositoryEntry();
		CourseNode node = new SPCourseNode();
		String subIdent = node.getIdent();
		String subIdenOther = random();
		Identity identity1 = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Identity identity2 = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Identity identityOther = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		
		AssessmentEntry ae1 = assessmentService.getOrCreateAssessmentEntry(identity1, null, entry, subIdent, Boolean.FALSE, null);
		ae1.setAttempts(Integer.valueOf(1));
		assessmentService.updateAssessmentEntry(ae1);
		AssessmentEntry ae2 = assessmentService.getOrCreateAssessmentEntry(identity2, null, entry, subIdent, Boolean.FALSE, null);
		ae2.setAttempts(Integer.valueOf(2));
		assessmentService.updateAssessmentEntry(ae2);
		AssessmentEntry aeOtherEntry = assessmentService.getOrCreateAssessmentEntry(identity1, null, entryOther, subIdent, Boolean.FALSE, null);
		aeOtherEntry.setAttempts(Integer.valueOf(3));
		assessmentService.updateAssessmentEntry(aeOtherEntry);
		AssessmentEntry aeOtherSubIdent = assessmentService.getOrCreateAssessmentEntry(identity1, null, entry, subIdenOther, Boolean.FALSE, null);
		aeOtherSubIdent.setAttempts(Integer.valueOf(4));
		assessmentService.updateAssessmentEntry(aeOtherSubIdent);
		AssessmentEntry aeOtherIdentity = assessmentService.getOrCreateAssessmentEntry(identityOther, null, entry, subIdent, Boolean.FALSE, null);
		aeOtherIdentity.setAttempts(Integer.valueOf(5));
		assessmentService.updateAssessmentEntry(aeOtherIdentity);
		
		Map<Long, Integer> attempts = sut.getAttempts(entry, node, asList(identity1, identity2));
		
		assertThat(attempts.size()).isEqualTo(2);
		assertThat(attempts.get(identity1.getKey())).isEqualTo(1);
		assertThat(attempts.get(identity2.getKey())).isEqualTo(2);
	}
	
	@Test
	public void shouldLoadPassed() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry entryOther = JunitTestHelper.createAndPersistRepositoryEntry();
		CourseNode node = new SPCourseNode();
		String subIdent = node.getIdent();
		String subIdenOther = random();
		Identity identityPassed1 = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Identity identityPassed2 = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Identity identityNotUserVisible = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Identity identityNoUserVisibility = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Identity identityFailed = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Identity identityNotGraded = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Identity identityOtherEntry = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Identity identityOtherSubEntry = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Identity identityNotTarget = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		List<Identity> targetIdentities = asList(identityPassed1, identityPassed2, identityNotUserVisible,
				identityNoUserVisibility, identityFailed, identityNotGraded, identityOtherEntry, identityOtherSubEntry);
		
		AssessmentEntry aePassed1 = assessmentService.getOrCreateAssessmentEntry(identityPassed1, null, entry, subIdent, Boolean.FALSE, null);
		aePassed1.setPassed(Boolean.TRUE);
		aePassed1.setUserVisibility(Boolean.TRUE);
		assessmentService.updateAssessmentEntry(aePassed1);
		AssessmentEntry aePassed2 = assessmentService.getOrCreateAssessmentEntry(identityPassed2, null, entry, subIdent, Boolean.FALSE, null);
		aePassed2.setPassed(Boolean.TRUE);
		aePassed2.setUserVisibility(Boolean.TRUE);
		assessmentService.updateAssessmentEntry(aePassed2);
		AssessmentEntry aeNotUserVisible = assessmentService.getOrCreateAssessmentEntry(identityNotUserVisible, null, entry, subIdent, Boolean.FALSE, null);
		aeNotUserVisible.setPassed(Boolean.TRUE);
		aeNotUserVisible.setUserVisibility(Boolean.FALSE);
		assessmentService.updateAssessmentEntry(aeNotUserVisible);
		AssessmentEntry aeNoUserVisibility = assessmentService.getOrCreateAssessmentEntry(identityNoUserVisibility, null, entry, subIdent, Boolean.FALSE, null);
		aeNoUserVisibility.setPassed(Boolean.TRUE);
		aeNoUserVisibility.setUserVisibility(null);
		assessmentService.updateAssessmentEntry(aeNoUserVisibility);
		AssessmentEntry aeFailed = assessmentService.getOrCreateAssessmentEntry(identityFailed, null, entry, subIdent, Boolean.FALSE, null);
		aeFailed.setPassed(Boolean.FALSE);
		aeFailed.setUserVisibility(Boolean.TRUE);
		assessmentService.updateAssessmentEntry(aeFailed);
		AssessmentEntry aeNotGraded = assessmentService.getOrCreateAssessmentEntry(identityNotGraded, null, entry, subIdent, Boolean.FALSE, null);
		aeNotGraded.setPassed(null);
		aeNotGraded.setUserVisibility(Boolean.TRUE);
		assessmentService.updateAssessmentEntry(aeNotGraded);
		AssessmentEntry aeOtherEntry = assessmentService.getOrCreateAssessmentEntry(identityOtherEntry, null, entryOther, subIdent, Boolean.FALSE, null);
		aeOtherEntry.setPassed(Boolean.TRUE);
		aeOtherEntry.setUserVisibility(Boolean.TRUE);
		assessmentService.updateAssessmentEntry(aeOtherEntry);
		AssessmentEntry aeOtherSubIdent = assessmentService.getOrCreateAssessmentEntry(identityOtherSubEntry, null, entry, subIdenOther, Boolean.FALSE, null);
		aeOtherSubIdent.setPassed(Boolean.TRUE);
		aeOtherSubIdent.setUserVisibility(Boolean.TRUE);
		assessmentService.updateAssessmentEntry(aeOtherSubIdent);
		AssessmentEntry aeOtherIdentity = assessmentService.getOrCreateAssessmentEntry(identityNotTarget, null, entry, subIdent, Boolean.FALSE, null);
		aeOtherIdentity.setPassed(Boolean.FALSE);
		aeOtherIdentity.setUserVisibility(Boolean.TRUE);
		assessmentService.updateAssessmentEntry(aeOtherIdentity);
		
		List<Long> keys = sut.getPassed(entry, node, targetIdentities, Set.of(Status.gradedPassed));
		assertThat(keys).containsExactlyInAnyOrder(
						identityPassed1.getKey(),
						identityPassed2.getKey())
				.doesNotContain(
						identityNotUserVisible.getKey(),
						identityNoUserVisibility.getKey(),
						identityFailed.getKey(),
						identityNotGraded.getKey(),
						identityOtherEntry.getKey(),
						identityOtherSubEntry.getKey(),
						identityNotTarget.getKey()
				);
		
		keys = sut.getPassed(entry, node, targetIdentities, Set.of(Status.gradedFailed, Status.notGraded));
		assertThat(keys).containsExactlyInAnyOrder(
						identityNotUserVisible.getKey(), // = not graded
						identityNoUserVisibility.getKey(), // = not graded
						identityFailed.getKey(),
						identityNotGraded.getKey())
				.doesNotContain(
						identityPassed1.getKey(),
						identityPassed2.getKey(),
						identityOtherEntry.getKey(),
						identityOtherSubEntry.getKey(),
						identityNotTarget.getKey()
				);
	}
	
	@Test
	public void shouldLoadCompletions() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry entryOther = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity identity1 = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Identity identity2 = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Identity identityOther = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		
		AssessmentEntry ae1 = assessmentService.getOrCreateAssessmentEntry(identity1, null, entry, "top", Boolean.TRUE, null);
		ae1.setCompletion(Double.valueOf(90));
		assessmentService.updateAssessmentEntry(ae1);
		AssessmentEntry ae1Sub = assessmentService.getOrCreateAssessmentEntry(identity1, null, entry, "sub", Boolean.FALSE, null);
		ae1Sub.setCompletion(Double.valueOf(80));
		assessmentService.updateAssessmentEntry(ae1Sub);
		AssessmentEntry ae2 = assessmentService.getOrCreateAssessmentEntry(identity2, null, entry, "top", Boolean.TRUE, null);
		ae2.setCompletion(Double.valueOf(70));
		assessmentService.updateAssessmentEntry(ae2);
		AssessmentEntry aeOtherIdentity = assessmentService.getOrCreateAssessmentEntry(identityOther, null, entry, "top", Boolean.TRUE, null);
		aeOtherIdentity.setCompletion(Double.valueOf(60));
		assessmentService.updateAssessmentEntry(aeOtherIdentity);
		AssessmentEntry aeOtherEntry = assessmentService.getOrCreateAssessmentEntry(identity1, null, entryOther, "top", Boolean.TRUE, null);
		aeOtherEntry.setCompletion(Double.valueOf(50));
		assessmentService.updateAssessmentEntry(aeOtherEntry);
		dbInstance.commitAndCloseSession();
		
		Map<Long, Double> completions = sut.getRootCompletions(entry, asList(identity1, identity2));
		
		assertThat(completions).hasSize(2);
		assertThat(completions.get(identity1.getKey())).isEqualTo(90);
		assertThat(completions.get(identity2.getKey())).isEqualTo(70);
	}

	@Test
	public void shouldLoadLastAttemptDates() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry entryOther = JunitTestHelper.createAndPersistRepositoryEntry();
		CourseNode node = new SPCourseNode();
		String subIdent = node.getIdent();
		String subIdenOther = random();
		Identity identity1 = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Identity identity2 = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Identity identityNotVisited = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Identity identityOther = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		
		AssessmentEntry ae1 = assessmentService.getOrCreateAssessmentEntry(identity1, null, entry, subIdent, Boolean.FALSE, null);
		Date date1 = (new GregorianCalendar(2020, 2, 3)).getTime();
		ae1.setLastAttempt(date1);
		assessmentService.updateAssessmentEntry(ae1);
		AssessmentEntry ae2 = assessmentService.getOrCreateAssessmentEntry(identity2, null, entry, subIdent, Boolean.FALSE, null);
		Date date2 = (new GregorianCalendar(2020, 2, 4)).getTime();
		ae2.setLastAttempt(date2);
		assessmentService.updateAssessmentEntry(ae2);
		AssessmentEntry aeOtherEntry = assessmentService.getOrCreateAssessmentEntry(identity1, null, entryOther, subIdent, Boolean.FALSE, null);
		Date dateOtherEntry = (new GregorianCalendar(2020, 2, 5)).getTime();
		aeOtherEntry.setLastAttempt(dateOtherEntry);
		assessmentService.updateAssessmentEntry(aeOtherEntry);
		AssessmentEntry aeOtherSubIdent = assessmentService.getOrCreateAssessmentEntry(identity1, null, entry, subIdenOther, Boolean.FALSE, null);
		Date dateOtherSubIdent = (new GregorianCalendar(2020, 2, 6)).getTime();
		aeOtherSubIdent.setLastAttempt(dateOtherSubIdent);
		assessmentService.updateAssessmentEntry(aeOtherSubIdent);
		AssessmentEntry aeOtherIdentity = assessmentService.getOrCreateAssessmentEntry(identityOther, null, entry, subIdent, Boolean.FALSE, null);
		Date dateOtherIdentity = (new GregorianCalendar(2020, 2, 6)).getTime();
		aeOtherIdentity.setLastAttempt(dateOtherIdentity);
		assessmentService.updateAssessmentEntry(aeOtherIdentity);
		// No attempts
		assessmentService.getOrCreateAssessmentEntry(identityNotVisited, null, entry, subIdent, Boolean.FALSE, null);
		
		Map<Long, Date> lastAttemptsDates = sut.getLastAttemptsDates(entry, node, List.of(identity1, identity2));
		
		assertThat(lastAttemptsDates).hasSize(2);
		assertThat(lastAttemptsDates.get(identity1.getKey())).isCloseTo(date1, 1000);
		assertThat(lastAttemptsDates.get(identity2.getKey())).isCloseTo(date2, 1000);
	}

}

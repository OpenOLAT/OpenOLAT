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
package org.olat.course.learningpath.manager;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.olat.core.util.DateUtils;
import org.olat.course.assessment.MappedScoreAccounting;
import org.olat.course.nodeaccess.NoAccessResolver.NoAccessReason;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.SPCourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.ObligationOverridable;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentObligation;

/**
 * 
 * Initial date: 15 Nov 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LearningPathNoAccessResolverTest {

	@Test
	public void shouldGetStartInFuture() {
		MappedScoreAccounting scoreAccounting = new MappedScoreAccounting();
		
		CourseNode root = new STCourseNode();
		CourseNode child1 = new SPCourseNode();
		root.addChild(child1);
		scoreAccounting.put(child1, of(AssessmentObligation.mandatory, Boolean.TRUE, AssessmentEntryStatus.done, null));
		CourseNode child2 = new SPCourseNode();
		root.addChild(child2);
		scoreAccounting.put(child2, of(AssessmentObligation.mandatory, Boolean.FALSE, AssessmentEntryStatus.notReady, inFuture()));
		CourseNode child3 = new SPCourseNode();
		root.addChild(child3);
		scoreAccounting.put(child3, of(AssessmentObligation.mandatory, Boolean.FALSE, AssessmentEntryStatus.notReady, inFuture()));
		CourseNode child4 = new SPCourseNode();
		root.addChild(child4);
		scoreAccounting.put(child4, of(AssessmentObligation.mandatory, Boolean.FALSE, AssessmentEntryStatus.notReady, null));
		
		UserCourseEnvironment userCourseEnv = mock(UserCourseEnvironment.class);
		when(userCourseEnv.getScoreAccounting()).thenReturn(scoreAccounting);

		LearningPathNoAccessResolver sut = new LearningPathNoAccessResolver(userCourseEnv, root);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(sut.getNoAccessMessage(child1).getReason()).as("Child 1 is accessible").isEqualTo(NoAccessReason.unknown);
		softly.assertThat(sut.getNoAccessMessage(child2).getReason()).as("Child 2 has start in future").isEqualTo(NoAccessReason.startDateInFuture);
		softly.assertThat(sut.getNoAccessMessage(child3).getReason()).as("Child 3 previous not done").isEqualTo(NoAccessReason.previousNotDone);
		softly.assertThat(sut.getNoAccessMessage(child3).getGoToNodeIdent()).as("Child 3 is blocked by child 2").isEqualTo(child2.getIdent());
		softly.assertThat(sut.getNoAccessMessage(child4).getReason()).as("Child 4 previous not done").isEqualTo(NoAccessReason.previousNotDone);
		softly.assertThat(sut.getNoAccessMessage(child3).getGoToNodeIdent()).as("Child 4 is blocked by child 2").isEqualTo(child2.getIdent());
		softly.assertAll();
	}
	
	@Test
	public void shouldGetLastAccessibleCourseNode() {
		MappedScoreAccounting scoreAccounting = new MappedScoreAccounting();
		
		CourseNode root = new STCourseNode();
		CourseNode child1 = new SPCourseNode();
		root.addChild(child1);
		scoreAccounting.put(child1, of(AssessmentObligation.mandatory, Boolean.TRUE, AssessmentEntryStatus.done, null));
		CourseNode child2 = new SPCourseNode();
		root.addChild(child2);
		scoreAccounting.put(child2, of(AssessmentObligation.mandatory, Boolean.FALSE, AssessmentEntryStatus.inProgress, null));
		CourseNode child3 = new SPCourseNode();
		root.addChild(child3);
		scoreAccounting.put(child3, of(AssessmentObligation.mandatory, Boolean.FALSE, AssessmentEntryStatus.notReady, null));
		CourseNode child4 = new SPCourseNode();
		root.addChild(child4);
		scoreAccounting.put(child4, of(AssessmentObligation.mandatory, Boolean.FALSE, AssessmentEntryStatus.notReady, null));
		CourseNode child5 = new SPCourseNode();
		root.addChild(child5);
		scoreAccounting.put(child5, of(AssessmentObligation.mandatory, Boolean.TRUE, AssessmentEntryStatus.done, null));
		CourseNode child6 = new SPCourseNode();
		root.addChild(child6);
		scoreAccounting.put(child6, of(AssessmentObligation.mandatory, Boolean.FALSE, AssessmentEntryStatus.notReady, null));
		
		UserCourseEnvironment userCourseEnv = mock(UserCourseEnvironment.class);
		when(userCourseEnv.getScoreAccounting()).thenReturn(scoreAccounting);

		LearningPathNoAccessResolver sut = new LearningPathNoAccessResolver(userCourseEnv, root);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(sut.getNoAccessMessage(child1).getReason()).as("Child 1 is accessible").isEqualTo(NoAccessReason.unknown);
		softly.assertThat(sut.getNoAccessMessage(child2).getReason()).as("Child 2 is accessible").isEqualTo(NoAccessReason.unknown);
		softly.assertThat(sut.getNoAccessMessage(child3).getReason()).as("Child 3 previous not done").isEqualTo(NoAccessReason.previousNotDone);
		softly.assertThat(sut.getNoAccessMessage(child3).getGoToNodeIdent()).as("Child 3 is blocked by child 2").isEqualTo(child2.getIdent());
		softly.assertThat(sut.getNoAccessMessage(child4).getReason()).as("Child 4 previous not done").isEqualTo(NoAccessReason.previousNotDone);
		softly.assertThat(sut.getNoAccessMessage(child4).getGoToNodeIdent()).as("Child 4 is blocked by child 2").isEqualTo(child2.getIdent());
		softly.assertThat(sut.getNoAccessMessage(child5).getReason()).as("Child 5 is accessible").isEqualTo(NoAccessReason.unknown);
		softly.assertThat(sut.getNoAccessMessage(child6).getReason()).as("Child 6 previous not done").isEqualTo(NoAccessReason.previousNotDone);
		softly.assertThat(sut.getNoAccessMessage(child6).getGoToNodeIdent()).as("Child 6 is blocked by child 2").isEqualTo(child2.getIdent());
		softly.assertAll();
	}
	
	@Test
	public void shouldGetLastAccessibleNotFullyAssessedCourseNode() {
		MappedScoreAccounting scoreAccounting = new MappedScoreAccounting();
		
		CourseNode root = new STCourseNode();
		CourseNode child1 = new SPCourseNode();
		root.addChild(child1);
		scoreAccounting.put(child1, of(AssessmentObligation.optional, Boolean.FALSE, AssessmentEntryStatus.notStarted, null));
		CourseNode child2 = new SPCourseNode();
		root.addChild(child2);
		scoreAccounting.put(child2, of(AssessmentObligation.mandatory, Boolean.TRUE, AssessmentEntryStatus.done, null));
		CourseNode child3 = new SPCourseNode();
		root.addChild(child3);
		scoreAccounting.put(child3, of(AssessmentObligation.mandatory, Boolean.FALSE, AssessmentEntryStatus.inProgress, null));
		CourseNode child4 = new SPCourseNode();
		root.addChild(child4);
		scoreAccounting.put(child4, of(AssessmentObligation.mandatory, Boolean.FALSE, AssessmentEntryStatus.notReady, null));
		CourseNode child5 = new SPCourseNode();
		root.addChild(child5);
		scoreAccounting.put(child5, of(AssessmentObligation.mandatory, Boolean.TRUE, AssessmentEntryStatus.done, null));
		CourseNode child6 = new SPCourseNode();
		root.addChild(child6);
		scoreAccounting.put(child6, of(AssessmentObligation.mandatory, Boolean.FALSE, AssessmentEntryStatus.notReady, null));
		
		UserCourseEnvironment userCourseEnv = mock(UserCourseEnvironment.class);
		when(userCourseEnv.getScoreAccounting()).thenReturn(scoreAccounting);

		LearningPathNoAccessResolver sut = new LearningPathNoAccessResolver(userCourseEnv, root);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(sut.getNoAccessMessage(child1).getReason()).as("Child 1 is accessible").isEqualTo(NoAccessReason.unknown);
		softly.assertThat(sut.getNoAccessMessage(child2).getReason()).as("Child 2 is accessible").isEqualTo(NoAccessReason.unknown);
		softly.assertThat(sut.getNoAccessMessage(child3).getReason()).as("Child 3 is accessible").isEqualTo(NoAccessReason.unknown);
		softly.assertThat(sut.getNoAccessMessage(child4).getReason()).as("Child 4 previous not done").isEqualTo(NoAccessReason.previousNotDone);
		softly.assertThat(sut.getNoAccessMessage(child4).getGoToNodeIdent()).as("Child 4 is blocked by child 3").isEqualTo(child3.getIdent());
		softly.assertThat(sut.getNoAccessMessage(child5).getReason()).as("Child 5 is accessible").isEqualTo(NoAccessReason.unknown);
		softly.assertThat(sut.getNoAccessMessage(child6).getReason()).as("Child 6 previous not done").isEqualTo(NoAccessReason.previousNotDone);
		softly.assertThat(sut.getNoAccessMessage(child6).getGoToNodeIdent()).as("Child 6 is blocked by child 3").isEqualTo(child3.getIdent());
		softly.assertAll();
	}
	
	@Test
	public void shouldGetLastAccessibleNotExcludedCourseNode() {
		MappedScoreAccounting scoreAccounting = new MappedScoreAccounting();
		
		CourseNode root = new STCourseNode();
		CourseNode child1 = new SPCourseNode();
		root.addChild(child1);
		scoreAccounting.put(child1, of(AssessmentObligation.mandatory, Boolean.TRUE, AssessmentEntryStatus.done, null));
		CourseNode child2 = new SPCourseNode();
		root.addChild(child2);
		scoreAccounting.put(child2, of(AssessmentObligation.excluded, Boolean.FALSE, AssessmentEntryStatus.inProgress, null));
		CourseNode child3 = new SPCourseNode();
		root.addChild(child3);
		scoreAccounting.put(child3, of(AssessmentObligation.mandatory, Boolean.FALSE, AssessmentEntryStatus.inProgress, null));
		CourseNode child4 = new SPCourseNode();
		root.addChild(child4);
		scoreAccounting.put(child4, of(AssessmentObligation.mandatory, Boolean.FALSE, AssessmentEntryStatus.notReady, null));
		
		UserCourseEnvironment userCourseEnv = mock(UserCourseEnvironment.class);
		when(userCourseEnv.getScoreAccounting()).thenReturn(scoreAccounting);

		LearningPathNoAccessResolver sut = new LearningPathNoAccessResolver(userCourseEnv, root);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(sut.getNoAccessMessage(child1).getReason()).as("Child 1 is accessible").isEqualTo(NoAccessReason.unknown);
		softly.assertThat(sut.getNoAccessMessage(child2).getReason()).as("Child 2 is accessible").isEqualTo(NoAccessReason.unknown);
		softly.assertThat(sut.getNoAccessMessage(child3).getReason()).as("Child 3 is accessible").isEqualTo(NoAccessReason.unknown);
		softly.assertThat(sut.getNoAccessMessage(child4).getReason()).as("Child 4 previous not done").isEqualTo(NoAccessReason.previousNotDone);
		softly.assertThat(sut.getNoAccessMessage(child4).getGoToNodeIdent()).as("Child 4 is blocked by child 3").isEqualTo(child3.getIdent());
		softly.assertAll();
	}
	
	private AssessmentEvaluation of(AssessmentObligation obligation, Boolean fullyAssessed,
			AssessmentEntryStatus status, Date startDate) {
		return new AssessmentEvaluation(null, null, null, null, null, null, null, status, null, fullyAssessed, null,
				null, null, null, null, null, null, 0, null, null, null, null, startDate, null,
				ObligationOverridable.of(obligation), null, null, null);
	}

	private Date inFuture() {
		return DateUtils.addDays(new Date(), 3);
	}


}

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

import java.util.Date;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.olat.core.util.DateUtils;
import org.olat.course.learningpath.model.SequenceConfigImpl;
import org.olat.course.learningpath.ui.LearningPathTreeNode;
import org.olat.course.nodeaccess.NoAccessResolver.NoAccessReason;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.SPCourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
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
		LearningPathTreeNode root = add(null, false, new STCourseNode(), ae(AssessmentObligation.mandatory, Boolean.FALSE, AssessmentEntryStatus.inReview, null));
		LearningPathTreeNode child1 = add(root, true, new SPCourseNode(), ae(AssessmentObligation.mandatory, Boolean.TRUE, AssessmentEntryStatus.done, null));
		LearningPathTreeNode child2 = add(root, true, new SPCourseNode(), ae(AssessmentObligation.mandatory, Boolean.FALSE, AssessmentEntryStatus.notReady, inFuture()));
		LearningPathTreeNode child3 = add(root, true, new SPCourseNode(), ae(AssessmentObligation.mandatory, Boolean.FALSE, AssessmentEntryStatus.notReady, inFuture()));
		LearningPathTreeNode child4 = add(root, true, new SPCourseNode(), ae(AssessmentObligation.mandatory, Boolean.FALSE, AssessmentEntryStatus.notReady, null));

		LearningPathNoAccessResolver sut = new LearningPathNoAccessResolver(root);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(sut.getNoAccessMessage(child1.getCourseNode()).getReason()).as("Child 1 is accessible").isEqualTo(NoAccessReason.unknown);
		softly.assertThat(sut.getNoAccessMessage(child2.getCourseNode()).getReason()).as("Child 2 has start in future").isEqualTo(NoAccessReason.startDateInFuture);
		softly.assertThat(sut.getNoAccessMessage(child3.getCourseNode()).getReason()).as("Child 3 has start in future").isEqualTo(NoAccessReason.startDateInFuture);
		softly.assertThat(sut.getNoAccessMessage(child4.getCourseNode()).getReason()).as("Child 4 previous not done").isEqualTo(NoAccessReason.previousNotDone);
		softly.assertThat(sut.getNoAccessMessage(child4.getCourseNode()).getGoToNodeIdent()).as("Child 4 is blocked by child 2").isEqualTo(child2.getIdent());
		softly.assertAll();
	}
	
	@Test
	public void shouldGetFirstMandatoryNotFullyAssessedCourseNode_first() {
		LearningPathTreeNode root = add(null, false, new STCourseNode(), ae(AssessmentObligation.mandatory, Boolean.FALSE, AssessmentEntryStatus.inReview, null));
		LearningPathTreeNode child1 = add(root, true, new SPCourseNode(), ae(AssessmentObligation.mandatory, Boolean.TRUE, AssessmentEntryStatus.done, null));
		LearningPathTreeNode child2 = add(root, true, new SPCourseNode(), ae(AssessmentObligation.mandatory, Boolean.FALSE, AssessmentEntryStatus.inProgress, null));
		LearningPathTreeNode child3 = add(root, true, new SPCourseNode(), ae(AssessmentObligation.mandatory, Boolean.FALSE, AssessmentEntryStatus.notReady, null));
		LearningPathTreeNode child4 = add(root, true, new SPCourseNode(), ae(AssessmentObligation.mandatory, Boolean.FALSE, AssessmentEntryStatus.notReady, null));
		LearningPathTreeNode child5 = add(root, true, new SPCourseNode(), ae(AssessmentObligation.mandatory, Boolean.TRUE, AssessmentEntryStatus.done, null));
		LearningPathTreeNode child6 = add(root, true, new SPCourseNode(), ae(AssessmentObligation.mandatory, Boolean.FALSE, AssessmentEntryStatus.notReady, null));
		
		LearningPathNoAccessResolver sut = new LearningPathNoAccessResolver(root);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(sut.getNoAccessMessage(child1.getCourseNode()).getReason()).as("Child 1 is accessible").isEqualTo(NoAccessReason.unknown);
		softly.assertThat(sut.getNoAccessMessage(child2.getCourseNode()).getReason()).as("Child 2 is accessible").isEqualTo(NoAccessReason.unknown);
		softly.assertThat(sut.getNoAccessMessage(child3.getCourseNode()).getReason()).as("Child 3 previous not done").isEqualTo(NoAccessReason.previousNotDone);
		softly.assertThat(sut.getNoAccessMessage(child3.getCourseNode()).getGoToNodeIdent()).as("Child 3 is blocked by child 2").isEqualTo(child2.getIdent());
		softly.assertThat(sut.getNoAccessMessage(child4.getCourseNode()).getReason()).as("Child 4 previous not done").isEqualTo(NoAccessReason.previousNotDone);
		softly.assertThat(sut.getNoAccessMessage(child4.getCourseNode()).getGoToNodeIdent()).as("Child 4 is blocked by child 2").isEqualTo(child2.getIdent());
		softly.assertThat(sut.getNoAccessMessage(child5.getCourseNode()).getReason()).as("Child 5 is accessible").isEqualTo(NoAccessReason.unknown);
		softly.assertThat(sut.getNoAccessMessage(child6.getCourseNode()).getReason()).as("Child 6 previous not done").isEqualTo(NoAccessReason.previousNotDone);
		softly.assertThat(sut.getNoAccessMessage(child6.getCourseNode()).getGoToNodeIdent()).as("Child 6 is blocked by child 2").isEqualTo(child2.getIdent());
		softly.assertAll();
	}
	
	@Test
	public void shouldGetFirstMandatoryNotFullyAssessedCourseNode_mandatory() {
		LearningPathTreeNode root = add(null, false, new STCourseNode(), ae(AssessmentObligation.mandatory, Boolean.FALSE, AssessmentEntryStatus.inReview, null));
		LearningPathTreeNode child1 = add(root, true, new SPCourseNode(), ae(AssessmentObligation.optional, Boolean.FALSE, AssessmentEntryStatus.notStarted, null));
		LearningPathTreeNode child2 = add(root, true, new SPCourseNode(), ae(AssessmentObligation.mandatory, Boolean.TRUE, AssessmentEntryStatus.done, null));
		LearningPathTreeNode child3 = add(root, true, new SPCourseNode(), ae(AssessmentObligation.mandatory, Boolean.FALSE, AssessmentEntryStatus.inProgress, null));
		LearningPathTreeNode child4 = add(root, true, new SPCourseNode(), ae(AssessmentObligation.mandatory, Boolean.FALSE, AssessmentEntryStatus.notReady, null));
		LearningPathTreeNode child5 = add(root, true, new SPCourseNode(), ae(AssessmentObligation.mandatory, Boolean.TRUE, AssessmentEntryStatus.done, null));
		LearningPathTreeNode child6 = add(root, true, new SPCourseNode(), ae(AssessmentObligation.mandatory, Boolean.FALSE, AssessmentEntryStatus.notReady, null));

		LearningPathNoAccessResolver sut = new LearningPathNoAccessResolver(root);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(sut.getNoAccessMessage(child1.getCourseNode()).getReason()).as("Child 1 is accessible").isEqualTo(NoAccessReason.unknown);
		softly.assertThat(sut.getNoAccessMessage(child2.getCourseNode()).getReason()).as("Child 2 is accessible").isEqualTo(NoAccessReason.unknown);
		softly.assertThat(sut.getNoAccessMessage(child3.getCourseNode()).getReason()).as("Child 3 is accessible").isEqualTo(NoAccessReason.unknown);
		softly.assertThat(sut.getNoAccessMessage(child4.getCourseNode()).getReason()).as("Child 4 previous not done").isEqualTo(NoAccessReason.previousNotDone);
		softly.assertThat(sut.getNoAccessMessage(child4.getCourseNode()).getGoToNodeIdent()).as("Child 4 is blocked by child 3").isEqualTo(child3.getIdent());
		softly.assertThat(sut.getNoAccessMessage(child5.getCourseNode()).getReason()).as("Child 5 is accessible").isEqualTo(NoAccessReason.unknown);
		softly.assertThat(sut.getNoAccessMessage(child6.getCourseNode()).getReason()).as("Child 6 previous not done").isEqualTo(NoAccessReason.previousNotDone);
		softly.assertThat(sut.getNoAccessMessage(child6.getCourseNode()).getGoToNodeIdent()).as("Child 6 is blocked by child 3").isEqualTo(child3.getIdent());
		softly.assertAll();
	}
	
	@Test
	public void shouldGetFirstMandatoryNotFullyAssessedCourseNode_excluded() {
		LearningPathTreeNode root = add(null, false, new STCourseNode(), ae(AssessmentObligation.mandatory, Boolean.FALSE, AssessmentEntryStatus.inReview, null));
		LearningPathTreeNode child1 = add(root, true, new SPCourseNode(), ae(AssessmentObligation.mandatory, Boolean.TRUE, AssessmentEntryStatus.done, null));
		LearningPathTreeNode child2 = add(root, true, new SPCourseNode(), ae(AssessmentObligation.excluded, Boolean.FALSE, AssessmentEntryStatus.inProgress, null));
		LearningPathTreeNode child3 = add(root, true, new SPCourseNode(), ae(AssessmentObligation.mandatory, Boolean.FALSE, AssessmentEntryStatus.inProgress, null));
		LearningPathTreeNode child4 = add(root, true, new SPCourseNode(), ae(AssessmentObligation.mandatory, Boolean.FALSE, AssessmentEntryStatus.notReady, null));

		LearningPathNoAccessResolver sut = new LearningPathNoAccessResolver(root);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(sut.getNoAccessMessage(child1.getCourseNode()).getReason()).as("Child 1 is accessible").isEqualTo(NoAccessReason.unknown);
		softly.assertThat(sut.getNoAccessMessage(child2.getCourseNode()).getReason()).as("Child 2 is accessible").isEqualTo(NoAccessReason.unknown);
		softly.assertThat(sut.getNoAccessMessage(child3.getCourseNode()).getReason()).as("Child 3 is accessible").isEqualTo(NoAccessReason.unknown);
		softly.assertThat(sut.getNoAccessMessage(child4.getCourseNode()).getReason()).as("Child 4 previous not done").isEqualTo(NoAccessReason.previousNotDone);
		softly.assertThat(sut.getNoAccessMessage(child4.getCourseNode()).getGoToNodeIdent()).as("Child 4 is blocked by child 3").isEqualTo(child3.getIdent());
		softly.assertAll();
	}
	
	@Test
	public void shouldGetLastAccessibleCourseNode_mix() {
		LearningPathTreeNode root = add(null, false, new STCourseNode(), ae(AssessmentObligation.mandatory, Boolean.FALSE, AssessmentEntryStatus.inReview, null));
		LearningPathTreeNode child1 = add(root, true, new SPCourseNode(), ae(AssessmentObligation.mandatory, Boolean.TRUE, AssessmentEntryStatus.done, null));
		LearningPathTreeNode child2 = add(root, true, new SPCourseNode(), ae(AssessmentObligation.mandatory, Boolean.FALSE, AssessmentEntryStatus.inProgress, null));
		LearningPathTreeNode child3 = add(root, true, new SPCourseNode(), ae(AssessmentObligation.mandatory, Boolean.TRUE, AssessmentEntryStatus.done, null));
		LearningPathTreeNode child4 = add(root, true, new SPCourseNode(), ae(AssessmentObligation.mandatory, Boolean.FALSE, AssessmentEntryStatus.notReady, inFuture()));
		LearningPathTreeNode child5 = add(root, true, new SPCourseNode(), ae(AssessmentObligation.mandatory, Boolean.TRUE, AssessmentEntryStatus.done, null));
		LearningPathTreeNode child6 = add(root, true, new SPCourseNode(), ae(AssessmentObligation.mandatory, Boolean.FALSE, AssessmentEntryStatus.notReady, null));
		
		LearningPathNoAccessResolver sut = new LearningPathNoAccessResolver(root);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(sut.getNoAccessMessage(child1.getCourseNode()).getReason()).as("Child 1 is accessible").isEqualTo(NoAccessReason.unknown);
		softly.assertThat(sut.getNoAccessMessage(child2.getCourseNode()).getReason()).as("Child 2 is accessible").isEqualTo(NoAccessReason.unknown);
		softly.assertThat(sut.getNoAccessMessage(child3.getCourseNode()).getReason()).as("Child 3 is accessible").isEqualTo(NoAccessReason.unknown);
		softly.assertThat(sut.getNoAccessMessage(child4.getCourseNode()).getReason()).as("Child 4 has start in future").isEqualTo(NoAccessReason.startDateInFuture);
		softly.assertThat(sut.getNoAccessMessage(child5.getCourseNode()).getReason()).as("Child 5 is accessible").isEqualTo(NoAccessReason.unknown);
		softly.assertThat(sut.getNoAccessMessage(child6.getCourseNode()).getReason()).as("Child 6 previous not done").isEqualTo(NoAccessReason.previousNotDone);
		softly.assertThat(sut.getNoAccessMessage(child6.getCourseNode()).getGoToNodeIdent()).as("Child 6 is blocked by child 4").isEqualTo(child4.getIdent());
		softly.assertAll();
	}
	@Test
	public void shouldGetLastAccessibleCourseNode_not_sequention() {
		LearningPathTreeNode root = add(null, false, new STCourseNode(), ae(AssessmentObligation.mandatory, Boolean.FALSE, AssessmentEntryStatus.inReview, null));
		LearningPathTreeNode child1 = add(root, false, new SPCourseNode(), ae(AssessmentObligation.mandatory, Boolean.TRUE, AssessmentEntryStatus.done, null));
		LearningPathTreeNode child2 = add(root, false, new STCourseNode(), ae(AssessmentObligation.mandatory, Boolean.FALSE, AssessmentEntryStatus.inProgress, null));
		LearningPathTreeNode child21 = add(child2, true, new SPCourseNode(), ae(AssessmentObligation.mandatory, Boolean.FALSE, AssessmentEntryStatus.inProgress, null));
		LearningPathTreeNode child22 = add(child2, true, new SPCourseNode(), ae(AssessmentObligation.mandatory, Boolean.FALSE, AssessmentEntryStatus.notReady, null));
		LearningPathTreeNode child3 = add(root, false, new SPCourseNode(), ae(AssessmentObligation.mandatory, Boolean.FALSE, AssessmentEntryStatus.notStarted, null));
		LearningPathTreeNode child4 = add(root, false, new STCourseNode(), ae(AssessmentObligation.mandatory, Boolean.FALSE, AssessmentEntryStatus.notStarted, null));
		LearningPathTreeNode child41 = add(child4, true, new SPCourseNode(), ae(AssessmentObligation.mandatory, Boolean.FALSE, AssessmentEntryStatus.inProgress, null));
		LearningPathTreeNode child42 = add(child4, true, new SPCourseNode(), ae(AssessmentObligation.mandatory, Boolean.FALSE, AssessmentEntryStatus.notReady, null));
		LearningPathTreeNode child5 = add(root, false, new SPCourseNode(), ae(AssessmentObligation.mandatory, Boolean.TRUE, AssessmentEntryStatus.notStarted, null));
		LearningPathTreeNode child6 = add(root, false, new SPCourseNode(), ae(AssessmentObligation.mandatory, Boolean.FALSE, AssessmentEntryStatus.notStarted, null));
		
		LearningPathNoAccessResolver sut = new LearningPathNoAccessResolver(root);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(sut.getNoAccessMessage(child1.getCourseNode()).getReason()).as("Child 1 is accessible").isEqualTo(NoAccessReason.unknown);
		softly.assertThat(sut.getNoAccessMessage(child2.getCourseNode()).getReason()).as("Child 2 is accessible").isEqualTo(NoAccessReason.unknown);
		softly.assertThat(sut.getNoAccessMessage(child21.getCourseNode()).getReason()).as("Child 21 is accessible").isEqualTo(NoAccessReason.unknown);
		softly.assertThat(sut.getNoAccessMessage(child22.getCourseNode()).getReason()).as("Child 22 is accessible").isEqualTo(NoAccessReason.previousNotDone);
		softly.assertThat(sut.getNoAccessMessage(child22.getCourseNode()).getGoToNodeIdent()).as("Child 22 is blocked by child 21").isEqualTo(child21.getIdent());
		softly.assertThat(sut.getNoAccessMessage(child3.getCourseNode()).getReason()).as("Child 3 is accessible").isEqualTo(NoAccessReason.unknown);
		softly.assertThat(sut.getNoAccessMessage(child4.getCourseNode()).getReason()).as("Child 4 is accessible").isEqualTo(NoAccessReason.unknown);
		softly.assertThat(sut.getNoAccessMessage(child41.getCourseNode()).getReason()).as("Child 41 is accessible").isEqualTo(NoAccessReason.unknown);
		softly.assertThat(sut.getNoAccessMessage(child42.getCourseNode()).getReason()).as("Child 42 is accessible").isEqualTo(NoAccessReason.previousNotDone);
		softly.assertThat(sut.getNoAccessMessage(child42.getCourseNode()).getGoToNodeIdent()).as("Child 42 is blocked by child 41").isEqualTo(child41.getIdent());
		softly.assertThat(sut.getNoAccessMessage(child5.getCourseNode()).getReason()).as("Child 5 is accessible").isEqualTo(NoAccessReason.unknown);
		softly.assertThat(sut.getNoAccessMessage(child6.getCourseNode()).getReason()).as("Child 6 is accessible").isEqualTo(NoAccessReason.unknown);
		softly.assertAll();
	}
	
	private LearningPathTreeNode add(LearningPathTreeNode parent, boolean inSequence, CourseNode courseNode, AssessmentEvaluation ae) {
		SequenceConfigImpl sequenceConfig = new SequenceConfigImpl(inSequence, false);
		LearningPathTreeNode treeNode = new LearningPathTreeNode(courseNode, 0, sequenceConfig, ae, false);
		if (parent != null) {
			parent.addChild(treeNode);
		}
		return treeNode;
	}
	
	private AssessmentEvaluation ae(AssessmentObligation obligation, Boolean fullyAssessed,
			AssessmentEntryStatus status, Date startDate) {
		return new AssessmentEvaluation(null, null, null, null, null, null, null, status, null, fullyAssessed, null,
				null, null, null, null, null, null, 0, null, null, null, null, startDate, null,
				ObligationOverridable.of(obligation), null, null, null);
	}

	private Date inFuture() {
		return DateUtils.addDays(new Date(), 3);
	}


}

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
package org.olat.course.run.scoring;

import java.util.Date;
import java.util.List;

import org.olat.course.condition.interpreter.ConditionInterpreter;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.st.assessment.WithoutSequenceBlocker;
import org.olat.modules.assessment.Overridable;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 17 Sep 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
class AccountingEvaluatorsFactory {
	
	private static final BlockerEvaluator UNCHANGING_BLOCKER_EVALUATOR = new UnchangingBlockerEvaluator();
	private static final StartDateEvaluator NO_BLOCKING_START_DATE_EVALUATOR = new NoBlockingStartDateEvaluator();
	private static final EndDateEvaluator NO_BLOCKING_END_DATE_EVALUATOR = new NoBlockingEndDateEvaluator();
	private static final ObligationEvaluator NONE_OBLIGATION_EVALUATOR = new NoneObligationEvaluator();
	private static final DurationEvaluator NULL_DURATION_EVALUATOR = new NullDurationEvaluator();
	private static final ScoreEvaluator UNCHANGING_SCORE_EVALUATOR = new UnchangingScoreEvaluator();
	private static final ScoreEvaluator NULL_SCORE_EVALUATOR = new NullScoreEvaluator();
	private static final PassedEvaluator UNCHANGING_PASSED_EVALUATOR = new UnchangingPassedEvaluator();
	private static final RootPassedEvaluator UNCHANGING_ROOT_PASSED_EVALUATOR = new UnchangingRootPassedEvaluator();
	private static final CompletionEvaluator UNCHANGING_COMPLETION_EVALUATOR = new UnchangingCompletionEvaluator();
	private static final StatusEvaluator UNCHANGING_STATUS_EVALUATOR = new UnchangingStatusEvaluator();
	private static final FullyAssessedEvaluator UNCHANGING_FULLY_ASSESSED_EVALUATOR = new UnchangingFullyAssessedEvaluator();
	private static final LastModificationsEvaluator UNCHANGING_LAST_MODIFICATIONS_EVALUATOR = new UnchangingLastModificationEvaluator();
	
	static BlockerEvaluator createUnchangingBlockerEvaluator() {
		return UNCHANGING_BLOCKER_EVALUATOR;
	}
	
	static StartDateEvaluator createNoBlockingStartDateEvaluator() {
		return NO_BLOCKING_START_DATE_EVALUATOR;
	}
	
	static EndDateEvaluator createNoBlockingEndDateEvaluator() {
		return NO_BLOCKING_END_DATE_EVALUATOR;
	}
	
	static ObligationEvaluator createNoneObligationEvaluator() {
		return NONE_OBLIGATION_EVALUATOR;
	}
	
	static DurationEvaluator createNullDurationEvaluator() {
		return NULL_DURATION_EVALUATOR;
	}
	
	static ScoreEvaluator createUnchangingScoreEvaluator() {
		return UNCHANGING_SCORE_EVALUATOR;
	}
	
	static ScoreEvaluator createNullScoreEvaluator() {
		return NULL_SCORE_EVALUATOR;
	}
	
	static PassedEvaluator createUnchangingPassedEvaluator() {
		return UNCHANGING_PASSED_EVALUATOR;
	}
	
	static RootPassedEvaluator createUnchangingRootPassedEvaluator() {
		return UNCHANGING_ROOT_PASSED_EVALUATOR;
	}
	
	public static CompletionEvaluator createUnchangingCompletionEvaluator() {
		return UNCHANGING_COMPLETION_EVALUATOR;
	}
	
	static StatusEvaluator createUnchangingStatusEvaluator() {
		return UNCHANGING_STATUS_EVALUATOR;
	}
	
	static FullyAssessedEvaluator createUnchangingFullyAssessedEvaluator() {
		return UNCHANGING_FULLY_ASSESSED_EVALUATOR;
	}
	
	static LastModificationsEvaluator createUnchangingLastModificationsEvaluator() {
		return UNCHANGING_LAST_MODIFICATIONS_EVALUATOR;
	}
	
	
	private AccountingEvaluatorsFactory() {
		//
	}
	
	private static class UnchangingBlockerEvaluator implements BlockerEvaluator {

		@Override
		public Blocker getChildrenBlocker(Blocker blocker) {
			return blocker != null? blocker: new WithoutSequenceBlocker(null);
		}

		@Override
		public void mergeChildrenBlocker(Blocker blocker, Blocker childrenBlocker) {
			//
		}
		
	}
	
	private static class NoBlockingStartDateEvaluator implements StartDateEvaluator {
		
		@Override
		public Date evaluate(AssessmentEvaluation currentEvaluation, CourseNode courseNode, Blocker blocker) {
			return null;
		}
		
	}

	private static class NoBlockingEndDateEvaluator implements EndDateEvaluator {

		@Override
		public Overridable<Date> getEndDate(AssessmentEvaluation currentEvaluation, CourseNode courseNode, Blocker blocker) {
			return Overridable.empty();
		}
		
	}

	private static class NoneObligationEvaluator implements ObligationEvaluator {

		@Override
		public Overridable<AssessmentObligation> getObligation(AssessmentEvaluation currentEvaluation, CourseNode courseNode) {
			return Overridable.empty();
		}

		@Override
		public Overridable<AssessmentObligation> getObligation(AssessmentEvaluation currentEvaluation,
				List<AssessmentEvaluation> children) {
			return Overridable.empty();
		}
		
	}
	
	private static class NullDurationEvaluator implements DurationEvaluator {

		@Override
		public boolean isDependingOnCurrentNode() {
			return false;
		}

		@Override
		public Integer getDuration(CourseNode courseNode) {
			return null;
		}

		@Override
		public boolean isDependingOnChildNodes() {
			return false;
		}

		@Override
		public Integer getDuration(List<AssessmentEvaluation> children) {
			return null;
		}
		
	}
	
	private static class UnchangingScoreEvaluator implements ScoreEvaluator {
		
		@Override
		public Float getScore(AssessmentEvaluation currentEvaluation, CourseNode courseNode, ScoreAccounting scoreAccounting, ConditionInterpreter conditionInterpreter) {
			return currentEvaluation.getScore();
		}
		
	}
	
	private static class NullScoreEvaluator implements ScoreEvaluator {
		
		@Override
		public Float getScore(AssessmentEvaluation currentEvaluation, CourseNode courseNode, ScoreAccounting scoreAccounting, ConditionInterpreter conditionInterpreter) {
			return null;
		}
		
	}
	
	private static class UnchangingPassedEvaluator implements PassedEvaluator {
		
		@Override
		public Boolean getPassed(AssessmentEvaluation currentEvaluation, CourseNode courseNode,
				RepositoryEntry courseEntry, ConditionInterpreter conditionInterpreter) {
			return currentEvaluation.getPassed();
		}
	}
	
	private static class UnchangingRootPassedEvaluator implements RootPassedEvaluator {
		
		@Override
		public Boolean getPassed(AssessmentEvaluation currentEvaluation, CourseNode courseNode,
				ScoreAccounting scoreAccounting, RepositoryEntry courseEntry) {
			return currentEvaluation.getPassed();
		}
	}
	
	private static class UnchangingCompletionEvaluator implements CompletionEvaluator {

		@Override
		public Double getCompletion(AssessmentEvaluation currentEvaluation, CourseNode courseNode,
				ScoreAccounting scoureAccounting) {
			return currentEvaluation.getCompletion();
		}
		
	}
	
	private static class UnchangingStatusEvaluator implements StatusEvaluator {

		@Override
		public AssessmentEntryStatus getStatus(AssessmentEvaluation currentEvaluation,
				Blocker blocker) {
			return currentEvaluation.getAssessmentStatus();
		}

		@Override
		public AssessmentEntryStatus getStatus(AssessmentEvaluation currentEvaluation,
				List<AssessmentEvaluation> children) {
			return currentEvaluation.getAssessmentStatus();
		}
		
	}
	
	private static class UnchangingFullyAssessedEvaluator implements FullyAssessedEvaluator {

		@Override
		public Boolean getFullyAssessed(AssessmentEvaluation currentEvaluation, List<AssessmentEvaluation> children,
				Blocker blocker) {
			return currentEvaluation.getFullyAssessed();
		}
		
	}
	
	private static class UnchangingLastModificationEvaluator implements LastModificationsEvaluator {

		@Override
		public LastModifications getLastModifications(AssessmentEvaluation currentEvaluation, List<AssessmentEvaluation> children) {
			return LastModificationsEvaluator.of(currentEvaluation.getLastUserModified(), currentEvaluation.getLastCoachModified());
		}
		
	}

}

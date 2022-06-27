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
package org.olat.course.learningpath.evaluation;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.course.Structure;
import org.olat.course.learningpath.LearningPathService;
import org.olat.course.learningpath.obligation.ExceptionalObligation;
import org.olat.course.learningpath.obligation.ExceptionalObligationHandler;
import org.olat.course.run.scoring.ObligationContext;
import org.olat.course.run.scoring.ScoreAccounting;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 4 Oct 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ExceptionalObligationEvaluator {

	private final Identity identity;
	private final RepositoryEntryRef courseEntry;
	private final Structure runStructure;
	private final ScoreAccounting scoreAccounting;
	private ObligationContext obligationContext;

	private LearningPathService learningPathService;
	
	public ExceptionalObligationEvaluator(Identity identity, RepositoryEntryRef courseEntry, Structure runStructure, ScoreAccounting scoreAccounting) {
		this.identity = identity;
		this.courseEntry = courseEntry;
		this.runStructure = runStructure;
		this.scoreAccounting = scoreAccounting;
	}
	
	public void setObligationContext(ObligationContext obligationContext) {
		this.obligationContext = obligationContext;
	}

	public Set<AssessmentObligation> filterAssessmentObligation(List<ExceptionalObligation> exceptionalObligations, AssessmentObligation defaultObligation) {
				return filter(exceptionalObligations, defaultObligation)
						.map(ExceptionalObligation::getObligation)
						.collect(Collectors.toSet());
			}

	public List<ExceptionalObligation> filterExceptionalObligations(List<ExceptionalObligation> exceptionalObligations, AssessmentObligation defaultObligation) {
				return filter(exceptionalObligations, defaultObligation)
						.collect(Collectors.toList());
			}

	private Stream<ExceptionalObligation> filter(List<ExceptionalObligation> exceptionalObligations, AssessmentObligation defaultObligation) {
		return exceptionalObligations.stream()
				.filter(eo -> defaultObligation != eo.getObligation()) // not exceptional if the same obligation
				.filter(eo -> matchesIdentity(eo, obligationContext));
	}

	private boolean matchesIdentity( ExceptionalObligation exceptionalObligation, ObligationContext obligationContext) {
		ExceptionalObligationHandler exceptionalObligationHandler = getLearningPathService().getExceptionalObligationHandler(exceptionalObligation.getType());
		if (exceptionalObligationHandler != null) {
			return exceptionalObligationHandler.matchesIdentity(exceptionalObligation, identity, obligationContext, courseEntry, runStructure, scoreAccounting);
		}
		return false;
	}

	protected LearningPathService getLearningPathService() {
		if (learningPathService == null) {
			learningPathService = CoreSpringFactory.getImpl(LearningPathService.class);
		}
		return learningPathService;
	}

	/**
	 * For Testing only!
	 *
	 * @param learningPathService
	 */
	protected void setLearningPathService(LearningPathService learningPathService) {
		this.learningPathService = learningPathService;
	}

}
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
package org.olat.course.nodes.st.assessment;

import java.util.Date;

import org.apache.logging.log4j.Logger;
import org.hibernate.LazyInitializationException;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.DateUtils;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.nodes.st.assessment.AssessmentCounter.AssessmentCounts;
import org.olat.course.nodes.st.assessment.PassCounter.Counts;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.RootPassedEvaluator;
import org.olat.course.run.scoring.ScoreAccounting;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.RepositoryEntryLifecycle;

/**
 * 
 * Initial date: 13 Mar 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class STRootPassedEvaluator implements RootPassedEvaluator {

	private static final Logger log = Tracing.createLoggerFor(STRootPassedEvaluator.class);
	
	private final PassCounter passCounter;
	private final AssessmentCounter assessmentCounter;
	
	public STRootPassedEvaluator() {
		this(new PassCounter(), new AssessmentCounter());
	}
	
	STRootPassedEvaluator(PassCounter passCounter, AssessmentCounter assessmentCounter) {
		this.passCounter = passCounter;
		this.assessmentCounter = assessmentCounter;
	}

	@Override
	public GradePassed getPassed(AssessmentEvaluation currentEvaluation, CourseNode courseNode,
			ScoreAccounting scoreAccounting, RepositoryEntry courseEntry, Identity assessedIdentity) {
		Boolean currentPassed = currentEvaluation.getPassedOverridable().isOverridden()
				? currentEvaluation.getPassedOverridable().getOriginal()
				: currentEvaluation.getPassedOverridable().getCurrent();
		if (currentPassed != null && currentPassed.booleanValue()) {
			// Never reset a passed course to null or failed
			return GradePassed.of(currentPassed);
		}
		
		ModuleConfiguration config = courseNode.getModuleConfiguration();
		
		// Progress
		if (config.getBooleanSafe(STCourseNode.CONFIG_PASSED_PROGRESS)) {
			// Completion may not be safe due to not precise double rounding.
			// So we use fully assessed as an accurate value
			// and supplement it with a almost complete course.
			Boolean fullyAssessed = currentEvaluation.getFullyAssessed();
			Double completion = currentEvaluation.getCompletion();
			if (fullyAssessed != null && fullyAssessed.booleanValue() && completion != null && completion.intValue() >= 0.999) {
				return GradePassed.passedTrue();
			}
		}
		
		// Points
		if (config.getBooleanSafe(STCourseNode.CONFIG_PASSED_POINTS)) {
			Float score = currentEvaluation.getScore();
			if (score != null) {
				int cutValue = config.getIntegerSafe(STCourseNode.CONFIG_PASSED_POINTS_CUT, Integer.MAX_VALUE);
				if (score.floatValue() >= cutValue) {
					return GradePassed.passedTrue();
				}
			}
		}
		
		// Number passed
		if (config.getBooleanSafe(STCourseNode.CONFIG_PASSED_NUMBER)) {
			int cutValue = config.getIntegerSafe(STCourseNode.CONFIG_PASSED_NUMBER_CUT, Integer.MAX_VALUE);
			Counts counts = passCounter.getCounts(courseEntry, courseNode, scoreAccounting);
			if (counts.getPassed() >= cutValue) {
				return GradePassed.passedTrue();
			}
		}
		
		// All passed
		if (config.getBooleanSafe(STCourseNode.CONFIG_PASSED_ALL)) {
			Counts counts = passCounter.getCounts(courseEntry, courseNode, scoreAccounting);
			if (counts.getPassable() > 0) {
				if (counts.isAllAssessed() && counts.getPassable() == counts.getPassed()) {
					return GradePassed.passedTrue();
				}
				if (counts.getFailed() > 0 && getActivePassedConfigs(config) == 1) {
					return GradePassed.passedFalse();
				}
				if (getActivePassedConfigs(config) == 1 && !isFullyAssessed(currentEvaluation)) {
					return GradePassed.none();
				}
			}
		}
	
		if (currentPassed == null && getActivePassedConfigs(config) > 0) {
			AssessmentCounts assessmentCounts = assessmentCounter.getCounts(courseEntry, courseNode, scoreAccounting);
			if (assessmentCounts.getNumAssessable() > 0) {
				
				// Failed if all assessable course elements are assessed.
				if (assessmentCounts.isAllAssessed()) {
					return GradePassed.passedFalse();
				}
			
				// Failed if course end date is over
				RepositoryEntryLifecycle lifecycle = getLifecycle(courseEntry);
				if (lifecycle != null && lifecycle.getValidTo() != null) {
					Date validTo =  DateUtils.setTime(lifecycle.getValidTo(), 23, 59, 59);
					if (validTo.before(new Date())) {
						return GradePassed.passedFalse();
					}
				}
			}
		}
		
		return GradePassed.of(currentPassed);
	}
	
	private boolean isFullyAssessed(AssessmentEvaluation currentEvaluation) {
		Boolean fullyAssessed = currentEvaluation.getFullyAssessed();
		return fullyAssessed != null && fullyAssessed.booleanValue();
	}

	public static int getActivePassedConfigs(ModuleConfiguration config) {
		int active = 0;
		if (config.has(STCourseNode.CONFIG_PASSED_PROGRESS)) {
			active++;
		}
		if (config.has(STCourseNode.CONFIG_PASSED_ALL)) {
			active++;
		}
		if (config.has(STCourseNode.CONFIG_PASSED_NUMBER)) {
			active++;
		}
		if (config.has(STCourseNode.CONFIG_PASSED_POINTS)) {
			active++;
		}
		return active;
	}
	
	private RepositoryEntryLifecycle getLifecycle(RepositoryEntry courseEntry) {
		RepositoryEntryLifecycle lifecycle = null;
		
		if (courseEntry != null) {
			try {
				lifecycle = courseEntry.getLifecycle();
				if(lifecycle != null) {
					lifecycle.getValidTo();
				}
			} catch (LazyInitializationException lie) {
				RepositoryService repositoryService = CoreSpringFactory.getImpl(RepositoryService.class);
				RepositoryEntry repositoryEntry = repositoryService.loadByKey(courseEntry.getKey());
				lifecycle = repositoryEntry.getLifecycle();
			} catch (Exception e) {
				log.error("", e);
			}
		}
		
		return lifecycle;
	}

}

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

import org.olat.core.CoreSpringFactory;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.nodes.CourseNode;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 28 Oct 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ConfigMaxScoreEvaluator implements MaxScoreEvaluator {
	
	private CourseAssessmentService courseAssessmentService;

	@Override
	public Float getMaxScore(AssessmentEvaluation currentEvaluation, CourseNode courseNode, ScoreAccounting scoreAccounting, RepositoryEntryRef courseEntry) {
		AssessmentConfig assessmentConfig = getCourseAssessmentService().getAssessmentConfig(courseEntry, courseNode);
		return Mode.none != assessmentConfig.getScoreMode()? assessmentConfig.getMaxScore(): null;
	}
	
	private CourseAssessmentService getCourseAssessmentService() {
		if (courseAssessmentService == null) {
			courseAssessmentService = CoreSpringFactory.getImpl(CourseAssessmentService.class);
		}
		return courseAssessmentService;
	}

}

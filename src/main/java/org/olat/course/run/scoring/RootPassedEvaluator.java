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

import org.olat.core.id.Identity;
import org.olat.course.nodes.CourseNode;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 13 Mar 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface RootPassedEvaluator {

	public GradePassed getPassed(AssessmentEvaluation currentEvaluation, CourseNode courseNode,
			ScoreAccounting scoreAccounting, RepositoryEntry courseEntry, Identity assessedIdentity);
	
	public static final class GradePassed {
		
		private static final GradePassed NONE = of(null, null, null, null);
		private static final GradePassed PASSED_TRUE = of(null, null, null, Boolean.TRUE);
		private static final GradePassed PASSED_FALSE = of(null, null, null, Boolean.FALSE);
		
		private final String grade;
		private final String gradeSystemIdent;
		private final String performanceClassIdent;
		private final Boolean passed;
		
		public static final GradePassed none() {
			return NONE;
		}
		
		public static final GradePassed passedTrue() {
			return PASSED_TRUE;
		}
		
		public static final GradePassed passedFalse() {
			return PASSED_FALSE;
		}
		
		public static final GradePassed of(Boolean passed) {
			if (passed != null) {
				return passed.booleanValue()? PASSED_TRUE: PASSED_FALSE;
			}
			return NONE;
		}
		
		public static final GradePassed of(String grade, String gradeSystemIdent, String performanceClassIdent, Boolean passed) {
			return new GradePassed(grade, gradeSystemIdent, performanceClassIdent, passed);
		}
		
		private GradePassed(String grade, String gradeSystemIdent, String performanceClassIdent, Boolean passed) {
			this.grade = grade;
			this.gradeSystemIdent = gradeSystemIdent;
			this.performanceClassIdent = performanceClassIdent;
			this.passed = passed;
		}

		public String getGrade() {
			return grade;
		}

		public String getGradeSystemIdent() {
			return gradeSystemIdent;
		}

		public String getPerformanceClassIdent() {
			return performanceClassIdent;
		}

		public Boolean getPassed() {
			return passed;
		}
		
	}

}

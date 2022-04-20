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

import org.olat.core.CoreSpringFactory;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.core.util.tree.Visitor;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.nodes.CourseNode;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 9 Mar 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
class MaxScoreCumulator {
	
	public interface MaxScore {
		
		public Float getSum();
		
		public Float getMax();
	}
	
	private CourseAssessmentService courseAssessmentService;
	
	// see CumulatingMaxScoreEvaluator
	MaxScore getMaxScore(RepositoryEntryRef courseEntry, CourseNode courseNode) {
		return getMaxScore(courseEntry, courseNode, courseAssessmentService());
	}
	
	MaxScore getMaxScore(RepositoryEntryRef courseEntry, CourseNode courseNode, CourseAssessmentService courseAssessmentService) {
		ScoreVisitor visitor = new ScoreVisitor(courseEntry, courseNode, courseAssessmentService);
		TreeVisitor treeVisitor = new TreeVisitor(visitor, courseNode, true);
		treeVisitor.visitAll();
		return visitor;
	}
	
	private CourseAssessmentService courseAssessmentService() {
		if (courseAssessmentService== null) {
			courseAssessmentService = CoreSpringFactory.getImpl(CourseAssessmentService.class);
		}
		return courseAssessmentService;
	}
	
	private final static class ScoreVisitor implements MaxScore, Visitor {
		
		private final RepositoryEntryRef courseEntry;
		private final CourseNode root;
		private int count;
		private float sum;
		private float max = 0;
		
		private final CourseAssessmentService courseAssessmentService;
		
		private ScoreVisitor(RepositoryEntryRef courseEntry, CourseNode root, CourseAssessmentService courseAssessmentService) {
			this.courseEntry = courseEntry;
			this.root = root;
			this.courseAssessmentService = courseAssessmentService;
		}
		
		@Override
		public Float getSum() {
			return count > 0? Float.valueOf(sum): null;
		}

		@Override
		public Float getMax() {
			return count > 0? Float.valueOf(max): null;
		}

		@Override
		public void visit(INode node) {
			if (node.getIdent().equals(root.getIdent())) return;
			
			if (node instanceof CourseNode) {
				CourseNode courseNode = (CourseNode)node;
				AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(courseEntry, courseNode);
				if (Mode.setByNode == assessmentConfig.getScoreMode() && !assessmentConfig.ignoreInCourseAssessment()) {
					Float maxScore = assessmentConfig.getMaxScore();
					if (maxScore != null) {
						count++;
						sum += maxScore.floatValue();
						if (max < maxScore.floatValue()) {
							max = maxScore.floatValue();
						}
					}
				}
			}
		}
	}
	
}

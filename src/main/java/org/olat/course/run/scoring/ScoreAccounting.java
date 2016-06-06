/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.course.run.scoring;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.core.util.tree.Visitor;
import org.olat.course.condition.interpreter.ConditionInterpreter;
import org.olat.course.nodes.AssessableCourseNode;
import org.olat.course.nodes.CalculatedAssessableCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.PersistentAssessableCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.model.AssessmentEntryStatus;

/**
 * Description:<BR/>
 * The score accounting contains all score evaluations for a user
 * <P/>
 * Initial Date:  Oct 12, 2004
 *
 * @author Felix Jost
 */
public class ScoreAccounting {
	
	private static final OLog log = Tracing.createLoggerFor(ScoreAccounting.class);

	private boolean error;
	private final UserCourseEnvironment userCourseEnvironment;
	private final Map<AssessableCourseNode, AssessmentEvaluation> cachedScoreEvals = new HashMap<>();

	/**
	 * Constructor of the user score accounting object
	 * @param userCourseEnvironment
	 */
	public ScoreAccounting(UserCourseEnvironment userCourseEnvironment) {
		this.userCourseEnvironment = userCourseEnvironment;
	}

	/**
	 * Retrieve all the score evaluations for all course nodes
	 */
	public void evaluateAll() {
		evaluateAll(false);
	}
	
	public boolean evaluateAll(boolean update) {
		Identity identity = userCourseEnvironment.getIdentityEnvironment().getIdentity();
		List<AssessmentEntry> entries = userCourseEnvironment.getCourseEnvironment()
				.getAssessmentManager().getAssessmentEntries(identity);

		AssessableTreeVisitor visitor = new AssessableTreeVisitor(entries, update);
		// collect all assessable nodes and eval 'em
		CourseNode root = userCourseEnvironment.getCourseEnvironment().getRunStructure().getRootNode();
		// breadth first traversal gives an easier order of evaluation for debugging
		// however, for live it is absolutely mandatory to use depth first since using breadth first
		// the score accoutings local cache hash map will never be used. this can slow down things like 
		// crazy (course with 10 tests, 300 users and some crazy score and passed calculations will have
		// 10 time performance differences) 

		cachedScoreEvals.clear();
		for(AssessmentEntry entry:entries) {
			String nodeIdent = entry.getSubIdent();
			CourseNode courseNode = userCourseEnvironment.getCourseEnvironment().getRunStructure().getNode(nodeIdent);
			if(courseNode instanceof AssessableCourseNode) {
				AssessableCourseNode acn = (AssessableCourseNode)courseNode;
				AssessmentEvaluation se = AssessmentEvaluation.toAssessmentEvalutation(entry, acn);
				cachedScoreEvals.put(acn, se);
			}
		}
		
		TreeVisitor tv = new TreeVisitor(visitor, root, true); // true=depth first
		tv.visitAll();
		return visitor.hasChanges();
	}
	
	private class AssessableTreeVisitor implements Visitor {
		
		private final boolean update;
		private boolean changes = false;
		private int recursionLevel = 0;
		private final Map<String,AssessmentEntry> identToEntries = new HashMap<>();
		
		public AssessableTreeVisitor(List<AssessmentEntry> entries, boolean update) {
			this.update = update;
			for(AssessmentEntry entry:entries) {
				String ident = entry.getSubIdent();
				if(identToEntries.containsKey(ident)) {
					AssessmentEntry currentEntry = identToEntries.get(ident);
					if(entry.getLastModified().after(currentEntry.getLastModified())) {
						identToEntries.put(ident, entry);
					}
				} else {
					identToEntries.put(ident, entry);
				}
			}
		}
		
		public boolean hasChanges() {
			return changes;
		}

		@Override
		public void visit(INode node) {
			CourseNode cn = (CourseNode) node;
			if (cn instanceof AssessableCourseNode) {
				evalCourseNode((AssessableCourseNode)cn);
			}
		}
		
		public AssessmentEvaluation evalCourseNode(AssessableCourseNode cn) {
			// make sure we have no circular calculations
			recursionLevel++;
			AssessmentEvaluation se = null;
			if (recursionLevel <= 15) {
				se = cachedScoreEvals.get(cn);
				if (se == null) { // result of this node has not been calculated yet, do it
					AssessmentEntry entry = identToEntries.get(cn.getIdent());
					if(cn instanceof PersistentAssessableCourseNode) {
						se = ((PersistentAssessableCourseNode)cn).getUserScoreEvaluation(entry);
					} else if(cn instanceof CalculatedAssessableCourseNode) {
						if(update) {
							se = calculateScoreEvaluation(entry, (CalculatedAssessableCourseNode)cn);
						} else {
							se = ((CalculatedAssessableCourseNode)cn).getUserScoreEvaluation(entry);
						}
					} else {
						se = cn.getUserScoreEvaluation(userCourseEnvironment);
					}
					cachedScoreEvals.put(cn, se);
				} else if(update && cn instanceof CalculatedAssessableCourseNode) {
					AssessmentEntry entry = identToEntries.get(cn.getIdent());
					se = calculateScoreEvaluation(entry, (CalculatedAssessableCourseNode)cn);
					cachedScoreEvals.put(cn, se);
				}
			}
			recursionLevel--;
			return se;
		}
		
		private AssessmentEvaluation calculateScoreEvaluation(AssessmentEntry entry, CalculatedAssessableCourseNode cNode) {
			AssessmentEvaluation se;
			if(cNode.hasScoreConfigured() || cNode.hasPassedConfigured()) {
				ScoreCalculator scoreCalculator = cNode.getScoreCalculator();
				String scoreExpressionStr = scoreCalculator.getScoreExpression();
				String passedExpressionStr = scoreCalculator.getPassedExpression();

				Float score = null;
				Boolean passed = null;
				AssessmentEntryStatus assessmentStatus = AssessmentEntryStatus.inProgress;
				ConditionInterpreter ci = userCourseEnvironment.getConditionInterpreter();
				if (cNode.hasScoreConfigured() && scoreExpressionStr != null) {
					score = new Float(ci.evaluateCalculation(scoreExpressionStr));
				}
				if (cNode.hasPassedConfigured() && passedExpressionStr != null) {
					boolean hasPassed = ci.evaluateCondition(passedExpressionStr);
					if(hasPassed) {
						passed = Boolean.TRUE;
						assessmentStatus = AssessmentEntryStatus.done;
					}
					//some rules to set -> failed
				}
				se = new AssessmentEvaluation(score, passed, null, assessmentStatus, null, null, null, null);
				
				if(entry == null) {
					Identity assessedIdentity = userCourseEnvironment.getIdentityEnvironment().getIdentity();
					userCourseEnvironment.getCourseEnvironment().getAssessmentManager()
						.createAssessmentEntry(cNode, assessedIdentity, se);
					changes = true;
				} else if(!same(se, entry)) {
					if(score != null) {
						entry.setScore(new BigDecimal(score));
					} else {
						entry.setScore(null);
					}
					entry.setPassed(passed);
					entry = userCourseEnvironment.getCourseEnvironment().getAssessmentManager().updateAssessmentEntry(entry);
					identToEntries.put(cNode.getIdent(), entry);
					changes = true;
				}
			} else {
				se = AssessmentEvaluation.EMPTY_EVAL;
			}
			return se;
		}
		
		private boolean same(AssessmentEvaluation se, AssessmentEntry entry) {
			boolean same = true;
			
			if((se.getPassed() == null && entry.getPassed() != null)
					|| (se.getPassed() != null && entry.getPassed() == null)
					|| (se.getPassed() != null && !se.getPassed().equals(entry.getPassed()))) {
				same &= false;
			}
			
			if((se.getScore() == null && entry.getScore() != null)
					|| (se.getScore() != null && entry.getScore() == null)
					|| (se.getScore() != null && entry.getScore() != null
							&& Math.abs(se.getScore().floatValue() - entry.getScore().floatValue()) > 0.00001)) {
				same &= false;
			}
			
			return same;
		}
	}

	/**
	 * Get the score evaluation for a given course node without using the cache.
	 * @param courseNode
	 * @return The score evaluation
	 */
	public AssessmentEvaluation getScoreEvaluation(CourseNode courseNode) {
		AssessmentEvaluation se = null;
		if (courseNode instanceof AssessableCourseNode) {
			AssessableCourseNode acn = (AssessableCourseNode) courseNode;
			se = acn.getUserScoreEvaluation(userCourseEnvironment);
		}
		return se;
	}

	/**
	 * Evaluates the course node or simply returns the evaluation from the cache.
	 * @param cn
	 * @return ScoreEvaluation
	 */
	public AssessmentEvaluation evalCourseNode(AssessableCourseNode cn) {
		AssessmentEvaluation se = cachedScoreEvals.get(cn);
		if (se == null) { // result of this node has not been calculated yet, do it
			se = cn.getUserScoreEvaluation(userCourseEnvironment);
			cachedScoreEvals.put(cn, se);
		}
		return se;
	}

	/**
	 * ----- to called by getScoreFunction only -----
	 * @param childId
	 * @return Float
	 */
	public Float evalScoreOfCourseNode(String childId) {
		CourseNode foundNode = findChildByID(childId);
		
		Float score = null;
		if (foundNode instanceof AssessableCourseNode) {
			AssessableCourseNode acn = (AssessableCourseNode) foundNode;
			ScoreEvaluation se = evalCourseNode(acn);
			if(se != null) { // the node could not provide any sensible information on scoring. e.g. a STNode with no calculating rules
				score = se.getScore();
			}
			if (score == null) { // a child has no score yet
				score = new Float(0.0f); // default to 0.0, so that the condition can be evaluated (zero points makes also the most sense for "no results yet", if to be expressed in a number)
			}
		} else {
			error = true;
			score = new Float(0.0f);
		}
		
		return score;
	}

	/**
	 * ----- to be called by getPassedFunction only -----
	 * @param childId
	 * @return Boolean
	 */
	public Boolean evalPassedOfCourseNode(String childId) {
		CourseNode foundNode = findChildByID(childId);
		if (foundNode == null) {
			error = true;
			return Boolean.FALSE;
		}
		if (!(foundNode instanceof AssessableCourseNode)) {
			error = true;
			return Boolean.FALSE;
		}
		AssessableCourseNode acn = (AssessableCourseNode) foundNode;
		ScoreEvaluation se = evalCourseNode(acn);
		if (se == null) { // the node could not provide any sensible information on scoring. e.g. a STNode with no calculating rules
			log.error("could not evaluate node '" + acn.getShortTitle() + "' (" + acn.getClass().getName() + "," + childId + ")", null);
		}
		Boolean passed = se.getPassed();
		if (passed == null) { // a child has no "Passed" yet
			passed = Boolean.FALSE;
		}
		return passed;
	}

	private CourseNode findChildByID(String id) {
		return userCourseEnvironment.getCourseEnvironment().getRunStructure().getNode(id);
	}

	/**
	 * @return true if an error occured
	 */
	public boolean isError() {
		return error;
	}
}


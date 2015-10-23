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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.id.Identity;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.util.Util;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.core.util.tree.Visitor;
import org.olat.course.nodes.AssessableCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.AssessmentEntry;

/**
 * Description:<BR/>
 * The score accounting contains all score evaluations for a user
 * <P/>
 * Initial Date:  Oct 12, 2004
 *
 * @author Felix Jost
 */
public class ScoreAccounting {
	private UserCourseEnvironment userCourseEnvironment;

	private boolean error;
	private CourseNode evaluatingCourseNode;
	private String wrongChildID;

	private Map<AssessableCourseNode, ScoreEvaluation> cachedScoreEvals = new HashMap<AssessableCourseNode, ScoreEvaluation>();
	private int recursionCnt;

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
		Identity identity = userCourseEnvironment.getIdentityEnvironment().getIdentity();
		List<AssessmentEntry> entries = userCourseEnvironment.getCourseEnvironment()
				.getAssessmentManager().getAssessmentEntries(identity);

		AssessableTreeVisitor visitor = new AssessableTreeVisitor(entries);
		// collect all assessable nodes and eval 'em
		CourseNode root = userCourseEnvironment.getCourseEnvironment().getRunStructure().getRootNode();
		// breadth first traversal gives an easier order of evaluation for debugging
		// however, for live it is absolutely mandatory to use depth first since using breadth first
		// the score accoutings local cache hash map will never be used. this can slow down things like 
		// crazy (course with 10 tests, 300 users and some crazy score and passed calculations will have
		// 10 time performance differences) 
		TreeVisitor tv = new TreeVisitor(visitor, root, true); // true=depth first
		tv.visitAll();
		cachedScoreEvals.clear();
		cachedScoreEvals.putAll(visitor.nodeToScoreEvals);
	}
	
	private class AssessableTreeVisitor implements Visitor {
		
		private int recursionLevel = 0;
		private final  Map<String,AssessmentEntry> identToEntries = new HashMap<>();
		private final  Map<AssessableCourseNode, ScoreEvaluation> nodeToScoreEvals = new HashMap<>();
		
		public AssessableTreeVisitor(List<AssessmentEntry> entries) {
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

		@Override
		public void visit(INode node) {
			CourseNode cn = (CourseNode) node;
			if (cn instanceof AssessableCourseNode) {
				evalCourseNode((AssessableCourseNode)cn);
			}
		}
		
		public ScoreEvaluation evalCourseNode(AssessableCourseNode cn) {
			// make sure we have no circular calculations
			recursionLevel++;
			ScoreEvaluation se = null;
			if (recursionLevel <= 15) {
				se = nodeToScoreEvals.get(cn);
				if (se == null) { // result of this node has not been calculated yet, do it
					AssessmentEntry entry = identToEntries.get(cn.getIdent());
					se = cn.getUserScoreEvaluation(entry);
					nodeToScoreEvals.put(cn, se);
				}
			}
			recursionLevel--;
			return se;
		}
	}

	/**
	 * FIXME:fj: cmp this method and evalCourseNode
	 * Get the score evaluation for a given course node
	 * @param courseNode
	 * @return The score evaluation
	 */
	public ScoreEvaluation getScoreEvaluation(CourseNode courseNode) {
		ScoreEvaluation se = null;
		if (courseNode instanceof AssessableCourseNode) {
			AssessableCourseNode acn = (AssessableCourseNode) courseNode;
			se = acn.getUserScoreEvaluation(userCourseEnvironment);
		}
		return se;
	}

	/**
	 * evals the coursenode or simply returns the evaluation from the cache
	 * @param cn
	 * @return ScoreEvaluation
	 */
	public ScoreEvaluation evalCourseNode(AssessableCourseNode cn) {
		// make sure we have no circular calculations
		recursionCnt++;
		if (recursionCnt > 15) throw new OLATRuntimeException("scoreaccounting.stackoverflow", 
				new String[]{cn.getIdent(), cn.getShortTitle()},
				Util.getPackageName(ScoreAccounting.class), 
				"stack overflow in scoreaccounting, probably circular logic: acn ="
				+ cn.toString(), null);

		ScoreEvaluation se = cachedScoreEvals.get(cn);
		if (se == null) { // result of this node has not been calculated yet, do it
			se = cn.getUserScoreEvaluation(userCourseEnvironment);
			cachedScoreEvals.put(cn, se);
		}
		recursionCnt--;
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
			wrongChildID = childId;
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
			wrongChildID = childId;
			return Boolean.FALSE;
		}
		if (!(foundNode instanceof AssessableCourseNode)) {
			error = true;
			wrongChildID = childId;
			return Boolean.FALSE;
		}
		AssessableCourseNode acn = (AssessableCourseNode) foundNode;
		ScoreEvaluation se = evalCourseNode(acn);
		if (se == null) { // the node could not provide any sensible information on scoring. e.g. a STNode with no calculating rules
			String msg = "could not evaluate node '" + acn.getShortTitle() + "' (" + acn.getClass().getName() + "," + childId + ")";
			new OLATRuntimeException(ScoreAccounting.class, "scoreaccounting.evaluationerror.score", 
					new String[]{acn.getIdent(), acn.getShortTitle()},
					Util.getPackageName(ScoreAccounting.class), 
					msg, null);
		}
		Boolean passed = se.getPassed();
		if (passed == null) { // a child has no "Passed" yet
			passed = Boolean.FALSE;
		}
		return passed;
	}

	/**
	 * Change the score information for the given course node
	 * @param acn
	 * @param se
	 */
	public void scoreInfoChanged(AssessableCourseNode acn, ScoreEvaluation se) {
		evaluateAll();
	}

	private CourseNode findChildByID(String id) {
		return userCourseEnvironment.getCourseEnvironment().getRunStructure().getNode(id);
	}

	/**
	 * used for error msg and debugging. denotes the coursenode which started a calculation.
	 * when an error occurs, we know which coursenode contains a faulty formula
	 * @param evaluatingCourseNode
	 */
	public void setEvaluatingCourseNode(CourseNode evaluatingCourseNode) {
		this.evaluatingCourseNode = evaluatingCourseNode;
	}

	/**
	 * @see org.olat.core.util.tree.Visitor#visit(org.olat.core.util.nodes.INode)
	 */
	public void visit(INode node) {
		CourseNode cn = (CourseNode) node;
		if (cn instanceof AssessableCourseNode) {
			AssessableCourseNode acn = (AssessableCourseNode) cn;
			evalCourseNode(acn);
			// evalCourseNode will cache all infos
		}
		// else: non assessable nodes are not interesting here
	}

	/**
	 * @return true if an error occured
	 */
	public boolean isError() {
		return error;
	}

	/** 
	 * @return CourseNode
	 */
	public CourseNode getEvaluatingCourseNode() {
		return evaluatingCourseNode;
	}

	/**
	 * @return int
	 */
	public int getRecursionCnt() {
		return recursionCnt;
	}
	
	/**
	 * @return String
	 */
	public String getWrongChildID() {
		return wrongChildID;
	}
}


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

import static java.util.stream.Collectors.joining;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.olat.course.condition.interpreter.score.GetAverageScoreFunction;

/**
 * Description:<br>
 * The score calculator stores the expression which is used to calculate a users
 * score and passed value for a structure node. The expression uses the same 
 * condition interpreter as in the node conditions.
 * 
 * <P>
 * @author Felix Jost
 */
public class ScoreCalculator implements Serializable {
	private String scoreExpression;
	private String passedExpression;
	private String failedExpression;
	
	public static final String SCORE_TYPE_NONE = "no";
	public static final String SCORE_TYPE_SUM = "sum";
	public static final String SCORE_TYPE_AVG = "avg";
	
	/** config flag: no passed configured **/
	public static final String PASSED_TYPE_NONE = "no";
	/** config flag: passed based on cutvalue **/
	public static final String PASSED_TYPE_CUTVALUE = "cut";
	/** config flag: passed inherited from other course nodes **/
	public static final String PASSED_TYPE_INHERIT = "inherit";
	
	public static final String PASSED_NODES_TYPE_ALL = "allnodes";
	public static final String PASSED_NODES_TYPE_PARTIAL = "partialnodes";
	
	
	private boolean expertMode = false;
	// The legacy expertMode is the fallback of the two individual expert modes.
	private Boolean scoreExpertMode;
	private Boolean passedExpertMode;
	// easy mode variables
	// score configuration
	private String scoreType;
	// nodes for all scoreTypes (not only sum)
	// Can't rename because of the XML serialization
	private List<String> sumOfScoreNodes;
	// passed configuration
	private String passedType;
	private List<String> passedNodes;
	private int passedCutValue;
	
	private String passedNodesType;
	private int numberOfNodesToPass = -1;
	
	private FailedEvaluationType failedType;
	
	public ScoreCalculator() {
		//
	}
	
	/**
	 * @return Returns the passedExpression. If null, then there is no expression to calculate.
	 */
	public String getPassedExpression() {
		// always return expression, even if in easy mode! whenever something in the easy mode
		// has been changed the one who changes something must also set the passedExpression
		// to the new correct value using something like
		// sc.setScoreExpression(sc.getScoreExpressionFromEasyModeConfiguration());
		return passedExpression;
	}
	
	/**
	 * @return Returns the scoreExpression. if null, then there is no expression to calculate
	 */
	public String getScoreExpression() {
		// always return expression, even if in easy mode! whenever something in the easy mode
		// has been changed the one who changes something must also set the passedExpression
		// to the new correct value using something like
		// sc.setScoreExpression(sc.getScoreExpressionFromEasyModeConfiguration());
		return scoreExpression;
	}
	
	public String getFailedExpression() {
		return failedExpression;
	}
	
	
	/**
	 * Calculate the score expression based on the easy mode configuration. This must not be used 
	 * during calculation of a score but after changing an expression in the editor to set the
	 * new score expression.
	 * 
	 * @return 
	 */
	public String getScoreExpressionFromEasyModeConfiguration() {
		if (getSumOfScoreNodes() != null && !getSumOfScoreNodes().isEmpty()) {
			if(scoreType == null || SCORE_TYPE_SUM.equals(scoreType)) {
				return getSumScoreExpression();
			} else if(SCORE_TYPE_AVG.equals(scoreType)) {
				return getAvgScoreExpression();
			}
		}
		return null;
	}

	private String getSumScoreExpression() {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		for(Iterator<String> iter = getSumOfScoreNodes().iterator(); iter.hasNext(); ) {
			String nodeIdent = iter.next();
			sb.append("getScore(\"");
			sb.append(nodeIdent);
			sb.append("\")");			
			if (iter.hasNext()) sb.append(" + ");
		}
		sb.append(")");
		return sb.toString();
	}
	
	private String getAvgScoreExpression() {
		return new StringBuilder()
			.append(GetAverageScoreFunction.NAME)
			.append("(\"")
			.append(getSumOfScoreNodes().stream().collect(joining("\",\"")))
			.append("\")")
			.toString();
	}

	/**
	 * Calculate the passed expression based on the easy mode configuration. This must not be used 
	 * during calculation of a passed but after changing an expression in the editor to set the
	 * new passed expression.
	 * 
	 * @return 
	 */
	public String getPassedExpressionFromEasyModeConfiguration() {
		if (getPassedType() == null || getPassedType().equals(PASSED_TYPE_NONE)) return null;
		StringBuilder sb = new StringBuilder();
		if (getPassedType().equals(PASSED_TYPE_INHERIT) && getPassedNodes() != null && !getPassedNodes().isEmpty()) {
			sb.append("(");
			if(PASSED_NODES_TYPE_PARTIAL.equals(getPassedNodesType())) {
				sb.append("getPassedNodes(");
				for(Iterator<String> iter = getPassedNodes().iterator(); iter.hasNext(); ) {
					sb.append("\"")
					  .append(iter.next())
					  .append("\",");
				}
				int numOfNodes = getNumberOfNodesToPass();
				sb.append("\"").append(numOfNodes).append("\"");
				sb.append(")");	
			} else {
				for(Iterator<String> iter = getPassedNodes().iterator(); iter.hasNext(); ) {
					String nodeIdent = iter.next();
					sb.append("getPassed(\"");
					sb.append(nodeIdent);
					sb.append("\")");			
					if (iter.hasNext()) sb.append(" & ");
				}
			}
			sb.append(")");
		} 
		else if (getPassedType().equals(PASSED_TYPE_CUTVALUE)) { 
			sb.append(getScoreExpressionFromEasyModeConfiguration());
			sb.append(" >= ");
			sb.append(getPassedCutValue());
		}
		
		return sb.length() > 0? sb.toString(): null;
	}
	
	public boolean isScoreExpertMode() {
		return scoreExpertMode != null? scoreExpertMode.booleanValue(): expertMode;
	}
	
	public void setScoreExpertMode(boolean scoreExpertMode) {
		this.scoreExpertMode = scoreExpertMode;
	}
	
	public boolean isPassedExpertMode() {
		return passedExpertMode != null? passedExpertMode.booleanValue(): expertMode;
	}
	
	public void setPassedExpertMode(boolean passedExpertMode) {
		this.passedExpertMode = passedExpertMode;
	}
	
	public String getScoreType() {
		return scoreType;
	}

	public void setScoreType(String scoreType) {
		this.scoreType = scoreType;
	}

	/**
	 * @return List of nodeIdents as Strings
	 */
	public List<String> getSumOfScoreNodes() {
		return sumOfScoreNodes;
	}
	
	public void setSumOfScoreNodes(List<String> sumOfScoreNodes) {
		this.sumOfScoreNodes = sumOfScoreNodes;
	}
	
	public void setPassedExpression(String passedExpression) {
		this.passedExpression = passedExpression;
	}
	
	public void setScoreExpression(String scoreExpression) {
		this.scoreExpression = scoreExpression;
	}
	
	public int getPassedCutValue() {
		return passedCutValue;
	}
	
	public void setPassedCutValue(int passedCutValue) {
		this.passedCutValue = passedCutValue;
	}

	/**
	 * @return List of nodeIdents as Strings
	 */
	public List<String> getPassedNodes() {
		return passedNodes;
	}
	
	public void setPassedNodes(List<String> passedNodes) {
		this.passedNodes = passedNodes;
	}
	
	public String getPassedType() {
		return passedType;
	}
	
	public void setPassedType(String passedType) {
		this.passedType = passedType;
	}
	
	public String getPassedNodesType() {
		return passedNodesType;
	}
	
	public void setPassedNodesType(String passedNodesType) {
		this.passedNodesType = passedNodesType;
	}
	
	public int getNumberOfNodesToPass() {
		return numberOfNodesToPass;
	}

	public void setNumberOfNodesToPass(int numberOfNodesToPass) {
		this.numberOfNodesToPass = numberOfNodesToPass;
	}

	public FailedEvaluationType getFailedType() {
		return failedType;
	}

	public void setFailedType(FailedEvaluationType failedType) {
		this.failedType = failedType;
	}

}

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
package org.olat.course.assessment.model;

import static org.olat.course.assessment.AssessmentHelper.KEY_ATTEMPTS;
import static org.olat.course.assessment.AssessmentHelper.KEY_DETAILS;
import static org.olat.course.assessment.AssessmentHelper.KEY_GRADE;
import static org.olat.course.assessment.AssessmentHelper.KEY_IDENTIFYER;
import static org.olat.course.assessment.AssessmentHelper.KEY_INDENT;
import static org.olat.course.assessment.AssessmentHelper.KEY_LAST_COACH_MODIFIED;
import static org.olat.course.assessment.AssessmentHelper.KEY_LAST_USER_MODIFIED;
import static org.olat.course.assessment.AssessmentHelper.KEY_MAX;
import static org.olat.course.assessment.AssessmentHelper.KEY_MIN;
import static org.olat.course.assessment.AssessmentHelper.KEY_PASSED;
import static org.olat.course.assessment.AssessmentHelper.KEY_PERFORMANCE_CLASS_IDENT;
import static org.olat.course.assessment.AssessmentHelper.KEY_SCORE;
import static org.olat.course.assessment.AssessmentHelper.KEY_SCORE_F;
import static org.olat.course.assessment.AssessmentHelper.KEY_SELECTABLE;
import static org.olat.course.assessment.AssessmentHelper.KEY_TITLE_LONG;
import static org.olat.course.assessment.AssessmentHelper.KEY_TITLE_SHORT;
import static org.olat.course.assessment.AssessmentHelper.KEY_TYPE;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.IndentedNodeRenderer.IndentedCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.modules.assessment.Overridable;
import org.olat.modules.assessment.model.AssessmentEntryStatus;

/**
 * 
 * Initial date: 23.10.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentNodeData implements IndentedCourseNode {
	
	private int recursionLevel;
	
	private String ident;
	private String type;
	private String shortTitle;
	private String longTitle;
	
	private String details;
	private Integer attempts;
	
	private Float score;
	private String roundedScore;
	private boolean ignoreInCourseAssessment;
	private FormItem scoreDesc;
	
	private Float maxScore;
	private Float minScore;
	private String grade;
	private String performanceClassIdent;
	
	private Boolean passed;
	private Overridable<Boolean> passedOverridable;
	
	private Boolean userVisibility;
	private AssessmentEntryStatus assessmentStatus;
	
	private int numOfAssessmentDocs;
	
	private Date lastModified;
	private Date lastUserModified;
	private Date lastCoachModified;
	
	private boolean selectable;
	private boolean onyx = false;
	
	public AssessmentNodeData() {
		//
	}
	
	public AssessmentNodeData(Map<String,Object> data) {
		fromMap(data);
	}
	
	public AssessmentNodeData(int indent, CourseNode courseNode) {
		this(indent, courseNode.getIdent(), courseNode.getType(), courseNode.getShortTitle(), courseNode.getLongTitle());
	}

	public AssessmentNodeData(int recursionLevel, String ident, String type, String shortTitle, String longTitle) {
		this.recursionLevel = recursionLevel;
		this.ident = ident;
		this.type = type;
		this.shortTitle = shortTitle;
		this.longTitle = longTitle;
	}

	public String getIdent() {
		return ident;
	}

	public void setIdent(String ident) {
		this.ident = ident;
	}

	@Override
	public int getRecursionLevel() {
		return recursionLevel;
	}

	public void setRecursionLevel(int recursionLevel) {
		this.recursionLevel = recursionLevel;
	}

	@Override
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String getShortTitle() {
		return shortTitle;
	}

	public void setShortTitle(String shortTitle) {
		this.shortTitle = shortTitle;
	}

	@Override
	public String getLongTitle() {
		return longTitle;
	}

	public void setLongTitle(String longTitle) {
		this.longTitle = longTitle;
	}

	public Integer getAttempts() {
		return attempts;
	}

	public void setAttempts(Integer attempts) {
		this.attempts = attempts;
	}

	public Boolean getUserVisibility() {
		return userVisibility;
	}

	public void setUserVisibility(Boolean userVisibility) {
		this.userVisibility = userVisibility;
	}

	public Float getScore() {
		return score;
	}

	public void setScore(Float score) {
		this.score = score;
	}

	public String getRoundedScore() {
		return roundedScore;
	}

	public void setRoundedScore(String roundedScore) {
		this.roundedScore = roundedScore;
	}

	public boolean isIgnoreInCourseAssessment() {
		return ignoreInCourseAssessment;
	}

	public void setIgnoreInCourseAssessment(boolean ignoreInCourseAssessment) {
		this.ignoreInCourseAssessment = ignoreInCourseAssessment;
	}

	public FormItem getScoreDesc() {
		return scoreDesc;
	}

	public void setScoreDesc(FormItem scoreDesc) {
		this.scoreDesc = scoreDesc;
	}

	public Float getMaxScore() {
		return maxScore;
	}

	public void setMaxScore(Float maxScore) {
		this.maxScore = maxScore;
	}

	public Float getMinScore() {
		return minScore;
	}

	public void setMinScore(Float minScore) {
		this.minScore = minScore;
	}

	public String getGrade() {
		return grade;
	}

	public void setGrade(String grade) {
		this.grade = grade;
	}

	public String getPerformanceClassIdent() {
		return performanceClassIdent;
	}

	public void setPerformanceClassIdent(String performanceClassIdent) {
		this.performanceClassIdent = performanceClassIdent;
	}

	public Boolean getPassed() {
		return passed;
	}

	public void setPassed(Boolean passed) {
		this.passed = passed;
	}
	
	public Overridable<Boolean> getPassedOverridable() {
		return passedOverridable;
	}

	public void setPassedOverridable(Overridable<Boolean> passedOverridable) {
		this.passedOverridable = passedOverridable;
	}

	public AssessmentEntryStatus getAssessmentStatus() {
		return assessmentStatus;
	}

	public void setAssessmentStatus(AssessmentEntryStatus assessmentStatus) {
		this.assessmentStatus = assessmentStatus;
	}

	public int getNumOfAssessmentDocs() {
		return numOfAssessmentDocs;
	}

	public void setNumOfAssessmentDocs(int numOfAssessmentDocs) {
		this.numOfAssessmentDocs = numOfAssessmentDocs;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public Date getLastUserModified() {
		return lastUserModified;
	}

	public void setLastUserModified(Date lastUserModified) {
		this.lastUserModified = lastUserModified;
	}

	public Date getLastCoachModified() {
		return lastCoachModified;
	}

	public void setLastCoachModified(Date lastCoachModified) {
		this.lastCoachModified = lastCoachModified;
	}

	public boolean isOnyx() {
		return onyx;
	}

	public void setOnyx(boolean onyx) {
		this.onyx = onyx;
	}

	public boolean isSelectable() {
		return selectable;
	}

	public void setSelectable(boolean selectable) {
		this.selectable = selectable;
	}

	public Map<String,Object> toMap() {
		Map<String,Object> nodeData = new HashMap<>();
		
		nodeData.put(KEY_INDENT, Integer.valueOf(recursionLevel));
		nodeData.put(KEY_TYPE, getType());
		nodeData.put(KEY_TITLE_SHORT, getShortTitle());
		nodeData.put(KEY_TITLE_LONG, getLongTitle());
		nodeData.put(KEY_IDENTIFYER, getIdent());
		if(details != null) {
			nodeData.put(KEY_DETAILS, details);
		}
		if(attempts != null) {
			nodeData.put(KEY_ATTEMPTS, attempts);
		}
		if(score != null) {
			nodeData.put(KEY_SCORE, roundedScore);
			nodeData.put(KEY_SCORE_F, score);
		}
		if(maxScore != null) {
			nodeData.put(KEY_MAX, maxScore);
		}
		if(minScore != null) {
			nodeData.put(KEY_MIN, minScore);
		}
		if(grade != null) {
			nodeData.put(KEY_GRADE, grade);
		}
		if(performanceClassIdent != null) {
			nodeData.put(KEY_PERFORMANCE_CLASS_IDENT, performanceClassIdent);
		}
		if (passed != null) {
			nodeData.put(KEY_PASSED, passed);
		}
		if(lastUserModified != null) {
			nodeData.put(KEY_LAST_USER_MODIFIED, lastUserModified);
		}
		if(lastCoachModified != null) {
			nodeData.put(KEY_LAST_COACH_MODIFIED, lastCoachModified);
		}
		nodeData.put(KEY_SELECTABLE, selectable ? Boolean.TRUE : Boolean.FALSE);
		return nodeData;
	}

	private void fromMap(Map<String,Object> nodeData) {
		if(nodeData.get(KEY_INDENT) instanceof Integer) {
			recursionLevel = ((Integer)nodeData.get(KEY_INDENT)).intValue();
		}
		type = (String)nodeData.get(KEY_TYPE);
		shortTitle = (String)nodeData.get(KEY_TITLE_SHORT);
		longTitle = (String)nodeData.get(KEY_TITLE_LONG);
		ident = (String)nodeData.get(KEY_IDENTIFYER);
		details = (String)nodeData.get(KEY_DETAILS);
		attempts = (Integer)nodeData.get(KEY_ATTEMPTS);
		score = (Float)nodeData.get(KEY_SCORE_F);
		roundedScore = (String)nodeData.get(KEY_SCORE);
		maxScore = (Float)nodeData.get(KEY_MAX);
		minScore = (Float)nodeData.get(KEY_MIN);
		grade = (String)nodeData.get(KEY_GRADE);
		performanceClassIdent = (String)nodeData.get(KEY_PERFORMANCE_CLASS_IDENT);
		passed = (Boolean)nodeData.get(KEY_PASSED);
		if(nodeData.get(KEY_SELECTABLE) instanceof Boolean) {
			selectable = ((Boolean)nodeData.get(KEY_SELECTABLE)).booleanValue();
		}
		if(nodeData.get(KEY_LAST_USER_MODIFIED) instanceof Date) {
			lastUserModified = ((Date)nodeData.get(KEY_LAST_USER_MODIFIED));
		}
		if(nodeData.get(KEY_LAST_COACH_MODIFIED) instanceof Date) {
			lastCoachModified = ((Date)nodeData.get(KEY_LAST_COACH_MODIFIED));
		}
		
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(64);
		sb.append("data[title=").append(StringHelper.containsNonWhitespace(longTitle) ? longTitle : (shortTitle == null ? "" : shortTitle))
		  .append(":score=").append(score == null ? "" : AssessmentHelper.getRoundedScore(score))
		  .append(":passed=").append(passed == null ? "" : passed.toString())
		  .append("]");
		return sb.toString();
	}
}

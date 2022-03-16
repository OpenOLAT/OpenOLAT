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
package org.olat.course.nodes.gta.ui;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.id.Identity;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.run.userview.UserCourseEnvironment;


/**
 * 
 * This is a compact view of the assessment data with indexed arrays. It prevent
 * to have 1000x identities in memory which is a memory issue.
 * 
 * Initial date: 17.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentRow {

	private final boolean duplicate;
	private final Identity identity;
	private UserCourseEnvironment userCourseEnv;
	
	private TextElement scoreEl;
	private MultipleSelectionElement applyGradeEl;
	private MultipleSelectionElement passedEl;
	private FormLink assessmentDocsTooltipLink;
	private FormLink assessmentDocsEditLink;
	private FormLink commentTooltipLink, commentEditLink;
	
	private String score;
	private String grade;
	private String translatedGrade;
	private Boolean passed;
	private String comment;
	
	public AssessmentRow(Identity identity, boolean duplicate) {
		this.identity = identity;
		this.duplicate = duplicate;
	}
	
	public Identity getIdentity() {
		return identity;
	}

	public UserCourseEnvironment getUserCourseEnvironment(ICourse course) {
		if(userCourseEnv == null) {
			userCourseEnv = AssessmentHelper.createAndInitUserCourseEnvironment(identity, course);
		}
		return userCourseEnv;
	}

	public boolean isDuplicate() {
		return duplicate;
	}

	public String getScore() {
		return score;
	}

	public void setScore(String score) {
		this.score = score;
	}

	public String getGrade() {
		return grade;
	}

	public void setGrade(String grade) {
		this.grade = grade;
	}

	public String getTranslatedGrade() {
		return translatedGrade;
	}

	public void setTranslatedGrade(String translatedGrade) {
		this.translatedGrade = translatedGrade;
	}

	public Boolean getPassed() {
		return passed;
	}

	public void setPassed(Boolean passed) {
		this.passed = passed;
	}

	public FormLink getAssessmentDocsTooltipLink() {
		return assessmentDocsTooltipLink;
	}

	public void setAssessmentDocsTooltipLink(FormLink assessmentDocsTooltipLink) {
		this.assessmentDocsTooltipLink = assessmentDocsTooltipLink;
	}

	public FormLink getAssessmentDocsEditLink() {
		return assessmentDocsEditLink;
	}

	public void setAssessmentDocsEditLink(FormLink assessmentDocsEditLink) {
		this.assessmentDocsEditLink = assessmentDocsEditLink;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public FormLink getCommentTooltipLink() {
		return commentTooltipLink;
	}

	public void setCommentTooltipLink(FormLink comment) {
		this.commentTooltipLink = comment;
	}

	public FormLink getCommentEditLink() {
		return commentEditLink;
	}

	public void setCommentEditLink(FormLink commentEditLink) {
		this.commentEditLink = commentEditLink;
	}

	public TextElement getScoreEl() {
		return scoreEl;
	}

	public void setScoreEl(TextElement scoreEl) {
		this.scoreEl = scoreEl;
	}

	public MultipleSelectionElement getApplyGradeEl() {
		return applyGradeEl;
	}

	public void setApplyGradeEl(MultipleSelectionElement applyGradeEl) {
		this.applyGradeEl = applyGradeEl;
	}

	public MultipleSelectionElement getPassedEl() {
		return passedEl;
	}

	public void setPassedEl(MultipleSelectionElement passedEl) {
		this.passedEl = passedEl;
	}
}

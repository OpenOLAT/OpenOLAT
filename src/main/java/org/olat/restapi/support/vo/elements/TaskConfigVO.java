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
package org.olat.restapi.support.vo.elements;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * 
 * Description:<br>
 * task course node configuration
 * 
 * <P>
 * Initial Date:  27.07.2010 <br>
 * @author skoeber
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "taskConfigVO")
public class TaskConfigVO {
	
	/** @see org.olat.course.nodes.TACourseNode.CONF_TASK_ENABLED */
	private Boolean isAssignmentEnabled;
	/** @see org.olat.course.nodes.TACourseNode.CONF_TASK_TYPE */
	private String taskAssignmentType;
	/** @see org.olat.course.nodes.TACourseNode.CONF_TASK_TEXT */
	private String taskAssignmentText;
	/** @see org.olat.course.nodes.TACourseNode.CONF_TASK_PREVIEW */
	private Boolean isTaskPreviewEnabled;
	/** @see org.olat.course.nodes.TACourseNode.CONF_TASK_DESELECT */
	private Boolean isTaskDeselectEnabled;
	/** @see org.olat.course.nodes.TACourseNode.CONF_TASK_SAMPLING_WITH_REPLACEMENT */
	private Boolean onlyOneUserPerTask;
	
	/** @see org.olat.course.nodes.TACourseNode.CONF_DROPBOX_ENABLED */
	private Boolean isDropboxEnabled;
	/** @see org.olat.course.nodes.TACourseNode.CONF_DROPBOX_ENABLEMAIL */
	private Boolean isDropboxConfirmationMailEnabled;
	/** @see org.olat.course.nodes.TACourseNode.CONF_DROPBOX_CONFIRMATION */
	private String dropboxConfirmationText;
	
	/** @see org.olat.course.nodes.TACourseNode.CONF_RETURNBOX_ENABLED */
	private Boolean isReturnboxEnabled;
	
	/** @see org.olat.course.nodes.TACourseNode.CONF_SCORING_ENABLED */
	private Boolean isScoringEnabled;
	/** @see org.olat.course.nodes.MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD */
	private Boolean isScoringGranted;
	/** @see org.olat.course.nodes.MSCourseNode.CONFIG_KEY_SCORE_MIN */
	private Float minScore;
	/** @see org.olat.course.nodes.MSCourseNode.CONFIG_KEY_SCORE_MAX */
	private Float maxScore;
	/** @see org.olat.course.nodes.MSCourseNode.CONFIG_KEY_HAS_PASSED_FIELD */
	private Boolean isPassingGranted;
	/** @see org.olat.course.nodes.MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE */
	private Float passingScoreThreshold;
	/** @see org.olat.course.nodes.MSCourseNode.CONFIG_KEY_HAS_COMMENT_FIELD */
	private Boolean hasCommentField;
	/** @see org.olat.course.nodes.MSCourseNode.CONFIG_KEY_INFOTEXT_USER */
	private String commentForUser;
	/** @see org.olat.course.nodes.MSCourseNode.CONFIG_KEY_INFOTEXT_COACH */
	private String commentForCoaches;
	
	/** @see org.olat.course.nodes.TACourseNode.CONF_SOLUTION_ENABLED */
	private Boolean isSolutionEnabled;
	
	/** @see org.olat.course.nodes.TACourseNode.ACCESS_TASK */
	private String conditionTask;
	/** @see org.olat.course.nodes.TACourseNode.ACCESS_DROPBOX */
	private String conditionDropbox;
	/** @see org.olat.course.nodes.TACourseNode.ACCESS_RETURNBOX */
	private String conditionReturnbox;
	/** @see org.olat.course.nodes.TACourseNode.ACCESS_SCORING */
	private String conditionScoring;
	/** @see org.olat.course.nodes.TACourseNode.ACCESS_SOLUTION */
	private String conditionSolution;

	public TaskConfigVO() {
		//make JAXB happy
	}

	public Boolean getIsAssignmentEnabled() {
		return isAssignmentEnabled;
	}

	public void setIsAssignmentEnabled(Boolean isAssignmentEnabled) {
		this.isAssignmentEnabled = isAssignmentEnabled;
	}

	public String getTaskAssignmentType() {
		return taskAssignmentType;
	}

	public void setTaskAssignmentType(String taskAssignmentType) {
		this.taskAssignmentType = taskAssignmentType;
	}

	public String getTaskAssignmentText() {
		return taskAssignmentText;
	}

	public void setTaskAssignmentText(String taskAssignmentText) {
		this.taskAssignmentText = taskAssignmentText;
	}

	public Boolean getIsTaskPreviewEnabled() {
		return isTaskPreviewEnabled;
	}

	public void setIsTaskPreviewEnabled(Boolean isTaskPreviewEnabled) {
		this.isTaskPreviewEnabled = isTaskPreviewEnabled;
	}

	public Boolean getIsTaskDeselectEnabled() {
		return isTaskDeselectEnabled;
	}

	public void setIsTaskDeselectEnabled(Boolean isTaskDeselectEnabled) {
		this.isTaskDeselectEnabled = isTaskDeselectEnabled;
	}

	public Boolean getOnlyOneUserPerTask() {
		return onlyOneUserPerTask;
	}

	public void setOnlyOneUserPerTask(Boolean onlyOneUserPerTask) {
		this.onlyOneUserPerTask = onlyOneUserPerTask;
	}

	public Boolean getIsDropboxEnabled() {
		return isDropboxEnabled;
	}

	public void setIsDropboxEnabled(Boolean isDropboxEnabled) {
		this.isDropboxEnabled = isDropboxEnabled;
	}

	public Boolean getIsDropboxConfirmationMailEnabled() {
		return isDropboxConfirmationMailEnabled;
	}

	public void setIsDropboxConfirmationMailEnabled(Boolean isDropboxConfirmationMailEnabled) {
		this.isDropboxConfirmationMailEnabled = isDropboxConfirmationMailEnabled;
	}

	public String getDropboxConfirmationText() {
		return dropboxConfirmationText;
	}

	public void setDropboxConfirmationText(String dropboxConfirmationText) {
		this.dropboxConfirmationText = dropboxConfirmationText;
	}

	public Boolean getIsReturnboxEnabled() {
		return isReturnboxEnabled;
	}

	public void setIsReturnboxEnabled(Boolean isReturnboxEnabled) {
		this.isReturnboxEnabled = isReturnboxEnabled;
	}

	public Boolean getIsScoringEnabled() {
		return isScoringEnabled;
	}

	public void setIsScoringEnabled(Boolean isScoringEnabled) {
		this.isScoringEnabled = isScoringEnabled;
	}

	public Boolean getIsScoringGranted() {
		return isScoringGranted;
	}

	public void setIsScoringGranted(Boolean isScoringGranted) {
		this.isScoringGranted = isScoringGranted;
	}

	public Float getMinScore() {
		return minScore;
	}

	public void setMinScore(Float minScore) {
		this.minScore = minScore;
	}

	public Float getMaxScore() {
		return maxScore;
	}

	public void setMaxScore(Float maxScore) {
		this.maxScore = maxScore;
	}

	public Boolean getIsPassingGranted() {
		return isPassingGranted;
	}

	public void setIsPassingGranted(Boolean isPassingGranted) {
		this.isPassingGranted = isPassingGranted;
	}

	public Float getPassingScoreThreshold() {
		return passingScoreThreshold;
	}

	public void setPassingScoreThreshold(Float passingScoreThreshold) {
		this.passingScoreThreshold = passingScoreThreshold;
	}

	public Boolean getHasCommentField() {
		return hasCommentField;
	}

	public void setHasCommentField(Boolean hasCommentField) {
		this.hasCommentField = hasCommentField;
	}

	public String getCommentForUser() {
		return commentForUser;
	}

	public void setCommentForUser(String commentForUser) {
		this.commentForUser = commentForUser;
	}

	public String getCommentForCoaches() {
		return commentForCoaches;
	}

	public void setCommentForCoaches(String commentForCoaches) {
		this.commentForCoaches = commentForCoaches;
	}

	public Boolean getIsSolutionEnabled() {
		return isSolutionEnabled;
	}

	public void setIsSolutionEnabled(Boolean isSolutionEnabled) {
		this.isSolutionEnabled = isSolutionEnabled;
	}

	public String getConditionTask() {
		return conditionTask;
	}

	public void setConditionTask(String conditionTask) {
		this.conditionTask = conditionTask;
	}

	public String getConditionDropbox() {
		return conditionDropbox;
	}

	public void setConditionDropbox(String conditionDropbox) {
		this.conditionDropbox = conditionDropbox;
	}

	public String getConditionReturnbox() {
		return conditionReturnbox;
	}

	public void setConditionReturnbox(String conditionReturnbox) {
		this.conditionReturnbox = conditionReturnbox;
	}

	public String getConditionScoring() {
		return conditionScoring;
	}

	public void setConditionScoring(String conditionScoring) {
		this.conditionScoring = conditionScoring;
	}

	public String getConditionSolution() {
		return conditionSolution;
	}

	public void setConditionSolution(String conditionSolution) {
		this.conditionSolution = conditionSolution;
	}

}

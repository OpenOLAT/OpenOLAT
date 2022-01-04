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
package org.olat.repository.ui.author.copy.wizard;

import java.util.Date;

import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.util.StringHelper;
import org.olat.course.editor.overview.OverviewRow;
import org.olat.course.learningpath.LearningPathConfigs;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext.CopyType;

/**
 * Initial date: 19.08.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class CopyCourseOverviewRow extends OverviewRow {

	private LearningPathConfigs learningPathConfigs;
	private String earliestDateWithLabel;
	private Date newStartDate;
	private Date newEndDate;
	private FormItem newStartDateChooser;
	private FormItem newEndDateChooser;
	private AssessmentObligation assesssmentObligation;
	private SingleSelection assessmentObligationChooser;
	private CopyType resourceCopyType;
	private SingleSelection resourceCopyTypeChooser;
	
	public CopyCourseOverviewRow(CourseEditorTreeNode editorNode, int recursionLevel) {
		super(editorNode, recursionLevel);
	}
	
	public LearningPathConfigs getLearningPathConfigs() {
		return learningPathConfigs;
	}
	
	public void setLearningPathConfigs(LearningPathConfigs learningPathConfigs) {
		this.learningPathConfigs = learningPathConfigs;
	}
	
	public String getEarliestDateWithLabel() {
		return earliestDateWithLabel;
	}
	
	public void setEarliestDate(String earliestDate, String label) {
		this.earliestDateWithLabel = earliestDate + (StringHelper.containsNonWhitespace(label) ? " - " + label : "");
	}
	
	public Date getNewStartDate() {
		return newStartDate;
	}
	
	public void setNewStartDate(Date startDate) {
		this.newStartDate = startDate;
	}
	
	public Date getNewEndDate() {
		return newEndDate;
	}

	public void setNewEndDate(Date endDate) {
		this.newEndDate = endDate;
	}
	
	public SingleSelection getObligationChooser() {
		return assessmentObligationChooser;
	}
	
	public void setObligationChooser(SingleSelection obligationChooser) {
		this.assessmentObligationChooser = obligationChooser;
	}
	
	public SingleSelection getResourceChooser() {
		return resourceCopyTypeChooser;
	}
	
	public void setResourceChooser(SingleSelection resourceChooser) {
		this.resourceCopyTypeChooser = resourceChooser;
	}
	
	public FormItem getNewEndDateChooser() {
		return newEndDateChooser;
	}
	
	public void setNewEndDateChooser(FormItem newEndDateChooser) {
		this.newEndDateChooser = newEndDateChooser;
	}
	public FormItem getNewStartDateChooser() {
		return newStartDateChooser;
	}
	
	public void setNewStartDateChooser(FormItem newStartDateChooser) {
		this.newStartDateChooser = newStartDateChooser;
	}
	
	public AssessmentObligation getAssesssmentObligation() {
		return assesssmentObligation;
	}
	
	public void setAssesssmentObligation(AssessmentObligation assesssmentObligation) {
		this.assesssmentObligation = assesssmentObligation;
	}
	
	public CopyType getResourceCopyType() {
		return resourceCopyType;
	}
	
	public void setResourceCopyType(CopyType resourceCopyType) {
		this.resourceCopyType = resourceCopyType;
	}
	
}

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

import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.TaskLight;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.modules.ModuleConfiguration;

/**
 * 
 * Initial date: 21.12.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class GTACoachedListController extends FormBasicController {
	
	protected final GTACourseNode gtaNode;
	protected final CourseEnvironment courseEnv;
	
	public GTACoachedListController(UserRequest ureq, WindowControl wControl,
			CourseEnvironment courseEnv, GTACourseNode gtaNode) {
		super(ureq, wControl, "coach_list");
		this.gtaNode = gtaNode;
		this.courseEnv = courseEnv;
	}


	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			
			ModuleConfiguration config = gtaNode.getModuleConfiguration();
			boolean assignment = config.getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT);
			layoutCont.contextPut("assignmentEnabled", Boolean.valueOf(assignment));
			
			boolean submit = config.getBooleanSafe(GTACourseNode.GTASK_SUBMIT);
			layoutCont.contextPut("submitEnabled", submit);
			
			boolean reviewAndCorrection = config.getBooleanSafe(GTACourseNode.GTASK_REVIEW_AND_CORRECTION);
			layoutCont.contextPut("reviewAndCorrectionEnabled", reviewAndCorrection);
			
			boolean revision = config.getBooleanSafe(GTACourseNode.GTASK_REVISION_PERIOD);
			layoutCont.contextPut("revisionEnabled", reviewAndCorrection && revision);
			
			boolean solution = config.getBooleanSafe(GTACourseNode.GTASK_SAMPLE_SOLUTION);
			layoutCont.contextPut("solutionEnabled", solution);
			
			boolean grading = config.getBooleanSafe(GTACourseNode.GTASK_GRADING);
			layoutCont.contextPut("gradingEnabled", grading);
		}
	}
	
	protected Date getSyntheticSubmissionDate(TaskLight task) {
		Date date = task.getSubmissionDate();
		if(date == null || (task.getSubmissionRevisionsDate() != null && task.getSubmissionRevisionsDate().after(date))) {
			date = task.getSubmissionRevisionsDate();
		}
		if(date == null || (task.getCollectionDate() != null && task.getCollectionDate().after(date))) {
			date = task.getCollectionDate();
		}
		return date;
	}
	
	public boolean hasSubmittedDocument(TaskLight task) {
		Integer numOfDocs = task.getSubmissionNumOfDocs();
		Date date = task.getSubmissionDate();
		if(date == null || (task.getSubmissionRevisionsDate() != null && task.getSubmissionRevisionsDate().after(date))) {
			date = task.getSubmissionRevisionsDate();
			numOfDocs = task.getSubmissionRevisionsNumOfDocs();
		}
		if(date == null || (task.getCollectionDate() != null && task.getCollectionDate().after(date))) {
			numOfDocs = task.getCollectionNumOfDocs();
		}
		return numOfDocs == null ? false : numOfDocs.intValue() > 0;
	}
}

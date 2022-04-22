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
package org.olat.course.assessment.bulk;

import org.olat.core.commons.services.taskexecutor.Task;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.course.assessment.model.BulkAssessmentDatas;
import org.olat.course.nodes.CourseNode;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 18.11.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BulkAssessment_2_DatasStep extends BasicStep {
	
	private final Task task;
	private final BulkAssessmentDatas savedDatas;
	private final RepositoryEntry courseEntry;
	private final CourseNode courseNode;
	private final boolean canEditUserVisibility;
	private boolean hasPreviousStep = true;

	public BulkAssessment_2_DatasStep(UserRequest ureq, RepositoryEntry courseEntry, boolean canEditUserVisibility) {
		this(ureq,courseEntry, null, null, null, canEditUserVisibility);
	}
	
	public BulkAssessment_2_DatasStep(UserRequest ureq, RepositoryEntry courseEntry, CourseNode courseNode, boolean canEditUserVisibility) {
		this(ureq, courseEntry, courseNode, null, null, canEditUserVisibility);
	}
	
	/**
	 * This constructor start the edit the bulk update.
	 * @param ureq
	 * @param courseNode
	 * @param datas
	 */
	public BulkAssessment_2_DatasStep(UserRequest ureq, RepositoryEntry courseEntry, CourseNode courseNode,
			BulkAssessmentDatas savedDatas, Task task, boolean canEditUserVisibility) {
		super(ureq);
		this.task = task;
		this.savedDatas = savedDatas;
		this.courseEntry = courseEntry;
		this.courseNode = courseNode;
		this.canEditUserVisibility = canEditUserVisibility;
		setI18nTitleAndDescr("data.title", "data.title");
		if(savedDatas == null) {
			setNextStep(new BulkAssessment_2b_ChooseColumnsStep(ureq, courseEntry));
		} else {
			setNextStep(new BulkAssessment_2b_ChooseColumnsStep(ureq, courseEntry, savedDatas.getColumnsSettings()));
		}
		
		hasPreviousStep = courseNode != null;
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(!hasPreviousStep, true, false);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext context, Form form) {
		form.setMultipartEnabled(true);
		if(courseNode != null) {
			context.put("courseNode", courseNode);
		}
		if(task != null) {
			context.put("task", task);
		}
		
		DataStepForm ctrl;
		if(savedDatas != null) {
			ctrl = new DataStepForm(ureq, wControl, courseEntry, courseNode, savedDatas, context, canEditUserVisibility, form);
		} else {
			ctrl = new DataStepForm(ureq, wControl, courseEntry, context, canEditUserVisibility, form);
		}
		return ctrl;
	}
}

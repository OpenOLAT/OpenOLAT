/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.assessment.ui.inspection;

import java.util.Date;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.course.assessment.AssessmentInspection;
import org.olat.course.assessment.AssessmentInspectionConfiguration;
import org.olat.course.assessment.AssessmentInspectionService;
import org.olat.course.nodes.CourseNode;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 déc. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CreateInspectionFinishStepCallback implements StepRunnerCallback {
	
	private final CreateInspectionContext inspectionContext;
	
	@Autowired
	private AssessmentInspectionService inspectionService;
	
	public CreateInspectionFinishStepCallback(CreateInspectionContext inspectionContext) {
		CoreSpringFactory.autowireObject(this);
		this.inspectionContext = inspectionContext;
	}

	@Override
	public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
		
		AssessmentInspectionConfiguration configuration = inspectionContext.getInspectionConfiguration();
		CourseNode courseNode = inspectionContext.getCourseNode();
		Date startDate = inspectionContext.getStartDate();
		Date endDate = inspectionContext.getEndDate();
		boolean accessCode = inspectionContext.isAccessCode();
		
		if(inspectionContext.getEditedInspection() != null) {
			AssessmentInspection inspection = inspectionContext.getEditedInspection();
			Integer extraTime = null;
			if(inspectionContext.getInspectionCompensations() != null && inspectionContext.getInspectionCompensations().size() == 1) {
				extraTime = inspectionContext.getInspectionCompensations().get(0).extraTimeInSeconds();
			}
			inspectionService.updateInspection(inspection, inspectionContext.getInspectionConfiguration(),
					startDate, endDate, extraTime, accessCode, ureq.getIdentity());
		} else {
			inspectionService.addInspection(configuration, startDate, endDate, inspectionContext.getInspectionCompensations(),
					accessCode, courseNode.getIdent(), inspectionContext.getParticipants(), ureq.getIdentity());
		}
		return StepsMainRunController.DONE_MODIFIED;
	}
}

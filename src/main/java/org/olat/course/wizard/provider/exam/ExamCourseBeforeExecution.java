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
package org.olat.course.wizard.provider.exam;

import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.course.wizard.CourseWizardCallback;
import org.olat.course.wizard.CourseWizardCallback.BeforeExecution;
import org.olat.course.wizard.ui.CertificateController;

/**
 * 
 * Initial date: 11 Jan 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ExamCourseBeforeExecution implements BeforeExecution {

	private final ExamCourseSteps examCourseSteps;

	public ExamCourseBeforeExecution(ExamCourseSteps examCourseSteps) {
		this.examCourseSteps = examCourseSteps;
	}

	@Override
	public void obBeforeExecution(StepsRunContext runContext) {
		if (!examCourseSteps.isRetest()) {
			runContext.remove(CourseWizardCallback.RUN_CONTEXT_RETEST);
		}
		if (!examCourseSteps.isCertificate()) {
			runContext.remove(CertificateController.RUN_CONTEXT_KEY);
		}
		if (!examCourseSteps.isCoaches()) {
			runContext.remove(CourseWizardCallback.RUN_CONTEXT_COACHES);
		}
		if (!examCourseSteps.isParticipants()) {
			runContext.remove(CourseWizardCallback.RUN_CONTEXT_PARTICIPANTS);
		}
	}

}

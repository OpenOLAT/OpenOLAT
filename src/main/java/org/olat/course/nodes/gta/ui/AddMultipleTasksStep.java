/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.course.nodes.gta.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.run.environment.CourseEnvironment;

/**
 * Initial date: Aug 07, 2023
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class AddMultipleTasksStep extends BasicStep {

	private final CourseEnvironment courseEnv;
	private final GTACourseNode gtaNode;

	public AddMultipleTasksStep(UserRequest ureq, CourseEnvironment courseEnv, GTACourseNode gtaNode) {
		super(ureq);
		this.courseEnv = courseEnv;
		this.gtaNode = gtaNode;
		setI18nTitleAndDescr("wizard.step0.title", "wizard.step0.description");
		setNextStep(new AddMetadataStep(ureq));
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(false, true, false);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext stepsRunContext, Form form) {
		return new AddMultipleTasksStepController(ureq, wControl, form, stepsRunContext, courseEnv, gtaNode);
	}
}

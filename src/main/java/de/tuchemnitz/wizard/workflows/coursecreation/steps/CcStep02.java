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
* <p>
* Initial code contributed and copyrighted by<br>
* Technische Universitaet Chemnitz Lehrstuhl Technische Informatik<br>
* <br>
* Author Marcel Karras (toka@freebits.de)<br>
* Author Norbert Englisch (norbert.englisch@informatik.tu-chemnitz.de)<br>
* Author Sebastian Fritzsche (seb.fritzsche@googlemail.com)
*/

package de.tuchemnitz.wizard.workflows.coursecreation.steps;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.repository.wizard.ui.AccessAndPropertiesController;

import de.tuchemnitz.wizard.workflows.coursecreation.model.CourseCreationConfiguration;

/**
 * 
 * Description:<br>
 * Third step of the course creation wizard:
 * <br/>- choose access condition
 * <br/>- publish?
 * 
 * <P>
 * @author Marcel Karras (toka@freebits.de)
 * @author Norbert Englisch (norbert.englisch@informatik.tu-chemnitz.de)
 * @author Sebastian Fritzsche (seb.fritzsche@googlemail.com)
 * @author skoeber
 */
public class CcStep02 extends BasicStep {

	private PrevNextFinishConfig prevNextConfig;
	private final CourseCreationConfiguration courseConfig;

	/**
	 * Third step of the course creation wizard.
	 * @param ureq
	 * @param courseConfig
	 */
	public CcStep02(UserRequest ureq, CourseCreationConfiguration courseConfig) {
		super(ureq);
		this.courseConfig = courseConfig;
		setI18nTitleAndDescr("coursecreation.publish.title", "coursecreation.publish.description");
		setNextStep(Step.NOSTEP);
		prevNextConfig = PrevNextFinishConfig.BACK_FINISH;
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return prevNextConfig;
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl, StepsRunContext stepsRunContext, Form form) {
		StepFormController stepP = new AccessAndPropertiesController(ureq, windowControl, form, stepsRunContext);
		return stepP;
	}

	public CourseCreationConfiguration getCourseConfig() {
		return courseConfig;
	}

}

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
package org.olat.modules.grade.ui.wizard;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.Util;
import org.olat.course.nodes.CourseNode;
import org.olat.modules.grade.ui.GradeUIFactory;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 10 Mar 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class GradeScaleAdjustStep extends BasicStep {

	private final RepositoryEntry courseEntry;
	private final CourseNode courseNode;

	public GradeScaleAdjustStep(UserRequest ureq, RepositoryEntry courseEntry, CourseNode courseNode, boolean autoGrade) {
		super(ureq);
		this.courseEntry = courseEntry;
		this.courseNode = courseNode;
		setTranslator(Util.createPackageTranslator(GradeUIFactory.class, getLocale()));
		setI18nTitleAndDescr("grade.scale.ajust.title", "grade.scale.ajust.title");
		setNextStep(new GradeChangesStep(ureq, autoGrade));
	}
	
	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(false, true, false);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		runContext.put(GradeScaleAdjustCallback.KEY_COURSE_ENTRY, courseEntry);
		runContext.put(GradeScaleAdjustCallback.KEY_COURSE_NODE, courseNode);
		return new GradeScaleAdjustController(ureq, wControl, form, runContext);
	}

}

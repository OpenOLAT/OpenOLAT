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
package org.olat.course.archiver.wizard;

import org.olat.core.commons.services.export.ArchiveType;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;

/**
 * 
 * Initial date: 16 févr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseArchive_1_ArchiveTypeStep extends BasicStep {
	
	private final CourseArchiveContext archiveContext;
	
	public CourseArchive_1_ArchiveTypeStep(UserRequest ureq, CourseArchiveContext archiveContext) {
		super(ureq);
		this.archiveContext = archiveContext;
		setNextStep(ureq);
		setI18nTitleAndDescr("wizard.archive.type.title", "wizard.archive.type.title");
	}
	
	private void setNextStep(UserRequest ureq) {
		if(archiveContext.getArchiveOptions().getArchiveType() == ArchiveType.PARTIAL) {
			setNextStep(new CourseArchive_2_CourseElementsStep(ureq, archiveContext));
		} else if((archiveContext.getArchiveOptions().getArchiveType() == ArchiveType.COMPLETE && archiveContext.isAdministrator())
				|| archiveContext.hasCustomization()) {
			setNextStep(new CourseArchive_3_SettingsStep(ureq, archiveContext));
		} else {
			setNextStep(new CourseArchive_5_OverviewStep(ureq, archiveContext));
		}
	}
	
	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(false, true, false);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl,
			StepsRunContext stepsRunContext, Form form) {
		return new CourseArchiveTypeController(ureq, wControl, archiveContext, stepsRunContext, form,
				this::setNextStep);
	}
}

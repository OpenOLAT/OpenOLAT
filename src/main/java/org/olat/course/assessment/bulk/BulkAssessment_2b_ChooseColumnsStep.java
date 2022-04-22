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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.course.assessment.model.BulkAssessmentColumnSettings;
import org.olat.course.assessment.model.BulkAssessmentSettings;
import org.olat.course.nodes.CourseNode;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 9.1.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BulkAssessment_2b_ChooseColumnsStep extends BasicStep {
	
	private final RepositoryEntryRef courseEntry;
	private final BulkAssessmentColumnSettings savedColumnsSettings;
	
	/**
	 * This constructor start the edit the bulk update.
	 * @param ureq
	 * @param courseNode
	 * @param datas
	 */
	public BulkAssessment_2b_ChooseColumnsStep(UserRequest ureq, RepositoryEntry courseEntry) {
		this(ureq, courseEntry, null);
	}

	/**
	 * This constructor start the edit the bulk update.
	 * @param ureq
	 * @param courseEntry 
	 * @param courseNode
	 * @param datas
	 */
	public BulkAssessment_2b_ChooseColumnsStep(UserRequest ureq, RepositoryEntry courseEntry, BulkAssessmentColumnSettings savedColumnsSettings) {
		super(ureq);
		this.courseEntry = courseEntry;
		this.savedColumnsSettings = savedColumnsSettings;
		setI18nTitleAndDescr("chooseColumns.title", "chooseColumns.title");
		setNextStep(new BulkAssessment_3_ValidationStep(ureq, courseEntry));
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, true, false);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext context, Form form) {
		// Skip this step if it has only return files
		CourseNode courseNode = (CourseNode)context.get("courseNode");
		BulkAssessmentSettings settings = new BulkAssessmentSettings(courseNode, courseEntry);
		boolean onlyReturnFiles = (!settings.isHasScore() && !settings.isHasPassed() && !settings.isHasUserComment());
		return onlyReturnFiles
				? new ChooseColumnsStepSkipForm(ureq, wControl, context, form)
				: new ChooseColumnsStepForm(ureq, wControl, savedColumnsSettings, context, form, courseEntry);
	}
}

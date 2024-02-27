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
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;

/**
 * 
 * Initial date: 16 févr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseArchiveTypeController extends StepFormBasicController {
	
	private SingleSelection typeEl;
	
	private final CourseArchiveOptions archiveOptions;
	private final CourseArchiveStepsListener stepsListener;
	
	public CourseArchiveTypeController(UserRequest ureq, WindowControl wControl,
			CourseArchiveContext archiveContext, StepsRunContext runContext, Form rootForm, CourseArchiveStepsListener stepsListener) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT, null);
		archiveOptions = archiveContext.getArchiveOptions();
		this.stepsListener = stepsListener;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		SelectionValues typesPK = new SelectionValues();
		typesPK.add(SelectionValues.entry(ArchiveType.COMPLETE.name(), translate("archive.types.complete"),
				translate("archive.types.complete.desc"), null, null, true));
		typesPK.add(SelectionValues.entry(ArchiveType.PARTIAL.name(), translate("archive.types.partial"),
				translate("archive.types.partial.desc"), null, null, true));
		typeEl = uifactory.addCardSingleSelectHorizontal("archive.types", "archive.types", formLayout, typesPK);
		
		if(archiveOptions.getArchiveType() != null) {
			typeEl.select(archiveOptions.getArchiveType().name(), true);
		} else {
			typeEl.select(ArchiveType.COMPLETE.name(), true);
		}
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		typeEl.clearError();
		if(!typeEl.isOneSelected()) {
			typeEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		ArchiveType type = ArchiveType.valueOf(typeEl.getSelectedKey());
		archiveOptions.setArchiveType(type);
		stepsListener.onStepsChanged(ureq);
		fireEvent(ureq, StepsEvent.STEPS_CHANGED);
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
}

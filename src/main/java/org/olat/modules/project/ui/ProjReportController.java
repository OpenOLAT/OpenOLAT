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
package org.olat.modules.project.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.util.DateRange;
import org.olat.core.util.DateUtils;
import org.olat.modules.project.ProjAppointment;
import org.olat.modules.project.ProjDecision;
import org.olat.modules.project.ProjFile;
import org.olat.modules.project.ProjMilestone;
import org.olat.modules.project.ProjNote;
import org.olat.modules.project.ProjProjectRef;
import org.olat.modules.project.ProjToDo;
import org.olat.modules.project.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21 Sep 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjReportController extends FormBasicController {
	
	private static final String[] onKeys = new String[] { "on" };
	
	private DateChooser dateRangeEl;
	private MultipleSelectionElement timelineEl;
	private MultipleSelectionElement artefactTypeEl;

	private final ProjProjectRef project;
	
	@Autowired
	private ProjectService projectService;

	protected ProjReportController(UserRequest ureq, WindowControl wControl, ProjProjectRef project) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.project = project;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		dateRangeEl = uifactory.addDateChooser("report.date.range", null, formLayout);
		dateRangeEl.setSecondDate(true);
		dateRangeEl.setSeparator("report.date.range.separator");
		
		timelineEl = uifactory.addCheckboxesVertical("report.timeline", formLayout, onKeys,
				new String[] { translate("report.timeline.include") }, 1);
		
		SelectionValues artefactTypeSV = new SelectionValues();
		artefactTypeSV.add(SelectionValues.entry(ProjAppointment.TYPE, translate("report.types.appointments")));
		artefactTypeSV.add(SelectionValues.entry(ProjMilestone.TYPE, translate("report.types.milestones")));
		artefactTypeSV.add(SelectionValues.entry(ProjToDo.TYPE, translate("report.types.todos")));
		artefactTypeSV.add(SelectionValues.entry(ProjDecision.TYPE, translate("report.types.decisions")));
		artefactTypeSV.add(SelectionValues.entry(ProjNote.TYPE, translate("report.types.notes")));
		artefactTypeSV.add(SelectionValues.entry(ProjFile.TYPE, translate("report.types.files")));
		artefactTypeEl = uifactory.addCheckboxesVertical("report.types", formLayout, artefactTypeSV.keys(), artefactTypeSV.values(), 1);
		artefactTypeEl.getKeys().forEach(key -> artefactTypeEl.select(key, true));
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormSubmitButton("download", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		dateRangeEl.clearError();
		if (dateRangeEl.getDate() != null && dateRangeEl.getSecondDate() != null && dateRangeEl.getDate().after(dateRangeEl.getSecondDate())) {
			dateRangeEl.setErrorKey("error.date.range.to.before.from");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doDownload(ureq);
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private void doDownload(UserRequest ureq) {
		MediaResource resource = projectService.createWordReport(getIdentity(), project,
				artefactTypeEl.getSelectedKeys(),
				new DateRange(dateRangeEl.getDate(), dateRangeEl.getSecondDate() != null? DateUtils.setTime(dateRangeEl.getSecondDate(), 23, 59, 29): null),
				timelineEl.isAtLeastSelected(1),
				getLocale());
		ureq.getDispatchResult().setResultingMediaResource(resource);
	}

}

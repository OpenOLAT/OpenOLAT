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
package org.olat.course.archiver;

import static org.olat.core.gui.components.util.SelectionValues.entry;

import java.util.ArrayList;
import java.util.List;

import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.OrganisationStatus;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.MultiSelectionFilterElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Organisation;
import org.olat.core.util.DateRange;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * CMC for exporting forum reports with or without filter options
 * <p>
 * Initial date: Apr 18, 2023
 *
 * @author Sumit Kapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class ForumArchiveReportExportController extends FormBasicController {

	private static final String FILTER_OPTION_SELECTIVE = "selective";

	private SingleSelection reportDataEl;
	private DateChooser dateRangeEl;
	private MultiSelectionFilterElement orgaSelectionEl;

	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private OrganisationModule organisationModule;


	public ForumArchiveReportExportController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_DEFAULT);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormInfo("fo.report.generator.info");

		// Radio buttons for choosing which data should be exported all/selective
		SelectionValues reportDataSV = new SelectionValues();
		reportDataSV.add(entry("all", translate("fo.report.generator.report.data.all")));
		reportDataSV.add(entry(FILTER_OPTION_SELECTIVE, translate("fo.report.generator.report.data.selective")));
		reportDataEl = uifactory.addRadiosVertical("fo.report.generator.report.data.label", formLayout, reportDataSV.keys(), reportDataSV.values());
		reportDataEl.select("all", true);
		reportDataEl.addActionListener(FormEvent.ONCHANGE);

		// daterange chooser for filtering by creation date of entry
		dateRangeEl = uifactory.addDateChooser("daterange", "fo.report.generator.date.range.label", null, formLayout);
		dateRangeEl.setSecondDate(true);
		dateRangeEl.setSeparator("fo.report.generator.date.range.end");

		// filter by selected organisations. Filter only available if organisationModule is enabled
		SelectionValues organisationsSV = new SelectionValues();
		List<Organisation> organisations = organisationService.getOrganisations(OrganisationStatus.notDelete());
		organisations.forEach(o -> organisationsSV.add(new SelectionValues.SelectionValue(o.getKey().toString(), o.getDisplayName())));
		orgaSelectionEl = uifactory.addCheckboxesFilterDropdown("fo.report.generator.orga.filter.label",
				"fo.report.generator.orga.filter.label", formLayout, getWindowControl(), organisationsSV);
		

		FormLayoutContainer buttons = FormLayoutContainer.createButtonLayout("buttonGroupLayout", getTranslator());
		formLayout.add(buttons);
		uifactory.addFormSubmitButton("fo.report.generate.confirm", buttons);
		uifactory.addFormCancelButton("cancel", buttons, ureq, getWindowControl());

		updateSelectiveVisibility();
	}

	/**
	 * Only show filter elements if FILTER_OPTION_SELECTIVE is selected
	 */
	private void updateSelectiveVisibility() {
		dateRangeEl.setVisible(reportDataEl.isKeySelected(FILTER_OPTION_SELECTIVE));
		orgaSelectionEl.setVisible(organisationModule.isEnabled() && reportDataEl.isKeySelected(FILTER_OPTION_SELECTIVE));
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (validateFormLogic(ureq)) {
			fireEvent(ureq, Event.DONE_EVENT);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == reportDataEl) {
			updateSelectiveVisibility();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	public boolean isReportAllData() {
		return reportDataEl.isKeySelected("all");
	}

	public DateRange getDateRange() {
		return new DateRange(dateRangeEl.getDate(), dateRangeEl.getSecondDate());
	}

	public List<String> getOrganisationsSelection() {
		if(orgaSelectionEl.isVisible()) {
			return new ArrayList<>(orgaSelectionEl.getSelectedKeys());
		}
		return List.of();
	}
}

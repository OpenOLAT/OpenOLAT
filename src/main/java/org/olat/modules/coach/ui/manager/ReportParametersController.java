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
package org.olat.modules.coach.ui.manager;

import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.FormSection;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.modules.coach.reports.ReportConfiguration;
import org.olat.modules.coach.reports.ReportDateRange;
import org.olat.modules.coach.reports.ReportParameterDefinition;
import org.olat.modules.coach.reports.ReportParameterName;
import org.olat.modules.coach.reports.ReportParameters;
import org.olat.modules.coach.reports.ReportParametersText;
import org.olat.modules.coach.reports.ReportTimeRange;
import org.olat.modules.curriculum.CurriculumModule;
import org.olat.repository.LifecycleModule;
import org.olat.repository.manager.RepositoryEntryLifecycleDAO;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The dialog to choose the parameters of a report before it is generated.
 * The texts which depend on the report (title, date label, descriptions of
 * the cards) come from the template.
 * 
 * Initial date: 27 nov. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ReportParametersController extends FormBasicController {

	private static final String DATE_RANGE_CUSTOM = "custom";
	private static final String DATE_RANGE_TIME_PERIOD = "timePeriod";
	private static final String DATE_RANGE_FISCAL_YEAR = "fiscalYear";

	private static final ReportTimeRange[] FISCAL_YEARS = {
			ReportTimeRange.currentFiscalYear, ReportTimeRange.lastFiscalYear, ReportTimeRange.last2FiscalYears
		};
	private static final ReportTimeRange[] RELATIVES = {
			ReportTimeRange.last7Days, ReportTimeRange.last4Weeks, ReportTimeRange.last6Months,
			ReportTimeRange.last12Months, ReportTimeRange.last24Months
		};

	private SingleSelection timeRangeEl;
	private SingleSelection dateRangeEl;
	private DateChooser customDatesEl;
	private SingleSelection timePeriodEl;
	private SingleSelection fiscalYearEl;
	private DateChooser resolvedDatesEl;
	private SingleSelection relativeEl;
	private DateChooser changedSinceEl;

	private final ReportConfiguration template;
	private final List<ReportParameterDefinition> definitions;
	private final List<RepositoryEntryLifecycle> timePeriods;
	private ReportParameters parameters;

	@Autowired
	private LifecycleModule lifecycleModule;
	@Autowired
	private CurriculumModule curriculumModule;
	@Autowired
	private RepositoryEntryLifecycleDAO lifecycleDao;

	public ReportParametersController(UserRequest ureq, WindowControl wControl, ReportConfiguration template) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.template = template;
		definitions = template.getParameterDefinitions();
		timePeriods = lifecycleModule.isEnabled() ? lifecycleDao.loadPublicLifecycle() : List.of();

		initForm(ureq);
		updateUI();
	}

	public ReportConfiguration getTemplate() {
		return template;
	}

	/**
	 * @return The parameters chosen by the user, available after the DONE_EVENT
	 */
	public ReportParameters getParameters() {
		return parameters;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormStyle("o_sel_report_params");
		ReportParameterDefinition timeRangeDefinition = getDefinition(ReportParameterName.timeRange);
		if(timeRangeDefinition != null) {
			initTimeRangeForm(formLayout, timeRangeDefinition.defaultTimeRange());
		}
		if(getDefinition(ReportParameterName.changedSince) != null) {
			initChangedSinceForm(formLayout);
		}

		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		uifactory.addFormSubmitButton("report.params.run", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	private void initTimeRangeForm(FormItemContainer formLayout, ReportTimeRange defaultTimeRange) {
		ReportTimeRange.Group defaultGroup = defaultTimeRange == null ? ReportTimeRange.Group.all : defaultTimeRange.group();

		SelectionValues timeRangeValues = new SelectionValues();
		timeRangeValues.add(new SelectionValue(ReportTimeRange.Group.all.name(), translate("report.params.time.range.all"),
				template.getParametersText(ReportParametersText.allDescription, getLocale())));
		timeRangeValues.add(new SelectionValue(ReportTimeRange.Group.specific.name(), translate("report.params.time.range.specific"),
				template.getParametersText(ReportParametersText.specificDescription, getLocale())));
		timeRangeValues.add(new SelectionValue(ReportTimeRange.Group.relative.name(), translate("report.params.time.range.relative"),
				template.getParametersText(ReportParametersText.relativeDescription, getLocale())));
		timeRangeEl = uifactory.addCardSingleSelectHorizontal("time.range", null, formLayout, timeRangeValues);
		timeRangeEl.setLabel(template.getParametersText(ReportParametersText.timeRange, getLocale()), null, false);
		timeRangeEl.setElementCssClass("o_sel_report_params_time_range");
		timeRangeEl.addActionListener(FormEvent.ONCHANGE);
		timeRangeEl.select(defaultGroup.name(), true);

		// Specific date range
		SelectionValues dateRangeValues = new SelectionValues();
		dateRangeValues.add(new SelectionValue(DATE_RANGE_CUSTOM, translate("report.params.date.range.custom")));
		if(!timePeriods.isEmpty()) {
			dateRangeValues.add(new SelectionValue(DATE_RANGE_TIME_PERIOD, translate("report.params.date.range.time.period")));
		}
		dateRangeValues.add(new SelectionValue(DATE_RANGE_FISCAL_YEAR, translate("report.params.date.range.fiscal.year")));
		dateRangeEl = uifactory.addButtonGroupSingleSelectHorizontal("report.params.date.range", formLayout, dateRangeValues);
		dateRangeEl.setElementCssClass("o_sel_report_params_date_range");
		dateRangeEl.addActionListener(FormEvent.ONCHANGE);

		customDatesEl = uifactory.addDateChooser("custom.dates", "report.params.from.to", null, formLayout);
		customDatesEl.setSeparator("report.params.separator");
		customDatesEl.setSecondDate(true);
		customDatesEl.setMandatory(true);
		customDatesEl.setElementCssClass("o_sel_report_params_custom_dates");

		SelectionValues timePeriodValues = new SelectionValues();
		for(RepositoryEntryLifecycle timePeriod:timePeriods) {
			timePeriodValues.add(new SelectionValue(timePeriod.getKey().toString(), getTimePeriodLabel(timePeriod)));
		}
		timePeriodEl = uifactory.addDropdownSingleselect("report.params.time.period", formLayout,
				timePeriodValues.keys(), timePeriodValues.values());
		timePeriodEl.addActionListener(FormEvent.ONCHANGE);
		timePeriods.stream()
			.filter(RepositoryEntryLifecycle::isDefaultPublicCycle)
			.findFirst()
			.ifPresent(timePeriod -> timePeriodEl.select(timePeriod.getKey().toString(), true));
		if(!timePeriodEl.isOneSelected() && !timePeriods.isEmpty()) {
			timePeriodEl.select(timePeriodValues.keys()[0], true);
		}

		SelectionValues fiscalYearValues = new SelectionValues();
		for(ReportTimeRange fiscalYear:FISCAL_YEARS) {
			fiscalYearValues.add(new SelectionValue(fiscalYear.name(), translate("report.params.time.range." + fiscalYear.name())));
		}
		fiscalYearEl = uifactory.addRadiosHorizontal("report.params.fiscal.year", formLayout,
				fiscalYearValues.keys(), fiscalYearValues.values());
		fiscalYearEl.addActionListener(FormEvent.ONCHANGE);
		fiscalYearEl.select(ReportTimeRange.currentFiscalYear.name(), true);

		resolvedDatesEl = uifactory.addDateChooser("resolved.dates", "report.params.from.to", null, formLayout);
		resolvedDatesEl.setSeparator("report.params.separator");
		resolvedDatesEl.setSecondDate(true);
		resolvedDatesEl.setEnabled(false);

		// Relative to today
		SelectionValues relativeValues = new SelectionValues();
		for(ReportTimeRange relative:RELATIVES) {
			relativeValues.add(new SelectionValue(relative.name(), translate("report.params.time.range." + relative.name())));
		}
		relativeEl = uifactory.addRadiosHorizontal("report.params.time.range.relative", formLayout,
				relativeValues.keys(), relativeValues.values());
		relativeEl.setElementCssClass("o_sel_report_params_relative");
		relativeEl.select(ReportTimeRange.last4Weeks.name(), true);

		// Preselect the default of the template
		if(defaultTimeRange != null) {
			switch(defaultTimeRange) {
				case custom -> dateRangeEl.select(DATE_RANGE_CUSTOM, true);
				case timePeriod -> dateRangeEl.select(DATE_RANGE_TIME_PERIOD, true);
				case currentFiscalYear, lastFiscalYear, last2FiscalYears -> {
					dateRangeEl.select(DATE_RANGE_FISCAL_YEAR, true);
					fiscalYearEl.select(defaultTimeRange.name(), true);
				}
				case last7Days, last4Weeks, last6Months, last12Months, last24Months -> relativeEl.select(defaultTimeRange.name(), true);
				default -> { /* all */ }
			}
		}
		if(!dateRangeEl.isOneSelected()) {
			dateRangeEl.select(DATE_RANGE_CUSTOM, true);
		}
	}

	private void initChangedSinceForm(FormItemContainer formLayout) {
		FormSection restrictionsSection = uifactory.addFormSection("restrictions",
				translate("report.params.additional.restrictions"), formLayout, FormSection.Level.SUB_TITLE);
		restrictionsSection.setCollapsible(true);

		changedSinceEl = uifactory.addDateChooser("report.params.changed.since", null, restrictionsSection);
		changedSinceEl.setElementCssClass("o_sel_report_params_changed_since");
		String hint = template.getParametersText(ReportParametersText.changedSinceHint, getLocale());
		if(StringHelper.containsNonWhitespace(hint)) {
			changedSinceEl.setExampleKey("noTransOnlyParam", new String[] { hint });
		}
	}

	private String getTimePeriodLabel(RepositoryEntryLifecycle timePeriod) {
		StringBuilder sb = new StringBuilder(32);
		boolean labelAvailable = StringHelper.containsNonWhitespace(timePeriod.getLabel());
		if(labelAvailable) {
			sb.append(timePeriod.getLabel());
		}
		if(StringHelper.containsNonWhitespace(timePeriod.getSoftKey())) {
			if(labelAvailable) {
				sb.append(" - ");
			}
			sb.append(timePeriod.getSoftKey());
		}
		return sb.toString();
	}

	private ReportParameterDefinition getDefinition(ReportParameterName name) {
		return definitions.stream()
				.filter(def -> def.name() == name)
				.findFirst().orElse(null);
	}

	private void updateUI() {
		if(timeRangeEl == null) {
			return;
		}

		ReportTimeRange.Group group = ReportTimeRange.Group.valueOf(timeRangeEl.getSelectedKey());
		boolean specific = group == ReportTimeRange.Group.specific;
		String dateRange = dateRangeEl.isOneSelected() ? dateRangeEl.getSelectedKey() : DATE_RANGE_CUSTOM;

		dateRangeEl.setVisible(specific);
		customDatesEl.setVisible(specific && DATE_RANGE_CUSTOM.equals(dateRange));
		timePeriodEl.setVisible(specific && DATE_RANGE_TIME_PERIOD.equals(dateRange));
		fiscalYearEl.setVisible(specific && DATE_RANGE_FISCAL_YEAR.equals(dateRange));
		resolvedDatesEl.setVisible(specific && !DATE_RANGE_CUSTOM.equals(dateRange));
		relativeEl.setVisible(group == ReportTimeRange.Group.relative);

		if(resolvedDatesEl.isVisible()) {
			updateResolvedDates(dateRange);
		}
	}

	/**
	 * Shows the first and the last day of the chosen time period or fiscal year.
	 */
	private void updateResolvedDates(String dateRange) {
		Date from = null;
		Date to = null;
		if(DATE_RANGE_TIME_PERIOD.equals(dateRange)) {
			RepositoryEntryLifecycle timePeriod = getSelectedTimePeriod();
			if(timePeriod != null) {
				from = timePeriod.getValidFrom();
				to = timePeriod.getValidTo();
			}
		} else if(DATE_RANGE_FISCAL_YEAR.equals(dateRange) && fiscalYearEl.isOneSelected()) {
			ReportTimeRange fiscalYear = ReportTimeRange.valueOf(fiscalYearEl.getSelectedKey());
			ReportDateRange range = fiscalYear.resolve(new Date(), null, null,
					curriculumModule.getReportsAccountingFiscalYearStartDay(),
					curriculumModule.getReportsAccountingFiscalYearStartMonth());
			from = range.from();
			to = range.toExclusive() == null ? null : DateUtils.addDays(range.toExclusive(), -1);
		}
		resolvedDatesEl.setDate(from);
		resolvedDatesEl.setSecondDate(to);
	}

	private RepositoryEntryLifecycle getSelectedTimePeriod() {
		if(!timePeriodEl.isOneSelected()) {
			return null;
		}
		Long key = Long.valueOf(timePeriodEl.getSelectedKey());
		return timePeriods.stream()
				.filter(timePeriod -> key.equals(timePeriod.getKey()))
				.findFirst().orElse(null);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(timeRangeEl == source || dateRangeEl == source
				|| timePeriodEl == source || fiscalYearEl == source) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		if(customDatesEl != null) {
			customDatesEl.clearError();
			if(customDatesEl.isVisible()) {
				if(customDatesEl.getDate() == null || customDatesEl.getSecondDate() == null) {
					customDatesEl.setErrorKey("form.legende.mandatory");
					allOk &= false;
				} else if(customDatesEl.getSecondDate().before(DateUtils.getStartOfDay(customDatesEl.getDate()))) {
					customDatesEl.setErrorKey("error.report.params.range");
					allOk &= false;
				}
			}
		}

		if(timePeriodEl != null) {
			timePeriodEl.clearError();
			if(timePeriodEl.isVisible()) {
				RepositoryEntryLifecycle timePeriod = getSelectedTimePeriod();
				if(timePeriod == null) {
					timePeriodEl.setErrorKey("form.legende.mandatory");
					allOk &= false;
				} else if(lifecycleDao.loadById(timePeriod.getKey()) == null) {
					timePeriodEl.setErrorKey("error.report.params.time.period.deleted");
					allOk &= false;
				}
			}
		}

		if(changedSinceEl != null) {
			changedSinceEl.clearError();
			Date startOfTomorrow = DateUtils.getStartOfDay(DateUtils.addDays(new Date(), 1));
			if(changedSinceEl.getDate() != null && !changedSinceEl.getDate().before(startOfTomorrow)) {
				changedSinceEl.setErrorKey("error.report.params.changed.since.future");
				allOk &= false;
			}
		}

		return allOk;
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		parameters = buildParameters();
		fireEvent(ureq, Event.DONE_EVENT);
	}

	private ReportParameters buildParameters() {
		ReportParameters params = new ReportParameters();
		if(timeRangeEl != null) {
			ReportTimeRange.Group group = ReportTimeRange.Group.valueOf(timeRangeEl.getSelectedKey());
			if(group == ReportTimeRange.Group.all) {
				params.setTimeRange(ReportTimeRange.all);
			} else if(group == ReportTimeRange.Group.relative) {
				params.setTimeRange(ReportTimeRange.valueOf(relativeEl.getSelectedKey()));
			} else {
				String dateRange = dateRangeEl.getSelectedKey();
				if(DATE_RANGE_TIME_PERIOD.equals(dateRange)) {
					params.setTimeRange(ReportTimeRange.timePeriod);
					RepositoryEntryLifecycle timePeriod = getSelectedTimePeriod();
					params.setTimePeriodKey(timePeriod == null ? null : timePeriod.getKey());
				} else if(DATE_RANGE_FISCAL_YEAR.equals(dateRange)) {
					params.setTimeRange(ReportTimeRange.valueOf(fiscalYearEl.getSelectedKey()));
				} else {
					params.setTimeRange(ReportTimeRange.custom);
					params.setFrom(customDatesEl.getDate());
					params.setTo(customDatesEl.getSecondDate());
				}
			}
		}
		if(changedSinceEl != null) {
			params.setChangedSince(changedSinceEl.getDate());
		}
		return params;
	}
}

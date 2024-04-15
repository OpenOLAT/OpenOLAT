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
package org.olat.core.gui.components.form.flexible.impl.elements.table.filter;

import java.time.Period;
import java.time.temporal.ChronoUnit;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableElementImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTablePeriodFilter.PeriodWithUnit;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;

/**
 * 
 * Initial date: 18 Nov 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class FlexiFilterPeriodController extends FormBasicController {
	
	private TextElement valueEl;
	private SingleSelection unitEl;
	private SingleSelection pastEl;
	private FormLink clearButton;
	private FormLink updateButton;

	private PeriodWithUnit filterPeriod;
	private final FlexiTablePeriodFilter filter;

	public FlexiFilterPeriodController(UserRequest ureq, WindowControl wControl, FlexiTablePeriodFilter filter) {
		super(ureq, wControl, "period", Util.createPackageTranslator(FlexiTableElementImpl.class, ureq.getLocale()));
		this.filter = filter;
		filterPeriod = filter.getPeriodWithUnit();
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		SelectionValues pastPK = new SelectionValues();
		pastPK.add(SelectionValues.entry("future", translate("filter.future")));
		pastPK.add(SelectionValues.entry("past", translate("filter.past")));
		pastEl = uifactory.addDropdownSingleselect("past", null, formLayout, pastPK.keys(), pastPK.values());
		pastEl.setDomReplacementWrapperRequired(false);
		
		String val = filterPeriod == null ? "" : Integer.toString(filterPeriod.value());
		valueEl = uifactory.addTextElement("value", null, 5, val, formLayout);
		valueEl.setDisplaySize(5);
		valueEl.setDomReplacementWrapperRequired(false);

		SelectionValues unitPK = new SelectionValues();
		unitPK.add(SelectionValues.entry(ChronoUnit.DAYS.name(), translate("filter.day")));
		unitPK.add(SelectionValues.entry(ChronoUnit.WEEKS.name(), translate("filter.week")));
		unitPK.add(SelectionValues.entry(ChronoUnit.MONTHS.name(), translate("filter.month")));
		unitPK.add(SelectionValues.entry(ChronoUnit.YEARS.name(), translate("filter.year")));
		unitEl = uifactory.addDropdownSingleselect("unit", null, formLayout, unitPK.keys(), unitPK.values());
		unitEl.setDomReplacementWrapperRequired(false);
		String unit = filterPeriod == null ? ChronoUnit.DAYS.name() : filterPeriod.unit().name();
		unitEl.select(unit, true);
		
		updateButton = uifactory.addFormLink("update", formLayout, Link.BUTTON_SMALL);
		updateButton.setElementCssClass("o_sel_flexiql_update");
		clearButton = uifactory.addFormLink("clear", formLayout, Link.LINK);
		clearButton.setElementCssClass("o_filter_clear");
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		valueEl.clearError();
		if(!StringHelper.containsNonWhitespace(valueEl.getValue())) {
			valueEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		} else if(!StringHelper.isLong(valueEl.getValue())) {
			valueEl.setErrorKey("form.error.nointeger");
			allOk &= false;
		}
		
		unitEl.clearError();
		if(!unitEl.isOneSelected()) {
			unitEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(clearButton == source) {
			doClear(ureq);
		} else if(updateButton == source && validateFormLogic(ureq)) {
			doUpdate(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doUpdate(UserRequest ureq) {
		if(unitEl.isOneSelected() && StringHelper.isLong(valueEl.getValue())) {
			fireEvent(ureq, new ChangeValueEvent(filter, toPeriod()));
		} else {
			fireEvent(ureq, new ChangeValueEvent(filter, null));
		}
	}
	
	private void doClear(UserRequest ureq) {
		if(unitEl.isOneSelected()) {
			valueEl.setValue("");
			unitEl.select(ChronoUnit.DAYS.name(), true);
		}
		fireEvent(ureq, new ChangeValueEvent(filter, null));
	}
	
	private PeriodWithUnit toPeriod() {
		int value = Integer.parseInt(valueEl.getValue());
		boolean past = pastEl.isOneSelected() && "past".equals(pastEl.getSelectedKey());
		ChronoUnit unit = ChronoUnit.valueOf(unitEl.getSelectedKey());
		return switch(unit) {
			case DAYS -> new PeriodWithUnit(Period.ofDays(value), past, value, ChronoUnit.DAYS);
			case WEEKS -> new PeriodWithUnit(Period.ofWeeks(value), past, value, ChronoUnit.WEEKS);
			case MONTHS -> new PeriodWithUnit(Period.ofMonths(value), past, value, ChronoUnit.MONTHS);
			case YEARS -> new PeriodWithUnit(Period.ofYears(value), past, value, ChronoUnit.YEARS);
			default -> new PeriodWithUnit(Period.ofDays(value), past, value, ChronoUnit.DAYS);
		};
	}
}

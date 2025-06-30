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

import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableDateRangeFilter.DateRange;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;

/**
 * 
 * Initial date: 18 Nov 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class FlexiFilterDateRangeController extends FlexiFilterExtendedController {
	
	private DateChooser rangeEl;

	private final FlexiTableDateRangeFilter filter;
	private final boolean timeEnabled;
	private final DateRange initialDateRange;

	public FlexiFilterDateRangeController(UserRequest ureq, WindowControl wControl, Form form, FlexiTableDateRangeFilter filter,
			boolean timeEnabled, DateRange initialDateRange) {
		super(ureq, wControl, LAYOUT_VERTICAL, null, form);
		this.filter = filter;
		this.timeEnabled = timeEnabled;
		this.initialDateRange = initialDateRange;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer datesCont = FormLayoutContainer.createCustomFormLayout("dates", getTranslator(), velocity_root + "/filter_date_range.html");
		datesCont.setRootForm(mainForm);
		formLayout.add("dates", datesCont);
		
		Date start = initialDateRange != null? initialDateRange.getStart(): null;
		Date end = initialDateRange != null? initialDateRange.getEnd(): null;
		rangeEl = uifactory.addDateChooser("range", null, start, datesCont);
		rangeEl.setElementCssClass("o_date_scope_range");
		rangeEl.showLabel(true);
		rangeEl.setLabel("from", null);
		rangeEl.setDateChooserTimeEnabled(timeEnabled);
		rangeEl.setSecondDate(true);
		rangeEl.setSecondDate(end);
		rangeEl.setSeparator("to.separator");
	}
	
	@Override
	protected void propagateDirtinessToContainer(FormItem source, FormEvent fe) {
		if (source == rangeEl) {
			super.propagateDirtinessToContainer(source, fe);
		}
	}
	
	@Override
	public void doUpdate(UserRequest ureq) {
		if(rangeEl.getDate() != null || rangeEl.getSecondDate() != null) {
			DateRange dateRange = new DateRange();
			dateRange.setStart(rangeEl.getDate());
			dateRange.setEnd(rangeEl.getSecondDate());
			String value = FlexiTableDateRangeFilter.toString(dateRange);
			fireEvent(ureq, new ChangeValueEvent(filter, value));
		} else {
			fireEvent(ureq, new ChangeValueEvent(filter, null));
		}
	}
	
	@Override
	public void doClear(UserRequest ureq) {
		rangeEl.setDate(null);
		rangeEl.setSecondDate(null);
		fireEvent(ureq, new ChangeValueEvent(filter, null));
	}

}

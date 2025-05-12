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
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableElementImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableDateRangeFilter.DateRange;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;

/**
 * 
 * Initial date: 18 Nov 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class FlexiFilterDateRangeController extends FormBasicController {
	
	private DateChooser rangeEl;
	private FormLink clearButton;
	private FormLink updateButton;

	private final FlexiTableDateRangeFilter filter;
	private final boolean timeEnabled;
	private final String dateLabel;
	private final String separator;
	private final DateRange initialDateRange;

	public FlexiFilterDateRangeController(UserRequest ureq, WindowControl wControl, FlexiTableDateRangeFilter filter,
			boolean timeEnabled, String dateLabel, String separator, DateRange initialDateRange) {
		super(ureq, wControl, LAYOUT_VERTICAL, Util.createPackageTranslator(FlexiTableElementImpl.class, ureq.getLocale()));
		this.filter = filter;
		this.timeEnabled = timeEnabled;
		this.dateLabel = dateLabel;
		this.separator = separator;
		this.initialDateRange = initialDateRange;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer datesCont = FormLayoutContainer.createVerticalFormLayout("dates", getTranslator());
		datesCont.setRootForm(mainForm);
		formLayout.add("dates", datesCont);
		
		Date start = initialDateRange != null? initialDateRange.getStart(): null;
		Date end = initialDateRange != null? initialDateRange.getEnd(): null;
		rangeEl = uifactory.addDateChooser("start", null, start, datesCont);
		rangeEl.setElementCssClass("o_date_scope_range");
		rangeEl.showLabel(true);
		rangeEl.setLabel(dateLabel, null, false);
		rangeEl.setDateChooserTimeEnabled(timeEnabled);
		rangeEl.setSecondDate(true);
		rangeEl.setSecondDate(end);
		rangeEl.setSeparator(separator, true);
		
		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		updateButton = uifactory.addFormLink("update", buttonsCont, Link.BUTTON_SMALL);
		updateButton.setElementCssClass("o_sel_flexiql_update");
		clearButton = uifactory.addFormLink("clear", buttonsCont, Link.LINK);
		clearButton.setElementCssClass("o_filter_clear");
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (clearButton == source) {
			doClear(ureq);
		} else if(updateButton == source) {
			doUpdate(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void propagateDirtinessToContainer(FormItem source, FormEvent fe) {
		if (source == clearButton || source == rangeEl) {
			super.propagateDirtinessToContainer(source, fe);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doUpdate(ureq);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doUpdate(UserRequest ureq) {
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
	
	private void doClear(UserRequest ureq) {
		rangeEl.setDate(null);
		rangeEl.setSecondDate(null);
		fireEvent(ureq, new ChangeValueEvent(filter, null));
	}

}

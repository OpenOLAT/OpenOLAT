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
	
	private DateChooser startEl;
	private DateChooser endEl;
	private FormLink clearButton;
	private FormLink updateButton;

	private final FlexiTableDateRangeFilter filter;
	private final boolean timeEnabled;
	private final String startLabel;
	private final String endLabel;
	private final DateRange initialDateRange;

	public FlexiFilterDateRangeController(UserRequest ureq, WindowControl wControl, FlexiTableDateRangeFilter filter,
			boolean timeEnabled, String startLabel, String endLabel, DateRange initialDateRange) {
		super(ureq, wControl, "date_range", Util.createPackageTranslator(FlexiTableElementImpl.class, ureq.getLocale()));
		this.filter = filter;
		this.timeEnabled = timeEnabled;
		this.startLabel = startLabel;
		this.endLabel = endLabel;
		this.initialDateRange = initialDateRange;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer datesCont = FormLayoutContainer.createVerticalFormLayout("dates", getTranslator());
		datesCont.setRootForm(mainForm);
		formLayout.add("dates", datesCont);
		
		Date start = initialDateRange != null? initialDateRange.getStart(): null;
		startEl = uifactory.addDateChooser("start", null, start, datesCont);
		startEl.showLabel(true);
		startEl.setLabel(startLabel, null, false);
		startEl.setDateChooserTimeEnabled(timeEnabled);
		
		Date end = initialDateRange != null? initialDateRange.getEnd(): null;
		endEl = uifactory.addDateChooser("end", null, end, datesCont);
		endEl.showLabel(true);
		endEl.setLabel(endLabel, null, false);
		endEl.setDateChooserTimeEnabled(timeEnabled);
		
		updateButton = uifactory.addFormLink("update", formLayout, Link.BUTTON_SMALL);
		updateButton.setElementCssClass("o_sel_flexiql_update");
		clearButton = uifactory.addFormLink("clear", formLayout, Link.LINK);
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
		if (source == clearButton || source == startEl || source == endEl) {
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
		if(startEl.getDate() != null || endEl.getDate() != null) {
			DateRange dateRange = new DateRange();
			dateRange.setStart(startEl.getDate());
			dateRange.setEnd(endEl.getDate());
			String value = FlexiTableDateRangeFilter.toString(dateRange);
			fireEvent(ureq, new ChangeValueEvent(filter, value));
		} else {
			fireEvent(ureq, new ChangeValueEvent(filter, null));
		}
	}
	
	private void doClear(UserRequest ureq) {
		startEl.setValue(null);
		endEl.setValue(null);
		fireEvent(ureq, new ChangeValueEvent(filter, null));
	}

}

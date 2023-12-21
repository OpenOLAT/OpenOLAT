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
package org.olat.core.gui.components.scope;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.DateRange;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Formatter;

/**
 * 
 * Initial date: 28 Nov 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CustomDateScopeController extends FormBasicController {

	public static final Event RESET_EVENT = new Event("resetscope");
	private static final String CMD_PREDEFINED = "predefined";
	
	private DateChooser daterangeEl;
	private FormLink resetLink;
	
	private final List<DateScope> additionalDateScopes;
	private final DateRange initialDateRange;
	private final DateRange limit;
	private int counter = 0;

	protected CustomDateScopeController(UserRequest ureq, WindowControl wControl, List<DateScope> additionalDateScopes,
			DateRange initialDateRange, DateRange limit) {
		super(ureq, wControl);
		this.additionalDateScopes = additionalDateScopes;
		this.initialDateRange = initialDateRange;
		this.limit = limit;
		
		initForm(ureq);
	}

	public DateRange getDateRange() {
		Date from = DateUtils.getStartOfDay(daterangeEl.getDate());
		Date to = DateUtils.getEndOfDay(daterangeEl.getSecondDate());
		return new DateRange(from, to);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		Date from = initialDateRange != null ? initialDateRange.getFrom() : null;
		Date to = initialDateRange != null ? initialDateRange.getTo() : null;
		daterangeEl = uifactory.addDateChooser("date.scope.custom.range", from, formLayout);
		daterangeEl.setElementCssClass("o_date_scope_range");
		daterangeEl.setSecondDate(true);
		daterangeEl.setSeparator("date.scope.custom.separator");
		daterangeEl.setSecondDate(to);
		
		if (additionalDateScopes != null && !additionalDateScopes.isEmpty()) {
			FormLayoutContainer predefinedCont = FormLayoutContainer.createCustomFormLayout(CMD_PREDEFINED, getTranslator(), velocity_root + "/predefined_ranges.html");
			predefinedCont.setRootForm(mainForm);
			predefinedCont.setLabel("date.scope.custom.predefined", null);
			formLayout.add(predefinedCont);
			
			List<String> predefinedNames = new ArrayList<>(additionalDateScopes.size());
			for (DateScope dateScope : additionalDateScopes) {
				String name = "o_predef_" + counter++;
				predefinedNames.add(name);
				FormLink link = uifactory.addFormLink(name, CMD_PREDEFINED, "", null, predefinedCont, Link.BUTTON_XSMALL + Link.NONTRANSLATED);
				link.setI18nKey(dateScope.getDisplayName());
				link.setElementCssClass("o_date_scope_predef");
				link.setUserObject(dateScope);
			}
			predefinedCont.contextPut("predefinedNames", predefinedNames);
		}
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("date.scope.custom.set", buttonsCont);
		resetLink = uifactory.addFormLink("date.scope.custom.reset", buttonsCont, Link.BUTTON);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == resetLink) {
			fireEvent(ureq, RESET_EVENT);
		} else if (source instanceof FormLink link) {
			if (CMD_PREDEFINED.equals(link.getCmd())) {
				if (link.getUserObject() instanceof DateScope dateScope) {
					updatesDatesUI(dateScope);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		daterangeEl.clearError();
		if (daterangeEl.getDate() == null || daterangeEl.getSecondDate() == null) {
			daterangeEl.setErrorKey("form.mandatory.hover");
			allOk &= false;
		} else if (daterangeEl.getDate().after(daterangeEl.getSecondDate())) {
			daterangeEl.setErrorKey("error.date.to.befre.date.from");
			allOk &= false;
		} else if (limit != null) {
			DateRange dateRange = getDateRange();
			if (limit.getFrom().after(dateRange.getFrom()) || limit.getTo().before(dateRange.getTo())) {
				daterangeEl.setErrorKey("error.outside.limit",
						Formatter.getInstance(getLocale()).formatDate(limit.getFrom()),
						Formatter.getInstance(getLocale()).formatDate(limit.getTo()));
				allOk &= false;
			}
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, FormEvent.DONE_EVENT);
	}
	
	private void updatesDatesUI(DateScope dateScope) {
		Date from = dateScope.getDateRange().getFrom();
		if (limit != null && limit.getFrom().after(from)) {
			from = limit.getFrom();
		}
		daterangeEl.setDate(from);
		Date to = dateScope.getDateRange().getTo();
		if (limit != null && limit.getTo().before(to)) {
			to = limit.getTo();
		}
		daterangeEl.setSecondDate(to);
	}

}

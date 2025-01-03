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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableElementImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableNumericalRangeFilter.NumericalRange;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;

/**
 * 
 * Initial date: 24 déc. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class FlexiFilterNumericalRangeController extends FormBasicController {
	
	private TextElement startEl;
	private TextElement endEl;
	private FormLink clearButton;
	private FormLink updateButton;

	private final FlexiTableNumericalRangeFilter filter;
	private final String startLabel;
	private final String endLabel;
	private final NumericalRange initialRange;

	public FlexiFilterNumericalRangeController(UserRequest ureq, WindowControl wControl, FlexiTableNumericalRangeFilter filter,
			String startLabel, String endLabel, NumericalRange initialRange) {
		super(ureq, wControl, "date_range", Util.createPackageTranslator(FlexiTableElementImpl.class, ureq.getLocale()));
		this.filter = filter;
		this.startLabel = startLabel;
		this.endLabel = endLabel;
		this.initialRange = initialRange;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer datesCont = FormLayoutContainer.createVerticalFormLayout("dates", getTranslator());
		datesCont.setRootForm(mainForm);
		formLayout.add("dates", datesCont);
		
		String start = initialRange != null? FlexiTableNumericalRangeFilter.formatNumerical(initialRange.getStart()) : null;
		startEl = uifactory.addTextElement("start", 12, start, datesCont);
		startEl.showLabel(true);
		startEl.setLabel(startLabel, null, false);
		
		String end = initialRange != null? FlexiTableNumericalRangeFilter.formatNumerical(initialRange.getEnd()) : null;
		endEl = uifactory.addTextElement("end", 12, end, datesCont);
		endEl.showLabel(true);
		endEl.setLabel(endLabel, null, false);
		
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
			if(validateFormLogic(ureq)) {
				doUpdate(ureq);
			}
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
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		allOk &= validateDouble(startEl);
		allOk &= validateDouble(endEl);
		return allOk;
	}
	
	private boolean validateDouble(TextElement el) {
		boolean allOk = true;
		
		String value = el.getValue();
		el.clearError();
		if(StringHelper.containsNonWhitespace(value)) {
			try {
				Double.parseDouble(value);
			} catch (NumberFormatException e) {
				el.setErrorKey("form.error.nofloat");
				allOk &= false;
			}
		}
		return allOk;
	}
	
	private void doUpdate(UserRequest ureq) {
		if(startEl.getValue() != null || endEl.getValue() != null) {
			NumericalRange dateRange = new NumericalRange();
			dateRange.setStart(toDouble(startEl.getValue()));
			dateRange.setEnd(toDouble(endEl.getValue()));
			String value = FlexiTableNumericalRangeFilter.toString(dateRange);
			fireEvent(ureq, new ChangeValueEvent(filter, value));
		} else {
			fireEvent(ureq, new ChangeValueEvent(filter, null));
		}
	}
	
	private Double toDouble(String val) {
		try {
			return Double.valueOf(val);
		} catch (NumberFormatException e) {
			getLogger().debug("Cannot parse: {}", val, e);
			return null;
		}
	}
	
	private void doClear(UserRequest ureq) {
		startEl.setValue(null);
		endEl.setValue(null);
		fireEvent(ureq, new ChangeValueEvent(filter, null));
	}
}

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
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableElementImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableNumericalRangeFilter.NumericalRange;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;

/**
 * 
 * Initial date: 24 déc. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class FlexiFilterNumericalRangeController extends FlexiFilterExtendedController {
	
	private TextElement startEl;
	private TextElement endEl;

	private final FlexiTableNumericalRangeFilter filter;
	private final String startLabel;
	private final String endLabel;
	private final NumericalRange initialRange;

	public FlexiFilterNumericalRangeController(UserRequest ureq, WindowControl wControl, Form form, FlexiTableNumericalRangeFilter filter,
			String startLabel, String endLabel, NumericalRange initialRange) {
		super(ureq, wControl, LAYOUT_CUSTOM, "date_range", form);
		setTranslator(Util.createPackageTranslator(FlexiTableElementImpl.class, ureq.getLocale()));
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
		
		String start = initialRange != null && initialRange.getStart() != null
				? FlexiTableNumericalRangeFilter.formatNumerical(initialRange.getStart()) : null;
		startEl = uifactory.addTextElement("start", 12, start, datesCont);
		startEl.showLabel(true);
		startEl.setLabel(startLabel, null, false);
		startEl.addActionListener(FormEvent.ONCHANGE);
		
		String end = initialRange != null && initialRange.getEnd() != null
				? FlexiTableNumericalRangeFilter.formatNumerical(initialRange.getEnd()) : null;
		endEl = uifactory.addTextElement("end", 12, end, datesCont);
		endEl.showLabel(true);
		endEl.setLabel(endLabel, null, false);
		endEl.addActionListener(FormEvent.ONCHANGE);
		
		updateClearButtonUI(ureq, isValueAvailable());
	}
	
	private boolean isValueAvailable() {
		return StringHelper.containsNonWhitespace(startEl.getValue()) || StringHelper.containsNonWhitespace(endEl.getValue());
	}
	
	@Override
	protected void propagateDirtinessToContainer(FormItem source, FormEvent fe) {
		if (source == startEl || source == endEl) {
			super.propagateDirtinessToContainer(source, fe);
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == startEl || source == endEl) {
			updateClearButtonUI(ureq, isValueAvailable());
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	public boolean validateFormLogic(UserRequest ureq) {
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
	
	@Override
	public void doUpdate(UserRequest ureq) {
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
	
	@Override
	public void doClear(UserRequest ureq) {
		startEl.setValue(null);
		endEl.setValue(null);
		fireEvent(ureq, new ChangeValueEvent(filter, null));
	}
}

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
package org.olat.course.duedate.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.JSDateChooser;
import org.olat.core.gui.components.form.flexible.impl.elements.SelectboxSelectionImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.TextElementImpl;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.ValidationStatus;
import org.olat.core.util.ValidationStatusImpl;
import org.olat.course.duedate.DueDateConfig;

/**
 * 
 * Initial date: 3 Nov 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DueDateFormItemImpl extends FormItemImpl implements DueDateConfigFormItem {

	private static final Logger log = Tracing.createLoggerFor(DueDateFormItemImpl.class);
	
	private SelectionValues relativeToDates;
	private boolean relative;
	private DueDateConfig initialDueDateConfig;
	
	private TextElementImpl numOfDaysEl;
	private SingleSelection realtiveToDateEl;
	private JSDateChooser absoluteDateEl;
	private final DueDateConfigComponent component;

	DueDateFormItemImpl(String name, SelectionValues relativeToDates, boolean relative, DueDateConfig initialDueDateConfig) {
		super(name);
		this.relativeToDates = relativeToDates;
		this.relative = relative;
		this.initialDueDateConfig = initialDueDateConfig;
		this.component = new DueDateConfigComponent(this);
	}

	@Override
	protected void rootFormAvailable() {
		if(numOfDaysEl == null) {
			int numOfDays = initialDueDateConfig.getNumOfDays();
			String numOfDaysValue = numOfDays >= 0 ? Integer.toString(numOfDays) : "";
			String numOfDaysName = "nod_" + getName();
			numOfDaysEl = new TextElementImpl(numOfDaysName, numOfDaysName, numOfDaysValue);
			numOfDaysEl.setDomReplacementWrapperRequired(false);
			numOfDaysEl.setRootForm(getRootForm());
			numOfDaysEl.setEnabled(isEnabled());
			numOfDaysEl.setNotLongerThanCheck(4, "text.element.error.notlongerthan");
			numOfDaysEl.setMaxLength(4);
			numOfDaysEl.setDisplaySize(4);
			
			String relativeToName = "rl_" + getName();
			realtiveToDateEl = new SelectboxSelectionImpl(relativeToName, relativeToName, getTranslator().getLocale());
			realtiveToDateEl.setDomReplacementWrapperRequired(false);
			realtiveToDateEl.setRootForm(getRootForm());
			realtiveToDateEl.setEnabled(isEnabled());
			
			realtiveToDateEl.setKeysAndValues(relativeToDates.keys(), relativeToDates.values(), null);
			String relativeToDateType = initialDueDateConfig.getRelativeToType();
			String selectedKey = Arrays.asList(realtiveToDateEl.getKeys()).contains(relativeToDateType)
					? relativeToDateType
					: realtiveToDateEl.getKey(0);
			realtiveToDateEl.select(selectedKey, true);
			
			absoluteDateEl = new JSDateChooser("ad_" + getFormDispatchId(), "ad_" + getName(), initialDueDateConfig.getAbsoluteDate(), getTranslator().getLocale()) {
				/**
				 * Catch the request to the date chooser and redirect it the our due date item.
				 * 
				 */
				@Override
				public void doDispatchFormRequest(UserRequest ureq) {
					DueDateFormItemImpl.this.doDispatchFormRequest(ureq);
				}
			};
			absoluteDateEl.setTranslator(getTranslator());
			absoluteDateEl.setValidDateCheck("form.error.date");
			absoluteDateEl.setRootForm(getRootForm());
			absoluteDateEl.setDateChooserTimeEnabled(true);
			absoluteDateEl.addActionListener(FormEvent.ONCHANGE);
		}
	}
	
	@Override
	public void validate(List<ValidationStatus> validationResults) {
		super.validate(validationResults);
		if (hasError()) {
			return;
		}
		
		if (!validateIntegerOrEmpty(numOfDaysEl)) {
			validationResults.add(new ValidationStatusImpl(ValidationStatus.ERROR));
		}
		if (!validateAbsulteDate()) {
			validationResults.add(new ValidationStatusImpl(ValidationStatus.ERROR));
		}
		
	}

	private boolean validateIntegerOrEmpty(TextElement textEl) {
		if (relative) {
			String val = textEl.getValue();
			if(StringHelper.containsNonWhitespace(val) && !StringHelper.isLong(val)) {
				setErrorKey("form.error.nointeger", null);
				return false;
			}
		}
		return true;
	}
	
	private boolean validateAbsulteDate() {
		List<ValidationStatus> validationResults = new ArrayList<>(1);
		absoluteDateEl.validate(validationResults);
		if (!validationResults.isEmpty()) {
			setErrorKey("form.error.date", null);
			return false;
		}
		return true;
	}

	@Override
	public void evalFormRequest(UserRequest ureq) {
		//
	}

	@Override
	public void doDispatchFormRequest(UserRequest ureq) {
		if(getRootForm().hasAlreadyFired()){
			//dispatchFormRequest did fire already
			//in this case we do not try to fire the general events
			return;
		}
		
		Form form = getRootForm();
		String dispatchuri = form.getRequestParameter("dispatchuri");
		if(action == getRootForm().getAction() && absoluteDateEl != null
				&& dispatchuri.equals(DISPPREFIX.concat(absoluteDateEl.getComponent().getDispatchID()))) {
			absoluteDateEl.evalFormRequest(ureq);
			getRootForm().fireFormEvent(ureq, new FormEvent("ONCHANGE", this, action));
		}
	}

	@Override
	public void setFocus(boolean hasFocus) {
		if(this.absoluteDateEl != null && absoluteDateEl.isVisible()) {
			absoluteDateEl.setFocus(true);
		} else {
			super.setFocus(hasFocus);
		}
	}

	@Override
	public void reset() {
		//
	}

	@Override
	protected Component getFormItemComponent() {
		return component;
	}

	@Override
	public Iterable<FormItem> getFormItems() {
		return List.of(numOfDaysEl, realtiveToDateEl, absoluteDateEl);
	}

	@Override
	public FormItem getFormComponent(String name) {
		for (FormItem item : getFormItems()) {
			if (item.getName().equals(name)) {
				return item;
			}
		}
		return null;
	}
	
	@Override
	public void setRelativeToDates(SelectionValues relativeToDates) {
		String currentKey = realtiveToDateEl.isOneSelected()? realtiveToDateEl.getSelectedKey(): null;
		
		realtiveToDateEl.setKeysAndValues(relativeToDates.keys(), relativeToDates.values(), null);
		String selectedKey = Arrays.asList(realtiveToDateEl.getKeys()).contains(currentKey)
				? currentKey
				: realtiveToDateEl.getKey(0);
		realtiveToDateEl.select(selectedKey, true);
	}
	
	public boolean isRelative() {
		return relative;
	}

	@Override
	public void setRelative(boolean relative) {
		this.relative = relative;
		clearError();
	}

	public TextElementImpl getNumOfDaysEl() {
		return numOfDaysEl;
	}

	public SingleSelection getRealtiveToDateEl() {
		return realtiveToDateEl;
	}

	public JSDateChooser getAbsoluteDateEl() {
		return absoluteDateEl;
	}

	@Override
	public DueDateConfig getDueDateConfig() {
		return relative
				? DueDateConfig.relative(getNumOfDays(), realtiveToDateEl.getSelectedKey())
				: DueDateConfig.absolute(absoluteDateEl.getDate());
	}
	
	private int getNumOfDays() {
		String val = numOfDaysEl.getValue();
		if (StringHelper.isLong(val)) {
			try {
				return Integer.parseInt(val);
			} catch (NumberFormatException e) {
				log.warn("", e);
			}
		}
		return -1;
	}

	@Override
	public void setDueDateConfig(DueDateConfig dueDateConfig) {
		int numOfDays = initialDueDateConfig.getNumOfDays();
		String numOfDaysValue = numOfDays >= 0 ? Integer.toString(numOfDays) : "";
		numOfDaysEl.setValue(numOfDaysValue);
		
		String realtiveToType = dueDateConfig.getRelativeToType();
		String selectedKey = Arrays.asList(realtiveToDateEl.getKeys()).contains(realtiveToType)
				? realtiveToType
				: realtiveToDateEl.getKey(0);
		realtiveToDateEl.select(selectedKey, true);
		
		absoluteDateEl.setDate(dueDateConfig.getAbsoluteDate());
	}

}

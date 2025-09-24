/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.certificationprogram.ui.component;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemCollection;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.SelectboxSelectionImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.TextElementImpl;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 4 sept. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class DurationFormItem extends FormItemImpl implements FormItemCollection {
	
	private static final Logger log = Tracing.createLoggerFor(DurationFormItem.class);
	
	private final TextElement valueEl;
	private final SingleSelection typeEl;
	
	private final DurationComponent component;
	
	public DurationFormItem(String name, Translator translator) {
		super(name);
		setTranslator(translator);
		
		String valueName = "val_" + getName();
		valueEl = new TextElementImpl(valueName, valueName, "");
		valueEl.setDomReplacementWrapperRequired(false);
		valueEl.setDisplaySize(4);
		valueEl.setMaxLength(4);
		
		String unitName = "vtype_" + getName();
		typeEl = new SelectboxSelectionImpl(unitName, unitName, translator.getLocale());
		typeEl.setTranslator(getTranslator());
		typeEl.setDomReplacementWrapperRequired(false);
		
		SelectionValues unitPK = new SelectionValues();
		unitPK.add(SelectionValues.entry(DurationType.day.name(), translator.translate("unit.days")));
		unitPK.add(SelectionValues.entry(DurationType.week.name(), translator.translate("unit.weeks")));
		unitPK.add(SelectionValues.entry(DurationType.month.name(), translator.translate("unit.months")));
		unitPK.add(SelectionValues.entry(DurationType.year.name(), translator.translate("unit.years")));
		typeEl.setKeysAndValues(unitPK.keys(), unitPK.values(), null);
		
		component = new DurationComponent(this, name);
	}
	
	@Override
	protected DurationComponent getFormItemComponent() {
		return component;
	}

	@Override
	public void reset() {
		//
	}
	
	public boolean isEmpty() {
		return !StringHelper.containsNonWhitespace(valueEl.getValue())
				|| !typeEl.isOneSelected();
	}
	
	public Integer getValue() {
		try {
			String val = valueEl.getValue();
			if(StringHelper.isLong(val)) {
				return Integer.valueOf(valueEl.getValue());
			}
		} catch (NumberFormatException e) {
			log.debug("", e);
		}
		return null;
	}
	
	public void setValue(String value) {
		valueEl.setValue(value);
	}
	
	protected TextElement getValueElement() {
		return valueEl;
	}
	
	public DurationType getType() {
		return typeEl.isOneSelected()
			? DurationType.valueOf(typeEl.getSelectedKey())
			: null;
	}
	
	public void setType(DurationType type) {
		if(type != null) {
			typeEl.select(type.name(), true);
		}
	}
	
	protected SingleSelection getTypeElement() {
		return typeEl;
	}

	@Override
	public Iterable<FormItem> getFormItems() {
		return List.of(valueEl, typeEl);
	}

	@Override
	public FormItem getFormComponent(String name) {
		if (valueEl.getName().equals(name)) {
			return valueEl;
		}
		if (typeEl.getName().equals(name)) {
			return typeEl;
		}
		return null;
	}

	@Override
	protected void rootFormAvailable() {
		if(valueEl.getRootForm() != getRootForm()) {
			valueEl.setRootForm(getRootForm());
		}
		if(typeEl.getRootForm() != getRootForm()) {
			typeEl.setRootForm(getRootForm());
		}
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
		if(action == getRootForm().getAction()
				&& dispatchuri.equals(DISPPREFIX.concat(typeEl.getComponent().getDispatchID()))) {
			valueEl.evalFormRequest(ureq);
			typeEl.evalFormRequest(ureq);
		}
	}
}

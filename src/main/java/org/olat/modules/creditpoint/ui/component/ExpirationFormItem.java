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
package org.olat.modules.creditpoint.ui.component;

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
import org.olat.modules.creditpoint.CreditPointExpirationType;

/**
 * 
 * Initial date: 21 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ExpirationFormItem extends FormItemImpl implements FormItemCollection {
	
	private static final Logger log = Tracing.createLoggerFor(ExpirationFormItem.class);
	
	private final TextElement valueEl;
	private final SingleSelection typeEl;
	
	private ExpirationComponent component;
	
	public ExpirationFormItem(String name, boolean withDefault, Translator translator) {
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
		if(withDefault) {
			unitPK.add(SelectionValues.entry(CreditPointExpirationType.DEFAULT.name(), translator.translate("expiration.type.default")));
		}
		unitPK.add(SelectionValues.entry(CreditPointExpirationType.DAY.name(), translator.translate("expiration.unit.day")));
		unitPK.add(SelectionValues.entry(CreditPointExpirationType.MONTH.name(), translator.translate("expiration.unit.month")));
		unitPK.add(SelectionValues.entry(CreditPointExpirationType.YEAR.name(), translator.translate("expiration.unit.year")));
		typeEl.setKeysAndValues(unitPK.keys(), unitPK.values(), null);
		
		component = new ExpirationComponent(this, name);
	}
	
	@Override
	protected ExpirationComponent getFormItemComponent() {
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
	
	public CreditPointExpirationType getType() {
		return typeEl.isOneSelected()
			? CreditPointExpirationType.valueOf(typeEl.getSelectedKey())
			: null;
	}
	
	public void setType(CreditPointExpirationType type) {
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

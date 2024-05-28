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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterItem;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement.Layout;
import org.olat.core.gui.components.form.flexible.impl.elements.MultipleSelectionElementImpl;
import org.olat.core.gui.components.util.SelectionValuesSupplier;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 12 juil. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FlexiTableOneClickSelectionFilter extends FlexiTableFilter implements FlexiTableFilterItem {

	private final SelectionValuesSupplier availableValues;
	private MultipleSelectionElement mse;
	
	private String value;
	
	public FlexiTableOneClickSelectionFilter(String label, String filter, SelectionValuesSupplier availableValues,
			boolean defaultVisible) {
		super(label, filter, defaultVisible);
		setDefaultVisible(defaultVisible);
		this.availableValues = availableValues;
	}
	
	public SelectionValuesSupplier getSelectionValues() {
		return availableValues;
	}

	@Override
	public String getValue() {
		if(mse == null) {
			return null;
		}
		if(!mse.isVisible()) {
			return value;
		}
		
		Collection<String> selectedKeys = mse.getSelectedKeys();
		Iterator<String> selectedKeysIt = selectedKeys.iterator();
		value = selectedKeysIt.hasNext() ? selectedKeysIt.next() : null;
		return value;
	}
	
	@Override
	public List<String> getValues() {
		List<String> values = new ArrayList<>();
		String val = getValue();
		if(val != null) {
			values.add(val);
		}
		return values;
	}
	
	@Override
	public void setValue(Object val) {
		value = convert(val);
		if(value != null && mse != null && mse.getKeys().contains(value)) {
			mse.uncheckAll();
			mse.select(value, true);
		}
	}
	
	private String convert(Object obj) {
		String val;
		if(obj == null) {
			val = null;
		} else if(obj instanceof List list && !list.isEmpty()) {
			val = list.get(0).toString();
		} else {
			val = obj.toString();
		}
		return val;
	}

	@Override
	public void reset() {
		value = null;
		if(mse != null) {
			mse.uncheckAll();
		}
	}
	
	@Override
	public String getDecoratedLabel(boolean withHtml) {
		return getDecoratedLabel(value, withHtml);
	}

	@Override
	public List<String> getHumanReadableValues() {
		List<String> hrValues = new ArrayList<>();
		if(value != null && !value.isEmpty()) {
			String selectionValue = availableValues.getValue(value);
			String valForLabel = selectionValue == null ? value : selectionValue;
			if(StringHelper.containsNonWhitespace(valForLabel)) {
				hrValues.add(StringHelper.unescapeHtml(valForLabel));
			}
		}
		return hrValues;
	}

	@Override
	public String getDecoratedLabel(Object objectValue, boolean withHtml) {
		StringBuilder label = new StringBuilder(getLabel());
		String val = convert(objectValue);
		if(StringHelper.containsNonWhitespace(val)) {
			String selectionValue = availableValues.getValue(val);
			String valForLabel = selectionValue == null ? val : selectionValue;
			if(valForLabel != null) {
				label.append(": ");
				if(withHtml) {
					label.append("<small>");
				}
				label.append("\"").append(valForLabel).append("\"");
				if(withHtml) {
					label.append("</small>");
				}
			}
		}
		return label.toString();
	}

	@Override
	public boolean isSelected() {
		return StringHelper.containsNonWhitespace(value);
	}

	@Override
	public FormItem getButtonFormItem(String id) {
		if(mse == null) {
			mse = new MultipleSelectionElementImpl(id, Layout.vertical, 1);
			mse.setDomReplacementWrapperRequired(false);
			mse.setKeysAndValues(availableValues.keys(), availableValues.values(), null, null);
			mse.setFormLayout("minimal");
			
			if(value != null && mse.getKeys().contains(value)) {
				mse.select(value, true);
			}
		}
		return mse;
	}
	
	@Override
	public Controller getController(UserRequest ureq, WindowControl wControl, Translator translator) {
		return new FlexiFilterOneClickSelectionController(ureq, wControl, this, getValues());
	}

	@Override
	public Controller getController(UserRequest ureq, WindowControl wControl, Translator translator, Object preselectedValue) {
		String preselectedKey = convert(preselectedValue);
		List<String> preselectedKeys = new ArrayList<>(2);
		if(StringHelper.containsNonWhitespace(preselectedKey)) {
			preselectedKeys.add(preselectedKey);
		}
		return new FlexiFilterOneClickSelectionController(ureq, wControl, this, preselectedKeys);
	}
}

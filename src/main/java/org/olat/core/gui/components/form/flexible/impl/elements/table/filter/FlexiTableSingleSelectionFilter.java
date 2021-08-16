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

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 12 juil. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FlexiTableSingleSelectionFilter extends FlexiTableFilter implements FlexiTableExtendedFilter {
	
	private final SelectionValues availableValues;
	
	private String value;
	
	public FlexiTableSingleSelectionFilter(String name, String filter, SelectionValues availableValues,
			boolean visible, boolean alwaysVisible) {
		super(name, filter, visible, alwaysVisible);
		this.availableValues = availableValues;
	}
	
	public SelectionValues getSelectionValues() {
		return availableValues;
	}

	@Override
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public void setValue(Object val) {
		this.value = val == null ? null : val.toString();
	}

	@Override
	public List<String> getValues() {
		if(StringHelper.containsNonWhitespace(value)) {
			return List.of(value);
		}
		return List.of();
	}

	@Override
	public void reset() {
		value = null;
	}
	
	@Override
	public String getDecoratedLabel() {
		StringBuilder label = new StringBuilder(getLabel());
		if(StringHelper.containsNonWhitespace(value)) {
			label.append(": <small>\"");
			SelectionValue selectionValue = getSelectionValue(value);
			if(selectionValue != null) {
				label.append(selectionValue.getValue());
			} else {
				label.append(value);
			}
			label.append("\"</small>");
		}
		return label.toString();
	}
	
	private SelectionValue getSelectionValue(String key) {
		List<SelectionValue> selectionValues = availableValues.keyValues();
		for(SelectionValue val:selectionValues) {
			if(key.equals(val.getKey())) {
				return val;
			}
		}
		return null;
	}

	@Override
	public boolean isSelected() {
		return value != null && !value.isEmpty();
	}

	@Override
	public Controller getController(UserRequest ureq, WindowControl wControl) {
		return new FlexiFilterSingleSelectionController(ureq, wControl, this);
	}

}

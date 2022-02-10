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
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
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
public class FlexiTableMultiSelectionFilter extends FlexiTableFilter implements FlexiTableExtendedFilter {

	private static final int MAX_EXPLANATION_LENGTH = 32;
	
	private final SelectionValues availableValues;
	
	private List<String> value;
	
	public FlexiTableMultiSelectionFilter(String label, String filter, SelectionValues availableValues,
			boolean defaultVisible) {
		super(label, filter, defaultVisible);
		setDefaultVisible(defaultVisible);
		this.availableValues = availableValues;
	}
	
	public SelectionValues getSelectionValues() {
		return availableValues;
	}

	@Override
	public String getValue() {
		if(value == null || value.isEmpty()) {
			return null;
		}
		if(value.size() == 1) {
			return value.get(0);
		}
		StringBuilder sb = new StringBuilder();
		for(String val:value) {
			if(sb.length() > 0) {
				sb.append(",");
			}
			sb.append(val);
		}
		
		return sb.toString();
	}
	
	@Override
	public List<String> getValues() {
		return value;
	}

	public void setValues(List<String> value) {
		this.value = value;
	}
	
	@Override
	public void setValue(Object val) {
		this.value = convert(val);
		if(val == null) {
			this.value = null;
		} else if(val instanceof List) {
			@SuppressWarnings("unchecked")
			List<String> vals = (List<String>)val;
			this.value = new ArrayList<>(vals);
		} else {
			this.value = new ArrayList<>();
			this.value.add(val.toString());
		}
	}
	
	private static List<String> convert(Object val) {
		if(val == null) {
			return null;
		} else if(val instanceof List) {
			@SuppressWarnings("unchecked")
			List<String> vals = (List<String>)val;
			return new ArrayList<>(vals);
		}
		List<String> list = new ArrayList<>();
		list.add(val.toString());
		return list;
	}

	@Override
	public void reset() {
		value = null;
	}
	
	/**
	 * @return A filtered list with selected long values.
	 */
	public List<Long> getLongValues() {
		if(value == null) return null;
		
		return value.stream()
				.filter(StringHelper::isLong)
				.map(Long::valueOf)
				.collect(Collectors.toList());
	}
	
	@Override
	public String getDecoratedLabel(boolean withHtml) {
		return getDecoratedLabel(value, withHtml);
	}

	@Override
	public String getDecoratedLabel(Object objectValue, boolean withHtml) {
		StringBuilder label = new StringBuilder(getLabel());
		List<String> list = convert(objectValue);
		if(list != null && !list.isEmpty()) {
			int currentLength = 0;
			for(String val:list) {
				SelectionValue selectionValue = getSelectionValue(val);
				String valForLabel = selectionValue == null ? val : selectionValue.getValue();
				if(valForLabel != null) {
					if(currentLength == 0) {
						label.append(": ");
						if(withHtml) {
							label.append("<small>");
						}
						label.append("\"");
					} else {
						if(withHtml && currentLength + valForLabel.length() > MAX_EXPLANATION_LENGTH) {
							label.append("\u2026");
							break;
						}
						label.append(", ");
					}

					label.append(valForLabel);
					currentLength += valForLabel.length();
				}
			}
			
			if(currentLength > 0) {
				label.append("\"");
				if(withHtml) {
					label.append("</small>");
				}
			}
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
	public Controller getController(UserRequest ureq, WindowControl wControl, Translator translator) {
		return new FlexiFilterMultiSelectionController(ureq, wControl, this, getValues());
	}

	@Override
	public Controller getController(UserRequest ureq, WindowControl wControl, Translator translator, Object preselectedValue) {
		List<String> preselectedKeys = convert(preselectedValue);
		return new FlexiFilterMultiSelectionController(ureq, wControl, this, preselectedKeys);
	}
}

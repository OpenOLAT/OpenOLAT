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
public class FlexiTableTextFilter extends FlexiTableFilter implements FlexiTableExtendedFilter {
	
	private String value;
	private String textAddOn;
	
	public FlexiTableTextFilter(String label, String filter, boolean defaultVisible) {
		super(label, filter);
		setDefaultVisible(defaultVisible);
	}
	
	public String getTextAddOn() {
		return textAddOn;
	}

	public void setTextAddOn(String textAddOn) {
		this.textAddOn = textAddOn;
	}

	@Override
	public boolean isSelected() {
		return value != null;
	}

	@Override
	public String getDecoratedLabel(boolean withHtml) {
		return getDecoratedLabel(value, withHtml);
	}
	
	@Override
	public String getDecoratedLabel(Object objectValue, boolean withHtml) {
		StringBuilder label = new StringBuilder(getLabel());
		String string = objectValue == null ? null : objectValue.toString();
		if(StringHelper.containsNonWhitespace(string)) {
			label.append(": ");
			if(withHtml) {
				label.append("<small>");
			}
			label.append("\"").append(StringHelper.escapeHtml(string)).append("\"");
			if(withHtml) {
				label.append("</small>");
			}
		}
		return label.toString();
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
	public List<String> getHumanReadableValues() {
		return getValues();
	}
	
	@Override
	public void reset() {
		value = null;
	}

	@Override
	public Controller getController(UserRequest ureq, WindowControl wControl, Translator translator) {
		return getController(ureq, wControl, translator, getValue());
	}
	
	@Override
	public Controller getController(UserRequest ureq, WindowControl wControl, Translator translator, Object val) {
		String preselectedValue = val instanceof String ? (String)val : null;
		FlexiFilterTextController filterEl = new FlexiFilterTextController(ureq, wControl, this, preselectedValue, translator);
		if(StringHelper.containsNonWhitespace(textAddOn)) {
			filterEl.setTextAddOn(textAddOn);
		}
		return filterEl;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if(obj instanceof FlexiTableTextFilter) {
			return super.equals(obj);
		}
		return false;
	}
}

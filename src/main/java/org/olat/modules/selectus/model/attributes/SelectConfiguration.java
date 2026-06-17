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
package org.olat.modules.selectus.model.attributes;

import java.util.List;
import java.util.Locale;

import org.olat.core.util.StringHelper;

import org.olat.modules.selectus.ui.RecruitingHelper;

/**
 * 
 * Initial date: 17 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SelectConfiguration implements AttributeConfiguration {
	
	private boolean multiple;
	private Display display;
	private Order order;
	private boolean other;
	private List<Option> options;
	
	public static SelectConfiguration defaultSingle() {
		SelectConfiguration select = new SelectConfiguration();
		select.setMultiple(false);
		select.setOther(false);
		select.setDisplay(Display.dropdown);
		select.setOrder(Order.alphabetically);
		return select;
	}
	
	public static SelectConfiguration defaultMultiple() {
		SelectConfiguration select = new SelectConfiguration();
		select.setMultiple(true);
		select.setOther(false);
		select.setDisplay(Display.list);
		select.setOrder(Order.alphabetically);
		return select;
	}

	public boolean isMultiple() {
		return multiple;
	}

	public void setMultiple(boolean multiple) {
		this.multiple = multiple;
	}

	public Display getDisplay() {
		return display;
	}

	public void setDisplay(Display display) {
		this.display = display;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public boolean isOther() {
		return other;
	}

	public void setOther(boolean other) {
		this.other = other;
	}

	public List<Option> getOptions() {
		return options;
	}

	public void setOptions(List<Option> options) {
		this.options = options;
	}
	
	public enum Display {
		dropdown,
		list
	}
	
	public enum Order {
		alphabetically,
		asEntered
	}
	
	public static class Option {
		
		private String key;
		private String value;
		private String valueDe;
		private String valueFr;
		
		public String getKey() {
			return key;
		}
		
		public void setKey(String key) {
			this.key = key;
		}
		
		public String getValue() {
			return value;
		}
		
		public void setValue(String value) {
			this.value = value;
		}
		
		public String getValueDe() {
			return valueDe;
		}
		
		public void setValueDe(String valueDe) {
			this.valueDe = valueDe;
		}
		
		public String getValueFr() {
			return valueFr;
		}

		public void setValueFr(String valueFr) {
			this.valueFr = valueFr;
		}
		
		public String getValue(Locale locale) {
			if(locale != null && locale.getLanguage().equals("de")) {
				return getValueDe();
			}
			if(locale != null && locale.getLanguage().equals("fr")) {
				return getValueFr();
			}
			return getValue();
		}
		
		public String getValue(Locale locale, boolean lenient) {
			if(lenient) {
				return RecruitingHelper.mlStringLenient(getValue(), getValueDe(), getValueFr(), locale);
			}
			if(locale != null && locale.getLanguage().equals("de")) {
				return getValueDe();
			}
			if(locale != null && locale.getLanguage().equals("fr")) {
				return getValueFr();
			}
			return getValue(locale);
		}
		
		public void setValue(String text, Locale locale) {
			if(locale != null && locale.getLanguage().equals("de")) {
				setValueDe(text);
			} else if(locale != null && locale.getLanguage().equals("fr")) {
				setValueFr(text);
 			} else {
				setValue(text);
			}
		}
		
		public boolean isInList(List<String> list) {
			return (StringHelper.containsNonWhitespace(value) && list.contains(value))
					|| (StringHelper.containsNonWhitespace(valueDe) && list.contains(valueDe))
					|| (StringHelper.containsNonWhitespace(valueFr) && list.contains(valueFr));
		}
		
		public boolean isOption(String val) {
			return (StringHelper.containsNonWhitespace(value) && value.equals(val))
					|| (StringHelper.containsNonWhitespace(valueDe) && valueDe.equals(val))
					|| (StringHelper.containsNonWhitespace(valueFr) && valueFr.contains(val));
		}
	}
}

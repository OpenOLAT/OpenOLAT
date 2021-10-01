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
package org.olat.core.gui.components.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 24.09.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SelectionValues {
	
	private static final Comparator<String> nullSafeStringComparator = Comparator.nullsLast(String::compareToIgnoreCase);
	public static final Comparator<SelectionValue> VALUE_ASC = Comparator.comparing(SelectionValue::getValue, nullSafeStringComparator);
	
	private List<SelectionValue> keyValues = new ArrayList<>();
	
	public SelectionValues(SelectionValue... keyValues) {
		if (keyValues != null && keyValues.length != 0) {
			for (SelectionValue keyValue : keyValues) {
				add(keyValue);
			}
		}
	}
	
	public static SelectionValue entry(String key, String value) {
		return new SelectionValue(key, value);
	}
	
	/**
	 * Adds the key / value pair at the end of the pairs. Since the key has to be
	 * unique, an existing pair with the same key is removed.
	 *
	 * @param keyValue
	 */
	public void add(SelectionValue keyValue) {
		remove(keyValue.getKey());
		keyValues.add(keyValue);
	}
	
	/**
	 * Adds an array of key / value pairs
	 * 
	 * @param keyValues
	 */
	public void add(SelectionValue... keyValues) {
		if (keyValues == null || keyValues.length == 0) {
			return;
		}
		
		for (SelectionValue keyValue : keyValues) {
			add(keyValue);
		}
	}

	public void addAll(SelectionValues additionalKeyValues) {
		if (additionalKeyValues == null) return;
		
		for (SelectionValue additionalKeyValue : additionalKeyValues.keyValues) {
			add(additionalKeyValue);
		}
	}
	
	/**
	 * If a key / value pair with the key exists, the existing pair is replaced. If
	 * no pair exists, the new pair is put at the end.
	 * 
	 * @param keyValue
	 */
	public void replaceOrPut(SelectionValue keyValue) {
		if (containsKey(keyValue.getKey())) {
			replace(keyValue);
		} else {
			keyValues.add(keyValue);
		}
	}
	
	private void replace(SelectionValue keyValue) {
		int index = keyValues.indexOf(keyValue);
		keyValues.set(index, keyValue);
	}
	
	/**
	 * Removes the key / value pair with the appropriate key.
	 *
	 * @param key
	 */
	public void remove(String key) {
		keyValues.removeIf(kv -> kv.getKey().equals(key));
	}
	
	public SelectionValue get(String key) {
		return keyValues.stream().filter(kv -> key.equals(kv.getKey())).findFirst().orElse(null);
	}
	
	/**
	 * Returns a array of all keys. The method creates a new array every time it is invoked.
	 *
	 * @return
	 */
	public String[] keys() {
		return keyValues.stream().map(SelectionValue::getKey).toArray(String[]::new);
	}
	
	/**
	 * Returns a array of all values. The method creates a new array every time it is invoked.
	 *
	 * @return
	 */
	public String[] values() {
		return keyValues.stream().map(SelectionValue::getValue).toArray(String[]::new);
	}
	
	/**
	 * Returns a array of all descriptions. The method creates a new array every time it is invoked.
	 *
	 * @return
	 */
	public String[] descriptions() {
		return keyValues.stream().map(SelectionValue::getDescription).toArray(String[]::new);
	}
	
	/**
	 * Returns a array of all icons. The method creates a new array every time it is invoked.
	 *
	 * @return
	 */
	public String[] icons() {
		return keyValues.stream().map(SelectionValue::getIcon).toArray(String[]::new);
	}
	
	/**
	 * Returns a array of all custom css classes. The method creates a new array every time it is invoked.
	 *
	 * @return
	 */
	public String[] cssClasses() {
		return keyValues.stream().map(SelectionValue::getCssClass).toArray(String[]::new);
	}
	
	/**
	 * Returns a array of all enabled states. The method creates a new array every time it is invoked.
	 *
	 * @return
	 */
	public Boolean[] enabledStates() {
		return keyValues.stream().map(SelectionValue::isEnabled).toArray(Boolean[]::new);
	}
	
	public void sort(Comparator<SelectionValue> comparator) {
		keyValues.sort(comparator);
	}
	
	public boolean containsKey(String key) {
		return keyValues.stream().map(SelectionValue::getKey).anyMatch(k -> k.equals(key));
	}
	
	public int size() {
		return keyValues.size();
	}
	
	public boolean isEmpty() {
		return keyValues.isEmpty();
	}
	
	public List<SelectionValue> keyValues() {
		return new ArrayList<>(keyValues);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (SelectionValue keyValue : keyValues) {
			builder.append(keyValue.toString()).append(" ");
		}
		return builder.toString();
	}

	public final static class SelectionValue {
		
		private final String key;
		private final String value;
		private final String description;
		private final String icon;
		private final String cssClass;
		private boolean enabled;
		
		
		public SelectionValue(String key, String value, String description, String icon, String cssClass, boolean enabled) {
			this.key = key;
			this.value = value;
			this.description = description;
			this.icon = icon;
			this.cssClass = cssClass;
			this.enabled = enabled;
		}
		
		public SelectionValue(String key, String value, String cssClass, boolean enabled) {
			this(key, value, null, null, cssClass, enabled);
		}
		
		public SelectionValue(String key, String value, String description) {
			this(key, value, description, null, null, true);
		}
		
		public SelectionValue(String key, String value) {
			this(key, value, null, null, null, true);
		}

		public String getKey() {
			return key;
		}

		public String getValue() {
			return value;
		}
		
		public String getIcon() {
			return icon;
		}
		
		public String getDescription() {
			return description;
		}
		
		public String getCssClass() {
			return cssClass;
		}
		
		public boolean isEnabled() {
			return enabled;
		}
		
		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((key == null) ? 0 : key.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			SelectionValue other = (SelectionValue) obj;
			if (key == null) {
				if (other.key != null)
					return false;
			} else if (!key.equals(other.key))
				return false;
			return true;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("[");
			builder.append(key);
			builder.append(" : ");
			builder.append(value);
			
			if (StringHelper.containsNonWhitespace(icon)) {
				builder.append(" : ").append(icon);
			}
			
			if (StringHelper.containsNonWhitespace(description)) {
				builder.append(" : ").append(description);
			}
			
			builder.append("]");
			return builder.toString();
		}
	}

}
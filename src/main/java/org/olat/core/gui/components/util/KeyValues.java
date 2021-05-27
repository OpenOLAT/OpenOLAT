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

/**
 * 
 * Initial date: 24.09.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class KeyValues {
	
	private static final Comparator<String> nullSafeStringComparator = Comparator.nullsLast(String::compareToIgnoreCase);
	public static final Comparator<KeyValue> VALUE_ASC = Comparator.comparing(KeyValue::getValue, nullSafeStringComparator);
	
	private List<KeyValue> keyValues = new ArrayList<>();
	
	public KeyValues(KeyValue... keyValues) {
		if (keyValues != null && keyValues.length != 0) {
			for (KeyValue keyValue : keyValues) {
				add(keyValue);
			}
		}
	}
	
	public static KeyValue entry(String key, String value) {
		return new KeyValue(key, value);
	}
	
	/**
	 * Adds the key / value pair at the end of the pairs. Since the key has to be
	 * unique, an existing pair with the same key is removed.
	 *
	 * @param keyValue
	 */
	public void add(KeyValue keyValue) {
		remove(keyValue.getKey());
		keyValues.add(keyValue);
	}
	
	/**
	 * Adds an array of key / value pairs
	 * 
	 * @param keyValues
	 */
	public void add(KeyValue... keyValues) {
		if (keyValues == null || keyValues.length == 0) {
			return;
		}
		
		for (KeyValue keyValue : keyValues) {
			add(keyValue);
		}
	}

	public void addAll(KeyValues additionalKeyValues) {
		if (additionalKeyValues == null) return;
		
		for (KeyValue additionalKeyValue : additionalKeyValues.keyValues) {
			add(additionalKeyValue);
		}
	}
	
	/**
	 * If a key / value pair with the key exists, the existing pair is replaced. If
	 * no pair exists, the new pair is put at the end.
	 * 
	 * @param keyValue
	 */
	public void replaceOrPut(KeyValue keyValue) {
		if (containsKey(keyValue.getKey())) {
			replace(keyValue);
		} else {
			keyValues.add(keyValue);
		}
	}
	
	private void replace(KeyValue keyValue) {
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
	
	/**
	 * Returns a array of all keys. The method creates a new array every time it is invoked.
	 *
	 * @return
	 */
	public String[] keys() {
		return keyValues.stream().map(KeyValue::getKey).toArray(String[]::new);
	}
	
	/**
	 * Returns a array of all values. The method creates a new array every time it is invoked.
	 *
	 * @return
	 */
	public String[] values() {
		return keyValues.stream().map(KeyValue::getValue).toArray(String[]::new);
	}
	
	public void sort(Comparator<KeyValue> comparator) {
		keyValues.sort(comparator);
	}
	
	public boolean containsKey(String key) {
		return keyValues.stream().map(KeyValue::getKey).anyMatch(k -> k.equals(key));
	}
	
	public int size() {
		return keyValues.size();
	}
	
	public boolean isEmpty() {
		return keyValues.isEmpty();
	}
	
	public List<KeyValue> keyValues() {
		return new ArrayList<>(keyValues);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (KeyValue keyValue : keyValues) {
			builder.append(keyValue.toString()).append(" ");
		}
		return builder.toString();
	}

	public final static class KeyValue {
		
		private final String key;
		private final String value;
		
		public KeyValue(String key, String value) {
			this.key = key;
			this.value = value;
		}

		public String getKey() {
			return key;
		}

		public String getValue() {
			return value;
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
			KeyValue other = (KeyValue) obj;
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
			builder.append("]");
			return builder.toString();
		}
	}

}
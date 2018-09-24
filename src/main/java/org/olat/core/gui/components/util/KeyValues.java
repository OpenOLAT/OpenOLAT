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
import java.util.Objects;

/**
 * 
 * Initial date: 24.09.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class KeyValues {
	
	private static Comparator<String> nullSafeStringComparator = Comparator.nullsLast(String::compareToIgnoreCase);
	public static Comparator<KeyValue> VALUE_ASC = Comparator.comparing(KeyValue::getValue, nullSafeStringComparator);
	
	private List<KeyValue> keyValues = new ArrayList<>();
	
	public void add(String key, String value) {
		add(new KeyValue(key, value));
	}
	
	public void add(KeyValue keyValue) {
		keyValues.add(keyValue);
	}
	
	public String[] keys() {
		return keyValues.stream().map(KeyValue::getKey).toArray(String[]::new);
	}
	
	public String[] values() {
		return keyValues.stream().map(KeyValue::getValue).toArray(String[]::new);
	}
	
	public void sort(Comparator<KeyValue> comparator) {
		keyValues.sort(comparator);
	}
	
	public boolean containsKey(String key) {
		return keyValues.stream().map(KeyValue::getKey).anyMatch(k -> Objects.equals(k, key));
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
	}

}

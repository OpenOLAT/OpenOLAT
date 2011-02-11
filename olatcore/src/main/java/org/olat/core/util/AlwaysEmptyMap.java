/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) frentix GmbH<br>
* http://www.frentix.com<br>
* <p>
*/
package org.olat.core.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * <h3>Description:</h3>
 * This map implementation does nothing. It is always empty and can be used to
 * transparently enable a non-caching mode.
 * 
 * <P>
 * Initial Date: 23.10.2008 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
public class AlwaysEmptyMap<K, V> implements Map<K, V> {

	public void clear() {
		// nothing to do
	}

	public boolean containsKey(@SuppressWarnings("unused") Object key) {
		return false;
	}

	public boolean containsValue(@SuppressWarnings("unused") Object value) {
		return false;
	}

	public Set<java.util.Map.Entry<K, V>> entrySet() {
		return new HashSet<java.util.Map.Entry<K, V>>();
	}

	public V get(@SuppressWarnings("unused") Object key) {
		return null;
	}

	public boolean isEmpty() {
		return true;
	}

	public Set<K> keySet() {
		return new HashSet<K>();
	}

	public V put(@SuppressWarnings("unused") K key, @SuppressWarnings("unused") V value) {
		return null;
	}

	public void putAll(@SuppressWarnings("unused") Map<? extends K, ? extends V> t) {
		// nothing to do
	}

	public V remove(@SuppressWarnings("unused") Object key) {
		return null;
	}

	public int size() {
		return 0;
	}

	public Collection<V> values() {
		return new HashSet<V>();
	}

}

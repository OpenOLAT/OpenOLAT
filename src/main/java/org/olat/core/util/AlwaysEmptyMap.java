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
package org.olat.core.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

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
public class AlwaysEmptyMap<K, V> implements ConcurrentMap<K, V> {

	@Override
	public void clear() {
		// nothing to do
	}

	@Override
	public boolean containsKey(Object key) {
		return false;
	}

	@Override
	public boolean containsValue(Object value) {
		return false;
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		return new HashSet<>();
	}

	@Override
	public V get(Object key) {
		return null;
	}

	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public Set<K> keySet() {
		return new HashSet<>();
	}

	@Override
	public V put(K key, V value) {
		return null;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> t) {
		// nothing to do
	}

	@Override
	public V remove(Object key) {
		return null;
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public Collection<V> values() {
		return new HashSet<>();
	}

	@Override
	public V putIfAbsent(K key, V value) {
		return null;
	}

	@Override
	public boolean remove(Object key, Object value) {
		return false;
	}

	@Override
	public boolean replace(K key, V oldValue, V newValue) {
		return false;
	}

	@Override
	public V replace(K key, V value) {
		return null;
	}
}

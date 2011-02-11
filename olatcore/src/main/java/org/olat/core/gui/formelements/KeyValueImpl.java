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
* Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.core.gui.formelements;

/**
 * @author Felix Jost
 */
public class KeyValueImpl implements KeyValue {
	private String key, value;

	/**
	 * Constructor for KeyValue.
	 */
	public KeyValueImpl() {
		super();
	}

	/**
	 * Constructor for KeyValueImpl.
	 * 
	 * @param key
	 * @param value
	 */
	public KeyValueImpl(String key, String value) {
		if (key == null || value == null) throw new RuntimeException("key or value null in KeyValue:key " + key + ", val" + value);
		this.key = key;
		this.value = value;
	}

	/**
	 * @see org.olat.core.gui.formelements.KeyValue#getKey()
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @see org.olat.core.gui.formelements.KeyValue#getValue()
	 */
	public String getValue() {
		return value;
	}

}


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
package org.olat.modules.quality.analysis;

/**
 * 
 * Initial date: 24.09.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class MultiKey {
	
	private final static MultiKey NONE = new MultiKey(null, null, null);
	
	private final String key1;
	private final String key2;
	private final String key3;
	
	public static final MultiKey none() {
		return NONE;
	}
	
	public static final MultiKey of(String key1) {
		return of(key1, null);
	}
	
	public static final MultiKey of(String key1, String key2) {
		return of(key1, key2, null);
	}
	
	public static final MultiKey of(String key1, String key2, String key3) {
		return new MultiKey(key1, key2, key3);
	}
	
	private MultiKey(String key1, String key2, String key3) {
		this.key1 = key1;
		this.key2 = key2;
		this.key3 = key3;
	}

	public String getKey1() {
		return key1;
	}

	public String getKey2() {
		return key2;
	}

	public String getKey3() {
		return key3;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key1 == null) ? 0 : key1.hashCode());
		result = prime * result + ((key2 == null) ? 0 : key2.hashCode());
		result = prime * result + ((key3 == null) ? 0 : key3.hashCode());
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
		MultiKey other = (MultiKey) obj;
		if (key1 == null) {
			if (other.key1 != null)
				return false;
		} else if (!key1.equals(other.key1))
			return false;
		if (key2 == null) {
			if (other.key2 != null)
				return false;
		} else if (!key2.equals(other.key2))
			return false;
		if (key3 == null) {
			if (other.key3 != null)
				return false;
		} else if (!key3.equals(other.key3))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MultiKey [Key1=");
		builder.append(key1);
		builder.append(", Key2=");
		builder.append(key2);
		builder.append(", Key3=");
		builder.append(key3);
		builder.append("]");
		return builder.toString();
	}

}

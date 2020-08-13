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
package org.olat.modules.opencast;

/**
 * 
 * Initial date: 13 Aug 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AuthDelegate {
	
	public enum Type {
		None, User, Roles;
		
		static public boolean isValid(String name) {
			for (Type type : Type.values())
				if (type.name().equals(name)) {
					return true;
				}
			return false;
		}
	};
	
	private final Type type;
	private final String value;
	
	public static AuthDelegate of(Type type, String value) {
		return new AuthDelegate(type, value);
	}
	
	private AuthDelegate(Type type, String value) {
		super();
		this.type = type;
		this.value = value;
	}

	public Type getType() {
		return type;
	}

	public String getValue() {
		return value;
	}

}

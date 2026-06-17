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
package org.olat.modules.selectus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 18 nov. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationFieldType {
	
	private final boolean enabled;
	private final boolean optional;
	private final Type type;
	private final List<String> dependencies;
	
	public ApplicationFieldType(boolean enabled, boolean optional, Type type) {
		this.enabled = enabled;
		this.optional = optional;
		this.type = type;
		dependencies = Collections.emptyList();
	}
	
	public ApplicationFieldType(boolean enabled, boolean optional, Type type, List<String> dependencies) {
		this.enabled = enabled;
		this.optional = optional;
		this.type = type;
		this.dependencies = (dependencies == null || dependencies.isEmpty()) ? Collections.emptyList() : new ArrayList<>(dependencies);
	}
	
	public static final ApplicationFieldType valueOf(String enable, String type) {
		boolean optional = "optional".equals(enable);
		boolean enabled = "enabled".equals(enable) || "optional".equals(enable);
		
		Type fieldType = Type.text;
		for(Type t:Type.values()) {
			if(type.startsWith(t.name())) {
				fieldType = t;
			}
		}
		
		int startDeps = type.indexOf('(');
		int endDeps = type.indexOf(')');
		if(startDeps >= 0 && endDeps >= 0) {
			String deps = type.substring(startDeps + 1, endDeps);
			String[] depArray = deps.split("[,]");
			List<String> depList = Arrays.asList(depArray);
			return new ApplicationFieldType(enabled, optional, fieldType, depList);
		}
		return new ApplicationFieldType(enabled, optional, fieldType);
	}
	
	public boolean isEnabled() {
		return enabled;
	}

	public boolean isOptional() {
		return optional;
	}

	public Type getType() {
		return type;
	}
	
	public List<String> getDependencies() {
		return dependencies;
	}
	
	public Class<?> toClass() {
		if(type == null) {
			return String.class;
		}
		switch(type) {
			case text: return String.class;
			case integer: return Integer.class;
			case number: return Double.class;
			case sum: return Integer.class;
			default: return String.class;
		}
	}
	
	public Object toTypedValue(String value) {
		if(type != null) {
			if(type == ApplicationFieldType.Type.integer || type == ApplicationFieldType.Type.sum) {
				if(StringHelper.containsNonWhitespace(value) && StringHelper.isLong(value)) {
					try {
						return Integer.valueOf(value);
					} catch (NumberFormatException e) {
						//not message here
					}
				}
			} else if(type == ApplicationFieldType.Type.number) {
				if(StringHelper.containsNonWhitespace(value)) {
					try {
						return Double.valueOf(value);
					} catch (NumberFormatException e) {
						//not message here
					}
				}
			}
		}
		return value;
	}
 
	public enum Type {
		text,
		integer,
		number,
		sum
		
		
		
		
	}
}

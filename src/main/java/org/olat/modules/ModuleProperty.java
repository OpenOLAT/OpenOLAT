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
package org.olat.modules;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

/**
 * A simple implementation of a strongly typed named property
 * 
 * <p>Initial date: May 6, 2016
 * @author lmihalkovic, http://www.frentix.com
 */
public abstract class ModuleProperty<T> {

	/**
	 * A strongly typed property value
	 * 
	 * Initial date: May 6, 2016<br>
	 * @author lmihalkovic, http://www.frentix.com
	 *
	 */
	public static final class ModulePropertyValue<X> {
		private X value;
		private final ModuleProperty<X> def;
		protected ModulePropertyValue(X value, ModuleProperty<X> def) {
			this.value = value;
			this.def = def;
		}
		public X val() {
			if(!isSet()) return getDefault();
			return value;
		}
		public void val(X val) {
			this.value = val;
		}
		public X getDefault() {
			return def.getDefault();
		}
		public boolean isSet() {
			return this.value != null;
		}
		public String name() {
			return def.name;
		}
	}
	
	private final String name;
	private final T defaultValue;
	private final Type type;
	
	public ModuleProperty(String name) {
		this(name, null);
	}
	
	public ModuleProperty(String name, T defaultValue) {
		this.name = name;
		this.defaultValue = defaultValue;
		this.type = getType();
	}
	
	public ModulePropertyValue<T> val(T value) {
		return new ModulePropertyValue<T>(value, this);
	}
	
	public String name() {
		return this.name;
	}
	
	public boolean hasDefault() {
		return defaultValue != null;
	}
	
	T getDefault() {
		return this.defaultValue;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[")
			.append(name())
			.append(":")
			.append(type);
		if (defaultValue!= null) {
			sb.append(" {").append(defaultValue).append("}");					
		}
		sb.append("]");
		return sb.toString();
	}

	// ------------------------------
	// Internal
	
	private Type getType() {
        Type superclass = getClass().getGenericSuperclass();
        if (superclass instanceof Class) {
            throw new RuntimeException("Missing type parameter.");
        }
        return ((ParameterizedType) superclass).getActualTypeArguments()[0];		
	}
	
	@SuppressWarnings("unchecked")
	Class<T> rawType() {
		// this is ok or leads to a CCE later if the types do not match
		return (Class<T>) getRawType(type);
	}
	
	private static Class<?> getRawType(Type type) {
		if (type instanceof Class<?>) {
			// type is a class
			return (Class<?>) type;
		} else if (type instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) type;
			Type rawType = parameterizedType.getRawType();
			checkArgument(rawType instanceof Class);
			return (Class<?>) rawType;
		} else if (type instanceof GenericArrayType) {
			Type componentType = ((GenericArrayType) type).getGenericComponentType();
			return Array.newInstance(getRawType(componentType), 0).getClass();
		} else if (type instanceof TypeVariable) {
			return Object.class;
		} else if (type instanceof WildcardType) {
			return getRawType(((WildcardType) type).getUpperBounds()[0]);
		} else {
			String className = type == null ? "null" : type.getClass().getName();
			throw new IllegalArgumentException("Expected a Class, ParameterizedType, or " + "GenericArrayType, but <"
					+ type + "> is of type " + className);
		}
	}

	private static void checkArgument(boolean condition) {
		if (!condition) {
			throw new IllegalArgumentException();
		}
	}
}

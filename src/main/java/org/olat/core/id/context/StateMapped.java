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
package org.olat.core.id.context;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * <h3>Description:</h3>
 * <p>A simple implementation of StateEntry, 
 * <p>
 * Initial Date:  18 jan. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 */
public class StateMapped implements StateEntry{

	private static final long serialVersionUID = -164313132644246934L;
	private Map<String,String> delegate = new HashMap<>();
	
	public StateMapped() {
		//make XStream happy
	}
	
	public StateMapped(String key, String value) {
		delegate.put(key, value);
	}

	public Map<String, String> getDelegate() {
		return delegate;
	}

	public void setDelegate(Map<String, String> delegate) {
		this.delegate = delegate;
	}
	
	@Override
	public int hashCode() {
		return delegate == null ? 721567 : delegate.hashCode();
	}
	
	@Override
	public String toString() {
		return delegate == null ? "{empty}" : delegate.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof StateMapped) {
			StateMapped map = (StateMapped)obj;
			if((map.delegate == null || map.delegate.isEmpty()) && (delegate == null || delegate.isEmpty())) {
				return true;
			} else if(map.delegate != null && delegate != null && map.delegate.equals(delegate)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public StateEntry clone() {
		StateMapped state = new StateMapped();
		if(delegate != null) {
			state.delegate.putAll(delegate);
		}
		return state;
	}
}

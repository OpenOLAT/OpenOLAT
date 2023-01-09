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
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.core.commons.persistence;

import static org.hibernate.cfg.AvailableSettings.QUERY_PLAN_CACHE_MAX_SIZE;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Description:<br>
 * Helper class created to concatenate DB specific connection properties (<code>java.util.Properties</code>) with the DB common
 * hibernate properties within the Spring config.
 * 
 * <P>
 * Initial Date:  07.09.2010 <br>
 * @author patrickb
 */
public class DBVendorHibernatePropertiesSimplification extends Properties {

	private static final long serialVersionUID = 2191563335029326588L;

	/**
	 * First added key value pairs, which are eventually overwritten by values added during a {@link #setMoreProperties(Map)} call.
	 * @param firstKeyValues
	 */
	public DBVendorHibernatePropertiesSimplification(Properties firstKeyValues) {
		super();
		optimizeForEnvironment(firstKeyValues);
		putAll(firstKeyValues);
	}
	
	/**
	 * add more key value pairs
	 * @param more
	 */
	public void setAddMoreProperties(Properties more) {
		optimizeForEnvironment(more);
		putAll(more);
	}
	
	private void optimizeForEnvironment(Properties more) {
		List<Object> keys = new ArrayList<>(more.keySet());
		for(Object key:keys) {
			optimizeForEnvironment(key, more);
		}
	}
	
	private void optimizeForEnvironment(Object key, Properties properties) {
		if(QUERY_PLAN_CACHE_MAX_SIZE.equals(key)) {
			MemorySize mem = getMaxMemoryAvailable();
			if(mem == MemorySize.small) {
				properties.put(QUERY_PLAN_CACHE_MAX_SIZE, "512");
			} else if(mem == MemorySize.medium) {
				properties.put(QUERY_PLAN_CACHE_MAX_SIZE, "1024");
			}
		}
	}
	
	private MemorySize getMaxMemoryAvailable() {
		long maxMem = Runtime.getRuntime().maxMemory();
		if(maxMem < 550000000l) {
			return MemorySize.small;
		} else if(maxMem < 1048000000l) {
			return MemorySize.medium;
		}
		return MemorySize.large;
	}
	
	private enum MemorySize {
		small,
		medium,
		large

	}
}

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
* <p>
*/ 

package org.olat.core.util.vfs.filters;

import java.util.HashMap;
import java.util.Map;

import org.olat.core.util.vfs.VFSItem;

/**
 * <h3>Description:</h3>
 * The VFSItemExcludePrefixFilter filters VFSItems that start with the given prefixes.
 * <p>
 * Note that this is not restricted to VFSLeaves, it does also filter
 * VFSContainers!
 * 
 */
public class VFSItemExcludePrefixFilter implements VFSItemFilter {

	private final boolean excludeSystemFiles;
	private final Map<String,String> excludedPrefixes = new HashMap<>();
	
	/**
	 * Constrtuctor
	 * @param prefixes 
	 */
	public VFSItemExcludePrefixFilter(String[] prefixes, boolean excludeSystemFiles) {
		this.excludeSystemFiles = excludeSystemFiles;
		for (int i = 0; i < prefixes.length; i++) {
			addExcludedPrefix(prefixes[i]);
		}
	}
	
	/**
	 * @param prefix
	 */
	public void addExcludedPrefix(String prefix) {
		prefix = prefix.toLowerCase();
		excludedPrefixes.put(prefix, prefix);
	}

	/**
	 * @param prefix
	 */
	public void removeSuffix(String prefix) {
		excludedPrefixes.remove(prefix.toLowerCase());
	}

	@Override
	public boolean accept(VFSItem vfsItem) {
		String name = vfsItem.getName().toLowerCase();
		if(excludeSystemFiles && name.startsWith(".")) {
			return false;
		}
		
		for(String excludedPrefix : excludedPrefixes.keySet()) {
			if (name.startsWith(excludedPrefix)) {
				return false;
			}
		}
		return true;
	}
}

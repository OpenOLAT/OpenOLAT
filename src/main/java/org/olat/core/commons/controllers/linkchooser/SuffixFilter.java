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

package org.olat.core.commons.controllers.linkchooser;

import java.io.File;
import java.io.FileFilter;
import java.util.Hashtable;
import java.util.Map;

/**
 * Description:<br>
 * Initial Date:  Aug 25, 2004 <br>
 *
 * @author pellmont <p>
 */
public class SuffixFilter implements FileFilter {

	private Map<String,String> suffixes = new Hashtable<>();

	public SuffixFilter() {
	//
	}

	/**
	 * @param suffixes
	 */
	public SuffixFilter(String[] suffixes) {
		for (int i = 0; i < suffixes.length; i++) {
			addSuffix(suffixes[i]);
		}
	}

	/**
	 * @param suffix
	 */
	public void addSuffix(String suffix) {
		suffix = suffix.toLowerCase();
		suffixes.put(suffix, suffix);
	}

	/**
	 * @param suffix
	 */
	public void removeSuffix(String suffix) {
		suffixes.remove(suffix.toLowerCase());
	}

	/**
	 * @see java.io.FileFilter#accept(java.io.File)
	 */
	public boolean accept(File file) {
		String name = file.getName().toLowerCase();
		if (file.isDirectory()) return false;
		int idx = name.lastIndexOf('.');
		if (idx >= 0) {
			return suffixes.containsKey(name.substring(idx + 1));
		}
		return suffixes.containsKey(name);
	}
}
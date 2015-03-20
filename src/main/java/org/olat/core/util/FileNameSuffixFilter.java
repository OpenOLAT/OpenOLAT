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

import java.io.File;
import java.io.FilenameFilter;

/**
 * 
 * Initial date: 10.03.2015<br>
 * @author gnaegi@frentix.com, http://www.frentix.com
 *
 */
public class FileNameSuffixFilter implements FilenameFilter {
	String suffix;
	boolean caseSensitive;
	
	/**
	 * Creates a case sensitive file name suffix filter for the given suffix
	 * @param suffix
	 */
	public FileNameSuffixFilter(String suffix) {
		this(suffix, true);
	}
	
	/**
	 * Creates a file name suffix filter
	 * 
	 * @param suffix
	 * @param caseSensitive
	 *            true: filename and filter is case sensitive; false: everything
	 *            checked to lowercase
	 */
	public FileNameSuffixFilter(String suffix, boolean caseSensitive) {
		if (suffix == null) {
			suffix = "";
		}
		this.suffix = (caseSensitive ? suffix : suffix.toLowerCase());
		this.caseSensitive = caseSensitive;
	}

	@Override
	public boolean accept(File dir, String name) {
		if (!caseSensitive) {
			name = name.toLowerCase();
		}
		if (name.endsWith(suffix)) {
			return true;
		} else {
			return false;
		}
	}

}

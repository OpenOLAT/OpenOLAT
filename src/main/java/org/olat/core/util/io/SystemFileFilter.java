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
package org.olat.core.util.io;

import java.io.File;
import java.io.FileFilter;

/**
 * 
 * Initial date: 06.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SystemFileFilter implements FileFilter {
	
	public static final SystemFileFilter FILES_ONLY = new SystemFileFilter(true, false);
	public static final SystemFileFilter DIRECTORY_ONLY = new SystemFileFilter(false, true);
	public static final SystemFileFilter DIRECTORY_FILES = new SystemFileFilter(true, true);
	
	private final boolean acceptFile;
	private final boolean acceptDirectory;
	
	public SystemFileFilter(boolean acceptFile, boolean acceptDirectory) {
		this.acceptFile = acceptFile;
		this.acceptDirectory = acceptDirectory;
	}

	@Override
	public boolean accept(File pathname) {
		boolean accept;
		if(pathname.isHidden()) {
			accept = false;
		} else if(acceptFile && pathname.isFile()) {
			accept = true;
		} else if(acceptDirectory && pathname.isDirectory()) {
			accept = true;
		} else {
			accept = false;
		}
		return accept;
	}
}
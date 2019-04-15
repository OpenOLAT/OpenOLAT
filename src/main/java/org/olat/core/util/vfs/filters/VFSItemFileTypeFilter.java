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

package org.olat.core.util.vfs.filters;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;

/**
 * <h3>Description:</h3>
 * This filter shows only VFSItems with the given file type
 * <p>
 * It does not affect VFSContainers.
 * 
 */
public class VFSItemFileTypeFilter extends VFSItemCompositeFilter {
	
	private final boolean uriValidation;
	private final boolean excludeSystemFiles;
	private Map<String, String> fileTypes = new HashMap<>();

	/**
	 * Constrtuctor
	 * 
	 * @param filetypes
	 */
	public VFSItemFileTypeFilter(String[] fileTypes, boolean excludeSystemFiles, boolean uriValidation) {
		this.uriValidation = uriValidation;
		this.excludeSystemFiles = excludeSystemFiles;
		for (int i = 0; i < fileTypes.length; i++) {
			addFileType(fileTypes[i]);
		}
	}

	/**
	 * @param fileType
	 */
	public void addFileType(String fileType) {
		fileType = fileType.toLowerCase();
		fileTypes.put(fileType, fileType);
	}

	/**
	 * @param fileType
	 */
	public void removeFileType(String fileType) {
		fileTypes.remove(fileType.toLowerCase());
	}

	@Override
	public boolean acceptFilter(VFSItem vfsItem) {
		if (vfsItem instanceof VFSContainer) {
			return true;			
		}
		
		String name = vfsItem.getName();
		if(uriValidation) {
			try {
				new URI(name).getPath();
			} catch(Exception e) {
				return false;
			}
		}
		
		if(excludeSystemFiles && name.startsWith(".")) {
			return false;
		}
		
		name = name.toLowerCase();
		int dotPos = name.lastIndexOf('.');
		if (dotPos == -1) return false;
		return fileTypes.containsKey(name.substring(dotPos + 1));
	}
}

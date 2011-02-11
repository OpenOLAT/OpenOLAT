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
 * Copyright (c) 2008 frentix GmbH, Switzerland<br>
 * http://www.frentix.com,
 * <p>
 */

package org.olat.core.util.vfs.filters;

import java.util.Hashtable;

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
	private Hashtable<String, String> fileTypes = new Hashtable<String, String>();

	/**
	 * Constrtuctor
	 * 
	 * @param filetypes
	 */
	public VFSItemFileTypeFilter(String[] fileTypes) {
		for (int i = 0; i < fileTypes.length; i++) {
			addFileType(fileTypes[i]);
		}
	}

	/**
	 * @param fileType
	 */
	public void addFileType(String fileType) {
		fileType = fileType.toLowerCase();
		this.fileTypes.put(fileType, fileType);
	}

	/**
	 * @param fileType
	 */
	public void removeFileType(String fileType) {
		this.fileTypes.remove(fileType.toLowerCase());
	}

	/**
	 * @see org.olat.core.util.vfs.filters.VFSItemCompositeFilter#acceptFilter(VFSItem)
	 */
	public boolean acceptFilter(VFSItem vfsItem) {
		if (vfsItem instanceof VFSContainer) {
			return true;			
		}		
		String name = vfsItem.getName().toLowerCase();
		int dotPos = name.lastIndexOf(".");
		if (dotPos == -1) return false;
		return fileTypes.containsKey(name.substring(dotPos + 1));
	}
}

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
* Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.core.commons.modules.bc;

import org.olat.core.gui.control.Event;

/**
 * This event signalizes a change in a folder
 * Initial Date:  Nov 16, 2004
 *
 * @author gnaegi 
 */
public class FolderEvent extends Event {

	private String filename;
	
	public static final String UPLOAD_EVENT = "upload";
	public static final String NEW_FOLDER_EVENT = "new.folder";
	public static final String NEW_FILE_EVENT = "new.file";
	public static final String EDIT_EVENT = "edit";
	public static final String ZIP_EVENT = "zip";
	public static final String UNZIP_EVENT = "unzip";
	public static final String MOVE_EVENT = "move";
	public static final String COPY_EVENT = "copy";
	public static final String DELETE_EVENT = "delete";	
	
	/**
	 * @param command The command that has been issued
	 * @param filename The filename to which the command has been applied
	 */
	public FolderEvent(String command, String filename) {
		super(command);
		this.filename = filename;
	}

	/**
	 * @return String representing the file name to which the command applied
	 */
	public String getFilename() {
		return filename;
	}
}

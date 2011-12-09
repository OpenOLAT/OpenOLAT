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

package org.olat.admin.sysinfo;

import org.olat.core.util.event.MultiUserEvent;

/**
 * @author Christian Guretzki
 */
public class FileSystemTestEvent extends MultiUserEvent {
	
	protected static final String COMMAND_FILE_WRITTEN = "cmd_file_written";
	protected static final String COMMAND_FILE_CHECKED = "cmd_file_checked";
	
	private int loop;
	private int maxNbrDirs;
	private int maxNbrFiles ;
	private Integer nodeId;
	private boolean fileCheckOk;
	private long duration;
	private boolean isCheckWithRetriesEnabled; 
	

	public FileSystemTestEvent(String command, Integer nodeId, int loop, boolean fileCheckOk, long duration) {
		super(command);		
		this.nodeId = nodeId;
		this.loop = loop;
		this.fileCheckOk = fileCheckOk;
		this.duration = duration;
	}
	
	
	public FileSystemTestEvent(String command, int loop, int maxNbrDirs, int maxNbrFiles, long duration, boolean isCheckWithRetriesEnabled) {
		super(command);		
		this.loop = loop;
		this.maxNbrDirs = maxNbrDirs;
		this.maxNbrFiles = maxNbrFiles;
		this.duration = duration;
		this.isCheckWithRetriesEnabled = isCheckWithRetriesEnabled;
	}

	public int getLoop() {
		return loop;
	}

	public int getMaxNbrDirs() {
		return maxNbrDirs;
	}

	public int getMaxNbrFiles() {
		return maxNbrFiles;
	}

	public Integer getNodeId() {
		return nodeId;
	}

	public boolean isFileCheckOk() {
		return fileCheckOk;
	}


	public long getDuration() {
		return duration;
	}


	public boolean isCheckWithRetriesEnabled() {
		return isCheckWithRetriesEnabled;
	}

	
	

	
}

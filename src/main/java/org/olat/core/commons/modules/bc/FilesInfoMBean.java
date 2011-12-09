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
package org.olat.core.commons.modules.bc;


/**
 * JMX MBean class to collect infos about file access (upload:client to server , 
 * download : server to client).
 * @author Christian Guretzki
 */
public class FilesInfoMBean {
	private long numberOfUploadedFiles = 0;
	private long sizeOfUploadedFiles   = 0;
	private long numberOfDownloadedFiles = 0;
	private long sizeOfDownloadedFiles   = 0;

  
	/**
	 * Call this method when some file was upload into olat.
	 * @param size  Uploaded-file-size
	 */
	public synchronized void logUpload(long size) {
		numberOfUploadedFiles++;
		sizeOfUploadedFiles += size;
	}
	
	/**
	 * Call this method when some file was downloaded from olat.
	 * @param size  Downloaded-file-size
	 */
	public synchronized void logDownload(long size) {
		numberOfDownloadedFiles++;
		sizeOfDownloadedFiles += size;
	}

	// JMX MBean Methods
	////////////////////
	/**
	 * @return Number of uploaded file since system start.
	 */
	public long getNumberOfUploadedFiles() {
		return numberOfUploadedFiles;
	}

	/**
	 * @return Overall size of uploaded files since system start.
	 */
	public long getSizeOfUploadedFiles() {
		return sizeOfUploadedFiles;
	}

	/**
	 * @return Number of downloaded file since system start.
	 */
	public long getNumberOfDownloadedFiles() {
		return numberOfDownloadedFiles;
	}

	/**
	 * @return Overall size of downloaded file since system start.
	 */
	public long getSizeOfDownloadedFiles() {
		return sizeOfDownloadedFiles;
	}

}


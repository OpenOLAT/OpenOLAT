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

import java.util.Date;

import org.olat.core.commons.services.vfs.VFSMetadata;

/**
 * Initial Date:  11.02.2005 <br>
 *
 * @author Felix Jost
 */
public class FileInfo {
	private String relPath;
	private VFSMetadata metaInfo;
	private Date lastModified;
	/**
	 * @param relPath e.g. chapter1/info.pdf -> will be used as title in notifications
	 * @param metaInfo
	 * @param lastModified
	 */
	public FileInfo(String relPath, VFSMetadata metaInfo, Date lastModified) {
		this.relPath = relPath;
		this.metaInfo = metaInfo;
		this.lastModified = lastModified;
	}
	
	public Long getModifiedByIdentityKey() {
		return metaInfo == null || metaInfo.getFileLastModifiedBy() == null ? null : metaInfo.getFileLastModifiedBy().getKey();
	}

	/**
	 * @return the date of the modification
	 */
	public Date getLastModified() {
		return lastModified;
	}

	/**
	 * @return the relative path of the file (e.g. /images/sky.jpg)
	 */
	public String getRelPath() {
		return relPath;
	}
	
	/**
	 * Get the file meta info or NULL if no meta info exists
	 * @return
	 */
	public VFSMetadata getMetaInfo() {
		return metaInfo;
	}
}


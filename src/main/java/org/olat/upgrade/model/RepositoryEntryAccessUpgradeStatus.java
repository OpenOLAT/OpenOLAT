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
package org.olat.upgrade.model;
/**
 * 
 * Description:<br>
 * This class is used to handle and interpret the status of repository entries.This
 * is a backup of the original one used to upgrade the repository entries.
 * 
 * <P>
 * Initial Date:  09.07.2009 <br>
 * @author bja <bja@bps-system.de>
 */
public class RepositoryEntryAccessUpgradeStatus {

	public static final int REPOSITORY_STATUS_OPEN = 0;
	/**
	 * Correspond to the "freeze" status
	 */
	public static final int REPOSITORY_STATUS_CLOSED = 2;
	public static final int REPOSITORY_STATUS_UNPUBLISHED = 4;
	
	private boolean closed;
	private boolean unpublished;
	
	public RepositoryEntryAccessUpgradeStatus(int statusCode) {
		// initialize closed status
		if((statusCode & REPOSITORY_STATUS_CLOSED) == REPOSITORY_STATUS_CLOSED ) {
			setClosed(true);
		} else {
			setClosed(false);
		}
		
		if((statusCode & REPOSITORY_STATUS_UNPUBLISHED) == REPOSITORY_STATUS_UNPUBLISHED ) {
			setUnpublished(true);
		} else {
			setUnpublished(false);
		}
	}

	/**
	 * @param closed
	 */
	private void setClosed(boolean closed) {
		this.closed = closed;
	}

	public boolean isClosed() {
		return closed;
	}
	
	private void setUnpublished(boolean unpublished) {
		this.unpublished = unpublished;
	}

	public boolean isUnpublished() {
		return unpublished;
	}
}

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

package org.olat.repository.controllers;

import org.olat.core.util.event.MultiUserEvent;
import org.olat.repository.RepositoryEntry;


/**
 * Initial Date:  May 27, 2004
 * @author Mike Stock
 */
public class EntryChangedEvent extends MultiUserEvent {

	private int change;
	private Long changedEntryKey;
	
	/**
	 * Entry modified status.
	 */
	public static final int MODIFIED = 0;
	/**
	 * Entry added status.
	 */
	public static final int ADDED = 1;
	/**
	 * Entry deleted status.
	 */
	public static final int DELETED = 2;
	
	/**
	 * Entry description modified status.
	 */
	public static final int MODIFIED_DESCRIPTION = 3;
	
	public static final int MODIFIED_AT_PUBLISH = 4;
	
	/**
	 * Event signaling the change of a repository entry. Use getChange to see the status of the change.
	 * 
	 * @param changedEntry
	 * @param change
	 */
	public EntryChangedEvent(RepositoryEntry changedEntry, int change) {
		super("");
		this.changedEntryKey = changedEntry.getKey();
		this.change = change;
	}
	
	/**
	 * @return the key of the repository entry that has been changed.
	 */
	public Long getChangedEntryKey() { return changedEntryKey; }
	
	/**
	 * Get the type of change.
	 * @return type of change.
	 */
	public int getChange() { return change; }
	
}

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

import org.olat.basesecurity.IdentityRef;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;


/**
 * Initial Date:  May 27, 2004
 * @author Mike Stock
 */
public class EntryChangedEvent extends MultiUserEvent {

	private static final long serialVersionUID = 8339474599787388699L;
	
	public static final String CHANGE_CMD = "repo-entry-changed";
	
	private final Change change;
	private final Long entryKey;
	private final Long identityKey;
	private final String source;
	
	/**
	 * Event signaling the change of a repository entry. Use getChange to see the status of the change.
	 * 
	 * @param changedEntry
	 * @param change
	 */
	public EntryChangedEvent(RepositoryEntryRef entry, IdentityRef identity, Change change, String source) {
		super(CHANGE_CMD);
		this.source = source;
		this.change = change;
		entryKey = entry.getKey();
		identityKey = identity == null ? null : identity.getKey();
	}
	
	/**
	 * @return the key of the repository entry that has been changed.
	 */
	public Long getRepositoryEntryKey() {
		return entryKey;
	}
	
	/**
	 * The author of the change
	 * @return
	 */
	public Long getIdentityKey() {
		return identityKey;
	}
	
	public boolean isMe(IdentityRef identity) {
		return identityKey != null && identity != null && identityKey.equals(identity.getKey());
	}
	
	public boolean isMe(RepositoryEntry entry) {
		return entryKey != null && entry != null && entryKey.equals(entry.getKey());
	}
	
	public String getSource() {
		return source;
	}

	/**
	 * Get the type of change.
	 * @return type of change.
	 */
	public Change getChange() {
		return change;
	}
	
	public static enum Change {
		added,
		deleted,
		modifiedAccess,
		modifiedDescription,
		modifiedAtPublish,
		addBookmark,
		removeBookmark,
		closed,
		unclosed,
		restored
	}
}

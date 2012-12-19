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
package org.olat.instantMessaging.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.instantMessaging.model.Buddy;

/**
 * 
 * Initial date: 05.12.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class Roster {
	
	private final Long identityKey;
	private final List<Buddy> entries;
	
	public Roster(Long identityKey) {
		this.identityKey = identityKey;
		entries = new ArrayList<Buddy>();
	}
	
	public Roster(List<Buddy> entries, Long identityKey) {
		this.identityKey = identityKey;
		this.entries = entries;
	}
	
	public synchronized boolean contains(Long identityKey) {
		for(Buddy entry:entries) {
			if(identityKey.equals(entry.getIdentityKey())) {
				return true;
			}
		}
		return false;
	}

	public synchronized Buddy get(Long identityKey) {
		for(Buddy entry:entries) {
			if(identityKey.equals(entry.getIdentityKey())) {
				return entry;
			}
		}
		return null;
	}
	
	public synchronized void addBuddies(List<Buddy> buddies) {
		if(buddies != null) {
			for(Buddy buddy:buddies) {
				if(identityKey != null && identityKey.equals(buddy.getIdentityKey())) {
					//continue
				} else if(buddy.getIdentityKey() == null) {
					//do nothing
				} else if(contains(buddy.getIdentityKey())) {
					//update status
					get(buddy.getIdentityKey()).setStatus(buddy.getStatus());
				} else {
					entries.add(buddy);
				}	
			}
		}
	}
	
	public synchronized void add(Buddy entry) {
		entries.add(entry);
	}
	
	public synchronized int size() {
		return entries == null ? 0: entries.size();
	}

	public synchronized List<Buddy> getEntries() {
		return entries;
	}
	
	public synchronized void update(List<Buddy> newBuddies) {
		//remove duplicates
		newBuddies.removeAll(entries);
		//add the new buddies
		entries.addAll(newBuddies);
	}
	
	public synchronized void clear() {
		entries.clear();
	}
}

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
	
	private final List<RosterEntry> entries;
	
	public Roster() {
		entries = new ArrayList<RosterEntry>();
	}
	
	public Roster(List<RosterEntry> entries) {
		this.entries = entries;
	}
	
	public boolean contains(Long identityKey) {
		for(RosterEntry entry:entries) {
			if(identityKey.equals(entry.getIdentityKey())) {
				return true;
			}
		}
		return false;
	}

	public RosterEntry get(Long identityKey) {
		for(RosterEntry entry:entries) {
			if(identityKey.equals(entry.getIdentityKey())) {
				return entry;
			}
		}
		return null;
	}
	
	public void addBuddies(List<Buddy> buddies) {
		if(buddies != null) {
			for(Buddy buddy:buddies) {
				if(contains(buddy.getIdentityKey())) {
					//update status
					get(buddy.getIdentityKey()).setStatus(buddy.getStatus());
				} else {
					entries.add(new RosterEntry(buddy));
				}	
			}
		}
	}
	
	public void add(RosterEntry entry) {
		entries.add(entry);
	}
	
	public int size() {
		return entries == null ? 0: entries.size();
	}

	public List<RosterEntry> getEntries() {
		return entries;
	}
	
	public void update(List<RosterEntry> newBuddies) {
		//remove duplicates
		newBuddies.removeAll(entries);
		//add the new buddies
		entries.addAll(newBuddies);
	}
}

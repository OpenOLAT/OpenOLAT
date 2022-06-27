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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.olat.instantMessaging.model.Buddy;
import org.olat.instantMessaging.model.BuddyGroup;

/**
 * 
 * Initial date: 05.12.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class Roster {
	
	private final boolean withMe;
	private final Long meIdentityKey;
	private final List<Buddy> entries = new CopyOnWriteArrayList<>();
	private final List<BuddyGroup> groups = new CopyOnWriteArrayList<>();
	
	public Roster(Long identityKey, boolean withMe) {
		this.meIdentityKey = identityKey;
		this.withMe = withMe;
	}
	
	public List<BuddyGroup> getGroups() {
		return groups;
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
		
		for(BuddyGroup group:groups) {
			for(Buddy entry:group.getBuddy()) {
				if(identityKey.equals(entry.getIdentityKey())) {
					return entry;
				}
			}
		}
		return null;
	}
	
	public synchronized void remove(Buddy entry) {
		if(entry == null) return;
		
		entries.remove(entry);
		for(BuddyGroup group:groups) {
			group.getBuddy().remove(entry);
		}
	}
	
	public synchronized void addBuddies(List<Buddy> buddies) {
		if(buddies != null) {
			for(Buddy buddy:buddies) {
				if(meIdentityKey != null && meIdentityKey.equals(buddy.getIdentityKey())) {
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
	
	public synchronized List<Buddy> getBuddies() {
		Set<Buddy> buddies = new HashSet<>();
		Set<Buddy> vips = new HashSet<>();
		for(Buddy entry:entries) {
			if(entry.isVip()) {
				vips.add(entry);
			}
			buddies.add(entry);
		}
		for(BuddyGroup group:groups) {
			for(Buddy entry:group.getBuddy()) {
				if(entry.isVip()) {
					vips.add(entry);
				}
				buddies.add(entry);
			}
		}

		//if vip once, vip always
		List<Buddy> orderedBuddies = new ArrayList<>(buddies.size());
		for(Buddy buddy:buddies) {
			Buddy clone = buddy.clone();
			clone.setVip(vips.contains(buddy));
			orderedBuddies.add(clone);
		}

		Collections.sort(orderedBuddies);
		return orderedBuddies;
	}
	
	public synchronized void add(Buddy entry) {
		entries.add(entry);
	}
	
	public synchronized int size() {
		return entries.size();
	}

	public synchronized List<Buddy> getEntries() {
		List<Buddy> copy;
		if(withMe) {
			copy = new ArrayList<>(entries);
		} else {
			copy = entries.stream()
					.filter(b -> !meIdentityKey.equals(b.getIdentityKey()))
					.collect(Collectors.toList());
		}
		return copy;
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

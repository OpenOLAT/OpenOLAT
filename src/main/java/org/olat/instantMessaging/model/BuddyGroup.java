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
package org.olat.instantMessaging.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 
 * A group of buddy
 * 
 * Initial date: 20.12.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BuddyGroup implements Serializable {

	private static final long serialVersionUID = 4744899346855880769L;
	private Long groupKey;
	private String groupName;
	private List<Buddy> buddyList;
	
	public BuddyGroup() {
		buddyList = new ArrayList<>();
	}
	
	public BuddyGroup(Long groupKey, String groupName) {
		this.groupKey = groupKey;
		this.groupName = groupName;
		buddyList = new ArrayList<>();
	}
	
	public int size() {
		return buddyList.size();
	}
		
	public Long getGroupKey() {
		return groupKey;
	}

	public void setGroupKey(Long groupKey) {
		this.groupKey = groupKey;
	}

	public String getGroupName() {
		return groupName;
	}
	
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	
	public void addBuddy(Buddy buddy) {
		for(Buddy current:buddyList) { 
			if(current.getIdentityKey().equals(buddy.getIdentityKey())) {
				if(!current.isVip() && buddy.isVip()) {
					current.setVip(true);
				}
				return;
			}
		}
		buddyList.add(buddy);
	}
	
	public List<Buddy> getBuddy() {
		List<Buddy> orderedBuddyList = new ArrayList<Buddy>(buddyList.size());
		for(Buddy buddy:buddyList) {
			Buddy clone = buddy.clone();
			orderedBuddyList.add(clone);
		}
		Collections.sort(orderedBuddyList);

		return orderedBuddyList;
	}
}

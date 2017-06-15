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
package org.olat.modules.lecture.model;

import java.util.Date;

/**
 * 
 * Initial date: 15 juin 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LocationHistory {
	
	private String location;
	private Date lastUsed;
	
	public LocationHistory() {
		//
	}
	
	public LocationHistory(String location, Date lastUsed) {
		this.location = location;
		this.lastUsed = lastUsed;
	}
	
	public String getLocation() {
		return location;
	}
	
	public void setLocation(String location) {
		this.location = location;
	}
	
	public Date getLastUsed() {
		return lastUsed;
	}
	
	public void setLastUsed(Date lastUsed) {
		this.lastUsed = lastUsed;
	}

	@Override
	public int hashCode() {
		return location == null ? 34658 : location.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof LocationHistory) {
			LocationHistory l = (LocationHistory)obj;
			return location != null && location.equalsIgnoreCase(l.location);
		}
		return super.equals(obj);
	}

	@Override
	public String toString() {
		return "locationHistory[location=" + location + "]";
	}
}

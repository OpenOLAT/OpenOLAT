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
 * 12.10.2011 by frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.openmeetings.model;

/**
 * 
 * Initial date: 07.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum RoomType {
	
	conference(1, "room.type.conference"),
	audience(2,"room.type.audience"),
	restricted(3, "room.type.restricted"),
	interview(4, "room.type.interview");

	private final long type;
	private final String i18nKey;

	private RoomType(long type, String i18nKey) {
		this.type = type;
		this.i18nKey = i18nKey;
	}
	
	public long type() {
		return type;
	}
	
	public static RoomType getType(long type) {
		for(RoomType roomType:RoomType.values()) {
			if(roomType.type == type) {
				return roomType;
			}
		}
		return null;
	}
	
	public String typeStr() {
		return Long.toString(type);
	}
	
	public String i18nKey() {
		return i18nKey;
	}

}

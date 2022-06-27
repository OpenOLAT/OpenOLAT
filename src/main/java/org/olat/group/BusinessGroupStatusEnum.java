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
package org.olat.group;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 2 sept. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum BusinessGroupStatusEnum {
	
	active,
	inactive,
	/**
	 * Trash, members removed
	 */
	trash,
	/**
	 * Empty shell let behind to respect some foreign key constraints of
	 * objects which cannot be deleted
	 */
	deleted;
	
	
	public static boolean isReadOnly(BusinessGroup businessGroup) {
		BusinessGroupStatusEnum status = businessGroup.getGroupStatus();
		return status != active;
	}
	
	public static boolean isValid(String string) {
		boolean allOk = false;
		if(StringHelper.containsNonWhitespace(string)) {
			for(BusinessGroupStatusEnum status:values()) {
				if(status.name().equals(string)) {
					allOk = true;
					break;
				}
			}
		}
		return allOk;
	}
	
	public static BusinessGroupStatusEnum secureValueOf(String string) {
		if(StringHelper.containsNonWhitespace(string)) {
			for(BusinessGroupStatusEnum status:values()) {
				if(status.name().equals(string)) {
					return status;
				}
			}
		}
		return BusinessGroupStatusEnum.active;
	}
	
	public static List<BusinessGroupStatusEnum> toList(List<String> status) {
		List<BusinessGroupStatusEnum> list = new ArrayList<>();
		if(status != null && !status.isEmpty()) {
			for(String s:status) {
				if(isValid(s)) {
					list.add(valueOf(s));
				}
			}
		}
		return list;
	}

}

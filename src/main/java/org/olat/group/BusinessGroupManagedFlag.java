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

import java.util.Arrays;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;

/**
 * List of flags for managed groups
 * 
 * Initial date: 10.07.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public enum BusinessGroupManagedFlag {
	
	all,
	  details(all),//details tab
	    title(details,all),
	    description(details,all),
	    settings(details,all),//max num of participants...
	  tools(all),//tools tab
	  members(all),//members tab
	    display(members,all),// members display options
	    membersmanagement(members,all),
	  resources(all),//add/remove courses
	  bookings(all),// change booking rules
	  inactivate(all),
	  delete(all);
	
	
	private BusinessGroupManagedFlag[] parents;
	private static final Logger log = Tracing.createLoggerFor(BusinessGroupManagedFlag.class);
	public static final BusinessGroupManagedFlag[] EMPTY_ARRAY = new BusinessGroupManagedFlag[0];
	private static BusinessGroupModule groupModule;
	
	private BusinessGroupManagedFlag() {
		//
	}
	
	private BusinessGroupManagedFlag(BusinessGroupManagedFlag... parents) {
		if(parents == null) {
			this.parents = new BusinessGroupManagedFlag[0];
		} else {
			this.parents = parents;
		}
	}
	
	public static BusinessGroupManagedFlag[] toEnum(String flags) {
		if(StringHelper.containsNonWhitespace(flags)) {
			String[] flagArr = flags.split(",");
			BusinessGroupManagedFlag[] flagEnums = new BusinessGroupManagedFlag[flagArr.length];
	
			int count = 0;
			for(String flag:flagArr) {
				if(StringHelper.containsNonWhitespace(flag)) {
					try {
						BusinessGroupManagedFlag flagEnum = valueOf(flag);
						flagEnums[count++] = flagEnum;
					} catch (Exception e) {
						log.warn("Cannot parse this managed flag: " + flag, e);
					}
				}
			}
			
			if(count != flagEnums.length) {
				flagEnums = Arrays.copyOf(flagEnums, count);
			}
			return flagEnums;
		} else {
			return EMPTY_ARRAY;
		}
	}
	
	public static boolean isManaged(BusinessGroup group, BusinessGroupManagedFlag marker) {
		if(groupModule == null) {
			groupModule = CoreSpringFactory.getImpl(BusinessGroupModule.class);
		}
		if(!groupModule.isManagedBusinessGroups()) {
			return false;
		}
		
		if(group != null && (contains(group, marker) || contains(group, marker.parents))) {
			return true;
		}
		return false;
	}
	
	public static boolean isManaged(BusinessGroupManagedFlag[] flags, BusinessGroupManagedFlag marker) {
		if(groupModule == null) {
			groupModule = CoreSpringFactory.getImpl(BusinessGroupModule.class);
		}
		if(!groupModule.isManagedBusinessGroups()) {
			return false;
		}
		
		if(flags != null && (contains(flags, marker) || contains(flags, marker.parents))) {
			return true;
		}
		return false;
	}
	
	private static boolean contains(BusinessGroup group, BusinessGroupManagedFlag... markers) {
		if(group == null) return false;
		BusinessGroupManagedFlag[] flags = group.getManagedFlags();
		return contains(flags, markers);
	}

	private static boolean contains(BusinessGroupManagedFlag[] flags, BusinessGroupManagedFlag... markers) {
		if(flags == null || flags.length == 0) return false;

		for(BusinessGroupManagedFlag flag:flags) {
			for(BusinessGroupManagedFlag marker:markers) {
				if(flag.equals(marker)) {
					return true;
				}
			}
		}
		return false;
	}
}

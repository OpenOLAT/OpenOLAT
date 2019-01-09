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
package org.olat.group.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BusinessGroupEnvironment {

	private final List<BGAreaReference> areas = new ArrayList<>();
	private final List<BusinessGroupReference> groups = new ArrayList<>();
	
	public List<BGAreaReference> getAreas() {
		return areas;
	}
	
	public List<BusinessGroupReference> getGroups() {
		return groups;
	}
	
	public String getGroupName(Long groupKey) {
		for(BusinessGroupReference ref:getGroups()) {
			if(ref.getKey().equals(groupKey)) {
				return ref.getName();
			}
		}
		return null;
	}
	
	public String getAreaName(Long areaKey) {
		for(BGAreaReference ref:getAreas()) {
			if(ref.getKey().equals(areaKey)) {
				return ref.getName();
			}
		}
		return null;
	}
}

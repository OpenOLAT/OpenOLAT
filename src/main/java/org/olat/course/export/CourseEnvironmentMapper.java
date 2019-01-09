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
package org.olat.course.export;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.group.model.BGAreaReference;
import org.olat.group.model.BusinessGroupEnvironment;
import org.olat.group.model.BusinessGroupReference;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class CourseEnvironmentMapper {

	private final List<BGAreaReference> areas = new ArrayList<>();
	private final List<BusinessGroupReference> groups = new ArrayList<>();
	private final Map<String,Map<String,String>> mapUniqueKeys = new HashMap<>();
	
	private Identity author;
	
	public CourseEnvironmentMapper() {
		//
	}
	
	public Identity getAuthor() {
		return author;
	}

	public void setAuthor(Identity author) {
		this.author = author;
	}
	
	public void addUniqueKeyPair(String ident, String sourceKey, String targetKey) {
		Map<String,String> map;
		if(mapUniqueKeys.containsKey(ident)) {
			map = mapUniqueKeys.get(ident);
		} else {
			map = new HashMap<>();
			mapUniqueKeys.put(ident, map);
		}
		map.put(sourceKey, targetKey);
	}
	
	public String getTargetUniqueKey(String ident, String sourceKey) {
		if(mapUniqueKeys.containsKey(ident)) {
			return mapUniqueKeys.get(ident).get(sourceKey);
		}
		return null;
	}

	public List<BGAreaReference> getAreas() {
		return areas;
	}
	
	public List<BusinessGroupReference> getGroups() {
		return groups;
	}
	
	public void addBusinessGroupEnvironment(BusinessGroupEnvironment env) {
		areas.addAll(env.getAreas());
		groups.addAll(env.getGroups());
	}
	
	public List<Long> toGroupKeyFromOriginalNames(String groupNames) {
		if(!StringHelper.containsNonWhitespace(groupNames)) return null;
		String[] groupNameArr = groupNames.split(",");
		List<Long> groupKeyList = new ArrayList<>();
		for(String groupName:groupNameArr) {
			groupName = groupName.trim();
			for(BusinessGroupReference group:groups) {
				if(groupName.equalsIgnoreCase(group.getOriginalName())) {
					groupKeyList.add(group.getKey());
					break;
				}
			}
		}
		return groupKeyList;
	}
	
	public List<Long> toGroupKeyFromOriginalKeys(List<Long> originalKeys) {
		if(originalKeys == null || originalKeys.isEmpty()) return null;
		List<Long> groupKeyList = new ArrayList<>();
		for(Long originalKey:originalKeys) {
			for(BusinessGroupReference group:groups) {
				if(originalKey.equals(group.getOriginalKey())) {
					groupKeyList.add(group.getKey());
					break;
				}
			}
		}
		return groupKeyList;
	}
	
	public Long toGroupKeyFromOriginalKey(Long originalKey) {
		if(originalKey == null) return null;
		Long groupKey = null;
		for(BusinessGroupReference group:groups) {
			if(originalKey.equals(group.getOriginalKey())) {
				groupKey = group.getKey();
				break;
			}
		}
		return groupKey;
	}
	
	public List<Long> toAreaKeyFromOriginalNames(String areaNames) {
		if(!StringHelper.containsNonWhitespace(areaNames)) return null;
		String[] areaNameArr = areaNames.split(",");
		List<Long> areaKeyList = new ArrayList<>();
		for(String areaName:areaNameArr) {
			areaName = areaName.trim();
			for(BGAreaReference area:areas) {
				if(areaName.equalsIgnoreCase(area.getOriginalName())) {
					areaKeyList.add(area.getKey());
					break;
				}
			}
		}
		return areaKeyList;
	}
	
	public List<Long> toAreaKeyFromOriginalKeys(List<Long> originalKeys) {
		if(originalKeys == null || originalKeys.isEmpty()) return null;
		List<Long> areaKeyList = new ArrayList<>();
		for(Long originalKey:originalKeys) {
			for(BGAreaReference area:areas) {
				if(originalKey.equals(area.getOriginalKey())) {
					areaKeyList.add(area.getKey());
					break;
				}
			}
		}
		return areaKeyList;
	}
	
	public String toGroupNames(List<Long> groupKeys) {
		if(groupKeys == null || groupKeys.isEmpty()) return "";
		StringBuilder sb = new StringBuilder();
		for(Long groupKey:groupKeys) {
			for(BusinessGroupReference group:groups) {
				if(groupKey.equals(group.getKey())) {
					if(sb.length() > 0) sb.append(',');
					sb.append(group.getName());
					break;
				}
			}
		}
		return sb.toString();
	}
	
	public String toAreaNames(List<Long> areaKeys) {
		if(areaKeys == null || areaKeys.isEmpty()) return "";
		StringBuilder sb = new StringBuilder();
		for(Long areaKey:areaKeys) {
			for(BGAreaReference area:areas) {
				if(areaKey.equals(area.getKey())) {
					if(sb.length() > 0) sb.append(',');
					
					sb.append(area.getName());
					break;
				}
			}
		}
		return sb.toString();
	}
}

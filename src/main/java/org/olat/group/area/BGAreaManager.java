/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.group.area;

import java.util.List;

import org.olat.core.id.Identity;
import org.olat.group.BusinessGroup;
import org.olat.resource.OLATResource;

/**
 * Description:<BR/> Manager to handle business group areas. A business group
 * area is used to group areas. A business group can be associated to many
 * areas, an area can have many groups <P/> Initial Date: Aug 25, 2004
 * 
 * @author gnaegi
 */
public interface BGAreaManager {
	/**
	 * Creates an area object and persists the object in the database only if such
	 * an area name does not already exist in this context
	 * 
	 * @param areaName The visible area name
	 * @param description The area description
	 * @param resource The resource of this area
	 * @return The new area or null if no area has been created
	 */
	public BGArea createAndPersistBGArea(String areaName, String description, OLATResource resource);

	/**
	 * Finds an area in the given context
	 * 
	 * @param areaName
	 * @param resource
	 * @return The area or null if the area does not exists
	 */
	public BGArea findBGArea(String areaName, OLATResource resource);
	
	/**
	 * Load an area by its primary key
	 * @param key
	 * @return
	 */
	public BGArea loadArea(Long key);
	
	/**
	 * Load a list of areas
	 * @param keys
	 * @return
	 */
	public List<BGArea> loadAreas(List<Long> keys);

	/**
	 * Update the given area in the database
	 * 
	 * @param area
	 * @return the updated area
	 */
	public BGArea updateBGArea(BGArea area);

	/**
	 * Delete the given area form the database
	 * 
	 * @param area
	 */
	public void deleteBGArea(BGArea area);

	/**
	 * Add a business group to a business group area. Does not check it this
	 * relationship does already exists. Check this prior to using this method.
	 * 
	 * @param group
	 * @param area
	 */
	public void addBGToBGArea(BusinessGroup group, BGArea area);

	/**
	 * Remove a business group from a business group area. If no such relation
	 * exists, the mehthod does nothing.
	 * 
	 * @param group
	 * @param area
	 */
	public void removeBGFromArea(BusinessGroup group, BGArea area);
	
	public void removeBGFromAreas(BusinessGroup group, OLATResource resource);

	/**
	 * Deletes all business group to area relations from the given business group
	 * 
	 * @param group
	 */
	public void deleteBGtoAreaRelations(BusinessGroup group);

	/**
	 * Searches for all business groups that are associated with the given
	 * business group area
	 * 
	 * @param area
	 * @return A list of business groups
	 */
	public List<BusinessGroup> findBusinessGroupsOfArea(BGArea area);

	/**
	 * Searches for all business groups that are associated with the given
	 * business group areas
	 * 
	 * @param area
	 * @return A list of business groups
	 */
	public List<BusinessGroup> findBusinessGroupsOfAreas(List<BGArea> areas);
	
	/**
	 * Searches for all business groups that are associated with the given
	 * business group areas primary keys
	 * 
	 * @param area
	 * @return A list of business groups
	 */
	public List<BusinessGroup> findBusinessGroupsOfAreaKeys(List<Long> areaKeys);
	
	/**
	 * @param areaKeys
	 * @return List of keys
	 */
	public List<Long> findBusinessGroupKeysOfAreaKeys(List<Long> areaKeys);

	/**
	 * Searches for all business groups that are associated with the given
	 * business group area where the given identity is in the participants group
	 * 
	 * @param identity
	 * @param areaName
	 * @param context
	 * @return A list of business groups
	 */
	public List<BusinessGroup> findBusinessGroupsOfAreaAttendedBy(Identity identity, List<Long> areaKeys, OLATResource resource);

	/**
	 * Searches for all business group areas associated with the given business
	 * group
	 * 
	 * @param group
	 * @return A list of business group area
	 */
	public List<BGArea> findBGAreasOfBusinessGroup(BusinessGroup group);
	
	/**
	 * Searches for all business group areas associated with the given business
	 * groups
	 * 
	 * @param group
	 * @return A list of business group area
	 */
	public List<BGArea> findBGAreasOfBusinessGroups(List<BusinessGroup> groups);
	
	/**
	 * 
	 * @param groups
	 * @return
	 */
	public int countBGAreasOfBusinessGroups(List<BusinessGroup> groups);
	
	/**
	 * Counts the number of business group areas of the given business group
	 * context
	 * 
	 * @param resource
	 * @return Number of business group areas
	 */
	public int countBGAreasInContext(OLATResource resource);

	/**
	 * Searches for all business group areas in the given business group context
	 * 
	 * @param resource
	 * @return A list of business group areas
	 */
	public List<BGArea> findBGAreasInContext(OLATResource resource);

	/**
	 * Checks if an identity is in a business group areas with a given name in the
	 * given group context
	 * 
	 * @param identity
	 * @param areaName
	 * @param resource
	 * @return true if identity is in such an area, false otherwise
	 */
	public boolean isIdentityInBGArea(Identity identity, String areaName, Long groupKey, OLATResource resource);

	/**
	 * Reloads the business group area from the database or the hibernate second
	 * level cache
	 * 
	 * @param area
	 * @return The reloaded area
	 */
	public BGArea reloadArea(BGArea area);

	/**
	 * Check if an area exist with this anem or this primary key within the
	 * context of the resource
	 * @param nameOrKey
	 * @param resource
	 * @return
	 */
	public boolean existArea(String nameOrKey, OLATResource resource);
	
	/**
	 * Retrieve the area's primary keys from the name
	 * @param areaNames
	 * @return
	 */
	public List<Long> toAreaKeys(String areaNames, OLATResource resource);
}
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
import java.util.Map;
import java.util.Set;

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
	 * @param groupContext The group context of this area
	 * @return The new area or null if no area has been created
	 */
	public abstract BGArea createAndPersistBGAreaIfNotExists(String areaName, String description, OLATResource resource);

	/**
	 * Copies all group areas from the original context to the target context. The
	 * method returns a hash map with all original areas as key and the newly
	 * created areas as value.
	 * 
	 * @param origBgContext Context containing the orignial areas
	 * @param targetBgContext Context where the areas should be created
	 * @return Map mapping the original to the new areas
	 */
	public abstract Map<BGArea,BGArea> copyBGAreasOfBGContext(OLATResource sourceResource, OLATResource targetResource);

	/**
	 * Finds an area in the given context
	 * 
	 * @param areaName
	 * @param groupContext
	 * @return The area or null if the area does not exists
	 */
	public abstract BGArea findBGArea(String areaName, OLATResource resource);

	/**
	 * Update the given area in the database
	 * 
	 * @param area
	 * @return the updated area
	 */
	public abstract BGArea updateBGArea(BGArea area);

	/**
	 * Delete the given area form the database
	 * 
	 * @param area
	 */
	public abstract void deleteBGArea(BGArea area);

	/**
	 * Add a business group to a business group area. Does not check it this
	 * relationship does already exists. Check this prior to using this method.
	 * 
	 * @param group
	 * @param area
	 */
	public abstract void addBGToBGArea(BusinessGroup group, BGArea area);

	/**
	 * Remove a business group from a business group area. If no such relation
	 * exists, the mehthod does nothing.
	 * 
	 * @param group
	 * @param area
	 */
	public abstract void removeBGFromArea(BusinessGroup group, BGArea area);

	/**
	 * Deletes all business group to area relations from the given business group
	 * 
	 * @param group
	 */
	public abstract void deleteBGtoAreaRelations(BusinessGroup group);

	/**
	 * Searches for all business groups that are associated with the given
	 * business group area
	 * 
	 * @param area
	 * @return A list of business groups
	 */
	public List<BusinessGroup> findBusinessGroupsOfArea(BGArea area);
	public List<BusinessGroup> findBusinessGroupsOfAreas(List<BGArea> areas);

	/**
	 * Searches for all business groups that are associated with the given
	 * business group area where the given identity is in the participants group
	 * 
	 * @param identity
	 * @param areaName
	 * @param context
	 * @return A list of business groups
	 */
	public List<BusinessGroup> findBusinessGroupsOfAreaAttendedBy(Identity identity, String areaName, OLATResource resource);

	/**
	 * Searches for all business group areas associated with the given business
	 * group
	 * 
	 * @param group
	 * @return A list of business group area
	 */
	public List<BGArea> findBGAreasOfBusinessGroup(BusinessGroup group);
	
	public List<BGArea> findBGAreasOfBusinessGroups(List<BusinessGroup> groups);
	
	

	/**
	 * Counts the number of business group areas of the given business group
	 * context
	 * 
	 * @param groupContext
	 * @return Number of business gropu areas
	 */
	public abstract int countBGAreasOfBGContext(OLATResource resource);

	/**
	 * Searches for all business group areas in the given business group context
	 * 
	 * @param groupContext
	 * @return A list of business group areas
	 */
	public List<BGArea> findBGAreasOfBGContext(OLATResource resource);

	/**
	 * Checks if an identity is in a business group areas with a given name in the
	 * given group context
	 * 
	 * @param identity
	 * @param areaName
	 * @param groupContext
	 * @return true if identity is in such an area, false otherwhise
	 */
	public boolean isIdentityInBGArea(Identity identity, String areaName, OLATResource resource);

	/**
	 * Reloads the business group area from the database or the hibernate second
	 * level cache
	 * 
	 * @param area
	 * @return The reloaded area
	 */
	public abstract BGArea reloadArea(BGArea area);

	/**
	 * checks if one or more of the given names exists already in the context.
	 * @param allNames
	 * @param bgContext
	 * @return
	 */
	public abstract boolean checkIfOneOrMoreNameExistsInContext(Set<String> allNames, OLATResource resource);
}
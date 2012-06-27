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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.manager.BasicManager;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.SyncerCallback;
import org.olat.core.util.coordinate.SyncerExecutor;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupImpl;
import org.olat.group.manager.BusinessGroupArchiver;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Description:<BR/> Implementation of the business group area manager <P/>
 * Initial Date: Aug 24, 2004
 * 
 * @author gnaegi
 */
@Service("areaManager")
public class BGAreaManagerImpl extends BasicManager implements BGAreaManager {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BusinessGroupArchiver businessGroupArchiver;


	/**
	 * @see org.olat.group.area.BGAreaManager#createAndPersistBGAreaIfNotExists(java.lang.String,
	 *      java.lang.String, org.olat.group.context.BGContext)
	 */
	//o_clusterOK by:cg synchronized on resource
	public BGArea createAndPersistBGAreaIfNotExists(final String areaName, final String description, final OLATResource resource) { 
		BGArea createdBGArea =CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(resource, new SyncerCallback<BGArea>(){
			public BGArea execute() {
				BGArea area = findBGArea(areaName, resource);
				if (area == null) { 
					return createAndPersistBGArea(areaName, description, resource); 
				}
				return null;
			}
		});
		return createdBGArea;
	}

	/**
	 * @see org.olat.group.area.BGAreaManager#copyBGAreasOfBGContext(org.olat.group.context.BGContext,
	 *      org.olat.group.context.BGContext)
	 */
	// o_clusterOK by:cg ; must be synchronized too ? => not 100% sure, 
	public Map<BGArea,BGArea> copyBGAreasOfBGContext(OLATResource origBgContext, final OLATResource targetBgContext) {
		List<BGArea> origAreas = findBGAreasOfBGContext(origBgContext);
		Map<BGArea,BGArea> areas = new HashMap<BGArea,BGArea>();
		Iterator<BGArea> iterator = origAreas.iterator();
		while (iterator.hasNext()) {
			BGArea origArea = (BGArea) iterator.next();
			BGArea targetArea = createAndPersistBGArea(origArea.getName(), origArea.getDescription(), targetBgContext);
			areas.put(origArea, targetArea);
		}
		return areas;
	}

	/**
	 * @see org.olat.group.area.BGAreaManager#findBGArea(java.lang.String,
	 *      org.olat.group.context.BGContext)
	 */
	public BGArea findBGArea(String areaName, OLATResource resource) {
		StringBuilder sb = new StringBuilder();
		sb.append("select area from ").append(BGAreaImpl.class.getName()).append(" area ")
		  .append(" where area.name=:areaName and area.resource.key=:resourceKey");
		
		List<BGArea> areas = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), BGArea.class)
				.setParameter("areaName", areaName)
				.setParameter("resourceKey", resource.getKey())
				.getResultList();

		if (areas.isEmpty()) {
			return null;
		} else if (areas.size() > 1) {
			throw new OLATRuntimeException(BGAreaManagerImpl.class, "findBGArea(" + areaName+ ") returned more than one row for BGContext with key " + resource.getKey(), null);
		}
		return areas.get(0);
	}

	/**
	 * @see org.olat.group.area.BGAreaManager#updateBGArea(org.olat.group.area.BGArea)
	 */
	//o_clusterOK by:cg synchronized
	public BGArea updateBGArea(final BGArea area) {
		// look if an area with such a name does already exist in this context
		final OLATResource resource = area.getResource();
		
		BGArea updatedBGArea =CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(resource, new SyncerCallback<BGArea>(){
			public BGArea execute() {
				BGArea douplicate = findBGArea(area.getName(), resource);
				if (douplicate == null) {
					// does not exist, so just update it
					dbInstance.updateObject(area);
					return area;
				} else if (douplicate.getKey().equals(area.getKey())) {
					// name already exists, found the same object (name didn't change)
					// need to copy description (that has changed) and update the object.
					// if we updated area at this place we would get a hibernate exception
					douplicate.setDescription(area.getDescription());
					dbInstance.updateObject(douplicate);
					return douplicate;
				}
				return null; // nothing updated
			}
		});
		return updatedBGArea;
	}

	/**
	 * @see org.olat.group.area.BGAreaManager#deleteBGArea(org.olat.group.area.BGArea)
	 */
	// o_clusterOK by:cg must be synchronized too
	public void deleteBGArea(final BGArea area) {
		final OLATResource resource = area.getResource();
		
		CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(resource, new SyncerExecutor(){
			public void execute() {
				BGArea reloadArea = findBGArea(area.getName(), resource);
				if (reloadArea != null) {
					// 1) delete all area - group relations
					deleteBGtoAreaRelations(reloadArea);
					// 2) delete area itself
					dbInstance.deleteObject(reloadArea);
					logAudit("Deleted Business Group Area", reloadArea.toString());
				} else {
					logAudit("Business Group Area was already deleted", area.toString());
				}
			}
		});
	}

	/**
	 * Deletes all business group to area relations from the given business group
	 * 
	 * @param group
	 */
	public void deleteBGtoAreaRelations(BusinessGroup group) {
		StringBuilder sb = new StringBuilder();
		sb.append(" delete from ").append(BGtoAreaRelationImpl.class.getName()).append(" as bgarel where bgarel.businessGroup.key=:groupKey");
		
		dbInstance.getCurrentEntityManager().createQuery(sb.toString())
				.setParameter("groupKey", group.getKey())
				.executeUpdate();
	}

	/**
	 * @see org.olat.group.area.BGAreaManager#addBGToBGArea(org.olat.group.BusinessGroup,
	 *      org.olat.group.area.BGArea)
	 */
	public void addBGToBGArea(BusinessGroup group, BGArea area) {
		BGtoAreaRelation bgAreaRel = new BGtoAreaRelationImpl(area, group);
		dbInstance.saveObject(bgAreaRel);
	}

	/**
	 * @see org.olat.group.area.BGAreaManager#removeBGFromArea(org.olat.group.BusinessGroup,
	 *      org.olat.group.area.BGArea)
	 */
	public void removeBGFromArea(BusinessGroup group, BGArea area) {
		removeBGFromArea(group.getKey(), area.getKey());
	}

	/**
	 * @see org.olat.group.area.BGAreaManager#findBusinessGroupsOfArea(org.olat.group.area.BGArea)
	 */
	public List<BusinessGroup> findBusinessGroupsOfArea(BGArea area) {
		return findBusinessGroupsOfAreas(Collections.singletonList(area));
	}

	@Override
	public List<BusinessGroup> findBusinessGroupsOfAreas(List<BGArea> areas) {
		if(areas == null || areas.isEmpty()) return Collections.emptyList();
		
		StringBuilder sb = new StringBuilder();
		sb.append("select bgarel.businessGroup from ").append(BGtoAreaRelationImpl.class.getName()).append(" as bgarel ")
		  .append(" where  bgarel.groupArea.key in (:areakeys)");

		List<Long> areaKeys = new ArrayList<Long>();
		for(BGArea area:areas) {
			areaKeys.add(area.getKey());
		}
		
		List<BusinessGroup> result = DBFactory.getInstance().getCurrentEntityManager()
				.createQuery(sb.toString(), BusinessGroup.class)
				.setParameter("areaKeys", areaKeys)
				.getResultList();
		return result;
	}

	/**
	 * @see org.olat.group.area.BGAreaManager#findBusinessGroupsOfAreaAttendedBy(org.olat.core.id.Identity,
	 *      java.lang.String, org.olat.group.context.BGContext)
	 */
	public List<BusinessGroup> findBusinessGroupsOfAreaAttendedBy(Identity identity, String areaName, OLATResource resource) {
		StringBuilder sb = new StringBuilder();
		sb.append("select bgi from ").append(BusinessGroupImpl.class.getName()).append(" as bgi ")
		  .append(", org.olat.basesecurity.SecurityGroupMembershipImpl as sgmi , org.olat.group.area.BGtoAreaRelationImpl as bgarel")
			.append(", org.olat.group.area.BGAreaImpl as area")
			.append(" where area.name=:name and bgarel.businessGroup=bgi")
			.append("  and bgarel.groupArea=area and bgi.partipiciantGroup=sgmi.securityGroup and sgmi.identity.key=:identityKey")
			.append("  and area.resource.key=:resourceKey");
		
		List<BusinessGroup> groups = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), BusinessGroup.class)
			.setParameter("identityKey", identity.getKey())
			.setParameter("resourceKey", resource.getKey())
			.getResultList();

		return groups;
	}

	/**
	 * @see org.olat.group.area.BGAreaManager#findBGAreasOfBusinessGroup(org.olat.group.BusinessGroup)
	 */
	public List<BGArea> findBGAreasOfBusinessGroup(BusinessGroup group) {
		return findBGAreasOfBusinessGroups(Collections.singletonList(group));
	}

	@Override
	public List<BGArea> findBGAreasOfBusinessGroups(List<BusinessGroup> groups) {
		if(groups == null || groups.isEmpty()) return Collections.emptyList();
		
		StringBuilder sb = new StringBuilder();
		sb.append("select bgarel.groupArea from ").append(BGtoAreaRelationImpl.class.getName()).append(" as bgarel ")
		  .append("where bgarel.businessGroup.key in (:groupKeys)");

		TypedQuery<BGArea> areaQuery = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), BGArea.class);
		List<Long> groupKeys = new ArrayList<Long>();
		for(BusinessGroup group:groups) {
			groupKeys.add(group.getKey());
		}
		areaQuery.setParameter("groupKeys", groupKeys);
		return areaQuery.getResultList();
	}

	/**
	 * @see org.olat.group.area.BGAreaManager#countBGAreasOfBGContext(org.olat.group.context.BGContext)
	 */
	public int countBGAreasOfBGContext(OLATResource resource) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(area) from ").append(BGAreaImpl.class.getName()).append(" area where area.resource.key=:resourceKey");
		Number count = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Number.class)
				.setParameter("resourceKey", resource.getKey())
				.getSingleResult();
		return count.intValue();
	}

	/**
	 * @see org.olat.group.area.BGAreaManager#findBGAreasOfBGContext(org.olat.group.context.BGContext)
	 */
	public List<BGArea> findBGAreasOfBGContext(OLATResource resource) {
		StringBuilder sb = new StringBuilder();
		sb.append("select area from ").append(BGAreaImpl.class.getName()).append(" area where area.resource.key=:resourceKey");
		List<BGArea> areas = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), BGArea.class)
				.setParameter("resourceKey", resource.getKey())
				.getResultList();
		return areas;
	}

	/**
	 * @see org.olat.group.area.BGAreaManager#isIdentityInBGArea(org.olat.core.id.Identity,
	 *      java.lang.String, org.olat.group.context.BGContext)
	 */
	public boolean isIdentityInBGArea(Identity identity, String areaName, OLATResource resource) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(grp) from ").append(BusinessGroupImpl.class.getName()).append(" as grp")
		  .append(", org.olat.group.area.BGAreaImpl as area, org.olat.group.area.BGtoAreaRelationImpl bgarel, org.olat.basesecurity.SecurityGroupMembershipImpl as secgmemb")
		  .append(" where bgarel.groupArea = area and bgarel.businessGroup = grp")
			.append(" and area.name=:name and area.resource.key=:resourceKey ")
			.append(" and ((grp.partipiciantGroup = secgmemb.securityGroup and secgmemb.identity.key=:identityKey) or (grp.ownerGroup = secgmemb.securityGroup and secgmemb.identity=:identityKey))");
		
		Number count = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Number.class)
			.setParameter("identityKey", identity.getKey())
			.setParameter("resourceKey", resource.getKey())
			.setParameter("name", areaName)
			.getSingleResult();
		return count.intValue() > 0;
	}

	/**
	 * @see org.olat.group.area.BGAreaManager#reloadArea(org.olat.group.area.BGArea)
	 */
	public BGArea reloadArea(BGArea area) {
		return (BGArea) DBFactory.getInstance().loadObject(area);
	}

	public boolean checkIfOneOrMoreNameExistsInContext(Set<String> allNames, OLATResource resource) {
		if(allNames == null || allNames.isEmpty()) {
			return false;
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("select count(area) from ").append(BGAreaImpl.class.getName()).append(" area where area.resource.key=:resourceKey and area.name in (:names)");
		Number count = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Number.class)
				.setParameter("resourceKey", resource.getKey())
				.setParameter("names", allNames)
				.getSingleResult();
		return count.intValue() > 0;
	}

	/**
	 * Creates an area object and persists the object in the database
	 * 
	 * @param areaName The visible area name
	 * @param description The area description
	 * @param resource The resource of this area
	 * @return The new area
	 */
	private BGArea createAndPersistBGArea(String areaName, String description, OLATResource resource) {
		BGArea area = new BGAreaImpl(areaName, description, resource);
		dbInstance.getCurrentEntityManager().persist(area);
		if (area != null) {
			logAudit("Created Business Group Area", area.toString());
		}
		// else no area created, name duplicate
		return area;
	}
	
	/**
	 * Remove a business group from a business group area. If no such relation
	 * exists, the mehthod does nothing.
	 * 
	 * @param businessGroupKey
	 * @param bgAreaKey
	 */
	private void removeBGFromArea(Long businessGroupKey, Long bgAreaKey) {
		StringBuilder sb = new StringBuilder();
		sb.append("delete from ").append(BGtoAreaRelationImpl.class.getName()).append(" as bgarel where bgarel.groupArea.key=:areaKey and bgarel.businessGroup=:groupKey");
		
		dbInstance.getCurrentEntityManager().createQuery(sb.toString())
			.setParameter("areaKey", bgAreaKey)
			.setParameter("groupKey", businessGroupKey)
			.executeUpdate();
	}
	
	/**
	 * Deletes all business group to area relations from the given business group
	 * area
	 * 
	 * @param area
	 */
	private void deleteBGtoAreaRelations(BGArea area) {
		StringBuilder sb = new StringBuilder();
		sb.append("delete from ").append(BGtoAreaRelationImpl.class.getName()).append(" as bgarel where bgarel.groupArea.key=:areaKey");
		
		dbInstance.getCurrentEntityManager().createQuery(sb.toString())
			.setParameter("areaKey", area.getKey())
			.executeUpdate();
	}
	
	@Override
	public File archiveAreaMembers(OLATResource resource, List<String> columnList, List<BGArea> areaList, String archiveType, Locale locale, String charset) {
		return businessGroupArchiver.archiveAreaMembers(resource, columnList, areaList, archiveType, locale, charset);
	}
}
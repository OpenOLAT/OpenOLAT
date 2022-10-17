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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.persistence.TypedQuery;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.SyncerExecutor;
import org.olat.group.BusinessGroup;
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
public class BGAreaManagerImpl implements BGAreaManager {
	
	private static final Logger log = Tracing.createLoggerFor(BGAreaManagerImpl.class); 
	
	@Autowired
	private DB dbInstance;
	
	@Override
	public BGArea loadArea(Long key) {
		return dbInstance.getCurrentEntityManager().find(BGAreaImpl.class, key);
	}

	@Override
	public List<BGArea> loadAreas(List<Long> keys) {
		if(keys == null || keys.isEmpty()) return Collections.emptyList();
		
		StringBuilder sb = new StringBuilder();
		sb.append("select area from ").append(BGAreaImpl.class.getName()).append(" area ")
		  .append(" where area.key in (:keys)");
		
		return dbInstance.getCurrentEntityManager().createQuery(sb.toString(), BGArea.class)
				.setParameter("keys", keys)
				.getResultList();
	}

	@Override
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


	//o_clusterOK by:cg synchronized
	@Override
	public BGArea updateBGArea(final BGArea area) {
		// look if an area with such a name does already exist in this context
		final OLATResource resource = area.getResource();
		return CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(resource, () -> {
			BGArea reloadArea = loadArea(area.getKey());
			reloadArea.setName(area.getName());
			reloadArea.setDescription(area.getDescription());
			return dbInstance.getCurrentEntityManager().merge(reloadArea);
		});
	}

	@Override
	public void deleteBGArea(final BGArea area) {
		final OLATResource resource = area.getResource();
		
		CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(resource, new SyncerExecutor() {
			@Override
			public void execute() {
				BGArea reloadArea = loadArea(area.getKey());
				if (reloadArea != null) {
					// 1) delete all area - group relations
					deleteBGtoAreaRelations(reloadArea);
					// 2) delete area - assessment mode relations
					deleteAssessmentModeToAreaRelations(reloadArea);
					// 3) delete area itself
					dbInstance.deleteObject(reloadArea);
					log.info(Tracing.M_AUDIT, "Deleted Business Group Area {}", reloadArea);
				} else {
					log.info(Tracing.M_AUDIT, "Business Group Area was already deleted {}", area);
				}
			}
		});
	}

	/**
	 * Deletes all business group to area relations from the given business group
	 * 
	 * @param group
	 */
	@Override
	public void deleteBGtoAreaRelations(BusinessGroup group) {
		StringBuilder sb = new StringBuilder();
		sb.append(" delete from ").append(BGtoAreaRelationImpl.class.getName()).append(" as bgarel where bgarel.businessGroup.key=:groupKey");
		
		dbInstance.getCurrentEntityManager().createQuery(sb.toString())
				.setParameter("groupKey", group.getKey())
				.executeUpdate();
	}
	
	@Override
	public void addBGToBGArea(BusinessGroup group, BGArea area) {
		BGtoAreaRelation bgAreaRel = new BGtoAreaRelationImpl(area, group);
		dbInstance.getCurrentEntityManager().persist(bgAreaRel);
	}

	@Override
	public void removeBGFromArea(BusinessGroup group, BGArea area) {
		removeBGFromArea(group.getKey(), area.getKey());
	}
	
	@Override
	public void removeBGFromAreas(BusinessGroup group, OLATResource resource) {
		StringBuilder sb = new StringBuilder();
		sb.append("select bgarel from ").append(BGtoAreaRelationImpl.class.getName()).append(" as bgarel ")
		  .append(" inner join bgarel.groupArea as area")
		  .append(" where bgarel.businessGroup.key=:businessGroupKey and area.resource.key=:resourceKey");

		List<BGtoAreaRelationImpl> relations = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), BGtoAreaRelationImpl.class)
				.setParameter("businessGroupKey", group.getKey())
				.setParameter("resourceKey", resource.getKey())
				.getResultList();
		for(BGtoAreaRelationImpl relation:relations) {
			dbInstance.getCurrentEntityManager().remove(relation);
		}
	}

	@Override
	public List<BusinessGroup> findBusinessGroupsOfArea(BGArea area) {
		return findBusinessGroupsOfAreas(Collections.singletonList(area));
	}

	@Override
	public List<BusinessGroup> findBusinessGroupsOfAreas(List<BGArea> areas) {
		if(areas == null || areas.isEmpty()) return Collections.emptyList();
		List<Long> areaKeys = new ArrayList<>();
		for(BGArea area:areas) {
			areaKeys.add(area.getKey());
		}
		return findBusinessGroupsOfAreaKeys(areaKeys);
	}
	
	@Override
	public List<BusinessGroup> findBusinessGroupsOfAreaKeys(List<Long> areaKeys) {
		if(areaKeys == null || areaKeys.isEmpty()) return Collections.emptyList();
		
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct businessGroup from ").append(BGtoAreaRelationImpl.class.getName()).append(" as bgarel ")
		  .append(" inner join bgarel.businessGroup businessGroup ")
		  .append(" left join fetch businessGroup.resource resource")
		  .append(" where  bgarel.groupArea.key in (:areaKeys)");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), BusinessGroup.class)
				.setParameter("areaKeys", areaKeys)
				.getResultList();
	}

	@Override
	public List<Long> findBusinessGroupKeysOfAreaKeys(List<Long> areaKeys) {
		if(areaKeys == null || areaKeys.isEmpty()) return Collections.emptyList();
		
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct businessGroup.key from ").append(BGtoAreaRelationImpl.class.getName()).append(" as bgarel ")
		  .append(" inner join bgarel.businessGroup businessGroup ")
		  .append(" where  bgarel.groupArea.key in (:areaKeys)");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("areaKeys", areaKeys)
				.getResultList();
	}

	@Override
	public List<BusinessGroup> findBusinessGroupsOfAreaAttendedBy(Identity identity, List<Long> areaKeys, OLATResource resource) {
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct grp from ").append(BGtoAreaRelationImpl.class.getName()).append(" as bgarel")
		  .append(" inner join bgarel.businessGroup as grp")
		  .append(" inner join bgarel.groupArea as area")
		  .append(" inner join grp.baseGroup as baseGroup")
		  .append(" inner join baseGroup.members as membership")
		  .append(" where membership.identity.key=:identityKey and membership.role='").append(GroupRoles.participant.name()).append("'");
		if(areaKeys != null && !areaKeys.isEmpty()) {
			sb.append(" and area.key in (:areaKeys)");
		}
		if(resource != null) {
			sb.append(" and area.resource.key=:resourceKey");
		}

		TypedQuery<BusinessGroup> query = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), BusinessGroup.class)
				.setParameter("identityKey", identity.getKey());
		if(areaKeys != null && !areaKeys.isEmpty()) {
			query.setParameter("areaKeys", areaKeys);
		}
		if(resource != null) {
			query.setParameter("resourceKey", resource.getKey());
		}
		
		return query.getResultList();
	}

	@Override
	public List<BGArea> findBGAreasOfBusinessGroup(BusinessGroup group) {
		return findBGAreasOfBusinessGroups(Collections.singletonList(group));
	}

	@Override
	public List<BGArea> findBGAreasOfBusinessGroups(List<BusinessGroup> groups) {
		if(groups == null || groups.isEmpty()) return Collections.emptyList();
		
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct bgarel.groupArea from ").append(BGtoAreaRelationImpl.class.getName()).append(" as bgarel ")
		  .append("where bgarel.businessGroup.key in (:groupKeys)");

		TypedQuery<BGArea> areaQuery = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), BGArea.class);
		List<Long> groupKeys = new ArrayList<>();
		for(BusinessGroup group:groups) {
			groupKeys.add(group.getKey());
		}
		areaQuery.setParameter("groupKeys", groupKeys);
		return areaQuery.getResultList();
	}
	
	@Override
	public int countBGAreasOfBusinessGroups(List<BusinessGroup> groups) {
		if(groups == null || groups.isEmpty()) return 0;
		
		StringBuilder sb = new StringBuilder();
		sb.append("select count(distinct bgarel.groupArea.key) from ").append(BGtoAreaRelationImpl.class.getName()).append(" as bgarel ")
		  .append("where bgarel.businessGroup.key in (:groupKeys)");

		TypedQuery<Number> areaQuery = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Number.class);
		List<Long> groupKeys = new ArrayList<>();
		for(BusinessGroup group:groups) {
			groupKeys.add(group.getKey());
		}
		areaQuery.setParameter("groupKeys", groupKeys);
		return areaQuery.getSingleResult().intValue();
	}

	@Override
	public int countBGAreasInContext(OLATResource resource) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(area) from ").append(BGAreaImpl.class.getName()).append(" area where area.resource.key=:resourceKey");
		Number count = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Number.class)
				.setParameter("resourceKey", resource.getKey())
				.setHint("org.hibernate.cacheable", Boolean.TRUE)
				.getSingleResult();
		return count.intValue();
	}

	@Override
	public List<BGArea> findBGAreasInContext(OLATResource resource) {
		StringBuilder sb = new StringBuilder();
		sb.append("select area from ").append(BGAreaImpl.class.getName()).append(" area where area.resource.key=:resourceKey");
		return dbInstance.getCurrentEntityManager().createQuery(sb.toString(), BGArea.class)
				.setParameter("resourceKey", resource.getKey())
				.getResultList();
	}


	@Override
	public boolean isIdentityInBGArea(Identity identity, String areaName, Long areaKey, OLATResource resource) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(bgarel) from ").append(BGtoAreaRelationImpl.class.getName()).append(" as bgarel")
		  .append(" inner join bgarel.businessGroup as grp")
		  .append(" inner join bgarel.groupArea as area")
		  .append(" inner join grp.baseGroup as baseGroup")
		  .append(" inner join baseGroup.members as membership")
		  .append(" where area.resource.key=:resourceKey ")
		  .append(" and membership.identity.key=:identityKey and membership.role in ('").append(GroupRoles.coach.name()).append("','").append(GroupRoles.participant.name()).append("')");
		if(StringHelper.containsNonWhitespace(areaName)) {
			sb.append(" and area.name=:name ");
		}
		if(areaKey != null) {
			sb.append(" and area.key=:areaKey ");
		}
			
		TypedQuery<Number> query = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Number.class)
			.setParameter("identityKey", identity.getKey())
			.setParameter("resourceKey", resource.getKey());
		if(StringHelper.containsNonWhitespace(areaName)) {
			query.setParameter("name", areaName);
		}
		if(areaKey != null) {
			query.setParameter("areaKey", areaKey);
		}
		Number count = query
				.setHint("org.hibernate.cacheable", Boolean.TRUE)
				.getSingleResult();
		return count.intValue() > 0;
	}

	@Override
	public BGArea reloadArea(BGArea area) {
		return (BGArea) dbInstance.loadObject(area);
	}

	@Override
	public boolean existArea(String nameOrKey, OLATResource resource) {
		Long key = null;
		if(StringHelper.isLong(nameOrKey)) {
			key = Long.valueOf(nameOrKey);
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("select count(area) from ").append(BGAreaImpl.class.getName()).append(" area  ")
		  .append(" where area.resource.key=:resourceKey and area.");
		if(key == null) {
			sb.append("name");
		} else {
			sb.append("key");
		} 
		sb.append("=:nameOrKey");
		
		Number count = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Number.class)
				.setParameter("resourceKey", resource.getKey())
				.setParameter("nameOrKey", key == null ? nameOrKey : key)
				.setHint("org.hibernate.cacheable", Boolean.TRUE)
				.getSingleResult();
		return count.intValue() > 0;
	}
	
	

	@Override
	public List<Long> toAreaKeys(String areaNames, OLATResource resource) {
		if(!StringHelper.containsNonWhitespace(areaNames)) return Collections.emptyList();
		
		String[] areaNameArr = areaNames.split(",");
		List<String> areaNameList = new ArrayList<>();
		for(String areaName:areaNameArr) {
			areaNameList.add(areaName.trim());
		}
		
		if(areaNameList.isEmpty()) return Collections.emptyList();

		StringBuilder sb = new StringBuilder();
		sb.append("select area.key from ").append(BGAreaImpl.class.getName()).append(" area")
		  .append(" where area.resource.key=:resourceKey and area.name in (:names)");

		return dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Long.class)
				.setParameter("resourceKey", resource.getKey())
				.setParameter("names", areaNameList)
				.setHint("org.hibernate.cacheable", Boolean.TRUE)
				.getResultList();
	}

	/**
	 * Creates an area object and persists the object in the database
	 * 
	 * @param areaName The visible area name
	 * @param description The area description
	 * @param resource The resource of this area
	 * @return The new area
	 */
	@Override
	public BGArea createAndPersistBGArea(String areaName, String description, OLATResource resource) {
		BGArea area = new BGAreaImpl(areaName, description, resource);
		dbInstance.getCurrentEntityManager().persist(area);
		log.info(Tracing.M_AUDIT, "Created Business Group Area {}", area);
		// else no area created, name duplicate
		return area;
	}
	
	/**
	 * Remove a business group from a business group area. If no such relation
	 * exists, the method does nothing.
	 * 
	 * @param businessGroupKey
	 * @param bgAreaKey
	 */
	private void removeBGFromArea(Long businessGroupKey, Long bgAreaKey) {
		StringBuilder sb = new StringBuilder();
		sb.append("delete from ").append(BGtoAreaRelationImpl.class.getName()).append(" as bgarel where bgarel.groupArea.key=:areaKey and bgarel.businessGroup.key=:groupKey");
		
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
	
	private void deleteAssessmentModeToAreaRelations(BGArea area) {
		StringBuilder sb = new StringBuilder();
		sb.append("delete from courseassessmentmodetoarea as modearel where modearel.area.key=:areaKey");
		
		dbInstance.getCurrentEntityManager().createQuery(sb.toString())
			.setParameter("areaKey", area.getKey())
			.executeUpdate();
	}
}
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
package org.olat.upgrade;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Roles;
import org.olat.course.CorruptedCourseException;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupImpl;
import org.olat.group.BusinessGroupService;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaImpl;
import org.olat.group.area.BGAreaManager;
import org.olat.group.area.BGtoAreaRelationImpl;
import org.olat.group.context.BGContext2Resource;
import org.olat.group.model.BGAreaReference;
import org.olat.group.model.BGResourceRelation;
import org.olat.group.model.BusinessGroupReference;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.SearchRepositoryEntryParameters;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * upgrade code for OLAT 7.1.0 -> OLAT 7.1.1
 * - fixing invalid structures being built by synchronisation, see OLAT-6316 and OLAT-6306
 * - merges all yet found data to last valid node 
 * 
 * <P>
 * Initial Date: 24.03.2011 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, www.frentix.com
 */
public class OLATUpgrade_8_2_0 extends OLATUpgrade {

	private static final String TASK_CONTEXTS = "Upgrade contexts";
	private static final String TASK_AREAS = "Upgrade areas";
	private static final String TASK_CONDITIONS = "Upgrade conditions";
	private static final int REPO_ENTRIES_BATCH_SIZE = 20;
	private static final String VERSION = "OLAT_8.2.0";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private BGAreaManager areaManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private OLATResourceManager resourceManager;
	@Autowired
	private RepositoryManager repositoryManager;

	public OLATUpgrade_8_2_0() {
		super();
	}

	@Override
	public String getVersion() {
		return VERSION;
	}

	@Override
	public boolean doPreSystemInitUpgrade(UpgradeManager upgradeManager) {
		return false;
	}

	@Override
	public boolean doPostSystemInitUpgrade(UpgradeManager upgradeManager) {
		UpgradeHistoryData uhd = upgradeManager.getUpgradesHistory(VERSION);
		if (uhd == null) {
			// has never been called, initialize
			uhd = new UpgradeHistoryData();
		} else {
			if (uhd.isInstallationComplete()) {
				return false;
			}
		}
		
		boolean allOk = upgradeGroups(upgradeManager, uhd);
		allOk &= upgradeAreas(upgradeManager, uhd);
		allOk &= upgradeCourseConditions(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.audit("Finished OLATUpgrade_8_2_0 successfully!");
		} else {
			log.audit("OLATUpgrade_8_2_0 not finished, try to restart OpenOLAT!");
		}
		return allOk;
	}
	
	private boolean upgradeGroups(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		if (!uhd.getBooleanDataValue(TASK_CONTEXTS)) {
			int counter = 0;
			List<BusinessGroup> groups;
			do {
				groups = findBusinessGroups(counter, REPO_ENTRIES_BATCH_SIZE);
				for(BusinessGroup group:groups) {
					processBusinessGroup(group);
				}
				counter += groups.size();
				log.audit("Processed context: " + groups.size());
			} while(groups.size() == REPO_ENTRIES_BATCH_SIZE);
			
			uhd.setBooleanDataValue(TASK_CONTEXTS, true);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return true;
	}
	
	private boolean upgradeAreas(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		if (!uhd.getBooleanDataValue(TASK_AREAS)) {
			int counter = 0;
			List<BGAreaImpl> areas;
			do {
				areas = findAreas(counter, REPO_ENTRIES_BATCH_SIZE);
				for(BGAreaImpl area:areas) {
					processArea(area);
				}
				counter += areas.size();
				log.audit("Processed areas: " + areas.size());
			} while(areas.size() == REPO_ENTRIES_BATCH_SIZE);
			
			uhd.setBooleanDataValue(TASK_AREAS, true);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return true;
	}
	
	private boolean upgradeCourseConditions(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		if (!uhd.getBooleanDataValue(TASK_CONDITIONS)) {
			int counter = 0;
			List<RepositoryEntry> entries;
			SearchRepositoryEntryParameters params = new SearchRepositoryEntryParameters();
			params.setRoles(new Roles(true, false, false, false, false, false, false));
			params.addResourceTypes("CourseModule");
			do {
				entries = repositoryManager.genericANDQueryWithRolesRestriction(params, counter, REPO_ENTRIES_BATCH_SIZE, true);
				for(RepositoryEntry entry:entries) {
					try {
						ICourse course = CourseFactory.loadCourse(entry.getOlatResource());
						CourseEnvironmentMapper envMapper = getCourseEnvironmentMapper(entry.getOlatResource());
						course.postImport(envMapper);
					} catch (CorruptedCourseException e) {
						log.error("Course seems corrupt: " + entry.getOlatResource().getResourceableId());
					}
				}
				counter += entries.size();
				log.audit("Processed repository entries: " + entries.size());
			} while(entries.size() == REPO_ENTRIES_BATCH_SIZE);
			
			uhd.setBooleanDataValue(TASK_CONDITIONS, false);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return true;
	}
	
	private CourseEnvironmentMapper getCourseEnvironmentMapper(OLATResource courseResource) {
		CourseEnvironmentMapper envMapper = new CourseEnvironmentMapper();
		List<BusinessGroup> groups = businessGroupService.findBusinessGroups(null, courseResource, 0, -1);
		for(BusinessGroup group:groups) {
			envMapper.getGroups().add(new BusinessGroupReference(group));
		}
		List<BGArea> areas = areaManager.findBGAreasInContext(courseResource);
		for(BGArea area:areas) {
			envMapper.getAreas().add(new BGAreaReference(area));
		}
		return envMapper;
	}
	
	private void processBusinessGroup(BusinessGroup group) {
		List<OLATResource> resources = findOLATResourcesForBusinessGroup(group);
		List<OLATResource> currentList = businessGroupService.findResources(Collections.singletonList(group), 0, -1);
		
		boolean merge = false;
		if(group.getResource() == null) {
			OLATResource resource = resourceManager.findResourceable(group);
			((BusinessGroupImpl)group).setResource(resource);
			merge = true;
		}
		if(group.getOwnerGroup() == null) {
			((BusinessGroupImpl)group).setOwnerGroup(securityManager.createAndPersistSecurityGroup());
			merge = true;
		}
		if(group.getPartipiciantGroup() == null) {
			((BusinessGroupImpl)group).setPartipiciantGroup(securityManager.createAndPersistSecurityGroup());
			merge = true;
		}
		if(group.getWaitingGroup() == null) {
			group.setWaitingGroup(securityManager.createAndPersistSecurityGroup());
			merge = true;
		}
		if(merge) {
			group = dbInstance.getCurrentEntityManager().merge(group);
		}

		int count = 0;
		for	(OLATResource resource:resources) {
			if(!currentList.contains(resource)) {
				businessGroupService.addResourceTo(group, resource);
				count++;
			}
		}
		dbInstance.commitAndCloseSession();
		log.audit("Processed: " + group.getName() + " add " + count + " resources");
	}
	
	private void processArea(BGAreaImpl area) {
		if(area.getResource() != null) {
			//already migrated
			return;
		}
		Long groupContextKey = area.getGroupContextKey();
		List<OLATResource> resources = findOLATResourcesForBGContext(groupContextKey);
		if(resources.isEmpty()) {
			//nothing to do
		} else {
			//reuse the area for the first resource
			Iterator<OLATResource> resourcesIt = resources.iterator();
			OLATResource firstResource = resourcesIt.next();
			area.setResource(firstResource);
			dbInstance.getCurrentEntityManager().merge(area);
			List<Long> firstResourcesGroupKeys = findBusinessGroupsOfResource(firstResource);
			List<BusinessGroup> originalGroupList = findBusinessGroupsOfArea(area);
			//remove the groups which aren't part of the first resource
			for(BusinessGroup group:originalGroupList) {
				if(!firstResourcesGroupKeys.contains(group.getKey())) {
					areaManager.removeBGFromArea(group, area);
				}
			}
			
			//duplicate the areas for the next resources
			if(resourcesIt.hasNext()) {
				for( ;resourcesIt.hasNext(); ) {
					OLATResource resource = resourcesIt.next();
					List<Long> resourcesGroupKeys = findBusinessGroupsOfResource(resource);
					BGArea existingArea = areaManager.findBGArea(area.getName(), resource);
					if(existingArea == null) {
						BGArea copyArea = areaManager.createAndPersistBGArea(area.getName(), area.getDescription(), resource);
						for(BusinessGroup group:originalGroupList) {
							if(resourcesGroupKeys.contains(group.getKey())) {
								areaManager.addBGToBGArea(group, copyArea);
							}
						}
					}
				}
			}
		}
		dbInstance.commitAndCloseSession();
	}
	
	private List<OLATResource> findOLATResourcesForBGContext(Long contextKey) {
		StringBuilder q = new StringBuilder();
		q.append("select bgcr.resource from ").append(BGContext2Resource.class.getName()).append(" as bgcr where bgcr.groupContext.key=:contextKey");
		
		List<OLATResource> resources = dbInstance.getCurrentEntityManager()
				.createQuery(q.toString(), OLATResource.class)
				.setParameter("contextKey", contextKey)
				.getResultList();
		return resources;
	}
	
	private List<Long> findBusinessGroupsOfResource(OLATResource resource) {
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct relation.group.key from ").append(BGResourceRelation.class.getName()).append(" relation where relation.resource.key=:resourceKey");

		List<Long> groups = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("resourceKey", resource.getKey())
				.getResultList();
		return groups;
	}
	
	private List<BusinessGroup> findBusinessGroupsOfArea(BGArea area) {
		StringBuilder q = new StringBuilder();
		q.append("select bgarel.businessGroup from ").append(BGtoAreaRelationImpl.class.getName()).append(" as bgarel ")
		 .append(" where bgarel.groupArea.key=:areaKey");
		
		List<BusinessGroup> groups = dbInstance.getCurrentEntityManager()
				.createQuery(q.toString(), BusinessGroup.class)
				.setParameter("areaKey", area.getKey())
				.getResultList();
		return groups;
	}
	
	private List<BGAreaImpl> findAreas(int firstResult, int maxResults) {
		StringBuilder q = new StringBuilder();
		q.append("select area from ").append(BGAreaImpl.class.getName()).append(" area ")
		 .append(" left join fetch area.resource resource")
		 .append(" order by area.key");

		List<BGAreaImpl> resources = dbInstance.getCurrentEntityManager().createQuery(q.toString(), BGAreaImpl.class)
				.setFirstResult(firstResult)
				.setMaxResults(maxResults)
				.getResultList();
		return resources;
	}
	
	private List<BusinessGroup> findBusinessGroups(int firstResult, int maxResults) {
		StringBuilder q = new StringBuilder();
		q.append("select bg from ").append(BusinessGroupImpl.class.getName()).append(" bg ")
		 .append(" left join fetch bg.ownerGroup onwerGroup")
		 .append(" left join fetch bg.partipiciantGroup participantGroup")
		 .append(" left join fetch bg.waitingGroup waitingGroup")
		 .append(" order by bg.key");

		List<BusinessGroup> resources = dbInstance.getCurrentEntityManager().createQuery(q.toString(), BusinessGroup.class)
				.setFirstResult(firstResult)
				.setMaxResults(maxResults)
				.getResultList();
		return resources;
	}
	
	private List<OLATResource> findOLATResourcesForBusinessGroup(BusinessGroup group) {
		StringBuilder q = new StringBuilder();
		q.append("select bgcr.resource from ").append(BGContext2Resource.class.getName()).append(" as bgcr where bgcr.groupContext.key=:contextKey");

		List<OLATResource> resources = dbInstance.getCurrentEntityManager().createQuery(q.toString(), OLATResource.class)
				.setParameter("contextKey", ((BusinessGroupImpl)group).getGroupContextKey())
				.getResultList();
		return resources;
	}
}

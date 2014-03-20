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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.Policy;
import org.olat.basesecurity.SecurityGroup;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.group.right.BGRightManager;
import org.olat.group.right.BGRightsRole;
import org.olat.repository.RepositoryManager;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.resource.OLATResource;
import org.olat.upgrade.model.BGResourceRelation;
import org.olat.upgrade.model.BusinessGroupUpgrade;
import org.olat.upgrade.model.EPMapUpgrade;
import org.olat.upgrade.model.RepositoryEntryUpgrade;
import org.olat.upgrade.model.RepositoryEntryUpgradeToGroupRelation;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 27.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_10_0_0 extends OLATUpgrade {
	
	private static final int BATCH_SIZE = 50;
	private static final String TASK_BUSINESS_GROUPS = "Upgrade business groups";
	private static final String TASK_REPOENTRIES = "Upgrade repository entries";
	private static final String TASK_REPOENTRY_TO_BUSINESSGROUP = "Upgrade relation business groups to repository entries";
	private static final String TASK_UPGRADE_MAP = "Upgrade maps";
	private static final String VERSION = "OLAT_10.0.0";

	@Autowired
	private DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private GroupDAO groupDao;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private BGRightManager bgRightManager;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryToGroupDAO;
	
	public OLATUpgrade_10_0_0() {
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
		} else if (uhd.isInstallationComplete()) {
			return false;
		}
		
		boolean allOk = true;
		allOk &= upgradeBusinessGroups(upgradeManager, uhd);
		allOk &= upgradeRepositoryEntries(upgradeManager, uhd);
		allOk &= upgradeRelationsRepoToBusinessGroups(upgradeManager, uhd);
		allOk &= upgradeEPMap(upgradeManager, uhd);
		
		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.audit("Finished OLATUpgrade_10_0_0 successfully!");
		} else {
			log.audit("OLATUpgrade_10_0_0 not finished, try to restart OpenOLAT!");
		}
		return allOk;
	}
	
	private boolean upgradeBusinessGroups(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		if (!uhd.getBooleanDataValue(TASK_BUSINESS_GROUPS)) {
			int counter = 0;
			List<BusinessGroupUpgrade> businessGroups;
			do {
				businessGroups = findBusinessGroups(counter, BATCH_SIZE);
				for(BusinessGroupUpgrade businessGroup:businessGroups) {
					BusinessGroupUpgrade up = processBusinessGroup(businessGroup);
					processRightGroup(up); 
				}
				counter += businessGroups.size();
				log.audit("Business groups processed: " + businessGroups.size() + ", total processed (" + counter + ")");
				dbInstance.commitAndCloseSession();
			} while(businessGroups.size() == BATCH_SIZE);
			uhd.setBooleanDataValue(TASK_BUSINESS_GROUPS, true);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return true;
	}
	
	private BusinessGroupUpgrade processBusinessGroup(BusinessGroupUpgrade businessGroup) {
		Group baseGroup = businessGroup.getBaseGroup();
		if(baseGroup != null && baseGroup.getKey() != null) {
			return businessGroup;
		}

		Group group = groupDao.createGroup();
		//update tutors
		processSecurityGroup(group, GroupRoles.coach.name(), businessGroup.getOwnerGroup());
		//update participants
		processSecurityGroup(group, GroupRoles.participant.name(), businessGroup.getPartipiciantGroup());
		//update waiting
		processSecurityGroup(group, GroupRoles.waiting.name(), businessGroup.getWaitingGroup());
		
		dbInstance.commit();
		
		businessGroup.setBaseGroup(group);
		businessGroup = dbInstance.getCurrentEntityManager().merge(businessGroup);
		
		dbInstance.commit();
		return businessGroup;
	}

	private void processRightGroup(BusinessGroupUpgrade businessGroup) {
		boolean commit = false;
		
		List<String> tutorRights = findBGRights(businessGroup.getOwnerGroup());
		for(String right:tutorRights) {
			bgRightManager.addBGRight(right, businessGroup, BGRightsRole.tutor);
			commit = true;
		}
		
		List<String> participantsRights = findBGRights(businessGroup.getPartipiciantGroup());
		for(String right:participantsRights) {
			bgRightManager.addBGRight(right, businessGroup, BGRightsRole.participant);
			commit = true;
		}
		
		if(commit) {
			dbInstance.commit();
		}
	}
	
	private List<String> findBGRights(SecurityGroup secGroup) {
		List<Policy> results = securityManager.getPoliciesOfSecurityGroup(secGroup);
		// filter all business group rights permissions. group right permissions
		// start with bgr.
		List<String> rights = new ArrayList<String>();
		for (Policy rightPolicy:results) {
			String right = rightPolicy.getPermission();
			if (right.indexOf("bgr.") == 0) rights.add(right);
		}
		return rights;
	}
	
	private boolean upgradeRepositoryEntries(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		if (!uhd.getBooleanDataValue(TASK_REPOENTRIES)) {
			int counter = 0;
			List<RepositoryEntryUpgrade> repoEntries;
			do {
				repoEntries = findRepositoryEntries(counter, BATCH_SIZE);
				for(RepositoryEntryUpgrade repoEntry:repoEntries) {
					processRepositoryEntry(repoEntry);
				}
				counter += repoEntries.size();
				log.audit("Repository entries processed: " + repoEntries.size() + ", total processed (" + counter + ")");
				dbInstance.commitAndCloseSession();
			} while(repoEntries.size() == BATCH_SIZE);
			uhd.setBooleanDataValue(TASK_REPOENTRIES, true);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return true;
	}
	
	private void processRepositoryEntry(RepositoryEntryUpgrade repoEntry) {
		if(isDefaultGroupOk(repoEntry)) return;
		
		Group group = groupDao.createGroup();
		//update owners
		processSecurityGroup(group, GroupRoles.owner.name(), repoEntry.getOwnerGroup());
		//update tutors
		processSecurityGroup(group, GroupRoles.coach.name(), repoEntry.getTutorGroup());
		//update participants
		processSecurityGroup(group, GroupRoles.participant.name(), repoEntry.getParticipantGroup());

		dbInstance.commit();
		
		RepositoryEntryUpgradeToGroupRelation relation = create(repoEntry, group, true);
		Set<RepositoryEntryUpgradeToGroupRelation> relations = new HashSet<>(2);
		relations.add(relation);
		repoEntry.setGroups(relations);
		dbInstance.commit();
	}
	
	public RepositoryEntryUpgradeToGroupRelation create(RepositoryEntryUpgrade entry, Group group, boolean defaultRelation) {
		RepositoryEntryUpgradeToGroupRelation rel = new RepositoryEntryUpgradeToGroupRelation();
		rel.setCreationDate(new Date());
		rel.setDefaultGroup(defaultRelation);
		rel.setGroup(group);
		rel.setEntry(entry);
		dbInstance.getCurrentEntityManager().persist(rel);
		return rel;
	}
	
	private boolean isDefaultGroupOk(RepositoryEntryUpgrade repoEntry) {
		if(repoEntry.getGroups() == null || repoEntry.getGroups().isEmpty()) {
			return false;
		}
		for(RepositoryEntryUpgradeToGroupRelation rel:repoEntry.getGroups()) {
			if(rel.isDefaultGroup()) {
				return true;
			}
		}
		return false;
	}
	
	private boolean upgradeRelationsRepoToBusinessGroups(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		if (!uhd.getBooleanDataValue(TASK_REPOENTRY_TO_BUSINESSGROUP)) {
			int counter = 0;
			List<BusinessGroupUpgrade> businessGroups;
			do {
				businessGroups = findBusinessGroups(counter, BATCH_SIZE);
				for(BusinessGroupUpgrade businessGroup:businessGroups) {
					processRelationToRepo(businessGroup);
				}
				counter += businessGroups.size();
				log.audit("Business groups relations processed: " + businessGroups.size() + ", total processed (" + counter + ")");
				dbInstance.commitAndCloseSession();
			} while(businessGroups.size() == BATCH_SIZE);
			uhd.setBooleanDataValue(TASK_REPOENTRY_TO_BUSINESSGROUP, true);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return true;
	}
	
	private void processRelationToRepo(BusinessGroupUpgrade businessGroup) {
		try {
			List<BGResourceRelation> relationsToRepo = findRelations(businessGroup);
			
			if(relationsToRepo.size() > 0) {
				Group refGroup = businessGroup.getBaseGroup();
				for(BGResourceRelation relationToRepo:relationsToRepo) {
					
					RepositoryEntryUpgrade entry = lookupRepositoryEntry(relationToRepo.getResource());
					Set<RepositoryEntryUpgradeToGroupRelation> groupRelations = entry.getGroups();
					
					boolean found = false;
					for(RepositoryEntryUpgradeToGroupRelation groupRelation:groupRelations) {
						if(groupRelation.getGroup().equals(refGroup)) {
							found = true;
						}
					}
					
					if(!found) {
						create(entry, refGroup, false);
					}
				}
			}
			dbInstance.commit();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	private List<BGResourceRelation> findRelations(BusinessGroupUpgrade group) {
		StringBuilder sb = new StringBuilder();
		sb.append("select rel from ").append(BGResourceRelation.class.getName()).append(" as rel ")
			.append(" where rel.group.key=:groupKey");

		return dbInstance.getCurrentEntityManager().createQuery(sb.toString(), BGResourceRelation.class)
				.setParameter("groupKey", group.getKey())
				.getResultList();
	}
	
	private boolean upgradeEPMap(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		if (!uhd.getBooleanDataValue(TASK_UPGRADE_MAP)) {
			int counter = 0;
			List<EPMapUpgrade> businessGroups;
			do {
				businessGroups = findMaps(counter, BATCH_SIZE);
				for(EPMapUpgrade businessGroup:businessGroups) {
					processMap(businessGroup);
				}
				counter += businessGroups.size();
				log.audit("Maps processed: " + businessGroups.size() + ", total processed (" + counter + ")");
				dbInstance.commitAndCloseSession();
			} while(businessGroups.size() == BATCH_SIZE);
			uhd.setBooleanDataValue(TASK_UPGRADE_MAP, true);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return true;
	}
	
	private void processMap(EPMapUpgrade map) {
		if(map.getGroup() != null) return;
		
		SecurityGroup ownerGroup = map.getOwnerGroup();
		if(ownerGroup != null) {
			RepositoryEntryUpgrade re = findMapRepoEntry(ownerGroup);
			if(re != null) {
				Group reGroup = repositoryEntryToGroupDAO.getDefaultGroup(re);
				if(reGroup != null) {
					map.setGroup(reGroup);
				}
			}
			if(map.getGroup() == null) {
				Group group = groupDao.createGroup();
				map.setGroup(group);
				processSecurityGroup(group, GroupRoles.owner.name(), ownerGroup);
			}
			dbInstance.getCurrentEntityManager().merge(map);
		}
	}
	
	private RepositoryEntryUpgrade findMapRepoEntry(SecurityGroup ownerGroup) {
		StringBuilder sb = new StringBuilder();	
		sb.append("select v from ").append(RepositoryEntryUpgrade.class.getName()).append(" as v")
		  .append(" where v.ownerGroup=:ownerGroup");
		List<RepositoryEntryUpgrade> res = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntryUpgrade.class)
				.setParameter("ownerGroup", ownerGroup)
				.getResultList();
		if(res.size() > 0) {
			return res.get(0);
		}
		return null;
	}
	
	private List<EPMapUpgrade> findMaps(int firstResult, int maxResults) {
		StringBuilder sb = new StringBuilder();	
		sb.append("select map from ").append(EPMapUpgrade.class.getName()).append(" map")
		  .append(" left join fetch map.group as baseGroup")
		  .append(" left join fetch map.ownerGroup as ownerGroup")
		  .append(" where map.group is null and map.ownerGroup is not null")
		  .append(" order by map.key");
		return dbInstance.getCurrentEntityManager().createQuery(sb.toString(), EPMapUpgrade.class)
				.setFirstResult(firstResult)
				.setMaxResults(maxResults)
				.getResultList();
	}

	private void processSecurityGroup(Group group, String role, SecurityGroup secGroup) {
		if(secGroup == null) return;

		List<Identity> identities = securityManager.getIdentitiesOfSecurityGroup(secGroup);
		for(Identity identity:identities) {
			groupDao.addMembership(group, identity, role);
		}	
	}
	
	private List<BusinessGroupUpgrade> findBusinessGroups(int firstResult, int maxResults) {
		StringBuilder sb = new StringBuilder();	
		sb.append("select businessgroup from ").append(BusinessGroupUpgrade.class.getName()).append(" businessgroup")
		  .append(" left join fetch businessgroup.baseGroup as baseGroup")
		  .append(" left join fetch businessgroup.ownerGroup as ownerGroup")
		  .append(" left join fetch businessgroup.partipiciantGroup as partipiciantGroup")
		  .append(" left join fetch businessgroup.waitingGroup as waitingGroup")
		  .append(" left join fetch businessgroup.resource as resource")
		  .append(" order by businessgroup.key");
		return dbInstance.getCurrentEntityManager().createQuery(sb.toString(), BusinessGroupUpgrade.class)
				.setFirstResult(firstResult)
				.setMaxResults(maxResults)
				.getResultList();
	}
	
	private List<RepositoryEntryUpgrade> findRepositoryEntries(int firstResult, int maxResults) {
		StringBuilder sb = new StringBuilder();	
		sb.append("select v from ").append(RepositoryEntryUpgrade.class.getName()).append(" v")
		  .append(" inner join fetch v.olatResource as ores")
		  .append(" left join fetch v.ownerGroup as ownerGroup")
		  .append(" left join fetch v.participantGroup as participantGroup")
		  .append(" left join fetch v.tutorGroup as tutorGroup")
		  .append(" order by v.key");
		return dbInstance.getCurrentEntityManager().createQuery(sb.toString(), RepositoryEntryUpgrade.class)
				.setFirstResult(firstResult)
				.setMaxResults(maxResults)
				.getResultList();
	}
	
	private RepositoryEntryUpgrade lookupRepositoryEntry(OLATResource ores) {
		StringBuilder sb = new StringBuilder();
		sb.append("select v from ").append(RepositoryEntryUpgrade.class.getName()).append(" v ")
		  .append(" inner join fetch v.olatResource as ores")
		  .append(" left join fetch v.ownerGroup as ownerGroup")
		  .append(" left join fetch v.participantGroup as participantGroup")
		  .append(" left join fetch v.tutorGroup as tutorGroup")
		  .append(" where ores.key = :oreskey");

		List<RepositoryEntryUpgrade> result = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntryUpgrade.class)
				.setParameter("oreskey", ores.getKey())
				.getResultList();
		if(result.size() > 0) {
			return result.get(0);
		}
		return null;
	}
}

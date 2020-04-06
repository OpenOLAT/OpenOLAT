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

import java.io.File;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.olat.admin.layout.LayoutModule;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupMembership;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.SecurityGroup;
import org.olat.basesecurity.SecurityGroupMembershipImpl;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.basesecurity.model.GroupImpl;
import org.olat.basesecurity.model.GroupMembershipImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.properties.Property;
import org.olat.properties.PropertyManager;
import org.olat.resource.OLATResource;
import org.olat.upgrade.model.BGResourceRelation;
import org.olat.upgrade.model.BusinessGroupUpgrade;
import org.olat.upgrade.model.InvitationUpgrade;
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
	
	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_10_0_0.class);

	private static final int BATCH_SIZE = 50;
	private static final String TASK_BUSINESS_GROUPS = "Upgrade business groups";
	private static final String TASK_REPOENTRIES = "Upgrade repository entries";
	private static final String TASK_REPOENTRY_TO_BUSINESSGROUP = "Upgrade relation business groups to repository entries";
	private static final String TASK_INVITATION = "Upgrade invitations";
	private static final String TASK_LOGO = "Upgrade custom logo";
	private static final String VERSION = "OLAT_10.0.0";
	

	private static final String PROPERTY_CATEGORY = "_o3_";
	private static final String PNAME_LOGOURI = "customizing.img.uri";
	private static final String PNAME_LOGOALT = "customizing.img.alt";
	private static final String PNAME_LINKURI = "customizing.link.uri";
	private static final String PNAME_FOOTERLINE = "customizing.footer.text";

	@Autowired
	private DB dbInstance;
	@Autowired
	private GroupDAO groupDao;
	@Autowired
	private PropertyManager propertyManager;
	@Autowired
	private LayoutModule layoutModule;
	
	public OLATUpgrade_10_0_0() {
		super();
	}

	@Override
	public String getVersion() {
		return VERSION;
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
		allOk &= upgradeLogo(upgradeManager, uhd);
		allOk &= upgradeBusinessGroups(upgradeManager, uhd);
		allOk &= upgradeRepositoryEntries(upgradeManager, uhd);
		allOk &= upgradeRelationsRepoToBusinessGroups(upgradeManager, uhd);
		allOk &= upgradeInvitation(upgradeManager, uhd);
		
		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_10_0_0 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_10_0_0 not finished, try to restart OpenOLAT!");
		}
		return allOk;
	}
	
	private boolean upgradeLogo(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		if (!uhd.getBooleanDataValue(TASK_LOGO)) {
			try {
				Property pLogoUri = propertyManager.findProperty(null, null, null, PROPERTY_CATEGORY, PNAME_LOGOURI);
				if(pLogoUri != null && StringHelper.containsNonWhitespace(pLogoUri.getStringValue())) {
					String filename = pLogoUri.getStringValue();
					layoutModule.setLogoFilename(filename);
					
					File currentFile = Paths.get(WebappHelper.getUserDataRoot(), "system", "logo", filename).toFile();
					if(currentFile.exists()) {
						File target = Paths.get(WebappHelper.getUserDataRoot(), "customizing", "logo", filename).toFile();
						FileUtils.copyFile(currentFile, target);
					}	
				}
				Property pLogoAlt = propertyManager.findProperty(null, null, null, PROPERTY_CATEGORY, PNAME_LOGOALT);
				if(pLogoAlt != null && StringHelper.containsNonWhitespace(pLogoAlt.getStringValue())) {
					layoutModule.setLogoAlt(pLogoAlt.getStringValue());
				}
				Property pLinkUri = propertyManager.findProperty(null, null, null, PROPERTY_CATEGORY, PNAME_LINKURI);
				if(pLinkUri != null && StringHelper.containsNonWhitespace(pLinkUri.getStringValue())) {
					layoutModule.setLogoLinkUri(pLinkUri.getStringValue());
				}
				Property pFooterLine = propertyManager.findProperty(null, null, null, PROPERTY_CATEGORY, PNAME_FOOTERLINE);
				if(pFooterLine != null && StringHelper.containsNonWhitespace(pFooterLine.getTextValue())) {
					layoutModule.setFooterLine(pFooterLine.getTextValue());
				}
				
				
				
				uhd.setBooleanDataValue(TASK_LOGO, true);
				upgradeManager.setUpgradesHistory(uhd, VERSION);
			} catch (Exception e) {
				log.error("", e);
				return false;
			}
		}
		return true;
	}
	
	private boolean upgradeBusinessGroups(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		if (!uhd.getBooleanDataValue(TASK_BUSINESS_GROUPS)) {
			int counter = 0;
			List<BusinessGroupUpgrade> businessGroups;
			do {
				businessGroups = findBusinessGroups(counter, BATCH_SIZE);
				for(BusinessGroupUpgrade businessGroup:businessGroups) {
					processBusinessGroup(businessGroup); 
				}
				counter += businessGroups.size();
				log.info(Tracing.M_AUDIT, "Business groups processed: " + businessGroups.size() + ", total processed (" + counter + ")");
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
				log.info(Tracing.M_AUDIT, "Repository entries processed: " + repoEntries.size() + ", total processed (" + counter + ")");
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
				log.info(Tracing.M_AUDIT, "Business groups relations processed: " + businessGroups.size() + ", total processed (" + counter + ")");
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
					if(entry == null) {
						continue;
					}
					
					boolean found = false;
					Set<RepositoryEntryUpgradeToGroupRelation> groupRelations = entry.getGroups();
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
			log.error("", e);
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
	
	private boolean upgradeInvitation(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		if (!uhd.getBooleanDataValue(TASK_INVITATION)) {
			int counter = 0;
			List<InvitationUpgrade> invitations;
			do {
				invitations = findInvitations(counter, BATCH_SIZE);
				for(InvitationUpgrade invitation:invitations) {
					if(invitation.getBaseGroup() == null) {
						processInvitation(invitation);
					}
				}
				counter += invitations.size();
				log.info(Tracing.M_AUDIT, "Invitations processed: " + invitations.size() + ", total processed (" + counter + ")");
				dbInstance.commitAndCloseSession();
			} while(invitations.size() == BATCH_SIZE);
			uhd.setBooleanDataValue(TASK_INVITATION, true);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return true;
	}
	
	private List<InvitationUpgrade> findInvitations(int firstResult, int maxResult) {
		String sb = "select invitation from invitationupgrade as invitation order by invitation.key";
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb, InvitationUpgrade.class)
				.setFirstResult(firstResult)
				.setMaxResults(maxResult)
				.getResultList();
	}
	
	private void processInvitation(InvitationUpgrade invitation) {
		if(invitation.getBaseGroup() == null) {
			Group invitationGroup = groupDao.createGroup();
			invitation.setBaseGroup(invitationGroup);
			dbInstance.getCurrentEntityManager().merge(invitation);
		}
	}

	private void processSecurityGroup(Group group, String role, SecurityGroup secGroup) {
		if(secGroup == null) return;

		List<SecurityGroupMembershipImpl> oldMemberships = getMembershipsOfSecurityGroup(secGroup);
		for(SecurityGroupMembershipImpl oldMembership:oldMemberships) {
			GroupMembershipImpl membership = new GroupMembershipImpl();
			membership.setCreationDate(oldMembership.getCreationDate());
			membership.setLastModified(oldMembership.getLastModified());
			membership.setGroup(group);
			membership.setIdentity(oldMembership.getIdentity());
			membership.setRole(role);
			dbInstance.getCurrentEntityManager().persist(membership);

			Set<GroupMembership> members = ((GroupImpl)group).getMembers();
			if(members == null) {
				members = new HashSet<>();
				((GroupImpl)group).setMembers(members);
			}
			members.add(membership);
		}	
	}
	
	private List<SecurityGroupMembershipImpl> getMembershipsOfSecurityGroup(SecurityGroup secGroup) {
		StringBuilder sb = new StringBuilder();
		sb.append("select membership from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as membership")
		  .append("  where membership.securityGroup=:secGroup");
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), SecurityGroupMembershipImpl.class)
			.setParameter("secGroup", secGroup)
			.getResultList();
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

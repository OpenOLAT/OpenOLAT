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

import java.util.List;

import org.olat.basesecurity.GroupMembershipInheritance;
import org.olat.basesecurity.IdentityImpl;
import org.olat.basesecurity.NamedGroupImpl;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.SecurityGroupMembershipImpl;
import org.olat.basesecurity.manager.OrganisationDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 16 mars 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_13_0_0 extends OLATUpgrade {
	
	private static final String VERSION = "OLAT_13.0.0";
	private static final String MIGRATE_ROLE = "MIGRATE ROLE";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private OrganisationDAO organisationDao;
	@Autowired
	private OrganisationService organisationService;
	
	public OLATUpgrade_13_0_0() {
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
		allOk &= migrateRole(upgradeManager, uhd);
		
		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.audit("Finished OLATUpgrade_13_0_0 successfully!");
		} else {
			log.audit("OLATUpgrade_13_0_0 not finished, try to restart OpenOLAT!");
		}
		return allOk;
	}
	
	private boolean migrateRole(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MIGRATE_ROLE)) {
			try {
				List<Organisation> defOrganisations = organisationDao.loadByIdentifier(OrganisationService.DEFAULT_ORGANISATION_IDENTIFIER);
				if(defOrganisations.isEmpty()) {
					allOk &= false;
				} else {
					Organisation defOrganisation = defOrganisations.get(0);
					migrate(defOrganisation, "fxadmins", OrganisationRoles.sysadmin);
					migrate(defOrganisation, "admins", OrganisationRoles.administrator);
					migrate(defOrganisation, "users", OrganisationRoles.user);
					migrate(defOrganisation, "usermanagers", OrganisationRoles.usermanager);
					migrate(defOrganisation, "authors", OrganisationRoles.author);
					migrate(defOrganisation, "instoresmanager", OrganisationRoles.learnresourcemanager);
					migrate(defOrganisation, "groupmanagers", OrganisationRoles.groupmanager);
					migrate(defOrganisation, "poolsmanager", OrganisationRoles.poolmanager);
					migrate(defOrganisation, "curriculmanager", OrganisationRoles.curriculummanager);
					migrate(defOrganisation, "anonymous", OrganisationRoles.guest);
				}
				dbInstance.commitAndCloseSession();
			} catch (Exception e) {
				log.error("", e);
				allOk &= false;
			}

			uhd.setBooleanDataValue(MIGRATE_ROLE, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}

	private void migrate(Organisation organisation, String secGroupName, OrganisationRoles role) {
		log.info("Start migration of " + secGroupName);
		List<Long> identitiyKeys = getIdentityInSecurityGroup(secGroupName);
		for(int i=0; i<identitiyKeys.size(); i++) {
			Identity member = dbInstance.getCurrentEntityManager().getReference(IdentityImpl.class, identitiyKeys.get(i));
			organisationService.addMember(organisation, member, role, getInheritanceMode(role));
			if(i % 20 == 0) {
				dbInstance.commitAndCloseSession();
			}
			if(i % 500 == 0) {
				log.info("Migration of " + i + " " + secGroupName);
			}
		}
		dbInstance.commit();
		log.info("End migration of " + identitiyKeys.size() + " " + secGroupName);
	}
	
	private GroupMembershipInheritance getInheritanceMode(OrganisationRoles role) {
		if(role == OrganisationRoles.learnresourcemanager || role == OrganisationRoles.usermanager || role == OrganisationRoles.author) {
			return GroupMembershipInheritance.root;
		}
		return GroupMembershipInheritance.none;
	}
	
	public List<Long> getIdentityInSecurityGroup(String securityGroupName) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("select msi.identity.key from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as msi ")
		  .append(" inner join msi.securityGroup secGroup")
		  .append(" inner join ").append(NamedGroupImpl.class.getName()).append(" as ngroup on (ngroup.securityGroup.key=secGroup.key)")
		  .append(" where ngroup.groupName=:groupName");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("groupName", securityGroupName)
				.getResultList();
	}
}

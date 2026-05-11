/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.manager;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.admin.AdminModule;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.IdentityImpl;
import org.olat.basesecurity.NamedGroupImpl;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.SecurityGroupMembershipImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.modules.selectus.model.OrganisationUnit;
import org.olat.modules.selectus.model.OrganisationUnitImpl;
import org.olat.properties.Property;
import org.olat.properties.PropertyManager;
import org.olat.upgrade.model.UpgradePositionUnitImpl;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 8 mai 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Service
public class SelectusMigration implements InitializingBean {
	
	private static final Logger log = Tracing.createLoggerFor(SelectusMigration.class);

	public static final String SELECTUS_MIGRATION = "recruitingtool";
	
	private final DB dbInstance;
	private final PositionDAO positionDao;
	private final BaseSecurity securityManager;
	private final PropertyManager propertyManager;
	private final OrganisationService organisationService;
	private final OrganisationUnitDAO organisationUnitDao;
	
	@Autowired
	public SelectusMigration(OrganisationUnitDAO organisationUnitDao, OrganisationService organisationService,
			PropertyManager propertyManager, BaseSecurity securityManager, PositionDAO positionDao, DB dbInstance) {
		this.organisationService = organisationService;
		this.propertyManager = propertyManager;
		this.organisationUnitDao = organisationUnitDao;
		this.securityManager = securityManager;
		this.positionDao = positionDao;
		this.dbInstance = dbInstance;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Property p = propertyManager.findProperty(null, null, null, AdminModule.SYSTEM_PROPERTY_CATEGORY, SELECTUS_MIGRATION);
		if(p != null && "true".equals(p.getStringValue())) {
			// Create an organisation pro unit
			migrateOrganisationUnit();
			// Migrate roles
			migrateSelectus();
			// Migrate staff
			migrateStaff();
			// Migrate organisations of positions
			migrateOrganisationPositions();
			// Migrate applicant only to invitee
			migrateApplicantsToInvitee();
			
			p.setStringValue("done");
			propertyManager.updateProperty(p);
		}
		dbInstance.commitAndCloseSession();
	}
	
	private void migrateOrganisationUnit() {
		List<OrganisationUnit> organisationsUnits = organisationUnitDao.findAllOrganisationUnits();
		for(OrganisationUnit organisationUnit:organisationsUnits) {
			if(organisationUnit.getOrganisation() == null) {
				Organisation defaultOrganisation = organisationService.getDefaultOrganisation();
				Organisation unit = organisationService.createOrganisation(organisationUnit.getName(), organisationUnit.getName(), null, defaultOrganisation, null, null);
				((OrganisationUnitImpl)organisationUnit).setOrganisation(unit);
				organisationUnitDao.save(organisationUnit);
				dbInstance.commit();
			}
		}
		dbInstance.commitAndCloseSession();
	}
	
	private void migrateSelectus() {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		migrate(defOrganisation, "fxadmins", OrganisationRoles.sysadmin);
		migrate(defOrganisation, "fxadmins", OrganisationRoles.administrator);
		migrate(defOrganisation, "fxadmins", OrganisationRoles.rolesmanager);
		migrate(defOrganisation, "admins", OrganisationRoles.administrator);
		migrate(defOrganisation, "admins", OrganisationRoles.sysadmin);
		migrate(defOrganisation, "admins", OrganisationRoles.rolesmanager);
		migrate(defOrganisation, "users", OrganisationRoles.user);
		migrate(defOrganisation, "usermanagers", OrganisationRoles.usermanager);
		migrate(defOrganisation, "usermanagers", OrganisationRoles.rolesmanager);
		migrate(defOrganisation, "authors", OrganisationRoles.selectusmanager);// Authors was used as selectus manager in selectus
		migrate(defOrganisation, "anonymous", OrganisationRoles.guest);
	}
	
	private void migrate(Organisation organisation, String secGroupName, OrganisationRoles role) {
		log.info("Start migration of " + secGroupName);
		List<Long> identitiesKeys = getIdentityInSecurityGroup(secGroupName);
		for(int i=0; i<identitiesKeys.size(); i++) {
			Identity member = dbInstance.getCurrentEntityManager().getReference(IdentityImpl.class, identitiesKeys.get(i));
			organisationService.addMember(organisation, member, role, null);
			if(i % 20 == 0) {
				dbInstance.commitAndCloseSession();
			}
			if(i % 500 == 0) {
				log.info("Migration of " + i + " " + secGroupName);
			}
		}
		dbInstance.commit();
		log.info("End migration of " + identitiesKeys.size() + " " + secGroupName);
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
	
	private void migrateStaff() {
		List<OrganisationUnit> units = organisationUnitDao.findAllOrganisationUnits();
		for(OrganisationUnit unit:units) {
			migrateStaff(unit);
			dbInstance.commit();
		}
		dbInstance.commitAndCloseSession();
	}
	
	private void migrateStaff(OrganisationUnit unit) {
		Organisation organisation = organisationService.getOrganisation(unit.getOrganisation());
		List<Identity> staffList = findMemberships(unit.getKey());
		for(Identity staff:staffList) {
			organisationService.addMember(organisation, staff, OrganisationRoles.selectusmanager, null);
		}
	}

	private List<Identity> findMemberships(Long organisationUnitKey) {
		String query = """
			select ident from rorganisationunitmember membership
			inner join membership.organisationUnit orgUnit
			inner join membership.identity ident
			where membership.organisationUnit.key=:organisationUnitKey""";
		
		return dbInstance.getCurrentEntityManager()
			.createQuery(query, Identity.class)
			.setParameter("organisationUnitKey", organisationUnitKey)
			.getResultList();
	}
	
	private void migrateOrganisationPositions() {
		List<UpgradePositionUnitImpl> positions = positionWithoutOrganisations();
		for(UpgradePositionUnitImpl position:positions) {
			if(position.getOrganisation() == null && position.getOrganisationUnit() != null) {
				position.setOrganisation(position.getOrganisationUnit().getOrganisation());
				dbInstance.getCurrentEntityManager().merge(position);
				dbInstance.commit();
			}
		}
		dbInstance.commitAndCloseSession();
	}
	
	private List<UpgradePositionUnitImpl> positionWithoutOrganisations() {
		String query = """
				select upos from upgraderposition as upos
				inner join fetch organisationUnit as orgUnit
				inner join fetch orgUnit.organisation as orgOfTheUnit
				where orgUnit.key is not null and upos.organisation.key is null""";
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, UpgradePositionUnitImpl.class)
				.getResultList();
	}
	
	private void migrateApplicantsToInvitee() {
		List<Long> identityKeys = getApplicantsIdentityKeys();
		for(Long identityKey:identityKeys) {
			migrateApplicantToInvitee(identityKey);
			dbInstance.commit();
		}
		dbInstance.commitAndCloseSession();
	}
	
	private void migrateApplicantToInvitee(Long identityKey) {
		Identity identity = securityManager.loadIdentityByKey(identityKey);
		Roles roles = securityManager.getRoles(identity);
		if(roles.isMoreThanUser()) {
			return;
		}
		
		boolean isInCommittee = positionDao.isInCommittee(identity);
		if(isInCommittee) {
			return;
		}
		
		organisationService.addMember(identity, OrganisationRoles.invitee, null);
		organisationService.removeMember(identity, OrganisationRoles.user, null);
	}
	
	private List<Long> getApplicantsIdentityKeys() {
		String query = """
				select ident.key from rapplication as app
				inner join app.identity as ident
				where ident.key is not null""";
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, Long.class)
				.getResultList();
	}
}

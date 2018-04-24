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
package org.olat.basesecurity.manager;

import java.util.ArrayList;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.Organisation;
import org.olat.basesecurity.OrganisationManagedFlag;
import org.olat.basesecurity.OrganisationRef;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.OrganisationType;
import org.olat.basesecurity.model.OrganisationImpl;
import org.olat.basesecurity.model.OrganisationMember;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 9 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class OrganisationServiceImpl implements OrganisationService, InitializingBean {
	
	private static final OLog log = Tracing.createLoggerFor(OrganisationServiceImpl.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GroupDAO groupDao;
	@Autowired
	private OrganisationDAO organisationDao;

	@Override
	public void afterPropertiesSet() throws Exception {
		List<Organisation> defaultOrganisations = organisationDao.loadByIdentifier(DEFAULT_ORGANISATION_IDENTIFIER);
		if(defaultOrganisations.isEmpty()) {
			Organisation organisation = organisationDao.create("OpenOLAT", DEFAULT_ORGANISATION_IDENTIFIER, null, null, null);
			organisation.setManagedFlags(new OrganisationManagedFlag[] {
					OrganisationManagedFlag.identifier, OrganisationManagedFlag.externalId,
					OrganisationManagedFlag.move, OrganisationManagedFlag.delete
				});
			organisationDao.update(organisation);
			dbInstance.commitAndCloseSession();
		}
	}

	@Override
	public Organisation createOrganisation(String displayName, String identifier, String description, Organisation organisation, OrganisationType type) {
		return organisationDao.createAndPersistOrganisation(displayName, identifier, description, organisation, type);
	}

	@Override
	public Organisation getOrganisation(OrganisationRef organisation) {
		return organisationDao.loadByKey(organisation.getKey());
	}

	@Override
	public Organisation updateOrganisation(Organisation organisation) {
		return organisationDao.update(organisation);
	}

	@Override
	public Organisation getDefaultOrganisation() {
		List<Organisation> defOrganisations = organisationDao.loadDefaultOrganisation();
		if(defOrganisations.size() == 1) {
			return defOrganisations.get(0);
		}
		if(defOrganisations.size() > 1) {
			log.error("You have more than one default organisation");
			return defOrganisations.get(0);
		}
		log.error("You don't have a defualt organisation");
		return null;
	}

	@Override
	public List<Organisation> getOrganisations() {
		return organisationDao.find();
	}

	@Override
	public List<Organisation> getOrganisations(IdentityRef member, OrganisationRoles... role) {
		List<String> roleList = new ArrayList<>(role.length);
		for(OrganisationRoles r:role) {
			roleList.add(r.name());
		}
		return organisationDao.getOrganisations(member, roleList);
	}

	@Override
	public List<Organisation> getSearchableOrganisations(IdentityRef member, Roles roles) {
		List<String> roleList = new ArrayList<>();// if user manager, descent organization tree
		for(OrganisationRoles r:OrganisationRoles.values()) {
			if(r !=  OrganisationRoles.guest) {
				roleList.add(r.name());
			}
		}
		List<Organisation> organisations = organisationDao.getOrganisations(member, roleList);
		
		return organisations;
	}

	@Override
	public List<OrganisationMember> getMembers(Organisation organisation) {
		return organisationDao.getMembers(organisation);
	}
	
	@Override
	public void addMember(Identity member, OrganisationRoles role) {
		Organisation defOrganisation = getDefaultOrganisation();
		addMember(defOrganisation, member, role);
	}

	@Override
	public void removeMember(IdentityRef member, OrganisationRoles role) {
		Organisation defOrganisation = getDefaultOrganisation();
		removeMember(defOrganisation, member, role);
	}

	@Override
	public void addMember(Organisation organisation, Identity member, OrganisationRoles role) {
		OrganisationImpl org = (OrganisationImpl)organisation;
		if(!groupDao.hasRole(org.getGroup(), member, role.name())) {
			groupDao.addMembershipOneWay(org.getGroup(), member, role.name());
		}
	}
	
	@Override
	public void removeMember(Organisation organisation, IdentityRef member) {
		OrganisationImpl org = (OrganisationImpl)organisation;
		groupDao.removeMembership(org.getGroup(), member);
	}

	@Override
	public void removeMember(Organisation organisation, IdentityRef member, OrganisationRoles role) {
		OrganisationImpl org = (OrganisationImpl)organisation;
		groupDao.removeMembership(org.getGroup(), member, role.name());
	}
	
	@Override
	public boolean hasRole(String organisationIdentifier, IdentityRef identity, OrganisationRoles... roles) {
		List<String> roleList = new ArrayList<>();
		if(roles != null && roles.length > 0 && roles[0] != null) {
			for(int i=0; i<roles.length; i++) {
				if(roles[i] != null) {
					roleList.add(roles[i].name());
				}
			}
		}
		if(roleList.isEmpty()) {
			return false;
		}
		return organisationDao.hasRole(identity, organisationIdentifier, roleList.toArray(new String[roleList.size()]));
	}

	@Override
	public List<Identity> getDefaultsSystemAdministator() {
		return organisationDao.getIdentities(DEFAULT_ORGANISATION_IDENTIFIER, OrganisationRoles.administrator.name());
	}

	@Override
	public boolean hasRole(IdentityRef identity, OrganisationRoles role) {
		return organisationDao.hasRole(identity, null, role.name());
	}

	@Override
	public List<Identity> getIdentitiesWithRole(OrganisationRoles role) {
		return organisationDao.getIdentities(role.name());
	}
	
	
}

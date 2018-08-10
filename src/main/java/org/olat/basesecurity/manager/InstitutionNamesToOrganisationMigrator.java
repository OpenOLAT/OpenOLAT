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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryToOrganisation;
import org.olat.repository.RepositoryService;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.repository.manager.RepositoryEntryToOrganisationDAO;
import org.olat.user.UserImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 4 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class InstitutionNamesToOrganisationMigrator {
	
	private static final OLog log = Tracing.createLoggerFor(InstitutionNamesToOrganisationMigrator.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryEntryRelationDAO reToGroupDao;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private RepositoryEntryToOrganisationDAO repositoryEntryToOrganisationDao;
	
	public void migrate() {
		List<String> institutionNames = getInstitutionNames();
		//create missing organizations under the default one
		Map<String,Organisation> nameToOrganisations = createOrganisations(institutionNames);
		
		//link every repository entry -> list of user with institutional name
		List<Long> repositoryEntryKeys = getRepositoryEntryKeys();
		log.info("Migration to organisation: " + repositoryEntryKeys.size() + " resources to check");
		for(int i=0; i<repositoryEntryKeys.size(); i++) {
			migrateRepositoryEntry(repositoryEntryKeys.get(i), nameToOrganisations);
			if(i % 50 == 0) {
				log.info("Migration to organisation: checked " + i + " / " + repositoryEntryKeys.size() + " resources");
				dbInstance.commitAndCloseSession();
			}
		}
		log.info("Migration to organisation: " + repositoryEntryKeys.size() + " resources checked successfully");
		
		// add every learn resource manager as is under the right organisation
		migrateLearnResourceManager(nameToOrganisations);
	}
	
	private void migrateLearnResourceManager(Map<String,Organisation> nameToOrganisations) {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		List<Identity> managers = getLearnManagers(defOrganisation);
		for(Identity manager:managers) {
			String institutionName = manager.getUser().getProperty(UserConstants.INSTITUTIONALNAME, null);
			if(StringHelper.containsNonWhitespace(institutionName)) {
				Organisation institution = nameToOrganisations.get(institutionName.toLowerCase());
				if(institution != null) {
					organisationService
						.removeMember(defOrganisation, manager, OrganisationRoles.learnresourcemanager, true);
					organisationService
						.addMember(institution, manager, OrganisationRoles.learnresourcemanager);
					dbInstance.commit();
					log.info("Institutional resource manager: " + manager.getKey() + " moved to organisation: " + institution.getDisplayName() + " (" + institution.getKey() + ")");
				}
			}
		}
		dbInstance.commitAndCloseSession();
	}
	
	private void migrateRepositoryEntry(Long repositoryEntryKey, Map<String,Organisation> nameToOrganisations) {
		List<String> entryInstitutionNames = getInstitutionNames(repositoryEntryKey);
		if(entryInstitutionNames.isEmpty()) return;
		
		RepositoryEntry re = repositoryService.loadByKey(repositoryEntryKey);
		Set<RepositoryEntryToOrganisation> organisationRelations = re.getOrganisations();
		List<Organisation> currentOrganisations = organisationRelations.stream()
				.map(RepositoryEntryToOrganisation::getOrganisation).collect(Collectors.toList());
		
		for(String entryInstitutionName:entryInstitutionNames) {
			Organisation organisation = nameToOrganisations.get(entryInstitutionName.toLowerCase());
			if(!currentOrganisations.contains(organisation)) {
				reToGroupDao.createRelation(organisation.getGroup(), re);
				repositoryEntryToOrganisationDao.createRelation(organisation, re, false);
			}
		}
		dbInstance.commitAndCloseSession();
	}
	
	private Map<String,Organisation> createOrganisations(List<String> institutionNames) {
		List<Organisation> organisations = organisationService.getOrganisations();
		Map<String,Organisation> nameToOrganisations = new HashMap<>();
		for(Organisation organisation:organisations) {
			if(StringHelper.containsNonWhitespace(organisation.getDisplayName())) {
				nameToOrganisations.put(organisation.getDisplayName().toLowerCase(), organisation);
			}
			if(StringHelper.containsNonWhitespace(organisation.getIdentifier())) {
				nameToOrganisations.put(organisation.getIdentifier().toLowerCase(), organisation);
			}
		}
		
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		for(String institutionName:institutionNames) {	
			String lowerCased = institutionName.toLowerCase();
			if(!nameToOrganisations.containsKey(lowerCased)) {
				Organisation organisation = organisationService
						.createOrganisation(institutionName, institutionName, null, defOrganisation, null);
				nameToOrganisations.put(lowerCased, organisation);
			}
		}
		dbInstance.commitAndCloseSession();
		return nameToOrganisations;	
	}
	
	private List<Long> getRepositoryEntryKeys() {
		String q = "select v.key from repositoryentry as v";
		return dbInstance.getCurrentEntityManager()
				.createQuery(q, Long.class)
				.getResultList();
	}
	
	private List<String> getInstitutionNames(Long repositoryEntryKey) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select distinct identUser.institutionalName from repositoryentry as v")
		  .append(" inner join v.groups as relGroup")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as memberships")
		  .append(" inner join memberships.identity as ident")
		  .append(" inner join ident.user as identUser")
		  .append(" where v.key=:repositoryEntryKey");

		List<String> names = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), String.class)
				.setParameter("repositoryEntryKey", repositoryEntryKey)
				.getResultList();
		return names.stream()
				.filter(StringHelper::containsNonWhitespace)
				.collect(Collectors.toList());
	}
	
	private List<String> getInstitutionNames() {
		String q = "select distinct user.institutionalName from " + UserImpl.class.getCanonicalName() + " as user";
		List<String> names = dbInstance.getCurrentEntityManager()
				.createQuery(q, String.class)
				.getResultList();
		return names.stream()
				.filter(StringHelper::containsNonWhitespace)
				.collect(Collectors.toList());
	}
	
	private List<Identity> getLearnManagers(Organisation organisation) {
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct ident from bgroupmember as membership")
		  .append(" inner join membership.identity as ident")
		  .append(" inner join fetch ident.user as identUser")
		  .append(" where membership.group.key=:groupKey and membership.role=:role");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("groupKey", organisation.getGroup().getKey())
				.setParameter("role", OrganisationRoles.learnresourcemanager.name())
				.getResultList();
	}
}

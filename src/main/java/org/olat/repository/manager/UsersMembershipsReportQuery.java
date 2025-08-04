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
package org.olat.repository.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import jakarta.persistence.FlushModeType;
import jakarta.persistence.TemporalType;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.model.RepositoryEntryRefImpl;
import org.olat.repository.model.UsersMembershipsEntry;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 1 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Service
public class UsersMembershipsReportQuery {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private RepositoryEntryToTaxonomyLevelDAO repositoryEntryToTaxonomyLevelDao;
	
	public List<UsersMembershipsEntry> search(Date from, Date to, List<GroupRoles> roles,
			List<UserPropertyHandler> userPropertyHandlers, Locale locale, int firstResult, int maxResults) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select v.key, v.displayname, v.externalId, v.externalRef,")
		  .append(" v.status, v.publicVisible, lifecycle.validFrom, lifecycle.validTo,")
		  .append(" reMember.key, reMember.role, reMember.creationDate,")
		  .append(" ident.key, ident.creationDate, ident.lastLogin, ident.status, authorUser.nickName, authorUser.email");
		for(UserPropertyHandler userPropertyHandler:userPropertyHandlers) {
			sb.append(", identUser.").append(userPropertyHandler.getName());
		}
		sb.append(" from repositoryentry as v")
		  .append(" left join v.lifecycle as lifecycle")
		  .append(" inner join v.olatResource as ores")
		  .append(" inner join v.groups as rel")
		  .append(" inner join rel.group as bGroup")
		  .append(" inner join bGroup.members as reMember")
		  .append(" inner join reMember.identity as ident")
		  .append(" inner join ident.user as identUser")
		  .append(" left join bidentity as initialAuthor on initialAuthor.name = v.initialAuthor")
		  .append(" left join initialAuthor.user as authorUser");
		
		sb.where().append("reMember.creationDate >= :from and reMember.creationDate <= :to")
		  .and().append("reMember.role in (:roles)")
		  .and().append("ores.resName=:resourceType and v.status<>:excludedStatus");
		
		sb.append(" order by v.displayname, v.key, reMember.key");
		
		List<String> rolesList = roles.stream()
				.map(GroupRoles::name)
				.toList();
		
		List<Object[]> rawObjects = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Object[].class)
			.setParameter("from", from, TemporalType.TIMESTAMP)
			.setParameter("to", to, TemporalType.TIMESTAMP)
			.setParameter("roles", rolesList)
			.setParameter("resourceType", "CourseModule")
			.setParameter("excludedStatus", RepositoryEntryStatusEnum.deleted.name())
			.setFirstResult(firstResult)
			.setMaxResults(maxResults)
			.setFlushMode(FlushModeType.COMMIT)
			.getResultList();
		
		Set<Long> identityKeys = new HashSet<>();
		Set<Long> repositoryEntryKeys = new HashSet<>();
		
		List<UsersMembershipsEntry> entries = new ArrayList<>(rawObjects.size());
		for(Object[] objects:rawObjects) {
			int pos = 0;
			Long repositoryEntryKey = (Long)objects[pos++];
			String repositoryEntryDisplayname = (String)objects[pos++];
			String repositoryEntryExternalId = (String)objects[pos++];
			String repositoryEntryExternalRef = (String)objects[pos++];
			RepositoryEntryStatusEnum repositoryEntryStatus = RepositoryEntryStatusEnum.valueOf((String)objects[pos++]);
			Boolean repositoryEntryPublicVisible = (Boolean)objects[pos++];		
			Date lifecycleFrom = (Date)objects[pos++];
			Date lifecycleTo = (Date)objects[pos++];
			pos++; // Membership key 
			
			// Membership
			GroupRoles role = GroupRoles.valueOf((String)objects[pos++]);
			Date registrationDate = (Date)objects[pos++];
			
			// Identity
			Long identityKey = (Long)objects[pos++];
			Date identityCreationDate = (Date)objects[pos++];
			Date identityLastLogin = (Date)objects[pos++];
			int identityStatus = PersistenceHelper.extractPrimitiveInt(objects, pos++);

			String repositoryEntryInitialAuthorName = null;
			if (objects[pos++] instanceof String initialAuthorName) {
				repositoryEntryInitialAuthorName = initialAuthorName;
			}

			String repositoryEntryInitialAuthorEmail = null;
			if (objects[pos++] instanceof String initialAuthorEmail) {
				repositoryEntryInitialAuthorEmail = initialAuthorEmail;
			}
			
			String[] identityProps = new String[userPropertyHandlers.size()];
			System.arraycopy(objects, pos, identityProps, 0, userPropertyHandlers.size());
			
			UsersMembershipsEntry entry = new UsersMembershipsEntry(identityKey, userPropertyHandlers, identityProps, locale,
					identityStatus, identityCreationDate, identityLastLogin,
					repositoryEntryKey, repositoryEntryDisplayname, repositoryEntryInitialAuthorName, 
					repositoryEntryInitialAuthorEmail, repositoryEntryExternalId, repositoryEntryExternalRef,
					repositoryEntryStatus, repositoryEntryPublicVisible, lifecycleFrom, lifecycleTo, role, registrationDate);
			identityKeys.add(identityKey);
			repositoryEntryKeys.add(repositoryEntryKey);
			entries.add(entry);
		}
		
		// Decorate with taxonomy levels
		Map<Long,List<String>> taxonomyLevels = getTaxonomyLevels(repositoryEntryKeys, locale);
		appendTaxonomyLevels(entries, taxonomyLevels);
		
		// Decorate with organisations
		Map<Long,List<String>> organisationsMap = getOrganisations(List.copyOf(identityKeys));
		appendOrganisations(entries, organisationsMap);
		return entries;
	}
	
	private void appendTaxonomyLevels(List<UsersMembershipsEntry> entries, Map<Long,List<String>> taxonomyLevels) {
		for(UsersMembershipsEntry entry:entries) {
			Long repositoryEntryKey = entry.getRepositoryEntryKey();
			entry.setTaxonomyLevels(taxonomyLevels.get(repositoryEntryKey));
		}
	}
	
	private Map<Long,List<String>> getTaxonomyLevels(Collection<Long> repositoryEntryKeys, Locale locale) {
		List<RepositoryEntryRef> refs = repositoryEntryKeys.stream()
				.map(key -> new RepositoryEntryRefImpl(key))
				.map(RepositoryEntryRef.class::cast)
				.toList();
		
		Translator taxonomyTranslator = Util.createPackageTranslator(TaxonomyUIFactory.class, locale);
		
		Map<RepositoryEntryRef, List<TaxonomyLevel>> map = repositoryEntryToTaxonomyLevelDao.getTaxonomyLevels(refs, false);
		Map<Long, List<String>> levelsMap = new HashMap<>();
		for(Map.Entry<RepositoryEntryRef,List<TaxonomyLevel>> entry:map.entrySet()) {
			Long repositoryEntryKey = entry.getKey().getKey();
			List<TaxonomyLevel> levelsList = entry.getValue();
			List<String> levelsStrings = new ArrayList<>(levelsList.size());
			for(TaxonomyLevel level:levelsList) {
				String dn1 = TaxonomyUIFactory.translateDisplayName(taxonomyTranslator, level, level::getIdentifier);
				if(dn1 != null) {
					levelsStrings.add(dn1);
				}
			}
			levelsMap.put(repositoryEntryKey, levelsStrings);
		}
		return levelsMap;
	}
	
	private void appendOrganisations(List<UsersMembershipsEntry> entries, Map<Long,List<String>> organisationsMap) {
		for(UsersMembershipsEntry entry:entries) {
			Long identityKey = entry.getIdentityKey();
			entry.setOrganisations(organisationsMap.get(identityKey));
		}
	}
	
	private Map<Long,List<String>> getOrganisations(List<Long> identityKeys) {
		String query = """
				select membership.identity.key, org.displayName from organisation org
				inner join org.group baseGroup
				inner join baseGroup.members membership
				where membership.identity.key in (:identityKeys) and membership.role=:role""";

		Map<Long,List<String>> map = new HashMap<>();

		List<Object[]> rawObjectsList = dbInstance.getCurrentEntityManager()
			.createQuery(query, Object[].class)
			.setParameter("identityKeys", identityKeys)
			.setParameter("role", OrganisationRoles.user.name())
			.setFlushMode(FlushModeType.COMMIT)
			.getResultList();
			
		for(Object[] rawObjects:rawObjectsList) {
			Long identityKey = (Long)rawObjects[0];
			String organisationDisplayName = (String)rawObjects[1];
			map.computeIfAbsent(identityKey, key -> new ArrayList<>(3))
					.add(organisationDisplayName);
		}
		return map;
	}
}

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
package org.olat.repository.manager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.SearchIdentityParams;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.i18n.I18nManager;
import org.olat.repository.AutomaticLifecycleService;
import org.olat.repository.ErrorList;
import org.olat.repository.RepositoryDeletionModule;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryLifeCycleValue;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.RepositoryEntryRuntimeType;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.resource.references.Reference;
import org.olat.resource.references.ReferenceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 2 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class AutomaticLifecycleServiceImpl implements AutomaticLifecycleService {
	
	private static final Logger log = Tracing.createLoggerFor(AutomaticLifecycleServiceImpl.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private ReferenceManager referenceManager;
	@Autowired
	private RepositoryModule repositoryModule;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryDeletionModule repositoryDeletionModule;
	
	@Override
	public void close(AutomaticLifecycleJobState state) {
		String autoClose = repositoryModule.getLifecycleAutoClose();
		if(StringHelper.containsNonWhitespace(autoClose)) {
			RepositoryEntryLifeCycleValue autoCloseVal = RepositoryEntryLifeCycleValue.parse(autoClose);
			Date markerDate = autoCloseVal.limitDate(new Date());
			List<RepositoryEntry> entriesToClose = this.getRepositoryEntriesToClose(markerDate);
			for(RepositoryEntry entry:entriesToClose) {
				if(state.isInterrupted() || !repositoryModule.isLifecycleAutoCloseEnabled()) {
					break;
				}

				try {
					log.info(Tracing.M_AUDIT, "Automatic closing {}: {} [{}]", entry.getResourceableTypeName(), entry.getDisplayname(), entry.getKey());
					repositoryService.closeRepositoryEntry(entry, null, false);
					dbInstance.commit();
				} catch (Exception e) {
					log.error("",  e);
					dbInstance.commitAndCloseSession();
				}
			}
		}
	}
	
	@Override
	public void delete(AutomaticLifecycleJobState state) {
		String autoDelete = repositoryModule.getLifecycleAutoDelete();
		if(StringHelper.containsNonWhitespace(autoDelete)) {
			RepositoryEntryLifeCycleValue autoDeleteVal = RepositoryEntryLifeCycleValue.parse(autoDelete);
			Date markerDate = autoDeleteVal.limitDate(new Date());
			List<RepositoryEntry> entriesToDelete = getRepositoryEntriesToDelete(markerDate);
			for(RepositoryEntry entry:entriesToDelete) {
				if(state.isInterrupted() || !repositoryModule.isLifecycleAutoDeleteEnabled()) {
					break;
				}
				
				try {
					log.info(Tracing.M_AUDIT, "Automatic deleting (soft) {}: {} [{}]", entry.getResourceableTypeName(), entry.getDisplayname(), entry.getKey() );
					repositoryService.deleteSoftly(entry, null, false, false);
					dbInstance.commit();
				} catch (Exception e) {
					log.error("",  e);
					dbInstance.commitAndCloseSession();
				}
			}
		}
	}
	
	@Override
	public void definitivelyDelete(AutomaticLifecycleJobState state) {
		String autoDefinitivelyDelete = repositoryModule.getLifecycleAutoDefinitivelyDelete();
		if(StringHelper.containsNonWhitespace(autoDefinitivelyDelete)) {
			RepositoryEntryLifeCycleValue autoDefinitivelyDeleteVal = RepositoryEntryLifeCycleValue.parse(autoDefinitivelyDelete);
			Date markerDate = autoDefinitivelyDeleteVal.limitDate(new Date());
			List<RepositoryEntry> entriesToDelete = getRepositoryEntriesToDefinitivelyDelete(markerDate);
			for(RepositoryEntry entry:entriesToDelete) {
				if(state.isInterrupted() || !repositoryModule.isLifecycleAutoDefinitivelyDeleteEnabled()) {
					break;
				}
				
				try {
					definitivelyDelete(entry);
				} catch (Exception e) {
					log.error("",  e);
					dbInstance.commitAndCloseSession();
				}
			}
		}
	}
	
	private boolean isReferenced(RepositoryEntry entry) {
		List<Reference> targetReferences = referenceManager.getReferencesTo(entry.getOlatResource());
		return targetReferences != null && !targetReferences.isEmpty();
	}
	
	/**
	 * @param entry The repository entry to delete
	 * @return true if the deletion happens without errors
	 */
	protected boolean definitivelyDelete(RepositoryEntry entry) {
		boolean deleted = false;
		Identity administrator = getDefaultAdministrator(entry);
		if(administrator != null) {
			Roles roles = securityManager.getRoles(administrator);
			Locale locale = I18nManager.getInstance().getLocaleOrDefault(administrator.getUser().getPreferences().getLanguage());
			log.info(Tracing.M_AUDIT, "Automatic deleting (definitively) {}: {} [{}]", entry.getResourceableTypeName(), entry.getDisplayname(), entry.getKey());
			ErrorList errors = repositoryService.deletePermanently(entry, administrator, roles, locale);
			deleted = !errors.hasErrors();
		} else {
			log.error("Automatic deleting aborted, no administrator found for archives: {} [{}]", entry.getDisplayname(), entry.getKey());
		}
		dbInstance.commit();
		return deleted;
	}
	
	private Identity getDefaultAdministrator(RepositoryEntry entry) {
		Identity administrator = repositoryDeletionModule.getAdminUserIdentity();
		if(administrator == null) {
			List<OrganisationRef> identityOrgs = repositoryService.getOrganisationReferences(entry);
			SearchIdentityParams identityParams = new SearchIdentityParams();
			identityParams.setOrganisations(identityOrgs);
			identityParams.setRoles(new OrganisationRoles[]{ OrganisationRoles.administrator });
			identityParams.setStatus(Identity.STATUS_VISIBLE_LIMIT);
			List<Identity> admins = securityManager.getIdentitiesByPowerSearch(identityParams, 0, -1);
			if(!admins.isEmpty()) {
				administrator = admins.get(0);
			}
		}
		return administrator;
	}
	
	@Override
	public List<RepositoryEntry> getRepositoryEntriesToClose(Date date) {
		List<RepositoryEntry> entriesToClose = getRepositoryEntries(date, RepositoryEntryStatusEnum.preparationToPublished());
		List<RepositoryEntry> entriesToCloseFiltered = new ArrayList<>(entriesToClose.size());
		for(RepositoryEntry entry:entriesToClose) {
			boolean closeManaged = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.close);
			if(!closeManaged) {
				entriesToCloseFiltered.add(entry);
			}
		}
		return entriesToCloseFiltered;
	}

	@Override
	public List<RepositoryEntry> getRepositoryEntriesToDelete(Date date) {
		List<RepositoryEntry> entriesToDelete = getRepositoryEntries(date, RepositoryEntryStatusEnum.preparationToClosed());
		List<RepositoryEntry> entriesToDeleteFiltered = new ArrayList<>(entriesToDelete.size());
		for(RepositoryEntry entry:entriesToDelete) {
			boolean deleteManaged = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.delete);
			if(!deleteManaged) {
				entriesToDeleteFiltered.add(entry);
			}
		}
		return entriesToDeleteFiltered;
	}

	@Override
	public List<RepositoryEntry> getRepositoryEntriesToDefinitivelyDelete(Date date) {
		List<RepositoryEntry> entriesToDelete = getRepositoryEntriesInTrash(date);
		List<RepositoryEntry> entriesToDeleteFiltered = new ArrayList<>(entriesToDelete.size());
		for(RepositoryEntry entry:entriesToDelete) {
			boolean deleteManaged = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.delete);
			boolean referenced = isReferenced(entry);
			if(referenced) {
				log.info("Resource cannot be automatically and definitvely deleted because still referenced: {}", entry);
			} else if(!deleteManaged) {
				entriesToDeleteFiltered.add(entry);
			}
		}
		return entriesToDeleteFiltered;
	}

	protected List<RepositoryEntry> getRepositoryEntries(Date date, RepositoryEntryStatusEnum[] states) {
		QueryBuilder sb = new QueryBuilder(512);
		sb.append("select v from repositoryentry as v ")
		  .append(" inner join fetch v.olatResource as ores")
		  .append(" left join fetch v.statistics as statistics")
		  .append(" left join fetch v.lifecycle as lifecycle")
		  .append(" where lifecycle.validTo<:now and v.status ").in(states)
		  .append(" and (v.runtimeTypeString is null or v.runtimeTypeString<>:template)");

		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		CalendarUtils.getEndOfDay(cal);
		Date endOfDay = cal.getTime();
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntry.class)
				.setParameter("now", endOfDay)
				.setParameter("template", RepositoryEntryRuntimeType.template.name())
				.getResultList();
	}

	protected List<RepositoryEntry> getRepositoryEntriesInTrash(Date date) {
		QueryBuilder sb = new QueryBuilder(512);
		sb.append("select v from repositoryentry as v ")
		  .append(" inner join fetch v.olatResource as ores")
		  .append(" left join fetch v.statistics as statistics")
		  .append(" left join fetch v.lifecycle as lifecycle")
		  .append(" where v.deletionDate<:now and v.status ").in(RepositoryEntryStatusEnum.trash);
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		CalendarUtils.getEndOfDay(cal);
		Date endOfDay = cal.getTime();
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntry.class)
				.setParameter("now", endOfDay)
				.getResultList();
	}
}

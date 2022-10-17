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
package org.olat.course.assessment.manager;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.persistence.FlushModeType;
import jakarta.persistence.TypedQuery;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.SyncerExecutor;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.assessment.UserCourseInformations;
import org.olat.course.assessment.model.UserCourseInfosImpl;
import org.olat.group.BusinessGroupRef;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Manager for infos as initial launch date...
 * 
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Service("userCourseInformationsManager")
public class UserCourseInformationsManagerImpl implements UserCourseInformationsManager {
	
	private static final Logger log = Tracing.createLoggerFor(UserCourseInformationsManagerImpl.class);

	@Autowired
	private DB dbInstance;

	@Override
	public UserCourseInfosImpl getUserCourseInformations(OLATResource resource, IdentityRef identity) {
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("select infos from ").append(UserCourseInfosImpl.class.getName()).append(" as infos ")
			  .append(" inner join fetch infos.resource as resource")
			  .append(" inner join infos.identity as identity")
			  .append(" where identity.key=:identityKey and resource.key=:resKey and resource.resName='CourseModule'");

			List<UserCourseInfosImpl> infoList = dbInstance.getCurrentEntityManager()
					.createQuery(sb.toString(), UserCourseInfosImpl.class)
					.setParameter("identityKey", identity.getKey())
					.setParameter("resKey", resource.getKey())
					.getResultList();

			if(infoList.isEmpty()) {
				return null;
			}
			return infoList.get(0);
		} catch (Exception e) {
			log.error("Cannot retrieve course informations for: " + identity + " from " + identity, e);
			return null;
		}
	}
	
	@Override
	public List<UserCourseInformations> getUserCourseInformations(IdentityRef identity) {
		if(identity == null) {
			return Collections.emptyList();
		}

		try {
			StringBuilder sb = new StringBuilder();
			sb.append("select infos from ").append(UserCourseInfosImpl.class.getName()).append(" as infos ")
			  .append(" inner join fetch infos.resource as resource")
			  .append(" inner join infos.identity as identity")
			  .append(" where identity.key=:identityKey");

			return dbInstance.getCurrentEntityManager()
					.createQuery(sb.toString(), UserCourseInformations.class)
					.setParameter("identityKey", identity.getKey())
					.getResultList();
		} catch (Exception e) {
			log.error("Cannot retrieve course informations for: " + identity + " from " + identity, e);
			return null;
		}
	}
	
	@Override
	public List<UserCourseInformations> getUserCourseInformations(IdentityRef identity, List<OLATResource> resources) {
		if(resources == null || resources.isEmpty()) {
			return Collections.emptyList();
		}

		try {
			StringBuilder sb = new StringBuilder();
			sb.append("select infos from ").append(UserCourseInfosImpl.class.getName()).append(" as infos ")
			  .append(" inner join fetch infos.resource as resource")
			  .append(" inner join infos.identity as identity")
			  .append(" where identity.key=:identityKey and resource.key in (:resKeys)");

			List<Long> resourceKeys = PersistenceHelper.toKeys(resources);
			TypedQuery<UserCourseInformations> query = dbInstance.getCurrentEntityManager()
					.createQuery(sb.toString(), UserCourseInformations.class)
					.setParameter("identityKey", identity.getKey())
					.setParameter("resKeys", resourceKeys);

			return query.getResultList();
		} catch (Exception e) {
			log.error("Cannot retrieve course informations for: " + identity + " from " + identity, e);
			return null;
		}
	}
	
	/**
	 * Execute the update statement
	 * @param courseResource
	 * @param identity
	 * @return
	 */
	protected int lowLevelUpdate(OLATResource courseResource, Identity identity) {
		return dbInstance.getCurrentEntityManager().createNamedQuery("updateLaunchDates")
			.setParameter("identityKey", identity.getKey())
			.setParameter("resourceKey", courseResource.getKey())
			.setParameter("now", new Date())
			.executeUpdate();
	}
	
	/**
	 * Update (or create if not exists) the course informations for a user
	 * @param userCourseEnv
	 * @return
	 */
	@Override
	public void updateUserCourseInformations(final OLATResource courseResource, final Identity identity) {
		int updatedRows = lowLevelUpdate(courseResource, identity);
		dbInstance.commit();//to make it quick
		if(updatedRows == 0) {
			OLATResourceable lockRes = OresHelper.createOLATResourceableInstance("CourseLaunchDate::Identity", identity.getKey());
			CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(lockRes, new SyncerExecutor(){
				@Override
				public void execute() {
					try {
						int retryUpdatedRows = lowLevelUpdate(courseResource, identity);
						if(retryUpdatedRows == 0) {
							UserCourseInfosImpl infos = new UserCourseInfosImpl();
							infos.setIdentity(identity);
							infos.setCreationDate(new Date());
							infos.setInitialLaunch(new Date());
							infos.setLastModified(new Date());
							infos.setRecentLaunch(new Date());
							infos.setVisit(1);
							infos.setResource(courseResource);
							dbInstance.getCurrentEntityManager().persist(infos);
						}
					} catch (Exception e) {
						log.error("Cannot update course informations for: " + identity + " from " + identity, e);
					}
				}
			});
		}
	}
	
	@Override
	public Date getRecentLaunchDate(OLATResource resource, IdentityRef identity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select infos.recentLaunch from usercourseinfos as infos ")
		  .append(" inner join infos.resource as resource")
		  .append(" where infos.identity.key=:identityKey and resource.key=:resKey and resource.resName='CourseModule'");

		List<Date> infoList = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Date.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("resKey", resource.getKey())
				.getResultList();

		if(infoList.isEmpty()) {
			return null;
		}
		return infoList.get(0);
	}

	@Override
	public Date getInitialLaunchDate(OLATResource resource, IdentityRef identity) {
		return getInitialLaunchDate(resource, identity.getKey());
	}
	
	public Date getInitialLaunchDate(OLATResource resource, Long identityKey) {
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("select infos.initialLaunch from ").append(UserCourseInfosImpl.class.getName()).append(" as infos ")
			  .append(" inner join infos.resource as resource")
			  .append(" where infos.identity.key=:identityKey and resource.key=:resKey and resource.resName='CourseModule'");

			List<Date> infoList = dbInstance.getCurrentEntityManager()
					.createQuery(sb.toString(), Date.class)
					.setParameter("identityKey", identityKey)
					.setParameter("resKey", resource.getKey())
					.getResultList();

			if(infoList.isEmpty()) {
				return null;
			}
			return infoList.get(0);
		} catch (Exception e) {
			log.error("Cannot retrieve course informations for resource: " + resource.getResourceableId(), e);
			return null;
		}
	}
	
	@Override
	public Date getInitialLaunchDate(RepositoryEntryRef entry, IdentityRef identity) {
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("select infos.initialLaunch from ").append(UserCourseInfosImpl.class.getName()).append(" as infos ")
			  .append(" inner join infos.resource as resource")
			  .append(" inner join repositoryentry as entry on (resource.key=entry.olatResource.key)")
			  .append(" where infos.identity.key=:identityKey and entry.key=:entryKey");

			List<Date> infoList = dbInstance.getCurrentEntityManager()
					.createQuery(sb.toString(), Date.class)
					.setParameter("identityKey", identity.getKey())
					.setParameter("entryKey", entry.getKey())
					.getResultList();

			if(infoList.isEmpty()) {
				return null;
			}
			return infoList.get(0);
		} catch (Exception e) {
			log.error("Cannot retrieve course informations for entry: " + entry.getKey(), e);
			return null;
		}
	}

	@Override
	public Date getInitialParticipantLaunchDate(RepositoryEntryRef entry, BusinessGroupRef businessGroup) {
		StringBuilder sb = new StringBuilder();
		sb.append("select min(infos.initialLaunch) from ").append(UserCourseInfosImpl.class.getName()).append(" as infos ")
		  .append(" inner join infos.resource as resource")
		  .append(" inner join repositoryentry as entry on (resource.key=entry.olatResource.key and entry.key=:entryKey)")
		  .append(" inner join entry.groups as relGroup on relGroup.defaultGroup=false")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join businessgroup as bg on (relGroup.group.key=bg.baseGroup.key and bg.key=:groupKey)")
		  .append(" inner join baseGroup.members as memberships on (memberships.role = '").append(GroupRoles.participant.name()).append("')")
		  .append(" inner join memberships.identity as ident")
		  .append(" where infos.identity.key=memberships.identity.key");

		List<Date> infoList = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Date.class)
				.setParameter("groupKey", businessGroup.getKey())
				.setParameter("entryKey", entry.getKey())
				.getResultList();

		if(infoList.isEmpty()) {
			return null;
		}
		return infoList.get(0);
	}

	/**
	 * Return a map of identity keys to initial launch date.
	 * @param courseEnv
	 * @param identities
	 * @return
	 */
	@Override
	public Map<Long,Date> getInitialLaunchDates(OLATResource resource, List<Identity> identities) {
		if(identities == null || identities.isEmpty()) {
			return new HashMap<>();
		}
		try {
			List<Long> identityKeys = PersistenceHelper.toKeys(identities);

			StringBuilder sb = new StringBuilder();
			sb.append("select infos.identity.key, infos.initialLaunch from ").append(UserCourseInfosImpl.class.getName()).append(" as infos ")
			  .append(" inner join infos.resource as resource")
			  .append(" where resource.key=:resKey and resource.resName='CourseModule'");
			
			Set<Long> identityKeySet = null;
			if(identityKeys.size() < 100) {
				sb.append(" and infos.identity.key in (:identityKeys)");
				identityKeySet = new HashSet<>(identityKeys);
			}

			TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
					.createQuery(sb.toString(), Object[].class)
					.setParameter("resKey", resource.getKey());
			if(identityKeys.size() < 100) {
				query.setParameter("identityKeys", identityKeys);
			}

			List<Object[]> infoList = query.getResultList();
			Map<Long,Date> dateMap = new HashMap<>();
			for(Object[] infos:infoList) {
				Long identityKey = (Long)infos[0];
				if(identityKeySet == null || identityKeySet.contains(identityKey)) {
					Date initialLaunch = (Date)infos[1];
					dateMap.put(identityKey, initialLaunch);
				}
			}
			return dateMap;
		} catch (Exception e) {
			log.error("Cannot retrieve course informations for: " + resource.getResourceableId(), e);
			return Collections.emptyMap();
		}
	}
	
	/**
	 * Return a map of identity keys to initial launch date.
	 * 
	 * @param courseResourceId The course resourceable id
	 * @return
	 */
	@Override
	public Map<Long,Date> getInitialLaunchDates(Long courseResourceId) {
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("select infos.identity.key, infos.initialLaunch from ").append(UserCourseInfosImpl.class.getName()).append(" as infos ")
			  .append(" inner join infos.resource as resource")
			  .append(" where resource.resId=:resId and resource.resName='CourseModule'");

			TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Object[].class)
					.setParameter("resId", courseResourceId);

			List<Object[]> infoList = query.getResultList();
			Map<Long,Date> dateMap = new HashMap<>();
			for(Object[] infos:infoList) {
				Long identityKey = (Long)infos[0];
				Date initialLaunch = (Date)infos[1];
				if(identityKey != null && initialLaunch != null) {
					dateMap.put(identityKey, initialLaunch);
				}
			}
			return dateMap;
		} catch (Exception e) {
			log.error("Cannot retrieve course informations for: " + courseResourceId, e);
			return Collections.emptyMap();
		}
	}
	
	@Override
	public Map<Long,Date> getInitialLaunchDates(OLATResource resource) {
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("select infos.identity.key, infos.initialLaunch from usercourseinfos as infos ")
			  .append(" inner join infos.resource as resource")
			  .append(" where resource.key=:resourceKey");

			TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Object[].class)
					.setParameter("resourceKey", resource.getKey());

			List<Object[]> infoList = query.getResultList();
			Map<Long,Date> dateMap = new HashMap<>();
			for(Object[] infos:infoList) {
				Long identityKey = (Long)infos[0];
				Date initialLaunch = (Date)infos[1];
				if(identityKey != null && initialLaunch != null) {
					dateMap.put(identityKey, initialLaunch);
				}
			}
			return dateMap;
		} catch (Exception e) {
			log.error("Cannot retrieve course informations for: " + resource, e);
			return Collections.emptyMap();
		}
	}

	@Override
	public Map<Long, Date> getRecentLaunchDates(OLATResource resource) {
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("select infos.identity.key, infos.recentLaunch from usercourseinfos as infos where infos.resource.key=:resKey");
			
			List<Object[]> infoList = dbInstance.getCurrentEntityManager()
					.createQuery(sb.toString(), Object[].class)
					.setParameter("resKey", resource.getKey())
					.getResultList();
			Map<Long, Date> dateMap = new HashMap<>();
			for(Object[] infos:infoList) {
				Long identityKey = (Long)infos[0];
				Date recentLaunch = (Date)infos[1];
				dateMap.put(identityKey, recentLaunch);
			}
			return dateMap;
		} catch (Exception e) {
			log.error("Cannot retrieve course informations for: " + resource.getResourceableId(), e);
			return Collections.emptyMap();
		}
	}

	/**
	 * Return a map of identity keys to initial launch date.
	 * @param courseEnv
	 * @return
	 */
	@Override
	public Map<Long,Date> getRecentLaunchDates(OLATResource resource, List<Identity> identities) {
		if(identities == null || identities.isEmpty()) {
			return new HashMap<>();
		}
		try {
			List<Long> identityKeys = PersistenceHelper.toKeys(identities);

			StringBuilder sb = new StringBuilder();
			sb.append("select infos.identity.key, infos.recentLaunch from ").append(UserCourseInfosImpl.class.getName()).append(" as infos ")
			  .append(" inner join infos.resource as resource")
			  .append(" where resource.key=:resKey and resource.resName='CourseModule'");
			
			Set<Long> identityKeySet = null;
			if(identityKeys.size() < 100) {
				sb.append(" and infos.identity.key in (:identityKeys)");
				identityKeySet = new HashSet<>(identityKeys);
			}

			TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Object[].class)
					.setParameter("resKey", resource.getKey());
			if(identityKeys.size() < 100) {
				query.setParameter("identityKeys", identityKeys);
			}

			List<Object[]> infoList = query.getResultList();
			Map<Long,Date> dateMap = new HashMap<>();
			for(Object[] infos:infoList) {
				Long identityKey = (Long)infos[0];
				if(identityKeySet == null || identityKeySet.contains(identityKey)) {
					Date initialLaunch = (Date)infos[1];
					dateMap.put(identityKey, initialLaunch);
				}
			}
			return dateMap;
		} catch (Exception e) {
			log.error("Cannot retrieve course informations for: " + resource.getResourceableId(), e);
			return Collections.emptyMap();
		}
	}

	@Override
	public int deleteUserCourseInformations(RepositoryEntry entry) {
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("delete from ").append(UserCourseInfosImpl.class.getName()).append(" as infos ")
			  .append(" where resource.key=:resourceKey");

			int count = dbInstance.getCurrentEntityManager()
					.createQuery(sb.toString())
					.setParameter("resourceKey", entry.getOlatResource().getKey())
					.setFlushMode(FlushModeType.AUTO)
					.executeUpdate();
			return count;
		} catch (Exception e) {
			log.error("Cannot Delete course informations for: " + entry, e);
			return -1;
		}
	}
}
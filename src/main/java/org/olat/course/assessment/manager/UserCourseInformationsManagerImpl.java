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

import javax.persistence.FlushModeType;
import javax.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.id.Identity;
import org.olat.core.manager.BasicManager;
import org.olat.course.assessment.UserCourseInformations;
import org.olat.course.assessment.model.UserCourseInfosImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Manager for infos as initial launch date...
 * 
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Service("userCourseInformationsManager")
public class UserCourseInformationsManagerImpl extends BasicManager implements UserCourseInformationsManager {

	@Autowired
	private DB dbInstance;
	@Autowired
	private OLATResourceManager resourceManager;

	private UserCourseInfosImpl createUserCourseInformations(Identity identity, OLATResource courseResource) {
		UserCourseInfosImpl infos = new UserCourseInfosImpl();
		infos.setIdentity(identity);
		infos.setInitialLaunch(new Date());
		infos.setLastModified(new Date());
		infos.setRecentLaunch(new Date());
		infos.setVisit(1);
		infos.setResource(courseResource);
		dbInstance.saveObject(infos);
		return infos;
	}

	@Override
	public UserCourseInfosImpl getUserCourseInformations(Long courseResourceId, Identity identity) {
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("select infos from ").append(UserCourseInfosImpl.class.getName()).append(" as infos ")
				.append(" inner join infos.resource as resource")
			  .append(" where infos.identity.key=:identityKey and resource.resId=:resId and resource.resName='CourseModule'");

			List<UserCourseInfosImpl> infoList = dbInstance.getCurrentEntityManager()
					.createQuery(sb.toString(), UserCourseInfosImpl.class)
					.setParameter("identityKey", identity.getKey())
					.setParameter("resId", courseResourceId)
					.getResultList();

			if(infoList.isEmpty()) {
				return null;
			}
			return infoList.get(0);
		} catch (Exception e) {
			logError("Cannot retrieve course informations for: " + identity + " from " + identity, e);
			return null;
		}
	}
	
	@Override
	public List<UserCourseInformations> getUserCourseInformations(Identity identity, List<OLATResource> resources) {
		if(resources == null || resources.isEmpty()) {
			return Collections.emptyList();
		}

		try {
			StringBuilder sb = new StringBuilder();
			sb.append("select infos from ").append(UserCourseInfosImpl.class.getName()).append(" as infos ")
				.append(" inner join fetch infos.resource as resource")
				.append(" inner join fetch infos.identity as identity")
			  .append(" where identity.key=:identityKey and resource.key in (:resKeys)");

			List<Long> resourceKeys = PersistenceHelper.toKeys(resources);
			TypedQuery<UserCourseInformations> query = dbInstance.getCurrentEntityManager()
					.createQuery(sb.toString(), UserCourseInformations.class)
					.setParameter("identityKey", identity.getKey())
					.setParameter("resKeys", resourceKeys);

			List<UserCourseInformations> infoList = query.getResultList();
			return infoList;
		} catch (Exception e) {
			logError("Cannot retrieve course informations for: " + identity + " from " + identity, e);
			return null;
		}
	}
	
	/**
	 * Update (or create if not exists) the course informations for a user
	 * @param userCourseEnv
	 * @return
	 */
	@Override
	public UserCourseInformations updateUserCourseInformations(Long courseResourceableId, Identity identity) {
		try {
			UserCourseInfosImpl infos = getUserCourseInformations(courseResourceableId, identity);
			if(infos == null) {
				OLATResource courseResource = resourceManager.findResourceable(courseResourceableId, "CourseModule");
				infos = createUserCourseInformations(identity, courseResource);
			} else {
				infos.setVisit(infos.getVisit() + 1);
				infos.setRecentLaunch(new Date());
				infos.setLastModified(new Date());
				dbInstance.updateObject(infos);
			}
			return infos;
		} catch (Exception e) {
			logError("Cannot update course informations for: " + identity + " from " + identity, e);
			return null;
		}
	}
	

	
	@Override
	public Date getInitialLaunchDate(Long courseResourceId, Identity identity) {
		return getInitialLaunchDate(courseResourceId, identity.getKey());
	}
	
	public Date getInitialLaunchDate(Long courseResourceId, Long identityKey) {
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("select infos.initialLaunch from ").append(UserCourseInfosImpl.class.getName()).append(" as infos ")
			  .append(" inner join infos.resource as resource")
			  .append(" where infos.identity.key=:identityKey and resource.resId=:resId and resource.resName='CourseModule'");

			List<Date> infoList = dbInstance.getCurrentEntityManager()
					.createQuery(sb.toString(), Date.class)
					.setParameter("identityKey", identityKey)
					.setParameter("resId", courseResourceId)
					.getResultList();

			if(infoList.isEmpty()) {
				return null;
			}
			return infoList.get(0);
		} catch (Exception e) {
			logError("Cannot retrieve course informations for: " + courseResourceId, e);
			return null;
		}
	}

	/**
	 * Return a map of identity keys to initial launch date.
	 * @param courseEnv
	 * @param identities
	 * @return
	 */
	@Override
	public Map<Long,Date> getInitialLaunchDates(Long courseResourceId, List<Identity> identities) {
		if(identities == null || identities.isEmpty()) {
			return new HashMap<Long,Date>();
		}
		try {
			List<Long> identityKeys = PersistenceHelper.toKeys(identities);

			StringBuilder sb = new StringBuilder();
			sb.append("select infos.identity.key, infos.initialLaunch from ").append(UserCourseInfosImpl.class.getName()).append(" as infos ")
			  .append(" inner join infos.resource as resource")
			  .append(" where resource.resId=:resId and resource.resName='CourseModule'");
			
			Set<Long> identityKeySet = null;
			if(identityKeys.size() < 100) {
				sb.append(" and infos.identity.key in (:identityKeys)");
				identityKeySet = new HashSet<Long>(identityKeys);
			}

			TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Object[].class)
					.setParameter("resId", courseResourceId);
			if(identityKeys.size() < 100) {
				query.setParameter("identityKeys", identityKeys);
			}

			List<Object[]> infoList = query.getResultList();
			Map<Long,Date> dateMap = new HashMap<Long,Date>();
			for(Object[] infos:infoList) {
				Long identityKey = (Long)infos[0];
				if(identityKeySet == null || identityKeySet.contains(identityKey)) {
					Date initialLaunch = (Date)infos[1];
					dateMap.put(identityKey, initialLaunch);
				}
			}
			return dateMap;
		} catch (Exception e) {
			logError("Cannot retrieve course informations for: " + courseResourceId, e);
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
			logError("Cannot Delete course informations for: " + entry, e);
			return -1;
		}
	}
}
/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.course.assessment.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.FlushMode;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBQuery;
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

			DBQuery query = dbInstance.createQuery(sb.toString());
			query.setLong("identityKey", identity.getKey());
			query.setLong("resId", courseResourceId);
			@SuppressWarnings("unchecked")
			List<UserCourseInfosImpl> infoList = query.list();
			if(infoList.isEmpty()) {
				return null;
			}
			return infoList.get(0);
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

			DBQuery query = dbInstance.createQuery(sb.toString());

			query.setLong("identityKey", identityKey);
			query.setLong("resId", courseResourceId);
			@SuppressWarnings("unchecked")
			List<Date> infoList = query.list();
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
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("select infos.identity.key, infos.initialLaunch from ").append(UserCourseInfosImpl.class.getName()).append(" as infos ")
			  .append(" inner join infos.resource as resource")
			  .append(" where infos.identity.key in (:identityKeys) and resource.resId=:resId and resource.resName='CourseModule'");

			DBQuery query = dbInstance.createQuery(sb.toString());
			List<Long> identityKeys = new ArrayList<Long>();
			for(Identity identity:identities) {
				identityKeys.add(identity.getKey());
			}
			query.setParameterList("identityKeys", identityKeys);
			query.setLong("resId", courseResourceId);
			@SuppressWarnings("unchecked")
			List<Object[]> infoList = query.list();
			Map<Long,Date> dateMap = new HashMap<Long,Date>();
			for(Object[] infos:infoList) {
				Long identityKey = (Long)infos[0];
				Date initialLaunch = (Date)infos[1];
				dateMap.put(identityKey, initialLaunch);
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

			DBQuery query = dbInstance.createQuery(sb.toString());
			query.setLong("resourceKey", entry.getOlatResource().getKey());
			return query.executeUpdate(FlushMode.AUTO);
		} catch (Exception e) {
			logError("Cannot Delete course informations for: " + entry, e);
			return -1;
		}
	}
}
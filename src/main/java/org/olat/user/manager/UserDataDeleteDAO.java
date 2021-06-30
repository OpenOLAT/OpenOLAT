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
package org.olat.user.manager;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.Tracing;
import org.olat.user.UserDataDelete;
import org.olat.user.model.UserDataDeleteImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 30 juin 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class UserDataDeleteDAO {
	
	private static final Logger log = Tracing.createLoggerFor(UserDataDeleteDAO.class);
	
	@Autowired
	private DB dbInstance;
	
	public UserDataDelete create(String userData, String resourceIds) {
		UserDataDeleteImpl data = new UserDataDeleteImpl();
		data.setCreationDate(new Date());
		data.setLastModified(data.getCreationDate());
		data.setUserData(userData);
		data.setResourceIds(resourceIds);
		dbInstance.getCurrentEntityManager().persist(data);
		return data;
	}
	
	public List<UserDataDelete> getWithoutResourceIds() {
		String query = "select data from userdatadelete as data where data.resourceIds=:data";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, UserDataDelete.class)
				.setParameter("data", "all")
				.getResultList();
	}
	
	public List<UserDataDelete> getWithResourceIds() {
		String query = "select data from userdatadelete as data where data.resourceIds<>:data";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, UserDataDelete.class)
				.setParameter("data", "all")
				.getResultList();
	}
	
	public int updateCurrentCourseDir(List<UserDataDelete> deletionList, String currentResourceId) {
		if(deletionList == null || deletionList.isEmpty()) return 0;
		
		List<Long> keys = deletionList.stream()
				.map(UserDataDelete::getKey)
				.collect(Collectors.toList());

		String query = "update userdatadelete set currentResourceId=:id where key in (:keys)";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("id", currentResourceId)
				.setParameter("keys", keys)
				.executeUpdate();
	}
	
	public void deleteUserData(UserDataDelete userData) {
		try {
			UserDataDelete reloadedData = dbInstance.getCurrentEntityManager()
					.getReference(UserDataDeleteImpl.class, userData.getKey());
			dbInstance.getCurrentEntityManager().remove(reloadedData);
		} catch (EntityNotFoundException e) {
			log.error("Entity not found", e);
		}
	}
}

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
package org.olat.core.commons.services.doceditor.manager;

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.doceditor.UserInfo;
import org.olat.core.commons.services.doceditor.model.UserInfoImpl;
import org.olat.core.id.Identity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * Initial date: 25 Aug 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Component
public class UserInfoDAO {
	
	@Autowired
	private DB dbInstance;
	
	public UserInfo create(Identity identity, String info) {
		UserInfoImpl userInfo = new UserInfoImpl();
		userInfo.setCreationDate(new Date());
		userInfo.setLastModified(userInfo.getCreationDate());
		userInfo.setIdentity(identity);
		userInfo.setInfo(info);
		dbInstance.getCurrentEntityManager().persist(userInfo);
		return userInfo;
	}
	
	public UserInfo save(UserInfo userInfo) {
		if (userInfo instanceof UserInfoImpl userInfoImpl) {
			userInfoImpl.setLastModified(new Date());
			return dbInstance.getCurrentEntityManager().merge(userInfoImpl);
		}
		return userInfo;
	}
	
	public void delete(Identity identity) {
		String query = "delete from doceditoruserinfo userinfo where userinfo.identity.key = :identityKey";
		
		dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("identityKey", identity.getKey())
				.executeUpdate();
	}

	public UserInfo load(Identity identity) {
		String query = "select userinfo from doceditoruserinfo userinfo where userinfo.identity.key = :identityKey";
		
		List<UserInfo> userInfos = dbInstance.getCurrentEntityManager()
				.createQuery(query, UserInfo.class)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
		return userInfos.isEmpty()? null : userInfos.get(0);
	}

}

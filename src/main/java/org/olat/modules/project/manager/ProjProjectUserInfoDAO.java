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
package org.olat.modules.project.manager;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjProjectRef;
import org.olat.modules.project.ProjProjectUserInfo;
import org.olat.modules.project.model.ProjProjectUserInfoImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * Initial date: 28 Nov 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Component
public class ProjProjectUserInfoDAO {
	
	@Autowired
	private DB dbInstance;
	
	public ProjProjectUserInfo create(ProjProject project, Identity identity) {
		ProjProjectUserInfoImpl projectUserInfo = new ProjProjectUserInfoImpl();
		projectUserInfo.setCreationDate(new Date());
		projectUserInfo.setLastModified(projectUserInfo.getCreationDate());
		projectUserInfo.setProject(project);
		projectUserInfo.setIdentity(identity);
		dbInstance.getCurrentEntityManager().persist(projectUserInfo);
		return projectUserInfo;
	}
	
	public ProjProjectUserInfo save(ProjProjectUserInfo projectUserInfo) {
		if (projectUserInfo instanceof ProjProjectUserInfoImpl) {
			ProjProjectUserInfoImpl impl = (ProjProjectUserInfoImpl)projectUserInfo;
			impl.setLastModified(new Date());
			dbInstance.getCurrentEntityManager().merge(projectUserInfo);
		}
		return projectUserInfo;
	}

	public void delete(ProjProjectUserInfo projectUserInfo) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("delete from projprojectuserinfo userinfo");
		sb.and().append(" userinfo.key = :projectUserInfoKey");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("projectUserInfoKey", projectUserInfo.getKey())
				.executeUpdate();
	}

	public List<ProjProjectUserInfo> loadProjectUserInfos(ProjProjectRef project, Collection<? extends IdentityRef> identities) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select projectUserInfo");
		sb.append("  from projprojectuserinfo projectUserInfo");
		sb.and().append("projectUserInfo.project.key = :projectKey");
		sb.and().append("projectUserInfo.identity.key in :identityKeys");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ProjProjectUserInfo.class)
				.setParameter("projectKey", project.getKey())
				.setParameter("identityKeys", identities.stream().map(IdentityRef::getKey).collect(Collectors.toSet()))
				.getResultList();
	}

}

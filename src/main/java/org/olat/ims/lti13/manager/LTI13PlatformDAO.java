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
package org.olat.ims.lti13.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.ims.lti13.LTI13Platform;
import org.olat.ims.lti13.model.LTI13PlatformImpl;
import org.olat.ims.lti13.model.LTI13PlatformWithInfos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 23 févr. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class LTI13PlatformDAO {
	
	@Autowired
	private DB dbInstance;
	
	public List<LTI13PlatformWithInfos> getPlatforms() {
		StringBuilder sb = new StringBuilder(512);
		sb.append("select platform,")
		  .append(" (select count(deployment.key) from ltisharedtooldeployment as deployment")
		  .append("   where deployment.platform.key=platform.key")
		  .append(" ) as numOfDeployments")
		  .append(" from ltiplatform as platform");
		
		List<Object[]> rawObjects = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Object[].class)
			.getResultList();
		List<LTI13PlatformWithInfos> infos = new ArrayList<>();
		for(Object[] objects:rawObjects) {
			LTI13Platform tool = (LTI13Platform)objects[0];
			long numOfDeployments = PersistenceHelper.extractPrimitiveLong(objects, 1);
			infos.add(new LTI13PlatformWithInfos(tool, numOfDeployments));
		}
		return infos;
	}
	
	public LTI13Platform loadByClientId(String issuer, String clientId) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("select platform from ltiplatform as platform")
		  .append(" where platform.clientId=:clientId and platform.issuer=:issuer");
		
		List<LTI13Platform> tools = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), LTI13Platform.class)
			.setParameter("clientId", clientId)
			.setParameter("issuer", issuer)
			.getResultList();
		return tools != null && !tools.isEmpty() ? tools.get(0) : null;
	}
	
	public List<LTI13Platform> loadByKid(String kid) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("select platform from ltiplatform as platform")
		  .append(" where platform.keyId=:keyId");
		
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), LTI13Platform.class)
			.setParameter("keyId", kid)
			.getResultList();
	}
	
	public LTI13Platform loadByKey(Long key) {
		String sb = "select platform from ltiplatform as platform where platform.key=:key";
		List<LTI13Platform> tools = dbInstance.getCurrentEntityManager()
			.createQuery(sb, LTI13Platform.class)
			.setParameter("key", key)
			.getResultList();
		return tools != null && !tools.isEmpty() ? tools.get(0) : null;
	}
	
	public LTI13Platform updatePlatform(LTI13Platform tool) {
		if(tool.getKey() == null) {
			((LTI13PlatformImpl)tool).setCreationDate(new Date());
			((LTI13PlatformImpl)tool).setLastModified(tool.getCreationDate());
			dbInstance.getCurrentEntityManager().persist(tool);
		} else {
			((LTI13PlatformImpl)tool).setLastModified(new Date());
			tool = dbInstance.getCurrentEntityManager().merge(tool);
		}
		return tool;
	}
}

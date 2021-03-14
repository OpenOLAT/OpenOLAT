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
import org.olat.group.BusinessGroupRef;
import org.olat.ims.lti13.LTI13SharedTool;
import org.olat.ims.lti13.model.LTI13SharedToolImpl;
import org.olat.ims.lti13.model.LTI13SharedToolWithInfos;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 23 févr. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class LTI13SharedToolDAO {
	
	@Autowired
	private DB dbInstance;
	
	public List<LTI13SharedToolWithInfos> getSharedTools(RepositoryEntryRef entry) {
		StringBuilder sb = new StringBuilder(512);
		sb.append("select tool,")
		  .append(" (select count(deployment.key) from ltisharedtooldeployment as deployment")
		  .append("   where deployment.sharedTool.key=tool.key")
		  .append(" ) as numOfDeployments")
		  .append(" from ltisharedtool as tool")
		  .append(" inner join fetch tool.entry as v")
		  .append(" inner join fetch v.olatResource as ores")
		  .append(" inner join fetch v.statistics as statistics")
		  .append(" left join fetch v.lifecycle as lifecycle")
		  .append(" where tool.entry.key=:entryKey");
		
		List<Object[]> rawObjects = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Object[].class)
			.setParameter("entryKey", entry.getKey())
			.getResultList();
		List<LTI13SharedToolWithInfos> infos = new ArrayList<>();
		for(Object[] objects:rawObjects) {
			LTI13SharedTool tool = (LTI13SharedTool)objects[0];
			long numOfDeployments = PersistenceHelper.extractPrimitiveLong(objects, 1);
			infos.add(new LTI13SharedToolWithInfos(tool, numOfDeployments));
		}
		return infos;
	}
	
	public List<LTI13SharedToolWithInfos> getSharedTools(BusinessGroupRef businessGroup) {
		StringBuilder sb = new StringBuilder(512);
		sb.append("select tool,")
		  .append(" (select count(deployment.key) from ltisharedtooldeployment as deployment")
		  .append("   where deployment.sharedTool.key=tool.key")
		  .append(" ) as numOfDeployments")
		  .append(" from ltisharedtool as tool")
		  .append(" inner join fetch tool.businessGroup as bGroup")
		  .append(" inner join fetch bGroup.resource as ores")
		  .append(" where bGroup.key=:businessGroupKey");
		
		List<Object[]> rawObjects = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Object[].class)
			.setParameter("businessGroupKey", businessGroup.getKey())
			.getResultList();
		List<LTI13SharedToolWithInfos> infos = new ArrayList<>();
		for(Object[] objects:rawObjects) {
			LTI13SharedTool tool = (LTI13SharedTool)objects[0];
			long numOfDeployments = PersistenceHelper.extractPrimitiveLong(objects, 1);
			infos.add(new LTI13SharedToolWithInfos(tool, numOfDeployments));
		}
		return infos;
	}
	
	public LTI13SharedTool loadByClientId(String issuer, String clientId) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("select tool from ltisharedtool as tool")
		  .append(" left join fetch tool.entry as v")
		  .append(" left join fetch v.olatResource as ores")
		  .append(" left join fetch v.statistics as statistics")
		  .append(" left join fetch v.lifecycle as lifecycle")
		  .append(" where tool.clientId=:clientId and tool.issuer=:issuer");
		
		List<LTI13SharedTool> tools = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), LTI13SharedTool.class)
			.setParameter("clientId", clientId)
			.setParameter("issuer", issuer)
			.getResultList();
		return tools != null && !tools.isEmpty() ? tools.get(0) : null;
	}
	
	public LTI13SharedTool loadByKey(Long key) {
		String sb = "select tool from ltisharedtool as tool where tool.key=:key";
		List<LTI13SharedTool> tools = dbInstance.getCurrentEntityManager()
			.createQuery(sb, LTI13SharedTool.class)
			.setParameter("key", key)
			.getResultList();
		return tools != null && !tools.isEmpty() ? tools.get(0) : null;
	}
	
	public LTI13SharedTool updateSharedTool(LTI13SharedTool tool) {
		if(tool.getKey() == null) {
			((LTI13SharedToolImpl)tool).setCreationDate(new Date());
			((LTI13SharedToolImpl)tool).setLastModified(tool.getCreationDate());
			dbInstance.getCurrentEntityManager().persist(tool);
		} else {
			((LTI13SharedToolImpl)tool).setLastModified(new Date());
			tool = dbInstance.getCurrentEntityManager().merge(tool);
		}
		return tool;
	}
	
	public void deleteSharedTools(RepositoryEntryRef entry) {
		String sb = "delete from ltisharedtool as tool where tool.entry.key=:entryKey";
		dbInstance.getCurrentEntityManager()
			.createQuery(sb)
			.setParameter("entryKey", entry.getKey())
			.executeUpdate();
	}
	
	public void deleteSharedTools(BusinessGroupRef businessGroup) {
		String sb = "delete from ltisharedtool as tool where tool.businessGroup.key=:groupKey";
		dbInstance.getCurrentEntityManager()
			.createQuery(sb)
			.setParameter("groupKey", businessGroup.getKey())
			.executeUpdate();
	}
}

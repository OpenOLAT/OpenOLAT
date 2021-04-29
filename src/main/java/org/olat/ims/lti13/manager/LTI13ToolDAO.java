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

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.ims.lti13.LTI13Tool;
import org.olat.ims.lti13.LTI13ToolType;
import org.olat.ims.lti13.model.LTI13ToolImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 22 févr. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class LTI13ToolDAO {
	
	@Autowired
	private DB dbInstance;
	
	public LTI13Tool createTool(String toolName, String toolUrl, String clientId,
			String initiateLoginUrl, String redirectUrl, LTI13ToolType type) {
		LTI13ToolImpl tool = new LTI13ToolImpl();
		tool.setCreationDate(new Date());
		tool.setLastModified(tool.getCreationDate());
		tool.setClientId(clientId);
		tool.setToolName(toolName);
		tool.setToolUrl(toolUrl);
		tool.setToolTypeEnum(type);
		tool.setInitiateLoginUrl(initiateLoginUrl);
		tool.setRedirectUrl(redirectUrl);
		dbInstance.getCurrentEntityManager().persist(tool);
		return tool;
	}
	
	public List<LTI13Tool> getTools(LTI13ToolType type) {
		String sb = "select tool from ltitool as tool where tool.toolType=:type";
		
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb, LTI13Tool.class)
			.setParameter("type", type.name())
			.getResultList();
	}
	
	public LTI13Tool updateTool(LTI13Tool tool) {
		((LTI13ToolImpl)tool).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(tool);
	}
	
	public void deleteTool(LTI13Tool tool) {
		dbInstance.getCurrentEntityManager().remove(tool);
	}
	
	public LTI13Tool loadToolByKey(Long key) {
		String sb = "select tool from ltitool as tool where tool.key=:toolKey";
		
		List<LTI13Tool> tools = dbInstance.getCurrentEntityManager()
			.createQuery(sb, LTI13Tool.class)
			.setParameter("toolKey", key)
			.getResultList();
		return tools != null && !tools.isEmpty() ? tools.get(0) : null;
	}
	
	public List<LTI13Tool> loadToolsByClientId(String clientId) {
		String sb = "select tool from ltitool as tool where tool.clientId=:clientId";
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb, LTI13Tool.class)
			.setParameter("clientId", clientId)
			.getResultList();
	}
	
	public LTI13Tool loadToolBy(String toolUrl, String clientId) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select tool from ltitool as tool")
		  .append(" where tool.toolUrl=:toolUrl and tool.clientId=:clientId");
		
		List<LTI13Tool> tools = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), LTI13Tool.class)
			.setParameter("toolUrl", toolUrl)
			.setParameter("clientId", clientId)
			.getResultList();
		return tools != null && !tools.isEmpty() ? tools.get(0) : null;
	}
}

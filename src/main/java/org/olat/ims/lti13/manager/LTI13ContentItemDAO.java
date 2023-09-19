/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.ims.lti13.manager;

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.ims.lti13.LTI13ContentItem;
import org.olat.ims.lti13.LTI13ContentItemTypesEnum;
import org.olat.ims.lti13.LTI13Tool;
import org.olat.ims.lti13.LTI13ToolDeployment;
import org.olat.ims.lti13.model.LTI13ContentItemImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 7 sept. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Service
public class LTI13ContentItemDAO {
	

	@Autowired
	private DB dbInstance;
	
	public LTI13ContentItem createItem(LTI13ContentItemTypesEnum type, LTI13Tool tool, LTI13ToolDeployment deployment) {
		LTI13ContentItemImpl item = new LTI13ContentItemImpl();
		item.setCreationDate(new Date());
		item.setLastModified(item.getCreationDate());
		item.setType(type);
		item.setTool(tool);
		item.setDeployment(deployment);
		return item;
	}
	
	public void persistItem(LTI13ContentItem item) {
		dbInstance.getCurrentEntityManager().persist(item);
	}
	
	public LTI13ContentItem updateItem(LTI13ContentItem item) {
		item.setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(item);
	}
	
	public void deleteItem(LTI13ContentItem item) {
		dbInstance.getCurrentEntityManager().remove(item);
	}
	
	public LTI13ContentItem loadItemByKey(Long key) {
		String query = """
				select item from lticontentitem as item
				left join fetch item.tool as tool
				left join fetch item.deployment as deployment
				left join fetch deployment.tool as deploymentTool
				where item.key=:itemKey""";
		
		List<LTI13ContentItem> items = dbInstance.getCurrentEntityManager().createQuery(query, LTI13ContentItem.class)
				.setParameter("itemKey", key)
				.getResultList();
		return items == null || items.isEmpty() ? null : items.get(0);
	}
	
	public List<LTI13ContentItem> loadItemByTool(LTI13ToolDeployment deployment) {
		String query = "select item from lticontentitem as item where item.deployment.key=:deploymentKey";
		return dbInstance.getCurrentEntityManager().createQuery(query, LTI13ContentItem.class)
				.setParameter("deploymentKey", deployment.getKey())
				.getResultList();
	}
}

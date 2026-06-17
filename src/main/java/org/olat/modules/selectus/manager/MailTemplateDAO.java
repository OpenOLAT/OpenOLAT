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
package org.olat.modules.selectus.manager;

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionMailTemplate;
import org.olat.modules.selectus.model.PositionRef;
import org.olat.modules.selectus.model.mail.PositionMailTemplateImpl;

/**
 * 
 * Initial date: 24 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class MailTemplateDAO {
	
	@Autowired
	private DB dbInstance;
	
	public PositionMailTemplate createTemplate(Position position, String id, String name) {
		PositionMailTemplateImpl template = new PositionMailTemplateImpl();
		template.setCreationDate(new Date());
		template.setLastModified(template.getCreationDate());
		template.setId(id);
		template.setName(name);
		template.setPosition(position);
		dbInstance.getCurrentEntityManager().persist(template);
		return template;
	}
	
	public List<PositionMailTemplate> getTemplates(PositionRef position) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select template from rmailtemplate as template")
		  .append(" where template.position.key=:positionKey")
		  .append(" order by template.name asc, template.key asc");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), PositionMailTemplate.class)
				.setParameter("positionKey", position.getKey())
				.getResultList();
	}
	
	public PositionMailTemplate getTemplate(PositionMailTemplate template) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select template from rmailtemplate as template")
		  .append(" inner join fetch template.position as pos")
		  .append(" where template.key=:templateKey");
		List<PositionMailTemplate> templates = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), PositionMailTemplate.class)
				.setParameter("templateKey", template.getKey())
				.getResultList();
		return templates == null || templates.isEmpty() ? null : templates.get(0);
	}
	
	public PositionMailTemplate updateTemplate(PositionMailTemplate template) {
		((PositionMailTemplateImpl)template).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(template);
	}
	
	public void deleteTemplate(PositionMailTemplate template) {
		PositionMailTemplate templateToDelete = dbInstance.getCurrentEntityManager()
				.getReference(PositionMailTemplateImpl.class, template.getKey());
		dbInstance.getCurrentEntityManager().remove(templateToDelete);
	}
	
	public int deleteTemplates(PositionRef position) {
		String q = "delete from rmailtemplate as template where template.position.key=:positionKey";
		return dbInstance.getCurrentEntityManager().createQuery(q)
			.setParameter("positionKey", position.getKey())
			.executeUpdate();
	}

}

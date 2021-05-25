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
package org.olat.modules.bigbluebutton.manager;

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.modules.bigbluebutton.BigBlueButtonMeetingTemplate;
import org.olat.modules.bigbluebutton.JoinPolicyEnum;
import org.olat.modules.bigbluebutton.model.BigBlueButtonMeetingTemplateImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 19 mars 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class BigBlueButtonMeetingTemplateDAO {
	
	@Autowired
	private DB dbInstance;
	
	public BigBlueButtonMeetingTemplate createTemplate(String name, String externalId, boolean system) {
		BigBlueButtonMeetingTemplateImpl template = new BigBlueButtonMeetingTemplateImpl();
		template.setCreationDate(new Date());
		template.setLastModified(template.getCreationDate());
		template.setName(name);
		template.setSystem(system);
		template.setEnabled(true);
		template.setExternalId(externalId);
		template.setJoinPolicyEnum(JoinPolicyEnum.disabled);
		dbInstance.getCurrentEntityManager().persist(template);
		return template;
	}
	
	public BigBlueButtonMeetingTemplate updateTemplate(BigBlueButtonMeetingTemplate template) {
		((BigBlueButtonMeetingTemplateImpl)template).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(template);
	}
	
	public void deleteTemplate(BigBlueButtonMeetingTemplate template) {
		BigBlueButtonMeetingTemplate templateToDelete = dbInstance.getCurrentEntityManager()
			.getReference(BigBlueButtonMeetingTemplateImpl.class, template.getKey());
		dbInstance.getCurrentEntityManager().remove(templateToDelete);
	}
	
	public BigBlueButtonMeetingTemplate getTemplate(BigBlueButtonMeetingTemplate template) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select template from bigbluebuttontemplate as template")
		  .append(" where template.key=:templateKey");

		List<BigBlueButtonMeetingTemplate> templates = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), BigBlueButtonMeetingTemplate.class)
				.setParameter("templateKey", template.getKey())
				.getResultList();
		return templates == null || templates.isEmpty() ? null : templates.get(0);
	}
	
	public boolean isTemplateInUse(BigBlueButtonMeetingTemplate template) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select meeting.key from bigbluebuttonmeeting as meeting")
		  .append(" where meeting.template.key=:templateKey");

		List<Long> templates = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("templateKey", template.getKey())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return templates != null && !templates.isEmpty()
				&& templates.get(0) != null && templates.get(0).longValue() > 0;
	}
	
	public List<BigBlueButtonMeetingTemplate> getTemplates() {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select template from bigbluebuttontemplate as template")
		  .append(" order by lower(template.name) asc");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), BigBlueButtonMeetingTemplate.class)
				.getResultList();
	}

}

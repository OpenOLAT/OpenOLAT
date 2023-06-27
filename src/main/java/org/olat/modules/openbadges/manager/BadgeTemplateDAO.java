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
package org.olat.modules.openbadges.manager;

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.modules.openbadges.BadgeTemplate;
import org.olat.modules.openbadges.model.BadgeTemplateImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initial date: 2023-05-16<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Service
public class BadgeTemplateDAO {

	@Autowired
	private DB dbInstance;

	public BadgeTemplate createTemplate(String identifier, String image, String name) {
		BadgeTemplateImpl badgeTemplate = new BadgeTemplateImpl();
		badgeTemplate.setCreationDate(new Date());
		badgeTemplate.setLastModified(badgeTemplate.getCreationDate());
		badgeTemplate.setIdentifier(identifier);
		badgeTemplate.setImage(image);
		badgeTemplate.setName(name);
		dbInstance.getCurrentEntityManager().persist(badgeTemplate);
		return badgeTemplate;
	}

	public BadgeTemplate getTemplate(Long key) {
		return dbInstance.getCurrentEntityManager().find(BadgeTemplateImpl.class, key);
	}

	public List<BadgeTemplate> getTemplates() {
		String q = "select template from badgetemplate template order by template.name asc";
		return dbInstance.getCurrentEntityManager().createQuery(q, BadgeTemplate.class).getResultList();
	}

	public BadgeTemplate updateTemplate(BadgeTemplate template) {
		template.setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(template);
	}

	public void deleteTemplate(BadgeTemplate template) {
		dbInstance.deleteObject(template);
	}
}

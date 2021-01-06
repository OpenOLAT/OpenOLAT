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
package org.olat.course.nodes.livestream.manager;

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.course.nodes.livestream.model.UrlTemplate;
import org.olat.course.nodes.livestream.model.UrlTemplateImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * Initial date: 4 Jan 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Component
public class UrlTemplateDAO {
	
	@Autowired
	private DB dbInstance;

	public UrlTemplate create(String name) {
		UrlTemplateImpl urlTemplate = new UrlTemplateImpl();
		urlTemplate.setCreationDate(new Date());
		urlTemplate.setLastModified(urlTemplate.getCreationDate());
		urlTemplate.setName(name);
		dbInstance.getCurrentEntityManager().persist(urlTemplate);
		return urlTemplate;
	}

	public UrlTemplate update(UrlTemplate urlTemplate) {
		if (urlTemplate instanceof UrlTemplateImpl) {
			UrlTemplateImpl impl = (UrlTemplateImpl)urlTemplate;
			impl.setLastModified(new Date());
			dbInstance.getCurrentEntityManager().merge(impl);
		}
		return urlTemplate;
	}
	
	public List<UrlTemplate> loadAll() {
		String query = "select urlTemplate from livestreamurltemplate urlTemplate";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, UrlTemplate.class)
				.getResultList();
	}

	public UrlTemplate loadByKey(Long key) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select urlTemplate");
		sb.append("  from livestreamurltemplate urlTemplate");
		sb.and().append("urlTemplate.key = :key");
		
		List<UrlTemplate> urlTemplates  = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), UrlTemplate.class)
				.setParameter("key", key)
				.getResultList();
		return !urlTemplates.isEmpty()? urlTemplates.get(0): null;
	}

	public void delete(UrlTemplate urlTemplate) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("delete from livestreamurltemplate urlTemplate");
		sb.and().append("urlTemplate.key = :key");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("key", urlTemplate.getKey())
				.executeUpdate();
	}

}

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
package org.olat.course.assessment.manager;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.course.assessment.SafeExamBrowserTemplate;
import org.olat.course.assessment.SafeExamBrowserTemplateSearchParams;
import org.olat.course.assessment.model.SafeExamBrowserTemplateImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Initial date: 19 Feb 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
@Service
public class SafeExamBrowserTemplateDAO {

	@Autowired
	private DB dbInstance;

	public SafeExamBrowserTemplate createTemplate(String name) {
		SafeExamBrowserTemplateImpl sebTemplate = new SafeExamBrowserTemplateImpl();
		sebTemplate.setCreationDate(new Date());
		sebTemplate.setLastModified(sebTemplate.getCreationDate());
		sebTemplate.setActive(true);
		sebTemplate.setDefault(false);
		sebTemplate.setName(name);
		dbInstance.getCurrentEntityManager().persist(sebTemplate);
		return sebTemplate;
	}

	public SafeExamBrowserTemplate updateTemplate(SafeExamBrowserTemplate sebTemplate) {
		((SafeExamBrowserTemplateImpl)sebTemplate).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(sebTemplate);
	}

	public void deleteTemplate(SafeExamBrowserTemplate sebTemplate) {
		SafeExamBrowserTemplate ref = dbInstance.getCurrentEntityManager()
				.find(SafeExamBrowserTemplateImpl.class, sebTemplate.getKey());
		if(ref != null) {
			dbInstance.getCurrentEntityManager().remove(ref);
		}
	}

	public List<SafeExamBrowserTemplate> loadTemplates(SafeExamBrowserTemplateSearchParams params) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select template from courseassessmentsebtemplate as template");

		if(params.getKeys() != null && !params.getKeys().isEmpty()) {
			sb.and().append("template.key in :keys");
		}
		if(params.getActive() != null) {
			sb.and().append("template.active=:activeVal");
		}
		if(params.getDefault() != null) {
			sb.and().append("template.isDefault=:defaultVal");
		}

		TypedQuery<SafeExamBrowserTemplate> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), SafeExamBrowserTemplate.class);

		if(params.getKeys() != null && !params.getKeys().isEmpty()) {
			query.setParameter("keys", params.getKeys());
		}
		if(params.getActive() != null) {
			query.setParameter("activeVal", params.getActive().booleanValue());
		}
		if(params.getDefault() != null) {
			query.setParameter("defaultVal", params.getDefault().booleanValue());
		}

		return query.getResultList();
	}

	public Map<Long, Long> getTemplateToUsageCount() {
		String query = """
				select mode.safeExamBrowserTemplate.key, count(mode.key)
				  from courseassessmentmode as mode
				 where mode.safeExamBrowserTemplate is not null
				 group by mode.safeExamBrowserTemplate.key
				 union all
				select config.safeExamBrowserTemplate.key, count(config.key)
				  from courseassessmentinspectionconfig as config
				 where config.safeExamBrowserTemplate is not null
				 group by config.safeExamBrowserTemplate.key
				""";

		List<Object[]> rawResults = dbInstance.getCurrentEntityManager()
				.createQuery(query, Object[].class)
				.getResultList();

		Map<Long, Long> counts = new HashMap<>();
		for (Object[] row : rawResults) {
			counts.merge((Long) row[0], (Long) row[1], Long::sum);
		}
		return counts;
	}

}

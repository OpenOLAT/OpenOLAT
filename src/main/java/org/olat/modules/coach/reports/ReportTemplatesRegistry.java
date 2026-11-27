/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.coach.reports;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Finds the report templates by their identifier. The report tasks store
 * only the identifier of their template.
 *
 * Initial date: 25 Nov 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
@Service
public class ReportTemplatesRegistry {

	private static final Logger log = Tracing.createLoggerFor(ReportTemplatesRegistry.class);

	private final Map<String, ReportConfiguration> templates = new HashMap<>();

	@Autowired
	public ReportTemplatesRegistry(List<ReportConfiguration> reportConfigurations) {
		for(ReportConfiguration reportConfiguration:reportConfigurations) {
			String id = reportConfiguration.getId();
			if(!StringHelper.containsNonWhitespace(id)) {
				log.error("Report template without identifier: {}", reportConfiguration.getClass().getName());
			} else if(templates.containsKey(id)) {
				log.error("Duplicate report template identifier, skipped: {}", id);
			} else {
				templates.put(id, reportConfiguration);
			}
		}
	}

	/**
	 * @param id The identifier of the template
	 * @return The template or null if not found
	 */
	public ReportConfiguration getTemplate(String id) {
		return id == null ? null : templates.get(id);
	}

}

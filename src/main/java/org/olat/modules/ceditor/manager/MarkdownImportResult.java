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
package org.olat.modules.ceditor.manager;

import java.util.List;

import org.olat.modules.ceditor.model.jpa.ContainerPart;

/**
 * Result of a markdown-to-page conversion and persistence.
 *
 * @param warnings        Warning i18n keys with tab-separated arguments for partial conversion issues
 * @param container       The ContainerPart that wraps the imported parts (newly created or pre-existing);
 *                        {@code null} if the markdown produced no parts.
 * @param aiMetadataJobs  Number of asynchronous AI metadata generation tasks scheduled for imported images
 *
 * Initial date: 2026-03-11<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 */
public record MarkdownImportResult(
	List<String> warnings,
	ContainerPart container,
	int aiMetadataJobs
) {
	public boolean hasWarnings() {
		return warnings != null && !warnings.isEmpty();
	}
}

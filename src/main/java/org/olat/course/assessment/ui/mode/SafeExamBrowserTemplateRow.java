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
package org.olat.course.assessment.ui.mode;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.course.assessment.SafeExamBrowserTemplate;
import org.olat.course.assessment.model.SafeExamBrowserConfiguration;

/**
 *
 * Initial date: 19 Feb 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class SafeExamBrowserTemplateRow {

	private final SafeExamBrowserTemplate template;
	private final SafeExamBrowserConfiguration configuration;
	private final Long usage;
	private FormLink toolsButton;

	public SafeExamBrowserTemplateRow(SafeExamBrowserTemplate template, SafeExamBrowserConfiguration configuration, Long usage) {
		this.template = template;
		this.configuration = configuration;
		this.usage = usage;
	}

	public SafeExamBrowserTemplate getTemplate() {
		return template;
	}

	public Long getKey() {
		return template.getKey();
	}

	public String getName() {
		return template.getName();
	}

	public boolean isActive() {
		return template.isActive();
	}

	public boolean isDefault() {
		return template.isDefault();
	}

	public Long getUsage() {
		return usage;
	}

	public SafeExamBrowserConfiguration getConfiguration() {
		return configuration;
	}

	public FormLink getToolsButton() {
		return toolsButton;
	}

	public void setToolsButton(FormLink toolsButton) {
		this.toolsButton = toolsButton;
	}

}

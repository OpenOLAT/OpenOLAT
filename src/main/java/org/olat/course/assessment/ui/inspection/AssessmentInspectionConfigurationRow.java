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
package org.olat.course.assessment.ui.inspection;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.course.assessment.AssessmentInspectionConfiguration;

/**
 * 
 * Initial date: 15 déc. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentInspectionConfigurationRow {
	
	private FormLink infosButton;
	private FormLink toolsButton;
	private final long usage;
	private final AssessmentInspectionConfiguration configuration;
	
	public AssessmentInspectionConfigurationRow(AssessmentInspectionConfiguration configuration, long usage) {
		this.configuration = configuration;
		this.usage = usage;
	}
	
	public Long getKey() {
		return configuration.getKey();
	}
	
	public String getName() {
		return configuration.getName();
	}
	
	public long getUsage() {
		return usage;
	}
	
	public AssessmentInspectionConfiguration getConfiguration() {
		return configuration;
	}

	public FormLink getInfosButton() {
		return infosButton;
	}

	public void setInfosButton(FormLink button) {
		this.infosButton = button;
	}

	public FormLink getToolsButton() {
		return toolsButton;
	}

	public void setToolsButton(FormLink toolsButton) {
		this.toolsButton = toolsButton;
	}
}

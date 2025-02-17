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
package org.olat.modules.curriculum.ui;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryStatusEnum;

/**
 * 
 * Initial date: 14 févr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementRepositoryRow implements RepositoryEntryRef {
	
	private final RepositoryEntry entry;
	
	private FormLink toolsLink;
	private FormLink resourcesLink;
	private FormLink instantiateLink;
	
	public CurriculumElementRepositoryRow(RepositoryEntry entry) {
		this.entry = entry;
	}

	@Override
	public Long getKey() {
		return entry.getKey();
	}
	
	public String getDisplayname() {
		return entry.getDisplayname();
	}
	
	public String getExternalRef() {
		return entry.getExternalRef();
	}
	
	public String getInitialAuthor() {
		return entry.getInitialAuthor();
	}
	
	public RepositoryEntryStatusEnum getEntryStatus() {
		return entry.getEntryStatus();
	}
	
	public RepositoryEntry getRepositoryEntry() {
		return entry;
	}

	public FormLink getToolsLink() {
		return toolsLink;
	}

	public void setToolsLink(FormLink toolsLink) {
		this.toolsLink = toolsLink;
	}

	public FormLink getResourcesLink() {
		return resourcesLink;
	}

	public void setResourcesLink(FormLink resourcesLink) {
		this.resourcesLink = resourcesLink;
	}

	public FormLink getInstantiateLink() {
		return instantiateLink;
	}

	public void setInstantiateLink(FormLink instantiateLink) {
		this.instantiateLink = instantiateLink;
	}
}

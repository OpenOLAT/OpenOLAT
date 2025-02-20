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
package org.olat.modules.curriculum.ui.copy;

import org.olat.modules.curriculum.model.CurriculumCopySettings.CopyResources;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.model.RepositoryEntryLifecycle;

/**
 * 
 * Initial date: 19 févr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public record CopyElementDetailsResourcesRow(RepositoryEntry entry, long numOfEvents, CopyResources copySetting)  {
	
	public String displayName() {
		return entry.getDisplayname();
	}
	
	public String externalId() {
		return entry.getExternalId();
	}
	
	public String externalRef() {
		return entry.getExternalRef();
	}
	
	public RepositoryEntryStatusEnum entryStatus() {
		return entry.getEntryStatus();
	}
	
	public RepositoryEntryLifecycle lifecycle() {
		return entry.getLifecycle();
	}
}

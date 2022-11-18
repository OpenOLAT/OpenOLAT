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
package org.olat.repository.ui;

import java.util.List;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.id.Identity;
import org.olat.repository.RepositoryEntry;

public interface RepositoryEntryReferenceProvider {
	
	public List<String> getResourceTypes();
	
	public String getIconCssClass(RepositoryEntry repositoryEntry);
	
	public EmptyStateConfig getEmptyStateConfig();
	
	public String getSelectionTitle();
	
	public ReferenceContentProvider getReferenceContentProvider();
	
	public boolean isReplaceable(RepositoryEntry repositoryEntry);
	
	public boolean isEditable(RepositoryEntry repositoryEntry, Identity identity);
	
	public boolean canCreate();
	
	public boolean canImport();
	
	public static interface ReferenceContentProvider {
		
		public Component getContent(RepositoryEntry repositoryEntry);
		
		public void refresh(Component cmp, RepositoryEntry repositoryEntry);
		
	}
	
}
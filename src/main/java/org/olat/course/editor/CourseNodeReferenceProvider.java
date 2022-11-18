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
package org.olat.course.editor;

import java.util.List;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.id.Identity;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.RepositoryEntryReferenceProvider;
import org.olat.repository.ui.RepositoyUIFactory;

/**
 * 
 * Initial date: 16 Nov 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CourseNodeReferenceProvider implements RepositoryEntryReferenceProvider {
	
	private final RepositoryService repositoryService;
	private final List<String> resourceTypes;
	private final EmptyStateConfig emptyStateConfig;
	private final String selectionTitle;
	private final ReferenceContentProvider referenceContentProvider;
	
	public CourseNodeReferenceProvider(RepositoryService repositoryService, List<String> resourceTypes,
			EmptyStateConfig emptyStateConfig, String selectionTitle, ReferenceContentProvider referenceContentProvider) {
		this.repositoryService = repositoryService;
		this.resourceTypes = resourceTypes;
		this.emptyStateConfig = emptyStateConfig;
		this.selectionTitle = selectionTitle;
		this.referenceContentProvider = referenceContentProvider;
	}

	@Override
	public List<String> getResourceTypes() {
		return resourceTypes;
	}

	@Override
	public String getIconCssClass(RepositoryEntry repositoryEntry) {
		return "o_icon o_icon-fw " + RepositoyUIFactory.getIconCssClass(repositoryEntry);
	}

	@Override
	public EmptyStateConfig getEmptyStateConfig() {
		return emptyStateConfig;
	}

	@Override
	public String getSelectionTitle() {
		return selectionTitle;
	}

	@Override
	public ReferenceContentProvider getReferenceContentProvider() {
		return referenceContentProvider;
	}

	@Override
	public boolean isReplaceable(RepositoryEntry repositoryEntry) {
		return resourceTypes.contains(repositoryEntry.getOlatResource().getResourceableTypeName());
	}

	@Override
	public boolean isEditable(RepositoryEntry repositoryEntry, Identity identity) {
		return resourceTypes.contains(repositoryEntry.getOlatResource().getResourceableTypeName())
				&& repositoryService.hasRoleExpanded(identity, repositoryEntry, OrganisationRoles.administrator.name(),
						OrganisationRoles.learnresourcemanager.name(), GroupRoles.owner.name());
	}

	@Override
	public boolean canCreate() {
		return true;
	}

	@Override
	public boolean canImport() {
		return true;
	}

}

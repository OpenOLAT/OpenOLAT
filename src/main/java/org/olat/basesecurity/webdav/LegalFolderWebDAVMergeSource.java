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
package org.olat.basesecurity.webdav;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.webdav.manager.WebDAVMergeSource;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.Organisation;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.MergeSource;
import org.olat.core.util.vfs.NamedContainerImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.callbacks.FullAccessCallback;
import org.olat.core.util.vfs.callbacks.ReadOnlyCallback;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: Feb 24, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class LegalFolderWebDAVMergeSource extends WebDAVMergeSource {

	private final IdentityEnvironment identityEnv;
	
	@Autowired
	private OrganisationService organisationService;

	public LegalFolderWebDAVMergeSource(String name, IdentityEnvironment identityEnv) {
		super(name, identityEnv.getIdentity());
		this.identityEnv = identityEnv;
		CoreSpringFactory.autowireObject(this);
	}

	@Override
	protected List<VFSContainer> loadMergedContainers() {
		List<Organisation> organisations = organisationService.getOrganisations(identityEnv.getIdentity(),
				identityEnv.getRoles(), LegalFolderWebDAVProvider.LEGAL_FOLDER_ROLES);
		
		Map<Long, MergeSource> organisationKeyToContainer = new HashMap<>(organisations.size());
		for (Organisation organisation : organisations) {
			String name = organisation.getDisplayName();
			if (StringHelper.containsNonWhitespace(organisation.getLocation())) {
				name += " - " + organisation.getLocation();
			}
			MergeSource organisationCont = new MergeSource(null, name);
			organisationCont.setIconCSS("o_icon_legal_folder");
			organisationKeyToContainer.put(organisation.getKey(), organisationCont);
			
			VFSContainer legalContainer = organisationService.getLegalContainer(organisation);
			VFSSecurityCallback secCallback = identityEnv.getRoles().hasRole(organisation, OrganisationRoles.administrator)
					? new FullAccessCallback()
					: new ReadOnlyCallback();
			legalContainer.setLocalSecurityCallback(secCallback);
			legalContainer = new NamedContainerImpl("_legal_documents", legalContainer);
			organisationCont.addContainer(legalContainer);
		}
		
		for (Organisation organisation : organisations) {
			if (organisation.getParent() != null) {
				MergeSource parentContainer = organisationKeyToContainer.get(organisation.getParent().getKey());
				if (parentContainer != null) {
					MergeSource organisationContainer = organisationKeyToContainer.get(organisation.getKey());
					if (organisationContainer != null) {
						parentContainer.addContainer(organisationContainer);
					}
				}
			}
		}
		
		return organisationService
				.getOrganisationsNotInherited(identityEnv.getIdentity(), LegalFolderWebDAVProvider.LEGAL_FOLDER_ROLES)
				.stream()
				.distinct()
				.map(organisation -> (VFSContainer)organisationKeyToContainer.get(organisation.getKey()))
				.toList();
	}

}

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

import java.util.Arrays;
import java.util.Locale;

import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.commons.services.webdav.WebDAVProvider;
import org.olat.core.util.UserSession;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.user.ui.organisation.OrganisationOverviewController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: Feb 24, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
@Service
public class LegalFolderWebDAVProvider implements WebDAVProvider {
	
	static final OrganisationRoles[] LEGAL_FOLDER_ROLES = Arrays.asList(OrganisationRoles.managersRoles())
			.stream().filter(role -> OrganisationRoles.author != role)
			.toArray(OrganisationRoles[]::new);
	private static final String MOUNTPOINT = "legaldocuments";
	
	@Autowired
	private OrganisationModule organisationModule;

	@Override
	public String getMountPoint() {
		return MOUNTPOINT;
	}

	@Override
	public int getSortOrder() {
		return 200;
	}

	@Override
	public String getIconCss() {
		return "o_icon_legal_folder";
	}

	@Override
	public String getName(Locale locale) {
		return Util.createPackageTranslator(OrganisationOverviewController.class, locale).translate("webdav.legal.folder");
	}

	@Override
	public boolean hasAccess(UserSession usess) {
		return organisationModule.isEnabled() && organisationModule.isLegalFolderEnabled()
				&& usess != null && usess.getRoles() != null
				&& usess.getRoles().hasSomeRoles(LEGAL_FOLDER_ROLES);
	}

	@Override
	public VFSContainer getContainer(UserSession usess) {
		return new LegalFolderWebDAVMergeSource("legalfolder", usess.getIdentityEnvironment());
	}

}

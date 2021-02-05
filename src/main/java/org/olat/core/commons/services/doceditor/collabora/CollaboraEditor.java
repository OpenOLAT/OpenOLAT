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
package org.olat.core.commons.services.doceditor.collabora;

import java.util.Locale;

import org.olat.core.commons.services.doceditor.Access;
import org.olat.core.commons.services.doceditor.DocEditor;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorIdentityService;
import org.olat.core.commons.services.doceditor.collabora.ui.CollaboraEditorController;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSLeaf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 18 Mar 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class CollaboraEditor implements DocEditor {

	public static final String TYPE = "collabora";
	
	@Autowired
	private CollaboraModule collaboraModule;
	@Autowired
	private CollaboraService collaboraService;
	@Autowired
	private DocEditorIdentityService identityService;

	@Override
	public boolean isEnable() {
		return collaboraModule.isEnabled();
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getDisplayName(Locale locale) {
		Translator translator = Util.createPackageTranslator(CollaboraEditorController.class, locale);
		return translator.translate("editor.display.name");
	}

	@Override
	public int getPriority() {
		return 50;
	}

	@Override
	public boolean isEditEnabled() {
		return true;
	}

	@Override
	public boolean isCollaborative() {
		return true;
	}

	@Override
	public boolean isDataTransferConfirmationEnabled() {
		return collaboraModule.isDataTransferConfirmationEnabled();
	}

	@Override
	public boolean hasDocumentBaseUrl() {
		return false;
	}

	@Override
	public String getDocumentBaseUrl() {
		return null;
	}

	@Override
	public boolean isEnabledFor(Identity identity, Roles roles) {
		if (collaboraModule.isUsageRestricted()) {
			if (roles.isAdministrator()) return true;
			if (collaboraModule.isUsageRestrictedToAuthors() && roles.isAuthor()) return true;
			if (collaboraModule.isUsageRestrictedToManagers() && roles.isManager()) return true;
			if (collaboraModule.isUsageRestrictedToCoaches() && identityService.isCoach(identity)) return true;
			return false;
		}
		return true;
	}

	@Override
	public boolean isSupportingFormat(String suffix, Mode mode, boolean metadataAvailable) {
		return metadataAvailable && collaboraService.accepts(suffix, mode);
	}

	@Override
	public boolean isLockedForMe(VFSLeaf vfsLeaf, Identity identity, Mode mode) {
		if (collaboraService.isLockNeeded(mode)) {
			return collaboraService.isLockedForMe(vfsLeaf, identity);
		}
		return false;
	}

	@Override
	public boolean isLockedForMe(VFSLeaf vfsLeaf, VFSMetadata metadata, Identity identity, Mode mode) {
		if (collaboraService.isLockNeeded(mode)) {
			return collaboraService.isLockedForMe(vfsLeaf, metadata, identity);
		}
		return false;
	}

	@Override
	public Controller getRunController(UserRequest ureq, WindowControl wControl, Identity identity, DocEditorConfigs configs,
			Access access) {
		return new CollaboraEditorController(ureq, wControl, configs.getVfsLeaf(), access);
	}

	@Override
	public int getAccessDurationMinutes(Mode mode) {
		return 1440; // Like collaborationExpireAt in VFSLockManagerImpl
	}

}

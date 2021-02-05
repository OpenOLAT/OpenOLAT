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
package org.olat.core.commons.services.doceditor.onlyoffice;

import java.util.Locale;

import org.olat.core.commons.services.doceditor.Access;
import org.olat.core.commons.services.doceditor.DocEditor;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorIdentityService;
import org.olat.core.commons.services.doceditor.onlyoffice.ui.OnlyOfficeEditorController;
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
 * Initial date: 11 Apr 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class OnlyOfficeEditor implements DocEditor {
	
	public static final String TYPE = "onlyoffice";
	
	// Like collaborationExpireAt in VFSLockManagerImpl
	private static final int DURATION_EDIT = 60 * 24;
	// The access is only used to open the editor, but not in the web service.
	private static final int DURATION_VIEW = 60 * 4;
	
	@Autowired
	private OnlyOfficeModule onlyOfficeModule;
	@Autowired
	private OnlyOfficeService onlyOfficeService;
	@Autowired
	private DocEditorIdentityService identityService;

	@Override
	public boolean isEnable() {
		return onlyOfficeModule.isEnabled() && onlyOfficeModule.isEditorEnabled();
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public int getPriority() {
		return 100;
	}

	@Override
	public String getDisplayName(Locale locale) {
		Translator translator = Util.createPackageTranslator(OnlyOfficeEditorController.class, locale);
		return translator.translate("editor.display.name");
	}

	@Override
	public boolean isEditEnabled() {
		Integer licenseEdit = onlyOfficeModule.getLicenseEdit();
		return licenseEdit == null || licenseEdit.intValue() >= 1;
	}

	@Override
	public boolean isCollaborative() {
		return true;
	}

	@Override
	public boolean isDataTransferConfirmationEnabled() {
		return onlyOfficeModule.isDataTransferConfirmationEnabled();
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
		if (onlyOfficeModule.isUsageRestricted()) {
			if (roles.isAdministrator()) return true;
			if (onlyOfficeModule.isUsageRestrictedToAuthors() && roles.isAuthor()) return true;
			if (onlyOfficeModule.isUsageRestrictedToManagers() && roles.isManager()) return true;
			if (onlyOfficeModule.isUsageRestrictedToCoaches() && identityService.isCoach(identity)) return true;
			return false;
		}
		return true;
	}

	@Override
	public boolean isSupportingFormat(String suffix, Mode mode, boolean metadataAvailable) {
		if (Mode.EDIT == mode && !isEditEnabled()) return false;
		
		return metadataAvailable && onlyOfficeService.isSupportedFormat(suffix, mode);
	}

	@Override
	public boolean isLockedForMe(VFSLeaf vfsLeaf, Identity identity, Mode mode) {
		if (onlyOfficeService.isLockNeeded(mode)) {
			return onlyOfficeService.isLockedForMe(vfsLeaf, identity);
		}
		return false;
	}

	@Override
	public boolean isLockedForMe(VFSLeaf vfsLeaf, VFSMetadata metadata, Identity identity, Mode mode) {
		if (onlyOfficeService.isLockNeeded(mode)) {
			return onlyOfficeService.isLockedForMe(vfsLeaf, metadata, identity);
		}
		return false;
	}

	@Override
	public int getAccessDurationMinutes(Mode mode) {
		return Mode.EDIT == mode? DURATION_EDIT: DURATION_VIEW;
	}
	@Override
	public Controller getRunController(UserRequest ureq, WindowControl wControl, Identity identity, DocEditorConfigs configs,
			Access access) {
		return new OnlyOfficeEditorController(ureq, wControl, configs, access);
	}


}

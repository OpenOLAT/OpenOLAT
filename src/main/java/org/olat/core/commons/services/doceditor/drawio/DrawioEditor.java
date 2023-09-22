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
package org.olat.core.commons.services.doceditor.drawio;

import java.util.Locale;

import org.olat.core.commons.services.doceditor.Access;
import org.olat.core.commons.services.doceditor.DocEditor;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorDisplayInfo;
import org.olat.core.commons.services.doceditor.drawio.ui.DrawioEditorController;
import org.olat.core.commons.services.doceditor.model.DefaultEditorInfo;
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
 * Initial date: 21 Jul 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class DrawioEditor implements DocEditor {
	
	public static final String TYPE = "drawio";
	
	// Like exclusiveExpireAt in VFSLockManagerImpl
	private static final int DURATION_EDIT = 60 * 24;
	// The access is only used to open the editor
	private static final int DURATION_VIEW = 60 * 4;
	
	@Autowired
	private DrawioModule drawioModule;
	@Autowired
	private DrawioService drawioService;

	@Override
	public boolean isEnable() {
		return drawioModule.isEnabled();
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public int getPriority() {
		return 1005;
	}

	@Override
	public String getDisplayName(Locale locale) {
		Translator translator = Util.createPackageTranslator(DrawioEditorController.class, locale);
		return translator.translate("editor.display.name");
	}

	@Override
	public boolean isEditEnabled() {
		return drawioModule.isEnabled();
	}

	@Override
	public boolean isCollaborative() {
		return drawioModule.isCollaborationEnabled();
	}
	
	@Override
	public DocEditorDisplayInfo getEditorInfo(Mode mode) {
		return DefaultEditorInfo.get(mode, true);
	}

	@Override
	public boolean isDataTransferConfirmationEnabled() {
		return drawioModule.isDataTransferConfirmationEnabled();
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
		return true;
	}

	@Override
	public boolean isSupportingFormat(String suffix, Mode mode, boolean metadataAvailable) {
		return "drawio".equalsIgnoreCase(suffix) || "dwb".equalsIgnoreCase(suffix) ;
	}

	@Override
	public boolean isLockedForMe(VFSLeaf vfsLeaf, Identity identity, Mode mode) {
		if (drawioService.isLockNeeded(mode)) {
			return drawioService.isLockedForMe(vfsLeaf, identity);
		}
		return false;
	}

	@Override
	public boolean isLockedForMe(VFSLeaf vfsLeaf, VFSMetadata metadata, Identity identity, Mode mode) {
		if (drawioService.isLockNeeded(mode)) {
			return drawioService.isLockedForMe(vfsLeaf, metadata, identity);
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
		return new DrawioEditorController(ureq, wControl, configs, access);
	}

}

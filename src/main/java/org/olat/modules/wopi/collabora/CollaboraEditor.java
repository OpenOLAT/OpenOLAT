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
package org.olat.modules.wopi.collabora;

import java.util.Locale;

import org.olat.core.commons.modules.bc.components.FolderComponent;
import org.olat.core.commons.services.vfs.VFSLeafEditor;
import org.olat.core.commons.services.vfs.VFSLeafEditorSecurityCallback;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.wopi.Access;
import org.olat.modules.wopi.collabora.ui.CollaboraEditorController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 18 Mar 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class CollaboraEditor implements VFSLeafEditor {

	@Autowired
	private CollaboraModule collaboraModule;
	@Autowired
	private CollaboraService collaboraService;

	@Override
	public boolean isEnable() {
		return collaboraModule.isEnabled();
	}

	@Override
	public String getType() {
		return "collabora";
	}

	@Override
	public String getDisplayName(Locale locale) {
		Translator translator = Util.createPackageTranslator(CollaboraEditorController.class, locale);
		return translator.translate("editor.display.name");
	}

	@Override
	public boolean isSupportingFormat(String suffix) {
		return collaboraService.accepts(suffix);
	}

	@Override
	public Controller getRunController(UserRequest ureq, WindowControl wControl, VFSLeaf vfsLeaf,
			FolderComponent folderComponent, Identity identity, VFSLeafEditorSecurityCallback securityCallback) {
		Access access = collaboraService.createAccess(vfsLeaf.getMetaInfo(), identity, securityCallback);
		return new CollaboraEditorController(ureq, wControl, access);
	}

}

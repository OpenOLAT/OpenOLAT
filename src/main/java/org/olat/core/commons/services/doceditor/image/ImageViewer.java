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
package org.olat.core.commons.services.doceditor.image;

import java.util.Locale;

import org.olat.core.commons.services.doceditor.Access;
import org.olat.core.commons.services.doceditor.DocEditor;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorDisplayInfo;
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
import org.olat.modules.video.ui.VideoRuntimeController;
import org.springframework.stereotype.Service;

/**
 * Initial date: 23 Mar 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ImageViewer implements DocEditor {

	private static final String TYPE = "image";
	private static final String FORMAT_JPG = "jpg";
	private static final String FORMAT_JPEG = "jpeg";
	private static final String FORMAT_PNG = "png";
	private static final String FORMAT_GIF = "gif";

	@Override
	public boolean isEnable() {
		return true;
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getDisplayName(Locale locale) {
		Translator translator = Util.createPackageTranslator(VideoRuntimeController.class, locale);
		return translator.translate("viewer.display.name");
	}

	@Override
	public int getPriority() {
		return 5;
	}

	@Override
	public boolean isEditEnabled() {
		// this is only a player
		return false;
	}

	@Override
	public boolean isCollaborative() {
		return false;
	}

	@Override
	public boolean isDataTransferConfirmationEnabled() {
		return false;
	}
	
	@Override
	public DocEditorDisplayInfo getEditorInfo(Mode mode) {
		return DefaultEditorInfo.get(mode, false);
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
		if (Mode.EDIT == mode) return false;
		if (FORMAT_JPG.equalsIgnoreCase(suffix)) return true;
		if (FORMAT_JPEG.equalsIgnoreCase(suffix)) return true;
		if (FORMAT_PNG.equalsIgnoreCase(suffix)) return true;
		if (FORMAT_GIF.equalsIgnoreCase(suffix)) return true;
		return false;
	}

	@Override
	public boolean isLockedForMe(VFSLeaf vfsLeaf, Identity identity, Mode mode) {
		return false;
	}

	@Override
	public boolean isLockedForMe(VFSLeaf vfsLeaf, VFSMetadata metadata, Identity identity, Mode mode) {
		return false;
	}

	@Override
	public int getAccessDurationMinutes(Mode mode) {
		return 60;
	}

	@Override
	public Controller getRunController(UserRequest ureq, WindowControl wControl, Identity identity,
			DocEditorConfigs configs, Access access) {
		return new ImageViewerController(ureq, wControl, configs, access);
	}

}

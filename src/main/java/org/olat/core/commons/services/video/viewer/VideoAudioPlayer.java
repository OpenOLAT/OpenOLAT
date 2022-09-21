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
package org.olat.core.commons.services.video.viewer;

import java.util.Locale;

import org.olat.core.commons.services.doceditor.Access;
import org.olat.core.commons.services.doceditor.DocEditor;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.video.ui.VideoAudioPlayerController;
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
 * The video and audio player implements a basic media player using the DocEditor
 * interface. There is no editor support, it is only player. 
 * 
 * Initial date: 12 July 2022<br>
 * 
 * @author gnaegi@frentix.com, https://www.frentix.com
 *
 */
@Service
public class VideoAudioPlayer implements DocEditor {

	public static final String TYPE = "mediaelement";
	// video types
	public static final String FORMAT_MP4 = "mp4";
	public static final String FORMAT_M4V = "m4v";
	public static final String FORMAT_MPG = "mpg";
	public static final String FORMAT_MOV = "mov";
	public static final String FORMAT_WEBM = "webm";
	// audio types
	public static final String FORMAT_MP3 = "mp3";
	public static final String FORMAT_M4A = "m4a";
	public static final String FORMAT_WAV = "wav";
	public static final String FORMAT_OGG = "ogg";
	public static final String FORMAT_WEBA = "weba";

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
		return 1;
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
		if (FORMAT_MP4.equalsIgnoreCase(suffix)) return true;
		if (FORMAT_M4V.equalsIgnoreCase(suffix)) return true;
		if (FORMAT_MOV.equalsIgnoreCase(suffix)) return true;
		if (FORMAT_MPG.equalsIgnoreCase(suffix)) return true;		
		if (FORMAT_MP3.equalsIgnoreCase(suffix)) return true;
		if (FORMAT_M4A.equalsIgnoreCase(suffix)) return true;
		if (FORMAT_WAV.equalsIgnoreCase(suffix)) return true;
		if (FORMAT_WEBM.equalsIgnoreCase(suffix)) return true;
		if (FORMAT_OGG.equalsIgnoreCase(suffix)) return true;
		if (FORMAT_WEBA.equalsIgnoreCase(suffix)) return true;
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
		return 120;
	}

	@Override
	public Controller getRunController(UserRequest ureq, WindowControl wControl, Identity identity,
			DocEditorConfigs configs, Access access) {
		return new VideoAudioPlayerController(ureq, wControl, configs, access);
	}

}

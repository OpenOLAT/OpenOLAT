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
package org.olat.core.commons.services.video.viewer;

import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocEditorDisplayInfo;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 6 Jul 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class VideoPlayerInfo implements DocEditorDisplayInfo {
	
	private static final VideoPlayerInfo EDIT = new VideoPlayerInfo(Mode.EDIT);
	private static final VideoPlayerInfo VIEW = new VideoPlayerInfo(Mode.VIEW);
	
	private final Mode mode;
	
	public static final VideoPlayerInfo get(Mode mode) {
		return Mode.EDIT == mode? EDIT: VIEW;
	}
	
	private VideoPlayerInfo(Mode mode) {
		this.mode = mode;
	}

	@Override
	public Mode getMode() {
		return mode;
	}

	@Override
	public boolean isEditorAvailable() {
		return true;
	}

	@Override
	public String getModeIcon() {
		return Mode.EDIT == mode? "o_icon_edit": "o_icon_video_play";
	}

	@Override
	public String getModeButtonLabel(Translator translator) {
		return Mode.EDIT == mode
				? translator.translate("edit.button")
				: translator.translate("play.button");
	}

	@Override
	public boolean isNewWindow() {
		return false;
	}

}

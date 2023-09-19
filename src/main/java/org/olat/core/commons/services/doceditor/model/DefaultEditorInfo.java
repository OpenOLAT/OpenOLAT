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
package org.olat.core.commons.services.doceditor.model;

import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocEditorDisplayInfo;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 6 Jul 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DefaultEditorInfo implements DocEditorDisplayInfo {
	
	private static final DefaultEditorInfo EDIT_SAME_WINDOW = new DefaultEditorInfo(Mode.EDIT, false);
	private static final DefaultEditorInfo VIEW_SAME_WINDOW = new DefaultEditorInfo(Mode.VIEW, false);
	private static final DefaultEditorInfo EMBEDDED_SAME_WINDOW = new DefaultEditorInfo(Mode.EMBEDDED, false);
	private static final DefaultEditorInfo EDIT_NEW_WINDOW = new DefaultEditorInfo(Mode.EDIT, true);
	private static final DefaultEditorInfo VIEW_NEW_WINDOW = new DefaultEditorInfo(Mode.VIEW, true);
	private static final DefaultEditorInfo EMBEDDED_NEW_WINDOW = new DefaultEditorInfo(Mode.EMBEDDED, true);
	
	public static final DefaultEditorInfo get(Mode mode, boolean newWindow) {
		return newWindow
				? switch (mode) {
					case EDIT -> EDIT_NEW_WINDOW;
					case VIEW -> VIEW_NEW_WINDOW;
					case EMBEDDED -> EMBEDDED_NEW_WINDOW;
					}
				: switch (mode) {
					case EDIT -> EDIT_SAME_WINDOW;
					case VIEW -> VIEW_SAME_WINDOW;
					case EMBEDDED -> EMBEDDED_SAME_WINDOW;
				};
	}
	
	private final Mode mode;
	private final boolean newWindow;
	
	private DefaultEditorInfo(Mode mode, boolean newWindow) {
		this.mode = mode;
		this.newWindow = newWindow;
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
		return Mode.EDIT == mode? "o_icon_edit": "o_icon_preview";
	}

	@Override
	public String getModeButtonLabel(Translator translator) {
		return Mode.EDIT == mode
				? translator.translate("edit.button")
				: translator.translate("open.button");
	}

	@Override
	public boolean isNewWindow() {
		return newWindow;
	}

}

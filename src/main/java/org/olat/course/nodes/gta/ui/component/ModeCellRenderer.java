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
package org.olat.course.nodes.gta.ui.component;

import org.apache.commons.lang3.tuple.Pair;
import org.olat.core.commons.services.doceditor.DocEditor;
import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.commons.services.video.viewer.VideoAudioPlayer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.FileUtils;
import org.olat.course.nodes.gta.GTAManager;

import java.util.Optional;

public class ModeCellRenderer extends StaticFlexiCellRenderer {
	public static final String CONVERTING_LINK_PREFIX = "o_converting_link_";

	private final DocEditorService docEditorService;

	public ModeCellRenderer(String action, DocEditorService docEditorService) {
		super("", action, true, true);
		this.docEditorService = docEditorService;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row,
			FlexiTableComponent source, URLBuilder ubu, Translator translator) {
		String icon = null;
		String buttonLabel = null;
		String buttonStyle = null;
		setNewWindow(true);

		if (cellValue instanceof Pair<?, ?>) {
			@SuppressWarnings("unchecked")
			Pair<Mode, String> pair = (Pair<Mode, String>) cellValue;
			Mode mode = pair.getLeft();
			String fileName = pair.getRight();
			if (mode != null && fileName != null) {
				// use the standard icon and translation from the document service for consistency
				icon = docEditorService.getModeIcon(mode, fileName);
				buttonStyle = "btn btn-default btn-xs o_button_ghost";
				buttonLabel = docEditorService.getModeButtonLabel(mode, fileName, translator);
			}
			String suffix = FileUtils.getFileSuffix(fileName);
			Optional<DocEditor> videoAudioPlayer = docEditorService.getEditor(VideoAudioPlayer.TYPE);
			videoAudioPlayer.ifPresent((DocEditor docEditor) -> {
				if (docEditor.isSupportingFormat(suffix, mode, false)) {
					setNewWindow(false);
				}
			});
		} else if (GTAManager.BUSY_VALUE.equals(cellValue)) {
			buttonStyle = "btn btn-default btn-xs o_button_ghost";
			buttonLabel = "<span id=\"" + CONVERTING_LINK_PREFIX + row + "\">" +
					translator.translate("av.converting") + "</span>";
			setNewWindow(false);
		}

		// Set with calculated values or reset using the null values from initialization
		setIconLeftCSS(icon);				
		setLabel(buttonLabel);
		setLinkCSS(buttonStyle);
		super.render(renderer, target, cellValue, row, source, ubu, translator);
	}

}
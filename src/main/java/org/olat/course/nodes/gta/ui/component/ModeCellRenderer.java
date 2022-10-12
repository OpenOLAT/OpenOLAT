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
import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.course.nodes.gta.GTAManager;

public class ModeCellRenderer extends StaticFlexiCellRenderer {

	private DocEditorService docEditorService;

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
		} else if (GTAManager.BUSY_VALUE.equals(cellValue)) {
			icon = "o_icon o_icon_busy o_icon-spin";
			setNewWindow(false);
		}

		// Set with calculated values or reset using the null values from initialization
		setIconLeftCSS(icon);				
		setLabel(buttonLabel);
		setLinkCSS(buttonStyle);
		super.render(renderer, target, cellValue, row, source, ubu, translator);
	}

}